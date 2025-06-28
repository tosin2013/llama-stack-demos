# A2A Agent Configuration Reference

**Document Type:** Reference  
**Framework:** Diátaxis  
**Date:** December 27, 2025  
**Author:** Sophia (Methodological Pragmatism AI Assistant)  

---

## Configuration Schema Overview

This reference documents the complete configuration schema for A2A agents in the Llama Stack ecosystem. All agents must follow this standardized structure for proper integration and deployment.

---

## AGENT_CONFIG Structure

### Root Configuration Object

```python
AGENT_CONFIG = {
    "agent_params": {...},           # Llama Stack agent configuration
    "task_manager_class": Class,     # A2A protocol adapter class
    "agent_card_params": {...},      # Capability advertisement
    "default_port": int              # Service endpoint port
}
```

---

## agent_params Configuration

### Required Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `model_env_var` | `str` | Environment variable for model ID | `"INFERENCE_MODEL_ID"` |
| `default_model` | `str` | Fallback model identifier | `"llama3.1:8b-instruct-fp16"` |
| `instructions` | `str` | Agent behavior instructions | `"You are a helpful assistant..."` |
| `tools` | `List[Callable]` | Available tool functions | `[tool_func1, tool_func2]` |
| `max_infer_iters` | `int` | Maximum inference iterations | `3` |
| `sampling_params` | `Dict` | Model sampling configuration | See [Sampling Parameters](#sampling-parameters) |

### Optional Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `tool_parser` | `ToolParser` | `None` | Custom tool parsing logic |
| `tool_config` | `ToolConfig` | `None` | Tool execution configuration |
| `input_shields` | `List[str]` | `None` | Input safety filters |
| `output_shields` | `List[str]` | `None` | Output safety filters |
| `response_format` | `ResponseFormat` | `None` | Structured response format |
| `enable_session_persistence` | `bool` | `None` | Session state persistence |

### Sampling Parameters

```python
"sampling_params": {
    "strategy": {
        "type": "greedy" | "top_p" | "top_k",
        "temperature": float,      # 0.0-2.0, default: 0.7
        "top_p": float,           # 0.0-1.0, for nucleus sampling
        "top_k": int,             # For top-k sampling
    },
    "max_tokens": int,            # Maximum response tokens
    "stop_tokens": List[str],     # Stop generation tokens
    "repetition_penalty": float,  # Repetition penalty factor
}
```

**Strategy Types:**
- `"greedy"`: Deterministic, highest probability tokens
- `"top_p"`: Nucleus sampling with probability threshold
- `"top_k"`: Sample from top-k highest probability tokens

---

## task_manager_class Configuration

### Standard Task Manager

```python
from demos.a2a_llama_stack.task_manager import AgentTaskManager

"task_manager_class": AgentTaskManager
```

### Custom Task Manager Requirements

Custom task managers must inherit from `InMemoryTaskManager` and implement:

```python
class CustomTaskManager(InMemoryTaskManager):
    def __init__(self, agent: Agent, **kwargs):
        super().__init__()
        self.agent = agent
    
    async def on_send_task(self, request: SendTaskRequest) -> SendTaskResponse:
        # Implementation required
        pass
    
    async def on_send_task_subscribe(self, request: SendTaskStreamingRequest):
        # Implementation required for streaming
        pass
```

---

## agent_card_params Configuration

### Required Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `name` | `str` | Agent display name | `"Workshop Chat Assistant"` |
| `description` | `str` | Agent purpose description | `"Provides contextual assistance..."` |
| `version` | `str` | Agent version identifier | `"0.1.0"` |
| `default_input_modes` | `List[str]` | Supported input formats | `["text/plain"]` |
| `default_output_modes` | `List[str]` | Supported output formats | `["text/plain", "application/json"]` |
| `capabilities_params` | `Dict` | Agent capabilities | See [Capabilities](#capabilities-parameters) |
| `skills_params` | `List[Dict]` | Agent skills definition | See [Skills](#skills-parameters) |

### Capabilities Parameters

```python
"capabilities_params": {
    "streaming": bool,                    # Supports streaming responses
    "pushNotifications": bool,            # Supports push notifications
    "stateTransitionHistory": bool,       # Maintains state history
    "multiModal": bool,                   # Supports multiple modalities
    "authentication": Dict,               # Authentication requirements
    "rateLimiting": Dict,                # Rate limiting configuration
}
```

### Skills Parameters

Each skill in `skills_params` must include:

```python
{
    "id": str,                           # Unique skill identifier
    "name": str,                         # Human-readable skill name
    "description": str,                  # Skill capability description
    "tags": List[str],                   # Categorization tags
    "examples": List[str],               # Usage examples
    "inputModes": List[str],             # Accepted input formats
    "outputModes": List[str],            # Produced output formats
    "parameters": Dict,                  # Optional: skill parameters
    "metadata": Dict,                    # Optional: additional metadata
}
```

---

## Content Type Constants

### Supported Input/Output Modes

```python
# Text formats
"text/plain"                    # Plain text
"text/markdown"                 # Markdown formatted text
"text/html"                     # HTML content

# Structured formats  
"application/json"              # JSON data
"application/xml"               # XML data
"application/yaml"              # YAML data

# Media formats
"image/jpeg"                    # JPEG images
"image/png"                     # PNG images
"audio/wav"                     # WAV audio
"video/mp4"                     # MP4 video

# Document formats
"application/pdf"               # PDF documents
"application/msword"            # Word documents
```

### SUPPORTED_CONTENT_TYPES

```python
from demos.a2a_llama_stack.task_manager import SUPPORTED_CONTENT_TYPES

# Default supported types for most agents
SUPPORTED_CONTENT_TYPES = [
    "text/plain",
    "application/json",
    "text/markdown"
]
```

---

## Port Allocation Standards

### Reserved Port Ranges

| Range | Purpose | Examples |
|-------|---------|----------|
| `10010-10019` | Core A2A agents | Planner (10010), Custom Tools (10011) |
| `10020-10029` | Extended agents | Composer (10012) |
| `10030-10039` | Development/Testing | User agents |
| `10040-10049` | Workshop agents | Chat (10040), Converter (10041) |
| `10050-10059` | Integration agents | Pipeline (10050), Source Manager (10051) |

### Port Configuration

```python
"default_port": 10040,           # Agent's default port
```

**Port Selection Guidelines:**
- Use sequential allocation within assigned ranges
- Document port assignments in agent documentation
- Avoid conflicts with system services (< 1024)
- Consider container port mapping for deployment

---

## Environment Variables

### Required Environment Variables

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `LLAMA_STACK_ENDPOINT` | Llama Stack server URL | `http://localhost:8321` | `http://llamastack:8321` |
| `INFERENCE_MODEL_ID` | Default inference model | `llama3.1:8b-instruct-fp16` | `llama3.2:3b-instruct-fp16` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EMBEDDING_MODEL_ID` | RAG embedding model | `all-MiniLM-L6-v2` |
| `MAX_CONCURRENT_REQUESTS` | Request concurrency limit | `10` |
| `LOG_LEVEL` | Logging verbosity | `INFO` |
| `AGENT_TIMEOUT` | Request timeout (seconds) | `30` |

---

## Tool Function Requirements

### Function Signature

```python
def tool_function(param1: type, param2: type = default) -> return_type:
    """
    Tool function docstring - used for agent instructions.
    
    Args:
        param1: Parameter description
        param2: Optional parameter description
    
    Returns:
        Description of return value
    """
    # Implementation
    return result
```

### Type Annotations

**Required for all parameters and return values:**
- Enables automatic parameter validation
- Supports tool discovery and documentation
- Ensures type safety in agent interactions

### Error Handling

```python
def robust_tool(input_data: str) -> str:
    """Tool with proper error handling."""
    try:
        # Tool logic
        result = process_data(input_data)
        return result
    except ValueError as e:
        return f"Input validation error: {str(e)}"
    except Exception as e:
        return f"Tool execution error: {str(e)}"
```

---

## Validation Rules

### Configuration Validation

1. **Required Fields**: All mandatory parameters must be present
2. **Type Checking**: Parameters must match expected types
3. **Port Uniqueness**: No port conflicts within deployment
4. **Tool Imports**: All tools must be importable
5. **Model Availability**: Specified models must exist

### Runtime Validation

1. **Request Compatibility**: Input/output mode matching
2. **Parameter Validation**: Tool parameter type checking
3. **Resource Limits**: Memory and computation constraints
4. **Authentication**: Access control verification

---

## Example Configurations

### Minimal Agent Configuration

```python
AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": "You are a helpful assistant.",
        "tools": [],
        "max_infer_iters": 3,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 1024,
        },
    },
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Basic Agent",
        "description": "Simple assistant agent",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": ["text/plain"],
        "capabilities_params": {
            "streaming": False,
            "pushNotifications": False,
            "stateTransitionHistory": False,
        },
        "skills_params": []
    },
    "default_port": 10030,
}
```

### Full-Featured Agent Configuration

```python
AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": "Detailed agent instructions...",
        "tools": [tool1, tool2, tool3],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "top_p", "top_p": 0.9, "temperature": 0.7},
            "max_tokens": 2048,
            "repetition_penalty": 1.1,
        },
        "input_shields": ["safety_filter"],
        "output_shields": ["content_filter"],
    },
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Advanced Agent",
        "description": "Full-featured agent with multiple capabilities",
        "version": "1.0.0",
        "default_input_modes": ["text/plain", "application/json"],
        "default_output_modes": ["text/plain", "application/json", "text/markdown"],
        "capabilities_params": {
            "streaming": True,
            "pushNotifications": True,
            "stateTransitionHistory": True,
            "multiModal": True,
        },
        "skills_params": [
            {
                "id": "skill1",
                "name": "Primary Skill",
                "description": "Main agent capability",
                "tags": ["primary", "core"],
                "examples": ["Example usage 1", "Example usage 2"],
                "inputModes": ["text/plain"],
                "outputModes": ["application/json"],
            }
        ]
    },
    "default_port": 10040,
}
```

---

## Related Documentation

- **Tutorial**: [Getting Started with A2A Multi-Agent Systems](./a2a_tutorial_getting_started.md)
- **How-to**: [Building Workshop Template System Agents](./a2a_howto_workshop_agents.md)
- **Explanation**: [A2A Architecture Analysis](./a2a_architecture_analysis.md)
