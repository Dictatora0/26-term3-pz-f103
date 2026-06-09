import CoreConsts from "@/components/CoreConsts";

function randomString(length = 64) {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
  const values = new Uint8Array(length);
  window.crypto.getRandomValues(values);
  return Array.from(values, item => chars[item % chars.length]).join("");
}

function toBase64Url(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  bytes.forEach(item => {
    binary += String.fromCharCode(item);
  });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

export async function createPkcePair() {
  const verifier = randomString(64);
  const digest = await window.crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier));
  return { verifier, challenge: toBase64Url(digest) };
}

export function createOAuthState() {
  return randomString(32);
}

export function savePendingOAuth(verifier, state) {
  localStorage.setItem(CoreConsts.OAuth2PkceVerifier, verifier);
  localStorage.setItem(CoreConsts.OAuth2State, state);
}

export function getPendingOAuth() {
  return {
    verifier: localStorage.getItem(CoreConsts.OAuth2PkceVerifier) || "",
    state: localStorage.getItem(CoreConsts.OAuth2State) || ""
  };
}

export function clearPendingOAuth() {
  localStorage.removeItem(CoreConsts.OAuth2PkceVerifier);
  localStorage.removeItem(CoreConsts.OAuth2State);
}
