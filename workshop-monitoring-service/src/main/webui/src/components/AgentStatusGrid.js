import React, { useState } from 'react';
import {
  CheckCircle,
  AlertTriangle,
  XCircle,
  HelpCircle,
  Clock,
  Wrench,
  ExternalLink,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import { getHealthStatusColor } from '../services/monitoringApi';

/**
 * Agent Status Grid Component
 * Displays detailed status information for all workshop agents
 */
const AgentStatusGrid = ({ agentStatuses, agentsByStatus }) => {
  const [expandedAgent, setExpandedAgent] = useState(null);

  if (!agentStatuses || agentStatuses.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Agent Status</h3>
        <div className="text-center py-8">
          <HelpCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">No agent data available</p>
        </div>
      </div>
    );
  }

  const getStatusIcon = (status, size = 20) => {
    const color = getHealthStatusColor(status);
    const iconProps = { size, color };
    
    switch (status) {
      case 'HEALTHY':
        return <CheckCircle {...iconProps} />;
      case 'DEGRADED':
        return <AlertTriangle {...iconProps} />;
      case 'UNHEALTHY':
        return <XCircle {...iconProps} />;
      case 'UNKNOWN':
      default:
        return <HelpCircle {...iconProps} />;
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'HEALTHY':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'DEGRADED':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'UNHEALTHY':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'UNKNOWN':
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const formatResponseTime = (timeMs) => {
    if (timeMs < 1000) {
      return `${timeMs}ms`;
    } else {
      return `${(timeMs / 1000).toFixed(1)}s`;
    }
  };

  const getAgentDescription = (agentName) => {
    const descriptions = {
      'workshop-chat': 'RAG-based participant assistance and Q&A support',
      'template-converter': 'Repository-to-workshop transformation engine',
      'content-creator': 'Original workshop content creation and generation',
      'source-manager': 'Repository management and deployment coordination',
      'research-validation': 'Internet-grounded fact-checking and validation',
      'documentation-pipeline': 'Content monitoring and documentation updates'
    };
    return descriptions[agentName] || 'Workshop Template System Agent';
  };

  const toggleAgentExpansion = (agentName) => {
    setExpandedAgent(expandedAgent === agentName ? null : agentName);
  };

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-6 border-b border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900">Agent Status</h3>
        <p className="text-sm text-gray-600 mt-1">
          Detailed status for all {agentStatuses.length} workshop agents
        </p>
      </div>

      <div className="divide-y divide-gray-200">
        {agentStatuses.map((agent) => (
          <div key={agent.name} className="p-6">
            {/* Agent Header */}
            <div 
              className="flex items-center justify-between cursor-pointer"
              onClick={() => toggleAgentExpansion(agent.name)}
            >
              <div className="flex items-center space-x-4">
                {getStatusIcon(agent.health, 24)}
                <div>
                  <h4 className="text-lg font-medium text-gray-900">
                    {agent.name}
                  </h4>
                  <p className="text-sm text-gray-600">
                    {getAgentDescription(agent.name)}
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-4">
                {/* Status Badge */}
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getStatusBadgeClass(agent.health)}`}>
                  {agent.health}
                </span>

                {/* Response Time */}
                <div className="flex items-center text-sm text-gray-500">
                  <Clock size={16} className="mr-1" />
                  {formatResponseTime(agent.responseTimeMs || 0)}
                </div>

                {/* Endpoint Link */}
                <a
                  href={agent.endpoint}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:text-blue-800 transition-colors"
                  onClick={(e) => e.stopPropagation()}
                >
                  <ExternalLink size={16} />
                </a>

                {/* Expand/Collapse Icon */}
                {expandedAgent === agent.name ? (
                  <ChevronUp size={20} className="text-gray-400" />
                ) : (
                  <ChevronDown size={20} className="text-gray-400" />
                )}
              </div>
            </div>

            {/* Expanded Details */}
            {expandedAgent === agent.name && (
              <div className="mt-4 pl-10 space-y-4">
                {/* Basic Info */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <p className="text-sm font-medium text-gray-500">Endpoint</p>
                    <p className="text-sm text-gray-900 font-mono">{agent.endpoint}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-500">Last Checked</p>
                    <p className="text-sm text-gray-900">
                      {agent.lastChecked ? new Date(agent.lastChecked).toLocaleString() : 'Never'}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-500">Response Time</p>
                    <p className="text-sm text-gray-900">
                      {formatResponseTime(agent.responseTimeMs || 0)}
                    </p>
                  </div>
                </div>

                {/* Available Tools */}
                {agent.availableTools && agent.availableTools.length > 0 && (
                  <div>
                    <p className="text-sm font-medium text-gray-500 mb-2 flex items-center">
                      <Wrench size={16} className="mr-1" />
                      Available Tools ({agent.availableTools.length})
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {agent.availableTools.map((tool, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                        >
                          {tool}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Error Message */}
                {agent.errorMessage && (
                  <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-sm font-medium text-red-800">Error Details:</p>
                    <p className="text-sm text-red-700 mt-1">{agent.errorMessage}</p>
                  </div>
                )}

                {/* Metadata */}
                {agent.metadata && Object.keys(agent.metadata).length > 0 && (
                  <div>
                    <p className="text-sm font-medium text-gray-500 mb-2">Additional Information</p>
                    <div className="bg-gray-50 rounded-lg p-3">
                      <pre className="text-xs text-gray-700 overflow-x-auto">
                        {JSON.stringify(agent.metadata, null, 2)}
                      </pre>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* Summary Footer */}
      <div className="p-6 bg-gray-50 border-t border-gray-200">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
          <div>
            <p className="text-2xl font-bold text-green-600">
              {agentsByStatus.healthy?.length || 0}
            </p>
            <p className="text-sm text-gray-600">Healthy</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-yellow-600">
              {agentsByStatus.degraded?.length || 0}
            </p>
            <p className="text-sm text-gray-600">Degraded</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-red-600">
              {agentsByStatus.unhealthy?.length || 0}
            </p>
            <p className="text-sm text-gray-600">Unhealthy</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-gray-600">
              {agentsByStatus.unknown?.length || 0}
            </p>
            <p className="text-sm text-gray-600">Unknown</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AgentStatusGrid;
