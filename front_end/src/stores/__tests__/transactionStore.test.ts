import { describe, it, expect, beforeEach, vi } from "vitest";

const mockStorage: Record<string, any> = {};

beforeEach(() => {
  Object.keys(mockStorage).forEach((k) => delete mockStorage[k]);
  (uni.getStorageSync as any).mockImplementation((key: string) => mockStorage[key]);
  (uni.setStorageSync as any).mockImplementation((key: string, value: any) => {
    mockStorage[key] = value;
  });
  (uni.removeStorageSync as any).mockImplementation((key: string) => {
    delete mockStorage[key];
  });
});

function makeTx(overrides: Record<string, any> = {}) {
  return {
    book_id: 12345,
    type: 0,
    amount: 2500,
    account_id: 1,
    category_id: 1,
    trans_time: "2026-06-17T12:00:00",
    ...overrides,
  };
}

describe("transactionStore", () => {
  let store: typeof import("../transactionStore");

  beforeEach(async () => {
    vi.resetModules();
    store = await import("../transactionStore");
  });

  describe("addTransaction", () => {
    it("creates transaction with pending sync_status", () => {
      const tx = store.addTransaction(makeTx());
      expect(tx.sync_status).toBe("pending");
      expect(tx.deleted).toBe(0);
    });

    it("assigns negative local id", () => {
      const tx = store.addTransaction(makeTx());
      expect(tx.id).toBeLessThan(0);
    });

    it("unshifts to transactions array", () => {
      store.addTransaction(makeTx({ amount: 100 }));
      store.addTransaction(makeTx({ amount: 200 }));
      expect(store.useTransactions().transactions.value[0].amount).toBe(200);
    });

    it("persists to storage", () => {
      store.addTransaction(makeTx());
      const raw = mockStorage["local_transactions"];
      expect(raw).toBeTruthy();
      const parsed = JSON.parse(raw);
      expect(parsed.length).toBe(1);
    });
  });

  describe("updateTransaction", () => {
    it("merges data into existing transaction", () => {
      const tx = store.addTransaction(makeTx({ amount: 1000 }));
      store.updateTransaction(tx.id, { amount: 2000 });
      const updated = store.useTransactions().transactions.value.find((t) => t.id === tx.id);
      expect(updated?.amount).toBe(2000);
    });

    it("resets sync_status to pending if was synced", () => {
      const tx = store.addTransaction(makeTx());
      store.markSynced(tx.id, 999);
      store.updateTransaction(tx.id, { amount: 3000 });
      const updated = store.useTransactions().transactions.value.find((t) => t.id === tx.id);
      expect(updated?.sync_status).toBe("pending");
    });

    it("no-ops for non-existent id", () => {
      expect(() => store.updateTransaction(99999, { amount: 1 })).not.toThrow();
    });
  });

  describe("deleteTransaction", () => {
    it("hard deletes pending transaction without server_id", () => {
      const tx = store.addTransaction(makeTx());
      store.deleteTransaction(tx.id);
      const found = store.useTransactions().transactions.value.find((t) => t.id === tx.id);
      expect(found).toBeUndefined();
    });

    it("soft deletes synced transaction (sets deleted=1)", () => {
      const tx = store.addTransaction(makeTx());
      store.markSynced(tx.id, 999);
      store.deleteTransaction(tx.id);
      const found = store.useTransactions().transactions.value.find((t) => t.id === tx.id);
      expect(found?.deleted).toBe(1);
      expect(found?.sync_status).toBe("pending");
    });
  });

  describe("markSynced", () => {
    it("sets sync_status to synced and assigns server_id", () => {
      const tx = store.addTransaction(makeTx());
      store.markSynced(tx.id, 888);
      const found = store.useTransactions().transactions.value.find((t) => t.id === tx.id);
      expect(found?.sync_status).toBe("synced");
      expect(found?.server_id).toBe(888);
    });
  });

  describe("getTransactionsByDate", () => {
    it("groups transactions by date key", () => {
      store.addTransaction(makeTx({ amount: 1000, trans_time: "2026-06-17T10:00:00" }));
      store.addTransaction(makeTx({ amount: 500, trans_time: "2026-06-17T14:00:00" }));
      store.addTransaction(makeTx({ amount: 2000, trans_time: "2026-06-18T09:00:00", type: 1 }));

      const groups = store.getTransactionsByDate();
      expect(groups.size).toBe(2);
      const jun17 = groups.get("2026-06-17")!;
      expect(jun17.items.length).toBe(2);
      expect(jun17.expense).toBe(1500);
      const jun18 = groups.get("2026-06-18")!;
      expect(jun18.items.length).toBe(1);
      expect(jun18.income).toBe(2000);
    });

    it("excludes deleted transactions", () => {
      const tx = store.addTransaction(makeTx());
      store.deleteTransaction(tx.id);
      const groups = store.getTransactionsByDate();
      expect(groups.size).toBe(0);
    });
  });

  describe("getPendingTransactions", () => {
    it("returns only pending non-deleted transactions", () => {
      const tx1 = store.addTransaction(makeTx({ amount: 100 }));
      const tx2 = store.addTransaction(makeTx({ amount: 200 }));
      store.markSynced(tx2.id, 10);
      const pending = store.getPendingTransactions();
      expect(pending.length).toBe(1);
      expect(pending[0].id).toBe(tx1.id);
    });
  });

  describe("getPendingDeletes", () => {
    it("returns soft-deleted synced transactions", () => {
      const tx = store.addTransaction(makeTx());
      store.markSynced(tx.id, 999);
      store.deleteTransaction(tx.id);
      const deletes = store.getPendingDeletes();
      expect(deletes.length).toBe(1);
    });
  });

  describe("clearTransactions", () => {
    it("clears all transactions and removes storage", () => {
      store.addTransaction(makeTx());
      store.clearTransactions();
      expect(store.useTransactions().transactions.value.length).toBe(0);
      expect(mockStorage["local_transactions"]).toBeUndefined();
    });
  });

  describe("loadTransactions", () => {
    it("loads persisted transactions from storage", async () => {
      store.addTransaction(makeTx({ amount: 999 }));
      vi.resetModules();
      const fresh = await import("../transactionStore");
      fresh.loadTransactions();
      const txs = fresh.useTransactions().transactions.value;
      expect(txs.length).toBe(1);
      expect(txs[0].amount).toBe(999);
    });
  });
});
