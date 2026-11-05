import { describe, it, expect, beforeEach, vi } from "vitest";
import {
  pushPending,
  pushDeletes,
  pullFromServer,
  runSyncCycle,
} from "../sync";
import type { LocalTransaction } from "@/stores/transactionStore";

describe("sync.ts unit tests", () => {
  const STORAGE_KEY = "u_101:local_transactions";
  let storageData: Record<string, string> = {};

  beforeEach(() => {
    storageData = {
      userInfo: JSON.stringify({ id: 101 }),
    };
    vi.mocked(uni.getStorageSync).mockImplementation((key: string) => storageData[key] || "");
    vi.mocked(uni.setStorageSync).mockImplementation((key: string, val: string) => {
      storageData[key] = val;
    });
    vi.mocked(uni.removeStorageSync).mockImplementation((key: string) => {
      delete storageData[key];
    });
  });

  it("pushPending: returns 0 when no pending items", async () => {
    storageData[STORAGE_KEY] = JSON.stringify([]);
    const res = await pushPending();
    expect(res).toEqual({ success: 0, fail: 0 });
  });

  it("pushPending: batch pushes new items without server_id and marks synced", async () => {
    const localTx: LocalTransaction = {
      id: "-1",
      book_id: "100",
      type: 0,
      amount: 1500,
      account_id: "1",
      trans_time: "2026-07-23 12:00:00",
      sync_status: "pending",
      deleted: 0,
      created_at: "2026-07-23T12:00:00Z",
      updated_at: "2026-07-23T12:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([localTx]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: {
          code: 0,
          data: {
            count: 1,
            list: [{ id: "501" }],
          },
        },
      });
    });

    const res = await pushPending();
    expect(res.success).toBe(1);
    expect(res.fail).toBe(0);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    expect(saved[0].sync_status).toBe("synced");
    expect(saved[0].server_id).toBe("501");
  });

  it("pushPending: marks overflow records as failed and prevents invalid push", async () => {
    const invalidTx: LocalTransaction = {
      id: "-99",
      book_id: "100",
      type: 0,
      amount: 10000000000, // 100亿分（1亿元）
      account_id: "1",
      trans_time: "2026-07-23 12:00:00",
      sync_status: "pending",
      deleted: 0,
      created_at: "2026-07-23T12:00:00Z",
      updated_at: "2026-07-23T12:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([invalidTx]);

    const res = await pushPending();
    expect(res.fail).toBe(1);
    expect(res.error).toBeDefined();

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    expect(saved[0].sync_status).toBe("failed");
  });

  it("pushDeletes: pushes pending deletes to batch-delete and purges from local storage", async () => {
    const deletedTx: LocalTransaction = {
      id: "-2",
      server_id: "777",
      book_id: "100",
      type: 0,
      amount: 2000,
      account_id: "1",
      trans_time: "2026-07-23 12:00:00",
      sync_status: "pending",
      deleted: 1,
      created_at: "2026-07-23T12:00:00Z",
      updated_at: "2026-07-23T12:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([deletedTx]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: { code: 0, data: { count: 1 } },
      });
    });

    const count = await pushDeletes();
    expect(count).toBe(1);

    const saved = JSON.parse(storageData[STORAGE_KEY] || "[]") as LocalTransaction[];
    expect(saved.length).toBe(0);
  });

  it("pullFromServer: merges non-duplicate server transactions into local storage", async () => {
    storageData[STORAGE_KEY] = JSON.stringify([]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: {
          code: 0,
          data: {
            list: [
              {
                id: "888",
                book_id: "100",
                type: 1,
                amount: 8800,
                account_id: "1",
                trans_time: "2026-07-23 09:00:00",
              },
            ],
          },
        },
      });
    });

    const pulled = await pullFromServer("100", "2026-07");
    expect(pulled).toBe(1);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    expect(saved.length).toBe(1);
    expect(saved[0].server_id).toBe("888");
    expect(saved[0].sync_status).toBe("synced");
  });

  it("pullFromServer: reconciles server transactions and safeguards local un-synced data from data-loss", async () => {
    const localDeletedOrphan: LocalTransaction = {
      id: "999",
      server_id: "999",
      book_id: "100",
      type: 0,
      amount: 500,
      account_id: "1",
      trans_time: "2026-07-20 10:00:00",
      sync_status: "pending",
      deleted: 1, // 用户主动删除了它
      created_at: "2026-07-20T10:00:00Z",
      updated_at: "2026-07-20T10:00:00Z",
    };
    const localRealData: LocalTransaction = {
      id: "-10",
      book_id: "100",
      type: 0,
      amount: 800,
      account_id: "1",
      trans_time: "2026-07-21 11:00:00",
      sync_status: "pending",
      deleted: 0, // 真实数据，绝对不能丢失
      created_at: "2026-07-21T11:00:00Z",
      updated_at: "2026-07-21T11:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([localDeletedOrphan, localRealData]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: {
          code: 0,
          data: {
            list: [
              {
                id: "1001",
                book_id: "100",
                type: 0,
                amount: 1200,
                account_id: "1",
                trans_time: "2026-07-22 12:00:00",
              },
            ],
          },
        },
      });
    });

    const count = await pullFromServer("100", "2026-07");
    expect(count).toBe(1);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    // localDeletedOrphan (已删且服务端无) 应被清理；localRealData (未删真实数据) 必须安全保留为 pending 等待上报；server 1001 应被加入
    expect(saved.find((t) => t.id === "999")).toBeUndefined();
    expect(saved.find((t) => t.id === "-10")?.sync_status).toBe("pending");
    expect(saved.find((t) => t.id === "1001")).toBeDefined();
    expect(saved.length).toBe(2);
  });

  it("pullFromServer: purges synced record when deleted on server by device B, preventing resurrection", async () => {
    // 场景：A 设备本地原有一条已经同步成功的数据（server_id: "2001", sync_status: "synced"）
    const syncedTxOnDeviceA: LocalTransaction = {
      id: "2001",
      server_id: "2001",
      book_id: "100",
      type: 0,
      amount: 6600,
      account_id: "1",
      trans_time: "2026-07-15 15:00:00",
      sync_status: "synced",
      deleted: 0,
      created_at: "2026-07-15T15:00:00Z",
      updated_at: "2026-07-15T15:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([syncedTxOnDeviceA]);

    // B 设备在服务端删除了该账单，服务端返回的当前月列表中为空
    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: {
          code: 0,
          data: {
            list: [],
          },
        },
      });
    });

    const pulled = await pullFromServer("100", "2026-07");
    expect(pulled).toBe(0);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    // A 设备刷新后，该记录已被远端权威删除，本地直接清理，绝不复活且绝不转为 pending
    expect(saved.length).toBe(0);
  });

  it("pullFromServer: preserves failed records and does not discard un-synced data", async () => {
    // 场景：本地有一笔此前由于网络异常同步失败（failed）的本地新建账单
    const failedTx: LocalTransaction = {
      id: "-88",
      book_id: "100",
      type: 0,
      amount: 3200,
      account_id: "1",
      trans_time: "2026-07-16 16:00:00",
      sync_status: "failed",
      deleted: 0,
      created_at: "2026-07-16T16:00:00Z",
      updated_at: "2026-07-16T16:00:00Z",
    };
    storageData[STORAGE_KEY] = JSON.stringify([failedTx]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: {
          code: 0,
          data: {
            list: [],
          },
        },
      });
    });

    const pulled = await pullFromServer("100", "2026-07");
    expect(pulled).toBe(0);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    // failed 记录绝对不能被丢弃！
    expect(saved.length).toBe(1);
    expect(saved[0].id).toBe("-88");
    expect(saved[0].sync_status).toBe("failed");
  });

  it("runSyncCycle: completes cycle without throwing", async () => {
    storageData[STORAGE_KEY] = JSON.stringify([]);

    vi.mocked(uni.request).mockImplementation(({ success }: any) => {
      success({
        statusCode: 200,
        data: { code: 0, data: { list: [] } },
      });
    });

    const res = await runSyncCycle("100");
    expect(res).toBeDefined();
    expect(res.pushed).toBe(0);
    expect(res.pulled).toBe(0);
  });
});
