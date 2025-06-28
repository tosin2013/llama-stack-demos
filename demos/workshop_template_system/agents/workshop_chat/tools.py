"""
Workshop Chat Agent Tools
RAG-based tools for workshop participant assistance
"""

import os
import logging
from typing import List, Dict, Any, Optional
# # from llama_stack_client.lib.agents.client_tool import client_tool  # TODO: Fix when API is stable

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func  # TODO: Fix when API is stable

# Simple tool decorator workaround
def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func

logger = logging.getLogger(__name__)

# Simplified RAG implementation for development
# In production, this would integrate with existing Llama Stack RAG infrastructure
WORKSHOP_CONTENT_CACHE = {}
WORKSHOP_SECTIONS = {
    "introduction": {
        "title": "Introduction",
        "description": "Workshop overview, objectives, and prerequisites",
        "content": "This section covers the workshop introduction and learning objectives."
    },
    "setup": {
        "title": "Environment Setup", 
        "description": "Development environment and tool installation",
        "content": "Instructions for setting up your development environment, installing required tools, and verifying your setup."
    },
    "exercises": {
        "title": "Hands-on Exercises",
        "description": "Interactive practice activities and labs",
        "content": "Step-by-step exercises to practice the concepts covered in this workshop."
    },
    "troubleshooting": {
        "title": "Troubleshooting",
        "description": "Common issues and solutions",
        "content": "Solutions to common problems you might encounter during the workshop."
    },
    "resources": {
        "title": "Additional Resources",
        "description": "Further learning materials and references",
        "content": "Links to documentation, tutorials, and other resources for continued learning."
    },
    "conclusion": {
        "title": "Conclusion",
        "description": "Summary and next steps",
        "content": "Workshop summary, key takeaways, and suggested next steps for your learning journey."
    }
}


@client_tool
def workshop_query_tool(query: str, context_limit: int = 5) -> str:
    """
    :description: Query workshop content using RAG for contextual assistance.
    :use_case: Use when participants ask questions about workshop content, concepts, or need help with specific topics.
    :param query: User question about workshop content
    :param context_limit: Maximum number of context chunks to retrieve (default: 5)
    :returns: Contextually relevant response based on workshop content
    """
    try:
        # Simplified content search - in production this would use vector similarity
        query_lower = query.lower()
        relevant_sections = []
        
        # Simple keyword matching for demonstration
        for section_id, section_data in WORKSHOP_SECTIONS.items():
            section_text = f"{section_data['title']} {section_data['description']} {section_data['content']}"
            
            # Basic relevance scoring
            relevance_score = 0
            query_words = query_lower.split()
            
            for word in query_words:
                if word in section_text.lower():
                    relevance_score += 1
            
            if relevance_score > 0:
                relevant_sections.append({
                    "section": section_id,
                    "title": section_data["title"],
                    "content": section_data["content"],
                    "score": relevance_score
                })
        
        # Sort by relevance and limit results
        relevant_sections.sort(key=lambda x: x["score"], reverse=True)
        relevant_sections = relevant_sections[:context_limit]
        
        if not relevant_sections:
            return (
                f"I couldn't find specific information about '{query}' in the workshop content. "
                "You might want to check the troubleshooting section or ask your instructor for help. "
                "You can also try rephrasing your question or asking about a specific workshop section."
            )
        
        # Generate response based on relevant content
        response_parts = [
            f"Based on the workshop content, here's what I found about '{query}':\n"
        ]
        
        for i, section in enumerate(relevant_sections, 1):
            response_parts.append(
                f"{i}. **{section['title']}**: {section['content']}\n"
            )
        
        response_parts.append(
            "\nIf you need more specific help, please let me know what particular aspect you're struggling with!"
        )
        
        return "\n".join(response_parts)
        
    except Exception as e:
        logger.error(f"Error in workshop_query_tool: {e}")
        return (
            f"I encountered an error while searching for information about '{query}'. "
            "Please try rephrasing your question or contact your instructor for assistance."
        )


@client_tool
def workshop_navigation_tool(section: str) -> str:
    """
    :description: Provide navigation assistance for workshop sections.
    :use_case: Use when participants need help finding specific workshop sections or want to navigate to different parts of the workshop.
    :param section: Workshop section name or topic to navigate to
    :returns: Navigation guidance and section overview
    """
    try:
        section_lower = section.lower().strip()
        
        # Direct section match
        if section_lower in WORKSHOP_SECTIONS:
            section_data = WORKSHOP_SECTIONS[section_lower]
            return (
                f"📍 **{section_data['title']}** Section\n\n"
                f"**Description**: {section_data['description']}\n\n"
                f"**Content Overview**: {section_data['content']}\n\n"
                f"This section will help you with {section_data['description'].lower()}. "
                f"Let me know if you have specific questions about this section!"
            )
        
        # Fuzzy matching for common variations
        section_mappings = {
            "start": "introduction",
            "begin": "introduction", 
            "intro": "introduction",
            "overview": "introduction",
            "install": "setup",
            "installation": "setup",
            "configure": "setup",
            "config": "setup",
            "environment": "setup",
            "env": "setup",
            "practice": "exercises",
            "lab": "exercises",
            "labs": "exercises",
            "hands-on": "exercises",
            "exercise": "exercises",
            "problem": "troubleshooting",
            "problems": "troubleshooting",
            "issue": "troubleshooting",
            "issues": "troubleshooting",
            "error": "troubleshooting",
            "errors": "troubleshooting",
            "help": "troubleshooting",
            "stuck": "troubleshooting",
            "learn": "resources",
            "learning": "resources",
            "more": "resources",
            "additional": "resources",
            "docs": "resources",
            "documentation": "resources",
            "end": "conclusion",
            "finish": "conclusion",
            "summary": "conclusion",
            "next": "conclusion"
        }
        
        if section_lower in section_mappings:
            mapped_section = section_mappings[section_lower]
            section_data = WORKSHOP_SECTIONS[mapped_section]
            return (
                f"📍 **{section_data['title']}** Section\n\n"
                f"**Description**: {section_data['description']}\n\n"
                f"**Content Overview**: {section_data['content']}\n\n"
                f"This section will help you with {section_data['description'].lower()}. "
                f"Let me know if you have specific questions about this section!"
            )
        
        # If no match found, provide overview of all sections
        available_sections = []
        for section_id, section_data in WORKSHOP_SECTIONS.items():
            available_sections.append(f"• **{section_data['title']}**: {section_data['description']}")
        
        return (
            f"I couldn't find a section called '{section}'. Here are the available workshop sections:\n\n"
            + "\n".join(available_sections) + 
            "\n\nWhich section would you like to explore? Just ask me about any of these sections!"
        )
        
    except Exception as e:
        logger.error(f"Error in workshop_navigation_tool: {e}")
        return (
            f"I encountered an error while trying to navigate to '{section}'. "
            "Please try asking about a specific workshop section like 'introduction', 'setup', or 'exercises'."
        )
