import React, { useState, useEffect } from 'react';
import { 
  Clock, 
  User, 
  AlertTriangle, 
  CheckCircle, 
  XCircle, 
  ArrowUp,
  Eye,
  MessageSquare,
  Calendar,
  Filter
} from 'lucide-react';

/**
 * Approval Queue Component
 * Displays pending approval requests for human reviewers
 * Implements ADR-0002: Human-in-the-Loop Agent Integration
 */
const ApprovalQueue = ({ onApprovalSelect, refreshTrigger }) => {
  const [approvals, setApprovals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState({
    type: 'all',
    priority: 'all',
    status: 'pending'
  });

  // Fetch pending approvals
  const fetchApprovals = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams();
      if (filter.type !== 'all') params.append('type', filter.type);
      if (filter.priority !== 'all') params.append('priority', filter.priority);

      const response = await fetch(`/api/approvals/pending?${params}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch approvals: ${response.status}`);
      }

      const data = await response.json();
      setApprovals(data.pending_approvals || []);
    } catch (err) {
      console.error('Error fetching approvals:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApprovals();
  }, [filter, refreshTrigger]);

  // Auto-refresh every 30 seconds
  useEffect(() => {
    const interval = setInterval(fetchApprovals, 30000);
    return () => clearInterval(interval);
  }, [filter]);

  const getStatusIcon = (status) => {
    switch (status) {
      case 'pending':
        return <Clock className="w-4 h-4 text-yellow-500" />;
      case 'in_review':
        return <Eye className="w-4 h-4 text-blue-500" />;
      case 'escalated':
        return <ArrowUp className="w-4 h-4 text-red-500" />;
      default:
        return <Clock className="w-4 h-4 text-gray-500" />;
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'urgent':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'high':
        return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'normal':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'low':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getTypeIcon = (type) => {
    switch (type) {
      case 'classification':
        return '🔍';
      case 'content_review':
        return '📝';
      case 'deployment_authorization':
        return '🚀';
      case 'conflict_resolution':
        return '⚠️';
      default:
        return '📋';
    }
  };

  const formatTimeAgo = (timestamp) => {
    const now = new Date();
    const time = new Date(timestamp);
    const diffMinutes = Math.floor((now - time) / (1000 * 60));
    
    if (diffMinutes < 1) return 'Just now';
    if (diffMinutes < 60) return `${diffMinutes}m ago`;
    if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}h ago`;
    return `${Math.floor(diffMinutes / 1440)}d ago`;
  };

  const isOverdue = (approval) => {
    if (!approval.timeout_at) return false;
    return new Date() > new Date(approval.timeout_at);
  };

  const needsEscalation = (approval) => {
    if (!approval.escalation_at) return false;
    return new Date() > new Date(approval.escalation_at) && approval.status !== 'escalated';
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center h-32">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-2 text-gray-600">Loading approvals...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center h-32 text-red-600">
          <AlertTriangle className="w-6 h-6 mr-2" />
          <span>Error loading approvals: {error}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <MessageSquare className="w-5 h-5 text-blue-600 mr-2" />
            <h3 className="text-lg font-semibold text-gray-900">Approval Queue</h3>
            <span className="ml-2 px-2 py-1 bg-blue-100 text-blue-800 text-sm rounded-full">
              {approvals.length}
            </span>
          </div>
          
          {/* Filter Controls */}
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-gray-500" />
            <select
              value={filter.type}
              onChange={(e) => setFilter({...filter, type: e.target.value})}
              className="text-sm border border-gray-300 rounded px-2 py-1"
            >
              <option value="all">All Types</option>
              <option value="classification">Classification</option>
              <option value="content_review">Content Review</option>
              <option value="deployment_authorization">Deployment</option>
              <option value="conflict_resolution">Conflicts</option>
            </select>
            
            <select
              value={filter.priority}
              onChange={(e) => setFilter({...filter, priority: e.target.value})}
              className="text-sm border border-gray-300 rounded px-2 py-1"
            >
              <option value="all">All Priorities</option>
              <option value="urgent">Urgent</option>
              <option value="high">High</option>
              <option value="normal">Normal</option>
              <option value="low">Low</option>
            </select>
          </div>
        </div>
      </div>

      {/* Approval List */}
      <div className="divide-y divide-gray-200 max-h-96 overflow-y-auto">
        {approvals.length === 0 ? (
          <div className="px-6 py-8 text-center text-gray-500">
            <CheckCircle className="w-12 h-12 mx-auto mb-3 text-green-500" />
            <p className="text-lg font-medium">No pending approvals</p>
            <p className="text-sm">All approval requests have been processed</p>
          </div>
        ) : (
          approvals.map((approval) => (
            <div
              key={approval.approval_id}
              className={`px-6 py-4 hover:bg-gray-50 cursor-pointer transition-colors ${
                isOverdue(approval) ? 'bg-red-50 border-l-4 border-red-500' : ''
              } ${
                needsEscalation(approval) ? 'bg-orange-50 border-l-4 border-orange-500' : ''
              }`}
              onClick={() => onApprovalSelect && onApprovalSelect(approval)}
            >
              <div className="flex items-start justify-between">
                <div className="flex-1 min-w-0">
                  {/* Header Row */}
                  <div className="flex items-center mb-2">
                    <span className="text-lg mr-2">{getTypeIcon(approval.type)}</span>
                    <h4 className="text-sm font-medium text-gray-900 truncate">
                      {approval.name || approval.type.replace('_', ' ').toUpperCase()}
                    </h4>
                    <span className={`ml-2 px-2 py-1 text-xs rounded-full border ${getPriorityColor(approval.priority)}`}>
                      {approval.priority}
                    </span>
                  </div>

                  {/* Details Row */}
                  <div className="flex items-center text-sm text-gray-600 mb-2">
                    <User className="w-3 h-3 mr-1" />
                    <span className="mr-4">By {approval.requester}</span>
                    <Calendar className="w-3 h-3 mr-1" />
                    <span>{formatTimeAgo(approval.created_at)}</span>
                  </div>

                  {/* Context */}
                  {approval.context && (
                    <p className="text-sm text-gray-700 truncate">
                      {approval.context}
                    </p>
                  )}

                  {/* Warning Messages */}
                  {isOverdue(approval) && (
                    <div className="flex items-center mt-2 text-red-600 text-sm">
                      <AlertTriangle className="w-4 h-4 mr-1" />
                      <span>Overdue - Timeout exceeded</span>
                    </div>
                  )}
                  
                  {needsEscalation(approval) && (
                    <div className="flex items-center mt-2 text-orange-600 text-sm">
                      <ArrowUp className="w-4 h-4 mr-1" />
                      <span>Needs escalation</span>
                    </div>
                  )}
                </div>

                {/* Status and Actions */}
                <div className="flex items-center ml-4">
                  <div className="flex items-center">
                    {getStatusIcon(approval.status)}
                    <span className="ml-1 text-sm text-gray-600 capitalize">
                      {approval.status.replace('_', ' ')}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Footer */}
      {approvals.length > 0 && (
        <div className="px-6 py-3 bg-gray-50 border-t border-gray-200">
          <div className="flex items-center justify-between text-sm text-gray-600">
            <span>{approvals.length} pending approval{approvals.length !== 1 ? 's' : ''}</span>
            <span>Last updated: {new Date().toLocaleTimeString()}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ApprovalQueue;
