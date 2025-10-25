import axios from 'axios';
import { getAuthStore } from '@/stores/storeRegistry.js';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1';
const USE_CREDENTIALS = (import.meta.env.VITE_WITH_CREDENTIALS ?? 'false') === 'true';

export const http = axios.create({
    baseURL: BASE_URL,
    timeout: 15000,
    withCredentials: USE_CREDENTIALS, // QUAN TRỌNG: gửi cookie (access/refresh) mỗi request
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
    }
});

// Gắn Authorization header từ Pinia (song song cookie -> an toàn hơn)
http.interceptors.request.use((config) => {
    const auth = getAuthStore?.();
    const token = auth?.accessToken;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

let isRefreshing = false;
let queue = [];
http.interceptors.response.use(
    (res) => res,
    async (error) => {
        const original = error.config || {};
        if (error.response?.status === 401 && !original.__isRetryRequest) {
            const auth = getAuthStore?.();
            if (!auth?.refreshToken) return Promise.reject(error);

            if (!isRefreshing) {
                isRefreshing = true;
                try {
                    const newToken = await auth.refreshTokenOnce();
                    queue.forEach((cb) => cb(newToken));
                    queue = [];
                    isRefreshing = false;

                    original.__isRetryRequest = true;
                    original.headers = original.headers || {};
                    original.headers.Authorization = `Bearer ${newToken}`;
                    return http(original);
                } catch (e) {
                    isRefreshing = false;
                    queue = [];
                    try {
                        auth?.logout();
                    } catch {}
                    return Promise.reject(error);
                }
            }

            return new Promise((resolve) => {
                queue.push((newToken) => {
                    original.__isRetryRequest = true;
                    original.headers = original.headers || {};
                    original.headers.Authorization = `Bearer ${newToken}`;
                    resolve(http(original));
                });
            });
        }
        return Promise.reject(error);
    }
);

// Tải file blob
export async function httpDownload(url, config = {}) {
    const res = await http.request({ url, method: 'GET', responseType: 'blob', ...config });
    return res.data;
}
