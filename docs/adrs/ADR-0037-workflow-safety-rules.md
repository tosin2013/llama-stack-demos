# ADR-0037: Workflow Safety Rules and Pipeline Testing Standards

## Status
Accepted

## Context

Recent development work revealed critical gaps in our workflow safety and pipeline testing procedures:

**Critical Issues Identified**:
1. **Git Workflow Accidents**: Using `git add .` accidentally committed `node_modules/` and nearly exposed sensitive data
2. **Pipeline Parameter Failures**: Hardcoded validation types like `intelligent-workshop-validation` caused pipeline failures
3. **Build Workflow Inconsistencies**: Building from outdated code due to unpushed changes
4. **Content Masking Risks**: Bulk git operations after content masking could expose sensitive data
5. **Manual Parameter Management**: No systematic approach for getting correct pipeline parameters

**Evidence of Problems**:
- Pipeline failure: `Unknown validation type: intelligent-workshop-validation`
- Git commit containing thousands of node_modules files
- BuildConfig pulling from wrong repository (Gitea vs GitHub)
- Manual parameter guessing leading to PVC name mismatches

## Decision

Implement comprehensive workflow safety rules and standardized pipeline testing procedures:

### 1. Git and Build Workflow Safety Rules

#### **Critical Safety Rules**
```json
{
  "GIT-001": "Never use 'git add .' - always add files explicitly",
  "GIT-003": "Always push code changes before triggering OpenShift builds", 
  "MASK-001": "Never use 'git add .' after content masking operations",
  "NODE-001": "Never commit node_modules directory",
  "ENV-001": "Never commit .env files or environment configuration"
}
```

#### **Pipeline Safety Rules**
```json
{
  "PIPELINE-001": "Use middleware endpoints to get correct pipeline parameters",
  "PIPELINE-002": "Follow complete testing workflow: config → validate → execute → verify",
  "PIPELINE-003": "Use dynamic pipeline script for complex executions",
  "BUILD-001": "Validate parameters before pipeline execution",
  "BUILD-002": "Use correct workspace PVC names"
}
```

### 2. Standardized Pipeline Testing Workflow

#### **Mandatory Testing Sequence**
1. **Verify Middleware Health**
   ```bash
   curl -k ${MIDDLEWARE_URL}/api/monitoring/health
   ```

2. **Get Pipeline Configuration**
   ```bash
   curl -k ${MIDDLEWARE_URL}/api/pipeline/config
   ```

3. **Validate Parameters**
   ```bash
   curl -X POST ${MIDDLEWARE_URL}/api/pipeline/validate-parameters
   ```

4. **Execute with Validated Parameters**
   ```bash
   tkn pipeline start <pipeline> --param <validated-params>
   ```

5. **Monitor and Verify Results**
   ```bash
   tkn pipelinerun logs -f
   curl -k ${MIDDLEWARE_URL}/api/monitoring/agents
   ```

### 3. Automated Validation Tools

#### **Workflow Validation Script**
- Location: `scripts/validate-workflow.sh`
- Validates git commands, build operations, and pipeline parameters
- Provides real-time feedback on rule violations
- Can be used as pre-commit hook

#### **Dynamic Pipeline Script**
- Location: `scripts/run-pipeline-with-config.sh`
- Automatically retrieves configuration from middleware
- Validates parameters before execution
- Handles workspace PVC names correctly

### 4. Rule Enforcement Mechanisms

#### **Pre-commit Validation**
```bash
# Install as git hook
cp scripts/validate-workflow.sh .git/hooks/pre-commit
```

#### **Interactive Validation**
```bash
# Validate commands before execution
./scripts/validate-workflow.sh "git add ."
./scripts/validate-workflow.sh "tkn pipeline start workflow-1-intelligent-workshop"
```

#### **CI/CD Integration**
- Rules stored in machine-readable JSON format
- Can be integrated with automated validation tools
- Provides consistent enforcement across team

## Implementation Plan

### Phase 1: Immediate Safety (Completed)
1. ✅ **Created comprehensive rule set** in `rules/git-build-workflow-safety-rules.json`
2. ✅ **Implemented validation script** with real-time rule checking
3. ✅ **Updated .gitignore** to prevent node_modules commits
4. ✅ **Fixed BuildConfig** to pull from correct repository

### Phase 2: Pipeline Testing Standards (Completed)
1. ✅ **Added middleware configuration endpoints** (`/api/pipeline/config`, `/api/pipeline/validate-parameters`)
2. ✅ **Created dynamic pipeline script** with automated parameter handling
3. ✅ **Documented complete testing workflow** in comprehensive guide
4. ✅ **Added pipeline-specific validation rules**

### Phase 3: Team Adoption (Next Steps)
1. **Train team members** on new workflow procedures
2. **Integrate validation** into development environment setup
3. **Add automated checks** to CI/CD pipeline
4. **Monitor compliance** and refine rules based on usage

## Consequences

### Positive
- **Eliminated Git Accidents**: No more accidental commits of node_modules or sensitive files
- **Prevented Pipeline Failures**: Systematic parameter validation prevents runtime errors
- **Improved Developer Experience**: Clear guidance and automated validation
- **Enhanced Security**: Content masking protection and environment file safety
- **Consistent Workflows**: Standardized approach across team members
- **Reduced Debugging Time**: Fewer issues due to incorrect parameters or configurations

### Negative
- **Additional Overhead**: Developers must follow more steps and validations
- **Learning Curve**: Team needs to adopt new tools and procedures
- **Script Maintenance**: Validation scripts need updates as system evolves
- **Potential Friction**: More validation steps may slow down rapid prototyping

### Risk Mitigation
- **Gradual Adoption**: Rules can be implemented incrementally
- **Tool Automation**: Scripts reduce manual overhead
- **Clear Documentation**: Comprehensive guides reduce learning curve
- **Flexible Enforcement**: Warning vs error severity levels allow adaptation

## Success Metrics

### Immediate (1 week)
- ✅ Zero git commits containing node_modules
- ✅ Zero pipeline failures due to invalid validation types
- ✅ All team members using validation script

### Short-term (1 month)
- 100% of pipeline executions using middleware configuration
- Zero build failures due to unpushed code changes
- Automated validation integrated into development workflow

### Long-term (3 months)
- Measurable reduction in pipeline debugging time
- Consistent parameter usage across all pipeline executions
- Team adoption of standardized testing procedures

## Related ADRs
- ADR-0036: Pipeline Parameter and Validation Type Standards
- ADR-0018: Quarkus Middleware Architecture
- ADR-0023: OpenShift Deployment Strategy

## Documentation
- [Git and Build Workflow Safety Rules](../../rules/git-build-workflow-safety-rules.json)
- [Tekton Pipeline Testing Workflow Guide](../guides/tekton-pipeline-testing-workflow.md)
- [Pipeline Parameters Reference](../reference/pipeline-parameters.md)
- [Workflow Validation Script](../../scripts/validate-workflow.sh)
- [Dynamic Pipeline Script](../../scripts/run-pipeline-with-config.sh)

---

**Date**: 2025-01-04  
**Participants**: Workshop Template System Development Team, DevOps, Security Team  
**Review Date**: 2025-04-04 (3 months)
