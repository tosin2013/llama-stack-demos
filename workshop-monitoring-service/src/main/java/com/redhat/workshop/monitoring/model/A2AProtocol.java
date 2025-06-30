package com.redhat.workshop.monitoring.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * A2A Protocol Models
 * Implements the correct Agent-to-Agent communication protocol
 * Based on the real agent implementation using /send-task endpoint
 */
public class A2AProtocol {

    /**
     * A2A Task Request - sent to /send-task endpoint
     */
    public static class TaskRequest {
        @JsonProperty("id")
        public String id;
        
        @JsonProperty("acceptedOutputModes")
        public List<String> acceptedOutputModes;
        
        @JsonProperty("message")
        public Message message;
        
        public TaskRequest() {}
        
        public TaskRequest(String id, List<String> acceptedOutputModes, Message message) {
            this.id = id;
            this.acceptedOutputModes = acceptedOutputModes;
            this.message = message;
        }
    }

    /**
     * A2A Message structure
     */
    public static class Message {
        @JsonProperty("role")
        public String role;
        
        @JsonProperty("parts")
        public List<MessagePart> parts;
        
        public Message() {}
        
        public Message(String role, List<MessagePart> parts) {
            this.role = role;
            this.parts = parts;
        }
    }

    /**
     * Message Part (text, file, data)
     */
    public static class MessagePart {
        @JsonProperty("type")
        public String type;
        
        @JsonProperty("text")
        public String text;
        
        public MessagePart() {}
        
        public MessagePart(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    /**
     * A2A Task Response - received from /send-task endpoint
     */
    public static class TaskResponse {
        @JsonProperty("id")
        public String id;
        
        @JsonProperty("result")
        public TaskResult result;
        
        public TaskResponse() {}
    }

    /**
     * Task Result structure
     */
    public static class TaskResult {
        @JsonProperty("id")
        public String id;
        
        @JsonProperty("status")
        public TaskStatus status;
        
        public TaskResult() {}
    }

    /**
     * Task Status structure
     */
    public static class TaskStatus {
        @JsonProperty("state")
        public String state;
        
        @JsonProperty("message")
        public Message message;
        
        public TaskStatus() {}
    }

    /**
     * Legacy Tool Invocation Request (for backward compatibility)
     * This converts tool-based requests to A2A protocol
     */
    public static class ToolInvocationRequest {
        @JsonProperty("tool_name")
        public String toolName;
        
        @JsonProperty("parameters")
        public Map<String, Object> parameters;
        
        public ToolInvocationRequest() {}
        
        public ToolInvocationRequest(String toolName, Map<String, Object> parameters) {
            this.toolName = toolName;
            this.parameters = parameters;
        }
        
        /**
         * Convert tool invocation to A2A task request
         */
        public TaskRequest toA2ATaskRequest(String taskId) {
            // Create natural language instruction from tool name and parameters
            String instruction = buildInstructionFromTool();
            
            MessagePart textPart = new MessagePart("text", instruction);
            Message message = new Message("user", List.of(textPart));
            
            return new TaskRequest(
                taskId,
                List.of("text", "application/json"),
                message
            );
        }
        
        /**
         * Build natural language instruction from tool name and parameters
         */
        private String buildInstructionFromTool() {
            StringBuilder instruction = new StringBuilder();
            
            // Convert tool name to natural language
            switch (toolName) {
                case "setup_workshop_rag_tool":
                    instruction.append("Setup RAG system for workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" named '").append(parameters.get("workshop_name")).append("'");
                    }
                    if (parameters.containsKey("workshop_content")) {
                        instruction.append(" with content: ").append(parameters.get("workshop_content"));
                    }
                    break;
                    
                case "clone_showroom_template_tool":
                    instruction.append("Clone showroom template");
                    if (parameters.containsKey("template_name")) {
                        instruction.append(" '").append(parameters.get("template_name")).append("'");
                    }
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" for workshop '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                case "analyze_repository_tool":
                    instruction.append("Analyze repository");
                    if (parameters.containsKey("repository_url")) {
                        instruction.append(" at ").append(parameters.get("repository_url"));
                    }
                    break;
                    
                case "create_workshop_tool":
                    instruction.append("Create workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" named '").append(parameters.get("workshop_name")).append("'");
                    }
                    if (parameters.containsKey("repository_url")) {
                        instruction.append(" from repository ").append(parameters.get("repository_url"));
                    }
                    break;
                    
                case "validate_workshop_content_tool":
                    instruction.append("Validate workshop content");
                    if (parameters.containsKey("workshop_content")) {
                        instruction.append(": ").append(parameters.get("workshop_content"));
                    }
                    break;
                    
                case "validate_external_references_tool":
                    instruction.append("Validate external references for workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                case "update_rag_content_tool":
                    instruction.append("Update RAG content for workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                case "enhance_with_references_tool":
                    instruction.append("Enhance workshop content with validated references");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" for '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                case "manage_workshop_repository_tool":
                    instruction.append("Manage workshop repository");
                    if (parameters.containsKey("repository_name")) {
                        instruction.append(" '").append(parameters.get("repository_name")).append("'");
                    }
                    break;
                    
                case "update_workshop_tool":
                    instruction.append("Update workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                case "generate_workshop_documentation_tool":
                    instruction.append("Generate documentation for workshop");
                    if (parameters.containsKey("workshop_name")) {
                        instruction.append(" '").append(parameters.get("workshop_name")).append("'");
                    }
                    break;
                    
                default:
                    instruction.append("Execute tool '").append(toolName).append("'");
                    if (!parameters.isEmpty()) {
                        instruction.append(" with parameters: ").append(parameters.toString());
                    }
            }
            
            return instruction.toString();
        }
    }
}
