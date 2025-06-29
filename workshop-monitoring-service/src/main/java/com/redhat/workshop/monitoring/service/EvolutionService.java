package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.EvolutionStatus;
import com.redhat.workshop.monitoring.model.EvolutionType;
import com.redhat.workshop.monitoring.model.EvolutionPhase;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing workshop evolution tracking and lifecycle.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration for workshop evolution.
 */
@ApplicationScoped
public class EvolutionService {

    private static final Logger LOG = Logger.getLogger(EvolutionService.class);

    // In-memory storage for evolution tracking (in production, this would be a database)
    private final Map<String, EvolutionStatus> evolutions = new ConcurrentHashMap<>();
    private final Map<String, List<EvolutionStatus>> workshopEvolutions = new ConcurrentHashMap<>();

    /**
     * Create a new evolution tracking record
     */
    public EvolutionStatus createEvolution(String workshopName, EvolutionType evolutionType, 
                                         String requestedBy, String description) {
        LOG.infof("Creating evolution: workshop=%s, type=%s, requestedBy=%s", 
                 workshopName, evolutionType, requestedBy);

        String evolutionId = UUID.randomUUID().toString();
        
        EvolutionStatus evolution = new EvolutionStatus(workshopName, evolutionType, requestedBy);
        evolution.setEvolutionId(evolutionId);
        evolution.setEvolutionDescription(description);
        evolution.setStatus(EvolutionPhase.REQUESTED);

        // Store evolution
        evolutions.put(evolutionId, evolution);
        
        // Add to workshop-specific tracking
        workshopEvolutions.computeIfAbsent(workshopName, k -> new ArrayList<>()).add(evolution);

        LOG.infof("Evolution created: id=%s, workshop=%s", evolutionId, workshopName);
        return evolution;
    }

    /**
     * Update evolution status and phase
     */
    public EvolutionStatus updateEvolutionStatus(String evolutionId, EvolutionPhase newPhase, 
                                               String updatedBy, String message) {
        LOG.infof("Updating evolution status: id=%s, phase=%s, updatedBy=%s", 
                 evolutionId, newPhase, updatedBy);

        EvolutionStatus evolution = evolutions.get(evolutionId);
        if (evolution == null) {
            LOG.warnf("Evolution not found: %s", evolutionId);
            return null;
        }

        EvolutionPhase previousPhase = evolution.getStatus();
        evolution.setStatus(newPhase);

        // Update phase-specific timestamps and fields
        switch (newPhase) {
            case UNDER_REVIEW:
                // No specific timestamp for under review
                break;
            case APPROVED:
                evolution.setApprovedAt(Instant.now());
                evolution.setApprovedBy(updatedBy);
                break;
            case IMPLEMENTING:
                evolution.setImplementedBy(updatedBy);
                break;
            case COMPLETED:
                evolution.setImplementedAt(Instant.now());
                evolution.setContentUpdated(true);
                break;
            case DEPLOYED:
                evolution.setCompletedAt(Instant.now());
                evolution.setDeploymentTriggered(true);
                break;
            case FAILED:
            case REJECTED:
                evolution.setErrorMessage(message);
                break;
            case ROLLED_BACK:
                evolution.setRollbackReason(message);
                evolution.setCompletedAt(Instant.now());
                break;
        }

        LOG.infof("Evolution status updated: %s -> %s -> %s", evolutionId, previousPhase, newPhase);
        return evolution;
    }

    /**
     * Get evolution by ID
     */
    public EvolutionStatus getEvolutionById(String evolutionId) {
        return evolutions.get(evolutionId);
    }

