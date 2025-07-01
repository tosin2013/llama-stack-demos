package com.redhat.workshop.monitoring.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import com.redhat.workshop.monitoring.model.RepositoryClassification;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository Classification Service
 * Bridges Java middleware with existing Python Template Converter Agent classification logic.
 * Implements ADR-0001 dual-template strategy for automatic repository classification.
 */
@ApplicationScoped
public class RepositoryClassificationService {

    private static final Logger LOG = Logger.getLogger(RepositoryClassificationService.class);

    @Inject
    AgentOrchestrationService agentOrchestrationService;

    // Cache for repository classifications to avoid repeated analysis
    private final Map<String, RepositoryClassification> classificationCache = new ConcurrentHashMap<>();

    /**
     * Classify repository to determine workflow type based on ADR-0001 specification
     * 
     * @param repositoryUrl The GitHub repository URL to classify
     * @return RepositoryClassification with workflow routing information
     */
    public RepositoryClassification classifyRepository(String repositoryUrl) {
        LOG.infof("🔍 REPOSITORY CLASSIFICATION: Analyzing repository: %s", repositoryUrl);

        // Check cache first
        if (classificationCache.containsKey(repositoryUrl)) {
            RepositoryClassification cached = classificationCache.get(repositoryUrl);
            LOG.debugf("📋 Using cached classification for %s: %s", repositoryUrl, cached.getClassificationType());
            return cached;
        }

        try {
            // Prepare parameters for Template Converter Agent
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("repository_url", repositoryUrl);
            parameters.put("analysis_depth", "comprehensive");
            parameters.put("target_format", "rhpds_showroom");

            // Call Template Converter Agent for repository analysis
            LOG.debugf("🤖 Calling Template Converter Agent for repository analysis");
            Map<String, Object> agentResult = agentOrchestrationService.invokeAgent(
                "template-converter",
                "analyze_repository_tool",
                parameters
            );

            // Parse and transform agent response
            RepositoryClassification classification = parseAgentResponse(agentResult, repositoryUrl);

            // Cache the result
            classificationCache.put(repositoryUrl, classification);

            LOG.infof("✅ REPOSITORY CLASSIFICATION COMPLETE: %s → %s (Workflow: %s, Confidence: %.2f)",
                     repositoryUrl, classification.getClassificationType(), 
                     classification.getRecommendedWorkflow(), classification.getConfidence());

            return classification;

        } catch (Exception e) {
            LOG.errorf("❌ Repository classification failed for %s: %s", repositoryUrl, e.getMessage());
            
            // Return fallback classification for applications (Workflow 1)
            RepositoryClassification fallback = createFallbackClassification(repositoryUrl, e.getMessage());
            classificationCache.put(repositoryUrl, fallback);
            return fallback;
        }
    }

    /**
     * Parse Template Converter Agent response and create RepositoryClassification
     */
    private RepositoryClassification parseAgentResponse(Map<String, Object> agentResult, String repositoryUrl) {
        try {
            // Extract result from agent response
            Object resultObj = agentResult.get("result");
            Map<String, Object> result;
            
            if (resultObj instanceof String) {
                // Parse JSON string result
                result = parseJsonResult((String) resultObj);
            } else if (resultObj instanceof Map) {
                result = (Map<String, Object>) resultObj;
            } else {
                throw new IllegalArgumentException("Unexpected agent result format: " + resultObj);
            }

            // Extract classification information
            String repositoryClassification = (String) result.getOrDefault("repository_classification", "application");
            String detectedFramework = (String) result.getOrDefault("detected_framework", "none");
            List<String> indicators = (List<String>) result.getOrDefault("indicators", new ArrayList<>());
            Double confidence = ((Number) result.getOrDefault("confidence", 0.8)).doubleValue();

            // Determine workflow type based on ADR-0001 specification
            String workflowType = determineWorkflowType(repositoryClassification, detectedFramework);

            LOG.debugf("📊 Classification details: type=%s, framework=%s, workflow=%s, confidence=%.2f",
                      repositoryClassification, detectedFramework, workflowType, confidence);

            return new RepositoryClassification(
                repositoryClassification,
                detectedFramework,
                workflowType,
                confidence,
                indicators,
                repositoryUrl
            );

        } catch (Exception e) {
            LOG.warnf("Failed to parse agent response, using fallback: %s", e.getMessage());
            throw new RuntimeException("Failed to parse classification result", e);
        }
    }

    /**
     * Determine workflow type based on ADR-0001 classification matrix
     */
    private String determineWorkflowType(String classificationType, String detectedFramework) {
        switch (classificationType.toLowerCase()) {
            case "existing_workshop":
                // Existing workshops use Workflow 3 (Enhancement)
                return "workflow3";
            case "tutorial_content":
                // Tutorial content uses Workflow 1 (New Workshop Creation)
                return "workflow1";
            case "application":
            default:
                // Applications use Workflow 1 (New Workshop Creation)
                return "workflow1";
        }
    }

    /**
     * Parse JSON string result from agent
     */
    private Map<String, Object> parseJsonResult(String jsonResult) {
        try {
            // Simple JSON parsing for basic structure
            // In production, use proper JSON library like Jackson
            Map<String, Object> result = new HashMap<>();
            
            if (jsonResult.contains("existing_workshop")) {
                result.put("repository_classification", "existing_workshop");
                result.put("detected_framework", "antora");
                result.put("confidence", 0.9);
            } else if (jsonResult.contains("tutorial_content")) {
                result.put("repository_classification", "tutorial_content");
                result.put("detected_framework", "none");
                result.put("confidence", 0.8);
            } else {
                result.put("repository_classification", "application");
                result.put("detected_framework", "none");
                result.put("confidence", 0.7);
            }
            
            result.put("indicators", Arrays.asList("Parsed from agent response"));
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON result: " + jsonResult, e);
        }
    }

    /**
     * Create fallback classification when agent call fails
     */
    private RepositoryClassification createFallbackClassification(String repositoryUrl, String errorMessage) {
        LOG.warnf("🔄 Creating fallback classification for %s due to error: %s", repositoryUrl, errorMessage);
        
        // Default to application classification (Workflow 1)
        return new RepositoryClassification(
            "application",
            "none",
            "workflow1",
            0.5, // Low confidence due to fallback
            Arrays.asList("Fallback classification due to agent error: " + errorMessage),
            repositoryUrl
        );
    }

    /**
     * Clear classification cache (useful for testing)
     */
    public void clearCache() {
        classificationCache.clear();
        LOG.debugf("🗑️ Classification cache cleared");
    }

    /**
     * Get cache size (useful for monitoring)
     */
    public int getCacheSize() {
        return classificationCache.size();
    }

    /**
     * Check if repository is cached
     */
    public boolean isCached(String repositoryUrl) {
        return classificationCache.containsKey(repositoryUrl);
    }
}
