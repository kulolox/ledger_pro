<template>
  <view class="home-page">
    <NavBar>
      <template #left>
        <view class="menu-btn" @click="showSidebar = true">
          <wd-icon name="menu" size="32rpx" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <view class="month-nav">
          <view class="nav-arrow" @click="prevMonth">
            <wd-icon name="left" size="24px" color="var(--text-secondary)" />
          </view>
          <text class="month-label">{{ monthLabel }}</text>
          <view class="nav-arrow" @click="nextMonth">
            <wd-icon name="right" size="24px" color="var(--text-secondary)" />
          </view>
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
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <!-- 无账单数据显示 -->
        <view v-if="dayGroups.length === 0" class="empty-state">
          <view class="empty-icon-wrapper">
            <wd-icon name="book" size="72rpx" color="var(--text-label)" />
          </view>
          <text class="empty-title">暂无账单</text>
          <text class="empty-hint">点击右下角 + 开始记账</text>
        </view>

        <!-- 按日聚合的账单列表 -->
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
            :class="{
              'tx-item-pending': tx.sync_status === 'pending',
              'tx-item-failed': tx.sync_status === 'failed',
            }"
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
              <text
                v-if="tx.sync_status === 'pending'"
                class="tx-sync-badge tx-sync-pending"
              >
                待同步
              </text>
              <text
                v-if="tx.sync_status === 'failed'"
                class="tx-sync-badge tx-sync-failed"
              >
                同步失败
              </text>
            </view>
          </view>
        </view>

        <view class="list-bottom" v-if="dayGroups.length > 0">
          <view class="bottom-divider" />
          <text class="bottom-text">已加载全部账单</text>
        </view>
      </scroll-view>

      <view class="fab" @click="onAdd">
        <wd-icon name="plus" size="48rpx" color="var(--text-inverse)" />
      </view>

      <wd-dialog selector="delete-confirm" />

      <!-- 记账器组件 -->
      <TransactionEditor
        :visible="editorVisible"
        :bookId="currentBookId"
        :editData="editingTx"
        @close="onEditorClose"
        @saved="onEditorSaved"
      />

      <!-- 侧边栏组件 -->
      <Sidebar
        v-model:visible="showSidebar"
        v-model:books="books"
        :userInfo="userInfo"
        :currentBookId="currentBookId"
        @close="showSidebar = false"
        @switchBook="onSwitchBook"
      />
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
import {
  isLoggedIn,
  getUserInfo,
  getCurrentBookId,
  setCurrentBookId,
} from "@/utils/storage";
import { pullFromServer, scheduleSync } from "@/utils/sync";
import { formatCents, formatDayHeader, formatWeekday } from "@/utils/format";
import { getAccountBooks, type UserInfo, type AccountBook } from "@/api/common";

const { load, delete: removeTx, getByDate } = useTransactions();

const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories } =
  useCategories();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const editorVisible = ref(false);
const editingTx = ref<LocalTransaction | null>(null);
const refreshing = ref(false);
const pageLoading = ref(true);

const showSidebar = ref(false);
const userInfo = ref<UserInfo | null>(null);
const books = ref<AccountBook[]>([]);
const currentBookId = ref(getCurrentBookId());

const monthLabel = computed(
  () => `${currentYear.value}年 ${currentMonth.value}月`,
);

// 按日期分组，过滤当前账本和月份，计算每日收入/支出，按日期倒序
const dayGroups = computed(() => {
  const map = getByDate();
  const result: Array<{
    date: string;
    income: number;
    expense: number;
    items: LocalTransaction[];
  }> = [];

  const prefix = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;

  for (const [date, group] of map) {
    if (date.startsWith(prefix)) {
      const filtered = group.items.filter(
        (t) => t.book_id === currentBookId.value,
      );
      if (filtered.length === 0) continue;
      const income = filtered
        .filter((t) => t.type === 1)
        .reduce((s, t) => s + t.amount, 0);
      const expense = filtered
        .filter((t) => t.type === 0)
        .reduce((s, t) => s + t.amount, 0);
      result.push({ date, income, expense, items: filtered });
    }
  }

  result.sort((a, b) => b.date.localeCompare(a.date));
  return result;
});

