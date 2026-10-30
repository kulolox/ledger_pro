<template>
  <view class="nav-header">
    <view
      class="nav-bar"
      :style="{ gridTemplateColumns: `72rpx 1fr ${rightWidth}` }"
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
  }>(),
  {
    rightWidth: "72rpx",
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
  transition: background-color 0.2s var(--spring-default), border-color 0.2s var(--spring-default);

  .nav-bar {
    display: grid;
    align-items: center;
    padding: calc(var(--status-bar-height, 44px) + 14rpx) 32rpx 18rpx;
    box-sizing: border-box;

    .nav-slot {
      display: flex;
      align-items: center;

      &-left {
        width: 72rpx;
      }

      &-center {
        justify-content: center;
        text-align: center;
        font-weight: 600;
        font-size: 32rpx;
        color: var(--text-primary);
        letter-spacing: -0.5rpx;
      }

      &-right {
        justify-content: flex-end;
      }
    }
  }
}
</style>
