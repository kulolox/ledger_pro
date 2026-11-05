import {
  loadTransactions,
  type LocalTransaction,
} from "@/stores/transactionStore";
import { getToken, getScopedItem, setScopedItem } from "@/utils/storage";
import { tryRefreshToken } from "@/utils/request";
import { MAX_AMOUNT_CENTS } from "./format";

const STORAGE_KEY = "local_transactions";
// 自动同步间隔：30分钟
const SYNC_INTERVAL = 30 * 60 * 1000;
const SYNC_LAST_KEY = "sync_last_at";
function currentMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

let syncTimer: number | null = null;
let networkChangeHandler: ((res: any) => void) | null = null;
// 防止并发同步
let syncing = false;
const pullPromises = new Map<string, Promise<number>>();

// 直接从 scoped storage 读取，避免与 store 的响应式引用混淆
function getLocalTransactions(): LocalTransaction[] {
  try {
    const list = getScopedItem<LocalTransaction[]>(STORAGE_KEY, []);
    return Array.isArray(list) ? list : [];
  } catch {
    return [];
  }
}

function setLocalTransactions(list: LocalTransaction[]) {
  setScopedItem(STORAGE_KEY, list);
}

function getLastSyncTime(): string {
  return getScopedItem<string>(SYNC_LAST_KEY) || "";
}

function setLastSyncTime() {
  setScopedItem(SYNC_LAST_KEY, new Date().toISOString());
}

// 静默请求：不显示 loading，不弹 toast，用于后台同步
async function silentRequest<T = any>(
  options: {
    url: string;
    method?: "GET" | "POST" | "PUT";
    data?: Record<string, any>;
  },
  retry = false,
): Promise<T> {
  const token = getToken();
  const baseURL = import.meta.env?.VITE_API_BASE_URL || "";

  try {
    return await new Promise<T>((resolve, reject) => {
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
            const err = new Error(`HTTP ${statusCode}`) as Error & { statusCode: number };
            err.statusCode = statusCode;
            reject(err);
          }
        },
        fail(err) {
          reject(err);
        },
      });
    });
  } catch (error: any) {
    const is401 = error?.statusCode === 401 || error?.code === 401 || error?.message?.includes("401");
    if (is401 && !retry) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        return silentRequest<T>(options, true);
      }
    }
    throw error;
  }
}

