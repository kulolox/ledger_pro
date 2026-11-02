import "./polyfill";
import forge from "node-forge";
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

/**
 * 获取加密凭证并加密登录密码，密文传输
 * 采用 100% 纯 JavaScript (node-forge) 的 RSA-OAEP + SHA-256 + MGF1(SHA-256) 加密
 * 无任何 WebCrypto (crypto.subtle) 浏览器专属依赖，在 H5、小程序、Android/iOS App 均可无缝跨端运行
 */
export async function encryptPassword(plaintext: string): Promise<string> {
  const rawKey = await fetchPublicKey();
  const pem = `-----BEGIN PUBLIC KEY-----\n${rawKey}\n-----END PUBLIC KEY-----`;
  const publicKey = forge.pki.publicKeyFromPem(pem);

  const encryptedBytes = publicKey.encrypt(plaintext, "RSA-OAEP", {
    md: forge.md.sha256.create(),
    mgf1: {
      md: forge.md.sha256.create(),
    },
  });

  return forge.util.encode64(encryptedBytes);
}
