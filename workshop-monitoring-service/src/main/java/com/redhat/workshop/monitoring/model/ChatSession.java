package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model representing a chat session in the Human Oversight Panel.
 * Manages conversation history and session state for natural language interactions.
 * Implements ADR-0004 Human Oversight Domain session management.
 */
@RegisterForReflection
public class ChatSession {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("status")
    private String status; // "active", "inactive", "expired"

    @JsonProperty("messages")
    private List<ChatMessage> messages;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("last_activity")
    private Instant lastActivity;

    @JsonProperty("expires_at")
    private Instant expiresAt;

    @JsonProperty("context")
    private String context;

    @JsonProperty("session_type")
    private String sessionType; // "oversight", "support", "coordination"

    @JsonProperty("max_messages")
    private Integer maxMessages;

    @JsonProperty("total_messages")
    private Integer totalMessages;

    // Default constructor
    public ChatSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.status = "active";
        this.messages = new ArrayList<>();
        this.createdAt = Instant.now();
        this.lastActivity = Instant.now();
        this.maxMessages = 100; // Default message limit
        this.totalMessages = 0;
        // Default session expires in 24 hours
        this.expiresAt = Instant.now().plusSeconds(24 * 60 * 60);
    }

    // Constructor with user ID
    public ChatSession(String userId) {
        this();
        this.userId = userId;
    }

    // Constructor with user ID and session type
    public ChatSession(String userId, String sessionType) {
        this(userId);
        this.sessionType = sessionType;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.lastActivity = Instant.now();
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        this.totalMessages = messages != null ? messages.size() : 0;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Integer getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(Integer maxMessages) {
        this.maxMessages = maxMessages;
    }

    public Integer getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(Integer totalMessages) {
        this.totalMessages = totalMessages;
    }

    // Utility methods
    public boolean isActive() {
        return "active".equals(status) && !isExpired();
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isInactive() {
        return "inactive".equals(status);
    }

    public void addMessage(ChatMessage message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        message.setSessionId(this.sessionId);
        messages.add(message);
        this.totalMessages = messages.size();
        this.lastActivity = Instant.now();
        
        // Trim messages if exceeding max limit
        if (maxMessages != null && messages.size() > maxMessages) {
            messages = messages.subList(messages.size() - maxMessages, messages.size());
        }
    }

    public ChatMessage getLastMessage() {
        return messages != null && !messages.isEmpty() ? 
               messages.get(messages.size() - 1) : null;
    }

    public List<ChatMessage> getRecentMessages(int count) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        
        int start = Math.max(0, messages.size() - count);
        return new ArrayList<>(messages.subList(start, messages.size()));
    }

    public void extendSession(long additionalHours) {
        this.expiresAt = Instant.now().plusSeconds(additionalHours * 60 * 60);
        this.lastActivity = Instant.now();
    }

    public void deactivate() {
        this.status = "inactive";
        this.lastActivity = Instant.now();
    }

    public void activate() {
        this.status = "active";
        this.lastActivity = Instant.now();
        // Extend expiration if needed
        if (isExpired()) {
            extendSession(24); // Extend by 24 hours
        }
    }

    public long getInactiveMinutes() {
        return (Instant.now().toEpochMilli() - lastActivity.toEpochMilli()) / (1000 * 60);
    }

    public boolean needsCleanup(long maxInactiveMinutes) {
        return getInactiveMinutes() > maxInactiveMinutes || isExpired();
    }

    @Override
    public String toString() {
        return String.format("ChatSession{id='%s', user='%s', status='%s', messages=%d}", 
                           sessionId, userId, status, totalMessages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChatSession that = (ChatSession) obj;
        return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}
