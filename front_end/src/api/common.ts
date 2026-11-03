import request from "@/utils/request";
import { encryptPassword } from "@/utils/crypto";

// ========== 认证相关 ==========

export const login = async (data: { email: string; password: string }) => {
  const encryptedPassword = await encryptPassword(data.password);
  return request.post<{
    token: string;
    refreshToken: string;
    userInfo: UserInfo;
  }>("/api/v1/auth/login", {
    email: data.email,
    password: encryptedPassword,
  });
};

export const register = async (data: {
  email: string;
  password: string;
  nickname: string;
}) => {
  const encryptedPassword = await encryptPassword(data.password);
  console.log("encryptedPassword", encryptedPassword);
  return request.post<{
    token: string;
    refreshToken: string;
    userInfo: UserInfo;
  }>("/api/v1/auth/register", {
    email: data.email,
    password: encryptedPassword,
    nickname: data.nickname,
  });
};

export const sendResetCode = (data: { email: string }) => {
  return request.post("/api/v1/auth/send-reset-code", data);
};

export const resetPassword = async (data: {
  email: string;
  code: string;
  new_password: string;
}) => {
  const encryptedPassword = await encryptPassword(data.new_password);
  return request.post("/api/v1/auth/reset-password", {
    email: data.email,
    code: data.code,
    new_password: encryptedPassword,
  });
};

export const refreshToken = (data: { refresh_token: string }) => {
  return request.post<{
    token: string;
    refreshToken: string;
    userInfo: UserInfo;
  }>("/api/v1/auth/refresh", data);
};

export const logoutApi = (data: { refresh_token: string }) => {
  return request.post("/api/v1/auth/logout", data);
};

// ========== 账本相关 ==========

export const getAccountBooks = () => {
  return request.get<AccountBook[]>("/api/v1/account-books");
};

export const createAccountBook = (data: {
  name: string;
  coverIcon?: string;
  type?: number;
}) => {
  return request.post<AccountBook>("/api/v1/account-books", {
    ...data,
    type: data.type ?? 0,
  });
};

export const updateAccountBook = (
  id: string,
  data: {
    name: string;
    coverIcon?: string;
    type?: number;
  },
) => {
  return request.put<AccountBook>(`/api/v1/account-books/${id}`, {
    ...data,
    type: data.type ?? 0,
  });
};

export interface AccountBook {
  id: string;
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

export const changePassword = async (data: {
  old_password: string;
  new_password: string;
}) => {
  const encryptedOldPassword = await encryptPassword(data.old_password);
  const encryptedNewPassword = await encryptPassword(data.new_password);
  return request.put("/api/v1/user/password", {
    old_password: encryptedOldPassword,
    new_password: encryptedNewPassword,
  });
};

export const deleteAccount = () => {
  return request.delete("/api/v1/user/account");
};

// ========== 记账相关 ==========

export const getTransactions = (params: {
  book_id: string;
  month: string;
  page?: number;
  size?: number;
}) => {
  return request.get<{ list: Transaction[]; total: number; page: number }>(
    "/api/v1/transactions",
    params,
  );
};

export const createTransaction = (data: {
  book_id: string;
  type: number;
  amount: number;
  account_id: string;
  to_account_id?: string;
  category_id?: string;
  remark?: string;
  trans_time?: string;
}) => {
  return request.post<Transaction>("/api/v1/transactions", data);
};

export const updateTransaction = (
  id: string,
  data: {
    book_id: string;
    type: number;
    amount: number;
    account_id: string;
    to_account_id?: string;
    category_id?: string;
    remark?: string;
    trans_time: string;
  },
) => {
  return request.put<Transaction>(`/api/v1/transactions/${id}`, data);
};

export const deleteTransaction = (id: string) => {
  return request.delete<void>(`/api/v1/transactions/${id}`);
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
  parent_id?: string;
}) => {
  return request.post<Category>("/api/v1/categories", data);
};

