#!/usr/bin/env python3
"""
Test our 6-agent workshop template system with real repositories
"""

import json
import requests
import uuid
import time

def test_template_converter_agent():
    """Test Template Converter Agent with OpenShift Bare Metal Workshop"""
    
    print("🔍 Testing Template Converter Agent with OpenShift Bare Metal Workshop...")
    
    agent_url = "http://localhost:10041"
    
    # Test repository analysis
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-openshift-workshop",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Analyze https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git for workshop conversion potential"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Repository Analysis Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text'][:500]}...")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed repository analysis: {response.status_code}")
        print(f"   Response: {response.text}")

def test_content_creator_agent():
    """Test Content Creator Agent with original workshop creation"""
    
    print("\n🎨 Testing Content Creator Agent with original workshop creation...")
    
    agent_url = "http://localhost:10080"
    
    # Test workshop design from objectives
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-content-creation",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Design a workshop with these learning objectives:\n- Understand cloud-native security fundamentals\n- Implement container security best practices\n- Configure network policies in Kubernetes\n- Set up monitoring and alerting for security events"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Workshop Design Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text'][:500]}...")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed workshop design: {response.status_code}")
        print(f"   Response: {response.text}")

def test_research_validation_agent():
    """Test Research & Validation Agent with technology research"""
    
    print("\n🔍 Testing Research & Validation Agent with technology research...")
    
    agent_url = "http://localhost:10070"
    
    # Test technology research
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-research",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Research current information about OpenShift 4.16+ for workshop content validation, focusing on installation methods and best practices"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Technology Research Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text'][:500]}...")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed technology research: {response.status_code}")
        print(f"   Response: {response.text}")

def test_healthcare_ml_analysis():
    """Test Template Converter Agent with Healthcare ML repository"""
    
    print("\n🧬 Testing Template Converter Agent with Healthcare ML Genetic Predictor...")
    
    agent_url = "http://localhost:10041"
    
    # Test repository analysis for complex application
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-healthcare-ml",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion potential. This is a complex ML application with Quarkus, Kafka, and OpenShift deployment."
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Healthcare ML Analysis Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text'][:500]}...")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed healthcare ML analysis: {response.status_code}")
        print(f"   Response: {response.text}")

def test_agent_coordination():
    """Test coordination between multiple agents"""
    
    print("\n🤝 Testing Multi-Agent Coordination...")
    
    # Test Documentation Pipeline Agent
    doc_agent_url = "http://localhost:10050"
    
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-coordination",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Monitor changes in https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git,https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for the last 7 days"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{doc_agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Documentation Pipeline Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(f"   {message['parts'][0]['text'][:500]}...")
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed documentation pipeline: {response.status_code}")
        print(f"   Response: {response.text}")

if __name__ == "__main__":
    print("🚀 Testing Workshop Template System with Real Repositories")
    print("=" * 60)
    
    # Test each agent with real repository data
    test_template_converter_agent()
    time.sleep(2)
    
    test_content_creator_agent()
    time.sleep(2)
    
    test_research_validation_agent()
    time.sleep(2)
    
    test_healthcare_ml_analysis()
    time.sleep(2)
    
    test_agent_coordination()
    
    print("\n🎯 Real Repository Testing Complete!")
    print("Check the responses above to see how our agents handle real-world workshop content.")
