import { ref } from "vue";
import { DEFAULT_CATEGORIES, type Category } from "@/constants/categories";
import { createCategory, updateCategory, deleteCategory, reorderCategories } from "@/api/common";

const CATEGORY_CACHE_KEY = "category_cache";
const CATEGORY_ETAG_KEY = "category_etag";

interface CategoryCache {
  categories: Record<number, Category[]>;
}

const categories = ref<Record<number, Category[]>>({
  0: [...(DEFAULT_CATEGORIES[0] || [])],
  1: [...(DEFAULT_CATEGORIES[1] || [])],
});

function getCache(): CategoryCache | null {
  try {
    const raw = uni.getStorageSync(CATEGORY_CACHE_KEY);
    if (!raw) return null;
    return JSON.parse(raw) as CategoryCache;
  } catch {
    return null;
  }
}

function setCache(data: Record<number, Category[]>) {
  uni.setStorageSync(CATEGORY_CACHE_KEY, JSON.stringify({ categories: data }));
}

function getEtag(): string {
  return uni.getStorageSync(CATEGORY_ETAG_KEY) || "";
}

function setEtag(etag: string) {
  uni.setStorageSync(CATEGORY_ETAG_KEY, etag);
}

function fetchFromServer(
  force?: boolean,
): Promise<{ data: Record<number, Category[]>; etag: string } | null> {
  const baseURL = import.meta.env?.["VITE_API_BASE_URL"] || "";
  const etag = force ? "" : getEtag();

  return new Promise((resolve, reject) => {
    const header: Record<string, string> = {};
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
          reject(new Error("Failed to fetch categories"));
        }
      },
      fail: reject,
    });
  });
}

export function useCategories() {
  const loadCategories = async () => {
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
    }
  };

  const refreshCategories = async () => {
    try {
      const result = await fetchFromServer(true);
      if (result) {
        categories.value = result.data;
        setCache(result.data);
        if (result.etag) setEtag(result.etag);
      }
    } catch {
      // ignore
    }
  };

  const addCategory = async (data: {
    name: string;
    type: number;
    icon: string;
    color: string;
    parent_id?: number;
  }) => {
    await createCategory(data);
    await refreshCategories();
  };

  const editCategory = async (
    id: number,
    data: { name?: string; icon?: string; color?: string; sort_order?: number; visible?: number },
  ) => {
    await updateCategory(id, data);
    await refreshCategories();
  };

  const removeCategory = async (id: number) => {
    await deleteCategory(id);
    await refreshCategories();
  };

  const reorder = async (ids: number[]) => {
    await reorderCategories({ ids });
    await refreshCategories();
  };

  const toggleVisible = async (id: number, visible: number) => {
    await updateCategory(id, { visible });
    await refreshCategories();
  };

  const getCategoryName = (type: number, categoryId?: number): string => {
    if (categoryId == null) return type === 0 ? "支出" : "收入";
    const cats = categories.value[type] || [];
    return cats.find((c) => c.id === categoryId)?.name || "其他";
  };

  const getCategoryColor = (type: number, categoryId?: number): string => {
    if (categoryId == null) return "#607d8b";
    const cats = categories.value[type] || [];
    return cats.find((c) => c.id === categoryId)?.color || "#607d8b";
  };

  const getCategoryIcon = (type: number, categoryId?: number): string => {
    if (categoryId == null) return "?";
    const cats = categories.value[type] || [];
    return cats.find((c) => c.id === categoryId)?.icon || "?";
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
  };
}
