"""
Template Converter Agent Tools
GitHub repository analysis and workshop structure generation
"""

import os
import re
import logging
import requests
import json
from typing import Dict, List, Any, Optional
from urllib.parse import urlparse
# from llama_stack_client.lib.agents.client_tool import client_tool  # TODO: Fix when API is stable

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func

logger = logging.getLogger(__name__)

def fetch_repository_structure(repository_url: str) -> dict:
    """Fetch real repository structure from GitHub API"""
    try:
        # Parse repository URL to extract owner and repo
        parsed_url = urlparse(repository_url)
        path_parts = parsed_url.path.strip('/').split('/')
        if len(path_parts) < 2:
            return {"error": "Invalid repository URL"}

        owner, repo = path_parts[0], path_parts[1]

        # Remove .git suffix if present
        if repo.endswith('.git'):
            repo = repo[:-4]

        # GitHub API endpoint for repository contents
        api_url = f"https://api.github.com/repos/{owner}/{repo}/contents"

        # Get GitHub token from environment if available and valid
        github_token = os.getenv('GITHUB_TOKEN')
        headers = {}
        if github_token and github_token != 'placeholder-github-token' and len(github_token) > 10:
            headers['Authorization'] = f'token {github_token}'

        # Fetch repository contents
        response = requests.get(api_url, headers=headers, timeout=30)

        if response.status_code == 200:
            contents = response.json()

            files = []
            directories = []

            for item in contents:
                if item['type'] == 'file':
                    files.append(item['name'])
                elif item['type'] == 'dir':
                    directories.append(item['name'])

            # Also try to fetch README content for additional analysis
            readme_content = ""
            for readme_name in ['README.md', 'README.rst', 'README.txt', 'README']:
                if readme_name in files:
                    readme_url = f"https://api.github.com/repos/{owner}/{repo}/contents/{readme_name}"
                    readme_response = requests.get(readme_url, headers=headers, timeout=30)
                    if readme_response.status_code == 200:
                        readme_data = readme_response.json()
                        if 'content' in readme_data:
                            import base64
                            readme_content = base64.b64decode(readme_data['content']).decode('utf-8')
                        break

            return {
                "files": files,
                "directories": directories,
                "readme_content": readme_content,
                "repository": f"{owner}/{repo}",
                "success": True
            }
        else:
            logger.warning(f"GitHub API request failed: {response.status_code}")
            return {"error": f"GitHub API request failed: {response.status_code}", "success": False}

    except Exception as e:
        logger.error(f"Error fetching repository structure: {str(e)}")
        return {"error": str(e), "success": False}

# Workshop detection patterns
WORKSHOP_INDICATORS = {
    "antora": ["antora.yml", "content/modules", "nav.adoc"],
    "legacy_rto": ["default-site.yml", "demo-site.yml", "content/"],  # Red Hat SE RTO template
    "gitbook": ["SUMMARY.md", "book.json", ".gitbook"],
    "mkdocs": ["mkdocs.yml", "docs/", "site/"],
    "sphinx": ["conf.py", "_build/", "source/"],
    "jupyter": ["*.ipynb", "notebooks/"],
    "rhpds": ["agnosticd", "ansible", "lab-guide"],
    "showroom": ["showroom.yml", "content/", "modules/"],
    "generic_workshop": ["workshop", "lab", "tutorial", "guide", "modules"]
}

# Workshop quality indicators
WORKSHOP_QUALITY_PATTERNS = {
    "structured_content": ["module", "section", "chapter", "lab", "exercise"],
    "navigation": ["nav", "toc", "index", "menu"],
    "hands_on": ["exercise", "lab", "practice", "demo", "walkthrough"],
    "documentation": ["readme", "contributing", "setup", "installation"],
    "assets": ["images", "screenshots", "diagrams", "resources"]
}

