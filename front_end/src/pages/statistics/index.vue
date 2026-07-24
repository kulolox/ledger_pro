<template>
  <view class="stats-page">
    <NavBar>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon
            name="arrow-left"
            size="24px"
            color="var(--text-secondary)"
          />
        </view>
      </template>
      <template #center>
        <view class="nav-center">
          <view class="nav-arrow" @click="prevPeriod">
            <wd-icon name="left" size="24px" color="var(--text-secondary)" />
          </view>
          <text class="month-label">{{ periodLabel }}</text>
          <view class="nav-arrow" @click="nextPeriod">
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

      <view class="period-segment">
        <view
          class="segment-item"
          :class="{ active: periodMode === 'month' }"
          @click="periodMode = 'month'"
        >
          <text>按月</text>
        </view>
        <view
          class="segment-item"
          :class="{ active: periodMode === 'year' }"
          @click="periodMode = 'year'"
        >
          <text>按年</text>
        </view>
      </view>

      <view class="stats-tabs">
        <wd-tabs v-model="activeTab" :key="periodMode" custom-class="tab-custom">
          <wd-tab v-for="tab in tabs" :key="tab" :title="tab" />
        </wd-tabs>
      </view>

      <scroll-view scroll-y class="stats-body">
        <!-- 按月内容 -->
        <template v-if="periodMode === 'month'">
          <view class="stats-card">
            <view v-if="currentCategories.length > 0" class="chart-section">
              <EChart type="ring" :chartData="chartData" :opts="chartOpts" :height="320" />
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
                    :class="{ 'income-text': activeTab === 1 || activeTab === 2 }"
                  >
                    {{ cat.name && activeTab === 0 ? "-" : "+" }}{{ formatCents(cat.amount) }}
                  </text>
                  <text class="cat-percent">{{ cat.percent }}%</text>
                </view>
              </view>
            </view>

            <view v-if="currentCategories.length === 0" class="empty-state">
              <wd-icon name="dashboard" size="72rpx" color="var(--text-muted)" />
              <text class="empty-title">暂无数据</text>
            </view>
          </view>

          <view v-if="dailyRows.length > 0" class="daily-table-card">
            <text class="section-title">每日报表</text>
            <view class="daily-table">
              <view class="daily-header">
                <text class="daily-col date-col">日期</text>
                <text class="daily-col amount-col">支出</text>
                <text class="daily-col amount-col">收入</text>
                <text class="daily-col amount-col">结余</text>
              </view>
              <view v-for="row in dailyRows" :key="row.date" class="daily-row">
                <text class="daily-col date-col">{{ formatDayLabel(row.date) }}</text>
                <text class="daily-col amount-col">{{ formatCents(row.expense) }}</text>
                <text class="daily-col amount-col">{{ formatCents(row.income) }}</text>
                <text class="daily-col amount-col" :class="row.income - row.expense >= 0 ? 'income-text' : 'expense-text'">
                  {{ row.income - row.expense >= 0 ? '+' : '-' }}{{ formatCents(Math.abs(row.income - row.expense)) }}
                </text>
              </view>
            </view>
          </view>
        </template>

        <!-- 按年内容 -->
        <template v-else>
          <view class="stats-card">
            <view class="chart-section">
              <EChart type="line" :chartData="yearChartData" :opts="yearChartOpts" :height="300" />
            </view>

            <view v-if="activeTab !== 2 && yearCurrentCategories.length > 0" class="chart-section">
              <EChart type="ring" :chartData="yearCatChartData" :opts="chartOpts" :height="300" />
            </view>

            <view v-if="activeTab !== 2 && yearCurrentCategories.length > 0" class="category-list">
              <view
                v-for="(cat, idx) in yearCurrentCategories"
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
                    :class="{ 'income-text': activeTab === 1 || activeTab === 3 }"
                  >
                    {{ activeTab === 0 ? "-" : "+" }}{{ formatCents(cat.amount) }}
                  </text>
                  <text class="cat-percent">{{ cat.percent }}%</text>
                </view>
              </view>
            </view>

            <view v-if="activeTab !== 2 && yearCurrentCategories.length === 0" class="empty-state">
              <wd-icon name="dashboard" size="72rpx" color="var(--text-muted)" />
              <text class="empty-title">暂无数据</text>
            </view>
          </view>

          <view class="daily-table-card">
            <text class="section-title">月报表</text>
            <view class="daily-table">
              <view class="daily-header">
                <text class="daily-col date-col">月份</text>
                <text class="daily-col amount-col">支出</text>
                <text class="daily-col amount-col">收入</text>
                <text class="daily-col amount-col">结余</text>
              </view>
              <view
                v-for="row in yearlyRowsReversed"
                :key="row.month"
                class="daily-row"
                @click="drillToMonth(row.month)"
              >
                <text class="daily-col date-col">{{ row.month }}月</text>
                <text class="daily-col amount-col">{{ formatCents(row.expense) }}</text>
                <text class="daily-col amount-col">{{ formatCents(row.income) }}</text>
                <text class="daily-col amount-col" :class="row.income - row.expense >= 0 ? 'income-text' : 'expense-text'">
                  {{ row.income - row.expense >= 0 ? '+' : '-' }}{{ formatCents(Math.abs(row.income - row.expense)) }}
                </text>
              </view>
            </view>
          </view>
        </template>

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
const periodMode = ref<"month" | "year">("month");
const activeTab = ref(0);
const pageLoading = ref(true);
const totalExpense = ref(0);
const totalIncome = ref(0);
const expenseCategories = ref<CatItem[]>([]);
const incomeCategories = ref<CatItem[]>([]);
const dailyRows = ref<DailyRow[]>([]);
const yearlyRows = ref<YearlyRow[]>([]);
const yearExpenseCategories = ref<CatItem[]>([]);
const yearIncomeCategories = ref<CatItem[]>([]);

