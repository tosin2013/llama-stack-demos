package com.redhat.workshop.monitoring.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for MonitoringResource REST endpoints
 */
@QuarkusTest
class MonitoringResourceTest {

    @Test
    void testGetSystemHealth() {
        given()
            .when().get("/api/monitoring/health")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("overall_status", notNullValue())
                .body("total_agents", notNullValue())
                .body("last_updated", notNullValue());
    }

    @Test
    void testGetAllAgentStatus() {
        given()
            .when().get("/api/monitoring/agents")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    void testGetAgentStatusNotFound() {
        given()
            .when().get("/api/monitoring/agents/non-existent-agent")
            .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("error", equalTo("Agent not found"))
                .body("agentName", equalTo("non-existent-agent"));
    }

    @Test
    void testGetSystemSummary() {
        given()
            .when().get("/api/monitoring/summary")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("overall_status", notNullValue())
                .body("total_agents", notNullValue())
                .body("healthy_agents", notNullValue())
                .body("degraded_agents", notNullValue())
                .body("unhealthy_agents", notNullValue())
                .body("unknown_agents", notNullValue())
                .body("last_updated", notNullValue())
                .body("configured_endpoints", notNullValue())
                .body("agent_overview", notNullValue());
    }

    @Test
    void testTriggerHealthCheck() {
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/monitoring/health-check")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("message", equalTo("Health check triggered successfully"))
                .body("timestamp", notNullValue());
    }

    @Test
    void testGetServiceInfo() {
        given()
            .when().get("/api/monitoring/info")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("service_name", equalTo("Workshop Monitoring Service"))
                .body("version", equalTo("1.0.0-SNAPSHOT"))
                .body("description", notNullValue())
                .body("configured_agents", notNullValue())
                .body("last_health_update", notNullValue())
                .body("api_endpoints", notNullValue());
    }

    @Test
    void testGetSpecificAgentStatus() {
        // First trigger a health check to ensure we have some agent data
        given()
            .contentType(ContentType.JSON)
            .when().post("/api/monitoring/health-check")
            .then()
                .statusCode(200);

        // Wait a moment for the health check to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Try to get status for a configured agent
        given()
            .when().get("/api/monitoring/agents/workshop-chat")
            .then()
                .statusCode(anyOf(is(200), is(404))) // May be 404 if agent not reachable in test
                .contentType(ContentType.JSON);
    }

    @Test
    void testCorsHeaders() {
        given()
            .header("Origin", "http://localhost:3000")
            .when().get("/api/monitoring/health")
            .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", notNullValue());
    }

    @Test
    void testOpenApiDocumentation() {
        // Test that OpenAPI documentation is available
        given()
            .when().get("/q/openapi")
            .then()
                .statusCode(200)
                .contentType(anyOf(equalTo("application/json"), equalTo("text/plain"), equalTo("application/yaml;charset=UTF-8")));
    }

    @Test
    void testSwaggerUI() {
        // Test that Swagger UI is available
        given()
            .when().get("/q/swagger-ui")
            .then()
                .statusCode(200);
    }
}
