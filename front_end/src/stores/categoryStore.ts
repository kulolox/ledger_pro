import { ref } from "vue";
import { DEFAULT_CATEGORIES, type Category } from "@/constants/categories";
import { createCategory, updateCategory, deleteCategory, reorderCategories } from "@/api/common";
import { tryRefreshToken } from "@/utils/request";
import { getScopedItem, setScopedItem, removeScopedItem } from "@/utils/storage";

const CATEGORY_CACHE_KEY = "category_cache";
const CATEGORY_ETAG_KEY = "category_etag";

interface CategoryCache {
  categories: Record<number, Category[]>;
}

const categories = ref<Record<number, Category[]>>({
  0: [...(DEFAULT_CATEGORIES[0] || [])],
  1: [...(DEFAULT_CATEGORIES[1] || [])],
});

let loadPromise: Promise<void> | null = null;

function getCache(): CategoryCache | null {
  try {
    return getScopedItem<CategoryCache>(CATEGORY_CACHE_KEY);
  } catch {
    return null;
  }
}

function setCache(data: Record<number, Category[]>) {
  setScopedItem(CATEGORY_CACHE_KEY, { categories: data });
}

function getEtag(): string {
  return getScopedItem<string>(CATEGORY_ETAG_KEY) || "";
}

function setEtag(etag: string) {
  setScopedItem(CATEGORY_ETAG_KEY, etag);
}

async function fetchFromServer(
  force?: boolean,
  retry = false,
): Promise<{ data: Record<number, Category[]>; etag: string } | null> {
  const baseURL = import.meta.env?.["VITE_API_BASE_URL"] || "";
  const etag = force ? "" : getEtag();

  try {
    return await new Promise((resolve, reject) => {
      const header: Record<string, string> = {};
      const token = uni.getStorageSync("token");
      if (token) {
        header["Authorization"] = `Bearer ${token}`;
      }
      if (etag) {
        header["If-None-Match"] = etag;
      }

      uni.request({
        url: `${baseURL}/api/v1/categories`,
        method: "GET",
        header,
        success: (res) => {
          if (res.statusCode === 304) {
            const cache = getCache();
            if (cache) {
              resolve({ data: cache.categories, etag });
            } else {
              resolve(null);
            }
            return;
          }
          const body = res.data as Record<string, any>;
          if (res.statusCode >= 200 && res.statusCode < 300 && (body?.code === 0 || body?.code === 200)) {
            const list = body.data as Category[];
            const grouped: Record<number, Category[]> = {};
            for (const cat of list) {
              if (!grouped[cat.type]) grouped[cat.type] = [];
              grouped[cat.type].push(cat);
            }
            const headers = res.header as Record<string, string>;
            const newEtag = headers["ETag"] || headers["etag"] || "";
            resolve({ data: grouped, etag: newEtag });
          } else {
            const err = new Error("Failed to fetch categories") as Error & { statusCode: number };
            err.statusCode = res.statusCode;
            reject(err);
          }
        },
        fail: reject,
      });
    });
  } catch (error: any) {
    const is401 = error?.statusCode === 401 || error?.code === 401;
    if (is401 && !retry) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        return fetchFromServer(force, true);
      }
    }
    throw error;
  }
}

export function useCategories() {
  const loadCategories = async () => {
    if (loadPromise) return loadPromise;

    loadPromise = (async () => {
      const cache = getCache();
      if (cache) {
        categories.value = cache.categories;
      }

      try {
        const result = await fetchFromServer();
        if (result) {
          categories.value = result.data;
          setCache(result.data);
          if (result.etag) setEtag(result.etag);
        }
      } catch {
        if (!cache) {
          categories.value = {
            0: [...(DEFAULT_CATEGORIES[0] || [])],
            1: [...(DEFAULT_CATEGORIES[1] || [])],
          };
        }
      } finally {
        loadPromise = null;
      }
    })();

    return loadPromise;
  };

  const refreshCategories = async () => {
    if (loadPromise) return loadPromise;

    loadPromise = (async () => {
      try {
        const result = await fetchFromServer(true);
        if (result) {
          categories.value = result.data;
          setCache(result.data);
          if (result.etag) setEtag(result.etag);
        }
      } catch {
        // ignore
      } finally {
        loadPromise = null;
      }
    })();

    return loadPromise;
  };

  const addCategory = async (data: {
    name: string;
    type: number;
    icon: string;
    color: string;
    parent_id?: string;
  }) => {
    await createCategory(data);
    await refreshCategories();
  };

  const editCategory = async (
    id: string,
    data: { name?: string; icon?: string; color?: string; sort_order?: number; visible?: number },
  ) => {
    await updateCategory(id, data);
    await refreshCategories();
  };

  const removeCategory = async (
    id: string,
    options?: { targetCategoryId?: string; deleteTransactions?: boolean },
  ) => {
    await deleteCategory(id, options);
    await refreshCategories();
  };

  const reorder = async (ids: string[]) => {
    await reorderCategories({ ids });
    await refreshCategories();
  };

  const toggleVisible = async (id: string, visible: number) => {
    await updateCategory(id, { visible });
    await refreshCategories();
  };

  const getCategoryName = (type: number, categoryId?: string | number): string => {
    if (categoryId == null) return type === 0 ? "支出" : "收入";
    const cats = categories.value[type] || [];
    return cats.find((c) => String(c.id) === String(categoryId))?.name || "其他";
  };

  const getCategoryColor = (type: number, categoryId?: string | number): string => {
    if (categoryId == null) return "#607d8b";
    const cats = categories.value[type] || [];
    return cats.find((c) => String(c.id) === String(categoryId))?.color || "#607d8b";
  };

  const getCategoryIcon = (type: number, categoryId?: string | number): string => {
    if (categoryId == null) return "?";
    const cats = categories.value[type] || [];
    return cats.find((c) => String(c.id) === String(categoryId))?.icon || "?";
  };

  /**
   * 切换账号时重置分类内存状态（保留用户本地持久化缓存）
   */
  const resetCategoryStore = () => {
    categories.value = {
      0: [...(DEFAULT_CATEGORIES[0] || [])],
      1: [...(DEFAULT_CATEGORIES[1] || [])],
    };
    loadPromise = null;
  };

  /**
   * 彻底清空当前账号的分类本地缓存及内存状态
   */
  const clearCategories = () => {
    resetCategoryStore();
    removeScopedItem(CATEGORY_CACHE_KEY);
    removeScopedItem(CATEGORY_ETAG_KEY);
  };

  return {
    categories,
    loadCategories,
    refreshCategories,
    addCategory,
    editCategory,
    removeCategory,
    reorder,
    toggleVisible,
    getCategoryName,
    getCategoryColor,
    getCategoryIcon,
    reset: resetCategoryStore,
    clear: clearCategories,
  };
}
