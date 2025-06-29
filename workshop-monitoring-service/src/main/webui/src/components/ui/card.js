import React from 'react';

/**
 * Card UI Components
 * Provides consistent card-based layout following ADR-0004 design patterns
 */

/**
 * Main Card Container
 */
export const Card = ({ children, className = "", ...props }) => {
  return (
    <div 
      className={`bg-white rounded-lg shadow border border-gray-200 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

/**
 * Card Header Section
 */
export const CardHeader = ({ children, className = "", ...props }) => {
  return (
    <div 
      className={`p-6 border-b border-gray-200 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

/**
 * Card Title
 */
export const CardTitle = ({ children, className = "", ...props }) => {
  return (
    <h3 
      className={`text-lg font-medium text-gray-900 ${className}`}
      {...props}
    >
      {children}
    </h3>
  );
};

/**
 * Card Content Section
 */
export const CardContent = ({ children, className = "", ...props }) => {
  return (
    <div 
      className={`p-6 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

/**
 * Card Footer Section
 */
export const CardFooter = ({ children, className = "", ...props }) => {
  return (
    <div 
      className={`p-6 border-t border-gray-200 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

/**
 * Card Description
 */
export const CardDescription = ({ children, className = "", ...props }) => {
  return (
    <p 
      className={`text-sm text-gray-600 mt-1 ${className}`}
      {...props}
    >
      {children}
    </p>
  );
};

export default Card;
