<template>
  <view
    class="app-logo"
    :style="{ width: size + 'rpx', height: size + 'rpx' }"
    :class="{ animated }"
  >
    <view class="logo-box">
      <!-- 柔光内边框遮罩 -->
      <view class="logo-gloss" />
      <!-- 三条账本数据横杠 -->
      <view class="logo-line line-1" />
      <view class="logo-line line-2" />
      <view class="logo-line line-3" />
      <!-- 右上角状态绿点 -->
      <view class="logo-dot" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ size?: number; animated?: boolean }>(), {
  size: 152,
  animated: false,
});
</script>

<style lang="scss" scoped>
.app-logo {
  flex-shrink: 0;
  display: inline-block;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    transform: scale(0.96);
  }
}

.logo-box {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 26%;
  background: linear-gradient(180deg, #2e8cff 0%, #007aff 50%, #0062e3 100%);
  box-shadow:
    inset 0 2rpx 4rpx rgba(255, 255, 255, 0.4),
    0 16rpx 36rpx rgba(0, 122, 255, 0.32);
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
  padding-left: 25%;
  overflow: hidden;
}

.logo-gloss {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22) 0%, rgba(255, 255, 255, 0) 100%);
  border-top-left-radius: 26%;
  border-top-right-radius: 26%;
  pointer-events: none;
}

.logo-line {
  height: 8.5%;
  border-radius: 999rpx;
  background: #ffffff;
  margin-bottom: 7%;
  transform-origin: left center;

  &.line-1 {
    width: 44%;
  }

  &.line-2 {
    width: 60%;
    opacity: 0.92;
  }

  &.line-3 {
    width: 34%;
    opacity: 0.75;
    margin-bottom: 0;
  }
}

.logo-dot {
  position: absolute;
  top: 24%;
  right: 18%;
  width: 15%;
  height: 15%;
  border-radius: 50%;
  background: #34c759;
  border: 4rpx solid #007aff;
  box-sizing: border-box;
  box-shadow: 0 0 10rpx rgba(52, 199, 89, 0.6);
}

.animated {
  animation: logo-float 2.4s ease-in-out infinite;

  .logo-line {
    animation: logo-draw 2s ease-in-out infinite;
  }

  .line-1 {
    animation-delay: 0s;
  }
  .line-2 {
    animation-delay: 0.15s;
  }
  .line-3 {
    animation-delay: 0.3s;
  }
}

@keyframes logo-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8rpx);
  }
}

@keyframes logo-draw {
  0%,
  20% {
    opacity: 0.35;
    transform: scaleX(0.65);
  }
  50%,
  100% {
    opacity: 1;
    transform: scaleX(1);
  }
}
</style>
