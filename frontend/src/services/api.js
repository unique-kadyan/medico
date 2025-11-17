import axios from "axios";
import { toast } from "sonner";

const api = axios.create({
  baseURL: "/api",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000,
});

api.interceptors.request.use(
  (config) => {
    const apiKey = import.meta.env.VITE_API_KEY;
    if (apiKey) {
      config.headers["X-API-Key"] = apiKey;
    }

    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const { response } = error;

    if (!response) {
      toast.error("Network error. Please check your connection.");
      return Promise.reject(error);
    }

    switch (response.status) {
      case 401: {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.href = "/login";
        toast.error("Session expired. Please login again.");
        break;
      }

      case 403: {
        toast.error("You do not have permission to perform this action.");
        break;
      }

      case 404: {
        toast.error("Resource not found.");
        break;
      }

      case 500: {
        toast.error("Server error. Please try again later.");
        break;
      }

      default: {
        const message = response.data?.message || "An error occurred";
        toast.error(message);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
