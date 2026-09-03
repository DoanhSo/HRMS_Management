import React, { useState, useEffect } from 'react';
import { LogOut, AlertTriangle, CheckCircle2, X } from 'lucide-react';
import { FormField } from '@/components/shared/FormField';

interface CheckOutConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (notes?: string) => Promise<void>;
  checkInTime?: string | null;
  isLoading?: boolean;
}

export const CheckOutConfirmModal: React.FC<CheckOutConfirmModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  checkInTime,
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
  const isEarly = hours < 17 || (hours === 17 && minutes < 30);
  const earlyMinutes = isEarly ? (17 - hours) * 60 + (30 - minutes) : 0;

  // Calculate duration if checkInTime available
  let durationStr = 'Chưa xác định';
  if (checkInTime) {
    try {
      const checkInDate = new Date(checkInTime);
      const diffMs = currentTime.getTime() - checkInDate.getTime();
      if (diffMs > 0) {
        const diffHrs = Math.floor(diffMs / (1000 * 60 * 60));
        const diffMins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
        durationStr = `${diffHrs} giờ ${diffMins} phút`;
      }
    } catch {
      // ignore
    }
  }

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

  const formattedCheckIn = checkInTime
    ? new Date(checkInTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : '--:--';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-gray-100 space-y-5">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              <LogOut className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900">Xác nhận Chấm công Ra ca</h3>
              <p className="text-xs text-gray-400">Ghi nhận thời gian kết thúc ngày làm việc</p>
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

        {/* Shift Summary Card */}
        <div className="p-4 bg-gradient-to-br from-indigo-50 to-purple-50/50 border border-indigo-100/80 rounded-2xl space-y-2">
          <div className="grid grid-cols-2 gap-2 text-center pb-2 border-b border-indigo-100">
            <div>
              <span className="text-[11px] text-gray-500 block font-medium">Giờ vào sáng nay</span>
              <span className="text-sm font-bold text-gray-900 font-mono">{formattedCheckIn}</span>
            </div>
            <div>
              <span className="text-[11px] text-gray-500 block font-medium">Giờ ra hiện tại</span>
              <span className="text-sm font-bold text-indigo-700 font-mono">{formattedTime}</span>
            </div>
          </div>

          <div className="text-center pt-1 space-y-1">
            <span className="text-xs text-gray-600 block">
              Tổng thời lượng ca làm: <strong className="text-gray-900">{durationStr}</strong>
            </span>
            <div>
              {isEarly ? (
                <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-700 bg-amber-100/80 px-2.5 py-1 rounded-full border border-amber-200">
                  <AlertTriangle className="w-3.5 h-3.5" />
                  <span>Về sớm {earlyMinutes} phút (trước 17:30)</span>
                </span>
              ) : (
                <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-700 bg-emerald-100/80 px-2.5 py-1 rounded-full border border-emerald-200">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Đã đủ tiêu chuẩn ca làm việc</span>
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField label="Báo cáo / Ghi chú ca làm (Tùy chọn)">
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={isEarly ? 'Nhập lý do về sớm (nếu có)...' : 'Tóm tắt công việc đã hoàn thành hôm nay...'}
              rows={3}
              className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
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
              className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>{isLoading ? 'Đang chấm công...' : 'Xác nhận Kết thúc ca'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
