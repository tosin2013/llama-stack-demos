package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.AgentStatus;
import com.redhat.workshop.monitoring.model.HealthStatus;
import com.redhat.workshop.monitoring.model.SystemHealth;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Core service responsible for monitoring the health of all workshop agents.
 * Performs scheduled health checks, maintains status cache, and provides
 * system-wide health aggregation.
 * 
 * @author Workshop Monitoring Service
 */
@ApplicationScoped
public class AgentHealthService {

    private static final Logger LOG = Logger.getLogger(AgentHealthService.class);

    @ConfigProperty(name = "workshop.agents.endpoints")
    Map<String, String> agentEndpoints;

    @ConfigProperty(name = "workshop.health.timeout", defaultValue = "5s")
    String healthTimeout;

    @ConfigProperty(name = "workshop.health.retry-attempts", defaultValue = "3")
    int retryAttempts;

    // JAX-RS client for making HTTP calls to agents
    private final Client httpClient;

    // Thread-safe cache for agent status
    private final Map<String, AgentStatus> statusCache = new ConcurrentHashMap<>();

    /**
     * Constructor - initializes the HTTP client with appropriate timeouts
     */
    public AgentHealthService() {
        this.httpClient = ClientBuilder.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    
    // Track last system health calculation
    private volatile SystemHealth lastSystemHealth;
    private volatile Instant lastSystemHealthUpdate = Instant.now();

    /**
     * Scheduled method that checks all agent health every 30 seconds
     */
    @Scheduled(every = "{workshop.health.check-interval}")
    public void checkAllAgents() {
        LOG.info("Starting scheduled health check for all agents");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<String, String> entry : agentEndpoints.entrySet()) {
            String agentName = entry.getKey();
            String endpoint = entry.getValue();
            
            try {
                checkAgentHealth(agentName, endpoint);
                successCount++;
                LOG.debugf("Health check successful for agent: %s", agentName);
            } catch (Exception e) {
                failureCount++;
                LOG.warnf("Health check failed for agent %s: %s", agentName, e.getMessage());
                
                // Update status cache with error information
                AgentStatus errorStatus = statusCache.getOrDefault(agentName, 
                    new AgentStatus(agentName, endpoint));
                errorStatus.setHealth(HealthStatus.UNHEALTHY);
                errorStatus.setErrorMessage(e.getMessage());
                errorStatus.setLastChecked(Instant.now());
                statusCache.put(agentName, errorStatus);
            }
        }

        // Update system health after all checks
        updateSystemHealth();
        
        long duration = System.currentTimeMillis() - startTime;
        LOG.infof("Health check completed in %dms - Success: %d, Failures: %d", 
                 duration, successCount, failureCount);
    }

