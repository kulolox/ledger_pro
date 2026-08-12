const TOKEN_KEY = "token";
const REFRESH_TOKEN_KEY = "refreshToken";
const USER_KEY = "userInfo";

// JWT access token 存取
export const getToken = (): string => {
  return uni.getStorageSync(TOKEN_KEY) || "";
};

export const setToken = (token: string): void => {
  uni.setStorageSync(TOKEN_KEY, token);
};

export const removeToken = (): void => {
  uni.removeStorageSync(TOKEN_KEY);
};

// JWT refresh token 存取
export const getRefreshToken = (): string => {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || "";
};

export const setRefreshToken = (token: string): void => {
  uni.setStorageSync(REFRESH_TOKEN_KEY, token);
};

export const removeRefreshToken = (): void => {
  uni.removeStorageSync(REFRESH_TOKEN_KEY);
};

// 用户信息存取（JSON 序列化）
export const getUserInfo = <T = any>(): T | null => {
  const data = uni.getStorageSync(USER_KEY);
  return data ? JSON.parse(data) : null;
};

export const setUserInfo = <T = any>(info: T): void => {
  uni.setStorageSync(USER_KEY, JSON.stringify(info));
};

export const removeUserInfo = (): void => {
  uni.removeStorageSync(USER_KEY);
};

const BOOK_KEY = "currentBookId";

// 当前选中账本 ID 存取
export const getCurrentBookId = (): string => {
  const v = uni.getStorageSync(BOOK_KEY);
  return v == null ? "" : String(v);
};

export const setCurrentBookId = (bookId: string): void => {
  uni.setStorageSync(BOOK_KEY, bookId);
};

// 是否已登录（有 access token 即认为已登录）
export const isLoggedIn = (): boolean => {
  return !!getToken();
};

const ALL_KEYS = [
  TOKEN_KEY,
  REFRESH_TOKEN_KEY,
  USER_KEY,
  BOOK_KEY,
  "local_transactions",
  "category_cache",
  "category_etag",
  "sync_last_at",
];

// 清除全部 storage 数据
export const clearAllData = (): void => {
  ALL_KEYS.forEach((key) => uni.removeStorageSync(key));
};

// 登出
export const logout = (): void => {
  clearAllData();
};
