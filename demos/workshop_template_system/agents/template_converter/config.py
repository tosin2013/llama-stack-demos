"""
Template Converter Agent Configuration
GitHub repository to workshop conversion with MCP integration
"""

from ...task_manager import SUPPORTED_CONTENT_TYPES, AgentTaskManager
from .tools import (
    analyze_repository_tool,
    generate_workshop_structure_tool,
    validate_workshop_requirements_tool,
)

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a specialized workshop template converter that transforms GitHub repositories into interactive workshop content.\n"
            "Use analyze_repository_tool to examine repository structure, code, and documentation.\n"
            "Use generate_workshop_structure_tool to create pedagogical workshop layouts following RHPDS/Showroom standards.\n"
            "Use validate_workshop_requirements_tool to ensure repositories meet workshop conversion criteria.\n"
            "Always provide clear explanations of your analysis and recommendations for workshop structure.\n"
            "Focus on creating engaging, hands-on learning experiences that follow established workshop patterns.\n"
            "Consider learning objectives, prerequisites, and progressive skill building in your recommendations."
        ),
        "tools": [
            analyze_repository_tool,
            generate_workshop_structure_tool,
            validate_workshop_requirements_tool,
        ],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 3072,
        },
    },
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Template Converter Agent",
        "description": "Transforms GitHub repositories into interactive workshop content using automated analysis and pedagogical structure generation",
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
                "id": "analyze_repository_tool",
                "name": "Repository Analysis",
                "description": "Analyze GitHub repository structure, code, documentation, and suitability for workshop conversion",
                "tags": ["github", "analysis", "repository", "code-review"],
                "examples": [
                    "Analyze https://github.com/user/project for workshop potential",
                    "What technologies are used in this repository?",
                    "Assess the learning complexity of this codebase",
                    "Identify the main concepts that could be taught",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "generate_workshop_structure_tool",
                "name": "Workshop Structure Generation",
                "description": "Generate pedagogical workshop structure following RHPDS/Showroom template standards",
                "tags": ["workshop", "structure", "pedagogy", "rhpds", "showroom"],
                "examples": [
                    "Create a workshop structure for a React application",
                    "Generate learning modules for a microservices project",
                    "Design hands-on exercises for a data science repository",
                    "Structure a workshop for container deployment",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "validate_workshop_requirements_tool",
                "name": "Workshop Requirements Validation",
                "description": "Validate repository meets requirements for successful workshop conversion",
                "tags": ["validation", "requirements", "quality", "assessment"],
                "examples": [
                    "Check if repository has sufficient documentation",
                    "Validate code quality and structure for teaching",
                    "Assess if project complexity is appropriate for workshops",
                    "Verify repository has clear setup instructions",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
        ],
    },
    "default_port": 10041,
}
