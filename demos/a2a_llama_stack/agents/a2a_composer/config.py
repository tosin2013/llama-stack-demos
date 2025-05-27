# Updated import to use AgentTaskManager from the root a2a_llama_stack directory
from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
# common.types.AgentSkill, AgentCapabilities are imported in generic_main

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.2:3b-instruct-fp16",
        "instructions": "You are skilled at writing human-friendly text based on the query and associated skills.",
        "max_infer_iters": 3,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 4096,
        },
        "tools": []
    },
    # Updated to use AgentTaskManager
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Writing Agent",
        "description": "Generate human-friendly text based on the query and associated skills",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": False,
            "pushNotifications": False,
            "stateTransitionHistory": False,
        },
        "skills_params": [
            {
                "id": "writing_agent",
                "name": "Writing Agent",
                "description": "Write human-friendly text based on the query and associated skills",
                "tags": ["writing"],
                "examples": ["Write human-friendly text based on the query and associated skills"],
                "inputModes": ["text/plain"],
                "outputModes": ["application/json"], # As per original file
            }
        ]
    },
    "default_port": 10012,
}