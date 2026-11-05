import { describe, it, expect, beforeEach, vi } from "vitest";

const mockStorage: Record<string, any> = {};

beforeEach(() => {
  Object.keys(mockStorage).forEach((k) => delete mockStorage[k]);
  (uni.getStorageSync as any).mockImplementation((key: string) => {
    return mockStorage[key];
  });
  (uni.setStorageSync as any).mockImplementation((key: string, value: any) => {
    mockStorage[key] = value;
  });
  (uni.removeStorageSync as any).mockImplementation((key: string) => {
    delete mockStorage[key];
  });
});

describe("storage", () => {
  let storage: typeof import("../storage");

  beforeEach(async () => {
    vi.resetModules();
    storage = await import("../storage");
  });

  describe("token", () => {
    it("getToken returns empty string when not set", () => {
      expect(storage.getToken()).toBe("");
    });

    it("setToken and getToken roundtrip", () => {
      storage.setToken("test-token");
      expect(storage.getToken()).toBe("test-token");
    });

    it("removeToken clears token", () => {
      storage.setToken("test-token");
      storage.removeToken();
      expect(storage.getToken()).toBe("");
    });
  });

  describe("refreshToken", () => {
    it("getRefreshToken returns empty string when not set", () => {
      expect(storage.getRefreshToken()).toBe("");
    });

    it("setRefreshToken and getRefreshToken roundtrip", () => {
      storage.setRefreshToken("test-refresh");
      expect(storage.getRefreshToken()).toBe("test-refresh");
    });

    it("removeRefreshToken clears refreshToken", () => {
      storage.setRefreshToken("test-refresh");
      storage.removeRefreshToken();
      expect(storage.getRefreshToken()).toBe("");
    });
  });

  describe("userInfo", () => {
    it("getUserInfo returns null when not set", () => {
      expect(storage.getUserInfo()).toBeNull();
    });

    it("setUserInfo and getUserInfo roundtrip", () => {
      storage.setUserInfo({ id: 1, nickname: "test" });
      expect(storage.getUserInfo()).toEqual({ id: 1, nickname: "test" });
    });

    it("removeUserInfo clears userInfo", () => {
      storage.setUserInfo({ id: 1 });
      storage.removeUserInfo();
      expect(storage.getUserInfo()).toBeNull();
    });
  });

  describe("currentBookId", () => {
    it("getCurrentBookId returns empty string when not set", () => {
      expect(storage.getCurrentBookId()).toBe("");
    });

    it("setCurrentBookId and getCurrentBookId roundtrip", () => {
      storage.setCurrentBookId("42");
      expect(storage.getCurrentBookId()).toBe("42");
    });
  });

  describe("isLoggedIn", () => {
    it("returns false when no token", () => {
      expect(storage.isLoggedIn()).toBe(false);
    });

    it("returns true when token exists", () => {
      storage.setToken("test-token");
      expect(storage.isLoggedIn()).toBe(true);
    });
  });

  describe("logout", () => {
    it("clears session data while retaining user scoped data", () => {
      storage.setToken("t1");
      storage.setRefreshToken("r1");
      storage.setUserInfo({ id: 1 });
      storage.setCurrentBookId("99");
      storage.setScopedItem("local_transactions", [{ id: "tx-1" }]);

      // 登出：清除会话，但保留用户1的隔离数据
      storage.logout();

      expect(storage.getToken()).toBe("");
      expect(storage.getRefreshToken()).toBe("");
      expect(storage.getUserInfo()).toBeNull();
      // 未登录时（guest），获取不到用户1的 currentBookId
      expect(storage.getCurrentBookId()).toBe("");

      // 用户1重新登录，数据无缝恢复
      storage.setUserInfo({ id: 1 });
      expect(storage.getCurrentBookId()).toBe("99");
      expect(storage.getScopedItem("local_transactions")).toEqual([{ id: "tx-1" }]);
    });
  });

  describe("user-scoped namespace isolation", () => {
    it("isolates data between two users completely", () => {
      // 模拟用户 A (id: 101)
      storage.setUserInfo({ id: 101, email: "user_a@example.com" });
      storage.setCurrentBookId("book-101");
      storage.setScopedItem("category_cache", { 0: [{ id: "c1", name: "餐饮" }] });
      storage.setScopedItem("local_transactions", [{ id: "tx-101", amount: 500 }]);

      // 验证底层存储 key 带有 u_101: 前缀
      expect(mockStorage["u_101:currentBookId"]).toBe("book-101");
      expect(mockStorage["u_101:local_transactions"]).toContain("tx-101");

      // 切换到用户 B (id: 202)
      storage.logout();
      storage.setUserInfo({ id: 202, email: "user_b@example.com" });

      // 用户 B 不会读取到用户 A 的任何缓存
      expect(storage.getCurrentBookId()).toBe("");
      expect(storage.getScopedItem("local_transactions")).toBeNull();
      expect(storage.getScopedItem("category_cache")).toBeNull();

      // 用户 B 写入自己的数据
      storage.setCurrentBookId("book-202");
      storage.setScopedItem("local_transactions", [{ id: "tx-202", amount: 888 }]);

      expect(mockStorage["u_202:currentBookId"]).toBe("book-202");
      expect(mockStorage["u_101:currentBookId"]).toBe("book-101"); // 用户 A 数据不受影响

      // 切换回用户 A
      storage.logout();
      storage.setUserInfo({ id: 101, email: "user_a@example.com" });

      expect(storage.getCurrentBookId()).toBe("book-101");
      expect(storage.getScopedItem<any[]>("local_transactions")?.[0]?.id).toBe("tx-101");
    });

    it("clears user scoped data completely when requested (e.g. account deletion)", () => {
      storage.setUserInfo({ id: 888 });
      storage.setCurrentBookId("book-888");
      storage.setScopedItem("local_transactions", [{ id: "tx-888" }]);

      expect(mockStorage["u_888:currentBookId"]).toBe("book-888");

      storage.clearUserScopedData(888);

      expect(mockStorage["u_888:currentBookId"]).toBeUndefined();
      expect(mockStorage["u_888:local_transactions"]).toBeUndefined();
    });
  });
});
