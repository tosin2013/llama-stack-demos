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
