package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the current status and health information of a workshop agent.
 * This model mirrors the structure returned by agent /agent-card endpoints.
 * 
 * @author Workshop Monitoring Service
 */
public class AgentStatus {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("health")
    private HealthStatus health;
    
    @JsonProperty("available_tools")
    private List<String> availableTools;
    
    @JsonProperty("response_time_ms")
    private long responseTimeMs;
    
    @JsonProperty("last_checked")
    private Instant lastChecked;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    // Default constructor for JSON deserialization
    public AgentStatus() {}
    
    // Constructor with required fields
    public AgentStatus(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
        this.health = HealthStatus.UNKNOWN;
        this.lastChecked = Instant.now();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public HealthStatus getHealth() {
        return health;
    }
    
    public void setHealth(HealthStatus health) {
        this.health = health;
    }
    
    public List<String> getAvailableTools() {
        return availableTools;
    }
    
    public void setAvailableTools(List<String> availableTools) {
        this.availableTools = availableTools;
    }
    
    public long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
    
    public Instant getLastChecked() {
        return lastChecked;
    }
    
    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentStatus that = (AgentStatus) o;
        return Objects.equals(name, that.name) && Objects.equals(endpoint, that.endpoint);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, endpoint);
    }
    
    @Override
    public String toString() {
        return "AgentStatus{" +
                "name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", health=" + health +
                ", responseTimeMs=" + responseTimeMs +
                ", lastChecked=" + lastChecked +
                '}';
    }
}
