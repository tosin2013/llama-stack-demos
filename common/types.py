"""
A2A Protocol Types
Simplified implementation based on Google A2A specification
"""

from dataclasses import dataclass
from enum import Enum
from typing import Any, Dict, List, Optional, Union


class TaskState(Enum):
    PENDING = "pending"
    WORKING = "working"
    COMPLETED = "completed"
    FAILED = "failed"


@dataclass
class TextPart:
    type: str = "text"
    text: str = ""


@dataclass
class FilePart:
    type: str = "file"
    file: "FileContent" = None


@dataclass
class FileContent:
    name: str
    content: bytes
    mime_type: str


@dataclass
class Message:
    role: str
    parts: List[Union[TextPart, FilePart]]


@dataclass
class Artifact:
    parts: List[Union[TextPart, FilePart]]


@dataclass
class TaskStatus:
    state: TaskState
    message: Message


@dataclass
class Task:
    id: str
    status: TaskStatus
    artifacts: Optional[List[Artifact]] = None


@dataclass
class TaskStatusUpdateEvent:
    id: str
    status: TaskStatus
    final: bool = False


@dataclass
class TaskArtifactUpdateEvent:
    id: str
    artifact: Artifact


@dataclass
class SendTaskRequest:
    id: str
    params: "SendTaskParams"


@dataclass
class SendTaskParams:
    id: str
    sessionId: str
    message: Message
    acceptedOutputModes: List[str]


@dataclass
class SendTaskResponse:
    id: str
    result: Task


@dataclass
class SendTaskStreamingRequest:
    id: str
    params: SendTaskParams


@dataclass
class SendTaskStreamingResponse:
    id: str
    result: Union[TaskStatusUpdateEvent, TaskArtifactUpdateEvent]


@dataclass
class JSONRPCResponse:
    id: str
    error: Optional[Dict[str, Any]] = None
    result: Optional[Any] = None


@dataclass
class AgentSkill:
    id: str
    name: str
    description: str
    tags: List[str]
    examples: List[str]
    inputModes: List[str]
    outputModes: List[str]


@dataclass
class AgentCapabilities:
    streaming: bool = False
    pushNotifications: bool = False
    stateTransitionHistory: bool = False


@dataclass
class AgentCard:
    name: str
    description: str
    url: str
    version: str
    defaultInputModes: List[str]
    defaultOutputModes: List[str]
    capabilities: AgentCapabilities
    skills: List[AgentSkill]
