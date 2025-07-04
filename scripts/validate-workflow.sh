#!/bin/bash

# Workflow Validation Script
# Based on Git and Build Workflow Safety Rules
# Validates common workflow operations before execution

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() { echo -e "${BLUE}ℹ️  $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }

# Rule validation functions
validate_git_add() {
    local command="$1"
    
    # GIT-001: No Bulk Git Add
    if [[ "$command" =~ "git add ." ]] || [[ "$command" =~ "git add *" ]]; then
        print_error "RULE GIT-001 VIOLATION: Never use 'git add .' or 'git add *'"
        print_info "Reason: Can accidentally include node_modules, sensitive files, or break content masking"
        print_info "Solution: Add files explicitly: git add specific-file.js, git add docs/, etc."
        return 1
    fi
    
    return 0
}

validate_git_commit() {
    # GIT-002: Verify Git Status Before Commit
    print_info "Checking staged files before commit..."
    
    local staged_files=$(git diff --cached --name-only 2>/dev/null || echo "")
    
    if [ -z "$staged_files" ]; then
        print_warning "No files staged for commit"
        return 1
    fi
    
    echo "Staged files:"
    echo "$staged_files" | while read -r file; do
        echo "  - $file"
        
        # NODE-001: Exclude Node Modules
        if [[ "$file" =~ node_modules ]]; then
            print_error "RULE NODE-001 VIOLATION: node_modules directory should not be committed"
            print_info "Solution: Add 'node_modules/' to .gitignore"
            return 1
        fi
        
        # ENV-001: Never Commit Environment Files
        if [[ "$file" =~ \.env ]] || [[ "$file" =~ private-secret ]]; then
            print_error "RULE ENV-001 VIOLATION: Environment/secret files should not be committed"
            print_info "Solution: Add environment files to .gitignore"
            return 1
        fi
    done
    
    return 0
}

validate_build_workflow() {
    # GIT-003: Push Before Build
    print_info "Checking if local changes are pushed..."
    
    local unpushed_commits=$(git log origin/main..HEAD --oneline 2>/dev/null || echo "")
    
    if [ -n "$unpushed_commits" ]; then
        print_warning "RULE GIT-003: You have unpushed commits"
        print_info "Unpushed commits:"
        echo "$unpushed_commits"
        print_info "Solution: Run 'git push origin main' before building"
        return 1
    fi
    
    # GIT-004: Check BuildConfig Source
    if command -v oc &> /dev/null; then
        print_info "Checking BuildConfig source..."
        local bc_source=$(oc get bc workshop-monitoring-service-build -n workshop-system -o jsonpath='{.spec.source.git.uri}' 2>/dev/null || echo "")
        local current_remote=$(git remote get-url origin 2>/dev/null || echo "")
        
        if [ -n "$bc_source" ] && [ -n "$current_remote" ]; then
            if [[ "$bc_source" != "$current_remote" ]]; then
                print_warning "RULE GIT-004: BuildConfig source doesn't match current repository"
                print_info "BuildConfig source: $bc_source"
                print_info "Current repository: $current_remote"
                print_info "Solution: Update BuildConfig or push to correct repository"
                return 1
            fi
        fi
    fi
    
    return 0
}

validate_pipeline_params() {
    local params="$1"

    # BUILD-001: Validate Parameters Before Pipeline
    print_info "Validating pipeline parameters..."

    # BUILD-002: Use Correct Workspace PVC
    if [[ "$params" =~ "shared-workspace-pvc" ]]; then
        print_error "RULE BUILD-002 VIOLATION: Incorrect PVC name"
        print_info "Use 'workshop-shared-pvc' instead of 'shared-workspace-pvc'"
        return 1
    fi

    # PIPELINE-001: Use Middleware for Pipeline Configuration
    if [[ "$params" =~ "intelligent-workshop-validation" ]]; then
        print_error "RULE PIPELINE-001 VIOLATION: Invalid validation type"
        print_info "Use middleware to get correct validation types"
        print_info "Correct validation type: 'new-workshop-validation'"
        return 1
    fi

    return 0
}

validate_pipeline_workflow() {
    local command="$1"

    # PIPELINE-001: Use Middleware for Pipeline Configuration
    print_warning "RULE PIPELINE-001: Recommended workflow for pipeline testing:"
    print_info "1. Get configuration: curl -k .../api/pipeline/config"
    print_info "2. Validate parameters: curl -X POST .../api/pipeline/validate-parameters"
    print_info "3. Execute pipeline with validated parameters"
    print_info "4. Monitor execution: tkn pipelinerun logs -f"

    # PIPELINE-003: Use Dynamic Pipeline Script
    if [[ "$command" =~ "tkn pipeline start" ]] && [[ "$command" =~ "--param.*--param" ]]; then
        print_warning "RULE PIPELINE-003: Consider using dynamic pipeline script"
        print_info "Use: ./scripts/run-pipeline-with-config.sh <pipeline> <repo-url> <workshop-name>"
        print_info "This automatically handles middleware integration and parameter validation"
    fi

    return 0
}

# Main validation function
validate_command() {
    local command="$1"
    local errors=0
    
    print_info "Validating command: $command"
    
    # Check git add commands
    if [[ "$command" =~ "git add" ]]; then
        if ! validate_git_add "$command"; then
            ((errors++))
        fi
    fi
    
    # Check git commit commands
    if [[ "$command" =~ "git commit" ]]; then
        if ! validate_git_commit; then
            ((errors++))
        fi
    fi
    
    # Check build commands
    if [[ "$command" =~ "oc start-build" ]]; then
        if ! validate_build_workflow; then
            ((errors++))
        fi
    fi
    
    # Check pipeline commands
    if [[ "$command" =~ "tkn pipeline start" ]]; then
        if ! validate_pipeline_params "$command"; then
            ((errors++))
        fi
        # Always show workflow guidance for pipeline commands
        validate_pipeline_workflow "$command"
    fi
    
    if [ $errors -eq 0 ]; then
        print_success "Command validation passed"
        return 0
    else
        print_error "Command validation failed with $errors error(s)"
        return 1
    fi
}

# Interactive mode
interactive_mode() {
    print_info "Interactive Workflow Validation"
    print_info "Enter commands to validate (or 'exit' to quit):"
    
    while true; do
        echo -n "Command: "
        read -r command
        
        if [ "$command" = "exit" ]; then
            break
        fi
        
        if [ -n "$command" ]; then
            validate_command "$command"
            echo
        fi
    done
}

# Pre-commit hook mode
pre_commit_hook() {
    print_info "Running pre-commit validation..."
    
    if ! validate_git_commit; then
        print_error "Pre-commit validation failed"
        exit 1
    fi
    
    print_success "Pre-commit validation passed"
}

# Usage information
usage() {
    echo "Usage: $0 [OPTIONS] [COMMAND]"
    echo
    echo "Options:"
    echo "  -i, --interactive    Run in interactive mode"
    echo "  -p, --pre-commit     Run as pre-commit hook"
    echo "  -h, --help          Show this help message"
    echo
    echo "Examples:"
    echo "  $0 'git add .'"
    echo "  $0 'oc start-build workshop-monitoring-service-build'"
    echo "  $0 --interactive"
    echo "  $0 --pre-commit"
}

# Main script logic
main() {
    case "${1:-}" in
        -i|--interactive)
            interactive_mode
            ;;
        -p|--pre-commit)
            pre_commit_hook
            ;;
        -h|--help)
            usage
            ;;
        "")
            usage
            ;;
        *)
            validate_command "$*"
            ;;
    esac
}

# Run main function with all arguments
main "$@"
