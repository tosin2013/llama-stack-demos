import React from 'react';

/**
 * Badge UI Component
 * Provides consistent badge styling following ADR-0004 design patterns
 */

const badgeVariants = {
  default: 'bg-blue-100 text-blue-800 border-blue-200',
  secondary: 'bg-gray-100 text-gray-800 border-gray-200',
  success: 'bg-green-100 text-green-800 border-green-200',
  warning: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  danger: 'bg-red-100 text-red-800 border-red-200',
  outline: 'bg-transparent text-gray-700 border-gray-300'
};

const badgeSizes = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-2.5 py-0.5 text-sm',
  lg: 'px-3 py-1 text-sm'
};

/**
 * Badge Component
 */
export const Badge = ({ 
  children, 
  variant = 'default', 
  size = 'md',
  className = "", 
  ...props 
}) => {
  const variantClasses = badgeVariants[variant] || badgeVariants.default;
  const sizeClasses = badgeSizes[size] || badgeSizes.md;

  return (
    <span 
      className={`inline-flex items-center rounded-full border font-medium ${variantClasses} ${sizeClasses} ${className}`}
      {...props}
    >
      {children}
    </span>
  );
};

/**
 * Status Badge - Specialized badge for status indicators
 */
export const StatusBadge = ({ status, className = "", ...props }) => {
  const getStatusVariant = (status) => {
    switch (status?.toLowerCase()) {
      case 'healthy':
      case 'completed':
      case 'success':
      case 'approved':
        return 'success';
      case 'degraded':
      case 'warning':
      case 'pending':
        return 'warning';
      case 'unhealthy':
      case 'failed':
      case 'error':
      case 'rejected':
        return 'danger';
      case 'unknown':
      case 'inactive':
        return 'secondary';
      default:
        return 'default';
    }
  };

  return (
    <Badge 
      variant={getStatusVariant(status)} 
      className={className}
      {...props}
    >
      {status}
    </Badge>
  );
};

/**
 * Count Badge - Badge with numeric count
 */
export const CountBadge = ({ count, maxCount = 99, className = "", ...props }) => {
  const displayCount = count > maxCount ? `${maxCount}+` : count;
  
  return (
    <Badge 
      variant="default" 
      size="sm"
      className={`${className}`}
      {...props}
    >
      {displayCount}
    </Badge>
  );
};

export default Badge;
