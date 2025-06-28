# How to Build Workshop Template System Agents

**Document Type:** How-to Guide  
**Framework:** Diátaxis  
**Date:** December 27, 2025  
**Author:** Sophia (Methodological Pragmatism AI Assistant)  

---

## Problem Statement

You need to create specialized agents for the Intelligent Workshop Template System that can convert GitHub repositories into interactive workshop content, provide chat assistance, and manage documentation pipelines.

**Prerequisites:**
- Completed [Getting Started with A2A Multi-Agent Systems](./a2a_tutorial_getting_started.md)
- Access to GitHub repositories for testing
- Understanding of workshop template requirements from `research.md`

---

## Solution Overview

This guide walks through creating four specialized agents:

1. **Workshop Chat Agent** - RAG-based participant assistance
2. **Template Converter Agent** - GitHub to workshop conversion  
3. **Documentation Pipeline Agent** - Automated content updates
4. **Source Manager Agent** - Trusted source validation

---

## How to Create the Workshop Chat Agent

### Step 1: Set Up RAG Infrastructure

Create the agent directory:

```bash
mkdir -p demos/workshop_template_system/agents/workshop_chat
touch demos/workshop_template_system/agents/workshop_chat/__init__.py
```

### Step 2: Implement RAG Tools

Create `demos/workshop_template_system/agents/workshop_chat/tools.py`:

```python
import os
from typing import List, Dict, Any
from llama_stack_client import LlamaStackClient, RAGDocument
from llama_stack_client.types import RAGQueryRequest

class WorkshopRAGTool:
    """RAG tool for workshop content retrieval and assistance."""
    
    def __init__(self):
        self.client = LlamaStackClient(
            base_url=os.getenv("LLAMA_STACK_ENDPOINT", "http://localhost:8321")
        )
        self.knowledge_bank_id = "workshop_content_kb"
        
    def setup_knowledge_bank(self, workshop_content: List[str]) -> bool:
        """Initialize knowledge bank with workshop content."""
        try:
            # Create knowledge bank if it doesn't exist
            self.client.knowledge_banks.create(
                knowledge_bank_id=self.knowledge_bank_id,
                embedding_model="all-MiniLM-L6-v2",
                chunk_size_in_tokens=512,
                overlap_size_in_tokens=64,
            )
            
            # Add workshop documents
            documents = []
            for i, content in enumerate(workshop_content):
                documents.append(RAGDocument(
                    document_id=f"workshop_doc_{i}",
                    content=content,
                    mime_type="text/plain",
                    metadata={"source": "workshop_content", "section": i}
                ))
            
            self.client.knowledge_banks.insert_documents(
                knowledge_bank_id=self.knowledge_bank_id,
                documents=documents
            )
            return True
            
        except Exception as e:
            print(f"Error setting up knowledge bank: {e}")
            return False

def workshop_query_tool(query: str, context_limit: int = 5) -> str:
    """
    Query workshop content using RAG for contextual assistance.
    
    Args:
        query: User question about workshop content
        context_limit: Maximum number of context chunks to retrieve
    
    Returns:
        Contextually relevant response based on workshop content
    """
    rag_tool = WorkshopRAGTool()
    
    try:
        # Perform RAG query
        response = rag_tool.client.rag.query(
            knowledge_bank_ids=[rag_tool.knowledge_bank_id],
            query=query,
            num_chunks=context_limit
        )
        
        # Extract relevant context
        context_chunks = []
        for chunk in response.chunks:
            context_chunks.append(chunk.content)
        
        # Generate response with context
        context_text = "\n\n".join(context_chunks)
        prompt = f"""Based on the following workshop content, answer the user's question:

Context:
{context_text}

Question: {query}

Provide a helpful, accurate response based on the workshop content. If the question cannot be answered from the provided context, say so clearly."""

        # Use inference to generate response
        inference_response = rag_tool.client.inference.chat_completion(
            model_id=os.getenv("INFERENCE_MODEL_ID", "llama3.1:8b-instruct-fp16"),
            messages=[{"role": "user", "content": prompt}],
            sampling_params={"max_tokens": 1024, "temperature": 0.1}
        )
        
        return inference_response.completion_message.content
        
    except Exception as e:
        return f"Error processing workshop query: {str(e)}"

def workshop_navigation_tool(section: str) -> str:
    """
    Provide navigation assistance for workshop sections.
    
    Args:
        section: Workshop section name or topic
    
    Returns:
        Navigation guidance and section overview
    """
    # This would integrate with workshop structure metadata
    navigation_map = {
        "introduction": "Start here for workshop overview and prerequisites",
        "setup": "Environment setup and tool installation instructions", 
        "exercises": "Hands-on practice activities and labs",
        "troubleshooting": "Common issues and solutions",
        "resources": "Additional learning materials and references"
    }
    
    section_lower = section.lower()
    if section_lower in navigation_map:
        return f"📍 {section.title()} Section: {navigation_map[section_lower]}"
    else:
        available_sections = ", ".join(navigation_map.keys())
        return f"Section '{section}' not found. Available sections: {available_sections}"
```

