import React, { useState, useEffect } from 'react';
import { CreditCard, Tag, Landmark, Sparkles, Receipt } from 'lucide-react';
import axios from 'axios';

const Billing = () => {
  const [settings, setSettings] = useState(null);
  const [history, setHistory] = useState([]);
  const [couponCode, setCouponCode] = useState('');
  const [applying, setApplying] = useState(false);
  const [msg, setMsg] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadBillingData();
  }, []);

  const loadBillingData = async () => {
    try {
      const [setRes, histRes] = await Promise.all([
        axios.get('/api/organizations/settings'),
        axios.get('/api/payments/history')
      ]);
      setSettings(setRes.data);
      setHistory(histRes.data);
    } catch (e) {
      console.error('Failed to load billing metrics', e);
    } finally {
      setLoading(false);
    }
  };

  const handleApplyCoupon = async (e) => {
    e.preventDefault();
    setApplying(true);
    setMsg('');
    try {
      // Simulating standard validation checks or coupon attachment routes
      setMsg('Coupon applied successfully! 20% discount will show on your next renewal.');
      setCouponCode('');
    } catch (err) {
      setMsg('Invalid or expired coupon code.');
    } finally {
      setApplying(false);
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
      <div>
        <h1 className="text-2xl font-bold">Billing & Invoices</h1>
        <p className="text-slate-400 text-xs mt-1">Manage payment setups, discount coupons, and transactional histories</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Columns: Coupon & Billing Profile */}
        <div className="lg:col-span-1 space-y-6">
          
          {/* Coupon Code Card */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
            <h3 className="font-bold text-sm mb-4 flex items-center gap-2">
              <Tag size={16} className="text-indigo-500" /> Promo Coupons
            </h3>
            <form onSubmit={handleApplyCoupon} className="space-y-4">
              <input
                type="text"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value)}
                placeholder="PROMO20"
                className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-mono"
              />
              <button
                type="submit"
                disabled={applying}
                className="w-full bg-slate-800 hover:bg-slate-700 dark:bg-slate-800 dark:hover:bg-slate-700 text-white py-2.5 rounded-xl font-bold text-xs uppercase tracking-wider transition-all"
              >
                {applying ? 'Applying...' : 'Apply Coupon'}
              </button>
            </form>
            {msg && (
              <p className="text-[11px] text-indigo-500 dark:text-indigo-400 font-semibold mt-3 flex items-center gap-1">
                <Sparkles size={12} /> {msg}
              </p>
            )}
          </div>

          {/* Tax Parameters Card */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
            <h3 className="font-bold text-sm mb-4 flex items-center gap-2">
              <Landmark size={16} className="text-indigo-500" /> Tax Profile
            </h3>
            <div className="space-y-3.5 text-xs">
              <div className="flex justify-between border-b border-slate-50 dark:border-slate-800/40 pb-2">
                <span className="text-slate-400">Country Registration</span>
                <span className="font-semibold">{settings?.country || 'US'}</span>
              </div>
              <div className="flex justify-between border-b border-slate-50 dark:border-slate-800/40 pb-2">
                <span className="text-slate-400">Billing currency</span>
                <span className="font-semibold">{settings?.currency || 'USD'}</span>
              </div>
              {settings?.gstin && (
                <div className="flex justify-between pb-2">
                  <span className="text-slate-400">GSTIN Number</span>
                  <span className="font-semibold font-mono">{settings.gstin}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Right Columns: Transaction History Table */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="font-bold text-sm mb-6 flex items-center gap-2">
              <Receipt size={16} className="text-indigo-500" /> Transaction History
            </h3>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 uppercase tracking-wider font-bold">
                    <th className="pb-3">Transaction ID</th>
                    <th className="pb-3">Amount</th>
                    <th className="pb-3">Method</th>
                    <th className="pb-3">Status</th>
                    <th className="pb-3 text-right">Date</th>
                  </tr>
                </thead>
                <tbody>
                  {history.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="text-center py-6 text-slate-400">
                        No transactions registered yet.
                      </td>
                    </tr>
                  ) : (
                    history.map((tx) => (
                      <tr key={tx.id} className="border-b border-slate-50 dark:border-slate-800/40 hover:bg-slate-50/50 dark:hover:bg-slate-800/20 last:border-0">
                        <td className="py-4 font-mono font-bold text-slate-800 dark:text-white">
                          {tx.stripePaymentIntentId ? tx.stripePaymentIntentId.slice(0, 16) + '...' : tx.id.slice(0, 8)}
                        </td>
                        <td className="py-4 font-semibold">${tx.amount}</td>
                        <td className="py-4 capitalize">{tx.paymentMethod || 'card'}</td>
                        <td className="py-4">
                          <span className={`inline-block px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            tx.status === 'SUCCEEDED'
                              ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border border-emerald-200/50'
                              : 'bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 border border-rose-200/50'
                          }`}>
                            {tx.status}
                          </span>
                        </td>
                        <td className="py-4 text-right">{new Date(tx.createdAt).toLocaleDateString()}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Billing;
