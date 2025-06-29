package com.redhat.workshop.monitoring.resource;

import com.redhat.workshop.monitoring.model.ImpactAnalysis;
import com.redhat.workshop.monitoring.service.ImpactAssessmentService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoints for AI-powered impact assessment of workshop evolution requests.
 * 
 * @author Workshop Template System Team
 */
@Path("/api/impact-assessment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Impact Assessment", description = "AI-Powered Workshop Evolution Impact Analysis APIs")
public class ImpactAssessmentResource {

    private static final Logger LOG = Logger.getLogger(ImpactAssessmentResource.class);

    @Inject
    ImpactAssessmentService impactAssessmentService;

    /**
     * Analyze impact of proposed evolution changes
     */
    @POST
    @Path("/analyze")
    @Operation(summary = "Analyze Evolution Impact", 
               description = "Perform comprehensive AI-powered impact analysis for proposed workshop evolution")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Impact analysis completed successfully",
                    content = @Content(schema = @Schema(implementation = ImpactAnalysis.class))),
        @APIResponse(responseCode = "400", description = "Invalid analysis request"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response analyzeImpact(Map<String, Object> analysisRequest) {
        try {
            // Extract required fields
            String evolutionId = (String) analysisRequest.get("evolution_id");
            String workshopName = (String) analysisRequest.get("workshop_name");
            String evolutionType = (String) analysisRequest.get("evolution_type");
            String proposedChanges = (String) analysisRequest.get("proposed_changes");
            
            // Validate required fields
            if (workshopName == null || workshopName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Workshop name is required"))
                              .build();
            }
            
            if (evolutionType == null || evolutionType.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Evolution type is required"))
                              .build();
            }
            
            if (proposedChanges == null || proposedChanges.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Proposed changes description is required"))
                              .build();
            }
            
            LOG.infof("Starting impact analysis: workshop=%s, type=%s, evolution=%s", 
                     workshopName, evolutionType, evolutionId);
            
            ImpactAnalysis analysis = impactAssessmentService.analyzeEvolutionImpact(
                evolutionId, workshopName, evolutionType, proposedChanges);
            
            LOG.infof("Impact analysis completed: %s - Risk: %s, Score: %.1f", 
                     analysis.getAnalysisId(), analysis.getRiskLevel(), analysis.getOverallImpactScore());
            
            return Response.status(Response.Status.CREATED)
                          .entity(analysis)
                          .build();
                          
        } catch (Exception e) {
            LOG.errorf("Error performing impact analysis: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to perform impact analysis", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get impact analysis by ID
     */
    @GET
    @Path("/{analysisId}")
    @Operation(summary = "Get Impact Analysis", 
               description = "Retrieve detailed impact analysis results by analysis ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Impact analysis retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ImpactAnalysis.class))),
        @APIResponse(responseCode = "404", description = "Impact analysis not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getImpactAnalysis(@PathParam("analysisId") String analysisId) {
        try {
            LOG.debugf("Getting impact analysis: %s", analysisId);
            
            ImpactAnalysis analysis = impactAssessmentService.getAnalysisById(analysisId);
            
            if (analysis == null) {
                LOG.warnf("Impact analysis not found: %s", analysisId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Impact analysis not found", 
                                           "analysis_id", analysisId))
                              .build();
            }
            
            LOG.debugf("Impact analysis retrieved: %s", analysisId);
            
            return Response.ok(analysis).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting impact analysis for %s: %s", analysisId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve impact analysis", 
                                       "analysis_id", analysisId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get impact analyses for an evolution
     */
    @GET
    @Path("/evolution/{evolutionId}")
    @Operation(summary = "Get Evolution Impact Analyses", 
               description = "Retrieve all impact analyses for a specific evolution")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Evolution impact analyses retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getEvolutionImpactAnalyses(@PathParam("evolutionId") String evolutionId) {
        try {
            LOG.debugf("Getting impact analyses for evolution: %s", evolutionId);
            
            List<ImpactAnalysis> analyses = impactAssessmentService.getAnalysesForEvolution(evolutionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("evolution_id", evolutionId);
            response.put("analyses", analyses);
            response.put("total_count", analyses.size());
            response.put("last_updated", Instant.now());
            
            LOG.debugf("Retrieved %d impact analyses for evolution: %s", analyses.size(), evolutionId);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting evolution impact analyses for %s: %s", evolutionId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve evolution impact analyses", 
                                       "evolution_id", evolutionId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get recent impact analyses
     */
    @GET
    @Path("/recent")
    @Operation(summary = "Get Recent Impact Analyses", 
               description = "Retrieve recent impact analyses with optional limit")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Recent impact analyses retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getRecentImpactAnalyses(@QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            LOG.debugf("Getting recent impact analyses with limit: %d", limit);
            
            // Validate limit
            if (limit < 1 || limit > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Limit must be between 1 and 100"))
                              .build();
            }
            
            List<ImpactAnalysis> analyses = impactAssessmentService.getRecentAnalyses(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("analyses", analyses);
            response.put("total_count", analyses.size());
            response.put("limit", limit);
            response.put("last_updated", Instant.now());
            
            LOG.debugf("Retrieved %d recent impact analyses", analyses.size());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting recent impact analyses: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve recent impact analyses", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get impact analysis summary statistics
     */
    @GET
    @Path("/statistics")
    @Operation(summary = "Get Impact Analysis Statistics", 
               description = "Retrieve comprehensive statistics about impact analyses")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Impact analysis statistics retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getImpactAnalysisStatistics() {
        try {
            LOG.debug("Getting impact analysis statistics");
            
            List<ImpactAnalysis> recentAnalyses = impactAssessmentService.getRecentAnalyses(100);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // Total counts
            statistics.put("total_analyses", recentAnalyses.size());
            
            // By risk level
            Map<String, Long> byRiskLevel = recentAnalyses.stream()
                    .filter(analysis -> analysis.getRiskLevel() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        analysis -> analysis.getRiskLevel().getValue(),
                        java.util.stream.Collectors.counting()
                    ));
            statistics.put("by_risk_level", byRiskLevel);
            
            // By evolution type
            Map<String, Long> byEvolutionType = recentAnalyses.stream()
                    .filter(analysis -> analysis.getEvolutionType() != null)
                    .collect(java.util.stream.Collectors.groupingBy(
                        ImpactAnalysis::getEvolutionType,
                        java.util.stream.Collectors.counting()
                    ));
            statistics.put("by_evolution_type", byEvolutionType);
            
            // Average scores
            double avgImpactScore = recentAnalyses.stream()
                    .filter(analysis -> analysis.getOverallImpactScore() != null)
                    .mapToDouble(ImpactAnalysis::getOverallImpactScore)
                    .average()
                    .orElse(0.0);
            statistics.put("average_impact_score", Math.round(avgImpactScore * 100.0) / 100.0);
            
            double avgConfidenceScore = recentAnalyses.stream()
                    .filter(analysis -> analysis.getConfidenceScore() != null)
                    .mapToDouble(ImpactAnalysis::getConfidenceScore)
                    .average()
                    .orElse(0.0);
            statistics.put("average_confidence_score", Math.round(avgConfidenceScore * 100.0) / 100.0);
            
            // High risk analyses count
            long highRiskCount = recentAnalyses.stream()
                    .filter(ImpactAnalysis::isHighRisk)
                    .count();
            statistics.put("high_risk_analyses", highRiskCount);
            
            // Analyses requiring expert review
            long expertReviewCount = recentAnalyses.stream()
                    .filter(ImpactAnalysis::requiresExpertReview)
                    .count();
            statistics.put("expert_review_required", expertReviewCount);
            
            statistics.put("last_updated", Instant.now());
            
            LOG.debug("Impact analysis statistics retrieved successfully");
            
            return Response.ok(statistics).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting impact analysis statistics: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve impact analysis statistics", 
                                       "message", e.getMessage()))
                          .build();
        }
    }
}
