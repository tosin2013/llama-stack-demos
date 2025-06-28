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
    "workshop_chat")
        echo "Starting Workshop Chat Agent..."
        cd demos/workshop_template_system/agents/workshop_chat
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "template_converter")
        echo "Starting Template Converter Agent..."
        cd demos/workshop_template_system/agents/template_converter
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "content_creator")
        echo "Starting Content Creator Agent..."
        cd demos/workshop_template_system/agents/content_creator
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "source_manager")
        echo "Starting Source Manager Agent..."
        cd demos/workshop_template_system/agents/source_manager
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "research_validation")
        echo "Starting Research Validation Agent..."
        cd demos/workshop_template_system/agents/research_validation
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    "documentation_pipeline")
        echo "Starting Documentation Pipeline Agent..."
        cd demos/workshop_template_system/agents/documentation_pipeline
        python -m uvicorn main:app --host 0.0.0.0 --port ${AGENT_PORT}
        ;;
    *)
        echo "Unknown agent: ${AGENT_NAME}"
        echo "Starting simple HTTP server as fallback..."
        python -m http.server ${AGENT_PORT}
        ;;
esac
