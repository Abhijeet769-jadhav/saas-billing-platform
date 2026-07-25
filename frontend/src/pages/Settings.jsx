import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Settings as SettingsIcon, CheckCircle, Save, Landmark } from 'lucide-react';
import api from '../services/api';

const Settings = () => {
  const { user, updateOrgDetails } = useAuth();
  
  const [orgName, setOrgName] = useState('');
  const [billingEmail, setBillingEmail] = useState('');
  const [country, setCountry] = useState('US');
  const [currency, setCurrency] = useState('USD');
  const [gstin, setGstin] = useState('');
  const [taxRegistrationNumber, setTaxRegistrationNumber] = useState('');
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const [orgRes, setRes] = await Promise.all([
        api.get('/api/organizations/current'),
        api.get('/api/organizations/settings')
      ]);

      setOrgName(orgRes.data?.name || '');
      setBillingEmail(setRes.data?.billingEmail || '');
      setCountry(setRes.data?.country || 'US');
      setCurrency(setRes.data?.currency || 'USD');
      setGstin(setRes.data?.gstin || '');
      setTaxRegistrationNumber(setRes.data?.taxRegistrationNumber || '');
    } catch (e) {
      console.error('Failed to load settings', e);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMsg('');

    try {
      // 1. Update Organization Metadata name
      const orgRes = await api.put('/api/organizations/current', {
        name: orgName,
        slug: orgName.toLowerCase().replace(/[^a-z0-9]/g, '-')
      });
      updateOrgDetails(orgRes.data);

      // 2. Update Settings
      await api.put('/api/organizations/settings', {
        billingEmail,
        country,
        currency,
        gstin,
        taxRegistrationNumber
      });

      setMsg('Settings updated successfully!');
      setTimeout(() => setMsg(''), 4000);
    } catch (e) {
      console.error('Failed to save settings', e);
      setMsg('Failed to save settings. Please try again.');
    } finally {
      setSaving(false);
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
    <div className="space-y-8 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold">Workspace Settings</h1>
        <p className="text-slate-400 text-xs mt-1">Configure your organization profiles and regional tax identifiers</p>
      </div>

      {msg && (
        <div className="bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 text-emerald-600 dark:text-emerald-400 p-3 rounded-xl flex items-center gap-2.5 text-xs font-semibold">
          <CheckCircle size={16} />
          <span>{msg}</span>
        </div>
      )}

      <form onSubmit={handleSave} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-8 shadow-sm space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Organization Name</label>
            <input
              type="text"
              required
              value={orgName}
              onChange={(e) => setOrgName(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-semibold"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Billing Email</label>
            <input
              type="email"
              required
              value={billingEmail}
              onChange={(e) => setBillingEmail(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-semibold"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Country Code</label>
            <select
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-semibold"
            >
              <option value="US">United States (US)</option>
              <option value="IN">India (IN)</option>
              <option value="GB">United Kingdom (GB)</option>
              <option value="CA">Canada (CA)</option>
            </select>
          </div>

          <div className="space-y-1.5">
            <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Currency</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-semibold"
            >
              <option value="USD">USD ($)</option>
              <option value="INR">INR (₹)</option>
              <option value="GBP">GBP (£)</option>
              <option value="EUR">EUR (€)</option>
            </select>
          </div>
        </div>

        <div className="border-t border-slate-100 dark:border-slate-800/80 my-6" />

        {/* GSTIN / Indian Tax block */}
        <div className="space-y-4">
          <h3 className="text-sm font-bold flex items-center gap-2 text-indigo-500">
            <Landmark size={16} /> Regional Tax Registrations
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-1.5">
              <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Tax ID / Business Number</label>
              <input
                type="text"
                value={taxRegistrationNumber}
                onChange={(e) => setTaxRegistrationNumber(e.target.value)}
                placeholder="Tax registration ID..."
                className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-semibold"
              />
            </div>

            {country === 'IN' && (
              <div className="space-y-1.5">
                <label className="text-[10px] font-bold uppercase tracking-wider text-slate-400">GSTIN Identification Number</label>
                <input
                  type="text"
                  maxLength={15}
                  value={gstin}
                  onChange={(e) => setGstin(e.target.value)}
                  placeholder="27AAAAA0000A1Z5"
                  className="w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all font-mono font-semibold"
                />
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-end pt-4">
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold text-xs uppercase tracking-wider transition-all flex items-center gap-2"
          >
            <Save size={14} /> {saving ? 'Saving...' : 'Save Settings'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default Settings;
