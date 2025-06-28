demos# Getting Started with A2A Multi-Agent Systems

**Document Type:** Tutorial  
**Framework:** Diátaxis  
**Date:** December 27, 2025  
**Author:** Sophia (Methodological Pragmatism AI Assistant)  

---

## Learning Objectives

By the end of this tutorial, you will:

1. **Understand** the A2A agent architecture and communication patterns
2. **Create** your first A2A agent with custom tools
3. **Deploy** agents using the established configuration patterns
4. **Coordinate** multiple agents using A2AFleet
5. **Test** agent communication and verify functionality

**Prerequisites:**
- Python 3.8+ development environment
- Access to Llama Stack server (local or remote)
- Basic understanding of REST APIs and async programming
- Familiarity with containerization concepts

**Estimated Time:** 45 minutes

---

## Step 1: Environment Setup

### 1.1 Verify Llama Stack Access

First, ensure you have access to a running Llama Stack server:

```bash
# Check if Llama Stack is accessible
curl -X GET "http://localhost:8321/health" 

# Expected response: {"status": "ok"}
```

### 1.2 Install Dependencies

Navigate to the project directory and install requirements:

```bash
cd /home/ec2-user/llama-stack-demos/demos/a2a_llama_stack
pip install -r requirements.txt
```

### 1.3 Set Environment Variables

Create a `.env` file with your configuration:

```bash
# .env file
LLAMA_STACK_ENDPOINT=http://localhost:8321
INFERENCE_MODEL_ID=llama3.1:8b-instruct-fp16
```

**Verification Check:** Run `python -c "import llama_stack_client; print('✓ Dependencies installed')"` to confirm setup.

---

## Step 2: Create Your First A2A Agent

### 2.1 Create Agent Directory Structure

```bash
mkdir -p agents/my_first_agent
touch agents/my_first_agent/__init__.py
touch agents/my_first_agent/config.py
touch agents/my_first_agent/tools.py
```

### 2.2 Implement Custom Tools

Create `agents/my_first_agent/tools.py`:

```python
import time
from datetime import datetime

def greeting_tool(name: str = "World") -> str:
    """
    Generate a personalized greeting message.
    
    Args:
        name: The name to greet (default: "World")
    
    Returns:
        A friendly greeting message
    """
    current_time = datetime.now().strftime("%H:%M")
    return f"Hello {name}! The current time is {current_time}."

def calculation_tool(operation: str, a: float, b: float) -> str:
    """
    Perform basic mathematical operations.
    
    Args:
        operation: The operation to perform (add, subtract, multiply, divide)
        a: First number
        b: Second number
    
    Returns:
        The result of the calculation
    """
    operations = {
        "add": lambda x, y: x + y,
        "subtract": lambda x, y: x - y,
        "multiply": lambda x, y: x * y,
        "divide": lambda x, y: x / y if y != 0 else "Error: Division by zero"
    }
    
    if operation not in operations:
        return f"Error: Unknown operation '{operation}'"
    
    result = operations[operation](a, b)
    return f"Result: {a} {operation} {b} = {result}"
```

### 2.3 Configure Agent Settings

Create `agents/my_first_agent/config.py`:

```python
from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import greeting_tool, calculation_tool

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": (
            "You are a helpful assistant with access to greeting and calculation tools.\n"
            "Use the greeting_tool to welcome users and the calculation_tool for math operations.\n"
            "Always be friendly and provide clear explanations of your actions."
        ),
        "tools": [greeting_tool, calculation_tool],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "My First Agent",
        "description": "A friendly assistant that can greet users and perform calculations",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": False,
            "pushNotifications": False,
            "stateTransitionHistory": False,
        },
        "skills_params": [
            {
                "id": "greeting_tool",
                "name": "Personal Greeter",
                "description": "Generates personalized greeting messages with current time",
                "tags": ["greeting", "social"],
                "examples": ["Say hello to Alice", "Greet me"],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "calculation_tool", 
                "name": "Basic Calculator",
                "description": "Performs basic mathematical operations (add, subtract, multiply, divide)",
                "tags": ["math", "calculation"],
                "examples": ["Calculate 15 + 27", "What is 100 divided by 4?"],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10030,
}
```

**Key Configuration Elements:**
- **agent_params**: Llama Stack agent configuration
- **task_manager_class**: A2A protocol adapter
- **agent_card_params**: Capability advertisement
- **default_port**: Service endpoint configuration

---

## Step 3: Deploy Your Agent

### 3.1 Start the Agent Server

```bash
# From the a2a_llama_stack directory
python -m demos.a2a_llama_stack --agent-name my_first_agent --port 10030
```

**Expected Output:**
```
INFO:root:Starting A2A server for agent: My First Agent
INFO:root:Server running on http://localhost:10030
INFO:root:Agent card available at http://localhost:10030/agent-card
```

### 3.2 Verify Agent Deployment

Test the agent card endpoint:

