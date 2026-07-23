import request from "@/utils/request";
import { encryptPassword } from "@/utils/crypto";

// ========== 认证相关 ==========

export const login = async (data: { email: string; password: string }) => {
  const encryptedPassword = await encryptPassword(data.password);
  return request.post<{ token: string; refreshToken: string; userInfo: UserInfo }>(
    "/api/v1/auth/login",
    {
      email: data.email,
      password: encryptedPassword,
    },
  );
};

export const register = async (data: { email: string; password: string; nickname: string }) => {
  const encryptedPassword = await encryptPassword(data.password);
  return request.post<{ token: string; refreshToken: string; userInfo: UserInfo }>(
    "/api/v1/auth/register",
    {
      email: data.email,
      password: encryptedPassword,
      nickname: data.nickname,
    },
  );
};

export const sendResetCode = (data: { email: string }) => {
  return request.post("/api/v1/auth/send-reset-code", data);
};

export const resetPassword = async (data: { email: string; code: string; new_password: string }) => {
  const encryptedPassword = await encryptPassword(data.new_password);
  return request.post("/api/v1/auth/reset-password", {
    email: data.email,
    code: data.code,
    new_password: encryptedPassword,
  });
};

export const refreshToken = (data: { refresh_token: string }) => {
  return request.post<{ token: string; refreshToken: string; userInfo: UserInfo }>(
    "/api/v1/auth/refresh",
    data,
  );
};

export const logoutApi = (data: { refresh_token: string }) => {
  return request.post("/api/v1/auth/logout", data);
};

// ========== 账本相关 ==========

export const getAccountBooks = () => {
  return request.get<AccountBook[]>("/api/v1/account-books");
};

export const createAccountBook = (data: { name: string; coverIcon?: string; type?: number }) => {
  return request.post<AccountBook>("/api/v1/account-books", data);
};

export interface AccountBook {
  id: number;
  name: string;
  coverIcon: string;
  type: number;
}

// ========== 用户相关 ==========

export const getUserInfo = () => {
  return request.get<UserInfo>("/api/v1/user/info");
};

export const updateUserInfo = (data: Partial<UserInfo>) => {
  return request.put<UserInfo>("/api/v1/user/info", data);
};

// ========== 记账相关 ==========

export const getTransactions = (params: { book_id: number; month: string; page?: number; size?: number }) => {
  return request.get<{ list: Transaction[]; total: number; page: number }>("/api/v1/transactions", params);
};

export const createTransaction = (data: {
  book_id: number;
  type: number;
  amount: number;
  account_id: number;
  to_account_id?: number;
  category_id?: number;
  remark?: string;
  trans_time?: string;
}) => {
  return request.post<Transaction>("/api/v1/transactions", data);
};

export const updateTransaction = (id: number, data: {
  book_id: number;
  type: number;
  amount: number;
  account_id: number;
  to_account_id?: number;
  category_id?: number;
  remark?: string;
  trans_time: string;
}) => {
  return request.put<Transaction>(`/api/v1/transactions/${id}`, data);
};

// ========== 分类相关 ==========

export const getCategories = () => {
  return request.get<Category[]>("/api/v1/categories");
};

export const createCategory = (data: {
  name: string;
  type: number;
  icon: string;
  color: string;
  parent_id?: number;
}) => {
  return request.post<Category>("/api/v1/categories", data);
};

export const updateCategory = (
  id: number,
  data: { name?: string; icon?: string; color?: string; sort_order?: number; visible?: number },
) => {
  return request.put<Category>(`/api/v1/categories/${id}`, data);
};

export const deleteCategory = (id: number) => {
  return request.delete(`/api/v1/categories/${id}`);
};

export const reorderCategories = (data: { ids: number[] }) => {
  return request.put("/api/v1/categories/reorder", data);
};

// ========== 账户相关 ==========

export const getAccounts = (params: { book_id: number }) => {
  return request.get<Account[]>("/api/v1/accounts", params);
};

export const createAccount = (data: {
  book_id: number;
  name: string;
  type: number;
  icon?: string;
  currency?: string;
  initial_balance?: number;
  is_include_total?: number;
}) => request.post<Account>("/api/v1/accounts", data);

export const updateAccount = (id: number, data: {
  name?: string;
  type?: number;
  icon?: string;
  currency?: string;
  initial_balance?: number;
  is_include_total?: number;
}) => request.put<Account>(`/api/v1/accounts/${id}`, data);

export const deleteAccount = (id: number, book_id: number) =>
  request.delete(`/api/v1/accounts/${id}?book_id=${book_id}`);

// ========== 类型定义 ==========

export interface UserInfo {
  id: number;
  nickname: string;
  avatar: string;
  email: string;
  phone: string;
}

export interface Transaction {
  id: number;
  book_id: number;
  type: number;
  amount: number;
  account_id: number;
  to_account_id: number;
  category_id: number;
  remark: string;
  trans_time: string;
}

export interface Category {
  id: number;
  name: string;
  type: number;
  icon: string;
  color: string;
  parent_id: number;
  is_system: number;
  sort_order: number;
  visible: number;
}

export interface Account {
  id: number;
  name: string;
  type: number;
  icon: string;
  currency: string;
  initial_balance?: number;
  is_include_total?: number;
  sort_order?: number;
  deleted?: number;
}

// ========== 导入导出相关 ==========

export const exportTransactions = (params: { book_id: number; month?: string; year?: string; format: string }) => {
  return request.download("/api/v1/transactions/export", params as Record<string, any>);
};

export const importTransactions = (bookId: number, filePath: string, format: string) => {
  return request.upload<{ imported: number; errors: string[]; total: number }>(
    "/api/v1/transactions/import",
    filePath,
    { book_id: bookId, format },
  );
};

export const downloadTemplate = (format: string) => {
  return request.download("/api/v1/transactions/template", { format });
};

// ========== 预算相关 ==========

export const listBudgets = (params: { book_id: number; month: string }) => {
  return request.get<any[]>("/api/v1/budgets", params);
};

export const createBudget = (data: {
  book_id: number;
  category_id?: number | null;
  amount: number;
  period_type: number;
  start_date: string;
}) => request.post<any>("/api/v1/budgets", data);

export const updateBudget = (id: number, data: any) =>
  request.put<any>(`/api/v1/budgets/${id}`, data);

export const deleteBudget = (id: number, book_id: number) =>
  request.delete(`/api/v1/budgets/${id}?book_id=${book_id}`);

// ========== 统计相关 ==========

export interface MonthlyStats {
  total_expense: number;
  total_income: number;
  expense_by_category: Array<{ category_id: number; amount: number; count: number }>;
  income_by_category: Array<{ category_id: number; amount: number; count: number }>;
}

export const getMonthlyStatistics = (params: { book_id: number; month: string }) => {
  return request.get<MonthlyStats>("/api/v1/statistics/monthly", params);
};

export const getTrend = (params: { book_id: number; months?: number }) => {
  return request.get<{ months: string[]; expense: number[]; income: number[] }>("/api/v1/statistics/trend", params);
};
