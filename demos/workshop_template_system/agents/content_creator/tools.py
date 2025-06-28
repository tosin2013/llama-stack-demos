"""
Content Creator Agent Tools
Original workshop creation from learning objectives and concepts
"""

import os
import logging
import json
import re
from typing import Dict, List, Any, Optional
from datetime import datetime
# from llama_stack_client.lib.agents.client_tool import client_tool  # TODO: Fix when API is stable

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func

logger = logging.getLogger(__name__)

@client_tool
def transform_repository_to_workshop_tool(repository_analysis: str, workshop_focus: str = "comprehensive", target_audience: str = "intermediate") -> str:
    """
    :description: Transform repository analysis into structured workshop content based on actual repository structure and content.
    :use_case: Use when you have repository analysis from Template Converter Agent and need to create workshop materials from real code/content.
    :param repository_analysis: JSON string or text analysis from Template Converter Agent containing repository structure and content
    :param workshop_focus: Focus area for the workshop (comprehensive, technology-specific, hands-on, conceptual)
    :param target_audience: Target audience level (beginner, intermediate, advanced)
    :returns: Structured workshop content based on actual repository analysis
    """
    try:
        # Parse repository analysis
        if isinstance(repository_analysis, str):
            # Try to extract structured data from analysis text
            repo_data = parse_repository_analysis(repository_analysis)
        else:
            repo_data = repository_analysis

        # Extract key information
        repo_name = repo_data.get('repository', 'Unknown Repository')
        technologies = repo_data.get('detected_technologies', [])
        workflow_type = repo_data.get('workflow_recommendation', 'creation')
        repo_structure = repo_data.get('structure', {})
        readme_content = repo_structure.get('readme_content', '')

        # Generate workshop content based on repository analysis
        workshop_parts = [
            f"# Workshop: {repo_name.replace('/', ' - ').title()}",
            f"**Generated from Repository Analysis**",
            f"**Workshop Focus**: {workshop_focus.title()}",
            f"**Target Audience**: {target_audience.title()}",
            f"**Technologies**: {', '.join(technologies) if technologies else 'General'}",
            f"**Creation Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## 🎯 Workshop Overview",
            "",
        ]

        # Extract learning objectives from README content
        learning_objectives = extract_learning_objectives_from_readme(readme_content, technologies)
        workshop_parts.extend([
            "### Learning Objectives",
            "By the end of this workshop, participants will be able to:",
        ])
        for objective in learning_objectives:
            workshop_parts.append(f"- {objective}")

        workshop_parts.extend([
            "",
            "### Prerequisites",
            f"- Basic understanding of {technologies[0] if technologies else 'programming'}",
            f"- Familiarity with command line tools",
            f"- Git and GitHub knowledge",
            "",
            "## 📚 Workshop Modules",
            "",
        ])

        # Generate modules based on repository structure
        modules = generate_modules_from_structure(repo_structure, technologies, target_audience)
        for i, module in enumerate(modules, 1):
            workshop_parts.extend([
                f"### Module {i}: {module['title']}",
                f"**Duration**: {module['duration']}",
                f"**Type**: {module['type']}",
                "",
                f"#### Overview",
                module['overview'],
                "",
                f"#### Learning Outcomes",
            ])
            for outcome in module['outcomes']:
                workshop_parts.append(f"- {outcome}")

            workshop_parts.extend([
                "",
                f"#### Key Activities",
            ])
            for activity in module['activities']:
                workshop_parts.append(f"- {activity}")

            workshop_parts.append("")

        # Add hands-on exercises based on repository content
        exercises = generate_exercises_from_repository(repo_structure, technologies)
        workshop_parts.extend([
            "## 🔧 Hands-On Exercises",
            "",
        ])

        for i, exercise in enumerate(exercises, 1):
            workshop_parts.extend([
                f"### Exercise {i}: {exercise['title']}",
                f"**Estimated Time**: {exercise['time']}",
                f"**Difficulty**: {exercise['difficulty']}",
                "",
                f"#### Objective",
                exercise['objective'],
                "",
                f"#### Instructions",
                exercise['instructions'],
                "",
                f"#### Expected Outcome",
                exercise['outcome'],
                "",
            ])

        # Add assessment and wrap-up
        workshop_parts.extend([
            "## 📊 Assessment and Validation",
            "",
            "### Knowledge Check",
            "- Review key concepts covered in each module",
            "- Validate hands-on exercise completion",
            "- Discuss real-world applications",
            "",
            "### Next Steps",
            "- Explore advanced features and configurations",
            "- Apply concepts to your own projects",
            "- Join community discussions and contribute back",
            "",
            "## 📖 Additional Resources",
            "",
            f"- **Source Repository**: {repo_data.get('url', 'N/A')}",
            "- **Official Documentation**: [Links to be added based on technologies]",
            "- **Community Resources**: [Links to be added]",
            "",
            "---",
            f"*Workshop content generated from repository analysis on {datetime.now().strftime('%Y-%m-%d')}*"
        ])

        return "\n".join(workshop_parts)

    except Exception as e:
        logger.error(f"Error in transform_repository_to_workshop_tool: {e}")
        return f"Error transforming repository to workshop: {str(e)}. Please check the repository analysis input."

