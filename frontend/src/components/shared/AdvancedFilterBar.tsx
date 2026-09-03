import React, { useState, useEffect } from 'react';
import { Search, X, Filter, RotateCcw } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface AdvancedFilterBarProps {
  placeholder?: string;
  searchTerm?: string;
  onSearchChange: (value: string) => void;
  children?: React.ReactNode;
  onResetFilters?: () => void;
  activeFilterCount?: number;
  showExpandToggle?: boolean;
}

export const AdvancedFilterBar: React.FC<AdvancedFilterBarProps> = ({
  placeholder,
  searchTerm = '',
  onSearchChange,
  children,
  onResetFilters,
  activeFilterCount = 0,
}) => {
  const { t } = useTranslation();
  const [internalValue, setInternalValue] = useState(searchTerm);

  useEffect(() => {
    setInternalValue(searchTerm);
  }, [searchTerm]);

  // Debounce search input (350ms)
  useEffect(() => {
    const handler = setTimeout(() => {
      if (internalValue !== searchTerm) {
        onSearchChange(internalValue);
      }
    }, 350);

    return () => clearTimeout(handler);
  }, [internalValue, onSearchChange, searchTerm]);

  return (
    <div className="bg-white border border-gray-100 rounded-xl p-4 mb-6 shadow-sm">
      <div className="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4">
        {/* Search Bar */}
        <div className="relative flex-1 min-w-[280px]">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={internalValue}
            onChange={(e) => setInternalValue(e.target.value)}
            placeholder={placeholder || t('actions.search')}
            className="w-full pl-10 pr-9 py-2 text-xs bg-gray-50/50 hover:bg-white focus:bg-white border border-gray-200 rounded-lg placeholder-gray-400 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
          />
          {internalValue && (
            <button
              type="button"
              onClick={() => {
                setInternalValue('');
                onSearchChange('');
              }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-0.5 rounded-full hover:bg-gray-200/60 transition-colors"
              title="Xóa tìm kiếm"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Filter Controls & Reset */}
        <div className="flex flex-wrap items-center gap-2.5">
          {children}

          {/* Active Filter Badge & Reset Button */}
          {activeFilterCount > 0 && onResetFilters && (
            <button
              type="button"
              onClick={onResetFilters}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-rose-600 bg-rose-50 hover:bg-rose-100/80 border border-rose-200/60 rounded-lg transition-all"
              title="Xóa toàn bộ bộ lọc"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Xóa lọc ({activeFilterCount})</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
