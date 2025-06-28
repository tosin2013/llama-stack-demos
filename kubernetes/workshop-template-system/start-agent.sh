#!/bin/bash

# Workshop Template System Agent Startup Script
set -e

# Default values
AGENT_NAME="${AGENT_NAME:-workshop_chat}"
AGENT_PORT="${AGENT_PORT:-8080}"
LLAMA_STACK_ENDPOINT="${LLAMA_STACK_ENDPOINT:-http://llama-stack-server:8321}"
INFERENCE_MODEL_ID="${INFERENCE_MODEL_ID:-meta-llama/Llama-3.2-3B-Instruct}"

echo "Starting Workshop Template System Agent: ${AGENT_NAME}"
echo "Port: ${AGENT_PORT}"
echo "Llama Stack: ${LLAMA_STACK_ENDPOINT}"
echo "Model: ${INFERENCE_MODEL_ID}"

# Set up environment
export PYTHONPATH="/opt/app-root/src:${PYTHONPATH}"

# Start the appropriate agent based on AGENT_NAME
case "${AGENT_NAME}" in
    "workshop_chat"|"workshop-chat")
        echo "Starting Workshop Chat Agent..."
        python -m demos.workshop_template_system --agent-name workshop_chat --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "template_converter"|"template-converter")
        echo "Starting Template Converter Agent..."
        python -m demos.workshop_template_system --agent-name template_converter --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "content_creator"|"content-creator")
        echo "Starting Content Creator Agent..."
        python -m demos.workshop_template_system --agent-name content_creator --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "source_manager"|"source-manager")
        echo "Starting Source Manager Agent..."
        python -m demos.workshop_template_system --agent-name source_manager --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "research_validation"|"research-validation")
        echo "Starting Research Validation Agent..."
        python -m demos.workshop_template_system --agent-name research_validation --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "documentation_pipeline"|"documentation-pipeline")
        echo "Starting Documentation Pipeline Agent..."
        python -m demos.workshop_template_system --agent-name documentation_pipeline --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    *)
        echo "Unknown agent: ${AGENT_NAME}"
        echo "Available agents: workshop_chat, template_converter, content_creator, source_manager, research_validation, documentation_pipeline"
        echo "Starting simple HTTP server as fallback..."
        python -m http.server ${AGENT_PORT}
        ;;
esac
