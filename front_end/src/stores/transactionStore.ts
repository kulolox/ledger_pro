import { ref } from "vue";
import { select, insert, updateById, execute } from "@/utils/db";

export interface LocalTransaction {
  id: number;
  type: number;
  amount: number;
  category_id: number;
  account_id: number;
  to_account_id?: number;
  remark?: string;
  trans_time: string;
  created_at: string;
  updated_at: string;
}

const transactions = ref<LocalTransaction[]>([]);

function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

export async function loadTransactions(month?: string): Promise<void> {
  const m = month || currentMonth();
  try {
    const rows = await select<LocalTransaction>(
      `SELECT * FROM transactions WHERE trans_time LIKE '${m}%' ORDER BY trans_time DESC, id DESC`,
    );
    transactions.value = rows;
  } catch {
    transactions.value = [];
  }
}

export async function addTransaction(data: {
  type: number;
  amount: number;
  category_id?: number;
  account_id: number;
  to_account_id?: number;
  remark?: string;
  trans_time: string;
}): Promise<void> {
  const now = new Date().toISOString();
  await insert("transactions", {
    type: data.type,
    amount: data.amount,
    category_id: data.category_id || 0,
    account_id: data.account_id,
    to_account_id: data.to_account_id || 0,
    remark: data.remark || "",
    trans_time: data.trans_time,
    created_at: now,
    updated_at: now,
  });
}

export async function updateTransaction(
  id: number,
  data: Partial<{
    amount: number;
    category_id: number;
    to_account_id: number;
    account_id: number;
    remark: string;
    trans_time: string;
    type: number;
  }>,
): Promise<void> {
  const now = new Date().toISOString();
  await updateById("transactions", id, { ...data, updated_at: now });
}

export async function deleteTransaction(id: number): Promise<void> {
  await execute(`DELETE FROM transactions WHERE id = ${id}`);
}

export function getTransactionsByDate(): Map<
  string,
  { income: number; expense: number; items: LocalTransaction[] }
> {
  const map = new Map<
    string,
    { income: number; expense: number; items: LocalTransaction[] }
  >();
  for (const tx of transactions.value) {
    const dateKey = tx.trans_time.substring(0, 10);
    if (!map.has(dateKey)) {
      map.set(dateKey, { income: 0, expense: 0, items: [] });
    }
    const group = map.get(dateKey)!;
    group.items.push(tx);
    if (tx.type === 1) {
      group.income += tx.amount;
    } else if (tx.type === 0) {
      group.expense += tx.amount;
    }
  }
  return map;
}

export async function clearTransactions(): Promise<void> {
  await execute("DELETE FROM transactions");
  transactions.value = [];
}

export function useTransactions() {
  return {
    transactions,
    load: loadTransactions,
    add: addTransaction,
    update: updateTransaction,
    delete: deleteTransaction,
    getByDate: getTransactionsByDate,
    clear: clearTransactions,
  };
}
