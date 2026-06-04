import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  LayoutDashboard, CreditCard, FileText, Activity, 
  Settings, HelpCircle, Shield, LogOut, Sun, Moon 
} from 'lucide-react';

const Sidebar = ({ darkMode, setDarkMode }) => {
  const { user, logout } = useAuth();
  const location = useLocation();

  const menuItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'Plans', path: '/plans', icon: CreditCard },
    { name: 'Billing', path: '/billing', icon: CreditCard },
    { name: 'Invoices', path: '/invoices', icon: FileText },
    { name: 'Usage', path: '/usage', icon: Activity },
    { name: 'Settings', path: '/settings', icon: Settings },
    { name: 'Support', path: '/support', icon: HelpCircle },
  ];

  const adminItems = [
    { name: 'Admin Panel', path: '/admin', icon: Shield },
  ];

  const isActive = (path) => location.pathname === path;

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col justify-between h-screen sticky top-0 border-r border-slate-800">
      <div>
        <div className="p-6 flex items-center gap-3">
          <div className="h-10 w-10 bg-indigo-600 rounded-lg flex items-center justify-center font-bold text-xl text-white shadow-lg shadow-indigo-500/30">
            S
          </div>
          <div>
            <h1 className="font-bold text-lg text-white leading-tight">SaaS Billing</h1>
            <span className="text-xs text-indigo-400 font-medium">Platform</span>
          </div>
        </div>

        <nav className="px-4 space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all ${
                  isActive(item.path) 
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' 
                    : 'hover:bg-slate-800 hover:text-white'
                }`}
              >
                <Icon size={18} />
                {item.name}
              </Link>
            );
          })}

          {user?.role === 'ROLE_ADMIN' && (
            <>
              <div className="my-4 border-t border-slate-800 mx-4" />
              <div className="px-4 mb-2 text-xs font-semibold text-slate-500 uppercase tracking-wider">
                System Admin
              </div>
              {adminItems.map((item) => {
                const Icon = item.icon;
                return (
                  <Link
                    key={item.name}
                    to={item.path}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all ${
                      isActive(item.path) 
                        ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20' 
                        : 'hover:bg-slate-800 hover:text-white'
                    }`}
                  >
                    <Icon size={18} />
                    {item.name}
                  </Link>
                );
              })}
            </>
          )}
        </nav>
      </div>

      <div className="p-4 border-t border-slate-800 space-y-3">
        <button
          onClick={() => setDarkMode(!darkMode)}
          className="flex items-center justify-between w-full px-4 py-2.5 rounded-lg text-sm hover:bg-slate-800 transition-all font-medium"
        >
          <span className="flex items-center gap-3">
            {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            {darkMode ? 'Light Mode' : 'Dark Mode'}
          </span>
          <span className="text-xs text-indigo-400 font-semibold uppercase">
            {darkMode ? 'ON' : 'OFF'}
          </span>
        </button>

        <button
          onClick={logout}
          className="flex items-center gap-3 w-full px-4 py-2.5 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-950/20 transition-all"
        >
          <LogOut size={18} />
          Sign Out
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
