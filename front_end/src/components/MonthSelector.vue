<template>
  <view class="month-selector-root">
    <!-- 导航栏年月触发器（无左右箭头，点击直接弹出年月选择） -->
    <view
      class="month-capsule month-nav"
      :class="{ 'is-open': pickerVisible }"
      @click="openPicker"
    >
      <view class="capsule-label-wrap">
        <text class="capsule-label month-label">{{ monthLabel }}</text>
        <view class="capsule-caret" :class="{ 'is-rotated': pickerVisible }">
          <view class="caret-chevron" />
        </view>
      </view>
    </view>

    <!-- 年月快速选择弹窗 -->
    <wd-popup
      v-model="pickerVisible"
      position="bottom"
      custom-style="border-radius: 48rpx 48rpx 0 0; overflow: hidden; background: transparent;"
      :z-index="1000"
      @close="onPopupClose"
    >
      <view class="month-picker-sheet">
        <view class="sheet-drag-handle" />

        <!-- 模式切换分段器（仅在支持按年查询时展示） -->
        <view v-if="allowYear" class="mode-segmented-bar">
          <view
            class="segmented-pill"
            :class="{ 'is-active': pickerMode === 'month' }"
            @click="setPickerMode('month')"
          >
            <text class="segmented-pill-text">按月查询</text>
          </view>
          <view
            class="segmented-pill"
            :class="{ 'is-active': pickerMode === 'year' }"
            @click="setPickerMode('year')"
          >
            <text class="segmented-pill-text">按年查询</text>
          </view>
        </view>

        <view class="picker-header">
          <view class="picker-title-group">
            <text class="picker-title">{{ pickerMode === 'year' ? '选择年份' : '选择年月' }}</text>
            <text class="picker-subtitle">
              {{ pickerMode === 'year' ? '快速查看年度统计数据' : '快速跳转至指定月份账单' }}
            </text>
          </view>
          <view class="picker-actions">
            <view
              v-if="pickerMode === 'month' && !isCurrentRealMonth"
              class="today-btn"
              @click="jumpToCurrentMonth"
            >
              <text class="today-btn-text">回到本月</text>
            </view>
            <view
              v-else-if="pickerMode === 'year' && !isCurrentRealYear"
              class="today-btn"
              @click="jumpToCurrentYear"
            >
              <text class="today-btn-text">回到今年</text>
            </view>
            <view class="picker-close-btn" @click="pickerVisible = false">
              <wd-icon name="close" size="28rpx" color="var(--text-secondary)" />
            </view>
          </view>
        </view>

        <!-- 模式一：按月查询 -->
        <template v-if="pickerMode === 'month'">
          <!-- 年份切换栏 -->
          <view class="year-switcher">
            <view class="year-stepper-bar">
              <view
                class="year-step-btn"
                :class="{ disabled: pickerYear <= minYear }"
                @click="changePickerYear(-1)"
              >
                <wd-icon name="left" size="28rpx" color="var(--text-primary)" />
              </view>
              <view class="year-display-wrap">
                <text class="year-display">{{ pickerYear }}年</text>
              </view>
              <view
                class="year-step-btn"
                :class="{ disabled: pickerYear >= maxYear }"
                @click="changePickerYear(1)"
              >
                <wd-icon name="right" size="28rpx" color="var(--text-primary)" />
              </view>
            </view>

            <!-- 快捷年份胶囊条 -->
            <view class="year-pills-row">
              <view
                v-for="y in quickYears"
                :key="y"
                class="year-pill"
                :class="{ active: y === pickerYear }"
                @click="selectPickerYear(y)"
              >
                <text class="year-pill-text">{{ y }}</text>
              </view>
            </view>
          </view>

          <!-- 12个月份宫格 -->
          <view class="month-grid">
            <view
              v-for="m in 12"
              :key="m"
              class="month-cell"
              :class="{
                'is-selected': pickerYear === year && m === month && mode === 'month',
                'is-current': pickerYear === todayYear && m === todayMonth,
              }"
              @click="selectMonth(m)"
            >
              <text class="month-num">{{ m }}月</text>
              <text
                v-if="pickerYear === todayYear && m === todayMonth"
                class="month-badge"
              >
                本月
              </text>
            </view>
          </view>
        </template>

        <!-- 模式二：按年查询（去冗余：区间翻页栏 + 年份宫格，点选直接完成） -->
        <template v-else>
          <view class="year-switcher">
            <view class="year-stepper-bar">
              <view
                class="year-step-btn"
                :class="{ disabled: yearGridStart <= minYear }"
                @click="changeYearRange(-9)"
              >
                <wd-icon name="left" size="28rpx" color="var(--text-primary)" />
              </view>
              <view class="year-display-wrap">
                <text class="year-display">{{ yearGridStart }}年 - {{ yearGridEnd }}年</text>
              </view>
              <view
                class="year-step-btn"
                :class="{ disabled: yearGridEnd >= maxYear }"
                @click="changeYearRange(9)"
              >
                <wd-icon name="right" size="28rpx" color="var(--text-primary)" />
              </view>
            </view>
          </view>

          <!-- 9 个年份宫格：直接点击完成选择并关闭 -->
          <view class="year-grid">
            <view
              v-for="y in yearGridList"
              :key="y"
              class="year-cell"
              :class="{
                'is-selected': y === year && mode === 'year',
                'is-current': y === todayYear,
              }"
              @click="selectYear(y)"
            >
              <text class="year-num">{{ y }}年</text>
              <text v-if="y === todayYear" class="month-badge">今年</text>
            </view>
          </view>
        </template>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";

