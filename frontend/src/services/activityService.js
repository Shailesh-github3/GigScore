import httpClient from "./httpClient";

export async function getRecentActivities(userId, page = 0, size = 5) {
  const response = await httpClient.get(`/api/v1/activity/${userId}?page=${page}&size=${size}`);
  return response.data;
}
