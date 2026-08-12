<template>
  <view class="search-page">
    <NavBar>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon name="left" size="24px" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">搜索</text>
      </template>
      <template #right>
        <view class="nav-right" />
      </template>
    </NavBar>

    <view class="filter-section">
      <view class="filter-search">
        <view class="search-row">
          <view class="search-input-wrap">
            <wd-icon name="search-line" size="28rpx" color="var(--text-muted)" />
            <input
              class="search-input"
              v-model="keyword"
              placeholder="搜索备注..."
              confirm-type="search"
              @confirm="doSearch"
            />
            <text v-if="keyword" class="search-clear" @click="keyword = ''">✕</text>
          </view>
        </view>
      </view>

      <view class="filter-cells">
        <view class="filter-cell" @click="showTypePicker = true">
          <text class="cell-title">类型</text>
          <view class="cell-value">
            <text>{{ typeLabel }}</text>
            <wd-icon name="right" size="24rpx" color="var(--text-muted)" />
          </view>
        </view>
        <view class="filter-cell" @click="showDateRangePopup = true">
          <text class="cell-title">时间</text>
          <view class="cell-value">
            <text>{{ dateLabel }}</text>
            <wd-icon name="right" size="24rpx" color="var(--text-muted)" />
          </view>
        </view>
        <view class="filter-cell" @click="showCategoryPicker = true">
          <text class="cell-title">分类</text>
          <view class="cell-value">
            <text>{{ categoryLabel }}</text>
            <wd-icon name="right" size="24rpx" color="var(--text-muted)" />
          </view>
        </view>
        <view class="filter-cell" @click="showBookPicker = true">
          <text class="cell-title">账本</text>
          <view class="cell-value">
            <text>{{ bookLabel }}</text>
            <wd-icon name="right" size="24rpx" color="var(--text-muted)" />
          </view>
        </view>
      </view>

      <view class="filter-actions">
        <view class="action-btn search-action" @click="doSearch">
          <text>搜索</text>
        </view>
        <view class="action-btn reset-action" @click="resetFilters">
          <text>重置</text>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="results-body"
      @scrolltolower="loadMore"
    >
      <view class="results-inner">
      <view v-if="loading && results.length === 0" class="loading-state">
        <wd-icon name="loading" size="72rpx" color="var(--text-muted)" />
        <text class="loading-text">搜索中...</text>
      </view>

      <view v-else-if="results.length === 0 && hasSearched" class="empty-state">
        <wd-icon name="search-line" size="72rpx" color="var(--text-muted)" />
        <text class="empty-title">未找到匹配的交易</text>
        <text class="empty-hint">试试调整筛选条件</text>
      </view>

      <view v-else-if="results.length === 0 && !hasSearched" class="empty-state">
        <wd-icon name="search-line" size="72rpx" color="var(--text-muted)" />
        <text class="empty-title">输入关键字或选择筛选条件</text>
        <text class="empty-hint">点击搜索开始查找</text>
      </view>

      <view v-for="day in dayGroups" :key="day.date" class="day-group">
        <view class="day-header">
          <view class="day-info">
            <text class="day-date">{{ formatDayHeader(day.date) }}</text>
            <text class="day-weekday">{{ formatWeekday(day.date) }}</text>
          </view>
          <view class="day-totals">
            <text v-if="day.expense > 0" class="day-expense">-{{ formatCents(day.expense) }}</text>
            <text v-if="day.income > 0" class="day-income">+{{ formatCents(day.income) }}</text>
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
        >
          <view class="tx-left">
            <view
              class="tx-icon"
              :style="{ background: getCategoryColor(tx.type, tx.category_id) }"
            >
              <text>{{ getCategoryIcon(tx.type, tx.category_id) }}</text>
            </view>
            <view class="tx-info">
              <text class="tx-category">{{ getCategoryName(tx.type, tx.category_id) }}</text>
              <text v-if="tx.remark" class="tx-remark">{{ tx.remark }}</text>
            </view>
          </view>
          <view class="tx-right">
            <text class="tx-amount" :class="tx.type === 1 ? 'income' : 'expense'">
              {{ tx.type === 1 ? '+' : '-' }}{{ formatCents(tx.amount) }}
            </text>
            <text v-if="tx.sync_status === 'pending'" class="tx-sync-badge tx-sync-pending">待同步</text>
            <text v-else-if="tx.sync_status === 'failed'" class="tx-sync-badge tx-sync-failed">同步失败</text>
          </view>
        </view>
      </view>

      <view v-if="hasMore" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view class="list-bottom" />
      </view>
    </scroll-view>

    <wd-popup :model-value="showDateRangePopup" @update:model-value="showDateRangePopup = $event" position="bottom">
      <view class="picker-content">
        <view class="picker-header">
          <text class="picker-title">选择时间范围</text>
          <text class="picker-close" @click="showDateRangePopup = false">完成</text>
        </view>
        <view class="date-range-content">
          <view class="date-range-row-inline">
            <view class="date-range-btn date-btn-start" @click="showDateStartPicker = true">
              <text>{{ dateFrom || '开始' }}</text>
            </view>
            <text class="date-sep">至</text>
            <view class="date-range-btn date-btn-end" @click="showDateEndPicker = true">
              <text>{{ dateTo || '结束' }}</text>
            </view>
          </view>
          <view class="date-quick-actions">
            <view class="quick-btn" @click="quickDate(7)">近7天</view>
            <view class="quick-btn" @click="quickDate(30)">近30天</view>
            <view class="quick-btn" @click="quickDate(90)">近3月</view>
            <view class="quick-btn" @click="quickDate(365)">近1年</view>
          </view>
        </view>
      </view>
    </wd-popup>

    <wd-datetime-picker
      v-model="dateRangeStartTs"
      v-model:visible="showDateStartPicker"
      :max-date="maxStartDate"
      type="date"
      @confirm="onDateStartConfirm"
    />
    <wd-datetime-picker
      v-model="dateRangeEndTs"
      v-model:visible="showDateEndPicker"
      :min-date="minEndDate"
      type="date"
      @confirm="onDateEndConfirm"
    />

    <wd-picker
      v-model="typePickerValue"
      v-model:visible="showTypePicker"
      :columns="typeColumns"
      title="选择类型"
      @confirm="onTypeConfirm"
    />

    <wd-picker
      v-model="bookPickerValue"
      v-model:visible="showBookPicker"
      :columns="bookColumns"
      title="选择账本"
      @confirm="onBookConfirm"
    />

    <wd-picker
      v-model="categoryPickerValue"
      v-model:visible="showCategoryPicker"
      :columns="categoryColumns"
      title="选择分类"
      @confirm="onCategoryConfirm"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import { useCategories } from "@/stores/categoryStore";
