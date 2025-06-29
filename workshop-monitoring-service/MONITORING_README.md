# Workshop Monitoring Service

A comprehensive monitoring dashboard for the Workshop Template System, providing real-time health monitoring and status tracking for all 6 workshop agents.

## 🎯 Overview

The Workshop Monitoring Service is a full-stack application built with **Quarkus** (Java 17) and **React** that monitors the health and performance of the Workshop Template System agents:

- **workshop-chat** (8080) - RAG-based participant assistance
- **template-converter** (8081) - Repository-to-workshop transformation  
- **content-creator** (8082) - Original workshop content creation
- **source-manager** (8083) - Repository management and deployment
- **research-validation** (8084) - Internet-grounded fact-checking
- **documentation-pipeline** (8085) - Content monitoring and updates

## ✨ Features

### 🔍 Real-time Monitoring
- **Automated health checks** every 30 seconds
- **Response time tracking** and performance analysis
- **System-wide health aggregation** with intelligent status calculation
- **Manual health check triggers** for immediate updates

### 📊 Interactive Dashboard
- **Modern React UI** with Tailwind CSS styling
- **Real-time status updates** with auto-refresh
- **Interactive agent status grid** with expandable details
- **Response time visualization** with performance charts
- **Service information panel** with configuration details

### 🛠 REST API
- **Comprehensive REST endpoints** for all monitoring data
- **OpenAPI 3.1 documentation** with Swagger UI
- **JSON responses** with structured error handling
- **CORS support** for frontend integration

## 🚀 Quick Start

### Prerequisites
- Java 17+ (managed via SDKMAN)
- Maven 3.8+
- Node.js 18+ and npm

### 1. Build the Frontend
```bash
./build-frontend.sh
```

### 2. Start the Application
```bash
mvn quarkus:dev
```

### 3. Access the Dashboard
- **Dashboard**: http://localhost:8086
- **API Documentation**: http://localhost:8086/q/swagger-ui
- **OpenAPI Spec**: http://localhost:8086/q/openapi

## 📡 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/monitoring/health` | GET | System health status |
| `/api/monitoring/agents` | GET | All agent statuses |
| `/api/monitoring/agents/{name}` | GET | Specific agent status |
| `/api/monitoring/summary` | GET | Dashboard summary data |
| `/api/monitoring/health-check` | POST | Trigger manual health check |
| `/api/monitoring/info` | GET | Service information |

## 🏗 Architecture

### Backend (Quarkus)
- **Java 17** with Quarkus framework
- **JAX-RS** REST endpoints with OpenAPI documentation
- **Scheduled health monitoring** with configurable intervals
- **HTTP client** for agent communication
- **Thread-safe caching** with ConcurrentHashMap

### Frontend (React)
- **React 18** with modern hooks and functional components
- **Tailwind CSS** for responsive styling
- **Recharts** for data visualization
- **Axios** for API communication
- **Lucide React** for icons

### Data Models
- **AgentStatus** - Individual agent health and metadata
- **SystemHealth** - Aggregated system status
- **HealthStatus** enum - HEALTHY, DEGRADED, UNHEALTHY, UNKNOWN

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Test Coverage
- **27 tests** covering all components
- **Unit tests** for data models and services
- **Integration tests** for REST endpoints
- **Real-time monitoring** validation

## 📦 Deployment

### Development Mode
```bash
mvn quarkus:dev
```

### Production Build
```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

## 🎨 Dashboard Features

### System Health Card
- Overall system status with color-coded indicators
- Health metrics breakdown (healthy/degraded/unhealthy/unknown)
- Health score progress bar
- Active issues display

### Agent Status Grid
- Expandable agent cards with detailed information
- Response time tracking
- Available tools listing
- Error message display
- Direct links to agent endpoints

### Response Time Chart
- Bar chart visualization of agent performance
- Performance distribution analysis
- Response time statistics (min/max/average)
- Performance categorization (Excellent/Good/Fair/Slow)

### Service Information Panel
- Service metadata and configuration
- API endpoint documentation
- Agent descriptions and port mappings
- System architecture overview

## 🔍 Monitoring Capabilities

### Health Status Calculation
- **HEALTHY**: All agents responding normally
- **DEGRADED**: Some agents slow or unknown status
- **UNHEALTHY**: One or more agents failing
- **UNKNOWN**: Status being determined

### Performance Metrics
- Response time tracking (milliseconds)
- Success/failure rates
- Agent availability percentages
- System uptime monitoring

## 🛡 Error Handling

- **Graceful degradation** when agents are unavailable
- **Comprehensive error logging** with structured messages
- **Retry mechanisms** for transient failures
- **User-friendly error displays** in the dashboard

## 🔄 Auto-refresh

- **30-second intervals** for automatic data updates
- **Manual refresh** capability
- **Real-time status indicators** with visual feedback
- **Last updated timestamps** for data freshness

## 📈 Performance

- **Concurrent health checks** for all agents
- **Efficient caching** with thread-safe data structures
- **Optimized React rendering** with proper state management
- **Responsive design** for mobile and desktop

---

**🎯 Ready to monitor your Workshop Template System!** 

The dashboard provides comprehensive visibility into all 6 agents with real-time health monitoring, performance tracking, and interactive visualizations.
