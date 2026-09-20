import httpClient from "./httpClient";

export async function getWorkers(params) {
  const url = params ? `/api/v1/lender/workers?page=${params.page ?? 0}&size=${params.size ?? 20}` : "/api/v1/lender/workers";
  const response = await httpClient.get(url);
  return response.data;
}

export async function getWorker(userId) {
  const response = await httpClient.get(`/api/v1/lender/workers/${userId}`);
  return response.data;
}