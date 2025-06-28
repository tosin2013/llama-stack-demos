#!/usr/bin/env python3
"""
Test workshop detection logic directly
"""

import sys
import os
sys.path.append('/home/ec2-user/llama-stack-demos')

from demos.workshop_template_system.agents.template_converter.tools import detect_existing_workshop

def test_detection_logic():
    """Test the workshop detection logic"""
    
    print("🎯 Testing Workshop Detection Logic")
    print("=" * 50)
    
    # Test 1: OpenShift workshop
    print("🔍 Test 1: OpenShift Bare Metal Workshop")
    print("-" * 30)
    
    result1 = detect_existing_workshop("Red-Hat-SE-RTO", "openshift-bare-metal-deployment-workshop")
    
    print(f"Is Workshop: {result1['is_workshop']}")
    print(f"Workshop Type: {result1['workshop_type']}")
    print(f"Quality Level: {result1['quality_level']}")
    print(f"Confidence: {result1['confidence']}")
    print("Indicators Found:")
    for indicator in result1['indicators']:
        print(f"  - {indicator}")
    
    print("\n" + "=" * 50)
    
    # Test 2: Healthcare ML app
    print("🧬 Test 2: Healthcare ML Application")
    print("-" * 30)
    
    result2 = detect_existing_workshop("tosin2013", "healthcare-ml-genetic-predictor")
    
    print(f"Is Workshop: {result2['is_workshop']}")
    print(f"Workshop Type: {result2['workshop_type']}")
    print(f"Quality Level: {result2['quality_level']}")
    print(f"Confidence: {result2['confidence']}")
    print("Indicators Found:")
    for indicator in result2['indicators']:
        print(f"  - {indicator}")
    
    print("\n" + "=" * 50)
    
    # Test 3: Generic workshop name
    print("📚 Test 3: Generic Workshop Repository")
    print("-" * 30)
    
    result3 = detect_existing_workshop("example", "kubernetes-workshop")
    
    print(f"Is Workshop: {result3['is_workshop']}")
    print(f"Workshop Type: {result3['workshop_type']}")
    print(f"Quality Level: {result3['quality_level']}")
    print(f"Confidence: {result3['confidence']}")
    print("Indicators Found:")
    for indicator in result3['indicators']:
        print(f"  - {indicator}")

if __name__ == "__main__":
    test_detection_logic()
