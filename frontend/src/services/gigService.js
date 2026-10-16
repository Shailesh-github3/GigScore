import httpClient from "./httpClient";

export async function addGig(payload) {
  const response = await httpClient.post("/api/gigs", payload);
  return response.data;
}
