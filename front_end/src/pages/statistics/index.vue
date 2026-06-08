<template>
  <view class="stats-page">
    <view class="stats-header">
      <view class="nav-bar">
        <view class="nav-left" @click="goBack">
          <text class="nav-back">←</text>
        </view>
        <view class="nav-center">
          <view class="nav-arrow" @click="prevMonth">
            <text>‹</text>
          </view>
          <text class="month-label">{{ monthLabel }}</text>
          <view class="nav-arrow" @click="nextMonth">
            <text>›</text>
          </view>
        </view>
        <view class="nav-right" />
      </view>

      <view class="month-summary">
        <view class="summary-item expense-card">
          <text class="summary-label">支出</text>
          <text class="summary-value expense"
            >-{{ formatCents(totalExpense) }}</text
          >
        </view>
        <view class="summary-item income-card">
          <text class="summary-label">收入</text>
          <text class="summary-value income"
            >+{{ formatCents(totalIncome) }}</text
          >
        </view>
        <view class="summary-item balance-card">
          <text class="summary-label">结余</text>
          <text
            class="summary-value"
            :class="balance >= 0 ? 'income' : 'expense'"
          >
            {{ balance >= 0 ? "+" : "-" }}{{ formatCents(Math.abs(balance)) }}
          </text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="stats-body">
      <view v-if="expenseCategories.length > 0" class="chart-section">
        <text class="section-title">支出分类</text>
        <EChart :options="pieOption" :height="420" />
      </view>

      <view v-if="expenseCategories.length > 0" class="category-list">
        <view
          v-for="(cat, idx) in expenseCategories"
          :key="cat.category_id"
          class="category-item"
        >
          <view class="cat-left">
            <view
              class="cat-dot"
              :style="{ background: catColors[idx % catColors.length] }"
            />
            <text class="cat-name">{{ cat.name }}</text>
          </view>
          <view class="cat-right">
            <text class="cat-amount">-{{ formatCents(cat.amount) }}</text>
            <text class="cat-percent">{{ cat.percent }}%</text>
          </view>
        </view>
      </view>

      <view v-if="incomeCategories.length > 0" class="chart-section">
        <text class="section-title">收入分类</text>
      </view>

      <view v-if="incomeCategories.length > 0" class="category-list">
        <view
          v-for="(cat, idx) in incomeCategories"
          :key="cat.category_id"
          class="category-item"
        >
          <view class="cat-left">
            <view
              class="cat-dot"
              :style="{ background: incomeColors[idx % incomeColors.length] }"
            />
            <text class="cat-name">{{ cat.name }}</text>
          </view>
          <view class="cat-right">
            <text class="cat-amount income-text"
              >+{{ formatCents(cat.amount) }}</text
            >
            <text class="cat-percent">{{ cat.percent }}%</text>
          </view>
        </view>
      </view>

      <view
        v-if="expenseCategories.length === 0 && incomeCategories.length === 0"
        class="empty-state"
      >
        <text class="empty-icon">📊</text>
        <text class="empty-title">本月暂无数据</text>
      </view>

      <view class="list-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import EChart from "@/components/EChart.vue";
import { useTransactions } from "@/stores/transactionStore";
import { DEFAULT_CATEGORIES, getCategoryName } from "@/constants/categories";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { formatCents } from "@/utils/format";
import { getMonthlyStatistics, type MonthlyStats } from "@/api/common";

const { transactions, load } = useTransactions();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const statsData = ref<MonthlyStats | null>(null);
const isMockMode = ref(false);

const monthLabel = computed(
  () => `${currentYear.value}年 ${currentMonth.value}月`,
);

const totalExpense = computed(() => statsData.value?.total_expense ?? 0);
const totalIncome = computed(() => statsData.value?.total_income ?? 0);
const balance = computed(() => totalIncome.value - totalExpense.value);

interface CatItem {
  category_id: number;
  name: string;
  amount: number;
  count: number;
  percent: string;
}

const catColors = [
  "#ff6b35",
  "#5b9bd5",
  "#ff6b8a",
  "#a29bfe",
  "#fdcb6e",
  "#00b894",
  "#e17055",
  "#00cec9",
  "#fd79a8",
  "#636e72",
];

const incomeColors = [
  "#00b894",
  "#5b9bd5",
  "#fdcb6e",
  "#ff6b6b",
  "#00cec9",
  "#636e72",
];

function buildCategories(
  rawList: Array<{ category_id: number; amount: number; count: number }>,
  type: number,
): CatItem[] {
  const total = rawList.reduce((s, c) => s + c.amount, 0);
  return rawList.map((c) => ({
    category_id: c.category_id,
    name: getCategoryName(type, c.category_id),
    amount: c.amount,
    count: c.count,
    percent: total > 0 ? ((c.amount / total) * 100).toFixed(1) : "0",
  }));
}

const expenseCategories = computed(() => {
  if (!statsData.value?.expense_by_category) return [];
  return buildCategories(statsData.value.expense_by_category, 0);
});

const incomeCategories = computed(() => {
  if (!statsData.value?.income_by_category) return [];
  return buildCategories(statsData.value.income_by_category, 1);
});

