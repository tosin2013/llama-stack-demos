"""
Repository Cloning Agent Configuration
ADR-0001 compliant repository and template cloning into shared workspace
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import (
    clone_repositories_for_workflow_tool,
    validate_cloned_repositories_tool
)

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a repository cloning agent that implements ADR-0001 workshop template strategy.\n"
            "Use clone_repositories_for_workflow_tool to clone repositories into shared workspace based on workflow type:\n"
            "- Workflow 1 (application): Clone showroom_template_default + source repository for analysis\n"
            "- Workflow 3 (existing workshop): Clone existing workshop repository for enhancement\n"
            "Use validate_cloned_repositories_tool to verify successful cloning and repository structure.\n"
            "Always ensure repositories are properly cloned into shared workspace for agent coordination.\n"
            "Follow ADR-0001 dual-template strategy based on repository classification.\n"
            "Provide clear status updates and coordinate with other agents in the workshop template system.\n"
            "Focus on reliable repository cloning, proper workspace organization, and ADR-0001 compliance."
        ),
        "tools": [
            clone_repositories_for_workflow_tool,
            validate_cloned_repositories_tool
        ],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Repository Cloning Agent",
        "description": "ADR-0001 compliant repository and template cloning into shared workspace for workshop creation",
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
                "id": "clone_repositories_for_workflow_tool",
                "name": "ADR-0001 Repository Cloning",
                "description": "Clone repositories into shared workspace based on ADR-0001 workflow strategy",
                "tags": ["adr-0001", "repository-cloning", "shared-workspace", "workflow", "template"],
                "examples": [
                    "Clone showroom_template_default for Workflow 1 (application → workshop)",
                    "Clone existing workshop repository for Workflow 3 (enhancement)",
                    "Organize repositories in shared workspace for agent coordination",
                    "Create working copies for workshop customization"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "validate_cloned_repositories_tool",
                "name": "Repository Validation",
                "description": "Validate that repositories were properly cloned into shared workspace",
                "tags": ["validation", "shared-workspace", "repository", "structure"],
                "examples": [
                    "Verify showroom template structure is complete",
                    "Validate source repository cloning success",
                    "Check workshop working copy creation",
                    "Confirm ADR-0001 compliance in cloned repositories"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10070,
}
