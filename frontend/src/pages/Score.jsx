import { useEffect, useState } from "react";
import { getScore } from "../services/scoreService";
import { getUserDashboard } from "../services/userService";
import { getRecentActivities } from "../services/activityService";
import ScoreHistoryChart from "../components/ScoreHistoryChart";
import { calculateScoreContributions, calculateTotalScore, SCORE_CONFIG } from "../utils/scoreFormula";

const formatPoints = (value) => Number(value || 0).toFixed(2);

function Score({ userId }) {
  const [score, setScore] = useState(null);
  const [metrics, setMetrics] = useState({
    totalEarnings: 0,
    jobsCompleted: 0,
    avgRating: 0,
    activeDays: 0,
  });
  const [activities, setActivities] = useState([]);
  const [status, setStatus] = useState({ loading: true, error: "" });

  useEffect(() => {
    const fetchScore = async () => {
      setStatus({ loading: true, error: "" });
      try {
        const [scoreResponse, dashboardResponse, activityResponse] = await Promise.all([
          getScore(userId),
          getUserDashboard(userId),
          getRecentActivities(userId),
        ]);

        const nextMetrics = {
          totalEarnings: Number(dashboardResponse.totalEarnings || 0),
          jobsCompleted: Number(dashboardResponse.jobsCompleted || 0),
          avgRating: Number(dashboardResponse.avgRating || 0),
          activeDays: Number(dashboardResponse.activeDays || 0),
        };

        setScore(Number(scoreResponse.score));
        setMetrics(nextMetrics);
        setActivities(Array.isArray(activityResponse) ? activityResponse : []);
        setStatus({ loading: false, error: "" });
      } catch (error) {
        setStatus({
          loading: false,
          error: error.response?.data?.message || "Unable to fetch score from backend.",
        });
      }
    };

    fetchScore();
  }, [userId]);

  const contributions = calculateScoreContributions(metrics);
  const computedTotal = calculateTotalScore(contributions);

  const contributionRows = [
    {
      label: "Earnings",
      formula: `min(${metrics.totalEarnings.toFixed(2)} / ${SCORE_CONFIG.earningsTarget}, 1)^${SCORE_CONFIG.exponents.earnings} x ${SCORE_CONFIG.weights.earnings * 100}`,
      value: contributions.earnings,
    },
    {
      label: "Jobs",
      formula: `min(${metrics.jobsCompleted} / ${SCORE_CONFIG.jobsTarget}, 1)^${SCORE_CONFIG.exponents.jobs} x ${SCORE_CONFIG.weights.jobs * 100}`,
      value: contributions.jobs,
    },
    {
      label: "Rating",
      formula: `min(${metrics.avgRating.toFixed(2)} / ${SCORE_CONFIG.ratingMax}, 1)^${SCORE_CONFIG.exponents.rating} x ${SCORE_CONFIG.weights.rating * 100}`,
      value: contributions.rating,
    },
    {
      label: "Active Days",
      formula: `min(${metrics.activeDays} / ${SCORE_CONFIG.activeDaysTarget}, 1)^${SCORE_CONFIG.exponents.activeDays} x ${SCORE_CONFIG.weights.activeDays * 100}`,
      value: contributions.activeDays,
    },
  ];

  return (
    <section className="space-y-8">
      <div className="grid gap-6 border-b border-slate-200 pb-8 dark:border-slate-800 md:grid-cols-[minmax(0,1fr)_260px]">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Current Rating</p>
          <h1 className="mt-2 text-5xl font-black leading-[0.92] tracking-tight text-slate-900 dark:text-slate-100 md:text-6xl">
            Financial
            <br />
            Reliability.
          </h1>
        </div>
        <p className="self-start pt-1 text-right text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          Performance stability remains visible across recent earnings and consistency streaks.
        </p>
      </div>

      <div className="grid gap-6 border border-[var(--color-border)] bg-[var(--color-surface)] p-8 transition-colors duration-200 md:grid-cols-[minmax(0,1fr)_260px] md:p-10">
        <div>
          {status.loading ? <p className="text-slate-500 dark:text-slate-300">Loading score...</p> : null}

          {status.error ? (
            <p className="max-w-md border border-rose-300 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-300">
              {status.error}
            </p>
          ) : null}

          {!status.loading && !status.error ? (
            <>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">Current Score</p>
              <p className="mt-2 text-[140px] font-black leading-none tracking-tight text-slate-900 dark:text-slate-100 md:text-[170px]">{score}</p>
            </>
          ) : null}
        </div>

        <div className="border-l border-slate-200 pl-6 dark:border-slate-800">
          <p className="text-sm leading-relaxed text-slate-500 dark:text-slate-400">
            This score reflects weighted strength across earnings, completion consistency, quality signal, and sustained activity.
          </p>
          <p className="mt-4 text-xs uppercase tracking-[0.16em] text-slate-500">Out of {SCORE_CONFIG.maxScore}</p>
        </div>
      </div>

      {!status.loading && !status.error ? <ScoreHistoryChart activities={activities} score={score} /> : null}

      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200 md:p-7">
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Score Breakdown</h2>
        <p className="mt-2 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          Score = (Earnings / EarningsMax)^a x 35 + (Jobs / JobsMax)^b x 25 + (Rating / RatingMax)^c x 30 +
          (ActiveDays / ActiveDaysMax)^d x 10.
          We use a={SCORE_CONFIG.exponents.earnings}, b={SCORE_CONFIG.exponents.jobs}, c={SCORE_CONFIG.exponents.rating},
          d={SCORE_CONFIG.exponents.activeDays}. Ratios are capped at 1 before power weighting.
        </p>

        <div className="mt-5 border border-[var(--color-border)] bg-[var(--color-surface-muted)] p-4 transition-colors duration-200">
          <div className="grid grid-cols-12 border-b border-slate-200 pb-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500 dark:border-slate-800">
            <p className="col-span-2">Metric</p>
            <p className="col-span-8">Computation</p>
            <p className="col-span-2 text-right">Points</p>
          </div>

          <div className="divide-y divide-slate-200 dark:divide-slate-800">
            {contributionRows.map((row) => (
              <div key={row.label} className="grid grid-cols-12 items-center gap-2 py-3 text-sm">
                <p className="col-span-2 font-semibold text-slate-700 dark:text-slate-200">{row.label}</p>
                <p className="col-span-8 font-mono text-xs text-slate-500 dark:text-slate-400 md:text-sm">{row.formula}</p>
                <p className="col-span-2 text-right font-bold text-slate-900 dark:text-slate-100">+{formatPoints(row.value)}</p>
              </div>
            ))}
          </div>

          <div className="mt-3 flex items-center justify-between border-t border-slate-300 pt-3 text-base dark:border-slate-700">
            <p className="font-semibold text-slate-700 dark:text-slate-200">Computed Total</p>
            <p className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">{formatPoints(computedTotal)}</p>
          </div>
        </div>
      </div>

      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200">
        <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">AI Suggestions</h2>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
          {metrics.jobsCompleted < 3
            ? "Complete a few more deliveries this week to improve both jobs and consistency components."
            : "Keep your rating above 4.8 and grow earnings steadily to push your score toward 100."}
        </p>
      </div>
    </section>
  );
}

export default Score;
