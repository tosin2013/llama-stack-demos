"""
Repository Cloning Agent Tools
Handles ADR-0001 compliant repository and template cloning into shared workspace
"""

import os
import logging
import subprocess
import json
import shutil
from typing import Dict, Any
from urllib.parse import urlparse

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func

logger = logging.getLogger(__name__)

@client_tool
def clone_repositories_for_workflow_tool(repository_url: str, workflow_type: str, workshop_name: str = "") -> str:
    """
    :description: Clone repositories into shared workspace based on ADR-0001 workflow strategy.
    :use_case: Clone showroom_template_default for Workflow 1 or existing workshop for Workflow 3 into shared workspace.
    :param repository_url: Source repository URL to analyze/clone
    :param workflow_type: Either 'workflow1' (application) or 'workflow3' (existing workshop)
    :param workshop_name: Name for the workshop being created
    :return: JSON string with cloning results and shared workspace paths
    """
    try:
        logger.info(f"🔄 Starting ADR-0001 repository cloning for {workflow_type}")
        
        # Define shared workspace paths
        shared_workspace = os.environ.get('WORKSPACE_PATH', '/workspace/shared-data')
        if not os.path.exists(shared_workspace):
            shared_workspace = '/tmp/workshop-shared-workspace'
        
        templates_dir = os.path.join(shared_workspace, 'shared', 'templates')
        source_repos_dir = os.path.join(shared_workspace, 'shared', 'source-repositories')
        working_dir = os.path.join(shared_workspace, 'agents', 'repository-cloning', 'working')
        
        # Ensure directories exist
        os.makedirs(templates_dir, exist_ok=True)
        os.makedirs(source_repos_dir, exist_ok=True)
        os.makedirs(working_dir, exist_ok=True)
        
        results = {
            'workflow_type': workflow_type,
            'repository_url': repository_url,
            'workshop_name': workshop_name,
            'shared_workspace': shared_workspace,
            'cloned_repositories': []
        }
        
        if workflow_type == 'workflow1':
            # Workflow 1: Application → Workshop (use showroom template)
            results.update(clone_workflow1_repositories(
                repository_url, workshop_name, templates_dir, source_repos_dir, working_dir
            ))
        elif workflow_type == 'workflow3':
            # Workflow 3: Existing Workshop Enhancement
            results.update(clone_workflow3_repositories(
                repository_url, workshop_name, source_repos_dir, working_dir
            ))
        else:
            raise ValueError(f"Unsupported workflow type: {workflow_type}")
        
        logger.info(f"✅ Repository cloning completed for {workflow_type}")
        return json.dumps(results, indent=2)
        
    except Exception as e:
        logger.error(f"❌ Error in clone_repositories_for_workflow_tool: {e}")
        return json.dumps({
            'success': False,
            'error': str(e),
            'workflow_type': workflow_type,
            'repository_url': repository_url
        })

def clone_workflow1_repositories(repository_url: str, workshop_name: str, templates_dir: str, source_repos_dir: str, working_dir: str) -> Dict[str, Any]:
    """Clone repositories for Workflow 1: Application → Workshop"""
    logger.info("🎯 Executing Workflow 1: Application → Workshop")
    
    cloned_repos = []
    
    # 1. Clone showroom_template_default (ADR-0001 template base)
    showroom_template_path = os.path.join(templates_dir, 'showroom_template_default')
    if not os.path.exists(showroom_template_path):
        logger.info("📦 Cloning showroom_template_default...")
        result = clone_repository(
            'https://github.com/rhpds/showroom_template_default.git',
            showroom_template_path
        )
        cloned_repos.append({
            'type': 'template',
            'url': 'https://github.com/rhpds/showroom_template_default.git',
            'local_path': showroom_template_path,
            'success': result['success']
        })
    else:
        logger.info("✅ showroom_template_default already cached")
        cloned_repos.append({
            'type': 'template',
            'url': 'https://github.com/rhpds/showroom_template_default.git',
            'local_path': showroom_template_path,
            'success': True,
            'cached': True
        })
    
    # 2. Clone source application repository for analysis
    repo_name = extract_repo_name(repository_url)
    source_repo_path = os.path.join(source_repos_dir, repo_name)
    logger.info(f"📥 Cloning source repository: {repository_url}")
    result = clone_repository(repository_url, source_repo_path)
    cloned_repos.append({
        'type': 'source',
        'url': repository_url,
        'local_path': source_repo_path,
        'success': result['success']
    })
    
    # 3. Create working copy of template for customization
    workshop_working_path = os.path.join(working_dir, workshop_name)
    if os.path.exists(workshop_working_path):
        shutil.rmtree(workshop_working_path)
    
    logger.info(f"🔧 Creating working copy of template for {workshop_name}")
    shutil.copytree(showroom_template_path, workshop_working_path, ignore=shutil.ignore_patterns('.git'))
    
    return {
        'success': True,
        'strategy': 'ADR-0001 Workflow 1: Application → Workshop',
        'template_source': 'showroom_template_default',
        'source_repository': repository_url,
        'working_path': workshop_working_path,
        'template_path': showroom_template_path,
        'source_path': source_repo_path,
        'cloned_repositories': cloned_repos
    }

