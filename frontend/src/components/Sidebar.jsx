import { useState } from "react";
import { NavLink } from "react-router-dom";

function Sidebar({ role }) {
  const [mobileOpen, setMobileOpen] = useState(false);

  const linkClass = ({ isActive }) =>
    `group relative flex items-center rounded-none px-4 py-3 text-[11px] font-semibold uppercase tracking-[0.14em] transition-all ${
      isActive
        ? "bg-[var(--color-accent)] text-white hover:bg-[var(--color-accent-strong)]"
        : "text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-muted)] hover:text-[var(--color-text)]"
    }`;

  const navLinks = (
    <nav className="flex flex-col gap-3">
      <NavLink to="/dashboard" className={linkClass} onClick={() => setMobileOpen(false)}>
        Dashboard
      </NavLink>
      <NavLink to="/add-gig" className={linkClass} onClick={() => setMobileOpen(false)}>
        Add Gig
      </NavLink>
      <NavLink to="/score" className={linkClass} onClick={() => setMobileOpen(false)}>
        Score
      </NavLink>
      {role === "LENDER" && (
        <NavLink to="/lenders" className={linkClass} onClick={() => setMobileOpen(false)}>
          Lenders
        </NavLink>
      )}
    </nav>
  );

  return (
    <>
      <button
        onClick={() => setMobileOpen(!mobileOpen)}
        className="fixed bottom-4 right-4 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-slate-900 text-white shadow-lg lg:hidden dark:bg-slate-100 dark:text-slate-900"
        aria-label="Toggle navigation"
      >
        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          {mobileOpen ? (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          ) : (
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          )}
        </svg>
      </button>

      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/40 lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside
        className={`fixed top-16 z-40 h-[calc(100vh-4rem)] w-72 border-r border-[var(--color-border)] bg-[var(--color-bg)] px-5 py-6 transition-transform duration-200 lg:sticky lg:hidden lg:translate-x-0 lg:flex lg:flex-col ${
          mobileOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="mb-10 border border-[var(--color-border)] bg-[var(--color-surface)] p-4 transition-colors duration-200">
          <h1 className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">GigScore</h1>
          <p className="mt-1 text-[10px] uppercase tracking-[0.18em] text-slate-500">Premium Tier</p>
        </div>
        {navLinks}
      </aside>

      <aside className="sticky top-16 hidden h-[calc(100vh-4rem)] w-72 shrink-0 border-r border-[var(--color-border)] bg-[var(--color-bg)] px-5 py-6 transition-colors duration-200 lg:flex lg:flex-col">
        <div className="mb-10 border border-[var(--color-border)] bg-[var(--color-surface)] p-4 transition-colors duration-200">
          <h1 className="text-2xl font-black tracking-tight text-slate-900 dark:text-slate-100">GigScore</h1>
          <p className="mt-1 text-[10px] uppercase tracking-[0.18em] text-slate-500">Premium Tier</p>
        </div>
        {navLinks}
      </aside>
    </>
  );
}

export default Sidebar;
