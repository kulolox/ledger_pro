import request from "./request";

let cachedPublicKey: string | null = null;

async function fetchPublicKey(): Promise<string> {
  if (cachedPublicKey) return cachedPublicKey;
  const res = await request.get<{ publicKey: string }>(
    "/api/v1/auth/public-key",
  );
  cachedPublicKey = res.publicKey;
  return cachedPublicKey;
}

function base64ToBytes(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

function bytesToBase64(bytes: Uint8Array): string {
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

async function importPublicKey(base64: string): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    "spki",
    base64ToBytes(base64),
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["encrypt"],
  );
}

// 获取加密凭证并加密登录密码，登录密码密文传递
export async function encryptPassword(plaintext: string): Promise<string> {
  const publicKeyBase64 = await fetchPublicKey();
  const publicKey = await importPublicKey(publicKeyBase64);
  const encrypted = await crypto.subtle.encrypt(
    { name: "RSA-OAEP" },
    publicKey,
    new TextEncoder().encode(plaintext),
  );
  return bytesToBase64(new Uint8Array(encrypted));
}
