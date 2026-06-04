import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Bell, User, Check, Building } from 'lucide-react';
import axios from 'axios';

const Navbar = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [showNotif, setShowNotif] = useState(false);

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await axios.get('/api/notifications');
      setNotifications(res.data.filter(n => !n.isRead));
    } catch (e) {
      console.error('Failed to load notifications', e);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await axios.put(`/api/notifications/${id}/read`);
      setNotifications(prev => prev.filter(n => n.id !== id));
    } catch (e) {
      console.error('Failed to mark read', e);
    }
  };

  return (
    <header className="h-16 border-b bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 flex items-center justify-between px-8 z-10">
      <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
        <Building size={18} className="text-indigo-500" />
        <span className="font-semibold text-slate-800 dark:text-white">
          {user?.organizationName || 'Personal Workspace'}
        </span>
        <span className="text-xs bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 px-2 py-0.5 rounded-full font-semibold border border-indigo-200/50">
          {user?.role === 'ROLE_ORGANIZATION' ? 'Owner' : user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Member'}
        </span>
      </div>

      <div className="flex items-center gap-4">
        {/* Notifications Popover */}
        <div className="relative">
          <button
            onClick={() => setShowNotif(!showNotif)}
            className="h-10 w-10 flex items-center justify-center rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 relative transition-all"
          >
            <Bell size={18} className="text-slate-600 dark:text-slate-300" />
            {notifications.length > 0 && (
              <span className="absolute top-2 right-2 h-2.5 w-2.5 bg-rose-500 rounded-full ring-2 ring-white dark:ring-slate-900 animate-pulse" />
            )}
          </button>

          {showNotif && (
            <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl shadow-xl z-20 py-2">
              <div className="px-4 py-2 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
                <span className="font-semibold text-sm">Notifications</span>
                <span className="text-xs bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-full font-medium">
                  {notifications.length} Unread
                </span>
              </div>
              <div className="max-h-64 overflow-y-auto">
                {notifications.length === 0 ? (
                  <div className="p-4 text-center text-xs text-slate-400">
                    No new notifications
                  </div>
                ) : (
                  notifications.map((n) => (
                    <div key={n.id} className="p-3 hover:bg-slate-50 dark:hover:bg-slate-800/50 flex gap-2 border-b last:border-0 border-slate-100 dark:border-slate-800/30">
                      <div className="flex-1">
                        <h4 className="font-bold text-xs">{n.title}</h4>
                        <p className="text-[11px] text-slate-500 mt-0.5 leading-relaxed">{n.message}</p>
                      </div>
                      <button
                        onClick={() => handleMarkAsRead(n.id)}
                        className="h-6 w-6 flex items-center justify-center rounded bg-slate-100 dark:bg-slate-800 hover:bg-indigo-600 hover:text-white transition-all text-slate-500"
                        title="Mark as read"
                      >
                        <Check size={12} />
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* Profile Details */}
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 bg-indigo-100 dark:bg-indigo-950/60 rounded-lg flex items-center justify-center border border-indigo-200 dark:border-indigo-800 text-indigo-600 dark:text-indigo-400">
            <User size={18} />
          </div>
          <div className="hidden md:block">
            <h3 className="text-xs font-bold leading-tight">{user?.email}</h3>
            <span className="text-[10px] text-slate-400 font-medium">Active User Account</span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
