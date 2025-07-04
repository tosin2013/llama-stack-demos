# Pipeline Parameters Reference

**Last Updated**: 2025-01-04  
**Version**: 1.0.0  
**Status**: Authoritative Reference  

## 📋 **Overview**

This document provides the authoritative reference for all pipeline parameters and validation types in the Workshop Template System. All pipeline definitions MUST use only the parameters and values documented here.

## 🎯 **Validation Types by Agent**

### **Research Validation Agent**
**Endpoint**: `https://research-validation-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`

| Validation Type | Description | Use Case | Required Parameters |
|----------------|-------------|----------|-------------------|
| `new-workshop-validation` | Validates new workshop content for accuracy and completeness | New workshop creation workflows | `workshop-content` |
| `enhancement-analysis` | Analyzes existing workshops for enhancement opportunities | Workshop enhancement workflows | `repository-url`, `original-workshop-url` |
| `enhancement-validation` | Validates enhanced workshop content against original | Enhancement validation workflows | `workshop-content`, `original-content` |

### **Template Converter Agent**
**Endpoint**: `https://template-converter-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`

| Analysis Type | Description | Use Case | Required Parameters |
|--------------|-------------|----------|-------------------|
| `repository-analysis` | Analyzes repository structure and content | Repository classification | `repository-url` |
| `workshop-detection` | Detects existing workshop type and framework | Workshop type identification | `repository-url` |
| `template-classification` | Classifies workshop template requirements | Template selection | `repository-url`, `workshop-type` |

### **Content Creator Agent**
**Endpoint**: `https://content-creator-agent-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com`

| Content Type | Description | Use Case | Required Parameters |
|-------------|-------------|----------|-------------------|
| `content-generation` | Generates new workshop content | New workshop creation | `repository-url`, `workshop-name` |
| `content-enhancement` | Enhances existing workshop content | Workshop improvement | `repository-url`, `original-content` |
| `content-validation` | Validates generated content quality | Quality assurance | `workshop-content` |

## 📝 **Standard Pipeline Parameters**

### **Required Parameters (All Pipelines)**

#### **repository-url**
```yaml
name: repository-url
type: string
description: Source repository URL for workshop creation
required: true
format: https://github.com/owner/repo.git
validation: ^https://github\.com/[\w-]+/[\w-]+(\.git)?$
examples:
  - https://github.com/jeremyrdavis/dddhexagonalworkshop.git
  - https://github.com/tosin2013/ansible-controller-cac.git
```

#### **workshop-name**
```yaml
name: workshop-name
type: string
description: Name for the new workshop (used for file naming and identification)
required: true
format: lowercase-with-hyphens
validation: ^[a-z0-9-]+$
max_length: 50
examples:
  - ddd-hexagonal-workshop
  - ansible-automation-basics
  - openshift-deployment-guide
```

### **Optional Parameters (Common)**

#### **base-template**
```yaml
name: base-template
type: string
description: Base template to use for workshop creation
required: false
default: showroom_template_default
valid_values:
  - showroom_template_default
  - antora_template
  - custom_template
examples:
  - showroom_template_default
  - antora_template
```

#### **auto-approve**
```yaml
name: auto-approve
type: string
description: Auto-approve human-in-the-loop steps (for testing)
required: false
default: "false"
valid_values: ["true", "false"]
examples:
  - "true"   # For automated testing
  - "false"  # For production workflows
```

#### **human-approver**
```yaml
name: human-approver
type: string
description: Human approver for manual approval steps
required: false
default: system-operator
format: alphanumeric-with-hyphens
validation: ^[a-z0-9-]+$
examples:
  - system-operator
  - workshop-admin
  - content-reviewer
```

### **Agent-Specific Parameters**

#### **Research Validation Parameters**

##### **validation-type**
```yaml
name: validation-type
type: string
description: Type of validation to perform
required: true
default: new-workshop-validation
valid_values:
  - new-workshop-validation
  - enhancement-analysis
  - enhancement-validation
agent: research-validation-agent
examples:
  - new-workshop-validation  # For new workshops
  - enhancement-analysis     # For enhancement planning
  - enhancement-validation   # For validating enhancements
```

##### **workshop-content**
```yaml
name: workshop-content
type: string
description: Workshop content to validate (markdown or structured content)
required: false
default: ""
format: text/markdown or JSON
agent: research-validation-agent
```

