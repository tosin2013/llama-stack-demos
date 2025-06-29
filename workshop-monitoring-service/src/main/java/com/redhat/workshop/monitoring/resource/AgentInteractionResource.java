package com.redhat.workshop.monitoring.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.redhat.workshop.monitoring.service.AgentHealthService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Agent Interaction Resource
 * Implements ADR-0004 Agent Management Domain REST API
 * Provides agent configuration, interaction, and workflow execution endpoints
 */
@Path("/api/agent-interaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentInteractionResource {

    private static final Logger LOG = Logger.getLogger(AgentInteractionResource.class);

    @Inject
    AgentHealthService agentHealthService;

    @ConfigProperty(name = "workshop.agents.endpoints.workshop_chat", defaultValue = "http://workshop-chat-agent:80")
    String workshopChatEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.template_converter", defaultValue = "http://template-converter-agent:80")
    String templateConverterEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.content_creator", defaultValue = "http://content-creator-agent:80")
    String contentCreatorEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.source_manager", defaultValue = "http://source-manager-agent:80")
    String sourceManagerEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.research_validation", defaultValue = "http://research-validation-agent:80")
    String researchValidationEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.documentation_pipeline", defaultValue = "http://documentation-pipeline-agent:80")
    String documentationPipelineEndpoint;

    @ConfigProperty(name = "workshop.agents.endpoints.human_oversight", defaultValue = "http://human-oversight-coordinator:80")
    String humanOversightEndpoint;

    /**
     * Get agent configuration for frontend
     */
    @GET
    @Path("/config")
    public Response getAgentConfiguration() {
        try {
            LOG.info("Fetching agent configuration for frontend");

            Map<String, Object> config = new HashMap<>();
            
            // Agent endpoints configuration
            Map<String, String> endpoints = new HashMap<>();
            endpoints.put("workshop-chat", workshopChatEndpoint);
            endpoints.put("template-converter", templateConverterEndpoint);
            endpoints.put("content-creator", contentCreatorEndpoint);
            endpoints.put("source-manager", sourceManagerEndpoint);
            endpoints.put("research-validation", researchValidationEndpoint);
            endpoints.put("documentation-pipeline", documentationPipelineEndpoint);
            endpoints.put("human-oversight", humanOversightEndpoint);
            
            config.put("endpoints", endpoints);

            // Workflow templates
            List<Map<String, Object>> workflowTemplates = Arrays.asList(
                createWorkflowTemplate(
                    "repository-to-workshop",
                    "Repository to Workshop",
                    "Convert a GitHub repository into an interactive workshop",
                    Arrays.asList("template-converter", "content-creator", "research-validation", "source-manager"),
                    Map.of("repository_url", "https://github.com/user/project")
                ),
                createWorkflowTemplate(
                    "original-workshop-creation",
                    "Original Workshop Creation",
                    "Create an original workshop from learning objectives",
                    Arrays.asList("content-creator", "research-validation", "source-manager"),
                    Map.of("learning_objectives", "Cloud security fundamentals")
                ),
                createWorkflowTemplate(
                    "workshop-enhancement",
                    "Workshop Enhancement",
                    "Enhance existing workshop with new content and validation",
                    Arrays.asList("research-validation", "content-creator", "documentation-pipeline"),
                    Map.of("workshop_id", "existing-workshop-123")
                ),
                createWorkflowTemplate(
                    "content-validation",
                    "Content Validation",
                    "Validate workshop content accuracy and currency",
                    Arrays.asList("research-validation", "documentation-pipeline"),
                    Map.of("content_type", "technical_documentation")
                )
            );
            
            config.put("workflowTemplates", workflowTemplates);

            // Agent capabilities (from agent health service)
            try {
                var agentStatuses = agentHealthService.getAllAgentStatus();
                if (agentStatuses != null) {
                    config.put("agentCapabilities", agentStatuses);
                }
            } catch (Exception e) {
                LOG.warn("Failed to fetch agent capabilities from agent health service", e);
            }

            return Response.ok(Map.of(
                "success", true,
                "data", config,
                "metadata", Map.of(
                    "timestamp", new Date().toInstant().toString(),
                    "version", "1.0.0"
                )
            )).build();

        } catch (Exception e) {
            LOG.error("Error fetching agent configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "AGENT_CONFIG_ERROR",
                        "message", "Failed to fetch agent configuration",
                        "details", e.getMessage()
                    )
                )).build();
        }
    }

    /**
     * Execute agent interaction
     */
    @POST
    @Path("/agents/{agentId}/interact")
    public CompletionStage<Response> interactWithAgent(
            @PathParam("agentId") String agentId,
            Map<String, Object> request) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.infof("Executing interaction with agent: %s", agentId);

                String endpoint = getAgentEndpoint(agentId);
                if (endpoint == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of(
                            "success", false,
                            "error", Map.of(
                                "code", "AGENT_NOT_FOUND",
                                "message", "Agent not found: " + agentId
                            )
                        )).build();
                }

                // For now, return a mock response
                // In a full implementation, this would make HTTP calls to the agent
                Map<String, Object> response = Map.of(
                    "agentId", agentId,
                    "status", "completed",
                    "result", "Agent interaction completed successfully",
                    "timestamp", new Date().toInstant().toString()
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", response
                )).build();

            } catch (Exception e) {
                LOG.errorf("Error interacting with agent %s: %s", agentId, e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "AGENT_INTERACTION_ERROR",
                            "message", "Failed to interact with agent",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    /**
     * Execute workflow template
     */
    @POST
    @Path("/workflows/execute")
    public CompletionStage<Response> executeWorkflow(Map<String, Object> request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String workflowId = (String) request.get("workflowId");
                Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
                
                LOG.infof("Executing workflow: %s with parameters: %s", workflowId, parameters);

                // Mock workflow execution
                Map<String, Object> response = Map.of(
                    "workflowId", workflowId,
                    "executionId", UUID.randomUUID().toString(),
                    "status", "started",
                    "message", "Workflow execution initiated",
                    "timestamp", new Date().toInstant().toString()
                );

                return Response.ok(Map.of(
                    "success", true,
                    "data", response
                )).build();

            } catch (Exception e) {
                LOG.error("Error executing workflow", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                        "success", false,
                        "error", Map.of(
                            "code", "WORKFLOW_EXECUTION_ERROR",
                            "message", "Failed to execute workflow",
                            "details", e.getMessage()
                        )
                    )).build();
            }
        });
    }

    private String getAgentEndpoint(String agentId) {
        switch (agentId) {
            case "workshop-chat": return workshopChatEndpoint;
            case "template-converter": return templateConverterEndpoint;
            case "content-creator": return contentCreatorEndpoint;
            case "source-manager": return sourceManagerEndpoint;
            case "research-validation": return researchValidationEndpoint;
            case "documentation-pipeline": return documentationPipelineEndpoint;
            case "human-oversight": return humanOversightEndpoint;
            default: return null;
        }
    }

    private Map<String, Object> createWorkflowTemplate(String id, String name, String description, 
                                                      List<String> agents, Map<String, Object> defaultParams) {
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("name", name);
        template.put("description", description);
        template.put("agents", agents);
        template.put("defaultParameters", defaultParams);
        template.put("estimatedDuration", "5-15 minutes");
        return template;
    }
}
