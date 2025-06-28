"""
Documentation Pipeline Agent Configuration
Automated content updates with human-in-the-loop validation
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import monitor_repository_changes_tool, analyze_impact_tool, create_update_proposal_tool, monitor_external_sources_tool

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a documentation pipeline agent that monitors source repositories and orchestrates workshop content updates.\n"
            "Use monitor_repository_changes_tool to detect changes in source repositories.\n"
            "Use analyze_impact_tool to determine which workshop sections need updates based on repository changes.\n"
            "Use create_update_proposal_tool to generate human-reviewable update proposals.\n"
            "Always prioritize human oversight and approval for content changes.\n"
            "Provide clear explanations of detected changes and their impact on workshop content.\n"
            "Focus on maintaining workshop quality and educational effectiveness through systematic updates."
        ),
        "tools": [monitor_repository_changes_tool, analyze_impact_tool, create_update_proposal_tool, monitor_external_sources_tool],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Documentation Pipeline Agent",
        "description": "Monitors source repositories and orchestrates workshop content updates with human-in-the-loop validation",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": True,
            "pushNotifications": True,
            "stateTransitionHistory": True,
        },
        "skills_params": [
            {
                "id": "monitor_repository_changes_tool",
                "name": "Repository Change Monitoring",
                "description": "Monitor source repositories for changes that may affect workshop content",
                "tags": ["monitoring", "github", "changes", "automation"],
                "examples": [
                    "Monitor https://github.com/user/project for changes",
                    "Check for updates in the main branch",
                    "Detect new releases or version tags",
                    "Track documentation changes"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "analyze_impact_tool",
                "name": "Change Impact Analysis",
                "description": "Analyze repository changes to determine impact on workshop content",
                "tags": ["analysis", "impact", "workshop", "content"],
                "examples": [
                    "Analyze impact of API changes on workshop exercises",
                    "Assess how dependency updates affect setup instructions",
                    "Evaluate new features for workshop inclusion",
                    "Review breaking changes for content updates"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "create_update_proposal_tool",
                "name": "Update Proposal Creation",
                "description": "Create human-reviewable proposals for workshop content updates",
                "tags": ["proposals", "updates", "human-review", "workflow"],
                "examples": [
                    "Create proposal for updating setup instructions",
                    "Generate update plan for new API endpoints",
                    "Propose exercise modifications for version changes",
                    "Draft content updates for security patches"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "monitor_external_sources_tool",
                "name": "External Documentation Monitoring",
                "description": "Monitor external documentation sources like PDFs, websites, and APIs for changes that affect workshop content",
                "tags": ["external", "documentation", "monitoring", "rag"],
                "examples": [
                    "Monitor OpenShift documentation for API changes",
                    "Track updates to external PDF guides",
                    "Watch for changes in third-party documentation",
                    "Monitor vendor documentation and release notes"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10050,
}
