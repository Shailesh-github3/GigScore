import axios from "axios";

const rawBaseUrl = import.meta.env.VITE_API_URL;
const baseURL = rawBaseUrl !== undefined
  ? rawBaseUrl.replace(/\/+$/, "")
  : "http://localhost:8080";

const httpClient = axios.create({
  baseURL,
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("gigscoreToken");

  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem("gigscoreToken");
        localStorage.removeItem("gigscoreUser");
        if (
          typeof window !== "undefined" &&
          window.location.pathname !== "/login" &&
          window.location.pathname !== "/register"
        ) {
          window.location.href = "/login";
        }
      } else if (error.response.status === 403) {
        console.warn("Access forbidden (403): insufficient permissions.");
      }
    }
    return Promise.reject(error);
  }
);

export default httpClient;
