import { baseURL, GET, POST } from "@/utils/request";
import CoreConsts from "@/components/CoreConsts";
import { createOAuthState, createPkcePair, savePendingOAuth } from "@/utils/oauth2";

const captchaUri = `${baseURL}/valid/captcha`;
const avatarUploadUri = "/core/center/avatar";

export { captchaUri, avatarUploadUri };

export function getCode() {
  return GET("/valid/code");
}

export function getMenus() {
  return GET("/core/menu/bars");
}

export function getUser() {
  return GET("/core/admin/detail");
}

export function getPermissions() {
  return GET("/core/menu/permissions");
}

export function getSysConfig(config) {
  return GET("/core/config/list", config || {});
}

export function editPwd(model) {
  return POST("/core/center/pwd", model);
}

export function editUser(user) {
  return POST("/core/center/editUser", user);
}

export function getNotifyList() {
  return GET("/core/notify/view");
}

export function getDict(type) {
  return GET("/core/dictData/listByType", { type }).then(({ data }) => {
    if (!(data instanceof Array)) {
      return [];
    }
    return data.map(item => ({ label: item.label, value: item.value }));
  });
}

export function login(user) {
  let config = {};
  config[CoreConsts.CancelRespResolver] = true;
  return POST(`/core/login?code=${user.code}`, user, config);
}

export function logout() {
  return POST("/core/logout");
}

export function getOAuth2Config() {
  return GET("/oauth2/config");
}

export async function oauth2DoLogin({ username, password }) {
  return POST(
    "/oauth2/doLogin",
    new URLSearchParams({
      name: username || "",
      pwd: password || ""
    }).toString(),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      }
    }
  );
}

export async function oauth2Login(credentials) {
  if (credentials?.username && credentials?.password) {
    const loginResp = await oauth2DoLogin(credentials);
    if (loginResp.code !== 200) {
      throw new Error(loginResp.message || "OAuth2 登录失败");
    }
  }

  const config = await getOAuth2Config();
  const data = config.data || {};
  const clientId = data.defaultClientId || "iboot-local-web";
  const scopes = data.defaultScopes || ["device.read", "device.control", "user.manage"];
  const authorizeEndpoint = data.authorizeEndpoint || "/oauth2/authorize";
  const { verifier, challenge } = await createPkcePair();
  const state = createOAuthState();
  const redirectUri = `${window.location.origin}/oauth/callback.html`;

  savePendingOAuth(verifier, state);
  localStorage.setItem(CoreConsts.OAuth2ClientId, clientId);

  const query = new URLSearchParams({
    response_type: "code",
    client_id: clientId,
    redirect_uri: redirectUri,
    scope: scopes.join(" "),
    state,
    code_challenge: challenge,
    code_challenge_method: "S256"
  });

  window.location.href = `${baseURL}${authorizeEndpoint}?${query.toString()}`;
}

export function exchangeOAuthToken({ code, codeVerifier }) {
  const clientId = localStorage.getItem(CoreConsts.OAuth2ClientId) || "iboot-local-web";
  const redirectUri = `${window.location.origin}/oauth/callback.html`;
  return POST(
    "/oauth2/token",
    new URLSearchParams({
      grant_type: "authorization_code",
      client_id: clientId,
      code,
      redirect_uri: redirectUri,
      code_verifier: codeVerifier
    }).toString(),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      }
    }
  );
}
