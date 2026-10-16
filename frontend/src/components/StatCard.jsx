const toneClasses = {
  success: "bg-emerald-500",
  warning: "bg-amber-500",
  neutral: "bg-slate-500",
  accent: "bg-slate-900 dark:bg-slate-100",
};

function StatCard({ title, value, subtitle, icon, tone = "neutral", priority = "secondary" }) {
  const isPrimary = priority === "primary";

  return (
    <article className="rounded-none border border-[var(--color-border)] bg-[var(--color-surface)] p-6 transition-colors duration-200">
      <div className="flex items-start justify-between gap-3">
        <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">{title}</p>
        <span className={`inline-block h-2.5 w-2.5 rounded-full ${toneClasses[tone] || toneClasses.neutral}`} aria-hidden="true" />
      </div>

      <h3 className={`mt-4 font-black leading-none tracking-tight text-slate-900 dark:text-slate-100 ${isPrimary ? "text-5xl" : "text-4xl"}`}>{value}</h3>

      {subtitle ? <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">{subtitle}</p> : null}

      {icon ? <p className="mt-4 text-xs font-semibold uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">{icon}</p> : null}
    </article>
  );
}

export default StatCard;