import { useTransactions } from "@/stores/transactionStore";
import { isLoggedIn } from "@/utils/storage";
import { formatCents, formatWeekday } from "@/utils/format";
import { searchTransactions, getAccountBooks, type SearchParams, type AccountBook } from "@/api/common";

const { getCategoryName, getCategoryColor, getCategoryIcon, loadCategories, categories } = useCategories();
const { transactions } = useTransactions();

const keyword = ref("");
const results = ref<any[]>([]);
const total = ref(0);
const page = ref(1);
const loading = ref(false);
const hasMore = ref(false);
const hasSearched = ref(false);
const PAGE_SIZE = 50;

// Date range
const dateRangeStartTs = ref(0);
const dateRangeEndTs = ref(0);
const showDateStartPicker = ref(false);
const showDateEndPicker = ref(false);
const showDateRangePopup = ref(false);
const dateFrom = ref("");
const dateTo = ref("");

const showCategoryPicker = ref(false);
const showBookPicker = ref(false);
const books = ref<AccountBook[]>([]);

const categoryLabel = computed(() => {
  if (selectedCategoryId.value === undefined) return "全部分类";
  const name = getCategoryName(0, selectedCategoryId.value) || getCategoryName(1, selectedCategoryId.value);
  return name || "全部分类";
});

const bookLabel = computed(() => {
  if (selectedBookId.value === undefined) return "全部账本";
  const book = books.value.find(b => b.id === selectedBookId.value);
  return book?.name || "全部账本";
});

const dateLabel = computed(() => {
  if (!dateFrom.value && !dateTo.value) return "全部时间";
  if (dateFrom.value && dateTo.value) return `${dateFrom.value} 至 ${dateTo.value}`;
  if (dateFrom.value) return `${dateFrom.value} 起`;
  return `至 ${dateTo.value}`;
});

function resetFilters() {
  keyword.value = "";
  selectedType.value = undefined;
  selectedCategoryId.value = undefined;
  selectedBookId.value = undefined;
  initDates();
}

function initDates() {
  const now = new Date();
  const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
  dateRangeStartTs.value = thirtyDaysAgo.getTime();
  dateRangeEndTs.value = now.getTime();
  dateFrom.value = formatDateStr(thirtyDaysAgo);
  dateTo.value = formatDateStr(now);
}

