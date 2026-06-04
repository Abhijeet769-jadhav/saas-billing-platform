import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Line } from 'react-chartjs-2';
import { 
  Chart as ChartJS, CategoryScale, LinearScale, PointElement, 
  LineElement, Title, Tooltip, Legend, Filler 
} from 'chart.js';
import { CreditCard, ShieldCheck, Activity, Users, Download, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';
import axios from 'axios';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const Dashboard = () => {
  const { user } = useAuth();
  
  const [subscription, setSubscription] = useState(null);
  const [usage, setUsage] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [subRes, usageRes, invRes] = await Promise.all([
        axios.get('/api/subscriptions'),
        axios.get('/api/usage/current'),
        axios.get('/api/invoices')
      ]);

      setSubscription(subRes.data);
      setUsage(usageRes.data);
      setInvoices(invRes.data.slice(0, 5)); // Load top 5
    } catch (e) {
      console.error('Failed to load dashboard metrics', e);
    } finally {
      setLoading(false);
    }
  };

  const chartData = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      {
        label: 'Workspace API Requests',
        data: [1200, 1900, 3000, 5000, 4000, 7000],
        borderColor: '#6366f1',
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        fill: true,
        tension: 0.4,
      }
    ]
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }
    },
    scales: {
      y: { grid: { display: false } },
      x: { grid: { display: false } }
    }
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
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold">Workspace Overview</h1>
        <p className="text-slate-400 text-xs mt-1">Real-time status of your subscription tier and consumption metrics</p>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Subscription Tier */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex items-start gap-4">
          <div className="h-12 w-12 bg-indigo-50 dark:bg-indigo-950/40 border border-indigo-200 dark:border-indigo-800 rounded-xl flex items-center justify-center text-indigo-600 dark:text-indigo-400">
            <ShieldCheck size={24} />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Active Plan</span>
            <h2 className="text-lg font-bold mt-0.5">{subscription?.planName || 'Basic Plan'}</h2>
            <span className={`inline-block text-[10px] px-2 py-0.5 rounded-full font-bold uppercase mt-2 ${
              subscription?.status === 'ACTIVE' 
                ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border border-emerald-200/50'
                : 'bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 border border-indigo-200/50'
            }`}>
              {subscription?.status || 'TRIAL'}
            </span>
          </div>
        </div>

        {/* Next Invoice Billing Date */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex items-start gap-4">
          <div className="h-12 w-12 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 rounded-xl flex items-center justify-center text-emerald-600 dark:text-emerald-400">
            <CreditCard size={24} />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Next Billing Date</span>
            <h2 className="text-lg font-bold mt-0.5">
              {subscription?.currentPeriodEnd 
                ? new Date(subscription.currentPeriodEnd).toLocaleDateString()
                : 'N/A'}
            </h2>
            <Link to="/plans" className="text-xs text-indigo-500 font-semibold hover:underline mt-2 inline-flex items-center gap-1">
              Change plan tier <ExternalLink size={12} />
            </Link>
          </div>
        </div>

        {/* Feature Usage Overview */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex items-start gap-4">
          <div className="h-12 w-12 bg-purple-50 dark:bg-purple-950/40 border border-purple-200 dark:border-purple-800 rounded-xl flex items-center justify-center text-purple-600 dark:text-purple-400">
            <Activity size={24} />
          </div>
          <div>
            <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">API Usage Percentage</span>
            <h2 className="text-lg font-bold mt-0.5">
              {usage.find(u => u.metricKey === 'api_calls')?.usagePercentage?.toFixed(1) || '0.0'}%
            </h2>
            <p className="text-[11px] text-slate-400 mt-2 font-medium">Logged in current billing cycle</p>
          </div>
        </div>
      </div>

      {/* Main Grid: Charts + Progress Meters */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Chart Card */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h3 className="font-bold text-sm">Metered API Requests</h3>
              <p className="text-slate-400 text-[10px] mt-0.5">Requests processed across the API gateway</p>
            </div>
            <span className="text-xs bg-slate-100 dark:bg-slate-800 px-3 py-1 rounded-full font-medium">
              Last 6 Months
            </span>
          </div>
          <div className="h-64 relative">
            <Line data={chartData} options={chartOptions} />
          </div>
        </div>

        {/* Usage Limits Progress Bars */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm space-y-6">
          <div>
            <h3 className="font-bold text-sm">Feature Limits Summary</h3>
            <p className="text-slate-400 text-[10px] mt-0.5">Meters track current usage caps</p>
          </div>

          <div className="space-y-5">
            {usage.map((metric) => (
              <div key={metric.metricKey} className="space-y-2">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="capitalize">{metric.metricKey.replace('_', ' ')}</span>
                  <span className="text-slate-400">
                    {metric.quantity} / {metric.maxLimit >= 999999 ? 'Unlimited' : metric.maxLimit}
                  </span>
                </div>
                <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-indigo-500 rounded-full transition-all duration-500" 
                    style={{ width: `${Math.min(metric.usagePercentage || 0, 100)}%` }} 
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Invoices List Section */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h3 className="font-bold text-sm">Recent Invoices</h3>
            <p className="text-slate-400 text-[10px] mt-0.5">Download generated receipts history sheets</p>
          </div>
          <Link to="/invoices" className="text-xs text-indigo-500 font-semibold hover:underline">
            View All
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 uppercase tracking-wider font-bold">
                <th className="pb-3">Invoice Number</th>
                <th className="pb-3">Date</th>
                <th className="pb-3">Reason</th>
                <th className="pb-3">Amount</th>
                <th className="pb-3">Status</th>
                <th className="pb-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {invoices.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-6 text-slate-400">
                    No billing history found
                  </td>
                </tr>
              ) : (
                invoices.map((inv) => (
                  <tr key={inv.id} className="border-b border-slate-50 dark:border-slate-800/40 hover:bg-slate-50/50 dark:hover:bg-slate-800/20 last:border-0">
                    <td className="py-4 font-bold text-slate-800 dark:text-white">{inv.invoiceNumber}</td>
                    <td className="py-4">{new Date(inv.createdAt).toLocaleDateString()}</td>
                    <td className="py-4 capitalize">{inv.billingReason?.replace('_', ' ') || 'Renewal'}</td>
                    <td className="py-4 font-semibold">${inv.total}</td>
                    <td className="py-4">
                      <span className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                        inv.status === 'PAID'
                          ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border border-emerald-200/50'
                          : 'bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 border border-amber-200/50'
                      }`}>
                        {inv.status}
                      </span>
                    </td>
                    <td className="py-4 text-right">
                      <a
                        href={`/api/invoices/${inv.id}/download`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all font-semibold"
                      >
                        <Download size={12} /> Download
                      </a>
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

export default Dashboard;
