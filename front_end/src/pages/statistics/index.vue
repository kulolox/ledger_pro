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
          <view class="month-capsule month-nav">
            <view class="capsule-arrow nav-arrow" @click="prevMonth">
              <wd-icon name="left" size="14px" color="var(--text-secondary)" />
            </view>
            <view class="capsule-label-wrap">
              <text class="capsule-label month-label">{{ monthLabel }}</text>
            </view>
            <view class="capsule-arrow nav-arrow" @click="nextMonth">
              <wd-icon name="right" size="14px" color="var(--text-secondary)" />
            </view>
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
          <!-- 有数据时展示：Apple 甜甜圈图表 + 分类列表 -->
          <template v-if="currentCategories.length > 0">
            <view class="chart-section">
              <EChart :options="pieOption" :height="280" />
            </view>

            <view class="category-list">
              <view
                v-for="cat in currentCategories"
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
          </template>

          <!-- 无数据时展示：Apple 空状态精致圆环占位图 + 引导记账按钮 -->
          <view v-else class="empty-chart-section">
            <view class="empty-donut-ring">
              <view class="empty-donut-center">
                <text class="empty-donut-symbol">¥</text>
                <text class="empty-donut-value">0.00</text>
                <text class="empty-donut-label">暂无{{ activeTab === 0 ? "支出" : "收入" }}</text>
              </view>
            </view>
            <text class="empty-hint-text">本月还没有{{ activeTab === 0 ? "支出" : "收入" }}账单数据</text>
            <view class="empty-action-btn" @click="goRecord">
              <wd-icon name="plus" size="28rpx" color="var(--color-primary)" />
              <text class="empty-action-label">记一笔</text>
            </view>
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
const activeTab = ref(0);
const pageLoading = ref(true);

const monthLabel = computed(
  () => `${currentYear.value}年 ${currentMonth.value}月`,
);

const totalExpense = computed(() => statsData.value?.total_expense ?? 0);
const totalIncome = computed(() => statsData.value?.total_income ?? 0);
const balance = computed(() => totalIncome.value - totalExpense.value);

interface CatItem {
  category_id: string;
  name: string;
  amount: number;
  count: number;
  percent: string;
  color: string;
}

