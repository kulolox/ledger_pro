<template>
  <view class="stats-page">
    <NavBar floating>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <view class="nav-center">
          <view class="month-capsule month-nav">
            <view class="capsule-arrow nav-arrow" @click="prevMonth">
              <wd-icon name="left" size="28rpx" color="var(--text-tertiary)" />
            </view>
            <view class="capsule-label-wrap">
              <text class="capsule-label month-label">{{ monthLabel }}</text>
            </view>
            <view class="capsule-arrow nav-arrow" @click="nextMonth">
              <wd-icon name="right" size="28rpx" color="var(--text-tertiary)" />
            </view>
          </view>
        </view>
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

      <scroll-view scroll-y class="stats-body">
        <view class="stats-card">
          <view class="stats-tabs">
            <wd-tabs v-model="activeTab" custom-class="tab-custom">
              <wd-tab title="支出" />
              <wd-tab title="收入" />
            </wd-tabs>
          </view>

          <!-- 有数据时展示：Apple 甜甜圈图表 + 分类列表 + 洞察摘要 -->
          <template v-if="currentCategories.length > 0">
            <view class="chart-section">
              <EChart :options="pieOption" :height="270" />
            </view>

            <view class="category-list">
              <view
                v-for="cat in currentCategories"
                :key="cat.category_id"
                class="category-item"
              >
                <view class="cat-left">
                  <view class="cat-dot" :style="{ background: cat.color }" />
                  <view
                    class="cat-icon-badge"
                    :style="{ background: cat.color + '20' }"
                  >
                    <text>{{ cat.icon }}</text>
                  </view>
                  <view class="cat-meta">
                    <text class="cat-name">{{ cat.name }}</text>
                    <text class="cat-count">{{ cat.count }} 笔交易</text>
                  </view>
                </view>
                <view class="cat-right">
                  <text
                    class="cat-amount"
                    :class="{ 'income-text': activeTab === 1 }"
                  >
                    {{ activeTab === 0 ? "-" : "+"
                    }}{{ formatCents(cat.amount) }}
                  </text>
                  <text class="cat-percent">{{ cat.percent }}%</text>
                </view>
              </view>

              <view class="insights-footer">
                <view class="insight-row">
                  <text class="insight-label"
                    >日均{{ activeTab === 0 ? "支出" : "收入" }}</text
                  >
                  <text class="insight-value">¥{{ dailyAverage }} / 天</text>
                </view>
                <view class="insight-row">
                  <text class="insight-label">本月累计笔数</text>
                  <text class="insight-value">共 {{ totalTxCount }} 笔</text>
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
                <text class="empty-donut-label"
                  >暂无{{ activeTab === 0 ? "支出" : "收入" }}</text
                >
              </view>
            </view>
            <text class="empty-hint-text"
              >本月还没有{{ activeTab === 0 ? "支出" : "收入" }}账单数据</text
            >
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
import { useTheme } from "@/stores/themeStore";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { formatCents } from "@/utils/format";
import { getMonthlyStatistics, type MonthlyStats } from "@/api/common";

const { transactions, load } = useTransactions();
const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories } =
  useCategories();
const { themeMode } = useTheme();

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
  icon: string;
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
    icon: getCategoryIcon(type, c.category_id),
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

const totalTxCount = computed(() => {
  return currentCategories.value.reduce((s, c) => s + Number(c.count), 0);
});

const dailyAverage = computed(() => {
  const daysInMonth = new Date(
    currentYear.value,
    currentMonth.value,
    0,
  ).getDate();
  const avgCents = currentTotal.value / (daysInMonth || 30);
  return formatCents(avgCents);
});

