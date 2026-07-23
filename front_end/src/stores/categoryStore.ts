import { ref } from "vue";
import { select, insert, updateById, execute } from "@/utils/db";
import { DEFAULT_CATEGORIES, type Category } from "@/constants/categories";

const categories = ref<Record<number, Category[]>>({
  0: [...(DEFAULT_CATEGORIES[0] || [])],
  1: [...(DEFAULT_CATEGORIES[1] || [])],
});

let loadPromise: Promise<void> | null = null;

async function seedDefaults() {
  for (const type of [0, 1]) {
    const defaults = DEFAULT_CATEGORIES[type] || [];
    for (const cat of defaults) {
      await insert("categories", {
        id: cat.id,
        name: cat.name,
        type: cat.type,
        icon: cat.icon,
        color: cat.color,
        is_system: 1,
        sort_order: cat.sort_order ?? 0,
      });
    }
  }
}

async function loadFromDB(): Promise<Record<number, Category[]>> {
  const rows = await select<Category>(
    "SELECT * FROM categories ORDER BY sort_order ASC, id ASC",
  );
  if (rows.length === 0) {
    await seedDefaults();
    const reloaded = await select<Category>(
      "SELECT * FROM categories ORDER BY sort_order ASC, id ASC",
    );
    const grouped: Record<number, Category[]> = {};
    for (const cat of reloaded) {
      if (!grouped[cat.type]) grouped[cat.type] = [];
      grouped[cat.type].push(cat);
    }
    return grouped;
  }
  const grouped: Record<number, Category[]> = {};
  for (const cat of rows) {
    if (!grouped[cat.type]) grouped[cat.type] = [];
    grouped[cat.type].push(cat);
  }
  return grouped;
}

export function useCategories() {
  const loadCategories = async () => {
    if (loadPromise) return loadPromise;

    loadPromise = (async () => {
      try {
        categories.value = await loadFromDB();
      } catch {
        // fallback: keep defaults
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
        categories.value = await loadFromDB();
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
    parent_id?: number;
  }) => {
    const rows = await select<{ maxSort: number }>(
      `SELECT MAX(sort_order) as maxSort FROM categories WHERE type = ${data.type}`,
    );
    const sortOrder =
      rows.length > 0 && rows[0].maxSort != null ? (rows[0].maxSort as number) + 1 : 0;
    await insert("categories", {
      name: data.name,
      type: data.type,
      icon: data.icon,
      color: data.color,
      parent_id: data.parent_id || 0,
      sort_order: sortOrder,
    });
    await refreshCategories();
  };

  const editCategory = async (
    id: number,
    data: {
      name?: string;
      icon?: string;
      color?: string;
      sort_order?: number;
      visible?: number;
    },
  ) => {
    await updateById("categories", id, data as Record<string, unknown>);
    await refreshCategories();
  };

  const removeCategory = async (id: number) => {
    await execute(`DELETE FROM categories WHERE id = ${id} AND is_system = 0`);
    await refreshCategories();
  };

  const reorder = async (ids: number[]) => {
    for (let i = 0; i < ids.length; i++) {
      await updateById("categories", ids[i], { sort_order: i });
    }
    await refreshCategories();
  };

  const toggleVisible = async (id: number, visible: number) => {
    await updateById("categories", id, { visible });
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

  const clearCategories = async () => {
    await execute("DELETE FROM categories");
    categories.value = {
      0: [...(DEFAULT_CATEGORIES[0] || [])],
      1: [...(DEFAULT_CATEGORIES[1] || [])],
    };
    loadPromise = null;
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
    clear: clearCategories,
  };
}