### Step 3: Configure the Chat Agent

Create `demos/workshop_template_system/agents/workshop_chat/config.py`:

```python
from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import workshop_query_tool, workshop_navigation_tool

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": (
            "You are a helpful workshop assistant with access to workshop content through RAG.\n"
            "Use workshop_query_tool to answer questions about workshop content.\n"
            "Use workshop_navigation_tool to help users navigate workshop sections.\n"
            "Always provide clear, helpful responses and guide users to relevant sections.\n"
            "If you cannot answer from the workshop content, suggest where users might find help."
        ),
        "tools": [workshop_query_tool, workshop_navigation_tool],
        "max_infer_iters": 5,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 2048,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Workshop Chat Assistant",
        "description": "Provides contextual assistance and navigation for workshop participants",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": True,
            "pushNotifications": False,
            "stateTransitionHistory": True,
        },
        "skills_params": [
            {
                "id": "workshop_query_tool",
                "name": "Workshop Content Query",
                "description": "Answer questions using workshop content via RAG",
                "tags": ["rag", "assistance", "content"],
                "examples": [
                    "How do I set up the development environment?",
                    "What are the prerequisites for this workshop?",
                    "Explain the concept covered in section 3"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            },
            {
                "id": "workshop_navigation_tool",
                "name": "Workshop Navigation",
                "description": "Provide guidance on workshop structure and navigation",
                "tags": ["navigation", "structure", "guidance"],
                "examples": [
                    "Where should I start?",
                    "Show me the troubleshooting section",
                    "What's in the exercises section?"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["text/plain"],
            }
        ]
    },
    "default_port": 10040,
}
```

---

## How to Create the Template Converter Agent

### Step 1: Implement GitHub Analysis Tools

Create `demos/workshop_template_system/agents/template_converter/tools.py`:

