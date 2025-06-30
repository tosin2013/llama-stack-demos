package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for enhancing existing workshop content (Workflow 3)
 */
public class EnhanceWorkshopRequest {

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("enhancement_plan")
    private String enhancementPlan;

    @JsonProperty("original_content")
    private String originalContent;

    @JsonProperty("enhancement_type")
    private String enhancementType = "content-update";

    // Default constructor
    public EnhanceWorkshopRequest() {}

    // Getters and Setters
    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getEnhancementPlan() {
        return enhancementPlan;
    }

    public void setEnhancementPlan(String enhancementPlan) {
        this.enhancementPlan = enhancementPlan;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getEnhancementType() {
        return enhancementType;
    }

    public void setEnhancementType(String enhancementType) {
        this.enhancementType = enhancementType;
    }
}
