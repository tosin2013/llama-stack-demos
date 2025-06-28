# Intelligent Workshop Template System - Implementation Todo List

## Project Overview
Create a comprehensive Intelligent Workshop Template System (demos/workshop_template_system) that extends the proven demos/a2a_llama_stack architecture to transform GitHub projects into interactive workshop content.

## Core Objectives
- Transform GitHub projects into rhpds/showroom_template_default compatible workshops
- Provide interactive chat assistance for workshop participants
- Implement automated documentation pipelines with human oversight
- Support both OpenShift and local Podman deployments
- Achieve 80%+ code reuse from existing patterns

## Task Breakdown

### Phase 1: Foundation (Days 1-3)

#### 1. Project Structure Setup and Base Configuration
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** None  

**Description:**
Create the foundational directory structure for demos/workshop_template_system following the established demos/a2a_llama_stack patterns.

**Key Deliverables:**
- [ ] Create demos/workshop_template_system/ directory structure
- [ ] Copy and adapt __main__.py, requirements.txt from a2a_llama_stack
- [ ] Set up agents/ subdirectory with four agent folders
- [ ] Create base configuration following existing AGENT_CONFIG pattern

**Files to Create:**
- `demos/workshop_template_system/__init__.py`
- `demos/workshop_template_system/__main__.py`
- `demos/workshop_template_system/requirements.txt`
- `demos/workshop_template_system/task_manager.py`
- `demos/workshop_template_system/agents/` (directory structure)

**Reference Files:**
- `demos/a2a_llama_stack/__main__.py` (template)
- `demos/a2a_llama_stack/requirements.txt` (base requirements)

### Phase 2: Core Agents (Days 4-10)

#### 2. Interactive Chat Agent Implementation
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** Project Structure Setup  

**Description:**
Implement workshop chat agent that provides real-time assistance to workshop participants using RAG patterns.

**Key Deliverables:**
- [ ] Create workshop_chat agent configuration
- [ ] Implement RAG-based tools for workshop content retrieval
- [ ] Add conversation context management
- [ ] Create workshop-specific content indexing

**Files to Create:**
- `demos/workshop_template_system/agents/workshop_chat/config.py`
- `demos/workshop_template_system/agents/workshop_chat/tools.py`

**Reference Files:**
- `demos/rag_agentic/notebooks/A2A_Agentic_RAG.ipynb`
- `demos/a2a_llama_stack/agents/a2a_custom_tools/config.py`

#### 3. Template Conversion Agent Implementation
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** Project Structure Setup  

**Description:**
Implement GitHub repository to workshop template conversion agent using existing GitHub MCP integration.

**Key Deliverables:**
- [ ] Create template_converter agent configuration
- [ ] Implement GitHub repository analysis tools
- [ ] Add showroom template structure generation
- [ ] Implement human oversight checkpoints

**Files to Create:**
- `demos/workshop_template_system/agents/template_converter/config.py`
- `demos/workshop_template_system/agents/template_converter/tools.py`

**Reference Files:**
- `kubernetes/mcp-servers/github-mcp/` (GitHub integration)
- `research.md` (showroom template requirements)

#### 4. Documentation Pipeline Agent Implementation
**Status:** [ ] Not Started  
**Priority:** Medium  
**Dependencies:** Project Structure Setup  

**Description:**
Implement automated documentation pipeline agent with change detection and human-in-the-loop validation.

**Key Deliverables:**
- [ ] Create doc_pipeline agent configuration
- [ ] Implement repository monitoring and change detection
- [ ] Add impact analysis for workshop content updates
- [ ] Create human review workflow integration

**Files to Create:**
- `demos/workshop_template_system/agents/doc_pipeline/config.py`
- `demos/workshop_template_system/agents/doc_pipeline/tools.py`

**Reference Files:**
- `.github/workflows/` (CI/CD patterns)
- `kubernetes/kustomize/overlay/` (configuration management)

#### 5. Source Management Agent Implementation
**Status:** [ ] Not Started  
**Priority:** Medium  
**Dependencies:** Project Structure Setup  

**Description:**
Implement trusted source configuration and validation agent for authoritative documentation management.

**Key Deliverables:**
- [ ] Create source_manager agent configuration
- [ ] Implement trusted source configuration management
- [ ] Add source validation and integrity checking
- [ ] Create citation and reference management

**Files to Create:**
- `demos/workshop_template_system/agents/source_manager/config.py`
- `demos/workshop_template_system/agents/source_manager/tools.py`

**Reference Files:**
- `kubernetes/kustomize/overlay/dev/run-config-patch.yaml`

