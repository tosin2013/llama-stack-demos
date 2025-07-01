package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import com.redhat.workshop.monitoring.model.ApprovalRequest;
import com.redhat.workshop.monitoring.model.ApprovalStatus;
import java.time.Instant;

/**
 * Pipeline Approval Response Model
 * Transforms ApprovalRequest objects to pipeline-compatible response format
 * Implements ADR-0002: Human-in-the-Loop Agent Integration
 */
@RegisterForReflection
public class PipelineApprovalResponse {

    @JsonProperty("approval_id")
    private String approvalId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("next_steps")
    private String nextSteps;

    @JsonProperty("approver")
    private String approver;

    @JsonProperty("workflow_id")
    private String workflowId;

    @JsonProperty("approval_type")
    private String approvalType;

    @JsonProperty("timeout_at")
    private String timeoutAt;

    @JsonProperty("escalation_at")
    private String escalationAt;

    // Default constructor
    public PipelineApprovalResponse() {
    }

    // Constructor with required fields
    public PipelineApprovalResponse(String approvalId, String status) {
        this.approvalId = approvalId;
        this.status = status;
        this.timestamp = Instant.now().toString();
    }

    /**
     * Create from ApprovalRequest for pipeline compatibility
     */
    public static PipelineApprovalResponse fromApprovalRequest(ApprovalRequest approval) {
        PipelineApprovalResponse response = new PipelineApprovalResponse();
        
        response.setApprovalId(approval.getApprovalId());
        response.setStatus(mapStatusToPipelineFormat(approval.getStatus()));
        response.setApprover(approval.getAssignedReviewer());
        response.setComments(approval.getDecisionComments());
        response.setApprovalType(approval.getType());
        
        if (approval.getLastUpdated() != null) {
            response.setTimestamp(approval.getLastUpdated().toString());
        }
        
        if (approval.getTimeoutAt() != null) {
            response.setTimeoutAt(approval.getTimeoutAt().toString());
        }
        
        if (approval.getEscalationAt() != null) {
            response.setEscalationAt(approval.getEscalationAt().toString());
        }
        
        // Set decision based on status
        if (approval.isApproved()) {
            response.setDecision("approved");
            response.setNextSteps("Pipeline can proceed to next stage");
        } else if (approval.isRejected()) {
            response.setDecision("rejected");
            response.setNextSteps("Pipeline execution halted - review comments and resubmit");
        } else if (approval.getStatus() == ApprovalStatus.NEEDS_CHANGES) {
            response.setDecision("needs_changes");
            response.setNextSteps("Address requested changes and resubmit for approval");
        } else {
            response.setDecision("pending");
            response.setNextSteps("Waiting for human reviewer decision");
        }
        
        return response;
    }

    /**
     * Map ApprovalStatus to pipeline-compatible status strings
     */
    private static String mapStatusToPipelineFormat(ApprovalStatus status) {
        switch (status) {
            case PENDING:
                return "pending";
            case IN_REVIEW:
                return "in_review";
            case APPROVED:
                return "approved";
            case REJECTED:
                return "rejected";
            case NEEDS_CHANGES:
                return "needs_changes";
            case ESCALATED:
                return "escalated";
            case TIMEOUT:
                return "timeout";
            default:
                return "unknown";
        }
    }

    // Getters and Setters
    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(String nextSteps) {
        this.nextSteps = nextSteps;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(String approvalType) {
        this.approvalType = approvalType;
    }

    public String getTimeoutAt() {
        return timeoutAt;
    }

    public void setTimeoutAt(String timeoutAt) {
        this.timeoutAt = timeoutAt;
    }

    public String getEscalationAt() {
        return escalationAt;
    }

    public void setEscalationAt(String escalationAt) {
        this.escalationAt = escalationAt;
    }

    @Override
    public String toString() {
        return String.format("PipelineApprovalResponse{id='%s', status='%s', decision='%s'}", 
                           approvalId, status, decision);
    }
}
