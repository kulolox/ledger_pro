export interface Category {
  id: number;
  name: string;
  type: number;
  icon: string;
  color: string;
  is_system?: number;
  parent_id?: number;
  sort_order?: number;
  visible?: number;
}

export const DEFAULT_CATEGORIES: Record<number, Category[]> = {
  0: [
    { id: 1, name: "餐饮", type: 0, icon: "🍔", color: "#ff6b35" },
    { id: 2, name: "交通", type: 0, icon: "🚌", color: "#5b9bd5" },
    { id: 3, name: "购物", type: 0, icon: "🛍", color: "#ff6b8a" },
    { id: 4, name: "娱乐", type: 0, icon: "🎮", color: "#a29bfe" },
    { id: 5, name: "居家", type: 0, icon: "🏠", color: "#fdcb6e" },
    { id: 6, name: "通讯", type: 0, icon: "📱", color: "#00b894" },
    { id: 7, name: "医疗", type: 0, icon: "💊", color: "#e17055" },
    { id: 8, name: "教育", type: 0, icon: "📚", color: "#00cec9" },
    { id: 9, name: "人情", type: 0, icon: "🎁", color: "#fd79a8" },
    { id: 10, name: "其他", type: 0, icon: "💸", color: "#636e72" },
  ],
  1: [
    { id: 101, name: "工资", type: 1, icon: "💰", color: "#00b894" },
    { id: 102, name: "兼职", type: 1, icon: "💼", color: "#5b9bd5" },
    { id: 103, name: "理财", type: 1, icon: "📈", color: "#fdcb6e" },
    { id: 104, name: "红包", type: 1, icon: "🧧", color: "#ff6b6b" },
    { id: 105, name: "退款", type: 1, icon: "↩️", color: "#00cec9" },
    { id: 106, name: "其他", type: 1, icon: "💵", color: "#636e72" },
  ],
};

export function getCategoryName(type: number, categoryId?: number): string {
  if (categoryId == null) return type === 0 ? "支出" : "收入";
  const cats = DEFAULT_CATEGORIES[type] || [];
  return cats.find((c) => c.id === categoryId)?.name || "其他";
}

export function getCategoryColor(type: number, categoryId?: number): string {
  if (categoryId == null) return "#607d8b";
  const cats = DEFAULT_CATEGORIES[type] || [];
  return cats.find((c) => c.id === categoryId)?.color || "#607d8b";
}
