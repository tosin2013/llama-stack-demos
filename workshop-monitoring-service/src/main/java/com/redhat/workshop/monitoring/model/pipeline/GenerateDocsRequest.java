package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request model for generating documentation via Documentation Pipeline Agent
 */
public class GenerateDocsRequest {

    
    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("documentation_type")
    private String documentationType = "comprehensive";

    // Default constructor
    public GenerateDocsRequest() {}

    // Getters and Setters
    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getWorkshopContent() {
        return workshopContent;
    }

    public void setWorkshopContent(String workshopContent) {
        this.workshopContent = workshopContent;
    }

    public String getDocumentationType() {
        return documentationType;
    }

    public void setDocumentationType(String documentationType) {
        this.documentationType = documentationType;
    }
}