### Phase 3: Integration (Days 11-14)

#### 6. A2A Fleet Integration and Multi-Agent Coordination
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** All four agent implementations  

**Description:**
Integrate all workshop agents into A2AFleet coordination system with unified orchestration.

**Key Deliverables:**
- [ ] Extend A2AFleet to include workshop agents
- [ ] Configure inter-agent communication protocols
- [ ] Implement workflow orchestration
- [ ] Create unified API endpoints

**Files to Create:**
- `demos/workshop_template_system/workshop_fleet.py`

**Reference Files:**
- `demos/a2a_llama_stack/A2AFleet.py`
- `demos/a2a_llama_stack/notebooks/A2A_Advanced_Multi_Agent.ipynb`

#### 7. OpenShift Deployment Configuration
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** A2A Fleet Integration  

**Description:**
Create OpenShift deployment manifests extending existing kubernetes/llama-stack patterns.

**Key Deliverables:**
- [ ] Create Kubernetes deployment manifests
- [ ] Configure Services and Routes
- [ ] Set up ConfigMaps and PVC
- [ ] Add monitoring configurations

**Files to Create:**
- `kubernetes/workshop-template-system/deployment.yaml`
- `kubernetes/workshop-template-system/service.yaml`
- `kubernetes/workshop-template-system/route.yaml`
- `kubernetes/workshop-template-system/configmap.yaml`
- `kubernetes/workshop-template-system/pvc.yaml`

**Reference Files:**
- `kubernetes/llama-stack/deployment.yaml`
- `kubernetes/llama-stack/service.yaml`

#### 8. Local Development Setup and Makefile Integration
**Status:** [ ] Not Started  
**Priority:** Medium  
**Dependencies:** A2A Fleet Integration  

**Description:**
Extend existing Makefile and local setup patterns for workshop template system development.

**Key Deliverables:**
- [ ] Extend Makefile with workshop targets
- [ ] Create Podman container build configurations
- [ ] Add local development environment setup
- [ ] Create testing and validation targets

**Files to Modify:**
- `Makefile` (add workshop targets)
- `local_setup_guide.md` (add setup instructions)

**Files to Create:**
- `demos/workshop_template_system/Containerfile`

### Phase 4: Testing & Documentation (Days 15-16)

#### 9. Testing Framework and Quality Assurance
**Status:** [ ] Not Started  
**Priority:** High  
**Dependencies:** A2A Fleet Integration  

**Description:**
Implement comprehensive testing framework with unit, integration, and end-to-end tests.

**Key Deliverables:**
- [ ] Create unit tests for each agent
- [ ] Implement integration tests for A2A communication
- [ ] Add end-to-end workflow tests
- [ ] Create performance and load testing

**Files to Create:**
- `tests/workshop_template_system/unit/`
- `tests/workshop_template_system/integration/`
- `tests/workshop_template_system/e2e/`

#### 10. Documentation and User Guides
**Status:** [ ] Not Started  
**Priority:** Medium  
**Dependencies:** Testing Framework  

**Description:**
Create comprehensive documentation including README, user guides, and API documentation.

**Key Deliverables:**
- [ ] Create main README for workshop template system
- [ ] Write user guides for each agent and workflow
- [ ] Document API endpoints and integration patterns
- [ ] Create deployment and configuration guides

**Files to Create:**
- `demos/workshop_template_system/README.md`
- `demos/workshop_template_system/docs/` (user guides)

**Reference Files:**
- `demos/a2a_llama_stack/README.md` (documentation pattern)

## Success Metrics
- [ ] All four agents successfully coordinate through A2A protocol
- [ ] GitHub repositories can be converted to workshop templates
- [ ] Interactive chat provides contextual workshop assistance
- [ ] Documentation pipeline maintains content currency
- [ ] System deploys successfully on both OpenShift and local environments
- [ ] 80%+ code reuse achieved from existing patterns
- [ ] Comprehensive test coverage (>90%)
- [ ] Complete documentation and user guides

## Risk Mitigation
- **Technical Risk:** Start with existing RAG and GitHub MCP integrations
- **Integration Risk:** Use proven A2A agent patterns for coordination
- **Deployment Risk:** Follow established Kubernetes deployment patterns
- **Quality Risk:** Implement comprehensive testing using existing infrastructure

## Resources and References
- **Base Architecture:** `demos/a2a_llama_stack/`
- **RAG Patterns:** `demos/rag_agentic/`
- **GitHub Integration:** `kubernetes/mcp-servers/github-mcp/`
- **Deployment Patterns:** `kubernetes/llama-stack/`
- **Requirements:** `research.md`