def parse_repository_analysis(analysis_text: str) -> dict:
    """Parse repository analysis text to extract structured data"""
    try:
        # Try to parse as JSON first
        if analysis_text.strip().startswith('{'):
            return json.loads(analysis_text)

        # Parse text-based analysis
        repo_data = {
            'repository': 'Unknown',
            'detected_technologies': [],
            'workflow_recommendation': 'creation',
            'structure': {'files': [], 'directories': [], 'readme_content': ''}
        }

        # Extract repository name
        if "Repository Analysis:" in analysis_text:
            lines = analysis_text.split('\n')
            for line in lines:
                if "Repository Analysis:" in line:
                    repo_data['repository'] = line.split(':')[1].strip()
                    break

        # Extract technologies
        if "Detected Technologies" in analysis_text:
            tech_line = ""
            lines = analysis_text.split('\n')
            for line in lines:
                if "Detected Technologies" in line and ":" in line:
                    tech_line = line.split(':')[1].strip()
                    break

            if tech_line and tech_line != "General programming project":
                repo_data['detected_technologies'] = [tech.strip() for tech in tech_line.split(',')]

        # Extract workflow recommendation
        if "Workflow 1" in analysis_text:
            repo_data['workflow_recommendation'] = 'creation'
        elif "Workflow 3" in analysis_text:
            repo_data['workflow_recommendation'] = 'enhancement'

        return repo_data

    except Exception as e:
        logger.warning(f"Error parsing repository analysis: {e}")
        return {'repository': 'Unknown', 'detected_technologies': [], 'workflow_recommendation': 'creation', 'structure': {}}

def extract_learning_objectives_from_readme(readme_content: str, technologies: list) -> list:
    """Extract or generate learning objectives based on README content and technologies"""
    objectives = []

    # Default objectives based on technologies
    tech_objectives = {
        'java': "Understand Java application architecture and best practices",
        'python': "Implement Python applications with proper structure and dependencies",
        'javascript': "Build modern JavaScript applications with current frameworks",
        'quarkus': "Develop cloud-native applications using Quarkus framework",
        'kafka': "Implement event-driven architectures with Apache Kafka",
        'kubernetes': "Deploy and manage applications in Kubernetes environments",
        'openshift': "Utilize OpenShift for enterprise container orchestration",
        'machine-learning': "Apply machine learning concepts to real-world problems",
        'docker': "Containerize applications using Docker best practices"
    }

    # Add technology-specific objectives
    for tech in technologies:
        if tech in tech_objectives:
            objectives.append(tech_objectives[tech])

    # Try to extract objectives from README content
    if readme_content:
        readme_lower = readme_content.lower()

        # Look for common objective patterns
        if 'learn' in readme_lower or 'tutorial' in readme_lower:
            objectives.append("Follow step-by-step implementation guidance")

        if 'deploy' in readme_lower or 'deployment' in readme_lower:
            objectives.append("Deploy the application to target environments")

        if 'test' in readme_lower or 'testing' in readme_lower:
            objectives.append("Implement and run comprehensive tests")

        if 'api' in readme_lower:
            objectives.append("Understand API design and integration patterns")

    # Ensure we have at least some objectives
    if not objectives:
        objectives = [
            "Understand the application architecture and design patterns",
            "Set up the development environment and dependencies",
            "Implement core functionality following best practices",
            "Deploy and validate the application"
        ]

    return objectives[:6]  # Limit to 6 objectives

