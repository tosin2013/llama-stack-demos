package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

/**
 * Repository Classification Model
 * Represents the result of repository analysis for ADR-0001 dual-template strategy.
 * Encapsulates classification logic output and provides clear workflow determination.
 */
@RegisterForReflection
public class RepositoryClassification {

    @JsonProperty("classification_type")
    private String classificationType;

    @JsonProperty("detected_framework")
    private String detectedFramework;

    @JsonProperty("recommended_workflow")
    private String recommendedWorkflow;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("indicators")
    private List<String> indicators;

    @JsonProperty("repository_url")
    private String repositoryUrl;

    @JsonProperty("analysis_timestamp")
    private String analysisTimestamp;

    @JsonProperty("workflow_description")
    private String workflowDescription;

    // Default constructor
    public RepositoryClassification() {
        this.indicators = new ArrayList<>();
        this.analysisTimestamp = Instant.now().toString();
    }

    // Full constructor
    public RepositoryClassification(String classificationType, String detectedFramework, 
                                  String recommendedWorkflow, double confidence, 
                                  List<String> indicators, String repositoryUrl) {
        this();
        this.classificationType = classificationType;
        this.detectedFramework = detectedFramework;
        this.recommendedWorkflow = recommendedWorkflow;
        this.confidence = confidence;
        this.indicators = indicators != null ? new ArrayList<>(indicators) : new ArrayList<>();
        this.repositoryUrl = repositoryUrl;
        this.workflowDescription = generateWorkflowDescription();
    }

    /**
     * Check if repository is classified as an existing workshop
     * Existing workshops should use Workflow 3 (Enhancement)
     */
    public boolean isExistingWorkshop() {
        return "existing_workshop".equals(classificationType);
    }

    /**
     * Check if repository is classified as tutorial content
     * Tutorial content should use Workflow 1 (New Workshop Creation)
     */
    public boolean isTutorialContent() {
        return "tutorial_content".equals(classificationType);
    }

    /**
     * Check if repository is classified as an application
     * Applications should use Workflow 1 (New Workshop Creation)
     */
    public boolean isApplication() {
        return "application".equals(classificationType);
    }

    /**
     * Check if repository should use Workflow 3 (Enhancement)
     * Based on ADR-0001 specification
     */
    public boolean shouldUseWorkflow3() {
        return "workflow3".equals(recommendedWorkflow);
    }

    /**
     * Check if repository should use Workflow 1 (New Workshop Creation)
     * Based on ADR-0001 specification
     */
    public boolean shouldUseWorkflow1() {
        return "workflow1".equals(recommendedWorkflow);
    }

    /**
     * Get template source based on workflow type
     * ADR-0001 Template Selection Matrix
     */
    public String getTemplateSource() {
        if (shouldUseWorkflow3()) {
            return "original_repository"; // Clone original workshop
        } else {
            return "showroom_template_default"; // Use default template
        }
    }

    /**
     * Get Gitea strategy based on workflow type
     * ADR-0001 Template Selection Matrix
     */
    public String getGiteaStrategy() {
        if (shouldUseWorkflow3()) {
            return "clone_original_to_gitea"; // Clone original → Gitea
        } else {
            return "clone_template_to_gitea"; // Clone template → Gitea
        }
    }

    /**
     * Check if classification has high confidence
     */
    public boolean hasHighConfidence() {
        return confidence >= 0.8;
    }

    /**
     * Check if classification has medium confidence
     */
    public boolean hasMediumConfidence() {
        return confidence >= 0.6 && confidence < 0.8;
    }

    /**
     * Check if classification has low confidence
     */
    public boolean hasLowConfidence() {
        return confidence < 0.6;
    }

    /**
     * Get confidence level as string
     */
    public String getConfidenceLevel() {
        if (hasHighConfidence()) return "high";
        if (hasMediumConfidence()) return "medium";
        return "low";
    }

    /**
     * Generate workflow description based on classification
     */
    private String generateWorkflowDescription() {
        if (shouldUseWorkflow3()) {
            return String.format("Workflow 3: Enhancement - Clone original %s workshop for modernization", 
                                detectedFramework != null ? detectedFramework : "existing");
        } else {
            return String.format("Workflow 1: New Workshop Creation - Convert %s to workshop using showroom_template_default", 
                                classificationType);
        }
    }

    /**
     * Add classification indicator
     */
    public void addIndicator(String indicator) {
        if (this.indicators == null) {
            this.indicators = new ArrayList<>();
        }
        this.indicators.add(indicator);
    }

    // Getters and Setters
    public String getClassificationType() {
        return classificationType;
    }

    public void setClassificationType(String classificationType) {
        this.classificationType = classificationType;
        this.workflowDescription = generateWorkflowDescription();
    }

    public String getDetectedFramework() {
        return detectedFramework;
    }

    public void setDetectedFramework(String detectedFramework) {
        this.detectedFramework = detectedFramework;
        this.workflowDescription = generateWorkflowDescription();
    }

    public String getRecommendedWorkflow() {
        return recommendedWorkflow;
    }

    public void setRecommendedWorkflow(String recommendedWorkflow) {
        this.recommendedWorkflow = recommendedWorkflow;
        this.workflowDescription = generateWorkflowDescription();
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public void setAnalysisTimestamp(String analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }

    @Override
    public String toString() {
        return String.format("RepositoryClassification{type='%s', framework='%s', workflow='%s', confidence=%.2f}", 
                           classificationType, detectedFramework, recommendedWorkflow, confidence);
    }
}
