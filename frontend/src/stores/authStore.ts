import { create } from 'zustand';
import { TokenResponse, UserResponse } from '@/types';
import { wsService } from '@/lib/websocket';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserResponse | null;
  roles: string[];
  isAuthenticated: boolean;
  login: (tokens: TokenResponse, user?: UserResponse) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: UserResponse) => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (...roles: string[]) => boolean;
}

const ACCESS_TOKEN_KEY = 'hrms_access_token';
const REFRESH_TOKEN_KEY = 'hrms_refresh_token';
const USER_KEY = 'hrms_user';

const storedAccessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
const storedRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
const storedUser = localStorage.getItem(USER_KEY);
let initialUser: UserResponse | null = null;
try {
  if (storedUser) initialUser = JSON.parse(storedUser);
} catch {
  initialUser = null;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  accessToken: storedAccessToken,
  refreshToken: storedRefreshToken,
  user: initialUser,
  roles: initialUser?.roles || [],
  isAuthenticated: !!storedAccessToken,

  login: (tokens: TokenResponse, user?: UserResponse) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    }
    set({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: user || null,
      roles: user?.roles || [],
      isAuthenticated: true,
    });
    // Connect WebSocket on login
    wsService.connect(tokens.accessToken);
  },

  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    set({ accessToken, refreshToken, isAuthenticated: true });
  },

  setUser: (user: UserResponse) => {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    const userRoles = Array.isArray(user.roles) ? user.roles : [];
    set({ user, roles: userRoles });
  },

  logout: () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    // Disconnect WebSocket
    wsService.disconnect();
    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      roles: [],
      isAuthenticated: false,
    });
  },

  hasRole: (role: string) => {
    const user = get().user;
    const roles = get().roles || [];
    const allRoles: string[] = [];

    // Extract roles from store state
    if (Array.isArray(roles)) {
      roles.forEach((r) => {
        if (typeof r === 'string') allRoles.push(r);
        else if (r && typeof r === 'object' && 'name' in r) allRoles.push((r as any).name);
      });
    }

    // Extract roles from user object
    if (user && Array.isArray(user.roles)) {
      user.roles.forEach((r) => {
        if (typeof r === 'string') allRoles.push(r);
        else if (r && typeof r === 'object' && 'name' in r) allRoles.push((r as any).name);
      });
    }

    // Always grant full admin access if username is admin
    if (user && user.username?.toLowerCase() === 'admin') {
      allRoles.push('ADMIN', 'ROLE_ADMIN', 'HR', 'ROLE_HR', 'MANAGER', 'ROLE_MANAGER');
    }

    const cleanTarget = role.replace(/^ROLE_/, '').toUpperCase();
    return allRoles.some((r) => {
      const cleanR = String(r).replace(/^ROLE_/, '').toUpperCase();
      return cleanR === cleanTarget;
    });
  },

  hasAnyRole: (...requiredRoles: string[]) => {
    return requiredRoles.some((role) => get().hasRole(role));
  },
}));
