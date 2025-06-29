import React from 'react';

/**
 * Button UI Component
 * Provides consistent button styling following ADR-0004 design patterns
 */

const buttonVariants = {
  default: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
  secondary: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
  outline: 'bg-transparent text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-gray-500',
  ghost: 'bg-transparent text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
  success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
  warning: 'bg-yellow-600 text-white hover:bg-yellow-700 focus:ring-yellow-500',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500'
};

const buttonSizes = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-4 py-2 text-sm',
  lg: 'px-6 py-3 text-base'
};

/**
 * Button Component
 */
export const Button = ({ 
  children, 
  variant = 'default', 
  size = 'md',
  disabled = false,
  loading = false,
  className = "", 
  ...props 
}) => {
  const variantClasses = buttonVariants[variant] || buttonVariants.default;
  const sizeClasses = buttonSizes[size] || buttonSizes.md;

  const baseClasses = 'inline-flex items-center justify-center rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2';
  const disabledClasses = disabled || loading ? 'opacity-50 cursor-not-allowed' : '';

  return (
    <button 
      className={`${baseClasses} ${variantClasses} ${sizeClasses} ${disabledClasses} ${className}`}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <svg 
          className="animate-spin -ml-1 mr-2 h-4 w-4" 
          xmlns="http://www.w3.org/2000/svg" 
          fill="none" 
          viewBox="0 0 24 24"
        >
          <circle 
            className="opacity-25" 
            cx="12" 
            cy="12" 
            r="10" 
            stroke="currentColor" 
            strokeWidth="4"
          ></circle>
          <path 
            className="opacity-75" 
            fill="currentColor" 
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          ></path>
        </svg>
      )}
      {children}
    </button>
  );
};

/**
 * Icon Button - Button with icon only
 */
export const IconButton = ({ 
  icon: Icon, 
  variant = 'ghost', 
  size = 'md',
  className = "",
  ...props 
}) => {
  const iconSizes = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
    lg: 'h-6 w-6'
  };

  return (
    <Button 
      variant={variant} 
      size={size}
      className={`p-2 ${className}`}
      {...props}
    >
      <Icon className={iconSizes[size]} />
    </Button>
  );
};

/**
 * Button Group - Group of related buttons
 */
export const ButtonGroup = ({ children, className = "", ...props }) => {
  return (
    <div 
      className={`inline-flex rounded-lg shadow-sm ${className}`}
      role="group"
      {...props}
    >
      {React.Children.map(children, (child, index) => {
        if (React.isValidElement(child)) {
          const isFirst = index === 0;
          const isLast = index === React.Children.count(children) - 1;
          
          return React.cloneElement(child, {
            className: `${child.props.className || ''} ${
              isFirst ? 'rounded-r-none' : isLast ? 'rounded-l-none' : 'rounded-none'
            } ${!isFirst ? '-ml-px' : ''}`.trim()
          });
        }
        return child;
      })}
    </div>
  );
};

export default Button;
