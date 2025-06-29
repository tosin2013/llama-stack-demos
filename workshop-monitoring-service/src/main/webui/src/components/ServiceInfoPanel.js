import React from 'react';
import { X, Info, Server, Globe, Code, Clock } from 'lucide-react';

/**
 * Service Info Panel Component
 * Displays detailed information about the monitoring service and configuration
 */
const ServiceInfoPanel = ({ serviceInfo, onClose }) => {
  if (!serviceInfo) {
    return null;
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <Info className="h-6 w-6 text-blue-600" />
            <h2 className="text-xl font-semibold text-gray-900">
              Service Information
            </h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Service Overview */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3 flex items-center">
                  <Server className="h-5 w-5 mr-2 text-blue-600" />
                  Service Details
                </h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Service Name:</span>
                    <span className="text-sm font-medium text-gray-900">
                      {serviceInfo.service_name}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Version:</span>
                    <span className="text-sm font-medium text-gray-900">
                      {serviceInfo.version}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-sm text-gray-600">Last Health Update:</span>
                    <span className="text-sm font-medium text-gray-900">
                      {serviceInfo.last_health_update 
                        ? new Date(serviceInfo.last_health_update).toLocaleString()
                        : 'Never'
                      }
                    </span>
                  </div>
                </div>
              </div>

              <div>
                <h4 className="text-md font-medium text-gray-900 mb-2">Description</h4>
                <p className="text-sm text-gray-700 bg-gray-50 p-3 rounded-lg">
                  {serviceInfo.description}
                </p>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-3 flex items-center">
                  <Globe className="h-5 w-5 mr-2 text-green-600" />
                  API Endpoints
                </h3>
                <div className="space-y-2">
                  {serviceInfo.api_endpoints?.map((endpoint, index) => (
                    <div key={index} className="flex items-center space-x-2">
                      <Code size={14} className="text-gray-400" />
                      <span className="text-sm font-mono text-gray-700 bg-gray-100 px-2 py-1 rounded">
                        {endpoint}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Configured Agents */}
          <div>
            <h3 className="text-lg font-medium text-gray-900 mb-3 flex items-center">
              <Server className="h-5 w-5 mr-2 text-purple-600" />
              Configured Agents ({serviceInfo.configured_agents?.length || 0})
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {serviceInfo.configured_agents?.map((agentName, index) => {
                const agentDescriptions = {
                  'workshop-chat': {
                    description: 'RAG-based participant assistance and Q&A support',
                    port: '8080',
                    color: 'blue'
                  },
                  'template-converter': {
                    description: 'Repository-to-workshop transformation engine',
                    port: '8081',
                    color: 'green'
                  },
                  'content-creator': {
                    description: 'Original workshop content creation and generation',
                    port: '8082',
                    color: 'purple'
                  },
                  'source-manager': {
                    description: 'Repository management and deployment coordination',
                    port: '8083',
                    color: 'orange'
                  },
                  'research-validation': {
                    description: 'Internet-grounded fact-checking and validation',
                    port: '8084',
                    color: 'red'
                  },
                  'documentation-pipeline': {
                    description: 'Content monitoring and documentation updates',
                    port: '8085',
                    color: 'indigo'
                  }
                };

                const agentInfo = agentDescriptions[agentName] || {
                  description: 'Workshop Template System Agent',
                  port: 'Unknown',
                  color: 'gray'
                };

                return (
                  <div key={index} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-center space-x-2 mb-2">
                      <div className={`w-3 h-3 rounded-full bg-${agentInfo.color}-500`}></div>
                      <h4 className="text-sm font-medium text-gray-900">
                        {agentName}
                      </h4>
                    </div>
                    <p className="text-xs text-gray-600 mb-2">
                      {agentInfo.description}
                    </p>
                    <div className="flex items-center text-xs text-gray-500">
                      <Globe size={12} className="mr-1" />
                      <span>Port {agentInfo.port}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* System Architecture */}
          <div>
            <h3 className="text-lg font-medium text-gray-900 mb-3">
              System Architecture
            </h3>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <h4 className="font-medium text-gray-900 mb-2">Frontend</h4>
                  <ul className="space-y-1 text-gray-700">
                    <li>• React 18 with modern hooks</li>
                    <li>• Tailwind CSS for styling</li>
                    <li>• Recharts for data visualization</li>
                    <li>• Real-time monitoring dashboard</li>
                  </ul>
                </div>
                <div>
                  <h4 className="font-medium text-gray-900 mb-2">Backend</h4>
                  <ul className="space-y-1 text-gray-700">
                    <li>• Quarkus Java 17 framework</li>
                    <li>• JAX-RS REST endpoints</li>
                    <li>• Scheduled health monitoring</li>
                    <li>• OpenAPI 3.1 documentation</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* Monitoring Features */}
          <div>
            <h3 className="text-lg font-medium text-gray-900 mb-3 flex items-center">
              <Clock className="h-5 w-5 mr-2 text-blue-600" />
              Monitoring Features
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <h4 className="text-sm font-medium text-gray-900">Health Monitoring</h4>
                <ul className="text-sm text-gray-700 space-y-1">
                  <li>• Automated health checks every 30 seconds</li>
                  <li>• Response time tracking and analysis</li>
                  <li>• Agent status aggregation</li>
                  <li>• Real-time system health calculation</li>
                </ul>
              </div>
              <div className="space-y-2">
                <h4 className="text-sm font-medium text-gray-900">Dashboard Features</h4>
                <ul className="text-sm text-gray-700 space-y-1">
                  <li>• Interactive agent status grid</li>
                  <li>• Response time visualization</li>
                  <li>• Manual health check triggers</li>
                  <li>• Detailed error reporting</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Technical Specifications */}
          <div className="border-t border-gray-200 pt-6">
            <h3 className="text-lg font-medium text-gray-900 mb-3">
              Technical Specifications
            </h3>
            <div className="bg-gray-900 rounded-lg p-4 text-green-400 font-mono text-sm overflow-x-auto">
              <pre>{JSON.stringify(serviceInfo, null, 2)}</pre>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="border-t border-gray-200 px-6 py-4 bg-gray-50">
          <div className="flex justify-between items-center">
            <p className="text-sm text-gray-600">
              Workshop Template System Monitoring Service
            </p>
            <button
              onClick={onClose}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ServiceInfoPanel;
