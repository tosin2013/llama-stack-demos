package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Model representing workshop evolution status and tracking information.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration for workshop evolution.
 */
@RegisterForReflection
public class EvolutionStatus {

    @JsonProperty("evolution_id")
    private String evolutionId;

    @JsonProperty("workshop_name")
    private String workshopName;

    @JsonProperty("evolution_type")
    private EvolutionType evolutionType;

    @JsonProperty("status")
    private EvolutionPhase status;

    @JsonProperty("current_version")
    private String currentVersion;

    @JsonProperty("target_version")
    private String targetVersion;

    @JsonProperty("backup_version")
    private String backupVersion;

    @JsonProperty("approval_id")
    private String approvalId;

    @JsonProperty("requested_by")
    private String requestedBy;

    @JsonProperty("approved_by")
    private String approvedBy;

    @JsonProperty("implemented_by")
    private String implementedBy;

    @JsonProperty("evolution_description")
    private String evolutionDescription;

    @JsonProperty("approved_changes")
    private String approvedChanges;

    @JsonProperty("research_basis")
    private String researchBasis;

    @JsonProperty("impact_assessment")
    private String impactAssessment;

    @JsonProperty("files_modified")
    private Integer filesModified;

    @JsonProperty("content_updated")
    private Boolean contentUpdated;

    @JsonProperty("deployment_triggered")
    private Boolean deploymentTriggered;

    @JsonProperty("rollback_available")
    private Boolean rollbackAvailable;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("approved_at")
    private Instant approvedAt;

    @JsonProperty("implemented_at")
    private Instant implementedAt;

    @JsonProperty("completed_at")
    private Instant completedAt;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    @JsonProperty("evolution_metrics")
    private Map<String, Object> evolutionMetrics;

    @JsonProperty("validation_results")
    private List<String> validationResults;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("rollback_reason")
    private String rollbackReason;

    // Default constructor
    public EvolutionStatus() {
        this.status = EvolutionPhase.REQUESTED;
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.contentUpdated = false;
        this.deploymentTriggered = false;
        this.rollbackAvailable = false;
    }

    // Constructor with required fields
    public EvolutionStatus(String workshopName, EvolutionType evolutionType, String requestedBy) {
        this();
        this.workshopName = workshopName;
        this.evolutionType = evolutionType;
        this.requestedBy = requestedBy;
    }

    // Getters and Setters
    public String getEvolutionId() {
        return evolutionId;
    }

    public void setEvolutionId(String evolutionId) {
        this.evolutionId = evolutionId;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public EvolutionType getEvolutionType() {
        return evolutionType;
    }

    public void setEvolutionType(EvolutionType evolutionType) {
        this.evolutionType = evolutionType;
    }

    public EvolutionPhase getStatus() {
        return status;
    }

    public void setStatus(EvolutionPhase status) {
        this.status = status;
        this.lastUpdated = Instant.now();
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public String getBackupVersion() {
        return backupVersion;
    }

    public void setBackupVersion(String backupVersion) {
        this.backupVersion = backupVersion;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getImplementedBy() {
        return implementedBy;
    }

    public void setImplementedBy(String implementedBy) {
        this.implementedBy = implementedBy;
    }

    public String getEvolutionDescription() {
        return evolutionDescription;
    }

    public void setEvolutionDescription(String evolutionDescription) {
        this.evolutionDescription = evolutionDescription;
    }

    public String getApprovedChanges() {
        return approvedChanges;
    }

    public void setApprovedChanges(String approvedChanges) {
        this.approvedChanges = approvedChanges;
    }

    public String getResearchBasis() {
        return researchBasis;
    }

    public void setResearchBasis(String researchBasis) {
        this.researchBasis = researchBasis;
    }

    public String getImpactAssessment() {
        return impactAssessment;
    }

    public void setImpactAssessment(String impactAssessment) {
        this.impactAssessment = impactAssessment;
    }

    public Integer getFilesModified() {
        return filesModified;
    }

    public void setFilesModified(Integer filesModified) {
        this.filesModified = filesModified;
    }

    public Boolean getContentUpdated() {
        return contentUpdated;
    }

    public void setContentUpdated(Boolean contentUpdated) {
        this.contentUpdated = contentUpdated;
    }

    public Boolean getDeploymentTriggered() {
        return deploymentTriggered;
    }

    public void setDeploymentTriggered(Boolean deploymentTriggered) {
        this.deploymentTriggered = deploymentTriggered;
    }

    public Boolean getRollbackAvailable() {
        return rollbackAvailable;
    }

    public void setRollbackAvailable(Boolean rollbackAvailable) {
        this.rollbackAvailable = rollbackAvailable;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getImplementedAt() {
        return implementedAt;
    }

    public void setImplementedAt(Instant implementedAt) {
        this.implementedAt = implementedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, Object> getEvolutionMetrics() {
        return evolutionMetrics;
    }

    public void setEvolutionMetrics(Map<String, Object> evolutionMetrics) {
        this.evolutionMetrics = evolutionMetrics;
    }

    public List<String> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(List<String> validationResults) {
        this.validationResults = validationResults;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRollbackReason() {
        return rollbackReason;
    }

    public void setRollbackReason(String rollbackReason) {
        this.rollbackReason = rollbackReason;
    }

    // Utility methods
    public boolean isInProgress() {
        return status == EvolutionPhase.APPROVED || 
               status == EvolutionPhase.IMPLEMENTING || 
               status == EvolutionPhase.VALIDATING;
    }

    public boolean isCompleted() {
        return status == EvolutionPhase.COMPLETED || 
               status == EvolutionPhase.DEPLOYED;
    }

    public boolean isFailed() {
        return status == EvolutionPhase.FAILED || 
               status == EvolutionPhase.ROLLED_BACK;
    }

    public boolean canRollback() {
        return rollbackAvailable && backupVersion != null && 
               (status == EvolutionPhase.COMPLETED || status == EvolutionPhase.DEPLOYED);
    }

    public long getDurationMinutes() {
        if (createdAt == null) return 0;
        Instant endTime = completedAt != null ? completedAt : Instant.now();
        return java.time.Duration.between(createdAt, endTime).toMinutes();
    }

    @Override
    public String toString() {
        return String.format("EvolutionStatus{id='%s', workshop='%s', type='%s', status='%s'}", 
                           evolutionId, workshopName, evolutionType, status);
    }
}
