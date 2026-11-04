<template>
  <view
    class="nav-header"
    :class="{ 'nav-floating': floating }"
    :style="headerStyle"
  >
    <view
      class="nav-bar"
      :style="navBarStyle"
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
import { computed, ref, onMounted } from "vue";
import { useTheme } from "@/stores/themeStore";

const { themeMode, syncStatusBarStyle } = useTheme();

const props = withDefaults(
  defineProps<{
    rightWidth?: string;
    floating?: boolean;
  }>(),
  {
    rightWidth: "72rpx",
    floating: false,
  },
);

// 获取状态栏安全高度 (单位: px)，避免在 App 端 CSS 变量失效导致导航栏被时间信号栏遮挡
function getSafeStatusBarHeight(): number {
  try {
    if (typeof uni !== "undefined" && typeof uni.getSystemInfoSync === "function") {
      const sys = uni.getSystemInfoSync();
      if (sys && typeof sys.statusBarHeight === "number" && sys.statusBarHeight > 0) {
        return sys.statusBarHeight;
      }
      // #ifdef APP-PLUS
      return 24; // Android / iOS App 兜底高度
      // #endif
    }
  } catch {
    // #ifdef APP-PLUS
    return 24;
    // #endif
  }
  return 0;
}

const statusBarHeight = ref(getSafeStatusBarHeight());

onMounted(() => {
  // 确保进入任何二级页面挂载时，状态栏文字颜色强力纠偏并与当前主题严格吻合，杜绝原生页面转场后被默认变黑
  syncStatusBarStyle(themeMode.value);

  // #ifndef H5
  try {
    if (typeof uni !== "undefined" && typeof uni.getSystemInfoSync === "function") {
      const sys = uni.getSystemInfoSync();
      if (sys && typeof sys.statusBarHeight === "number" && sys.statusBarHeight > 0) {
        statusBarHeight.value = sys.statusBarHeight;
      }
    }
  } catch {
    // ignore
  }
  // #endif
});

const headerStyle = computed(() => {
  if (props.floating) {
    return {
      paddingTop: `calc(${statusBarHeight.value}px + 8rpx)`,
    };
  }
  return {};
});

const navBarStyle = computed(() => {
  const col = props.rightWidth === "auto" ? "72rpx" : props.rightWidth;
  if (props.floating) {
    return {
      gridTemplateColumns: `${col} 1fr ${col}`,
    };
  }
  return {
    gridTemplateColumns: `${col} 1fr ${col}`,
    paddingTop: `calc(${statusBarHeight.value}px + 12rpx)`,
  };
});
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
    padding: calc(var(--status-bar-height, 0px) + 12rpx) 28rpx 16rpx;
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
    padding: calc(var(--status-bar-height, 0px) + 8rpx) 24rpx 8rpx;

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
