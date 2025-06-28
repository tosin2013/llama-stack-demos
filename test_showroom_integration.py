#!/usr/bin/env python3
"""
Test Showroom template integration
"""

import sys
import os
sys.path.append('/home/ec2-user/llama-stack-demos')

from demos.workshop_template_system.agents.content_creator.tools import clone_showroom_template_tool

def test_showroom_integration():
    """Test Showroom template integration for different scenarios"""
    
    print("🎯 Testing Showroom Template Integration")
    print("=" * 60)
    
    # Test 1: Healthcare ML Workshop using Showroom template
    print("🧬 Test 1: Healthcare ML Workshop with Showroom Template")
    print("-" * 50)
    
    result1 = clone_showroom_template_tool(
        workshop_name="Healthcare ML Genetic Risk Prediction",
        technology_focus="openshift",
        customization_level="extensive"
    )
    
    print(result1)
    print("\n" + "=" * 60)
    
    # Test 2: OpenShift workshop enhancement
    print("🔧 Test 2: OpenShift Workshop Enhancement")
    print("-" * 50)
    
    result2 = clone_showroom_template_tool(
        workshop_name="Advanced OpenShift Security",
        technology_focus="openshift", 
        customization_level="standard"
    )
    
    print(result2)
    print("\n" + "=" * 60)

def test_workflow_integration():
    """Test how Showroom template integrates with our workflow"""
    
    print("🔄 Testing Complete Workflow Integration")
    print("=" * 60)
    
    print("📋 Complete Workshop Creation Workflow:")
    print()
    print("1. **Repository Analysis** (Template Converter Agent)")
    print("   - Detect if existing workshop or application")
    print("   - Assess conversion potential")
    print()
    print("2. **Content Creation** (Content Creator Agent)")
    print("   - For Applications: Clone Showroom template")
    print("   - For Existing Workshops: Enhancement recommendations")
    print()
    print("3. **Research & Validation** (Research Agent)")
    print("   - Validate technical accuracy")
    print("   - Find current resources and documentation")
    print()
    print("4. **Documentation Pipeline** (Documentation Agent)")
    print("   - Monitor for changes")
    print("   - Create update proposals")
    print()
    print("5. **Source Management** (Source Manager Agent)")
    print("   - Deploy to RHPDS/Showroom")
    print("   - Manage repository lifecycle")
    print()
    print("6. **Workshop Chat** (Chat Agent)")
    print("   - Provide participant assistance")
    print("   - Answer workshop-related questions")

if __name__ == "__main__":
    test_showroom_integration()
    test_workflow_integration()
