package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

/**
 * Model representing a human decision on an approval request.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration.
 */
@RegisterForReflection
public class ApprovalDecision {

    @JsonProperty("approval_id")
    private String approvalId;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("reviewer")
    private String reviewer;

    @JsonProperty("reviewer_role")
    private String reviewerRole;

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("rationale")
    private String rationale;

    @JsonProperty("compliance_notes")
    private String complianceNotes;

    @JsonProperty("decision_time")
    private Instant decisionTime;

    @JsonProperty("review_duration_minutes")
    private Long reviewDurationMinutes;

    @JsonProperty("quality_score")
    private Integer qualityScore;

    @JsonProperty("confidence_level")
    private String confidenceLevel;

    @JsonProperty("requires_followup")
    private Boolean requiresFollowup;

    @JsonProperty("followup_instructions")
    private String followupInstructions;

    // Default constructor
    public ApprovalDecision() {
        this.decisionTime = Instant.now();
    }

    // Constructor with required fields
    public ApprovalDecision(String approvalId, String decision, String reviewer, String comments) {
        this();
        this.approvalId = approvalId;
        this.decision = decision;
        this.reviewer = reviewer;
        this.comments = comments;
    }

    // Getters and Setters
    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReviewerRole() {
        return reviewerRole;
    }

    public void setReviewerRole(String reviewerRole) {
        this.reviewerRole = reviewerRole;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getComplianceNotes() {
        return complianceNotes;
    }

    public void setComplianceNotes(String complianceNotes) {
        this.complianceNotes = complianceNotes;
    }

    public Instant getDecisionTime() {
        return decisionTime;
    }

    public void setDecisionTime(Instant decisionTime) {
        this.decisionTime = decisionTime;
    }

    public Long getReviewDurationMinutes() {
        return reviewDurationMinutes;
    }

    public void setReviewDurationMinutes(Long reviewDurationMinutes) {
        this.reviewDurationMinutes = reviewDurationMinutes;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public Boolean getRequiresFollowup() {
        return requiresFollowup;
    }

    public void setRequiresFollowup(Boolean requiresFollowup) {
        this.requiresFollowup = requiresFollowup;
    }

    public String getFollowupInstructions() {
        return followupInstructions;
    }

    public void setFollowupInstructions(String followupInstructions) {
        this.followupInstructions = followupInstructions;
    }

    // Utility methods
    public boolean isApproved() {
        return "approved".equalsIgnoreCase(decision);
    }

    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(decision);
    }

    public boolean isEscalated() {
        return "escalated".equalsIgnoreCase(decision);
    }

    public boolean isDeferred() {
        return "deferred".equalsIgnoreCase(decision);
    }

    public boolean hasComments() {
        return comments != null && !comments.trim().isEmpty();
    }

    public boolean hasRationale() {
        return rationale != null && !rationale.trim().isEmpty();
    }

    public boolean hasQualityScore() {
        return qualityScore != null && qualityScore >= 1 && qualityScore <= 10;
    }

    @Override
    public String toString() {
        return String.format("ApprovalDecision{approvalId='%s', decision='%s', reviewer='%s', decisionTime='%s'}", 
                           approvalId, decision, reviewer, decisionTime);
    }
}
