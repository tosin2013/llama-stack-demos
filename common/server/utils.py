"""
A2A Server Utilities
"""

from typing import List

from ..types import JSONRPCResponse


def are_modalities_compatible(
    accepted_modes: List[str], supported_modes: List[str]
) -> bool:
    """Check if accepted output modes are compatible with supported modes"""
    if not accepted_modes:
        return True

    return any(mode in supported_modes for mode in accepted_modes)


def new_incompatible_types_error(request_id: str) -> JSONRPCResponse:
    """Create an incompatible types error response"""
    return JSONRPCResponse(
        id=request_id,
        error={
            "code": -32602,
            "message": "Incompatible output modes",
            "data": "The requested output modes are not supported by this agent",
        },
    )
