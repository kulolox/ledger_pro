<template>
  <view class="home-page" :class="['theme-' + themeMode]" :data-theme="themeMode">
    <NavBar floating>
      <template #left>
        <view class="menu-btn" @click="showSidebar = true">
          <wd-icon name="menu" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <MonthSelector
          v-model:year="currentYear"
          v-model:month="currentMonth"
          @change="onMonthChange"
        />
      </template>
      <template #right>
        <view class="stats-btn" @click="goStats">
          <wd-icon name="dashboard" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
    </NavBar>

    <PageLoader v-if="pageLoading" />

    <template v-else>
      <view class="month-summary">
        <view class="summary-item expense-card">
          <text class="summary-label">支出</text>
          <text class="summary-value expense"
            >{{ monthExpense > 0 ? "-" : "" }}{{ formatCents(monthExpense) }}</text
          >
        </view>
        <view class="summary-item income-card">
          <text class="summary-label">收入</text>
          <text class="summary-value income"
            >{{ monthIncome > 0 ? "+" : "" }}{{ formatCents(monthIncome) }}</text
          >
        </view>
        <view class="summary-item balance-card">
          <text class="summary-label">结余</text>
          <text
            class="summary-value"
            :class="monthBalance > 0 ? 'income' : monthBalance < 0 ? 'expense' : ''"
          >
            {{ monthBalance > 0 ? "+" : monthBalance < 0 ? "-" : "" }}{{ formatCents(Math.abs(monthBalance)) }}
          </text>
        </view>
      </view>

      <scroll-view
        scroll-y
        class="transaction-list"
        refresher-enabled
        :refresher-triggered="refreshing"
        :refresher-background="themeMode === 'dark' ? '#000000' : '#f2f2f7'"
        :refresher-default-style="themeMode === 'dark' ? 'white' : 'black'"
        @refresherrefresh="onRefresh"
        @scrolltolower="loadMore"
      >
        <!-- 无账单数据显示 -->
        <view v-if="dayGroups.length === 0" class="empty-state">
          <view class="empty-icon-wrapper">
            <wd-icon name="book" size="72rpx" color="var(--text-tertiary)" />
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
            v-for="tx in day.items"
            :key="tx.id"
            class="tx-item"
            :class="{
              'tx-item-pending': tx.sync_status === 'pending',
              'tx-item-failed': tx.sync_status === 'failed',
            }"
            @click="onSelectTx(tx)"
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
        <wd-icon name="plus" size="52rpx" color="#ffffff" />
      </view>

      <!-- 账单详情底部弹窗组件 -->
      <TransactionDetailModal
        v-model:visible="detailVisible"
        :transaction="selectedTx"
        @edit="onEditFromDetail"
        @delete="onDeleteFromDetail"
      />

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
import { ref, computed, watch, nextTick } from "vue";
import { onShow } from "@dcloudio/uni-app";
import TransactionEditor from "@/components/TransactionEditor.vue";
import TransactionDetailModal from "@/components/TransactionDetailModal.vue";
import NavBar from "@/components/NavBar.vue";
import MonthSelector from "@/components/MonthSelector.vue";
import Sidebar from "@/components/Sidebar.vue";
import PageLoader from "@/components/PageLoader.vue";
import {
  useTransactions,
  type LocalTransaction,
} from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";
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
const { themeMode } = useTheme();

const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories } =
  useCategories();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const editorVisible = ref(false);
const editingTx = ref<LocalTransaction | null>(null);
const detailVisible = ref(false);
const selectedTx = ref<LocalTransaction | null>(null);
const refreshing = ref(false);
const pageLoading = ref(true);

const showSidebar = ref(false);
const userInfo = ref<UserInfo | null>(null);
const books = ref<AccountBook[]>([]);
const currentBookId = ref(getCurrentBookId());

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
        (t) => String(t.book_id) === String(currentBookId.value),
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

