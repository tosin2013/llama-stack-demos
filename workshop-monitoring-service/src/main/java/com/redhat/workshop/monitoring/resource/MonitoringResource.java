package com.redhat.workshop.monitoring.resource;

import com.redhat.workshop.monitoring.model.AgentStatus;
import com.redhat.workshop.monitoring.model.SystemHealth;
import com.redhat.workshop.monitoring.service.AgentHealthService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for monitoring workshop agents and system health.
 * Provides comprehensive monitoring data for both web dashboard and frontend integration.
 * 
 * @author Workshop Monitoring Service
 */
@Path("/api/monitoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Monitoring", description = "Workshop Template System Monitoring APIs")
public class MonitoringResource {

    private static final Logger LOG = Logger.getLogger(MonitoringResource.class);

    @Inject
    AgentHealthService agentHealthService;

    /**
     * Gets the overall system health status including all agents
     */
    @GET
    @Path("/health")
    @Operation(summary = "Get System Health", 
               description = "Returns the overall health status of the Workshop Template System including all monitored agents")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "System health retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SystemHealth.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getSystemHealth() {
        try {
            LOG.debug("Getting system health status");
            SystemHealth systemHealth = agentHealthService.getSystemHealth();
            
            LOG.debugf("System health: %s (%d total agents)", 
                      systemHealth.getOverallStatus(), systemHealth.getTotalAgents());
            
            return Response.ok(systemHealth).build();
        } catch (Exception e) {
            LOG.errorf("Error getting system health: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve system health", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Gets the status of all monitored agents
     */
    @GET
    @Path("/agents")
    @Operation(summary = "Get All Agent Status", 
               description = "Returns the current status of all monitored workshop agents")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Agent statuses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AgentStatus.class))),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllAgentStatus() {
        try {
            LOG.debug("Getting all agent statuses");
            List<AgentStatus> agentStatuses = agentHealthService.getAllAgentStatus();
            
            LOG.debugf("Retrieved %d agent statuses", agentStatuses.size());
            
            return Response.ok(agentStatuses).build();
        } catch (Exception e) {
            LOG.errorf("Error getting agent statuses: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve agent statuses", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Gets the status of a specific agent by name
     */
    @GET
    @Path("/agents/{agentName}")
    @Operation(summary = "Get Specific Agent Status", 
               description = "Returns the current status of a specific workshop agent")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Agent status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AgentStatus.class))),
        @APIResponse(responseCode = "404", description = "Agent not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAgentStatus(@PathParam("agentName") String agentName) {
        try {
            LOG.debugf("Getting status for agent: %s", agentName);
            AgentStatus agentStatus = agentHealthService.getAgentStatus(agentName);
            
            if (agentStatus == null) {
                LOG.warnf("Agent not found: %s", agentName);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Agent not found", 
                                           "agentName", agentName))
                              .build();
            }
            
            LOG.debugf("Agent %s status: %s", agentName, agentStatus.getHealth());
            
            return Response.ok(agentStatus).build();
        } catch (Exception e) {
            LOG.errorf("Error getting agent status for %s: %s", agentName, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve agent status", 
                                       "agentName", agentName,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Gets a summary of system status for dashboard display
     */
    @GET
    @Path("/summary")
    @Operation(summary = "Get System Summary", 
               description = "Returns a concise summary of system status for dashboard display")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "System summary retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getSystemSummary() {
        try {
            LOG.debug("Getting system summary");
            SystemHealth systemHealth = agentHealthService.getSystemHealth();
            List<AgentStatus> agentStatuses = agentHealthService.getAllAgentStatus();
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("overall_status", systemHealth.getOverallStatus());
            summary.put("total_agents", systemHealth.getTotalAgents());
            summary.put("healthy_agents", systemHealth.getHealthyAgents());
            summary.put("degraded_agents", systemHealth.getDegradedAgents());
            summary.put("unhealthy_agents", systemHealth.getUnhealthyAgents());
            summary.put("unknown_agents", systemHealth.getUnknownAgents());
            summary.put("last_updated", agentHealthService.getLastSystemHealthUpdate());
            
            // Add agent endpoints configuration
            summary.put("configured_endpoints", agentHealthService.getAgentEndpoints());
            
            // Add quick agent status overview
            Map<String, String> agentOverview = new HashMap<>();
            for (AgentStatus agent : agentStatuses) {
                agentOverview.put(agent.getName(), agent.getHealth().toString());
            }
            summary.put("agent_overview", agentOverview);
            
            LOG.debugf("System summary: %s with %d agents", 
                      systemHealth.getOverallStatus(), systemHealth.getTotalAgents());
            
            return Response.ok(summary).build();
        } catch (Exception e) {
            LOG.errorf("Error getting system summary: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve system summary", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Manually triggers a health check for all agents
     */
    @POST
    @Path("/health-check")
    @Operation(summary = "Trigger Health Check", 
               description = "Manually triggers an immediate health check for all agents")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Health check triggered successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response triggerHealthCheck() {
        try {
            LOG.info("Manual health check triggered via API");
            agentHealthService.triggerHealthCheck();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Health check triggered successfully");
            response.put("timestamp", Instant.now());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.errorf("Error triggering health check: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to trigger health check", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Gets monitoring service metadata and configuration
     */
    @GET
    @Path("/info")
    @Operation(summary = "Get Service Info", 
               description = "Returns monitoring service metadata and configuration information")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Service info retrieved successfully")
    })
    public Response getServiceInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("service_name", "Workshop Monitoring Service");
            info.put("version", "1.0.0-SNAPSHOT");
            info.put("description", "Monitors health and status of Workshop Template System agents");
            info.put("configured_agents", agentHealthService.getAgentEndpoints().keySet());
            info.put("last_health_update", agentHealthService.getLastSystemHealthUpdate());
            info.put("api_endpoints", List.of(
                "/api/monitoring/health",
                "/api/monitoring/agents",
                "/api/monitoring/agents/{agentName}",
                "/api/monitoring/summary",
                "/api/monitoring/health-check",
                "/api/monitoring/info"
            ));
            
            return Response.ok(info).build();
        } catch (Exception e) {
            LOG.errorf("Error getting service info: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve service info", 
                                       "message", e.getMessage()))
                          .build();
        }
    }
}
