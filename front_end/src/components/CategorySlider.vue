<template>
  <scroll-view scroll-x class="category-slider" :show-scrollbar="false">
    <view class="category-track">
      <view
        v-for="cat in categories"
        :key="cat.id"
        class="category-item"
        :class="{ active: modelValue === cat.id }"
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
</template>

<script setup lang="ts">
defineProps<{
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
</script>

<style lang="scss" scoped>
.category-slider {
  width: 100%;
  white-space: nowrap;
}

.category-track {
  display: flex;
  gap: 20rpx;
  padding: 12rpx 32rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 6rpx;
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
      transform: scale(1.04);
      box-shadow:
        0 0 0 4rpx var(--bg-secondary),
        0 0 0 8rpx var(--color-primary),
        0 8rpx 20rpx rgba(0, 122, 255, 0.25);
    }

    .cat-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }
}

.cat-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition:
    transform 0.18s var(--spring-default),
    box-shadow 0.18s var(--spring-default);

  text {
    font-size: 40rpx;
    color: #ffffff;
    line-height: 1;
  }
}

.cat-name {
  font-size: 24rpx;
  color: var(--text-tertiary);
  white-space: nowrap;
  font-weight: 500;
  transition: color 0.15s;
}
</style>
