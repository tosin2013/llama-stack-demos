import React, { useState, useEffect } from 'react';
import {
  Users,
  Shield,
  CheckCircle,
  AlertTriangle,
  Activity,
  Settings,
  Eye,
  BarChart3,
  Clock,
  TrendingUp,
  RefreshCw,
  MessageCircle,
  Terminal,
  ThumbsUp,
  ThumbsDown,
  Send,
  Mic,
  MicOff,
  Play,
  Pause,
  Square,
  FileText,
  Download,
  Upload
} from 'lucide-react';

/**
 * Human Oversight Panel Component
 * Implements ADR-0004 Human Oversight Domain with centralized coordinator controls
 * Provides quality assurance, compliance monitoring, and agent coordination
 */
const HumanOversightPanel = () => {
  const [coordinatorStatus, setCoordinatorStatus] = useState(null);
  const [activeWorkflows, setActiveWorkflows] = useState([]);
  const [qualityMetrics, setQualityMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Interaction modes
  const [activeTab, setActiveTab] = useState('overview');
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [commandInput, setCommandInput] = useState('');
  const [commandHistory, setCommandHistory] = useState([]);
  const [isRecording, setIsRecording] = useState(false);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);

  // Fetch coordinator data
  useEffect(() => {
    fetchCoordinatorData();
    const interval = setInterval(fetchCoordinatorData, 30000); // 30 second refresh
    return () => clearInterval(interval);
  }, []);

  const fetchCoordinatorData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch coordinator status
      const coordinatorResponse = await fetch('/api/oversight/coordinator/status');
      if (coordinatorResponse.ok) {
        const coordinatorData = await coordinatorResponse.json();
        setCoordinatorStatus(coordinatorData);
      }

      // Fetch active workflows
      const workflowsResponse = await fetch('/api/oversight/workflows/active');
      if (workflowsResponse.ok) {
        const workflowsData = await workflowsResponse.json();
        setActiveWorkflows(workflowsData.data?.workflows || []);
      }

      // Fetch quality metrics
      const metricsResponse = await fetch('/api/oversight/metrics/quality');
      if (metricsResponse.ok) {
        const metricsData = await metricsResponse.json();
        setQualityMetrics(metricsData.data);
      }

    } catch (err) {
      console.error('Error fetching oversight data:', err);
      setError('Failed to load oversight data');
    } finally {
      setLoading(false);
    }
  };

  // Chat interaction handlers
  const handleChatSend = async () => {
    if (!chatInput.trim()) return;

    const userMessage = { type: 'user', content: chatInput, timestamp: new Date() };
    setChatMessages(prev => [...prev, userMessage]);

    try {
      // Chat with oversight coordinator via backend API
      const response = await fetch('/api/oversight/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: chatInput })
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success && result.data && result.data.message) {
          const botMessage = {
            type: 'assistant',
            content: result.data.message.content,
            timestamp: new Date(),
            responseTime: result.data.message.response_time_ms,
            confidence: result.data.message.confidence_score
          };
          setChatMessages(prev => [...prev, botMessage]);
        } else {
          throw new Error('Invalid response format from chat API');
        }
      } else {
        throw new Error(`Chat API error: ${response.status} ${response.statusText}`);
      }
    } catch (err) {
      console.error('Chat error:', err);
      const errorMessage = {
        type: 'assistant',
        content: 'I apologize, but I encountered an error processing your request. Please try again.',
        timestamp: new Date()
      };
      setChatMessages(prev => [...prev, errorMessage]);
    }

    setChatInput('');
  };

  // Command execution handlers
  const handleCommandExecute = async () => {
    if (!commandInput.trim()) return;

    const command = { command: commandInput, timestamp: new Date(), status: 'executing' };
    setCommandHistory(prev => [...prev, command]);

    try {
      // Execute command via oversight coordinator
      const response = await fetch('/api/oversight/coordinate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'execute_command', command: commandInput })
      });

      if (response.ok) {
        const result = await response.json();
        setCommandHistory(prev => prev.map(cmd =>
          cmd.command === commandInput ? { ...cmd, status: 'completed', result: result.data } : cmd
        ));
      }
    } catch (err) {
      console.error('Command error:', err);
      setCommandHistory(prev => prev.map(cmd =>
        cmd.command === commandInput ? { ...cmd, status: 'failed', error: err.message } : cmd
      ));
    }

    setCommandInput('');
  };

  // Workflow approval handlers
  const handleApproveWorkflow = async (workflowId) => {
    try {
      const response = await fetch(`/api/oversight/workflows/${workflowId}/approve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          comment: 'Approved via dashboard interface',
          approver: 'human-operator'
        })
      });

      if (response.ok) {
        fetchCoordinatorData(); // Refresh data
      }
    } catch (err) {
      console.error('Approval error:', err);
    }
  };

  const handleRejectWorkflow = async (workflowId) => {
    try {
      const response = await fetch(`/api/oversight/workflows/${workflowId}/reject`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          comment: 'Rejected via dashboard interface',
          approver: 'human-operator'
        })
      });

      if (response.ok) {
        fetchCoordinatorData(); // Refresh data
      }
    } catch (err) {
      console.error('Rejection error:', err);
    }
  };

  if (loading && !coordinatorStatus) {
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
            <div className="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
            <div className="h-4 bg-gray-200 rounded w-3/4"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Error Banner */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <AlertTriangle className="h-5 w-5 text-red-500 mr-2" />
            <p className="text-red-700">{error}</p>
            <button
              onClick={fetchCoordinatorData}
              className="ml-auto text-red-600 hover:text-red-800"
            >
              <RefreshCw className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Interaction Mode Tabs */}
      <div className="bg-white rounded-lg shadow">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8 px-6">
            {[
              { id: 'overview', name: 'Overview', icon: Eye },
              { id: 'chat', name: 'Chat Interface', icon: MessageCircle },
              { id: 'commands', name: 'Commands', icon: Terminal },
              { id: 'approvals', name: 'Approvals', icon: CheckCircle }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center space-x-2`}
              >
                <tab.icon className="h-4 w-4" />
                <span>{tab.name}</span>
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <>
          {/* Coordinator Status Card */}
          <div className="bg-white rounded-lg shadow">
            <div className="p-6 border-b border-gray-200">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Users className="h-8 w-8 text-blue-600" />
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">
                      Human Oversight Coordinator
                    </h2>
                    <p className="text-sm text-gray-600">
                      Quality assurance and compliance monitoring
                    </p>
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <div className={`w-3 h-3 rounded-full ${
                    coordinatorStatus?.data?.status === 'HEALTHY' ? 'bg-green-500' :
                    coordinatorStatus?.data?.status === 'DEGRADED' ? 'bg-yellow-500' : 'bg-red-500'
                  }`}></div>
                  <span className="text-sm font-medium text-gray-700">
                    {coordinatorStatus?.data?.status || 'UNKNOWN'}
                  </span>
                </div>
              </div>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {/* Active Oversight Sessions */}
                <div className="text-center">
                  <div className="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-full mx-auto mb-2">
                    <Eye className="h-6 w-6 text-blue-600" />
                  </div>
                  <p className="text-2xl font-semibold text-gray-900">
                    {coordinatorStatus?.data?.activeSessions || 0}
                  </p>
                  <p className="text-sm text-gray-600">Active Sessions</p>
                </div>

                {/* Pending Approvals */}
                <div className="text-center">
                  <div className="flex items-center justify-center w-12 h-12 bg-orange-100 rounded-full mx-auto mb-2">
                    <Clock className="h-6 w-6 text-orange-600" />
                  </div>
                  <p className="text-2xl font-semibold text-gray-900">
                    {coordinatorStatus?.data?.pendingApprovals || 0}
                  </p>
                  <p className="text-sm text-gray-600">Pending Approvals</p>
                </div>

                {/* Quality Score */}
                <div className="text-center">
                  <div className="flex items-center justify-center w-12 h-12 bg-green-100 rounded-full mx-auto mb-2">
                    <Shield className="h-6 w-6 text-green-600" />
                  </div>
                  <p className="text-2xl font-semibold text-gray-900">
                    {qualityMetrics?.overallScore || 0}%
                  </p>
                  <p className="text-sm text-gray-600">Quality Score</p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Chat Interface Tab */}
      {activeTab === 'chat' && (
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium text-gray-900">Chat with Oversight Coordinator</h3>
              <div className="flex items-center space-x-2">
                <MessageCircle className="h-5 w-5 text-gray-400" />
                <button
                  onClick={() => setIsRecording(!isRecording)}
                  className={`p-2 rounded-full ${isRecording ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'}`}
                >
                  {isRecording ? <MicOff className="h-4 w-4" /> : <Mic className="h-4 w-4" />}
                </button>
              </div>
            </div>
          </div>

          <div className="p-6">
            {/* Chat Messages */}
            <div className="h-96 overflow-y-auto border border-gray-200 rounded-lg p-4 mb-4 bg-gray-50">
              {chatMessages.length === 0 ? (
                <div className="text-center py-8">
                  <MessageCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-500">Start a conversation with the oversight coordinator</p>
                  <p className="text-sm text-gray-400 mt-2">
                    You can ask about workflows, quality metrics, agent status, or request actions
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {chatMessages.map((message, index) => (
                    <div key={index} className={`flex ${message.type === 'user' ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                        message.type === 'user'
                          ? 'bg-blue-600 text-white'
                          : 'bg-white border border-gray-200'
                      }`}>
                        <p className="text-sm">{message.content}</p>
                        <p className={`text-xs mt-1 ${
                          message.type === 'user' ? 'text-blue-100' : 'text-gray-500'
                        }`}>
                          {message.timestamp.toLocaleTimeString()}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Chat Input */}
            <div className="flex space-x-2">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleChatSend()}
                placeholder="Type your message to the oversight coordinator..."
                className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <button
                onClick={handleChatSend}
                disabled={!chatInput.trim()}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Send className="h-4 w-4" />
              </button>
            </div>

            {/* Quick Actions */}
            <div className="mt-4 flex flex-wrap gap-2">
              <button
                onClick={() => setChatInput('Show me the current system status')}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-gray-200"
              >
                System Status
              </button>
              <button
                onClick={() => setChatInput('What workflows need my attention?')}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-gray-200"
              >
                Pending Workflows
              </button>
              <button
                onClick={() => setChatInput('Run quality assurance check')}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-gray-200"
              >
                Quality Check
              </button>
              <button
                onClick={() => setChatInput('Generate compliance report')}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm hover:bg-gray-200"
              >
                Compliance Report
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Commands Tab */}
      {activeTab === 'commands' && (
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium text-gray-900">Command Interface</h3>
              <Terminal className="h-5 w-5 text-gray-400" />
            </div>
          </div>

          <div className="p-6">
            {/* Command Input */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Execute Command
              </label>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={commandInput}
                  onChange={(e) => setCommandInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleCommandExecute()}
                  placeholder="Enter command (e.g., 'approve workflow wf-001', 'quality check', 'agent status')"
                  className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono"
                />
                <button
                  onClick={handleCommandExecute}
                  disabled={!commandInput.trim()}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <Play className="h-4 w-4" />
                </button>
              </div>
            </div>

            {/* Command History */}
            <div className="mb-6">
              <h4 className="text-sm font-medium text-gray-700 mb-3">Command History</h4>
              <div className="bg-gray-900 text-green-400 rounded-lg p-4 h-64 overflow-y-auto font-mono text-sm">
                {commandHistory.length === 0 ? (
                  <div className="text-gray-500">
                    <p>$ # Command history will appear here</p>
                    <p>$ # Available commands:</p>
                    <p>$ # - approve workflow &lt;id&gt;</p>
                    <p>$ # - reject workflow &lt;id&gt;</p>
                    <p>$ # - quality check</p>
                    <p>$ # - agent status</p>
                    <p>$ # - system health</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {commandHistory.map((cmd, index) => (
                      <div key={index}>
                        <div className="flex items-center space-x-2">
                          <span className="text-blue-400">$</span>
                          <span>{cmd.command}</span>
                          <span className={`ml-auto text-xs ${
                            cmd.status === 'completed' ? 'text-green-400' :
                            cmd.status === 'failed' ? 'text-red-400' :
                            'text-yellow-400'
                          }`}>
                            [{cmd.status}]
                          </span>
                        </div>
                        {cmd.result && (
                          <div className="ml-4 text-gray-300 text-xs">
                            {JSON.stringify(cmd.result, null, 2)}
                          </div>
                        )}
                        {cmd.error && (
                          <div className="ml-4 text-red-400 text-xs">
                            Error: {cmd.error}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Quick Commands */}
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-3">Quick Commands</h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                <button
                  onClick={() => setCommandInput('system health')}
                  className="px-3 py-2 bg-blue-100 text-blue-700 rounded-lg text-sm hover:bg-blue-200 font-mono"
                >
                  system health
                </button>
                <button
                  onClick={() => setCommandInput('agent status')}
                  className="px-3 py-2 bg-green-100 text-green-700 rounded-lg text-sm hover:bg-green-200 font-mono"
                >
                  agent status
                </button>
                <button
                  onClick={() => setCommandInput('quality check')}
                  className="px-3 py-2 bg-purple-100 text-purple-700 rounded-lg text-sm hover:bg-purple-200 font-mono"
                >
                  quality check
                </button>
                <button
                  onClick={() => setCommandInput('list workflows')}
                  className="px-3 py-2 bg-orange-100 text-orange-700 rounded-lg text-sm hover:bg-orange-200 font-mono"
                >
                  list workflows
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Approvals Tab */}
      {activeTab === 'approvals' && (
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium text-gray-900">Workflow Approvals</h3>
              <CheckCircle className="h-5 w-5 text-gray-400" />
            </div>
          </div>

          <div className="p-6">
            {/* Active Workflows Requiring Approval */}
            <div className="space-y-4">
              {activeWorkflows.filter(w => w.status === 'pending_approval').length === 0 ? (
                <div className="text-center py-8">
                  <CheckCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-500">No workflows pending approval</p>
                  <p className="text-sm text-gray-400 mt-2">
                    All workflows are either approved or in progress
                  </p>
                </div>
              ) : (
                activeWorkflows
                  .filter(w => w.status === 'pending_approval')
                  .map((workflow) => (
                    <div key={workflow.id} className="border border-gray-200 rounded-lg p-6">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h4 className="text-lg font-medium text-gray-900">{workflow.name}</h4>
                          <p className="text-sm text-gray-600 mt-1">{workflow.type}</p>
                          <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
                            <div>
                              <span className="font-medium text-gray-700">Priority:</span>
                              <span className={`ml-2 px-2 py-1 rounded-full text-xs ${
                                workflow.priority === 'high' ? 'bg-red-100 text-red-800' :
                                workflow.priority === 'medium' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-green-100 text-green-800'
                              }`}>
                                {workflow.priority}
                              </span>
                            </div>
                            <div>
                              <span className="font-medium text-gray-700">Submitted:</span>
                              <span className="ml-2 text-gray-600">
                                {new Date(workflow.submittedAt).toLocaleString()}
                              </span>
                            </div>
                          </div>

                          {/* Workflow Details */}
                          <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                            <h5 className="font-medium text-gray-700 mb-2">Workflow Details</h5>
                            <div className="text-sm text-gray-600 space-y-1">
                              <p><strong>Type:</strong> {workflow.type}</p>
                              <p><strong>Requester:</strong> {workflow.requester || 'System'}</p>
                              <p><strong>Description:</strong> Convert repository into interactive workshop content</p>
                            </div>
                          </div>
                        </div>

                        {/* Approval Actions */}
                        <div className="ml-6 flex flex-col space-y-2">
                          <button
                            onClick={() => handleApproveWorkflow(workflow.id)}
                            className="flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                          >
                            <ThumbsUp className="h-4 w-4 mr-2" />
                            Approve
                          </button>
                          <button
                            onClick={() => handleRejectWorkflow(workflow.id)}
                            className="flex items-center px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
                          >
                            <ThumbsDown className="h-4 w-4 mr-2" />
                            Reject
                          </button>
                          <button
                            onClick={() => setSelectedWorkflow(workflow)}
                            className="flex items-center px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                          >
                            <FileText className="h-4 w-4 mr-2" />
                            Details
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
              )}
            </div>

            {/* Approval Statistics */}
            <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="text-center p-4 bg-green-50 rounded-lg">
                <div className="text-2xl font-semibold text-green-600">
                  {qualityMetrics?.approvalEfficiency || 88}%
                </div>
                <div className="text-sm text-green-700">Approval Efficiency</div>
              </div>
              <div className="text-center p-4 bg-blue-50 rounded-lg">
                <div className="text-2xl font-semibold text-blue-600">
                  {coordinatorStatus?.data?.activeSessions || 3}
                </div>
                <div className="text-sm text-blue-700">Active Sessions</div>
              </div>
              <div className="text-center p-4 bg-orange-50 rounded-lg">
                <div className="text-2xl font-semibold text-orange-600">
                  {coordinatorStatus?.data?.pendingApprovals || 2}
                </div>
                <div className="text-sm text-orange-700">Pending Approvals</div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default HumanOversightPanel;
