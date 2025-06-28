import os
import logging
import importlib
import click

from llama_stack_client import LlamaStackClient
from llama_stack_client.types import AgentConfig
from common.server import A2AServer
from common.types import AgentCard, AgentCapabilities, AgentSkill

logging.basicConfig(level=logging.INFO)

def build_server(agent_name: str, host: str, port: int = None):
    """
    Build A2A server for workshop template system agents.
    
    Extends the proven patterns from demos/a2a_llama_stack with workshop-specific
    agent configurations and port allocation (10040-10051).
    """
    try:
        config_module_path = f"{__package__}.agents.{agent_name.replace('-', '_')}.config"
        config_module = importlib.import_module(config_module_path)
        agent_config_data = config_module.AGENT_CONFIG
    except ModuleNotFoundError:
        logging.error(f"Configuration module not found for agent: {agent_name} at {config_module_path}")
        raise
    except AttributeError:
        logging.error(f"AGENT_CONFIG not found in {config_module_path}")
        raise

    if port is None:
        port = agent_config_data.get("default_port", 8000)

    agent_params_config = agent_config_data["agent_params"]
    tools_to_pass = agent_params_config.get("tools", [])

    # Create Llama Stack agent with workshop-specific configuration
    agent = Agent(
        client=LlamaStackClient(base_url=os.getenv("LLAMA_STACK_ENDPOINT", "http://localhost:8321")),
        model=os.getenv(agent_params_config["model_env_var"], agent_params_config["default_model"]),
        instructions=agent_params_config["instructions"],
        tools=tools_to_pass,
        max_infer_iters=agent_params_config.get("max_infer_iters", 3),
        sampling_params=agent_params_config.get("sampling_params", None)
    )

    # Initialize task manager for A2A protocol bridging
    TaskManagerClass = agent_config_data["task_manager_class"]
    task_manager = TaskManagerClass(agent=agent, internal_session_id=True)

    # Build agent card for capability advertisement
    card_params_config = agent_config_data["agent_card_params"]
    agent_skills = [AgentSkill(**skill_p) for skill_p in card_params_config.get("skills_params", [])]
    capabilities = AgentCapabilities(**card_params_config.get("capabilities_params", {}))

    card = AgentCard(
        name=card_params_config["name"],
        description=card_params_config["description"],
        url=f"http://{host}:{port}/",
        version=card_params_config.get("version", "0.1.0"),
        defaultInputModes=card_params_config.get("default_input_modes", ["text/plain"]),
        defaultOutputModes=card_params_config.get("default_output_modes", ["text/plain"]),
        capabilities=capabilities,
        skills=agent_skills
    )

    return A2AServer(
        agent_card=card,
        task_manager=task_manager,
        host=host,
        port=port,
    )

@click.command()
@click.option("--agent-name", required=True, 
              help="Workshop agent to run: workshop_chat, template_converter, documentation_pipeline, source_manager")
@click.option("--host", default="0.0.0.0", help="Host to bind the server to.")
@click.option("--port", type=int, default=None, help="Port to bind the server to (overrides agent's default).")
def main(agent_name, host, port):
    """
    Start workshop template system agents.
    
    Available agents:
    - workshop_chat (port 10040): RAG-based participant assistance
    - template_converter (port 10041): GitHub to workshop conversion  
    - documentation_pipeline (port 10050): Automated content updates
    - source_manager (port 10051): Trusted source validation
    """
    # Determine effective port for logging
    effective_port = port
    if port is None:
        try:
            config_module_path = f"demos.workshop_template_system.agents.{agent_name.replace('-', '_')}.config"
            config_module = importlib.import_module(config_module_path)
            effective_port = config_module.AGENT_CONFIG.get("default_port", "config_default")
        except Exception:
            effective_port = "unknown_default"

    logging.info(f"Starting Workshop Template System agent: {agent_name}")
    logging.info(f"Server binding to {host}:{effective_port}")
    
    server = build_server(agent_name=agent_name, host=host, port=port)
    server.start()
    
    logging.info(f"✓ Workshop agent '{agent_name}' is running on {host}:{effective_port}")
    logging.info(f"✓ Agent card available at http://{host}:{effective_port}/agent-card")

if __name__ == "__main__":
    main()
