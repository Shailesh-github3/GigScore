import httpClient from "./httpClient";

export async function createUser(payload) {
  const response = await httpClient.post("/api/v1/users", payload);
  return response.data;
}

export async function getUserDashboard(userId) {
  const response = await httpClient.get(`/api/v1/users/${userId}`);
  return response.data;
}

export async function loginUser(payload) {
  const response = await httpClient.post("/api/v1/users/login", payload);
  return response.data;
}
