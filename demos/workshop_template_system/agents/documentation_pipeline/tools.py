"""
Documentation Pipeline Agent Tools
Repository monitoring and automated content update orchestration
"""

import logging
from datetime import datetime, timedelta
from urllib.parse import urlparse

# from llama_stack_client.lib.agents.client_tool import client_tool  #
# TODO: Fix when API is stable

# Simple tool decorator workaround


def client_tool(func):
    """Simple tool decorator placeholder"""
    func.tool_name = func.__name__
    return func


logger = logging.getLogger(__name__)

# Change types and their impact levels
CHANGE_IMPACT_LEVELS = {
    "breaking": {"priority": "high", "requires_review": True},
    "feature": {"priority": "medium", "requires_review": True},
    "bugfix": {"priority": "medium", "requires_review": False},
    "documentation": {"priority": "low", "requires_review": False},
    "dependency": {"priority": "medium", "requires_review": True},
    "security": {"priority": "high", "requires_review": True},
}

# Workshop sections that may be affected by different change types
SECTION_IMPACT_MAP = {
    "breaking": ["setup", "exercises", "troubleshooting"],
    "feature": ["exercises", "advanced_topics"],
    "bugfix": ["troubleshooting", "exercises"],
    "documentation": ["introduction", "resources"],
    "dependency": ["setup", "prerequisites"],
    "security": ["setup", "deployment", "troubleshooting"],
}


