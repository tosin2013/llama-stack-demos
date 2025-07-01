#!/bin/bash

# Start RAG Stack Locally for Workshop Template System
# Includes Milvus, etcd, and MinIO for complete RAG functionality

set -e

echo "🚀 Starting RAG Stack for Workshop Template System"
echo "=================================================="

# Configuration
NETWORK_NAME="workshop-rag-network"
MILVUS_PORT=19530
ETCD_PORT=2379
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if container is running
check_container() {
    local container_name=$1
    if podman ps --format "{{.Names}}" | grep -q "^${container_name}$"; then
        echo -e "${GREEN}✅ $container_name is already running${NC}"
        return 0
    else
        return 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local host=$2
    local port=$3
    local max_attempts=30
    local attempt=1

    echo "⏳ Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://$host:$port" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start after $max_attempts attempts${NC}"
    return 1
}

# Create network if it doesn't exist
echo "🔧 Setting up network..."
if ! podman network exists $NETWORK_NAME; then
    podman network create $NETWORK_NAME
    echo -e "${GREEN}✅ Created network: $NETWORK_NAME${NC}"
else
    echo -e "${GREEN}✅ Network $NETWORK_NAME already exists${NC}"
fi

# Start etcd (required by Milvus)
echo ""
echo "📊 Starting etcd..."
if ! check_container "workshop-etcd"; then
    podman run -d \
        --name workshop-etcd \
        --network $NETWORK_NAME \
        -p $ETCD_PORT:2379 \
        -e ETCD_AUTO_COMPACTION_MODE=revision \
        -e ETCD_AUTO_COMPACTION_RETENTION=1000 \
        -e ETCD_QUOTA_BACKEND_BYTES=4294967296 \
        -e ETCD_SNAPSHOT_COUNT=50000 \
        quay.io/coreos/etcd:v3.5.5 \
        /usr/local/bin/etcd \
        --advertise-client-urls=http://0.0.0.0:2379 \
        --listen-client-urls=http://0.0.0.0:2379 \
        --listen-peer-urls=http://0.0.0.0:2380 \
        --data-dir=/etcd-data \
        --initial-cluster=node1=http://0.0.0.0:2380 \
        --initial-cluster-state=new \
        --name=node1 \
        --initial-advertise-peer-urls=http://0.0.0.0:2380
    
    echo -e "${GREEN}✅ Started etcd container${NC}"
fi

# Start MinIO (object storage for Milvus)
echo ""
echo "💾 Starting MinIO..."
if ! check_container "workshop-minio"; then
    podman run -d \
        --name workshop-minio \
        --network $NETWORK_NAME \
        -p $MINIO_PORT:9000 \
        -p $MINIO_CONSOLE_PORT:9001 \
        -e MINIO_ACCESS_KEY=minioadmin \
        -e MINIO_SECRET_KEY=minioadmin \
        minio/minio:RELEASE.2023-03-20T20-16-18Z \
        server /data --console-address ":9001"
    
    echo -e "${GREEN}✅ Started MinIO container${NC}"
fi

# Wait for etcd and MinIO to be ready
wait_for_service "etcd" "localhost" $ETCD_PORT
wait_for_service "MinIO" "localhost" $MINIO_PORT

# Start Milvus (vector database)
echo ""
echo "🔍 Starting Milvus..."
if ! check_container "workshop-milvus"; then
    podman run -d \
        --name workshop-milvus \
        --network $NETWORK_NAME \
        -p $MILVUS_PORT:19530 \
        -p 9091:9091 \
        -e ETCD_ENDPOINTS=workshop-etcd:2379 \
        -e MINIO_ADDRESS=workshop-minio:9000 \
        -e MINIO_ACCESS_KEY=minioadmin \
        -e MINIO_SECRET_KEY=minioadmin \
        milvusdb/milvus:v2.3.0 \
        milvus run standalone
    
    echo -e "${GREEN}✅ Started Milvus container${NC}"
fi

# Wait for Milvus to be ready
echo ""
echo "⏳ Waiting for Milvus to be fully ready..."
sleep 10  # Milvus takes a bit longer to start

# Check Milvus health
max_attempts=30
attempt=1
while [ $attempt -le $max_attempts ]; do
    if curl -s "http://localhost:9091/healthz" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Milvus is ready!${NC}"
        break
    fi
    
    echo "   Attempt $attempt/$max_attempts - Milvus not ready yet..."
    sleep 3
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}❌ Milvus failed to start properly${NC}"
    echo "Check logs with: podman logs workshop-milvus"
    exit 1
fi

# Display status
echo ""
echo "🎉 RAG Stack Status"
echo "=================="
echo -e "${GREEN}✅ etcd:${NC}     http://localhost:$ETCD_PORT"
echo -e "${GREEN}✅ MinIO:${NC}    http://localhost:$MINIO_PORT (Console: http://localhost:$MINIO_CONSOLE_PORT)"
echo -e "${GREEN}✅ Milvus:${NC}   http://localhost:$MILVUS_PORT"
echo ""
echo -e "${YELLOW}📋 MinIO Credentials:${NC}"
echo "   Username: minioadmin"
echo "   Password: minioadmin"
echo ""
echo -e "${YELLOW}🔧 Environment Variables for Agents:${NC}"
echo "   MILVUS_ENDPOINT=http://localhost:$MILVUS_PORT"
echo "   RAG_ENABLED=true"
echo "   VDB_PROVIDER=milvus"
echo "   VDB_EMBEDDING=all-MiniLM-L6-v2"
echo ""
echo -e "${GREEN}🚀 RAG Stack is ready for Workshop Template System testing!${NC}"
echo ""
echo "📝 Next steps:"
echo "1. Start Quarkus middleware: cd workshop-monitoring-service && mvn quarkus:dev"
echo "2. Test RAG endpoints: curl http://localhost:8080/api/pipeline/mock/research-validation/update-rag-content"
echo "3. Start agents (optional): ./scripts/start-agents-local.sh"
echo ""
echo "🛑 To stop the RAG stack: ./scripts/stop-rag-stack-local.sh"