// 切换月份时拉取对应月份账单
function onMonthChange({ year, month }: { year: number; month: number }) {
  if (currentBookId.value) {
    const mStr = `${year}-${String(month).padStart(2, "0")}`;
    pullFromServer(currentBookId.value, mStr);
  }
}

watch([currentYear, currentMonth, currentBookId], ([y, m, b]) => {
  if (b) {
    const mStr = `${y}-${String(m).padStart(2, "0")}`;
    pullFromServer(b, mStr);
  }
});

// 格式化交易金额显示
function formatTxAmount(tx: LocalTransaction): string {
  const sign = tx.type === 1 ? "+" : tx.type === 0 ? "-" : "";
  return `${sign}${formatCents(tx.amount)}`;
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

// 点击账单弹出详情弹窗
function onSelectTx(tx: LocalTransaction) {
  selectedTx.value = { ...tx };
  detailVisible.value = true;
}

// 格式化详情弹窗中的交易时间（YYYY.MM.dd HH:mm）
function formatDetailTime(transTime?: string): string {
  if (!transTime) return "-";
  const normalized = transTime.replace("T", " ").trim();
  const [datePart, timePart] = normalized.split(" ");
  const formattedDate = (datePart || "").replace(/-/g, ".");
  if (!timePart) return formattedDate;
  return `${formattedDate} ${timePart.slice(0, 5)}`;
}

// 从详情弹窗打开记一笔修改
function onEditFromDetail(tx?: any) {
  const target = tx || selectedTx.value;
  if (!target) return;
  editingTx.value = { ...target };
  detailVisible.value = false;
  editorVisible.value = true;
}

// 从详情弹窗删除账单回调
function onDeleteFromDetail(_id?: string) {
  detailVisible.value = false;
  selectedTx.value = null;
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
function onSwitchBook(bookId: string | number) {
  const idStr = String(bookId);
  currentBookId.value = idStr;
  setCurrentBookId(idStr);
  showSidebar.value = false;
  load();
  scheduleSync(idStr);
  pullFromServer(idStr);
}

// 跳转到统计页面（携带当前选中的年月）
function goStats() {
  uni.navigateTo({
    url: `/pages/statistics/index?year=${currentYear.value}&month=${currentMonth.value}`,
  });
}

// 下拉刷新触发同步
async function onRefresh() {
  refreshing.value = true;
  const { runSyncCycle } = await import("@/utils/sync");
  const result = await runSyncCycle(currentBookId.value, false);
  refreshing.value = false;
  if (result.failed > 0) {
    uni.showToast({
      title: result.error || `有 ${result.failed} 条记录同步失败`,
      icon: "none",
      duration: 3500,
    });
  }
}

// 滚动到底部加载更多（暂未实现分页）
function loadMore() {}

let pageReady = false;

// 页面显示时初始化数据、加载交易列表
onShow(async () => {
  syncThemeFromStorage();
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }

  userInfo.value = getUserInfo();
  currentBookId.value = getCurrentBookId();

  if (!pageReady) {
    pageReady = true;
    loadCategories();
    try {
      books.value = await getAccountBooks();
    } catch {
      books.value = [];
    }
    scheduleSync(currentBookId.value);
  }

  // 账本合法性自愈纠偏：无论何时（包括从二级页返回），确保 currentBookId 一定存在于用户的有效账本中
  if (books.value.length > 0) {
    const matched = books.value.find(
      (b) => String(b.id) === String(currentBookId.value),
    );
    if (!matched) {
      currentBookId.value = String(books.value[0].id);
      setCurrentBookId(currentBookId.value);
    }
  }

  load();
  const mStr = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
  pullFromServer(currentBookId.value, mStr);

  await nextTick();
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
  transition: background-color 0.2s var(--spring-default);
}

.menu-btn,
.stats-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.15s;

  &:active {
    background: var(--bg-hover);
    transform: scale(0.92);
  }
}

.month-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  padding: 12rpx 24rpx 20rpx;
  background: var(--bg-primary);
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 26rpx 12rpx;
  min-width: 0;
  overflow: hidden;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.2s var(--spring-default);

  &:active {
    transform: scale(0.98);
  }

  .summary-label {
    font-size: 24rpx;
    color: var(--text-tertiary);
    font-weight: 500;
  }

  .summary-value {
    font-size: 30rpx;
    font-weight: 700;
    letter-spacing: -0.5rpx;
    font-variant-numeric: tabular-nums;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

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
  padding: 4rpx 24rpx 60rpx;
  overflow: hidden;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;

  .empty-icon-wrapper {
    width: 144rpx;
    height: 144rpx;
    border-radius: 50%;
    background: var(--bg-secondary);
    border: 1rpx solid var(--border-subtle);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: var(--shadow-sm);
  }

  .empty-title {
    font-size: 32rpx;
    color: var(--text-primary);
    margin-top: 32rpx;
    font-weight: 600;
  }

  .empty-hint {
    font-size: 26rpx;
    color: var(--text-tertiary);
    margin-top: 12rpx;
  }
}

.day-group {
  margin-top: 24rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: background-color 0.2s var(--spring-default);

  &:first-child {
    margin-top: 0;
  }
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid var(--border-subtle);
}

.day-info {
  display: flex;
  align-items: baseline;
  gap: 16rpx;

  .day-date {
    font-size: 30rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
  }

  .day-weekday {
    font-size: 24rpx;
    color: var(--text-tertiary);
    font-weight: 400;
  }
}

.day-totals {
  display: flex;
  gap: 18rpx;
  font-size: 28rpx;
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
  padding: 28rpx 32rpx;
  border-top: 1rpx solid var(--border-subtle);
  cursor: pointer;
  transition:
    transform 0.12s var(--spring-default),
    background-color 0.15s var(--spring-default);

  &:first-of-type {
    border-top: none;
  }

  &:active {
    background: var(--bg-overlay);
    transform: scale(0.99);
  }
}

.tx-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex: 1;
  overflow: hidden;
}

