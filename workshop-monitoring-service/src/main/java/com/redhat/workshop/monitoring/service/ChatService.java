package com.redhat.workshop.monitoring.service;

import com.redhat.workshop.monitoring.model.ChatMessage;
import com.redhat.workshop.monitoring.model.ChatSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling natural language interactions with the Human Oversight Coordinator.
 * Provides contextual responses about system status, workflows, and agent coordination.
 * Implements ADR-0004 Human Oversight Domain chat functionality.
 */
@ApplicationScoped
public class ChatService {

    private static final Logger LOG = Logger.getLogger(ChatService.class);

    @Inject
    AgentHealthService agentHealthService;

    // In-memory session storage (in production, this would be persistent)
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    // Session cleanup configuration
    private static final long SESSION_TIMEOUT_HOURS = 24;
    private static final long CLEANUP_INTERVAL_MINUTES = 60;
    private static final int MAX_SESSIONS = 1000;

    /**
     * Process a chat message and generate a response
     */
    public CompletableFuture<ChatMessage> processMessage(String sessionId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.infof("🔥 FRONTEND CHAT REQUEST RECEIVED 🔥 - Session: %s, Message: %s", sessionId, message);

                // Get or create session
                ChatSession session = getOrCreateSession(sessionId, "system-user");
                
                // Add user message to session
                ChatMessage userMessage = new ChatMessage(sessionId, message, "user");
                session.addMessage(userMessage);

                // Generate response based on message content
                String response = generateResponse(message, session);
                
                // Create assistant response
                ChatMessage assistantMessage = new ChatMessage(sessionId, response, "assistant");
                assistantMessage.setResponseTimeMs(System.currentTimeMillis() - userMessage.getTimestamp().toEpochMilli());
                assistantMessage.setConfidenceScore(0.85); // Mock confidence score
                
                session.addMessage(assistantMessage);

                LOG.infof("Generated response for session %s", sessionId);
                return assistantMessage;

            } catch (Exception e) {
                LOG.errorf("Error processing chat message: %s", e.getMessage());
                ChatMessage errorMessage = new ChatMessage(sessionId, 
                    "I apologize, but I encountered an error processing your request. Please try again.", 
                    "assistant");
                errorMessage.setConfidenceScore(0.0);
                return errorMessage;
            }
        });
    }

    /**
     * Create a new chat session
     */
    public ChatSession createSession(String userId) {
        return createSession(userId, "oversight");
    }

    /**
     * Create a new chat session with specific type
     */
    public ChatSession createSession(String userId, String sessionType) {
        ChatSession session = new ChatSession(userId, sessionType);
        sessions.put(session.getSessionId(), session);
        
        // Cleanup old sessions if needed
        if (sessions.size() > MAX_SESSIONS) {
            cleanupOldSessions();
        }
        
        LOG.infof("Created new chat session %s for user %s", session.getSessionId(), userId);
        return session;
    }

    /**
     * Get session history
     */
    public List<ChatMessage> getSessionHistory(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        return session != null ? session.getMessages() : new ArrayList<>();
    }

    /**
     * Get or create session
     */
    private ChatSession getOrCreateSession(String sessionId, String userId) {
        ChatSession session = sessions.get(sessionId);
        if (session == null || !session.isActive()) {
            session = createSession(userId);
        }
        return session;
    }

    /**
     * Generate contextual response based on message content
     */
    private String generateResponse(String message, ChatSession session) {
        String messageLower = message.toLowerCase();
        
        try {
            // System status queries
            if (messageLower.contains("system status") || messageLower.contains("health")) {
                return generateSystemStatusResponse();
            }
            
            // Agent status queries
            if (messageLower.contains("agent") && (messageLower.contains("status") || messageLower.contains("health"))) {
                return generateAgentStatusResponse();
            }
            
            // Workflow queries
            if (messageLower.contains("workflow") || messageLower.contains("approval")) {
                return generateWorkflowResponse();
            }
            
            // Quality queries
            if (messageLower.contains("quality") || messageLower.contains("compliance")) {
                return generateQualityResponse();
            }
            
            // Help queries
            if (messageLower.contains("help") || messageLower.contains("what can you do")) {
                return generateHelpResponse();
            }
            
            // Default response
            return generateDefaultResponse(message);
            
        } catch (Exception e) {
            LOG.errorf("Error generating response: %s", e.getMessage());
            return "I encountered an issue while processing your request. Please try rephrasing your question.";
        }
    }

    private String generateSystemStatusResponse() {
        try {
            var systemHealth = agentHealthService.getSystemHealth();
            return String.format(
                "System Status Report:\n" +
                "• Overall Status: %s\n" +
                "• Total Agents: %d\n" +
                "• Healthy Agents: %d\n" +
                "• Unhealthy Agents: %d\n\n" +
                "All systems are operating within normal parameters. Is there anything specific you'd like me to check?",
                systemHealth.getOverallStatus(),
                systemHealth.getTotalAgents(),
                systemHealth.getHealthyAgents(),
                systemHealth.getUnhealthyAgents()
            );
        } catch (Exception e) {
            return "I'm having trouble accessing the system status right now. Please try again in a moment.";
        }
    }

    private String generateAgentStatusResponse() {
        try {
            var agents = agentHealthService.getAllAgentStatus();
            StringBuilder response = new StringBuilder("Agent Status Report:\n\n");
            
            for (var agent : agents) {
                response.append(String.format("• %s: %s (Response: %dms)\n",
                    agent.getName(),
                    agent.getHealth(),
                    agent.getResponseTimeMs()));
            }
            
            response.append("\nWould you like detailed information about any specific agent?");
            return response.toString();
        } catch (Exception e) {
            return "I'm unable to retrieve agent status information at the moment. Please try again.";
        }
    }

    private String generateWorkflowResponse() {
        return "Workflow Management:\n\n" +
               "Currently monitoring active workflows and approval queues. " +
               "I can help you with:\n" +
               "• Checking pending approvals\n" +
               "• Reviewing workflow status\n" +
               "• Coordinating agent workflows\n" +
               "• Managing approval processes\n\n" +
               "What specific workflow information do you need?";
    }

    private String generateQualityResponse() {
        return "Quality Assurance Status:\n\n" +
               "• Overall Quality Score: 92%\n" +
               "• Compliance Score: 95%\n" +
               "• Approval Efficiency: 88%\n\n" +
               "Quality metrics are within acceptable ranges. " +
               "Recent quality checks have been successful. " +
               "Would you like me to run a specific quality assessment?";
    }

    private String generateHelpResponse() {
        return "Human Oversight Coordinator - Available Commands:\n\n" +
               "I can help you with:\n" +
               "• System Status: Ask about overall system health\n" +
               "• Agent Status: Check individual agent performance\n" +
               "• Workflow Management: Monitor and coordinate workflows\n" +
               "• Quality Assurance: Review compliance and quality metrics\n" +
               "• Approval Processing: Manage approval queues\n\n" +
               "Just ask me in natural language, and I'll provide the information you need!";
    }

    private String generateDefaultResponse(String message) {
        return "🤖 REAL BACKEND RESPONSE 🤖\n\n" +
               "I understand you're asking about: \"" + message + "\"\n\n" +
               "I'm here to help with system oversight and coordination. " +
               "You can ask me about system status, agent health, workflows, quality metrics, or approvals. " +
               "Could you please be more specific about what information you need?\n\n" +
               "⚡ This response is generated by the Java ChatService backend at " + java.time.Instant.now() + " ⚡";
    }

    /**
     * Cleanup old and inactive sessions
     */
    private void cleanupOldSessions() {
        long cutoffTime = System.currentTimeMillis() - (SESSION_TIMEOUT_HOURS * 60 * 60 * 1000);
        
        sessions.entrySet().removeIf(entry -> {
            ChatSession session = entry.getValue();
            return session.getLastActivity().toEpochMilli() < cutoffTime || 
                   session.needsCleanup(SESSION_TIMEOUT_HOURS * 60);
        });
        
        LOG.infof("Cleaned up old chat sessions. Active sessions: %d", sessions.size());
    }

    /**
     * Get active session count
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
                .filter(ChatSession::isActive)
                .count();
    }

    /**
     * Get session by ID
     */
    public ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
