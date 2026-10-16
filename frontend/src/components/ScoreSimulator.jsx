import React, { useState, useEffect } from "react";
import { calculateScoreContributions, calculateTotalScore, SCORE_CONFIG } from "../utils/scoreFormula";

export default function ScoreSimulator({ initialMetrics }) {
  const [metrics, setMetrics] = useState({
    totalEarnings: Math.min(initialMetrics?.totalEarnings || 2500, 10000),
    activeDays: Math.min(initialMetrics?.activeDays || 15, 30),
    avgRating: initialMetrics?.avgRating || 4.0,
    platformsUsed: initialMetrics?.platformsUsed || 1,
  });

  const [simulatedScore, setSimulatedScore] = useState(0);

  useEffect(() => {
    const contributions = calculateScoreContributions(metrics);
    const score = calculateTotalScore(contributions);
    setSimulatedScore(score);
  }, [metrics]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setMetrics((prev) => ({
      ...prev,
      [name]: Number(value),
    }));
  };

  return (
    <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200 md:p-8">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Score Simulator</h2>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
            Adjust your metrics to see how they impact your final GigScore and credit eligibility.
          </p>
        </div>
        <div className="mt-4 md:mt-0 flex items-baseline gap-2 bg-slate-100 dark:bg-slate-800 px-4 py-2 rounded-lg">
          <span className="text-sm font-semibold uppercase tracking-wider text-slate-500">Projected Score</span>
          <span className="text-3xl font-black text-slate-900 dark:text-slate-100">{simulatedScore}</span>
        </div>
      </div>

      <div className="grid gap-8 md:grid-cols-2">
        {/* Earnings Slider */}
        <div className="space-y-3">
          <div className="flex justify-between items-end">
            <label htmlFor="totalEarnings" className="text-sm font-semibold text-slate-700 dark:text-slate-300">
              Earnings Stability
            </label>
            <span className="text-sm font-mono text-slate-500">${metrics.totalEarnings.toFixed(0)}</span>
          </div>
          <input
            type="range"
            id="totalEarnings"
            name="totalEarnings"
            min="0"
            max="10000"
            step="100"
            value={metrics.totalEarnings}
            onChange={handleChange}
            className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer dark:bg-slate-700 accent-slate-900 dark:accent-slate-100"
          />
          <p className="text-[10px] uppercase tracking-wider text-slate-400">Target: ${SCORE_CONFIG.earningsTarget}</p>
        </div>

        {/* Consistency Slider */}
        <div className="space-y-3">
          <div className="flex justify-between items-end">
            <label htmlFor="activeDays" className="text-sm font-semibold text-slate-700 dark:text-slate-300">
              Consistency (Active Days)
            </label>
            <span className="text-sm font-mono text-slate-500">{metrics.activeDays} days</span>
          </div>
          <input
            type="range"
            id="activeDays"
            name="activeDays"
            min="0"
            max="30"
            step="1"
            value={metrics.activeDays}
            onChange={handleChange}
            className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer dark:bg-slate-700 accent-slate-900 dark:accent-slate-100"
          />
          <p className="text-[10px] uppercase tracking-wider text-slate-400">Target: {SCORE_CONFIG.activeDaysTarget} days</p>
        </div>

        {/* Rating Slider */}
        <div className="space-y-3">
          <div className="flex justify-between items-end">
            <label htmlFor="avgRating" className="text-sm font-semibold text-slate-700 dark:text-slate-300">
              Average Rating
            </label>
            <span className="text-sm font-mono text-slate-500">{metrics.avgRating.toFixed(1)} ★</span>
          </div>
          <input
            type="range"
            id="avgRating"
            name="avgRating"
            min="1.0"
            max="5.0"
            step="0.1"
            value={metrics.avgRating}
            onChange={handleChange}
            className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer dark:bg-slate-700 accent-slate-900 dark:accent-slate-100"
          />
          <p className="text-[10px] uppercase tracking-wider text-slate-400">Max: {SCORE_CONFIG.ratingMax}.0</p>
        </div>

        {/* Platform Diversity Slider */}
        <div className="space-y-3">
          <div className="flex justify-between items-end">
            <label htmlFor="platformsUsed" className="text-sm font-semibold text-slate-700 dark:text-slate-300">
              Platform Diversity
            </label>
            <span className="text-sm font-mono text-slate-500">{metrics.platformsUsed}</span>
          </div>
          <input
            type="range"
            id="platformsUsed"
            name="platformsUsed"
            min="1"
            max="5"
            step="1"
            value={metrics.platformsUsed}
            onChange={handleChange}
            className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer dark:bg-slate-700 accent-slate-900 dark:accent-slate-100"
          />
          <p className="text-[10px] uppercase tracking-wider text-slate-400">Target: {SCORE_CONFIG.platformsTarget} platforms</p>
        </div>
      </div>
    </div>
  );
}