const props = withDefaults(
  defineProps<{
    year: number;
    month: number;
    mode?: "month" | "year";
    allowYear?: boolean;
    minYear?: number;
    maxYear?: number;
  }>(),
  {
    mode: "month",
    allowYear: false,
    minYear: 2000,
    maxYear: 9999,
  },
);

const emit = defineEmits<{
  (e: "update:year", val: number): void;
  (e: "update:month", val: number): void;
  (e: "update:mode", val: "month" | "year"): void;
  (e: "change", payload: { year: number; month: number; mode: "month" | "year" }): void;
}>();

const now = new Date();
const todayYear = now.getFullYear();
const todayMonth = now.getMonth() + 1;

const pickerVisible = ref(false);
const pickerYear = ref(props.year);
const gridCenterYear = ref(props.year);
const pickerMode = ref<"month" | "year">(props.mode);

const monthLabel = computed(() => {
  if (props.mode === "year") {
    return `${props.year}年`;
  }
  return `${props.year}-${String(props.month).padStart(2, "0")}`;
});

const isCurrentRealMonth = computed(
  () => props.year === todayYear && props.month === todayMonth,
);

const isCurrentRealYear = computed(() => props.year === todayYear);

// 生成围绕当前选择年份的 5 个快捷年份胶囊（按月模式下使用）
const quickYears = computed(() => {
  const center = Math.min(
    Math.max(pickerYear.value, props.minYear + 2),
    props.maxYear - 2,
  );
  const list: number[] = [];
  for (let y = center - 2; y <= center + 2; y++) {
    if (y >= props.minYear && y <= props.maxYear) {
      list.push(y);
    }
  }
  return list;
});

// 生成按年模式下的年份网格（9 宫格，3 行 x 3 列）
const yearGridList = computed(() => {
  const center = Math.min(
    Math.max(gridCenterYear.value, props.minYear + 4),
    props.maxYear - 4,
  );
  const list: number[] = [];
  for (let y = center - 4; y <= center + 4; y++) {
    if (y >= props.minYear && y <= props.maxYear) {
      list.push(y);
    }
  }
  return list;
});

const yearGridStart = computed(() => yearGridList.value[0] ?? props.minYear);
const yearGridEnd = computed(
  () => yearGridList.value[yearGridList.value.length - 1] ?? props.maxYear,
);

watch(
  () => props.year,
  (val) => {
    pickerYear.value = val;
    gridCenterYear.value = val;
  },
);

watch(
  () => props.mode,
  (val) => {
    pickerMode.value = val;
  },
);

