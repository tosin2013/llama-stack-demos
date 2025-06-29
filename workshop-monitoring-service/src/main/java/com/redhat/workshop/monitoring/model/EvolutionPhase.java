package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Enumeration of workshop evolution phases/statuses.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration for workshop evolution tracking.
 */
@RegisterForReflection
public enum EvolutionPhase {
    
    /**
     * Evolution request has been submitted and is awaiting approval
     */
    REQUESTED("requested"),
    
    /**
     * Evolution request is under human review
     */
    UNDER_REVIEW("under_review"),
    
    /**
     * Evolution request has been approved and is ready for implementation
     */
    APPROVED("approved"),
    
    /**
     * Evolution changes are being implemented by Source Manager Agent
     */
    IMPLEMENTING("implementing"),
    
    /**
     * Evolution implementation is being validated and tested
     */
    VALIDATING("validating"),
    
    /**
     * Evolution has been completed successfully
     */
    COMPLETED("completed"),
    
    /**
     * Evolution has been deployed to production
     */
    DEPLOYED("deployed"),
    
    /**
     * Evolution request was rejected during review
     */
    REJECTED("rejected"),
    
    /**
     * Evolution implementation failed
     */
    FAILED("failed"),
    
    /**
     * Evolution was rolled back due to issues
     */
    ROLLED_BACK("rolled_back"),
    
    /**
     * Evolution was cancelled before completion
     */
    CANCELLED("cancelled");

    private final String value;

    EvolutionPhase(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Get EvolutionPhase from string value
     */
    public static EvolutionPhase fromValue(String value) {
        for (EvolutionPhase phase : EvolutionPhase.values()) {
            if (phase.value.equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown evolution phase: " + value);
    }

    /**
     * Check if phase represents an active/in-progress state
     */
    public boolean isActive() {
        return this == REQUESTED || 
               this == UNDER_REVIEW || 
               this == APPROVED || 
               this == IMPLEMENTING || 
               this == VALIDATING;
    }

    /**
     * Check if phase represents a completed state (success or failure)
     */
    public boolean isCompleted() {
        return this == COMPLETED || 
               this == DEPLOYED || 
               this == REJECTED || 
               this == FAILED || 
               this == ROLLED_BACK || 
               this == CANCELLED;
    }

    /**
     * Check if phase represents a successful completion
     */
    public boolean isSuccessful() {
        return this == COMPLETED || this == DEPLOYED;
    }

    /**
     * Check if phase represents a failure state
     */
    public boolean isFailed() {
        return this == REJECTED || 
               this == FAILED || 
               this == ROLLED_BACK || 
               this == CANCELLED;
    }

    /**
     * Check if evolution can be cancelled from this phase
     */
    public boolean canBeCancelled() {
        return this == REQUESTED || 
               this == UNDER_REVIEW || 
               this == APPROVED;
    }

    /**
     * Check if evolution can be rolled back from this phase
     */
    public boolean canBeRolledBack() {
        return this == COMPLETED || this == DEPLOYED;
    }

    /**
     * Get the next expected phase in the evolution workflow
     */
    public EvolutionPhase getNextPhase() {
        switch (this) {
            case REQUESTED:
                return UNDER_REVIEW;
            case UNDER_REVIEW:
                return APPROVED; // or REJECTED
            case APPROVED:
                return IMPLEMENTING;
            case IMPLEMENTING:
                return VALIDATING; // or FAILED
            case VALIDATING:
                return COMPLETED; // or FAILED
            case COMPLETED:
                return DEPLOYED;
            default:
                return null; // Terminal states have no next phase
        }
    }

    /**
     * Get human-readable description of the phase
     */
    public String getDescription() {
        switch (this) {
            case REQUESTED:
                return "Evolution request submitted, awaiting review";
            case UNDER_REVIEW:
                return "Human reviewer is evaluating the evolution request";
            case APPROVED:
                return "Evolution approved, ready for implementation";
            case IMPLEMENTING:
                return "Source Manager Agent is applying evolution changes";
            case VALIDATING:
                return "Evolution changes are being validated and tested";
            case COMPLETED:
                return "Evolution successfully completed";
            case DEPLOYED:
                return "Evolution deployed to production environment";
            case REJECTED:
                return "Evolution request was rejected during review";
            case FAILED:
                return "Evolution implementation encountered errors";
            case ROLLED_BACK:
                return "Evolution was rolled back due to issues";
            case CANCELLED:
                return "Evolution was cancelled before completion";
            default:
                return "Unknown evolution phase";
        }
    }

    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        switch (this) {
            case REQUESTED:
            case UNDER_REVIEW:
                return "blue";
            case APPROVED:
                return "green";
            case IMPLEMENTING:
            case VALIDATING:
                return "yellow";
            case COMPLETED:
            case DEPLOYED:
                return "green";
            case REJECTED:
            case CANCELLED:
                return "gray";
            case FAILED:
            case ROLLED_BACK:
                return "red";
            default:
                return "gray";
        }
    }

    /**
     * Get icon for UI display
     */
    public String getIcon() {
        switch (this) {
            case REQUESTED:
                return "📝";
            case UNDER_REVIEW:
                return "👀";
            case APPROVED:
                return "✅";
            case IMPLEMENTING:
                return "🔧";
            case VALIDATING:
                return "🧪";
            case COMPLETED:
                return "🎉";
            case DEPLOYED:
                return "🚀";
            case REJECTED:
                return "❌";
            case FAILED:
                return "💥";
            case ROLLED_BACK:
                return "🔄";
            case CANCELLED:
                return "🚫";
            default:
                return "❓";
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