def generate_modules_from_structure(repo_structure: dict, technologies: list, audience_level: str) -> list:
    """Generate workshop modules based on repository structure and technologies"""
    modules = []

    # Base modules for any workshop
    modules.append({
        'title': 'Introduction and Environment Setup',
        'duration': '30 minutes',
        'type': 'Setup',
        'overview': 'Get familiar with the project structure and set up the development environment.',
        'outcomes': [
            'Understand the project architecture and components',
            'Set up local development environment',
            'Clone and configure the repository',
            'Verify all dependencies are installed'
        ],
        'activities': [
            'Repository walkthrough and structure explanation',
            'Environment setup and dependency installation',
            'Initial project build and verification',
            'IDE/editor configuration and setup'
        ]
    })

    # Technology-specific modules
    if 'java' in technologies or 'quarkus' in technologies:
        modules.append({
            'title': 'Java/Quarkus Application Development',
            'duration': '45 minutes',
            'type': 'Development',
            'overview': 'Explore Java application patterns and Quarkus framework features.',
            'outcomes': [
                'Understand Java application architecture',
                'Work with Quarkus development mode',
                'Implement REST endpoints and services',
                'Configure application properties'
            ],
            'activities': [
                'Code walkthrough and architecture review',
                'Live coding session with Quarkus dev mode',
                'REST API implementation and testing',
                'Configuration and dependency injection'
            ]
        })

    if 'kafka' in technologies:
        modules.append({
            'title': 'Event-Driven Architecture with Kafka',
            'duration': '40 minutes',
            'type': 'Integration',
            'overview': 'Implement event streaming and messaging patterns using Apache Kafka.',
            'outcomes': [
                'Understand event-driven architecture principles',
                'Configure Kafka producers and consumers',
                'Implement message serialization and deserialization',
                'Handle error scenarios and retries'
            ],
            'activities': [
                'Kafka setup and topic configuration',
                'Producer implementation and message publishing',
                'Consumer implementation and message processing',
                'Error handling and monitoring'
            ]
        })

    if 'machine-learning' in technologies:
        modules.append({
            'title': 'Machine Learning Integration',
            'duration': '50 minutes',
            'type': 'Advanced',
            'overview': 'Integrate machine learning models and implement inference pipelines.',
            'outcomes': [
                'Understand ML model integration patterns',
                'Implement model inference endpoints',
                'Handle data preprocessing and validation',
                'Monitor model performance and accuracy'
            ],
            'activities': [
                'Model loading and initialization',
                'Data preprocessing pipeline implementation',
                'Inference endpoint development',
                'Performance monitoring and logging'
            ]
        })

    # Deployment module
    if 'kubernetes' in technologies or 'openshift' in technologies or 'docker' in technologies:
        modules.append({
            'title': 'Containerization and Deployment',
            'duration': '35 minutes',
            'type': 'Deployment',
            'overview': 'Package the application in containers and deploy to target environments.',
            'outcomes': [
                'Create optimized container images',
                'Configure deployment manifests',
                'Deploy to Kubernetes/OpenShift',
                'Verify application health and scaling'
            ],
            'activities': [
                'Dockerfile creation and optimization',
                'Kubernetes manifest configuration',
                'Deployment and service setup',
                'Health checks and monitoring configuration'
            ]
        })

    # Wrap-up module
    modules.append({
        'title': 'Testing, Monitoring, and Best Practices',
        'duration': '25 minutes',
        'type': 'Validation',
        'overview': 'Implement testing strategies and establish monitoring for production readiness.',
        'outcomes': [
            'Implement comprehensive testing strategies',
            'Set up monitoring and observability',
            'Apply security best practices',
            'Plan for production deployment'
        ],
        'activities': [
            'Unit and integration testing implementation',
            'Monitoring and metrics configuration',
            'Security scanning and hardening',
            'Production readiness checklist review'
        ]
    })

    return modules

