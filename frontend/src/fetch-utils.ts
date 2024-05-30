const defaultFetchConfig: RequestInit = {
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include",
};

export class FetchError extends Error {
  constructor(
    public response: Response,
    message?: string,
    status?: number,
  ) {
    super(`[${status} ${message}`);
  }
}

function fetchJson<T>(url: RequestInfo, init: RequestInit = {}): Promise<T> {
  return fetch(url, { ...defaultFetchConfig, ...init }).then((resp) =>
    resp.json(),
  );
}

export function post<T>(url: RequestInfo, init: RequestInit = {}) {
  return fetchJson<T>(url, { method: "POST", ...init });
}

export function put<T>(url: RequestInfo, init: RequestInit = {}) {
  return fetchJson<T>(url, { method: "PUT", ...init });
}

export function del(url: RequestInfo, init: RequestInit = {}) {
  return fetch(url, { method: "DELETE", ...defaultFetchConfig, ...init });
}

export async function get<T>(
  url: RequestInfo,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(url, { method: "GET", ...init });
  return handleResponse(response);
}

function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok || response.redirected) {
    return parseError<T>(response);
  }
  return parseResponse<T>(response);
}

function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.indexOf("application/json") !== -1) {
    return response.json();
  } else if (contentType && contentType.indexOf("text/plain") !== -1) {
    return response.text() as unknown as Promise<T>;
  } else {
    return Promise.resolve({} as T);
  }
}

async function parseError<T>(response: Response): Promise<T> {
  const text = await response.text();
  throw new FetchError(response, text, response.status);
}
