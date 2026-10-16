import React from 'react';

function SystemTrustLayer() {
  return (
    <div className="mt-8 flex flex-col items-center justify-center space-y-3 border-t border-slate-200 pt-6 dark:border-slate-800">
      <div className="flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 dark:bg-slate-800">
        <span className="flex h-2 w-2 rounded-full bg-emerald-500"></span>
        <span className="text-[10px] font-bold uppercase tracking-widest text-slate-700 dark:text-slate-300">
          Confidence Level: HIGH (Rule-Based)
        </span>
      </div>
      
      <div className="flex flex-wrap items-center justify-center gap-4 text-xs font-semibold text-slate-500 dark:text-slate-400">
        <div className="flex items-center gap-1.5">
          <span className="text-emerald-500">✔</span>
          <span>3+ Connected Platforms</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="text-emerald-500">✔</span>
          <span>30+ Days Activity</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="text-emerald-500">✔</span>
          <span>Consistent Earnings History</span>
        </div>
      </div>
    </div>
  );
}

export default SystemTrustLayer;
