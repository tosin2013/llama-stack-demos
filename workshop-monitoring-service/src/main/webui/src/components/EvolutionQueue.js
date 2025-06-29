import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { Clock, User, FileText, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';

/**
 * Evolution Queue Component - Displays pending and active workshop evolution requests
 * Follows existing ApprovalQueue.js patterns for consistency
 */
const EvolutionQueue = ({ onEvolutionSelect, refreshTrigger }) => {
  const [evolutions, setEvolutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all'); // all, pending, active, completed

  // Fetch evolution data from monitoring service
  useEffect(() => {
    const fetchEvolutions = async () => {
      try {
        setLoading(true);
        const response = await fetch('/api/evolution/active');
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        setEvolutions(data.active_evolutions || []);
        setError(null);
      } catch (err) {
        console.error('Error fetching evolutions:', err);
        setError('Failed to load evolution queue');
      } finally {
        setLoading(false);
      }
    };

    fetchEvolutions();
    
    // Set up polling for real-time updates
    const interval = setInterval(fetchEvolutions, 30000); // Poll every 30 seconds
    
    return () => clearInterval(interval);
  }, [refreshTrigger]);

  // Filter evolutions based on selected filter
  const filteredEvolutions = evolutions.filter(evolution => {
    if (filter === 'all') return true;
    if (filter === 'pending') return ['requested', 'under_review'].includes(evolution.status);
    if (filter === 'active') return ['approved', 'implementing', 'validating'].includes(evolution.status);
    if (filter === 'completed') return ['completed', 'deployed'].includes(evolution.status);
    return true;
  });

  // Get status badge variant and icon
  const getStatusInfo = (status) => {
    const statusMap = {
      'requested': { variant: 'secondary', icon: FileText, color: 'text-blue-600' },
      'under_review': { variant: 'secondary', icon: User, color: 'text-yellow-600' },
      'approved': { variant: 'success', icon: CheckCircle, color: 'text-green-600' },
      'implementing': { variant: 'warning', icon: Clock, color: 'text-orange-600' },
      'validating': { variant: 'warning', icon: AlertTriangle, color: 'text-yellow-600' },
      'completed': { variant: 'success', icon: CheckCircle, color: 'text-green-600' },
      'deployed': { variant: 'success', icon: CheckCircle, color: 'text-green-600' },
      'failed': { variant: 'destructive', icon: XCircle, color: 'text-red-600' },
      'rolled_back': { variant: 'destructive', icon: XCircle, color: 'text-red-600' }
    };
    
    return statusMap[status] || { variant: 'secondary', icon: FileText, color: 'text-gray-600' };
  };

  // Format evolution type for display
  const formatEvolutionType = (type) => {
    return type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  };

  // Calculate time since creation
  const getTimeAgo = (createdAt) => {
    const now = new Date();
    const created = new Date(createdAt);
    const diffMs = now - created;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffHours / 24);
    
    if (diffDays > 0) return `${diffDays}d ago`;
    if (diffHours > 0) return `${diffHours}h ago`;
    return 'Just now';
  };

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Evolution Queue</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-2">Loading evolution queue...</span>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Evolution Queue</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8">
            <XCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
            <p className="text-red-600 mb-4">{error}</p>
            <Button onClick={() => window.location.reload()}>
              Retry
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Evolution Queue</CardTitle>
          <div className="flex space-x-2">
            {['all', 'pending', 'active', 'completed'].map((filterOption) => (
              <Button
                key={filterOption}
                variant={filter === filterOption ? 'default' : 'outline'}
                size="sm"
                onClick={() => setFilter(filterOption)}
              >
                {filterOption.charAt(0).toUpperCase() + filterOption.slice(1)}
              </Button>
            ))}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {filteredEvolutions.length === 0 ? (
          <div className="text-center py-8">
            <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500">
              {filter === 'all' ? 'No evolutions in queue' : `No ${filter} evolutions`}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredEvolutions.map((evolution) => {
              const statusInfo = getStatusInfo(evolution.status);
              const StatusIcon = statusInfo.icon;
              
              return (
                <div
                  key={evolution.evolution_id}
                  className="border rounded-lg p-4 hover:bg-gray-50 cursor-pointer transition-colors"
                  onClick={() => onEvolutionSelect && onEvolutionSelect(evolution)}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <StatusIcon className={`h-4 w-4 ${statusInfo.color}`} />
                        <h3 className="font-medium text-gray-900">
                          {evolution.workshop_name}
                        </h3>
                        <Badge variant={statusInfo.variant}>
                          {evolution.status.replace(/_/g, ' ')}
                        </Badge>
                      </div>
                      
                      <div className="text-sm text-gray-600 mb-2">
                        <span className="font-medium">Type:</span> {formatEvolutionType(evolution.evolution_type)}
                      </div>
                      
                      {evolution.evolution_description && (
                        <p className="text-sm text-gray-700 mb-2 line-clamp-2">
                          {evolution.evolution_description}
                        </p>
                      )}
                      
                      <div className="flex items-center space-x-4 text-xs text-gray-500">
                        <span>
                          <User className="h-3 w-3 inline mr-1" />
                          {evolution.requested_by || 'System'}
                        </span>
                        <span>
                          <Clock className="h-3 w-3 inline mr-1" />
                          {getTimeAgo(evolution.created_at)}
                        </span>
                        {evolution.approval_id && (
                          <span>
                            ID: {evolution.approval_id.slice(0, 8)}...
                          </span>
                        )}
                      </div>
                    </div>
                    
                    <div className="ml-4 text-right">
                      {evolution.target_version && (
                        <div className="text-xs text-gray-500 mb-1">
                          → {evolution.target_version}
                        </div>
                      )}
                      {evolution.rollback_available && (
                        <Badge variant="outline" className="text-xs">
                          Rollback Available
                        </Badge>
                      )}
                    </div>
                  </div>
                  
                  {/* Progress indicator for active evolutions */}
                  {['implementing', 'validating'].includes(evolution.status) && (
                    <div className="mt-3">
                      <div className="flex items-center justify-between text-xs text-gray-500 mb-1">
                        <span>Progress</span>
                        <span>{evolution.status === 'implementing' ? '60%' : '85%'}</span>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-2">
                        <div 
                          className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                          style={{ width: evolution.status === 'implementing' ? '60%' : '85%' }}
                        ></div>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
        
        {/* Queue summary */}
        <div className="mt-6 pt-4 border-t">
          <div className="flex items-center justify-between text-sm text-gray-500">
            <span>Total: {evolutions.length} evolutions</span>
            <span>Showing: {filteredEvolutions.length} {filter !== 'all' ? filter : ''}</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default EvolutionQueue;
