# Workshop Template System - Comprehensive Makefile
# Includes original targets plus comprehensive code quality and testing

.PHONY: help install install-dev clean lint format type-check security test test-unit test-integration test-e2e test-all coverage validate-all pre-commit ci-check

# Default target
.DEFAULT_GOAL := help

# Colors for output
YELLOW := \033[1;33m
GREEN := \033[0;32m
RED := \033[0;31m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Project configuration
PYTHON := python3
PIP := pip3
PROJECT_NAME := workshop-template-system
PYTHON_DIRS := demos/ common/ tests/ scripts/
REQUIREMENTS_FILES := requirements.txt tests/requirements.txt demos/workshop_template_system/requirements.txt
COVERAGE_THRESHOLD := 80
PYTEST_ARGS := -v --tb=short --strict-markers --disable-warnings --color=yes

# Virtual environment
VENV := .venv
VENV_PYTHON := $(VENV)/bin/python
VENV_PIP := $(VENV)/bin/pip

help: ## Show this help message
	@echo "$(YELLOW)Workshop Template System - Comprehensive Makefile$(NC)"
	@echo "$(BLUE)Available targets:$(NC)"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)

# =============================================================================
# ORIGINAL LLAMA STACK TARGETS
# =============================================================================

build_llamastack: ## Build Llama Stack container
	CONTAINER_BINARY=podman BUILD_PLATFORM=linux/amd64 llama stack build --template ollama --image-type container

build_mcp: ## Build MCP server container
	podman build -t mcp_server:latest --platform="linux/amd64" build_mcp

build_ui: ## Build UI container
	podman build -t streamlit_client:latest --platform="linux/amd64" -f demos/rag_agentic/frontend/build/Containerfile .

run_ui: ## Run UI container
	 podman run -it -p 8501:8501 --env LLAMA_STACK_ENDPOINT=$(LLAMA_STACK_ENDPOINT) --env TAVILY_SEARCH_API_KEY=$(TAVILY_SEARCH_API_KEY) streamlit_client:latest

run_mcp: ## Run MCP server locally
	python build_mcp/mcp_tools.py

run_mcp_container: ## Run MCP server in container
	podman run -it -p 8000:8000 mcp_server

setup_local: ## Setup local development environment
	ollama run llama3.2:3b-instruct-fp16 --keepalive 160m &
	podman run -it -p 8321:8321 -v ~/.llama:/root/.llama localhost/distribution-ollama:0.2.7 --port 8321 --env INFERENCE_MODEL="meta-llama/Llama-3.2-3B-Instruct" --env OLLAMA_URL=http://host.containers.internal:11434

# =============================================================================
# INSTALLATION AND SETUP
# =============================================================================

install: ## Install production dependencies
	@echo "$(YELLOW)📦 Installing production dependencies...$(NC)"
	$(PIP) install -r requirements.txt

install-dev: $(VENV) ## Install development dependencies in virtual environment
	@echo "$(YELLOW)📦 Installing development dependencies...$(NC)"
	$(VENV_PIP) install -r requirements.txt
	$(VENV_PIP) install -r tests/requirements.txt
	$(VENV_PIP) install -r demos/workshop_template_system/requirements.txt
	$(VENV_PIP) install black isort flake8 mypy bandit safety pytest-cov pre-commit
	@echo "$(GREEN)✅ Development environment ready$(NC)"

$(VENV):
	@echo "$(YELLOW)🔧 Creating virtual environment...$(NC)"
	$(PYTHON) -m venv $(VENV)
	$(VENV_PIP) install --upgrade pip setuptools wheel

clean: ## Clean up generated files and caches
	@echo "$(YELLOW)🧹 Cleaning up...$(NC)"
	find . -type f -name "*.pyc" -delete
	find . -type d -name "__pycache__" -exec rm -rf {} + 2>/dev/null || true
	find . -type d -name "*.egg-info" -exec rm -rf {} + 2>/dev/null || true
	find . -type d -name ".pytest_cache" -exec rm -rf {} + 2>/dev/null || true
	find . -type d -name ".mypy_cache" -exec rm -rf {} + 2>/dev/null || true
	rm -rf build/ dist/ .coverage htmlcov/ .tox/ coverage.xml
	@echo "$(GREEN)✅ Cleanup complete$(NC)"

# =============================================================================
# CODE QUALITY CHECKS
# =============================================================================

lint: ## Run all linting checks
	@echo "$(YELLOW)🔍 Running linting checks...$(NC)"
	@$(MAKE) lint-flake8
	@$(MAKE) lint-imports
	@echo "$(GREEN)✅ All linting checks passed$(NC)"

lint-flake8: ## Run flake8 linting
	@echo "$(BLUE)Running flake8...$(NC)"
	$(VENV_PYTHON) -m flake8 $(PYTHON_DIRS) --config=.flake8 || (echo "$(RED)❌ flake8 failed$(NC)" && exit 1)

lint-imports: ## Check import sorting with isort
	@echo "$(BLUE)Checking import sorting...$(NC)"
	$(VENV_PYTHON) -m isort --check-only --diff $(PYTHON_DIRS) || (echo "$(RED)❌ Import sorting failed$(NC)" && exit 1)

format: ## Format code with black and isort
	@echo "$(YELLOW)🎨 Formatting code...$(NC)"
	$(VENV_PYTHON) -m black $(PYTHON_DIRS)
	$(VENV_PYTHON) -m isort $(PYTHON_DIRS)
	@echo "$(GREEN)✅ Code formatting complete$(NC)"

auto-fix: ## Auto-fix common code issues (format + basic syntax fixes)
	@echo "$(YELLOW)🔧 Auto-fixing code issues...$(NC)"
	@echo "$(BLUE)Step 1: Removing unused imports...$(NC)"
	$(VENV_PYTHON) -m autoflake --in-place --remove-all-unused-imports --remove-unused-variables --recursive $(PYTHON_DIRS) 2>/dev/null || echo "$(BLUE)autoflake not available, skipping$(NC)"
	@echo "$(BLUE)Step 2: Auto-fixing PEP8 issues...$(NC)"
	$(VENV_PYTHON) -m autopep8 --in-place --aggressive --aggressive --recursive $(PYTHON_DIRS) 2>/dev/null || echo "$(BLUE)autopep8 not available, skipping$(NC)"
	@echo "$(BLUE)Step 3: Formatting code...$(NC)"
	$(VENV_PYTHON) -m black $(PYTHON_DIRS) || echo "$(RED)⚠️ Black formatting had issues$(NC)"
	$(VENV_PYTHON) -m isort $(PYTHON_DIRS) || echo "$(RED)⚠️ Import sorting had issues$(NC)"
	@echo "$(BLUE)Step 4: Checking for remaining syntax issues...$(NC)"
	@for file in $$(find $(PYTHON_DIRS) -name "*.py" -type f); do \
		echo "$(BLUE)Checking: $$file$(NC)"; \
		python -m py_compile "$$file" 2>&1 | grep -E "(SyntaxError|IndentationError)" || true; \
	done
	@echo "$(GREEN)✅ Auto-fix complete - review changes and run 'make lint' to verify$(NC)"

fix-syntax: ## Quick syntax-only fixes for critical errors
	@echo "$(YELLOW)🚨 Quick syntax fixes...$(NC)"
	@for file in $$(find $(PYTHON_DIRS) -name "*.py" -type f); do \
		echo "$(BLUE)Syntax check: $$file$(NC)"; \
		python -m py_compile "$$file" 2>&1 || echo "$(RED)❌ Syntax error in $$file$(NC)"; \
	done
	@echo "$(BLUE)Running autopep8 for syntax fixes...$(NC)"
	$(VENV_PYTHON) -m autopep8 --in-place --select=E9,W6 --recursive $(PYTHON_DIRS) 2>/dev/null || echo "$(BLUE)autopep8 not available$(NC)"

format-check: ## Check if code formatting is correct
	@echo "$(BLUE)Checking code formatting...$(NC)"
	$(VENV_PYTHON) -m black --check $(PYTHON_DIRS) || (echo "$(RED)❌ Code formatting check failed$(NC)" && exit 1)

type-check: ## Run type checking with mypy
	@echo "$(YELLOW)🔍 Running type checks...$(NC)"
	$(VENV_PYTHON) -m mypy $(PYTHON_DIRS) --ignore-missing-imports --no-strict-optional || (echo "$(RED)❌ Type checking failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ Type checking passed$(NC)"

security: ## Run security checks
	@echo "$(YELLOW)🔒 Running security checks...$(NC)"
	@$(MAKE) security-bandit
	@$(MAKE) security-safety
	@echo "$(GREEN)✅ Security checks passed$(NC)"

security-bandit: ## Run bandit security linting
	@echo "$(BLUE)Running bandit security scan...$(NC)"
	$(VENV_PYTHON) -m bandit -r $(PYTHON_DIRS) -f json -o bandit-report.json || (echo "$(RED)❌ Security scan failed$(NC)" && exit 1)

security-safety: ## Check for known security vulnerabilities
	@echo "$(BLUE)Checking for known vulnerabilities...$(NC)"
	$(VENV_PYTHON) -m safety check --json --output safety-report.json || (echo "$(RED)❌ Vulnerability check failed$(NC)" && exit 1)

# =============================================================================
# TESTING
# =============================================================================

test: test-unit ## Run unit tests (default test target)

test-unit: ## Run unit tests
	@echo "$(YELLOW)🧪 Running unit tests...$(NC)"
	$(VENV_PYTHON) -m pytest tests/unit/ $(PYTEST_ARGS) -m "not slow" || (echo "$(RED)❌ Unit tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ Unit tests passed$(NC)"

test-integration: ## Run integration tests
	@echo "$(YELLOW)🔗 Running integration tests...$(NC)"
	$(VENV_PYTHON) -m pytest tests/integration/ $(PYTEST_ARGS) -k "not ollama" || (echo "$(RED)❌ Integration tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ Integration tests passed$(NC)"

test-e2e: ## Run end-to-end tests
	@echo "$(YELLOW)🎯 Running end-to-end tests...$(NC)"
	$(VENV_PYTHON) -m pytest tests/e2e/ $(PYTEST_ARGS) || (echo "$(RED)❌ End-to-end tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ End-to-end tests passed$(NC)"

test-ollama: ## Run Ollama-specific tests (requires Ollama server)
	@echo "$(YELLOW)🦙 Running Ollama integration tests...$(NC)"
	$(VENV_PYTHON) -m pytest -m ollama $(PYTEST_ARGS) || (echo "$(RED)❌ Ollama tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ Ollama tests passed$(NC)"

test-all: ## Run all tests
	@echo "$(YELLOW)🧪 Running all tests...$(NC)"
	$(VENV_PYTHON) -m pytest tests/ $(PYTEST_ARGS) || (echo "$(RED)❌ Tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ All tests passed$(NC)"

test-fast: ## Run fast tests only (exclude slow tests)
	@echo "$(YELLOW)⚡ Running fast tests...$(NC)"
	$(VENV_PYTHON) -m pytest tests/ $(PYTEST_ARGS) -m "not slow and not ollama" || (echo "$(RED)❌ Fast tests failed$(NC)" && exit 1)
	@echo "$(GREEN)✅ Fast tests passed$(NC)"

coverage: ## Run tests with coverage report
	@echo "$(YELLOW)📊 Running tests with coverage...$(NC)"
	$(VENV_PYTHON) -m pytest tests/ $(PYTEST_ARGS) \
		--cov=demos --cov=common \
		--cov-report=html --cov-report=xml --cov-report=term \
		--cov-fail-under=$(COVERAGE_THRESHOLD) || (echo "$(RED)❌ Coverage below threshold$(NC)" && exit 1)
	@echo "$(GREEN)✅ Coverage report generated$(NC)"
	@echo "$(BLUE)📄 HTML report: htmlcov/index.html$(NC)"

# =============================================================================
# VALIDATION AND CI
# =============================================================================

validate-all: clean lint format-check type-check security test-fast ## Run all validation checks
	@echo "$(GREEN)🎉 All validation checks passed!$(NC)"

pre-commit: ## Run pre-commit checks
	@echo "$(YELLOW)🔍 Running pre-commit checks...$(NC)"
	@$(MAKE) format-check
	@$(MAKE) lint
	@$(MAKE) type-check
	@$(MAKE) test-fast
	@echo "$(GREEN)✅ Pre-commit checks passed$(NC)"

ci-check: ## Run CI pipeline checks
	@echo "$(YELLOW)🚀 Running CI pipeline checks...$(NC)"
	@$(MAKE) install-dev
	@$(MAKE) validate-all
	@$(MAKE) coverage
	@echo "$(GREEN)🎉 CI pipeline checks completed successfully!$(NC)"

# =============================================================================
# WORKSHOP-SPECIFIC TARGETS
# =============================================================================

test-agents: ## Test agent functionality
	@echo "$(YELLOW)🤖 Testing agent functionality...$(NC)"
	$(VENV_PYTHON) -m pytest tests/integration/test_agents.py $(PYTEST_ARGS) || (echo "$(RED)❌ Agent tests failed$(NC)" && exit 1)

test-middleware: ## Test Quarkus middleware integration
	@echo "$(YELLOW)🔗 Testing middleware integration...$(NC)"
	$(VENV_PYTHON) -m pytest tests/integration/test_middleware.py $(PYTEST_ARGS) || (echo "$(RED)❌ Middleware tests failed$(NC)" && exit 1)

test-pipelines: ## Test Tekton pipeline integration
	@echo "$(YELLOW)🚀 Testing pipeline integration...$(NC)"
	$(VENV_PYTHON) -m pytest tests/e2e/test_pipelines.py $(PYTEST_ARGS) || (echo "$(RED)❌ Pipeline tests failed$(NC)" && exit 1)

test-agent-fixes: ## Test agent fixes with our validation script
	@echo "$(YELLOW)🔧 Testing agent fixes...$(NC)"
	@if [ -f "scripts/test-agent-fixes.sh" ]; then \
		chmod +x scripts/test-agent-fixes.sh && ./scripts/test-agent-fixes.sh; \
	else \
		echo "$(RED)❌ Agent fix test script not found$(NC)" && exit 1; \
	fi

validate-adrs: ## Validate ADR documentation
	@echo "$(YELLOW)📋 Validating ADR documentation...$(NC)"
	@find docs/adrs -name "*.md" -type f | while read adr; do \
		echo "$(BLUE)Checking: $$adr$(NC)"; \
		grep -q "## Status" "$$adr" || (echo "$(RED)❌ Missing Status section in $$adr$(NC)" && exit 1); \
		grep -q "## Context" "$$adr" || (echo "$(RED)❌ Missing Context section in $$adr$(NC)" && exit 1); \
		grep -q "## Decision" "$$adr" || (echo "$(RED)❌ Missing Decision section in $$adr$(NC)" && exit 1); \
	done
	@echo "$(GREEN)✅ ADR documentation is valid$(NC)"

# =============================================================================
# UTILITY TARGETS
# =============================================================================

check-deps: ## Check for outdated dependencies
	@echo "$(YELLOW)📦 Checking for outdated dependencies...$(NC)"
	$(VENV_PIP) list --outdated

update-deps: ## Update dependencies (use with caution)
	@echo "$(YELLOW)⬆️ Updating dependencies...$(NC)"
	$(VENV_PIP) install --upgrade -r requirements.txt
	$(VENV_PIP) install --upgrade -r tests/requirements.txt

requirements-check: ## Validate requirements files
	@echo "$(YELLOW)📋 Validating requirements files...$(NC)"
	@for req_file in $(REQUIREMENTS_FILES); do \
		if [ -f "$$req_file" ]; then \
			echo "$(BLUE)✓ Found: $$req_file$(NC)"; \
			$(VENV_PIP) install --dry-run -r "$$req_file" > /dev/null 2>&1 || \
				(echo "$(RED)❌ Invalid requirements in $$req_file$(NC)" && exit 1); \
		else \
			echo "$(RED)❌ Missing: $$req_file$(NC)"; \
		fi; \
	done
	@echo "$(GREEN)✅ All requirements files are valid$(NC)"

docs: ## Generate documentation
	@echo "$(YELLOW)📚 Documentation locations:$(NC)"
	@echo "  $(BLUE)- ADR documentation: docs/adrs/$(NC)"
	@echo "  $(BLUE)- API documentation: Generated from docstrings$(NC)"
	@echo "  $(BLUE)- Test reports: htmlcov/index.html$(NC)"
	@echo "  $(BLUE)- Security reports: bandit-report.json, safety-report.json$(NC)"

build: ## Build the project
	@echo "$(YELLOW)🔨 Building project...$(NC)"
	@echo "$(BLUE)Python project - no build step required$(NC)"
	@echo "$(BLUE)Quarkus middleware builds via Maven in workshop-monitoring-service/$(NC)"
	@echo "$(GREEN)✅ Build complete$(NC)"

docker-build: ## Build Docker images
	@echo "$(YELLOW)🐳 Building Docker images...$(NC)"
	@if [ -f "Dockerfile" ]; then \
		docker build -t $(PROJECT_NAME):latest .; \
		echo "$(GREEN)✅ Docker image built$(NC)"; \
	else \
		echo "$(BLUE)No Dockerfile found, using existing container builds$(NC)"; \
	fi