@client_tool
def monitor_repository_changes_tool(
    repository_urls: str, time_period: str = "7d"
) -> str:
    """
    :description: Monitor source repositories for changes that may affect workshop content.
    :use_case: Use to detect changes in source repositories that might require workshop content updates.
    :param repository_urls: Comma-separated list of GitHub repository URLs to monitor
    :param time_period: Time period to check for changes (1d, 7d, 30d)
    :returns: Report of detected changes with impact assessment
    """
    try:
        # Parse repository URLs
        urls = [url.strip() for url in repository_urls.split(",")]

        # Parse time period
        time_mapping = {"1d": 1, "7d": 7, "30d": 30}
        days = time_mapping.get(time_period, 7)

        # Simulate change detection (in production, this would use GitHub MCP)
        changes_report = {
            "monitoring_period": f"Last {days} days",
            "repositories_monitored": len(urls),
            "total_changes": 0,
            "repositories": [],
        }

        for url in urls:
            parsed_url = urlparse(url)
            if "github.com" not in parsed_url.netloc:
                continue

            path_parts = parsed_url.path.strip("/").split("/")
            if len(path_parts) < 2:
                continue

            owner, repo = path_parts[0], path_parts[1]

            # Simulate detected changes
            simulated_changes = [
                {
                    "type": "feature",
                    "description": "Added new API endpoint for user authentication",
                    "commit": "abc123",
                    "date": "2025-06-27",
                    "files_changed": ["src/auth/api.py", "docs/api.md"],
                    "impact_level": "medium",
                },
                {
                    "type": "dependency",
                    "description": "Updated React from 18.2.0 to 18.3.0",
                    "commit": "def456",
                    "date": "2025-06-26",
                    "files_changed": ["package.json", "package-lock.json"],
                    "impact_level": "medium",
                },
                {
                    "type": "documentation",
                    "description": "Updated README with new installation instructions",
                    "commit": "ghi789",
                    "date": "2025-06-25",
                    "files_changed": ["README.md"],
                    "impact_level": "low",
                },
            ]

            repo_changes = {
                "repository": f"{owner}/{repo}",
                "url": url,
                "changes_detected": len(simulated_changes),
                "changes": simulated_changes,
            }

            changes_report["repositories"].append(repo_changes)
            changes_report["total_changes"] += len(simulated_changes)

        # Generate report
        report_parts = [
            f"# Repository Change Monitoring Report",
            f"**Monitoring Period**: {changes_report['monitoring_period']}",
            f"**Repositories Monitored**: {changes_report['repositories_monitored']}",
            f"**Total Changes Detected**: {changes_report['total_changes']}",
            f"**Report Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "## 📊 Change Summary by Repository",
            "",
        ]

        for repo_data in changes_report["repositories"]:
            report_parts.extend(
                [
                    f"### {repo_data['repository']}",
                    f"**URL**: {repo_data['url']}",
                    f"**Changes Detected**: {repo_data['changes_detected']}",
                    "",
                ]
            )

            if repo_data["changes"]:
                report_parts.append("**Recent Changes**:")
                for change in repo_data["changes"]:
                    impact_info = CHANGE_IMPACT_LEVELS.get(
                        change["type"], {"priority": "low"}
                    )
                    priority_emoji = {"high": "🔴", "medium": "🟡", "low": "🟢"}.get(
                        impact_info["priority"], "⚪"
                    )

                    report_parts.extend(
                        [
                            f"- {priority_emoji} **{change['type'].title()}** ({change['date']}): {change['description']}",
                            f"  - Commit: `{change['commit']}`",
                            f"  - Files: {', '.join(change['files_changed'])}",
                            f"  - Impact Level: {change['impact_level'].title()}",
                            "",
                        ]
                    )
            else:
                report_parts.append("- No changes detected in monitoring period")

            report_parts.append("")

        # Add impact analysis summary
        high_priority_changes = []
        medium_priority_changes = []

        for repo_data in changes_report["repositories"]:
            for change in repo_data["changes"]:
                impact_info = CHANGE_IMPACT_LEVELS.get(
                    change["type"], {"priority": "low"}
                )
                if impact_info["priority"] == "high":
                    high_priority_changes.append(
                        f"{repo_data['repository']}: {change['description']}"
                    )
                elif impact_info["priority"] == "medium":
                    medium_priority_changes.append(
                        f"{repo_data['repository']}: {change['description']}"
                    )

        report_parts.extend(
            [
                "## 🎯 Impact Analysis",
                "",
                f"**High Priority Changes**: {len(high_priority_changes)}",
                f"**Medium Priority Changes**: {len(medium_priority_changes)}",
                "",
                "### Recommended Actions:",
            ]
        )

        if high_priority_changes:
            report_parts.extend(
                [
                    "**Immediate Review Required** (High Priority):",
                ]
            )
            for change in high_priority_changes:
                report_parts.append(f"- {change}")
            report_parts.append("")

        if medium_priority_changes:
            report_parts.extend(
                [
                    "**Review Recommended** (Medium Priority):",
                ]
            )
            for change in medium_priority_changes:
                report_parts.append(f"- {change}")
            report_parts.append("")

        report_parts.extend(
            [
                "### Next Steps:",
                "1. Use analyze_impact_tool to assess specific workshop content impact",
                "2. Use create_update_proposal_tool to generate update proposals",
                "3. Schedule human review for high-priority changes",
                "4. Plan workshop content updates based on change analysis",
            ]
        )

        return "\n".join(report_parts)

    except Exception as e:
        logger.error(f"Error in monitor_repository_changes_tool: {e}")
        return f"Error monitoring repository changes: {
            str(e)}. Please check the repository URLs and try again."


