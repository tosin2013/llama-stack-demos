package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Enumeration of workshop evolution types.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration for workshop evolution.
 */
@RegisterForReflection
public enum EvolutionType {
    
    /**
     * Content updates based on new research findings
     */
    RESEARCH_UPDATE("research_update"),
    
    /**
     * Technology stack or framework updates
     */
    TECHNOLOGY_REFRESH("technology_refresh"),
    
    /**
     * Integration of learner feedback and suggestions
     */
    FEEDBACK_INTEGRATION("feedback_integration"),
    
    /**
     * Addition of new content, modules, or sections
     */
    CONTENT_EXPANSION("content_expansion"),
    
    /**
     * General content updates and improvements
     */
    CONTENT_UPDATE("content_update"),
    
    /**
     * Bug fixes and error corrections
     */
    BUG_FIX("bug_fix"),
    
    /**
     * Security updates and patches
     */
    SECURITY_UPDATE("security_update"),
    
    /**
     * Performance optimizations and improvements
     */
    PERFORMANCE_OPTIMIZATION("performance_optimization");

    private final String value;

    EvolutionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Get EvolutionType from string value
     */
    public static EvolutionType fromValue(String value) {
        for (EvolutionType type : EvolutionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown evolution type: " + value);
    }

    /**
     * Get human-readable description of the evolution type
     */
    public String getDescription() {
        switch (this) {
            case RESEARCH_UPDATE:
                return "Content updates based on new research findings";
            case TECHNOLOGY_REFRESH:
                return "Technology stack or framework updates";
            case FEEDBACK_INTEGRATION:
                return "Integration of learner feedback and suggestions";
            case CONTENT_EXPANSION:
                return "Addition of new content, modules, or sections";
            case CONTENT_UPDATE:
                return "General content updates and improvements";
            case BUG_FIX:
                return "Bug fixes and error corrections";
            case SECURITY_UPDATE:
                return "Security updates and patches";
            case PERFORMANCE_OPTIMIZATION:
                return "Performance optimizations and improvements";
            default:
                return "Workshop evolution";
        }
    }

    /**
     * Get typical duration in hours for this evolution type
     */
    public int getTypicalDurationHours() {
        switch (this) {
            case RESEARCH_UPDATE:
                return 48; // 2 days for research validation
            case TECHNOLOGY_REFRESH:
                return 72; // 3 days for technology updates
            case FEEDBACK_INTEGRATION:
                return 24; // 1 day for feedback integration
            case CONTENT_EXPANSION:
                return 96; // 4 days for major content additions
            case CONTENT_UPDATE:
                return 12; // Half day for minor updates
            case BUG_FIX:
                return 4;  // 4 hours for bug fixes
            case SECURITY_UPDATE:
                return 8;  // 8 hours for security patches
            case PERFORMANCE_OPTIMIZATION:
                return 16; // 16 hours for performance work
            default:
                return 24; // Default 1 day
        }
    }

    /**
     * Get priority level for this evolution type
     */
    public String getPriority() {
        switch (this) {
            case SECURITY_UPDATE:
                return "urgent";
            case BUG_FIX:
                return "high";
            case TECHNOLOGY_REFRESH:
            case PERFORMANCE_OPTIMIZATION:
                return "high";
            case RESEARCH_UPDATE:
            case FEEDBACK_INTEGRATION:
                return "normal";
            case CONTENT_EXPANSION:
            case CONTENT_UPDATE:
                return "normal";
            default:
                return "normal";
        }
    }

    /**
     * Check if this evolution type requires extensive testing
     */
    public boolean requiresExtensiveTesting() {
        return this == TECHNOLOGY_REFRESH || 
               this == CONTENT_EXPANSION || 
               this == SECURITY_UPDATE ||
               this == PERFORMANCE_OPTIMIZATION;
    }

    /**
     * Check if this evolution type typically affects multiple workshop sections
     */
    public boolean affectsMultipleSections() {
        return this == TECHNOLOGY_REFRESH || 
               this == CONTENT_EXPANSION || 
               this == RESEARCH_UPDATE ||
               this == PERFORMANCE_OPTIMIZATION;
    }

    @Override
    public String toString() {
        return value;
    }
}
