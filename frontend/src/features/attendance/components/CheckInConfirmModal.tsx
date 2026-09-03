import React, { useState, useEffect } from 'react';
import { Clock, LogIn, AlertCircle, CheckCircle2, X } from 'lucide-react';
import { FormField } from '@/components/shared/FormField';

interface CheckInConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (notes?: string) => Promise<void>;
  isLoading?: boolean;
}

export const CheckInConfirmModal: React.FC<CheckInConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  isLoading,
}) => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [notes, setNotes] = useState('');

  useEffect(() => {
    if (!isOpen) return;
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, [isOpen]);

  if (!isOpen) return null;

  const hours = currentTime.getHours();
  const minutes = currentTime.getMinutes();
  const isLate = hours > 8 || (hours === 8 && minutes > 30);
  const lateMinutes = isLate ? (hours - 8) * 60 + (minutes - 30) : 0;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onConfirm(notes.trim() || undefined);
    setNotes('');
  };

  const formattedTime = currentTime.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });

  const formattedDate = currentTime.toLocaleDateString('vi-VN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-gray-100 space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <LogIn className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900">Xác nhận Chấm công Vào ca</h3>
              <p className="text-xs text-gray-400">Ghi nhận thời gian bắt đầu ngày làm việc</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Live Clock Card */}
        <div className="p-4 bg-gradient-to-br from-emerald-50 to-teal-50/50 border border-emerald-100/80 rounded-2xl text-center space-y-1">
          <span className="text-xs font-semibold text-emerald-800 uppercase tracking-wider">{formattedDate}</span>
          <div className="text-3xl font-black text-gray-900 font-mono tracking-tight py-1">{formattedTime}</div>
          <div className="pt-1">
            {isLate ? (
              <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-700 bg-amber-100/80 px-2.5 py-1 rounded-full border border-amber-200">
                <AlertCircle className="w-3.5 h-3.5" />
                <span>Chấm công muộn {lateMinutes} phút (sau 08:30)</span>
              </span>
            ) : (
              <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-700 bg-emerald-100/80 px-2.5 py-1 rounded-full border border-emerald-200">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Chấm công đúng giờ</span>
              </span>
            )}
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField label="Ghi chú ca làm (Tùy chọn)">
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={isLate ? 'Nhập lý do đi muộn (nếu có)...' : 'Ghi chú công việc hôm nay...'}
              rows={3}
              className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
            />
          </FormField>

          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
            >
              <LogIn className="w-3.5 h-3.5" />
              <span>{isLoading ? 'Đang chấm công...' : 'Xác nhận Vào làm'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
