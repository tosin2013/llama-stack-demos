# Tekton Pipeline Testing Workflow with Middleware Integration

**Version**: 1.0.0  
**Date**: 2025-01-04  
**Status**: Active  

## 🎯 **Overview**

This guide documents the standardized workflow for testing Tekton pipelines in OpenShift using the Workshop Monitoring Service middleware. This approach ensures consistent parameter usage, prevents pipeline failures, and validates configurations before execution.

## 📋 **Prerequisites**

### **Required Access**
- OpenShift cluster access with `tkn` CLI
- Workshop Monitoring Service deployed and accessible
- Git repository with workshop content
- Proper RBAC permissions for pipeline execution

### **Required Tools**
```bash
# Verify required tools are available
oc version
tkn version
curl --version
jq --version  # Optional but recommended
```

### **Environment Setup**
```bash
# Set environment variables
export MIDDLEWARE_URL="https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com"
export NAMESPACE="workshop-system"
export REPO_URL="https://github.com/your-org/your-repo.git"
export WORKSHOP_NAME="test-workshop-$(date +%s)"
```

## 🔄 **Complete Testing Workflow**

### **Step 1: Verify Middleware Health**
```bash
# Check if middleware is accessible and healthy
curl -k "${MIDDLEWARE_URL}/api/monitoring/health"

# Expected response: {"status": "healthy", ...}
```

### **Step 2: Get Pipeline Configuration**
```bash
# Retrieve available pipelines and their configurations
curl -k "${MIDDLEWARE_URL}/api/pipeline/config" | jq '.'

# Get specific pipeline configuration
curl -k "${MIDDLEWARE_URL}/api/pipeline/config" | \
  jq '.data.pipelines."workflow-1-simple-corrected"'
```

**Example Response:**
```json
{
  "name": "workflow-1-simple-corrected",
  "description": "Workflow 1: Simple Corrected Test",
  "required_parameters": ["repository-url", "workshop-name", "base-template"],
  "required_workspaces": ["shared-data"],
  "validation_types_used": [],
  "parameter_defaults": {"base-template": "showroom_template_default"}
}
```

### **Step 3: Validate Parameters**
```bash
# Validate your parameters before pipeline execution
curl -k -X POST \
  -H "Content-Type: application/json" \
  -d "{
    \"repository-url\": \"${REPO_URL}\",
    \"workshop-name\": \"${WORKSHOP_NAME}\",
    \"base-template\": \"showroom_template_default\"
  }" \
  "${MIDDLEWARE_URL}/api/pipeline/validate-parameters"
```

**Expected Success Response:**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "errors": [],
    "warnings": [],
    "message": "All parameters are valid"
  }
}
```

### **Step 4: Get Correct Workspace Configuration**
```bash
# Get workspace PVC information
curl -k "${MIDDLEWARE_URL}/api/pipeline/config" | \
  jq '.data.workspaces."shared-data".pvc_name'

# Verify PVC exists in OpenShift
oc get pvc workshop-shared-pvc -n "${NAMESPACE}"
```

### **Step 5: Execute Pipeline with Validated Parameters**

#### **Option A: Manual Execution**
```bash
# Execute pipeline with middleware-validated parameters
tkn pipeline start workflow-1-simple-corrected \
  -n "${NAMESPACE}" \
  --param repository-url="${REPO_URL}" \
  --param workshop-name="${WORKSHOP_NAME}" \
  --param base-template="showroom_template_default" \
  --workspace name=shared-data,claimName=workshop-shared-pvc
```

#### **Option B: Automated Script (Recommended)**
```bash
# Use the dynamic pipeline script for automated parameter handling
./scripts/run-pipeline-with-config.sh \
  workflow-1-simple-corrected \
  "${REPO_URL}" \
  "${WORKSHOP_NAME}"
```

### **Step 6: Monitor Pipeline Execution**
```bash
# Get the latest pipeline run
PIPELINE_RUN=$(tkn pipelinerun list -n "${NAMESPACE}" --limit 1 -o name | head -1)

# Follow logs in real-time
tkn pipelinerun logs "${PIPELINE_RUN}" -f -n "${NAMESPACE}"

# Check pipeline run status
tkn pipelinerun describe "${PIPELINE_RUN}" -n "${NAMESPACE}"
```

### **Step 7: Verify Results and Agent Health**
```bash
# Check agent health after pipeline execution
curl -k "${MIDDLEWARE_URL}/api/monitoring/agents"

# Check for any pipeline-related events
oc get events -n "${NAMESPACE}" --sort-by='.lastTimestamp' | tail -10

