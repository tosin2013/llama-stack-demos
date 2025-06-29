#!/usr/bin/env python3
"""
End-to-End Tekton Human Oversight Workflow Testing
Script: test_e2e_tekton_human_oversight.py
ADR: 0006-tekton-agent-integration-architecture.md

This script implements the Python human interface layer for Tekton pipeline integration,
providing human-in-the-loop interaction with pipelines and agents as defined in ADR-0006.
"""

import json
import subprocess
import time
import requests
import sys
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass
from datetime import datetime

@dataclass
class TestRepository:
    """Test repository configuration"""
    name: str
    url: str
    expected_workflow: int
    description: str

# Test repositories from ADR-0001 and ADR-0006
TEST_REPOSITORIES = {
    "ddd-hexagonal": TestRepository(
        name="DDD Hexagonal Workshop",
        url="https://github.com/jeremyrdavis/dddhexagonalworkshop",
        expected_workflow=3,
        description="Existing workshop - should trigger Workflow 3 (Enhancement)"
    ),
    "ansible-cac": TestRepository(
        name="Ansible Controller CaC",
        url="https://github.com/tosin2013/ansible-controller-cac.git",
        expected_workflow=1,
        description="Application repository - should trigger Workflow 1 (New Workshop)"
    ),
    "llama-stack-demos": TestRepository(
        name="Llama Stack Demos",
        url="https://github.com/tosin2013/llama-stack-demos",
        expected_workflow=1,
        description="Tutorial content - should trigger Workflow 1 (New Workshop)"
    )
}

