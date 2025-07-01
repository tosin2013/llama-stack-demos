# ADR-0019: Research Validation Agent Architecture

## Status
Accepted - **IMPLEMENTED AND OPERATIONAL**

## Context

The Research Validation Agent is responsible for technical accuracy validation, external source verification, and content quality assurance. This agent ensures workshop content meets technical standards and provides fact-checking capabilities for workshop materials.

**Current Implementation Status:**
- ✅ **DEPLOYED**: Running in OpenShift workshop-system namespace
- ✅ **OPERATIONAL**: Successfully providing validation services
- ✅ **PIPELINE-INTEGRATED**: Connected to Tekton workflows for validation tasks
- ✅ **API-ACCESSIBLE**: HTTP endpoints available for validation requests

## Decision

### **Agent Architecture**

#### **1. Validation-Focused Deployment**
```yaml
# Standard Agent Deployment Pattern
spec:
  containers:
  - name: research-validation-agent
    image: image-registry.openshift-image-registry.svc:5000/workshop-system/workshop-agent-system:latest
    command: ["python", "-m", "demos.workshop_template_system", "--agent-name", "research_validation", "--port", "8080"]
    env:
    - name: AGENT_NAME
      value: "research_validation"
    - name: LLAMA_STACK_ENDPOINT
      value: "http://llamastack-server.llama-serve.svc.cluster.local:8321"
    - name: INFERENCE_MODEL_ID
      value: "meta-llama/Llama-3.2-3B-Instruct"
```

**Key Features:**
- **Technical Validation**: Verify technical accuracy of workshop content
- **External Source Verification**: Validate information against authoritative sources
- **Content Quality Assurance**: Ensure workshop materials meet quality standards
- **Fact-Checking**: Cross-reference technical claims and procedures

#### **2. Validation Capabilities**

**Primary Functions:**
1. **Technical Accuracy Validation**: Verify code examples, commands, and procedures
2. **External Source Verification**: Check information against official documentation
3. **Content Compliance**: Ensure content meets workshop standards and guidelines
4. **Enhancement Analysis**: Analyze proposed workshop enhancements for accuracy
5. **Research Integration**: Incorporate latest technical information and best practices

**Tool Integration:**
```python
# Research Validation Tools
- validate_technical_content_tool: Verify technical accuracy of content
- verify_external_sources_tool: Check against authoritative sources
- analyze_content_quality_tool: Assess content quality and standards
- research_latest_practices_tool: Research current best practices
- validate_enhancement_proposal_tool: Verify enhancement proposals
```

#### **3. HTTP API Structure**
```bash
# Validation Endpoint
POST /invoke
Content-Type: application/json

{
  "tool_name": "validate_technical_content_tool",
  "parameters": {
    "content_type": "workshop_module",
    "content_path": "/workspace/shared-data/workshop-content",
    "validation_scope": "technical_accuracy",
    "reference_sources": ["official_docs", "best_practices"]
  }
}
```

**Response Format:**
```json
{
  "result": "# Validation Report\n\n## Technical Accuracy Assessment\n...",
  "status": "success",
  "validation_score": 0.92,
  "issues_found": 2,
  "recommendations": ["Update deprecated API usage", "Add error handling examples"]
}
```

### **4. Validation Methodology**

#### **Technical Accuracy Validation**
1. **Code Verification**: Validate code examples for syntax and functionality
2. **Command Validation**: Verify CLI commands and their expected outputs
3. **API Documentation**: Check API usage against current documentation
4. **Version Compatibility**: Ensure compatibility with specified software versions
5. **Best Practices**: Validate against current industry best practices

#### **External Source Verification**
1. **Official Documentation**: Cross-reference with official product documentation
2. **Community Standards**: Validate against community-accepted practices
3. **Security Guidelines**: Ensure compliance with security best practices
4. **Performance Considerations**: Validate performance recommendations
5. **Accessibility Standards**: Check accessibility compliance

### **5. Tekton Pipeline Integration**

