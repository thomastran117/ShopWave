import axios from "axios";
import { store } from "./stores";
import { setCredentials, clearCredentials } from "./stores/authSlice";
import Environment from "./configuration/Environment";

const api = axios.create({
  baseURL: Environment.backend_url,
  withCredentials: true, // send cookies for refresh
});

// Single shared promise for the in-flight token refresh. All concurrent 401 responses
// queue onto this promise rather than each triggering its own refresh request.
let refreshPromise: Promise<string> | null = null;

api.interceptors.request.use((config) => {
  const state = store.getState();
  const token = state.auth.accessToken;

  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const status = error.response?.status;
    if ((status === 401 || status === 403) && !originalRequest._retry) {
      originalRequest._retry = true;

      if (!refreshPromise) {
        refreshPromise = axios
          .post(
            `${Environment.backend_url}/auth/refresh`,
            {},
            { withCredentials: true }
          )
          .then((resp) => {
            const newToken: string = resp.data.accessToken;
            store.dispatch(
              setCredentials({
                accessToken: newToken,
                email: resp.data.email ?? null,
                role: resp.data.role ?? null,
              })
            );
            return newToken;
          })
          .catch((err) => {
            store.dispatch(clearCredentials());
            return Promise.reject(err);
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      return refreshPromise.then((newToken) => {
        if (originalRequest.headers) {
          originalRequest.headers["Authorization"] = "Bearer " + newToken;
        }
        return api(originalRequest);
      });
    }

    return Promise.reject(error);
  }
);

export default api;
