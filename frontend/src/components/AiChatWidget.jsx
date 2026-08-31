import { useEffect, useMemo, useRef, useState } from "react";
import { askChatAssistant } from "../services/chatService";

const STORAGE_KEY = "gigscoreAiChatMessages";

const defaultMessages = [
  {
    role: "assistant",
    content:
      "Hi, I am your GigScore AI assistant. Ask me about score breakdown, improvements, gigs, or performance trends.",
  },
];

function AiChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [messages, setMessages] = useState(() => {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return defaultMessages;
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed) || parsed.length === 0) return defaultMessages;
      return parsed;
    } catch {
      return defaultMessages;
    }
  });

  const historyEndRef = useRef(null);

  useEffect(() => {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
  }, [messages]);

  useEffect(() => {
    historyEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading]);

  const canSend = useMemo(() => input.trim().length > 0 && !isLoading, [input, isLoading]);

  const handleSend = async (event) => {
    event.preventDefault();
    const userText = input.trim();
    if (!userText || isLoading) return;

    const nextMessages = [...messages, { role: "user", content: userText }];
    setMessages(nextMessages);
    setInput("");
    setError("");
    setIsLoading(true);

    try {
      const response = await askChatAssistant(nextMessages);
      const aiReply = response.reply || "I could not generate a response right now.";
      setMessages((prev) => [...prev, { role: "assistant", content: aiReply }]);
    } catch (requestError) {
      setError(requestError.response?.data?.message || "AI assistant is temporarily unavailable.");
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: "I am having trouble connecting right now. Please try again in a moment.",
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <button
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        className="fixed bottom-6 right-6 z-50 inline-flex items-center gap-2 rounded-full bg-[var(--color-accent)] px-5 py-3 text-sm font-semibold text-white shadow-lg transition hover:-translate-y-0.5 hover:bg-[var(--color-accent-strong)]"
      >
        <span>AI Chat</span>
      </button>

      <div
        onClick={() => setIsOpen(false)}
        className={`fixed inset-0 z-40 bg-slate-950/35 backdrop-blur-[2px] transition-opacity duration-300 ${
          isOpen ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0"
        }`}
      />

      <aside
        className={`fixed right-0 top-0 z-50 flex h-full w-full max-w-md flex-col border-l border-[var(--color-border)] bg-[var(--color-surface)] shadow-2xl transition-transform duration-300 ${
          isOpen ? "translate-x-0" : "translate-x-full"
        }`}
        aria-hidden={!isOpen}
      >
        <div className="flex items-center justify-between border-b border-slate-200 px-4 py-4 dark:border-slate-800">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">GigScore AI</p>
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-100">Assistant</h2>
          </div>
          <button
            type="button"
            className="rounded-md px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-slate-100"
            onClick={() => setIsOpen(false)}
          >
            Close
          </button>
        </div>

        <div className="flex-1 space-y-3 overflow-y-auto px-4 py-4">
          {messages.map((message, index) => {
            const isUser = message.role === "user";
            return (
              <div key={`${message.role}-${index}`} className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
                <div
                  className={`max-w-[85%] rounded-2xl px-4 py-2 text-sm leading-relaxed ${
                    isUser
                      ? "bg-[var(--color-accent)] text-white"
                      : "bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-100"
                  }`}
                >
                  {message.content}
                </div>
              </div>
            );
          })}

          {isLoading ? (
            <div className="flex justify-start">
              <div className="rounded-2xl bg-slate-100 px-4 py-2 text-sm text-slate-700 dark:bg-slate-800 dark:text-slate-200">
                Thinking...
              </div>
            </div>
          ) : null}

          {error ? <p className="text-xs font-medium text-red-600">{error}</p> : null}
          <div ref={historyEndRef} />
        </div>

        <form onSubmit={handleSend} className="border-t border-slate-200 p-4 dark:border-slate-800">
          <div className="flex items-end gap-2">
            <textarea
              rows={2}
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="Ask about your score, gigs, or suggestions..."
              className="max-h-28 min-h-[44px] w-full resize-y rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm text-slate-900 outline-none ring-indigo-500 transition placeholder:text-slate-400 focus:ring-2 dark:text-slate-100"
            />
            <button
              type="submit"
              disabled={!canSend}
              className="rounded-xl bg-[var(--color-accent)] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[var(--color-accent-strong)] disabled:cursor-not-allowed disabled:opacity-50"
            >
              Send
            </button>
          </div>
        </form>
      </aside>
    </>
  );
}

export default AiChatWidget;
