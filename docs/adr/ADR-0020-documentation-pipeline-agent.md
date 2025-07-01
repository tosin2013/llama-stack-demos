# ADR-0020: Documentation Pipeline Agent Architecture

## Status
Accepted - **IMPLEMENTED AND OPERATIONAL**

## Context

The Documentation Pipeline Agent is responsible for automated documentation generation, maintenance, and updates for workshop content. This agent handles documentation workflows, ensures documentation consistency, and manages documentation lifecycle throughout workshop development.

**Current Implementation Status:**
- ✅ **DEPLOYED**: Running in OpenShift workshop-system namespace
- ✅ **OPERATIONAL**: Successfully generating workshop documentation
- ✅ **PIPELINE-INTEGRATED**: Connected to Tekton workflows for documentation tasks
- ✅ **API-ACCESSIBLE**: HTTP endpoints available for documentation requests

## Decision

### **Agent Architecture**

#### **1. Documentation-Focused Deployment**
```yaml
# Standard Agent Deployment Pattern
spec:
  containers:
  - name: documentation-pipeline-agent
    image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
    command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "documentation_pipeline", "--port", "8080"]
    env:
    - name: AGENT_NAME
      value: "documentation_pipeline"
    - name: LLAMA_STACK_ENDPOINT
      value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
    - name: INFERENCE_MODEL_ID
      value: "meta-llama/Llama-3.2-3B-Instruct"
```

**Key Features:**
- **Automated Documentation**: Generate comprehensive workshop documentation
- **Documentation Maintenance**: Update and maintain existing documentation
- **Consistency Enforcement**: Ensure documentation follows standards and templates
- **Lifecycle Management**: Handle documentation throughout workshop lifecycle

#### **2. Documentation Capabilities**

**Primary Functions:**
1. **New Workshop Documentation**: Generate complete documentation for new workshops
2. **Enhanced Workshop Documentation**: Update documentation for enhanced workshops
3. **Documentation Validation**: Ensure documentation quality and completeness
4. **Template Compliance**: Verify documentation follows workshop templates
5. **Cross-Reference Management**: Maintain links and references between documents

**Tool Integration:**
```python
# Documentation Pipeline Tools
- generate_workshop_documentation_tool: Create comprehensive workshop docs
- update_documentation_tool: Update existing documentation
- validate_documentation_tool: Verify documentation quality
- manage_documentation_lifecycle_tool: Handle documentation versioning
- cross_reference_documentation_tool: Manage inter-document references
```

#### **3. HTTP API Structure**
```bash
# Documentation Generation Endpoint
POST /invoke
Content-Type: application/json

{
  "tool_name": "generate_workshop_documentation_tool",
  "parameters": {
    "workshop_name": "ddd-hexagonal-workshop",
    "gitea_repository_url": "https://gitea-with-admin-gitea.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/workshop-system/workshop-ddd-hexagonal-workshop-demo-1751149405809",
    "documentation_type": "new-workshop-docs",
    "template_type": "showroom_template_default"
  }
}
```

**Response Format:**
```json
{
  "result": "# Documentation Generation Report\n\n## Generated Documentation\n...",
  "status": "success",
  "documentation_url": "https://workshop-docs.example.com/ddd-hexagonal-workshop",
  "generated_files": ["README.md", "modules/ROOT/nav.adoc", "modules/ROOT/pages/"],
  "documentation_score": 0.95
}
```

### **4. Documentation Types and Templates**

#### **New Workshop Documentation**
1. **README.md**: Workshop overview and setup instructions
2. **Navigation Structure**: Antora navigation configuration
3. **Module Pages**: Individual workshop module documentation
4. **Asset Documentation**: Image and resource documentation
5. **Configuration Files**: Antora and Showroom configuration

#### **Enhanced Workshop Documentation**
1. **Update Summaries**: Documentation of enhancements and changes
2. **Version History**: Changelog and version documentation
3. **Migration Guides**: Instructions for updating from previous versions
4. **Feature Documentation**: New feature and capability documentation

### **5. Tekton Pipeline Integration**

#### **Agent Task Definition**
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-documentation-pipeline
spec:
  params:
  - name: workshop-name
  - name: gitea-repository-url
  - name: documentation-type
    default: "new-workshop-docs"
  - name: agent-endpoint
    default: "http://documentation-pipeline-agent:80"
  
  results:
  - name: documentation-status
  - name: documentation-url
  - name: update-summary