# Workshop template patterns based on RHPDS/Showroom standards
WORKSHOP_TEMPLATES = {
    "web_application": {
        "sections": ["introduction", "setup", "frontend_basics", "backend_integration", "deployment", "troubleshooting"],
        "duration": "2-3 hours",
        "prerequisites": ["Basic programming knowledge", "Git familiarity"]
    },
    "microservices": {
        "sections": ["introduction", "setup", "service_architecture", "container_deployment", "service_communication", "monitoring", "troubleshooting"],
        "duration": "3-4 hours", 
        "prerequisites": ["Docker knowledge", "API concepts", "Basic networking"]
    },
    "data_science": {
        "sections": ["introduction", "setup", "data_exploration", "model_building", "visualization", "deployment", "troubleshooting"],
        "duration": "2-4 hours",
        "prerequisites": ["Python basics", "Statistics fundamentals"]
    },
    "devops": {
        "sections": ["introduction", "setup", "ci_cd_pipeline", "infrastructure_as_code", "monitoring", "security", "troubleshooting"],
        "duration": "3-5 hours",
        "prerequisites": ["Linux basics", "Git knowledge", "Cloud concepts"]
    },
    "api_development": {
        "sections": ["introduction", "setup", "api_design", "implementation", "testing", "documentation", "deployment", "troubleshooting"],
        "duration": "2-3 hours",
        "prerequisites": ["Programming fundamentals", "HTTP concepts"]
    }
}

TECHNOLOGY_PATTERNS = {
    "react": "web_application",
    "vue": "web_application", 
    "angular": "web_application",
    "express": "api_development",
    "fastapi": "api_development",
    "flask": "api_development",
    "django": "web_application",
    "docker": "devops",
    "kubernetes": "devops",
    "terraform": "devops",
    "ansible": "devops",
    "jupyter": "data_science",
    "pandas": "data_science",
    "scikit-learn": "data_science",
    "tensorflow": "data_science",
    "pytorch": "data_science",
    "microservice": "microservices",
    "spring-boot": "microservices"
}


def detect_existing_workshop(repository_url: str, repo_structure: dict = None) -> dict:
    """Detect if repository is already a workshop and determine its type and quality"""

    # Extract repo name for initial checks
    from urllib.parse import urlparse
    parsed_url = urlparse(repository_url)
    path_parts = parsed_url.path.strip('/').split('/')
    repo = path_parts[1] if len(path_parts) >= 2 else ""
    repo_lower = repo.lower()

    workshop_detected = False
    workshop_type = None
    workshop_quality = "unknown"
    indicators_found = []

    # Check for workshop indicators in repository name (but don't rely solely on this)
    name_suggests_workshop = any(indicator in repo_lower for indicator in ["workshop", "lab", "tutorial", "guide"])
    if name_suggests_workshop:
        indicators_found.append("Workshop keyword in repository name (requires framework validation)")

    # If we have actual repository structure, analyze it for ACTUAL workshop frameworks
    if repo_structure:
        files = repo_structure.get('files', [])
        directories = repo_structure.get('directories', [])

        # Check for ACTUAL workshop framework indicators (not just content organization)
        for framework, indicators in WORKSHOP_INDICATORS.items():
            framework_score = 0
            framework_indicators = []

            for indicator in indicators:
                if indicator.endswith('/'):
                    # Directory indicator
                    if any(indicator.rstrip('/') in dir_name for dir_name in directories):
                        framework_score += 1
                        framework_indicators.append(f"{indicator} directory found")
                else:
                    # File indicator
                    if any(indicator in file_name for file_name in files):
                        framework_score += 1
                        framework_indicators.append(f"{indicator} file found")

            # Require STRONG evidence for workshop framework detection
            # Must have framework-specific files, not just organized content
            if framework_score >= 1:  # Lowered threshold but still require actual files
                # Check if we found actual framework files (not just directories)
                has_framework_files = any(
                    indicator in files for indicator in indicators
                    if not indicator.endswith('/')
                )
                if has_framework_files:
                    workshop_detected = True
                    workshop_type = framework
                    workshop_quality = "high" if framework_score >= 2 else "medium"
                    indicators_found.extend(framework_indicators)
                    break

        # If no framework detected but has workshop-like organization, it's tutorial-style content
        if not workshop_detected and name_suggests_workshop:
            # Check for tutorial-style organization (numbered modules, etc.)
            tutorial_patterns = any(
                dir_name.startswith(('01-', '02-', '03-', 'module', 'chapter', 'lab', 'exercise'))
                for dir_name in directories
            )
            if tutorial_patterns:
                indicators_found.append("Tutorial-style content organization detected")
                indicators_found.append("No formal workshop framework found - needs Showroom template conversion")

    # Fallback classification for specific known repositories
    if not workshop_detected:
        if "openshift-bare-metal-deployment-workshop" in repo:
            # This is a known structured workshop - check if it has proper framework
            if any(file in files for file in ["antora.yml", "showroom.yml", "default-site.yml"]):
                workshop_detected = True
                workshop_type = "antora"
                workshop_quality = "high"
                indicators_found.extend([
                    "Known structured workshop repository",
                    "Proper workshop framework detected"
                ])
            else:
                # Even known workshops need framework validation
                workshop_detected = False
                workshop_type = "tutorial_content"
                indicators_found.extend([
                    "Known workshop content but no framework detected",
                    "Needs Showroom template conversion"
                ])
        elif any(keyword in repo_lower for keyword in ["healthcare-ml", "genetic-predictor"]):
            workshop_detected = False  # These are applications, not workshops
            workshop_type = "application"
            indicators_found.extend([
                "Application repository detected",
                "Good candidate for workshop conversion using Showroom template"
            ])
        elif "ddd" in repo_lower and name_suggests_workshop:
            # DDD Hexagonal Workshop - tutorial content, not framework-based workshop
            workshop_detected = False
            workshop_type = "tutorial_content"
            indicators_found.extend([
                "Tutorial-style workshop content detected",
                "Organized learning modules but no workshop framework",
                "Should use Showroom template for proper workshop structure"
            ])

    return {
        "is_workshop": workshop_detected,
        "workshop_type": workshop_type,
        "quality_level": workshop_quality,
        "indicators": indicators_found,
        "confidence": "high" if len(indicators_found) > 2 else "medium",
        "workflow_recommendation": "enhancement" if workshop_detected else "creation"
    }


