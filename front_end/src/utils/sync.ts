import { getPendingTransactions, markSynced } from "@/stores/transactionStore";
import { createTransaction, getTransactions } from "@/api/common";
import { getToken } from "@/utils/storage";

const SYNC_LAST_KEY = "sync_last_at";
let syncing = false;

export function getLastSyncTime(): string {
  return uni.getStorageSync(SYNC_LAST_KEY) || "";
}

function setLastSyncTime() {
  uni.setStorageSync(SYNC_LAST_KEY, new Date().toISOString());
}

export async function pushPending(): Promise<{ success: number; fail: number }> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return { success: 0, fail: 0 };
  if (syncing) return { success: 0, fail: 0 };

  syncing = true;
  let success = 0;
  let fail = 0;

  try {
    const pending = getPendingTransactions();
    for (const tx of pending) {
      try {
        const result = await createTransaction({
          book_id: tx.book_id,
          type: tx.type,
          amount: tx.amount,
          account_id: tx.account_id,
          to_account_id: tx.to_account_id,
          category_id: tx.category_id,
          remark: tx.remark,
          trans_time: tx.trans_time,
        });
        markSynced(tx.id, (result as any).id || 0);
        success++;
      } catch {
        fail++;
      }
    }
    if (success > 0) setLastSyncTime();
  } finally {
    syncing = false;
  }

  return { success, fail };
}

export async function pullFromServer(): Promise<number> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return 0;
  if (syncing) return 0;

  syncing = true;
  let count = 0;

  try {
    const lastSync = getLastSyncTime();
    const result = await getTransactions({
      book_id: 1,
      page: 1,
      size: 100,
      start_time: lastSync || undefined,
    });
    const serverList = (result as any)?.list || [];
    count = serverList.length;

    if (count > 0) {
      const { loadTransactions } = await import("@/stores/transactionStore");
      const local = JSON.parse(uni.getStorageSync("local_transactions") || "[]");

      for (const serverItem of serverList) {
        const exists = local.find(
          (t: any) => t.server_id === serverItem.id || t.id === serverItem.id
        );
        if (!exists) {
          local.unshift({
            ...serverItem,
            id: serverItem.id,
            server_id: serverItem.id,
            sync_status: "synced",
            created_at: serverItem.trans_time,
            updated_at: serverItem.trans_time,
          });
        }
      }

      uni.setStorageSync("local_transactions", JSON.stringify(local));
      loadTransactions();
    }

    setLastSyncTime();
  } catch {
    // sync pull failed, will retry next time
  } finally {
    syncing = false;
  }

  return count;
}

export async function fullSync(): Promise<{ pushed: number; pulled: number }> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return { pushed: 0, pulled: 0 };

  const pushResult = await pushPending();
  const pulled = await pullFromServer();
  return { pushed: pushResult.success, pulled };
}

export function setupAutoSync() {
  uni.onNetworkStatusChange((res) => {
    if (res.isConnected) {
      pushPending();
    }
  });
}
