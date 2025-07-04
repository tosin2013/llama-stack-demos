import logging
from typing import AsyncIterable, Union, AsyncIterator, Optional

from llama_stack_client import LlamaStackClient
from llama_stack_client.types import AgentConfig
from typing import Any  # Temporary fix for Agent type

import common.server.utils as utils
from common.server.task_manager import InMemoryTaskManager
from common.types import (
    SendTaskRequest, SendTaskResponse,
    SendTaskStreamingRequest, SendTaskStreamingResponse,
    TaskStatus, Artifact,
    Message, TaskState,
    TaskStatusUpdateEvent, TaskArtifactUpdateEvent,
    JSONRPCResponse, TextPart
)

logger = logging.getLogger(__name__)

SUPPORTED_CONTENT_TYPES = ["text", "text/plain", "application/json"]


class AgentTaskManager(InMemoryTaskManager):
    def __init__(self, agent: Any, internal_session_id=False, tools=None):  # Temporary fix
        super().__init__()
        self.agent = agent
        self.tools = tools or []  # Store tools for direct invocation
        if internal_session_id:
            self.session_id = self.agent.create_session("custom-agent-session")
        else:
            self.session_id = None

    def _validate_request(
        self, request: Union[SendTaskRequest, SendTaskStreamingRequest]
    ) -> Optional[JSONRPCResponse]:
        params = request.params
        if not utils.are_modalities_compatible(
            params.acceptedOutputModes,
            SUPPORTED_CONTENT_TYPES
        ):
            logger.warning("Unsupported output modes: %s", params.acceptedOutputModes)
            return utils.new_incompatible_types_error(request.id)
        return None

    async def on_send_task(self, request: SendTaskRequest) -> SendTaskResponse:
        err = self._validate_request(request)
        if err:
            return err

        await self.upsert_task(request.params)
        result = self._invoke(
            request.params.message.parts[0].text,
            request.params.sessionId
        )
        parts = [TextPart(type="text", text=result)]
        status = TaskStatus(state=TaskState.COMPLETED, message=Message(role="agent", parts=parts))
        task = await self._update_store(request.params.id, status, [Artifact(parts=parts)])
        return SendTaskResponse(id=request.id, result=task)

    async def on_send_task_subscribe(
        self, request: SendTaskStreamingRequest
    ) -> Union[AsyncIterable[SendTaskStreamingResponse], JSONRPCResponse]:
        err = self._validate_request(request)
        if err:
            return err

        await self.upsert_task(request.params)
        return self._stream_generator(request)

    async def _stream_generator(
        self, request: SendTaskStreamingRequest
    ) -> AsyncIterable[SendTaskStreamingResponse]:
        params = request.params
        query = params.message.parts[0].text

        async for update in self._stream(query, params.sessionId):
            done = update["is_task_complete"]
            content = update["content"]
            delta = update["updates"]

            state = TaskState.COMPLETED if done else TaskState.WORKING
            text = content if done else delta
            parts = [TextPart(type="text", text=text)]
            artifacts = [Artifact(parts=parts)] if done else None

            status = TaskStatus(state=state, message=Message(role="agent", parts=parts))
            await self._update_store(request.params.id, status, artifacts or [])

            yield SendTaskStreamingResponse(
                id=request.id,
                result=TaskStatusUpdateEvent(id=params.id, status=status, final=done)
            )
            if artifacts:
                yield SendTaskStreamingResponse(
                    id=request.id,
                    result=TaskArtifactUpdateEvent(id=params.id, artifact=artifacts[0])
                )

    async def _update_store(self, task_id: str, status: TaskStatus, artifacts):
        async with self.lock:
            task = self.tasks[task_id]
            task.status = status
            if artifacts:
                task.artifacts = (task.artifacts or []) + artifacts
            return task

    def _invoke(self, query: str, session_id: str) -> str:
        """
        Route the user query through the Agent, executing tools as needed.
        """
        try:
            logger.info(f"Processing agent query: {query[:100]}...")

            # Check if this is a tool invocation request
            if self._is_tool_invocation(query):
                return self._handle_tool_invocation(query)

            # Determine which session to use
            if self.session_id is not None:
                sid = self.session_id
            else:
                sid = self.agent.create_session(session_id)

            # Send the user query to the Agent
            turn_resp = self.agent.create_turn(
                messages=[{"role": "user", "content": query}],
                session_id=sid,
            )

            # Extract tool and LLM outputs from events
            output = self._extract_response_content(turn_resp)

            if not output:
                output = f"Agent processed query successfully: {query[:50]}..."

            logger.info(f"Agent response: {output[:100]}...")
            return output

        except Exception as e:
            logger.error(f"Error in agent invocation: {e}")
            return f"Error processing request: {str(e)}"

    def _is_tool_invocation(self, query: str) -> bool:
        """Check if the query is a tool invocation request"""
        try:
            import json
            data = json.loads(query)
            return "tool_name" in data and "parameters" in data
        except:
            return False

    def _handle_tool_invocation(self, query: str) -> str:
        """Handle direct tool invocation"""
        try:
            import json
            data = json.loads(query)
            tool_name = data.get("tool_name")
            parameters = data.get("parameters", {})

            logger.info(f"Invoking tool: {tool_name} with parameters: {parameters}")

            # Find and execute the tool
            for tool in self.tools:
                if hasattr(tool, '__name__') and tool.__name__ == tool_name:
                    result = tool(**parameters)
                    return str(result)

            # If tool not found, return error
            return f"Tool '{tool_name}' not found. Available tools: {[getattr(t, '__name__', str(t)) for t in self.tools]}"

        except Exception as e:
            logger.error(f"Error in tool invocation: {e}")
            return f"Error invoking tool: {str(e)}"

    def _extract_response_content(self, turn_resp) -> str:
        """Extract content from agent turn response"""
        try:
            # Try to extract from different response formats
            if hasattr(turn_resp, 'content'):
                return str(turn_resp.content)
            elif hasattr(turn_resp, 'message') and hasattr(turn_resp.message, 'content'):
                return str(turn_resp.message.content)
            elif hasattr(turn_resp, 'text'):
                return str(turn_resp.text)
            elif isinstance(turn_resp, dict):
                if 'content' in turn_resp:
                    return str(turn_resp['content'])
                elif 'text' in turn_resp:
                    return str(turn_resp['text'])

            # Fallback: convert entire response to string
            return str(turn_resp)

        except Exception as e:
            logger.error(f"Error extracting response content: {e}")
            return f"Response extraction error: {str(e)}"

    async def _stream(self, query: str, session_id: str) -> AsyncIterator[dict]:
        """
        Simplest streaming stub: synchronously invoke and emit once.
        """
        result = self._invoke(query, session_id)
        yield {"updates": result, "is_task_complete": True, "content": result}
