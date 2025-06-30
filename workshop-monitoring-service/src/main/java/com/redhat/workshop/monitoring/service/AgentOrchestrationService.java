package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.A2AProtocol;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Agent Orchestration Service
 * Implements ADR-0018 Quarkus Middleware Architecture
 * Handles A2A protocol communication with agents
 */
@ApplicationScoped
public class AgentOrchestrationService {

    private static final Logger LOG = Logger.getLogger(AgentOrchestrationService.class);

    @Inject
    AgentHealthService agentHealthService;

    @ConfigProperty(name = "workshop.agents.timeout", defaultValue = "30s")
    String agentTimeout;

    @ConfigProperty(name = "workshop.agents.retry-attempts", defaultValue = "3")
    int retryAttempts;

    // JAX-RS client for making HTTP calls to agents
    private final Client httpClient;

    public AgentOrchestrationService() {
        this.httpClient = ClientBuilder.newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    /**
     * Invoke an agent tool using A2A protocol
     *
     * @param agentName The name of the agent to call
     * @param toolName The tool to invoke on the agent
     * @param parameters The parameters to pass to the tool
     * @return The response from the agent
     */
    public Map<String, Object> invokeAgent(String agentName, String toolName, Map<String, Object> parameters) {
        LOG.infof("Invoking agent '%s' with tool '%s' using A2A protocol", agentName, toolName);

        // Get agent endpoint from health service
        String agentEndpoint = getAgentEndpoint(agentName);
        if (agentEndpoint == null) {
            throw new RuntimeException("Agent endpoint not found for: " + agentName);
        }

        // Convert tool invocation to A2A protocol request
        A2AProtocol.ToolInvocationRequest toolRequest = new A2AProtocol.ToolInvocationRequest(toolName, parameters);
        String taskId = UUID.randomUUID().toString();
        A2AProtocol.TaskRequest a2aRequest = toolRequest.toA2ATaskRequest(taskId);

        // Call agent with retry logic
        Exception lastException = null;
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                LOG.debugf("Attempt %d/%d: Calling agent %s at %s with task ID %s",
                    attempt, retryAttempts, agentName, agentEndpoint, taskId);

                Map<String, Object> result = callAgentWithA2AProtocol(agentEndpoint, a2aRequest);

                LOG.infof("Successfully called agent '%s' on attempt %d", agentName, attempt);
                return result;

            } catch (Exception e) {
                lastException = e;
                LOG.warnf("Attempt %d/%d failed for agent '%s': %s", attempt, retryAttempts, agentName, e.getMessage());

                if (attempt < retryAttempts) {
                    try {
                        // Exponential backoff: 1s, 2s, 4s
                        long backoffMs = 1000L * (1L << (attempt - 1));
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry backoff", ie);
                    }
                }
            }
        }

