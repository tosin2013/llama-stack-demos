package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Model representing impact analysis results for workshop evolution requests.
 * Provides comprehensive assessment of proposed changes and their effects.
 */
@RegisterForReflection
public class ImpactAnalysis {

    @JsonProperty("analysis_id")
    private String analysisId;

    @JsonProperty("evolution_id")
    private String evolutionId;

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("evolution_type")
    private String evolutionType;

    @JsonProperty("analysis_status")
    private AnalysisStatus analysisStatus;

    @JsonProperty("overall_impact_score")
    private Double overallImpactScore; // 0.0 to 10.0 scale

    @JsonProperty("risk_level")
    private RiskLevel riskLevel;

    @JsonProperty("confidence_score")
    private Double confidenceScore; // 0.0 to 1.0 scale

    @JsonProperty("analysis_summary")
    private String analysisSummary;

    @JsonProperty("content_impact")
    private ContentImpact contentImpact;

    @JsonProperty("technical_impact")
    private TechnicalImpact technicalImpact;

    @JsonProperty("learner_impact")
    private LearnerImpact learnerImpact;

    @JsonProperty("dependency_analysis")
    private List<DependencyImpact> dependencyAnalysis;

    @JsonProperty("affected_sections")
    private List<String> affectedSections;

    @JsonProperty("breaking_changes")
    private List<String> breakingChanges;

    @JsonProperty("compatibility_issues")
    private List<String> compatibilityIssues;

    @JsonProperty("recommendations")
    private List<String> recommendations;

    @JsonProperty("mitigation_strategies")
    private List<String> mitigationStrategies;

    @JsonProperty("testing_requirements")
    private List<String> testingRequirements;

    @JsonProperty("rollback_complexity")
    private String rollbackComplexity; // simple, moderate, complex, high-risk

    @JsonProperty("estimated_effort_hours")
    private Integer estimatedEffortHours;

    @JsonProperty("required_expertise")
    private List<String> requiredExpertise;

    @JsonProperty("stakeholder_notifications")
    private List<String> stakeholderNotifications;

    @JsonProperty("analysis_metadata")
    private Map<String, Object> analysisMetadata;

    @JsonProperty("agent_contributions")
    private Map<String, String> agentContributions;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("analyzed_by")
    private String analyzedBy;

    @JsonProperty("review_required")
    private Boolean reviewRequired;

    @JsonProperty("approval_recommendation")
    private String approvalRecommendation; // approve, reject, conditional, needs_review

    // Default constructor
    public ImpactAnalysis() {
        this.analysisStatus = AnalysisStatus.PENDING;
        this.createdAt = Instant.now();
        this.confidenceScore = 0.0;
        this.overallImpactScore = 0.0;
        this.reviewRequired = false;
    }

    // Constructor with required fields
    public ImpactAnalysis(String workshopName, String evolutionType, String evolutionId) {
        this();
        this.workshopName = workshopName;
        this.evolutionType = evolutionType;
        this.evolutionId = evolutionId;
    }

    // Getters and Setters
    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getEvolutionId() {
        return evolutionId;
    }

    public void setEvolutionId(String evolutionId) {
        this.evolutionId = evolutionId;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getEvolutionType() {
        return evolutionType;
    }

    public void setEvolutionType(String evolutionType) {
        this.evolutionType = evolutionType;
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public Double getOverallImpactScore() {
        return overallImpactScore;
    }

    public void setOverallImpactScore(Double overallImpactScore) {
        this.overallImpactScore = overallImpactScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(String analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public ContentImpact getContentImpact() {
        return contentImpact;
    }

    public void setContentImpact(ContentImpact contentImpact) {
        this.contentImpact = contentImpact;
    }

    public TechnicalImpact getTechnicalImpact() {
        return technicalImpact;
    }

    public void setTechnicalImpact(TechnicalImpact technicalImpact) {
        this.technicalImpact = technicalImpact;
    }

    public LearnerImpact getLearnerImpact() {
        return learnerImpact;
    }

    public void setLearnerImpact(LearnerImpact learnerImpact) {
        this.learnerImpact = learnerImpact;
    }

    public List<DependencyImpact> getDependencyAnalysis() {
        return dependencyAnalysis;
    }

    public void setDependencyAnalysis(List<DependencyImpact> dependencyAnalysis) {
        this.dependencyAnalysis = dependencyAnalysis;
    }

    public List<String> getAffectedSections() {
        return affectedSections;
    }

    public void setAffectedSections(List<String> affectedSections) {
        this.affectedSections = affectedSections;
    }

    public List<String> getBreakingChanges() {
        return breakingChanges;
    }

    public void setBreakingChanges(List<String> breakingChanges) {
        this.breakingChanges = breakingChanges;
    }

    public List<String> getCompatibilityIssues() {
        return compatibilityIssues;
    }

    public void setCompatibilityIssues(List<String> compatibilityIssues) {
        this.compatibilityIssues = compatibilityIssues;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getMitigationStrategies() {
        return mitigationStrategies;
    }

    public void setMitigationStrategies(List<String> mitigationStrategies) {
        this.mitigationStrategies = mitigationStrategies;
    }

    public List<String> getTestingRequirements() {
        return testingRequirements;
    }

    public void setTestingRequirements(List<String> testingRequirements) {
        this.testingRequirements = testingRequirements;
    }

    public String getRollbackComplexity() {
        return rollbackComplexity;
    }

    public void setRollbackComplexity(String rollbackComplexity) {
        this.rollbackComplexity = rollbackComplexity;
    }

    public Integer getEstimatedEffortHours() {
        return estimatedEffortHours;
    }

    public void setEstimatedEffortHours(Integer estimatedEffortHours) {
        this.estimatedEffortHours = estimatedEffortHours;
    }

    public List<String> getRequiredExpertise() {
        return requiredExpertise;
    }

    public void setRequiredExpertise(List<String> requiredExpertise) {
        this.requiredExpertise = requiredExpertise;
    }

    public List<String> getStakeholderNotifications() {
        return stakeholderNotifications;
    }

    public void setStakeholderNotifications(List<String> stakeholderNotifications) {
        this.stakeholderNotifications = stakeholderNotifications;
    }

    public Map<String, Object> getAnalysisMetadata() {
        return analysisMetadata;
    }

    public void setAnalysisMetadata(Map<String, Object> analysisMetadata) {
        this.analysisMetadata = analysisMetadata;
    }

    public Map<String, String> getAgentContributions() {
        return agentContributions;
    }

    public void setAgentContributions(Map<String, String> agentContributions) {
        this.agentContributions = agentContributions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getAnalyzedBy() {
        return analyzedBy;
    }

    public void setAnalyzedBy(String analyzedBy) {
        this.analyzedBy = analyzedBy;
    }

    public Boolean getReviewRequired() {
        return reviewRequired;
    }

    public void setReviewRequired(Boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public String getApprovalRecommendation() {
        return approvalRecommendation;
    }

    public void setApprovalRecommendation(String approvalRecommendation) {
        this.approvalRecommendation = approvalRecommendation;
    }

    // Utility methods
    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }

    public boolean hasBreakingChanges() {
        return breakingChanges != null && !breakingChanges.isEmpty();
    }

    public boolean requiresExpertReview() {
        return reviewRequired || isHighRisk() || hasBreakingChanges();
    }

    public long getAnalysisDurationMinutes() {
        if (createdAt == null || completedAt == null) return 0;
        return java.time.Duration.between(createdAt, completedAt).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("ImpactAnalysis{id='%s', workshop='%s', risk='%s', score=%.1f}", 
                           analysisId, workshopName, riskLevel, overallImpactScore);
    }
}
