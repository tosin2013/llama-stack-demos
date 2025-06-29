import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { 
  MessageCircle, 
  GitBranch, 
  FileText, 
  Database, 
  Search, 
  BookOpen, 
  Users,
  Play,
  Settings,
  ChevronRight,
  Zap,
  Activity
} from 'lucide-react';

/**
 * Agent Interaction Menu Component - Provides direct interface to interact with all 7 workshop agents
 * Includes individual agent interactions and pre-built workflow templates
 */
const AgentInteractionMenu = () => {
  const [agentConfig, setAgentConfig] = useState(null);
  const [selectedAgent, setSelectedAgent] = useState(null);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Agent icon mapping
  const iconMap = {
    MessageCircle,
    GitBranch,
    FileText,
    Database,
    Search,
    BookOpen,
    Users
  };

  // Fetch agent configuration
  useEffect(() => {
    const fetchAgentConfig = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/agent-interaction/config');
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        setAgentConfig(data.agentInteractionMenu);
        setError(null);
      } catch (err) {
        console.error('Error fetching agent configuration:', err);
        setError('Failed to load agent configuration');
      } finally {
        setLoading(false);
      }
    };

    fetchAgentConfig();
  }, []);

  // Handle agent interaction
  const handleAgentInteraction = async (agent, interactionType, inputData = {}) => {
    try {
      const response = await fetch(interactionType.endpoint, {
        method: interactionType.method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: interactionType.method !== 'GET' ? JSON.stringify(inputData) : undefined,
      });

      if (!response.ok) {
        throw new Error(`Agent interaction failed: ${response.status}`);
      }

      const result = await response.json();
      return result;
    } catch (err) {
      console.error('Agent interaction error:', err);
      throw err;
    }
  };

  // Handle workflow execution
  const handleWorkflowExecution = async (workflow, inputData = {}) => {
    try {
      const response = await fetch('/api/agent-interaction/workflow/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          workflowId: workflow.id,
          inputData: inputData
        }),
      });

      if (!response.ok) {
        throw new Error(`Workflow execution failed: ${response.status}`);
      }

      const result = await response.json();
      return result;
    } catch (err) {
      console.error('Workflow execution error:', err);
      throw err;
    }
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Agent Interaction</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-2">Loading agent configuration...</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Agent Interaction</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8">
            <div className="text-red-600 mb-4">{error}</div>
            <Button onClick={() => window.location.reload()}>
              Retry
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Agent Grid */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Activity className="h-5 w-5" />
            <span>Workshop Agents</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {agentConfig?.agents?.map((agent) => {
              const IconComponent = iconMap[agent.icon] || MessageCircle;
              
              return (
                <Card 
                  key={agent.id}
                  className={`cursor-pointer transition-all duration-200 hover:shadow-md ${
                    selectedAgent?.id === agent.id ? 'ring-2 ring-blue-500' : ''
                  }`}
                  onClick={() => setSelectedAgent(agent)}
                >
                  <CardContent className="p-4">
                    <div className="flex items-start space-x-3">
                      <div className={`p-2 rounded-lg bg-${agent.color}-100`}>
                        <IconComponent className={`h-5 w-5 text-${agent.color}-600`} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <h3 className="font-medium text-sm truncate">{agent.name}</h3>
                        <p className="text-xs text-gray-500 mt-1 line-clamp-2">
                          {agent.description}
                        </p>
                        <div className="mt-2">
                          <Badge variant="outline" className="text-xs">
                            {agent.capabilities?.length || 0} capabilities
                          </Badge>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </CardContent>
      </Card>

      {/* Workflow Templates */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Zap className="h-5 w-5" />
            <span>Workflow Templates</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {agentConfig?.workflowTemplates?.map((workflow) => (
              <Card 
                key={workflow.id}
                className={`cursor-pointer transition-all duration-200 hover:shadow-md ${
                  selectedWorkflow?.id === workflow.id ? 'ring-2 ring-green-500' : ''
                }`}
                onClick={() => setSelectedWorkflow(workflow)}
              >
                <CardContent className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="font-medium text-sm mb-1">{workflow.name}</h3>
                      <p className="text-xs text-gray-500 mb-3">{workflow.description}</p>
                      
                      <div className="flex items-center space-x-2 mb-3">
                        <Badge variant="outline" className="text-xs">
                          {workflow.agents?.length || 0} agents
                        </Badge>
                        <Badge variant="outline" className="text-xs">
                          {workflow.steps?.length || 0} steps
                        </Badge>
                      </div>
                      
                      <div className="flex items-center space-x-1">
                        {workflow.steps?.slice(0, 3).map((step, index) => (
                          <React.Fragment key={index}>
                            <div className="w-2 h-2 bg-blue-400 rounded-full"></div>
                            {index < 2 && <ChevronRight className="h-3 w-3 text-gray-400" />}
                          </React.Fragment>
                        ))}
                        {workflow.steps?.length > 3 && (
                          <span className="text-xs text-gray-400 ml-1">+{workflow.steps.length - 3}</span>
                        )}
                      </div>
                    </div>
                    
                    <Button 
                      size="sm" 
                      variant="outline"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleWorkflowExecution(workflow);
                      }}
                    >
                      <Play className="h-3 w-3 mr-1" />
                      Start
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Selected Agent Details */}
      {selectedAgent && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Settings className="h-5 w-5" />
              <span>{selectedAgent.name} - Interactions</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div>
                <h4 className="font-medium mb-2">Capabilities</h4>
                <div className="flex flex-wrap gap-2">
                  {selectedAgent.capabilities?.map((capability, index) => (
                    <Badge key={index} variant="secondary" className="text-xs">
                      {capability}
                    </Badge>
                  ))}
                </div>
              </div>
              
              <div>
                <h4 className="font-medium mb-2">Available Interactions</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {selectedAgent.interactionTypes?.map((interaction, index) => (
                    <Button
                      key={index}
                      variant="outline"
                      className="justify-start h-auto p-3"
                      onClick={() => handleAgentInteraction(selectedAgent, interaction)}
                    >
                      <div className="text-left">
                        <div className="font-medium text-sm">{interaction.label}</div>
                        <div className="text-xs text-gray-500 mt-1">
                          {interaction.method} {interaction.endpoint}
                        </div>
                      </div>
                    </Button>
                  ))}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default AgentInteractionMenu;
