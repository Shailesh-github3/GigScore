import { useState } from "react";
import httpClient from "../services/httpClient";

function GoalTracker({ userId, currentScore }) {
  const [isSetting, setIsSetting] = useState(false);
  const [targetScore, setTargetScore] = useState(740);
  const [loading, setLoading] = useState(false);
  const [activeGoal, setActiveGoal] = useState(null);

  const handleSetGoal = async () => {
    setLoading(true);
    try {
      const response = await httpClient.post(`/api/v1/coach/set-goal?userId=${userId}`, { targetScore });
      setActiveGoal(response.data);
      setIsSetting(false);
    } catch (err) {
      console.error(err);
      alert("Failed to set goal.");
    } finally {
      setLoading(false);
    }
  };

  if (activeGoal) {
    const progress = Math.min(100, Math.max(0, ((currentScore - 300) / (activeGoal.targetScore - 300)) * 100));

    return (
      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 md:p-8 transition-colors duration-200">
        <div className="flex items-center gap-3 mb-6">
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 dark:bg-emerald-900/50 dark:text-emerald-400 font-bold">
            🚀
          </span>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Financial Transformation</h2>
        </div>
        
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-6 dark:border-slate-800 dark:bg-slate-900/50">
          <div className="grid gap-6 md:grid-cols-2">
            <div>
              <p className="text-xs font-bold uppercase tracking-widest text-slate-500 mb-1">Current State</p>
              <p className="text-xl font-bold text-slate-900 dark:text-slate-100">{currentScore}</p>
              <p className="text-sm text-slate-600 dark:text-slate-400">Limited Access</p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-widest text-emerald-600 dark:text-emerald-400 mb-1">Target State</p>
              <p className="text-xl font-bold text-slate-900 dark:text-slate-100">{activeGoal.targetScore}</p>
              <p className="text-sm text-slate-600 dark:text-slate-400">Eligible for ₹2.5L Loan</p>
            </div>
          </div>

          <div className="mt-8">
            <div className="flex justify-between text-xs font-bold text-slate-500 mb-2">
              <span>Progress</span>
              <span>{Math.round(progress)}%</span>
            </div>
            <div className="h-4 w-full rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
              <div 
                className="h-full bg-emerald-500 transition-all duration-1000 ease-out" 
                style={{ width: `${progress}%` }} 
              /> 
            </div>
          </div>
        </div>

        <div className="mt-6 flex items-center justify-between">
          <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800 dark:bg-emerald-900/50 dark:text-emerald-400">
            Timeline: {activeGoal.estimatedTimeline}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 md:p-8 transition-colors duration-200">
      <div className="flex items-center gap-3 mb-4">
        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 dark:bg-emerald-900/50 dark:text-emerald-400 font-bold">
          🎯
        </span>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Set Growth Target</h2>
      </div>
      <p className="mb-6 text-sm text-slate-600 dark:text-slate-400 max-w-md">
        Visualize your path to higher credit limits. We'll track your transformation journey.
      </p>

      {isSetting ? (
        <div className="flex items-end gap-4">
          <div className="flex flex-col">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">Target Score</label>
            <input 
              type="number" 
              value={targetScore} 
              onChange={e => setTargetScore(Number(e.target.value))}
              className="rounded-lg border border-slate-300 px-4 py-2 text-slate-900 outline-none focus:border-emerald-500 dark:bg-slate-900 dark:text-white dark:border-slate-700 dark:focus:border-emerald-500"
            />
          </div>
          <button 
            onClick={handleSetGoal}
            disabled={loading}
            className="rounded-lg bg-emerald-600 px-6 py-2.5 font-semibold text-white transition hover:bg-emerald-700 disabled:opacity-50"
          >
            {loading ? "Activating..." : "Lock in Goal"}
          </button>
        </div>
      ) : (
        <button 
          onClick={() => setIsSetting(true)}
          className="rounded-lg bg-emerald-600 px-6 py-2.5 font-semibold text-white transition hover:bg-emerald-700"
        >
          Create Transformation Goal
        </button>
      )}
    </div>
  );
}

export default GoalTracker;
