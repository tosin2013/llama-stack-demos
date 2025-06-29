package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.Map;

/**
 * Model representing a human approval request in the Workshop Template System.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration.
 */
@RegisterForReflection
public class ApprovalRequest {

    @JsonProperty("approval_id")
    private String approvalId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("content")
    private Map<String, Object> content;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("requester")
    private String requester;

    @JsonProperty("context")
    private String context;

    @JsonProperty("required_role")
    private String requiredRole;

    @JsonProperty("timeout_hours")
    private Double timeoutHours;

    @JsonProperty("escalation_hours")
    private Double escalationHours;

    @JsonProperty("status")
    private ApprovalStatus status;

    @JsonProperty("assigned_reviewer")
    private String assignedReviewer;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("timeout_at")
    private Instant timeoutAt;

    @JsonProperty("escalation_at")
    private Instant escalationAt;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    @JsonProperty("decision_time")
    private Instant decisionTime;

    @JsonProperty("decision_comments")
    private String decisionComments;

    @JsonProperty("escalation_reason")
    private String escalationReason;

    @JsonProperty("audit_trail")
    private String auditTrail;

    // Default constructor
    public ApprovalRequest() {
        this.status = ApprovalStatus.PENDING;
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
    }

    // Constructor with required fields
    public ApprovalRequest(String type, String name, String description, 
                          Map<String, Object> content, String requester) {
        this();
        this.type = type;
        this.name = name;
        this.description = description;
        this.content = content;
        this.requester = requester;
    }

    // Getters and Setters
    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public Double getTimeoutHours() {
        return timeoutHours;
    }

    public void setTimeoutHours(Double timeoutHours) {
        this.timeoutHours = timeoutHours;
    }

    public Double getEscalationHours() {
        return escalationHours;
    }

    public void setEscalationHours(Double escalationHours) {
        this.escalationHours = escalationHours;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
        this.lastUpdated = Instant.now();
    }

    public String getAssignedReviewer() {
        return assignedReviewer;
    }

    public void setAssignedReviewer(String assignedReviewer) {
        this.assignedReviewer = assignedReviewer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getTimeoutAt() {
        return timeoutAt;
    }

    public void setTimeoutAt(Instant timeoutAt) {
        this.timeoutAt = timeoutAt;
    }

    public Instant getEscalationAt() {
        return escalationAt;
    }

    public void setEscalationAt(Instant escalationAt) {
        this.escalationAt = escalationAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Instant getDecisionTime() {
        return decisionTime;
    }

    public void setDecisionTime(Instant decisionTime) {
        this.decisionTime = decisionTime;
    }

    public String getDecisionComments() {
        return decisionComments;
    }

    public void setDecisionComments(String decisionComments) {
        this.decisionComments = decisionComments;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public String getAuditTrail() {
        return auditTrail;
    }

    public void setAuditTrail(String auditTrail) {
        this.auditTrail = auditTrail;
    }

    // Utility methods
    public boolean isPending() {
        return status == ApprovalStatus.PENDING;
    }

    public boolean isInReview() {
        return status == ApprovalStatus.IN_REVIEW;
    }

    public boolean isApproved() {
        return status == ApprovalStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == ApprovalStatus.REJECTED;
    }

    public boolean isEscalated() {
        return status == ApprovalStatus.ESCALATED;
    }

    public boolean isTimeout() {
        return status == ApprovalStatus.TIMEOUT;
    }

    public boolean isComplete() {
        return isApproved() || isRejected();
    }

    public boolean isOverdue() {
        return timeoutAt != null && Instant.now().isAfter(timeoutAt) && !isComplete();
    }

    public boolean needsEscalation() {
        return escalationAt != null && Instant.now().isAfter(escalationAt) && !isComplete() && !isEscalated();
    }

    @Override
    public String toString() {
        return String.format("ApprovalRequest{id='%s', type='%s', status='%s', requester='%s'}", 
                           approvalId, type, status, requester);
    }
}
