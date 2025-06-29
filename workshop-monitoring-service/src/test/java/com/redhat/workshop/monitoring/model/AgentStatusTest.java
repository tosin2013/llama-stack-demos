package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AgentStatus model
 */
class AgentStatusTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void testAgentStatusCreation() {
        AgentStatus status = new AgentStatus("workshop-chat", "http://localhost:8080");
        
        assertEquals("workshop-chat", status.getName());
        assertEquals("http://localhost:8080", status.getEndpoint());
        assertEquals(HealthStatus.UNKNOWN, status.getHealth());
        assertNotNull(status.getLastChecked());
    }

    @Test
    void testAgentStatusSerialization() throws Exception {
        AgentStatus status = new AgentStatus("workshop-chat", "http://localhost:8080");
        status.setHealth(HealthStatus.HEALTHY);
        status.setAvailableTools(Arrays.asList("tool1", "tool2", "tool3"));
        status.setResponseTimeMs(150);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("uptime", "2h 30m");
        status.setMetadata(metadata);
        
        // Test serialization
        String json = objectMapper.writeValueAsString(status);
        assertNotNull(json);
        assertTrue(json.contains("workshop-chat"));
        assertTrue(json.contains("HEALTHY"));
        
        // Test deserialization
        AgentStatus deserialized = objectMapper.readValue(json, AgentStatus.class);
        assertEquals(status.getName(), deserialized.getName());
        assertEquals(status.getHealth(), deserialized.getHealth());
        assertEquals(status.getAvailableTools(), deserialized.getAvailableTools());
        assertEquals(status.getResponseTimeMs(), deserialized.getResponseTimeMs());
    }

    @Test
    void testAgentStatusEquality() {
        AgentStatus status1 = new AgentStatus("workshop-chat", "http://localhost:8080");
        AgentStatus status2 = new AgentStatus("workshop-chat", "http://localhost:8080");
        AgentStatus status3 = new AgentStatus("template-converter", "http://localhost:8081");
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertEquals(status1.hashCode(), status2.hashCode());
    }

    @Test
    void testAgentStatusToString() {
        AgentStatus status = new AgentStatus("workshop-chat", "http://localhost:8080");
        status.setHealth(HealthStatus.HEALTHY);
        status.setResponseTimeMs(120);
        
        String toString = status.toString();
        assertTrue(toString.contains("workshop-chat"));
        assertTrue(toString.contains("HEALTHY"));
        assertTrue(toString.contains("120"));
    }
}