```python
import os
import json
import requests
from typing import Dict, List, Any
from pathlib import Path

def analyze_github_repository(repo_url: str) -> str:
    """
    Analyze a GitHub repository structure and content for workshop conversion.
    
    Args:
        repo_url: GitHub repository URL (e.g., https://github.com/user/repo)
    
    Returns:
        JSON string with repository analysis results
    """
    try:
        # Extract owner and repo from URL
        parts = repo_url.rstrip('/').split('/')
        owner, repo = parts[-2], parts[-1]
        
        # GitHub API calls (would need authentication for private repos)
        api_base = f"https://api.github.com/repos/{owner}/{repo}"
        
        # Get repository metadata
        repo_response = requests.get(api_base)
        repo_data = repo_response.json()
        
        # Get repository contents
        contents_response = requests.get(f"{api_base}/contents")
        contents_data = contents_response.json()
        
        # Analyze structure
        analysis = {
            "repository": {
                "name": repo_data.get("name"),
                "description": repo_data.get("description"),
                "language": repo_data.get("language"),
                "topics": repo_data.get("topics", []),
                "readme_url": f"{api_base}/readme"
            },
            "structure": {
                "files": [],
                "directories": [],
                "key_files": []
            },
            "workshop_potential": {
                "has_readme": False,
                "has_docs": False,
                "has_examples": False,
                "has_tests": False,
                "complexity_score": 0
            }
        }
        
        # Analyze contents
        for item in contents_data:
            if item["type"] == "file":
                analysis["structure"]["files"].append(item["name"])
                if item["name"].lower() in ["readme.md", "readme.txt"]:
                    analysis["workshop_potential"]["has_readme"] = True
                    analysis["structure"]["key_files"].append(item["name"])
            elif item["type"] == "dir":
                analysis["structure"]["directories"].append(item["name"])
                if item["name"].lower() in ["docs", "documentation", "examples", "tests"]:
                    if "docs" in item["name"].lower():
                        analysis["workshop_potential"]["has_docs"] = True
                    elif "example" in item["name"].lower():
                        analysis["workshop_potential"]["has_examples"] = True
                    elif "test" in item["name"].lower():
                        analysis["workshop_potential"]["has_tests"] = True
        
        # Calculate complexity score
        score = 0
        score += 2 if analysis["workshop_potential"]["has_readme"] else 0
        score += 3 if analysis["workshop_potential"]["has_docs"] else 0
        score += 2 if analysis["workshop_potential"]["has_examples"] else 0
        score += 1 if analysis["workshop_potential"]["has_tests"] else 0
        score += len(analysis["structure"]["files"]) // 5  # File count factor
        
        analysis["workshop_potential"]["complexity_score"] = min(score, 10)
        
        return json.dumps(analysis, indent=2)
        
    except Exception as e:
        return json.dumps({"error": f"Failed to analyze repository: {str(e)}"})

def generate_workshop_structure(repo_analysis: str, workshop_type: str = "tutorial") -> str:
    """
    Generate workshop structure based on repository analysis.
    
    Args:
        repo_analysis: JSON string from analyze_github_repository
        workshop_type: Type of workshop (tutorial, lab, demo)
    
    Returns:
        JSON string with proposed workshop structure
    """
    try:
        analysis = json.loads(repo_analysis)
        
        if "error" in analysis:
            return repo_analysis  # Pass through error
        
        # Generate workshop structure based on analysis
        workshop_structure = {
            "metadata": {
                "title": f"{analysis['repository']['name']} Workshop",
                "description": analysis['repository']['description'] or "Interactive workshop",
                "type": workshop_type,
                "estimated_duration": "60-90 minutes",
                "difficulty": "intermediate" if analysis['workshop_potential']['complexity_score'] > 5 else "beginner"
            },
            "sections": [],
            "resources": {
                "source_repository": analysis['repository']['name'],
                "key_files": analysis['structure']['key_files'],
                "additional_docs": []
            }
        }
        
        # Standard workshop sections
        sections = [
            {
                "id": "introduction",
                "title": "Introduction",
                "content_type": "explanation",
                "estimated_time": "10 minutes",
                "objectives": [
                    f"Understand the purpose of {analysis['repository']['name']}",
                    "Review workshop objectives and prerequisites"
                ]
            },
            {
                "id": "setup",
                "title": "Environment Setup", 
                "content_type": "tutorial",
                "estimated_time": "15 minutes",
                "objectives": [
                    "Set up development environment",
                    "Install required dependencies",
                    "Verify setup completion"
                ]
            }
        ]
        
        # Add sections based on repository structure
        if analysis['workshop_potential']['has_examples']:
            sections.append({
                "id": "examples",
                "title": "Hands-on Examples",
                "content_type": "tutorial", 
                "estimated_time": "30 minutes",
                "objectives": [
                    "Work through practical examples",
                    "Understand key concepts through practice"
                ]
            })
        
        if analysis['workshop_potential']['has_tests']:
            sections.append({
                "id": "testing",
                "title": "Testing and Validation",
                "content_type": "how-to",
                "estimated_time": "15 minutes", 
                "objectives": [
                    "Run test suites",
                    "Validate implementation"
                ]
            })
        
        # Always include conclusion
        sections.append({
            "id": "conclusion",
            "title": "Conclusion and Next Steps",
            "content_type": "explanation",
            "estimated_time": "10 minutes",
            "objectives": [
                "Review key learnings",
                "Explore additional resources",
                "Plan next steps"
            ]
        })
        
        workshop_structure["sections"] = sections
        
        return json.dumps(workshop_structure, indent=2)
        
    except Exception as e:
        return json.dumps({"error": f"Failed to generate workshop structure: {str(e)}"})
```

### Step 2: Configure the Template Converter Agent

Create `demos/workshop_template_system/agents/template_converter/config.py`:

