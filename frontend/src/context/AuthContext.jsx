import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('auth_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [token, setToken] = useState(() => localStorage.getItem('auth_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common['Authorization'];
    }
    setLoading(false);
  }, [token]);

  const login = async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', {
        email,
        password
      });

      const data = response.data;

      localStorage.setItem('auth_token', data.token);
      localStorage.setItem('auth_refresh', data.refreshToken);
      localStorage.setItem(
        'auth_user',
        JSON.stringify({
          id: data.userId,
          email: data.email,
          organizationId: data.organizationId,
          organizationName: data.organizationName,
          organizationSlug: data.organizationSlug,
          role: data.role
        })
      );

      setToken(data.token);

      setUser({
        id: data.userId,
        email: data.email,
        organizationId: data.organizationId,
        organizationName: data.organizationName,
        organizationSlug: data.organizationSlug,
        role: data.role
      });

      return data;
    } catch (error) {
      console.error('Login Error:', error.response?.data || error);
      throw (
        error.response?.data?.message ||
        error.response?.data?.error ||
        'Login failed'
      );
    }
  };

  const register = async (registerData) => {
    try {
      const response = await api.post('/api/auth/register', registerData);

      const data = response.data;

      localStorage.setItem('auth_token', data.token);
      localStorage.setItem('auth_refresh', data.refreshToken);
      localStorage.setItem(
        'auth_user',
        JSON.stringify({
          id: data.userId,
          email: data.email,
          organizationId: data.organizationId,
          organizationName: data.organizationName,
          organizationSlug: data.organizationSlug,
          role: data.role
        })
      );

      setToken(data.token);

      setUser({
        id: data.userId,
        email: data.email,
        organizationId: data.organizationId,
        organizationName: data.organizationName,
        organizationSlug: data.organizationSlug,
        role: data.role
      });

      return data;
    } catch (error) {
      console.error('Registration Error:', error.response?.data || error);

      throw (
        error.response?.data?.details ||
        error.response?.data?.message ||
        error.response?.data?.error ||
        'Registration failed'
      );
    }
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_refresh');
    localStorage.removeItem('auth_user');

    setToken(null);
    setUser(null);
  };

  const updateOrgDetails = (updatedOrg) => {
    const updatedUser = {
      ...user,
      organizationName: updatedOrg.name,
      organizationSlug: updatedOrg.slug
    };

    localStorage.setItem('auth_user', JSON.stringify(updatedUser));
    setUser(updatedUser);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        register,
        logout,
        loading,
        updateOrgDetails
      }}
    >
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);