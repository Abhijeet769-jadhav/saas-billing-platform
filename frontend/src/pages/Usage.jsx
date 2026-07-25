import React, { useState, useEffect } from 'react';
import { Activity, ShieldAlert, Sparkles, Server } from 'lucide-react';
import api from '../services/api';

const Usage = () => {
  const [usage, setUsage] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUsageData();
  }, []);

  const fetchUsageData = async () => {
    try {
      const res = await api.get('/api/usage/current');
      setUsage(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error('Failed to load usage limits', e);
    } finally {
      setLoading(false);
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
        <h1 className="text-2xl font-bold">Workspace Resource Usage</h1>
        <p className="text-slate-400 text-xs mt-1">Monitor consumption volumes against active plan parameters</p>
      </div>

      {/* Warning if any limit is over 80% */}
      {usage.some(u => u.usagePercentage >= 80.0) && (
        <div className="bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 text-amber-600 dark:text-amber-400 p-4 rounded-2xl flex items-start gap-3 text-xs font-semibold">
          <ShieldAlert size={18} className="flex-shrink-0 mt-0.5" />
          <div>
            <h4 className="font-bold">Nearing Plan Resource Limits</h4>
            <p className="text-[11px] text-slate-500 mt-1">One or more resources are above 80% of allowed allocations. Upgrade your subscription to increase limits.</p>
          </div>
        </div>
      )}

      {/* Grid displays */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {usage.map((metric) => {
          const isHigh = metric.usagePercentage >= 85.0;
          return (
            <div key={metric.metricKey} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
              
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h3 className="font-bold text-sm capitalize">{metric.metricKey.replace('_', ' ')}</h3>
                  <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider">Metered Resource</span>
                </div>
                <div className={`h-8 w-8 rounded-lg flex items-center justify-center ${
                  isHigh ? 'bg-rose-50 text-rose-600 dark:bg-rose-950/30 dark:text-rose-450' : 'bg-indigo-50 text-indigo-600 dark:bg-indigo-950/40 dark:text-indigo-400'
                }`}>
                  <Server size={16} />
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex justify-between items-baseline">
                  <span className="text-2xl font-extrabold">{metric.quantity}</span>
                  <span className="text-xs text-slate-400 font-medium">
                    Allowed: {metric.maxLimit >= 999999 ? 'Unlimited' : metric.maxLimit}
                  </span>
                </div>

                {/* Progress bar */}
                <div className="w-full h-3 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isHigh ? 'bg-rose-500' : 'bg-indigo-500'
                    }`}
                    style={{ width: `${Math.min(metric.usagePercentage || 0, 100)}%` }}
                  />
                </div>

                <div className="flex justify-between text-[10px] text-slate-400 font-semibold pt-1">
                  <span>Consumption Percent</span>
                  <span className={isHigh ? 'text-rose-500 font-bold' : ''}>
                    {metric.usagePercentage?.toFixed(1)}%
                  </span>
                </div>
              </div>

            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Usage;
