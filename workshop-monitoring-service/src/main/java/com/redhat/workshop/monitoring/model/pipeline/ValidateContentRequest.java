package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Request model for validating content via Research Validation Agent
 */
public class ValidateContentRequest {

    
    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("validation_scope")
    private String validationScope = "comprehensive";

    // Default constructor
    public ValidateContentRequest() {}

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

    public String getValidationScope() {
        return validationScope;
    }

    public void setValidationScope(String validationScope) {
        this.validationScope = validationScope;
    }
}