class TektonPipelineInterface:
    """
    Python interface for Tekton pipeline interaction with human oversight integration.
    Implements ADR-0006 hybrid Tekton-Python architecture.
    """
    
    def __init__(self, 
                 monitoring_service_url: str = "https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com",
                 openshift_namespace: str = "workshop-system"):
        self.monitoring_service_url = monitoring_service_url
        self.openshift_namespace = openshift_namespace
        self.session = requests.Session()
        self.session.verify = False  # For self-signed certificates
        
    def analyze_repository(self, repository_url: str) -> Dict:
        """
        Analyze repository using Template Converter Agent to determine workflow type.
        This implements the repository classification logic from ADR-0001.
        """
        print(f"🔍 Analyzing repository: {repository_url}")
        
        try:
            # Call Template Converter Agent directly for analysis
            agent_url = "http://template-converter-agent:80"
            response = self.session.post(
                f"{agent_url}/tools/analyze_repository_structure_tool",
                json={
                    "repository_url": repository_url,
                    "analysis_type": "repository-structure"
                },
                timeout=300
            )
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ Repository analysis completed")
                print(f"📊 Analysis result: {json.dumps(result, indent=2)}")
                return result
            else:
                print(f"❌ Agent call failed: HTTP {response.status_code}")
                print(f"Response: {response.text}")
                return {"error": f"HTTP {response.status_code}: {response.text}"}
                
        except Exception as e:
            print(f"❌ Repository analysis failed: {str(e)}")
            return {"error": str(e)}
    
    def determine_workflow_type(self, analysis_result: Dict) -> int:
        """
        Determine workflow type based on repository analysis.
        Implements ADR-0001 workflow selection logic.
        """
        if "result" in analysis_result:
            result = analysis_result["result"]
            if isinstance(result, str):
                try:
                    result = json.loads(result)
                except:
                    pass
            
            if isinstance(result, dict):
                repo_classification = result.get("repository_classification", "application")
                
                if repo_classification == "existing_workshop":
                    print(f"📚 Existing workshop detected → Workflow 3 (Enhancement)")
                    return 3
                else:
                    print(f"🆕 New content detected → Workflow 1 (New Workshop)")
                    return 1
        
        print(f"🤔 Unable to determine workflow type, defaulting to Workflow 1")
        return 1
    
    def create_pipeline_run(self, pipeline_name: str, params: Dict) -> Optional[str]:
        """
        Create Tekton PipelineRun using oc CLI.
        Returns the PipelineRun name if successful.
        """
        print(f"🚀 Creating PipelineRun for: {pipeline_name}")
        print(f"📝 Parameters: {json.dumps(params, indent=2)}")
        
        # Generate unique PipelineRun name
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        pipeline_run_name = f"{pipeline_name}-{timestamp}"
        
        # Create PipelineRun YAML
        pipeline_run_yaml = {
            "apiVersion": "tekton.dev/v1beta1",
            "kind": "PipelineRun",
            "metadata": {
                "name": pipeline_run_name,
                "namespace": self.openshift_namespace,
                "labels": {
                    "app": "workshop-template-system",
                    "pipeline": pipeline_name,
                    "created-by": "python-interface"
                }
            },
            "spec": {
                "pipelineRef": {
                    "name": pipeline_name
                },
                "params": [{"name": k, "value": v} for k, v in params.items()],
                "workspaces": [
                    {
                        "name": "shared-data",
                        "emptyDir": {}
                    },
                    {
                        "name": "gitea-auth",
                        "emptyDir": {}
                    }
                ]
            }
        }
        
        try:
            # Write YAML to temporary file
            import tempfile
            with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
                import yaml
                yaml.dump(pipeline_run_yaml, f)
                yaml_file = f.name
            
            # Apply PipelineRun using oc CLI
            result = subprocess.run([
                "oc", "apply", "-f", yaml_file, "-n", self.openshift_namespace
            ], capture_output=True, text=True)
            
            if result.returncode == 0:
                print(f"✅ PipelineRun created: {pipeline_run_name}")
                return pipeline_run_name
            else:
                print(f"❌ Failed to create PipelineRun: {result.stderr}")
                return None
                
        except Exception as e:
            print(f"❌ Error creating PipelineRun: {str(e)}")
            return None
    
    def monitor_pipeline_with_oversight(self, pipeline_run_name: str) -> Dict:
        """
        Monitor pipeline progress and handle human approval gates.
        Integrates with existing Human Oversight APIs.
        """
        print(f"⏳ Monitoring pipeline: {pipeline_run_name}")
        
        start_time = time.time()
        max_wait_time = 3600  # 1 hour timeout
        
        while time.time() - start_time < max_wait_time:
            try:
                # Get pipeline status
                result = subprocess.run([
                    "oc", "get", "pipelinerun", pipeline_run_name, 
                    "-n", self.openshift_namespace, "-o", "json"
                ], capture_output=True, text=True)
                
                if result.returncode != 0:
                    print(f"❌ Failed to get pipeline status: {result.stderr}")
                    break
                
                pipeline_status = json.loads(result.stdout)
                status = pipeline_status.get("status", {})
                
                # Check completion
                completion_time = status.get("completionTime")
                if completion_time:
                    final_status = status.get("conditions", [{}])[-1].get("reason", "Unknown")
                    print(f"🏁 Pipeline completed with status: {final_status}")
                    return {
                        "status": final_status,
                        "completion_time": completion_time,
                        "pipeline_run": pipeline_status
                    }
                
                # Check for human approval gates
                task_runs = status.get("taskRuns", {})
                for task_name, task_run in task_runs.items():
                    if "human-approval" in task_name:
                        task_status = task_run.get("status", {})
                        if not task_status.get("completionTime") and task_status.get("startTime"):
                            print(f"👥 Human approval required for: {task_name}")
                            self.handle_human_approval_gate(pipeline_run_name, task_name)
                
                # Print progress
                completed_tasks = len([tr for tr in task_runs.values() 
                                     if tr.get("status", {}).get("completionTime")])
                total_tasks = len(task_runs)
                print(f"📊 Progress: {completed_tasks}/{total_tasks} tasks completed")
                
                time.sleep(30)  # Wait 30 seconds before next check
                
            except Exception as e:
                print(f"❌ Error monitoring pipeline: {str(e)}")
                break
        
        print(f"⏰ Pipeline monitoring timed out after {max_wait_time} seconds")
        return {"status": "timeout", "error": "Monitoring timeout"}
    
    def handle_human_approval_gate(self, pipeline_run_name: str, task_name: str):
        """
        Handle human approval gates by interacting with Human Oversight APIs.
        """
        print(f"🤖 Handling approval gate: {task_name}")
        
        try:
            # Send chat notification about approval requirement
            chat_response = self.session.post(
                f"{self.monitoring_service_url}/api/oversight/chat",
                json={
                    "message": f"Approval required for pipeline {pipeline_run_name}, task {task_name}. Please review and approve/reject.",
                    "sessionId": f"pipeline-{pipeline_run_name}"
                }
            )
            
            if chat_response.status_code == 200:
                print(f"💬 Chat notification sent successfully")
            else:
                print(f"⚠️ Chat notification failed: {chat_response.status_code}")
            
            # The human approval task will handle the actual approval polling
            # This is just to notify the human oversight coordinator
            
        except Exception as e:
            print(f"❌ Error handling approval gate: {str(e)}")
    
    def trigger_workflow_1(self, repo_url: str, workshop_name: str) -> Dict:
        """
        Trigger Workflow 1 pipeline for new workshop creation.
        """
        print(f"🆕 Triggering Workflow 1: New Workshop Creation")
        print(f"Repository: {repo_url}")
        print(f"Workshop Name: {workshop_name}")
        
        params = {
            "repository-url": repo_url,
            "workshop-name": workshop_name,
            "base-template": "showroom_template_default",
            "gitea-repo-name": f"{workshop_name.lower().replace(' ', '-')}-workshop",
            "human-approver": "system-operator"
        }
        
        pipeline_run_name = self.create_pipeline_run("workflow-1-new-workshop", params)
        if not pipeline_run_name:
            return {"error": "Failed to create pipeline run"}
        
        return self.monitor_pipeline_with_oversight(pipeline_run_name)
    
    def trigger_workflow_3(self, repo_url: str, workshop_name: str, original_workshop_url: str) -> Dict:
        """
        Trigger Workflow 3 pipeline for existing workshop enhancement.
        """
        print(f"🔄 Triggering Workflow 3: Existing Workshop Enhancement")
        print(f"Repository: {repo_url}")
        print(f"Workshop Name: {workshop_name}")
        print(f"Original Workshop: {original_workshop_url}")
        
        params = {
            "repository-url": repo_url,
            "workshop-name": workshop_name,
            "original-workshop-url": original_workshop_url,
            "gitea-repo-name": f"{workshop_name.lower().replace(' ', '-')}-enhanced",
            "human-approver": "system-operator",
            "enhancement-type": "content-update"
        }
        
        pipeline_run_name = self.create_pipeline_run("workflow-3-enhance-workshop", params)
        if not pipeline_run_name:
            return {"error": "Failed to create pipeline run"}
        
        return self.monitor_pipeline_with_oversight(pipeline_run_name)

