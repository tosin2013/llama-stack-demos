package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the status of a workshop creation workflow that spans multiple agents.
 * Tracks progress across the 6-agent system for repository analysis and workshop generation.
 * 
 * @author Workshop Monitoring Service
 */
public class WorkflowStatus {
    
    @JsonProperty("workflow_id")
    private String workflowId;
    
    @JsonProperty("repository_url")
    private String repositoryUrl;
    
    @JsonProperty("workflow_type")
    private WorkflowType type;
    
    @JsonProperty("workflow_state")
    private WorkflowState state;
    
    @JsonProperty("agent_progress")
    private Map<String, String> agentProgress;
    
    @JsonProperty("started_at")
    private Instant startedAt;
    
    @JsonProperty("updated_at")
    private Instant updatedAt;
    
    @JsonProperty("completed_at")
    private Instant completedAt;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("current_agent")
    private String currentAgent;
    
    @JsonProperty("progress_percentage")
    private int progressPercentage;
    
    /**
     * Workflow types based on ADR-0001 dual-template strategy
     */
    public enum WorkflowType {
        @JsonProperty("WORKFLOW1")
        WORKFLOW1("New workshop creation using showroom_template_default"),
        
        @JsonProperty("WORKFLOW3") 
        WORKFLOW3("Enhancement of existing workshop structure");
        
        private final String description;
        
        WorkflowType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Workflow execution states
     */
    public enum WorkflowState {
        @JsonProperty("ANALYZING")
        ANALYZING("Repository analysis in progress"),
        
        @JsonProperty("GENERATING")
        GENERATING("Workshop content generation in progress"),
        
        @JsonProperty("DEPLOYING")
        DEPLOYING("Workshop deployment to Gitea in progress"),
        
        @JsonProperty("COMPLETED")
        COMPLETED("Workflow completed successfully"),
        
        @JsonProperty("FAILED")
        FAILED("Workflow failed with errors"),
        
        @JsonProperty("CANCELLED")
        CANCELLED("Workflow was cancelled");
        
        private final String description;
        
        WorkflowState(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Default constructor for JSON deserialization
    public WorkflowStatus() {
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    // Constructor with required fields
    public WorkflowStatus(String workflowId, String repositoryUrl, WorkflowType type) {
        this.workflowId = workflowId;
        this.repositoryUrl = repositoryUrl;
        this.type = type;
        this.state = WorkflowState.ANALYZING;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.progressPercentage = 0;
    }
    
    // Getters and Setters
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
    
    public WorkflowType getType() {
        return type;
    }
    
    public void setType(WorkflowType type) {
        this.type = type;
    }
    
    public WorkflowState getState() {
        return state;
    }
    
    public void setState(WorkflowState state) {
        this.state = state;
        this.updatedAt = Instant.now();
        if (state == WorkflowState.COMPLETED || state == WorkflowState.FAILED || state == WorkflowState.CANCELLED) {
            this.completedAt = Instant.now();
        }
    }
    
    public Map<String, String> getAgentProgress() {
        return agentProgress;
    }
    
    public void setAgentProgress(Map<String, String> agentProgress) {
        this.agentProgress = agentProgress;
        this.updatedAt = Instant.now();
    }
    
    public Instant getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getCurrentAgent() {
        return currentAgent;
    }
    
    public void setCurrentAgent(String currentAgent) {
        this.currentAgent = currentAgent;
        this.updatedAt = Instant.now();
    }
    
    public int getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        this.updatedAt = Instant.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowStatus that = (WorkflowStatus) o;
        return Objects.equals(workflowId, that.workflowId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(workflowId);
    }
    
    @Override
    public String toString() {
        return "WorkflowStatus{" +
                "workflowId='" + workflowId + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", type=" + type +
                ", state=" + state +
                ", currentAgent='" + currentAgent + '\'' +
                ", progressPercentage=" + progressPercentage +
                ", startedAt=" + startedAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
