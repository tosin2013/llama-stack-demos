#!/usr/bin/env python3
"""
Test script for Workshop Chat Agent
"""

import json
import requests
import uuid

def test_workshop_agent():
    """Test the Workshop Chat Agent functionality"""
    
    # Agent endpoint
    agent_url = "http://localhost:10040"
    
    # Test 1: Get agent card
    print("🔍 Testing Agent Card...")
    response = requests.get(f"{agent_url}/agent-card")
    if response.status_code == 200:
        agent_card = response.json()
        print(f"✅ Agent Card: {agent_card['name']}")
        print(f"   Description: {agent_card['description']}")
        print(f"   Skills: {len(agent_card['skills'])} available")
    else:
        print(f"❌ Failed to get agent card: {response.status_code}")
        return
    
    # Test 2: Send a workshop navigation query
    print("\n🧭 Testing Workshop Navigation...")
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-session-1",
            "message": {
                "role": "user",
                "parts": [{"type": "text", "text": "Where should I start with this workshop?"}]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Navigation Query Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text']}")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed navigation query: {response.status_code}")
        print(f"   Response: {response.text}")
    
    # Test 3: Send a workshop content query
    print("\n📚 Testing Workshop Content Query...")
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-session-2", 
            "message": {
                "role": "user",
                "parts": [{"type": "text", "text": "How do I set up the development environment?"}]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Content Query Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text']}")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed content query: {response.status_code}")
        print(f"   Response: {response.text}")

if __name__ == "__main__":
    test_workshop_agent()
