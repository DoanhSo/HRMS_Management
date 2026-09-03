import React, { useState, useEffect } from 'react';
import { Search, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';

interface SearchFilterProps {
  placeholder?: string;
  searchTerm?: string;
  onSearchChange: (value: string) => void;
  children?: React.ReactNode;
  onClearFilters?: () => void;
  hasActiveFilters?: boolean;
}

export const SearchFilter: React.FC<SearchFilterProps> = ({
  placeholder,
  searchTerm = '',
  onSearchChange,
  children,
  onClearFilters,
  hasActiveFilters = false,
}) => {
  const { t } = useTranslation();
  const [internalValue, setInternalValue] = useState(searchTerm);

  useEffect(() => {
    setInternalValue(searchTerm);
  }, [searchTerm]);

  // Debounce search input
  useEffect(() => {
    const handler = setTimeout(() => {
      if (internalValue !== searchTerm) {
        onSearchChange(internalValue);
      }
    }, 350);

    return () => clearTimeout(handler);
  }, [internalValue, onSearchChange, searchTerm]);

  return (
    <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3 mb-5">
      {/* Search Input */}
      <div className="relative flex-1 max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          value={internalValue}
          onChange={(e) => setInternalValue(e.target.value)}
          placeholder={placeholder || t('actions.search')}
          className="w-full pl-9 pr-8 py-2 text-xs bg-white border border-gray-200 rounded-lg placeholder-gray-400 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all shadow-[0_1px_2px_rgba(0,0,0,0.02)]"
        />
        {internalValue && (
          <button
            type="button"
            onClick={() => {
              setInternalValue('');
              onSearchChange('');
            }}
            className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        )}
      </div>

      {/* Filter Slots */}
      <div className="flex flex-wrap items-center gap-2.5">
        {children}

        {hasActiveFilters && onClearFilters && (
          <button
            type="button"
            onClick={onClearFilters}
            className="text-xs text-gray-500 hover:text-red-600 px-2 py-1.5 rounded-lg hover:bg-gray-100 transition-colors flex items-center gap-1"
          >
            <X className="w-3.5 h-3.5" />
            {t('actions.clearFilter')}
          </button>
        )}
      </div>
    </div>
  );
};
