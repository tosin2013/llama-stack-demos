package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request model for updating RAG content with external references
 * Focuses on content quality through external reference integration
 */
public class UpdateRAGContentRequest {

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("external_references")
    private List<ExternalReference> externalReferences;

    @JsonProperty("quality_threshold")
    private Double qualityThreshold = 0.7;

    @JsonProperty("update_mode")
    private String updateMode = "incremental"; // incremental, full, selective

    @JsonProperty("validation_required")
    private boolean validationRequired = true;

    // Default constructor
    public UpdateRAGContentRequest() {}

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

    public List<ExternalReference> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(List<ExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public Double getQualityThreshold() {
        return qualityThreshold;
    }

    public void setQualityThreshold(Double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    public String getUpdateMode() {
        return updateMode;
    }

    public void setUpdateMode(String updateMode) {
        this.updateMode = updateMode;
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(boolean validationRequired) {
        this.validationRequired = validationRequired;
    }

    /**
     * External Reference model for RAG content updates
     */
    public static class ExternalReference {
        @JsonProperty("url")
        private String url;

        @JsonProperty("type")
        private String type; // documentation, tutorial, api-reference, blog-post

        @JsonProperty("authority_score")
        private Double authorityScore;

        @JsonProperty("last_validated")
        private String lastValidated;

        @JsonProperty("content_summary")
        private String contentSummary;

        // Default constructor
        public ExternalReference() {}

        public ExternalReference(String url, String type) {
            this.url = url;
            this.type = type;
        }

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getAuthorityScore() {
            return authorityScore;
        }

        public void setAuthorityScore(Double authorityScore) {
            this.authorityScore = authorityScore;
        }

        public String getLastValidated() {
            return lastValidated;
        }

        public void setLastValidated(String lastValidated) {
            this.lastValidated = lastValidated;
        }

        public String getContentSummary() {
            return contentSummary;
        }

        public void setContentSummary(String contentSummary) {
            this.contentSummary = contentSummary;
        }
    }
}
