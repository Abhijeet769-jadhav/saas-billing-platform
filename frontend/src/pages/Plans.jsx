import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Check, CreditCard, Sparkles, ArrowUp, ArrowDown } from 'lucide-react';
import api from '../services/api';

const Plans = () => {
  const { user } = useAuth();
  
  const [plans, setPlans] = useState([]);
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [subscribingId, setSubscribingId] = useState(null);

  useEffect(() => {
    loadPlansAndSub();
  }, []);

  const loadPlansAndSub = async () => {
    try {
      const [plansRes, subRes] = await Promise.all([
        api.get('/api/plans'),
        api.get('/api/subscriptions').catch(() => ({ data: null }))
      ]);
      setPlans(Array.isArray(plansRes.data) ? plansRes.data : []);
      setSubscription(subRes.data);
    } catch (e) {
      console.error('Failed to load plans', e);
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (planId) => {
    setSubscribingId(planId);
    try {
      if (!subscription) {
        // New subscription
        await api.post('/api/subscriptions/subscribe', { planId });
        alert('✅ Subscribed successfully! Refreshing...');
      } else {
        // Upgrade or downgrade
        const currentAmount = plans.find(p => p.id === subscription.planId)?.amount || 0;
        const newAmount = plans.find(p => p.id === planId)?.amount || 0;
        const endpoint = newAmount >= currentAmount ? '/api/subscriptions/upgrade' : '/api/subscriptions/downgrade';
        await api.post(endpoint, { planId });
        alert('✅ Plan changed successfully! Refreshing...');
      }
      await loadPlansAndSub();
    } catch (e) {
      console.error('Plan change failed', e);
      alert('❌ Plan change failed: ' + (e.response?.data?.message || e.message));
    } finally {
      setSubscribingId(null);
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
        <h1 className="text-2xl font-bold">Subscription Plans</h1>
        <p className="text-slate-400 text-xs mt-1">Upgrade or modify your workspace plan limits</p>
      </div>

      <div className="grid md:grid-cols-3 gap-8 mt-12">
        {plans.map((plan) => {
          const isCurrent = subscription?.planId === plan.id;
          
          return (
            <div
              key={plan.id}
              className={`bg-white dark:bg-slate-900 border rounded-2xl p-8 flex flex-col justify-between relative transition-all duration-300 ${
                isCurrent 
                  ? 'border-indigo-600 shadow-xl shadow-indigo-600/5 ring-1 ring-indigo-500' 
                  : 'border-slate-200 dark:border-slate-800'
              }`}
            >
              {isCurrent && (
                <span className="absolute -top-3 left-8 bg-indigo-600 text-white font-bold text-[9px] uppercase tracking-wider px-3 py-1 rounded-full flex items-center gap-1 shadow-md">
                  <Sparkles size={10} /> Active Subscription
                </span>
              )}

              <div>
                <h3 className="text-lg font-bold">{plan.name}</h3>
                <p className="text-slate-400 text-xs mt-2 leading-relaxed">{plan.description}</p>

                <div className="my-6 flex items-baseline gap-1">
                  <span className="text-3xl font-extrabold">${plan.amount}</span>
                  <span className="text-slate-400 text-xs capitalize">/{plan.billingInterval}</span>
                </div>

                <div className="border-t border-slate-100 dark:border-slate-800 my-6" />

                <ul className="space-y-3.5">
                  {plan.features.map((feat) => (
                    <li key={feat.featureKey} className="flex items-start gap-2.5 text-xs text-slate-600 dark:text-slate-300 font-semibold">
                      <Check size={14} className="text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>
                        {feat.featureKey.replace('_', ' ')}:{' '}
                        <strong className="text-slate-800 dark:text-white capitalize">
                          {feat.featureValue === '999999' ? 'Unlimited' : feat.featureValue}
                        </strong>
                      </span>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="mt-8">
                <button
                  onClick={() => handleSubscribe(plan.id)}
                  disabled={isCurrent || subscribingId !== null}
                  className={`w-full py-3 px-4 rounded-xl text-xs font-bold uppercase tracking-wider transition-all flex items-center justify-center gap-2 ${
                    isCurrent
                      ? 'bg-slate-100 dark:bg-slate-800 text-slate-400 cursor-default'
                      : 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-md shadow-indigo-600/10'
                  }`}
                >
                  <CreditCard size={14} />
                  {subscribingId === plan.id ? 'Redirecting...' : isCurrent ? 'Active Plan' : 'Subscribe / Modify'}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Plans;
