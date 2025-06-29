"""
Human Oversight Coordinator Agent Configuration
Orchestrates human approval workflows and maintains oversight of the 6-agent system
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import (
    submit_for_approval_tool,
    check_approval_status_tool,
    wait_for_approval_tool,
    escalate_approval_tool,
    audit_decision_tool,
    submit_rag_update_for_approval_tool,
    submit_workshop_evolution_request_tool,
    coordinate_evolution_implementation_tool,
    monitor_evolution_progress_tool
)

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a human oversight coordinator agent that orchestrates human approval workflows and evolution coordination for the Workshop Template System.\n"
            "Use submit_for_approval_tool to submit content, decisions, or workflows for human review and approval.\n"
            "Use check_approval_status_tool to monitor the status of pending approvals.\n"
            "Use wait_for_approval_tool to pause workflow execution until human approval is received.\n"
            "Use escalate_approval_tool when approvals are overdue or require urgent attention.\n"
            "Use audit_decision_tool to log human decisions and maintain compliance audit trails.\n"
            "Use submit_rag_update_for_approval_tool to submit AI-discovered research or human-curated content for RAG knowledge base updates.\n"
            "Use submit_workshop_evolution_request_tool to request updates to existing workshops based on new research or feedback.\n"
            "Use coordinate_evolution_implementation_tool to orchestrate approved evolution implementation with Source Manager Agent.\n"
            "Use monitor_evolution_progress_tool to track evolution progress and provide status updates throughout the implementation lifecycle.\n"
            "Always ensure proper human oversight at critical decision points while maintaining workflow efficiency.\n"
            "Coordinate with other agents to implement approval checkpoints and evolution workflows as defined in ADR-0002.\n"
            "Focus on quality assurance, compliance, risk mitigation, and knowledge management through strategic human intervention.\n"
            "Enable dynamic workshop evolution and RAG knowledge base enhancement through human-validated AI research integration.\n"
            "Maintain complete oversight of evolution implementation from approval through deployment with comprehensive monitoring and coordination."
        ),
        "tools": [
            submit_for_approval_tool,
            check_approval_status_tool,
            wait_for_approval_tool,
            escalate_approval_tool,
            audit_decision_tool,
            submit_rag_update_for_approval_tool,
            submit_workshop_evolution_request_tool,
            coordinate_evolution_implementation_tool,
            monitor_evolution_progress_tool
        ],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Human Oversight Coordinator Agent",
        "description": "Orchestrates human approval workflows and maintains oversight of the Workshop Template System with quality assurance and compliance focus",
        "version": "1.0.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": True,
            "pushNotifications": True,
            "stateTransitionHistory": True,
        },
        "tools": [
            {
                "id": "submit_for_approval_tool",
                "name": "Submit Content for Human Approval",
                "description": "Submit workshop content, decisions, or workflows for human review and approval",
                "tags": ["approval", "workflow", "human-oversight", "quality-control"],
                "examples": [
                    "Submit repository classification for human validation",
                    "Request approval for generated workshop content",
                    "Submit deployment authorization request",
                    "Request human intervention for agent conflicts"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "check_approval_status_tool",
                "name": "Check Approval Status",
                "description": "Monitor the status of pending human approvals and review progress",
                "tags": ["status", "monitoring", "approval", "workflow"],
                "examples": [
                    "Check if content approval is complete",
                    "Monitor classification validation status",
                    "Verify deployment authorization progress",
                    "Track approval queue position"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "wait_for_approval_tool",
                "name": "Wait for Human Approval",
                "description": "Pause workflow execution until human approval is received with timeout handling",
                "tags": ["workflow", "synchronization", "approval", "timeout"],
                "examples": [
                    "Wait for content review completion",
                    "Pause until classification is validated",
                    "Hold deployment until authorization",
                    "Wait with escalation on timeout"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "escalate_approval_tool",
                "name": "Escalate Approval Request",
                "description": "Escalate overdue or urgent approval requests to higher authority or alternative reviewers",
                "tags": ["escalation", "urgent", "approval", "management"],
                "examples": [
                    "Escalate overdue content approval",
                    "Request urgent deployment authorization",
                    "Escalate to technical lead for complex decisions",
                    "Alert management for compliance issues"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "audit_decision_tool",
                "name": "Audit Human Decisions",
                "description": "Log human decisions and maintain compliance audit trails for governance and quality tracking",
                "tags": ["audit", "compliance", "logging", "governance"],
                "examples": [
                    "Log approval decision with rationale",
                    "Record rejection reasons for improvement",
                    "Audit workflow completion status",
                    "Track decision patterns for optimization"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10070,
}