    /**
     * Checks the health of a specific agent
     */
    private void checkAgentHealth(String agentName, String endpoint) {
        long startTime = System.currentTimeMillis();

        try {
            // Construct the full URL for the agent-card endpoint
            String agentCardUrl = endpoint + "/agent-card";

            // Make HTTP call to the agent
            WebTarget target = httpClient.target(agentCardUrl);
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            long responseTime = System.currentTimeMillis() - startTime;

            if (response.getStatus() == 200) {
                // Parse the JSON response
                @SuppressWarnings("unchecked")
                Map<String, Object> agentCard = response.readEntity(Map.class);

                // Parse the response and create AgentStatus
                AgentStatus status = parseAgentCardResponse(agentName, endpoint, agentCard, responseTime);

                // Update the cache
                statusCache.put(agentName, status);

                LOG.debugf("Agent %s health check completed in %dms", agentName, responseTime);
            } else {
                throw new RuntimeException("HTTP " + response.getStatus() + ": " + response.getStatusInfo().getReasonPhrase());
            }

            response.close();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            LOG.warnf("Failed to check health for agent %s after %dms: %s",
                     agentName, responseTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Parses the agent card response into an AgentStatus object
     */
    private AgentStatus parseAgentCardResponse(String agentName, String endpoint, 
                                             Map<String, Object> agentCard, long responseTime) {
        AgentStatus status = new AgentStatus(agentName, endpoint);
        status.setResponseTimeMs(responseTime);
        status.setLastChecked(Instant.now());
        
        try {
            // Extract information from agent card response
            if (agentCard.containsKey("tools")) {
                @SuppressWarnings("unchecked")
                List<String> tools = (List<String>) agentCard.get("tools");
                status.setAvailableTools(tools);
            }
            
            // Determine health status based on response
            if (responseTime > 5000) {
                status.setHealth(HealthStatus.DEGRADED);
                status.setErrorMessage("High response time: " + responseTime + "ms");
            } else if (responseTime > 10000) {
                status.setHealth(HealthStatus.UNHEALTHY);
                status.setErrorMessage("Very high response time: " + responseTime + "ms");
            } else {
                status.setHealth(HealthStatus.HEALTHY);
            }
            
            // Store additional metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("agent_card", agentCard);
            metadata.put("check_timestamp", Instant.now().toString());
            status.setMetadata(metadata);
            
        } catch (Exception e) {
            LOG.warnf("Error parsing agent card response for %s: %s", agentName, e.getMessage());
            status.setHealth(HealthStatus.DEGRADED);
            status.setErrorMessage("Failed to parse agent response: " + e.getMessage());
        }
        
        return status;
    }

    /**
     * Updates the system health based on current agent statuses
     */
    private void updateSystemHealth() {
        try {
            SystemHealth systemHealth = new SystemHealth(new HashMap<>(statusCache));
            
            // Add any system-wide issues
            List<String> issues = new ArrayList<>();
            for (AgentStatus agent : statusCache.values()) {
                if (agent.getHealth() != HealthStatus.HEALTHY && agent.getErrorMessage() != null) {
                    issues.add(agent.getName() + ": " + agent.getErrorMessage());
                }
            }
            systemHealth.setActiveIssues(issues);
            
            this.lastSystemHealth = systemHealth;
            this.lastSystemHealthUpdate = Instant.now();
            
            LOG.debugf("System health updated: %s (%d healthy, %d degraded, %d unhealthy)", 
                      systemHealth.getOverallStatus(),
                      systemHealth.getHealthyAgents(),
                      systemHealth.getDegradedAgents(),
                      systemHealth.getUnhealthyAgents());
                      
        } catch (Exception e) {
            LOG.errorf("Failed to update system health: %s", e.getMessage());
        }
    }

    /**
     * Gets the current system health
     */
    public SystemHealth getSystemHealth() {
        if (lastSystemHealth == null) {
            // If no health check has been performed yet, return unknown status
            SystemHealth unknownHealth = new SystemHealth();
            unknownHealth.setOverallStatus(HealthStatus.UNKNOWN);
            return unknownHealth;
        }
        return lastSystemHealth;
    }

    /**
     * Gets the status of all agents
     */
    public List<AgentStatus> getAllAgentStatus() {
        return new ArrayList<>(statusCache.values());
    }

    /**
     * Gets the status of a specific agent
     */
    public AgentStatus getAgentStatus(String agentName) {
        return statusCache.get(agentName);
    }

    /**
     * Gets the configured agent endpoints
     */
    public Map<String, String> getAgentEndpoints() {
        return Collections.unmodifiableMap(agentEndpoints);
    }

    /**
     * Manually triggers a health check for all agents (useful for testing)
     */
    public void triggerHealthCheck() {
        LOG.info("Manual health check triggered");
        checkAllAgents();
    }

    /**
     * Gets the timestamp of the last system health update
     */
    public Instant getLastSystemHealthUpdate() {
        return lastSystemHealthUpdate;
    }

    /**
     * Cleanup method to close the HTTP client
     */
    public void destroy() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
