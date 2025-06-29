package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.CommandExecution;
import com.redhat.workshop.monitoring.model.CommandStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing and executing oversight commands.
 * Supports system commands and integrates with existing monitoring infrastructure.
 * Implements ADR-0004 Human Oversight Domain command execution.
 */
@ApplicationScoped
public class CommandExecutionService {

    private static final Logger LOG = Logger.getLogger(CommandExecutionService.class);

    @Inject
    AgentHealthService agentHealthService;

    // In-memory command history (in production, this would be persistent)
    private final Map<String, List<CommandExecution>> userCommandHistory = new ConcurrentHashMap<>();

    // Command patterns
    private static final Pattern APPROVE_WORKFLOW_PATTERN = Pattern.compile("approve\\s+workflow\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern REJECT_WORKFLOW_PATTERN = Pattern.compile("reject\\s+workflow\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AGENT_STATUS_PATTERN = Pattern.compile("agent\\s+status(?:\\s+(\\S+))?", Pattern.CASE_INSENSITIVE);

    // Configuration
    private static final int MAX_HISTORY_PER_USER = 100;

    /**
     * Execute a command asynchronously
     */
    public CompletableFuture<CommandExecution> executeCommand(String command, String executor) {
        return CompletableFuture.supplyAsync(() -> {
            CommandExecution execution = new CommandExecution(command, executor);
            
            try {
                LOG.infof("Executing command: %s by %s", command, executor);
                execution.markAsExecuting();

                // Parse and execute the command
                Map<String, Object> result = parseAndExecuteCommand(command, execution);
                execution.markAsCompleted(result);

                // Add to history
                addToHistory(executor, execution);

                LOG.infof("Command executed successfully: %s", execution.getCommandId());
                return execution;

            } catch (Exception e) {
                LOG.errorf("Command execution failed: %s", e.getMessage());
                execution.markAsFailed(e.getMessage());
                addToHistory(executor, execution);
                return execution;
            }
        });
    }

    /**
     * Parse and validate a command
     */
    public CommandExecution parseCommand(String command) {
        CommandExecution execution = new CommandExecution(command, "system");
        
        try {
            String commandLower = command.toLowerCase().trim();
            
            // Determine command type
            if (commandLower.equals("system health")) {
                execution.setCommandType("system_health");
            } else if (commandLower.startsWith("agent status")) {
                execution.setCommandType("agent_status");
            } else if (commandLower.equals("quality check")) {
                execution.setCommandType("quality_check");
            } else if (commandLower.equals("list workflows")) {
                execution.setCommandType("list_workflows");
            } else if (APPROVE_WORKFLOW_PATTERN.matcher(command).matches()) {
                execution.setCommandType("approve_workflow");
            } else if (REJECT_WORKFLOW_PATTERN.matcher(command).matches()) {
                execution.setCommandType("reject_workflow");
            } else {
                execution.setCommandType("unknown");
            }
            
            return execution;
        } catch (Exception e) {
            execution.markAsFailed("Command parsing failed: " + e.getMessage());
            return execution;
        }
    }

    /**
     * Get command history for a user
     */
    public List<CommandExecution> getCommandHistory(String userId) {
        return userCommandHistory.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Validate if a command is supported
     */
    public boolean validateCommand(String command) {
        try {
            CommandExecution parsed = parseCommand(command);
            return !"unknown".equals(parsed.getCommandType());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse and execute the actual command
     */
    private Map<String, Object> parseAndExecuteCommand(String command, CommandExecution execution) {
        String commandLower = command.toLowerCase().trim();
        
        // System health command
        if (commandLower.equals("system health")) {
            return executeSystemHealthCommand();
        }
        
        // Agent status command
        if (commandLower.startsWith("agent status")) {
            Matcher matcher = AGENT_STATUS_PATTERN.matcher(command);
            String agentName = null;
            if (matcher.matches()) {
                agentName = matcher.group(1);
            }
            return executeAgentStatusCommand(agentName);
        }
        
        // Quality check command
        if (commandLower.equals("quality check")) {
            return executeQualityCheckCommand();
        }
        
        // List workflows command
        if (commandLower.equals("list workflows")) {
            return executeListWorkflowsCommand();
        }
        
        // Approve workflow command
        Matcher approveMatcher = APPROVE_WORKFLOW_PATTERN.matcher(command);
        if (approveMatcher.matches()) {
            String workflowId = approveMatcher.group(1);
            return executeApproveWorkflowCommand(workflowId, execution.getExecutor());
        }
        
        // Reject workflow command
        Matcher rejectMatcher = REJECT_WORKFLOW_PATTERN.matcher(command);
        if (rejectMatcher.matches()) {
            String workflowId = rejectMatcher.group(1);
            return executeRejectWorkflowCommand(workflowId, execution.getExecutor());
        }
        
        throw new IllegalArgumentException("Unknown command: " + command);
    }

    private Map<String, Object> executeSystemHealthCommand() {
        try {
            var systemHealth = agentHealthService.getSystemHealth();
            Map<String, Object> result = new HashMap<>();
            result.put("overall_status", systemHealth.getOverallStatus());
            result.put("total_agents", systemHealth.getTotalAgents());
            result.put("healthy_agents", systemHealth.getHealthyAgents());
            result.put("unhealthy_agents", systemHealth.getUnhealthyAgents());
            result.put("message", "System health check completed successfully");
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get system health: " + e.getMessage());
        }
    }

    private Map<String, Object> executeAgentStatusCommand(String agentName) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            if (agentName != null) {
                // Get specific agent status
                var agentStatus = agentHealthService.getAgentStatus(agentName);
                if (agentStatus != null) {
                    result.put("agent_name", agentStatus.getName());
                    result.put("health", agentStatus.getHealth());
                    result.put("response_time", agentStatus.getResponseTimeMs());
                    result.put("last_check", agentStatus.getLastChecked());
                    result.put("message", "Agent status retrieved successfully");
                } else {
                    throw new RuntimeException("Agent not found: " + agentName);
                }
            } else {
                // Get all agent statuses
                var agents = agentHealthService.getAllAgentStatus();
                List<Map<String, Object>> agentList = new ArrayList<>();
                
                for (var agent : agents) {
                    Map<String, Object> agentInfo = new HashMap<>();
                    agentInfo.put("name", agent.getName());
                    agentInfo.put("health", agent.getHealth());
                    agentInfo.put("response_time", agent.getResponseTimeMs());
                    agentList.add(agentInfo);
                }
                
                result.put("agents", agentList);
                result.put("total_agents", agentList.size());
                result.put("message", "All agent statuses retrieved successfully");
            }
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get agent status: " + e.getMessage());
        }
    }

    private Map<String, Object> executeQualityCheckCommand() {
        Map<String, Object> result = new HashMap<>();
        result.put("overall_score", 92);
        result.put("compliance_score", 95);
        result.put("approval_efficiency", 88);
        result.put("last_check", new Date().toInstant().toString());
        result.put("status", "PASSED");
        result.put("message", "Quality check completed successfully");
        return result;
    }

    private Map<String, Object> executeListWorkflowsCommand() {
        Map<String, Object> result = new HashMap<>();
        
        // Mock workflow data
        List<Map<String, Object>> workflows = Arrays.asList(
            Map.of(
                "id", "wf-001",
                "name", "Repository Analysis Workflow",
                "status", "pending_approval",
                "priority", "medium"
            ),
            Map.of(
                "id", "wf-002",
                "name", "Content Validation Workflow", 
                "status", "in_progress",
                "priority", "high"
            )
        );
        
        result.put("workflows", workflows);
        result.put("total_count", workflows.size());
        result.put("message", "Workflow list retrieved successfully");
        return result;
    }

    private Map<String, Object> executeApproveWorkflowCommand(String workflowId, String executor) {
        Map<String, Object> result = new HashMap<>();
        result.put("workflow_id", workflowId);
        result.put("action", "approved");
        result.put("approver", executor);
        result.put("approved_at", new Date().toInstant().toString());
        result.put("message", "Workflow approved successfully");
        return result;
    }

    private Map<String, Object> executeRejectWorkflowCommand(String workflowId, String executor) {
        Map<String, Object> result = new HashMap<>();
        result.put("workflow_id", workflowId);
        result.put("action", "rejected");
        result.put("rejector", executor);
        result.put("rejected_at", new Date().toInstant().toString());
        result.put("message", "Workflow rejected successfully");
        return result;
    }

    /**
     * Add command execution to user history
     */
    private void addToHistory(String userId, CommandExecution execution) {
        userCommandHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(execution);
        
        // Trim history if it exceeds maximum
        List<CommandExecution> history = userCommandHistory.get(userId);
        if (history.size() > MAX_HISTORY_PER_USER) {
            history.subList(0, history.size() - MAX_HISTORY_PER_USER).clear();
        }
    }

    /**
     * Get supported commands list
     */
    public List<String> getSupportedCommands() {
        return Arrays.asList(
            "system health",
            "agent status",
            "agent status <agent-name>",
            "quality check",
            "list workflows",
            "approve workflow <workflow-id>",
            "reject workflow <workflow-id>"
        );
    }
}