def generate_exercises_from_repository(repo_structure: dict, technologies: list) -> list:
    """Generate hands-on exercises based on repository content and technologies"""
    exercises = []

    # Exercise 1: Environment Setup and Exploration
    exercises.append({
        'title': 'Environment Setup and Code Exploration',
        'time': '15 minutes',
        'difficulty': 'Beginner',
        'objective': 'Set up the development environment and explore the codebase structure.',
        'instructions': '''
1. Clone the repository to your local machine
2. Install all required dependencies
3. Explore the project structure and identify key components
4. Run the application in development mode
5. Access the application and verify it's working correctly
        '''.strip(),
        'outcome': 'Successfully running application with understanding of project structure.'
    })

    # Exercise 2: Core Functionality Implementation
    exercises.append({
        'title': 'Core Feature Implementation',
        'time': '25 minutes',
        'difficulty': 'Intermediate',
        'objective': 'Implement or modify core application features following established patterns.',
        'instructions': '''
1. Identify the main business logic components
2. Implement a new feature or modify existing functionality
3. Follow the established coding patterns and conventions
4. Add appropriate error handling and validation
5. Test the implementation thoroughly
        '''.strip(),
        'outcome': 'Working feature implementation that follows project conventions.'
    })

    # Technology-specific exercises
    if 'kafka' in technologies:
        exercises.append({
            'title': 'Event Streaming Implementation',
            'time': '20 minutes',
            'difficulty': 'Intermediate',
            'objective': 'Implement event producers and consumers for asynchronous communication.',
            'instructions': '''
1. Configure Kafka topics and partitions
2. Implement an event producer for business events
3. Create a consumer to process events asynchronously
4. Add proper error handling and retry logic
5. Test the event flow end-to-end
            '''.strip(),
            'outcome': 'Functional event streaming pipeline with proper error handling.'
        })

    if 'machine-learning' in technologies:
        exercises.append({
            'title': 'ML Model Integration',
            'time': '30 minutes',
            'difficulty': 'Advanced',
            'objective': 'Integrate machine learning models for real-time inference.',
            'instructions': '''
1. Load and initialize the ML model
2. Implement data preprocessing pipeline
3. Create inference endpoints with proper validation
4. Add model performance monitoring
5. Test with sample data and validate results
            '''.strip(),
            'outcome': 'Working ML inference pipeline with monitoring and validation.'
        })

    # Exercise 4: Deployment and Validation
    exercises.append({
        'title': 'Containerization and Deployment',
        'time': '20 minutes',
        'difficulty': 'Intermediate',
        'objective': 'Package the application and deploy it to a container environment.',
        'instructions': '''
1. Create or review the Dockerfile for the application
2. Build the container image with proper tags
3. Configure deployment manifests for Kubernetes/OpenShift
4. Deploy the application to the target environment
5. Verify deployment health and functionality
        '''.strip(),
        'outcome': 'Successfully deployed application running in containerized environment.'
    })

    return exercises

# Workshop types and their characteristics
WORKSHOP_TYPES = {
    "conceptual": {
        "focus": "Understanding principles and theory",
        "activities": ["discussions", "case_studies", "concept_mapping", "presentations"],
        "duration": "2-4 hours",
        "materials": ["slides", "handouts", "reference_guides"]
    },
    "hands_on": {
        "focus": "Practical skills and implementation",
        "activities": ["coding", "configuration", "deployment", "troubleshooting"],
        "duration": "3-6 hours", 
        "materials": ["lab_environments", "code_templates", "step_by_step_guides"]
    },
    "hybrid": {
        "focus": "Theory combined with practical application",
        "activities": ["lectures", "demos", "labs", "group_work"],
        "duration": "4-8 hours",
        "materials": ["slides", "lab_guides", "reference_materials", "exercises"]
    }
}

# Learning objective taxonomies (Bloom's taxonomy levels)
LEARNING_LEVELS = {
    "remember": {"verbs": ["list", "identify", "recall", "define"], "activities": ["flashcards", "quizzes", "definitions"]},
    "understand": {"verbs": ["explain", "describe", "summarize", "interpret"], "activities": ["discussions", "explanations", "examples"]},
    "apply": {"verbs": ["use", "implement", "execute", "demonstrate"], "activities": ["exercises", "simulations", "practice"]},
    "analyze": {"verbs": ["compare", "examine", "break down", "investigate"], "activities": ["case_studies", "debugging", "analysis"]},
    "evaluate": {"verbs": ["assess", "critique", "judge", "validate"], "activities": ["reviews", "assessments", "evaluations"]},
    "create": {"verbs": ["design", "build", "develop", "compose"], "activities": ["projects", "designs", "implementations"]}
}


