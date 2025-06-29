package com.redhat.workshop.monitoring.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.redhat.workshop.monitoring.service.ChatService;
import com.redhat.workshop.monitoring.service.CommandExecutionService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Human Oversight Resource
 * Implements ADR-0004 Human Oversight Domain REST API
 * Provides oversight coordinator status, quality metrics, and workflow coordination
 */
@Path("/api/oversight")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HumanOversightResource {

    private static final Logger LOG = Logger.getLogger(HumanOversightResource.class);

    @ConfigProperty(name = "workshop.agents.endpoints.human_oversight", defaultValue = "http://human-oversight-coordinator:80")
    String humanOversightEndpoint;

    @Inject
    ChatService chatService;

    @Inject
    CommandExecutionService commandExecutionService;

    /**
     * Get Human Oversight Coordinator status
     */
    @GET
    @Path("/coordinator/status")
    public CompletionStage<Response> getCoordinatorStatus() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Fetching Human Oversight Coordinator status");

                // Mock coordinator status - in production this would call the actual coordinator
                Map<String, Object> status = Map.of(
                    "status", "HEALTHY",
                    "activeSessions", 3,
                    "pendingApprovals", 2,
                    "lastActivity", new Date().toInstant().toString(),
                    "version", "1.0.0",
                    "capabilities", Arrays.asList(
                        "workflow_coordination",
                        "quality_assurance", 
                        "compliance_monitoring",
                        "approval_management"
                    )
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", status,
                    "metadata", Map.of(
                        "timestamp", new Date().toInstant().toString(),
                        "source", "human-oversight-coordinator"
                    )
                )).build();

            } catch (Exception e) {
                LOG.error("Error fetching coordinator status", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "COORDINATOR_STATUS_ERROR",
                            "message", "Failed to fetch coordinator status",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Get active workflows requiring oversight
     */
    @GET
    @Path("/workflows/active")
    public CompletionStage<Response> getActiveWorkflows() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Fetching active workflows requiring oversight");

                // Mock active workflows
                List<Map<String, Object>> workflows = Arrays.asList(
                    Map.of(
                        "id", "wf-001",
                        "name", "Repository Analysis Workflow",
                        "type", "repository-to-workshop",
                        "status", "pending_approval",
                        "submittedAt", "2025-06-29T20:15:00Z",
                        "requester", "system",
                        "priority", "medium"
                    ),
                    Map.of(
                        "id", "wf-002", 
                        "name", "Content Validation Workflow",
                        "type", "content-validation",
                        "status", "in_progress",
                        "submittedAt", "2025-06-29T19:45:00Z",
                        "requester", "content-creator",
                        "priority", "high"
                    )
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                        "workflows", workflows,
                        "totalCount", workflows.size(),
                        "pendingApproval", 1,
                        "inProgress", 1
                    )
                )).build();

            } catch (Exception e) {
                LOG.error("Error fetching active workflows", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "WORKFLOWS_FETCH_ERROR",
                            "message", "Failed to fetch active workflows",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Get quality assurance metrics
     */
    @GET
    @Path("/metrics/quality")
    public CompletionStage<Response> getQualityMetrics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Fetching quality assurance metrics");

                // Mock quality metrics
                Map<String, Object> metrics = Map.of(
                    "overallScore", 92,
                    "complianceScore", 95,
                    "approvalEfficiency", 88,
                    "recentEvents", Arrays.asList(
                        Map.of(
                            "type", "compliance_check",
                            "message", "Workshop content compliance verified",
                            "timestamp", "2 hours ago"
                        ),
                        Map.of(
                            "type", "quality_review",
                            "message", "Content quality assessment completed",
                            "timestamp", "4 hours ago"
                        ),
                        Map.of(
                            "type", "approval_processed",
                            "message", "Workflow approval processed successfully",
                            "timestamp", "6 hours ago"
                        )
                    ),
                    "trends", Map.of(
                        "qualityImprovement", "+5%",
                        "approvalSpeed", "+12%",
                        "complianceRate", "stable"
                    )
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", metrics,
                    "metadata", Map.of(
                        "timestamp", new Date().toInstant().toString(),
                        "period", "last_24_hours"
                    )
                )).build();

            } catch (Exception e) {
                LOG.error("Error fetching quality metrics", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "QUALITY_METRICS_ERROR",
                            "message", "Failed to fetch quality metrics",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Submit workflow for approval
     */
    @POST
    @Path("/workflows/{workflowId}/approve")
    public CompletionStage<Response> approveWorkflow(
            @PathParam("workflowId") String workflowId,
            Map<String, Object> approvalRequest) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String comment = (String) approvalRequest.get("comment");
                String approver = (String) approvalRequest.get("approver");
                
                LOG.infof("Processing approval for workflow %s by %s", workflowId, approver);

                Map<String, Object> result = Map.of(
                    "workflowId", workflowId,
                    "status", "approved",
                    "approver", approver != null ? approver : "system",
                    "comment", comment != null ? comment : "Approved via dashboard",
                    "approvedAt", new Date().toInstant().toString(),
                    "nextSteps", "Workflow will proceed to execution phase"
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", result
                )).build();

            } catch (Exception e) {
                LOG.errorf("Error approving workflow %s: %s", workflowId, e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "APPROVAL_ERROR",
                            "message", "Failed to process workflow approval",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Coordinate agent workflow and execute commands
     */
    @POST
    @Path("/coordinate")
    public CompletionStage<Response> coordinateWorkflow(Map<String, Object> coordinationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String action = (String) coordinationRequest.get("action");

                LOG.infof("Coordinating action: %s", action);

                // Handle command execution
                if ("execute_command".equals(action)) {
                    String command = (String) coordinationRequest.get("command");
                    String executor = (String) coordinationRequest.getOrDefault("executor", "system");

                    if (command == null || command.trim().isEmpty()) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of(
                                "success", false,
                                "error", Map.of(
                                    "code", "INVALID_COMMAND",
                                    "message", "Command is required for execute_command action"
                                )
                            )).build();
                    }

                    // Execute command through command service
                    return commandExecutionService.executeCommand(command, executor)
                        .thenApply(commandExecution -> Response.ok(Map.of(
                            "success", true,
                            "data", commandExecution,
                            "metadata", Map.of(
                                "timestamp", new Date().toInstant().toString(),
                                "action", "execute_command"
                            )
                        )).build())
                        .exceptionally(ex -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of(
                                "success", false,
                                "error", Map.of(
                                    "code", "COMMAND_EXECUTION_ERROR",
                                    "message", "Failed to execute command",
                                    "details", ex.getMessage()
                                )
                            )).build())
                        .join();
                }

                // Handle regular workflow coordination
                List<String> agents = (List<String>) coordinationRequest.get("agents");

                Map<String, Object> result = Map.of(
                    "coordinationId", UUID.randomUUID().toString(),
                    "action", action,
                    "agents", agents != null ? agents : Arrays.asList(),
                    "status", "initiated",
                    "message", "Workflow coordination initiated successfully",
                    "timestamp", new Date().toInstant().toString()
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", result
                )).build();

            } catch (Exception e) {
                LOG.error("Error coordinating workflow", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "COORDINATION_ERROR",
                            "message", "Failed to coordinate workflow",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Handle chat messages with oversight coordinator
     */
    @POST
    @Path("/chat")
    public CompletionStage<Response> handleChatMessage(Map<String, Object> chatRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String message = (String) chatRequest.get("message");
                String sessionId = (String) chatRequest.getOrDefault("sessionId", UUID.randomUUID().toString());

                LOG.infof("Processing chat message for session %s: %s", sessionId, message);

                if (message == null || message.trim().isEmpty()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of(
                            "success", false,
                            "error", Map.of(
                                "code", "INVALID_MESSAGE",
                                "message", "Message content is required"
                            )
                        )).build();
                }

                // Process message through chat service
                return chatService.processMessage(sessionId, message)
                    .thenApply(chatMessage -> Response.ok(Map.of(
                        "success", true,
                        "data", Map.of(
                            "message", chatMessage,
                            "sessionId", sessionId
                        ),
                        "metadata", Map.of(
                            "timestamp", new Date().toInstant().toString(),
                            "responseTime", chatMessage.getResponseTimeMs()
                        )
                    )).build())
                    .exceptionally(ex -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(Map.of(
                            "success", false,
                            "error", Map.of(
                                "code", "CHAT_PROCESSING_ERROR",
                                "message", "Failed to process chat message",
                                "details", ex.getMessage()
                            )
                        )).build())
                    .join();

            } catch (Exception e) {
                LOG.errorf("Error handling chat message: %s", e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "CHAT_ERROR",
                            "message", "Failed to handle chat message",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Reject workflow
     */
    @POST
    @Path("/workflows/{workflowId}/reject")
    public CompletionStage<Response> rejectWorkflow(
            @PathParam("workflowId") String workflowId,
            Map<String, Object> rejectionRequest) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                String comment = (String) rejectionRequest.get("comment");
                String rejector = (String) rejectionRequest.get("approver");

                LOG.infof("Processing rejection for workflow %s by %s", workflowId, rejector);

                Map<String, Object> result = Map.of(
                    "workflowId", workflowId,
                    "status", "rejected",
                    "rejector", rejector != null ? rejector : "system",
                    "comment", comment != null ? comment : "Rejected via dashboard",
                    "rejectedAt", new Date().toInstant().toString(),
                    "nextSteps", "Workflow has been rejected and will not proceed"
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", result
                )).build();

            } catch (Exception e) {
                LOG.errorf("Error rejecting workflow %s: %s", workflowId, e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "REJECTION_ERROR",
                            "message", "Failed to process workflow rejection",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }
}
