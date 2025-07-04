"""
A2A Server Implementation
Simplified HTTP server for A2A protocol
"""

import json
import logging
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Any, Dict
from urllib.parse import urlparse, parse_qs

from ..types import AgentCard

logger = logging.getLogger(__name__)


class A2ARequestHandler(BaseHTTPRequestHandler):
    """HTTP request handler for A2A protocol"""
    
    def __init__(self, agent_card: AgentCard, task_manager, *args, **kwargs):
        self.agent_card = agent_card
        self.task_manager = task_manager
        super().__init__(*args, **kwargs)
    
    def do_GET(self):
        """Handle GET requests"""
        parsed_path = urlparse(self.path)
        
        if parsed_path.path == "/agent-card":
            self._serve_agent_card()
        elif parsed_path.path == "/health":
            self._serve_health()
        else:
            self._serve_404()
    
    def do_POST(self):
        """Handle POST requests"""
        parsed_path = urlparse(self.path)
        
        if parsed_path.path == "/send-task":
            self._handle_send_task()
        elif parsed_path.path == "/invoke":
            self._handle_tool_invoke()
        else:
            self._serve_404()
    
    def _serve_agent_card(self):
        """Serve the agent card"""
        try:
            # Convert dataclass to dict for JSON serialization
            card_dict = {
                "name": self.agent_card.name,
                "description": self.agent_card.description,
                "url": self.agent_card.url,
                "version": self.agent_card.version,
                "defaultInputModes": self.agent_card.defaultInputModes,
                "defaultOutputModes": self.agent_card.defaultOutputModes,
                "capabilities": {
                    "streaming": self.agent_card.capabilities.streaming,
                    "pushNotifications": self.agent_card.capabilities.pushNotifications,
                    "stateTransitionHistory": self.agent_card.capabilities.stateTransitionHistory,
                },
                "skills": [
                    {
                        "id": skill.id,
                        "name": skill.name,
                        "description": skill.description,
                        "tags": skill.tags,
                        "examples": skill.examples,
                        "inputModes": skill.inputModes,
                        "outputModes": skill.outputModes,
                    }
                    for skill in self.agent_card.skills
                ]
            }
            
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps(card_dict, indent=2).encode())
            
        except Exception as e:
            logger.error(f"Error serving agent card: {e}")
            self._serve_500(str(e))
    
    def _serve_health(self):
        """Serve health check"""
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({"status": "healthy"}).encode())
    
    def _handle_send_task(self):
        """Handle task submission with real agent processing"""
        try:
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            request_data = json.loads(post_data.decode('utf-8'))

            logger.info(f"Processing task request: {request_data.get('id', 'unknown')}")

            # Process task through task manager
            try:
                # Create SendTaskRequest from incoming data
                from common.types import SendTaskRequest
                task_request = SendTaskRequest(**request_data)

                # Process task asynchronously
                import asyncio
                task_response = asyncio.run(self.task_manager.on_send_task(task_request))

                # Convert response to dict format
                response = {
                    "id": task_response.id,
                    "result": {
                        "id": task_response.result.id,
                        "status": {
                            "state": task_response.result.status.state,
                            "message": {
                                "role": task_response.result.status.message.role,
                                "parts": [{"type": part.type, "text": part.text} for part in task_response.result.status.message.parts]
                            }
                        },
                        "artifacts": [{"parts": [{"type": part.type, "text": part.text} for part in artifact.parts]} for artifact in task_response.result.artifacts] if task_response.result.artifacts else []
                    }
                }

                logger.info(f"Task processed successfully: {response['result']['status']['state']}")

            except Exception as e:
                logger.error(f"Error processing task through task manager: {e}")
                # Fallback response with error information
                response = {
                    "id": request_data.get("id", "unknown"),
                    "result": {
                        "id": request_data.get("params", {}).get("id", "task-id"),
                        "status": {
                            "state": "failed",
                            "message": {
                                "role": "agent",
                                "parts": [{"type": "text", "text": f"Task processing error: {str(e)}"}]
                            }
                        }
                    }
                }
            
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps(response).encode())
            
        except Exception as e:
            logger.error(f"Error handling task: {e}")
            self._serve_500(str(e))

    def _handle_tool_invoke(self):
        """Handle direct tool invocation"""
        try:
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            request_data = json.loads(post_data.decode('utf-8'))

            tool_name = request_data.get("tool_name")
            parameters = request_data.get("parameters", {})

            # Get the tool from task manager if it has tools
            if hasattr(self.task_manager, 'tools'):
                tools = self.task_manager.tools
                tool_func = None

                # Find the tool by name
                for tool in tools:
                    if hasattr(tool, '__name__') and tool.__name__ == tool_name:
                        tool_func = tool
                        break

                if tool_func:
                    # Call the tool directly
                    result = tool_func(**parameters)

                    response = {
                        "success": True,
                        "result": result,
                        "tool_name": tool_name
                    }
                else:
                    response = {
                        "success": False,
                        "error": f"Tool '{tool_name}' not found",
                        "available_tools": [getattr(tool, '__name__', str(tool)) for tool in tools]
                    }
            else:
                response = {
                    "success": False,
                    "error": "No tools available in task manager"
                }

            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps(response).encode())

        except Exception as e:
            logger.error(f"Error handling tool invoke: {e}")
            self._serve_500(str(e))

    def _serve_404(self):
        """Serve 404 Not Found"""
        self.send_response(404)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({"error": "Not Found"}).encode())
    
    def _serve_500(self, error_message: str):
        """Serve 500 Internal Server Error"""
        self.send_response(500)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps({"error": error_message}).encode())
    
    def log_message(self, format, *args):
        """Override to use our logger"""
        logger.info(f"{self.address_string()} - {format % args}")


class A2AServer:
    """A2A HTTP Server"""
    
    def __init__(self, agent_card: AgentCard, task_manager, host: str = "0.0.0.0", port: int = 8000):
        self.agent_card = agent_card
        self.task_manager = task_manager
        self.host = host
        self.port = port
        self.server = None
        self.server_thread = None
    
    def start(self):
        """Start the A2A server"""
        try:
            # Create a custom handler class with our dependencies
            def handler_factory(*args, **kwargs):
                return A2ARequestHandler(self.agent_card, self.task_manager, *args, **kwargs)
            
            self.server = HTTPServer((self.host, self.port), handler_factory)
            
            logger.info(f"Starting A2A server for agent: {self.agent_card.name}")
            logger.info(f"Server running on http://{self.host}:{self.port}")
            logger.info(f"Agent card available at http://{self.host}:{self.port}/agent-card")
            
            # Start server in a daemon thread
            self.server_thread = threading.Thread(target=self.server.serve_forever, daemon=True)
            self.server_thread.start()
            
            # Keep the main thread alive
            try:
                self.server_thread.join()
            except KeyboardInterrupt:
                logger.info("Shutting down server...")
                self.stop()
                
        except Exception as e:
            logger.error(f"Failed to start A2A server: {e}")
            raise
    
    def stop(self):
        """Stop the A2A server"""
        if self.server:
            self.server.shutdown()
            self.server.server_close()
            logger.info("A2A server stopped")