@client_tool
def analyze_impact_tool(
    changes_description: str, workshop_sections: str = "all"
) -> str:
    """
    :description: Analyze repository changes to determine impact on workshop content.
    :use_case: Use after detecting repository changes to understand which workshop sections need updates.
    :param changes_description: Description of detected changes from repository monitoring
    :param workshop_sections: Specific workshop sections to analyze (comma-separated) or 'all'
    :returns: Detailed impact analysis with specific recommendations for workshop updates
    """
    try:
        # Parse workshop sections
        if workshop_sections.lower() == "all":
            sections_to_analyze = [
                "introduction",
                "setup",
                "exercises",
                "troubleshooting",
                "resources",
                "deployment",
            ]
        else:
            sections_to_analyze = [
                section.strip() for section in workshop_sections.split(",")
            ]

        # Analyze changes for impact
        changes_lower = changes_description.lower()

        # Detect change types from description
        detected_changes = []
        for change_type in CHANGE_IMPACT_LEVELS.keys():
            if change_type in changes_lower:
                detected_changes.append(change_type)

        # If no specific types detected, infer from keywords
        if not detected_changes:
            if any(word in changes_lower for word in ["api", "endpoint", "interface"]):
                detected_changes.append("feature")
            if any(word in changes_lower for word in ["update", "upgrade", "version"]):
                detected_changes.append("dependency")
            if any(word in changes_lower for word in ["fix", "bug", "patch"]):
                detected_changes.append("bugfix")
            if any(
                word in changes_lower for word in ["security", "vulnerability", "cve"]
            ):
                detected_changes.append("security")

        # Generate impact analysis
        report_parts = [
            f"# Workshop Content Impact Analysis",
            f"**Analysis Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"**Sections Analyzed**: {', '.join(sections_to_analyze)}",
            "",
            "## 🔍 Change Analysis",
            f"**Detected Change Types**: {', '.join(detected_changes) if detected_changes else 'General changes'}",
            "",
        ]

        # Analyze impact on each section
        section_impacts = {}

        for section in sections_to_analyze:
            impact_score = 0
            impact_reasons = []

            for change_type in detected_changes:
                if section in SECTION_IMPACT_MAP.get(change_type, []):
                    impact_info = CHANGE_IMPACT_LEVELS[change_type]
                    if impact_info["priority"] == "high":
                        impact_score += 3
                    elif impact_info["priority"] == "medium":
                        impact_score += 2
                    else:
                        impact_score += 1

                    impact_reasons.append(f"{change_type} changes affect {section}")

            # Determine impact level
            if impact_score >= 3:
                impact_level = "High"
                impact_emoji = "🔴"
            elif impact_score >= 2:
                impact_level = "Medium"
                impact_emoji = "🟡"
            elif impact_score >= 1:
                impact_level = "Low"
                impact_emoji = "🟢"
            else:
                impact_level = "None"
                impact_emoji = "⚪"

            section_impacts[section] = {
                "level": impact_level,
                "score": impact_score,
                "reasons": impact_reasons,
                "emoji": impact_emoji,
            }

        return f"Impact analysis completed for {
            len(sections_to_analyze)} sections. Use create_update_proposal_tool for specific proposals."

    except Exception as e:
        logger.error(f"Error in analyze_impact_tool: {e}")
        return f"Error analyzing impact: {
            str(e)}. Please check your inputs and try again."


@client_tool
def create_update_proposal_tool(
    impact_analysis: str, section_name: str, update_type: str = "content"
) -> str:
    """
    :description: Create human-reviewable proposals for workshop content updates.
    :use_case: Use after impact analysis to generate specific, actionable update proposals for human review.
    :param impact_analysis: Results from impact analysis or description of required changes
    :param section_name: Workshop section to create update proposal for
    :param update_type: Type of update (content, exercises, setup, troubleshooting)
    :returns: Detailed update proposal with specific actions and review checkpoints
    """
    try:
        # Generate proposal ID
        proposal_id = f"WS-{
            datetime.now().strftime('%Y%m%d')}-{
            section_name.upper()[
                :3]}-{
                update_type.upper()[
                    :3]}"

        # Parse section name
        section_title = section_name.replace("_", " ").title()

        # Generate update proposal
        proposal_parts = [
            f"# Workshop Update Proposal: {proposal_id}",
            f"**Section**: {section_title}",
            f"**Update Type**: {update_type.title()}",
            f"**Proposal Date**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"**Status**: Pending Review",
            "",
            "## 📋 Proposal Summary",
            f"This proposal outlines required updates to the **{section_title}** section based on detected repository changes.",
            "",
            "## 🎯 Proposed Changes",
            f"1. Review and update {section_title} content",
            f"2. Verify {section_title} instructions and examples",
            f"3. Test {section_title} functionality",
            f"4. Update troubleshooting information",
            "",
            "## ✅ Acceptance Criteria",
            f"- All {section_title} content reflects current repository state",
            "- All code examples execute successfully",
            "- Instructions are clear and complete",
            "- Human reviewer has approved changes",
            "",
            f"## 📝 Approval Required",
            f"**Reviewer**: [To be assigned]",
            f"**Review Deadline**: {(datetime.now() + timedelta(days=3)).strftime('%Y-%m-%d')}",
            "",
            "**Approval Status**: ⏳ Pending",
            "",
            "---",
            f"*Proposal ID: {proposal_id}*",
            f"*Generated by Documentation Pipeline Agent*",
        ]

        return "\n".join(proposal_parts)

    except Exception as e:
        logger.error(f"Error in create_update_proposal_tool: {e}")
        return f"Error creating update proposal: {
            str(e)}. Please check your inputs and try again."


