import { ref, computed } from "vue";

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
    const raw = uni.getStorageSync(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as LocalTransaction[];
      transactions.value = parsed;
      if (parsed.length > 0) {
        const minId = Math.min(...parsed.map((t) => Number(t.id)));
        nextLocalId = minId < 0 ? minId - 1 : -1;
      }
    }
  } catch {
    transactions.value = [];
  }
}

function persist() {
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(transactions.value));
}

export function addTransaction(data: {
  book_id: string;
  type: number;
  amount: number;
  account_id: string;
  to_account_id?: string;
  category_id?: string;
  remark?: string;
  trans_time: string;
}): LocalTransaction {
  const now = new Date().toISOString();
  const tx: LocalTransaction = {
    id: String(nextLocalId--),
    ...data,
    sync_status: "pending",
    deleted: 0,
    created_at: now,
    updated_at: now,
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
    >
  >,
) {
  const idx = transactions.value.findIndex((t) => t.id === id);
  if (idx === -1) return;
  const tx = transactions.value[idx];
  const updated = { ...tx, ...data, updated_at: new Date().toISOString() };
  if (updated.sync_status === "synced") {
    updated.sync_status = "pending";
  }
  transactions.value[idx] = updated;
  persist();
}

export function deleteTransaction(id: string) {
  const idx = transactions.value.findIndex((t) => t.id === id);
  if (idx === -1) return;
  const tx = transactions.value[idx];
  if (tx.sync_status === "pending" && !tx.server_id) {
    transactions.value.splice(idx, 1);
  } else {
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

export function clearTransactions() {
  transactions.value = [];
  nextLocalId = -1;
  uni.removeStorageSync(STORAGE_KEY);
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
    clear: clearTransactions,
  };
}
