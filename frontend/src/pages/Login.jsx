import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { loginUser } from "../services/userService";

function Login({ onUserAuthenticated }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [status, setStatus] = useState({ loading: false, error: "" });

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!form.email || !form.password) {
      return;
    }

    setStatus({ loading: true, error: "" });
    try {
      const loginResponse = await loginUser(form);
      onUserAuthenticated(loginResponse);
      navigate("/dashboard");
    } catch (error) {
      setStatus({
        loading: false,
        error: error.response?.status === 401
          ? "Invalid email or password."
          : error.response?.data?.message || "Unable to sign in right now.",
      });
      return;
    }

    setStatus({ loading: false, error: "" });
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[var(--color-bg)] px-4 py-10 transition-colors duration-200">
      <section className="grid w-full max-w-5xl overflow-hidden border border-[var(--color-border)] bg-[var(--color-surface)] transition-colors duration-200 md:grid-cols-2">
        <div className="p-8 md:p-12">
          <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-slate-500">Access Premium Tier</p>
          <h1 className="mt-3 text-4xl font-black tracking-tight text-slate-900 dark:text-slate-100">GigScore</h1>

          <div className="mt-10">
            <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Authentication</p>
            <p className="mt-2 max-w-sm text-sm text-slate-500 dark:text-slate-400">
              Sign in using your registered email and security password.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="mt-10 space-y-6">
            <div>
              <label htmlFor="email" className="mb-2 block text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">
                Email
              </label>
              <input
                id="email"
                type="email"
                value={form.email}
                onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
                placeholder="name@company.com"
                required
                className="w-full border-0 border-b border-slate-300 bg-transparent px-0 py-2 text-sm text-slate-900 outline-none ring-0 transition placeholder:text-slate-400 focus:border-slate-900 dark:border-slate-700 dark:text-slate-100 dark:focus:border-slate-200"
              />
            </div>

            <div>
              <label htmlFor="password" className="mb-2 block text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">
                Security Code
              </label>
              <input
                id="password"
                type="password"
                value={form.password}
                onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
                placeholder="********"
                required
                className="w-full border-0 border-b border-slate-300 bg-transparent px-0 py-2 text-sm text-slate-900 outline-none ring-0 transition placeholder:text-slate-400 focus:border-slate-900 dark:border-slate-700 dark:text-slate-100 dark:focus:border-slate-200"
              />
            </div>

            <button
              type="submit"
              disabled={status.loading}
              className="mt-2 w-full border border-slate-900 bg-slate-900 px-4 py-3 text-xs font-bold uppercase tracking-[0.16em] text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:opacity-70 dark:border-slate-100 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-slate-300"
            >
              {status.loading ? "Signing In..." : "Sign In"}
            </button>

            {status.error ? (
              <p className="border border-rose-300 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-300">
                {status.error}
              </p>
            ) : null}
          </form>

          <div className="mt-8 flex items-center justify-between gap-4 border-t border-slate-200 pt-4 text-[10px] font-semibold uppercase tracking-[0.14em] text-slate-500 dark:border-slate-800">
            <span>Encrypted Session</span>
            <span className="text-slate-400">Industry Standard</span>
          </div>

          <p className="mt-6 text-sm text-slate-600 dark:text-slate-300">
            Do not have an account?{" "}
            <Link to="/register" className="font-bold text-slate-900 underline decoration-slate-400 underline-offset-4 dark:text-slate-100 dark:decoration-slate-600">
              Create Account
            </Link>
          </p>
        </div>

        <aside className="relative hidden border-l border-[var(--color-border)] bg-[var(--color-surface-muted)] p-10 md:block">
          <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-slate-500">Session Layer</p>
          <h2 className="mt-4 text-3xl font-black leading-tight tracking-tight text-slate-900 dark:text-slate-100">
            Secure
            <br />
            Access Channel
          </h2>
          <p className="mt-4 max-w-xs text-sm text-slate-500 dark:text-slate-400">
            Access your performance dashboard and platform integrations through a protected identity session.
          </p>

          <div className="absolute bottom-8 right-8 text-[130px] font-black leading-none tracking-tight text-slate-300/65 dark:text-slate-800/70">
            GS
          </div>
        </aside>
      </section>
    </main>
  );
}

export default Login;
