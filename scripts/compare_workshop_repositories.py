#!/usr/bin/env python3
"""
Workshop Repository Comparison Script

This script compares the structure and content differences between workshop repositories
created using different ADR-0001 workflows to validate implementation differences.

Usage:
    python scripts/compare_workshop_repositories.py <repo1> <repo2>
    python scripts/compare_workshop_repositories.py --list-repos
"""

import os
import sys
import argparse

# Add the project root to Python path
sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'demos', 'workshop_template_system'))

def setup_environment():
    """Set up Gitea environment variables"""
    os.environ['GITEA_URL'] = 'https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com'
    os.environ['GITEA_ADMIN_TOKEN'] = '5064d47a5fdb598395a4eb57d3253c394467ca6c'
    os.environ['GITEA_USER'] = 'opentlc-mgr'

def list_workshop_repositories():
    """List all workshop repositories in Gitea"""
    print("📋 WORKSHOP REPOSITORIES IN GITEA")
    print("=" * 50)
    
    try:
        from agents.source_manager.tools import get_gitea_config
        import requests
        
        gitea_config = get_gitea_config()
        if not gitea_config['success']:
            print(f"❌ Cannot connect to Gitea: {gitea_config['error']}")
            return
        
        # List repositories for the user
        api_url = gitea_config['api_url']
        headers = {
            'Authorization': f"token {gitea_config['token']}",
            'Content-Type': 'application/json'
        }
        
        response = requests.get(
            f"{api_url}/user/repos",
            headers=headers,
            timeout=30
        )
        
        if response.status_code == 200:
            repos = response.json()
            workshop_repos = [repo for repo in repos if 'workshop' in repo['name'].lower() or 'test' in repo['name'].lower()]
            
            if workshop_repos:
                print(f"Found {len(workshop_repos)} workshop repositories:")
                for repo in workshop_repos:
                    print(f"  📦 {repo['name']}")
                    print(f"     Created: {repo['created_at'][:10]}")
                    print(f"     URL: {repo['html_url']}")
                    print()
            else:
                print("No workshop repositories found.")
        else:
            print(f"❌ Failed to list repositories: {response.status_code}")
            
    except Exception as e:
        print(f"❌ Error listing repositories: {str(e)}")

