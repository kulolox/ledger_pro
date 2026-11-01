<template>
  <view
    class="app-logo"
    :style="{ width: size + 'rpx', height: size + 'rpx' }"
    :class="{ animated }"
  >
    <svg viewBox="0 0 120 120" class="logo-svg" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="appleLogoGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#2E8CFF" />
          <stop offset="50%" stop-color="#007AFF" />
          <stop offset="100%" stop-color="#0062E3" />
        </linearGradient>
        <linearGradient id="appleLogoGloss" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.24" />
          <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0" />
        </linearGradient>
      </defs>
      <!-- iOS Squircle Base -->
      <rect x="8" y="8" width="104" height="104" rx="28" fill="url(#appleLogoGrad)" />
      <!-- Subtle Gloss Inner Rim -->
      <rect
        x="9"
        y="9"
        width="102"
        height="102"
        rx="27"
        fill="url(#appleLogoGloss)"
        stroke="rgba(255,255,255,0.28)"
        stroke-width="2"
      />
      <!-- Minimalist Ledger Bars -->
      <rect class="logo-line" x="30" y="40" width="44" height="9" rx="4.5" fill="#FFFFFF" />
      <rect class="logo-line" x="30" y="57" width="60" height="9" rx="4.5" fill="#FFFFFF" opacity="0.92" />
      <rect class="logo-line" x="30" y="74" width="34" height="9" rx="4.5" fill="#FFFFFF" opacity="0.72" />
      <!-- Active Status Accent Dot -->
      <circle
        cx="86"
        cy="44.5"
        r="8"
        fill="#34C759"
        stroke="#007AFF"
        stroke-width="3.5"
      />
    </svg>
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
  filter: drop-shadow(0 14rpx 28rpx rgba(0, 122, 255, 0.28));
  transition: transform 0.2s var(--spring-default);

  &:active {
    transform: scale(0.96);
  }

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

  .logo-line:nth-of-type(3) {
    animation-delay: 0s;
  }
  .logo-line:nth-of-type(4) {
    animation-delay: 0.15s;
  }
  .logo-line:nth-of-type(5) {
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
    transform-origin: left center;
  }
  50%,
  100% {
    opacity: 1;
    transform: scaleX(1);
    transform-origin: left center;
  }
}
</style>