.tx-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);

  text {
    color: #ffffff;
    font-size: 38rpx;
    line-height: 1;
  }
}

.tx-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  overflow: hidden;

  .tx-category {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: -0.3rpx;
  }

  .tx-remark {
    font-size: 24rpx;
    color: var(--text-tertiary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.tx-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
  margin-left: 20rpx;
}

.tx-amount {
  font-size: 34rpx;
  font-weight: 700;
  letter-spacing: -0.5rpx;
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
  padding-left: 26rpx;
}

.tx-item-failed {
  border-left: 6rpx solid var(--color-expense);
  padding-left: 26rpx;
}

.tx-sync-badge {
  font-size: 20rpx;
  font-weight: 600;
  padding: 4rpx 10rpx;
  border-radius: var(--radius-xs);
  line-height: 1.4;

  &.tx-sync-pending {
    color: #ff9500;
    background: rgba(255, 149, 0, 0.12);
  }

  &.tx-sync-failed {
    color: var(--color-expense);
    background: var(--color-expense-bg);
  }
}

.list-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56rpx 0 80rpx;
  gap: 16rpx;

  .bottom-divider {
    width: 64rpx;
    height: 8rpx;
    background: var(--text-dim);
    border-radius: var(--radius-full);
  }

  .bottom-text {
    font-size: 24rpx;
    color: var(--text-tertiary);
    font-weight: 500;
    letter-spacing: 0.5rpx;
  }
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: calc(64rpx + var(--safe-area-bottom, 0px));
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-fab);
  z-index: 30;
  cursor: pointer;
  transition:
    transform 0.18s var(--spring-bounce),
    background-color 0.18s;

  &:active {
    transform: scale(0.92);
    background: var(--color-primary-hover);
  }
}

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