```bash
curl -X GET "http://localhost:10030/agent-card" | jq .
```

**Expected Response Structure:**
```json
{
  "name": "My First Agent",
  "description": "A friendly assistant that can greet users and perform calculations",
  "url": "http://localhost:10030",
  "version": "0.1.0",
  "skills": [
    {
      "id": "greeting_tool",
      "name": "Personal Greeter",
      "description": "Generates personalized greeting messages with current time"
    }
  ]
}
```

---

## Step 4: Test Agent Communication

### 4.1 Direct Agent Testing

Create a test script `test_my_agent.py`:

```python
import asyncio
from common.client import A2AClient, A2ACardResolver
from uuid import uuid4

async def test_agent():
    # Discover agent capabilities
    agent_url = "http://localhost:10030"
    card_resolver = A2ACardResolver(agent_url)
    agent_card = card_resolver.get_agent_card()
    
    print(f"Connected to: {agent_card.name}")
    print(f"Description: {agent_card.description}")
    
    # Create client
    client = A2AClient(agent_card=agent_card)
    
    # Test greeting functionality
    greeting_payload = {
        "id": uuid4().hex,
        "acceptedOutputModes": ["text/plain"],
        "message": {
            "role": "user", 
            "parts": [{"type": "text", "text": "Please greet me as Sophia"}]
        }
    }
    
    response = await client.send_task(greeting_payload)
    print(f"Greeting Response: {response.result.status.message.parts[0].text}")
    
    # Test calculation functionality
    calc_payload = {
        "id": uuid4().hex,
        "acceptedOutputModes": ["text/plain"],
        "message": {
            "role": "user",
            "parts": [{"type": "text", "text": "Calculate 42 multiply 13"}]
        }
    }
    
    response = await client.send_task(calc_payload)
    print(f"Calculation Response: {response.result.status.message.parts[0].text}")

# Run the test
asyncio.run(test_agent())
```

### 4.2 Execute the Test

```bash
python test_my_agent.py
```

**Expected Output:**
```
Connected to: My First Agent
Description: A friendly assistant that can greet users and perform calculations
Greeting Response: Hello Sophia! The current time is 14:23.
Calculation Response: I'll help you calculate 42 multiply 13. Result: 42 multiply 13 = 546
```

---

## Step 5: Multi-Agent Coordination

### 5.1 Create a Fleet Configuration

Create `fleet_example.py`:

```python
from A2AFleet import A2AFleet, AgentSpecification, LLSAgentConfiguration
from common.types import AgentCard, AgentCapabilities, AgentSkill

# Define agent specifications
my_agent_spec = AgentSpecification(
    url="http://localhost:10030",
    managed=False  # External agent (already running)
)

# Create fleet
fleet = A2AFleet(
    llama_stack_url="http://localhost:8321",
    agent_specs=[my_agent_spec]
)

# Initialize fleet (discovers external agents)
fleet.run_fleet()

print("✓ Fleet initialized successfully")
print(f"Available agents: {list(fleet.agents.keys())}")
```

### 5.2 Test Fleet Communication

```bash
python fleet_example.py
```

**Verification Check:** You should see your agent listed in the fleet's available agents.

---

## Step 6: Assessment and Next Steps

### 6.1 Verify Learning Objectives

**Self-Assessment Questions:**
1. Can you explain the role of AgentCard in the A2A protocol?
2. What is the purpose of the task_manager_class in agent configuration?
3. How do tools get registered and exposed through the A2A interface?
4. What are the key differences between managed and external agents in A2AFleet?

### 6.2 Troubleshooting Common Issues

**Agent Won't Start:**
- Check port availability: `netstat -an | grep 10030`
- Verify Llama Stack connectivity
- Review configuration syntax

**Tool Not Working:**
- Confirm tool import in config.py
- Check function signatures match expected parameters
- Verify instructions mention the tool

**Communication Failures:**
- Test agent card endpoint accessibility
- Verify A2A protocol message format
- Check async/await usage in client code

---

## Recap

You have successfully:

✅ **Created** a custom A2A agent with greeting and calculation tools  
✅ **Configured** agent parameters following established patterns  
✅ **Deployed** the agent as an independent service  
✅ **Tested** direct agent communication using A2A protocol  
✅ **Integrated** the agent into a fleet management system  

### Next Steps

1. **Explore Advanced Patterns**: Study the existing agents (planner, composer, custom_tools)
2. **Add More Tools**: Implement file processing, web search, or database tools
3. **Build Workflows**: Create multi-agent coordination patterns
4. **Deploy to OpenShift**: Use Kubernetes manifests for production deployment

### Related Resources

- **How-to Guide**: [Creating Custom Agent Tools](./a2a_howto_custom_tools.md)
- **Reference**: [A2A Configuration Schema](./a2a_reference_config.md)
- **Explanation**: [A2A Architecture Analysis](./a2a_architecture_analysis.md)