interface YearlyRow {
  month: number;
  expense: number;
  income: number;
}

interface DailyRow {
  date: string;
  expense: number;
  income: number;
}

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

const periodLabel = computed(() =>
  periodMode.value === "month"
    ? `${currentYear.value}年 ${currentMonth.value}月`
    : `${currentYear.value}年`,
);

const balance = computed(() => totalIncome.value - totalExpense.value);

const tabs = computed(() =>
  periodMode.value === "month"
    ? ["支出", "收入", "全部"]
    : ["支出", "收入", "结余", "全部"],
);

const allCategories = computed(() => {
  const merged: Record<number, { category_id: number; type: number; amount: number; count: number }> = {};
  for (const c of expenseCategories.value) {
    merged[c.category_id] = { category_id: c.category_id, type: 0, amount: c.amount, count: c.count };
  }
  for (const c of incomeCategories.value) {
    if (merged[c.category_id]) {
      merged[c.category_id].amount += c.amount;
      merged[c.category_id].count += c.count;
    } else {
      merged[c.category_id] = { category_id: c.category_id, type: 1, amount: c.amount, count: c.count };
    }
  }
  const total = Object.values(merged).reduce((s, r) => s + r.amount, 0);
  return Object.values(merged)
    .map((r) => ({
      category_id: r.category_id,
      name: getCategoryName(r.type, r.category_id),
      amount: r.amount,
      count: r.count,
      percent: total > 0 ? ((r.amount / total) * 100).toFixed(1) : "0",
      color: getCategoryColor(r.type, r.category_id),
    }))
    .sort((a, b) => b.amount - a.amount);
});

const currentCategories = computed(() => {
  if (activeTab.value === 0) return expenseCategories.value;
  if (activeTab.value === 1) return incomeCategories.value;
  return allCategories.value;
});

function formatDayLabel(dateStr: string): string {
  const parts = dateStr.split("-");
  return `${parseInt(parts[1])}月${parseInt(parts[2])}日`;
}

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

const chartData = computed(() => {
  const data = currentCategories.value.map((c) => ({
    name: c.name,
    value: c.amount,
    color: c.color,
  }));
  return { series: [{ data, name: "" }] };
});

const chartOpts = computed(() => ({
  background: "#FFFFFF",
  dataLabel: true,
  tooltip: {
    showBox: true,
  },
  extra: {
    ring: {
      ringWidth: 40,
      labelWidth: 15,
    },
  },
}));