function buildCategories(
  rawList: Array<{ category_id: string; amount: number; count: number }>,
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

const currentTotal = computed(() =>
  activeTab.value === 0 ? totalExpense.value : totalIncome.value,
);

const pieOption = computed(() => {
  const cats = currentCategories.value;
  const prefix = activeTab.value === 0 ? "-" : "+";
  const totalAmount = currentTotal.value;
  const data = cats.map((c) => ({
    name: c.name,
    value: c.amount,
    itemStyle: { color: c.color },
  }));

  const isDark =
    typeof document !== "undefined" &&
    document.documentElement.getAttribute("data-theme") === "dark";
  const textColor = isDark ? "#ffffff" : "#1c1c1e";
  const subTextColor = isDark ? "#8e8e93" : "#8e8e93";

  return {
    backgroundColor: "transparent",
    tooltip: {
      trigger: "item",
      backgroundColor: isDark ? "#1c1c1e" : "#ffffff",
      borderColor: isDark ? "rgba(255,255,255,0.12)" : "rgba(60,60,67,0.12)",
      textStyle: {
        color: textColor,
        fontSize: 12,
      },
      formatter: (params: any) => {
        const val = (params.value / 100).toFixed(2);
        return `${params.name}: ${prefix}¥${val} (${params.percent}%)`;
      },
    },
    title: {
      text: `${prefix}¥${(totalAmount / 100).toFixed(2)}`,
      subtext: activeTab.value === 0 ? "总支出" : "总收入",
      left: "center",
      top: "40%",
      textStyle: {
        fontSize: 17,
        fontWeight: 700,
        color: activeTab.value === 0 ? "#ff3b30" : "#34c759",
      },
      subtextStyle: {
        fontSize: 11,
        color: subTextColor,
        fontWeight: 500,
      },
    },
    series: [
      {
        type: "pie",
        radius: ["46%", "68%"],
        center: ["50%", "50%"],
        data,
        padAngle: 3,
        itemStyle: {
          borderRadius: 6,
          borderColor: isDark ? "#1c1c1e" : "#ffffff",
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: (params: any) => `${params.name} ${params.percent}%`,
          fontSize: 11,
          color: textColor,
        },
        labelLine: {
          length: 6,
          length2: 10,
          lineStyle: {
            color: subTextColor,
          },
        },
        emphasis: {
          scale: true,
          scaleSize: 6,
          itemStyle: {
            shadowBlur: 10,
            shadowColor: "rgba(0,0,0,0.15)",
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

function goRecord() {
  uni.reLaunch({ url: "/pages/index/index" });
}

function getMonthStr(): string {
  return `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
}

async function fetchStats() {
  const bookId = getCurrentBookId();
  try {
    const res = await getMonthlyStatistics({
      book_id: bookId,
      month: getMonthStr(),
    });
    if (
      res &&
      (res.total_expense > 0 ||
        res.total_income > 0 ||
        (res.expense_by_category && res.expense_by_category.length > 0) ||
        (res.income_by_category && res.income_by_category.length > 0))
    ) {
      statsData.value = res;
    } else {
      calcLocalStats(bookId);
    }
  } catch {
    calcLocalStats(bookId);
  }
}

function calcLocalStats(bookId: string) {
  const prefix = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
  const map = new Map<string, { amount: number; count: number }>();
  let expense = 0;
  let income = 0;
  const expenseList: Array<{
    category_id: string;
    amount: number;
    count: number;
  }> = [];
  const incomeList: Array<{
    category_id: string;
    amount: number;
    count: number;
  }> = [];

  const all = transactions.value;
  for (const tx of all) {
    if (!tx.trans_time.startsWith(prefix)) continue;
    if (tx.book_id !== bookId) continue;
    if (tx.deleted) continue;

    const cid = tx.category_id ?? "0";
    if (tx.type === 0) {
      expense += tx.amount;
      const key = "e:" + cid;
      const existing = map.get(key);
      if (existing) {
        existing.amount += tx.amount;
        existing.count++;
      } else {
        map.set(key, { amount: tx.amount, count: 1 });
      }
    } else if (tx.type === 1) {
      income += tx.amount;
      const key = "i:" + cid;
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
    if (key.startsWith("i:")) {
      incomeList.push({
        category_id: key.slice(2),
        amount: val.amount,
        count: val.count,
      });
    } else {
      expenseList.push({
        category_id: key.slice(2),
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
  fetchStats();
});

onShow(async () => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  const startTime = Date.now();
  load();
  loadCategories();
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
  width: 68rpx;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.92);
  }
}

.nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.month-capsule {
  display: inline-flex;
  align-items: center;
  background: var(--bg-primary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-full);
  padding: 4rpx 8rpx;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s var(--spring-default);
}

.capsule-arrow {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s var(--spring-default), background-color 0.15s;

  &:active {
    background: var(--bg-hover);
    transform: scale(0.9);
  }
}

.capsule-label-wrap {
  padding: 0 16rpx;
}

.capsule-label {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5rpx;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.nav-arrow {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  transition: transform 0.15s var(--spring-default), background-color 0.15s;

  &:active {
    transform: scale(0.9);
  }
}

.month-label {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
  letter-spacing: -0.5rpx;
  flex-shrink: 0;
}

.nav-right {
  width: 68rpx;
  height: 68rpx;
  flex-shrink: 0;
}

.month-summary {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 28rpx 20rpx;
  background: var(--bg-primary);
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 24rpx 8rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.98);
  }

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
  border: 1rpx solid var(--border-subtle);
  border-top: none;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.stats-tabs {
  margin: 0 24rpx;
  padding: 0 24rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-bottom: none;
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

.empty-chart-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx 70rpx;
}

.empty-donut-ring {
  width: 260rpx;
  height: 260rpx;
  border-radius: 50%;
  border: 14rpx solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-sizing: border-box;
}

.empty-donut-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;

  .empty-donut-symbol {
    font-size: 24rpx;
    color: var(--text-muted);
    font-weight: 500;
  }

  .empty-donut-value {
    font-size: 40rpx;
    font-weight: 700;
    color: var(--text-tertiary);
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.5rpx;
  }

  .empty-donut-label {
    font-size: 22rpx;
    color: var(--text-muted);
    font-weight: 500;
    margin-top: 2rpx;
  }
}

.empty-hint-text {
  font-size: 26rpx;
  color: var(--text-muted);
  margin-top: 36rpx;
  font-weight: 500;
}

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 28rpx;
  padding: 16rpx 44rpx;
  background: var(--bg-primary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.96);
  }

  .empty-action-label {
    font-size: 28rpx;
    font-weight: 600;
    color: var(--color-primary);
  }
}

.list-bottom {
  height: 60rpx;
}
</style>
