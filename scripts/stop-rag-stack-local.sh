#!/bin/bash

# Stop RAG Stack for Workshop Template System
# Stops Milvus, etcd, and MinIO containers

set -e

echo "🛑 Stopping RAG Stack for Workshop Template System"
echo "================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to stop container if running
stop_container() {
    local container_name=$1
    if podman ps --format "{{.Names}}" | grep -q "^${container_name}$"; then
        echo "🛑 Stopping $container_name..."
        podman stop $container_name
        echo -e "${GREEN}✅ Stopped $container_name${NC}"
    else
        echo -e "${YELLOW}⚠️  $container_name is not running${NC}"
    fi
}

# Function to remove container if exists
remove_container() {
    local container_name=$1
    if podman ps -a --format "{{.Names}}" | grep -q "^${container_name}$"; then
        echo "🗑️  Removing $container_name..."
        podman rm $container_name
        echo -e "${GREEN}✅ Removed $container_name${NC}"
    fi
}

# Stop containers in reverse order
echo "🛑 Stopping containers..."
stop_container "workshop-milvus"
stop_container "workshop-minio"
stop_container "workshop-etcd"

echo ""
echo "🗑️  Removing containers..."
remove_container "workshop-milvus"
remove_container "workshop-minio"
remove_container "workshop-etcd"

# Option to remove network
echo ""
read -p "🔧 Remove workshop-rag-network? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if podman network exists workshop-rag-network; then
        podman network rm workshop-rag-network
        echo -e "${GREEN}✅ Removed workshop-rag-network${NC}"
    fi
fi

echo ""
echo -e "${GREEN}🎉 RAG Stack stopped successfully!${NC}"
echo ""
echo "📝 To restart the RAG stack: ./scripts/start-rag-stack-local.sh"