#### **Agent Task Definition**
```yaml
apiVersion: tekton.dev/v1beta1
kind: Task
metadata:
  name: agent-task-research-validation
spec:
  params:
  - name: repository-url
  - name: validation-type
    default: "comprehensive"
  - name: content-scope
    default: "workshop_content"
  - name: agent-endpoint
    default: "http://research-validation-agent:80"
  
  results:
  - name: validation-status
  - name: validation-score
  - name: validation-report
  - name: recommendations
```

**Validation Workflows:**
1. **Pre-Creation Validation**: Validate source repository content before workshop creation
2. **Content Validation**: Validate generated workshop content for accuracy
3. **Enhancement Validation**: Validate proposed workshop enhancements
4. **Quality Assurance**: Final validation before workshop deployment

### **6. Quality Metrics and Scoring**

#### **Validation Scoring System**
```yaml
# Validation Metrics
technical_accuracy: 0.0-1.0    # Code and command accuracy
content_quality: 0.0-1.0       # Overall content quality
source_verification: 0.0-1.0   # External source validation
compliance_score: 0.0-1.0      # Standards compliance
overall_score: 0.0-1.0         # Weighted average
```

**Quality Thresholds:**
- **Excellent**: 0.90-1.00 (Ready for deployment)
- **Good**: 0.80-0.89 (Minor improvements needed)
- **Acceptable**: 0.70-0.79 (Moderate improvements needed)
- **Needs Work**: 0.60-0.69 (Significant improvements needed)
- **Unacceptable**: 0.00-0.59 (Major revision required)

### **7. Integration with Content Creation**

#### **Validation Checkpoints**
1. **Repository Analysis**: Validate source repository technical content
2. **Content Generation**: Validate generated workshop content
3. **Enhancement Proposals**: Validate proposed workshop improvements
4. **Pre-Deployment**: Final validation before workshop deployment

#### **Feedback Integration**
```yaml
# Validation Feedback Loop
validation_result → content_creator_agent → content_revision → re_validation
```

### **8. Resource Configuration**

#### **Environment Variables**
```yaml
env:
- name: AGENT_NAME
  value: "research_validation"
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
- ✅ **Quality Assurance**: Ensures high-quality, technically accurate workshop content
- ✅ **Automated Validation**: Reduces manual review overhead for content validation
- ✅ **Standardized Process**: Consistent validation methodology across all workshops
- ✅ **External Verification**: Validates content against authoritative sources
- ✅ **Continuous Improvement**: Provides feedback for content enhancement

### **Negative**
- ⚠️ **Validation Overhead**: Adds processing time to workshop creation pipeline
- ⚠️ **External Dependencies**: Relies on external sources for verification
- ⚠️ **Accuracy Limitations**: AI-based validation may miss subtle technical issues

### **Mitigation Strategies**
- **Parallel Processing**: Run validation in parallel with other pipeline tasks where possible
- **Caching**: Cache validation results for similar content to improve performance
- **Human Review**: Implement human review for critical validation decisions

## Implementation Evidence

**OpenShift Deployment Status:**
```bash
$ oc get pods -n workshop-system | grep research-validation
research-validation-agent-5ff47d76d9-jv5cb   1/1     Running   0          11h
```

**Service Accessibility:**
```bash
$ oc get routes -n workshop-system | grep research-validation
research-validation-agent   research-validation-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com
```

**Operational Validation:**
- ✅ Agent responding to HTTP requests
- ✅ Tekton pipeline integration functional
- ✅ Validation endpoints accessible
- ✅ Quality metrics generation working

## Related ADRs

- **ADR-0016**: Template Converter Agent (provides content for validation)
- **ADR-0017**: Content Creator Agent (receives validation feedback)
- **ADR-0020**: Documentation Pipeline Agent (coordinates with validation)
- **ADR-0023**: OpenShift Deployment Strategy (defines deployment patterns)

---

**This ADR documents the actual implemented and operational Research Validation Agent architecture as deployed in the Workshop Template System.**
