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

    <PageLoader v-if="pageLoading" />

    <template v-else>
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

      <view class="stats-tabs">
        <wd-tabs v-model="activeTab" custom-class="tab-custom">
          <wd-tab title="支出" />
          <wd-tab title="收入" />
        </wd-tabs>
      </view>

      <scroll-view scroll-y class="stats-body">
        <view class="stats-card">
          <view v-if="currentCategories.length > 0" class="chart-section">
            <EChart :options="pieOption" :height="420" />
          </view>

          <view v-if="currentCategories.length > 0" class="category-list">
            <view
              v-for="(cat, idx) in currentCategories"
              :key="cat.category_id"
              class="category-item"
            >
              <view class="cat-left">
                <view class="cat-dot" :style="{ background: cat.color }" />
                <text class="cat-name">{{ cat.name }}</text>
              </view>
              <view class="cat-right">
                <text
                  class="cat-amount"
                  :class="{ 'income-text': activeTab === 1 }"
                >
                  {{ activeTab === 0 ? "-" : "+" }}{{ formatCents(cat.amount) }}
                </text>
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
        </view>

        <view class="list-bottom" />
      </scroll-view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import EChart from "@/components/EChart.vue";
import PageLoader from "@/components/PageLoader.vue";
import { useCategories } from "@/stores/categoryStore";
import { formatCents } from "@/utils/format";
import { select } from "@/utils/db";

const { getCategoryName, getCategoryColor, loadCategories } = useCategories();

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const activeTab = ref(0);
const pageLoading = ref(true);
const totalExpense = ref(0);
const totalIncome = ref(0);
const expenseCategories = ref<CatItem[]>([]);
const incomeCategories = ref<CatItem[]>([]);

interface CatItem {
  category_id: number;
  name: string;
  amount: number;
  count: number;
  percent: string;
  color: string;
}

function getMonthStr(): string {
  return `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
}

const monthLabel = computed(
  () => `${currentYear.value}年 ${currentMonth.value}月`,
);

const balance = computed(() => totalIncome.value - totalExpense.value);

const currentCategories = computed(() =>
  activeTab.value === 0 ? expenseCategories.value : incomeCategories.value,
);

function buildCatItems(
  rows: { category_id: number; amount: number; count: number }[],
  type: number,
): CatItem[] {
  const total = rows.reduce((s, r) => s + r.amount, 0);
  return rows.map((r) => ({
    category_id: r.category_id,
    name: getCategoryName(type, r.category_id),
    amount: r.amount,
    count: r.count,
    percent: total > 0 ? ((r.amount / total) * 100).toFixed(1) : "0",
    color: getCategoryColor(type, r.category_id),
  }));
}

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

async function fetchStats() {
  const m = getMonthStr();
  try {
    const expenseTotal = await select<{ total: number }>(
      `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE trans_time LIKE '${m}%' AND type = 0`,
    );
    totalExpense.value = expenseTotal[0]?.total ?? 0;

    const incomeTotal = await select<{ total: number }>(
      `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE trans_time LIKE '${m}%' AND type = 1`,
    );
    totalIncome.value = incomeTotal[0]?.total ?? 0;

    const expenseRows = await select<{
      category_id: number;
      amount: number;
      count: number;
    }>(
      `SELECT category_id, SUM(amount) as amount, COUNT(*) as count FROM transactions WHERE trans_time LIKE '${m}%' AND type = 0 GROUP BY category_id ORDER BY amount DESC`,
    );
    expenseCategories.value = buildCatItems(expenseRows, 0);

    const incomeRows = await select<{
      category_id: number;
      amount: number;
      count: number;
    }>(
      `SELECT category_id, SUM(amount) as amount, COUNT(*) as count FROM transactions WHERE trans_time LIKE '${m}%' AND type = 1 GROUP BY category_id ORDER BY amount DESC`,
    );
    incomeCategories.value = buildCatItems(incomeRows, 1);
  } catch {
    // ignore
  }
}

watch([currentYear, currentMonth], () => {
  fetchStats();
});

onShow(async () => {
  const startTime = Date.now();
  await loadCategories();
  await fetchStats();
  await nextTick();
  const elapsed = Date.now() - startTime;
  if (elapsed < 500) {
    await new Promise((r) => setTimeout(r, 500 - elapsed));
  }
  pageLoading.value = false;
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

.stats-card {
  background: var(--bg-secondary);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.stats-tabs {
  margin: 0 20rpx;
  padding: 0 24rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

:deep(.tab-custom.wd-tabs) {
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

.stats-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 0 20rpx 60rpx;
}

.chart-section {
  padding: 8rpx 0 0;
}

.category-list {
  border-top: 1rpx solid var(--border-subtle);
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
  padding: 120rpx 0;

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
