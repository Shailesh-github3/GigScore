import httpClient from "./httpClient";

export async function askChatAssistant(messages) {
  const response = await httpClient.post("/api/chat/ask", { messages });
  return response.data;
}

export async function analyzeCoach(userId, category) {
  const response = await httpClient.post(`/api/v1/coach/analyze?userId=${userId}`, { category });
  return response.data;
}
