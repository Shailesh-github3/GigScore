import { useEffect, useState } from "react";
import { getScore, getScoreHistory, getScoreConfig } from "../services/scoreService";
import { getUserDashboard } from "../services/userService";
import { calculateScoreContributions, getRiskClassification } from "../utils/scoreFormula";
import LoanOffers from "../components/LoanOffers";
import AiChatWidget from "../components/AiChatWidget";
import GoalTracker from "../components/GoalTracker";
import ScoreHistoryChart from "../components/ScoreHistoryChart";
import ScoreSimulator from "../components/ScoreSimulator";
import SystemTrustLayer from "../components/SystemTrustLayer";

const formatPoints = (value) => Number(value || 0).toFixed(1);

function Score({ userId }) {
  const [scoreData, setScoreData] = useState(null);
  const [scoreHistory, setScoreHistory] = useState([]);
  const [scoreConfig, setScoreConfig] = useState(null);
  const [metrics, setMetrics] = useState({
    totalEarnings: 0,
    jobsCompleted: 0,
    activeDays: 0,
    avgRating: 0,
  });
  const [status, setStatus] = useState({ loading: true, error: "" });

  useEffect(() => {
    const fetchScore = async () => {
      setStatus({ loading: true, error: "" });
      try {
        const [scoreResponse, dashboardResponse, historyResponse, configResponse] = await Promise.all([
          getScore(userId),
          getUserDashboard(userId),
          getScoreHistory(userId).catch(() => []),
          getScoreConfig().catch(() => null),
        ]);

        const historyList = Array.isArray(historyResponse) ? historyResponse : [];
        setScoreHistory(historyList);
        setScoreConfig(configResponse);

        const nextMetrics = {
          totalEarnings: Number(dashboardResponse.totalEarnings || 0),
          jobsCompleted: Number(dashboardResponse.totalGigs || dashboardResponse.jobsCompleted || 0),
          activeDays: Number(dashboardResponse.activeDays || 0),
          avgRating: Number(dashboardResponse.avgRating || 0),
        };

        setScoreData(scoreResponse);
        setMetrics(nextMetrics);
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

  if (status.loading) return <div className="p-12 text-center text-slate-500">Evaluating credit profile...</div>;
  if (status.error) return <div className="p-12 text-center text-red-500">{status.error}</div>;

  const currentScore = Number(scoreData?.score ?? 0);
  const bands = scoreConfig?.bands;
  const riskProfile = getRiskClassification(currentScore, bands);

  // Components from authoritative backend or fallback to formula
  const calculatedComponents = calculateScoreContributions(metrics);
  const components = scoreData?.components || calculatedComponents;

  const impactBars = [
    { label: "Earnings Stability", value: components.earnings || 0, max: 35, color: "bg-emerald-500", desc: "Target: $5,000 (35 pts)" },
    { label: "Job Completion Volume", value: components.jobs || 0, max: 25, color: "bg-blue-500", desc: "Target: 100 jobs (25 pts)" },
    { label: "Customer Ratings", value: components.rating || 0, max: 30, color: "bg-purple-500", desc: "Max: 5.0 ★ (30 pts)" },
    { label: "Consistency & Active Days", value: components.activeDays || components.consistency || 0, max: 10, color: "bg-amber-500", desc: "Target: 30 days (10 pts)" },
  ];

  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-slate-950">
      
      {/* 🟦 ZONE 1: Identity + Score (Authoritative Backend 0–100 Scale) */}
      <section className="bg-white px-4 py-12 border-b border-slate-200 dark:bg-slate-900 dark:border-slate-800">
        <div className="mx-auto max-w-4xl text-center">
          <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500 mb-6">Authoritative GigScore Index</p>
          
          <div className="relative inline-flex items-center justify-center h-48 w-48 rounded-full border-8 border-slate-900 dark:border-slate-100 shadow-xl">
            <div className="flex flex-col items-center">
              <span className="text-xs font-semibold uppercase tracking-widest text-slate-500">GigScore</span>
              <span className="text-6xl font-black tracking-tighter text-slate-900 dark:text-slate-100 mt-1">{currentScore.toFixed(0)}</span>
              <span className="text-xs font-medium text-slate-400 mt-0.5">/ 100</span>
            </div>
          </div>
          
          <div className="mt-6 flex items-center justify-center gap-3">
            <span className="inline-block rounded-full bg-slate-100 px-4 py-1.5 text-sm font-bold text-slate-800 dark:bg-slate-800 dark:text-slate-200">
              Tier: {scoreData?.band || riskProfile.label}
            </span>
            <span className="inline-block rounded-full bg-emerald-100 px-4 py-1.5 text-sm font-bold text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300">
              Risk: {riskProfile.risk}
            </span>
          </div>

          <SystemTrustLayer />
        </div>
      </section>

      {/* 🟨 ZONE 2: Explanation + Decision Traceability */}
      <section className="bg-slate-50 px-4 py-16 border-b border-slate-200 dark:bg-slate-950/50 dark:border-slate-800">
        <div className="mx-auto max-w-5xl">
          <div className="text-center mb-10">
             <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500 mb-2">Explanation Layer</p>
             <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Why This Score?</h2>
             <p className="mt-2 text-sm text-slate-500">Transparent mathematical decomposition of your credit standing.</p>
          </div>

          <div className="grid gap-8 md:grid-cols-2 mb-12">
            <div className="border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
              <h3 className="text-sm font-bold uppercase tracking-widest text-slate-500 mb-6">Weighted Factor Contributions</h3>
              <div className="space-y-6">
                {impactBars.map((bar) => {
                  const widthPercent = Math.min((bar.value / bar.max) * 100, 100);
                  return (
                    <div key={bar.label} className="relative">
                      <div className="flex items-end justify-between mb-1">
                        <span className="font-semibold text-slate-900 dark:text-slate-100">{bar.label}</span>
                        <span className="font-mono text-sm font-bold text-slate-700 dark:text-slate-300">
                          +{formatPoints(bar.value)} <span className="text-xs text-slate-400">/ {bar.max}</span>
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-400 mb-2">{bar.desc}</p>
                      <div className="h-2.5 w-full rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                        <div className={`h-full ${bar.color} transition-all duration-1000 ease-out`} style={{ width: `${widthPercent}%` }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="flex flex-col">
              <h3 className="text-sm font-bold uppercase tracking-widest text-slate-500 mb-6 px-2">Decision Traceability</h3>
              <LoanOffers score={currentScore} bands={bands} />
            </div>
          </div>

          {/* Authoritative Score History Trajectory from backend audit log */}
          <div className="mt-8">
            <ScoreHistoryChart history={scoreHistory} score={currentScore} />
          </div>
        </div>
      </section>

      {/* 🟩 ZONE 3: Growth System & What-If Simulation */}
      <section className="bg-indigo-50/50 px-4 py-16 dark:bg-indigo-950/10">
        <div className="mx-auto max-w-5xl">
          <div className="text-center mb-10">
             <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-indigo-500 mb-2">Growth System</p>
             <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Unlock Better Credit</h2>
             <p className="mt-2 text-sm text-slate-500">Interactive simulations, goal setting, and personalized AI coaching.</p>
          </div>

          {/* Score Simulator (What-If Analysis) */}
          <div className="mb-12">
            <ScoreSimulator initialMetrics={metrics} />
          </div>

          <div className="grid gap-8 md:grid-cols-2">
            <GoalTracker userId={userId} currentScore={currentScore} />
            <AiChatWidget userId={userId} />
          </div>
        </div>
      </section>

    </div>
  );
}

export default Score;
