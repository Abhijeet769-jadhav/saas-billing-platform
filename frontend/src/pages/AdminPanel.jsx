import React, { useState, useEffect } from 'react';
import { Bar, Pie } from 'react-chartjs-2';
import { 
  Chart as ChartJS, CategoryScale, LinearScale, BarElement, 
  ArcElement, Title, Tooltip, Legend 
} from 'chart.js';
import { Shield, Sparkles, Download, HelpCircle, Users, Percent, CheckCircle } from 'lucide-react';
import api from '../services/api';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, Title, Tooltip, Legend);

const AdminPanel = () => {
  const [analytics, setAnalytics] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    try {
      const [analyticsRes, ticketsRes] = await Promise.all([
        api.get('/api/analytics/platform').catch(() => ({ data: null })),
        api.get('/api/tickets/all').catch(() => ({ data: [] }))
      ]);

      setAnalytics(analyticsRes.data);
      setTickets(Array.isArray(ticketsRes.data) ? ticketsRes.data : []);
    } catch (e) {
      console.error('Failed to load admin analytics', e);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveTicket = async (ticketId) => {
    setSuccess('');
    try {
      await api.post(`/api/tickets/${ticketId}/resolve`);
      setSuccess('Support ticket resolved successfully!');
      setTickets(prev => prev.map(t => t.id === ticketId ? { ...t, status: 'RESOLVED' } : t));
      setTimeout(() => setSuccess(''), 4000);
    } catch (e) {
      console.error('Failed to resolve support ticket', e);
    }
  };

  // Setup plan distribution pie chart
  const planLabels = analytics?.planDistribution ? Object.keys(analytics.planDistribution) : [];
  const planCounts = analytics?.planDistribution ? Object.values(analytics.planDistribution) : [];

  const pieData = {
    labels: planLabels.length > 0 ? planLabels : ['Basic', 'Pro', 'Enterprise'],
    datasets: [
      {
        data: planCounts.length > 0 ? planCounts : [12, 19, 3],
        backgroundColor: ['#818cf8', '#6366f1', '#4f46e5'],
        borderWidth: 1
      }
    ]
  };

  // Setup monthly revenue bar chart
  const monthLabels = analytics?.revenueByMonth ? Object.keys(analytics.revenueByMonth) : [];
  const monthRevenue = analytics?.revenueByMonth ? Object.values(analytics.revenueByMonth) : [];

  const barData = {
    labels: monthLabels.length > 0 ? monthLabels : ['2026-01', '2026-02', '2026-03', '2026-04', '2026-05'],
    datasets: [
      {
        label: 'Monthly Revenue ($)',
        data: monthRevenue.length > 0 ? monthRevenue : [1200, 1900, 3000, 5000, 6000],
        backgroundColor: '#6366f1',
        borderRadius: 8
      }
    ]
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <Shield className="text-emerald-500" /> System Admin Control Panel
          </h1>
          <p className="text-slate-400 text-xs mt-1">Global platform metrics, revenue streams, and tenant audits</p>
        </div>
        <a
          href={`${import.meta.env.VITE_API_URL || ''}/api/analytics/export`}
          className="inline-flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl text-xs font-bold shadow-md shadow-emerald-600/10 transition-all"
        >
          <Download size={14} /> Export Revenue CSV
        </a>
      </div>

      {success && (
        <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400 p-3 rounded-xl flex items-center gap-2.5 text-xs font-semibold">
          <CheckCircle size={16} />
          <span>{success}</span>
        </div>
      )}

      {/* Admin KPIs Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-sm">
          <span className="text-[10px] text-slate-450 font-bold uppercase tracking-wider block">Monthly Recurring Revenue</span>
          <h2 className="text-xl font-extrabold mt-1">${analytics?.mrr?.toFixed(2) || '0.00'}</h2>
          <span className="text-[10px] text-slate-400 font-medium">Platform MRR</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-sm">
          <span className="text-[10px] text-slate-450 font-bold uppercase tracking-wider block">Annual Recurring Revenue</span>
          <h2 className="text-xl font-extrabold mt-1">${analytics?.arr?.toFixed(2) || '0.00'}</h2>
          <span className="text-[10px] text-slate-400 font-medium">ARR Projection</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-sm">
          <span className="text-[10px] text-slate-450 font-bold uppercase tracking-wider block">Active Subscribers</span>
          <h2 className="text-xl font-extrabold mt-1">{analytics?.activeSubscribers || 0}</h2>
          <span className="text-[10px] text-slate-400 font-medium">Subscribed tenants</span>
        </div>

        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-5 shadow-sm">
          <span className="text-[10px] text-slate-450 font-bold uppercase tracking-wider block">Payment Success Rate</span>
          <h2 className="text-xl font-extrabold mt-1">{analytics?.paymentSuccessRate?.toFixed(1) || '100.0'}%</h2>
          <span className="text-[10px] text-slate-400 font-medium">Transaction successes</span>
        </div>
      </div>

      {/* Analytics Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Monthly Revenue Bar Chart */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <h3 className="font-bold text-sm mb-6">Monthly Revenue Streams</h3>
          <div className="h-64 relative">
            <Bar data={barData} options={{ responsive: true, maintainAspectRatio: false }} />
          </div>
        </div>

        {/* Plan Distribution Pie Chart */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
          <h3 className="font-bold text-sm mb-6">Plan Tier Distribution</h3>
          <div className="h-64 relative flex justify-center">
            <Pie data={pieData} options={{ responsive: true, maintainAspectRatio: false }} />
          </div>
        </div>
      </div>

      {/* Global Support Tickets Table */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
        <h3 className="font-bold text-sm mb-6 flex items-center gap-2">
          <HelpCircle size={16} className="text-indigo-500" /> Pending Support Tickets
        </h3>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 uppercase tracking-wider font-bold">
                <th className="pb-3">Tenant Details</th>
                <th className="pb-3">Subject</th>
                <th className="pb-3">Priority</th>
                <th className="pb-3">Status</th>
                <th className="pb-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {tickets.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-6 text-slate-400">
                    No tickets currently in queue.
                  </td>
                </tr>
              ) : (
                tickets.map((t) => (
                  <tr key={t.id} className="border-b border-slate-50 dark:border-slate-800/40 hover:bg-slate-50/50 dark:hover:bg-slate-800/20 last:border-0">
                    <td className="py-4 font-semibold text-slate-500">
                      Org ID: {t.organizationId.slice(0, 8)}...
                    </td>
                    <td className="py-4 font-bold text-slate-800 dark:text-white">
                      {t.subject}
                    </td>
                    <td className="py-4">
                      <span className={`inline-block px-2.5 py-0.5 rounded-full text-[9px] font-bold ${
                        t.priority === 'URGENT' || t.priority === 'HIGH'
                          ? 'bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-455'
                          : 'bg-slate-100 dark:bg-slate-800 text-slate-500'
                      }`}>
                        {t.priority}
                      </span>
                    </td>
                    <td className="py-4 font-semibold capitalize">{t.status}</td>
                    <td className="py-4 text-right">
                      {t.status !== 'RESOLVED' ? (
                        <button
                          onClick={() => handleResolveTicket(t.id)}
                          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-all font-semibold"
                        >
                          Mark Resolved
                        </button>
                      ) : (
                        <span className="text-slate-400 font-medium italic">Resolved</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminPanel;