const pieOption = computed(() => {
  const cats = currentCategories.value;
  const prefix = activeTab.value === 0 ? "-" : "+";
  const totalAmount = currentTotal.value;
  const centerText = `${prefix}¥${formatCents(totalAmount)}`;
  const centerFontSize =
    centerText.length > 10 ? 13 : centerText.length > 8 ? 15 : 17;
  const data = cats.map((c) => ({
    name: c.name,
    value: c.amount,
    itemStyle: { color: c.color },
  }));

  const isDark = themeMode.value === "dark";
  const textColor = isDark ? "#ffffff" : "#1c1c1e";
  const subTextColor = "#8e8e93";

  return {
    backgroundColor: "transparent",
    tooltip: {
      trigger: "item",
      backgroundColor: isDark ? "#2c2c2e" : "#ffffff",
      borderColor: isDark ? "rgba(255,255,255,0.14)" : "rgba(60,60,67,0.12)",
      textStyle: {
        color: textColor,
        fontSize: 12,
      },
      formatter: (params: any) => {
        return `${params.name}: ${prefix}¥${formatCents(params.value)} (${params.percent}%)`;
      },
    },
    title: {
      text: centerText,
      subtext: activeTab.value === 0 ? "总支出" : "总收入",
      left: "center",
      top: "40%",
      itemGap: 4,
      textStyle: {
        fontSize: centerFontSize,
        fontWeight: 800,
        color: activeTab.value === 0 ? "#ff3b30" : "#34c759",
      },
      subtextStyle: {
        fontSize: 12,
        color: subTextColor,
        fontWeight: 500,
      },
    },
    series: [
      {
        type: "pie",
        radius: ["46%", "65%"],
        center: ["50%", "50%"],
        data,
        padAngle: 3,
        itemStyle: {
          borderRadius: 8,
          borderColor: isDark ? "#1c1c1e" : "#ffffff",
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: (params: any) =>
            `${params.name} ${Number(params.percent).toFixed(0)}%`,
          fontSize: 11,
          color: textColor,
          overflow: "none",
        },
        labelLine: {
          length: 6,
          length2: 8,
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
  transition: background-color 0.2s var(--spring-default);
}

.nav-left {
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

.nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.month-nav {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.month-capsule {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 8rpx;
  border-radius: var(--radius-md);
  transition: all 0.2s var(--spring-default);
}

.capsule-arrow,
.nav-arrow {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.15s;

  &:active {
    background: var(--bg-hover);
    transform: scale(0.9);
  }
}

.capsule-label-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 12rpx;
  border-radius: var(--radius-sm);
}

.capsule-label,
.month-label {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5rpx;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.nav-right {
  width: 72rpx;
  height: 72rpx;
  flex-shrink: 0;
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

.stats-body {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 4rpx 24rpx 60rpx;
}

.stats-card {
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: 48rpx;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: background-color 0.2s var(--spring-default);
}

.stats-tabs {
  padding: 24rpx 32rpx 16rpx;
}

:deep(.tab-custom.wd-tabs) {
  background: transparent !important;
  --wot-tabs-nav-bg: transparent;
  --wot-tabs-nav-color: var(--text-tertiary);
  --wot-tabs-nav-color-active: var(--text-primary);
  --wot-tabs-nav-font-weight-active: 600;
  --wot-tabs-nav-item-font-size: 26rpx;
  --wot-tabs-nav-line-bg: transparent;

  .wd-tabs__nav {
    box-sizing: border-box !important;
    width: 100% !important;
    background: var(--bg-seg-track) !important;
    border-radius: 18rpx !important;
    padding: 4rpx !important;
  }

  .wd-tabs__nav-item {
    box-sizing: border-box !important;
    flex: 1;
    height: 56rpx !important;
    line-height: 56rpx !important;
    padding: 0 16rpx !important;
    border-radius: 14rpx !important;
    transition: all 0.2s var(--spring-default);

    &.is-active {
      background: var(--bg-seg-thumb);
      color: var(--text-primary) !important;
      font-weight: 600;
      box-shadow: var(--shadow-seg);
    }
  }

  .wd-tabs__line {
    display: none !important;
  }
}

.chart-section {
  padding: 16rpx 0 8rpx;
}

.category-list {
  border-top: 1rpx solid var(--border-subtle);
  padding: 12rpx 36rpx 28rpx;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-top: 1rpx solid var(--border-subtle);

  &:first-child {
    border-top: none;
  }
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.cat-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-icon-badge {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  text {
    font-size: 32rpx;
    line-height: 1;
  }
}

.cat-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.cat-name {
  font-size: 30rpx;
  color: var(--text-primary);
  font-weight: 600;
  letter-spacing: -0.3rpx;
}

.cat-count {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.cat-right {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.cat-amount {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--color-expense);
  font-variant-numeric: tabular-nums;

  &.income-text {
    color: var(--color-income);
  }
}

.cat-percent {
  font-size: 24rpx;
  color: var(--text-tertiary);
  font-weight: 500;
  min-width: 84rpx;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.insights-footer {
  margin-top: 12rpx;
  padding-top: 20rpx;
  border-top: 2rpx dashed var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  .insight-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 24rpx;

    .insight-label {
      color: var(--text-tertiary);
    }

    .insight-value {
      color: var(--text-secondary);
      font-weight: 500;
      font-variant-numeric: tabular-nums;
    }
  }
}

.empty-chart-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64rpx 40rpx 76rpx;
}

.empty-donut-ring {
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  border: 24rpx solid var(--bg-input-filled);
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
    color: var(--text-tertiary);
    font-weight: 500;
  }

  .empty-donut-value {
    font-size: 42rpx;
    font-weight: 800;
    color: var(--text-primary);
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.5rpx;
  }

  .empty-donut-label {
    font-size: 22rpx;
    color: var(--text-tertiary);
    font-weight: 500;
    margin-top: 2rpx;
  }
}

.empty-hint-text {
  font-size: 26rpx;
  color: var(--text-tertiary);
  margin-top: 36rpx;
  font-weight: 500;
}

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  margin-top: 28rpx;
  padding: 18rpx 48rpx;
  background: var(--bg-active);
  border: 1rpx solid rgba(0, 122, 255, 0.2);
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
