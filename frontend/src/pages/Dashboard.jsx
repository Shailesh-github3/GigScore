import { useEffect, useState } from "react";
import StatCard from "../components/StatCard";
import { getUserDashboard } from "../services/userService";
import { getRecentActivities } from "../services/activityService";

const getRatingTone = (rating) => {
  if (rating >= 4.7) return "text-slate-900 dark:text-slate-100";
  if (rating >= 4.0) return "text-slate-700 dark:text-slate-300";
  return "text-slate-500 dark:text-slate-400";
};

const getAmountTone = (amount) => {
  if (amount >= 700) return "text-slate-900 dark:text-slate-100";
  if (amount >= 300) return "text-slate-700 dark:text-slate-300";
  return "text-slate-500 dark:text-slate-400";
};

function Dashboard({ userId }) {
  const [dashboard, setDashboard] = useState(null);
  const [activities, setActivities] = useState([]);
  const [status, setStatus] = useState({ loading: true, error: "" });

  useEffect(() => {
    const fetchDashboard = async () => {
      setStatus({ loading: true, error: "" });
      try {
        const [dashboardResponse, activityResponse] = await Promise.all([
          getUserDashboard(userId),
          getRecentActivities(userId),
        ]);

        setDashboard(dashboardResponse);
        setActivities(activityResponse);
        setStatus({ loading: false, error: "" });
      } catch (error) {
        setStatus({
          loading: false,
          error: error.response?.data?.message || "Unable to load dashboard metrics.",
        });
      }
    };

    fetchDashboard();
  }, [userId]);

  if (status.loading) {
    return <p className="text-sm text-slate-600 dark:text-slate-300">Loading dashboard...</p>;
  }

  if (status.error) {
    return <p className="rounded-lg bg-red-100 px-3 py-2 text-sm font-medium text-red-700">{status.error}</p>;
  }

  return (
    <section className="space-y-8">
      <div className="grid gap-6 border-b border-slate-200 pb-8 dark:border-slate-800 md:grid-cols-[minmax(0,1fr)_260px]">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">System Status: Optimal</p>
          <h1 className="mt-2 text-5xl font-black leading-[0.92] tracking-tight text-slate-900 dark:text-slate-100 md:text-6xl">
            Performance
            <br />
            Architecture.
          </h1>
        </div>
        <p className="self-start pt-1 text-right text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          A holistic view of your operational efficiency and revenue streams.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Total Earnings"
          value={`$${Number(dashboard.totalEarnings || 0).toFixed(2)}`}
          subtitle="Revenue"
          icon="+12%"
          tone="accent"
          priority="primary"
        />
        <StatCard
          title="Jobs Completed"
          value={dashboard.jobsCompleted || 0}
          subtitle="Execution"
          icon="Running"
          tone="success"
        />
        <StatCard
          title="Average Rating"
          value={Number(dashboard.avgRating || 0).toFixed(2)}
          subtitle="Quality"
          icon="Peak"
          tone="warning"
        />
        <StatCard
          title="Activity Days"
          value={dashboard.activeDays || 0}
          subtitle="Consistency"
          icon="Streak"
          tone="neutral"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_280px]">
      <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200 md:p-7">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Recent Activity</h2>
          <p className="text-[10px] uppercase tracking-[0.18em] text-slate-500">Latest events</p>
        </div>

        {activities.length === 0 ? (
          <p className="text-sm text-slate-500 dark:text-slate-400">No recent activity yet.</p>
        ) : (
          <ol className="relative space-y-4 border-l border-slate-200 pl-5 dark:border-slate-800">
            {activities.map((activity) => (
              <li
                key={activity.id}
                className="relative border border-[var(--color-border)] bg-[var(--color-surface-muted)] p-4 text-sm transition-colors duration-200"
              >
                <span className="absolute -left-[28px] top-5 h-3 w-3 rounded-full border-2 border-[var(--color-surface)] bg-slate-900 dark:bg-slate-100" />

                <div className="flex flex-wrap items-center justify-between gap-2">
                  <p className="font-semibold text-slate-900 dark:text-slate-100">
                    {activity.platform} - {activity.action}
                  </p>
                  <p className={`text-sm font-bold ${getAmountTone(Number(activity.amount || 0))}`}>
                    {activity.amount !== null && activity.amount !== undefined
                      ? `$${Number(activity.amount).toFixed(2)}`
                      : "N/A"}
                  </p>
                </div>

                <div className="mt-1 flex items-center justify-between gap-3">
                  <p className="text-slate-500 dark:text-slate-400">{new Date(activity.timestamp).toLocaleString()}</p>
                  <p className={`font-semibold ${getRatingTone(Number(activity.rating || 0))}`}>
                    Rating {activity.rating !== null && activity.rating !== undefined ? Number(activity.rating).toFixed(1) : "N/A"}
                  </p>
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>

      <aside className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200">
        <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Project Focus</p>
        <h3 className="mt-3 text-4xl font-black leading-none tracking-tight text-slate-900 dark:text-slate-100">Reliability</h3>
        <p className="mt-4 text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          Your current operating pattern indicates stable throughput and reliable completion trends.
        </p>
      </aside>
      </div>
    </section>
  );
}

export default Dashboard;
