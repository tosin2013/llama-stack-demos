# ADR-0036: Pipeline Parameter and Validation Type Standards

## Status
Proposed

## Context

The Workshop Template System has experienced pipeline failures due to inconsistencies between pipeline definitions and agent implementations. Specifically:

**Critical Issue**: Pipeline `workflow-1-intelligent-workshop` fails because it uses `validation-type: intelligent-workshop-validation` which is not implemented in the research validation agent.

**Root Cause Analysis**:
- No authoritative documentation of supported validation types
- Pipeline parameters defined without verification against agent implementations
- Assumptions about validation types leading to runtime failures
- Inconsistent parameter naming and usage across pipelines

**Evidence from Recent Failures**:
```bash
# Failed pipeline run
tkn taskrun logs workflow-1-intelligent-workshop-run-p756z-content-validation
# Error: Unknown validation type: intelligent-workshop-validation
```

**Current State**:
- Research Validation Agent supports: `new-workshop-validation`, `enhancement-analysis`, `enhancement-validation`
- Pipeline definitions reference unsupported types
- No parameter validation at pipeline creation time
- Missing documentation for valid parameter values

## Decision

Implement comprehensive pipeline parameter and validation type standards:

### 1. Validation Type Registry and Standards

#### **Standardized Validation Types**
```yaml
# Research Validation Agent
research_validation_types:
  - new-workshop-validation      # For new workshop content validation
  - enhancement-analysis         # For analyzing enhancement requirements  
  - enhancement-validation       # For validating enhanced content

# Template Converter Agent  
template_converter_types:
  - repository-analysis          # For repository structure analysis
  - workshop-detection          # For detecting existing workshop type
  - template-classification     # For classifying workshop templates

# Content Creator Agent
content_creator_types:
  - content-generation          # For generating new workshop content
  - content-enhancement         # For enhancing existing content
  - content-validation          # For validating generated content
```

#### **Validation Type Naming Convention**
- Format: `{action}-{target}` (e.g., `new-workshop-validation`)
- Use lowercase with hyphens for separation
- Be specific about the validation purpose
- Avoid generic terms like "intelligent" or "smart"

### 2. Pipeline Parameter Standards

#### **Required Parameters (All Pipelines)**
```yaml
required_parameters:
  repository-url:
    type: string
    description: "Source repository URL (must be valid Git URL)"
    format: "https://github.com/owner/repo.git"
    validation: "^https://github\\.com/[\\w-]+/[\\w-]+(\\.git)?$"
    
  workshop-name:
    type: string  
    description: "Name for the workshop (alphanumeric, hyphens allowed)"
    format: "my-workshop-name"
    validation: "^[a-z0-9-]+$"
    max_length: 50
```

#### **Agent-Specific Parameters**
```yaml
research_validation_parameters:
  validation-type:
    type: string
    description: "Type of validation to perform"
    required: true
    valid_values: 
      - "new-workshop-validation"
      - "enhancement-analysis" 
      - "enhancement-validation"
    default: "new-workshop-validation"
    
  workshop-content:
    type: string
    description: "Workshop content to validate"
    required: false
    default: ""
    
template_converter_parameters:
  analysis-type:
    type: string
    description: "Type of repository analysis"
    required: true
    valid_values:
      - "repository-analysis"
      - "workshop-detection"
      - "template-classification"
    default: "repository-analysis"
```

### 3. Parameter Validation Framework

#### **Pipeline-Level Validation**
```yaml
# Add to all pipeline definitions
apiVersion: tekton.dev/v1beta1
kind: Pipeline
metadata:
  annotations:
    # Parameter validation schema
    workshop.template.system/parameter-schema: |
      {
        "repository-url": {
          "type": "string",
          "pattern": "^https://github\\.com/[\\w-]+/[\\w-]+(\\.git)?$",
          "required": true
        },
        "validation-type": {
          "type": "string", 
          "enum": ["new-workshop-validation", "enhancement-analysis", "enhancement-validation"],
          "default": "new-workshop-validation"
        }
      }
```

