import httpClient from "./httpClient";

export async function getWorkers() {
  const response = await httpClient.get("/api/v1/lender/workers");
  return response.data;
}

export async function getWorker(userId) {
  const response = await httpClient.get(`/api/v1/lender/workers/${userId}`);
  return response.data;
}