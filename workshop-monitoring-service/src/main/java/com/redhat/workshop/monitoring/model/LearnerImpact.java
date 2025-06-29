package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Model representing learner impact analysis.
 */
@RegisterForReflection
public class LearnerImpact {

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

    // Getters and setters
    public Integer getActiveLearnersAffected() {
        return activeLearnersAffected;
    }

    public void setActiveLearnersAffected(Integer activeLearnersAffected) {
        this.activeLearnersAffected = activeLearnersAffected;
    }

    public String getLearningDisruptionLevel() {
        return learningDisruptionLevel;
    }

    public void setLearningDisruptionLevel(String learningDisruptionLevel) {
        this.learningDisruptionLevel = learningDisruptionLevel;
    }

    public String getSkillLevelRequirements() {
        return skillLevelRequirements;
    }

    public void setSkillLevelRequirements(String skillLevelRequirements) {
        this.skillLevelRequirements = skillLevelRequirements;
    }

    public String getTimeInvestmentChange() {
        return timeInvestmentChange;
    }

    public void setTimeInvestmentChange(String timeInvestmentChange) {
        this.timeInvestmentChange = timeInvestmentChange;
    }

    public String getCompletionRateImpact() {
        return completionRateImpact;
    }

    public void setCompletionRateImpact(String completionRateImpact) {
        this.completionRateImpact = completionRateImpact;
    }

    public Boolean getFeedbackIntegration() {
        return feedbackIntegration;
    }

    public void setFeedbackIntegration(Boolean feedbackIntegration) {
        this.feedbackIntegration = feedbackIntegration;
    }

    public Boolean getAccessibilityImprovements() {
        return accessibilityImprovements;
    }

    public void setAccessibilityImprovements(Boolean accessibilityImprovements) {
        this.accessibilityImprovements = accessibilityImprovements;
    }

    public Double getEngagementScoreChange() {
        return engagementScoreChange;
    }

    public void setEngagementScoreChange(Double engagementScoreChange) {
        this.engagementScoreChange = engagementScoreChange;
    }

    public List<String> getSupportRequirements() {
        return supportRequirements;
    }

    public void setSupportRequirements(List<String> supportRequirements) {
        this.supportRequirements = supportRequirements;
    }

    public Boolean getCommunicationNeeded() {
        return communicationNeeded;
    }

    public void setCommunicationNeeded(Boolean communicationNeeded) {
        this.communicationNeeded = communicationNeeded;
    }
}
