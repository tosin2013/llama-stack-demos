package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request model for setting up RAG via Workshop Chat Agent
 */
public class SetupRAGRequest {

    
    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("rag_configuration")
    private String ragConfiguration = "default";

    // Default constructor
    public SetupRAGRequest() {}

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

    public String getRagConfiguration() {
        return ragConfiguration;
    }

    public void setRagConfiguration(String ragConfiguration) {
        this.ragConfiguration = ragConfiguration;
    }
}
