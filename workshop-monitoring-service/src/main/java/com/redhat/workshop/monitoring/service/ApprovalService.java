package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.ApprovalRequest;
import com.redhat.workshop.monitoring.model.ApprovalDecision;
import com.redhat.workshop.monitoring.model.ApprovalStatus;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing human approval workflows in the Workshop Template System.
 * Implements ADR-0002: Human-in-the-Loop Agent Integration.
 */
@ApplicationScoped
public class ApprovalService {

    private static final Logger LOG = Logger.getLogger(ApprovalService.class);

    // In-memory storage for approvals (in production, this would be a database)
    private final Map<String, ApprovalRequest> approvals = new ConcurrentHashMap<>();
    private final Map<String, List<ApprovalDecision>> approvalHistory = new ConcurrentHashMap<>();

    // Approval type configurations
    private static final Map<String, ApprovalTypeConfig> APPROVAL_CONFIGS = Map.of(
        "classification", new ApprovalTypeConfig("Repository Classification Validation", 4.0, 2.0, "technical_lead"),
        "content_review", new ApprovalTypeConfig("Workshop Content Quality Review", 8.0, 4.0, "subject_matter_expert"),
        "deployment_authorization", new ApprovalTypeConfig("Production Deployment Authorization", 2.0, 1.0, "workshop_owner"),
        "conflict_resolution", new ApprovalTypeConfig("Agent Conflict Resolution", 1.0, 0.5, "system_administrator"),
        "rag_update", new ApprovalTypeConfig("RAG Knowledge Base Update", 24.0, 12.0, "content_curator"),
        "workshop_evolution", new ApprovalTypeConfig("Workshop Evolution Request", 48.0, 24.0, "workshop_owner"),
        "research_integration", new ApprovalTypeConfig("Research Integration Approval", 72.0, 36.0, "subject_matter_expert"),
        "technology_refresh", new ApprovalTypeConfig("Technology Stack Refresh", 96.0, 48.0, "technical_lead")
    );

    /**
     * Submit a new approval request
     */
    public ApprovalRequest submitApproval(ApprovalRequest request) {
        LOG.infof("Submitting approval request: type=%s, requester=%s", request.getType(), request.getRequester());

        // Validate request
        validateApprovalRequest(request);

        // Generate unique ID
        String approvalId = UUID.randomUUID().toString();
        request.setApprovalId(approvalId);

        // Set configuration based on type
        ApprovalTypeConfig config = APPROVAL_CONFIGS.get(request.getType());
        if (config != null) {
            request.setName(config.name);
            request.setDescription(config.name);
            request.setTimeoutHours(config.timeoutHours);
            request.setEscalationHours(config.escalationHours);
            request.setRequiredRole(config.requiredRole);
        }

        // Set timestamps
        Instant now = Instant.now();
        request.setCreatedAt(now);
        request.setLastUpdated(now);
        
        if (request.getTimeoutHours() != null) {
            request.setTimeoutAt(now.plus((long)(request.getTimeoutHours() * 60), ChronoUnit.MINUTES));
        }
        
        if (request.getEscalationHours() != null) {
            request.setEscalationAt(now.plus((long)(request.getEscalationHours() * 60), ChronoUnit.MINUTES));
        }

        // Set initial status
        request.setStatus(ApprovalStatus.PENDING);

        // Store approval
        approvals.put(approvalId, request);
        approvalHistory.put(approvalId, new ArrayList<>());

        // TODO: Send notification to reviewers
        sendNotificationToReviewers(request);

        LOG.infof("Approval request submitted successfully: id=%s", approvalId);
        return request;
    }

    /**
     * Get all pending approvals with optional filtering
     */
    public List<ApprovalRequest> getPendingApprovals(String type, String priority, String reviewer) {
        LOG.debugf("Getting pending approvals: type=%s, priority=%s, reviewer=%s", type, priority, reviewer);

        return approvals.values().stream()
            .filter(approval -> approval.getStatus().isPending())
            .filter(approval -> type == null || type.equals(approval.getType()))
            .filter(approval -> priority == null || priority.equals(approval.getPriority()))
            .filter(approval -> reviewer == null || reviewer.equals(approval.getAssignedReviewer()))
            .sorted(Comparator.comparing(ApprovalRequest::getCreatedAt))
            .collect(Collectors.toList());
    }

    /**
     * Get approval by ID
     */
    public ApprovalRequest getApprovalById(String approvalId) {
        LOG.debugf("Getting approval by ID: %s", approvalId);
        return approvals.get(approvalId);
    }

    /**
     * Approve a request
     */
    public ApprovalRequest approveRequest(String approvalId, ApprovalDecision decision) {
        LOG.infof("Approving request: %s by %s", approvalId, decision.getReviewer());

        ApprovalRequest approval = approvals.get(approvalId);
        if (approval == null) {
            LOG.warnf("Approval not found: %s", approvalId);
            return null;
        }

        // Validate state
        if (!approval.getStatus().isPending()) {
            throw new IllegalStateException("Approval is not in a pending state: " + approval.getStatus());
        }

        // Update approval
        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setAssignedReviewer(decision.getReviewer());
        approval.setDecisionTime(Instant.now());
        approval.setDecisionComments(decision.getComments());
        approval.setLastUpdated(Instant.now());

        // Calculate review duration
        if (approval.getCreatedAt() != null) {
            long durationMinutes = ChronoUnit.MINUTES.between(approval.getCreatedAt(), approval.getDecisionTime());
            decision.setReviewDurationMinutes(durationMinutes);
        }

        // Store decision in history
        decision.setApprovalId(approvalId);
        decision.setDecision("approved");
        approvalHistory.get(approvalId).add(decision);

        // Update audit trail
        updateAuditTrail(approval, "APPROVED", decision.getReviewer(), decision.getComments());

        LOG.infof("Request approved successfully: %s", approvalId);
        return approval;
    }