@client_tool
def design_workshop_from_objectives_tool(learning_objectives: str, target_audience: str = "intermediate", workshop_type: str = "hybrid") -> str:
    """
    :description: Design complete workshop structures from learning objectives and educational goals.
    :use_case: Use to create workshop designs when you have clear learning objectives but no source repository.
    :param learning_objectives: Specific learning objectives or goals for the workshop
    :param target_audience: Target audience level and background (beginner, intermediate, advanced, mixed)
    :param workshop_type: Type of workshop (conceptual, hands_on, hybrid)
    :returns: Complete workshop design with structure, timeline, and implementation guidance
    """
    try:
        # Parse learning objectives
        objectives_list = [obj.strip() for obj in learning_objectives.split('\n') if obj.strip()]
        
        # Get workshop type configuration
        workshop_config = WORKSHOP_TYPES.get(workshop_type, WORKSHOP_TYPES["hybrid"])
        
        # Generate workshop design
        design_parts = [
            f"# Workshop Design: Learning Objectives-Based",
            f"**Target Audience**: {target_audience.title()}",
            f"**Workshop Type**: {workshop_type.title()}",
            f"**Focus**: {workshop_config['focus']}",
            f"**Estimated Duration**: {workshop_config['duration']}",
            f"**Design Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## 🎯 Learning Objectives Analysis",
            ""
        ]
        
        # Analyze learning objectives using Bloom's taxonomy
        objective_analysis = []
        for i, objective in enumerate(objectives_list, 1):
            obj_lower = objective.lower()
            
            # Determine learning level
            detected_level = "apply"  # default
            for level, config in LEARNING_LEVELS.items():
                if any(verb in obj_lower for verb in config["verbs"]):
                    detected_level = level
                    break
            
            objective_analysis.append({
                "number": i,
                "text": objective,
                "level": detected_level,
                "activities": LEARNING_LEVELS[detected_level]["activities"]
            })
            
            design_parts.extend([
                f"### Objective {i}: {detected_level.title()} Level",
                f"**Statement**: {objective}",
                f"**Bloom's Level**: {detected_level.title()}",
                f"**Suggested Activities**: {', '.join(LEARNING_LEVELS[detected_level]['activities'])}",
                ""
            ])
        
        # Generate workshop structure with sections
        design_parts.extend([
            "## 🏗️ Workshop Structure Design",
            "",
            "### Recommended Sections:",
            "1. **Introduction & Overview** (15-20 minutes)",
            "2. **Core Learning Modules** (based on objectives)",
            "3. **Integration & Practice** (hands-on activities)",
            "4. **Wrap-up & Next Steps** (10-15 minutes)",
            "",
            "## 📋 Implementation Guidance",
            "",
            "### Materials Needed:",
        ])
        
        for material in workshop_config['materials']:
            material_name = material.replace('_', ' ').title()
            design_parts.append(f"- {material_name}")
        
        design_parts.extend([
            "",
            "### Next Steps:",
            "1. Use create_original_content_tool for detailed content",
            "2. Use generate_exercises_tool for hands-on activities",
            "3. Coordinate with research_validation agent for accuracy",
            "",
            "---",
            f"*Workshop design based on {len(objectives_list)} learning objectives*"
        ])
        
        return "\n".join(design_parts)
        
    except Exception as e:
        logger.error(f"Error in design_workshop_from_objectives_tool: {e}")
        return f"Error designing workshop: {str(e)}. Please check your inputs and try again."