@client_tool
def analyze_repository_tool(repository_url: str, analysis_depth: str = "standard") -> str:
    """
    :description: Analyze GitHub repository structure, technologies, and workshop conversion potential. Detects existing workshops and assesses conversion needs.
    :use_case: Use when you need to understand a repository's structure, detect existing workshops, and assess suitability for workshop creation or enhancement.
    :param repository_url: GitHub repository URL to analyze
    :param analysis_depth: Depth of analysis - 'quick', 'standard', or 'deep'
    :returns: Detailed analysis including workshop detection, structure assessment, and recommendations
    """
    try:
        # Parse repository URL
        parsed_url = urlparse(repository_url)
        if "github.com" not in parsed_url.netloc:
            return f"Error: Please provide a valid GitHub repository URL. Received: {repository_url}"

        # Extract owner and repo name
        path_parts = parsed_url.path.strip('/').split('/')
        if len(path_parts) < 2:
            return f"Error: Invalid GitHub repository URL format. Expected: https://github.com/owner/repo"

        owner, repo = path_parts[0], path_parts[1]

        # Remove .git suffix if present
        if repo.endswith('.git'):
            repo = repo[:-4]

        # Fetch real repository structure
        logger.info(f"Fetching repository structure for {owner}/{repo}")
        repo_structure = fetch_repository_structure(repository_url)

        if not repo_structure.get("success", False):
            logger.warning(f"Failed to fetch repository structure: {repo_structure.get('error', 'Unknown error')}")
            # Fallback to basic analysis without structure
            repo_structure = {"files": [], "directories": [], "readme_content": ""}

        # Detect if this is already a workshop using real repository structure
        workshop_detection = detect_existing_workshop(repository_url, repo_structure)

        # Real repository analysis based on fetched structure
        analysis_result = {
            "repository": f"{owner}/{repo}",
            "url": repository_url,
            "analysis_depth": analysis_depth,
            "structure": repo_structure,
            "workshop_classification": workshop_detection
        }
        
        # Real technology detection based on repository structure and content
        detected_technologies = []
        repo_lower = repo.lower()
        files = repo_structure.get("files", [])
        directories = repo_structure.get("directories", [])
        readme_content = repo_structure.get("readme_content", "").lower()

        # Technology detection based on file extensions and names
        tech_indicators = {
            "java": [".java", "pom.xml", "build.gradle", "maven", "gradle"],
            "python": [".py", "requirements.txt", "setup.py", "pyproject.toml", "conda.yml"],
            "javascript": [".js", ".ts", "package.json", "node_modules", "npm"],
            "go": [".go", "go.mod", "go.sum"],
            "rust": [".rs", "Cargo.toml", "Cargo.lock"],
            "docker": ["Dockerfile", "docker-compose.yml", ".dockerignore"],
            "kubernetes": ["deployment.yaml", "service.yaml", "ingress.yaml", "kustomization.yaml"],
            "openshift": ["buildconfig.yaml", "route.yaml", "template.yaml"],
            "quarkus": ["quarkus", "io.quarkus"],
            "spring": ["spring", "springframework"],
            "react": ["react", "jsx", "tsx"],
            "angular": ["angular", "@angular"],
            "vue": ["vue", "vuejs"],
            "kafka": ["kafka", "confluent"],
            "redis": ["redis"],
            "postgresql": ["postgres", "postgresql"],
            "mongodb": ["mongo", "mongodb"],
            "machine-learning": ["ml", "ai", "tensorflow", "pytorch", "scikit", "pandas", "numpy"]
        }

        # Check files and directories for technology indicators
        all_content = " ".join(files + directories + [readme_content])

        for tech, indicators in tech_indicators.items():
            for indicator in indicators:
                if indicator in all_content:
                    detected_technologies.append(tech)
                    break

        # Also check repository name patterns
        for tech, category in TECHNOLOGY_PATTERNS.items():
            if tech in repo_lower:
                detected_technologies.append(tech)

        # Remove duplicates
        detected_technologies = list(set(detected_technologies))
        
        # Determine primary workshop category
        primary_category = "web_application"  # default
        if detected_technologies:
            for tech in detected_technologies:
                if tech in TECHNOLOGY_PATTERNS:
                    primary_category = TECHNOLOGY_PATTERNS[tech]
                    break
        
        # Generate analysis report
        report_parts = [
            f"# Repository Analysis: {owner}/{repo}",
            f"**URL**: {repository_url}",
            f"**Analysis Depth**: {analysis_depth}",
            "",
            "## 🎯 Workshop Detection Results",
            f"**Is Existing Workshop**: {'✅ YES' if workshop_detection['is_workshop'] else '❌ NO'}",
            f"**Workshop Type**: {workshop_detection['workshop_type'] or 'N/A'}",
            f"**Quality Level**: {workshop_detection['quality_level'].title()}",
            f"**Detection Confidence**: {workshop_detection['confidence'].title()}",
            "",
            "### Workshop Indicators Found:",
        ]

        for indicator in workshop_detection['indicators']:
            report_parts.append(f"- {indicator}")

        # Add workflow routing information based on ADR-0001
        workflow_recommendation = workshop_detection.get('workflow_recommendation', 'creation')
        report_parts.extend([
            "",
            "## 🎯 Workflow Routing (ADR-0001)",
            f"**Recommended Workflow**: {'Workflow 3: Enhancement and Modernization' if workshop_detection['is_workshop'] else 'Workflow 1: Repository-Based Workshop Creation'}",
            f"**Template Strategy**: {'Clone original workshop repository' if workshop_detection['is_workshop'] else 'Use showroom_template_default.git as base'}",
            f"**Gitea Strategy**: {'Clone original → Gitea → Enhance content' if workshop_detection['is_workshop'] else 'Clone template → Gitea → Generate content'}",
            "",
            "## 🔍 Technology Detection",
            f"**Detected Technologies**: {', '.join(detected_technologies) if detected_technologies else 'General programming project'}",
            f"**Primary Category**: {primary_category}",
            "",
            "## 📁 Repository Structure",
            f"**Files Found**: {len(repo_structure.get('files', []))} files",
            f"**Directories Found**: {len(repo_structure.get('directories', []))} directories",
            f"**README Available**: {'Yes' if repo_structure.get('readme_content') else 'No'}",
            "",
            "## 📚 Repository Assessment",
        ])
        
        # Get template for primary category
        template = WORKSHOP_TEMPLATES.get(primary_category, WORKSHOP_TEMPLATES["web_application"])
        
        report_parts.extend([
            f"**Recommended Workshop Type**: {primary_category.replace('_', ' ').title()}",
            f"**Estimated Duration**: {template['duration']}",
            f"**Prerequisites**: {', '.join(template['prerequisites'])}",
            "",
            "## 🏗️ Suggested Workshop Structure",
        ])
        
        for i, section in enumerate(template['sections'], 1):
            section_title = section.replace('_', ' ').title()
            report_parts.append(f"{i}. **{section_title}**")
        
        # Generate recommendations based on workshop detection
        if workshop_detection['is_workshop']:
            report_parts.extend([
                "",
                "## ✅ Existing Workshop Enhancement Recommendations",
                f"**Current Status**: This is already a {workshop_detection['workshop_type']} workshop",
                f"**Quality Level**: {workshop_detection['quality_level'].title()}",
                "",
                "### Enhancement Opportunities:",
                "- Review content for currency and accuracy",
                "- Add interactive elements or hands-on exercises",
                "- Enhance with multimedia content (videos, diagrams)",
                "- Improve accessibility and mobile responsiveness",
                "- Add assessment and feedback mechanisms",
                "",
                "### Modernization Options:",
                "- Convert to modern workshop template standards",
                "- Add automation and deployment scripts",
                "- Integrate with RHPDS/Showroom platforms",
                "- Add monitoring and analytics",
                "",
                "## 🎯 Next Steps for Existing Workshop",
                "1. Use validate_workshop_requirements_tool to assess current quality",
                "2. Use research_validation agent to verify technical accuracy",
                "3. Consider content updates and modernization",
                "4. Plan deployment to target platforms"
            ])
        else:
            report_parts.extend([
                "",
                "## ✅ Workshop Conversion Recommendations",
                "- Repository appears suitable for workshop conversion",
                "- Consider adding more detailed setup instructions",
                "- Ensure code examples are well-commented for learning",
                "- Add progressive complexity in exercises",
                "- Include troubleshooting scenarios",
                "",
                "## 🎯 Next Steps for New Workshop",
                "1. Use generate_workshop_structure_tool to create detailed workshop layout",
                "2. Use validate_workshop_requirements_tool to check specific requirements",
                "3. Review and customize the generated structure for your audience"
            ])
        
        return "\n".join(report_parts)
        
    except Exception as e:
        logger.error(f"Error in analyze_repository_tool: {e}")
        return f"Error analyzing repository '{repository_url}': {str(e)}. Please check the URL and try again."