function applyYearMonth(newYear: number, newMonth: number) {
  emit("update:year", newYear);
  emit("update:month", newMonth);
  emit("update:mode", "month");
  emit("change", { year: newYear, month: newMonth, mode: "month" });
}

function applyYear(newYear: number) {
  emit("update:year", newYear);
  emit("update:mode", "year");
  emit("change", { year: newYear, month: props.month, mode: "year" });
}

function openPicker() {
  pickerYear.value = props.year;
  gridCenterYear.value = props.year;
  pickerMode.value = props.mode;
  pickerVisible.value = true;
}

function onPopupClose() {
  pickerVisible.value = false;
}

function setPickerMode(mode: "month" | "year") {
  pickerMode.value = mode;
}

function changePickerYear(delta: number) {
  const target = pickerYear.value + delta;
  if (target < props.minYear || target > props.maxYear) return;
  pickerYear.value = target;
}

function changeYearRange(delta: number) {
  const target = gridCenterYear.value + delta;
  if (target < props.minYear - 4 || target > props.maxYear + 4) return;
  gridCenterYear.value = target;
}

function selectPickerYear(y: number) {
  pickerYear.value = y;
}

function selectMonth(m: number) {
  applyYearMonth(pickerYear.value, m);
  pickerVisible.value = false;
}

function selectYear(y: number) {
  gridCenterYear.value = y;
  applyYear(y);
  pickerVisible.value = false;
}

function jumpToCurrentMonth() {
  pickerYear.value = todayYear;
  applyYearMonth(todayYear, todayMonth);
  pickerVisible.value = false;
}

function jumpToCurrentYear() {
  gridCenterYear.value = todayYear;
  applyYear(todayYear);
  pickerVisible.value = false;
}
</script>

<style lang="scss" scoped>
.month-selector-root {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.month-capsule {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
}

.capsule-label-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 10rpx 24rpx 10rpx 28rpx;
  border-radius: var(--radius-full);
  background: var(--bg-input-filled);
  transition:
    transform 0.18s var(--spring-default),
    background-color 0.18s ease;

  &:active {
    transform: scale(0.95);
    background: var(--bg-tertiary);
  }
}

.month-capsule.is-open .capsule-label-wrap {
  background: var(--color-primary-subtle);

  .capsule-label {
    color: var(--color-primary);
  }

  .caret-chevron {
    border-color: var(--color-primary);
  }
}

.capsule-label,
.month-label {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5rpx;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  line-height: 1.2;
  transition: color 0.18s ease;
}

.capsule-caret {
  width: 24rpx;
  height: 24rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.24s cubic-bezier(0.22, 1, 0.36, 1);

  .caret-chevron {
    width: 11rpx;
    height: 11rpx;
    border-right: 3rpx solid var(--text-secondary);
    border-bottom: 3rpx solid var(--text-secondary);
    transform: translateY(-2rpx) rotate(45deg);
    transition: border-color 0.2s ease;
  }

  &.is-rotated {
    transform: rotate(180deg);
  }
}

/* 底部年月选择面板（重置父级导航栏字体继承） */
.month-picker-sheet {
  background: var(--bg-secondary);
  color: var(--text-primary);
  text-align: left;
  font-weight: 400;
  font-size: 28rpx;
  letter-spacing: normal;
  padding: 16rpx 36rpx calc(44rpx + env(safe-area-inset-bottom, 0px));
  border-radius: 48rpx 48rpx 0 0;
  box-shadow: var(--shadow-lg);
  transition:
    background-color 0.2s var(--spring-default),
    color 0.2s var(--spring-default);
}

.sheet-drag-handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: var(--color-handle);
  margin: 4rpx auto 20rpx;
}

/* 模式切换分段胶囊 */
.mode-segmented-bar {
  display: flex;
  background: var(--bg-input-filled);
  border-radius: var(--radius-full);
  padding: 6rpx;
  margin-bottom: 24rpx;
  gap: 8rpx;
}

