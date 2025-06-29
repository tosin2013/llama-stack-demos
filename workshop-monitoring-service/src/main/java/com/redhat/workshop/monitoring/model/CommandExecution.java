package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Model representing a command execution in the Human Oversight Panel.
 * Tracks command execution state, results, and audit trail.
 * Implements ADR-0004 Human Oversight Domain command tracking.
 */
@RegisterForReflection
public class CommandExecution {

    @JsonProperty("command_id")
    private String commandId;

    @JsonProperty("command")
    private String command;

    @JsonProperty("status")
    private CommandStatus status;

    @JsonProperty("executor")
    private String executor;

    @JsonProperty("result")
    private Map<String, Object> result;

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @JsonProperty("executed_at")
    private Instant executedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("execution_time_ms")
    private Long executionTimeMs;

    @JsonProperty("command_type")
    private String commandType;

    @JsonProperty("context")
    private String context;

    @JsonProperty("audit_trail")
    private String auditTrail;

    // Default constructor
    public CommandExecution() {
        this.commandId = UUID.randomUUID().toString();
        this.status = CommandStatus.PENDING;
        this.executedAt = Instant.now();
    }

    // Constructor with required fields
    public CommandExecution(String command, String executor) {
        this();
        this.command = command;
        this.executor = executor;
    }

    // Constructor with command type
    public CommandExecution(String command, String executor, String commandType) {
        this(command, executor);
        this.commandType = commandType;
    }

    // Getters and Setters
    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
        if (status == CommandStatus.COMPLETED || status == CommandStatus.FAILED) {
            this.completedAt = Instant.now();
            if (executedAt != null) {
                this.executionTimeMs = completedAt.toEpochMilli() - executedAt.toEpochMilli();
            }
        }
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getAuditTrail() {
        return auditTrail;
    }

    public void setAuditTrail(String auditTrail) {
        this.auditTrail = auditTrail;
    }

    // Utility methods
    public boolean isPending() {
        return status == CommandStatus.PENDING;
    }

    public boolean isExecuting() {
        return status == CommandStatus.EXECUTING;
    }

    public boolean isCompleted() {
        return status == CommandStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == CommandStatus.FAILED;
    }

    public boolean isFinished() {
        return isCompleted() || isFailed();
    }

    public void markAsExecuting() {
        this.status = CommandStatus.EXECUTING;
        this.executedAt = Instant.now();
    }

    public void markAsCompleted(Map<String, Object> result) {
        this.status = CommandStatus.COMPLETED;
        this.result = result;
        this.completedAt = Instant.now();
        if (executedAt != null) {
            this.executionTimeMs = completedAt.toEpochMilli() - executedAt.toEpochMilli();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = CommandStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        if (executedAt != null) {
            this.executionTimeMs = completedAt.toEpochMilli() - executedAt.toEpochMilli();
        }
    }

    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    public void addResultData(String key, Object value) {
        if (result == null) {
            result = new java.util.HashMap<>();
        }
        result.put(key, value);
    }

    public Object getResultData(String key) {
        return result != null ? result.get(key) : null;
    }

    @Override
    public String toString() {
        return String.format("CommandExecution{id='%s', command='%s', status='%s', executor='%s'}", 
                           commandId, command, status, executor);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CommandExecution that = (CommandExecution) obj;
        return commandId != null ? commandId.equals(that.commandId) : that.commandId == null;
    }

    @Override
    public int hashCode() {
        return commandId != null ? commandId.hashCode() : 0;
    }
}
