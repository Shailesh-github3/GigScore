import httpClient from "./httpClient";

export async function getScore(userId) {
  const response = await httpClient.get(`/api/v1/score/${userId}`);
  return response.data;
}
