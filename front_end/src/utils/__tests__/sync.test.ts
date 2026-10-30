import { describe, it, expect, beforeEach, vi } from "vitest";
import {
  pushPending,
  pushDeletes,
  pullFromServer,
  runSyncCycle,
} from "../sync";
import type { LocalTransaction } from "@/stores/transactionStore";

describe("sync.ts unit tests", () => {
  const STORAGE_KEY = "local_transactions";
  let storageData: Record<string, string> = {};

  beforeEach(() => {
    storageData = {};
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

    const pulled = await pullFromServer("100");
    expect(pulled).toBe(1);

    const saved = JSON.parse(storageData[STORAGE_KEY]) as LocalTransaction[];
    expect(saved.length).toBe(1);
    expect(saved[0].server_id).toBe("888");
    expect(saved[0].sync_status).toBe("synced");
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
