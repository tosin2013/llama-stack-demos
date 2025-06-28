#!/bin/bash
# Workshop Template System Test Runner

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Workshop Template System Test Runner${NC}"
echo "=================================================="

# Check if agents are running
echo -e "\n${YELLOW}🔍 Checking if agents are running...${NC}"

AGENTS=(
    "localhost:8080"  # workshop-chat
    "localhost:8081"  # template-converter
    "localhost:8082"  # content-creator
    "localhost:8083"  # source-manager
    "localhost:8084"  # research-validation
    "localhost:8085"  # documentation-pipeline
)

AGENTS_RUNNING=0
for agent in "${AGENTS[@]}"; do
    if curl -s "http://${agent}/agent-card" > /dev/null 2>&1; then
        echo -e "  ✅ Agent at ${agent} is running"
        ((AGENTS_RUNNING++))
    else
        echo -e "  ❌ Agent at ${agent} is not responding"
    fi
done

if [ $AGENTS_RUNNING -lt 6 ]; then
    echo -e "\n${RED}❌ Not all agents are running. Please start all 6 agents first.${NC}"
    echo "Expected agents:"
    echo "  - workshop-chat (port 8080)"
    echo "  - template-converter (port 8081)"
    echo "  - content-creator (port 8082)"
    echo "  - source-manager (port 8083)"
    echo "  - research-validation (port 8084)"
    echo "  - documentation-pipeline (port 8085)"
    exit 1
fi

echo -e "\n${GREEN}✅ All ${AGENTS_RUNNING} agents are running!${NC}"

# Check if Ollama is available
echo -e "\n${YELLOW}🔍 Checking Ollama availability...${NC}"
if curl -s "http://localhost:11434/api/tags" > /dev/null 2>&1; then
    echo -e "  ✅ Ollama is available"
    OLLAMA_AVAILABLE=true
else
    echo -e "  ⚠️ Ollama is not available - LLM tests will be skipped"
    OLLAMA_AVAILABLE=false
fi

# Install test dependencies
echo -e "\n${YELLOW}📦 Installing test dependencies...${NC}"
pip install -r tests/requirements.txt

# Parse command line arguments
TEST_TYPE="all"
VERBOSE=""
COVERAGE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit)
            TEST_TYPE="unit"
            shift
            ;;
        --integration)
            TEST_TYPE="integration"
            shift
            ;;
        --e2e)
            TEST_TYPE="e2e"
            shift
            ;;
        --ollama)
            TEST_TYPE="ollama"
            shift
            ;;
        --verbose|-v)
            VERBOSE="-v"
            shift
            ;;
        --coverage)
            COVERAGE="--cov=demos.workshop_template_system --cov-report=html --cov-report=term"
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --unit        Run only unit tests"
            echo "  --integration Run only integration tests"
            echo "  --e2e         Run only end-to-end tests"
            echo "  --ollama      Run only Ollama integration tests"
            echo "  --verbose     Verbose output"
            echo "  --coverage    Generate coverage report"
            echo "  --help        Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run tests based on type
echo -e "\n${BLUE}🚀 Running tests...${NC}"

case $TEST_TYPE in
    "unit")
        echo -e "${YELLOW}Running unit tests...${NC}"
        pytest tests/unit/ $VERBOSE $COVERAGE
        ;;
    "integration")
        echo -e "${YELLOW}Running integration tests...${NC}"
        pytest tests/integration/ $VERBOSE $COVERAGE
        ;;
    "e2e")
        echo -e "${YELLOW}Running end-to-end tests...${NC}"
        pytest tests/e2e/ $VERBOSE $COVERAGE
        ;;
    "ollama")
        if [ "$OLLAMA_AVAILABLE" = true ]; then
            echo -e "${YELLOW}Running Ollama integration tests...${NC}"
            pytest -m ollama $VERBOSE $COVERAGE
        else
            echo -e "${RED}❌ Ollama is not available. Cannot run Ollama tests.${NC}"
            exit 1
        fi
        ;;
    "all")
        echo -e "${YELLOW}Running all tests...${NC}"
        
        # Run tests in order of complexity
        echo -e "\n${BLUE}1. Unit Tests${NC}"
        pytest tests/unit/ $VERBOSE
        
        echo -e "\n${BLUE}2. Integration Tests${NC}"
        pytest tests/integration/ -k "not ollama" $VERBOSE
        
        echo -e "\n${BLUE}3. End-to-End Tests${NC}"
        pytest tests/e2e/ $VERBOSE
        
        if [ "$OLLAMA_AVAILABLE" = true ]; then
            echo -e "\n${BLUE}4. Ollama Integration Tests${NC}"
            pytest -m ollama $VERBOSE
        else
            echo -e "\n${YELLOW}⚠️ Skipping Ollama tests (not available)${NC}"
        fi
        
        # Generate final coverage report if requested
        if [ -n "$COVERAGE" ]; then
            echo -e "\n${BLUE}📊 Generating coverage report...${NC}"
            pytest tests/ $COVERAGE --cov-report=html
        fi
        ;;
esac

echo -e "\n${GREEN}✅ Test run completed!${NC}"

# Show test results summary
if [ -f "htmlcov/index.html" ]; then
    echo -e "\n${BLUE}📊 Coverage report generated: htmlcov/index.html${NC}"
fi

echo -e "\n${BLUE}📋 Test Summary:${NC}"
echo "  - Agents tested: ${AGENTS_RUNNING}/6"
echo "  - Ollama available: ${OLLAMA_AVAILABLE}"
echo "  - Test type: ${TEST_TYPE}"

echo -e "\n${GREEN}🎉 Workshop Template System testing complete!${NC}"
