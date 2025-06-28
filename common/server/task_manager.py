"""
A2A Task Manager Base Classes
"""

import asyncio
import logging
from typing import Dict, List, Optional
from abc import ABC, abstractmethod

from ..types import Task, TaskStatus, SendTaskRequest, SendTaskResponse, SendTaskStreamingRequest

logger = logging.getLogger(__name__)


class InMemoryTaskManager(ABC):
    """Base class for A2A task managers"""
    
    def __init__(self):
        self.tasks: Dict[str, Task] = {}
        self.lock = asyncio.Lock()
    
    @abstractmethod
    async def on_send_task(self, request: SendTaskRequest) -> SendTaskResponse:
        """Handle synchronous task requests"""
        pass
    
    @abstractmethod
    async def on_send_task_subscribe(self, request: SendTaskStreamingRequest):
        """Handle streaming task requests"""
        pass
    
    async def upsert_task(self, params):
        """Create or update a task"""
        async with self.lock:
            task = Task(
                id=params.id,
                status=TaskStatus(
                    state="pending",
                    message=params.message
                ),
                artifacts=[]
            )
            self.tasks[params.id] = task
            return task
    
    async def get_task(self, task_id: str) -> Optional[Task]:
        """Get a task by ID"""
        async with self.lock:
            return self.tasks.get(task_id)
    
    async def list_tasks(self) -> List[Task]:
        """List all tasks"""
        async with self.lock:
            return list(self.tasks.values())
