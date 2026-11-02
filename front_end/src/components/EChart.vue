<template>
  <view class="echart-wrap" :style="{ height: height + 'px' }">
    <LEchart ref="chartRef" @finished="initChart" />
  </view>
</template>

<script setup lang="ts">
/**
 * ECharts 跨端图表组件（基于 lime-echart）
 * - H5 / App-vue / 小程序统一走 uni Canvas 适配层，无需真实 DOM，真机可用
 * - 直接接收标准 ECharts option（支持 pie 的 borderRadius / padAngle 圆角间隙风格）
 */
import { ref, watch, onBeforeUnmount } from "vue";
import * as echarts from "echarts/core";
import { PieChart } from "echarts/charts";
import { TitleComponent, TooltipComponent } from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";
import LEchart from "@/uni_modules/lime-echart/components/l-echart/l-echart.vue";

echarts.use([PieChart, TitleComponent, TooltipComponent, CanvasRenderer]);

const props = withDefaults(
  defineProps<{
    options: Record<string, any>;
    height?: number;
  }>(),
  {
    height: 270,
  },
);

const chartRef = ref<any>(null);
let chart: any = null;

async function initChart() {
  if (!chartRef.value) return;
  try {
    chart = await chartRef.value.init(echarts);
    if (chart && props.options?.series) {
      chart.setOption(props.options, true);
    }
  } catch (e) {
    console.error("[EChart] init failed", e);
  }
}

watch(
  () => props.options,
  (opts) => {
    if (chart && opts?.series) {
      chart.setOption(opts, true);
    }
  },
  { deep: true },
);

onBeforeUnmount(() => {
  chart = null;
});
</script>

<style lang="scss" scoped>
.echart-wrap {
  width: 100%;
  position: relative;
}
</style>
