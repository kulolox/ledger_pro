<template>
  <view class="page-loader">
    <AppLogo :size="160" animated />
    <text class="loader-label">LedgerPro</text>
  </view>
</template>

<script setup lang="ts">
import AppLogo from "./AppLogo.vue";
</script>

<style lang="scss" scoped>
.page-loader {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 40rpx;
  background: var(--bg-primary);
}

.loader-label {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-secondary);
  letter-spacing: 4rpx;
}
</style>
