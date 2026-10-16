import { useState } from "react";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { createUser } from "../services/userService";

function CreateUser({ onUserAuthenticated }) {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState({ type: "", message: "" });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setStatus({ type: "", message: "" });

    try {
      const dashboard = await createUser({
        name,
        email,
        password,
      });

      onUserAuthenticated(dashboard);

      setStatus({
        type: "success",
        message: `Account created successfully.`,
      });
      setName("");
      setEmail("");
      setPassword("");
      navigate("/dashboard");
    } catch (error) {
      setStatus({
        type: "error",
        message:
          error.response?.status === 409
            ? "This email is already registered. Please sign in instead."
            : error.response?.data?.message || "Unable to create user right now.",
      });
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[var(--color-bg)] px-4 py-10 transition-colors duration-200">
      <section className="w-full max-w-md rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-8 shadow-sm transition-colors duration-200">

        <h1 className="text-3xl font-extrabold tracking-tight text-blue-700">Create Account</h1>
        <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">Join GigScore to track your financial reputation.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          <div>
            <label htmlFor="name" className="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">
              Full Name
            </label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
              className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 outline-none ring-indigo-200 transition focus:ring dark:text-slate-100"
            />
          </div>

          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 outline-none ring-indigo-200 transition focus:ring dark:text-slate-100"
            />
          </div>

          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-semibold text-slate-700 dark:text-slate-300">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={6}
              className="w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2.5 outline-none ring-indigo-200 transition focus:ring dark:text-slate-100"
            />
          </div>

          <button
            type="submit"
            className="w-full rounded-lg bg-blue-600 px-4 py-3 font-bold text-white transition hover:bg-blue-700"
          >
            Create Account
          </button>
        </form>

        {status.message ? (
          <p
            className={`mt-4 rounded-lg px-3 py-2 text-sm font-medium ${
              status.type === "success"
                ? "bg-emerald-100 text-emerald-800"
                : "bg-red-100 text-red-700"
            }`}
          >
            {status.message}
          </p>
        ) : null}

        <p className="mt-6 text-center text-sm text-slate-600 dark:text-slate-300">
          Already have an account?{" "}
          <Link to="/login" className="font-bold text-blue-700 hover:underline">
            Sign In
          </Link>
        </p>
      </section>
    </main>
  );
}

export default CreateUser;
