import asyncio
import threading
from typing import Dict
from uuid import uuid4

from llama_stack_client.lib.agents.client_tool import ClientTool
from llama_stack_client.types.tool_def_param import Parameter

from common.client import A2ACardResolver, A2AClient
from common.types import AgentCard, TextPart


class A2ATool(ClientTool):
    """
    A wrapper for communicating with an external A2A agent.
    """

    def __init__(self, agent_url: str, agent_card: AgentCard = None):
        self.url = agent_url
        if agent_card is None:
            self.agent_card = A2ACardResolver(self.url).get_agent_card()
        else:
            self.agent_card = agent_card
        self.client = A2AClient(agent_card=self.agent_card)

    def get_name(self) -> str:
        return self.agent_card.name

    def get_description(self) -> str:
        return self.agent_card.description

    def get_params_definition(self) -> Dict[str, Parameter]:
        return {
            "query": Parameter(
                name="query",
                parameter_type="str",
                description="A free-text query specifying the desired functionality of this tool",
                required=True,
            )
        }

    def run_impl(self, query: str):
        # we should cover both the case where the method is called from non-async code, and when
        # there is an active event loop (i.e., async code)
        try:
            asyncio.get_running_loop()
        except RuntimeError:
            # run_impl is called from non-async code
            return asyncio.run(self.async_run_impl(query=query))
        else:
            # run_impl is called from async code
            return self._execute_async_run_in_new_loop(query=query)

    async def async_run_impl(self, **kwargs):
        message = {
            "role": "user",
            "parts": [
                {
                    "type": "text",
                    "text": kwargs["query"],
                }
            ],
        }

        task_id = uuid4().hex
        payload = {
            "id": task_id,
            "acceptedOutputModes": ["text"],
            "message": message,
        }

        response = await self.client.send_task(payload)
        # TODO: add support for FilePart and DataPart
        text_response_parts = [
            p for p in response.result.status.message.parts if isinstance(p, TextPart)
        ]
        return "\n".join([t.text for t in text_response_parts])

    def _execute_async_run_in_new_loop(self, **kwargs):
        result_container = {}
        exception_container = {}

        def thread_target():
            try:
                result_container["result"] = asyncio.run(self.async_run_impl(**kwargs))
            except Exception as e:
                exception_container["error"] = e

        thread = threading.Thread(target=thread_target)
        thread.start()
        thread.join()

        if "error" in exception_container:
            raise exception_container["error"]
        return result_container["result"]
