import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Plus,
  Edit2,
  Trash2,
  KeyRound,
  Shield,
  ShieldAlert,
  UserCheck,
  UserX,
  X,
  Save,
  Lock,
  Unlock,
  Sparkles,
} from 'lucide-react';
import { toast } from 'sonner';
import { userApi } from '@/api/user.api';
import { employeeApi } from '@/api/employee.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { FormField } from '@/components/shared/FormField';
import { formatDate } from '@/lib/utils';
import { EmployeeResponse, RoleResponse, UserManagementResponse } from '@/types';

export const UserListPage: React.FC = () => {
  const { t } = useTranslation();

  const [users, setUsers] = useState<UserManagementResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Available options
  const [availableRoles, setAvailableRoles] = useState<RoleResponse[]>([]);
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [selectedRole, setSelectedRole] = useState<string | undefined>();
  const [selectedStatus, setSelectedStatus] = useState<boolean | undefined>();

  // Modal: Create User
  const [isOpenCreateModal, setIsOpenCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    username: '',
    email: '',
    password: '',
    roles: ['EMPLOYEE'] as string[],
    employeeId: undefined as number | undefined,
  });
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [isCreating, setIsCreating] = useState(false);

  // Modal: Edit User
  const [isOpenEditModal, setIsOpenEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState<UserManagementResponse | null>(null);
  const [editForm, setEditForm] = useState({
    email: '',
    roles: [] as string[],
    enabled: true,
    accountNonLocked: true,
    employeeId: undefined as number | undefined,
  });
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});
  const [isUpdating, setIsUpdating] = useState(false);

  // Modal: Reset Password
  const [isOpenResetModal, setIsOpenResetModal] = useState(false);
  const [resetUser, setResetUser] = useState<UserManagementResponse | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [resetError, setResetError] = useState('');
  const [isResetting, setIsResetting] = useState(false);

  // Confirm Delete
  const [deletingUser, setDeletingUser] = useState<UserManagementResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Action status loading per row
  const [actionId, setActionId] = useState<number | null>(null);

  useEffect(() => {
    userApi.getAllRoles().then(setAvailableRoles).catch(console.warn);
    employeeApi.search({ page: 0, size: 100 }).then((res) => setEmployees(res.content)).catch(console.warn);
  }, []);

  const loadUsers = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await userApi.searchUsers({
        page: currentPage,
        size: pageSize,
        keyword: keyword || undefined,
        role: selectedRole,
        enabled: selectedStatus,
      });
      setUsers(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load users:', err);
      toast.error('Không thể tải danh sách người dùng');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, keyword, selectedRole, selectedStatus]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // Create handler
  const handleOpenCreate = () => {
    setCreateForm({
      username: '',
      email: '',
      password: '',
      roles: ['EMPLOYEE'],
      employeeId: undefined,
    });
    setCreateErrors({});
    setIsOpenCreateModal(true);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (!createForm.username.trim()) errs.username = 'Vui lòng nhập tên đăng nhập';
    else if (createForm.username.trim().length < 3) errs.username = 'Tên đăng nhập phải có ít nhất 3 ký tự';

    if (!createForm.email.trim()) errs.email = 'Vui lòng nhập email';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(createForm.email.trim())) errs.email = 'Email không hợp lệ';

    if (!createForm.password) errs.password = 'Vui lòng nhập mật khẩu khởi tạo';
    else if (createForm.password.length < 6) errs.password = 'Mật khẩu phải từ 6 ký tự trở lên';

    if (!createForm.roles || createForm.roles.length === 0) errs.roles = 'Vui lòng chọn ít nhất một vai trò';

    setCreateErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setIsCreating(true);
    try {
      await userApi.createUser({
        username: createForm.username.trim(),
        email: createForm.email.trim(),
        password: createForm.password,
        roles: createForm.roles,
        employeeId: createForm.employeeId,
      });
      toast.success('Tạo tài khoản người dùng thành công');
      setIsOpenCreateModal(false);
      loadUsers();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Không thể tạo tài khoản người dùng';
      toast.error(msg);
    } finally {
      setIsCreating(false);
    }
  };

  // Edit handler
  const handleOpenEdit = (user: UserManagementResponse) => {
    setEditingUser(user);
    setEditForm({
      email: user.email,
      roles: user.roles || [],
      enabled: user.enabled,
      accountNonLocked: user.accountNonLocked,
      employeeId: user.employeeId || undefined,
    });
    setEditErrors({});
    setIsOpenEditModal(true);
  };

  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;

    const errs: Record<string, string> = {};
    if (!editForm.email.trim()) errs.email = 'Vui lòng nhập email';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email.trim())) errs.email = 'Email không hợp lệ';

    if (!editForm.roles || editForm.roles.length === 0) errs.roles = 'Vui lòng chọn ít nhất một vai trò';

    setEditErrors(errs);
    if (Object.keys(errs).length > 0) return;

    setIsUpdating(true);
    try {
      await userApi.updateUser(editingUser.id, {
        email: editForm.email.trim(),
        roles: editForm.roles,
        enabled: editForm.enabled,
        accountNonLocked: editForm.accountNonLocked,
        employeeId: editForm.employeeId,
      });
      toast.success('Cập nhật tài khoản thành công');
      setIsOpenEditModal(false);
      loadUsers();
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Không thể cập nhật tài khoản';
      toast.error(msg);
    } finally {
      setIsUpdating(false);
    }
  };

  // Reset password handler
  const handleOpenReset = (user: UserManagementResponse) => {
    setResetUser(user);
    setNewPassword('Admin@123');
    setResetError('');
    setIsOpenResetModal(true);
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetUser) return;

    if (!newPassword || newPassword.length < 6) {
      setResetError('Mật khẩu mới phải từ 6 ký tự trở lên');
      return;
    }

    setIsResetting(true);
    try {
      await userApi.resetPassword(resetUser.id, { newPassword });
      toast.success(`Đã đặt lại mật khẩu cho tài khoản "${resetUser.username}"`);
      setIsOpenResetModal(false);
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể đặt lại mật khẩu');
    } finally {
      setIsResetting(false);
    }
  };

  // Toggle status
  const handleToggleStatus = async (user: UserManagementResponse) => {
    setActionId(user.id);
    try {
      await userApi.toggleUserStatus(user.id, !user.enabled);
      toast.success(`Đã ${!user.enabled ? 'kích hoạt' : 'tạm dừng'} tài khoản ${user.username}`);
      loadUsers();
    } catch (err: any) {
      toast.error('Không thể thay đổi trạng thái tài khoản');
    } finally {
      setActionId(null);
    }
  };

  // Delete handler
  const handleDeleteConfirm = async () => {
    if (!deletingUser) return;
    setIsDeleting(true);
    try {
      await userApi.deleteUser(deletingUser.id);
      toast.success(`Đã xóa tài khoản "${deletingUser.username}"`);
      setDeletingUser(null);
      loadUsers();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể xóa tài khoản');
    } finally {
      setIsDeleting(false);
    }
  };

  const getRoleBadge = (roleName: string) => {
    switch (roleName) {
      case 'ADMIN':
      case 'ROLE_ADMIN':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'HR':
      case 'ROLE_HR':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'MANAGER':
      case 'ROLE_MANAGER':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const columns: ColumnDef<UserManagementResponse>[] = [
    {
      key: 'username',
      header: 'Tên đăng nhập',
      render: (row) => (
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-full bg-blue-600/10 text-blue-600 font-bold text-xs flex items-center justify-center shrink-0">
            {row.username ? row.username[0].toUpperCase() : 'U'}
          </div>
          <div>
            <span className="font-semibold text-gray-900 block text-xs">{row.username}</span>
            <span className="text-[11px] text-gray-400 font-mono">{row.email}</span>
          </div>
        </div>
      ),
    },
    {
      key: 'employee',
      header: 'Nhân viên liên kết',
      render: (row) =>
        row.employeeCode ? (
          <div>
            <span className="text-xs font-medium text-gray-900 block">{row.employeeName}</span>
            <span className="text-[11px] text-blue-600 font-mono bg-blue-50 px-1.5 py-0.5 rounded">
              {row.employeeCode}
            </span>
          </div>
        ) : (
          <span className="text-xs text-gray-400 italic">Chưa liên kết NV</span>
        ),
    },
    {
      key: 'roles',
      header: 'Vai trò & Quyền hạn',
      render: (row) => (
        <div className="flex flex-wrap gap-1">
          {row.roles && row.roles.length > 0 ? (
            row.roles.map((r) => (
              <span
                key={r}
                className={`text-[11px] font-semibold px-2 py-0.5 rounded-md border ${getRoleBadge(r)}`}
              >
                {r.replace('ROLE_', '')}
              </span>
            ))
          ) : (
            <span className="text-xs text-gray-400">-</span>
          )}
        </div>
      ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (row) => (
        <div className="space-y-0.5">
          <span
            className={`inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-0.5 rounded-full ${
              row.enabled ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-500'
            }`}
          >
            <span className={`w-1.5 h-1.5 rounded-full ${row.enabled ? 'bg-emerald-500' : 'bg-gray-400'}`} />
            {row.enabled ? 'Hoạt động' : 'Tạm dừng'}
          </span>
          {!row.accountNonLocked && (
            <span className="block text-[10px] text-rose-600 font-medium">Bị khóa (Locked)</span>
          )}
        </div>
      ),
    },
    {
      key: 'createdAt',
      header: 'Ngày tạo',
      render: (row) => <span className="text-xs text-gray-500">{formatDate(row.createdAt)}</span>,
    },
    {
      key: 'actions',
      header: '',
      headerClassName: 'text-right',
      className: 'text-right',
      render: (row) => (
        <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
          {/* Edit */}
          <button
            type="button"
            title="Chỉnh sửa tài khoản"
            onClick={() => handleOpenEdit(row)}
            className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>

          {/* Reset Password */}
          <button
            type="button"
            title="Đặt lại mật khẩu"
            onClick={() => handleOpenReset(row)}
            className="p-1.5 text-gray-500 hover:text-amber-600 hover:bg-amber-50 rounded-md transition-colors"
          >
            <KeyRound className="w-3.5 h-3.5" />
          </button>

          {/* Toggle Lock / Status */}
          <button
            type="button"
            title={row.enabled ? 'Tạm dừng tài khoản' : 'Kích hoạt tài khoản'}
            disabled={actionId === row.id}
            onClick={() => handleToggleStatus(row)}
            className={`p-1.5 rounded-md transition-colors ${
              row.enabled
                ? 'text-gray-500 hover:text-rose-600 hover:bg-rose-50'
                : 'text-gray-500 hover:text-emerald-600 hover:bg-emerald-50'
            }`}
          >
            {row.enabled ? <UserX className="w-3.5 h-3.5" /> : <UserCheck className="w-3.5 h-3.5" />}
          </button>

          {/* Delete */}
          {row.username !== 'admin' && (
            <button
              type="button"
              title="Xóa tài khoản"
              onClick={() => setDeletingUser(row)}
              className="p-1.5 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4 max-w-6xl">
      <PageHeader
        title="Quản lý Tài khoản & Phân quyền"
        subtitle="Quản trị người dùng hệ thống, phân quyền vai trò và bảo mật đăng nhập"
        action={
          <button
            type="button"
            onClick={handleOpenCreate}
            className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>Thêm tài khoản</span>
          </button>
        }
      />

      <AdvancedFilterBar
        searchTerm={keyword}
        onSearchChange={(val) => {
          setKeyword(val);
          setCurrentPage(0);
        }}
        placeholder="Tìm kiếm theo username, email..."
        activeFilterCount={[keyword, selectedRole, selectedStatus].filter((v) => v !== undefined && v !== '').length}
        onResetFilters={() => {
          setKeyword('');
          setSelectedRole(undefined);
          setSelectedStatus(undefined);
          setCurrentPage(0);
        }}
      >
        {/* Role Filter */}
        <select
          value={selectedRole ?? ''}
          onChange={(e) => {
            setSelectedRole(e.target.value ? e.target.value : undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">🛡️ Tất cả vai trò</option>
          <option value="ADMIN">ADMIN</option>
          <option value="HR">HR</option>
          <option value="MANAGER">MANAGER</option>
          <option value="EMPLOYEE">EMPLOYEE</option>
        </select>

        {/* Status Filter */}
        <select
          value={selectedStatus === undefined ? '' : selectedStatus ? 'true' : 'false'}
          onChange={(e) => {
            setSelectedStatus(e.target.value === '' ? undefined : e.target.value === 'true');
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">⚡ Tất cả trạng thái</option>
          <option value="true">Đang hoạt động</option>
          <option value="false">Tạm dừng / Khóa</option>
        </select>
      </AdvancedFilterBar>

      <DataTable
        columns={columns}
        data={users}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />

      {/* Modal: Create User */}
      {isOpenCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-100 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
                  <Shield className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-gray-900">Tạo tài khoản người dùng mới</h3>
                  <p className="text-xs text-gray-400">Cấp tài khoản đăng nhập vào hệ thống HRMS</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setIsOpenCreateModal(false)}
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit} className="space-y-4">
              <FormField label="Tên đăng nhập (Username)" required error={createErrors.username}>
                <input
                  type="text"
                  value={createForm.username}
                  onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
                  placeholder="VD: nguyen_an"
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </FormField>

              <FormField label="Email" required error={createErrors.email}>
                <input
                  type="email"
                  value={createForm.email}
                  onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
                  placeholder="VD: an.nguyen@company.com"
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </FormField>

              <FormField label="Mật khẩu khởi tạo" required error={createErrors.password}>
                <div className="relative">
                  <input
                    type="text"
                    value={createForm.password}
                    onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                    placeholder="Mật khẩu từ 6 ký tự trở lên..."
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 pr-24 font-mono"
                  />
                  <button
                    type="button"
                    onClick={() => setCreateForm({ ...createForm, password: 'User@' + Math.floor(1000 + Math.random() * 9000) })}
                    className="absolute right-1.5 top-1.5 px-2 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 text-[11px] rounded flex items-center gap-1 transition-colors"
                  >
                    <Sparkles className="w-3 h-3 text-amber-500" />
                    <span>Tạo mẫu</span>
                  </button>
                </div>
              </FormField>

              {/* Roles Multi-select */}
              <FormField label="Phân quyền vai trò" required error={createErrors.roles}>
                <div className="grid grid-cols-2 gap-2 pt-1">
                  {['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'].map((role) => {
                    const isChecked = createForm.roles.includes(role);
                    return (
                      <label
                        key={role}
                        className={`flex items-center gap-2 p-2.5 rounded-lg border text-xs cursor-pointer transition-all ${
                          isChecked
                            ? 'bg-blue-50/70 border-blue-300 text-blue-900 font-semibold'
                            : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setCreateForm({ ...createForm, roles: [...createForm.roles, role] });
                            } else {
                              setCreateForm({
                                ...createForm,
                                roles: createForm.roles.filter((r) => r !== role),
                              });
                            }
                          }}
                          className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span>{role}</span>
                      </label>
                    );
                  })}
                </div>
              </FormField>

              {/* Employee Linkage */}
              <FormField label="Liên kết với Hồ sơ Nhân viên">
                <select
                  value={createForm.employeeId ?? ''}
                  onChange={(e) =>
                    setCreateForm({
                      ...createForm,
                      employeeId: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                >
                  <option value="">-- Không liên kết --</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName} ({emp.employeeCode})
                    </option>
                  ))}
                </select>
              </FormField>

              <div className="pt-3 border-t border-gray-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsOpenCreateModal(false)}
                  className="px-4 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isCreating}
                  className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
                >
                  <Save className="w-3.5 h-3.5" />
                  <span>{isCreating ? 'Đang tạo...' : 'Tạo tài khoản'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Edit User */}
      {isOpenEditModal && editingUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-100 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
                  <Edit2 className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-gray-900">
                    Chỉnh sửa tài khoản: {editingUser.username}
                  </h3>
                  <p className="text-xs text-gray-400">Cập nhật vai trò, email và trạng thái truy cập</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setIsOpenEditModal(false)}
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleEditSubmit} className="space-y-4">
              <FormField label="Email" required error={editErrors.email}>
                <input
                  type="email"
                  value={editForm.email}
                  onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </FormField>

              {/* Roles Multi-select */}
              <FormField label="Phân quyền vai trò" required error={editErrors.roles}>
                <div className="grid grid-cols-2 gap-2 pt-1">
                  {['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'].map((role) => {
                    const isChecked = editForm.roles.includes(role);
                    return (
                      <label
                        key={role}
                        className={`flex items-center gap-2 p-2.5 rounded-lg border text-xs cursor-pointer transition-all ${
                          isChecked
                            ? 'bg-blue-50/70 border-blue-300 text-blue-900 font-semibold'
                            : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={isChecked}
                          onChange={(e) => {
                            if (e.target.checked) {
                              setEditForm({ ...editForm, roles: [...editForm.roles, role] });
                            } else {
                              setEditForm({
                                ...editForm,
                                roles: editForm.roles.filter((r) => r !== role),
                              });
                            }
                          }}
                          className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span>{role}</span>
                      </label>
                    );
                  })}
                </div>
              </FormField>

              {/* Employee Linkage */}
              <FormField label="Liên kết với Hồ sơ Nhân viên">
                <select
                  value={editForm.employeeId ?? ''}
                  onChange={(e) =>
                    setEditForm({
                      ...editForm,
                      employeeId: e.target.value ? Number(e.target.value) : undefined,
                    })
                  }
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                >
                  <option value="">-- Không liên kết --</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName} ({emp.employeeCode})
                    </option>
                  ))}
                </select>
              </FormField>

              {/* Status toggles */}
              <div className="grid grid-cols-2 gap-3 pt-2">
                <label className="flex items-center gap-2 p-2.5 rounded-lg border border-gray-200 text-xs cursor-pointer">
                  <input
                    type="checkbox"
                    checked={editForm.enabled}
                    onChange={(e) => setEditForm({ ...editForm, enabled: e.target.checked })}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span>Kích hoạt tài khoản</span>
                </label>

                <label className="flex items-center gap-2 p-2.5 rounded-lg border border-gray-200 text-xs cursor-pointer">
                  <input
                    type="checkbox"
                    checked={editForm.accountNonLocked}
                    onChange={(e) => setEditForm({ ...editForm, accountNonLocked: e.target.checked })}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <span>Không bị khóa</span>
                </label>
              </div>

              <div className="pt-3 border-t border-gray-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsOpenEditModal(false)}
                  className="px-4 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isUpdating}
                  className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
                >
                  <Save className="w-3.5 h-3.5" />
                  <span>{isUpdating ? 'Đang lưu...' : 'Lưu thay đổi'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Reset Password */}
      {isOpenResetModal && resetUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-gray-100 space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
                  <KeyRound className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-gray-900">Đặt lại mật khẩu</h3>
                  <p className="text-xs text-gray-400">Tài khoản: {resetUser.username}</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setIsOpenResetModal(false)}
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleResetSubmit} className="space-y-4">
              <FormField label="Mật khẩu mới" required error={resetError}>
                <div className="relative">
                  <input
                    type="text"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="Nhập mật khẩu mới..."
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 pr-24 font-mono"
                  />
                  <button
                    type="button"
                    onClick={() => setNewPassword('Admin@' + Math.floor(1000 + Math.random() * 9000))}
                    className="absolute right-1.5 top-1.5 px-2 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 text-[11px] rounded flex items-center gap-1 transition-colors"
                  >
                    <Sparkles className="w-3 h-3 text-amber-500" />
                    <span>Tạo ngẫu nhiên</span>
                  </button>
                </div>
              </FormField>

              <div className="pt-3 border-t border-gray-100 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsOpenResetModal(false)}
                  className="px-4 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isResetting}
                  className="px-4 py-2 text-xs font-semibold text-white bg-amber-600 hover:bg-amber-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs disabled:opacity-50"
                >
                  <KeyRound className="w-3.5 h-3.5" />
                  <span>{isResetting ? 'Đang cập nhật...' : 'Xác nhận đổi'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Confirm Delete User */}
      <ConfirmDialog
        isOpen={!!deletingUser}
        title="Xác nhận xóa tài khoản"
        description={`Bạn có chắc chắn muốn xóa tài khoản người dùng "${deletingUser?.username}" (${deletingUser?.email})?`}
        confirmText="Xác nhận xóa"
        cancelText="Hủy"
        isDestructive
        isLoading={isDeleting}
        onConfirm={handleDeleteConfirm}
        onClose={() => setDeletingUser(null)}
      />
    </div>
  );
};