const pieOption = computed(() => {
  const data = expenseCategories.value.map((c, i) => ({
    name: c.name,
    value: c.amount,
    itemStyle: { color: catColors[i % catColors.length] },
  }));

  return {
    tooltip: {
      trigger: "item",
      formatter: (params: any) => {
        const val = (params.value / 100).toFixed(2);
        return `${params.name}: -${val} (${params.percent}%)`;
      },
    },
    series: [
      {
        type: "pie",
        radius: ["30%", "55%"],
        center: ["50%", "50%"],
        data,
        label: {
          formatter: (params: any) => `${params.name}\n${params.percent}%`,
          fontSize: 11,
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0,0,0,0.3)",
          },
        },
      },
    ],
  };
});

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
}

function goBack() {
  uni.navigateBack();
}

function getMonthStr(): string {
  return `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
}

async function fetchStats() {
  const bookId = getCurrentBookId();
  try {
    statsData.value = await getMonthlyStatistics({
      book_id: bookId,
      month: getMonthStr(),
    });
  } catch {
    calcLocalStats(bookId);
  }
}

function calcLocalStats(bookId: number) {
  const prefix = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
  const map = new Map<number, { amount: number; count: number }>();
  let expense = 0;
  let income = 0;
  const expenseList: Array<{
    category_id: number;
    amount: number;
    count: number;
  }> = [];
  const incomeList: Array<{
    category_id: number;
    amount: number;
    count: number;
  }> = [];

  const all = transactions.value;
  for (const tx of all) {
    if (!tx.trans_time.startsWith(prefix)) continue;
    if (tx.book_id !== bookId) continue;
    if (tx.deleted) continue;

    const cid = tx.category_id ?? 0;
    if (tx.type === 0) {
      expense += tx.amount;
      const existing = map.get(cid);
      if (existing) {
        existing.amount += tx.amount;
        existing.count++;
      } else {
        map.set(cid, { amount: tx.amount, count: 1 });
      }
    } else if (tx.type === 1) {
      income += tx.amount;
      const key = cid + 10000;
      const existing = map.get(key);
      if (existing) {
        existing.amount += tx.amount;
        existing.count++;
      } else {
        map.set(key, { amount: tx.amount, count: 1 });
      }
    }
  }

  for (const [key, val] of map) {
    if (key >= 10000) {
      incomeList.push({
        category_id: key - 10000,
        amount: val.amount,
        count: val.count,
      });
    } else {
      expenseList.push({
        category_id: key,
        amount: val.amount,
        count: val.count,
      });
    }
  }

  expenseList.sort((a, b) => b.amount - a.amount);
  incomeList.sort((a, b) => b.amount - a.amount);

  statsData.value = {
    total_expense: expense,
    total_income: income,
    expense_by_category: expenseList,
    income_by_category: incomeList,
  };
}

watch([currentYear, currentMonth], () => {
  if (isMockMode.value) {
    calcLocalStats(getCurrentBookId());
  } else {
    fetchStats();
  }
});

onShow(async () => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  const token = uni.getStorageSync("token");
  isMockMode.value = token === "dev-mock-token";
  load();
  if (isMockMode.value) {
    calcLocalStats(getCurrentBookId());
  } else {
    await fetchStats();
  }
});
</script>

<style lang="scss" scoped>
.stats-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.stats-header {
  padding: 0 32rpx 28rpx;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: calc(var(--status-bar-height, 44px) + 16rpx) 0 28rpx;
}

.nav-left {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);

  .nav-back {
    font-size: 32rpx;
    color: var(--text-secondary);
    line-height: 1;
  }
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 0;
}

.nav-arrow {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);

  &:active {
    transform: scale(0.92);
  }

  text {
    font-size: 48rpx;
    color: var(--text-secondary);
    font-weight: 300;
    line-height: 1;
  }
}

.month-label {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text-primary);
  min-width: 320rpx;
  text-align: center;
}

.nav-right {
  width: 64rpx;
}

.month-summary {
  display: flex;
  gap: 16rpx;
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

.stats-body {
  flex: 1;
  box-sizing: border-box;
  padding: 0 20rpx 40rpx;
}

.chart-section {
  margin-top: 20rpx;
}


.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  padding: 0 16rpx 16rpx;
}

.category-list {
  margin-top: 28rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 24rpx;
  border-top: 1rpx solid var(--border-subtle);

  &:first-child {
    border-top: none;
  }
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.cat-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-name {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;
}

.cat-right {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.cat-amount {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-expense);
  font-variant-numeric: tabular-nums;

  &.income-text {
    color: var(--color-income);
  }
}

.cat-percent {
  font-size: 24rpx;
  color: var(--text-muted);
  min-width: 72rpx;
  text-align: right;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 240rpx 0;

  .empty-icon {
    font-size: 72rpx;
  }

  .empty-title {
    font-size: 30rpx;
    color: var(--text-muted);
    margin-top: 24rpx;
  }
}

.list-bottom {
  height: 60rpx;
}
</style>
