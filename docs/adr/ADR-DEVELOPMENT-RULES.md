# ADR Development Rules and Guidelines

## 🎯 **Purpose**

This document establishes mandatory rules and best practices for using Architecture Decision Records (ADRs) during Workshop Template System development. These rules ensure architectural consistency, prevent technical debt, and maintain system coherence across all development activities.

## 📋 **Mandatory Development Rules**

### **Rule 1: ADR-First Development**
**MANDATORY**: Before implementing any new feature or making architectural changes, developers MUST:

1. **Review Relevant ADRs**: Identify and read all ADRs that relate to your development area
2. **Verify Compliance**: Ensure your implementation aligns with existing architectural decisions
3. **Identify Gaps**: If no relevant ADR exists, create one before implementation
4. **Document Deviations**: Any deviation from ADR guidance requires explicit justification and approval

**Example Workflow:**
```bash
# Before implementing new agent functionality
1. Read ADR-0015 through ADR-0020 (Agent Architectures)
2. Review ADR-0026 (LLM Infrastructure) for AI integration
3. Check ADR-0025 (Kubernetes Deployment) for deployment patterns
4. Implement following ADR guidance
5. Update ADR if implementation reveals new architectural insights
```

### **Rule 2: Component-Specific ADR Compliance**

#### **Agent Development (ADR-0015 through ADR-0020)**
**MANDATORY Requirements:**
- ✅ Use `@client_tool` decorator pattern for all agent tools
- ✅ Implement health check endpoint at `/agent-card`
- ✅ Follow single container image strategy with agent selection via CLI
- ✅ Integrate with Llama Stack endpoint as defined in ADR-0026
- ✅ Include safety validation for content generation (ADR-0027)
- ✅ Support workspace coordination patterns (ADR-0007, ADR-0008)

**Code Template Compliance:**
```python
# MANDATORY: Follow this pattern for all agent tools
@client_tool
def your_agent_tool(parameter: str, optional_param: str = "default") -> str:
    """
    :description: Clear description of tool functionality
    :use_case: Specific use case for this tool
    :param parameter: Description of required parameter
    :param optional_param: Description of optional parameter
    :returns: Description of return value
    """
    # Implementation following ADR guidance
```

#### **LLM Integration (ADR-0026)**
**MANDATORY Requirements:**
- ✅ Use model selection strategy based on task complexity
- ✅ Integrate safety validation for all content generation
- ✅ Follow resource allocation patterns for GPU usage
- ✅ Implement proper error handling for LLM communication
- ✅ Use environment variables for model endpoint configuration

**Model Selection Rules:**
```python
# MANDATORY: Follow model selection strategy
MODEL_SELECTION = {
    'conversation': 'llama-3b',        # Fast inference for chat
    'content_generation': 'llama-70b', # High-capacity for creation
    'code_analysis': 'granite',        # Specialized for code
    'safety_check': 'llama-guard'      # Content moderation
}
```

#### **Kubernetes Deployment (ADR-0025)**
**MANDATORY Requirements:**
- ✅ Use Kustomize overlay structure for all deployments
- ✅ Implement health checks (liveness and readiness probes)
- ✅ Follow resource allocation patterns (requests and limits)
- ✅ Use shared workspace PVC for agent coordination
- ✅ Include proper labels and annotations for monitoring

**Deployment Template Compliance:**
```yaml
# MANDATORY: Include these elements in all deployments
livenessProbe:
  httpGet:
    path: /agent-card
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /agent-card
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5

resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1"
```

### **Rule 3: Safety and Content Moderation (ADR-0027)**
**MANDATORY**: All content generation MUST include safety validation:

```python
# MANDATORY: Safety validation for all generated content
def generate_content_with_safety(content_request: str) -> dict:
    # Generate content
    raw_content = generate_content(content_request)
    
    # MANDATORY: Safety validation
    safety_result = validate_content_safety(raw_content, "educational_standard")
    
    if not safety_result['is_safe']:
        # MANDATORY: Regenerate with safety feedback
        return regenerate_with_safety_feedback(content_request, safety_result['feedback'])
    
    return {'content': raw_content, 'safety_validated': True}
```

### **Rule 4: MCP Server Integration (ADR-0028)**
**MANDATORY**: When integrating external tools:

1. **Use MCP Protocol**: All external tool integration MUST use MCP servers
2. **Service Discovery**: Use MCP server registry for endpoint discovery
3. **Error Handling**: Implement circuit breaker patterns for MCP communication
4. **Authentication**: Follow MCP authentication patterns for each service type

**MCP Integration Template:**
```python
# MANDATORY: MCP integration pattern
def call_mcp_tool(server_name: str, tool_name: str, parameters: dict) -> dict:
    mcp_endpoint = get_mcp_endpoint(server_name)
    
    mcp_request = {
        "jsonrpc": "2.0",
        "id": str(uuid.uuid4()),
        "method": "tools/call",
        "params": {
            "name": tool_name,
            "arguments": parameters
        }
    }
    
    # MANDATORY: Error handling and timeout
    try:
        response = requests.post(f"{mcp_endpoint}/mcp", json=mcp_request, timeout=30)
        return handle_mcp_response(response)
    except Exception as e:
        return handle_mcp_error(e, server_name, tool_name)
```

