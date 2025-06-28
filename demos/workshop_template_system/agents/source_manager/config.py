"""
Source Manager Agent Configuration
Repository management and workshop deployment coordination
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import manage_workshop_repository_tool, coordinate_deployment_tool, sync_content_tool, export_github_pages_tool, commit_to_gitea_tool, trigger_buildconfig_tool

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a source manager agent that coordinates workshop repository management and deployment processes.\n"
            "Use manage_workshop_repository_tool to handle workshop repository operations like creation, updates, and maintenance.\n"
            "Use coordinate_deployment_tool to orchestrate workshop deployments to RHPDS/Showroom platforms.\n"
            "Use sync_content_tool to synchronize content between source repositories and workshop repositories.\n"
            "Always ensure proper version control, backup procedures, and deployment validation.\n"
            "Provide clear status updates and coordinate with other agents in the workshop template system.\n"
            "Focus on maintaining workshop repository integrity and successful deployment coordination."
        ),
        "tools": [manage_workshop_repository_tool, coordinate_deployment_tool, sync_content_tool, export_github_pages_tool, commit_to_gitea_tool, trigger_buildconfig_tool],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Source Manager Agent",
        "description": "Coordinates workshop repository management and deployment processes with version control and validation",
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
                "id": "manage_workshop_repository_tool",
                "name": "Workshop Repository Management",
                "description": "Create, update, and maintain workshop repositories with proper version control",
                "tags": ["repository", "git", "version-control", "maintenance"],
                "examples": [
                    "Create a new workshop repository from template",
                    "Update workshop content from source repository",
                    "Manage workshop repository branches and releases",
                    "Backup and restore workshop repositories"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "coordinate_deployment_tool",
                "name": "Deployment Coordination",
                "description": "Orchestrate workshop deployments to RHPDS/Showroom platforms with validation",
                "tags": ["deployment", "rhpds", "showroom", "orchestration"],
                "examples": [
                    "Deploy workshop to RHPDS environment",
                    "Coordinate Showroom workshop publication",
                    "Validate deployment prerequisites",
                    "Monitor deployment status and health"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "sync_content_tool",
                "name": "Content Synchronization",
                "description": "Synchronize content between source repositories and workshop repositories",
                "tags": ["sync", "content", "integration", "automation"],
                "examples": [
                    "Sync latest changes from source repository",
                    "Update workshop content with new features",
                    "Resolve content conflicts and merges",
                    "Validate content synchronization"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "export_github_pages_tool",
                "name": "GitHub Pages Export",
                "description": "Export workshop for static GitHub Pages deployment with upgrade path to OpenShift features",
                "tags": ["github-pages", "static", "export", "deployment"],
                "examples": [
                    "Export workshop for GitHub Pages hosting",
                    "Create static version with upgrade information",
                    "Generate deployment instructions for GitHub Pages",
                    "Prepare workshop for free static hosting"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "commit_to_gitea_tool",
                "name": "Gitea Repository Commit",
                "description": "Commit workshop content to Gitea repository to trigger OpenShift BuildConfig automation",
                "tags": ["gitea", "git", "commit", "buildconfig", "ci-cd"],
                "examples": [
                    "Commit updated workshop content to Gitea",
                    "Trigger automatic OpenShift builds via Git push",
                    "Deploy workshop updates through GitOps workflow",
                    "Integrate with CI/CD pipeline for live updates"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "trigger_buildconfig_tool",
                "name": "OpenShift BuildConfig Trigger",
                "description": "Manually trigger OpenShift BuildConfig for workshop deployment and updates",
                "tags": ["openshift", "buildconfig", "build", "deployment", "trigger"],
                "examples": [
                    "Manually trigger workshop build in OpenShift",
                    "Force rebuild after content updates",
                    "Restart deployment pipeline for bug fixes",
                    "Trigger build for workshop enhancements"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10060,
}