```

**Documentation Workflows:**
1. **Post-Creation Documentation**: Generate documentation after workshop creation
2. **Enhancement Documentation**: Update documentation for workshop enhancements
3. **Validation Documentation**: Document validation results and recommendations
4. **Deployment Documentation**: Create deployment and usage documentation

### **6. Documentation Standards and Quality**

#### **Documentation Quality Metrics**
```yaml
# Documentation Quality Assessment
completeness: 0.0-1.0         # Coverage of all workshop components
clarity: 0.0-1.0              # Readability and comprehension
accuracy: 0.0-1.0             # Technical accuracy of documentation
consistency: 0.0-1.0          # Adherence to documentation standards
usability: 0.0-1.0            # User experience and navigation
overall_score: 0.0-1.0        # Weighted average
```

**Quality Standards:**
- **Complete Coverage**: All workshop components documented
- **Clear Instructions**: Step-by-step guidance for users
- **Accurate Information**: Technically correct and up-to-date
- **Consistent Format**: Follows workshop documentation templates
- **User-Friendly**: Easy to navigate and understand

### **7. Template Integration**

#### **Showroom Template Documentation Structure**
```yaml
# Documentation Structure
documentation/
├── README.md                 # Workshop overview
├── modules/
│   └── ROOT/
│       ├── nav.adoc         # Navigation structure
│       ├── pages/           # Workshop pages
│       │   ├── index.adoc   # Landing page
│       │   ├── setup.adoc   # Setup instructions
│       │   └── modules/     # Workshop modules
│       └── assets/          # Documentation assets
├── antora.yml               # Antora configuration
└── showroom.yml            # Showroom configuration
```

**Template Compliance:**
1. **Antora Compatibility**: Ensure documentation works with Antora site generator
2. **Showroom Integration**: Compatible with RHPDS Showroom platform
3. **Navigation Consistency**: Standardized navigation structure
4. **Asset Management**: Proper handling of images and resources

### **8. Documentation Lifecycle Management**

#### **Version Control Integration**
1. **Git Integration**: Documentation versioned with workshop content
2. **Branch Management**: Documentation branches aligned with workshop versions
3. **Release Documentation**: Generate release notes and documentation
4. **Archive Management**: Maintain historical documentation versions

#### **Automated Updates**
1. **Content Change Detection**: Monitor workshop content for changes
2. **Automatic Regeneration**: Update documentation when content changes
3. **Link Validation**: Verify all links and references remain valid
4. **Consistency Checks**: Ensure documentation remains consistent with content

### **9. Resource Configuration**

#### **Environment Variables**
```yaml
env:
- name: AGENT_NAME
  value: "documentation_pipeline"
- name: LLAMA_STACK_ENDPOINT
  value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
- name: INFERENCE_MODEL_ID
  value: "meta-llama/Llama-3.2-3B-Instruct"
```

#### **Resource Allocation**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1"
```

## Consequences

### **Positive**
- ✅ **Automated Documentation**: Reduces manual documentation overhead
- ✅ **Consistency Enforcement**: Ensures standardized documentation across workshops
- ✅ **Quality Assurance**: Maintains high documentation quality standards
- ✅ **Lifecycle Management**: Handles documentation throughout workshop lifecycle
- ✅ **Template Compliance**: Ensures compatibility with workshop platforms

### **Negative**
- ⚠️ **Generation Complexity**: Complex workshops may require sophisticated documentation generation
- ⚠️ **Template Dependency**: Tightly coupled to specific documentation templates
- ⚠️ **Maintenance Overhead**: Requires ongoing maintenance of documentation standards

### **Mitigation Strategies**
- **Template Flexibility**: Design for multiple documentation template support
- **Quality Monitoring**: Implement automated documentation quality checks
- **User Feedback**: Incorporate user feedback for documentation improvement

## Implementation Evidence

**OpenShift Deployment Status:**
```bash
$ oc get pods -n workshop-system | grep documentation-pipeline
documentation-pipeline-agent-6d47c8c48f-ph4nq   1/1     Running   0          11h
```

**Service Accessibility:**
```bash
$ oc get routes -n workshop-system | grep documentation-pipeline
documentation-pipeline-agent   documentation-pipeline-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
```

**Operational Documentation:**
- ✅ Agent responding to HTTP requests
- ✅ Tekton pipeline integration functional
- ✅ Documentation generation working
- ✅ Template compliance verified

## Related ADRs

- **ADR-0017**: Content Creator Agent (provides content for documentation)
- **ADR-0018**: Source Manager Agent (coordinates documentation deployment)
- **ADR-0019**: Research Validation Agent (validates documentation accuracy)
- **ADR-0023**: OpenShift Deployment Strategy (defines deployment patterns)

---

**This ADR documents the actual implemented and operational Documentation Pipeline Agent architecture as deployed in the Workshop Template System.**
