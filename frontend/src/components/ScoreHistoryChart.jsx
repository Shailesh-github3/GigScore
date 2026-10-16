import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { calculateScoreContributions, calculateTotalScore } from "../utils/scoreFormula";

const scoreFromAggregates = (totalEarnings, jobsCompleted, avgRating, activeDays) => {
  const contributions = calculateScoreContributions({ totalEarnings, jobsCompleted, avgRating, activeDays });
  return calculateTotalScore(contributions);
};

const buildScoreHistory = (activities = [], currentScore = 0) => {
  const sorted = [...activities].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

  if (!sorted.length) {
    const safeCurrentScore = Number(currentScore || 0);
    return [
      {
        interval: "T0",
        score: 0,
        timeLabel: "Start",
        isLatest: false,
      },
      {
        interval: "T1",
        score: safeCurrentScore,
        timeLabel: "Current",
        isLatest: true,
      },
    ];
  }

  let totalEarnings = 0;
  let jobsCompleted = 0;
  let ratingSum = 0;
  const activeDays = new Set();

  const points = sorted.map((activity, index) => {
    const amount = Number(activity.amount || 0);
    const rating = Number(activity.rating || 0);
    const date = activity.timestamp ? new Date(activity.timestamp) : null;

    totalEarnings += amount;
    jobsCompleted += 1;
    ratingSum += rating;

    if (date && !Number.isNaN(date.valueOf())) {
      activeDays.add(date.toISOString().slice(0, 10));
    }

    const avgRating = jobsCompleted > 0 ? ratingSum / jobsCompleted : 0;
    const score = scoreFromAggregates(totalEarnings, jobsCompleted, avgRating, activeDays.size);

    return {
      interval: `T${index + 1}`,
      score: Number(score.toFixed(2)),
      timeLabel:
        date && !Number.isNaN(date.valueOf())
          ? date.toLocaleString([], {
              month: "short",
              day: "2-digit",
              hour: "2-digit",
              minute: "2-digit",
            })
          : `Point ${index + 1}`,
      isLatest: index === sorted.length - 1,
    };
  });

  if (points.length === 1) {
    const firstScore = Number(points[0].score || 0);
    return [
      {
        interval: "T0",
        score: Math.max(firstScore - 5, 0),
        timeLabel: "Start",
        isLatest: false,
      },
      points[0],
    ];
  }

  return points;
};

function ScoreHistoryChart({ activities, score }) {
  const chartData = buildScoreHistory(activities, score);
  const isDarkMode =
    typeof document !== "undefined" && document.documentElement.classList.contains("dark");

  const tickColor = isDarkMode ? "#cbd5e1" : "#1e293b";
  const legendColor = isDarkMode ? "#e5e7eb" : "#0f172a";
  const dotStroke = isDarkMode ? "#d1d5db" : "#334155";
  const dotFill = isDarkMode ? "#818cf8" : "#4f46e5";
  const dotOuter = isDarkMode ? "#1b1b1b" : "#ffffff";
  const tooltipStyle = {
    borderRadius: "12px",
    border: "1px solid var(--color-tooltip-border)",
    background: "var(--color-tooltip-bg)",
    color: isDarkMode ? "#f3f4f6" : "#0b1220",
  };

  return (
    <div className="border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-all duration-300">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">Growth Trajectory</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">Progression across time intervals</p>
        </div>
        <p className="rounded-full border border-slate-300 bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
          Latest: {chartData[chartData.length - 1].score}
        </p>
      </div>

      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="scoreStroke" x1="0" y1="0" x2="1" y2="0">
                <stop offset="0%" stopColor="var(--color-chart-line-start)" />
                <stop offset="100%" stopColor="var(--color-chart-line-end)" />
              </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-chart-grid)" strokeOpacity={0.65} />
            <XAxis dataKey="interval" tick={{ fill: tickColor, fontSize: 12, fontWeight: 600 }} axisLine={false} tickLine={false} />
            <YAxis
              tick={{ fill: tickColor, fontSize: 12, fontWeight: 600 }}
              axisLine={false}
              tickLine={false}
              domain={["dataMin - 4", "dataMax + 4"]}
            />
            <Tooltip
              contentStyle={tooltipStyle}
              itemStyle={{ color: isDarkMode ? "#f3f4f6" : "#0b1220", fontWeight: 600 }}
              labelStyle={{ color: isDarkMode ? "#e5e7eb" : "#0b1220", fontWeight: 600 }}
              formatter={(value) => [`${value} pts`, "Score"]}
              labelFormatter={(_, payload) => payload?.[0]?.payload?.timeLabel || "Time"}
            />
            <Legend
              verticalAlign="top"
              height={28}
              wrapperStyle={{ color: legendColor, fontWeight: 700 }}
              formatter={(value) => <span style={{ color: legendColor, fontWeight: 700 }}>{value}</span>}
            />
            <Line
              type="monotone"
              dataKey="score"
              name="GigScore"
              stroke="url(#scoreStroke)"
              strokeWidth={3.5}
              animationDuration={900}
              animationEasing="ease-in-out"
              activeDot={{ r: 7, stroke: dotStroke, strokeWidth: 2, fill: dotOuter }}
              dot={({ cx, cy, payload }) => {
                if (!payload?.isLatest) return null;
                return (
                  <circle cx={cx} cy={cy} r={6} fill={dotFill} stroke={dotOuter} strokeWidth={2.5} />
                );
              }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default ScoreHistoryChart;