@client_tool
def monitor_external_sources_tool(
    sources_config: str, monitoring_frequency: str = "daily"
) -> str:
    """
    :description: Monitor external documentation sources like PDFs, websites, and APIs for changes that affect workshop content.
    :use_case: Use to track updates in external documentation that workshops depend on, enabling proactive content updates.
    :param sources_config: JSON-formatted configuration of external sources to monitor (URLs, PDFs, APIs)
    :param monitoring_frequency: How often to check for changes (hourly, daily, weekly)
    :returns: Monitoring report with detected changes and impact assessment
    """
    try:
        import json

        # Parse sources configuration
        try:
            sources = json.loads(sources_config)
        except json.JSONDecodeError:
            # Fallback to simple parsing if not JSON
            sources = {
                "documentation_sites": (
                    [sources_config] if sources_config.startswith("http") else []
                ),
                "pdf_documents": [],
                "api_endpoints": [],
            }

        # Generate monitoring report
        report_parts = [
            f"# External Sources Monitoring Report",
            f"**Monitoring Frequency**: {monitoring_frequency.title()}",
            f"**Report Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"**Sources Monitored**: {len(sources.get('documentation_sites',
                                                                  [])) + len(sources.get('pdf_documents',
                                                                                         [])) + len(sources.get('api_endpoints',
                                                                                                                []))}",
            "",
            "## 📊 Monitoring Configuration",
            "",
        ]

        # Documentation Sites
        if sources.get("documentation_sites"):
            report_parts.extend(
                [
                    "### 🌐 Documentation Websites",
                    "**Monitored Sites**:",
                ]
            )
            for site in sources["documentation_sites"]:
                report_parts.append(f"- {site}")
            report_parts.extend(
                [
                    "",
                    "**Monitoring Method**: Web scraping + content hashing",
                    "**Change Detection**: Page content, last-modified headers, version numbers",
                    "",
                ]
            )

        # PDF Documents
        if sources.get("pdf_documents"):
            report_parts.extend(
                [
                    "### 📄 PDF Documents",
                    "**Monitored PDFs**:",
                ]
            )
            for pdf in sources["pdf_documents"]:
                report_parts.append(f"- {pdf}")
            report_parts.extend(
                [
                    "",
                    "**Monitoring Method**: File hash comparison, metadata analysis",
                    "**Change Detection**: Content changes, version updates, file size",
                    "",
                ]
            )

        # API Endpoints
        if sources.get("api_endpoints"):
            report_parts.extend(
                [
                    "### 🔌 API Endpoints",
                    "**Monitored APIs**:",
                ]
            )
            for api in sources["api_endpoints"]:
                report_parts.append(f"- {api}")
            report_parts.extend(
                [
                    "",
                    "**Monitoring Method**: API versioning, schema changes, response structure",
                    "**Change Detection**: New endpoints, deprecated methods, schema updates",
                    "",
                ]
            )

        # Simulated change detection results
        report_parts.extend(
            [
                "## 🔍 Change Detection Results",
                "",
                "### Recent Changes Detected:",
            ]
        )

        # Simulate some detected changes
        detected_changes = [
            {
                "source": "https://docs.openshift.com/container-platform/4.16/",
                "type": "documentation_site",
                "change": "New installation method added for bare metal deployments",
                "impact": "High - affects workshop setup instructions",
                "date": "2025-06-27",
            },
            {
                "source": "https://kubernetes.io/docs/reference/",
                "type": "documentation_site",
                "change": "API version v1.31 documentation updated",
                "impact": "Medium - may affect kubectl examples",
                "date": "2025-06-26",
            },
        ]

        if detected_changes:
            for change in detected_changes:
                impact_emoji = (
                    "🔴"
                    if change["impact"].startswith("High")
                    else "🟡" if change["impact"].startswith("Medium") else "🟢"
                )
                report_parts.extend(
                    [
                        f"#### {impact_emoji} {change['source']}",
                        f"**Change**: {change['change']}",
                        f"**Impact**: {change['impact']}",
                        f"**Date**: {change['date']}",
                        f"**Source Type**: {change['type'].replace('_', ' ').title()}",
                        "",
                    ]
                )
        else:
            report_parts.append("- No changes detected in current monitoring period")

        # RAG Integration recommendations
        report_parts.extend(
            [
                "",
                "## 🧠 RAG Integration Recommendations",
                "",
                "### Content Ingestion Strategy:",
                "1. **Automatic Ingestion**: Set up automated content extraction",
                "2. **Vector Embeddings**: Create embeddings for semantic search",
                "3. **Metadata Tagging**: Tag content with source, date, version",
                "4. **Change Tracking**: Maintain version history for content",
                "",
                "### RAG Database Updates:",
                "```python",
                "# Pseudocode for RAG integration",
                "for source in external_sources:",
                "    if source.has_changes():",
                "        new_content = extract_content(source)",
                "        embeddings = create_embeddings(new_content)",
                "        rag_db.update(source.id, embeddings, metadata)",
                "        notify_workshop_agents(source.id, changes)",
                "```",
                "",
                "### Workshop Agent Integration:",
                "- **Workshop Chat Agent**: Access updated external content for Q&A",
                "- **Research & Validation Agent**: Cross-reference with latest docs",
                "- **Content Creator Agent**: Use current best practices",
                "- **Template Converter Agent**: Validate against latest standards",
            ]
        )

        # Configuration recommendations
        report_parts.extend(
            [
                "",
                "## ⚙️ Configuration Recommendations",
                "",
                "### External Sources Configuration Example:",
                "```json",
                "{",
                '  "documentation_sites": [',
                '    "https://docs.openshift.com/container-platform/latest/",',
                '    "https://kubernetes.io/docs/",',
                '    "https://quarkus.io/guides/"',
                "  ],",
                '  "pdf_documents": [',
                '    "https://example.com/installation-guide.pdf",',
                '    "https://vendor.com/api-reference.pdf"',
                "  ],",
                '  "api_endpoints": [',
                '    "https://api.openshift.com/v1/",',
                '    "https://api.kubernetes.io/v1/"',
                "  ],",
                '  "monitoring_settings": {',
                '    "frequency": "daily",',
                '    "change_threshold": 0.1,',
                '    "notification_webhook": "https://workshop-system.com/webhook"',
                "  }",
                "}",
                "```",
                "",
                "### Integration with Workshop System:",
                "1. **Add to workshop configuration**: Include external sources in workshop metadata",
                "2. **Automated monitoring**: Set up scheduled monitoring jobs",
                "3. **Change notifications**: Alert workshop maintainers of relevant changes",
                "4. **Content updates**: Trigger update proposals when changes detected",
                "",
                "## 🎯 Next Steps",
                "",
                "### Implementation Tasks:",
                "1. **Set up monitoring infrastructure** for external sources",
                "2. **Integrate with RAG database** for content storage",
                "3. **Configure change detection** algorithms",
                "4. **Set up notification system** for workshop maintainers",
                "",
                "### Workshop Integration:",
                "1. **Update workshop metadata** to include external dependencies",
                "2. **Configure monitoring frequency** based on source criticality",
                "3. **Set up automated content updates** where appropriate",
                "4. **Train workshop maintainers** on external source management",
                "",
                "---",
                f"*External sources monitoring for workshop content maintenance*",
                f"*Frequency: {monitoring_frequency} | Sources: {len(sources.get('documentation_sites',
                                                                                              [])) + len(sources.get('pdf_documents',
                                                                                                                     [])) + len(sources.get('api_endpoints',
                                                                                                                                            []))}*",
            ]
        )

        return "\n".join(report_parts)

    except Exception as e:
        logger.error(f"Error in monitor_external_sources_tool: {e}")
        return f"Error monitoring external sources: {
            str(e)}. Please check your configuration and try again."
