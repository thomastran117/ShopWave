import axios from "axios";
import { store } from "./stores";

const api = axios.create({
  baseURL: "http://localhost:8090/api",
});

api.interceptors.request.use((config) => {
  const state = store.getState();
  const token = state.auth.accessToken;

  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
