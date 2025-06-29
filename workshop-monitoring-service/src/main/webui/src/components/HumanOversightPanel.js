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
  RefreshCw
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
        setActiveWorkflows(workflowsData.workflows || []);
      }

      // Fetch quality metrics
      const metricsResponse = await fetch('/api/oversight/metrics/quality');
      if (metricsResponse.ok) {
        const metricsData = await metricsResponse.json();
        setQualityMetrics(metricsData);
      }

    } catch (err) {
      console.error('Error fetching oversight data:', err);
      setError('Failed to load oversight data');
    } finally {
      setLoading(false);
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
                coordinatorStatus?.status === 'HEALTHY' ? 'bg-green-500' : 
                coordinatorStatus?.status === 'DEGRADED' ? 'bg-yellow-500' : 'bg-red-500'
              }`}></div>
              <span className="text-sm font-medium text-gray-700">
                {coordinatorStatus?.status || 'UNKNOWN'}
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
                {coordinatorStatus?.activeSessions || 0}
              </p>
              <p className="text-sm text-gray-600">Active Sessions</p>
            </div>

            {/* Pending Approvals */}
            <div className="text-center">
              <div className="flex items-center justify-center w-12 h-12 bg-orange-100 rounded-full mx-auto mb-2">
                <Clock className="h-6 w-6 text-orange-600" />
              </div>
              <p className="text-2xl font-semibold text-gray-900">
                {coordinatorStatus?.pendingApprovals || 0}
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

      {/* Active Workflows */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">Active Workflows</h3>
            <Activity className="h-5 w-5 text-gray-400" />
          </div>
        </div>
        <div className="p-6">
          {activeWorkflows.length === 0 ? (
            <div className="text-center py-8">
              <Activity className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">No active workflows requiring oversight</p>
            </div>
          ) : (
            <div className="space-y-4">
              {activeWorkflows.slice(0, 5).map((workflow) => (
                <div key={workflow.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-medium text-gray-900">{workflow.name}</p>
                    <p className="text-sm text-gray-600">{workflow.type}</p>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      workflow.status === 'pending_approval' ? 'bg-yellow-100 text-yellow-800' :
                      workflow.status === 'in_progress' ? 'bg-blue-100 text-blue-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {workflow.status}
                    </span>
                    <button className="text-blue-600 hover:text-blue-800 text-sm">
                      Review
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Quality Assurance Metrics */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">Quality Assurance</h3>
            <BarChart3 className="h-5 w-5 text-gray-400" />
          </div>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Compliance Score */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">Compliance Score</span>
                <span className="text-sm text-gray-600">
                  {qualityMetrics?.complianceScore || 0}%
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-green-600 h-2 rounded-full" 
                  style={{ width: `${qualityMetrics?.complianceScore || 0}%` }}
                ></div>
              </div>
            </div>

            {/* Approval Efficiency */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700">Approval Efficiency</span>
                <span className="text-sm text-gray-600">
                  {qualityMetrics?.approvalEfficiency || 0}%
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-600 h-2 rounded-full" 
                  style={{ width: `${qualityMetrics?.approvalEfficiency || 0}%` }}
                ></div>
              </div>
            </div>
          </div>

          {/* Recent Quality Events */}
          <div className="mt-6">
            <h4 className="text-sm font-medium text-gray-700 mb-3">Recent Quality Events</h4>
            <div className="space-y-2">
              {qualityMetrics?.recentEvents?.slice(0, 3).map((event, index) => (
                <div key={index} className="flex items-center space-x-3 text-sm">
                  <div className={`w-2 h-2 rounded-full ${
                    event.type === 'compliance_check' ? 'bg-green-500' :
                    event.type === 'approval_timeout' ? 'bg-yellow-500' :
                    'bg-blue-500'
                  }`}></div>
                  <span className="text-gray-600">{event.message}</span>
                  <span className="text-gray-400 ml-auto">{event.timestamp}</span>
                </div>
              )) || (
                <p className="text-gray-500 text-sm">No recent quality events</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Agent Coordination Controls */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">Agent Coordination</h3>
            <Settings className="h-5 w-5 text-gray-400" />
          </div>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button className="flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <Activity className="h-4 w-4 mr-2" />
              Coordinate Workflow
            </button>
            <button className="flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <Shield className="h-4 w-4 mr-2" />
              Quality Check
            </button>
            <button className="flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <TrendingUp className="h-4 w-4 mr-2" />
              Performance Review
            </button>
            <button className="flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors">
              <Eye className="h-4 w-4 mr-2" />
              Monitor Agents
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HumanOversightPanel;
