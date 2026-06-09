import Qs from "qs";
import axios from "axios";
import router from "@/router";
import CoreConsts from "@/components/CoreConsts";
import env from "@/env";

let baseConfig = {
  success: 200,
  duration: 3000,
  timeout: 20000
};

let urlConfig = env.http;
let baseURL = urlConfig.getBaseURI();
let refreshPromise = null;

export function saveAuthSession(data = {}) {
  const accessToken = data.accessToken || data.access_token;
  const refreshToken = data.refreshToken || data.refresh_token;
  const clientId = data.clientId || localStorage.getItem(CoreConsts.OAuth2ClientId) || "iboot-local-web";
  const scopes = data.scopes || data.scope || [];
  const roles = data.roles || [];

  if (accessToken) {
    localStorage.setItem(CoreConsts.AccessToken, accessToken);
  }
  if (refreshToken) {
    localStorage.setItem(CoreConsts.RefreshToken, refreshToken);
  }
  if (clientId) {
    localStorage.setItem(CoreConsts.OAuth2ClientId, clientId);
  }

  localStorage.setItem(
    CoreConsts.OAuth2Scopes,
    JSON.stringify(Array.isArray(scopes) ? scopes : String(scopes || "").split(/[,\s]+/).filter(Boolean))
  );
  localStorage.setItem(CoreConsts.OAuth2Roles, JSON.stringify(Array.isArray(roles) ? roles : []));
}

export function clearAuthSession() {
  localStorage.removeItem(CoreConsts.AccessToken);
  localStorage.removeItem(CoreConsts.RefreshToken);
  localStorage.removeItem(CoreConsts.OAuth2ClientId);
  localStorage.removeItem(CoreConsts.OAuth2Scopes);
  localStorage.removeItem(CoreConsts.OAuth2Roles);
}

async function refreshAccessToken() {
  if (refreshPromise) {
    return refreshPromise;
  }

  const refreshToken = localStorage.getItem(CoreConsts.RefreshToken);
  const clientId = localStorage.getItem(CoreConsts.OAuth2ClientId) || "iboot-local-web";
  if (!refreshToken) {
    throw new Error("missing refresh token");
  }

  refreshPromise = axios
    .post(
      `${baseURL}/oauth2/token`,
      Qs.stringify({
        grant_type: "refresh_token",
        client_id: clientId,
        refresh_token: refreshToken
      }),
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        }
      }
    )
    .then(resp => {
      const body = resp.data || {};
      if (body.code && body.code !== 200) {
        throw new Error(body.message || "refresh failed");
      }

      const tokenData = body.data || body;
      saveAuthSession({
        accessToken: tokenData.access_token,
        refreshToken: tokenData.refresh_token || refreshToken,
        clientId,
        scopes: tokenData.scope ? String(tokenData.scope).split(/[,\s]+/).filter(Boolean) : [],
        roles: JSON.parse(localStorage.getItem(CoreConsts.OAuth2Roles) || "[]")
      });
      return localStorage.getItem(CoreConsts.AccessToken);
    })
    .finally(() => {
      refreshPromise = null;
    });

  return refreshPromise;
}

const handleResponse = data => {
  let { code, message } = data;
  switch (code) {
    case 401:
      clearAuthSession();
      router.push({ path: "/login" }).finally(() => {});
      return Promise.reject(data);
    case 403:
      console.error(message || "forbidden");
      return Promise.reject(data);
    case 404:
      return Promise.reject(data);
    default:
      return data;
  }
};

const instance = axios.create({
  baseURL,
  timeout: baseConfig.timeout,
  headers: {
    "x-requested-with": "XMLHttpRequest",
    "Content-Type": "application/json; charset=UTF-8"
  },
  paramsSerializer: params => Qs.stringify(params, { arrayFormat: "indices", allowDots: true })
});

instance.interceptors.request.use(
  config => {
    let token = localStorage.getItem(CoreConsts.AccessToken);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      config.headers[CoreConsts.AccessToken] = token;
    }
    return config;
  },
  error => Promise.reject(error)
);

instance.interceptors.response.use(
  response => {
    const { data, config } = response;
    if (config[CoreConsts.CancelRespResolver]) {
      return response;
    }
    return handleResponse(data);
  },
  async error => {
    const { response, config } = error;

    if (response && response.status === 401 && !config._retry) {
      const refreshToken = localStorage.getItem(CoreConsts.RefreshToken);
      if (refreshToken) {
        try {
          config._retry = true;
          const newToken = await refreshAccessToken();
          config.headers.Authorization = `Bearer ${newToken}`;
          config.headers[CoreConsts.AccessToken] = newToken;
          return instance(config);
        } catch (refreshError) {
          clearAuthSession();
          router.push({ path: "/login" }).finally(() => {});
          return Promise.reject(refreshError);
        }
      }
    }

    if (response && response.data) {
      const { status } = response;
      return handleResponse({ code: status, message: response.data.message || error.message });
    }

    let { message } = error;
    if (message === "Network Error") {
      message = "网络连接异常";
    } else if (message.includes("timeout")) {
      message = "接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      const code = message.substr(message.length - 3);
      message = `后端接口[${code}]异常`;
    }

    console.error(message || "后端接口异常");
    return Promise.reject(error);
  }
);

let GET = (url, data, config) => {
  if (config) {
    config.params = data;
  } else {
    config = { params: data };
  }
  return instance.get(url, config);
};

let PUT = instance.put;
let POST = instance.post;
let PATCH = instance.patch;
let DELETE = instance.delete;

export { GET, PUT, POST, PATCH, DELETE, baseURL, instance as http, urlConfig, env };
