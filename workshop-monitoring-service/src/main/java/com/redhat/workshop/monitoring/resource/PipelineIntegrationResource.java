package com.redhat.workshop.monitoring.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.Entity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.redhat.workshop.monitoring.service.AgentOrchestrationService;
import com.redhat.workshop.monitoring.service.ApprovalService;
import com.redhat.workshop.monitoring.service.RepositoryClassificationService;
import com.redhat.workshop.monitoring.model.pipeline.*;
import com.redhat.workshop.monitoring.model.pipeline.UpdateWorkshopRequest;
import com.redhat.workshop.monitoring.model.pipeline.ApproveUpdateRequest;
import com.redhat.workshop.monitoring.model.pipeline.UpdateRAGContentRequest;
import com.redhat.workshop.monitoring.model.pipeline.ValidateReferencesRequest;
import com.redhat.workshop.monitoring.model.pipeline.EnhanceWithReferencesRequest;
import com.redhat.workshop.monitoring.model.pipeline.PipelineApprovalRequest;
import com.redhat.workshop.monitoring.model.pipeline.PipelineApprovalResponse;
import com.redhat.workshop.monitoring.model.pipeline.PipelineApprovalDecision;
import com.redhat.workshop.monitoring.model.ApprovalRequest;
import com.redhat.workshop.monitoring.model.ApprovalDecision;
import com.redhat.workshop.monitoring.model.ApprovalStatus;
import com.redhat.workshop.monitoring.model.RepositoryClassification;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Pipeline Integration Resource
 * Implements ADR-0018 Quarkus Middleware Architecture
 * Provides middleware/API gateway between Tekton pipelines and agents
 */