def run_complete_e2e_test():
    """
    Execute complete end-to-end testing workflow per ADR-0006.
    Tests all repository types with appropriate workflows.
    """
    print("🚀 Starting End-to-End Tekton Human Oversight Workflow Test (ADR-0006)")
    print("=" * 80)
    
    interface = TektonPipelineInterface()
    results = {}
    
    for repo_key, repo_config in TEST_REPOSITORIES.items():
        print(f"\n📁 Testing repository: {repo_config.name}")
        print(f"🔗 URL: {repo_config.url}")
        print(f"📋 Description: {repo_config.description}")
        print("-" * 60)
        
        try:
            # Phase 1: Repository Analysis
            analysis_result = interface.analyze_repository(repo_config.url)
            if "error" in analysis_result:
                print(f"❌ Repository analysis failed: {analysis_result['error']}")
                results[repo_key] = {"error": "Analysis failed", "details": analysis_result}
                continue
            
            # Phase 2: Workflow Determination
            determined_workflow = interface.determine_workflow_type(analysis_result)
            expected_workflow = repo_config.expected_workflow
            
            print(f"🎯 Expected Workflow: {expected_workflow}")
            print(f"🤖 Determined Workflow: {determined_workflow}")
            
            if determined_workflow != expected_workflow:
                print(f"⚠️ Workflow mismatch! Expected {expected_workflow}, got {determined_workflow}")
            
            # Phase 3: Pipeline Execution
            workshop_name = f"{repo_config.name} Test"
            
            if determined_workflow == 1:
                pipeline_result = interface.trigger_workflow_1(repo_config.url, workshop_name)
            elif determined_workflow == 3:
                pipeline_result = interface.trigger_workflow_3(
                    repo_config.url, 
                    workshop_name, 
                    repo_config.url  # Use same URL as original for testing
                )
            else:
                pipeline_result = {"error": f"Unknown workflow type: {determined_workflow}"}
            
            results[repo_key] = {
                "repository": repo_config.name,
                "analysis": analysis_result,
                "expected_workflow": expected_workflow,
                "determined_workflow": determined_workflow,
                "pipeline_result": pipeline_result
            }
            
            print(f"✅ Repository {repo_config.name} processing completed")
            
        except Exception as e:
            print(f"❌ Error processing {repo_config.name}: {str(e)}")
            results[repo_key] = {"error": str(e)}
    
    # Phase 4: Generate comprehensive report
    generate_e2e_test_report(results)
    
    print("\n🎉 End-to-End Tekton Human Oversight Testing Completed!")
    return results

def generate_e2e_test_report(results: Dict):
    """Generate comprehensive test report"""
    print("\n📊 End-to-End Test Report")
    print("=" * 80)
    
    total_tests = len(results)
    successful_tests = len([r for r in results.values() if "error" not in r])
    
    print(f"📈 Test Summary: {successful_tests}/{total_tests} repositories processed successfully")
    print(f"🕒 Test completed at: {datetime.now().isoformat()}")
    print()
    
    for repo_key, result in results.items():
        print(f"📁 {repo_key.upper()}:")
        if "error" in result:
            print(f"   ❌ Error: {result['error']}")
        else:
            print(f"   📊 Expected Workflow: {result['expected_workflow']}")
            print(f"   🤖 Determined Workflow: {result['determined_workflow']}")
            print(f"   ✅ Workflow Match: {'Yes' if result['expected_workflow'] == result['determined_workflow'] else 'No'}")
            
            pipeline_result = result.get('pipeline_result', {})
            if 'status' in pipeline_result:
                print(f"   🏁 Pipeline Status: {pipeline_result['status']}")
        print()

if __name__ == "__main__":
    try:
        run_complete_e2e_test()
    except KeyboardInterrupt:
        print("\n⏹️ Test interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Test failed with error: {str(e)}")
        sys.exit(1)
