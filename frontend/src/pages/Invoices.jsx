import React, { useState, useEffect } from 'react';
import { Download, Mail, Search, FileText, CheckCircle } from 'lucide-react';
import api from '../services/api';

const Invoices = () => {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('ALL'); // ALL, PAID, OPEN
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    fetchInvoices();
  }, []);

  const fetchInvoices = async () => {
    try {
      const res = await api.get('/api/invoices');
      setInvoices(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error('Failed to load invoices', e);
    } finally {
      setLoading(false);
    }
  };

  const handleEmailInvoice = async (id) => {
    setSuccessMsg('');
    try {
      await api.post(`/api/invoices/${id}/email`);
      setSuccessMsg('Invoice notification email dispatched successfully!');
      setTimeout(() => setSuccessMsg(''), 4000);
    } catch (e) {
      console.error('Failed to send email', e);
    }
  };

  const filteredInvoices = invoices.filter((inv) => {
    const matchesSearch = inv.invoiceNumber.toLowerCase().includes(search.toLowerCase());
    const matchesFilter = filter === 'ALL' || inv.status === filter;
    return matchesSearch && matchesFilter;
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">Invoices Archive</h1>
          <p className="text-slate-400 text-xs mt-1">Review your workspace billing sheets histories</p>
        </div>
      </div>

      {successMsg && (
        <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400 p-3 rounded-lg flex items-center gap-2.5 text-xs font-semibold">
          <CheckCircle size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Filters & Search Row */}
      <div className="flex flex-col md:flex-row justify-between gap-4">
        {/* Search */}
        <div className="relative w-full md:w-80">
          <Search size={16} className="absolute left-3.5 top-3 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search invoice number..."
            className="w-full pl-11 pr-4 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
          />
        </div>

        {/* Filter buttons */}
        <div className="flex gap-2">
          {['ALL', 'PAID', 'OPEN'].map((status) => (
            <button
              key={status}
              onClick={() => setFilter(status)}
              className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all border ${
                filter === status
                  ? 'bg-slate-800 border-slate-800 text-white dark:bg-slate-800 dark:border-slate-800'
                  : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300'
              }`}
            >
              {status}
            </button>
          ))}
        </div>
      </div>

      {/* Invoices List Card */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400 uppercase tracking-wider font-bold">
                <th className="pb-3">Invoice Number</th>
                <th className="pb-3">Date</th>
                <th className="pb-3">Due Date</th>
                <th className="pb-3">Subtotal</th>
                <th className="pb-3">GST/Tax</th>
                <th className="pb-3">Total Amount</th>
                <th className="pb-3">Status</th>
                <th className="pb-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredInvoices.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-8 text-slate-400">
                    No matching invoices found.
                  </td>
                </tr>
              ) : (
                filteredInvoices.map((inv) => (
                  <tr key={inv.id} className="border-b border-slate-50 dark:border-slate-800/40 hover:bg-slate-50/50 dark:hover:bg-slate-800/20 last:border-0">
                    <td className="py-4 font-bold text-slate-850 dark:text-white flex items-center gap-2">
                      <FileText size={14} className="text-slate-400" />
                      {inv.invoiceNumber}
                    </td>
                    <td className="py-4">{new Date(inv.createdAt).toLocaleDateString()}</td>
                    <td className="py-4">{new Date(inv.dueDate).toLocaleDateString()}</td>
                    <td className="py-4">${inv.subtotal}</td>
                    <td className="py-4 text-slate-500">${inv.taxAmount}</td>
                    <td className="py-4 font-bold">${inv.total}</td>
                    <td className="py-4">
                      <span className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                        inv.status === 'PAID'
                          ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border border-emerald-200/50'
                          : 'bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 border border-amber-200/50'
                      }`}>
                        {inv.status}
                      </span>
                    </td>
                    <td className="py-4 text-right space-x-2">
                      <button
                        onClick={() => handleEmailInvoice(inv.id)}
                        className="inline-flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all font-semibold"
                        title="Email receipt"
                      >
                        <Mail size={12} /> Email
                      </button>
                      <a
                        href={`${import.meta.env.VITE_API_URL || ''}/api/invoices/${inv.id}/download`}
                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-all font-semibold"
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

export default Invoices;
