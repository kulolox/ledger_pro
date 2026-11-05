import { ref } from "vue";
import { getScopedItem, setScopedItem, removeScopedItem } from "@/utils/storage";

export interface LocalTransaction {
  id: string;
  server_id?: string;
  book_id: string;
  type: number;
  amount: number;
  account_id: string;
  to_account_id?: string;
  category_id?: string;
  remark?: string;
  trans_time: string;
  sync_status: "pending" | "synced" | "failed";
  deleted: number;
  created_at: string;
  updated_at: string;
}

const STORAGE_KEY = "local_transactions";

let nextLocalId = -1;
const transactions = ref<LocalTransaction[]>([]);

export function loadTransactions() {
  try {
    const list = getScopedItem<LocalTransaction[]>(STORAGE_KEY, []);
    if (list && Array.isArray(list)) {
      transactions.value = list;
      if (list.length > 0) {
        // 只提取本地负数临时 ID（如 "-1", "-2"），绝不对正数雪花算法大整数 ID 执行 Number() 转换
        const negativeIds = list
          .map((t) => String(t.id))
          .filter((id) => id.startsWith("-"))
          .map((id) => Number(id))
          .filter((n) => !Number.isNaN(n));
        const minId = negativeIds.length > 0 ? Math.min(...negativeIds) : 0;
        nextLocalId = minId < 0 ? minId - 1 : -1;
      }
    } else {
      transactions.value = [];
    }
  } catch {
    transactions.value = [];
  }
}

function persist() {
  setScopedItem(STORAGE_KEY, transactions.value);
}

export function addTransaction(data: {
  id?: string;
  server_id?: string;
  book_id: string;
  type: number;
  amount: number;
  account_id: string;
  to_account_id?: string;
  category_id?: string;
  remark?: string;
  trans_time: string;
  sync_status?: "pending" | "synced" | "failed";
  created_at?: string;
  updated_at?: string;
}): LocalTransaction {
  const now = new Date().toISOString();
  const txId = data.id || (data.server_id ? String(data.server_id) : String(nextLocalId--));
  const tx: LocalTransaction = {
    book_id: data.book_id,
    type: data.type,
    amount: data.amount,
    account_id: data.account_id,
    to_account_id: data.to_account_id,
    category_id: data.category_id,
    remark: data.remark,
    trans_time: data.trans_time,
    id: txId,
    server_id: data.server_id,
    sync_status: data.sync_status || "pending",
    deleted: 0,
    created_at: data.created_at || now,
    updated_at: data.updated_at || now,
  };
  transactions.value.unshift(tx);
  persist();
  return tx;
}

export function updateTransaction(
  id: string,
  data: Partial<
    Pick<
      LocalTransaction,
      | "amount"
      | "category_id"
      | "to_account_id"
      | "account_id"
      | "remark"
      | "trans_time"
      | "type"
      | "sync_status"
    >
  >,
) {
  const idx = transactions.value.findIndex((t) => t.id === id);
  if (idx === -1) return;
  const tx = transactions.value[idx];
  const updated: LocalTransaction = {
    ...tx,
    ...data,
    sync_status: data.sync_status || "pending",
    updated_at: new Date().toISOString(),
  };
  transactions.value[idx] = updated;
  persist();
}

export function deleteTransaction(id: string, hard = false) {
  const idx = transactions.value.findIndex((t) => t.id === id);
  if (idx === -1) return;
  const tx = transactions.value[idx];
  // 若显式指定硬删除（如在线删除已成功）或未落库的本地临时记录，直接物理移除
  if (hard || !tx.server_id) {
    transactions.value.splice(idx, 1);
  } else {
    // 离线软删除，标记为 pending 待恢复网络后上报
    tx.deleted = 1;
    tx.sync_status = "pending";
    tx.updated_at = new Date().toISOString();
  }
  persist();
}

export function markSynced(localId: string, serverId: string) {
  const idx = transactions.value.findIndex((t) => t.id === localId);
  if (idx === -1) return;
  transactions.value[idx] = {
    ...transactions.value[idx],
    sync_status: "synced",
    server_id: serverId,
    deleted: 0,
  };
  persist();
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
    if (tx.deleted === 1) continue;
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
    } else {
      group.income += tx.amount;
      group.expense += tx.amount;
    }
  }
  return map;
}

export function getPendingTransactions(): LocalTransaction[] {
  return transactions.value.filter(
    (t) => t.sync_status === "pending" && t.deleted === 0,
  );
}

export function getPendingDeletes(): LocalTransaction[] {
  return transactions.value.filter(
    (t) =>
      t.sync_status === "pending" && t.deleted === 1 && t.server_id != null,
  );
}

/**
 * 切换账号时重置内存状态（不清空当前账号本地持久化缓存）
 */
export function resetTransactionStore() {
  transactions.value = [];
  nextLocalId = -1;
}

/**
 * 清除当前账号的本地离线流水缓存及内存状态
 */
export function clearTransactions() {
  resetTransactionStore();
  removeScopedItem(STORAGE_KEY);
}

export function useTransactions() {
  return {
    transactions,
    load: loadTransactions,
    add: addTransaction,
    update: updateTransaction,
    delete: deleteTransaction,
    markSynced,
    getByDate: getTransactionsByDate,
    getPending: getPendingTransactions,
    getPendingDeletes,
    reset: resetTransactionStore,
    clear: clearTransactions,
  };
}
