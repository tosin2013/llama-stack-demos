import React from 'react';
import { AlertTriangle, RefreshCw, Home, Bug } from 'lucide-react';

/**
 * Domain Error Boundary Component
 * Implements ADR-0004 error handling strategy with domain-specific error reporting
 */
class DomainErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { 
      hasError: false, 
      error: null, 
      errorInfo: null,
      retryCount: 0
    };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo });
    
    // Log error to monitoring service following ADR-0004 patterns
    this.reportError(error, errorInfo);
  }

  reportError = (error, errorInfo) => {
    const errorReport = {
      error: {
        message: error.message,
        stack: error.stack,
        name: error.name
      },
      errorInfo,
      domain: this.props.domain || 'Unknown',
      component: this.props.componentName || 'Unknown',
      userId: this.props.userId || 'anonymous',
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      retryCount: this.state.retryCount
    };

    // Send to monitoring endpoint
    fetch('/api/monitoring/errors', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(errorReport)
    }).catch(err => {
      console.error('Failed to report error to monitoring service:', err);
    });

    // Log to console for development
    console.error('Domain Error Boundary caught an error:', error, errorInfo);
  };

  handleRetry = () => {
    this.setState(prevState => ({
      hasError: false,
      error: null,
      errorInfo: null,
      retryCount: prevState.retryCount + 1
    }));
  };

  handleGoHome = () => {
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      const { domain, fallback: CustomFallback } = this.props;
      const { error, retryCount } = this.state;

      // Use custom fallback if provided
      if (CustomFallback) {
        return (
          <CustomFallback
            error={error}
            domain={domain}
            onRetry={this.handleRetry}
            retryCount={retryCount}
          />
        );
      }

      // Default error fallback UI
      return (
        <ErrorFallback
          error={error}
          domain={domain}
          onRetry={this.handleRetry}
          onGoHome={this.handleGoHome}
          retryCount={retryCount}
        />
      );
    }

    return this.props.children;
  }
}

/**
 * Default Error Fallback Component
 * Provides user-friendly error display with recovery options
 */
const ErrorFallback = ({ error, domain, onRetry, onGoHome, retryCount }) => {
  const isDevelopment = process.env.NODE_ENV === 'development';
  const maxRetries = 3;
  const canRetry = retryCount < maxRetries;

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-6">
        {/* Error Icon */}
        <div className="flex items-center justify-center w-12 h-12 mx-auto bg-red-100 rounded-full mb-4">
          <AlertTriangle className="h-6 w-6 text-red-600" />
        </div>

        {/* Error Title */}
        <h2 className="text-xl font-semibold text-gray-900 text-center mb-2">
          Something went wrong
        </h2>

        {/* Domain Context */}
        {domain && (
          <p className="text-sm text-gray-600 text-center mb-4">
            Error in <span className="font-medium">{domain}</span> domain
          </p>
        )}

        {/* Error Message */}
        <div className="bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
          <p className="text-sm text-red-700">
            {error?.message || 'An unexpected error occurred'}
          </p>
        </div>

        {/* Development Error Details */}
        {isDevelopment && error?.stack && (
          <details className="mb-4">
            <summary className="text-sm text-gray-600 cursor-pointer hover:text-gray-800">
              <Bug className="inline h-4 w-4 mr-1" />
              Technical Details
            </summary>
            <pre className="mt-2 text-xs text-gray-600 bg-gray-100 p-2 rounded overflow-auto max-h-32">
              {error.stack}
            </pre>
          </details>
        )}

        {/* Retry Information */}
        {retryCount > 0 && (
          <p className="text-xs text-gray-500 text-center mb-4">
            Retry attempt: {retryCount}/{maxRetries}
          </p>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-3">
          {canRetry && (
            <button
              onClick={onRetry}
              className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center"
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Try Again
            </button>
          )}
          
          <button
            onClick={onGoHome}
            className="flex-1 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors flex items-center justify-center"
          >
            <Home className="h-4 w-4 mr-2" />
            Go Home
          </button>
        </div>

        {/* Support Information */}
        <p className="text-xs text-gray-500 text-center mt-4">
          If this problem persists, please contact support with the error details above.
        </p>
      </div>
    </div>
  );
};

/**
 * Simple Error Fallback for smaller components
 */
export const SimpleErrorFallback = ({ error, onRetry, className = "" }) => {
  return (
    <div className={`bg-red-50 border border-red-200 rounded-lg p-4 ${className}`}>
      <div className="flex items-center">
        <AlertTriangle className="h-5 w-5 text-red-500 mr-2" />
        <div className="flex-1">
          <p className="text-sm text-red-700">
            {error?.message || 'Something went wrong'}
          </p>
        </div>
        {onRetry && (
          <button
            onClick={onRetry}
            className="ml-2 text-red-600 hover:text-red-800 transition-colors"
            title="Retry"
          >
            <RefreshCw className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default DomainErrorBoundary;