// 将本地 pending 或 failed 状态且未删除的记录批量推送到服务端，成功则标记为已同步
export async function pushPending(): Promise<{ success: number; fail: number; error?: string }> {
  const localList = getLocalTransactions();
  // 1. 清理本地未上报却已被标记删除的历史无效数据（无需且不能上报）
  const sanitizedList = localList.filter((t) => !(t.deleted === 1 && !t.server_id));
  if (sanitizedList.length !== localList.length) {
    setLocalTransactions(sanitizedList);
  }

  // 待推送列表：包含 pending 和先前网络原因 failed 的记录，允许重新尝试推送
  const pending = sanitizedList.filter(
    (t) => (t.sync_status === "pending" || t.sync_status === "failed") && t.deleted !== 1,
  );
  if (pending.length === 0) return { success: 0, fail: 0 };

  let success = 0;
  let fail = 0;
  let lastError = "";

  // 1. 过滤本地非法/超限记录（如金额超过 2000 万元或 <=0）
  const invalidItems = pending.filter(
    (t) => !t.amount || t.amount <= 0 || t.amount > MAX_AMOUNT_CENTS,
  );
  if (invalidItems.length > 0) {
    const invalidIds = new Set(invalidItems.map((t) => t.id));
    const updated = getLocalTransactions().map((t) =>
      invalidIds.has(t.id) ? { ...t, sync_status: "failed" as const } : t,
    );
    setLocalTransactions(updated);
    fail += invalidItems.length;
    lastError = "存在单笔金额超过 2000 万元或无效的记录，无法同步";
  }

  const validPending = pending.filter(
    (t) => t.amount && t.amount > 0 && t.amount <= MAX_AMOUNT_CENTS,
  );
  const newItems = validPending.filter((t) => !t.server_id);
  const updateItems = validPending.filter((t) => t.server_id);

  // 新增：批量 POST
  if (newItems.length > 0) {
    const items = newItems.map((tx) => ({
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

      const serverList = result?.list || [];
      const localToServerId = new Map<string, string>();
      for (let i = 0; i < newItems.length; i++) {
        if (serverList[i]) {
          localToServerId.set(newItems[i].id, String(serverList[i].id));
        }
      }

      const updated = getLocalTransactions().map((t) => {
        const serverId = localToServerId.get(t.id);
        if (serverId !== undefined) {
          return { ...t, sync_status: "synced" as const, server_id: serverId };
        }
        return t;
      });
      setLocalTransactions(updated);
      success += result?.count || items.length;
    } catch (e: any) {
      console.error("[sync] pushPending batch failed:", e);
      fail += newItems.length;
      lastError = e?.message || "批量同步失败";
      // 将本批失败的记录标记为 failed，避免死循环阻塞后续正常账单的同步
      const batchIds = new Set(newItems.map((t) => t.id));
      const updated = getLocalTransactions().map((t) =>
        batchIds.has(t.id) ? { ...t, sync_status: "failed" as const } : t,
      );
      setLocalTransactions(updated);
    }
  }

  // 更新：逐条 PUT
  for (const item of updateItems) {
    try {
      await silentRequest({
        url: `/api/v1/transactions/${item.server_id}`,
        method: "PUT",
        data: {
          book_id: item.book_id,
          type: item.type,
          amount: item.amount,
          account_id: item.account_id,
          to_account_id: item.to_account_id,
          category_id: item.category_id,
          remark: item.remark,
          trans_time: item.trans_time,
        },
      });
      const updated = getLocalTransactions().map((t) =>
        t.id === item.id ? { ...t, sync_status: "synced" as const } : t,
      );
      setLocalTransactions(updated);
      success++;
    } catch (e: any) {
      console.error(`[sync] pushPending update ${item.id} failed:`, e);
      const isNotFound =
        e?.statusCode === 404 ||
        e?.code === 404 ||
        e?.message?.includes("404") ||
        e?.message?.includes("不存在");

      if (isNotFound) {
        // 本地未同步数据权重最大：若服务端记录已被远端删除，自动降级为新建重新入库
        try {
          const res = (await silentRequest({
            url: "/api/v1/transactions",
            method: "POST",
            data: {
              book_id: item.book_id,
              type: item.type,
              amount: item.amount,
              account_id: item.account_id,
              to_account_id: item.to_account_id,
              category_id: item.category_id,
              remark: item.remark,
              trans_time: item.trans_time,
            },
          })) as any;
          const newServerId = String(res?.id || res?.data?.id || item.id);
          const updated = getLocalTransactions().map((t) =>
            t.id === item.id
              ? { ...t, id: newServerId, server_id: newServerId, sync_status: "synced" as const }
              : t,
          );
          setLocalTransactions(updated);
          success++;
          continue;
        } catch (createErr: any) {
          console.error(`[sync] pushPending downgrade create ${item.id} failed:`, createErr);
        }
      }

      fail++;
      lastError = e?.message || "更新同步失败";
      const updated = getLocalTransactions().map((t) =>
        t.id === item.id ? { ...t, sync_status: "failed" as const } : t,
      );
      setLocalTransactions(updated);
    }
  }

  loadTransactions();
  setLastSyncTime();
  return { success, fail, ...(lastError ? { error: lastError } : {}) };
}

// 将本地已删除且有 server_id 的记录批量同步到服务端做软删除
export async function pushDeletes(): Promise<number> {
  const localList = getLocalTransactions();
  // 1. 清理本地无 server_id 的废弃删除记录
  const sanitizedList = localList.filter((t) => !(t.deleted === 1 && !t.server_id));
  if (sanitizedList.length !== localList.length) {
    setLocalTransactions(sanitizedList);
  }

  const pending = sanitizedList.filter(
    (t) => t.sync_status === "pending" && t.deleted === 1 && Boolean(t.server_id),
  );
  if (pending.length === 0) return 0;

  const ids = pending.map((t) => t.server_id!).filter(Boolean);

  try {
    await silentRequest({
      url: "/api/v1/transactions/batch-delete",
      method: "POST",
      data: { ids },
    });

    const deleteIds = new Set(pending.map((t) => t.id));
    const remaining = sanitizedList.filter((t) => !deleteIds.has(t.id));
    setLocalTransactions(remaining);
    loadTransactions();

    return ids.length;
  } catch (e) {
    console.error("[sync] pushDeletes failed:", e);
    return 0;
  }
}

export async function pullFromServer(
  bookId: string,
  month?: string,
): Promise<number> {
  const targetMonth = month || currentMonth();
  const key = `${bookId}_${targetMonth}`;
  const existing = pullPromises.get(key);
  if (existing) return existing;

  const promise = (async () => {
    try {
      const result = (await silentRequest({
        url: "/api/v1/transactions",
        method: "GET",
        data: {
          book_id: bookId,
          month: targetMonth,
          page: 1,
          size: 999,
        },
      })) as any;

      const serverList = (result?.list || result) as any[];
      if (!Array.isArray(serverList)) return 0;

      const localList = getLocalTransactions();
      const serverMap = new Map<string, any>();
      for (const item of serverList) {
        serverMap.set(String(item.id), item);
      }

      const reconciledList: LocalTransaction[] = [];
      const matchedServerIds = new Set<string>();

      for (const t of localList) {
        const isCurrentScope =
          String(t.book_id) === String(bookId) &&
          Boolean(t.trans_time && t.trans_time.startsWith(targetMonth));

        if (!isCurrentScope) {
          // 不属于当前账本或当前月份的数据，原样保留
          reconciledList.push(t);
          continue;
        }

        const sId = t.server_id || t.id;
        if (serverMap.has(String(sId))) {
          // 服务端存在该记录：使用服务端权威数据刷新本地
          const serverItem = serverMap.get(String(sId))!;
          matchedServerIds.add(String(sId));
          reconciledList.push({
            ...t,
            id: String(serverItem.id),
            server_id: String(serverItem.id),
            book_id: String(serverItem.book_id),
            type: serverItem.type,
            amount: serverItem.amount,
            account_id: String(serverItem.account_id),
            to_account_id: serverItem.to_account_id ? String(serverItem.to_account_id) : undefined,
            category_id: serverItem.category_id != null ? String(serverItem.category_id) : undefined,
            remark: serverItem.remark || "",
            trans_time: serverItem.trans_time,
            sync_status: "synced",
            deleted: 0,
            updated_at: serverItem.updated_at || serverItem.trans_time,
          });
        } else if (
          (t.sync_status === "pending" || t.sync_status === "failed" || !t.server_id) &&
          t.deleted !== 1
        ) {
          // 本地未同步或同步失败的真实数据绝对保护（权重最大）：
          // 1. 若为本地新建未上报（!t.server_id），保留等待补推
          // 2. 若为本地离线修改未上报（Boolean(t.server_id)），保留等待 PUT 推送
          // 3. 若为先前网络波动导致的同步失败（failed），安全保留等待重试
          // 严禁抹除 server_id，保留原始状态，绝对不丢弃！
          reconciledList.push(t);
        } else {
          // 核心对齐修复（同步后以服务器为准）：
          // 1. 若本地为已同步数据（sync_status === 'synced'）且属于当前拉取月份，
          //    但服务端未返回该记录，明确说明该记录已被远端（如其他设备）删除，本地直接清理！
          // 2. 若本地为待删除标记（deleted === 1）但在服务端已无记录，直接清理无残留！
        }
      }

      // 服务端存在但本地尚未收录的记录，新增插入
      for (const item of serverList) {
        if (!matchedServerIds.has(String(item.id))) {
          reconciledList.unshift({
            id: String(item.id),
            server_id: String(item.id),
            book_id: String(item.book_id),
            type: item.type,
            amount: item.amount,
            account_id: String(item.account_id),
            to_account_id: item.to_account_id ? String(item.to_account_id) : undefined,
            category_id: item.category_id != null ? String(item.category_id) : undefined,
            remark: item.remark || "",
            trans_time: item.trans_time,
            sync_status: "synced",
            deleted: 0,
            created_at: item.created_at || item.trans_time,
            updated_at: item.updated_at || item.trans_time,
          });
        }
      }

      setLocalTransactions(reconciledList);
      loadTransactions();
      setLastSyncTime();

      return serverList.length;
    } catch (e) {
      console.error("[sync] pullFromServer failed:", e);
      return 0;
    } finally {
      pullPromises.delete(key);
    }
  })();

  pullPromises.set(key, promise);
  return promise;
}

// 单次同步周期：先推后拉，syncing 锁防止并发
export async function runSyncCycle(bookId: string, silent = true): Promise<{
  pushed: number;
  pulled: number;
  failed: number;
  error?: string;
}> {
  if (syncing) return { pushed: 0, pulled: 0, failed: 0 };

  syncing = true;
  try {
    const deleted = await pushDeletes();
    const pushResult = await pushPending();
    const pulled = await pullFromServer(bookId);
    return {
      pushed: pushResult.success + deleted,
      pulled,
      failed: pushResult.fail,
      error: pushResult.error,
    };
  } finally {
    syncing = false;
  }
}

export async function fullSync(bookId: string): Promise<{
  pushed: number;
  pulled: number;
  failed: number;
  error?: string;
}> {
  return runSyncCycle(bookId, false);
}

// 启动后台同步：定时轮询 + 网络恢复时触发
export function scheduleSync(bookId: string) {
  stopSync();

  syncTimer = setInterval(() => {
    runSyncCycle(bookId, true);
  }, SYNC_INTERVAL) as unknown as number;

  // 断网恢复后立即同步一次
  networkChangeHandler = (res: any) => {
    if (res.isConnected) {
      runSyncCycle(bookId, true);
    }
  };
  uni.onNetworkStatusChange(networkChangeHandler);
}

export function stopSync() {
  if (syncTimer) {
    clearInterval(syncTimer);
    syncTimer = null;
  }
  if (networkChangeHandler) {
    uni.offNetworkStatusChange(networkChangeHandler);
    networkChangeHandler = null;
  }
}