# Verify workspace content (if accessible)
oc get pods -n "${NAMESPACE}" | grep "${PIPELINE_RUN}"
```

## 🚨 **Common Issues and Solutions**

### **Issue 1: Invalid Validation Type**
```bash
# ❌ WRONG - Using undefined validation type
tkn pipeline start workflow-1-intelligent-workshop \
  --param validation-type=intelligent-workshop-validation

# ✅ CORRECT - Use middleware to get valid types
curl -k "${MIDDLEWARE_URL}/api/pipeline/config" | \
  jq '.data.validation_types."research-validation".supported_types'

# Use: new-workshop-validation, enhancement-analysis, or enhancement-validation
```

### **Issue 2: Wrong Workspace PVC**
```bash
# ❌ WRONG - Using non-existent PVC
--workspace name=shared-data,claimName=shared-workspace-pvc

# ✅ CORRECT - Use existing PVC
--workspace name=shared-data,claimName=workshop-shared-pvc
```

### **Issue 3: Parameter Validation Failures**
```bash
# Check parameter validation errors
curl -k -X POST \
  -H "Content-Type: application/json" \
  -d '{"repository-url": "invalid-url", "workshop-name": "Invalid Name!"}' \
  "${MIDDLEWARE_URL}/api/pipeline/validate-parameters" | jq '.data.errors'
```

## 📝 **Pipeline-Specific Workflows**

### **workflow-1-simple-corrected**
```bash
# Minimal parameters required
./scripts/run-pipeline-with-config.sh \
  workflow-1-simple-corrected \
  "https://github.com/jeremyrdavis/dddhexagonalworkshop.git" \
  "ddd-workshop-test"
```

### **workflow-1-intelligent-workshop**
```bash
# Requires additional parameters and validation
./scripts/run-pipeline-with-config.sh \
  workflow-1-intelligent-workshop \
  "https://github.com/tosin2013/ansible-controller-cac.git" \
  "ansible-workshop-test"
```

### **workflow-3-enhance-workshop**
```bash
# Requires original workshop URL
./scripts/run-pipeline-with-config.sh \
  workflow-3-enhance-workshop \
  "https://github.com/user/enhanced-repo.git" \
  "enhanced-workshop" \
  "--param original-workshop-url=https://github.com/original/repo.git"
```

## 🔧 **Troubleshooting Commands**

### **Debug Middleware Issues**
```bash
# Check middleware deployment
oc get deployment workshop-monitoring-service -n "${NAMESPACE}"

# Check middleware logs
oc logs deployment/workshop-monitoring-service -n "${NAMESPACE}" --tail=50

# Test middleware endpoints
curl -k "${MIDDLEWARE_URL}/api/monitoring/info"
```

### **Debug Pipeline Issues**
```bash
# List recent pipeline runs
tkn pipelinerun list -n "${NAMESPACE}" --limit 5

# Get detailed pipeline run information
tkn pipelinerun describe <pipeline-run-name> -n "${NAMESPACE}"

# Check task-specific logs
tkn taskrun logs <task-run-name> -n "${NAMESPACE}"
```

### **Debug Workspace Issues**
```bash
# Check PVC status
oc get pvc -n "${NAMESPACE}"

# Check PVC usage
oc describe pvc workshop-shared-pvc -n "${NAMESPACE}"

# Check storage class
oc get storageclass ocs-storagecluster-cephfs
```

## ✅ **Best Practices**

1. **Always use middleware configuration** - Don't hardcode parameters
2. **Validate parameters first** - Catch errors before pipeline execution
3. **Use the dynamic script** - Reduces manual errors and ensures consistency
4. **Monitor pipeline execution** - Don't start and forget
5. **Check agent health** - Verify system state after pipeline completion
6. **Use meaningful workshop names** - Include timestamps or unique identifiers
7. **Clean up test resources** - Remove test workshops and data regularly

## 📚 **Related Documentation**

- [ADR-0036: Pipeline Parameter and Validation Type Standards](../adrs/ADR-0036-pipeline-parameter-validation-standards.md)
- [Pipeline Parameters Reference](../reference/pipeline-parameters.md)
- [Git and Build Workflow Safety Rules](../../rules/git-build-workflow-safety-rules.json)
- [Dynamic Pipeline Script](../../scripts/run-pipeline-with-config.sh)

---

**Maintained by**: Workshop Template System Development Team  
**Last Updated**: 2025-01-04  
**Next Review**: 2025-02-04