#### **Agent-Level Validation**
```java
// Quarkus middleware validation
@ApplicationScoped
public class ParameterValidationService {
    
    private static final Set<String> VALID_VALIDATION_TYPES = Set.of(
        "new-workshop-validation",
        "enhancement-analysis", 
        "enhancement-validation"
    );
    
    public void validateParameters(Map<String, String> params) {
        String validationType = params.get("validation-type");
        if (validationType != null && !VALID_VALIDATION_TYPES.contains(validationType)) {
            throw new IllegalArgumentException(
                "Invalid validation-type: " + validationType + 
                ". Valid types: " + VALID_VALIDATION_TYPES
            );
        }
    }
}
```

### 4. Documentation and Tooling

#### **Authoritative Parameter Reference**
- Location: `docs/reference/pipeline-parameters.md`
- Format: Structured YAML with descriptions and examples
- Maintenance: Updated automatically from agent configurations
- Validation: CI/CD checks for consistency

#### **Validation Tooling**
```bash
# Pipeline validation script
#!/bin/bash
# validate-pipeline-config.sh

PIPELINE_FILE=$1
echo "Validating pipeline configuration: $PIPELINE_FILE"

# Check for required parameters
yq eval '.spec.params[] | select(.name == "repository-url")' $PIPELINE_FILE > /dev/null
if [ $? -ne 0 ]; then
    echo "ERROR: Missing required parameter: repository-url"
    exit 1
fi

# Validate validation-type values
VALIDATION_TYPES=$(yq eval '.spec.tasks[].params[] | select(.name == "validation-type") | .value' $PIPELINE_FILE)
for TYPE in $VALIDATION_TYPES; do
    if [[ ! "$TYPE" =~ ^(new-workshop-validation|enhancement-analysis|enhancement-validation)$ ]]; then
        echo "ERROR: Invalid validation-type: $TYPE"
        exit 1
    fi
done

echo "✅ Pipeline configuration is valid"
```

## Implementation Plan

### Phase 1: Immediate Fixes (Week 1)
1. **Fix Current Pipeline Failures**
   - Update `workflow-1-intelligent-workshop` to use `new-workshop-validation`
   - Test all existing pipelines with corrected parameters
   - Document current validation types and parameters

2. **Create Parameter Reference**
   - Document all currently supported validation types
   - Create parameter reference documentation
   - Add validation to critical pipelines

### Phase 2: Standardization (Week 2)
1. **Implement Validation Framework**
   - Add parameter validation to Quarkus middleware
   - Create pipeline validation tooling
   - Update all pipeline definitions with standards

2. **Agent Registration System**
   - Implement validation type registration in agents
   - Add health checks for supported validation types
   - Create API for discovering available validation types

### Phase 3: Automation (Week 3)
1. **CI/CD Integration**
   - Add pipeline validation to CI/CD
   - Automate parameter reference generation
   - Implement breaking change detection

2. **Migration and Testing**
   - Migrate all existing pipelines to new standards
   - Test end-to-end workflows with validated parameters
   - Create migration guide for future changes

## Consequences

### Positive
- **Eliminated Pipeline Failures**: No more failures due to undefined validation types
- **Improved Developer Experience**: Clear documentation of valid parameters
- **Automated Validation**: Catch configuration errors before deployment
- **Consistent Standards**: Uniform parameter naming and usage
- **Maintainable System**: Clear contracts between pipelines and agents

### Negative
- **Additional Complexity**: More validation and documentation overhead
- **Migration Effort**: Existing pipelines need updates
- **Maintenance Burden**: Documentation must be kept in sync
- **Potential Breaking Changes**: Stricter validation may break existing workflows

## Related ADRs
- ADR-0032: Pipeline Failure Recovery Strategy
- ADR-0034: Agent Health Monitoring Strategy
- ADR-0035: Deployment Progress Tracking Strategy

## Success Metrics
- Zero pipeline failures due to parameter/validation type issues
- 100% of pipelines using documented parameters
- Automated validation preventing configuration errors
- Complete parameter reference documentation

---

**Date**: 2025-01-04
**Participants**: Workshop Template System Development Team, DevOps, Technical Writers
**Review Date**: 2025-04-04 (3 months)