// 收入
const monthIncome = computed(() =>
  dayGroups.value.reduce((sum, d) => sum + d.income, 0),
);
// 支出
const monthExpense = computed(() =>
  dayGroups.value.reduce((sum, d) => sum + d.expense, 0),
);
// 结余
const monthBalance = computed(() => monthIncome.value - monthExpense.value);

// 切换到上月
function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
}

// 切换到下月
function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
}

// 格式化交易金额显示
function formatTxAmount(tx: LocalTransaction): string {
  const sign = tx.type === 1 ? "+" : tx.type === 0 ? "-" : "";
  return `${sign}${(tx.amount / 100).toFixed(2)}`;
}

// 根据交易类型返回金额 CSS 类名
function amountClass(type: number): string {
  if (type === 0) return "expense";
  if (type === 1) return "income";
  return "transfer";
}

// 打开新增交易编辑器
function onAdd() {
  editingTx.value = null;
  editorVisible.value = true;
}

// 打开编辑交易编辑器
function onEditTx(tx: LocalTransaction) {
  editingTx.value = { ...tx };
  editorVisible.value = true;
}

const dialog = useDialog("delete-confirm");

// 长按弹出删除确认框
async function onLongPressDelete(tx: LocalTransaction) {
  const result = await dialog.confirm({
    title: "确认删除",
    msg: `确定要删除这笔账单吗？`,
  });
  if (result.action === "confirm") {
    removeTx(tx.id);
    uni.showToast({ title: "已删除", icon: "success" });
  }
}

// 关闭交易编辑器
function onEditorClose() {
  editorVisible.value = false;
  editingTx.value = null;
}

// 编辑器保存成功后关闭
function onEditorSaved() {
  editorVisible.value = false;
  editingTx.value = null;
}

// 切换当前账本
function onSwitchBook(bookId: number) {
  currentBookId.value = bookId;
  setCurrentBookId(bookId);
  showSidebar.value = false;
  load();
  scheduleSync(bookId);
  pullFromServer(bookId);
}

// 跳转到统计页面
function goStats() {
  uni.navigateTo({ url: "/pages/statistics/index" });
}

// 下拉刷新触发同步
async function onRefresh() {
  refreshing.value = true;
  const { runSyncCycle } = await import("@/utils/sync");
  await runSyncCycle(currentBookId.value, false);
  refreshing.value = false;
}

// 滚动到底部加载更多（暂未实现分页）
function loadMore() {}

let pageReady = false;
const MIN_SKELETON_MS = 500;

// 页面显示时初始化数据、加载交易列表
onShow(async () => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }

  const startTime = Date.now();

  if (!pageReady) {
    pageReady = true;
    userInfo.value = getUserInfo();
    loadCategories();
    try {
      books.value = await getAccountBooks();
    } catch {
      books.value = [];
    }
    if (
      !books.value.find((b) => b.id === currentBookId.value) &&
      books.value.length > 0
    ) {
      currentBookId.value = books.value[0].id;
      setCurrentBookId(currentBookId.value);
    }
    scheduleSync(currentBookId.value);
  }

  load();
  pullFromServer(currentBookId.value);

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

.nav-arrow {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  background: var(--bg-primary);
  flex-shrink: 0;

  &:active {
    opacity: 0.6;
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
  padding: 0 20rpx 60rpx;
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

.tx-item-pending {
  border-left: 6rpx solid #ff9500;
  padding-left: 18rpx;
}

.tx-item-failed {
  border-left: 6rpx solid var(--color-expense);
  padding-left: 18rpx;
}

.tx-sync-badge {
  font-size: 20rpx;
  font-weight: 600;
  padding: 4rpx 10rpx;
  border-radius: var(--radius-xs);
  line-height: 1.4;

  &.tx-sync-pending {
    color: #ff9500;
    background: rgba(255, 149, 0, 0.1);
  }

  &.tx-sync-failed {
    color: var(--color-expense);
    background: rgba(255, 59, 48, 0.1);
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
</style>
