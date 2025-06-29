# ADR-0001: Workshop Template Strategy Based on Repository Type

## Status
Accepted

## Context

The Workshop Template System needs to handle two fundamentally different scenarios when processing GitHub repositories:

1. **Existing Workshops**: Repositories that already contain workshop content and structure
2. **Application Repositories**: Code repositories that need to be converted into workshops

The system must automatically detect the repository type and apply the appropriate template strategy to avoid confusion and ensure proper workflow routing.

## Decision

We will implement a **dual-template strategy** based on repository classification:

### For Existing Workshops (Workflow 3: Enhancement and Modernization)
- **Clone the original workshop repository** directly into Gitea
- **Preserve existing workshop structure** and content
- **Enhance and modernize** the existing content
- **Example**: `https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git`
- **Process**: 
  1. Clone existing workshop → Gitea
  2. Analyze current structure and quality
  3. Apply enhancements and updates
  4. Maintain original workshop framework (Antora, Showroom, etc.)

### For Application Repositories (Workflow 1: Repository-Based Workshop Creation)
- **Use Showroom default template** as the base structure
- **Transform application code** into educational workshop content
- **Create entirely new workshop** based on application analysis
- **Template Base**: `https://github.com/rhpds/showroom_template_default.git`
- **Process**:
  1. Analyze application repository
  2. Clone Showroom template → Gitea
  3. Generate workshop content from application analysis
  4. Populate template with educational materials

## Repository Classification Logic

```python
def classify_repository(repo_structure, repo_content):
    """
    Classify repository into four categories based on actual framework detection
    """
    # Workshop framework indicators (requires actual framework files)
    workshop_indicators = {
        "antora": ["antora.yml", "content/modules", "nav.adoc"],
        "legacy_rto": ["default-site.yml", "demo-site.yml", "content/"],  # Red Hat SE RTO template
        "showroom": ["showroom.yml", "content/", "modules/"],
        "rhpds": ["agnosticd", "ansible", "lab-guide"],
        "gitbook": ["SUMMARY.md", "book.json", ".gitbook"],
        "mkdocs": ["mkdocs.yml", "docs/", "site/"]
    }

    # Check for ACTUAL workshop framework files (not just name patterns)
    for workshop_type, indicators in workshop_indicators.items():
        framework_files = [ind for ind in indicators if not ind.endswith('/')]
        if any(file in repo_structure['files'] for file in framework_files):
            return "existing_workshop", workshop_type

    # Check for tutorial-style content (organized modules but no framework)
    repo_name_lower = repo_name.lower()
    has_workshop_name = any(keyword in repo_name_lower for keyword in ["workshop", "lab", "tutorial", "guide"])
    has_tutorial_structure = any(
        dir_name.startswith(('01-', '02-', '03-', 'module', 'chapter', 'lab'))
        for dir_name in repo_structure['directories']
    )

    if has_workshop_name and has_tutorial_structure:
        return "tutorial_content", "needs_showroom_template"

    # Default to application if no workshop structure found
    return "application", "needs_conversion"
```

## Template Selection Matrix

| Repository Type | Framework Detected | Template Source | Gitea Strategy | Workflow |
|----------------|-------------------|----------------|----------------|----------|
| **Modern Workshop** | `showroom.yml`, `antora.yml` | Original repository | Clone original → Gitea | Workflow 3: Enhancement |
| **Legacy Workshop** | `default-site.yml`, `demo-site.yml` | Original repository | Clone original → Gitea | Workflow 3: Enhancement + Modernization |
| **Tutorial Content** | Organized modules, no framework | `showroom_template_default.git` | Clone template → Gitea | Workflow 1: Creation |
| **Application** | No workshop structure | `showroom_template_default.git` | Clone template → Gitea | Workflow 1: Creation |

## Examples

