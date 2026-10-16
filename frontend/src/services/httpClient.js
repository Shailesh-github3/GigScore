import axios from "axios";

const httpClient = axios.create({
  baseURL: "http://127.0.0.1:8080",
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("gigscoreToken");

  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default httpClient;
