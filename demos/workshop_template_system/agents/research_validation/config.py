"""
Research & Validation Agent Configuration
Internet-grounded fact-checking and content validation
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import research_technology_tool, validate_content_accuracy_tool, find_learning_resources_tool, web_search_validation_tool

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a research and validation agent that ensures workshop content accuracy through internet-grounded fact-checking.\n"
            "Use research_technology_tool to gather current information about technologies, versions, and best practices.\n"
            "Use validate_content_accuracy_tool to cross-reference workshop content with authoritative sources.\n"
            "Use find_learning_resources_tool to discover additional educational materials and references.\n"
            "Always prioritize authoritative sources like official documentation, established tutorials, and reputable technical sites.\n"
            "Provide clear citations and confidence levels for your research findings.\n"
            "Flag potentially outdated or inaccurate information with specific recommendations for updates."
        ),
        "tools": [research_technology_tool, validate_content_accuracy_tool, find_learning_resources_tool, web_search_validation_tool],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 3072,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Research & Validation Agent",
        "description": "Provides internet-grounded fact-checking and content validation to ensure workshop accuracy and currency",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": True,
            "pushNotifications": False,
            "stateTransitionHistory": True,
        },
        "skills_params": [
            {
                "id": "research_technology_tool",
                "name": "Technology Research",
                "description": "Research current information about technologies, versions, APIs, and best practices",
                "tags": ["research", "technology", "versions", "documentation"],
                "examples": [
                    "Research current React version and new features",
                    "Find latest Docker installation procedures",
                    "Check current Kubernetes API versions",
                    "Validate Python package dependencies"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "validate_content_accuracy_tool",
                "name": "Content Accuracy Validation",
                "description": "Cross-reference workshop content with authoritative sources for accuracy",
                "tags": ["validation", "accuracy", "fact-checking", "sources"],
                "examples": [
                    "Validate installation instructions against official docs",
                    "Check API endpoint examples for accuracy",
                    "Verify code examples work with current versions",
                    "Confirm troubleshooting steps are current"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "find_learning_resources_tool",
                "name": "Learning Resources Discovery",
                "description": "Find additional educational materials, tutorials, and references",
                "tags": ["resources", "learning", "tutorials", "references"],
                "examples": [
                    "Find beginner-friendly tutorials for React",
                    "Discover advanced Docker learning resources",
                    "Locate official documentation and guides",
                    "Find community resources and examples"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "web_search_validation_tool",
                "name": "Web Search Validation",
                "description": "Search the web for current information to validate workshop content accuracy and currency",
                "tags": ["web-search", "validation", "fact-checking", "current"],
                "examples": [
                    "Validate OpenShift 4.16 installation procedures",
                    "Check current Quarkus WebSocket best practices",
                    "Verify latest Kubernetes security recommendations",
                    "Fact-check machine learning deployment patterns"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10070,
}