### Modern Workshop Example
- **Repository**: `https://github.com/Red-Hat-SE-RTO/openshift-bare-metal-deployment-workshop.git`
- **Detection**: Contains `antora.yml`, `content/modules/`, modern workshop framework
- **Action**: Clone original workshop → Gitea → Enhance existing content
- **Result**: Enhanced version of existing modern workshop

### Legacy Workshop Example
- **Repository**: `https://github.com/Red-Hat-SE-RTO/todo-demo-app-helmrepo-workshop.git`
- **Detection**: Contains `default-site.yml`, `demo-site.yml`, legacy RTO framework
- **Action**: Clone original workshop → Gitea → Enhance and modernize content
- **Result**: Enhanced and modernized workshop with current standards

### Tutorial Content Example
- **Repository**: `https://github.com/jeremyrdavis/dddhexagonalworkshop.git`
- **Detection**: Organized modules (`01-End-to-End-DDD`, `02-Value-Objects`) but no workshop framework
- **Action**: Clone `showroom_template_default.git` → Gitea → Transform tutorial into workshop
- **Result**: Professional workshop structure with tutorial content

### Application Repository Example
- **Repository**: `https://github.com/tosin2013/healthcare-ml-genetic-predictor.git`
- **Detection**: Application code, no workshop structure or tutorial organization
- **Action**: Clone `showroom_template_default.git` → Gitea → Generate workshop content
- **Result**: New workshop based on application analysis

## Consequences

### Positive
- **Clear separation** of concerns between enhancement and creation workflows
- **Preserves existing workshop investments** and structure
- **Consistent template base** for new workshop creation
- **Automatic workflow routing** based on repository classification
- **Reduces confusion** about which template to use

### Negative
- **Additional complexity** in repository classification logic
- **Two different code paths** to maintain and test
- **Potential misclassification** requiring manual override capability

## Implementation Status

### ✅ Completed (2025-06-28)
- **Repository Classification Logic**: Implemented four-way classification system
- **Workshop Framework Detection**: Added support for modern, legacy, and tutorial content detection
- **Template Converter Agent**: Updated with real GitHub API integration and refined detection
- **Content Creator Agent**: Enhanced with repository-based content transformation
- **Workflow Routing**: Properly routes DDD Hexagonal (Workflow 1) and Todo Demo (Workflow 3)

### 🚧 In Progress
- **Source Manager Agent**: Implementing real Gitea repository creation with ADR-0001 strategy
- **BuildConfig Integration**: Connecting workshop creation to OpenShift deployment pipeline

### 📋 Validation Results
| Repository | Classification | Workflow | Template | Status |
|------------|---------------|----------|----------|---------|
| `dddhexagonalworkshop` | Tutorial Content | Workflow 1 | showroom_template_default | ✅ Correct |
| `todo-demo-app-helmrepo-workshop` | Legacy Workshop | Workflow 3 | Original Repository | ✅ Correct |
| `healthcare-ml-genetic-predictor` | Application | Workflow 1 | showroom_template_default | ✅ Correct |

## Implementation Notes

1. **Template Converter Agent** must implement robust repository classification
2. **Source Manager Agent** must handle both template strategies
3. **Content Creator Agent** must adapt content generation based on workflow type
4. **Testing** must cover both workflow paths with real repository examples

## Monitoring and Review

- Monitor classification accuracy with real repositories
- Track workflow success rates for both paths
- Review and update classification logic based on new repository patterns
- Consider adding manual override capability for edge cases

---

**Date**: 2025-06-28 (Updated with implementation results)
**Participants**: Workshop Template System Development Team
**Review Date**: 2025-09-28 (3 months)
**Related ADRs**:
- ADR-0002 (Human-in-the-Loop Agent Integration) - Adds human oversight to workflows
- ADR-0003 (Agent-Pipeline Integration) - Pipeline implementation of workflows
- ADR-0006 (Tekton-Agent Integration Architecture) - Detailed pipeline-agent integration
**Status Update**: Workshop Template Strategy IMPLEMENTED (2025-06-29)
