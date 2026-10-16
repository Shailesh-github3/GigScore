import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

function Navbar({ userName, onLogout, isDarkMode, onSetDarkMode }) {
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const profileRef = useRef(null);
  const initials = userName
    ? userName
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase())
        .join("")
    : "US";

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (profileRef.current && !profileRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    onLogout();
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-30 flex flex-col border-b border-[var(--color-border)] bg-[color:var(--color-bg)]/95 backdrop-blur transition-colors duration-200">
      <div className="bg-indigo-600 px-4 py-1.5 text-center text-[10px] font-bold uppercase tracking-[0.2em] text-indigo-50">
        GigScore = Explainable Financial Identity for Gig Workers
      </div>
      <div className="flex h-16 items-center justify-end gap-3 px-4 md:px-6">
        <button
          type="button"
          onClick={() => onSetDarkMode(!isDarkMode)}
          className="inline-flex h-10 w-10 items-center justify-center rounded-none border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text-secondary)] transition-colors hover:bg-[var(--color-surface-muted)] hover:text-[var(--color-text)]"
          aria-label={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
          title={isDarkMode ? "Switch to light mode" : "Switch to dark mode"}
        >
          {isDarkMode ? (
            <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 3v2m0 14v2m9-9h-2M5 12H3m15.36 6.36-1.41-1.41M7.05 7.05 5.64 5.64m12.72 0-1.41 1.41M7.05 16.95l-1.41 1.41" />
              <circle cx="12" cy="12" r="4" />
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3c.03.33.05.66.05 1a8 8 0 0 0 8 8c.34 0 .67-.02 1-.05Z" />
            </svg>
          )}
        </button>

        <div ref={profileRef} className="relative">
          <button
            type="button"
            onClick={() => setIsOpen((prev) => !prev)}
            className="flex items-center gap-3 rounded-none border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm transition-colors hover:bg-[var(--color-surface-muted)]"
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-900 font-bold text-white dark:bg-slate-100 dark:text-slate-900">
              {initials}
            </div>
            <p className="font-semibold text-slate-900 dark:text-slate-100">{userName || "User"}</p>
          </button>

          {isOpen ? (
            <div className="absolute right-0 z-40 mt-2 w-36 rounded-none border border-[var(--color-border)] bg-[var(--color-surface)] p-2 shadow-sm">
              <button
                type="button"
                onClick={handleLogout}
                className="w-full rounded-none px-3 py-2 text-left text-xs font-semibold uppercase tracking-[0.14em] text-red-600 transition hover:bg-[var(--color-surface-muted)] dark:text-red-400"
              >
                Logout
              </button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  );
}

export default Navbar;
