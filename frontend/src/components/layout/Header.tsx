import React from 'react';
import { Menu } from 'lucide-react';
import { NotificationCenter } from '@/components/shared/NotificationCenter';

interface HeaderProps {
  onMenuClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ onMenuClick }) => {
  return (
    <header className="h-16 px-4 sm:px-8 bg-white border-b border-gray-200/80 flex items-center justify-between sticky top-0 z-30 shadow-[0_1px_2px_rgba(0,0,0,0.02)]">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuClick}
          className="p-2 text-gray-500 hover:text-gray-900 rounded-lg hover:bg-gray-100 lg:hidden"
        >
          <Menu className="w-5 h-5" />
        </button>
      </div>

      <div className="flex items-center gap-3">
        {/* Realtime Notification Center */}
        <NotificationCenter />
      </div>
    </header>
  );
};