.segmented-pill {
  flex: 1;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.2s var(--spring-default);

  &.is-active {
    background: var(--bg-secondary);
    box-shadow: var(--shadow-sm);

    .segmented-pill-text {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
}

.segmented-pill-text {
  font-size: 26rpx;
  color: var(--text-secondary);
  font-weight: 500;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}

.picker-title-group {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.picker-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5rpx;
}

.picker-subtitle {
  font-size: 24rpx;
  color: var(--text-tertiary);
}

.picker-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.today-btn {
  height: 56rpx;
  padding: 0 24rpx;
  border-radius: var(--radius-full);
  background: var(--color-primary-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    opacity 0.15s;

  &:active {
    transform: scale(0.94);
    opacity: 0.85;
  }
}

.today-btn-text {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--color-primary);
  line-height: 1;
}

.picker-close-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: var(--bg-input-filled);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.15s;

  &:active {
    transform: scale(0.9);
    background: var(--bg-tertiary);
  }
}

/* 年份切换区 */
.year-switcher {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-bottom: 28rpx;
}

.year-stepper-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-input-filled);
  border-radius: var(--radius-md);
  padding: 10rpx 14rpx;
}

.year-step-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition:
    transform 0.15s var(--spring-default),
    opacity 0.15s;

  &:active {
    transform: scale(0.92);
  }

  &.disabled {
    opacity: 0.35;
    pointer-events: none;
  }
}

.year-display-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.year-display {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.year-pills-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12rpx;
}

.year-pill {
  height: 60rpx;
  border-radius: var(--radius-full);
  background: var(--bg-input-filled);
  border: 2rpx solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.18s var(--spring-default);

  &:active {
    transform: scale(0.94);
  }

  &.active {
    background: var(--color-primary-subtle);
    border-color: var(--color-primary);

    .year-pill-text {
      color: var(--color-primary);
      font-weight: 700;
    }
  }
}

.year-pill-text {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

/* 12月份矩阵 */
.month-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.month-cell {
  height: 70rpx;
  border-radius: 16rpx;
  background: var(--bg-input-filled);
  border: 1.5rpx solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;

  &:active {
    transform: scale(0.94);
  }

  &.is-current:not(.is-selected) {
    border-color: var(--color-primary);
    background: var(--color-primary-subtle);

    .month-num {
      color: var(--color-primary);
      font-weight: 600;
    }

    .month-badge {
      color: var(--color-primary);
      background: rgba(0, 122, 255, 0.16);
    }
  }

  &.is-selected {
    background: var(--color-primary);
    border-color: var(--color-primary);
    box-shadow: 0 4rpx 14rpx rgba(0, 122, 255, 0.32);

    .month-num {
      color: #ffffff;
      font-weight: 700;
    }

    .month-badge {
      color: #ffffff;
      background: rgba(255, 255, 255, 0.28);
    }
  }
}

.month-num,
.year-num {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.month-badge {
  font-size: 16rpx;
  font-weight: 600;
  color: var(--color-primary);
  background: var(--color-primary-subtle);
  padding: 2rpx 6rpx;
  border-radius: 999rpx;
  line-height: 1.2;
}

/* 年份网格（3列 x 3行） */
.year-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.year-cell {
  height: 76rpx;
  border-radius: 16rpx;
  background: var(--bg-input-filled);
  border: 1.5rpx solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;

  &:active {
    transform: scale(0.94);
  }

  &.is-current:not(.is-selected) {
    border-color: var(--color-primary);
    background: var(--color-primary-subtle);

    .year-num {
      color: var(--color-primary);
      font-weight: 600;
    }

    .month-badge {
      color: var(--color-primary);
      background: rgba(0, 122, 255, 0.16);
    }
  }

  &.is-selected {
    background: var(--color-primary);
    border-color: var(--color-primary);
    box-shadow: 0 4rpx 14rpx rgba(0, 122, 255, 0.32);

    .year-num {
      color: #ffffff;
      font-weight: 700;
    }

    .month-badge {
      color: #ffffff;
      background: rgba(255, 255, 255, 0.28);
    }
  }
}
</style>
