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
}

.category-track {
  display: flex;
  gap: 4rpx;
  padding: 12rpx 24rpx;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 20rpx;
  border-radius: var(--radius-md);
  transition: all 0.2s var(--spring-default);
  flex-shrink: 0;

  &.active {
    background: var(--bg-active);

    .cat-icon {
      transform: scale(1.12);
      box-shadow: var(--shadow-primary-sm);
    }

    .cat-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }
}

.cat-icon {
  width: 68rpx;
  height: 68rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s var(--spring-bounce), box-shadow 0.2s var(--spring-default);

  text {
    font-size: 30rpx;
    color: var(--text-inverse);
    font-weight: 600;
  }
}

.cat-name {
  font-size: 22rpx;
  color: var(--text-secondary);
  white-space: nowrap;
  font-weight: 500;
}
</style>
