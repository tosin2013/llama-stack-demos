package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.redhat.workshop.monitoring.model.ApprovalRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Pipeline Approval Request Model
 * Bridges Tekton pipeline approval requests with existing ApprovalService infrastructure
 * Implements ADR-0002: Human-in-the-Loop Agent Integration
 */
@RegisterForReflection
public class PipelineApprovalRequest {

    @JsonProperty("approval_type")
    @NotBlank(message = "Approval type is required")
    private String approvalType;

    @JsonProperty("workflow_id")
    @NotBlank(message = "Workflow ID is required")
    private String workflowId;

    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("approval_data")
    private Map<String, Object> approvalData;

    @JsonProperty("approver")
    private String approver;

    @JsonProperty("timeout_minutes")
    private Integer timeoutMinutes;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("context")
    private String context;

    @JsonProperty("requester")
    private String requester;

    // Default constructor
    public PipelineApprovalRequest() {
        this.approvalData = new HashMap<>();
        this.priority = "normal";
        this.timeoutMinutes = 480; // 8 hours default
        this.requester = "tekton-pipeline";
    }

    // Constructor with required fields
    public PipelineApprovalRequest(String approvalType, String workflowId) {
        this();
        this.approvalType = approvalType;
        this.workflowId = workflowId;
    }

    /**
     * Transform to ApprovalRequest for existing ApprovalService
     */
    public ApprovalRequest toApprovalRequest() {
        ApprovalRequest approval = new ApprovalRequest();
        
        approval.setType(this.approvalType);
        approval.setName(generateApprovalName());
        approval.setDescription(generateApprovalDescription());
        approval.setContent(this.approvalData);
        approval.setPriority(this.priority);
        approval.setRequester(this.requester);
        approval.setContext(this.context);
        approval.setAssignedReviewer(this.approver);
        
        if (this.timeoutMinutes != null) {
            approval.setTimeoutHours(this.timeoutMinutes / 60.0);
        }
        
        return approval;
    }

    private String generateApprovalName() {
        if (this.workshopName != null) {
            return String.format("%s: %s", this.approvalType.replace("_", " ").toUpperCase(), this.workshopName);
        }
        return String.format("%s: %s", this.approvalType.replace("_", " ").toUpperCase(), this.workflowId);
    }

    private String generateApprovalDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Pipeline approval request for ").append(this.approvalType);
        
        if (this.workshopName != null) {
            desc.append(" of workshop '").append(this.workshopName).append("'");
        }
        
        if (this.repositoryUrl != null) {
            desc.append(" from repository: ").append(this.repositoryUrl);
        }
        
        desc.append(" (Workflow ID: ").append(this.workflowId).append(")");
        
        return desc.toString();
    }

    // Getters and Setters
    public String getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(String approvalType) {
        this.approvalType = approvalType;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public Map<String, Object> getApprovalData() {
        return approvalData;
    }

    public void setApprovalData(Map<String, Object> approvalData) {
        this.approvalData = approvalData;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    @Override
    public String toString() {
        return String.format("PipelineApprovalRequest{type='%s', workflowId='%s', workshopName='%s'}", 
                           approvalType, workflowId, workshopName);
    }
}
