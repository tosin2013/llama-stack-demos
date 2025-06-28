"""
Content Creator Agent Tools
Original workshop creation from learning objectives and concepts
"""

import os
import logging
from typing import Dict, List, Any, Optional
from datetime import datetime
# from llama_stack_client.lib.agents.client_tool import client_tool  # TODO: Fix when API is stable

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func

logger = logging.getLogger(__name__)

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
