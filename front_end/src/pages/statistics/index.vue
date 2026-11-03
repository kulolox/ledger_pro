<template>
  <view class="stats-page" :class="['theme-' + themeMode]" :data-theme="themeMode">
    <NavBar floating>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <view class="nav-center">
          <MonthSelector
            v-model:year="currentYear"
            v-model:month="currentMonth"
            v-model:mode="queryMode"
            :allow-year="true"
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
            >{{ totalExpense > 0 ? "-" : "" }}{{ formatCents(totalExpense) }}</text
          >
        </view>
        <view class="summary-item income-card">
          <text class="summary-label">收入</text>
          <text class="summary-value income"
            >{{ totalIncome > 0 ? "+" : "" }}{{ formatCents(totalIncome) }}</text
          >
        </view>
        <view class="summary-item balance-card">
          <text class="summary-label">结余</text>
          <text
            class="summary-value"
            :class="balance > 0 ? 'income' : balance < 0 ? 'expense' : ''"
          >
            {{ balance > 0 ? "+" : balance < 0 ? "-" : "" }}{{ formatCents(Math.abs(balance)) }}
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

          <!-- 有数据时展示：ECharts 饼图（与 H5 风格 100% 保持一致） + 顶部洞察摘要 + 分类聚合列表 -->
          <template v-if="currentCategories.length > 0">
            <view class="chart-section">
              <EChart :options="pieOption" :height="270" />
              <view v-if="hasMinorCategories" class="chart-footnote">
                <wd-icon name="info-circle" size="24rpx" color="var(--text-tertiary)" />
                <text class="footnote-text">占比低于 2% 的分类未在图表中展示，完整数据见下方明细</text>
              </view>
            </view>

            <view class="category-list">
              <view class="insights-header">
                <view class="insight-metric">
                  <text class="insight-label"
                    >{{ queryMode === "year" ? "月均" : "日均" }}{{ activeTab === 0 ? "支出" : "收入" }}</text
                  >
                  <text class="insight-value">¥{{ averageAmount }}</text>
                </view>
                <view class="insight-divider" />
                <view class="insight-metric">
                  <text class="insight-label">{{ queryMode === "year" ? "本年累计" : "本月累计" }}</text>
                  <text class="insight-value">{{ totalTxCount }} 笔</text>
                </view>
              </view>

              <view
                v-for="cat in currentCategories"
                :key="cat.category_id"
                class="category-item"
              >
                <view class="cat-left">
                  <view
                    class="cat-icon-badge"
                    :style="{ background: cat.color }"
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
              >{{ queryMode === "year" ? "本年" : "本月" }}还没有{{ activeTab === 0 ? "支出" : "收入" }}账单数据</text
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
import { onLoad, onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import MonthSelector from "@/components/MonthSelector.vue";
import EChart from "@/components/EChart.vue";
import PageLoader from "@/components/PageLoader.vue";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { pullFromServer } from "@/utils/sync";
import { formatCents } from "@/utils/format";
import { getMonthlyStatistics, getYearlyStatistics, type MonthlyStats } from "@/api/common";

const { transactions, load } = useTransactions();
const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories } =
  useCategories();
const { themeMode } = useTheme();

function getInitialYearMonth(): { year: number; month: number } {
  const now = new Date();
  let year = now.getFullYear();
  let month = now.getMonth() + 1;
  if (typeof window !== "undefined" && window.location?.hash) {
    const qIdx = window.location.hash.indexOf("?");
    if (qIdx !== -1) {
      const params = new URLSearchParams(window.location.hash.slice(qIdx + 1));
      const y = Number(params.get("year"));
      const m = Number(params.get("month"));
      if (!Number.isNaN(y) && y >= 2000) year = y;
      if (!Number.isNaN(m) && m >= 1 && m <= 12) month = m;
    }
  }
  return { year, month };
}

const initialYM = getInitialYearMonth();
const currentYear = ref(initialYM.year);
const currentMonth = ref(initialYM.month);
const queryMode = ref<"month" | "year">("month");
const statsData = ref<MonthlyStats | null>(null);
const activeTab = ref(0);
const pageLoading = ref(true);

onLoad((options) => {
  if (options?.year) {
    const y = Number(options.year);
    if (!Number.isNaN(y) && y >= 2000) {
      currentYear.value = y;
    }
  }
  if (options?.month) {
    const m = Number(options.month);
    if (!Number.isNaN(m) && m >= 1 && m <= 12) {
      currentMonth.value = m;
    }
  }
  if (options?.mode === "year" || options?.mode === "month") {
    queryMode.value = options.mode;
  }
});

