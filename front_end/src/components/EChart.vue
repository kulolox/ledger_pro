<template>
  <view
    :id="uid"
    ref="chartEl"
    class="echart-container"
    :style="{ height: height + 'px' }"
  />
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
// @ts-ignore
import * as echarts from "echarts/dist/echarts.min.js";
import { useTheme } from "@/stores/themeStore";

const props = withDefaults(
  defineProps<{
    options: Record<string, any>;
    height?: number;
  }>(),
  {
    height: 280,
  },
);

const { themeMode } = useTheme();
const uid = ref("ec_" + Math.random().toString(36).slice(2, 10));
const chartEl = ref<any>(null);
let chart: any = null;
let resizeObserver: ResizeObserver | null = null;

function getDomElement(): HTMLElement | null {
  if (chartEl.value) {
    if (chartEl.value instanceof HTMLElement) {
      return chartEl.value;
    }
    if (chartEl.value.$el && chartEl.value.$el instanceof HTMLElement) {
      return chartEl.value.$el;
    }
  }
  if (typeof document !== "undefined") {
    return document.getElementById(uid.value);
  }
  return null;
}

function initChart() {
  const dom = getDomElement();
  if (!dom) return;

  if (chart) {
    chart.dispose();
    chart = null;
  }

  const isDark =
    themeMode.value === "dark" ||
    (typeof document !== "undefined" &&
      document.documentElement.getAttribute("data-theme") === "dark");

  const width = dom.clientWidth || dom.offsetWidth || 340;
  const height = props.height || 280;

  try {
    chart = echarts.init(dom, isDark ? "dark" : undefined, {
      renderer: "canvas",
      width,
      height,
    });

    if (props.options?.series) {
      chart.setOption({
        backgroundColor: "transparent",
        ...props.options,
      });
    }

    if (typeof ResizeObserver !== "undefined" && !resizeObserver) {
      resizeObserver = new ResizeObserver(() => {
        if (chart && !chart.isDisposed()) {
          const newW = dom.clientWidth || dom.offsetWidth;
          if (newW > 0) {
            chart.resize({ width: newW, height: props.height });
          }
        }
      });
      resizeObserver.observe(dom);
    }
  } catch (err) {
    console.error("EChart init error:", err);
  }
}

onMounted(() => {
  nextTick(() => {
    initChart();
  });
});

watch(
  () => props.options,
  (newOpts) => {
    if (chart && !chart.isDisposed() && newOpts?.series) {
      chart.setOption(
        {
          backgroundColor: "transparent",
          ...newOpts,
        },
        true,
      );
    } else if (newOpts?.series) {
      nextTick(initChart);
    }
  },
  { deep: true },
);

watch(themeMode, () => {
  nextTick(() => {
    initChart();
  });
});

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect();
    resizeObserver = null;
  }
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style lang="scss" scoped>
.echart-container {
  width: 100%;
  box-sizing: border-box;
  display: block;
  overflow: hidden;
}
</style>
