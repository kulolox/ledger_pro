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
    it("getCurrentBookId returns undefined when not set", () => {
      expect(storage.getCurrentBookId()).toBeUndefined();
    });

    it("setCurrentBookId and getCurrentBookId roundtrip", () => {
      storage.setCurrentBookId(42);
      expect(storage.getCurrentBookId()).toBe(42);
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
    it("clears all stored data", () => {
      storage.setToken("t1");
      storage.setRefreshToken("r1");
      storage.setUserInfo({ id: 1 });
      storage.setCurrentBookId(99);
      (uni.setStorageSync as any)("local_transactions", [{ id: 1 }]);
      (uni.setStorageSync as any)("category_cache", { "0": [] });
      (uni.setStorageSync as any)("category_etag", "etag1");
      (uni.setStorageSync as any)("sync_last_at", Date.now());

      storage.logout();

      expect(storage.getToken()).toBe("");
      expect(storage.getRefreshToken()).toBe("");
      expect(storage.getUserInfo()).toBeNull();
      expect(storage.getCurrentBookId()).toBeUndefined();
    });
  });
});
