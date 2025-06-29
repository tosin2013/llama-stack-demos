package com.redhat.workshop.monitoring.service;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/**
 * REST client interface for communicating with workshop agents.
 * This client is used to call the /agent-card endpoint on each agent
 * to retrieve health and status information.
 * 
 * @author Workshop Monitoring Service
 */
@RegisterRestClient(configKey = "agent-card-client")
@ClientHeaderParam(name = "User-Agent", value = "Workshop-Monitoring-Service/1.0")
public interface AgentCardClient {

    /**
     * Retrieves the agent card information from an agent's /agent-card endpoint.
     * The agent card contains information about the agent's available tools,
     * status, and other metadata.
     * 
     * @param baseUrl The base URL of the agent (will be used to construct the full endpoint)
     * @return Map containing the agent card response data
     */
    @GET
    @Path("/agent-card")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> getAgentCard(String baseUrl);

    /**
     * Retrieves basic health information from an agent's /health endpoint.
     * This is a simpler endpoint that just returns basic health status.
     * 
     * @param baseUrl The base URL of the agent
     * @return Map containing the health response data
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> getHealth(String baseUrl);
}
