import httpClient from "./httpClient";

export async function getScore(userId, config) {
  const response = await httpClient.get(`/api/v1/score/${userId}`, config);
  return response.data;
}

export async function getScoreConfig(config) {
  const response = await httpClient.get("/api/v1/score/config", config);
  return response.data;
}

export async function getScoreHistory(userId, config) {
  const response = await httpClient.get(`/api/v1/score/${userId}/history`, config);
  return response.data;
}
