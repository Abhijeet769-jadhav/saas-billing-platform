import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Sidebar from './components/Sidebar';
import Navbar from './components/Navbar';

// Page Imports
import LandingPage from './pages/LandingPage';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Plans from './pages/Plans';
import Billing from './pages/Billing';
import Invoices from './pages/Invoices';
import Usage from './pages/Usage';
import Settings from './pages/Settings';
import Support from './pages/Support';
import AdminPanel from './pages/AdminPanel';

// Protected Route component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, token } = useAuth();
  
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

// Dashboard Layout wrapper
const DashboardLayout = ({ children, darkMode, setDarkMode }) => {
  return (
    <div className="flex min-h-screen">
      <Sidebar darkMode={darkMode} setDarkMode={setDarkMode} />
      <div className="flex-1 flex flex-col min-w-0 bg-slate-50 dark:bg-slate-950">
        <Navbar />
        <main className="p-8 flex-1 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
};

const AppContent = () => {
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('theme') === 'dark';
  });

  useEffect(() => {
    const root = window.document.documentElement;
    if (darkMode) {
      root.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      root.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [darkMode]);

  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected Dashboard Routes */}
        <Route path="/dashboard" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Dashboard />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/plans" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Plans />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/billing" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Billing />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/invoices" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Invoices />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/usage" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Usage />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/settings" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Settings />
            </DashboardLayout>
          </ProtectedRoute>
        } />
        <Route path="/support" element={
          <ProtectedRoute>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <Support />
            </DashboardLayout>
          </ProtectedRoute>
        } />

        {/* Admin Route */}
        <Route path="/admin" element={
          <ProtectedRoute allowedRoles={['ROLE_ADMIN']}>
            <DashboardLayout darkMode={darkMode} setDarkMode={setDarkMode}>
              <AdminPanel />
            </DashboardLayout>
          </ProtectedRoute>
        } />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Router>
  );
};

const App = () => {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
};

export default App;
