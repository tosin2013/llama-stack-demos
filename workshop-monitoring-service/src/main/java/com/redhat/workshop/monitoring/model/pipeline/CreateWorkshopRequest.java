package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for creating workshop content via pipeline integration
 * Used by Tekton pipelines to call Content Creator Agent through middleware
 */
public class CreateWorkshopRequest {

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("base_template")
    private String baseTemplate = "showroom_template_default";

    @JsonProperty("target_directory")
    private String targetDirectory;

    @JsonProperty("technology_focus")
    private String technologyFocus;

    @JsonProperty("customization_level")
    private String customizationLevel = "comprehensive";

    // ADR-0001 Dual-Template Strategy fields
    @JsonProperty("workflow_type")
    private String workflowType; // "workflow1" or "workflow3"

    @JsonProperty("repository_classification")
    private String repositoryClassification; // "existing_workshop", "tutorial_content", "application"

    @JsonProperty("auto_detect_workflow")
    private Boolean autoDetectWorkflow = false; // Enable intelligent workflow detection

    @JsonProperty("detected_framework")
    private String detectedFramework; // Framework detected during classification

    // Default constructor
    public CreateWorkshopRequest() {}

    // Constructor with required fields
    public CreateWorkshopRequest(String workshopName, String repositoryUrl) {
        this.workshopName = workshopName;
        this.repositoryUrl = repositoryUrl;
    }

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

    public String getBaseTemplate() {
        return baseTemplate;
    }

    public void setBaseTemplate(String baseTemplate) {
        this.baseTemplate = baseTemplate;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public String getTechnologyFocus() {
        return technologyFocus;
    }

    public void setTechnologyFocus(String technologyFocus) {
        this.technologyFocus = technologyFocus;
    }

    public String getCustomizationLevel() {
        return customizationLevel;
    }

    public void setCustomizationLevel(String customizationLevel) {
        this.customizationLevel = customizationLevel;
    }

    // ADR-0001 Dual-Template Strategy getters and setters
    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getRepositoryClassification() {
        return repositoryClassification;
    }

    public void setRepositoryClassification(String repositoryClassification) {
        this.repositoryClassification = repositoryClassification;
    }

    public Boolean getAutoDetectWorkflow() {
        return autoDetectWorkflow;
    }

    public void setAutoDetectWorkflow(Boolean autoDetectWorkflow) {
        this.autoDetectWorkflow = autoDetectWorkflow;
    }

    public String getDetectedFramework() {
        return detectedFramework;
    }

    public void setDetectedFramework(String detectedFramework) {
        this.detectedFramework = detectedFramework;
    }

    @Override
    public String toString() {
        return "CreateWorkshopRequest{" +
                "workshopName='" + workshopName + '\'' +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ", baseTemplate='" + baseTemplate + '\'' +
                ", targetDirectory='" + targetDirectory + '\'' +
                ", technologyFocus='" + technologyFocus + '\'' +
                ", customizationLevel='" + customizationLevel + '\'' +
                '}';
    }
}
