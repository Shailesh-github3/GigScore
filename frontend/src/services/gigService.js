import httpClient from "./httpClient";

export async function addGig(payload) {
  const response = await httpClient.post("/api/v1/gigs", payload);
  return response.data;
}
