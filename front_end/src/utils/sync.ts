import {
  loadTransactions,
  getPendingDeletes,
  type LocalTransaction,
} from "@/stores/transactionStore";
import { getToken } from "@/utils/storage";

const STORAGE_KEY = "local_transactions";
// 自动同步间隔：30分钟
const SYNC_INTERVAL = 30 * 60 * 1000;
const SYNC_LAST_KEY = "sync_last_at";
function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

let syncTimer: number | null = null;
// 防止并发同步
let syncing = false;

// 直接从 storage 读取，避免与 store 的响应式引用混淆
function getLocalTransactions(): LocalTransaction[] {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function setLocalTransactions(list: LocalTransaction[]) {
  uni.setStorageSync(STORAGE_KEY, JSON.stringify(list));
}

function getLastSyncTime(): string {
  return uni.getStorageSync(SYNC_LAST_KEY) || "";
}

function setLastSyncTime() {
  uni.setStorageSync(SYNC_LAST_KEY, new Date().toISOString());
}

// 静默请求：不显示 loading，不弹 toast，用于后台同步
function silentRequest<T = any>(options: {
  url: string;
  method?: "GET" | "POST";
  data?: Record<string, any>;
}): Promise<T> {
  const token = getToken();
  const baseURL = import.meta.env?.VITE_API_BASE_URL || "";

  return new Promise((resolve, reject) => {
    uni.request({
      url: baseURL + options.url,
      method: options.method || "GET",
      data: options.data,
      header: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      success(res) {
        const { statusCode, data } = res as any;
        if (statusCode >= 200 && statusCode < 300) {
          if (data.code === 0 || data.code === 200) {
            resolve(data.data ?? data);
          } else {
            reject(new Error(data.message || "业务错误"));
          }
        } else {
          reject(new Error(`HTTP ${statusCode}`));
        }
      },
      fail(err) {
        reject(err);
      },
    });
  });
}

// 将本地 pending 状态的记录批量推送到服务端，成功则清除本地记录
export async function pushPending(): Promise<{ success: number; fail: number }> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return { success: 0, fail: 0 };

  const localList = getLocalTransactions();
  const pending = localList.filter((t) => t.sync_status === "pending");
  if (pending.length === 0) return { success: 0, fail: 0 };

  // 组装批量数据，只传服务端需要的字段
  const items = pending.map((tx) => ({
    book_id: tx.book_id,
    type: tx.type,
    amount: tx.amount,
    account_id: tx.account_id,
    to_account_id: tx.to_account_id,
    category_id: tx.category_id,
    remark: tx.remark,
    trans_time: tx.trans_time,
  }));

  try {
    const result = (await silentRequest({
      url: "/api/v1/transactions/batch",
      method: "POST",
      data: { items },
    })) as any;

    const count = result?.count || items.length;
    const syncedIds = new Set(pending.map((t) => t.id));
    const remaining = localList.filter((t) => !syncedIds.has(t.id));
    setLocalTransactions(remaining);
    loadTransactions();
    setLastSyncTime();

    return { success: count, fail: 0 };
  } catch {
    return { success: 0, fail: pending.length };
  }
}

// 拉取当月全量数据，合并到本地，跳过已存在的记录
// 将本地已删除且有 server_id 的记录批量同步到服务端做软删除
export async function pushDeletes(): Promise<number> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return 0;

  const pending = getPendingDeletes();
  if (pending.length === 0) return 0;

  const ids = pending.map((t) => t.server_id!).filter(Boolean);

  try {
    await silentRequest({
      url: "/api/v1/transactions/batch-delete",
      method: "POST",
      data: { ids },
    });

    const localList = getLocalTransactions();
    const deleteIds = new Set(pending.map((t) => t.id));
    const remaining = localList.filter((t) => !deleteIds.has(t.id));
    setLocalTransactions(remaining);
    loadTransactions();

    return ids.length;
  } catch {
    return 0;
  }
}

export async function pullFromServer(bookId: number): Promise<number> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return 0;

  try {
    const result = (await silentRequest({
      url: "/api/v1/transactions",
      method: "GET",
      data: {
        book_id: bookId,
        month: currentMonth(),
      },
    })) as any;

    const serverList = (result?.list || result) as any[];
    if (!Array.isArray(serverList) || serverList.length === 0) return 0;

    const localList = getLocalTransactions();

    // 按 server_id 或 id 去重，防止重复写入
    for (const item of serverList) {
      const exists = localList.find(
        (t) => t.server_id === item.id || t.id === item.id,
      );
      if (!exists) {
        localList.unshift({
          id: item.id,
          server_id: item.id,
          book_id: item.book_id,
          type: item.type,
          amount: item.amount,
          account_id: item.account_id,
          to_account_id: item.to_account_id,
          category_id: item.category_id,
          remark: item.remark || "",
          trans_time: item.trans_time,
          sync_status: "synced",
          deleted: 0,
          created_at: item.created_at || item.trans_time,
          updated_at: item.updated_at || item.trans_time,
        });
      }
    }

    setLocalTransactions(localList);
    loadTransactions();
    setLastSyncTime();

    return serverList.length;
  } catch {
    return 0;
  }
}

// 单次同步周期：先推后拉，syncing 锁防止并发
export async function runSyncCycle(bookId: number, silent = true): Promise<{
  pushed: number;
  pulled: number;
}> {
  const token = getToken();
  if (!token || token === "dev-mock-token") return { pushed: 0, pulled: 0 };
  if (syncing) return { pushed: 0, pulled: 0 };

  syncing = true;
  try {
    const deleted = await pushDeletes();
    const pushResult = await pushPending();
    const pulled = await pullFromServer(bookId);
    return { pushed: pushResult.success + deleted, pulled };
  } finally {
    syncing = false;
  }
}

export async function fullSync(bookId: number): Promise<{ pushed: number; pulled: number }> {
  return runSyncCycle(bookId, false);
}

// 启动后台同步：定时轮询 + 网络恢复时触发
export function scheduleSync(bookId: number) {
  stopSync();

  syncTimer = setInterval(() => {
    runSyncCycle(bookId, true);
  }, SYNC_INTERVAL) as unknown as number;

  // 断网恢复后立即同步一次
  uni.onNetworkStatusChange((res) => {
    if (res.isConnected) {
      runSyncCycle(bookId, true);
    }
  });
}

export function stopSync() {
  if (syncTimer) {
    clearInterval(syncTimer);
    syncTimer = null;
  }
}
