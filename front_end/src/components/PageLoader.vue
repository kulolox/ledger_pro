<template>
  <view class="page-loader">
    <view class="ios-spinner">
      <view v-for="n in 12" :key="n" class="spinner-blade" />
    </view>
  </view>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.page-loader {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
}

.ios-spinner {
  position: relative;
  width: 48rpx;
  height: 48rpx;
}

.spinner-blade {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 4rpx;
  height: 13rpx;
  margin-left: -2rpx;
  margin-top: -6.5rpx;
  border-radius: 999rpx;
  background: var(--text-tertiary);
  animation: ios-blade-fade 1s linear infinite;

  @for $i from 1 through 12 {
    &:nth-child(#{$i}) {
      transform: rotate(#{($i - 1) * 30}deg) translateY(-16rpx);
      animation-delay: #{-1 + ($i - 1) * 0.0833}s;
    }
  }
}

@keyframes ios-blade-fade {
  0% {
    opacity: 0.85;
  }
  100% {
    opacity: 0.15;
  }
}
</style>
