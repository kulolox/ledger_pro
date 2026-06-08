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

      const token = uni.getStorageSync("token");
      const isMock = token === "dev-mock-token";

      if (isMock) {
        return Promise.reject(error);
      }

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
          uni.navigateTo({
            url: "/pages/login/index",
          });
        }
      } else if (error.name === "HttpError") {
        // HTTP错误
        uni.showToast({
          title: `网络错误: ${error.statusCode}`,
          icon: "none",
          duration: 2000,
        });
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
    if (config.showLoading !== false) {
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
      // 执行错误拦截器
      let result = error;
      for (const interceptor of this.errorInterceptors.getInterceptors()) {
        result = interceptor(result);
      }

      return Promise.reject(result);
    } finally {
      // 隐藏loading
      if (config.showLoading !== false) {
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
}

// 获取环境变量
const getEnv = (key: string): string => {
  // @ts-ignore
  return import.meta.env?.[key] || "";
};

// 创建默认实例
const request = new Request({
  baseURL: getEnv("VITE_API_BASE_URL"),
  timeout: 30000,
});

// 导出实例和类
export default request;
export { Request, type RequestConfig, type ResponseData };
