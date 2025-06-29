package com.redhat.workshop.monitoring.resource;

import com.redhat.workshop.monitoring.model.ApprovalRequest;
import com.redhat.workshop.monitoring.model.ApprovalDecision;
import com.redhat.workshop.monitoring.service.ApprovalService;
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
 * REST API endpoints for human approval workflows in the Workshop Template System.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration.
 * 
 * @author Workshop Template System Team
 */
@Path("/api/approvals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Approvals", description = "Human-in-the-Loop Approval Workflow APIs")
public class ApprovalResource {

    private static final Logger LOG = Logger.getLogger(ApprovalResource.class);

    @Inject
    ApprovalService approvalService;

    /**
     * Submit content for human approval
     */
    @POST
    @Path("/submit")
    @Operation(summary = "Submit Content for Approval", 
               description = "Submit workshop content, decisions, or workflows for human review and approval")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Approval request submitted successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalRequest.class))),
        @APIResponse(responseCode = "400", description = "Invalid approval request"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response submitApproval(ApprovalRequest request) {
        try {
            LOG.infof("Submitting approval request: type=%s, requester=%s", 
                     request.getType(), request.getRequester());
            
            ApprovalRequest submittedRequest = approvalService.submitApproval(request);
            
            LOG.infof("Approval submitted successfully: id=%s", submittedRequest.getApprovalId());
            
            return Response.status(Response.Status.CREATED)
                          .entity(submittedRequest)
                          .build();
                          
        } catch (IllegalArgumentException e) {
            LOG.warnf("Invalid approval request: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(Map.of("error", "Invalid approval request", 
                                       "message", e.getMessage()))
                          .build();
        } catch (Exception e) {
            LOG.errorf("Error submitting approval: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to submit approval", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get all pending approvals
     */
    @GET
    @Path("/pending")
    @Operation(summary = "Get Pending Approvals", 
               description = "Retrieve all pending approval requests in the review queue")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Pending approvals retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getPendingApprovals(
            @QueryParam("type") String approvalType,
            @QueryParam("priority") String priority,
            @QueryParam("reviewer") String reviewer) {
        try {
            LOG.debug("Getting pending approvals");
            
            List<ApprovalRequest> pendingApprovals = approvalService.getPendingApprovals(
                approvalType, priority, reviewer);
            
            LOG.debugf("Retrieved %d pending approvals", pendingApprovals.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("pending_approvals", pendingApprovals);
            response.put("total_count", pendingApprovals.size());
            response.put("last_updated", Instant.now());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting pending approvals: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve pending approvals", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get approval status by ID
     */
    @GET
    @Path("/{approvalId}/status")
    @Operation(summary = "Get Approval Status", 
               description = "Get the current status of a specific approval request")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Approval status retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Approval not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getApprovalStatus(@PathParam("approvalId") String approvalId) {
        try {
            LOG.debugf("Getting approval status: %s", approvalId);
            
            ApprovalRequest approval = approvalService.getApprovalById(approvalId);
            
            if (approval == null) {
                LOG.warnf("Approval not found: %s", approvalId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Approval not found", 
                                           "approval_id", approvalId))
                              .build();
            }
            
            Map<String, Object> status = new HashMap<>();
            status.put("approval_id", approval.getApprovalId());
            status.put("status", approval.getStatus());
            status.put("type", approval.getType());
            status.put("priority", approval.getPriority());
            status.put("created_at", approval.getCreatedAt());
            status.put("timeout_at", approval.getTimeoutAt());
            status.put("assigned_reviewer", approval.getAssignedReviewer());
            status.put("last_updated", approval.getLastUpdated());
            
            LOG.debugf("Approval status: %s - %s", approvalId, approval.getStatus());
            
            return Response.ok(status).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting approval status for %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve approval status", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Approve a request
     */
    @POST
    @Path("/{approvalId}/approve")
    @Operation(summary = "Approve Request", 
               description = "Approve a pending approval request")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Request approved successfully"),
        @APIResponse(responseCode = "404", description = "Approval not found"),
        @APIResponse(responseCode = "400", description = "Invalid approval state"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response approveRequest(@PathParam("approvalId") String approvalId, ApprovalDecision decision) {
        try {
            LOG.infof("Approving request: %s by %s", approvalId, decision.getReviewer());
            
            ApprovalRequest updatedApproval = approvalService.approveRequest(approvalId, decision);
            
            if (updatedApproval == null) {
                LOG.warnf("Approval not found for approval: %s", approvalId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Approval not found", 
                                           "approval_id", approvalId))
                              .build();
            }
            
            LOG.infof("Request approved successfully: %s", approvalId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request approved successfully");
            response.put("approval_id", approvalId);
            response.put("approved_by", decision.getReviewer());
            response.put("approved_at", updatedApproval.getLastUpdated());
            response.put("status", updatedApproval.getStatus());
            
            return Response.ok(response).build();
            
        } catch (IllegalStateException e) {
            LOG.warnf("Invalid approval state for %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(Map.of("error", "Invalid approval state", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        } catch (Exception e) {
            LOG.errorf("Error approving request %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to approve request", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Reject a request
     */
    @POST
    @Path("/{approvalId}/reject")
    @Operation(summary = "Reject Request", 
               description = "Reject a pending approval request with feedback")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Request rejected successfully"),
        @APIResponse(responseCode = "404", description = "Approval not found"),
        @APIResponse(responseCode = "400", description = "Invalid approval state"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response rejectRequest(@PathParam("approvalId") String approvalId, ApprovalDecision decision) {
        try {
            LOG.infof("Rejecting request: %s by %s", approvalId, decision.getReviewer());
            
            ApprovalRequest updatedApproval = approvalService.rejectRequest(approvalId, decision);
            
            if (updatedApproval == null) {
                LOG.warnf("Approval not found for rejection: %s", approvalId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Approval not found", 
                                           "approval_id", approvalId))
                              .build();
            }
            
            LOG.infof("Request rejected successfully: %s", approvalId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request rejected successfully");
            response.put("approval_id", approvalId);
            response.put("rejected_by", decision.getReviewer());
            response.put("rejected_at", updatedApproval.getLastUpdated());
            response.put("status", updatedApproval.getStatus());
            response.put("feedback", decision.getComments());
            
            return Response.ok(response).build();
            
        } catch (IllegalStateException e) {
            LOG.warnf("Invalid approval state for rejection %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity(Map.of("error", "Invalid approval state", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        } catch (Exception e) {
            LOG.errorf("Error rejecting request %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to reject request", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get approval details by ID
     */
    @GET
    @Path("/{approvalId}")
    @Operation(summary = "Get Approval Details", 
               description = "Get complete details of a specific approval request")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Approval details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalRequest.class))),
        @APIResponse(responseCode = "404", description = "Approval not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getApprovalDetails(@PathParam("approvalId") String approvalId) {
        try {
            LOG.debugf("Getting approval details: %s", approvalId);
            
            ApprovalRequest approval = approvalService.getApprovalById(approvalId);
            
            if (approval == null) {
                LOG.warnf("Approval not found: %s", approvalId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Approval not found", 
                                           "approval_id", approvalId))
                              .build();
            }
            
            LOG.debugf("Approval details retrieved: %s", approvalId);
            
            return Response.ok(approval).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting approval details for %s: %s", approvalId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve approval details", 
                                       "approval_id", approvalId,
                                       "message", e.getMessage()))
                          .build();
        }
    }
}
