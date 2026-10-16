import { NavLink } from "react-router-dom";

function Sidebar() {
  const linkClass = ({ isActive }) =>
    `group relative flex items-center rounded-none px-4 py-3 text-[11px] font-semibold uppercase tracking-[0.14em] transition-all ${
      isActive
        ? "bg-[var(--color-accent)] text-white hover:bg-[var(--color-accent-strong)]"
        : "text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-muted)] hover:text-[var(--color-text)]"
    }`;

  return (
    <aside className="sticky top-16 hidden h-[calc(100vh-4rem)] w-72 shrink-0 border-r border-[var(--color-border)] bg-[var(--color-bg)] px-5 py-6 transition-colors duration-200 lg:flex lg:flex-col">
      <div className="mb-10 border border-[var(--color-border)] bg-[var(--color-surface)] p-4 transition-colors duration-200">
        <h1 className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">GigScore</h1>
        <p className="mt-1 text-[10px] uppercase tracking-[0.18em] text-slate-500">Premium Tier</p>
      </div>

      <nav className="flex flex-col gap-3">
        <NavLink to="/dashboard" className={linkClass}>
          Dashboard
        </NavLink>
        <NavLink to="/add-gig" className={linkClass}>
          Add Gig
        </NavLink>
        <NavLink to="/score" className={linkClass}>
          Score
        </NavLink>
      </nav>
    </aside>
  );
}

export default Sidebar;
