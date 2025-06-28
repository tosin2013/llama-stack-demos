#!/usr/bin/env python3
"""
Test workshop detection capabilities with real repositories
"""

import json
import requests
import uuid

def test_openshift_workshop_detection():
    """Test detection of existing OpenShift workshop"""
    
    print("🔍 Testing Workshop Detection: OpenShift Bare Metal Workshop")
    print("=" * 60)
    
    agent_url = "http://localhost:10041"
    
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-workshop-detection",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Analyze https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git for workshop detection and assessment"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Workshop Detection Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(message['parts'][0]['text'])
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed workshop detection: {response.status_code}")
        print(f"   Response: {response.text}")

def test_healthcare_ml_detection():
    """Test detection with non-workshop repository"""
    
    print("\n🧬 Testing Non-Workshop Detection: Healthcare ML Application")
    print("=" * 60)
    
    agent_url = "http://localhost:10041"
    
    task_payload = {
        "id": str(uuid.uuid4()),
        "params": {
            "id": str(uuid.uuid4()),
            "sessionId": "test-app-detection",
            "message": {
                "role": "user",
                "parts": [{
                    "type": "text", 
                    "text": "Analyze https://github.com/tosin2013/healthcare-ml-genetic-predictor.git for workshop conversion potential"
                }]
            },
            "acceptedOutputModes": ["text/plain"]
        }
    }
    
    response = requests.post(f"{agent_url}/send-task", json=task_payload)
    if response.status_code == 200:
        result = response.json()
        print("✅ Application Analysis Response:")
        if "result" in result and "status" in result["result"]:
            message = result["result"]["status"]["message"]
            if "parts" in message and len(message["parts"]) > 0:
                print(message['parts'][0]['text'])
            else:
                print(f"   {message}")
        else:
            print(f"   {result}")
    else:
        print(f"❌ Failed application analysis: {response.status_code}")
        print(f"   Response: {response.text}")

if __name__ == "__main__":
    print("🎯 Testing Enhanced Workshop Detection Capabilities")
    print("Testing our updated Template Converter Agent's ability to:")
    print("1. Detect existing workshops")
    print("2. Assess workshop quality")
    print("3. Provide appropriate recommendations")
    print()
    
    test_openshift_workshop_detection()
    test_healthcare_ml_detection()
    
    print("\n🎯 Workshop Detection Testing Complete!")
    print("The agent should now distinguish between existing workshops and applications.")
