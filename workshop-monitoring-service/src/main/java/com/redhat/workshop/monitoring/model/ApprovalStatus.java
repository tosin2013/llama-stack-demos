package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Enumeration of approval statuses in the human-in-the-loop workflow.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration.
 */
@RegisterForReflection
public enum ApprovalStatus {
    
    /**
     * Approval request has been submitted and is waiting for reviewer assignment
     */
    PENDING("pending"),
    
    /**
     * Approval request is actively being reviewed by an assigned reviewer
     */
    IN_REVIEW("in_review"),
    
    /**
     * Approval request has been approved by the reviewer
     */
    APPROVED("approved"),
    
    /**
     * Approval request has been rejected by the reviewer
     */
    REJECTED("rejected"),

    /**
     * Approval request requires changes before it can be approved
     */
    NEEDS_CHANGES("needs_changes"),

    /**
     * Approval request has been escalated to higher authority due to timeout or urgency
     */
    ESCALATED("escalated"),
    
    /**
     * Approval request has timed out without a decision
     */
    TIMEOUT("timeout");

    private final String value;

    ApprovalStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Get ApprovalStatus from string value
     */
    public static ApprovalStatus fromValue(String value) {
        for (ApprovalStatus status : ApprovalStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown approval status: " + value);
    }

    /**
     * Check if status represents a completed approval (approved, rejected, or needs changes)
     */
    public boolean isComplete() {
        return this == APPROVED || this == REJECTED || this == NEEDS_CHANGES;
    }

    /**
     * Check if status represents a pending state (pending, in_review, escalated)
     */
    public boolean isPending() {
        return this == PENDING || this == IN_REVIEW || this == ESCALATED;
    }

    /**
     * Check if status represents a final state (approved, rejected, timeout)
     */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == TIMEOUT;
    }

    @Override
    public String toString() {
        return value;
    }
}
