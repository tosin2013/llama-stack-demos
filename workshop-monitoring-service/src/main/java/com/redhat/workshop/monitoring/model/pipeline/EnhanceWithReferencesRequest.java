package com.redhat.workshop.monitoring.model.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request model for enhancing workshop content with validated external references
 * Improves content quality through strategic reference integration
 */
public class EnhanceWithReferencesRequest {

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("workshop_content")
    private String workshopContent;

    @JsonProperty("validated_references")
    private List<ValidatedReference> validatedReferences;

    @JsonProperty("enhancement_strategy")
    private String enhancementStrategy = "contextual"; // contextual, comprehensive, minimal

    @JsonProperty("quality_threshold")
    private Double qualityThreshold = 0.8;

    @JsonProperty("preserve_original")
    private boolean preserveOriginal = true;

    // Default constructor
    public EnhanceWithReferencesRequest() {}

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

    public List<ValidatedReference> getValidatedReferences() {
        return validatedReferences;
    }

    public void setValidatedReferences(List<ValidatedReference> validatedReferences) {
        this.validatedReferences = validatedReferences;
    }

    public String getEnhancementStrategy() {
        return enhancementStrategy;
    }

    public void setEnhancementStrategy(String enhancementStrategy) {
        this.enhancementStrategy = enhancementStrategy;
    }

    public Double getQualityThreshold() {
        return qualityThreshold;
    }

    public void setQualityThreshold(Double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    public boolean isPreserveOriginal() {
        return preserveOriginal;
    }

    public void setPreserveOriginal(boolean preserveOriginal) {
        this.preserveOriginal = preserveOriginal;
    }

    /**
     * Validated Reference model for content enhancement
     */
    public static class ValidatedReference {
        @JsonProperty("url")
        private String url;

        @JsonProperty("title")
        private String title;

        @JsonProperty("quality_score")
        private Double qualityScore;

        @JsonProperty("relevance_score")
        private Double relevanceScore;

        @JsonProperty("content_type")
        private String contentType;

        @JsonProperty("integration_suggestion")
        private String integrationSuggestion;

        // Default constructor
        public ValidatedReference() {}

        // Getters and Setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Double getQualityScore() {
            return qualityScore;
        }

        public void setQualityScore(Double qualityScore) {
            this.qualityScore = qualityScore;
        }

        public Double getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Double relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getIntegrationSuggestion() {
            return integrationSuggestion;
        }

        public void setIntegrationSuggestion(String integrationSuggestion) {
            this.integrationSuggestion = integrationSuggestion;
        }
    }
}
