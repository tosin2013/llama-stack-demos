package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Enumeration of risk levels for impact analysis.
 */
@RegisterForReflection
public enum RiskLevel {
    
    /**
     * Very low risk - minimal impact expected
     */
    VERY_LOW("very_low"),
    
    /**
     * Low risk - minor impact with easy mitigation
     */
    LOW("low"),
    
    /**
     * Medium risk - moderate impact requiring careful planning
     */
    MEDIUM("medium"),
    
    /**
     * High risk - significant impact requiring extensive testing
     */
    HIGH("high"),
    
    /**
     * Critical risk - major impact requiring expert review
     */
    CRITICAL("critical");

    private final String value;

    RiskLevel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static RiskLevel fromValue(String value) {
        for (RiskLevel level : RiskLevel.values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown risk level: " + value);
    }

    public String getColorCode() {
        switch (this) {
            case VERY_LOW:
                return "green";
            case LOW:
                return "lightgreen";
            case MEDIUM:
                return "yellow";
            case HIGH:
                return "orange";
            case CRITICAL:
                return "red";
            default:
                return "gray";
        }
    }

    public String getIcon() {
        switch (this) {
            case VERY_LOW:
                return "✅";
            case LOW:
                return "🟢";
            case MEDIUM:
                return "🟡";
            case HIGH:
                return "🟠";
            case CRITICAL:
                return "🔴";
            default:
                return "❓";
        }
    }

    public boolean requiresExpertReview() {
        return this == HIGH || this == CRITICAL;
    }

    public boolean requiresExtensiveTesting() {
        return this == MEDIUM || this == HIGH || this == CRITICAL;
    }

    @Override
    public String toString() {
        return value;
    }
}
