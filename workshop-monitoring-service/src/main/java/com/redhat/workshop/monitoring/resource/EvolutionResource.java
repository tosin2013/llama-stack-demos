package com.redhat.workshop.monitoring.resource;

import com.redhat.workshop.monitoring.model.EvolutionStatus;
import com.redhat.workshop.monitoring.model.EvolutionType;
import com.redhat.workshop.monitoring.model.EvolutionPhase;
import com.redhat.workshop.monitoring.service.EvolutionService;
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
 * REST API endpoints for workshop evolution tracking and management.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration for workshop evolution.
 * 
 * @author Workshop Template System Team
 */
@Path("/api/evolution")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Evolution", description = "Workshop Evolution Tracking and Management APIs")
public class EvolutionResource {

    private static final Logger LOG = Logger.getLogger(EvolutionResource.class);

    @Inject
    EvolutionService evolutionService;

    /**
     * Get all active evolutions
     */
    @GET
    @Path("/active")
    @Operation(summary = "Get Active Evolutions", 
               description = "Retrieve all workshop evolutions currently in progress")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Active evolutions retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getActiveEvolutions() {
        try {
            LOG.debug("Getting active evolutions");
            
            List<EvolutionStatus> activeEvolutions = evolutionService.getActiveEvolutions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("active_evolutions", activeEvolutions);
            response.put("total_count", activeEvolutions.size());
            response.put("last_updated", Instant.now());
            
            LOG.debugf("Retrieved %d active evolutions", activeEvolutions.size());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting active evolutions: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve active evolutions", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get evolution statistics
     */
    @GET
    @Path("/statistics")
    @Operation(summary = "Get Evolution Statistics", 
               description = "Retrieve comprehensive statistics about workshop evolutions")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Evolution statistics retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getEvolutionStatistics() {
        try {
            LOG.debug("Getting evolution statistics");
            
            Map<String, Object> statistics = evolutionService.getEvolutionStatistics();
            
            LOG.debug("Evolution statistics retrieved successfully");
            
            return Response.ok(statistics).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting evolution statistics: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve evolution statistics", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get workshop evolution history
     */
    @GET
    @Path("/workshops/{workshopName}/history")
    @Operation(summary = "Get Workshop Evolution History", 
               description = "Retrieve evolution history for a specific workshop")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Workshop evolution history retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Workshop not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getWorkshopEvolutionHistory(@PathParam("workshopName") String workshopName) {
        try {
            LOG.debugf("Getting evolution history for workshop: %s", workshopName);
            
            List<EvolutionStatus> evolutions = evolutionService.getWorkshopEvolutions(workshopName);
            Map<String, Object> summary = evolutionService.getWorkshopEvolutionSummary(workshopName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("workshop_name", workshopName);
            response.put("evolutions", evolutions);
            response.put("summary", summary);
            response.put("total_count", evolutions.size());
            response.put("last_updated", Instant.now());
            
            LOG.debugf("Retrieved %d evolutions for workshop: %s", evolutions.size(), workshopName);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting workshop evolution history for %s: %s", workshopName, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve workshop evolution history", 
                                       "workshop_name", workshopName,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Get evolution by ID
     */
    @GET
    @Path("/{evolutionId}")
    @Operation(summary = "Get Evolution Details", 
               description = "Retrieve detailed information about a specific evolution")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Evolution details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = EvolutionStatus.class))),
        @APIResponse(responseCode = "404", description = "Evolution not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getEvolutionById(@PathParam("evolutionId") String evolutionId) {
        try {
            LOG.debugf("Getting evolution details: %s", evolutionId);
            
            EvolutionStatus evolution = evolutionService.getEvolutionById(evolutionId);
            
            if (evolution == null) {
                LOG.warnf("Evolution not found: %s", evolutionId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Evolution not found", 
                                           "evolution_id", evolutionId))
                              .build();
            }
            
            LOG.debugf("Evolution details retrieved: %s", evolutionId);
            
            return Response.ok(evolution).build();
            
        } catch (Exception e) {
            LOG.errorf("Error getting evolution details for %s: %s", evolutionId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to retrieve evolution details", 
                                       "evolution_id", evolutionId,
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Create new evolution tracking record
     */
    @POST
    @Path("/create")
    @Operation(summary = "Create Evolution Record", 
               description = "Create a new evolution tracking record for a workshop")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Evolution record created successfully",
                    content = @Content(schema = @Schema(implementation = EvolutionStatus.class))),
        @APIResponse(responseCode = "400", description = "Invalid evolution request"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response createEvolution(Map<String, Object> evolutionRequest) {
        try {
            // Extract required fields
            String workshopName = (String) evolutionRequest.get("workshop_name");
            String evolutionTypeStr = (String) evolutionRequest.get("evolution_type");
            String requestedBy = (String) evolutionRequest.get("requested_by");
            String description = (String) evolutionRequest.get("description");
            
            // Validate required fields
            if (workshopName == null || workshopName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Workshop name is required"))
                              .build();
            }
            
            if (evolutionTypeStr == null || evolutionTypeStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Evolution type is required"))
                              .build();
            }
            
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Requested by is required"))
                              .build();
            }
            
            // Parse evolution type
            EvolutionType evolutionType;
            try {
                evolutionType = EvolutionType.fromValue(evolutionTypeStr);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Invalid evolution type", 
                                           "evolution_type", evolutionTypeStr))
                              .build();
            }
            
            LOG.infof("Creating evolution: workshop=%s, type=%s, requestedBy=%s", 
                     workshopName, evolutionType, requestedBy);
            
            EvolutionStatus evolution = evolutionService.createEvolution(
                workshopName, evolutionType, requestedBy, description);
            
            LOG.infof("Evolution created successfully: %s", evolution.getEvolutionId());
            
            return Response.status(Response.Status.CREATED)
                          .entity(evolution)
                          .build();
                          
        } catch (Exception e) {
            LOG.errorf("Error creating evolution: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to create evolution", 
                                       "message", e.getMessage()))
                          .build();
        }
    }

    /**
     * Update evolution status
     */
    @PUT
    @Path("/{evolutionId}/status")
    @Operation(summary = "Update Evolution Status", 
               description = "Update the status/phase of an evolution")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Evolution status updated successfully"),
        @APIResponse(responseCode = "404", description = "Evolution not found"),
        @APIResponse(responseCode = "400", description = "Invalid status update"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response updateEvolutionStatus(@PathParam("evolutionId") String evolutionId, 
                                        Map<String, Object> statusUpdate) {
        try {
            String newPhaseStr = (String) statusUpdate.get("phase");
            String updatedBy = (String) statusUpdate.get("updated_by");
            String message = (String) statusUpdate.get("message");
            
            if (newPhaseStr == null || newPhaseStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Phase is required"))
                              .build();
            }
            
            // Parse evolution phase
            EvolutionPhase newPhase;
            try {
                newPhase = EvolutionPhase.fromValue(newPhaseStr);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                              .entity(Map.of("error", "Invalid evolution phase", 
                                           "phase", newPhaseStr))
                              .build();
            }
            
            LOG.infof("Updating evolution status: id=%s, phase=%s, updatedBy=%s", 
                     evolutionId, newPhase, updatedBy);
            
            EvolutionStatus updatedEvolution = evolutionService.updateEvolutionStatus(
                evolutionId, newPhase, updatedBy, message);
            
            if (updatedEvolution == null) {
                LOG.warnf("Evolution not found for status update: %s", evolutionId);
                return Response.status(Response.Status.NOT_FOUND)
                              .entity(Map.of("error", "Evolution not found", 
                                           "evolution_id", evolutionId))
                              .build();
            }
            
            LOG.infof("Evolution status updated successfully: %s", evolutionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Evolution status updated successfully");
            response.put("evolution_id", evolutionId);
            response.put("new_phase", newPhase.getValue());
            response.put("updated_by", updatedBy);
            response.put("updated_at", updatedEvolution.getLastUpdated());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.errorf("Error updating evolution status for %s: %s", evolutionId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(Map.of("error", "Failed to update evolution status", 
                                       "evolution_id", evolutionId,
                                       "message", e.getMessage()))
                          .build();
        }
    }
}
