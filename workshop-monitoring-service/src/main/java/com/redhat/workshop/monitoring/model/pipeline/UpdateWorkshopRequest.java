package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for updating existing workshop repository (Workshop Maintenance)
 * Supports human-in-the-loop approval for workshop content updates
 */
public class UpdateWorkshopRequest {

    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("updated_content")
    private String updatedContent;

    @JsonProperty("update_type")
    private String updateType = "content-update";

    @JsonProperty("gitea_url")
    private String giteaUrl;

    @JsonProperty("branch_name")
    private String branchName = "main";

    @JsonProperty("commit_message")
    private String commitMessage;

    @JsonProperty("require_approval")
    private boolean requireApproval = true;

    @JsonProperty("approver")
    private String approver;

    @JsonProperty("change_summary")
    private String changeSummary;

    // Default constructor
    public UpdateWorkshopRequest() {}

    // Constructor with required fields
    public UpdateWorkshopRequest(String repositoryName, String workshopName, String updatedContent) {
        this.repositoryName = repositoryName;
        this.workshopName = workshopName;
        this.updatedContent = updatedContent;
    }

    // Getters and Setters
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getUpdatedContent() {
        return updatedContent;
    }

    public void setUpdatedContent(String updatedContent) {
        this.updatedContent = updatedContent;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getGiteaUrl() {
        return giteaUrl;
    }

    public void setGiteaUrl(String giteaUrl) {
        this.giteaUrl = giteaUrl;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public boolean isRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    @Override
    public String toString() {
        return "UpdateWorkshopRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", workshopName='" + workshopName + '\'' +
                ", updateType='" + updateType + '\'' +
                ", branchName='" + branchName + '\'' +
                ", requireApproval=" + requireApproval +
                ", approver='" + approver + '\'' +
                '}';
    }
}