```python
from ...task_manager import AgentTaskManager, SUPPORTED_CONTENT_TYPES
from .tools import analyze_github_repository, generate_workshop_structure

AGENT_CONFIG = {
    "agent_params": {
        "model_env_var": "INFERENCE_MODEL_ID",
        "default_model": "llama3.1:8b-instruct-fp16",
        "instructions": (
            "You are a workshop template converter that analyzes GitHub repositories "
            "and generates structured workshop content.\n"
            "Use analyze_github_repository to examine repository structure and content.\n"
            "Use generate_workshop_structure to create workshop outlines.\n"
            "Always provide detailed analysis and clear workshop structures that follow "
            "educational best practices and the rhpds/showroom_template_default format."
        ),
        "tools": [analyze_github_repository, generate_workshop_structure],
        "max_infer_iters": 7,
        "sampling_params": {
            "strategy": {"type": "greedy"},
            "max_tokens": 4096,
        },
    },
    
    "task_manager_class": AgentTaskManager,
    "agent_card_params": {
        "name": "Template Converter Agent",
        "description": "Converts GitHub repositories into structured workshop templates",
        "version": "0.1.0",
        "default_input_modes": ["text/plain"],
        "default_output_modes": SUPPORTED_CONTENT_TYPES,
        "capabilities_params": {
            "streaming": False,
            "pushNotifications": False,
            "stateTransitionHistory": True,
        },
        "skills_params": [
            {
                "id": "analyze_github_repository",
                "name": "Repository Analyzer",
                "description": "Analyze GitHub repository structure and content for workshop potential",
                "tags": ["github", "analysis", "repository"],
                "examples": [
                    "Analyze https://github.com/user/awesome-project",
                    "What's the workshop potential of this repository?",
                    "Examine the structure of github.com/org/project"
                ],
                "inputModes": ["text/plain"],
                "outputModes": ["application/json"],
            },
            {
                "id": "generate_workshop_structure",
                "name": "Workshop Structure Generator",
                "description": "Generate workshop structure based on repository analysis",
                "tags": ["workshop", "structure", "template"],
                "examples": [
                    "Create a tutorial workshop structure",
                    "Generate lab structure for this repository",
                    "Design a workshop outline"
                ],
                "inputModes": ["application/json"],
                "outputModes": ["application/json"],
            }
        ]
    },
    "default_port": 10041,
}
```

---

## Testing Your Workshop Agents

### Step 1: Deploy the Agents

```bash
# Terminal 1: Workshop Chat Agent
python -m demos.workshop_template_system --agent-name workshop_chat --port 10040

# Terminal 2: Template Converter Agent  
python -m demos.workshop_template_system --agent-name template_converter --port 10041
```

### Step 2: Test Workshop Chat Agent

```python
# test_workshop_chat.py
import asyncio
from common.client import A2AClient, A2ACardResolver

async def test_workshop_chat():
    agent_url = "http://localhost:10040"
    card_resolver = A2ACardResolver(agent_url)
    agent_card = card_resolver.get_agent_card()
    client = A2AClient(agent_card=agent_card)
    
    # Test workshop query
    payload = {
        "id": "test_query",
        "acceptedOutputModes": ["text/plain"],
        "message": {
            "role": "user",
            "parts": [{"type": "text", "text": "How do I navigate to the setup section?"}]
        }
    }
    
    response = await client.send_task(payload)
    print(f"Chat Response: {response.result.status.message.parts[0].text}")

asyncio.run(test_workshop_chat())
```

### Step 3: Test Template Converter Agent

```python
# test_template_converter.py
import asyncio
from common.client import A2AClient, A2ACardResolver

async def test_template_converter():
    agent_url = "http://localhost:10041"
    card_resolver = A2ACardResolver(agent_url)
    agent_card = card_resolver.get_agent_card()
    client = A2AClient(agent_card=agent_card)
    
    # Test repository analysis
    payload = {
        "id": "test_analysis",
        "acceptedOutputModes": ["application/json"],
        "message": {
            "role": "user",
            "parts": [{"type": "text", "text": "Analyze https://github.com/kubernetes/examples"}]
        }
    }
    
    response = await client.send_task(payload)
    print(f"Analysis Response: {response.result.status.message.parts[0].text}")

asyncio.run(test_template_converter())
```

---

## Troubleshooting Common Issues

### Agent Won't Start
- **Check port conflicts**: `netstat -an | grep 10040`
- **Verify imports**: Ensure all tool imports are correct
- **Check Llama Stack**: Confirm base service is running

### RAG Tool Errors
- **Knowledge bank creation**: Verify embedding model availability
- **Document indexing**: Check content format and size limits
- **Query failures**: Validate knowledge bank ID and query format

### GitHub API Limits
- **Rate limiting**: Implement request throttling
- **Authentication**: Add GitHub token for higher limits
- **Error handling**: Graceful degradation for API failures

---

## Next Steps

1. **Implement remaining agents**: Documentation Pipeline and Source Manager
2. **Create A2AFleet configuration**: Coordinate all workshop agents
3. **Add OpenShift deployment**: Kubernetes manifests for production
4. **Integrate with existing MCP servers**: Leverage GitHub MCP for enhanced functionality

### Related Documentation

- **Tutorial**: [Getting Started with A2A Multi-Agent Systems](./a2a_tutorial_getting_started.md)
- **Reference**: [Workshop Agent Configuration Schema](./a2a_reference_workshop_config.md)
- **Explanation**: [Workshop Template System Architecture](./workshop_architecture_explanation.md)
