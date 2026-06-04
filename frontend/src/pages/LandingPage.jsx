import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Check, ArrowRight, Shield, Zap, Sparkles, Star } from 'lucide-react';

const LandingPage = () => {
  const [billingCycle, setBillingCycle] = useState('monthly');

  const plans = [
    {
      name: 'Basic Plan',
      monthlyPrice: 19,
      yearlyPrice: 15,
      description: 'Ideal for individuals and small team start-ups',
      features: [
        'Up to 5 active users',
        '10GB cloud storage space',
        '10,000 monthly API calls',
        'Basic reporting & analytics',
        'Email customer support',
      ],
      cta: 'Start 14-Day Free Trial',
      popular: false,
    },
    {
      name: 'Pro Plan',
      monthlyPrice: 49,
      yearlyPrice: 39,
      description: 'Advanced capabilities for growing product scale',
      features: [
        'Up to 50 active users',
        '100GB cloud storage space',
        '100,000 monthly API calls',
        'Advanced revenue dashboards',
        'Priority ticket support',
        'Custom webhook notifications',
      ],
      cta: 'Start 14-Day Free Trial',
      popular: true,
    },
    {
      name: 'Enterprise Plan',
      monthlyPrice: 299,
      yearlyPrice: 239,
      description: 'Maximum parameters and compliance for enterprises',
      features: [
        'Unlimited team members',
        '10TB dedicated storage',
        '10,000,000 monthly API calls',
        'Custom domain integration',
        'Dedicated account director',
        'Custom SLA contracts',
      ],
      cta: 'Contact Sales / Subscribe',
      popular: false,
    },
  ];

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 selection:bg-indigo-500 selection:text-white">
      {/* Navbar */}
      <header className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 bg-indigo-600 rounded-lg flex items-center justify-center font-bold text-xl text-white shadow-lg shadow-indigo-500/30">
            S
          </div>
          <span className="font-bold text-lg leading-tight tracking-wider">SaaS Billing</span>
        </div>
        <div className="flex items-center gap-4">
          <Link to="/login" className="text-sm font-semibold hover:text-indigo-400 transition-colors">
            Sign In
          </Link>
          <Link to="/register" className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-semibold shadow-md shadow-indigo-600/20 transition-all">
            Get Started
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <section className="max-w-5xl mx-auto px-6 pt-24 pb-16 text-center space-y-8">
        <div className="inline-flex items-center gap-2 bg-indigo-950/50 border border-indigo-500/20 text-indigo-400 px-4 py-1.5 rounded-full text-xs font-semibold uppercase tracking-wider">
          <Sparkles size={12} /> Live Stripe Sandbox Sandbox Included
        </div>
        
        <h1 className="text-5xl md:text-6xl font-bold tracking-tight leading-tight max-w-4xl mx-auto">
          Production Subscription Management <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-indigo-600">Simplified</span>
        </h1>
        
        <p className="text-lg text-slate-400 max-w-2xl mx-auto leading-relaxed">
          Manage subscriptions, track features limit, compute local GST rules, and monitor MRR/ARR analytics within our developer-tool dashboard interface.
        </p>

        <div className="flex justify-center gap-4 pt-4">
          <Link to="/register" className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3.5 rounded-xl font-bold text-base shadow-lg shadow-indigo-600/30 flex items-center gap-2 transition-all">
            Deploy Free Trial <ArrowRight size={18} />
          </Link>
        </div>
      </section>

      {/* Pricing Header */}
      <section className="max-w-7xl mx-auto px-6 py-12 text-center">
        <h2 className="text-3xl font-bold tracking-tight">Flexible Pricing Packages</h2>
        <p className="text-slate-400 mt-2 text-sm">Choose the tier that matches your product resource constraints</p>

        {/* Cycle Toggle */}
        <div className="flex justify-center items-center gap-3 mt-8">
          <span className={`text-sm font-semibold ${billingCycle === 'monthly' ? 'text-white' : 'text-slate-500'}`}>Monthly</span>
          <button
            onClick={() => setBillingCycle(billingCycle === 'monthly' ? 'yearly' : 'monthly')}
            className="w-12 h-6 bg-slate-800 rounded-full p-1 relative flex items-center cursor-pointer transition-all border border-slate-700"
          >
            <div className={`h-4 w-4 bg-indigo-500 rounded-full transition-transform transform ${billingCycle === 'yearly' ? 'translate-x-6' : 'translate-x-0'}`} />
          </button>
          <span className={`text-sm font-semibold ${billingCycle === 'yearly' ? 'text-white' : 'text-slate-500'}`}>
            Yearly <span className="text-[10px] bg-indigo-950 text-indigo-400 px-2 py-0.5 rounded-full font-bold border border-indigo-500/20">Save 20%</span>
          </span>
        </div>

        {/* Pricing Cards Grid */}
        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto mt-16 text-left">
          {plans.map((plan) => (
            <div
              key={plan.name}
              className={`rounded-2xl border bg-slate-900/50 p-8 flex flex-col justify-between relative transition-all duration-300 hover:-translate-y-1 ${
                plan.popular 
                  ? 'border-indigo-500 shadow-xl shadow-indigo-500/5 glow-indigo' 
                  : 'border-slate-800'
              }`}
            >
              {plan.popular && (
                <span className="absolute -top-3 left-8 bg-indigo-600 text-white font-bold text-[10px] uppercase tracking-wider px-3 py-1 rounded-full flex items-center gap-1.5 shadow-md">
                  <Star size={10} fill="white" /> Most Popular
                </span>
              )}

              <div>
                <h3 className="text-xl font-bold">{plan.name}</h3>
                <p className="text-slate-400 text-xs mt-2 leading-relaxed">{plan.description}</p>

                <div className="my-6 flex items-baseline gap-1">
                  <span className="text-4xl font-extrabold text-white">
                    ${billingCycle === 'monthly' ? plan.monthlyPrice : plan.yearlyPrice}
                  </span>
                  <span className="text-slate-400 text-sm">/month</span>
                </div>

                <div className="border-t border-slate-800 my-6" />

                <ul className="space-y-4">
                  {plan.features.map((feat) => (
                    <li key={feat} className="flex items-start gap-2.5 text-slate-300 text-xs font-medium">
                      <Check size={14} className="text-indigo-400 mt-0.5 flex-shrink-0" />
                      {feat}
                    </li>
                  ))}
                </ul>
              </div>

              <div className="mt-8">
                <Link
                  to="/register"
                  className={`w-full py-3 px-4 rounded-xl font-bold text-center block text-xs tracking-wider uppercase transition-all ${
                    plan.popular
                      ? 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-lg shadow-indigo-600/25'
                      : 'bg-slate-800 hover:bg-slate-700 text-white'
                  }`}
                >
                  {plan.cta}
                </Link>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="max-w-7xl mx-auto px-6 py-12 border-t border-slate-800 mt-24 text-center text-xs text-slate-500">
        <p>&copy; {new Date().getFullYear()} SaaS Billing Platform Inc. Fully compliant sandbox testing environment.</p>
      </footer>
    </div>
  );
};

export default LandingPage;