    /**
     * Get all evolutions for a workshop
     */
    public List<EvolutionStatus> getWorkshopEvolutions(String workshopName) {
        return workshopEvolutions.getOrDefault(workshopName, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(EvolutionStatus::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get active evolutions (in progress)
     */
    public List<EvolutionStatus> getActiveEvolutions() {
        return evolutions.values().stream()
                .filter(evolution -> evolution.getStatus().isActive())
                .sorted(Comparator.comparing(EvolutionStatus::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Get recent evolutions (last 30 days)
     */
    public List<EvolutionStatus> getRecentEvolutions(int days) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        
        return evolutions.values().stream()
                .filter(evolution -> evolution.getCreatedAt().isAfter(cutoff))
                .sorted(Comparator.comparing(EvolutionStatus::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get evolutions by type
     */
    public List<EvolutionStatus> getEvolutionsByType(EvolutionType evolutionType) {
        return evolutions.values().stream()
                .filter(evolution -> evolution.getEvolutionType() == evolutionType)
                .sorted(Comparator.comparing(EvolutionStatus::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get evolutions by status/phase
     */
    public List<EvolutionStatus> getEvolutionsByPhase(EvolutionPhase phase) {
        return evolutions.values().stream()
                .filter(evolution -> evolution.getStatus() == phase)
                .sorted(Comparator.comparing(EvolutionStatus::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Update evolution with implementation details
     */
    public EvolutionStatus updateEvolutionImplementation(String evolutionId, String approvedChanges,
                                                        String currentVersion, String targetVersion,
                                                        String backupVersion, Integer filesModified) {
        EvolutionStatus evolution = evolutions.get(evolutionId);
        if (evolution == null) {
            LOG.warnf("Evolution not found for implementation update: %s", evolutionId);
            return null;
        }

        evolution.setApprovedChanges(approvedChanges);
        evolution.setCurrentVersion(currentVersion);
        evolution.setTargetVersion(targetVersion);
        evolution.setBackupVersion(backupVersion);
        evolution.setFilesModified(filesModified);
        evolution.setRollbackAvailable(backupVersion != null);

        LOG.infof("Evolution implementation updated: %s", evolutionId);
        return evolution;
    }

    /**
     * Link evolution to approval workflow
     */
    public EvolutionStatus linkToApproval(String evolutionId, String approvalId) {
        EvolutionStatus evolution = evolutions.get(evolutionId);
        if (evolution == null) {
            LOG.warnf("Evolution not found for approval linking: %s", evolutionId);
            return null;
        }

        evolution.setApprovalId(approvalId);
        LOG.infof("Evolution linked to approval: evolution=%s, approval=%s", evolutionId, approvalId);
        return evolution;
    }

    /**
     * Add validation results to evolution
     */
    public EvolutionStatus addValidationResults(String evolutionId, List<String> validationResults) {
        EvolutionStatus evolution = evolutions.get(evolutionId);
        if (evolution == null) {
            LOG.warnf("Evolution not found for validation results: %s", evolutionId);
            return null;
        }

        evolution.setValidationResults(validationResults);
        LOG.infof("Validation results added to evolution: %s", evolutionId);
        return evolution;
    }

    /**
     * Get evolution statistics
     */
    public Map<String, Object> getEvolutionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total counts
        stats.put("total_evolutions", evolutions.size());
        stats.put("active_evolutions", getActiveEvolutions().size());
        
        // By phase
        Map<String, Long> byPhase = evolutions.values().stream()
                .collect(Collectors.groupingBy(
                    evolution -> evolution.getStatus().getValue(),
                    Collectors.counting()
                ));
        stats.put("by_phase", byPhase);
        
        // By type
        Map<String, Long> byType = evolutions.values().stream()
                .collect(Collectors.groupingBy(
                    evolution -> evolution.getEvolutionType().getValue(),
                    Collectors.counting()
                ));
        stats.put("by_type", byType);
        
        // Success rate
        long successful = evolutions.values().stream()
                .mapToLong(evolution -> evolution.getStatus().isSuccessful() ? 1 : 0)
                .sum();
        long completed = evolutions.values().stream()
                .mapToLong(evolution -> evolution.getStatus().isCompleted() ? 1 : 0)
                .sum();
        
        double successRate = completed > 0 ? (double) successful / completed * 100 : 0;
        stats.put("success_rate", Math.round(successRate * 100.0) / 100.0);
        
        // Average duration
        double avgDuration = evolutions.values().stream()
                .filter(evolution -> evolution.getStatus().isCompleted())
                .mapToLong(EvolutionStatus::getDurationMinutes)
                .average()
                .orElse(0.0);
        stats.put("average_duration_minutes", Math.round(avgDuration));
        
        // Recent activity (last 7 days)
        long recentActivity = getRecentEvolutions(7).size();
        stats.put("recent_activity_7_days", recentActivity);
        
        stats.put("last_updated", Instant.now());
        
        return stats;
    }

    /**
     * Get workshop evolution summary
     */
    public Map<String, Object> getWorkshopEvolutionSummary(String workshopName) {
        List<EvolutionStatus> workshopEvs = getWorkshopEvolutions(workshopName);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("workshop_name", workshopName);
        summary.put("total_evolutions", workshopEvs.size());
        
        if (!workshopEvs.isEmpty()) {
            EvolutionStatus latest = workshopEvs.get(0);
            summary.put("latest_evolution", Map.of(
                "evolution_id", latest.getEvolutionId(),
                "type", latest.getEvolutionType().getValue(),
                "status", latest.getStatus().getValue(),
                "created_at", latest.getCreatedAt(),
                "current_version", latest.getCurrentVersion() != null ? latest.getCurrentVersion() : "unknown"
            ));
            
            // Count by status
            Map<String, Long> statusCounts = workshopEvs.stream()
                    .collect(Collectors.groupingBy(
                        evolution -> evolution.getStatus().getValue(),
                        Collectors.counting()
                    ));
            summary.put("status_counts", statusCounts);
        }
        
        return summary;
    }

    /**
     * Clean up old completed evolutions (retention policy)
     */
    public int cleanupOldEvolutions(int retentionDays) {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        
        List<String> toRemove = evolutions.values().stream()
                .filter(evolution -> evolution.getStatus().isCompleted())
                .filter(evolution -> evolution.getCompletedAt() != null && 
                                   evolution.getCompletedAt().isBefore(cutoff))
                .map(EvolutionStatus::getEvolutionId)
                .collect(Collectors.toList());
        
        for (String evolutionId : toRemove) {
            EvolutionStatus removed = evolutions.remove(evolutionId);
            if (removed != null) {
                // Also remove from workshop-specific tracking
                List<EvolutionStatus> workshopEvs = workshopEvolutions.get(removed.getWorkshopName());
                if (workshopEvs != null) {
                    workshopEvs.removeIf(ev -> ev.getEvolutionId().equals(evolutionId));
                }
            }
        }
        
        LOG.infof("Cleaned up %d old evolutions (retention: %d days)", toRemove.size(), retentionDays);
        return toRemove.size();
    }
}
