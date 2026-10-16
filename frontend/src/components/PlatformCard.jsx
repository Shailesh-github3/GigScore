function PlatformCard({ platform, isConnected, onConnectClick }) {
  return (
    <article className="rounded-none border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200">
      <div className="mb-7 flex items-start justify-between gap-3">
        <h3 className="text-xl font-bold tracking-tight text-slate-900 dark:text-slate-100">{platform}</h3>
        <span
          className={`rounded-full border px-2.5 py-1 text-[10px] font-bold uppercase tracking-[0.14em] ${
            isConnected
              ? "border-emerald-300/45 bg-emerald-500/15 text-emerald-600 dark:text-emerald-300"
              : "border-slate-400 bg-slate-200 text-slate-800 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300"
          }`}
        >
          {isConnected ? "Connected" : "Not Connected"}
        </span>
      </div>

      <button
        type="button"
        disabled={isConnected}
        onClick={() => onConnectClick(platform)}
        className={`w-full rounded-none border px-4 py-3 text-sm font-bold uppercase tracking-[0.14em] transition ${
          isConnected
            ? "cursor-not-allowed border-[var(--color-border)] bg-[var(--color-surface-muted)] text-slate-400 dark:text-slate-500"
            : "border-slate-900 bg-slate-900 text-white hover:bg-slate-700 dark:border-slate-100 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
        }`}
      >
        {isConnected ? "Connected" : "Connect"}
      </button>
    </article>
  );
}

export default PlatformCard;
