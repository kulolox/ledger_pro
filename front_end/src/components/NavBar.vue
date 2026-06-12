<template>
  <view class="nav-header">
    <view class="nav-bar" :style="{ gridTemplateColumns: `64rpx 1fr ${rightWidth}` }">
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
withDefaults(defineProps<{
  rightWidth?: string;
}>(), {
  rightWidth: '64rpx',
});
</script>

<style lang="scss" scoped>
.nav-header {
  background: var(--bg-secondary);
  border-bottom: 1rpx solid var(--border-primary);
}

.nav-bar {
  display: grid;
  align-items: center;
  padding: calc(var(--status-bar-height, 44px) + 16rpx) 32rpx 20rpx;
}

.nav-slot {
  display: flex;
  align-items: center;
}

.nav-slot-left {
  width: 64rpx;
}

.nav-slot-center {
  justify-content: center;
}

.nav-slot-right {
  justify-content: flex-end;
}
</style>
