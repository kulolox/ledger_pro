import { describe, it, expect, beforeEach, vi } from "vitest";
import { DEFAULT_CATEGORIES } from "@/constants/categories";

const mockStorage: Record<string, any> = {};

beforeEach(() => {
  Object.keys(mockStorage).forEach((k) => delete mockStorage[k]);
  (uni.getStorageSync as any).mockImplementation((key: string) => mockStorage[key]);
  (uni.setStorageSync as any).mockImplementation((key: string, value: any) => {
    mockStorage[key] = value;
  });
  (uni.removeStorageSync as any).mockImplementation((key: string) => {
    delete mockStorage[key];
  });
  (uni.request as any).mockImplementation(() => {});
});

describe("categoryStore", () => {
  let store: ReturnType<typeof import("../categoryStore")["useCategories"]>;

  beforeEach(async () => {
    vi.resetModules();
    const mod = await import("../categoryStore");
    store = mod.useCategories();
  });

  describe("initial state", () => {
    it("has default expense categories", () => {
      const cats = store.categories.value[0];
      expect(cats.length).toBeGreaterThan(0);
    });

    it("has default income categories", () => {
      const cats = store.categories.value[1];
      expect(cats.length).toBeGreaterThan(0);
    });
  });

  describe("getCategoryName", () => {
    it("returns expense type label when categoryId is null", () => {
      expect(store.getCategoryName(0, undefined)).toBe("支出");
      expect(store.getCategoryName(1, undefined)).toBe("收入");
    });

    it("returns category name for valid id", () => {
      const firstCat = store.categories.value[0][0];
      const name = store.getCategoryName(0, firstCat.id);
      expect(name).toBe(firstCat.name);

      // 支持收入分类
      const incomeCat = store.categories.value[1][0];
      expect(store.getCategoryName(1, incomeCat.id)).toBe(incomeCat.name);

      // 支持数字类型的 id 匹配
      expect(store.getCategoryName(1, Number(incomeCat.id))).toBe(incomeCat.name);
    });

    it('returns "其他" for invalid id', () => {
      expect(store.getCategoryName(0, "99999")).toBe("其他");
    });
  });

  describe("getCategoryColor", () => {
    it("returns default color for null categoryId", () => {
      expect(store.getCategoryColor(0, undefined)).toBe("#607d8b");
    });

    it("returns category color for valid id", () => {
      const firstCat = store.categories.value[0][0];
      expect(store.getCategoryColor(0, firstCat.id)).toBe(firstCat.color);
    });

    it("returns default color for invalid id", () => {
      expect(store.getCategoryColor(0, "99999")).toBe("#607d8b");
    });
  });

  describe("getCategoryIcon", () => {
    it('returns "?" for null categoryId', () => {
      expect(store.getCategoryIcon(0, undefined)).toBe("?");
    });

    it("returns category icon for valid id", () => {
      const firstCat = store.categories.value[0][0];
      expect(store.getCategoryIcon(0, firstCat.id)).toBe(firstCat.icon);
    });

    it('returns "?" for invalid id', () => {
      expect(store.getCategoryIcon(0, "99999")).toBe("?");
    });
  });

  describe("clear", () => {
    it("resets to default categories and clears cache", () => {
      mockStorage["category_cache"] = "{}";
      mockStorage["category_etag"] = "some-etag";

      store.clear();

      expect(mockStorage["category_cache"]).toBeUndefined();
      expect(mockStorage["category_etag"]).toBeUndefined();
      expect(store.categories.value[0].length).toBe(DEFAULT_CATEGORIES[0].length);
    });
  });
});
