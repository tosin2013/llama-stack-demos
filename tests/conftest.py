"""
Pytest configuration and fixtures for Workshop Template System testing.
"""
import pytest
import requests
import time
import subprocess
import os
from typing import Dict, List
import json

# Agent configuration
AGENTS = {
    "workshop-chat": {"port": 8080, "container_name": "workshop-chat"},
    "template-converter": {"port": 8081, "container_name": "template-converter"},
    "content-creator": {"port": 8082, "container_name": "content-creator"},
    "source-manager": {"port": 8083, "container_name": "source-manager"},
    "research-validation": {"port": 8084, "container_name": "research-validation"},
    "documentation-pipeline": {"port": 8085, "container_name": "documentation-pipeline"}
}

TEST_REPOSITORIES = [
    {
        "name": "llama-stack-demos",
        "url": "https://github.com/tosin2013/llama-stack-demos.git",
        "type": "ai-demos",
        "expected_components": ["demos", "README.md", "requirements.txt"]
    },
    {
        "name": "simple-python-app",
        "url": "https://github.com/python/cpython.git",
        "type": "python-project", 
        "expected_components": ["Python", "Lib", "README.rst"]
    }
]

@pytest.fixture(scope="session")
def agent_endpoints():
    """Provide agent endpoint URLs for testing."""
    return {
        name: f"http://localhost:{config['port']}"
        for name, config in AGENTS.items()
    }

@pytest.fixture(scope="session")
def test_repositories():
    """Provide test repository configurations."""
    return TEST_REPOSITORIES

@pytest.fixture(scope="session")
def ollama_endpoint():
    """Provide ollama endpoint for LLM integration testing."""
    return "http://localhost:11434"

@pytest.fixture(scope="session", autouse=True)
def ensure_agents_running(agent_endpoints):
    """Ensure all agents are running before tests start."""
    print("\n🔍 Checking if all agents are running...")
    
    max_retries = 30
    retry_delay = 2
    
    for agent_name, endpoint in agent_endpoints.items():
        for attempt in range(max_retries):
            try:
                response = requests.get(f"{endpoint}/agent-card", timeout=5)
                if response.status_code == 200:
                    print(f"✅ {agent_name} is running at {endpoint}")
                    break
            except requests.exceptions.RequestException:
                if attempt < max_retries - 1:
                    print(f"⏳ Waiting for {agent_name} to start... (attempt {attempt + 1}/{max_retries})")
                    time.sleep(retry_delay)
                else:
                    pytest.fail(f"❌ {agent_name} is not responding at {endpoint}")

@pytest.fixture(scope="session")
def ollama_available(ollama_endpoint):
    """Check if ollama is available for LLM testing."""
    try:
        response = requests.get(f"{ollama_endpoint}/api/tags", timeout=5)
        if response.status_code == 200:
            models = response.json().get("models", [])
            available_models = [model["name"] for model in models]
            print(f"✅ Ollama available with models: {available_models}")
            return {"available": True, "models": available_models, "endpoint": ollama_endpoint}
        else:
            print("⚠️ Ollama endpoint responding but no models available")
            return {"available": False, "models": [], "endpoint": ollama_endpoint}
    except requests.exceptions.RequestException:
        print("⚠️ Ollama not available - some tests will be skipped")
        return {"available": False, "models": [], "endpoint": ollama_endpoint}

@pytest.fixture
def sample_workshop_request():
    """Provide a sample workshop generation request."""
    return {
        "repository_url": "https://github.com/tosin2013/llama-stack-demos.git",
        "workshop_title": "AI Agent Development Workshop",
        "target_audience": "developers",
        "duration": "2-3 hours",
        "learning_objectives": [
            "Understand AI agent architecture",
            "Build multi-agent systems",
            "Deploy agents to OpenShift"
        ]
    }

def pytest_configure(config):
    """Configure pytest with custom markers."""
    config.addinivalue_line("markers", "integration: Integration tests requiring all agents")
    config.addinivalue_line("markers", "e2e: End-to-end tests with real repositories")
    config.addinivalue_line("markers", "ollama: Tests requiring ollama LLM integration")
    config.addinivalue_line("markers", "slow: Slow tests that take more than 30 seconds")

def pytest_collection_modifyitems(config, items):
    """Modify test collection to add markers based on test names."""
    for item in items:
        # Add markers based on test file location
        if "integration" in str(item.fspath):
            item.add_marker(pytest.mark.integration)
        if "e2e" in str(item.fspath):
            item.add_marker(pytest.mark.e2e)
        if "ollama" in item.name.lower():
            item.add_marker(pytest.mark.ollama)
