<template>
  <view class="nav-header" :class="{ 'nav-floating': floating }">
    <view
      class="nav-bar"
      :style="{ gridTemplateColumns: `${rightWidth === 'auto' ? '72rpx' : rightWidth} 1fr ${rightWidth}` }"
    >
      <view class="nav-slot nav-slot-left">
        <slot name="left" />
      </view>
      <view class="nav-slot nav-slot-center">
        <slot name="center" />
      </view>
      <view class="nav-slot nav-slot-right">
        <slot name="right" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    rightWidth?: string;
    floating?: boolean;
  }>(),
  {
    rightWidth: "72rpx",
    floating: false,
  },
);
</script>

<style lang="scss" scoped>
.nav-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: var(--bg-secondary);
  border-bottom: 1rpx solid var(--border-subtle);
  transition:
    background-color 0.2s var(--spring-default),
    border-color 0.2s var(--spring-default);

  .nav-bar {
    display: grid;
    align-items: center;
    padding: calc(var(--status-bar-height, 44px) + 12rpx) 28rpx 16rpx;
    box-sizing: border-box;

    .nav-slot {
      display: flex;
      align-items: center;

      &-left {
        min-width: 72rpx;
      }

      &-center {
        justify-content: center;
        text-align: center;
        font-weight: 700;
        font-size: 34rpx;
        color: var(--text-primary);
        letter-spacing: -0.5rpx;
      }

      &-right {
        justify-content: flex-end;
      }
    }
  }

  &.nav-floating {
    background: var(--bg-primary);
    border-bottom: none;
    padding: calc(var(--status-bar-height, 44px) + 8rpx) 24rpx 8rpx;

    .nav-bar {
      background: var(--bg-secondary);
      border: 1rpx solid var(--border-subtle);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-sm);
      padding: 12rpx 20rpx;
    }
  }
}
</style>
