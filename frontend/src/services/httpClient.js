import axios from "axios";

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://127.0.0.1:8080",
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
    if (error.response?.status === 401) {
      localStorage.removeItem("gigscoreToken");
      localStorage.removeItem("gigscoreUserId");
      localStorage.removeItem("gigscoreUserName");
      localStorage.removeItem("gigscoreUserEmail");
      localStorage.removeItem("gigscoreUserRole");

      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default httpClient;
