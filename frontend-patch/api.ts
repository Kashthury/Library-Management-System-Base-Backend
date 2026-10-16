/**
 * Drop-in replacement for  src/lib/api.ts
 *
 * The backend always returns errors as JSON: { status, error, message, fieldErrors, ... }.
 * The original client threw "API request failed: 400 Bad Request" and hid the real reason.
 * This version throws the backend's own "message", so your existing
 * setError(e.message) calls show things like
 *   "Borrow limit exceeded: student already has 4 book(s) and requested 2 more ..."
 */
import { API_BASE_URL } from "../utils/apiEndpoint";

export const apiClient = {
  baseUrl: API_BASE_URL,

  async request<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...options?.headers,
      },
    });

    if (!response.ok) {
      let message = `API request failed: ${response.status} ${response.statusText}`;
      try {
        const body = await response.json();
        if (body && typeof body.message === "string" && body.message.trim()) {
          message = body.message;
        }
      } catch {
        // body was empty or not JSON - keep the generic message
      }
      throw new Error(message);
    }

    if (response.status === 204) return undefined as T;
    return response.json() as Promise<T>;
  },
};
