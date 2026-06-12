<template>
  <view class="stats-page">
    <NavBar>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon name="left" size="24px" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <view class="nav-center">
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
        <view class="nav-right" />
      </template>
    </NavBar>

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

    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: activeTab === 0 }"
        @click="activeTab = 0"
      >
        <text>支出</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 1 }"
        @click="activeTab = 1"
      >
        <text>收入</text>
      </view>
    </view>

    <scroll-view scroll-y class="stats-body">
      <view v-if="currentCategories.length > 0" class="chart-section">
        <text class="section-title">{{ activeTab === 0 ? '支出分类' : '收入分类' }}</text>
        <EChart :options="pieOption" :height="420" />
      </view>

      <view v-if="currentCategories.length > 0" class="category-list">
        <view
          v-for="(cat, idx) in currentCategories"
          :key="cat.category_id"
          class="category-item"
        >
          <view class="cat-left">
            <view
              class="cat-dot"
              :style="{ background: cat.color }"
            />
            <text class="cat-name">{{ cat.name }}</text>
          </view>
          <view class="cat-right">
            <text class="cat-amount" :class="{ 'income-text': activeTab === 1 }"
              >{{ activeTab === 0 ? '-' : '+' }}{{ formatCents(cat.amount) }}</text
            >
            <text class="cat-percent">{{ cat.percent }}%</text>
          </view>
        </view>
      </view>

      <view
        v-if="expenseCategories.length === 0 && incomeCategories.length === 0"
        class="empty-state"
      >
        <wd-icon name="dashboard" size="72rpx" color="var(--text-muted)" />
        <text class="empty-title">本月暂无数据</text>
      </view>

      <view class="list-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import EChart from "@/components/EChart.vue";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { formatCents } from "@/utils/format";
import { getMonthlyStatistics, type MonthlyStats } from "@/api/common";

const { transactions, load } = useTransactions();
const { getCategoryName, getCategoryColor, loadCategories } = useCategories();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const statsData = ref<MonthlyStats | null>(null);
const isMockMode = ref(false);
const activeTab = ref(0);

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
  color: string;
}

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
    color: getCategoryColor(type, c.category_id),
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

const currentCategories = computed(() =>
  activeTab.value === 0 ? expenseCategories.value : incomeCategories.value,
);

const pieOption = computed(() => {
  const cats = currentCategories.value;
  const prefix = activeTab.value === 0 ? "-" : "+";
  const data = cats.map((c) => ({
    name: c.name,
    value: c.amount,
    itemStyle: { color: c.color },
  }));

  return {
    tooltip: {
      trigger: "item",
      formatter: (params: any) => {
        const val = (params.value / 100).toFixed(2);
        return `${params.name}: ${prefix}${val} (${params.percent}%)`;
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
  loadCategories();
  if (isMockMode.value) {
    calcLocalStats(getCurrentBookId());
  } else {
    await fetchStats();
  }
});
</script>

<style lang="scss" scoped>
.stats-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  overflow: hidden;
}

.nav-left {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
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

.nav-right {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
}

.month-summary {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 32rpx 16rpx;
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

.tab-bar {
  display: flex;
  margin: 0 32rpx 16rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 6rpx;
  box-shadow: var(--shadow-sm);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: calc(var(--radius-lg) - 4rpx);
  font-size: 28rpx;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.2s;

  &.active {
    background: var(--color-primary);
    color: #fff;
    font-weight: 600;
  }
}

.stats-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 0 20rpx 60rpx;
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
