import httpClient from "./httpClient";

export async function askChatAssistant(messages) {
  const response = await httpClient.post("/api/v1/chat/ask", { messages });
  return response.data;
}

export async function analyzeCoach(userId, category) {
  const query = userId ? `?userId=${userId}` : "";
  const response = await httpClient.post(`/api/v1/coach/analyze${query}`, { category });
  return response.data;
}

export async function setCoachGoal(userId, targetScore) {
  const query = userId ? `?userId=${userId}` : "";
  const response = await httpClient.post(`/api/v1/coach/set-goal${query}`, { targetScore });
  return response.data;
}
