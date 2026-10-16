import httpClient from "./httpClient";

export async function getScore(userId) {
  const response = await httpClient.get(`/score/${userId}`);
  return response.data;
}
