import { describe, it, expect, vi, beforeEach } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import TransactionDetailModal from "../TransactionDetailModal.vue";

const mockRemoveTx = vi.fn();
vi.mock("@/stores/transactionStore", () => ({
  useTransactions: () => ({
    delete: mockRemoveTx,
  }),
}));

vi.mock("@/stores/categoryStore", () => ({
  useCategories: () => ({
    getCategoryName: vi.fn((type: number, id: string) => {
      if (id === "cat_food") return "餐饮美食";
      if (id === "cat_salary") return "工资收入";
      return "";
    }),
  }),
}));

const mockDeleteApi = vi.fn().mockResolvedValue(undefined);
vi.mock("@/api/common", () => ({
  deleteTransaction: () => mockDeleteApi(),
}));

const mockDialogConfirm = vi.fn();
vi.mock("@wot-ui/ui", () => ({
  useDialog: () => ({
    confirm: mockDialogConfirm,
  }),
}));

describe("TransactionDetailModal.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockTx = {
    id: "tx_123",
    book_id: "book_1",
    type: 0,
    amount: 2550, // 25.50 元
    category_id: "cat_food",
    remark: "午餐牛肉面",
    trans_time: "2026-11-03 12:30:00",
  };

  it("renders transaction details correctly", () => {
    const wrapper = mount(TransactionDetailModal, {
      props: {
        visible: true,
        transaction: mockTx,
        bookName: "日常账本",
      },
      global: {
        stubs: {
          "wd-popup": {
            template: "<div class='wd-popup-stub' v-if='modelValue'><slot /></div>",
            props: ["modelValue"],
          },
          "wd-dialog": true,
        },
      },
    });

    expect(wrapper.text()).toContain("账单详情");
    expect(wrapper.text()).toContain("-25.50");
    expect(wrapper.text()).toContain("餐饮美食");
    expect(wrapper.text()).toContain("日常账本");
    expect(wrapper.text()).toContain("午餐牛肉面");
    expect(wrapper.text()).toContain("2026.11.03 12:30");
  });

  it("emits edit and closes when edit button is clicked", async () => {
    const wrapper = mount(TransactionDetailModal, {
      props: {
        visible: true,
        transaction: mockTx,
      },
      global: {
        stubs: {
          "wd-popup": {
            template: "<div class='wd-popup-stub' v-if='modelValue'><slot /></div>",
            props: ["modelValue"],
          },
          "wd-dialog": true,
        },
      },
    });

    const editBtn = wrapper.find(".detail-edit-btn");
    expect(editBtn.exists()).toBe(true);
    await editBtn.trigger("click");

    expect(wrapper.emitted("update:visible")).toBeTruthy();
    expect(wrapper.emitted("update:visible")![0]).toEqual([false]);
    expect(wrapper.emitted("edit")).toBeTruthy();
    expect(wrapper.emitted("edit")![0][0]).toMatchObject({ id: "tx_123" });
  });

  it("confirms and removes transaction when delete button is clicked", async () => {
    mockDialogConfirm.mockResolvedValueOnce({ action: "confirm" });

    const wrapper = mount(TransactionDetailModal, {
      props: {
        visible: true,
        transaction: mockTx,
      },
      global: {
        stubs: {
          "wd-popup": {
            template: "<div class='wd-popup-stub' v-if='modelValue'><slot /></div>",
            props: ["modelValue"],
          },
          "wd-dialog": true,
        },
      },
    });

    const deleteBtn = wrapper.find(".detail-delete-btn");
    expect(deleteBtn.exists()).toBe(true);
    await deleteBtn.trigger("click");
    await flushPromises();

    expect(mockDialogConfirm).toHaveBeenCalled();
    expect(mockRemoveTx).toHaveBeenCalledWith("tx_123");
    expect(wrapper.emitted("delete")).toBeTruthy();
    expect(wrapper.emitted("delete")![0]).toEqual(["tx_123"]);
  });
});
