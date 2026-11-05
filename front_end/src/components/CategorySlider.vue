<template>
  <view class="category-slider-wrap">
    <scroll-view
      scroll-x
      class="category-slider"
      :show-scrollbar="false"
      :lower-threshold="4"
      :upper-threshold="4"
      @scroll="onScroll"
      @scrolltolower="onScrollToLower"
      @scrolltoupper="onScrollToUpper"
    >
      <view class="category-track">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: String(modelValue) === String(cat.id) }"
          @click="$emit('update:modelValue', cat.id)"
        >
          <view
            class="cat-icon"
            :style="{ background: cat.color || 'var(--color-primary)' }"
          >
            <text>{{ cat.icon || cat.name[0] }}</text>
          </view>
          <text class="cat-name">{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 极简横向滑动进度指示条 -->
    <view v-if="isScrollable" class="slider-scrollbar">
      <view
        class="slider-scrollbar-thumb"
        :style="{ transform: `translateX(${scrollProgress * 100}%)` }"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, getCurrentInstance } from "vue";

const props = defineProps<{
  modelValue: string;
  categories: Array<{
    id: string;
    name: string;
    icon?: string;
    color?: string;
  }>;
}>();

defineEmits<{
  "update:modelValue": [id: string];
}>();

const instance = getCurrentInstance();
const scrollProgress = ref(0);
const containerWidth = ref(0);

// 超过 6 个分类时展示底部滑动指示条
const isScrollable = computed(() => (props.categories?.length || 0) > 6);

function measureContainer() {
  if (!instance?.proxy) return;
  uni
    .createSelectorQuery()
    .in(instance.proxy)
    .select(".category-slider")
    .boundingClientRect((rect) => {
      if (rect && !Array.isArray(rect) && rect.width) {
        containerWidth.value = rect.width;
      }
    })
    .exec();
}

onMounted(() => {
  nextTick(measureContainer);
});

watch(
  () => props.categories,
  () => {
    scrollProgress.value = 0;
    nextTick(measureContainer);
  },
);

function getViewportWidth(): number {
  if (containerWidth.value > 0) return containerWidth.value;
  try {
    return uni.getSystemInfoSync().windowWidth || 375;
  } catch {
    return 375;
  }
}

function onScroll(e: any) {
  const detail = e?.detail;
  if (!detail) return;
  const left = detail.scrollLeft || 0;
  const scrollWidth = detail.scrollWidth || 0;

  if (!containerWidth.value) {
    measureContainer();
  }

  const viewWidth = getViewportWidth();
  // 预留 2px 容差消除浏览器亚像素取整导致滑到底差一点点的问题
  const maxScroll = Math.max(scrollWidth - viewWidth - 2, 1);
  const ratio = Math.min(Math.max(left / maxScroll, 0), 1);
  scrollProgress.value = ratio;
}

function onScrollToLower() {
  scrollProgress.value = 1;
}

function onScrollToUpper() {
  scrollProgress.value = 0;
}
</script>

<style lang="scss" scoped>
.category-slider-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.category-slider {
  width: 100%;
  white-space: nowrap;
}

.category-track {
  display: inline-flex;
  gap: 12rpx;
  padding: 8rpx 32rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  width: 96rpx;
  padding: 6rpx 0;
  border-radius: var(--radius-md);
  transition: transform 0.16s var(--spring-default);
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;

  &:active {
    transform: scale(0.94);
  }

  &.active {
    .cat-icon {
      opacity: 1;
      transform: scale(1.05);
      box-shadow:
        0 0 0 3rpx var(--bg-secondary),
        0 0 0 6rpx var(--color-primary);
    }

    .cat-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }
}

.cat-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  opacity: 0.72;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    opacity 0.18s var(--spring-default),
    transform 0.18s var(--spring-default),
    box-shadow 0.18s var(--spring-default);

  text {
    font-size: 32rpx;
    color: #ffffff;
    line-height: 1;
  }
}

.cat-name {
  font-size: 22rpx;
  color: var(--text-tertiary);
  white-space: nowrap;
  font-weight: 400;
  line-height: 1.2;
  transition: color 0.15s;
}

.slider-scrollbar {
  width: 56rpx;
  height: 6rpx;
  border-radius: 999rpx;
  background: var(--bg-input-filled);
  overflow: hidden;
  margin-top: 4rpx;
  margin-bottom: 4rpx;
}

.slider-scrollbar-thumb {
  width: 50%;
  height: 100%;
  border-radius: 999rpx;
  background: var(--color-handle);
  transition: transform 0.08s linear;
}
</style>
