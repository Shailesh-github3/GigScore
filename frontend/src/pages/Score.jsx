import { useEffect, useState } from "react";
import { getScore, getScoreConfig, getScoreHistory } from "../services/scoreService";
import { getUserDashboard } from "../services/userService";
import ScoreHistoryChart from "../components/ScoreHistoryChart";

const formatPoints = (value) => Number(value || 0).toFixed(2);

function Score({ userId }) {
  const [score, setScore] = useState(null);
  const [config, setConfig] = useState(null);
  const [history, setHistory] = useState([]);
  const [explanation, setExplanation] = useState(null);
  const [metrics, setMetrics] = useState({
    totalEarnings: 0,
    jobsCompleted: 0,
    avgRating: 0,
    activeDays: 0,
  });
  const [status, setStatus] = useState({ loading: true, error: "" });

  useEffect(() => {
    const fetchScore = async () => {
      setStatus({ loading: true, error: "" });
      try {
        const [scoreResponse, scoreConfigResponse, dashboardResponse, historyResponse] = await Promise.all([
          getScore(userId),
          getScoreConfig(),
          getUserDashboard(userId),
          getScoreHistory(userId),
        ]);

        const nextMetrics = {
          totalEarnings: Number(dashboardResponse.totalEarnings || 0),
          jobsCompleted: Number(dashboardResponse.jobsCompleted || 0),
          avgRating: Number(dashboardResponse.avgRating || 0),
          activeDays: Number(dashboardResponse.activeDays || 0),
        };

        setScore(Number(scoreResponse.score));
        setConfig(scoreConfigResponse);
        setMetrics(nextMetrics);
        setHistory(Array.isArray(historyResponse) ? historyResponse : []);
        setExplanation({
          components: scoreResponse.components || null,
          band: scoreResponse.band || null,
          suggestions: Array.isArray(scoreResponse.suggestions) ? scoreResponse.suggestions : [],
        });
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

  const components = explanation?.components || {};

  const contributionRows = config
    ? [
        {
          label: "Earnings",
          formula: `min(${metrics.totalEarnings.toFixed(2)} / ${config.earningsTarget}, 1)^${config.exponents.earnings} x ${config.weights.earnings * 100}`,
          value: components.earnings,
        },
        {
          label: "Jobs",
          formula: `min(${metrics.jobsCompleted} / ${config.jobsTarget}, 1)^${config.exponents.jobs} x ${config.weights.jobs * 100}`,
          value: components.jobs,
        },
        {
          label: "Rating",
          formula: `min(${metrics.avgRating.toFixed(2)} / ${config.ratingMax}, 1)^${config.exponents.rating} x ${config.weights.rating * 100}`,
          value: components.rating,
        },
        {
          label: "Active Days",
          formula: `min(${metrics.activeDays} / ${config.activeDaysTarget}, 1)^${config.exponents.activeDays} x ${config.weights.activeDays * 100}`,
          value: components.activeDays,
        },
      ]
    : [];

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
          <p className="mt-4 text-xs uppercase tracking-[0.16em] text-slate-500">Out of {config?.maxScore}</p>
          {explanation?.band ? (
            <p className="mt-3 inline-block bg-[var(--color-surface-muted)] px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-700 dark:text-slate-200">
              Band: {explanation.band}
            </p>
          ) : null}
        </div>
      </div>

      {!status.loading && !status.error ? <ScoreHistoryChart history={history} currentScore={score} /> : null}

      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200 md:p-7">
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Score Breakdown</h2>
        <p className="mt-2 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          Score = (Earnings / EarningsMax)^a x 35 + (Jobs / JobsMax)^b x 25 + (Rating / RatingMax)^c x 30 +
          (ActiveDays / ActiveDaysMax)^d x 10.
          We use a={config?.exponents.earnings}, b={config?.exponents.jobs}, c={config?.exponents.rating},
          d={config?.exponents.activeDays}. Ratios are capped at 1 before power weighting.
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
            <p className="font-semibold text-slate-700 dark:text-slate-200">Total (server)</p>
            <p className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">{formatPoints(score)}</p>
          </div>
        </div>
      </div>

      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200">
        <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">What to improve</h2>
        {explanation?.suggestions?.length ? (
          <ul className="mt-3 list-disc space-y-2 pl-5 text-sm text-slate-500 dark:text-slate-400">
            {explanation.suggestions.map((suggestion) => (
              <li key={suggestion}>{suggestion}</li>
            ))}
          </ul>
        ) : (
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Score details are loading — improvement factors will appear here.
          </p>
        )}
      </div>
    </section>
  );
}

export default Score;
