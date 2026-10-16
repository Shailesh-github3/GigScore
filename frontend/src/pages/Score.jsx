import { useEffect, useState } from "react";
import { getScore } from "../services/scoreService";
import { getUserDashboard } from "../services/userService";
import { getRecentActivities } from "../services/activityService";
import { calculateScoreContributions } from "../utils/scoreFormula";
import LoanOffers from "../components/LoanOffers";
import AiChatWidget from "../components/AiChatWidget";
import GoalTracker from "../components/GoalTracker";
import SystemTrustLayer from "../components/SystemTrustLayer";

const formatPoints = (value) => Number(value || 0).toFixed(0);

const getRiskLabel = (score) => {
  if (score >= 800) return "Excellent";
  if (score >= 700) return "Low Risk";
  if (score >= 600) return "Moderate Risk";
  return "High Risk";
};

function Score({ userId }) {
  const [score, setScore] = useState(null);
  const [metrics, setMetrics] = useState({
    totalEarnings: 0,
    activeDays: 0,
    avgRating: 0,
    platformsUsed: 1,
  });
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

        const activitiesList = Array.isArray(activityResponse) ? activityResponse : [];
        const uniquePlatforms = new Set(activitiesList.map((a) => a.platform).filter(Boolean));
        const platformsUsed = uniquePlatforms.size || 1;

        const nextMetrics = {
          totalEarnings: Number(dashboardResponse.totalEarnings || 0),
          activeDays: Number(dashboardResponse.activeDays || 0),
          avgRating: Number(dashboardResponse.avgRating || 0),
          platformsUsed: platformsUsed,
        };

        setScore(Number(scoreResponse.score));
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

  const contributions = calculateScoreContributions(metrics);

  const impactBars = [
    { label: "Earnings Stability", value: contributions.earnings, max: 40, color: "bg-emerald-500" },
    { label: "Consistency", value: contributions.consistency, max: 30, color: "bg-blue-500" },
    { label: "Ratings", value: contributions.rating, max: 20, color: "bg-purple-500" },
    { label: "Platform Diversity", value: contributions.platforms, max: 10, color: "bg-amber-500" },
  ];

  if (status.loading) return <div className="p-12 text-center text-slate-500">Evaluating profile...</div>;
  if (status.error) return <div className="p-12 text-center text-red-500">{status.error}</div>;

  return (
    <div className="flex flex-col min-h-screen bg-slate-50 dark:bg-slate-950">
      
      {/* 🟦 ZONE 1: Identity + Score (Message: "What am I?") */}
      <section className="bg-white px-4 py-12 border-b border-slate-200 dark:bg-slate-900 dark:border-slate-800">
        <div className="mx-auto max-w-4xl text-center">
          <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500 mb-6">Identity Layer</p>
          
          <div className="relative inline-flex items-center justify-center h-48 w-48 rounded-full border-8 border-slate-900 dark:border-slate-100 shadow-xl">
            <div className="flex flex-col items-center">
              <span className="text-sm font-semibold uppercase tracking-widest text-slate-500">GigScore</span>
              <span className="text-6xl font-black tracking-tighter text-slate-900 dark:text-slate-100 mt-1">{score}</span>
            </div>
          </div>
          
          <div className="mt-6">
            <span className="inline-block rounded-full bg-slate-100 px-4 py-1 text-sm font-bold text-slate-800 dark:bg-slate-800 dark:text-slate-200">
              {getRiskLabel(score)} Profile
            </span>
          </div>

          <SystemTrustLayer />
        </div>
      </section>

      {/* 🟨 ZONE 2: Explanation + Decision (Message: "Why is this my outcome?") */}
      <section className="bg-slate-50 px-4 py-16 border-b border-slate-200 dark:bg-slate-950/50 dark:border-slate-800">
        <div className="mx-auto max-w-4xl">
          <div className="text-center mb-10">
             <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500 mb-2">Explanation Layer</p>
             <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Why This Score?</h2>
          </div>

          <div className="grid gap-8 md:grid-cols-2 mb-12">
            <div className="border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
              <h3 className="text-sm font-bold uppercase tracking-widest text-slate-500 mb-6">Score Factors</h3>
              <div className="space-y-6">
                {impactBars.map((bar) => {
                  const widthPercent = Math.min((bar.value / bar.max) * 100, 100);
                  return (
                    <div key={bar.label} className="relative">
                      <div className="flex items-end justify-between mb-2">
                        <span className="font-semibold text-slate-900 dark:text-slate-100">{bar.label}</span>
                        <span className="font-mono text-sm font-bold text-slate-500">+{formatPoints(bar.value)}</span>
                      </div>
                      <div className="h-2 w-full rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                        <div className={`h-full ${bar.color} transition-all duration-1000 ease-out`} style={{ width: `${widthPercent}%` }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="flex flex-col">
              <h3 className="text-sm font-bold uppercase tracking-widest text-slate-500 mb-6 px-2">Decision Traceability</h3>
              <LoanOffers score={score} />
            </div>
          </div>
        </div>
      </section>

      {/* 🟩 ZONE 3: Growth System (Message: "How do I improve?") */}
      <section className="bg-indigo-50/50 px-4 py-16 dark:bg-indigo-950/10">
        <div className="mx-auto max-w-4xl">
          <div className="text-center mb-10">
             <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-indigo-500 mb-2">Growth System</p>
             <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Unlock Better Credit</h2>
          </div>

          <div className="grid gap-8">
            <AiChatWidget userId={userId} />
            <GoalTracker userId={userId} currentScore={score} />
          </div>
        </div>
      </section>

    </div>
  );
}

export default Score;
