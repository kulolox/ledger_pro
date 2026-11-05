/**
 * 应用版本工具（遵循 DCloud 官方 HTML5+ 原生规范）
 */

export interface AppRuntimeInfo {
  version: string; // 版本名称，对应 manifest.json 中的 versionName (如 "1.0.1")
  versionCode: number; // 版本代码，对应 manifest.json 中的 versionCode (如 101)
  appid: string; // 应用 AppID
  name: string; // 应用名称
}

/**
 * 默认基准版本（非 App 预览或未就绪时降级使用）
 */
export const APP_VERSION = "1.0.9";
export const APP_VERSION_CODE = 109;

function getFallbackRuntimeInfo(): AppRuntimeInfo {
  return {
    version: APP_VERSION,
    versionCode: APP_VERSION_CODE,
    appid: "__UNI__E03086B",
    name: "LedgerPro",
  };
}

/**
 * 获取当前应用运行时的真实版本信息
 * 在 App 端通过 DCloud 官方原生 API plus.runtime.getProperty 异步读取安装包或 WGT 补丁的实际配置
 * 在非 App 环境（如 H5、微信小程序、单测）自动降级返回基准配置
 */
export function getAppRuntimeInfo(): Promise<AppRuntimeInfo> {
  // #ifdef APP-PLUS
  return new Promise((resolve) => {
    try {
      if (typeof plus !== "undefined" && plus.runtime) {
        const appid = plus.runtime.appid || "";
        plus.runtime.getProperty(appid, (widgetInfo) => {
          if (widgetInfo) {
            const rawCode = String(widgetInfo.versionCode || "");
            const codeNum = parseInt(rawCode, 10);
            resolve({
              version: widgetInfo.version || APP_VERSION,
              versionCode: Number.isNaN(codeNum) ? APP_VERSION_CODE : codeNum,
              appid: widgetInfo.appid || plus.runtime.appid || "",
              name: widgetInfo.name || "LedgerPro",
            });
            return;
          }
          resolve(getFallbackRuntimeInfo());
        });
        return;
      }
    } catch (e) {
      console.warn("Failed to get plus runtime property:", e);
    }
    resolve(getFallbackRuntimeInfo());
  });
  // #endif

  // #ifndef APP-PLUS
  return Promise.resolve(getFallbackRuntimeInfo());
  // #endif
}
