<template>
  <view class="home-page">
    <NavBar>
      <template #left>
        <view class="menu-btn" @click="showSidebar = true">
          <wd-icon name="menu" size="32rpx" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <view class="month-nav" @click="openPicker">
          <text class="month-label">{{ monthLabel }}</text>
          <wd-icon name="down" size="28rpx" color="var(--text-secondary)" />
        </view>
      </template>
      <template #right>
        <view class="stats-btn" @click="goStats">
          <wd-icon
            name="dashboard"
            size="32rpx"
            color="var(--text-secondary)"
          />
        </view>
      </template>
    </NavBar>

    <PageLoader v-if="pageLoading" />

    <template v-else>
      <view class="month-summary">
        <view class="summary-item expense-card">
          <text class="summary-label">支出</text>
          <text class="summary-value expense"
            >-{{ formatCents(monthExpense) }}</text
          >
        </view>
        <view class="summary-item income-card">
          <text class="summary-label">收入</text>
          <text class="summary-value income"
            >+{{ formatCents(monthIncome) }}</text
          >
        </view>
        <view class="summary-item balance-card">
          <text class="summary-label">结余</text>
          <text
            class="summary-value"
            :class="monthBalance >= 0 ? 'income' : 'expense'"
          >
            {{ monthBalance >= 0 ? "+" : "-"
            }}{{ formatCents(Math.abs(monthBalance)) }}
          </text>
        </view>
      </view>

      <scroll-view
        scroll-y
        class="transaction-list"
        refresher-enabled
        refresher-background="#F2F2F7"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <view v-if="dayGroups.length === 0" class="empty-state">
          <view class="empty-icon-wrapper">
            <wd-icon name="book" size="72rpx" color="var(--text-label)" />
          </view>
          <text class="empty-title">暂无账单</text>
          <text class="empty-hint">点击右下角 + 开始记账</text>
        </view>

        <view v-for="day in dayGroups" :key="day.date" class="day-group">
          <view class="day-header">
            <view class="day-info">
              <text class="day-date">{{ formatDayHeader(day.date) }}</text>
              <text class="day-weekday">{{ formatWeekday(day.date) }}</text>
            </view>
            <view class="day-totals">
              <text v-if="day.expense > 0" class="day-expense"
                >-{{ formatCents(day.expense) }}</text
              >
              <text v-if="day.income > 0" class="day-income"
                >+{{ formatCents(day.income) }}</text
              >
            </view>
          </view>

          <view
            v-for="(tx, idx) in day.items"
            :key="tx.id"
            class="tx-item"
            @click="onEditTx(tx)"
            @longpress="onLongPressDelete(tx)"
          >
            <view class="tx-left">
              <view
                class="tx-icon"
                :style="{
                  background: getCategoryColor(tx.type, tx.category_id),
                }"
              >
                <text>{{ getCategoryIcon(tx.type, tx.category_id) }}</text>
              </view>
              <view class="tx-info">
                <text class="tx-category">{{
                  getCategoryName(tx.type, tx.category_id)
                }}</text>
                <text v-if="tx.remark" class="tx-remark">{{ tx.remark }}</text>
              </view>
            </view>
            <view class="tx-right">
              <text class="tx-amount" :class="amountClass(tx.type)">
                {{ formatTxAmount(tx) }}
              </text>
            </view>
          </view>
        </view>

        <view class="list-bottom" v-if="dayGroups.length > 0">
          <template v-if="loadingMore">
            <text class="bottom-text">加载中...</text>
          </template>
          <template v-else-if="!hasMore">
            <view class="bottom-divider" />
            <text class="bottom-text">已加载全部账单</text>
          </template>
        </view>
      </scroll-view>

      <view class="fab" @click="onAdd">
        <wd-icon name="plus" size="48rpx" color="var(--text-inverse)" />
      </view>

      <wd-dialog selector="delete-confirm" />

      <TransactionEditor
        :visible="editorVisible"
        :bookId="0"
        :editData="editingTx"
        @close="onEditorClose"
        @saved="onEditorSaved"
      />

      <Sidebar v-model:visible="showSidebar" @close="showSidebar = false" />

      <wd-popup
        v-model="pickerVisible"
        position="bottom"
        :round="true"
        :safe-area-inset-bottom="true"
        custom-class="picker-popup"
      >
        <view class="picker-container">
          <view class="picker-tabs">
            <wd-tabs v-model="pickerMode" @change="onModeChange">
              <wd-tab title="按月" name="month" />
              <wd-tab title="按年" name="year" />
            </wd-tabs>
          </view>
          <view class="picker-view-wrap">
            <wd-picker-view v-model="pickerValue" :columns="pickerColumns" />
          </view>
          <view class="picker-footer">
            <wd-button block size="large" @click="onPickerConfirm"
              >确认</wd-button
            >
          </view>
        </view>
      </wd-popup>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useDialog } from "@wot-ui/ui";