function formatDateStr(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

const maxStartDate = computed(() => {
  if (dateTo.value) return new Date(dateTo.value).getTime();
  return Date.now();
});

const minEndDate = computed(() => {
  if (dateFrom.value) return new Date(dateFrom.value).getTime();
  return 0;
});

function onDateStartConfirm() {
  dateFrom.value = formatDateStr(new Date(dateRangeStartTs.value));
  if (dateTo.value && dateTo.value < dateFrom.value) {
    dateTo.value = dateFrom.value;
    dateRangeEndTs.value = dateRangeStartTs.value;
  }
}

function onDateEndConfirm() {
  dateTo.value = formatDateStr(new Date(dateRangeEndTs.value));
  if (dateFrom.value && dateTo.value < dateFrom.value) {
    dateFrom.value = dateTo.value;
    dateRangeStartTs.value = dateRangeEndTs.value;
  }
}

function quickDate(days: number) {
  const now = new Date();
  const start = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
  dateRangeStartTs.value = start.getTime();
  dateRangeEndTs.value = now.getTime();
  dateFrom.value = formatDateStr(start);
  dateTo.value = formatDateStr(now);
  showDateRangePopup.value = false;
}

// wd-picker for type
const showTypePicker = ref(false);
const typePickerValue = ref<number[]>([]);
const typeColumns = [
  { label: "全部", value: 0 },
  { label: "支出", value: 1 },
  { label: "收入", value: 2 },
];
const selectedType = ref<number | undefined>(undefined);
const typeLabel = computed(() => {
  if (selectedType.value === undefined) return "全部";
  return selectedType.value === 0 ? "支出" : "收入";
});

function onTypeConfirm({ value }: { value: number[] }) {
  const v = value[0];
  selectedType.value = v === 0 ? undefined : v - 1;
}

// wd-picker for books
const bookPickerValue = ref<number[]>([]);
const bookColumns = computed(() => {
  const all = [{ label: "全部账本", value: 0 }];
  const list = books.value.map(b => ({ label: b.name, value: b.id }));
  return [...all, ...list];
});
const selectedBookId = ref<string | undefined>(undefined);

function onBookConfirm({ value }: { value: number[] }) {
  const v = value[0];
  selectedBookId.value = v === 0 ? undefined : String(v);
}

// wd-picker for categories
const categoryPickerValue = ref<number[]>([]);
const categoryColumns = computed(() => {
  const all = [{ label: "全部分类", value: 0 }];
  const cats = (categories.value[0] || []).concat(categories.value[1] || []).filter((c: any) => c.visible !== 0);
  const list = cats.map((c: any) => ({ label: c.name, value: c.id }));
  return [...all, ...list];
});
const selectedCategoryId = ref<string | undefined>(undefined);

function onCategoryConfirm({ value }: { value: number[] }) {
  const v = value[0];
  selectedCategoryId.value = v === 0 ? undefined : String(v);
}

async function doSearch() {
  hasSearched.value = true;
  page.value = 1;
  results.value = [];
  await fetchResults();
}

async function fetchResults() {
  loading.value = true;
  try {
    const params: SearchParams = {
      page: page.value,
      size: PAGE_SIZE,
    };
    if (keyword.value.trim()) params.keyword = keyword.value.trim();
    if (selectedType.value !== undefined) params.type = selectedType.value;
    if (selectedCategoryId.value !== undefined) params.category_id = selectedCategoryId.value;
    if (selectedBookId.value !== undefined) params.book_id = selectedBookId.value;
    if (dateFrom.value) params.date_from = dateFrom.value;
    if (dateTo.value) params.date_to = dateTo.value;

    const res = await searchTransactions(params);
    const list = res.list || [];
    results.value = page.value === 1 ? list : [...results.value, ...list];
    total.value = res.total;
    hasMore.value = results.value.length < total.value;
  } catch {
    if (page.value === 1) {
      fallbackLocalSearch();
    }
  } finally {
    loading.value = false;
  }
}

function fallbackLocalSearch() {
  const kw = keyword.value.trim().toLowerCase();
  const filtered = transactions.value.filter(tx => {
    if (tx.deleted) return false;
    if (kw && !tx.remark?.toLowerCase().includes(kw)) return false;
    if (selectedType.value !== undefined && tx.type !== selectedType.value) return false;
    if (selectedCategoryId.value !== undefined && tx.category_id !== selectedCategoryId.value) return false;
    if (selectedBookId.value !== undefined && tx.book_id !== selectedBookId.value) return false;
    if (dateFrom.value && tx.trans_time.substring(0, 10) < dateFrom.value) return false;
    if (dateTo.value && tx.trans_time.substring(0, 10) > dateTo.value) return false;
    return true;
  });

  filtered.sort((a, b) => b.trans_time.localeCompare(a.trans_time));
  results.value = filtered;
  total.value = filtered.length;
  hasMore.value = false;
}

function loadMore() {
  if (!hasMore.value || loading.value) return;
  page.value++;
  fetchResults();
}

const dayGroups = computed(() => {
  const map = new Map<string, { date: string; income: number; expense: number; items: any[] }>();
  for (const tx of results.value) {
    const dateKey = tx.trans_time.substring(0, 10);
    if (!map.has(dateKey)) {
      map.set(dateKey, { date: dateKey, income: 0, expense: 0, items: [] });
    }
    const group = map.get(dateKey)!;
    group.items.push(tx);
    if (tx.type === 1) group.income += tx.amount;
    else group.expense += tx.amount;
  }
  return Array.from(map.values());
});

function formatDayHeader(dateStr: string): string {
  return dateStr;
}

function goBack() {
  uni.navigateBack();
}

onShow(async () => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  loadCategories();
  try {
    books.value = await getAccountBooks();
  } catch {}
  initDates();
});
</script>

