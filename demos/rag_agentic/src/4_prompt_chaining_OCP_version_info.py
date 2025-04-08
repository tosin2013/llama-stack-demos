from llama_stack_client.lib.agents.agent import Agent
from llama_stack_client.lib.agents.event_logger import EventLogger
from llama_stack_client.types import Document

from llama_stack_client import LlamaStackClient
from termcolor import cprint
import argparse
import logging
import uuid
import json
import os
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
stream_handler = logging.StreamHandler()
stream_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(message)s')
stream_handler.setFormatter(formatter)
logger.addHandler(stream_handler)


parser = argparse.ArgumentParser()
parser.add_argument("-r", "--remote", help="Uses the remote_url", action="store_true")
parser.add_argument("-s", "--session-info-on-exit", help="Prints agent session info on exit", action="store_true")
parser.add_argument("-a", "--auto", help="Automatically runs examples, and does not start a chat session", action="store_true")
args = parser.parse_args()

model="meta-llama/Llama-3.2-3B-Instruct"

# Connect to a llama stack server
if args.remote:
    base_url = os.getenv("REMOTE_BASE_URL")
else:
    base_url="http://localhost:8321"

client = LlamaStackClient(
    base_url=base_url,
    provider_data={
        "tavily_search_api_key": os.getenv("TAVILY_API_KEY")
    })
logger.info(f"Connected to Llama Stack server @ {base_url} \n")

# Get tool info and register tools
registered_tools = client.tools.list()
registered_tools_identifiers = [t.identifier for t in registered_tools]
registered_toolgroups = [t.toolgroup_id for t in registered_tools]
if  "builtin::websearch" not in registered_toolgroups:
    error = AssertionError("Expected tool `builtin::websearch` to exist, but does not. Please fix your llama-stack deployment.")
    logger.error(error)
    raise error

agent = Agent(
    client=client,
    model=model,
    instructions = """You are a helpful AI assistant, responsible for helping me find and communicate information back to my team.
    You have access to a number of tools.
    Whenever a tool is called, be sure return the Response in a friendly and helpful tone.
    When you are asked to search the web you must use a tool.
    My team is comprised of the folllowing members, with the corresponding emails: Ryan Cook (rcook@redhat.com), Sally O'Malley (somalley@redhat.com), Tom Coufal (tcoufal@redhat.com), and myself, Greg Pereira (grpereir@redhat.com).
    When signing off on work, please be sure to include: - Sent from my llama-stack agent in the signature
    """,
    tools=["builtin::websearch"],
    tool_config={"tool_choice":"auto"},
    sampling_params={"max_tokens":4096}
)

session_name = f"Draft_email_with_latest_OCP_version{uuid.uuid4()}"
session_id = agent.create_session(session_name=session_name)
prompts = [
    """Search for the web for the latest Red Hat OpenShift version on the Red Hat website.""",
    """Summarize the latest Red Hat OpenShift version number and any significant features, fixes, or changes that occure in this version.""",
    """Draft and format an email to convey this information to my team members."""
]

for i, prompt in enumerate(prompts):    
    turn_response = agent.create_turn(
        messages=[
            {
                "role": "user",
                "content": prompt,
            }
        ],
        session_id=session_id,
        stream=True,
    )
    logger.info(f"========= Turn: {i} =========")
    for log in EventLogger().log(turn_response):
        log.print()