##### **original-content**
```yaml
name: original-content
type: string
description: Original workshop content for comparison (enhancement workflows)
required: false
default: ""
format: text/markdown or JSON
agent: research-validation-agent
```

#### **Template Converter Parameters**

##### **analysis-type**
```yaml
name: analysis-type
type: string
description: Type of repository analysis to perform
required: true
default: repository-analysis
valid_values:
  - repository-analysis
  - workshop-detection
  - template-classification
agent: template-converter-agent
```

##### **workshop-type**
```yaml
name: workshop-type
type: string
description: Expected workshop type for classification
required: false
default: ""
valid_values:
  - tutorial_content
  - hands_on_lab
  - demonstration
  - reference_guide
agent: template-converter-agent
```

## 🔧 **Pipeline-Specific Parameter Sets**

### **workflow-1-intelligent-workshop**
```yaml
required_parameters:
  - repository-url
  - workshop-name
  - auto-detect-workflow
  - human-approver
  - auto-approve

parameter_defaults:
  auto-detect-workflow: "true"
  human-approver: "system-operator"
  auto-approve: "false"

validation_types_used:
  - new-workshop-validation  # ✅ CORRECTED from intelligent-workshop-validation
```

### **workflow-1-simple-corrected**
```yaml
required_parameters:
  - repository-url
  - workshop-name
  - base-template

parameter_defaults:
  base-template: "showroom_template_default"

validation_types_used:
  - None  # This pipeline skips validation for simplicity
```

### **workflow-3-enhance-workshop**
```yaml
required_parameters:
  - repository-url
  - workshop-name
  - original-workshop-url

validation_types_used:
  - enhancement-analysis
  - enhancement-validation
```

## ⚠️ **Common Mistakes and Fixes**

### **❌ Invalid Validation Types**
```yaml
# WRONG - These validation types don't exist
validation-type: intelligent-workshop-validation  # ❌ NOT IMPLEMENTED
validation-type: smart-content-validation         # ❌ NOT IMPLEMENTED
validation-type: ai-powered-validation            # ❌ NOT IMPLEMENTED

# CORRECT - Use documented validation types
validation-type: new-workshop-validation          # ✅ IMPLEMENTED
validation-type: enhancement-analysis             # ✅ IMPLEMENTED
validation-type: enhancement-validation           # ✅ IMPLEMENTED
```

### **❌ Invalid Parameter Formats**
```yaml
# WRONG - Invalid repository URL format
repository-url: github.com/user/repo              # ❌ Missing https://
repository-url: https://gitlab.com/user/repo.git  # ❌ Only GitHub supported

# CORRECT - Valid repository URL format
repository-url: https://github.com/user/repo.git  # ✅ VALID
repository-url: https://github.com/user/repo      # ✅ VALID (.git optional)
```

### **❌ Invalid Workshop Names**
```yaml
# WRONG - Invalid workshop name formats
workshop-name: "My Workshop Name"                 # ❌ Spaces not allowed
workshop-name: "workshop_with_underscores"        # ❌ Underscores not allowed
workshop-name: "Workshop-With-Capitals"           # ❌ Capitals not allowed

# CORRECT - Valid workshop name formats
workshop-name: "my-workshop-name"                 # ✅ VALID
workshop-name: "workshop-123"                     # ✅ VALID
workshop-name: "simple-demo"                      # ✅ VALID
```

## 🛠️ **Validation Tools**

### **Pipeline Validation Script**
```bash
# Usage: ./validate-pipeline.sh pipeline-file.yaml
curl -s https://raw.githubusercontent.com/workshop-template-system/tools/main/validate-pipeline.sh | bash -s pipeline-file.yaml
```

### **Parameter Validation API**
```bash
# Validate parameters before pipeline execution
curl -X POST https://workshop-monitoring-service-workshop-system.apps.cluster-9cfzr.9cfzr.sandbox180.opentlc.com/api/validate-parameters \
  -H "Content-Type: application/json" \
  -d '{"repository-url": "https://github.com/user/repo.git", "validation-type": "new-workshop-validation"}'
```

---

**Maintained by**: Workshop Template System Development Team  
**Review Schedule**: Monthly  
**Last Validation**: 2025-01-04  
**Next Review**: 2025-02-04