@client_tool
def create_original_content_tool(topic: str, content_type: str = "instructional", audience_level: str = "intermediate") -> str:
    """
    :description: Generate original workshop content for concepts, tools, cloud services, or theoretical topics.
    :use_case: Use to create workshop content when no source repository exists.
    :param topic: Topic or concept to create content for
    :param content_type: Type of content (instructional, reference, exercise, assessment)
    :param audience_level: Target audience level (beginner, intermediate, advanced)
    :returns: Original workshop content with structure and educational elements
    """
    try:
        # Generate content structure
        content_parts = [
            f"# Original Workshop Content: {topic}",
            f"**Content Type**: {content_type.title()}",
            f"**Audience Level**: {audience_level.title()}",
            f"**Creation Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            f"## 📚 {topic} Overview",
            "",
            f"### What is {topic}?",
            f"[Content to be developed based on research and best practices]",
            "",
            f"### Why Learn {topic}?",
            f"- [Key benefit 1]",
            f"- [Key benefit 2]", 
            f"- [Key benefit 3]",
            "",
            f"### Prerequisites",
            f"- [Prerequisite knowledge 1]",
            f"- [Prerequisite knowledge 2]",
            "",
            f"## 🎯 Learning Objectives",
            f"By the end of this section, participants will be able to:",
            f"- [Specific objective 1]",
            f"- [Specific objective 2]",
            f"- [Specific objective 3]",
            "",
            f"## 📖 Core Concepts",
            "",
            f"### Concept 1: [Key Concept]",
            f"[Detailed explanation with examples]",
            "",
            f"### Concept 2: [Key Concept]", 
            f"[Detailed explanation with examples]",
            "",
            f"## 💡 Best Practices",
            f"- [Best practice 1]",
            f"- [Best practice 2]",
            f"- [Best practice 3]",
            "",
            f"## 🔧 Practical Application",
            f"[How to apply these concepts in real scenarios]",
            "",
            f"## 🎯 Next Steps",
            f"1. Use generate_exercises_tool to create hands-on activities",
            f"2. Use research_validation agent to verify technical accuracy",
            f"3. Add specific examples and case studies",
            "",
            "---",
            f"*Original content template for {topic}*",
            f"*Requires research and validation for completion*"
        ]
        
        return "\n".join(content_parts)
        
    except Exception as e:
        logger.error(f"Error in create_original_content_tool: {e}")
        return f"Error creating content for '{topic}': {str(e)}. Please check your inputs and try again."


@client_tool
def generate_exercises_tool(topic: str, exercise_type: str = "hands_on", difficulty: str = "intermediate") -> str:
    """
    :description: Create hands-on exercises, activities, and practical learning experiences.
    :use_case: Use to create practical exercises for workshop participants.
    :param topic: Topic or skill area for the exercises
    :param exercise_type: Type of exercise (hands_on, discussion, case_study, assessment)
    :param difficulty: Exercise difficulty level (beginner, intermediate, advanced)
    :returns: Structured exercises with instructions, objectives, and assessment criteria
    """
    try:
        # Generate exercise structure
        exercise_parts = [
            f"# Workshop Exercises: {topic}",
            f"**Exercise Type**: {exercise_type.replace('_', ' ').title()}",
            f"**Difficulty Level**: {difficulty.title()}",
            f"**Creation Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            f"## 🎯 Exercise Objectives",
            f"Participants will practice and demonstrate:",
            f"- [Specific skill 1]",
            f"- [Specific skill 2]",
            f"- [Specific skill 3]",
            "",
            f"## 📋 Exercise 1: Introduction to {topic}",
            f"**Duration**: 20-30 minutes",
            f"**Type**: {exercise_type.replace('_', ' ').title()}",
            "",
            f"### Instructions:",
            f"1. [Step-by-step instruction 1]",
            f"2. [Step-by-step instruction 2]",
            f"3. [Step-by-step instruction 3]",
            "",
            f"### Expected Outcome:",
            f"[What participants should achieve]",
            "",
            f"### Assessment Criteria:",
            f"- [ ] [Criterion 1]",
            f"- [ ] [Criterion 2]",
            f"- [ ] [Criterion 3]",
            "",
            f"## 📋 Exercise 2: Practical Application",
            f"**Duration**: 30-45 minutes",
            f"**Type**: Hands-on Practice",
            "",
            f"### Scenario:",
            f"[Real-world scenario description]",
            "",
            f"### Tasks:",
            f"1. [Task 1]",
            f"2. [Task 2]",
            f"3. [Task 3]",
            "",
            f"### Resources Provided:",
            f"- [Resource 1]",
            f"- [Resource 2]",
            "",
            f"## 🔧 Troubleshooting Guide",
            f"**Common Issues and Solutions:**",
            f"- **Issue**: [Common problem]",
            f"  **Solution**: [How to resolve]",
            "",
            f"## 🎯 Extension Activities",
            f"For participants who finish early:",
            f"- [Additional challenge 1]",
            f"- [Additional challenge 2]",
            "",
            f"## ✅ Assessment and Feedback",
            f"**Self-Assessment Questions:**",
            f"1. [Reflection question 1]",
            f"2. [Reflection question 2]",
            "",
            f"**Instructor Feedback Points:**",
            f"- [Key observation point 1]",
            f"- [Key observation point 2]",
            "",
            "---",
            f"*Exercises for {topic} - {difficulty} level*",
            f"*Type: {exercise_type} | Duration: Variable*"
        ]
        
        return "\n".join(exercise_parts)

    except Exception as e:
        logger.error(f"Error in generate_exercises_tool: {e}")
        return f"Error generating exercises for '{topic}': {str(e)}. Please check your inputs and try again."


