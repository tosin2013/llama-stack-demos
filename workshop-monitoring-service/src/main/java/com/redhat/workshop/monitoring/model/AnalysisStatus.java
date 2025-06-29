package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Enumeration of impact analysis statuses.
 */
@RegisterForReflection
public enum AnalysisStatus {
    
    /**
     * Analysis request has been submitted and is pending processing
     */
    PENDING("pending"),
    
    /**
     * Analysis is currently in progress
     */
    IN_PROGRESS("in_progress"),
    
    /**
     * Analysis has been completed successfully
     */
    COMPLETED("completed"),
    
    /**
     * Analysis failed due to an error
     */
    FAILED("failed"),
    
    /**
     * Analysis was cancelled before completion
     */
    CANCELLED("cancelled");

    private final String value;

    AnalysisStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static AnalysisStatus fromValue(String value) {
        for (AnalysisStatus status : AnalysisStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown analysis status: " + value);
    }

    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    @Override
    public String toString() {
        return value;
    }
}
