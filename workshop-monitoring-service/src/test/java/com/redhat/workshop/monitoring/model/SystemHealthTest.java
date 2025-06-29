package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SystemHealth model
 */
class SystemHealthTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void testSystemHealthWithAllHealthyAgents() {
        Map<String, AgentStatus> agents = new HashMap<>();
        agents.put("workshop-chat", createAgentStatus("workshop-chat", HealthStatus.HEALTHY));
        agents.put("template-converter", createAgentStatus("template-converter", HealthStatus.HEALTHY));
        agents.put("content-creator", createAgentStatus("content-creator", HealthStatus.HEALTHY));
        
        SystemHealth systemHealth = new SystemHealth(agents);
        
        assertEquals(HealthStatus.HEALTHY, systemHealth.getOverallStatus());
        assertEquals(3, systemHealth.getTotalAgents());
        assertEquals(3, systemHealth.getHealthyAgents());
        assertEquals(0, systemHealth.getDegradedAgents());
        assertEquals(0, systemHealth.getUnhealthyAgents());
        assertEquals(0, systemHealth.getUnknownAgents());
    }

    @Test
    void testSystemHealthWithMixedAgentStates() {
        Map<String, AgentStatus> agents = new HashMap<>();
        agents.put("workshop-chat", createAgentStatus("workshop-chat", HealthStatus.HEALTHY));
        agents.put("template-converter", createAgentStatus("template-converter", HealthStatus.DEGRADED));
        agents.put("content-creator", createAgentStatus("content-creator", HealthStatus.UNHEALTHY));
        
        SystemHealth systemHealth = new SystemHealth(agents);
        
        assertEquals(HealthStatus.UNHEALTHY, systemHealth.getOverallStatus());
        assertEquals(3, systemHealth.getTotalAgents());
        assertEquals(1, systemHealth.getHealthyAgents());
        assertEquals(1, systemHealth.getDegradedAgents());
        assertEquals(1, systemHealth.getUnhealthyAgents());
        assertEquals(0, systemHealth.getUnknownAgents());
    }

    @Test
    void testSystemHealthWithDegradedAgents() {
        Map<String, AgentStatus> agents = new HashMap<>();
        agents.put("workshop-chat", createAgentStatus("workshop-chat", HealthStatus.HEALTHY));
        agents.put("template-converter", createAgentStatus("template-converter", HealthStatus.DEGRADED));
        
        SystemHealth systemHealth = new SystemHealth(agents);
        
        assertEquals(HealthStatus.DEGRADED, systemHealth.getOverallStatus());
        assertEquals(2, systemHealth.getTotalAgents());
        assertEquals(1, systemHealth.getHealthyAgents());
        assertEquals(1, systemHealth.getDegradedAgents());
    }

    @Test
    void testSystemHealthSerialization() throws Exception {
        Map<String, AgentStatus> agents = new HashMap<>();
        agents.put("workshop-chat", createAgentStatus("workshop-chat", HealthStatus.HEALTHY));
        
        SystemHealth systemHealth = new SystemHealth(agents);
        
        // Test serialization
        String json = objectMapper.writeValueAsString(systemHealth);
        assertNotNull(json);
        assertTrue(json.contains("HEALTHY"));
        assertTrue(json.contains("workshop-chat"));
        
        // Test deserialization
        SystemHealth deserialized = objectMapper.readValue(json, SystemHealth.class);
        assertEquals(systemHealth.getOverallStatus(), deserialized.getOverallStatus());
        assertEquals(systemHealth.getTotalAgents(), deserialized.getTotalAgents());
    }

    @Test
    void testEmptySystemHealth() {
        SystemHealth systemHealth = new SystemHealth();
        systemHealth.calculateOverallStatus();
        
        assertEquals(HealthStatus.UNKNOWN, systemHealth.getOverallStatus());
        assertEquals(0, systemHealth.getTotalAgents());
    }

    private AgentStatus createAgentStatus(String name, HealthStatus health) {
        AgentStatus status = new AgentStatus(name, "http://localhost:8080");
        status.setHealth(health);
        return status;
    }
}
