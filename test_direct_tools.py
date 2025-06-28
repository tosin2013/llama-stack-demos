#!/usr/bin/env python3
"""
Test workshop detection tools directly
"""

import sys
import os
sys.path.append('/home/ec2-user/llama-stack-demos')

from demos.workshop_template_system.agents.template_converter.tools import analyze_repository_tool

def test_workshop_detection_direct():
    """Test workshop detection directly using the tool functions"""
    
    print("🎯 Direct Tool Testing: Workshop Detection")
    print("=" * 60)
    
    # Test 1: OpenShift Bare Metal Workshop (existing workshop)
    print("🔍 Test 1: OpenShift Bare Metal Workshop")
    print("-" * 40)
    
    result1 = analyze_repository_tool(
        repository_url="https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git",
        analysis_depth="standard"
    )
    
    print(result1)
    print("\n" + "=" * 60)
    
    # Test 2: Healthcare ML Application (not a workshop)
    print("🧬 Test 2: Healthcare ML Genetic Predictor")
    print("-" * 40)
    
    result2 = analyze_repository_tool(
        repository_url="https://github.com/tosin2013/healthcare-ml-genetic-predictor.git",
        analysis_depth="standard"
    )
    
    print(result2)
    print("\n" + "=" * 60)

if __name__ == "__main__":
    test_workshop_detection_direct()
