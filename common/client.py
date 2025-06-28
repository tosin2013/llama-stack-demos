"""
A2A Client Implementation
Simplified client for A2A protocol communication
"""

import json
import logging
import httpx
from typing import Dict, Any, Optional

from .types import AgentCard, AgentSkill, AgentCapabilities

logger = logging.getLogger(__name__)


class A2ACardResolver:
    """Resolves agent cards from A2A endpoints"""
    
    def __init__(self, agent_url: str):
        self.agent_url = agent_url.rstrip('/')
        
    def get_agent_card(self) -> AgentCard:
        """Fetch agent card from the agent endpoint"""
        try:
            with httpx.Client() as client:
                response = client.get(f"{self.agent_url}/agent-card")
                response.raise_for_status()
                card_data = response.json()
                
                # Convert JSON to AgentCard dataclass
                skills = [
                    AgentSkill(
                        id=skill["id"],
                        name=skill["name"],
                        description=skill["description"],
                        tags=skill.get("tags", []),
                        examples=skill.get("examples", []),
                        inputModes=skill.get("inputModes", ["text/plain"]),
                        outputModes=skill.get("outputModes", ["text/plain"])
                    )
                    for skill in card_data.get("skills", [])
                ]
                
                capabilities = AgentCapabilities(
                    streaming=card_data.get("capabilities", {}).get("streaming", False),
                    pushNotifications=card_data.get("capabilities", {}).get("pushNotifications", False),
                    stateTransitionHistory=card_data.get("capabilities", {}).get("stateTransitionHistory", False)
                )
                
                return AgentCard(
                    name=card_data["name"],
                    description=card_data["description"],
                    url=card_data["url"],
                    version=card_data.get("version", "0.1.0"),
                    defaultInputModes=card_data.get("defaultInputModes", ["text/plain"]),
                    defaultOutputModes=card_data.get("defaultOutputModes", ["text/plain"]),
                    capabilities=capabilities,
                    skills=skills
                )
                
        except Exception as e:
            logger.error(f"Failed to resolve agent card from {self.agent_url}: {e}")
            raise


class A2AClient:
    """Client for communicating with A2A agents"""
    
    def __init__(self, agent_card: AgentCard):
        self.agent_card = agent_card
        self.base_url = agent_card.url.rstrip('/')
        
    async def send_task(self, task_payload: Dict[str, Any]) -> Dict[str, Any]:
        """Send a task to the agent"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{self.base_url}/send-task",
                    json=task_payload,
                    headers={"Content-Type": "application/json"}
                )
                response.raise_for_status()
                return response.json()
                
        except Exception as e:
            logger.error(f"Failed to send task to {self.base_url}: {e}")
            raise
    
    def send_task_sync(self, task_payload: Dict[str, Any]) -> Dict[str, Any]:
        """Send a task to the agent (synchronous version)"""
        try:
            with httpx.Client() as client:
                response = client.post(
                    f"{self.base_url}/send-task",
                    json=task_payload,
                    headers={"Content-Type": "application/json"}
                )
                response.raise_for_status()
                return response.json()
                
        except Exception as e:
            logger.error(f"Failed to send task to {self.base_url}: {e}")
            raise
