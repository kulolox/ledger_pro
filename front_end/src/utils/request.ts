/**
 * API请求封装
 * 基于uni.request实现，支持GET、POST方法，包含全局错误拦截
 */

// 扩展Error类型
declare global {
  interface Error {
    code?: number;
    statusCode?: number;
  }
}

// 请求配置接口
interface RequestConfig {
  url: string;
  method?: "GET" | "POST" | "PUT" | "DELETE" | "PATCH" | "OPTIONS" | "HEAD" | "TRACE" | "CONNECT";
  data?: Record<string, any>;
  header?: Record<string, string>;
  timeout?: number;
  showLoading?: boolean;
  loadingText?: string;
  _retry?: boolean;
}

// 响应数据接口
interface ResponseData<T = any> {
  code: number;
  data: T;
  message: string;
}

// 请求拦截器类型
type RequestInterceptor = (
  config: RequestConfig,
) => RequestConfig | Promise<RequestConfig>;

// 响应拦截器类型
type ResponseInterceptor = (response: any) => any;

// 错误拦截器类型
type ErrorInterceptor = (error: any) => any;

// 拦截器管理
class InterceptorManager<T> {
  private interceptors: Array<T> = [];

  use(interceptor: T): void {
    this.interceptors.push(interceptor);
  }

  eject(interceptor: T): void {
    const index = this.interceptors.indexOf(interceptor);
    if (index !== -1) {
      this.interceptors.splice(index, 1);
    }
  }

  getInterceptors(): Array<T> {
    return this.interceptors;
  }
}

// Token刷新锁，防止并发401重复刷新
let refreshPromise: Promise<boolean> | null = null;