const totalExpense = computed(() => Number(statsData.value?.total_expense ?? 0));
const totalIncome = computed(() => Number(statsData.value?.total_income ?? 0));
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
  rawList: Array<{ category_id: string | number; amount: number | string; count: number | string }>,
  type: number,
): CatItem[] {
  const total = rawList.reduce((s, c) => s + Number(c.amount || 0), 0);
  return rawList.map((c) => {
    const amount = Number(c.amount || 0);
    const count = Number(c.count || 0);
    const percentVal = total > 0 ? (amount / total) * 100 : 0;
    let percent = percentVal.toFixed(1);
    if (amount > 0 && percentVal > 0 && percent === "0.0") {
      percent = "<0.1";
    }
    return {
      category_id: String(c.category_id),
      name: getCategoryName(type, String(c.category_id)),
      icon: getCategoryIcon(type, String(c.category_id)),
      amount,
      count,
      percent,
      color: getCategoryColor(type, String(c.category_id)),
    };
  });
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

const averageAmount = computed(() => {
  if (queryMode.value === "year") {
    const avgCents = currentTotal.value / 12;
    return formatCents(avgCents);
  }
  const daysInMonth = new Date(
    currentYear.value,
    currentMonth.value,
    0,
  ).getDate();
  const avgCents = currentTotal.value / (daysInMonth || 30);
  return formatCents(avgCents);
});

// 饼图中低于 2% 的分类不做渲染，避免数据差异过大时碎片扎堆
const renderedPieCategories = computed(() => {
  const total = currentTotal.value;
  if (!total || total <= 0) return currentCategories.value;
  const filtered = currentCategories.value.filter(
    (c) => (c.amount / total) * 100 >= 2,
  );
  return filtered.length > 0 ? filtered : currentCategories.value;
});

const hasMinorCategories = computed(() => {
  const total = currentTotal.value;
  if (!total || total <= 0) return false;
  return currentCategories.value.some((c) => (c.amount / total) * 100 < 2);
});

const pieOption = computed(() => {
  const cats = renderedPieCategories.value;
  const prefix = activeTab.value === 0 ? "-" : "+";
  const totalAmount = currentTotal.value;
  const centerText = `${prefix}¥${formatCents(totalAmount)}`;
  const centerFontSize =
    centerText.length > 10 ? 13 : centerText.length > 8 ? 15 : 17;
  const data = cats.map((c) => ({
    name: c.name,
    value: c.amount,
    percent: c.percent,
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
      subtext:
        queryMode.value === "year"
          ? activeTab.value === 0
            ? "年度支出"
            : "年度收入"
          : activeTab.value === 0
            ? "总支出"
            : "总收入",
      left: "center",
      top: "center",
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
        // 单分类时取消间隙与圆角，避免整环出现缺口
        padAngle: data.length > 1 ? 3 : 0,
        minShowLabelAngle: 5,
        itemStyle: {
          borderRadius: data.length > 1 ? 8 : 0,
          borderColor: isDark ? "#1c1c1e" : "#ffffff",
          borderWidth: data.length > 1 ? 2 : 0,
        },
        label: {
          show: true,
          formatter: (params: any) => {
            if (Number(params.percent) < 1) return "";
            return `${params.name} ${Number(params.percent).toFixed(0)}%`;
          },
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

  if (queryMode.value === "month") {
    const monthStr = getMonthStr();
    await pullFromServer(bookId, monthStr);

    const hasLocalForMonth = transactions.value.some(
      (tx) =>
        !tx.deleted &&
        String(tx.book_id) === String(bookId) &&
        tx.trans_time.startsWith(monthStr),
    );
    if (hasLocalForMonth) {
      calcLocalStats(bookId);
      return;
    }

    try {
      const res = await getMonthlyStatistics({
        book_id: bookId,
        month: monthStr,
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
  } else {
    // 按年查询：优先通过服务端聚合接口获取整年数据
    try {
      const res = await getYearlyStatistics({
        book_id: bookId,
        year: currentYear.value,
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
}

function calcLocalStats(bookId: string) {
  const prefix =
    queryMode.value === "year"
      ? `${currentYear.value}`
      : `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
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

watch([currentYear, currentMonth, queryMode], () => {
  fetchStats();
});

onShow(async () => {
  syncThemeFromStorage();
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  load();
  loadCategories();
  await fetchStats();
  await nextTick();
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

.chart-footnote {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 10rpx;
  margin-bottom: 12rpx;
  padding: 0 24rpx;
}

.footnote-text {
  font-size: 22rpx;
  color: var(--text-tertiary);
  line-height: 1.4;
}

.category-list {
  border-top: 1rpx solid var(--border-subtle);
  padding: 24rpx 32rpx 28rpx;
}

.insights-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-input-filled);
  border-radius: 24rpx;
  padding: 22rpx 32rpx;
  margin-bottom: 12rpx;
}

.insight-metric {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  min-width: 0;

  .insight-label {
    font-size: 24rpx;
    color: var(--text-tertiary);
    font-weight: 500;
  }

  .insight-value {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.3rpx;
  }
}

.insight-divider {
  width: 1rpx;
  height: 52rpx;
  background: var(--border-subtle);
  flex-shrink: 0;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 4rpx;
  border-top: 1rpx solid var(--border-subtle);
}

.insights-header + .category-item {
  border-top: none;
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 22rpx;
  min-width: 0;
  flex: 1;
}

.cat-icon-badge {
  width: 76rpx;
  height: 76rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  text {
    font-size: 34rpx;
    line-height: 1;
  }
}

.cat-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
  flex: 1;
}

.cat-name {
  font-size: 30rpx;
  color: var(--text-primary);
  font-weight: 600;
  letter-spacing: -0.3rpx;
}

.cat-count {
  font-size: 24rpx;
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
  flex-shrink: 0;
  margin-left: 20rpx;
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
  font-size: 22rpx;
  color: var(--text-tertiary);
  font-weight: 500;
  text-align: right;
  font-variant-numeric: tabular-nums;
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