## 🔍 **Development Workflow Rules**

### **Rule 5: Pre-Development Checklist**
**MANDATORY**: Before starting any development task:

- [ ] **ADR Review**: Read all relevant ADRs for your development area
- [ ] **Architecture Alignment**: Verify your approach aligns with existing decisions
- [ ] **Dependency Check**: Identify dependencies on other components and their ADRs
- [ ] **Implementation Evidence**: Review actual implementation examples in ADRs
- [ ] **Testing Strategy**: Plan testing approach based on ADR verification criteria

### **Rule 6: Implementation Validation**
**MANDATORY**: During implementation:

- [ ] **Code Review**: Ensure code follows ADR patterns and examples
- [ ] **Integration Testing**: Test integration points defined in ADRs
- [ ] **Performance Validation**: Verify performance meets ADR requirements
- [ ] **Safety Compliance**: Validate safety requirements are met
- [ ] **Documentation Update**: Update ADR implementation evidence if needed

### **Rule 7: Post-Implementation Requirements**
**MANDATORY**: After completing implementation:

- [ ] **ADR Compliance Verification**: Confirm implementation follows all relevant ADR guidance
- [ ] **Integration Testing**: Test with other components following ADR integration patterns
- [ ] **Performance Benchmarking**: Verify performance meets ADR specifications
- [ ] **Documentation Updates**: Update ADRs with new implementation evidence if applicable
- [ ] **Architectural Review**: Submit for architectural review if changes impact multiple ADRs

## 📊 **ADR Reference Matrix for Development**

### **By Development Area**

| Development Area | Primary ADRs | Secondary ADRs | Key Requirements |
|------------------|--------------|----------------|------------------|
| **Agent Development** | ADR-0015 to ADR-0020 | ADR-0026, ADR-0027 | Tool patterns, LLM integration, safety |
| **LLM Integration** | ADR-0026 | ADR-0027, ADR-0015 | Model selection, safety validation |
| **Kubernetes Deployment** | ADR-0025 | ADR-0023, ADR-0007 | Kustomize patterns, health checks |
| **Frontend Development** | ADR-0013, ADR-0024 | ADR-0014, ADR-0021 | React patterns, monitoring integration |
| **External Tool Integration** | ADR-0028 | ADR-0018, ADR-0025 | MCP protocol, service discovery |
| **Multi-Workshop Features** | ADR-0029 | ADR-0025, ADR-0015 | Context isolation, resource sharing |
| **Safety Implementation** | ADR-0027 | ADR-0026, ADR-0017 | Content validation, safety policies |
| **Monitoring Features** | ADR-0024 | ADR-0013, ADR-0014 | Dashboard patterns, API integration |
| **Tekton Integration** | ADR-0006 | ADR-0003, ADR-0025 | Pipeline tasks, human oversight |

### **By Component Type**

| Component | Must Follow ADRs | Implementation Requirements |
|-----------|------------------|----------------------------|
| **New Agent** | ADR-0015 to ADR-0020, ADR-0026, ADR-0027 | Tool patterns, LLM integration, safety validation |
| **Kubernetes Manifest** | ADR-0025, ADR-0023 | Kustomize structure, health checks, resource limits |
| **Frontend Component** | ADR-0013, ADR-0024 | React patterns, API integration, monitoring |
| **LLM Integration** | ADR-0026, ADR-0027 | Model selection, safety validation, resource management |
| **External Tool** | ADR-0028 | MCP protocol, authentication, error handling |
| **Workshop Instance** | ADR-0029, ADR-0025 | Context isolation, shared infrastructure |

## ⚠️ **Common Violations and How to Avoid Them**

### **Violation 1: Bypassing Safety Validation**
**WRONG:**
```python
# DON'T: Direct content generation without safety validation
def create_content(request):
    return llm_generate(request)  # Missing safety validation
```

**CORRECT:**
```python
# DO: Always include safety validation
def create_content(request):
    content = llm_generate(request)
    safety_result = validate_content_safety(content, "educational_standard")
    return content if safety_result['is_safe'] else regenerate_with_safety_feedback(request, safety_result['feedback'])
```

### **Violation 2: Ignoring Model Selection Strategy**
**WRONG:**
```python
# DON'T: Use random model selection
model = "llama-70b"  # Always using high-capacity model
```

**CORRECT:**
```python
# DO: Follow model selection strategy from ADR-0026
model = MODEL_SELECTION.get(task_type, 'llama-3b')
```

### **Violation 3: Missing Health Checks**
**WRONG:**
```yaml
# DON'T: Deploy without health checks
spec:
  containers:
  - name: agent
    image: agent:latest
    # Missing health checks
```

**CORRECT:**
```yaml
# DO: Include mandatory health checks
spec:
  containers:
  - name: agent
    image: agent:latest
    livenessProbe:
      httpGet:
        path: /agent-card
        port: 8080
    readinessProbe:
      httpGet:
        path: /agent-card
        port: 8080
```

## 🔧 **Development Tools and Automation**

