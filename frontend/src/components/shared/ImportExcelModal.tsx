import React, { useState, useRef } from 'react';
import {
  FileSpreadsheet,
  Download,
  UploadCloud,
  X,
  CheckCircle2,
  AlertCircle,
  FileCheck,
  AlertTriangle,
  RotateCcw,
} from 'lucide-react';
import { toast } from 'sonner';
import { ImportResultResponse } from '@/types';

interface ImportExcelModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  subtitle?: string;
  templateFileName: string;
  onDownloadTemplate: () => Promise<Blob>;
  onImport: (file: File) => Promise<ImportResultResponse>;
  onSuccess?: () => void;
}

export const ImportExcelModal: React.FC<ImportExcelModalProps> = ({
  isOpen,
  onClose,
  title,
  subtitle,
  templateFileName,
  onDownloadTemplate,
  onImport,
  onSuccess,
}) => {
  const [file, setFile] = useState<File | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [result, setResult] = useState<ImportResultResponse | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleDownloadTemplate = async () => {
    setIsDownloading(true);
    try {
      const blob = await onDownloadTemplate();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = templateFileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã tải xuống file mẫu Excel');
    } catch (err) {
      console.error('Download template error:', err);
      toast.error('Không thể tải file mẫu');
    } finally {
      setIsDownloading(false);
    }
  };

  const handleFileDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFile = e.dataTransfer.files[0];
      if (droppedFile.name.endsWith('.xlsx') || droppedFile.name.endsWith('.xls')) {
        setFile(droppedFile);
        setResult(null);
      } else {
        toast.error('Vui lòng chọn định dạng file Excel (.xlsx hoặc .xls)');
      }
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
      setResult(null);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      toast.error('Vui lòng chọn file Excel để tải lên');
      return;
    }

    setIsUploading(true);
    try {
      const res = await onImport(file);
      setResult(res);
      if (res.successCount > 0) {
        toast.success(`Đã nhập thành công ${res.successCount} bản ghi`);
        if (onSuccess) onSuccess();
      }
      if (res.failedCount > 0) {
        toast.warning(`Có ${res.failedCount} bản ghi bị lỗi, vui lòng kiểm tra bảng bên dưới`);
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Không thể nhập file Excel';
      toast.error(msg);
    } finally {
      setIsUploading(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setResult(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl max-w-2xl w-full p-6 shadow-2xl border border-gray-100 space-y-5 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-900">{title}</h3>
              <p className="text-xs text-gray-400">{subtitle || 'Nhập dữ liệu hàng loạt từ file Excel'}</p>
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

        <div className="space-y-4 overflow-y-auto pr-1 flex-1">
          {/* Download Template Banner */}
          <div className="p-3 bg-blue-50/70 border border-blue-100 rounded-xl flex items-center justify-between gap-3">
            <div className="flex items-center gap-2 text-blue-900 text-xs">
              <Download className="w-4 h-4 text-blue-600 shrink-0" />
              <span>Chưa có file mẫu? Tải file mẫu Excel chuẩn để nhập đúng định dạng:</span>
            </div>
            <button
              type="button"
              onClick={handleDownloadTemplate}
              disabled={isDownloading}
              className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shrink-0 transition-colors flex items-center gap-1.5 disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              <span>{isDownloading ? 'Đang tải...' : 'Tải File Mẫu'}</span>
            </button>
          </div>

          {/* Upload Area */}
          {!result && (
            <div
              onDragOver={(e) => e.preventDefault()}
              onDrop={handleFileDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all ${
                file
                  ? 'border-emerald-300 bg-emerald-50/30'
                  : 'border-gray-300 hover:border-blue-400 bg-gray-50/50 hover:bg-blue-50/30'
              }`}
            >
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                accept=".xlsx, .xls"
                className="hidden"
              />

              {file ? (
                <div className="space-y-2">
                  <div className="w-10 h-10 mx-auto bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center">
                    <FileCheck className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-gray-900">{file.name}</p>
                    <p className="text-[11px] text-gray-400">{(file.size / 1024).toFixed(1)} KB</p>
                  </div>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleReset();
                    }}
                    className="text-[11px] text-rose-600 hover:underline inline-flex items-center gap-1"
                  >
                    <RotateCcw className="w-3 h-3" /> Chọn file khác
                  </button>
                </div>
              ) : (
                <div className="space-y-2">
                  <div className="w-10 h-10 mx-auto bg-gray-100 text-gray-400 rounded-full flex items-center justify-center">
                    <UploadCloud className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold text-gray-700">
                      Kéo thả file Excel vào đây hoặc <span className="text-blue-600 underline">duyệt từ máy tính</span>
                    </p>
                    <p className="text-[11px] text-gray-400 mt-0.5">Hỗ trợ định dạng .xlsx, .xls</p>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Results Summary & Error Breakdown */}
          {result && (
            <div className="space-y-3">
              <div className="grid grid-cols-3 gap-3">
                <div className="p-3 bg-gray-50 border border-gray-100 rounded-xl text-center">
                  <span className="text-xs text-gray-500 block">Tổng số dòng</span>
                  <span className="text-base font-bold text-gray-900">{result.totalRows}</span>
                </div>
                <div className="p-3 bg-emerald-50 border border-emerald-100 rounded-xl text-center">
                  <span className="text-xs text-emerald-700 block">Thành công</span>
                  <span className="text-base font-bold text-emerald-600">{result.successCount}</span>
                </div>
                <div className="p-3 bg-rose-50 border border-rose-100 rounded-xl text-center">
                  <span className="text-xs text-rose-700 block">Lỗi không nhập</span>
                  <span className="text-base font-bold text-rose-600">{result.failedCount}</span>
                </div>
              </div>

              {result.errors && result.errors.length > 0 && (
                <div className="border border-rose-200 rounded-xl overflow-hidden">
                  <div className="bg-rose-50 px-3 py-2 border-b border-rose-200 flex items-center gap-1.5 text-xs font-bold text-rose-800">
                    <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                    <span>Chi tiết các dòng bị lỗi ({result.errors.length})</span>
                  </div>
                  <div className="max-h-48 overflow-y-auto">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-gray-50 text-gray-500 border-b border-gray-200 text-[11px]">
                        <tr>
                          <th className="px-3 py-1.5">Dòng</th>
                          <th className="px-3 py-1.5">Mã / Khóa</th>
                          <th className="px-3 py-1.5">Cột</th>
                          <th className="px-3 py-1.5">Nguyên nhân lỗi</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {result.errors.map((err, idx) => (
                          <tr key={idx} className="hover:bg-rose-50/30">
                            <td className="px-3 py-1.5 font-mono text-gray-500 font-bold">#{err.rowNumber}</td>
                            <td className="px-3 py-1.5 font-mono text-blue-600">{err.identifier || '-'}</td>
                            <td className="px-3 py-1.5 text-gray-700">{err.fieldName || '-'}</td>
                            <td className="px-3 py-1.5 text-rose-600">{err.errorMessage}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="pt-3 border-t border-gray-100 flex items-center justify-between">
          <div>
            {result && (
              <button
                type="button"
                onClick={handleReset}
                className="text-xs text-blue-600 hover:underline flex items-center gap-1 font-medium"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                <span>Nhập file khác</span>
              </button>
            )}
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              {result ? 'Đóng' : 'Hủy'}
            </button>
            {!result && (
              <button
                type="button"
                onClick={handleUpload}
                disabled={!file || isUploading}
                className="px-4 py-2 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
              >
                <UploadCloud className="w-3.5 h-3.5" />
                <span>{isUploading ? 'Đang xử lý...' : 'Tiến hành Nhập'}</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
