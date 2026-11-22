import httpClient from "./httpClient";

export async function getRecentActivities(userId) {
  const response = await httpClient.get(`/api/v1/activity/${userId}`);
  return response.data;
}
