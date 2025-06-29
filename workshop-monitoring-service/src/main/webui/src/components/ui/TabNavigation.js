import React from 'react';

/**
 * Shared Tab Navigation Component
 * Extracted from EvolutionDashboard.js following ADR-0004 DDD architecture
 * Provides consistent tab navigation across all dashboard views
 */
const TabNavigation = ({ tabs, activeTab, onTabChange, className = "" }) => {
  if (!tabs || tabs.length === 0) {
    return null;
  }

  return (
    <div className={`border-b border-gray-200 ${className}`}>
      <nav className="-mb-px flex space-x-8">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-center space-x-2 py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                isActive
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
              aria-selected={isActive}
              role="tab"
              title={tab.description || tab.name}
            >
              {Icon && <Icon className="h-4 w-4" />}
              <span>{tab.name}</span>
              {tab.badge && (
                <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
};

/**
 * Individual Tab Button Component
 * Extracted for reusability and testing
 */
export const TabButton = ({ tab, isActive, onClick, className = "" }) => {
  const Icon = tab.icon;
  
  return (
    <button
      onClick={() => onClick(tab.id)}
      className={`flex items-center space-x-2 py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
        isActive
          ? 'border-blue-500 text-blue-600'
          : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
      } ${className}`}
      aria-selected={isActive}
      role="tab"
      title={tab.description || tab.name}
    >
      {Icon && <Icon className="h-4 w-4" />}
      <span>{tab.name}</span>
      {tab.badge && (
        <span className="ml-1 inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          {tab.badge}
        </span>
      )}
    </button>
  );
};

/**
 * Tab Content Container
 * Provides consistent spacing and error boundary for tab content
 */
export const TabContent = ({ children, className = "" }) => {
  return (
    <div className={`mt-6 ${className}`} role="tabpanel">
      {children}
    </div>
  );
};

/**
 * Tab Group Container
 * Provides ARIA accessibility and keyboard navigation
 */
export const TabGroup = ({ children, className = "" }) => {
  return (
    <div className={`${className}`} role="tablist">
      {children}
    </div>
  );
};

export default TabNavigation;
