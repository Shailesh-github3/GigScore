import { useEffect, useState } from "react";
import { getWorkers } from "../services/lenderService";

function ConfidenceBadge({ confidence }) {
  const styles = {
    HIGH: "bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300",
    MEDIUM: "bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300",
    LOW: "bg-rose-100 text-rose-800 dark:bg-rose-950/60 dark:text-rose-300",
  };
  const label = confidence || "UNKNOWN";
  return (
    <span
      className={`inline-block px-2 py-1 text-[10px] font-bold uppercase tracking-[0.14em] ${
        styles[label] || "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300"
      }`}
    >
      {label}
    </span>
  );
}

function Lenders() {
  const [workers, setWorkers] = useState([]);
  const [status, setStatus] = useState({ loading: true, error: "" });

  useEffect(() => {
    let cancelled = false;

    getWorkers()
      .then((data) => {
        if (!cancelled) {
          setWorkers(data);
          setStatus({ loading: false, error: "" });
        }
      })
      .catch(() => {
        if (!cancelled) {
          setStatus({ loading: false, error: "Unable to load worker data." });
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  if (status.loading) {
    return <p className="text-sm text-[var(--color-text-secondary)]">Loading workers...</p>;
  }

  if (status.error) {
    return (
      <p className="border border-rose-300 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-300">
        {status.error}
      </p>
    );
  }

  return (
    <section>
      <header className="mb-6 border border-[var(--color-border)] bg-[var(--color-surface)] p-5 transition-colors duration-200">
        <h1 className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">
          Worker Creditworthiness
        </h1>
        <p className="mt-1 text-sm text-[var(--color-text-secondary)]">
          Verified earnings track records ranked by GigScore.
        </p>
      </header>

      {workers.length === 0 ? (
        <p className="border border-[var(--color-border)] bg-[var(--color-surface)] p-5 text-sm text-[var(--color-text-secondary)]">
          No workers with data yet.
        </p>
      ) : (
        <div className="overflow-x-auto border border-[var(--color-border)] bg-[var(--color-surface)] transition-colors duration-200">
          <table className="w-full text-left text-sm">
            <thead className="bg-[var(--color-surface-muted)]">
              <tr className="text-[10px] font-bold uppercase tracking-[0.16em] text-[var(--color-text-secondary)]">
                <th className="px-4 py-3">Worker</th>
                <th className="px-4 py-3">GigScore</th>
                <th className="px-4 py-3">Band</th>
                <th className="px-4 py-3">Confidence</th>
                <th className="px-4 py-3">Total Earnings</th>
                <th className="px-4 py-3">Active Days</th>
                <th className="px-4 py-3">Flags</th>
              </tr>
            </thead>
            <tbody>
              {workers.map((worker) => (
                <tr
                  key={worker.userId}
                  className="border-t border-[var(--color-border)]"
                >
                  <td className="px-4 py-3">
                    <p className="font-semibold text-slate-900 dark:text-slate-100">{worker.name}</p>
                    <p className="text-xs text-[var(--color-text-secondary)]">{worker.email}</p>
                  </td>
                  <td className="px-4 py-3 font-bold text-slate-900 dark:text-slate-100">
                    {worker.score != null ? worker.score.toFixed(2) : "-"}
                  </td>
                  <td className="px-4 py-3">
                    <span className="inline-block bg-[var(--color-surface-muted)] px-2 py-1 text-[10px] font-bold uppercase tracking-[0.14em] text-[var(--color-text-secondary)]">
                      {worker.band || "-"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <ConfidenceBadge confidence={worker.dataQuality?.confidence} />
                  </td>
                  <td className="px-4 py-3">
                    {worker.totalEarnings != null
                      ? new Intl.NumberFormat("en-US", {
                          style: "currency",
                          currency: "USD",
                        }).format(worker.totalEarnings)
                      : "-"}
                  </td>
                  <td className="px-4 py-3">{worker.activeDays}</td>
                  <td className="px-4 py-3">
                    {worker.dataQuality?.flags?.length ? (
                      <div className="flex flex-wrap gap-1">
                        {worker.dataQuality.flags.map((flag) => (
                          <span
                            key={flag}
                            className="bg-[var(--color-surface-muted)] px-1.5 py-0.5 text-[10px] font-semibold text-[var(--color-text-secondary)]"
                          >
                            {flag}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <span className="text-xs text-[var(--color-text-secondary)]">-</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default Lenders;