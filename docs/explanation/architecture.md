# 6-Agent Workshop Template System Architecture

A comprehensive overview of the multi-agent system for creating, maintaining, and deploying interactive workshops.

## 🎯 System Overview

The Workshop Template System is a distributed multi-agent architecture that automates the entire workshop lifecycle from repository analysis to participant assistance. The system consists of 6 specialized agents that coordinate through the A2A (Agent-to-Agent) protocol.

## 🏗️ High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Workshop Template System                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Core Agents                                                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │Workshop Chat    │  │Template Convert │  │Documentation    │             │
│  │Agent (10040)    │  │Agent (10041)    │  │Pipeline (10050) │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │Source Manager   │  │Research & Valid │  │Content Creator  │             │
│  │Agent (10060)    │  │Agent (10070)    │  │Agent (10080)    │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │Llama Stack      │  │Ollama Server    │  │Pinecone Vector  │             │
│  │Server (8321)    │  │(11434)          │  │Database         │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
├─────────────────────────────────────────────────────────────────────────────┤
│  External Sources                                                           │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │Documentation    │  │PDF Documents    │  │API Endpoints    │             │
│  │Sites            │  │                 │  │& RSS Feeds      │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Deployment Targets                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │RHPDS Platform   │  │Showroom Platform│  │OpenShift Cluster│             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 🤖 Agent Responsibilities

### 1. Workshop Chat Agent (Port 10040)
**Purpose**: Provide RAG-based assistance to workshop participants

**Core Capabilities**:
- Answer participant questions using workshop content
- Navigate workshop structure and modules
- Provide contextual help and troubleshooting
- Access external documentation through RAG integration

**Key Technologies**:
- Llama Stack for LLM integration
- Pinecone for vector storage and retrieval
- RAG (Retrieval-Augmented Generation) for accurate responses

**Interactions**:
- Receives queries from workshop participants
- Retrieves relevant content from vector database
- Coordinates with other agents for complex questions

### 2. Template Converter Agent (Port 10041)
**Purpose**: Analyze repositories and convert them to workshops

**Core Capabilities**:
- Detect existing workshops vs. applications
- Analyze repository structure and technologies
- Generate workshop conversion recommendations
- Assess workshop quality and enhancement opportunities

**Key Features**:
- **Workshop Detection**: Identifies Antora, GitBook, MkDocs, etc.
- **Technology Analysis**: Recognizes frameworks, languages, patterns
- **Quality Assessment**: Evaluates educational potential
- **Conversion Strategy**: Recommends appropriate approach

**Decision Matrix**:
```
Repository Type → Action
├── Existing Workshop → Enhancement recommendations
├── Application → Conversion using Showroom template
├── Documentation → Educational content extraction
└── Mixed Content → Hybrid approach
```

### 3. Documentation Pipeline Agent (Port 10050)
**Purpose**: Monitor and orchestrate content updates

**Core Capabilities**:
- Monitor GitHub repositories for changes
- Track external documentation sources
- Analyze impact on workshop content
- Generate human-reviewable update proposals

**Monitoring Sources**:
- **GitHub Repositories**: Commits, releases, issues
- **Documentation Sites**: Content changes, version updates
- **PDF Documents**: File modifications, new versions
- **API Endpoints**: Schema changes, deprecations

**Workflow**:
```
Change Detection → Impact Analysis → Update Proposal → Human Review → Implementation
```

### 4. Source Manager Agent (Port 10060)
**Purpose**: Coordinate repository management and deployment

**Core Capabilities**:
- Create and maintain workshop repositories
- Coordinate deployments to RHPDS/Showroom/GitHub Pages
- Synchronize content between sources
- Manage workshop lifecycle and versioning
- Export workshops for static GitHub Pages hosting

**Operations**:
- **Repository Management**: Create, update, backup, restore
- **Deployment Coordination**: RHPDS, Showroom, OpenShift, GitHub Pages
- **Content Synchronization**: Source → Workshop repository
- **Version Control**: Branching, merging, release management
- **Dual Deployment**: Static export + Dynamic deployment coordination

### 5. Research & Validation Agent (Port 10070)
**Purpose**: Ensure content accuracy through internet research

**Core Capabilities**:
- Research current technology information
- Validate content against authoritative sources
- Find additional learning resources
- Fact-check workshop materials

**Research Methods**:
- **Web Search**: Current documentation and best practices
- **Content Validation**: Cross-reference with official sources
- **Resource Discovery**: Tutorials, guides, examples
- **Version Verification**: Latest releases and compatibility

### 6. Content Creator Agent (Port 10080)
**Purpose**: Generate original workshop content from objectives

**Core Capabilities**:
- Design workshops from learning objectives
- Create original educational content
- Generate hands-on exercises and activities
- Integrate with Showroom template system

**Content Types**:
- **Conceptual Workshops**: Theory and principles
- **Hands-on Workshops**: Practical skills and implementation
- **Hybrid Workshops**: Combined theory and practice
- **Assessment Workshops**: Skill evaluation and certification

