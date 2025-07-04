# Code Quality and Testing Guide

This document describes the code quality standards, testing practices, and development workflow for the Workshop Template System.

## 🎯 Overview

The Workshop Template System uses a comprehensive code quality framework that includes:

- **Automated formatting** with Black and isort
- **Linting** with flake8
- **Type checking** with mypy (optional)
- **Security scanning** with bandit and safety
- **Comprehensive testing** with pytest
- **Pre-commit hooks** for automated quality checks
- **CI/CD integration** for continuous validation

## 🚀 Quick Start

### 1. Setup Development Environment

```bash
# Install development dependencies
make install-dev

# Install pre-commit hooks
pre-commit install
```

### 2. Run Quality Checks

```bash
# Run all validation checks
make validate-all

# Run specific checks
make lint          # Linting only
make format        # Format code
make type-check    # Type checking
make security      # Security scans
make test          # Unit tests
make coverage      # Tests with coverage
```

### 3. Pre-commit Workflow

```bash
# Before committing, run pre-commit checks
make pre-commit

# Or let pre-commit run automatically on git commit
git add .
git commit -m "Your commit message"
```

## 📋 Available Make Targets

### Installation and Setup
- `make install` - Install production dependencies
- `make install-dev` - Install development dependencies in virtual environment
- `make clean` - Clean up generated files and caches

### Code Quality
- `make lint` - Run all linting checks (flake8 + import sorting)
- `make format` - Format code with black and isort
- `make format-check` - Check if code formatting is correct
- `make type-check` - Run type checking with mypy
- `make security` - Run security checks (bandit + safety)

### Testing
- `make test` - Run unit tests (default)
- `make test-unit` - Run unit tests only
- `make test-integration` - Run integration tests
- `make test-e2e` - Run end-to-end tests
- `make test-ollama` - Run Ollama-specific tests
- `make test-all` - Run all tests
- `make test-fast` - Run fast tests (exclude slow tests)
- `make coverage` - Run tests with coverage report

### Workshop-Specific Testing
- `make test-agents` - Test agent functionality
- `make test-middleware` - Test Quarkus middleware integration
- `make test-pipelines` - Test Tekton pipeline integration
- `make test-agent-fixes` - Test agent fixes with validation script

### Validation and CI
- `make validate-all` - Run all validation checks
- `make pre-commit` - Run pre-commit checks
- `make ci-check` - Run CI pipeline checks

### Documentation and Utilities
- `make docs` - Show documentation locations
- `make validate-adrs` - Validate ADR documentation
- `make check-deps` - Check for outdated dependencies
- `make requirements-check` - Validate requirements files

## 🔧 Tool Configurations

### Black (Code Formatting)
- **Line length**: 88 characters
- **Target version**: Python 3.9+
- **Configuration**: `pyproject.toml` → `[tool.black]`

### isort (Import Sorting)
- **Profile**: black-compatible
- **Line length**: 88 characters
- **Configuration**: `pyproject.toml` → `[tool.isort]`

### flake8 (Linting)
- **Max line length**: 88 characters
- **Max complexity**: 10
- **Configuration**: `.flake8`

### mypy (Type Checking)
- **Python version**: 3.9
- **Ignore missing imports**: Yes
- **Configuration**: `pyproject.toml` → `[tool.mypy]`

### pytest (Testing)
- **Test discovery**: `tests/` directory
- **Markers**: unit, integration, e2e, ollama, slow
- **Configuration**: `pyproject.toml` → `[tool.pytest.ini_options]`

### Coverage
- **Threshold**: 80%
- **Source**: `demos/`, `common/`
- **Reports**: HTML, XML, terminal
- **Configuration**: `pyproject.toml` → `[tool.coverage]`

## 🧪 Testing Strategy

### Test Categories

1. **Unit Tests** (`tests/unit/`)
   - Test individual components in isolation
   - Fast execution (< 1 second per test)
   - No external dependencies

2. **Integration Tests** (`tests/integration/`)
   - Test component interactions
   - May require agent services
   - Moderate execution time

3. **End-to-End Tests** (`tests/e2e/`)
   - Test complete workflows
   - Require full system setup
   - Longer execution time

4. **Ollama Tests** (marked with `@pytest.mark.ollama`)
   - Require Ollama LLM server
   - Run separately from main test suite

### Test Execution

```bash
# Run tests by category
make test-unit          # Fast unit tests
make test-integration   # Integration tests
make test-e2e          # End-to-end tests
make test-ollama       # Ollama-specific tests

# Run tests by speed
make test-fast         # Exclude slow tests
make test-all          # All tests including slow ones

# Run with coverage
make coverage          # Generate coverage report
```

### Test Markers

Use pytest markers to categorize tests:

```python
import pytest

@pytest.mark.unit
def test_unit_function():
    """Fast unit test"""
    pass

@pytest.mark.integration
def test_integration_workflow():
    """Integration test requiring multiple components"""
    pass

@pytest.mark.e2e
def test_end_to_end_pipeline():
    """End-to-end test with real repositories"""
    pass

@pytest.mark.ollama
def test_ollama_integration():
    """Test requiring Ollama LLM server"""
    pass

@pytest.mark.slow
def test_long_running_process():
    """Test that takes more than 30 seconds"""
    pass
```

## 🔒 Security Practices

### Security Tools

1. **bandit** - Scans Python code for security issues
2. **safety** - Checks dependencies for known vulnerabilities

### Security Checks

```bash
# Run all security checks
make security

# Individual security tools
make security-bandit   # Code security scan
make security-safety   # Dependency vulnerability check
```

### Security Reports

- **bandit**: `bandit-report.json`
- **safety**: `safety-report.json`

## 🔄 CI/CD Integration

### GitHub Actions / CI Pipeline

The `make ci-check` target runs the complete CI pipeline:

1. Install development dependencies
2. Run all validation checks
3. Generate coverage report
4. Validate ADR documentation

### Pre-commit Hooks

Automatically run on every commit:

- Code formatting (black, isort)
- Linting (flake8)
- Basic security checks
- YAML/JSON validation
- Shell script linting

## 📊 Quality Metrics

### Coverage Requirements
- **Minimum coverage**: 80%
- **Coverage reports**: HTML (`htmlcov/index.html`)

### Code Quality Standards
- **Max line length**: 88 characters
- **Max function complexity**: 10
- **Import sorting**: Enforced
- **Type hints**: Encouraged but not required

## 🛠️ Development Workflow

### Recommended Workflow

1. **Setup**: `make install-dev`
2. **Develop**: Write code following quality standards
3. **Format**: `make format` (or let pre-commit handle it)
4. **Test**: `make test-fast` during development
5. **Validate**: `make validate-all` before committing
6. **Commit**: Git commit (pre-commit hooks run automatically)
7. **CI**: Pipeline runs `make ci-check`

### Quality Gates

- **Pre-commit**: Basic formatting and linting
- **CI Pipeline**: Full validation including tests and coverage
- **Manual Review**: Code review for logic and architecture

## 🔍 Troubleshooting

### Common Issues

1. **Import sorting conflicts**: Run `make format` to fix
2. **Type checking errors**: Add `# type: ignore` for external libraries
3. **Coverage below threshold**: Add tests or adjust threshold
4. **Security warnings**: Review and fix or add to ignore list

### Getting Help

- Check tool documentation in `pyproject.toml` and `.flake8`
- Run `make help` for available targets
- Review test examples in `tests/` directory
