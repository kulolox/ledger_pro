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
  try {
    const data = uni.getStorageSync(USER_KEY);
    return data ? JSON.parse(data) : null;
  } catch {
    return null;
  }
};

export const setUserInfo = <T = any>(info: T): void => {
  uni.setStorageSync(USER_KEY, JSON.stringify(info));
};

export const removeUserInfo = (): void => {
  uni.removeStorageSync(USER_KEY);
};

/**
 * 获取当前登录用户唯一标识 (ID)
 * 用于构建业务缓存的隔离命名空间
 */
export const getCurrentUserId = (): string => {
  const user = getUserInfo<{ id?: string | number }>();
  if (user && user.id != null && String(user.id).trim() !== "") {
    return String(user.id);
  }
  return "";
};

/**
 * 生成带用户命名空间前缀的缓存 Key
 * 规则：登录状态为 u_${userId}:${baseKey}，未登录/访客状态为 u_guest:${baseKey}
 */
export const getUserScopedKey = (
  baseKey: string,
  customUserId?: string | number,
): string => {
  const uid =
    customUserId != null && String(customUserId).trim() !== ""
      ? String(customUserId)
      : getCurrentUserId();
  return uid ? `u_${uid}:${baseKey}` : `u_guest:${baseKey}`;
};

/**
 * 命名空间隔离存取方法
 */
export const getScopedItem = <T = any>(
  baseKey: string,
  defaultValue?: T,
  customUserId?: string | number,
): T | null => {
  try {
    const key = getUserScopedKey(baseKey, customUserId);
    const raw = uni.getStorageSync(key);
    if (raw === "" || raw === null || raw === undefined) {
      return defaultValue ?? null;
    }
    try {
      return JSON.parse(raw) as T;
    } catch {
      return raw as unknown as T;
    }
  } catch {
    return defaultValue ?? null;
  }
};

export const setScopedItem = (
  baseKey: string,
  value: any,
  customUserId?: string | number,
): void => {
  try {
    const key = getUserScopedKey(baseKey, customUserId);
    const val = typeof value === "string" ? value : JSON.stringify(value);
    uni.setStorageSync(key, val);
  } catch (e) {
    console.error(`[storage] setScopedItem error for ${baseKey}:`, e);
  }
};

export const removeScopedItem = (
  baseKey: string,
  customUserId?: string | number,
): void => {
  try {
    const key = getUserScopedKey(baseKey, customUserId);
    uni.removeStorageSync(key);
  } catch (e) {
    console.error(`[storage] removeScopedItem error for ${baseKey}:`, e);
  }
};

const BOOK_BASE_KEY = "currentBookId";

// 当前选中账本 ID 存取（已按用户命名空间隔离）
export const getCurrentBookId = (): string => {
  const v = getScopedItem<string>(BOOK_BASE_KEY);
  return v == null ? "" : String(v);
};

export const setCurrentBookId = (bookId: string): void => {
  setScopedItem(BOOK_BASE_KEY, bookId);
};

export const removeCurrentBookId = (): void => {
  removeScopedItem(BOOK_BASE_KEY);
};

// 是否已登录（有 access token 即认为已登录）
export const isLoggedIn = (): boolean => {
  return !!getToken();
};

// 常见作用域业务缓存键列表
export const USER_SCOPED_KEYS = [
  BOOK_BASE_KEY,
  "local_transactions",
  "category_cache",
  "category_etag",
  "sync_last_at",
];

// 旧版本无前缀全局键（用于迁移兼容与主动清理）
const LEGACY_KEYS = [
  "currentBookId",
  "local_transactions",
  "category_cache",
  "category_etag",
  "sync_last_at",
];

/**
 * 清除旧版本遗留的无命名空间数据，防止旧全局数据干扰
 */
export const cleanLegacyUnscopedData = (): void => {
  LEGACY_KEYS.forEach((key) => {
    try {
      uni.removeStorageSync(key);
    } catch {
      // Ignore
    }
  });
};

/**
 * 彻底清除指定用户（或当前用户）的所有命名空间缓存
 * 常用于用户注销账号、或在设置中明确选择“清理本账号所有离线缓存”
 */
export const clearUserScopedData = (customUserId?: string | number): void => {
  const uid =
    customUserId != null && String(customUserId).trim() !== ""
      ? String(customUserId)
      : getCurrentUserId();
  if (!uid && uid !== "0") return;

  USER_SCOPED_KEYS.forEach((baseKey) => {
    try {
      uni.removeStorageSync(`u_${uid}:${baseKey}`);
    } catch {
      // Ignore
    }
  });
};

/**
 * 清除当前设备登录凭证（退出登录）
 * 注意：根据架构规范，普通退出登录安全保留原用户的本地命名空间缓存，
 * 仅清除会话凭证（token, refreshToken, userInfo），切回账号时可无缝恢复。
 */
export const clearUserSession = (): void => {
  removeToken();
  removeRefreshToken();
  removeUserInfo();
  cleanLegacyUnscopedData();
};

/**
 * 登出（保留用户离线数据，清除当前活动会话）
 */
export const logout = (): void => {
  clearUserSession();
};

/**
 * 彻底抹除设备上的所有缓存（慎用，仅在重置应用时调用）
 */
export const clearAllData = (): void => {
  const currentUid = getCurrentUserId();
  if (currentUid) {
    clearUserScopedData(currentUid);
  }
  clearUserScopedData("guest");
  clearUserSession();
};

// 启动时自动清理旧版残留的无前缀脏数据
cleanLegacyUnscopedData();
