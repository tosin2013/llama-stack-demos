package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.redhat.workshop.monitoring.model.ApprovalDecision;
import java.time.Instant;

/**
 * Pipeline Approval Decision Model
 * Handles approval decisions from Tekton pipelines and transforms to ApprovalDecision
 * Implements ADR-0002: Human-in-the-Loop Agent Integration
 */
@RegisterForReflection
public class PipelineApprovalDecision {

    @JsonProperty("decision")
    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "approved|rejected|needs_changes", message = "Decision must be 'approved', 'rejected', or 'needs_changes'")
    private String decision;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("approver")
    @NotBlank(message = "Approver is required")
    private String approver;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("requested_changes")
    private String requestedChanges;

    @JsonProperty("approval_reason")
    private String approvalReason;

    // Default constructor
    public PipelineApprovalDecision() {
        this.timestamp = Instant.now().toString();
    }

    // Constructor with required fields
    public PipelineApprovalDecision(String decision, String approver) {
        this();
        this.decision = decision;
        this.approver = approver;
    }

    /**
     * Transform to ApprovalDecision for existing ApprovalService
     */
    public ApprovalDecision toApprovalDecision() {
        ApprovalDecision approval = new ApprovalDecision();
        
        approval.setReviewer(this.approver);
        approval.setComments(buildDecisionComments());
        
        // Set decision timestamp
        if (this.timestamp != null) {
            try {
                approval.setDecisionTime(Instant.parse(this.timestamp));
            } catch (Exception e) {
                approval.setDecisionTime(Instant.now());
            }
        } else {
            approval.setDecisionTime(Instant.now());
        }
        
        return approval;
    }

    /**
     * Build comprehensive decision comments
     */
    private String buildDecisionComments() {
        StringBuilder comments = new StringBuilder();
        
        comments.append("Decision: ").append(this.decision.toUpperCase());
        
        if (this.comments != null && !this.comments.trim().isEmpty()) {
            comments.append("\nComments: ").append(this.comments);
        }
        
        if ("needs_changes".equals(this.decision) && this.requestedChanges != null) {
            comments.append("\nRequested Changes: ").append(this.requestedChanges);
        }
        
        if ("approved".equals(this.decision) && this.approvalReason != null) {
            comments.append("\nApproval Reason: ").append(this.approvalReason);
        }
        
        comments.append("\nDecision made by: ").append(this.approver);
        comments.append("\nDecision timestamp: ").append(this.timestamp);
        
        return comments.toString();
    }

    /**
     * Check if this is an approval decision
     */
    public boolean isApproved() {
        return "approved".equals(this.decision);
    }

    /**
     * Check if this is a rejection decision
     */
    public boolean isRejected() {
        return "rejected".equals(this.decision);
    }

    /**
     * Check if this requests changes
     */
    public boolean needsChanges() {
        return "needs_changes".equals(this.decision);
    }

    // Getters and Setters
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

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestedChanges() {
        return requestedChanges;
    }

    public void setRequestedChanges(String requestedChanges) {
        this.requestedChanges = requestedChanges;
    }

    public String getApprovalReason() {
        return approvalReason;
    }

    public void setApprovalReason(String approvalReason) {
        this.approvalReason = approvalReason;
    }

    @Override
    public String toString() {
        return String.format("PipelineApprovalDecision{decision='%s', approver='%s', timestamp='%s'}", 
                           decision, approver, timestamp);
    }
}
