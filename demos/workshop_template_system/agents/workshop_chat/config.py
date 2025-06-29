"""
Workshop Chat Agent Configuration
RAG-based participant assistance for workshop content
"""

from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import (
    workshop_query_tool,
    workshop_navigation_tool,
    update_workshop_rag_content_tool,
    manage_rag_content_versions_tool
)

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": (
            "You are a helpful workshop assistant with access to workshop content through RAG and dynamic content management.\n"
            "Use workshop_query_tool to answer questions about workshop content.\n"
            "Use workshop_navigation_tool to help users navigate workshop sections.\n"
            "Use update_workshop_rag_content_tool to integrate approved content updates from Human Oversight Coordinator.\n"
            "Use manage_rag_content_versions_tool to manage RAG content versions, rollbacks, and cleanup operations.\n"
            "Always provide clear, helpful responses and guide users to relevant sections.\n"
            "If you cannot answer from the workshop content, suggest where users might find help.\n"
            "Be encouraging and supportive to workshop participants.\n"
            "Maintain RAG knowledge base integrity through proper content versioning and approval workflows."
        ),
        "tools": [
            workshop_query_tool,
            workshop_navigation_tool,
            update_workshop_rag_content_tool,
            manage_rag_content_versions_tool
        ],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Workshop Chat Assistant",
        "description": "Provides contextual assistance and navigation for workshop participants using RAG-based content retrieval",
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
                "id": "workshop_query_tool",
                "name": "Workshop Content Query",
                "description": "Answer questions using workshop content via RAG retrieval",
                "tags": ["rag", "assistance", "content", "qa"],
                "examples": [
                    "How do I set up the development environment?",
                    "What are the prerequisites for this workshop?",
                    "Explain the concept covered in section 3",
                    "I'm stuck on the Docker installation step"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "workshop_navigation_tool",
                "name": "Workshop Navigation",
                "description": "Provide guidance on workshop structure and navigation",
                "tags": ["navigation", "structure", "guidance", "sections"],
                "examples": [
                    "Where should I start?",
                    "Show me the troubleshooting section",
                    "What's in the exercises section?",
                    "Take me to the next step"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10040,
}