        // All attempts failed
        String errorMsg = String.format("Failed to call agent '%s' after %d attempts", agentName, retryAttempts);
        LOG.errorf("%s. Last error: %s", errorMsg, lastException.getMessage());
        throw new RuntimeException(errorMsg, lastException);
    }

    /**
     * Call agent using A2A protocol with /send-task endpoint
     */
    private Map<String, Object> callAgentWithA2AProtocol(String agentEndpoint, A2AProtocol.TaskRequest request) {
        String sendTaskUrl = agentEndpoint + "/send-task";

        try {
            LOG.debugf("Sending A2A task request to %s with task ID %s", sendTaskUrl, request.id);

            WebTarget target = httpClient.target(sendTaskUrl);
            Response response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));

            if (response.getStatus() == 200) {
                A2AProtocol.TaskResponse taskResponse = response.readEntity(A2AProtocol.TaskResponse.class);
                response.close();

                LOG.debugf("Received A2A response for task %s with state: %s",
                    taskResponse.id, taskResponse.result.status.state);

                // Convert A2A response back to legacy format for compatibility
                return convertA2AResponseToLegacyFormat(taskResponse);

            } else {
                String errorBody = response.readEntity(String.class);
                response.close();
                throw new RuntimeException(String.format("Agent returned HTTP %d: %s", response.getStatus(), errorBody));
            }

        } catch (Exception e) {
            LOG.errorf("Failed to call agent at %s: %s", sendTaskUrl, e.getMessage());
            throw new RuntimeException("Agent call failed", e);
        }
    }

    /**
     * Convert A2A protocol response to legacy format for backward compatibility
     */
    private Map<String, Object> convertA2AResponseToLegacyFormat(A2AProtocol.TaskResponse taskResponse) {
        Map<String, Object> legacyResponse = new HashMap<>();

        // Extract status and message
        if (taskResponse.result != null && taskResponse.result.status != null) {
            legacyResponse.put("status", taskResponse.result.status.state);

            // Extract text content from message parts
            if (taskResponse.result.status.message != null &&
                taskResponse.result.status.message.parts != null &&
                !taskResponse.result.status.message.parts.isEmpty()) {

                StringBuilder content = new StringBuilder();
                for (A2AProtocol.MessagePart part : taskResponse.result.status.message.parts) {
                    if ("text".equals(part.type) && part.text != null) {
                        if (content.length() > 0) {
                            content.append("\n");
                        }
                        content.append(part.text);
                    }
                }

                if (content.length() > 0) {
                    legacyResponse.put("result", content.toString());
                } else {
                    legacyResponse.put("result", "Task completed successfully");
                }
            } else {
                legacyResponse.put("result", "Task completed successfully");
            }
        } else {
            legacyResponse.put("status", "unknown");
            legacyResponse.put("result", "No status information available");
        }

        // Add task ID for tracking
        legacyResponse.put("task_id", taskResponse.id);

        // Check for agent-level errors in response
        if ("error".equals(legacyResponse.get("status")) ||
            (legacyResponse.get("result") != null &&
             legacyResponse.get("result").toString().toLowerCase().contains("error"))) {
            throw new RuntimeException("Agent returned error: " + legacyResponse.get("result"));
        }

        return legacyResponse;
    }

    /**
     * Get agent endpoint from configuration
     */
    private String getAgentEndpoint(String agentName) {
        Map<String, String> endpoints = agentHealthService.getAgentEndpoints();
        
        // Map agent names to endpoint keys
        String endpointKey = mapAgentNameToEndpointKey(agentName);
        return endpoints.get(endpointKey);
    }

    /**
     * Map agent names used in pipeline to endpoint configuration keys
     */
    private String mapAgentNameToEndpointKey(String agentName) {
        switch (agentName) {
            case "content-creator":
                return "content-creator";
            case "template-converter":
                return "template-converter";
            case "source-manager":
                return "source-manager";
            case "research-validation":
                return "research-validation";
            case "documentation-pipeline":
                return "documentation-pipeline";
            case "workshop-chat":
                return "workshop-chat";
            default:
                LOG.warnf("Unknown agent name: %s", agentName);
                return agentName;
        }
    }

    /**
     * Check if an agent is healthy before calling
     */
    public boolean isAgentHealthy(String agentName) {
        try {
            return agentHealthService.getAgentStatus(agentName) != null;
        } catch (Exception e) {
            LOG.warnf("Failed to check health for agent '%s': %s", agentName, e.getMessage());
            return false;
        }
    }

    /**
     * Get agent status for debugging
     */
    public Map<String, Object> getAgentStatus(String agentName) {
        try {
            return Map.of(
                "agent_name", agentName,
                "endpoint", getAgentEndpoint(agentName),
                "healthy", isAgentHealthy(agentName),
                "last_check", System.currentTimeMillis()
            );
        } catch (Exception e) {
            return Map.of(
                "agent_name", agentName,
                "error", e.getMessage(),
                "healthy", false
            );
        }
    }

    /**
     * Cleanup resources
     */
    public void destroy() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}
