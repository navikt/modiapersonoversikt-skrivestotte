const defaultFetchConfig: RequestInit = {
    headers: {
        'Content-Type': 'application/json'
    },
    credentials: 'include'
};

function fetchJson<T>(url: RequestInfo, init: RequestInit = {}): Promise<T> {
    return fetch(url, { ...defaultFetchConfig, ...init})
        .then((resp) => resp.json());
}

export function post<T>(url: RequestInfo, init: RequestInit = {}) {
    return fetchJson<T>(url, { method: 'POST', ...init });
}

export function put<T>(url: RequestInfo, init: RequestInit = {}) {
    return fetchJson<T>(url, { method: 'PUT', ...init });
}

export function del<T>(url: RequestInfo, init: RequestInit = {}) {
    return fetchJson<T>(url, { method: 'DELETE', ...init });
}