## 🔄 Agent Coordination Workflows

### Repository-Based Workshop Creation
```
1. Template Converter Agent
   ├── Analyzes repository structure
   ├── Detects workshop vs. application
   └── Recommends conversion approach

2. Research & Validation Agent
   ├── Validates technical accuracy
   ├── Finds current documentation
   └── Suggests additional resources

3. Content Creator Agent
   ├── Generates workshop structure
   ├── Creates educational content
   └── Sets up Showroom template

4. Documentation Pipeline Agent
   ├── Configures monitoring
   ├── Sets up change detection
   └── Creates update workflows

5. Source Manager Agent
   ├── Creates workshop repository
   ├── Deploys to target platform
   └── Sets up maintenance procedures

6. Workshop Chat Agent
   ├── Ingests workshop content
   ├── Creates vector embeddings
   └── Enables participant assistance
```

### Original Content Workshop Creation
```
1. Content Creator Agent
   ├── Designs from learning objectives
   ├── Generates content structure
   └── Creates exercises and activities

2. Research & Validation Agent
   ├── Researches topic accuracy
   ├── Validates best practices
   └── Finds supporting resources

3. Content Creator Agent
   ├── Sets up Showroom template
   ├── Customizes for technology focus
   └── Integrates generated content

4. Documentation Pipeline Agent
   ├── Configures external monitoring
   ├── Sets up content validation
   └── Creates update procedures

5. Source Manager Agent
   ├── Creates workshop repository
   ├── Deploys to platform
   └── Sets up CI/CD pipeline

6. Workshop Chat Agent
   ├── Ingests all content
   ├── Enables Q&A assistance
   └── Provides navigation help
```

## 🧠 RAG Integration Architecture

### Vector Database Strategy
```
Content Sources → Embedding Generation → Vector Storage → Retrieval → Response Generation
     ↓                    ↓                   ↓            ↓            ↓
- Workshop content    - Sentence           - Pinecone    - Semantic   - Llama Stack
- External docs       transformers         - Metadata    search       - Contextual
- PDF documents       - Chunking           - Versioning  - Ranking    responses
- API documentation   - Preprocessing      - Indexing    - Filtering  - Citations
```

### Content Ingestion Pipeline
1. **Content Extraction**: Parse various formats (Markdown, PDF, HTML)
2. **Preprocessing**: Clean, chunk, and structure content
3. **Embedding Generation**: Create vector representations
4. **Metadata Enrichment**: Add source, version, date information
5. **Vector Storage**: Store in Pinecone with proper indexing
6. **Retrieval Optimization**: Configure search parameters

## 🔧 Technology Stack

### Core Infrastructure
- **Llama Stack**: LLM orchestration and agent coordination
- **Ollama**: Local LLM serving (Llama 3.2 3B)
- **Python**: Agent implementation and coordination
- **A2A Protocol**: Agent-to-Agent communication

### Storage and Retrieval
- **Pinecone**: Vector database for RAG
- **GitHub**: Source code and workshop repositories
- **File System**: Local content and configuration storage

### External Integrations
- **Firecrawl**: Web scraping and content extraction
- **GitHub API**: Repository analysis and management
- **Webhook Endpoints**: Real-time change notifications
- **RSS/Atom**: Documentation feed monitoring

### Deployment Platforms
- **RHPDS**: Red Hat Product Demo System
- **Showroom**: Red Hat Showroom platform
- **OpenShift**: Kubernetes-based container platform
- **GitHub Pages**: Free static hosting with upgrade path
- **Antora**: Documentation site generation

## 📊 System Characteristics

### Scalability
- **Horizontal Scaling**: Each agent can be scaled independently
- **Load Distribution**: Requests distributed across agent instances
- **Resource Optimization**: Agents scale based on demand

### Reliability
- **Fault Tolerance**: Agent failures don't affect entire system
- **Graceful Degradation**: System continues with reduced functionality
- **Health Monitoring**: Continuous agent health checks

### Performance
- **Asynchronous Processing**: Non-blocking agent communication
- **Caching**: Intelligent caching of LLM responses and content
- **Batch Processing**: Efficient handling of bulk operations

### Security
- **Agent Isolation**: Each agent runs in isolated environment
- **API Authentication**: Secure agent-to-agent communication
- **Content Validation**: Input sanitization and validation
- **Access Control**: Role-based access to system functions

## 🎯 Design Principles

### Modularity
Each agent has a single, well-defined responsibility with clear interfaces for interaction with other agents.

### Extensibility
New agents can be added to the system without modifying existing agents, following the A2A protocol.

### Maintainability
Clear separation of concerns makes the system easy to understand, debug, and enhance.

### Reusability
Agents and components can be reused across different workshop creation scenarios.

### Observability
Comprehensive logging, monitoring, and tracing enable effective system management and troubleshooting.

---

*This architecture enables automated, scalable, and maintainable workshop creation while preserving human oversight and quality control.*