const yearChartData = computed(() => {
  const rows = yearlyRows.value.length > 0 ? yearlyRows.value : Array.from({ length: 12 }, () => ({ expense: 0, income: 0 }));
  const months = Array.from({ length: 12 }, (_, i) => `${i + 1}月`);
  return {
    categories: months,
    series: [
      { name: "支出", data: rows.map((r) => r.expense / 100), color: "#ff3b30" },
      { name: "收入", data: rows.map((r) => r.income / 100), color: "#34c759" },
      { name: "结余", data: rows.map((r) => (r.income - r.expense) / 100), color: "#007aff" },
    ],
  };
});

const yearAllCategories = computed(() => {
  const merged: Record<number, { category_id: number; type: number; amount: number; count: number }> = {};
  for (const c of yearExpenseCategories.value) {
    merged[c.category_id] = { category_id: c.category_id, type: 0, amount: c.amount, count: c.count };
  }
  for (const c of yearIncomeCategories.value) {
    if (merged[c.category_id]) {
      merged[c.category_id].amount += c.amount;
      merged[c.category_id].count += c.count;
    } else {
      merged[c.category_id] = { category_id: c.category_id, type: 1, amount: c.amount, count: c.count };
    }
  }
  const total = Object.values(merged).reduce((s, r) => s + r.amount, 0);
  return Object.values(merged)
    .map((r) => ({
      category_id: r.category_id,
      name: getCategoryName(r.type, r.category_id),
      amount: r.amount,
      count: r.count,
      percent: total > 0 ? ((r.amount / total) * 100).toFixed(1) : "0",
      color: getCategoryColor(r.type, r.category_id),
    }))
    .sort((a, b) => b.amount - a.amount);
});

const yearCurrentCategories = computed(() => {
  if (activeTab.value === 0) return yearExpenseCategories.value;
  if (activeTab.value === 1) return yearIncomeCategories.value;
  if (activeTab.value === 2) return [];
  return yearAllCategories.value;
});

const yearlyRowsReversed = computed(() =>
  [...yearlyRows.value].reverse(),
);

const yearChartOpts = computed(() => ({
  background: "#FFFFFF",
  padding: [15, 15, 0, 15],
  dataLabel: false,
  tooltip: { showBox: true, group: [], index: [] },
  legend: { show: true },
  yAxis: {
    disabled: false,
    disableGrid: false,
    gridColor: "#f0f0f0",
    fontColor: "#8e8e93",
    fontSize: 11,
  },
  xAxis: {
    disableGrid: true,
    fontColor: "#8e8e93",
    fontSize: 11,
    boundaryGap: "center",
  },
  extra: {
    line: {
      type: "straight",
      width: 2,
    },
  },
}));

const yearCatChartData = computed(() => {
  const data = yearCurrentCategories.value.map((c) => ({
    name: c.name,
    value: c.amount,
    color: c.color,
  }));
  return { series: [{ data, name: "" }] };
});

function prevPeriod() {
  if (periodMode.value === "month") {
    if (currentMonth.value === 1) {
      currentMonth.value = 12;
      currentYear.value--;
    } else {
      currentMonth.value--;
    }
  } else {
    currentYear.value--;
  }
}

function nextPeriod() {
  if (periodMode.value === "month") {
    if (currentMonth.value === 12) {
      currentMonth.value = 1;
      currentYear.value++;
    } else {
      currentMonth.value++;
    }
  } else {
    currentYear.value++;
  }
}

function drillToMonth(month: number) {
  periodMode.value = "month";
  currentMonth.value = month;
}

function goBack() {
  uni.navigateBack();
}

async function fetchStats() {
  if (periodMode.value === "year") {
    await fetchYearStats();
  } else {
    await fetchMonthStats();
  }
}