@client_tool
def clone_showroom_template_tool(workshop_name: str, technology_focus: str = "general", customization_level: str = "standard") -> str:
    """
    :description: Clone and customize the official RHPDS Showroom template for professional workshop creation.
    :use_case: Use to create RHPDS/Showroom-compatible workshops with professional layouts and proven patterns.
    :param workshop_name: Name for the new workshop (will be used for directory and configuration)
    :param technology_focus: Primary technology focus (openshift, kubernetes, ansible, general)
    :param customization_level: Level of customization (minimal, standard, extensive)
    :returns: Setup report with cloned template structure and customization instructions
    """
    try:
        # Generate setup report
        setup_parts = [
            f"# Showroom Template Setup: {workshop_name}",
            f"**Technology Focus**: {technology_focus.title()}",
            f"**Customization Level**: {customization_level.title()}",
            f"**Setup Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## 🎯 Showroom Template Integration",
            "",
            "### Step 1: Clone Official Template",
            "```bash",
            "# Clone the official Showroom template",
            "git clone https://github.com/rhpds/showroom_template_default.git",
            f"mv showroom_template_default {workshop_name}-workshop",
            f"cd {workshop_name}-workshop",
            "",
            "# Remove original git history and initialize new repo",
            "rm -rf .git",
            "git init",
            "git add .",
            f'git commit -m "Initial commit: {workshop_name} workshop from Showroom template"',
            "```",
            "",
            "### Step 2: Basic Configuration",
            "```yaml",
            "# Update content/antora.yml",
            f"name: {workshop_name.lower().replace(' ', '-')}-workshop",
            f"title: {workshop_name} Workshop",
            "version: '1.0'",
            "nav:",
            "- modules/ROOT/nav.adoc",
            "```",
            "",
            "### Step 3: Content Structure Setup",
            "```",
            f"{workshop_name}-workshop/",
            "├── content/",
            "│   ├── antora.yml",
            "│   └── modules/",
            "│       └── ROOT/",
            "│           ├── nav.adoc",
            "│           ├── pages/",
            "│           │   ├── index.adoc",
            "│           │   ├── module-01-introduction.adoc",
            "│           │   ├── module-02-setup.adoc",
            "│           │   └── module-03-hands-on.adoc",
            "│           └── assets/",
            "│               └── images/",
            "├── default-site.yml",
            "└── README.md",
            "```",
            "",
            "## 🔧 Technology-Specific Customizations",
            ""
        ]

        # Add technology-specific customizations
        if technology_focus.lower() == "openshift":
            setup_parts.extend([
                "### OpenShift Workshop Customizations:",
                "- **Lab Environment**: Configure OpenShift cluster access",
                "- **Navigation**: Add OpenShift-specific modules",
                "- **Exercises**: Include oc CLI commands and YAML examples",
                "- **Resources**: Link to OpenShift documentation",
                "",
                "**Recommended Modules**:",
                "1. OpenShift Introduction",
                "2. Cluster Access and Setup",
                "3. Application Deployment",
                "4. Networking and Storage",
                "5. Monitoring and Troubleshooting",
            ])
        elif technology_focus.lower() == "kubernetes":
            setup_parts.extend([
                "### Kubernetes Workshop Customizations:",
                "- **Lab Environment**: Configure kubectl access",
                "- **Navigation**: Add Kubernetes-specific modules",
                "- **Exercises**: Include kubectl commands and manifests",
                "- **Resources**: Link to Kubernetes documentation",
                "",
                "**Recommended Modules**:",
                "1. Kubernetes Fundamentals",
                "2. Pods and Deployments",
                "3. Services and Networking",
                "4. ConfigMaps and Secrets",
                "5. Monitoring and Scaling",
            ])
        elif technology_focus.lower() == "ansible":
            setup_parts.extend([
                "### Ansible Workshop Customizations:",
                "- **Lab Environment**: Configure Ansible control node",
                "- **Navigation**: Add Ansible-specific modules",
                "- **Exercises**: Include playbooks and inventory examples",
                "- **Resources**: Link to Ansible documentation",
                "",
                "**Recommended Modules**:",
                "1. Ansible Basics",
                "2. Inventory and Configuration",
                "3. Playbooks and Tasks",
                "4. Roles and Collections",
                "5. Advanced Automation",
            ])
        else:
            setup_parts.extend([
                "### General Workshop Customizations:",
                "- **Content**: Adapt modules for your technology",
                "- **Navigation**: Update nav.adoc with relevant sections",
                "- **Exercises**: Create hands-on activities",
                "- **Resources**: Add technology-specific links",
            ])

        # Add customization level details
        setup_parts.extend([
            "",
            f"## 📋 {customization_level.title()} Customization Plan",
            ""
        ])

        if customization_level == "minimal":
            setup_parts.extend([
                "### Minimal Customization Tasks:",
                "- [ ] Update workshop title and description",
                "- [ ] Replace placeholder content in index.adoc",
                "- [ ] Add basic navigation structure",
                "- [ ] Update README with workshop information",
                "",
                "**Estimated Time**: 1-2 hours",
            ])
        elif customization_level == "standard":
            setup_parts.extend([
                "### Standard Customization Tasks:",
                "- [ ] Complete minimal customization tasks",
                "- [ ] Create detailed module content",
                "- [ ] Add hands-on exercises and labs",
                "- [ ] Include screenshots and diagrams",
                "- [ ] Set up proper navigation flow",
                "- [ ] Add troubleshooting sections",
                "",
                "**Estimated Time**: 1-2 days",
            ])
        else:  # extensive
            setup_parts.extend([
                "### Extensive Customization Tasks:",
                "- [ ] Complete standard customization tasks",
                "- [ ] Add interactive elements and demos",
                "- [ ] Create assessment and validation sections",
                "- [ ] Integrate with external tools and APIs",
                "- [ ] Add advanced troubleshooting scenarios",
                "- [ ] Create instructor guides and notes",
                "- [ ] Set up automated testing and validation",
                "",
                "**Estimated Time**: 1-2 weeks",
            ])

        # Add deployment and next steps
        setup_parts.extend([
            "",
            "## 🚀 Deployment and Testing",
            "",
            "### Local Development:",
            "```bash",
            "# Install Antora (if not already installed)",
            "npm install -g @antora/cli @antora/site-generator",
            "",
            "# Build and preview locally",
            "antora default-site.yml",
            "# Open site/index.html in browser",
            "```",
            "",
            "### RHPDS/Showroom Deployment:",
            "1. **Repository Setup**: Push to GitHub/GitLab",
            "2. **RHPDS Integration**: Configure in RHPDS catalog",
            "3. **Showroom Deployment**: Set up automated builds",
            "4. **Testing**: Validate all links and exercises",
            "",
            "## 🎯 Next Steps",
            "",
            "### Content Development:",
            "1. Use create_original_content_tool for detailed module content",
            "2. Use generate_exercises_tool for hands-on activities",
            "3. Use research_validation agent for technical accuracy",
            "4. Test workshop flow with pilot participants",
            "",
            "### Quality Assurance:",
            "- Validate all external links and references",
            "- Test exercises in clean environments",
            "- Review for accessibility and inclusivity",
            "- Gather feedback and iterate",
            "",
            "## 📚 Resources",
            "",
            "- **Showroom Documentation**: https://github.com/rhpds/showroom",
            "- **Antora Documentation**: https://antora.org/",
            "- **RHPDS Catalog**: https://demo.redhat.com/",
            "- **Workshop Best Practices**: Internal Red Hat guidelines",
            "",
            "---",
            f"*Showroom template setup for {workshop_name}*",
            f"*Technology: {technology_focus} | Customization: {customization_level}*"
        ])

        return "\n".join(setup_parts)

    except Exception as e:
        logger.error(f"Error in clone_showroom_template_tool: {e}")
        return f"Error setting up Showroom template for '{workshop_name}': {str(e)}. Please check your inputs and try again."
