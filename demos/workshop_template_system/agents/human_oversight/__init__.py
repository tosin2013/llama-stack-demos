"""
Human Oversight Coordinator Agent

This agent orchestrates human approval workflows and maintains oversight of the 6-agent system.
Implements ADR-0002: Human-in-the-Loop Agent Integration.
"""

from .config import AGENT_CONFIG
from .tools import (
    audit_decision_tool,
    check_approval_status_tool,
    escalate_approval_tool,
    submit_for_approval_tool,
    submit_rag_update_for_approval_tool,
    submit_workshop_evolution_request_tool,
    wait_for_approval_tool,
)

__all__ = [
    "AGENT_CONFIG",
    "submit_for_approval_tool",
    "check_approval_status_tool",
    "wait_for_approval_tool",
    "escalate_approval_tool",
    "audit_decision_tool",
    "submit_rag_update_for_approval_tool",
    "submit_workshop_evolution_request_tool",
]