import TransactionEditor from "@/components/TransactionEditor.vue";
import NavBar from "@/components/NavBar.vue";
import Sidebar from "@/components/Sidebar.vue";
import PageLoader from "@/components/PageLoader.vue";
import {
  useTransactions,
  type LocalTransaction,
} from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { useUser } from "@/stores/userStore";
import { formatCents, formatDayHeader, formatWeekday } from "@/utils/format";

const {
  load,
  loadMore: fetchMore,
  hasMore,
  delete: removeTx,
  getByDate,
} = useTransactions();
const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories } =
  useCategories();
const { loadUser } = useUser();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const editorVisible = ref(false);
const editingTx = ref<LocalTransaction | null>(null);
const refreshing = ref(false);
const pageLoading = ref(true);
const showSidebar = ref(false);
const pickerVisible = ref(false);
const pickerMode = ref<string>("month");
const pickerValue = ref<(string | number)[]>([
  currentYear.value,
  currentMonth.value,
]);
const loadingMore = ref(false);

function makeYearOptions() {
  const cy = new Date().getFullYear();
  const years: { label: string; value: number }[] = [];
  for (let y = cy - 10; y <= cy + 10; y++) {
    years.push({ label: `${y}年`, value: y });
  }
  return years;
}

function makeMonthOptions() {
  const months: { label: string; value: number }[] = [];
  for (let m = 1; m <= 12; m++) {
    months.push({ label: `${m}月`, value: m });
  }
  return months;
}

const pickerColumns = computed(() => {
  if (pickerMode.value === "year") {
    return [makeYearOptions()];
  }
  return [makeYearOptions(), makeMonthOptions()];
});

const monthLabel = computed(() => {
  if (currentMonth.value === 0) {
    return `${currentYear.value}年`;
  }
  return `${currentYear.value}年 ${currentMonth.value}月`;
});

