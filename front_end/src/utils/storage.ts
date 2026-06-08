const TOKEN_KEY = "token";
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

export const isLoggedIn = (): boolean => {
  return !!getToken();
};

export const logout = (): void => {
  removeToken();
  removeUserInfo();
};
