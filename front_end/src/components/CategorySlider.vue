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
  gap: 8rpx;
  padding: 12rpx 20rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 16rpx;
  border-radius: var(--radius-md);
  transition: background-color 0.18s var(--spring-default), transform 0.18s var(--spring-default);
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;

  &:active {
    transform: scale(0.94);
  }

  &.active {
    background: var(--bg-active);

    .cat-icon {
      transform: scale(1.06);
      box-shadow: 0 4rpx 12rpx rgba(0, 122, 255, 0.25);
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
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.18s var(--spring-default), box-shadow 0.18s var(--spring-default);

  text {
    font-size: 32rpx;
    color: #ffffff;
    font-weight: 600;
  }
}

.cat-name {
  font-size: 24rpx;
  color: var(--text-secondary);
  white-space: nowrap;
  font-weight: 500;
}
</style>