const monthPrefix = computed(() => {
  if (currentMonth.value === 0) {
    return `${currentYear.value}`;
  }
  return `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
});

const dayGroups = computed(() => {
  const map = getByDate();
  const result: Array<{
    date: string;
    income: number;
    expense: number;
    items: LocalTransaction[];
  }> = [];

  const prefix = monthPrefix.value;

  for (const [date, group] of map) {
    if (date.startsWith(prefix)) {
      result.push({
        date,
        income: group.income,
        expense: group.expense,
        items: group.items,
      });
    }
  }

  result.sort((a, b) => b.date.localeCompare(a.date));
  return result;
});

const monthIncome = computed(() =>
  dayGroups.value.reduce((sum, d) => sum + d.income, 0),
);

const monthExpense = computed(() =>
  dayGroups.value.reduce((sum, d) => sum + d.expense, 0),
);

const monthBalance = computed(() => monthIncome.value - monthExpense.value);

function openPicker() {
  pickerMode.value = currentMonth.value === 0 ? "year" : "month";
  if (pickerMode.value === "year") {
    pickerValue.value = [currentYear.value];
  } else {
    pickerValue.value = [currentYear.value, currentMonth.value];
  }
  pickerVisible.value = true;
}

function onModeChange(e: { name: string }) {
  const mode = e.name;
  if (pickerMode.value === mode) return;
  pickerMode.value = mode;
  if (mode === "year") {
    pickerValue.value = [pickerValue.value[0]];
  } else {
    pickerValue.value = [pickerValue.value[0], 1];
  }
}

async function onPickerConfirm() {
  const [year, month] = pickerValue.value;
  currentYear.value = Number(year);
  currentMonth.value = pickerMode.value === "year" ? 0 : Number(month ?? 1);
  pickerVisible.value = false;
  await load(monthPrefix.value);
}

function formatTxAmount(tx: LocalTransaction): string {
  const sign = tx.type === 1 ? "+" : tx.type === 0 ? "-" : "";
  return `${sign}${(tx.amount / 100).toFixed(2)}`;
}

function amountClass(type: number): string {
  if (type === 0) return "expense";
  if (type === 1) return "income";
  return "transfer";
}

function onAdd() {
  editingTx.value = null;
  editorVisible.value = true;
}

function onEditTx(tx: LocalTransaction) {
  editingTx.value = tx;
  editorVisible.value = true;
}

function onLongPressDelete(tx: LocalTransaction) {
  uni.showModal({
    title: "删除账单",
    content: "确定要删除这条账单吗？",
    success: async (res) => {
      if (res.confirm) {
        await removeTx(tx.id);
        const m = monthPrefix.value;
        await load(m);
      }
    },
  });
}

function onEditorClose() {
  editorVisible.value = false;
  editingTx.value = null;
}

async function onEditorSaved() {
  editorVisible.value = false;
  editingTx.value = null;
  const m = monthPrefix.value;
  await load(m);
}

async function onRefresh() {
  refreshing.value = true;
  const m = monthPrefix.value;
  await load(m);
  refreshing.value = false;
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return;
  loadingMore.value = true;
  await fetchMore(monthPrefix.value);
  loadingMore.value = false;
}

function goStats() {
  uni.navigateTo({ url: "/pages/statistics/index" });
}

let pageReady = false;
const MIN_SKELETON_MS = 500;

onShow(async () => {
  const startTime = Date.now();

  if (!pageReady) {
    pageReady = true;
    await loadUser();
    await loadCategories();
  }

  await load(monthPrefix.value);

  await nextTick();
  const elapsed = Date.now() - startTime;
  if (elapsed < MIN_SKELETON_MS) {
    await new Promise((r) => setTimeout(r, MIN_SKELETON_MS - elapsed));
  }
  pageLoading.value = false;
});
</script>

<style lang="scss" scoped>
.home-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  background: var(--bg-primary);
  overflow: hidden;
}

.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;

  &:active {
    opacity: 0.6;
  }
}

.menu-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.92);
  }
}

.month-label {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  letter-spacing: 1rpx;
  flex-shrink: 0;
}

.stats-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.92);
  }
}

.month-summary {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx 20rpx;
  background: var(--bg-primary);
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 28rpx 0;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);

  .summary-label {
    font-size: 22rpx;
    color: var(--text-label);
    font-weight: 500;
    letter-spacing: 1rpx;
  }

  .summary-value {
    font-size: 30rpx;
    font-weight: 700;
    font-variant-numeric: tabular-nums;

    &.expense {
      color: var(--color-expense);
    }
    &.income {
      color: var(--color-income);
    }
  }
}

.transaction-list {
  box-sizing: border-box;
  flex: 1;
  min-height: 0;
  padding: 0 32rpx 60rpx;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 240rpx 0;

  .empty-icon-wrapper {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    background: var(--bg-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: var(--shadow-md);
  }

  .empty-title {
    font-size: 32rpx;
    color: var(--text-secondary);
    margin-top: 40rpx;
    font-weight: 600;
  }

  .empty-hint {
    font-size: 26rpx;
    color: var(--text-muted);
    margin-top: 12rpx;
  }
}

.day-group {
  margin-top: 20rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);

  &:first-child {
    margin-top: 0;
  }
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 24rpx 18rpx;
}

.day-info {
  display: flex;
  align-items: baseline;
  gap: 14rpx;

  .day-date {
    font-size: 34rpx;
    font-weight: 700;
    color: var(--text-primary);
  }

  .day-weekday {
    font-size: 24rpx;
    color: var(--text-muted);
    font-weight: 500;
  }
}

.day-totals {
  display: flex;
  gap: 18rpx;
  font-size: 26rpx;
  font-weight: 600;
  font-variant-numeric: tabular-nums;

  .day-expense {
    color: var(--color-expense);
  }
  .day-income {
    color: var(--color-income);
  }
}

.tx-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  transition: background 0.15s var(--spring-default);
  border-top: 1rpx solid var(--border-subtle);

  &:active {
    background: var(--bg-overlay);
  }
}

.tx-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  overflow: hidden;
}

.tx-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  text {
    color: var(--text-inverse);
    font-size: 32rpx;
    font-weight: 600;
  }
}

.tx-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  overflow: hidden;

  .tx-category {
    font-size: 30rpx;
    font-weight: 500;
    color: var(--text-primary);
  }

  .tx-remark {
    font-size: 24rpx;
    color: var(--text-muted);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.tx-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.tx-amount {
  font-size: 32rpx;
  font-weight: 600;
  font-variant-numeric: tabular-nums;

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

.list-bottom {
  text-align: center;
  padding: 32rpx 0 60rpx;

  .bottom-divider {
    width: 80rpx;
    height: 4rpx;
    background: var(--border-primary);
    border-radius: 2rpx;
    margin: 0 auto 20rpx;
  }

  .bottom-text {
    font-size: 22rpx;
    color: var(--text-dim);
    font-weight: 500;
  }
}

.fab {
  position: fixed;
  right: 36rpx;
  bottom: calc(100rpx + var(--safe-area-bottom, 0px));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-fab);
  z-index: 1;
  transition:
    transform 0.2s var(--spring-bounce),
    box-shadow 0.2s var(--spring-default);

  &:active {
    transform: scale(0.88);
    box-shadow: var(--shadow-fab-active);
  }
}

.picker-container {
  background: var(--bg-primary);
}

.picker-tabs {
  padding: 20rpx 24rpx 0;

  :deep(.wd-tabs) {
    --wot-tabs-nav-bg: transparent;
    --wot-tabs-nav-color: var(--text-secondary);
    --wot-tabs-nav-color-active: var(--color-primary);
    --wot-tabs-nav-font-weight-active: 600;
    --wot-tabs-nav-item-font-size: 28rpx;
    --wot-tabs-nav-line-bg: var(--color-primary);
    --wot-tabs-nav-item-padding: 20rpx 24rpx;

    .wd-tabs__nav-item {
      flex: 1;
    }
  }
}

.picker-view-wrap {
  padding: 8rpx 0;
}

.picker-footer {
  padding: 8rpx 32rpx 32rpx;
  padding-bottom: calc(32rpx + var(--safe-area-bottom, 0px));
}
</style>