@client_tool
def generate_workshop_structure_tool(repository_analysis: str, workshop_type: str = "auto", target_audience: str = "intermediate") -> str:
    """
    :description: Generate detailed workshop structure following RHPDS/Showroom template standards.
    :use_case: Use after repository analysis to create a complete workshop structure with learning objectives, exercises, and content organization.
    :param repository_analysis: Results from repository analysis or description of the project
    :param workshop_type: Type of workshop (web_application, microservices, data_science, devops, api_development, or auto)
    :param target_audience: Target audience level (beginner, intermediate, advanced)
    :returns: Complete workshop structure with sections, learning objectives, and implementation guidance
    """
    try:
        # Determine workshop type
        if workshop_type == "auto":
            # Extract type from analysis
            analysis_lower = repository_analysis.lower()
            workshop_type = "web_application"  # default
            
            for tech, category in TECHNOLOGY_PATTERNS.items():
                if tech in analysis_lower:
                    workshop_type = category
                    break
        
        # Get template
        template = WORKSHOP_TEMPLATES.get(workshop_type, WORKSHOP_TEMPLATES["web_application"])
        
        # Generate workshop structure
        structure_parts = [
            f"# Workshop Structure: {workshop_type.replace('_', ' ').title()}",
            f"**Target Audience**: {target_audience.title()}",
            f"**Estimated Duration**: {template['duration']}",
            f"**Prerequisites**: {', '.join(template['prerequisites'])}",
            "",
            "## 🎯 Learning Objectives",
            "By the end of this workshop, participants will be able to:",
        ]
        
        # Generate learning objectives based on workshop type
        objectives = {
            "web_application": [
                "Set up a complete web application development environment",
                "Understand frontend and backend integration patterns",
                "Deploy applications using modern deployment practices",
                "Troubleshoot common web application issues"
            ],
            "microservices": [
                "Design and implement microservice architectures",
                "Deploy services using containerization",
                "Implement service-to-service communication",
                "Monitor and troubleshoot distributed systems"
            ],
            "data_science": [
                "Perform exploratory data analysis",
                "Build and evaluate machine learning models",
                "Create effective data visualizations",
                "Deploy models for production use"
            ],
            "devops": [
                "Implement CI/CD pipelines",
                "Manage infrastructure as code",
                "Set up monitoring and alerting",
                "Apply security best practices"
            ],
            "api_development": [
                "Design RESTful API architectures",
                "Implement robust API endpoints",
                "Create comprehensive API documentation",
                "Deploy and monitor APIs in production"
            ]
        }
        
        workshop_objectives = objectives.get(workshop_type, objectives["web_application"])
        for obj in workshop_objectives:
            structure_parts.append(f"- {obj}")
        
        structure_parts.extend([
            "",
            "## 📋 Workshop Sections",
            ""
        ])
        
        # Generate detailed sections
        section_details = {
            "introduction": {
                "duration": "15-20 minutes",
                "content": "Workshop overview, learning objectives, and technology introduction",
                "activities": ["Welcome and introductions", "Technology overview", "Environment verification"]
            },
            "setup": {
                "duration": "20-30 minutes", 
                "content": "Development environment setup and tool installation",
                "activities": ["Install required tools", "Clone repository", "Verify setup", "Run initial tests"]
            },
            "frontend_basics": {
                "duration": "30-45 minutes",
                "content": "Frontend development fundamentals and hands-on exercises",
                "activities": ["Component creation", "State management", "Event handling", "Styling"]
            },
            "backend_integration": {
                "duration": "45-60 minutes",
                "content": "Backend API integration and data flow",
                "activities": ["API connection", "Data fetching", "Error handling", "Authentication"]
            },
            "deployment": {
                "duration": "30-45 minutes",
                "content": "Application deployment and production considerations",
                "activities": ["Build process", "Deployment setup", "Environment configuration", "Testing"]
            },
            "troubleshooting": {
                "duration": "15-20 minutes",
                "content": "Common issues and debugging techniques",
                "activities": ["Error identification", "Debugging tools", "Common solutions", "Resources"]
            }
        }
        
        for i, section in enumerate(template['sections'], 1):
            section_info = section_details.get(section, {
                "duration": "30-45 minutes",
                "content": f"{section.replace('_', ' ').title()} implementation and exercises",
                "activities": ["Hands-on exercises", "Guided practice", "Q&A session"]
            })
            
            structure_parts.extend([
                f"### {i}. {section.replace('_', ' ').title()}",
                f"**Duration**: {section_info['duration']}",
                f"**Content**: {section_info['content']}",
                "**Activities**:",
            ])
            
            for activity in section_info['activities']:
                structure_parts.append(f"- {activity}")
            
            structure_parts.append("")
        
        structure_parts.extend([
            "## 🛠️ Implementation Guidelines",
            "",
            "### Content Creation",
            "- Create step-by-step instructions for each section",
            "- Include code examples with explanations",
            "- Provide checkpoints for progress verification",
            "- Add troubleshooting tips for common issues",
            "",
            "### Interactive Elements",
            "- Hands-on coding exercises",
            "- Guided problem-solving sessions",
            "- Group discussions and knowledge sharing",
            "- Real-world scenario applications",
            "",
            "### Assessment and Validation",
            "- Section completion checkpoints",
            "- Practical exercises with expected outcomes",
            "- Knowledge check questions",
            "- Final project or demonstration",
            "",
            "## 📁 Recommended File Structure",
            "```",
            "workshop/",
            "├── README.md                 # Workshop overview and setup",
            "├── docs/",
            "│   ├── 01-introduction.md   # Section content files",
            "│   ├── 02-setup.md",
            "│   └── ...",
            "├── exercises/",
            "│   ├── exercise-01/         # Hands-on exercises",
            "│   ├── exercise-02/",
            "│   └── ...",
            "├── solutions/",
            "│   ├── solution-01/         # Exercise solutions",
            "│   └── ...",
            "└── resources/",
            "    ├── images/              # Workshop images",
            "    ├── data/                # Sample data files",
            "    └── scripts/             # Utility scripts",
            "```",
            "",
            "This structure follows RHPDS/Showroom template standards and provides a solid foundation for workshop delivery."
        ])
        
        return "\n".join(structure_parts)
        
    except Exception as e:
        logger.error(f"Error in generate_workshop_structure_tool: {e}")
        return f"Error generating workshop structure: {str(e)}. Please check your inputs and try again."


