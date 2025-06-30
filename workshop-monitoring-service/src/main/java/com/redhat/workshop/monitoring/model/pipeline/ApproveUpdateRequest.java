package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for human approval of workshop updates
 * Used in human-in-the-loop workflow for workshop maintenance
 */
public class ApproveUpdateRequest {

    @JsonProperty("approval_id")
    private String approvalId;

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("approver")
    private String approver;

    @JsonProperty("approval_decision")
    private String approvalDecision; // "approved", "rejected", "needs_changes"

    @JsonProperty("approval_comments")
    private String approvalComments;

    @JsonProperty("requested_changes")
    private String requestedChanges;

    // Default constructor
    public ApproveUpdateRequest() {}

    // Constructor with required fields
    public ApproveUpdateRequest(String approvalId, String repositoryName, String approver, String approvalDecision) {
        this.approvalId = approvalId;
        this.repositoryName = repositoryName;
        this.approver = approver;
        this.approvalDecision = approvalDecision;
    }

    // Getters and Setters
    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApprovalDecision() {
        return approvalDecision;
    }

    public void setApprovalDecision(String approvalDecision) {
        this.approvalDecision = approvalDecision;
    }

    public String getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }

    public String getRequestedChanges() {
        return requestedChanges;
    }

    public void setRequestedChanges(String requestedChanges) {
        this.requestedChanges = requestedChanges;
    }

    @Override
    public String toString() {
        return "ApproveUpdateRequest{" +
                "approvalId='" + approvalId + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", approver='" + approver + '\'' +
                ", approvalDecision='" + approvalDecision + '\'' +
                '}';
    }
}
