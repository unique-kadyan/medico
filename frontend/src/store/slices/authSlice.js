import { createSlice } from '@reduxjs/toolkit';
import authService from '../../services/authService';

const loadUser = () => {
  try {
    const user = authService.getCurrentUser();
    if (user && typeof user === 'object') {
      return user;
    }
    return null;
  } catch (error) {
    console.error('Error loading user from localStorage:', error);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    return null;
  }
};

const initialState = {
  user: loadUser(),
  isAuthenticated: authService.isAuthenticated() && loadUser() !== null,
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginStart: (state) => {
      state.loading = true;
      state.error = null;
    },
    loginSuccess: (state, action) => {
      state.loading = false;
      state.isAuthenticated = true;
      state.user = action.payload;
      state.error = null;
    },
    loginFailure: (state, action) => {
      state.loading = false;
      state.isAuthenticated = false;
      state.user = null;
      state.error = action.payload;
    },
    logout: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.error = null;
      authService.logout();
    },
    clearError: (state) => {
      state.error = null;
    },
  },
});

export const { loginStart, loginSuccess, loginFailure, logout, clearError } =
  authSlice.actions;

export default authSlice.reducer;