<style lang="scss" scoped>
.search-page {
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

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.nav-right {
  width: 64rpx;
  flex-shrink: 0;
}

.filter-section {
  margin: 16rpx 24rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;

  .filter-search {
    padding: 16rpx 20rpx;

    .search-input-wrap {
      display: flex;
      align-items: center;
      gap: 12rpx;
      padding: 0 20rpx;
      height: 72rpx;
      background: var(--bg-primary);
      border-radius: var(--radius-lg);

      .search-input {
        flex: 1;
        font-size: 28rpx;
        color: var(--text-primary);
        background: transparent;
        border: none;
        outline: none;
        height: 100%;
      }

      .search-clear {
        font-size: 28rpx;
        color: var(--text-muted);
        padding: 8rpx;
      }
    }
  }

  .filter-cells {
    .filter-cell {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 22rpx 20rpx;
      border-top: 1rpx solid var(--border-subtle);

      &:active {
        background: var(--bg-overlay);
      }

      .cell-title {
        font-size: 28rpx;
        color: var(--text-secondary);
        flex-shrink: 0;
      }

      .cell-value {
        display: flex;
        align-items: center;
        gap: 8rpx;

        text {
          font-size: 28rpx;
          color: var(--text-primary);
          font-weight: 500;
        }
      }
    }
  }

  .filter-actions {
    display: flex;
    gap: 12rpx;
    padding: 16rpx 20rpx 20rpx;
    border-top: 1rpx solid var(--border-subtle);

    .action-btn {
      flex: 1;
      text-align: center;
      padding: 22rpx 0;
      border-radius: var(--radius-md);
      font-size: 28rpx;
      font-weight: 600;

      &:active {
        opacity: 0.7;
      }
    }

    .search-action {
      background: var(--color-primary);
      color: var(--text-inverse);
    }

    .reset-action {
      background: var(--bg-primary);
      color: var(--text-secondary);
    }
  }
}

.results-body {
  flex: 1;
  min-height: 0;
}

.results-inner {
  padding: 16rpx 20rpx 60rpx;
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

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;

  .loading-text {
    font-size: 28rpx;
    color: var(--text-muted);
    margin-top: 20rpx;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;

  .empty-title {
    font-size: 30rpx;
    color: var(--text-muted);
    margin-top: 24rpx;
  }

  .empty-hint {
    font-size: 26rpx;
    color: var(--text-dim);
    margin-top: 12rpx;
  }
}

.loading-more {
  text-align: center;
  padding: 32rpx 0;

  text {
    font-size: 24rpx;
    color: var(--text-muted);
  }
}

.list-bottom {
  height: 40rpx;
}

// Picker styles
.date-range-content {
  padding: 24rpx 32rpx;

  .date-range-row-inline {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .date-sep {
      font-size: 26rpx;
      color: var(--text-muted);
      flex-shrink: 0;
    }

    .date-range-btn {
      flex: 1;
      text-align: center;
      padding: 20rpx 0;
      background: var(--bg-secondary);
      border-radius: var(--radius-md);
      font-size: 28rpx;
      color: var(--text-primary);
      font-weight: 500;

      &:active {
        opacity: 0.7;
      }
    }
  }

  .date-quick-actions {
    display: flex;
    gap: 16rpx;
    padding: 20rpx 0 8rpx;

    .quick-btn {
      flex: 1;
      text-align: center;
      padding: 16rpx 0;
      background: var(--bg-secondary);
      border-radius: var(--radius-md);
      font-size: 24rpx;
      color: var(--color-primary);
      font-weight: 500;

      &:active {
        opacity: 0.7;
      }
    }
  }
}

.picker-content {
  background: var(--bg-primary);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 16rpx;
  border-bottom: 1rpx solid var(--border-subtle);

  .picker-title {
    font-size: 32rpx;
    font-weight: 700;
    color: var(--text-primary);
  }

  .picker-close {
    font-size: 28rpx;
    color: var(--color-primary);
    font-weight: 600;
  }
}
</style>
