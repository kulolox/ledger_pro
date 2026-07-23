<template>
  <view class="app-logo" :style="{ width: size + 'rpx', height: size + 'rpx' }" :class="{ animated }">
    <svg viewBox="0 0 120 120" class="logo-svg">
      <rect x="12" y="12" width="96" height="96" rx="24" style="fill:var(--color-primary)" />
      <rect class="logo-line" x="32" y="52" width="56" height="6" rx="3" fill="#fff" />
      <rect class="logo-line" x="32" y="64" width="40" height="6" rx="3" fill="#fff" opacity="0.7" />
      <rect class="logo-line" x="32" y="76" width="28" height="6" rx="3" fill="#fff" opacity="0.4" />
      <circle cx="82" cy="48" r="9" fill="#34c759" stroke="#fff" stroke-width="3" />
    </svg>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ size?: number; animated?: boolean }>(), {
  size: 160,
  animated: false,
});
</script>

<style lang="scss" scoped>
.app-logo {
  flex-shrink: 0;

  .logo-svg {
    width: 100%;
    height: 100%;
    display: block;
  }
}

.animated {
  animation: logo-float 2.4s ease-in-out infinite;

  .logo-line {
    animation: logo-draw 2s ease-in-out infinite;
  }

  .logo-line:nth-child(2) { animation-delay: 0s; }
  .logo-line:nth-child(3) { animation-delay: 0.15s; }
  .logo-line:nth-child(4) { animation-delay: 0.3s; }
}

@keyframes logo-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8rpx); }
}

@keyframes logo-draw {
  0%, 20% { opacity: 0.3; transform: scaleX(0.6); transform-origin: left center; }
  50%, 100% { opacity: 1; transform: scaleX(1); transform-origin: left center; }
}
</style>
