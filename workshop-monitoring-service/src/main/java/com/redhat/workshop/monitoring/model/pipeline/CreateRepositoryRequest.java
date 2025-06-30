package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request model for creating repository via Source Manager Agent
 */
public class CreateRepositoryRequest {

    
    @JsonProperty("repository_name")
    private String repositoryName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("gitea_url")
    private String giteaUrl;

    @JsonProperty("visibility")
    private String visibility = "public";

    // Default constructor
    public CreateRepositoryRequest() {}

    // Getters and Setters
    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getWorkshopContent() {
        return workshopContent;
    }

    public void setWorkshopContent(String workshopContent) {
        this.workshopContent = workshopContent;
    }

    public String getGiteaUrl() {
        return giteaUrl;
    }

    public void setGiteaUrl(String giteaUrl) {
        this.giteaUrl = giteaUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
