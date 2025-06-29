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

/**
 * Model representing technical impact analysis.
 */
@RegisterForReflection
class TechnicalImpact {

    @JsonProperty("technology_stack_changes")
    private List<String> technologyStackChanges;

    @JsonProperty("dependency_updates")
    private List<String> dependencyUpdates;

    @JsonProperty("breaking_api_changes")
    private Boolean breakingApiChanges;

    @JsonProperty("performance_impact")
    private String performanceImpact; // improved, degraded, unchanged

    @JsonProperty("security_implications")
    private List<String> securityImplications;

    @JsonProperty("compatibility_score")
    private Double compatibilityScore; // 0.0 to 10.0

    @JsonProperty("deployment_complexity")
    private String deploymentComplexity; // simple, moderate, complex

    @JsonProperty("infrastructure_changes")
    private Boolean infrastructureChanges;

    @JsonProperty("testing_complexity")
    private String testingComplexity; // simple, moderate, complex

    @JsonProperty("rollback_feasibility")
    private String rollbackFeasibility; // easy, moderate, difficult, risky

    // Default constructor
    public TechnicalImpact() {
        this.breakingApiChanges = false;
        this.infrastructureChanges = false;
        this.compatibilityScore = 10.0;
    }

    // Getters and setters would be here...
    // (Abbreviated for space - full implementation would include all getters/setters)
}

/**
 * Model representing learner impact analysis.
 */
@RegisterForReflection
class LearnerImpact {

    @JsonProperty("active_learners_affected")
    private Integer activeLearnersAffected;

    @JsonProperty("learning_disruption_level")
    private String learningDisruptionLevel; // none, minimal, moderate, significant

    @JsonProperty("skill_level_requirements")
    private String skillLevelRequirements; // beginner, intermediate, advanced

    @JsonProperty("time_investment_change")
    private String timeInvestmentChange; // decreased, unchanged, increased

    @JsonProperty("completion_rate_impact")
    private String completionRateImpact; // improved, unchanged, degraded

    @JsonProperty("feedback_integration")
    private Boolean feedbackIntegration;

    @JsonProperty("accessibility_improvements")
    private Boolean accessibilityImprovements;

    @JsonProperty("engagement_score_change")
    private Double engagementScoreChange; // -10.0 to +10.0

    @JsonProperty("support_requirements")
    private List<String> supportRequirements;

    @JsonProperty("communication_needed")
    private Boolean communicationNeeded;

    // Default constructor
    public LearnerImpact() {
        this.activeLearnersAffected = 0;
        this.feedbackIntegration = false;
        this.accessibilityImprovements = false;
        this.engagementScoreChange = 0.0;
        this.communicationNeeded = false;
    }

    // Getters and setters would be here...
    // (Abbreviated for space - full implementation would include all getters/setters)
}

/**
 * Model representing dependency impact analysis.
 */
@RegisterForReflection
class DependencyImpact {

    @JsonProperty("dependency_name")
    private String dependencyName;

    @JsonProperty("dependency_type")
    private String dependencyType; // internal, external, system

    @JsonProperty("impact_level")
    private String impactLevel; // low, medium, high, critical

    @JsonProperty("change_description")
    private String changeDescription;

    @JsonProperty("mitigation_required")
    private Boolean mitigationRequired;

    @JsonProperty("testing_required")
    private Boolean testingRequired;

    // Default constructor
    public DependencyImpact() {
        this.mitigationRequired = false;
        this.testingRequired = false;
    }

    // Getters and setters would be here...
    // (Abbreviated for space - full implementation would include all getters/setters)
}
