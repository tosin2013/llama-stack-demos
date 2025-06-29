package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Model representing a chat message in the Human Oversight Panel.
 * Supports natural language interaction with the oversight coordinator.
 * Implements ADR-0004 Human Oversight Domain data structures.
 */
@RegisterForReflection
public class ChatMessage {

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("role")
    private String role; // "user" or "assistant"

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("response_time_ms")
    private Long responseTimeMs;

    @JsonProperty("context_used")
    private String contextUsed;

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    // Default constructor
    public ChatMessage() {
        this.messageId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    // Constructor with required fields
    public ChatMessage(String sessionId, String content, String role) {
        this();
        this.sessionId = sessionId;
        this.content = content;
        this.role = role;
    }

    // Constructor with metadata
    public ChatMessage(String sessionId, String content, String role, Map<String, Object> metadata) {
        this(sessionId, content, role);
        this.metadata = metadata;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getContextUsed() {
        return contextUsed;
    }

    public void setContextUsed(String contextUsed) {
        this.contextUsed = contextUsed;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    // Utility methods
    public boolean isUserMessage() {
        return "user".equals(role);
    }

    public boolean isAssistantMessage() {
        return "assistant".equals(role);
    }

    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }

    public boolean isRecent(long maxAgeMinutes) {
        return timestamp.isAfter(Instant.now().minusSeconds(maxAgeMinutes * 60));
    }

    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    @Override
    public String toString() {
        return String.format("ChatMessage{id='%s', session='%s', role='%s', content='%s'}", 
                           messageId, sessionId, role, 
                           content != null && content.length() > 50 ? 
                           content.substring(0, 50) + "..." : content);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChatMessage that = (ChatMessage) obj;
        return messageId != null ? messageId.equals(that.messageId) : that.messageId == null;
    }

    @Override
    public int hashCode() {
        return messageId != null ? messageId.hashCode() : 0;
    }
}
