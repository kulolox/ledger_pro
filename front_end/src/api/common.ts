import request from "@/utils/request";

// ========== 认证相关 ==========

export const login = (data: { email: string; password: string }) => {
  return request.post<{ token: string; userInfo: UserInfo }>("/api/v1/auth/login", data);
};

export const register = (data: { email: string; password: string; nickname: string }) => {
  return request.post<{ token: string; userInfo: UserInfo }>("/api/v1/auth/register", data);
};

export const sendResetCode = (data: { email: string }) => {
  return request.post("/api/v1/auth/send-reset-code", data);
};

export const resetPassword = (data: { email: string; code: string; new_password: string }) => {
  return request.post("/api/v1/auth/reset-password", data);
};

export const refreshToken = (data: { refresh_token: string }) => {
  return request.post<{ token: string }>("/api/v1/auth/refresh", data);
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

export const getTransactions = (params: { book_id: number; month: string }) => {
  return request.get<{ list: Transaction[]; total: number }>("/api/v1/transactions", params);
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

// ========== 分类相关 ==========

export const getCategories = (params: { book_id: number }) => {
  return request.get<Category[]>("/api/v1/categories", params);
};

// ========== 账户相关 ==========

export const getAccounts = (params: { book_id: number }) => {
  return request.get<Account[]>("/api/v1/accounts", params);
};

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
}

export interface Account {
  id: number;
  name: string;
  type: number;
  icon: string;
  currency: string;
}

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