@Path("/api/pipeline")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PipelineIntegrationResource {

    private static final Logger LOG = Logger.getLogger(PipelineIntegrationResource.class);

    @Inject
    AgentOrchestrationService agentOrchestrationService;

    @Inject
    ApprovalService approvalService;

    @Inject
    RepositoryClassificationService repositoryClassificationService;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    // HTTP client for direct Gitea API calls
    private final Client httpClient = ClientBuilder.newClient();

    /**
     * Get pipeline configuration including valid parameters and validation types
     * ADR-0036: Pipeline Parameter and Validation Type Standards
     */
    @GET
    @Path("/config")
    public Response getPipelineConfiguration() {
        try {
            LOG.info("Fetching pipeline configuration and validation types");

            Map<String, Object> config = new HashMap<>();

            // Validation types by agent
            Map<String, Object> validationTypes = new HashMap<>();

            // Research Validation Agent validation types
            Map<String, Object> researchValidation = new HashMap<>();
            researchValidation.put("agent", "research-validation-agent");
            researchValidation.put("endpoint", "http://research-validation-agent:80");
            researchValidation.put("supported_types", List.of(
                "new-workshop-validation",
                "enhancement-analysis",
                "enhancement-validation"
            ));
            researchValidation.put("default_type", "new-workshop-validation");
            validationTypes.put("research-validation", researchValidation);

            // Template Converter Agent analysis types
            Map<String, Object> templateConverter = new HashMap<>();
            templateConverter.put("agent", "template-converter-agent");
            templateConverter.put("endpoint", "http://template-converter-agent:80");
            templateConverter.put("supported_types", List.of(
                "repository-analysis",
                "workshop-detection",
                "template-classification"
            ));
            templateConverter.put("default_type", "repository-analysis");
            validationTypes.put("template-converter", templateConverter);

            // Content Creator Agent content types
            Map<String, Object> contentCreator = new HashMap<>();
            contentCreator.put("agent", "content-creator-agent");
            contentCreator.put("endpoint", "http://content-creator-agent:80");
            contentCreator.put("supported_types", List.of(
                "content-generation",
                "content-enhancement",
                "content-validation"
            ));
            contentCreator.put("default_type", "content-generation");
            validationTypes.put("content-creator", contentCreator);

            config.put("validation_types", validationTypes);

            // Standard pipeline parameters
            Map<String, Object> parameters = new HashMap<>();

            // Required parameters for all pipelines
            Map<String, Object> requiredParams = new HashMap<>();

            Map<String, Object> repositoryUrl = new HashMap<>();
            repositoryUrl.put("type", "string");
            repositoryUrl.put("description", "Source repository URL for workshop creation");
            repositoryUrl.put("required", true);
            repositoryUrl.put("format", "https://github.com/owner/repo.git");
            repositoryUrl.put("validation", "^https://github\\.com/[\\w-]+/[\\w-]+(\\.git)?$");
            repositoryUrl.put("examples", List.of(
                "https://github.com/jeremyrdavis/dddhexagonalworkshop.git",
                "https://github.com/tosin2013/ansible-controller-cac.git"
            ));
            requiredParams.put("repository-url", repositoryUrl);

            Map<String, Object> workshopName = new HashMap<>();
            workshopName.put("type", "string");
            workshopName.put("description", "Name for the new workshop (used for file naming and identification)");
            workshopName.put("required", true);
            workshopName.put("format", "lowercase-with-hyphens");
            workshopName.put("validation", "^[a-z0-9-]+$");
            workshopName.put("max_length", 50);
            workshopName.put("examples", List.of(
                "ddd-hexagonal-workshop",
                "ansible-automation-basics",
                "openshift-deployment-guide"
            ));
            requiredParams.put("workshop-name", workshopName);

            parameters.put("required", requiredParams);

            // Optional parameters
            Map<String, Object> optionalParams = new HashMap<>();

            Map<String, Object> baseTemplate = new HashMap<>();
            baseTemplate.put("type", "string");
            baseTemplate.put("description", "Base template to use for workshop creation");
            baseTemplate.put("required", false);
            baseTemplate.put("default", "showroom_template_default");
            baseTemplate.put("valid_values", List.of(
                "showroom_template_default",
                "antora_template",
                "custom_template"
            ));
            optionalParams.put("base-template", baseTemplate);

            Map<String, Object> autoApprove = new HashMap<>();
            autoApprove.put("type", "string");
            autoApprove.put("description", "Auto-approve human-in-the-loop steps (for testing)");
            autoApprove.put("required", false);
            autoApprove.put("default", "false");
            autoApprove.put("valid_values", List.of("true", "false"));
            optionalParams.put("auto-approve", autoApprove);

            Map<String, Object> humanApprover = new HashMap<>();
            humanApprover.put("type", "string");
            humanApprover.put("description", "Human approver for manual approval steps");
            humanApprover.put("required", false);
            humanApprover.put("default", "system-operator");
            humanApprover.put("validation", "^[a-z0-9-]+$");
            humanApprover.put("examples", List.of("system-operator", "workshop-admin", "content-reviewer"));
            optionalParams.put("human-approver", humanApprover);

            parameters.put("optional", optionalParams);
            config.put("parameters", parameters);

            // Workspace configuration
            Map<String, Object> workspaces = new HashMap<>();

            Map<String, Object> sharedData = new HashMap<>();
            sharedData.put("name", "shared-data");
            sharedData.put("description", "Shared workspace for workshop content");
            sharedData.put("required", true);
            sharedData.put("pvc_name", "workshop-shared-pvc");
            sharedData.put("access_mode", "ReadWriteMany");
            sharedData.put("storage_class", "ocs-storagecluster-cephfs");
            workspaces.put("shared-data", sharedData);

            Map<String, Object> giteaAuth = new HashMap<>();
            giteaAuth.put("name", "gitea-auth");
            giteaAuth.put("description", "Gitea authentication credentials");
            giteaAuth.put("required", false);
            giteaAuth.put("secret_name", "gitea-auth-secret");
            workspaces.put("gitea-auth", giteaAuth);

            config.put("workspaces", workspaces);

            // Pipeline-specific configurations
            Map<String, Object> pipelines = new HashMap<>();

            // workflow-1-intelligent-workshop
            Map<String, Object> intelligentWorkflow = new HashMap<>();
            intelligentWorkflow.put("name", "workflow-1-intelligent-workshop");
            intelligentWorkflow.put("description", "ADR-0001 Intelligent Workshop Creation Pipeline");
            intelligentWorkflow.put("required_parameters", List.of(
                "repository-url", "workshop-name", "auto-detect-workflow", "human-approver", "auto-approve"
            ));
            intelligentWorkflow.put("required_workspaces", List.of("shared-data", "gitea-auth"));
            intelligentWorkflow.put("validation_types_used", List.of("new-workshop-validation"));
            intelligentWorkflow.put("parameter_defaults", Map.of(
                "auto-detect-workflow", "true",
                "human-approver", "system-operator",
                "auto-approve", "false"
            ));
            pipelines.put("workflow-1-intelligent-workshop", intelligentWorkflow);

            // workflow-1-simple-corrected
            Map<String, Object> simpleWorkflow = new HashMap<>();
            simpleWorkflow.put("name", "workflow-1-simple-corrected");
            simpleWorkflow.put("description", "Workflow 1: Simple Corrected Test");
            simpleWorkflow.put("required_parameters", List.of("repository-url", "workshop-name", "base-template"));
            simpleWorkflow.put("required_workspaces", List.of("shared-data"));
            simpleWorkflow.put("validation_types_used", List.of()); // No validation step
            simpleWorkflow.put("parameter_defaults", Map.of("base-template", "showroom_template_default"));
            pipelines.put("workflow-1-simple-corrected", simpleWorkflow);

            // workflow-3-enhance-workshop
            Map<String, Object> enhanceWorkflow = new HashMap<>();
            enhanceWorkflow.put("name", "workflow-3-enhance-workshop");
            enhanceWorkflow.put("description", "Workflow 3: Enhance Existing Workshop");
            enhanceWorkflow.put("required_parameters", List.of("repository-url", "workshop-name", "original-workshop-url"));
            enhanceWorkflow.put("required_workspaces", List.of("shared-data", "gitea-auth"));
            enhanceWorkflow.put("validation_types_used", List.of("enhancement-analysis", "enhancement-validation"));
            enhanceWorkflow.put("parameter_defaults", Map.of());
            pipelines.put("workflow-3-enhance-workshop", enhanceWorkflow);

            config.put("pipelines", pipelines);

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("version", "1.0.0");
            metadata.put("last_updated", new Date().toInstant().toString());
            metadata.put("documentation", "docs/reference/pipeline-parameters.md");
            metadata.put("adr_reference", "ADR-0036: Pipeline Parameter and Validation Type Standards");
            config.put("metadata", metadata);

            return Response.ok(Map.of(
                "success", true,
                "data", config,
                "message", "Pipeline configuration retrieved successfully"
            )).build();

        } catch (Exception e) {
            LOG.error("Error fetching pipeline configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "CONFIG_FETCH_ERROR",
                        "message", "Failed to fetch pipeline configuration",
                        "details", e.getMessage()
                    )
                )).build();
        }
    }

    /**
     * Validate pipeline parameters before execution
     * ADR-0036: Pipeline Parameter and Validation Type Standards
     */
    @POST
    @Path("/validate-parameters")
    public Response validatePipelineParameters(Map<String, Object> parameters) {
        try {
            LOG.infof("Validating pipeline parameters: %s", parameters.keySet());

            Map<String, Object> validationResult = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate repository-url
            String repositoryUrl = (String) parameters.get("repository-url");
            if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
                errors.add("repository-url is required");
            } else if (!repositoryUrl.matches("^https://github\\.com/[\\w-]+/[\\w-]+(\\.git)?$")) {
                errors.add("repository-url must be a valid GitHub URL (https://github.com/owner/repo.git)");
            }

            // Validate workshop-name
            String workshopName = (String) parameters.get("workshop-name");
            if (workshopName == null || workshopName.trim().isEmpty()) {
                errors.add("workshop-name is required");
            } else if (!workshopName.matches("^[a-z0-9-]+$")) {
                errors.add("workshop-name must contain only lowercase letters, numbers, and hyphens");
            } else if (workshopName.length() > 50) {
                errors.add("workshop-name must be 50 characters or less");
            }

            // Validate validation-type if present
            String validationType = (String) parameters.get("validation-type");
            if (validationType != null) {
                List<String> validTypes = List.of(
                    "new-workshop-validation",
                    "enhancement-analysis",
                    "enhancement-validation"
                );
                if (!validTypes.contains(validationType)) {
                    errors.add("validation-type must be one of: " + String.join(", ", validTypes));
                }
            }

            // Validate base-template if present
            String baseTemplate = (String) parameters.get("base-template");
            if (baseTemplate != null) {
                List<String> validTemplates = List.of(
                    "showroom_template_default",
                    "antora_template",
                    "custom_template"
                );
                if (!validTemplates.contains(baseTemplate)) {
                    warnings.add("base-template should be one of: " + String.join(", ", validTemplates));
                }
            }

            // Validate auto-approve if present
            String autoApprove = (String) parameters.get("auto-approve");
            if (autoApprove != null && !List.of("true", "false").contains(autoApprove)) {
                errors.add("auto-approve must be 'true' or 'false'");
            }

            boolean isValid = errors.isEmpty();

            validationResult.put("valid", isValid);
            validationResult.put("errors", errors);
            validationResult.put("warnings", warnings);
            validationResult.put("validated_parameters", parameters.keySet());

            if (isValid) {
                validationResult.put("message", "All parameters are valid");
                return Response.ok(Map.of(
                    "success", true,
                    "data", validationResult
                )).build();
            } else {
                validationResult.put("message", "Parameter validation failed");
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                        "success", false,
                        "data", validationResult
                    )).build();
            }

        } catch (Exception e) {
            LOG.error("Error validating pipeline parameters", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "success", false,
                    "error", Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", "Failed to validate pipeline parameters",
                        "details", e.getMessage()
                    )
                )).build();
        }
    }

    /**
     * Content Creator Agent - Create Workshop Content
     * Replaces direct agent calls from Tekton pipelines
     */
    @POST
    @Path("/content-creator/create-workshop")
    public Response createWorkshopContent(CreateWorkshopRequest request) {
        LOG.infof("🔧 PIPELINE REQUEST: Create workshop content for '%s'", request.getWorkshopName());

        try {
            // Validate request
            if (request.getWorkshopName() == null || request.getWorkshopName().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Workshop name is required"))
                    .build();
            }

            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("template_name", request.getBaseTemplate() != null ? request.getBaseTemplate() : "showroom_template_default");
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("technology_focus", request.getRepositoryUrl());
            parameters.put("customization_level", "comprehensive");

            if (request.getTargetDirectory() != null) {
                parameters.put("target_directory", request.getTargetDirectory());
            }

            // Call Content Creator Agent via orchestration service
            Map<String, Object> agentResult = agentOrchestrationService.invokeAgent(
                "content-creator",
                "clone_showroom_template_tool",
                parameters
            );

            // Transform agent response to pipeline-expected format
            LOG.infof("🔧 AGENT RESULT BEFORE TRANSFORM: %s", agentResult);
            Map<String, Object> pipelineResponse = transformContentCreatorResponse(agentResult, request);
            LOG.infof("🔧 PIPELINE RESPONSE AFTER TRANSFORM: %s", pipelineResponse);

            LOG.infof("Content Creator Agent response transformed for pipeline: %s", pipelineResponse.get("status"));
            return Response.ok(pipelineResponse).build();

        } catch (Exception e) {
            LOG.errorf("Failed to create workshop content: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to create workshop content: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Content Creator Agent - Enhance Workshop Content
     * For Workflow 3 (existing workshop enhancement)
     */
    @POST
    @Path("/content-creator/enhance-workshop")
    public Response enhanceWorkshopContent(EnhanceWorkshopRequest request) {
        LOG.infof("Pipeline request: Enhance workshop content for '%s'", request.getWorkshopName());
        
        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_url", request.getRepositoryUrl());
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("enhancement_plan", request.getEnhancementPlan());
            parameters.put("original_content", request.getOriginalContent());
            parameters.put("enhancement_type", "content-update");

            // Call Content Creator Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "content-creator", 
                "enhance_workshop_content_tool", 
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to enhance workshop content: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to enhance workshop content: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Template Converter Agent - Analyze Repository
     * Analyzes source repository for workshop conversion
     */
    @POST
    @Path("/template-converter/analyze-repository")
    public Response analyzeRepository(AnalyzeRepositoryRequest request) {
        LOG.infof("Pipeline request: Analyze repository '%s'", request.getRepositoryUrl());
        
        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_url", request.getRepositoryUrl());
            parameters.put("analysis_depth", request.getAnalysisDepth() != null ? request.getAnalysisDepth() : "comprehensive");
            parameters.put("target_format", "rhpds_showroom");

            // Call Template Converter Agent
            Map<String, Object> agentResult = agentOrchestrationService.invokeAgent(
                "template-converter",
                "analyze_repository_tool",
                parameters
            );

            // Transform agent response to pipeline-expected format
            Map<String, Object> pipelineResponse = transformTemplateConverterResponse(agentResult, request);

            return Response.ok(pipelineResponse).build();

        } catch (Exception e) {
            LOG.errorf("Failed to analyze repository: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to analyze repository: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Source Manager Agent - Create Repository
     * Creates workshop repository in Gitea
     */
    @POST
    @Path("/source-manager/create-repository")
    public Response createRepository(CreateRepositoryRequest request) {
        LOG.infof("Pipeline request: Create repository '%s'", request.getRepositoryName());
        
        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_name", request.getRepositoryName());
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("gitea_url", request.getGiteaUrl());
            parameters.put("visibility", request.getVisibility() != null ? request.getVisibility() : "public");

            // Call Source Manager Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "source-manager", 
                "manage_workshop_repository_tool", 
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to create repository: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to create repository: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Research Validation Agent - Validate Content
     * Validates workshop content for accuracy and completeness
     */
    @POST
    @Path("/research-validation/validate-content")
    public Response validateContent(ValidateContentRequest request) {
        LOG.infof("Pipeline request: Validate content for '%s'", request.getWorkshopName());
        
        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("validation_scope", request.getValidationScope() != null ? request.getValidationScope() : "comprehensive");
            parameters.put("workshop_name", request.getWorkshopName());

            // Call Research Validation Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "research-validation", 
                "validate_workshop_content_tool", 
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to validate content: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to validate content: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Documentation Pipeline Agent - Generate Documentation
     * Generates documentation for workshop content
     */
    @POST
    @Path("/documentation-pipeline/generate-docs")
    public Response generateDocumentation(GenerateDocsRequest request) {
        LOG.infof("Pipeline request: Generate documentation for '%s'", request.getWorkshopName());
        
        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("documentation_type", request.getDocumentationType() != null ? request.getDocumentationType() : "comprehensive");
            parameters.put("workshop_name", request.getWorkshopName());

            // Call Documentation Pipeline Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "documentation-pipeline", 
                "generate_workshop_documentation_tool", 
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to generate documentation: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to generate documentation: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Workshop Chat Agent - Setup RAG
     * Sets up RAG system for workshop chat assistance
     */
    @POST
    @Path("/workshop-chat/setup-rag")
    public Response setupWorkshopRAG(SetupRAGRequest request) {
        LOG.infof("Pipeline request: Setup RAG for workshop '%s'", request.getWorkshopName());

        try {
            // Prepare A2A protocol parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("rag_configuration", request.getRagConfiguration() != null ? request.getRagConfiguration() : "default");

            // Call Workshop Chat Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "workshop-chat",
                "setup_workshop_rag_tool",
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to setup workshop RAG: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to setup workshop RAG: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Research Validation Agent - Update RAG with External References (NEW)
     * Validates external references and updates RAG knowledge base for content quality
     */
    @POST
    @Path("/research-validation/update-rag-content")
    public Response updateRAGWithExternalContent(UpdateRAGContentRequest request) {
        LOG.infof("Pipeline request: Update RAG content for workshop '%s' with external references", request.getWorkshopName());

        try {
            // Prepare A2A protocol parameters for RAG content update
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("external_references", request.getExternalReferences());
            parameters.put("quality_threshold", request.getQualityThreshold() != null ? request.getQualityThreshold() : 0.7);
            parameters.put("update_mode", request.getUpdateMode() != null ? request.getUpdateMode() : "incremental");
            parameters.put("validation_scope", "external-references");

            // Call Research Validation Agent for external content validation
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "research-validation",
                "update_rag_with_external_content_tool",
                parameters
            );

            LOG.infof("RAG content update completed for workshop '%s'", request.getWorkshopName());
            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to update RAG content: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to update RAG content: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Research Validation Agent - Validate External References (NEW)
     * Checks external links and references for accuracy and freshness
     */
    @POST
    @Path("/research-validation/validate-external-references")
    public Response validateExternalReferences(ValidateReferencesRequest request) {
        LOG.infof("Pipeline request: Validate external references for workshop '%s'", request.getWorkshopName());

        try {
            // Prepare A2A protocol parameters for reference validation
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("reference_types", request.getReferenceTypes() != null ? request.getReferenceTypes() : "all");
            parameters.put("check_accessibility", request.isCheckAccessibility());
            parameters.put("check_freshness", request.isCheckFreshness());
            parameters.put("quality_scoring", request.isQualityScoring());

            // Call Research Validation Agent for reference validation
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "research-validation",
                "validate_external_references_tool",
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to validate external references: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to validate external references: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Content Creator Agent - Enhance Content with External References (NEW)
     * Improves workshop content by integrating validated external references
     */
    @POST
    @Path("/content-creator/enhance-with-references")
    public Response enhanceContentWithReferences(EnhanceWithReferencesRequest request) {
        LOG.infof("Pipeline request: Enhance workshop '%s' content with external references", request.getWorkshopName());

        try {
            // Prepare A2A protocol parameters for content enhancement
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("workshop_content", request.getWorkshopContent());
            parameters.put("validated_references", request.getValidatedReferences());
            parameters.put("enhancement_strategy", request.getEnhancementStrategy() != null ? request.getEnhancementStrategy() : "contextual");
            parameters.put("quality_threshold", request.getQualityThreshold() != null ? request.getQualityThreshold() : 0.8);

            // Call Content Creator Agent for content enhancement
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "content-creator",
                "enhance_content_with_references_tool",
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to enhance content with references: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to enhance content with references: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Source Manager Agent - Update Existing Workshop
     * Updates existing workshop repository in Gitea with human-in-the-loop approval
     * This supports workshop maintenance and content updates
     */
    @POST
    @Path("/source-manager/update-workshop")
    public Response updateExistingWorkshop(UpdateWorkshopRequest request) {
        LOG.infof("Pipeline request: Update existing workshop '%s' in repository '%s'",
                 request.getWorkshopName(), request.getRepositoryName());

        try {
            // Validate request
            if (request.getRepositoryName() == null || request.getRepositoryName().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Repository name is required"))
                    .build();
            }

            // Prepare A2A protocol parameters for workshop update
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_name", request.getRepositoryName());
            parameters.put("workshop_name", request.getWorkshopName());
            parameters.put("updated_content", request.getUpdatedContent());
            parameters.put("update_type", request.getUpdateType() != null ? request.getUpdateType() : "content-update");
            parameters.put("gitea_url", request.getGiteaUrl());
            parameters.put("branch_name", request.getBranchName() != null ? request.getBranchName() : "main");
            parameters.put("commit_message", request.getCommitMessage() != null ? request.getCommitMessage() : "Workshop maintenance update");

            // Add human oversight parameters
            parameters.put("require_approval", request.isRequireApproval());
            parameters.put("approver", request.getApprover());
            parameters.put("change_summary", request.getChangeSummary());

            // Call Source Manager Agent
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "source-manager",
                "update_workshop_repository_tool",
                parameters
            );

            LOG.infof("Workshop update initiated for '%s' - approval required: %s",
                     request.getRepositoryName(), request.isRequireApproval());
            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to update workshop: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to update workshop: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Human Oversight - Approve Workshop Update
     * Handles human approval for workshop maintenance updates
     */
    @POST
    @Path("/human-oversight/approve-workshop-update")
    public Response approveWorkshopUpdate(ApproveUpdateRequest request) {
        LOG.infof("Human oversight: Approve workshop update for '%s' by '%s'",
                 request.getRepositoryName(), request.getApprover());

        try {
            // Prepare A2A protocol parameters for approval
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("approval_id", request.getApprovalId());
            parameters.put("repository_name", request.getRepositoryName());
            parameters.put("approver", request.getApprover());
            parameters.put("approval_decision", request.getApprovalDecision());
            parameters.put("approval_comments", request.getApprovalComments());
            parameters.put("timestamp", System.currentTimeMillis());

            // Call Human Oversight Coordinator
            Map<String, Object> result = agentOrchestrationService.invokeAgent(
                "human-oversight",
                "process_workshop_approval_tool",
                parameters
            );

            return Response.ok(result).build();

        } catch (Exception e) {
            LOG.errorf("Failed to process workshop approval: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to process workshop approval: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Health check endpoint for pipeline integration
     */
    @GET
    @Path("/health")
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "Pipeline Integration Middleware");
        health.put("profile", profile);
        health.put("timestamp", System.currentTimeMillis());

        return Response.ok(health).build();
    }

    // ========================================
    // MOCK ENDPOINTS FOR DEVELOPMENT TESTING
    // ========================================

    /**
     * Mock Content Creator - Create Workshop (Development Only)
     */
    @POST
    @Path("/mock/content-creator/create-workshop")
    public Response mockCreateWorkshopContent(CreateWorkshopRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Create workshop content for '%s'", request.getWorkshopName());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("workshop_content", "Mock workshop content for " + request.getWorkshopName());
        mockResponse.put("content_summary", "Successfully created mock workshop content");
        mockResponse.put("template_path", "/workspace/shared-data/final-output/mock-template");
        mockResponse.put("files_created", 15);
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Template Converter - Analyze Repository (Development Only)
     */
    @POST
    @Path("/mock/template-converter/analyze-repository")
    public Response mockAnalyzeRepository(AnalyzeRepositoryRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Analyze repository '%s'", request.getRepositoryUrl());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("repository_analysis", "Mock analysis of " + request.getRepositoryUrl());
        mockResponse.put("workshop_type", "tutorial");
        mockResponse.put("technology_stack", "Java, Spring Boot, OpenShift");
        mockResponse.put("complexity_level", "intermediate");
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Source Manager - Create Repository (Development Only)
     */
    @POST
    @Path("/mock/source-manager/create-repository")
    public Response mockCreateRepository(CreateRepositoryRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Create repository '%s'", request.getRepositoryName());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("repository_url", "http://mock-gitea.example.com/workshop-system/" + request.getRepositoryName());
        mockResponse.put("repository_name", request.getRepositoryName());
        mockResponse.put("deployment_status", "deployed");
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Source Manager - Update Workshop (Development Only)
     */
    @POST
    @Path("/mock/source-manager/update-workshop")
    public Response mockUpdateWorkshop(UpdateWorkshopRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Update workshop '%s' in repository '%s'", request.getWorkshopName(), request.getRepositoryName());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("repository_name", request.getRepositoryName());
        mockResponse.put("workshop_name", request.getWorkshopName());
        mockResponse.put("update_type", request.getUpdateType());
        mockResponse.put("branch_name", request.getBranchName());
        mockResponse.put("commit_id", "mock-commit-" + System.currentTimeMillis());
        mockResponse.put("approval_required", request.isRequireApproval());

        if (request.isRequireApproval()) {
            mockResponse.put("approval_id", "approval-" + System.currentTimeMillis());
            mockResponse.put("approval_status", "pending");
            mockResponse.put("approver", request.getApprover());
        } else {
            mockResponse.put("deployment_status", "deployed");
        }

        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Human Oversight - Approve Workshop Update (Development Only)
     */
    @POST
    @Path("/mock/human-oversight/approve-workshop-update")
    public Response mockApproveWorkshopUpdate(ApproveUpdateRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Process approval '%s' for repository '%s' - Decision: %s",
                 request.getApprovalId(), request.getRepositoryName(), request.getApprovalDecision());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("approval_id", request.getApprovalId());
        mockResponse.put("repository_name", request.getRepositoryName());
        mockResponse.put("approver", request.getApprover());
        mockResponse.put("approval_decision", request.getApprovalDecision());
        mockResponse.put("processed_at", System.currentTimeMillis());

        if ("approved".equals(request.getApprovalDecision())) {
            mockResponse.put("deployment_status", "deployed");
            mockResponse.put("deployment_url", "http://mock-gitea.example.com/workshop-system/" + request.getRepositoryName());
        } else if ("rejected".equals(request.getApprovalDecision())) {
            mockResponse.put("deployment_status", "rejected");
            mockResponse.put("rejection_reason", request.getApprovalComments());
        } else if ("needs_changes".equals(request.getApprovalDecision())) {
            mockResponse.put("deployment_status", "needs_changes");
            mockResponse.put("requested_changes", request.getRequestedChanges());
        }

        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Research Validation - Update RAG Content (Development Only)
     */
    @POST
    @Path("/mock/research-validation/update-rag-content")
    public Response mockUpdateRAGContent(UpdateRAGContentRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Update RAG content for workshop '%s' with %d external references",
                 request.getWorkshopName(),
                 request.getExternalReferences() != null ? request.getExternalReferences().size() : 0);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("workshop_name", request.getWorkshopName());
        mockResponse.put("rag_update_mode", request.getUpdateMode());
        mockResponse.put("quality_threshold", request.getQualityThreshold());
        mockResponse.put("references_processed", request.getExternalReferences() != null ? request.getExternalReferences().size() : 0);
        mockResponse.put("references_validated", 8);
        mockResponse.put("references_integrated", 6);
        mockResponse.put("quality_score_improvement", 0.15);
        mockResponse.put("rag_knowledge_updated", true);
        mockResponse.put("content_freshness_score", 0.92);
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Research Validation - Validate External References (Development Only)
     */
    @POST
    @Path("/mock/research-validation/validate-external-references")
    public Response mockValidateExternalReferences(ValidateReferencesRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Validate external references for workshop '%s'", request.getWorkshopName());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("workshop_name", request.getWorkshopName());
        mockResponse.put("reference_types", request.getReferenceTypes());
        mockResponse.put("total_references_found", 12);
        mockResponse.put("accessible_references", 10);
        mockResponse.put("broken_links", 2);
        mockResponse.put("outdated_content", 1);
        mockResponse.put("high_quality_sources", 8);
        mockResponse.put("average_authority_score", 0.78);
        mockResponse.put("freshness_score", 0.85);
        mockResponse.put("validation_timestamp", System.currentTimeMillis());
        mockResponse.put("recommendations", "Update 2 broken links, refresh 1 outdated reference");
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    /**
     * Mock Content Creator - Enhance with References (Development Only)
     */
    @POST
    @Path("/mock/content-creator/enhance-with-references")
    public Response mockEnhanceWithReferences(EnhanceWithReferencesRequest request) {
        if (!"dev".equals(profile)) {
            return Response.status(404).entity(Map.of("error", "Mock endpoints only available in dev profile")).build();
        }

        LOG.infof("MOCK: Enhance workshop '%s' content with %d validated references",
                 request.getWorkshopName(),
                 request.getValidatedReferences() != null ? request.getValidatedReferences().size() : 0);

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "success");
        mockResponse.put("workshop_name", request.getWorkshopName());
        mockResponse.put("enhancement_strategy", request.getEnhancementStrategy());
        mockResponse.put("quality_threshold", request.getQualityThreshold());
        mockResponse.put("references_integrated", request.getValidatedReferences() != null ? request.getValidatedReferences().size() : 0);
        mockResponse.put("content_sections_enhanced", 5);
        mockResponse.put("new_learning_resources_added", 3);
        mockResponse.put("quality_score_before", 0.72);
        mockResponse.put("quality_score_after", 0.89);
        mockResponse.put("content_completeness_improvement", 0.23);
        mockResponse.put("enhanced_content_preview", "Added contextual links to official documentation, integrated relevant tutorials, and included authoritative API references");
        mockResponse.put("mock", true);

        return Response.ok(mockResponse).build();
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Transform Content Creator Agent response to pipeline-expected format
     * Converts generic agent response to specific fields expected by Tekton tasks
     */
    private Map<String, Object> transformContentCreatorResponse(Map<String, Object> agentResult, CreateWorkshopRequest request) {
        Map<String, Object> pipelineResponse = new HashMap<>();

        // Extract status from agent response
        String status = (String) agentResult.get("status");
        String result = (String) agentResult.get("result");
        String taskId = (String) agentResult.get("task_id");

        if ("completed".equals(status) || "success".equals(status)) {
            // Success case - provide expected fields for pipeline
            pipelineResponse.put("workshop_content", result != null ? result : "Workshop content created successfully");
            pipelineResponse.put("content_summary", String.format("Workshop '%s' content created via Content Creator Agent", request.getWorkshopName()));
            pipelineResponse.put("status", "success");
            pipelineResponse.put("template_used", request.getBaseTemplate() != null ? request.getBaseTemplate() : "showroom_template_default");
            pipelineResponse.put("workshop_name", request.getWorkshopName());

            if (request.getTargetDirectory() != null) {
                pipelineResponse.put("target_directory", request.getTargetDirectory());
            }

        } else {
            // Error case - provide error information
            pipelineResponse.put("workshop_content", "");
            pipelineResponse.put("content_summary", "Failed to create workshop content: " + (result != null ? result : "Unknown error"));
            pipelineResponse.put("status", "error");
            pipelineResponse.put("error", result != null ? result : "Content creation failed");
        }

        // Always include task tracking
        if (taskId != null) {
            pipelineResponse.put("task_id", taskId);
        }

        return pipelineResponse;
    }

    /**
     * Transform Template Converter Agent response to pipeline-expected format
     */
    private Map<String, Object> transformTemplateConverterResponse(Map<String, Object> agentResult, AnalyzeRepositoryRequest request) {
        Map<String, Object> pipelineResponse = new HashMap<>();

        String status = (String) agentResult.get("status");
        String result = (String) agentResult.get("result");
        String taskId = (String) agentResult.get("task_id");

        if ("completed".equals(status) || "success".equals(status)) {
            // Success case
            pipelineResponse.put("analysis_result", result != null ? result : "Repository analysis completed successfully");
            pipelineResponse.put("repository_url", request.getRepositoryUrl());
            pipelineResponse.put("analysis_depth", request.getAnalysisDepth() != null ? request.getAnalysisDepth() : "comprehensive");
            pipelineResponse.put("status", "success");
        } else {
            // Error case
            pipelineResponse.put("analysis_result", "");
            pipelineResponse.put("status", "error");
            pipelineResponse.put("error", result != null ? result : "Repository analysis failed");
        }

        if (taskId != null) {
            pipelineResponse.put("task_id", taskId);
        }

        return pipelineResponse;
    }

    // ========================================
    // PIPELINE APPROVAL ENDPOINTS
    // ========================================

    /**
     * Submit Pipeline Approval Request
     * Generic endpoint for Tekton pipelines to submit approval requests
     * Bridges Tekton tasks with existing ApprovalService infrastructure
     */
    @POST
    @Path("/approval/submit")
    public Response submitPipelineApproval(PipelineApprovalRequest request) {
        LOG.infof("🔧 PIPELINE APPROVAL: Submit approval request for workflow '%s', type '%s'",
                 request.getWorkflowId(), request.getApprovalType());

        try {
            // Validate request
            if (request.getApprovalType() == null || request.getApprovalType().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Approval type is required"))
                    .build();
            }

            if (request.getWorkflowId() == null || request.getWorkflowId().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Workflow ID is required"))
                    .build();
            }

            // Transform to ApprovalRequest and submit
            ApprovalRequest approvalRequest = request.toApprovalRequest();
            ApprovalRequest submittedApproval = approvalService.submitApproval(approvalRequest);

            // Transform response for pipeline compatibility
            PipelineApprovalResponse response = PipelineApprovalResponse.fromApprovalRequest(submittedApproval);
            response.setWorkflowId(request.getWorkflowId());

            LOG.infof("🔧 PIPELINE APPROVAL: Submitted successfully - ID: %s, Status: %s",
                     response.getApprovalId(), response.getStatus());

            return Response.ok(response).build();

        } catch (Exception e) {
            LOG.errorf("Failed to submit pipeline approval: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to submit approval request: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Get Pipeline Approval Status
     * Allows Tekton pipelines to poll for approval status and decisions
     */
    @GET
    @Path("/approval/{approvalId}/status")
    public Response getPipelineApprovalStatus(@PathParam("approvalId") String approvalId) {
        LOG.debugf("🔧 PIPELINE APPROVAL: Check status for approval ID: %s", approvalId);

        try {
            // Get approval from service
            ApprovalRequest approval = approvalService.getApprovalById(approvalId);

            if (approval == null) {
                return Response.status(404)
                    .entity(Map.of("error", "Approval not found: " + approvalId))
                    .build();
            }

            // Transform to pipeline response format
            PipelineApprovalResponse response = PipelineApprovalResponse.fromApprovalRequest(approval);

            LOG.debugf("🔧 PIPELINE APPROVAL: Status check - ID: %s, Status: %s, Decision: %s",
                      approvalId, response.getStatus(), response.getDecision());

            return Response.ok(response).build();

        } catch (Exception e) {
            LOG.errorf("Failed to get pipeline approval status: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to get approval status: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Process Pipeline Approval Decision
     * Handles approval decisions from human reviewers via the React interface
     */
    @POST
    @Path("/approval/{approvalId}/decision")
    public Response processPipelineApprovalDecision(@PathParam("approvalId") String approvalId,
                                                   PipelineApprovalDecision decision) {
        LOG.infof("🔧 PIPELINE APPROVAL: Process decision for ID: %s, Decision: %s by %s",
                 approvalId, decision.getDecision(), decision.getApprover());

        try {
            // Transform to ApprovalDecision
            ApprovalDecision approvalDecision = decision.toApprovalDecision();

            // Process decision based on type
            ApprovalRequest updatedApproval;
            if (decision.isApproved()) {
                updatedApproval = approvalService.approveRequest(approvalId, approvalDecision);
            } else if (decision.isRejected()) {
                updatedApproval = approvalService.rejectRequest(approvalId, approvalDecision);
            } else if (decision.needsChanges()) {
                // Handle needs_changes as a special case of rejection
                updatedApproval = approvalService.rejectRequest(approvalId, approvalDecision);
                if (updatedApproval != null) {
                    updatedApproval.setStatus(ApprovalStatus.NEEDS_CHANGES);
                }
            } else {
                return Response.status(400)
                    .entity(Map.of("error", "Invalid decision: " + decision.getDecision()))
                    .build();
            }

            if (updatedApproval == null) {
                return Response.status(404)
                    .entity(Map.of("error", "Approval not found or cannot be updated: " + approvalId))
                    .build();
            }

            // Transform response for pipeline compatibility
            PipelineApprovalResponse response = PipelineApprovalResponse.fromApprovalRequest(updatedApproval);

            LOG.infof("🔧 PIPELINE APPROVAL: Decision processed - ID: %s, Final Status: %s",
                     approvalId, response.getStatus());

            return Response.ok(response).build();

        } catch (IllegalStateException e) {
            LOG.warnf("Invalid approval state for decision: %s", e.getMessage());
            return Response.status(409)
                .entity(Map.of("error", "Approval is not in a valid state for decision: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.errorf("Failed to process pipeline approval decision: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of("error", "Failed to process approval decision: " + e.getMessage()))
                .build();
        }
    }

    // ========================================
    // ADR-0001 DUAL-TEMPLATE STRATEGY ENDPOINTS
    // ========================================

    /**
     * Intelligent Workshop Creation Endpoint
     * Automatically classifies repositories and routes to appropriate workflow based on ADR-0001
     * - Existing workshops → Workflow 3 (Enhancement)
     * - Applications/Tutorial content → Workflow 1 (New Workshop Creation)
     */
    @POST
    @Path("/content-creator/create-workshop-intelligent")
    public Response createWorkshopIntelligent(CreateWorkshopRequest request) {
        LOG.infof("🧠 INTELLIGENT WORKSHOP CREATION: Repository '%s', Workshop '%s'",
                 request.getRepositoryUrl(), request.getWorkshopName());

        try {
            // Validate request
            if (request.getRepositoryUrl() == null || request.getRepositoryUrl().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Repository URL is required for intelligent workshop creation"))
                    .build();
            }

            if (request.getWorkshopName() == null || request.getWorkshopName().trim().isEmpty()) {
                return Response.status(400)
                    .entity(Map.of("error", "Workshop name is required"))
                    .build();
            }

            // Step 1: Classify repository using ADR-0001 logic
            LOG.debugf("🔍 Classifying repository: %s", request.getRepositoryUrl());
            RepositoryClassification classification = repositoryClassificationService.classifyRepository(request.getRepositoryUrl());

            // Step 2: Update request with classification results
            request.setRepositoryClassification(classification.getClassificationType());
            request.setWorkflowType(classification.getRecommendedWorkflow());
            request.setDetectedFramework(classification.getDetectedFramework());

            // Step 3: Log workflow decision
            logWorkflowDecision(request.getRepositoryUrl(), classification);

            // Step 4: Route to appropriate workflow and create Gitea repository
            Response workflowResponse;
            String giteaRepositoryUrl = "";

            if (classification.shouldUseWorkflow3()) {
                // Workflow 3: Enhancement - Clone original workshop
                LOG.infof("🔄 Routing to Workflow 3 (Enhancement) for existing workshop");
                EnhanceWorkshopRequest enhanceRequest = convertToEnhanceRequest(request, classification);
                workflowResponse = enhanceWorkshopContent(enhanceRequest);

                // Create enhanced repository in Gitea
                giteaRepositoryUrl = createGiteaRepository(request.getWorkshopName() + "-enhanced", workflowResponse, classification);

            } else {
                // Workflow 1: New Workshop Creation - Use showroom_template_default
                LOG.infof("🆕 Routing to Workflow 1 (New Creation) for application/tutorial content");
                workflowResponse = createWorkshopContent(request);

                // Create new repository in Gitea
                giteaRepositoryUrl = createGiteaRepository(request.getWorkshopName(), workflowResponse, classification);
            }

            // Step 5: Build unified response with classification metadata and Gitea URL
            return buildUnifiedResponseWithGitea(workflowResponse, classification, request, giteaRepositoryUrl);

        } catch (Exception e) {
            LOG.errorf("Failed to create workshop intelligently: %s", e.getMessage());
            return Response.status(500)
                .entity(Map.of(
                    "error", "Failed to create workshop intelligently: " + e.getMessage(),
                    "repository_url", request.getRepositoryUrl(),
                    "workshop_name", request.getWorkshopName()
                ))
                .build();
        }
    }

    // ========================================
    // WORKFLOW ROUTING HELPER METHODS
    // ========================================

    /**
     * Convert CreateWorkshopRequest to EnhanceWorkshopRequest for Workflow 3
     */
    private EnhanceWorkshopRequest convertToEnhanceRequest(CreateWorkshopRequest request, RepositoryClassification classification) {
        EnhanceWorkshopRequest enhanceRequest = new EnhanceWorkshopRequest();
        enhanceRequest.setRepositoryUrl(request.getRepositoryUrl());
        enhanceRequest.setWorkshopName(request.getWorkshopName());
        enhanceRequest.setEnhancementPlan(String.format(
            "Intelligent enhancement of existing %s workshop. Classification confidence: %.2f. " +
            "Detected framework: %s. Workflow: %s",
            classification.getDetectedFramework(), classification.getConfidence(),
            classification.getDetectedFramework(), classification.getRecommendedWorkflow()
        ));
        enhanceRequest.setEnhancementType("intelligent-classification");

        LOG.debugf("🔄 Converted to EnhanceWorkshopRequest: %s", enhanceRequest.getWorkshopName());
        return enhanceRequest;
    }

    /**
     * Log workflow decision for audit and debugging
     */
    private void logWorkflowDecision(String repositoryUrl, RepositoryClassification classification) {
        LOG.infof("📊 WORKFLOW DECISION: Repository '%s' → Classification: %s, Framework: %s, Workflow: %s, Confidence: %.2f",
                 repositoryUrl,
                 classification.getClassificationType(),
                 classification.getDetectedFramework(),
                 classification.getRecommendedWorkflow(),
                 classification.getConfidence());

        if (!classification.getIndicators().isEmpty()) {
            LOG.debugf("🔍 Classification indicators: %s", String.join(", ", classification.getIndicators()));
        }
    }

    /**
     * Create Gitea repository for workshop content using ADR-0001 compliant template strategy
     */
    private String createGiteaRepository(String workshopName, Response workflowResponse, RepositoryClassification classification) {
        try {
            LOG.infof("📁 Creating Gitea repository for workshop: %s", workshopName);

            // Extract workshop content from workflow response
            Object responseEntity = workflowResponse.getEntity();
            Map<String, Object> responseData = (Map<String, Object>) responseEntity;
            String workshopContent = (String) responseData.getOrDefault("workshop_content", "Generated workshop content");

            // Prepare repository classification data for ADR-0001 workflow routing
            String classificationJson = String.format(
                "{\"workflow_recommendation\":\"%s\",\"detected_framework\":\"%s\",\"confidence\":%.2f,\"classification_type\":\"%s\"}",
                classification.getRecommendedWorkflow(),
                classification.getDetectedFramework(),
                classification.getConfidence(),
                classification.getClassificationType()
            );

            // Call Source Manager Agent with ADR-0001 compliant function
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_analysis", classificationJson);
            parameters.put("workshop_content", workshopContent);
            parameters.put("workshop_name", workshopName);

            LOG.infof("🔧 Calling ADR-0001 compliant Source Manager Agent: create_workshop_repository_tool");
            LOG.debugf("📊 Classification data: %s", classificationJson);

            // Call Source Manager Agent via orchestration service
            Map<String, Object> agentResult = agentOrchestrationService.invokeAgent(
                "source-manager",
                "create_workshop_repository_tool",
                parameters
            );

            if (agentResult != null && "success".equals(agentResult.get("status"))) {
                // Parse ADR-0001 compliant response from create_workshop_repository_tool
                String agentResponse = (String) agentResult.get("response");
                return parseADR0001Response(agentResponse, workshopName);

            } else if (agentResult != null && "completed".equals(agentResult.get("status"))) {
                // Handle completed status from create_workshop_repository_tool
                String result = (String) agentResult.get("result");
                if (result != null && result.contains("simplified implementation")) {
                    LOG.warnf("⚠️ Agent returned simplified implementation, attempting direct Gitea API call");
                    return createGiteaRepositoryDirect(workshopName, workshopContent);
                } else {
                    // Parse ADR-0001 compliant response
                    return parseADR0001Response(result, workshopName);
                }
            } else {
                LOG.warnf("⚠️ Failed to create Gitea repository via Source Manager Agent, attempting direct API call");
                return createGiteaRepositoryDirect(workshopName, workshopContent);
            }

        } catch (Exception e) {
            LOG.errorf("❌ Error creating Gitea repository: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Create Gitea repository directly via API (fallback method)
     */
    private String createGiteaRepositoryDirect(String workshopName, String workshopContent) {
        try {
            LOG.infof("📁 Creating Gitea repository directly via API: %s", workshopName);

            // Get Gitea configuration from environment/config
            String giteaUrl = "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com";
            String giteaApiUrl = giteaUrl + "/api/v1";
            String giteaOrg = "workshop-system";

            // Get Gitea token from secret
            String giteaToken = System.getenv("GITEA_TOKEN");
            if (giteaToken == null || giteaToken.isEmpty()) {
                LOG.warnf("⚠️ GITEA_TOKEN not available for direct API call");
                return "";
            }

            // Create repository via Gitea API
            Map<String, Object> repoData = new HashMap<>();
            repoData.put("name", workshopName);
            repoData.put("description", "Workshop created via Workshop Template System");
            repoData.put("private", false);
            repoData.put("auto_init", true);
            repoData.put("default_branch", "main");

            WebTarget target = httpClient.target(giteaApiUrl + "/orgs/" + giteaOrg + "/repos");
            Response response = target.request(MediaType.APPLICATION_JSON)
                .header("Authorization", "token " + giteaToken)
                .post(Entity.json(repoData));

            if (response.getStatus() == 201) {
                Map<String, Object> responseData = response.readEntity(Map.class);
                String repositoryUrl = (String) responseData.get("html_url");
                response.close();

                LOG.infof("✅ Gitea repository created directly via API: %s", repositoryUrl);
                return repositoryUrl;
            } else {
                String errorBody = response.readEntity(String.class);
                response.close();
                LOG.errorf("❌ Failed to create Gitea repository directly: HTTP %d - %s", response.getStatus(), errorBody);
                return "";
            }

        } catch (Exception e) {
            LOG.errorf("❌ Error creating Gitea repository directly: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Extract repository URL from Source Manager Agent response
     */
    private String extractRepositoryUrlFromResponse(String agentResponse) {
        try {
            if (agentResponse != null && agentResponse.contains("Repository URL")) {
                // Look for Gitea URL pattern in the response
                String[] lines = agentResponse.split("\n");
                for (String line : lines) {
                    if (line.contains("gitea") && line.contains("http")) {
                        // Extract URL from the line
                        String[] parts = line.split("\\s+");
                        for (String part : parts) {
                            if (part.startsWith("http") && part.contains("gitea")) {
                                return part.trim();
                            }
                        }
                    }
                }
            }
            return "";
        } catch (Exception e) {
            LOG.warnf("Failed to extract repository URL from response: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Parse ADR-0001 compliant response from Source Manager Agent
     */
    private String parseADR0001Response(String agentResponse, String workshopName) {
        try {
            if (agentResponse == null || agentResponse.trim().isEmpty()) {
                LOG.warnf("Empty response from Source Manager Agent for workshop: %s", workshopName);
                return "";
            }

            // Look for ADR-0001 specific response patterns
            String repositoryUrl = "";
            String templateStrategy = "";
            String templateSource = "";

            // Parse structured response for gitea_url
            if (agentResponse.contains("gitea_url")) {
                String[] lines = agentResponse.split("\n");
                for (String line : lines) {
                    if (line.contains("gitea_url") && line.contains("http")) {
                        // Extract URL from gitea_url field
                        String[] parts = line.split(":");
                        if (parts.length >= 2) {
                            repositoryUrl = parts[1].trim().replaceAll("[\"',]", "");
                        }
                    } else if (line.contains("strategy")) {
                        templateStrategy = line.split(":")[1].trim().replaceAll("[\"',]", "");
                    } else if (line.contains("template_source")) {
                        templateSource = line.split(":")[1].trim().replaceAll("[\"',]", "");
                    }
                }
            }

            // Fallback to legacy URL extraction if structured parsing fails
            if (repositoryUrl.isEmpty()) {
                repositoryUrl = extractRepositoryUrlFromResponse(agentResponse);
            }

            // Log ADR-0001 compliance information
            if (!templateStrategy.isEmpty()) {
                LOG.infof("🎯 ADR-0001 Template Strategy: %s", templateStrategy);
            }
            if (!templateSource.isEmpty()) {
                LOG.infof("📦 Template Source: %s", templateSource);
            }
            if (!repositoryUrl.isEmpty()) {
                LOG.infof("✅ Gitea repository created successfully: %s", repositoryUrl);
            }

            return repositoryUrl;

        } catch (Exception e) {
            LOG.errorf("❌ Error parsing ADR-0001 response: %s", e.getMessage());
            return extractRepositoryUrlFromResponse(agentResponse); // Fallback to legacy parsing
        }
    }

    /**
     * Build unified response format with classification metadata and Gitea URL
     */
    private Response buildUnifiedResponseWithGitea(Response workflowResponse, RepositoryClassification classification, CreateWorkshopRequest request, String giteaRepositoryUrl) {
        try {
            // Extract original response data
            Object originalEntity = workflowResponse.getEntity();
            Map<String, Object> originalData = (Map<String, Object>) originalEntity;

            // Build enhanced response with classification metadata and Gitea URL
            Map<String, Object> enhancedResponse = new HashMap<>(originalData);
            enhancedResponse.put("classification", Map.of(
                "repository_classification", classification.getClassificationType(),
                "detected_framework", classification.getDetectedFramework(),
                "recommended_workflow", classification.getRecommendedWorkflow(),
                "confidence", classification.getConfidence(),
                "template_source", classification.getTemplateSource(),
                "gitea_strategy", classification.getGiteaStrategy(),
                "workflow_description", classification.getWorkflowDescription()
            ));
            enhancedResponse.put("intelligent_routing", true);
            enhancedResponse.put("adr_compliance", "ADR-0001");
            enhancedResponse.put("gitea_repository_url", giteaRepositoryUrl);
            enhancedResponse.put("repository_created", !giteaRepositoryUrl.isEmpty());

            return Response.status(workflowResponse.getStatus())
                .entity(enhancedResponse)
                .build();

        } catch (Exception e) {
            LOG.warnf("Failed to build unified response with Gitea URL, returning original: %s", e.getMessage());
            return workflowResponse;
        }
    }

    /**
     * Build unified response format with classification metadata (legacy method)
     */
    private Response buildUnifiedResponse(Response workflowResponse, RepositoryClassification classification, CreateWorkshopRequest request) {
        return buildUnifiedResponseWithGitea(workflowResponse, classification, request, "");
    }
}
