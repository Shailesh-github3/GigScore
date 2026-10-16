import { useEffect, useMemo, useState } from "react";
import PlatformCard from "../components/PlatformCard";
import { addGig } from "../services/gigService";
import { getUserDashboard } from "../services/userService";

const PLATFORMS = ["Swiggy", "Zomato", "Uber", "Rapido", "Upwork"];

function AddGig({ userId }) {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedPlatform, setSelectedPlatform] = useState("");
  const [amount, setAmount] = useState("");
  const [rating, setRating] = useState("");
  const [status, setStatus] = useState({ type: "", message: "" });

  useEffect(() => {
    const fetchDashboard = async () => {
      setLoading(true);
      try {
        const response = await getUserDashboard(userId);
        setDashboard(response);
      } catch (error) {
        setStatus({
          type: "error",
          message: error.response?.data?.message || "Unable to load platform status.",
        });
      }
      setLoading(false);
    };

    fetchDashboard();
  }, [userId]);

  const connectedPlatforms = useMemo(
    () => new Set((dashboard?.gigSummaries || []).filter((gig) => gig.jobsCompleted > 0).map((gig) => gig.platform)),
    [dashboard],
  );

  const handleConnectClick = (platform) => {
    setSelectedPlatform(platform);
    setAmount("");
    setRating("");
    setStatus({ type: "", message: "" });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ type: "", message: "" });

    const payload = {
      userId,
      platform: selectedPlatform,
      amount: Number(amount),
      rating: Number(rating),
    };

    try {
      const updatedDashboard = await addGig(payload);
      setDashboard(updatedDashboard);
      setStatus({ type: "success", message: `${selectedPlatform} connected successfully.` });
      setSelectedPlatform("");
    } catch (error) {
      setStatus({
        type: "error",
        message: error.response?.data?.message || "Failed to connect platform.",
      });
    }
  };

  return (
    <section className="space-y-8">
      <div className="grid gap-6 border-b border-slate-200 pb-8 dark:border-slate-800 md:grid-cols-[minmax(0,1fr)_260px]">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Integrations</p>
          <h1 className="mt-2 text-5xl font-black leading-[0.92] tracking-tight text-slate-900 dark:text-slate-100 md:text-6xl">
            Connect Your
            <br />
            Workforce Ecosystem.
          </h1>
        </div>
        <p className="self-start pt-1 text-right text-sm leading-relaxed text-slate-500 dark:text-slate-400">
          Synchronize earnings, ratings, and operational performance into one unified score.
        </p>
      </div>

      {loading ? <p className="text-sm text-slate-500 dark:text-slate-300">Loading platform status...</p> : null}

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5">
        {PLATFORMS.map((platform) => (
          <PlatformCard
            key={platform}
            platform={platform}
            isConnected={connectedPlatforms.has(platform)}
            onConnectClick={handleConnectClick}
          />
        ))}
      </div>

      {selectedPlatform ? (
        <form
          onSubmit={handleSubmit}
          className="max-w-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200"
        >
          <h2 className="text-xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Connect {selectedPlatform}</h2>
          <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Submit your latest gig details to update score and dashboard metrics.</p>

          <div className="mt-5 grid gap-4 md:grid-cols-2">
            <div>
              <label htmlFor="amount" className="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">
                Amount
              </label>
              <input
                id="amount"
                type="number"
                min="0"
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                required
                className="w-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 text-slate-900 outline-none ring-slate-300 transition focus:ring dark:text-slate-100"
              />
            </div>

            <div>
              <label htmlFor="rating" className="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">
                Rating
              </label>
              <input
                id="rating"
                type="number"
                min="0"
                max="5"
                step="0.1"
                value={rating}
                onChange={(event) => setRating(event.target.value)}
                required
                className="w-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 text-slate-900 outline-none ring-slate-300 transition focus:ring dark:text-slate-100"
              />
            </div>
          </div>

          <div className="mt-6 grid gap-3 sm:grid-cols-2">
            <button
              type="submit"
              className="w-full border border-slate-900 bg-slate-900 px-5 py-2.5 text-sm font-bold uppercase tracking-[0.14em] text-white transition hover:bg-slate-700 dark:border-slate-100 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
            >
              Submit Gig
            </button>
            <button
              type="button"
              onClick={() => setSelectedPlatform("")}
              className="w-full border border-slate-300 px-5 py-2.5 text-sm font-semibold uppercase tracking-[0.14em] text-slate-700 transition hover:bg-slate-100 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
            >
              Cancel
            </button>
          </div>
        </form>
      ) : null}

      {status.message ? (
        <p
          className={`border px-3 py-2 text-sm font-medium ${
            status.type === "success"
              ? "border-slate-300 bg-slate-100 text-slate-700 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200"
              : "border-rose-300 bg-rose-50 text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-300"
          }`}
        >
          {status.message}
        </p>
      ) : null}
    </section>
  );
}

export default AddGig;
