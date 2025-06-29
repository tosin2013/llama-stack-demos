package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the overall health status of the Workshop Template System,
 * aggregating information from all monitored agents.
 * 
 * @author Workshop Monitoring Service
 */
public class SystemHealth {
    
    @JsonProperty("overall_status")
    private HealthStatus overallStatus;
    
    @JsonProperty("agents")
    private Map<String, AgentStatus> agents;
    
    @JsonProperty("active_issues")
    private List<String> activeIssues;
    
    @JsonProperty("last_updated")
    private Instant lastUpdated;
    
    @JsonProperty("total_agents")
    private int totalAgents;
    
    @JsonProperty("healthy_agents")
    private int healthyAgents;
    
    @JsonProperty("degraded_agents")
    private int degradedAgents;
    
    @JsonProperty("unhealthy_agents")
    private int unhealthyAgents;
    
    @JsonProperty("unknown_agents")
    private int unknownAgents;
    
    // Default constructor for JSON deserialization
    public SystemHealth() {
        this.lastUpdated = Instant.now();
    }
    
    // Constructor with agents map
    public SystemHealth(Map<String, AgentStatus> agents) {
        this.agents = agents;
        this.lastUpdated = Instant.now();
        calculateOverallStatus();
    }
    
    /**
     * Calculates the overall system status based on individual agent statuses
     */
    public void calculateOverallStatus() {
        if (agents == null || agents.isEmpty()) {
            this.overallStatus = HealthStatus.UNKNOWN;
            return;
        }
        
        // Count agents by status
        healthyAgents = 0;
        degradedAgents = 0;
        unhealthyAgents = 0;
        unknownAgents = 0;
        
        for (AgentStatus agent : agents.values()) {
            switch (agent.getHealth()) {
                case HEALTHY -> healthyAgents++;
                case DEGRADED -> degradedAgents++;
                case UNHEALTHY -> unhealthyAgents++;
                case UNKNOWN -> unknownAgents++;
            }
        }
        
        totalAgents = agents.size();
        
        // Determine overall status
        if (unhealthyAgents > 0) {
            this.overallStatus = HealthStatus.UNHEALTHY;
        } else if (degradedAgents > 0 || unknownAgents > 0) {
            this.overallStatus = HealthStatus.DEGRADED;
        } else if (healthyAgents == totalAgents) {
            this.overallStatus = HealthStatus.HEALTHY;
        } else {
            this.overallStatus = HealthStatus.UNKNOWN;
        }
    }
    
    // Getters and Setters
    public HealthStatus getOverallStatus() {
        return overallStatus;
    }
    
    public void setOverallStatus(HealthStatus overallStatus) {
        this.overallStatus = overallStatus;
    }
    
    public Map<String, AgentStatus> getAgents() {
        return agents;
    }
    
    public void setAgents(Map<String, AgentStatus> agents) {
        this.agents = agents;
        calculateOverallStatus();
    }
    
    public List<String> getActiveIssues() {
        return activeIssues;
    }
    
    public void setActiveIssues(List<String> activeIssues) {
        this.activeIssues = activeIssues;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public int getTotalAgents() {
        return totalAgents;
    }
    
    public int getHealthyAgents() {
        return healthyAgents;
    }
    
    public int getDegradedAgents() {
        return degradedAgents;
    }
    
    public int getUnhealthyAgents() {
        return unhealthyAgents;
    }
    
    public int getUnknownAgents() {
        return unknownAgents;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemHealth that = (SystemHealth) o;
        return overallStatus == that.overallStatus && 
               Objects.equals(agents, that.agents) && 
               Objects.equals(lastUpdated, that.lastUpdated);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(overallStatus, agents, lastUpdated);
    }
    
    @Override
    public String toString() {
        return "SystemHealth{" +
                "overallStatus=" + overallStatus +
                ", totalAgents=" + totalAgents +
                ", healthyAgents=" + healthyAgents +
                ", degradedAgents=" + degradedAgents +
                ", unhealthyAgents=" + unhealthyAgents +
                ", unknownAgents=" + unknownAgents +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
