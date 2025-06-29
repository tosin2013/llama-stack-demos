package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration representing the status of a command execution.
 * Used in CommandExecution model for tracking command lifecycle.
 * Implements ADR-0004 Human Oversight Domain status tracking.
 */
public enum CommandStatus {
    
    PENDING("pending"),
    EXECUTING("executing"), 
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    CommandStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CommandStatus fromValue(String value) {
        for (CommandStatus status : CommandStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown command status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
