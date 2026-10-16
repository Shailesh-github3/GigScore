import { useState } from "react";
import { analyzeCoach } from "../services/chatService";

const CATEGORIES = [
  { id: "improve_score", label: "📈 Improve my score" },
  { id: "increase_eligibility", label: "💰 Increase loan eligibility" },
  { id: "understand_drop", label: "📉 Understand score drop" },
  { id: "improve_consistency", label: "📅 Improve consistency" },
  { id: "earnings_analysis", label: "💵 Earnings analysis" },
];

function AiChatWidget({ userId = 1 }) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [response, setResponse] = useState(null);
  const [activeCategory, setActiveCategory] = useState(null);

  const handleCategoryClick = async (category) => {
    setActiveCategory(category);
    setIsLoading(true);
    setError("");
    setResponse(null);

    try {
      const data = await analyzeCoach(userId, category.id);
      setResponse(data);
    } catch (err) {
      setError("Failed to fetch AI insights. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const resetView = () => {
    setActiveCategory(null);
    setResponse(null);
    setError("");
  };

  return (
    <div className="border border-[var(--color-border)] bg-[var(--color-surface)] overflow-hidden transition-colors duration-200">
      <div className="flex items-center gap-3 border-b border-slate-200 bg-slate-50 px-6 py-4 dark:border-slate-800 dark:bg-slate-900/50">
        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-600 text-sm font-bold text-white shadow-sm">
          ✨
        </span>
        <div>
          <h2 className="text-lg font-bold tracking-tight text-slate-900 dark:text-slate-100">
            AI Financial Growth Coach
          </h2>
          <p className="text-[10px] font-bold uppercase tracking-widest text-slate-500">
            Advisory Layer
          </p>
        </div>
      </div>

      <div className="p-6 md:p-8">
        {!activeCategory && (
          <div className="space-y-4">
            <p className="text-sm font-medium text-slate-700 dark:text-slate-300 mb-6">
              Hi! I'm your Advisory Coach. What area would you like to explore?
            </p>
            <div className="grid gap-3 md:grid-cols-2">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => handleCategoryClick(cat)}
                  className="flex w-full items-center justify-between rounded-xl border border-slate-200 bg-white px-4 py-3 text-left text-sm font-semibold text-slate-700 shadow-sm transition hover:border-indigo-500 hover:text-indigo-600 dark:border-slate-800 dark:bg-slate-950 dark:text-slate-200 dark:hover:border-indigo-400 dark:hover:text-indigo-400"
                >
                  {cat.label}
                  <span className="text-slate-400">→</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {activeCategory && isLoading && (
          <div className="flex flex-col items-center justify-center space-y-4 py-12">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-indigo-600 dark:border-slate-800 dark:border-t-indigo-500"></div>
            <p className="text-sm text-slate-500">Analyzing your recent gig data...</p>
          </div>
        )}

        {activeCategory && error && (
          <div className="space-y-4">
            <p className="text-sm font-medium text-red-600">{error}</p>
            <button onClick={resetView} className="text-sm font-semibold text-indigo-600 hover:underline dark:text-indigo-400">
              ← Back to options
            </button>
          </div>
        )}

        {activeCategory && response && (
          <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <button onClick={resetView} className="text-sm font-semibold text-slate-500 hover:text-indigo-600 transition dark:hover:text-indigo-400">
              ← Back to Categories
            </button>

            <div className="rounded-xl bg-indigo-50 border border-indigo-100 p-5 dark:bg-indigo-950/30 dark:border-indigo-900/50">
              <h3 className="text-[10px] font-bold uppercase tracking-[0.2em] text-indigo-800 dark:text-indigo-400 mb-2">Behavioral Insight</h3>
              <p className="text-sm font-medium text-indigo-950 dark:text-indigo-100 leading-relaxed">
                {response.behavioralObservation}
              </p>
            </div>

            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400 italic border-l-2 border-slate-300 pl-4 dark:border-slate-700">
                "{response.dataInsight}"
              </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
              <div>
                <h3 className="text-sm font-bold text-slate-900 dark:text-slate-100 mb-3">Top Actions</h3>
                <ul className="space-y-2">
                  {(response.topActions || []).map((action, i) => (
                    <li key={i} className="flex items-start gap-2 text-sm text-slate-700 dark:text-slate-300">
                      <span className="text-indigo-600 font-bold dark:text-indigo-400">✔</span>
                      {action}
                    </li>
                  ))}
                </ul>
              </div>

              <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between rounded-xl bg-white p-4 border border-slate-200 shadow-sm dark:bg-slate-950 dark:border-slate-800">
                  <span className="text-sm font-bold text-slate-700 dark:text-slate-300">Estimated Impact</span>
                  <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800 dark:bg-emerald-900/50 dark:text-emerald-400">
                    {response.estimatedImpactRange}
                  </span>
                </div>

                {response.confidence && (
                  <div className="flex items-center justify-between rounded-xl bg-white p-4 border border-slate-200 shadow-sm dark:bg-slate-950 dark:border-slate-800">
                    <span className="text-sm font-bold text-slate-700 dark:text-slate-300">AI Confidence</span>
                    <span className={`rounded-full px-3 py-1 text-xs font-bold ${
                      response.confidence.level === 'High' ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/50 dark:text-emerald-400' :
                      response.confidence.level === 'Medium' ? 'bg-amber-100 text-amber-800 dark:bg-amber-900/50 dark:text-amber-400' : 'bg-rose-100 text-rose-800 dark:bg-rose-900/50 dark:text-rose-400'
                    }`}>
                      {response.confidence.level}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AiChatWidget;
