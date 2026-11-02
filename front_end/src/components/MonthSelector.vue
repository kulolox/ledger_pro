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

        <view class="picker-header">
          <view class="picker-title-group">
            <text class="picker-title">选择年月</text>
            <text class="picker-subtitle">快速跳转至指定月份账单</text>
          </view>
          <view class="picker-actions">
            <view
              v-if="!isCurrentRealMonth"
              class="today-btn"
              @click="jumpToCurrentMonth"
            >
              <text class="today-btn-text">回到本月</text>
            </view>
            <view class="picker-close-btn" @click="pickerVisible = false">
              <wd-icon name="close" size="28rpx" color="var(--text-secondary)" />
            </view>
          </view>
        </view>

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
              'is-selected': pickerYear === year && m === month,
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
    minYear?: number;
    maxYear?: number;
  }>(),
  {
    minYear: 2018,
    maxYear: () => new Date().getFullYear() + 5,
  },
);

const emit = defineEmits<{
  (e: "update:year", val: number): void;
  (e: "update:month", val: number): void;
  (e: "change", payload: { year: number; month: number }): void;
}>();

const now = new Date();
const todayYear = now.getFullYear();
const todayMonth = now.getMonth() + 1;

const pickerVisible = ref(false);
const pickerYear = ref(props.year);

const monthLabel = computed(
  () => `${props.year}-${String(props.month).padStart(2, "0")}`,
);

const isCurrentRealMonth = computed(
  () => props.year === todayYear && props.month === todayMonth,
);

// 生成围绕当前选择年份的 5 个快捷年份胶囊
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

watch(
  () => props.year,
  (val) => {
    pickerYear.value = val;
  },
);

function applyYearMonth(newYear: number, newMonth: number) {
  if (newYear === props.year && newMonth === props.month) return;
  emit("update:year", newYear);
  emit("update:month", newMonth);
  emit("change", { year: newYear, month: newMonth });
}

function openPicker() {
  pickerYear.value = props.year;
  pickerVisible.value = true;
}

function onPopupClose() {
  pickerVisible.value = false;
}

function changePickerYear(delta: number) {
  const target = pickerYear.value + delta;
  if (target < props.minYear || target > props.maxYear) return;
  pickerYear.value = target;
}

function selectPickerYear(y: number) {
  pickerYear.value = y;
}

function selectMonth(m: number) {
  applyYearMonth(pickerYear.value, m);
  pickerVisible.value = false;
}

function jumpToCurrentMonth() {
  pickerYear.value = todayYear;
  applyYearMonth(todayYear, todayMonth);
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
  margin: 4rpx auto 24rpx;
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

/* 12月份矩阵 - 黄金比例与轻盈排版 */
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

.month-num {
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
</style>