def clone_workflow3_repositories(repository_url: str, workshop_name: str, source_repos_dir: str, working_dir: str) -> Dict[str, Any]:
    """Clone repositories for Workflow 3: Existing Workshop Enhancement"""
    logger.info("🎯 Executing Workflow 3: Existing Workshop Enhancement")
    
    cloned_repos = []
    
    # Clone existing workshop repository
    repo_name = extract_repo_name(repository_url)
    workshop_repo_path = os.path.join(source_repos_dir, repo_name)
    logger.info(f"📥 Cloning existing workshop: {repository_url}")
    result = clone_repository(repository_url, workshop_repo_path)
    cloned_repos.append({
        'type': 'existing_workshop',
        'url': repository_url,
        'local_path': workshop_repo_path,
        'success': result['success']
    })
    
    # Create working copy for enhancement
    workshop_working_path = os.path.join(working_dir, workshop_name)
    if os.path.exists(workshop_working_path):
        shutil.rmtree(workshop_working_path)
    
    logger.info(f"🔧 Creating working copy for enhancement: {workshop_name}")
    shutil.copytree(workshop_repo_path, workshop_working_path, ignore=shutil.ignore_patterns('.git'))
    
    return {
        'success': True,
        'strategy': 'ADR-0001 Workflow 3: Existing Workshop Enhancement',
        'source_repository': repository_url,
        'working_path': workshop_working_path,
        'source_path': workshop_repo_path,
        'cloned_repositories': cloned_repos
    }

def clone_repository(repo_url: str, target_path: str) -> Dict[str, Any]:
    """Clone a git repository to target path"""
    try:
        if os.path.exists(target_path):
            shutil.rmtree(target_path)
        
        cmd = ['git', 'clone', repo_url, target_path]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        
        if result.returncode == 0:
            logger.info(f"✅ Successfully cloned {repo_url}")
            return {'success': True, 'path': target_path}
        else:
            logger.error(f"❌ Failed to clone {repo_url}: {result.stderr}")
            return {'success': False, 'error': result.stderr}
            
    except Exception as e:
        logger.error(f"❌ Exception cloning {repo_url}: {e}")
        return {'success': False, 'error': str(e)}

def extract_repo_name(repo_url: str) -> str:
    """Extract repository name from URL"""
    parsed = urlparse(repo_url)
    path = parsed.path.strip('/')
    if path.endswith('.git'):
        path = path[:-4]
    return path.split('/')[-1]

@client_tool
def validate_cloned_repositories_tool(workshop_name: str) -> str:
    """
    :description: Validate that repositories were properly cloned into shared workspace.
    :use_case: Verify ADR-0001 compliant repository cloning completed successfully.
    :param workshop_name: Name of the workshop to validate
    :return: JSON string with validation results
    """
    try:
        shared_workspace = os.environ.get('WORKSPACE_PATH', '/workspace/shared-data')
        if not os.path.exists(shared_workspace):
            shared_workspace = '/tmp/workshop-shared-workspace'
        
        working_dir = os.path.join(shared_workspace, 'agents', 'repository-cloning', 'working')
        workshop_path = os.path.join(working_dir, workshop_name)
        
        validation_results = {
            'workshop_name': workshop_name,
            'workshop_path': workshop_path,
            'exists': os.path.exists(workshop_path),
            'files': [],
            'structure_valid': False
        }
        
        if os.path.exists(workshop_path):
            # Check for key workshop files
            key_files = ['README.adoc', 'default-site.yml', 'content/antora.yml']
            validation_results['files'] = [
                {
                    'file': file,
                    'exists': os.path.exists(os.path.join(workshop_path, file))
                }
                for file in key_files
            ]
            
            # Validate structure
            has_required_files = all(
                os.path.exists(os.path.join(workshop_path, file))
                for file in ['README.adoc', 'content/antora.yml']
            )
            validation_results['structure_valid'] = has_required_files
        
        logger.info(f"📊 Validation results for {workshop_name}: {validation_results}")
        return json.dumps(validation_results, indent=2)
        
    except Exception as e:
        logger.error(f"❌ Error validating repositories: {e}")
        return json.dumps({'success': False, 'error': str(e)})
