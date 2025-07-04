"""
Content Creator Agent Configuration
Original workshop creation from learning objectives and concepts
"""

from ...task_manager import SUPPORTED_CONTENT_TYPES, AgentTaskManager
from .tools import (
    clone_showroom_template_tool,
    create_original_content_tool,
    design_workshop_from_objectives_tool,
    generate_exercises_tool,
)

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "meta-llama/Llama-3.2-3B-Instruct",
        "instructions": (
            "You are a content creator agent that generates original workshop content from learning objectives and concepts.\n"
            "Use design_workshop_from_objectives_tool to create workshop structures from educational goals.\n"
            "Use create_original_content_tool to generate workshop content for concepts, tools, or cloud services.\n"
            "Use generate_exercises_tool to create hands-on exercises and practical activities.\n"
            "Focus on pedagogical best practices, progressive skill building, and engaging learning experiences.\n"
            "Coordinate with research agents to ensure content accuracy and currency.\n"
            "Create workshops that work without requiring specific source repositories."
        ),
        "tools": [
            design_workshop_from_objectives_tool,
            create_original_content_tool,
            generate_exercises_tool,
            clone_showroom_template_tool,
        ],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 3072,
        },
    },
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Content Creator Agent",
        "description": "Creates original workshop content from learning objectives, concepts, and educational goals without requiring source repositories",
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
                "id": "design_workshop_from_objectives_tool",
                "name": "Workshop Design from Objectives",
                "description": "Design complete workshop structures from learning objectives and educational goals",
                "tags": ["design", "objectives", "pedagogy", "structure"],
                "examples": [
                    "Create a workshop to teach cloud security fundamentals",
                    "Design a workshop for API design best practices",
                    "Build a workshop on microservices architecture concepts",
                    "Create a workshop for DevOps culture and practices",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "create_original_content_tool",
                "name": "Original Content Creation",
                "description": "Generate original workshop content for concepts, tools, cloud services, or theoretical topics",
                "tags": ["content", "creation", "original", "concepts"],
                "examples": [
                    "Create content for AWS services workshop",
                    "Generate content for design patterns workshop",
                    "Build content for agile methodology workshop",
                    "Create content for data visualization concepts",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "generate_exercises_tool",
                "name": "Exercise Generation",
                "description": "Create hands-on exercises, activities, and practical learning experiences",
                "tags": ["exercises", "hands-on", "activities", "practice"],
                "examples": [
                    "Create hands-on exercises for cloud deployment",
                    "Generate API testing activities",
                    "Design troubleshooting scenarios",
                    "Create collaborative learning activities",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "clone_showroom_template_tool",
                "name": "Showroom Template Integration",
                "description": "Clone and customize the official RHPDS Showroom template for professional workshop creation",
                "tags": ["showroom", "rhpds", "template", "professional"],
                "examples": [
                    "Clone Showroom template for new workshop",
                    "Customize Showroom template for specific technology",
                    "Set up RHPDS-compatible workshop structure",
                    "Initialize professional workshop layout",
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
        ],
    },
    "default_port": 10080,
}