const tryRefreshToken = async (): Promise<boolean> => {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    try {
      const refreshToken = uni.getStorageSync("refreshToken");
      if (!refreshToken) return false;

      const baseURL = getEnv("VITE_API_BASE_URL");
      const res = await new Promise<any>((resolve, reject) => {
        uni.request({
          url: baseURL + "/api/v1/auth/refresh",
          method: "POST",
          data: { refresh_token: refreshToken },
          success: resolve,
          fail: reject,
        });
      });

      if (
        res.statusCode >= 200 &&
        res.statusCode < 300 &&
        (res.data.code === 0 || res.data.code === 200)
      ) {
        uni.setStorageSync("token", res.data.data.token);
        uni.setStorageSync("refreshToken", res.data.data.refreshToken);
        return true;
      }
      return false;
    } catch {
      return false;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
};

// 获取环境变量
const getEnv = (key: string): string => {
  // @ts-ignore
  return import.meta.env?.[key] || "";
};

// 请求实例类
class Request {
  private baseURL: string;
  private timeout: number;
  private requestInterceptors: InterceptorManager<RequestInterceptor>;
  private responseInterceptors: InterceptorManager<ResponseInterceptor>;
  private errorInterceptors: InterceptorManager<ErrorInterceptor>;

  constructor(config: { baseURL?: string; timeout?: number } = {}) {
    this.baseURL = config.baseURL || "";
    this.timeout = config.timeout || 30000;
    this.requestInterceptors = new InterceptorManager();
    this.responseInterceptors = new InterceptorManager();
    this.errorInterceptors = new InterceptorManager();

    // 添加默认请求拦截器
    this.requestInterceptors.use((config) => {
      // 添加token
      const token = uni.getStorageSync("token");
      if (token) {
        config.header = {
          ...config.header,
          Authorization: `Bearer ${token}`,
        };
      }

      // 添加时间戳防止缓存
      if (config.method === "GET") {
        config.data = {
          ...config.data,
          _t: Date.now(),
        };
      }

      return config;
    });

    // 添加默认响应拦截器
    this.responseInterceptors.use((response) => {
      const { statusCode, data } = response;

      // HTTP状态码检查
      if (statusCode >= 200 && statusCode < 300) {
        // 业务状态码检查
        if (data.code === 0 || data.code === 200) {
          return data.data;
        } else {
          // 业务错误
          const error = new Error(data.message || "请求失败") as Error & {
            code: number;
          };
          error.name = "BusinessError";
          error.code = data.code;
          throw error;
        }
      } else {
        // HTTP错误
        const error = new Error(`HTTP错误: ${statusCode}`) as Error & {
          statusCode: number;
        };
        error.name = "HttpError";
        error.statusCode = statusCode;
        throw error;
      }
    });

    // 添加默认错误拦截器
    this.errorInterceptors.use((error) => {
      console.error("请求错误:", error);

      // 处理不同类型的错误
      if (error.name === "BusinessError") {
        // 业务错误
        uni.showToast({
          title: error.message || "操作失败",
          icon: "none",
          duration: 2000,
        });

        // 特殊业务码处理
        if (error.code === 401) {
          // token过期，跳转登录
          uni.removeStorageSync("token");
          uni.removeStorageSync("refreshToken");
          uni.reLaunch({
            url: "/pages/login/index",
          });
        }
      } else if (error.name === "HttpError") {
        // HTTP错误
        if (error.statusCode === 401) {
          uni.removeStorageSync("token");
          uni.removeStorageSync("refreshToken");
          uni.reLaunch({
            url: "/pages/login/index",
          });
        } else {
          uni.showToast({
            title: `网络错误: ${error.statusCode}`,
            icon: "none",
            duration: 2000,
          });
        }
      } else {
        // 网络错误或其他错误
        uni.showToast({
          title: error.message || "网络请求失败",
          icon: "none",
          duration: 2000,
        });
      }

      return Promise.reject(error);
    });
  }

  /**
   * 设置基础URL
   */
  setBaseURL(baseURL: string): void {
    this.baseURL = baseURL;
  }

  /**
   * 设置超时时间
   */
  setTimeout(timeout: number): void {
    this.timeout = timeout;
  }

  /**
   * 添加请求拦截器
   */
  addRequestInterceptor(interceptor: RequestInterceptor): void {
    this.requestInterceptors.use(interceptor);
  }

  /**
   * 添加响应拦截器
   */
  addResponseInterceptor(interceptor: ResponseInterceptor): void {
    this.responseInterceptors.use(interceptor);
  }

  /**
   * 添加错误拦截器
   */
  addErrorInterceptor(interceptor: ErrorInterceptor): void {
    this.errorInterceptors.use(interceptor);
  }

  /**
   * 通用请求方法
   */
  private async request<T = any>(config: RequestConfig): Promise<T> {
    // 显示loading
    if (config.showLoading === true) {
      uni.showLoading({
        title: config.loadingText || "加载中...",
        mask: true,
      });
    }

    try {
      // 执行请求拦截器
      let requestConfig = { ...config };
      for (const interceptor of this.requestInterceptors.getInterceptors()) {
        requestConfig = await interceptor(requestConfig);
      }

      // 构建完整URL
      const url = this.baseURL + requestConfig.url;

      // 发起请求
      const response = await new Promise<any>((resolve, reject) => {
        uni.request({
          url,
          method: (requestConfig.method || "GET") as any,
          data: requestConfig.data,
          header: requestConfig.header,
          timeout: requestConfig.timeout || this.timeout,
          success: resolve,
          fail: reject,
        });
      });

      // 执行响应拦截器
      let result = response;
      for (const interceptor of this.responseInterceptors.getInterceptors()) {
        result = interceptor(result);
      }

      return result as T;
    } catch (error) {
      const err: any = error;
      const is401 = err?.code === 401 || err?.statusCode === 401;
      if (is401 && !config._retry) {
        const refreshed = await tryRefreshToken();
        if (refreshed) {
          return this.request({ ...config, _retry: true });
        }
        uni.removeStorageSync("token");
        uni.removeStorageSync("refreshToken");
      }

      // 执行错误拦截器
      let result = error;
      for (const interceptor of this.errorInterceptors.getInterceptors()) {
        result = interceptor(result);
      }

      return Promise.reject(result);
    } finally {
      // 隐藏loading
      if (config.showLoading === true) {
        uni.hideLoading();
      }
    }
  }

  /**
   * GET请求
   */
  get<T = any>(
    url: string,
    data?: Record<string, any>,
    config?: Partial<RequestConfig>,
  ): Promise<T> {
    return this.request<T>({
      url,
      method: "GET",
      data,
      ...config,
    });
  }

  /**
   * POST请求
   */
  post<T = any>(
    url: string,
    data?: Record<string, any>,
    config?: Partial<RequestConfig>,
  ): Promise<T> {
    return this.request<T>({
      url,
      method: "POST",
      data,
      ...config,
    });
  }

  /**
   * PUT请求
   */
  put<T = any>(
    url: string,
    data?: Record<string, any>,
    config?: Partial<RequestConfig>,
  ): Promise<T> {
    return this.request<T>({
      url,
      method: "PUT",
      data,
      ...config,
    });
  }

  /**
   * DELETE请求
   */
  delete<T = any>(
    url: string,
    data?: Record<string, any>,
    config?: Partial<RequestConfig>,
  ): Promise<T> {
    return this.request<T>({
      url,
      method: "DELETE",
      data,
      ...config,
    });
  }

  /**
   * PATCH请求
   */
  patch<T = any>(
    url: string,
    data?: Record<string, any>,
    config?: Partial<RequestConfig>,
  ): Promise<T> {
    return this.request<T>({
      url,
      method: "PATCH",
      data,
      ...config,
    });
  }

  /**
   * 文件下载（导出）
   */
  async download(url: string, params?: Record<string, any>): Promise<void> {
    const token = uni.getStorageSync("token");
    const query = params
      ? "?" +
        Object.entries(params)
          .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
          .join("&")
      : "";
    const fullUrl = this.baseURL + url + query;

    const response = await fetch(fullUrl, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    if (response.status === 401) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        return this.download(url, params);
      }
      uni.removeStorageSync("token");
      uni.removeStorageSync("refreshToken");
      uni.reLaunch({ url: "/pages/login/index" });
      return;
    }

    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`);
    }

    const blob = await response.blob();
    const contentDisposition = response.headers.get("Content-Disposition");
    let filename = "download";
    if (contentDisposition) {
      const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?([^;\s]+)/);
      if (match) {
        filename = decodeURIComponent(match[1]);
      }
    }

    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = blobUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(blobUrl);
  }

  /**
   * 文件上传（导入）
   */
  upload<T = any>(
    url: string,
    filePath: string,
    formData?: Record<string, any>,
  ): Promise<T> {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync("token");
      uni.uploadFile({
        url: this.baseURL + url,
        filePath,
        name: "file",
        formData,
        header: token ? { Authorization: `Bearer ${token}` } : {},
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 0 || data.code === 200) {
              resolve(data.data as T);
            } else {
              reject(new Error(data.message || "上传失败"));
            }
          } catch {
            reject(new Error("解析响应失败"));
          }
        },
        fail: (err) => {
          reject(err);
        },
      });
    });
  }
}

// 创建默认实例
const request = new Request({
  baseURL: getEnv("VITE_API_BASE_URL"),
  timeout: 30000,
});

// 导出实例和类与环境变量获取工具
export const getApiBaseUrl = (): string => getEnv("VITE_API_BASE_URL");
export default request;
export { Request, type RequestConfig, type ResponseData };