def compare_repositories(repo1_name: str, repo2_name: str):
    """Compare two workshop repositories in detail"""
    print(f"🔍 COMPARING WORKSHOP REPOSITORIES")
    print("=" * 60)
    print(f"Repository 1: {repo1_name}")
    print(f"Repository 2: {repo2_name}")
    print()
    
    try:
        from agents.source_manager.tools import fetch_gitea_repository_tree, get_gitea_config
        
        # Get Gitea configuration
        gitea_config = get_gitea_config()
        if not gitea_config['success']:
            print(f"❌ Cannot connect to Gitea: {gitea_config['error']}")
            return
        
        # Fetch repository structures
        print("📊 Fetching repository structures...")
        
        repo1_structure = fetch_gitea_repository_tree(repo1_name, gitea_config)
        repo2_structure = fetch_gitea_repository_tree(repo2_name, gitea_config)
        
        if not repo1_structure.get('success'):
            print(f"❌ Cannot fetch {repo1_name}: {repo1_structure.get('error', 'Unknown error')}")
            return
        
        if not repo2_structure.get('success'):
            print(f"❌ Cannot fetch {repo2_name}: {repo2_structure.get('error', 'Unknown error')}")
            return
        
        repo1_files = repo1_structure['content']['files']
        repo1_dirs = repo1_structure['content']['directories']
        repo2_files = repo2_structure['content']['files']
        repo2_dirs = repo2_structure['content']['directories']
        
        # Detailed comparison
        print("\n📋 DETAILED COMPARISON")
        print("-" * 60)
        
        print(f"{'Metric':<30} {repo1_name:<20} {repo2_name:<20}")
        print("-" * 70)
        print(f"{'Total Files':<30} {len(repo1_files):<20} {len(repo2_files):<20}")
        print(f"{'Total Directories':<30} {len(repo1_dirs):<20} {len(repo2_dirs):<20}")
        
        # File type analysis
        def get_file_types(files):
            types = {}
            for file in files:
                if '.' in file:
                    ext = file.split('.')[-1].lower()
                    types[ext] = types.get(ext, 0) + 1
                else:
                    types['no_extension'] = types.get('no_extension', 0) + 1
            return types
        
        repo1_types = get_file_types(repo1_files)
        repo2_types = get_file_types(repo2_files)
        
        print(f"\n📄 FILE TYPE BREAKDOWN")
        print("-" * 60)
        
        all_types = sorted(set(repo1_types.keys()) | set(repo2_types.keys()))
        for file_type in all_types:
            count1 = repo1_types.get(file_type, 0)
            count2 = repo2_types.get(file_type, 0)
            print(f"{f'.{file_type}':<30} {count1:<20} {count2:<20}")
        
        # Unique and common files
        repo1_only = set(repo1_files) - set(repo2_files)
        repo2_only = set(repo2_files) - set(repo1_files)
        common_files = set(repo1_files) & set(repo2_files)
        
        print(f"\n🔄 FILE OVERLAP ANALYSIS")
        print("-" * 60)
        print(f"Common files: {len(common_files)}")
        print(f"{repo1_name} unique: {len(repo1_only)}")
        print(f"{repo2_name} unique: {len(repo2_only)}")
        
        if repo1_only:
            print(f"\n📁 UNIQUE TO {repo1_name.upper()}:")
            for file in sorted(repo1_only)[:10]:  # Show first 10
                print(f"  ✅ {file}")
            if len(repo1_only) > 10:
                print(f"  ... and {len(repo1_only) - 10} more files")
        
        if repo2_only:
            print(f"\n📁 UNIQUE TO {repo2_name.upper()}:")
            for file in sorted(repo2_only)[:10]:  # Show first 10
                print(f"  ✅ {file}")
            if len(repo2_only) > 10:
                print(f"  ... and {len(repo2_only) - 10} more files")
        
        # Directory structure comparison
        repo1_only_dirs = set(repo1_dirs) - set(repo2_dirs)
        repo2_only_dirs = set(repo2_dirs) - set(repo1_dirs)
        
        if repo1_only_dirs or repo2_only_dirs:
            print(f"\n📂 DIRECTORY DIFFERENCES")
            print("-" * 60)
            
            if repo1_only_dirs:
                print(f"Unique to {repo1_name}:")
                for dir_name in sorted(repo1_only_dirs):
                    print(f"  📂 {dir_name}/")
            
            if repo2_only_dirs:
                print(f"Unique to {repo2_name}:")
                for dir_name in sorted(repo2_only_dirs):
                    print(f"  📂 {dir_name}/")
        
        # ADR-0001 compliance check
        print(f"\n🎯 ADR-0001 COMPLIANCE CHECK")
        print("-" * 60)
        
        adr_files = [
            'showroom.yml',
            'default-site.yml',
            'ui-config.yml',
            'content/modules/ROOT/nav.adoc',
            'content/modules/ROOT/pages/index.adoc',
            'utilities/build.sh'
        ]
        
        print(f"{'ADR-0001 Required File':<40} {repo1_name:<15} {repo2_name:<15}")
        print("-" * 70)
        
        for adr_file in adr_files:
            repo1_has = "✅" if adr_file in repo1_files else "❌"
            repo2_has = "✅" if adr_file in repo2_files else "❌"
            print(f"{adr_file:<40} {repo1_has:<15} {repo2_has:<15}")
        
        # Summary
        print(f"\n📊 COMPARISON SUMMARY")
        print("-" * 60)
        
        # Determine likely workflow types
        repo1_workflow = "Workflow 1" if 'content/modules/ROOT/nav.adoc' in repo1_files else "Workflow 3"
        repo2_workflow = "Workflow 1" if 'content/modules/ROOT/nav.adoc' in repo2_files else "Workflow 3"
        
        print(f"{repo1_name}:")
        print(f"  - Likely created with: {repo1_workflow}")
        print(f"  - Files: {len(repo1_files)}, Directories: {len(repo1_dirs)}")
        print(f"  - Structure: {'Antora/Showroom' if repo1_workflow == 'Workflow 1' else 'Legacy/Original'}")
        
        print(f"\n{repo2_name}:")
        print(f"  - Likely created with: {repo2_workflow}")
        print(f"  - Files: {len(repo2_files)}, Directories: {len(repo2_dirs)}")
        print(f"  - Structure: {'Antora/Showroom' if repo2_workflow == 'Workflow 1' else 'Legacy/Original'}")
        
        if repo1_workflow != repo2_workflow:
            print(f"\n🎯 WORKFLOW DIFFERENCE DETECTED!")
            print(f"This comparison shows the difference between ADR-0001 workflows:")
            print(f"  - {repo1_name}: {repo1_workflow} (showroom template vs original repository)")
            print(f"  - {repo2_name}: {repo2_workflow} (showroom template vs original repository)")
        
    except Exception as e:
        print(f"❌ Comparison error: {str(e)}")

def main():
    """Main execution"""
    parser = argparse.ArgumentParser(description='Compare workshop repositories')
    parser.add_argument('--list-repos', action='store_true', help='List all workshop repositories')
    parser.add_argument('repo1', nargs='?', help='First repository name')
    parser.add_argument('repo2', nargs='?', help='Second repository name')
    
    args = parser.parse_args()
    
    setup_environment()
    
    if args.list_repos:
        list_workshop_repositories()
    elif args.repo1 and args.repo2:
        compare_repositories(args.repo1, args.repo2)
    else:
        print("Usage:")
        print("  python scripts/compare_workshop_repositories.py <repo1> <repo2>")
        print("  python scripts/compare_workshop_repositories.py --list-repos")
        sys.exit(1)

if __name__ == "__main__":
    main()