    /**
     * Reject a request
     */
    public ApprovalRequest rejectRequest(String approvalId, ApprovalDecision decision) {
        LOG.infof("Rejecting request: %s by %s", approvalId, decision.getReviewer());

        ApprovalRequest approval = approvals.get(approvalId);
        if (approval == null) {
            LOG.warnf("Approval not found: %s", approvalId);
            return null;
        }

        // Validate state
        if (!approval.getStatus().isPending()) {
            throw new IllegalStateException("Approval is not in a pending state: " + approval.getStatus());
        }

        // Update approval
        approval.setStatus(ApprovalStatus.REJECTED);
        approval.setAssignedReviewer(decision.getReviewer());
        approval.setDecisionTime(Instant.now());
        approval.setDecisionComments(decision.getComments());
        approval.setLastUpdated(Instant.now());

        // Calculate review duration
        if (approval.getCreatedAt() != null) {
            long durationMinutes = ChronoUnit.MINUTES.between(approval.getCreatedAt(), approval.getDecisionTime());
            decision.setReviewDurationMinutes(durationMinutes);
        }

        // Store decision in history
        decision.setApprovalId(approvalId);
        decision.setDecision("rejected");
        approvalHistory.get(approvalId).add(decision);

        // Update audit trail
        updateAuditTrail(approval, "REJECTED", decision.getReviewer(), decision.getComments());

        LOG.infof("Request rejected successfully: %s", approvalId);
        return approval;
    }

    /**
     * Escalate an approval request
     */
    public ApprovalRequest escalateApproval(String approvalId, String escalationReason, String escalatedTo) {
        LOG.infof("Escalating approval: %s, reason: %s", approvalId, escalationReason);

        ApprovalRequest approval = approvals.get(approvalId);
        if (approval == null) {
            LOG.warnf("Approval not found for escalation: %s", approvalId);
            return null;
        }

        // Update approval status
        approval.setStatus(ApprovalStatus.ESCALATED);
        approval.setEscalationReason(escalationReason);
        approval.setAssignedReviewer(escalatedTo);
        approval.setLastUpdated(Instant.now());

        // Extend timeout for escalated approval
        if (approval.getTimeoutAt() != null) {
            approval.setTimeoutAt(Instant.now().plus(2, ChronoUnit.HOURS)); // 2 hour extension
        }

        // Update audit trail
        updateAuditTrail(approval, "ESCALATED", "system", escalationReason);

        // TODO: Send escalation notification
        sendEscalationNotification(approval, escalationReason);

        LOG.infof("Approval escalated successfully: %s", approvalId);
        return approval;
    }

    /**
     * Get approval history for an approval
     */
    public List<ApprovalDecision> getApprovalHistory(String approvalId) {
        return approvalHistory.getOrDefault(approvalId, Collections.emptyList());
    }

    /**
     * Check for overdue approvals and auto-escalate
     */
    public void processOverdueApprovals() {
        LOG.debug("Processing overdue approvals");

        Instant now = Instant.now();
        
        approvals.values().stream()
            .filter(approval -> approval.getStatus().isPending())
            .filter(approval -> approval.needsEscalation())
            .forEach(approval -> {
                LOG.warnf("Auto-escalating overdue approval: %s", approval.getApprovalId());
                escalateApproval(approval.getApprovalId(), "Automatic escalation due to timeout", "management");
            });
    }

    // Private helper methods

    private void validateApprovalRequest(ApprovalRequest request) {
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Approval type is required");
        }

        if (!APPROVAL_CONFIGS.containsKey(request.getType())) {
            throw new IllegalArgumentException("Invalid approval type: " + request.getType());
        }

        if (request.getRequester() == null || request.getRequester().trim().isEmpty()) {
            throw new IllegalArgumentException("Requester is required");
        }

        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new IllegalArgumentException("Content is required for approval");
        }
    }

    private void updateAuditTrail(ApprovalRequest approval, String action, String actor, String details) {
        String auditEntry = String.format("[%s] %s by %s: %s", 
            Instant.now().toString(), action, actor, details != null ? details : "No details");
        
        String currentTrail = approval.getAuditTrail();
        if (currentTrail == null || currentTrail.isEmpty()) {
            approval.setAuditTrail(auditEntry);
        } else {
            approval.setAuditTrail(currentTrail + "\n" + auditEntry);
        }
    }

    private void sendNotificationToReviewers(ApprovalRequest request) {
        // TODO: Implement email notification to reviewers
        LOG.infof("Notification sent for approval: %s (type: %s)", request.getApprovalId(), request.getType());
    }

    private void sendEscalationNotification(ApprovalRequest approval, String reason) {
        // TODO: Implement escalation notification
        LOG.infof("Escalation notification sent for approval: %s, reason: %s", approval.getApprovalId(), reason);
    }

    // Configuration class for approval types
    private static class ApprovalTypeConfig {
        final String name;
        final Double timeoutHours;
        final Double escalationHours;
        final String requiredRole;

        ApprovalTypeConfig(String name, Double timeoutHours, Double escalationHours, String requiredRole) {
            this.name = name;
            this.timeoutHours = timeoutHours;
            this.escalationHours = escalationHours;
            this.requiredRole = requiredRole;
        }
    }
}
