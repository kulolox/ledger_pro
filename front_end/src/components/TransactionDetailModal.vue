<template>
  <view class="transaction-detail-modal-wrapper">
    <wd-popup
      :model-value="visible"
      position="bottom"
      :z-index="100"
      custom-class="editor-popup"
      custom-style="background: transparent;"
      @close="onClose"
    >
      <view v-if="transaction" class="tx-detail-sheet">
        <view class="sheet-grabber" />

        <view class="detail-header">
          <text class="detail-title">账单详情</text>
          <view class="detail-actions">
            <view
              class="detail-btn detail-delete-btn"
              @click="onDelete"
            >
              <text>删除</text>
            </view>
            <view
              class="detail-btn detail-edit-btn"
              @click="onEdit"
            >
              <text>修改</text>
            </view>
          </view>
        </view>

        <!-- Apple HIG Inset Grouped 账单文字信息主卡片 -->
        <view class="detail-info-card">
          <view class="detail-row">
            <text class="detail-label">金额</text>
            <text
              class="detail-value tabular amount-val"
              :class="amountClass(transaction.type)"
            >
              {{ formatTxAmount(transaction) }}
            </text>
          </view>
          <view class="detail-row">
            <text class="detail-label">类型</text>
            <text class="detail-value">{{
              transaction.type === 1 ? "收入" : "支出"
            }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">分类</text>
            <text class="detail-value">{{
              getCategoryName(transaction.type, transaction.category_id) || "未分类"
            }}</text>
          </view>
          <view v-if="bookName" class="detail-row">
            <text class="detail-label">账本</text>
            <text class="detail-value">{{ bookName }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">时间</text>
            <text class="detail-value tabular">{{
              formatDetailTime(transaction.trans_time)
            }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">备注</text>
            <text class="detail-value remark-val">{{
              transaction.remark || "无"
            }}</text>
          </view>
        </view>
      </view>
    </wd-popup>

    <wd-dialog selector="tx-detail-delete-confirm" :z-index="1100" />
  </view>
</template>

<script setup lang="ts">
import { useDialog } from "@wot-ui/ui";
import { useCategories } from "@/stores/categoryStore";
import { useTransactions } from "@/stores/transactionStore";
import { formatCents } from "@/utils/format";
import { deleteTransaction as apiDeleteTransaction } from "@/api/common";

export interface DetailTransaction {
  id: string;
  book_id?: string;
  type: number;
  amount: number;
  category_id?: string;
  account_id?: string;
  to_account_id?: string;
  remark?: string;
  trans_time?: string;
  sync_status?: string;
  deleted?: number;
}

const props = defineProps<{
  visible: boolean;
  transaction: DetailTransaction | null;
  bookName?: string;
}>();

const emit = defineEmits<{
  (e: "update:visible", value: boolean): void;
  (e: "close"): void;
  (e: "edit", transaction: DetailTransaction): void;
  (e: "delete", id: string): void;
}>();

const { getCategoryName } = useCategories();
const { delete: removeTx } = useTransactions();
const dialog = useDialog("tx-detail-delete-confirm");

function onClose() {
  emit("update:visible", false);
  emit("close");
}

function formatTxAmount(tx: DetailTransaction): string {
  const sign = tx.type === 1 ? "+" : tx.type === 0 ? "-" : "";
  return `${sign}${formatCents(tx.amount)}`;
}

function amountClass(type: number): string {
  if (type === 0) return "expense";
  if (type === 1) return "income";
  return "transfer";
}

function formatDetailTime(transTime?: string): string {
  if (!transTime) return "-";
  const normalized = transTime.replace("T", " ").trim();
  const [datePart, timePart] = normalized.split(" ");
  const formattedDate = (datePart || "").replace(/-/g, ".");
  if (!timePart) return formattedDate;
  return `${formattedDate} ${timePart.slice(0, 5)}`;
}

function onEdit() {
  if (!props.transaction) return;
  const current = { ...props.transaction };
  onClose();
  emit("edit", current);
}

async function onDelete() {
  if (!props.transaction) return;
  const targetId = props.transaction.id;
  const result = await dialog.confirm({
    title: "确认删除",
    msg: "确定要删除这笔账单吗？",
    zIndex: 1100,
  });
  if (result.action === "confirm") {
    onClose();
    await removeTx(targetId);
    if (!String(targetId).startsWith("-")) {
      try {
        await apiDeleteTransaction(String(targetId));
      } catch (err) {
        console.warn("API deleteTransaction failed:", err);
      }
    }
    uni.showToast({ title: "已删除", icon: "success" });
    emit("delete", targetId);
  }
}
</script>

<style lang="scss" scoped>
.tx-detail-sheet {
  background: var(--bg-secondary);
  border-top: 1rpx solid var(--border-subtle);
  border-radius: 44rpx 44rpx 0 0;
  padding: 0 32rpx calc(36rpx + var(--safe-area-bottom, 0px));
  box-shadow: 0 -16rpx 60rpx rgba(0, 0, 0, 0.12);
  transition: background-color 0.2s var(--spring-default);
}

.sheet-grabber {
  width: 68rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: var(--color-handle);
  margin: 18rpx auto 6rpx;
  opacity: 0.85;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 4rpx 24rpx;

  .detail-title {
    font-size: 30rpx;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: -0.3rpx;
  }
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;

  .detail-btn {
    height: 54rpx;
    padding: 0 26rpx;
    border-radius: 999rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    font-weight: 500;
    cursor: pointer;
    transition:
      transform 0.15s var(--spring-default),
      opacity 0.15s;

    &:active {
      transform: scale(0.95);
      opacity: 0.8;
    }
  }

  .detail-delete-btn {
    background: var(--color-expense-bg);
    color: var(--color-expense);
  }

  .detail-edit-btn {
    background: var(--color-primary-subtle);
    color: var(--color-primary);
  }
}

.detail-info-card {
  background: var(--bg-input-filled);
  border: 1rpx solid var(--border-subtle);
  border-radius: 24rpx;
  padding: 4rpx 32rpx;
}

.detail-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 32rpx;
  padding: 28rpx 0;
  border-bottom: 1rpx solid var(--border-subtle);

  &:last-child {
    border-bottom: none;
  }

  .detail-label {
    font-size: 28rpx;
    color: var(--text-tertiary);
    flex-shrink: 0;
  }

  .detail-value {
    font-size: 30rpx;
    font-weight: 500;
    color: var(--text-primary);
    text-align: right;
    word-break: break-all;
    line-height: 1.4;

    &.tabular {
      font-variant-numeric: tabular-nums;
    }

    &.amount-val {
      font-weight: 600;

      &.expense {
        color: var(--color-expense);
      }
      &.income {
        color: var(--color-income);
      }
      &.transfer {
        color: var(--color-primary);
      }
    }

    &.remark-val {
      color: var(--text-primary);
    }
  }
}
</style>