export const updateCategory = (
  id: string,
  data: {
    name?: string;
    icon?: string;
    color?: string;
    sort_order?: number;
    visible?: number;
  },
) => {
  return request.put<Category>(`/api/v1/categories/${id}`, data);
};

export const getCategoryTransactionCount = (id: string) => {
  return request.get<{ count: number }>(`/api/v1/categories/${id}/transaction-count`);
};

export const deleteCategory = (
  id: string,
  params?: { targetCategoryId?: string; deleteTransactions?: boolean },
) => {
  const query = new URLSearchParams();
  if (params?.targetCategoryId) {
    query.set("targetCategoryId", params.targetCategoryId);
  }
  if (params?.deleteTransactions) {
    query.set("deleteTransactions", "true");
  }
  const qs = query.toString();
  const url = qs ? `/api/v1/categories/${id}?${qs}` : `/api/v1/categories/${id}`;
  return request.delete(url);
};

export const reorderCategories = (data: { ids: string[] }) => {
  return request.put("/api/v1/categories/reorder", data);
};

// ========== 账户相关 ==========

export const getAccounts = (params: { book_id: string }) => {
  return request.get<Account[]>("/api/v1/accounts", params);
};

// ========== 类型定义 ==========

export interface UserInfo {
  id: string;
  nickname: string;
  avatar: string;
  email: string;
  phone: string;
  createdAt?: string;
}

export interface Transaction {
  id: string;
  book_id: string;
  type: number;
  amount: number;
  account_id: string;
  to_account_id: string;
  category_id: string;
  remark: string;
  trans_time: string;
}

export interface Category {
  id: string;
  name: string;
  type: number;
  icon: string;
  color: string;
  parent_id: string;
  is_system: number;
  sort_order: number;
  visible: number;
}

export interface Account {
  id: string;
  name: string;
  type: number;
  icon: string;
  currency: string;
  initial_balance?: number;
  is_include_total?: number;
  sort_order?: number;
  deleted?: number;
}

// ========== 搜索相关 ==========

export interface SearchParams {
  book_id?: string;
  keyword?: string;
  type?: number;
  category_id?: string;
  amount_min?: number;
  amount_max?: number;
  date_from?: string;
  date_to?: string;
  page?: number;
  size?: number;
}

export const searchTransactions = (params: SearchParams) => {
  return request.get<{ list: Transaction[]; total: number; page: number }>(
    "/api/v1/transactions/search",
    params,
  );
};

// ========== 导入导出相关 ==========

export const exportTransactions = (params: {
  book_id: string;
  month?: string;
  year?: string;
  format: string;
}) => {
  return request.download(
    "/api/v1/transactions/export",
    params as Record<string, any>,
  );
};

export const importTransactions = (
  bookId: string,
  filePath: string,
  format: string,
) => {
  return request.upload<{ imported: number; errors: string[]; total: number }>(
    "/api/v1/transactions/import",
    filePath,
    { book_id: bookId, format },
  );
};

export const downloadTemplate = (format: string) => {
  return request.download("/api/v1/transactions/template", { format });
};

// ========== 统计相关 ==========

export interface MonthlyStats {
  total_expense: number;
  total_income: number;
  expense_by_category: Array<{
    category_id: string;
    amount: number;
    count: number;
  }>;
  income_by_category: Array<{
    category_id: string;
    amount: number;
    count: number;
  }>;
}

export const getMonthlyStatistics = (params: {
  book_id: string;
  month: string;
}) => {
  return request.get<MonthlyStats>("/api/v1/statistics/monthly", params);
};

export const getYearlyStatistics = (params: {
  book_id: string;
  year: string | number;
}) => {
  return request.get<MonthlyStats>("/api/v1/statistics/yearly", params);
};

export const getTrend = (params: { book_id: string; months?: number }) => {
  return request.get<{ months: string[]; expense: number[]; income: number[] }>(
    "/api/v1/statistics/trend",
    params,
  );
};