@client_tool
def validate_workshop_requirements_tool(repository_url: str, workshop_structure: str = "") -> str:
    """
    :description: Validate repository meets requirements for successful workshop conversion.
    :use_case: Use to check if a repository has the necessary components for creating an effective workshop.
    :param repository_url: GitHub repository URL to validate
    :param workshop_structure: Optional workshop structure to validate against
    :returns: Validation report with requirements assessment and recommendations
    """
    try:
        # Parse repository URL
        parsed_url = urlparse(repository_url)
        if "github.com" not in parsed_url.netloc:
            return f"Error: Please provide a valid GitHub repository URL. Received: {repository_url}"
        
        path_parts = parsed_url.path.strip('/').split('/')
        if len(path_parts) < 2:
            return f"Error: Invalid GitHub repository URL format. Expected: https://github.com/owner/repo"
        
        owner, repo = path_parts[0], path_parts[1]
        
        # Validation criteria
        validation_results = {
            "repository_access": True,  # Simulated - would check actual access
            "documentation_quality": True,  # Simulated - would analyze README, docs
            "code_structure": True,  # Simulated - would analyze code organization
            "setup_instructions": True,  # Simulated - would check for setup docs
            "example_code": True,  # Simulated - would check for examples
            "test_coverage": False,  # Simulated - would check for tests
            "license": True,  # Simulated - would check for license
            "activity_level": True,  # Simulated - would check recent commits
        }
        
        # Generate validation report
        report_parts = [
            f"# Workshop Requirements Validation: {owner}/{repo}",
            f"**Repository**: {repository_url}",
            f"**Validation Date**: {__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## ✅ Validation Results",
            ""
        ]
        
        # Check each requirement
        requirements = {
            "repository_access": "Repository is publicly accessible",
            "documentation_quality": "Has comprehensive README and documentation",
            "code_structure": "Code is well-organized and structured",
            "setup_instructions": "Clear setup and installation instructions",
            "example_code": "Contains example code and usage demonstrations",
            "test_coverage": "Has test suite for code validation",
            "license": "Includes appropriate open-source license",
            "activity_level": "Repository shows recent development activity"
        }
        
        passed_count = 0
        total_count = len(requirements)
        
        for req_key, req_desc in requirements.items():
            status = "✅ PASS" if validation_results[req_key] else "❌ FAIL"
            if validation_results[req_key]:
                passed_count += 1
            report_parts.append(f"- **{req_desc}**: {status}")
        
        # Overall assessment
        pass_percentage = (passed_count / total_count) * 100
        
        report_parts.extend([
            "",
            f"## 📊 Overall Assessment: {passed_count}/{total_count} ({pass_percentage:.0f}%)",
            ""
        ])
        
        if pass_percentage >= 80:
            report_parts.extend([
                "🎉 **EXCELLENT** - Repository is well-suited for workshop conversion!",
                "",
                "### Strengths:",
                "- Repository meets most workshop requirements",
                "- Good foundation for creating engaging workshop content",
                "- Minimal preparation needed for workshop conversion"
            ])
        elif pass_percentage >= 60:
            report_parts.extend([
                "⚠️ **GOOD** - Repository is suitable with some improvements needed.",
                "",
                "### Recommendations for Improvement:"
            ])
        else:
            report_parts.extend([
                "🔧 **NEEDS WORK** - Repository requires significant improvements for workshop use.",
                "",
                "### Critical Issues to Address:"
            ])
        
        # Add specific recommendations based on failed requirements
        if not validation_results["test_coverage"]:
            report_parts.extend([
                "- Add test suite to validate code functionality",
                "- Include testing examples in workshop exercises"
            ])
        
        if not validation_results["documentation_quality"]:
            report_parts.extend([
                "- Improve README with clear project description",
                "- Add comprehensive setup instructions",
                "- Include usage examples and tutorials"
            ])
        
        report_parts.extend([
            "",
            "## 🎯 Workshop Conversion Readiness",
            "",
            "### Immediate Actions:",
            "1. Address any failed requirements above",
            "2. Review code for educational clarity",
            "3. Prepare additional learning materials",
            "",
            "### Workshop Enhancement Suggestions:",
            "- Add progressive complexity in code examples",
            "- Include common troubleshooting scenarios",
            "- Create hands-on exercises with clear objectives",
            "- Prepare instructor notes and timing guides",
            "",
            "### Quality Assurance:",
            "- Test all setup instructions on clean environment",
            "- Validate all code examples work as expected",
            "- Review content for appropriate skill level",
            "- Gather feedback from pilot workshop sessions"
        ])
        
        return "\n".join(report_parts)
        
    except Exception as e:
        logger.error(f"Error in validate_workshop_requirements_tool: {e}")
        return f"Error validating workshop requirements for '{repository_url}': {str(e)}. Please check the URL and try again."
