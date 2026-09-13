import httpClient from "./httpClient";

export async function getScore(userId) {
  const response = await httpClient.get(`/api/v1/score/${userId}`);
  return response.data;
}

export async function getScoreConfig() {
  const response = await httpClient.get("/api/v1/score/config");
  return response.data;
}

export async function getScoreHistory(userId) {
  const response = await httpClient.get(`/api/v1/score/${userId}/history`);
  return response.data;
}
