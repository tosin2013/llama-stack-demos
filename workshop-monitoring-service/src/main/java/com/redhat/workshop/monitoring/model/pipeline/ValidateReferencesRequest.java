package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for validating external references in workshop content
 * Ensures content quality through reference validation
 */
public class ValidateReferencesRequest {

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("reference_types")
    private String referenceTypes = "all"; // all, links, documentation, apis

    @JsonProperty("check_accessibility")
    private boolean checkAccessibility = true;

    @JsonProperty("check_freshness")
    private boolean checkFreshness = true;

    @JsonProperty("quality_scoring")
    private boolean qualityScoring = true;

    @JsonProperty("timeout_seconds")
    private int timeoutSeconds = 30;

    // Default constructor
    public ValidateReferencesRequest() {}

    // Constructor with required fields
    public ValidateReferencesRequest(String workshopName, String workshopContent) {
        this.workshopName = workshopName;
        this.workshopContent = workshopContent;
    }

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

    public String getReferenceTypes() {
        return referenceTypes;
    }

    public void setReferenceTypes(String referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public boolean isCheckAccessibility() {
        return checkAccessibility;
    }

    public void setCheckAccessibility(boolean checkAccessibility) {
        this.checkAccessibility = checkAccessibility;
    }

    public boolean isCheckFreshness() {
        return checkFreshness;
    }

    public void setCheckFreshness(boolean checkFreshness) {
        this.checkFreshness = checkFreshness;
    }

    public boolean isQualityScoring() {
        return qualityScoring;
    }

    public void setQualityScoring(boolean qualityScoring) {
        this.qualityScoring = qualityScoring;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
