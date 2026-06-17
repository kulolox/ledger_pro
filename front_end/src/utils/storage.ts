const TOKEN_KEY = "token";
const REFRESH_TOKEN_KEY = "refreshToken";
const USER_KEY = "userInfo";

export const getToken = (): string => {
  return uni.getStorageSync(TOKEN_KEY) || "";
};

export const setToken = (token: string): void => {
  uni.setStorageSync(TOKEN_KEY, token);
};

export const removeToken = (): void => {
  uni.removeStorageSync(TOKEN_KEY);
};

export const getRefreshToken = (): string => {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || "";
};

export const setRefreshToken = (token: string): void => {
  uni.setStorageSync(REFRESH_TOKEN_KEY, token);
};

export const removeRefreshToken = (): void => {
  uni.removeStorageSync(REFRESH_TOKEN_KEY);
};

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

export const getCurrentBookId = (): number => {
  return uni.getStorageSync(BOOK_KEY);
};

export const setCurrentBookId = (bookId: number): void => {
  uni.setStorageSync(BOOK_KEY, bookId);
};

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

export const clearAllData = (): void => {
  ALL_KEYS.forEach((key) => uni.removeStorageSync(key));
};

export const logout = (): void => {
  clearAllData();
};