### **ADR Compliance Checker Script**
```bash
#!/bin/bash
# adr-compliance-check.sh - Validate code against ADR requirements

echo "🔍 Checking ADR Compliance..."

# Check for mandatory health check endpoints
if ! grep -r "/agent-card" --include="*.py" .; then
    echo "❌ Missing /agent-card health check endpoint (ADR-0015 to ADR-0020)"
    exit 1
fi

# Check for safety validation in content generation
if grep -r "generate.*content" --include="*.py" . | grep -v "safety"; then
    echo "⚠️  Content generation without safety validation detected (ADR-0027)"
fi

# Check for proper MCP integration
if grep -r "requests.post" --include="*.py" . | grep -v "mcp"; then
    echo "⚠️  Direct HTTP calls detected - consider MCP integration (ADR-0028)"
fi

echo "✅ ADR compliance check completed"
```

### **ADR Template Generator**
```bash
#!/bin/bash
# generate-adr-compliant-agent.sh - Generate ADR-compliant agent template

AGENT_NAME=$1
echo "🚀 Generating ADR-compliant agent: $AGENT_NAME"

# Generate agent structure following ADR patterns
mkdir -p "demos/workshop_template_system/agents/$AGENT_NAME"
cat > "demos/workshop_template_system/agents/$AGENT_NAME/agent.py" << EOF
# ADR-Compliant Agent Template
# Follows: ADR-0015 to ADR-0020, ADR-0026, ADR-0027

from common.agent_base import AgentBase
from .tools import *
from .config import *

class ${AGENT_NAME^}Agent(AgentBase):
    def __init__(self, port: int = 8080):
        super().__init__(
            agent_name="$AGENT_NAME",
            port=port,
            tools=[
                # Add your tools here following @client_tool pattern
            ]
        )
    
    # MANDATORY: Health check endpoint (ADR-0015 to ADR-0020)
    def get_agent_card(self):
        return {
            "agent_name": "$AGENT_NAME",
            "status": "healthy",
            "capabilities": [],
            "version": "1.0.0"
        }
EOF

echo "✅ ADR-compliant agent template generated"
```

## 📚 **Quick Reference Commands**

### **Find Relevant ADRs**
```bash
# Find ADRs related to your development area
grep -r "your_keyword" docs/adr/ --include="*.md"

# List all ADRs by category
grep -h "^#.*ADR-" docs/adr/ADR-INDEX.md
```

### **Validate Implementation**
```bash
# Check ADR compliance
./scripts/adr-compliance-check.sh

# Validate against specific ADR
./scripts/validate-adr-compliance.sh ADR-0026
```

### **Update ADR Implementation Evidence**
```bash
# When your implementation adds new evidence to an ADR
# Update the "Implementation Evidence" section with:
# - New file paths
# - Code snippets
# - Operational evidence
# - Performance metrics
```

## 🔧 **Development Tools and Automation**

### **ADR Compliance Checker Script**
```bash
#!/bin/bash
# adr-compliance-check.sh - Validate code against ADR requirements

echo "🔍 Checking ADR Compliance..."

# Check for mandatory health check endpoints
if ! grep -r "/agent-card" --include="*.py" .; then
    echo "❌ Missing /agent-card health check endpoint (ADR-0015 to ADR-0020)"
    exit 1
fi

# Check for safety validation in content generation
if grep -r "generate.*content" --include="*.py" . | grep -v "safety"; then
    echo "⚠️  Content generation without safety validation detected (ADR-0027)"
fi

# Check for proper MCP integration
if grep -r "requests.post" --include="*.py" . | grep -v "mcp"; then
    echo "⚠️  Direct HTTP calls detected - consider MCP integration (ADR-0028)"
fi

echo "✅ ADR compliance check completed"
```

### **Quick Reference Commands**
```bash
# Find ADRs related to your development area
grep -r "your_keyword" docs/adr/ --include="*.md"

# List all ADRs by category
grep -h "^#.*ADR-" docs/adr/ADR-INDEX.md

# Check ADR compliance
./scripts/adr-compliance-check.sh

# Validate against specific ADR
./scripts/validate-adr-compliance.sh ADR-0026
```

## 🎯 **Success Criteria**

Development is considered ADR-compliant when:

- ✅ All relevant ADRs have been reviewed and followed
- ✅ Implementation includes all mandatory requirements
- ✅ Code follows established patterns and templates
- ✅ Integration points work as defined in ADRs
- ✅ Safety and performance requirements are met
- ✅ Documentation is updated with implementation evidence

## 📞 **Support and Escalation**

**For ADR Questions:**
1. Review the specific ADR and its implementation evidence
2. Check related ADRs for additional context
3. Consult the Architecture Overview Map for system-wide context
4. Escalate to architecture team for clarification if needed

**For ADR Updates:**
1. Propose changes through standard review process
2. Ensure changes maintain architectural coherence
3. Update related ADRs if changes have dependencies
4. Document rationale for architectural decisions

---

**These development rules ensure that the sophisticated Workshop Template System architecture documented in our ADRs is properly implemented and maintained throughout the development lifecycle.**
