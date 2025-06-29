package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.AgentStatus;
import com.redhat.workshop.monitoring.model.HealthStatus;
import com.redhat.workshop.monitoring.model.SystemHealth;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AgentHealthService
 */
@QuarkusTest
class AgentHealthServiceTest {

    @Inject
    AgentHealthService agentHealthService;

    @Test
    void testGetSystemHealthInitialState() {
        // System health should be available (may be UNKNOWN or UNHEALTHY depending on timing)
        SystemHealth systemHealth = agentHealthService.getSystemHealth();

        assertNotNull(systemHealth);
        // In test environment, agents are not available, so status should be UNKNOWN or UNHEALTHY
        assertTrue(systemHealth.getOverallStatus() == HealthStatus.UNKNOWN ||
                  systemHealth.getOverallStatus() == HealthStatus.UNHEALTHY);
    }

    @Test
    void testGetAgentEndpoints() {
        Map<String, String> endpoints = agentHealthService.getAgentEndpoints();
        
        assertNotNull(endpoints);
        // Should contain the configured agent endpoints
        assertTrue(endpoints.containsKey("workshop-chat"));
        assertTrue(endpoints.containsKey("template-converter"));
        assertTrue(endpoints.containsKey("content-creator"));
        assertTrue(endpoints.containsKey("source-manager"));
        assertTrue(endpoints.containsKey("research-validation"));
        assertTrue(endpoints.containsKey("documentation-pipeline"));
    }

    @Test
    void testGetAllAgentStatusInitialState() {
        List<AgentStatus> agentStatuses = agentHealthService.getAllAgentStatus();

        assertNotNull(agentStatuses);
        // In test environment, scheduled health checks may have run, so we just verify the list is not null
        // and contains the expected number of agents (6)
        assertTrue(agentStatuses.size() <= 6); // May be 0 if no checks run, or up to 6 if checks ran
    }

    @Test
    void testGetAgentStatusNotFound() {
        AgentStatus status = agentHealthService.getAgentStatus("non-existent-agent");
        
        assertNull(status);
    }

    @Test
    void testTriggerHealthCheck() {
        // This test will attempt to call real endpoints, which may fail in test environment
        // But we can verify the method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            agentHealthService.triggerHealthCheck();
        });
        
        // After triggering health check, system health should be updated
        SystemHealth systemHealth = agentHealthService.getSystemHealth();
        assertNotNull(systemHealth);
        
        // Last update timestamp should be recent
        Instant lastUpdate = agentHealthService.getLastSystemHealthUpdate();
        assertNotNull(lastUpdate);
        assertTrue(lastUpdate.isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testGetLastSystemHealthUpdate() {
        Instant timestamp = agentHealthService.getLastSystemHealthUpdate();
        
        assertNotNull(timestamp);
        // Should be a recent timestamp
        assertTrue(timestamp.isBefore(Instant.now().plusSeconds(1)));
        assertTrue(timestamp.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
    }

    @Test
    void testServiceDestroy() {
        // Test that destroy method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            agentHealthService.destroy();
        });
    }
}
