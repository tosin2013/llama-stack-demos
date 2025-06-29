package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Model representing content-specific impact analysis.
 */
@RegisterForReflection
public class ContentImpact {

    @JsonProperty("sections_affected")
    private Integer sectionsAffected;

    @JsonProperty("exercises_modified")
    private Integer exercisesModified;

    @JsonProperty("new_content_added")
    private Boolean newContentAdded;

    @JsonProperty("content_removed")
    private Boolean contentRemoved;

    @JsonProperty("learning_objectives_changed")
    private Boolean learningObjectivesChanged;

    @JsonProperty("difficulty_level_change")
    private String difficultyLevelChange; // increased, decreased, unchanged

    @JsonProperty("prerequisite_changes")
    private List<String> prerequisiteChanges;

    @JsonProperty("content_quality_score")
    private Double contentQualityScore; // 0.0 to 10.0

    @JsonProperty("educational_flow_impact")
    private String educationalFlowImpact; // improved, degraded, unchanged

    @JsonProperty("content_consistency_issues")
    private List<String> contentConsistencyIssues;

    @JsonProperty("language_clarity_impact")
    private String languageClarityImpact; // improved, degraded, unchanged

    @JsonProperty("multimedia_changes")
    private Boolean multimediaChanges;

    @JsonProperty("accessibility_impact")
    private String accessibilityImpact; // improved, degraded, unchanged

    // Default constructor
    public ContentImpact() {
        this.sectionsAffected = 0;
        this.exercisesModified = 0;
        this.newContentAdded = false;
        this.contentRemoved = false;
        this.learningObjectivesChanged = false;
        this.multimediaChanges = false;
        this.contentQualityScore = 0.0;
    }

    // Getters and Setters
    public Integer getSectionsAffected() {
        return sectionsAffected;
    }

    public void setSectionsAffected(Integer sectionsAffected) {
        this.sectionsAffected = sectionsAffected;
    }

    public Integer getExercisesModified() {
        return exercisesModified;
    }

    public void setExercisesModified(Integer exercisesModified) {
        this.exercisesModified = exercisesModified;
    }

    public Boolean getNewContentAdded() {
        return newContentAdded;
    }

    public void setNewContentAdded(Boolean newContentAdded) {
        this.newContentAdded = newContentAdded;
    }

    public Boolean getContentRemoved() {
        return contentRemoved;
    }

    public void setContentRemoved(Boolean contentRemoved) {
        this.contentRemoved = contentRemoved;
    }

    public Boolean getLearningObjectivesChanged() {
        return learningObjectivesChanged;
    }

    public void setLearningObjectivesChanged(Boolean learningObjectivesChanged) {
        this.learningObjectivesChanged = learningObjectivesChanged;
    }

    public String getDifficultyLevelChange() {
        return difficultyLevelChange;
    }

    public void setDifficultyLevelChange(String difficultyLevelChange) {
        this.difficultyLevelChange = difficultyLevelChange;
    }

    public List<String> getPrerequisiteChanges() {
        return prerequisiteChanges;
    }

    public void setPrerequisiteChanges(List<String> prerequisiteChanges) {
        this.prerequisiteChanges = prerequisiteChanges;
    }

    public Double getContentQualityScore() {
        return contentQualityScore;
    }

    public void setContentQualityScore(Double contentQualityScore) {
        this.contentQualityScore = contentQualityScore;
    }

    public String getEducationalFlowImpact() {
        return educationalFlowImpact;
    }

    public void setEducationalFlowImpact(String educationalFlowImpact) {
        this.educationalFlowImpact = educationalFlowImpact;
    }

    public List<String> getContentConsistencyIssues() {
        return contentConsistencyIssues;
    }

    public void setContentConsistencyIssues(List<String> contentConsistencyIssues) {
        this.contentConsistencyIssues = contentConsistencyIssues;
    }

    public String getLanguageClarityImpact() {
        return languageClarityImpact;
    }

    public void setLanguageClarityImpact(String languageClarityImpact) {
        this.languageClarityImpact = languageClarityImpact;
    }

    public Boolean getMultimediaChanges() {
        return multimediaChanges;
    }

    public void setMultimediaChanges(Boolean multimediaChanges) {
        this.multimediaChanges = multimediaChanges;
    }

    public String getAccessibilityImpact() {
        return accessibilityImpact;
    }

    public void setAccessibilityImpact(String accessibilityImpact) {
        this.accessibilityImpact = accessibilityImpact;
    }

    // Utility methods
    public boolean hasSignificantChanges() {
        return sectionsAffected > 2 || exercisesModified > 3 || 
               learningObjectivesChanged || contentRemoved;
    }

    public boolean hasQualityImprovements() {
        return "improved".equals(educationalFlowImpact) || 
               "improved".equals(languageClarityImpact) ||
               "improved".equals(accessibilityImpact);
    }

    public boolean hasQualityDegradation() {
        return "degraded".equals(educationalFlowImpact) ||
               "degraded".equals(languageClarityImpact) ||
               "degraded".equals(accessibilityImpact);
    }
}
