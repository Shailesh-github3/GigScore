import httpClient from "./httpClient";

export async function askChatAssistant(messages) {
  const response = await httpClient.post("/api/v1/chat/ask", { messages });
  return response.data;
}