async function fetchYearStats() {
  const y = String(currentYear.value);
  try {
    const expenseTotal = await select<{ total: number }>(
      `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE trans_time LIKE '${y}%' AND type = 0`,
    );
    totalExpense.value = expenseTotal[0]?.total ?? 0;

    const incomeTotal = await select<{ total: number }>(
      `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE trans_time LIKE '${y}%' AND type = 1`,
    );
    totalIncome.value = incomeTotal[0]?.total ?? 0;

    const monthlyRaw = await select<{
      month: string;
      type: number;
      amount: number;
    }>(
      `SELECT strftime('%m', trans_time) as month, type, SUM(amount) as amount FROM transactions WHERE trans_time LIKE '${y}%' GROUP BY month, type ORDER BY month`,
    );
    const monthMap: Record<number, { expense: number; income: number }> = {};
    for (const r of monthlyRaw) {
      const m = parseInt(r.month);
      if (!monthMap[m]) monthMap[m] = { expense: 0, income: 0 };
      if (r.type === 0) monthMap[m].expense += r.amount;
      else monthMap[m].income += r.amount;
    }
    yearlyRows.value = Array.from({ length: 12 }, (_, i) => ({
      month: i + 1,
      expense: monthMap[i + 1]?.expense ?? 0,
      income: monthMap[i + 1]?.income ?? 0,
    }));

    const yearExpenseRows = await select<{
      category_id: number;
      amount: number;
      count: number;
    }>(
      `SELECT category_id, SUM(amount) as amount, COUNT(*) as count FROM transactions WHERE trans_time LIKE '${y}%' AND type = 0 GROUP BY category_id ORDER BY amount DESC`,
    );
    yearExpenseCategories.value = buildCatItems(yearExpenseRows, 0);

    const yearIncomeRows = await select<{
      category_id: number;
      amount: number;
      count: number;
    }>(
      `SELECT category_id, SUM(amount) as amount, COUNT(*) as count FROM transactions WHERE trans_time LIKE '${y}%' AND type = 1 GROUP BY category_id ORDER BY amount DESC`,
    );
    yearIncomeCategories.value = buildCatItems(yearIncomeRows, 1);
  } catch {
    // ignore
  }
}

async function fetchMonthStats() {
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

    const dailyRaw = await select<{
      date: string;
      type: number;
      amount: number;
    }>(
      `SELECT DATE(trans_time) as date, type, SUM(amount) as amount FROM transactions WHERE trans_time LIKE '${m}%' GROUP BY date, type ORDER BY date`,
    );
    const dayMap: Record<string, { expense: number; income: number }> = {};
    for (const r of dailyRaw) {
      if (!dayMap[r.date]) dayMap[r.date] = { expense: 0, income: 0 };
      if (r.type === 0) dayMap[r.date].expense += r.amount;
      else dayMap[r.date].income += r.amount;
    }
    dailyRows.value = Object.entries(dayMap)
      .map(([date, v]) => ({ date, ...v }))
      .sort((a, b) => b.date.localeCompare(a.date));
  } catch {
    // ignore
  }
}

watch([currentYear, currentMonth, periodMode], () => {
  activeTab.value = 0;
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

.period-segment {
  display: flex;
  margin: 0 32rpx 16rpx;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  padding: 4rpx;

  .segment-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12rpx 0;
    border-radius: var(--radius-xs);
    font-size: 26rpx;
    color: var(--text-secondary);
    transition: all 0.2s;

    &.active {
      background: var(--bg-secondary);
      color: var(--color-primary);
      font-weight: 600;
      box-shadow: var(--shadow-sm);
    }
  }
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
  margin: 0 32rpx;
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
  padding: 0 32rpx 60rpx;
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
  padding: 80rpx 0;

  .empty-title {
    font-size: 30rpx;
    color: var(--text-muted);
    margin-top: 24rpx;
  }
}

.daily-table-card {
  margin-top: 24rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.daily-table {
  .daily-header {
    display: flex;
    padding: 12rpx 0;
    border-bottom: 1rpx solid var(--border-primary);

    .daily-col {
      font-size: 24rpx;
      color: var(--text-label);
      font-weight: 500;
    }
  }

  .daily-row {
    display: flex;
    padding: 16rpx 0;
    border-bottom: 1rpx solid var(--border-subtle);

    &:last-child {
      border-bottom: none;
    }
  }

  .daily-col {
    font-size: 26rpx;
    font-variant-numeric: tabular-nums;

    &.date-col {
      flex: 0 0 120rpx;
      color: var(--text-secondary);
    }

    &.amount-col {
      flex: 1;
      text-align: right;
    }

    &.expense-text {
      color: var(--color-expense);
    }

    &.income-text {
      color: var(--color-income);
    }
  }
}

.list-bottom {
  height: 60rpx;
}
</style>
