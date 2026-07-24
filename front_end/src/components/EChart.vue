<template>
  <view ref="containerRef" class="echart" :style="{ height: height + 'px' }">
    <!-- #ifndef H5 -->
    <canvas type="2d" :id="canvasId" class="charts" @touchend="onTap" />
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import {
  ref,
  watch,
  onMounted,
  onBeforeUnmount,
  getCurrentInstance,
  nextTick,
} from "vue";
import uCharts from "@qiun/ucharts";

const props = withDefaults(
  defineProps<{
    type: string;
    chartData: Record<string, any>;
    opts?: Record<string, any>;
    height?: number;
  }>(),
  {
    height: 300,
    opts: () => ({}),
  },
);

let _id = 0;
function uid() {
  return `ec${_id++}`;
}

const canvasId = ref(uid());
const containerRef = ref<any>(null);
const instance = getCurrentInstance();
let chart: any = null;
// #ifdef H5
let h5Canvas: HTMLCanvasElement | null = null;
// #endif

function waitLayout(): Promise<void> {
  return new Promise((resolve) => {
    nextTick(() => {
      requestAnimationFrame(() => {
        setTimeout(resolve, 30);
      });
    });
  });
}

async function getCtx(): Promise<any> {
  // #ifdef H5
  const container = containerRef.value?.$el || containerRef.value;
  if (!container) return null;

  if (!h5Canvas) {
    h5Canvas = document.createElement("canvas");
    h5Canvas.style.width = "100%";
    h5Canvas.style.height = "100%";
    container.innerHTML = "";
    container.appendChild(h5Canvas);
  }

  await waitLayout();
  const rect = container.getBoundingClientRect();
  const w = Math.round(rect.width);
  const h = Math.round(rect.height);
  if (w === 0 || h === 0) return null;

  const dpr = uni.getSystemInfoSync().pixelRatio || 1;
  const pw = w * dpr;
  const ph = h * dpr;
  h5Canvas.width = pw;
  h5Canvas.height = ph;
  return { ctx: h5Canvas.getContext("2d"), dpr, w: pw, h: ph };
  // #endif
  // #ifndef H5
  return new Promise((resolve) => {
    const query = uni.createSelectorQuery().in(instance?.proxy);
    query
      .select(`#${canvasId.value}`)
      .fields({ node: true, size: true })
      .exec((res: any) => {
        if (!res[0]?.node) {
          resolve(null);
          return;
        }
        const canvas = res[0].node;
        const dpr = uni.getSystemInfoSync().pixelRatio || 1;
        const pw = res[0].width * dpr;
        const ph = res[0].height * dpr;
        canvas.width = pw;
        canvas.height = ph;
        resolve({ ctx: canvas.getContext("2d"), dpr, w: pw, h: ph });
      });
  });
  // #endif
}

async function initChart() {
  if (!props.chartData?.series) return;
  const result = await getCtx();
  if (!result) {
    chart = null;
    return;
  }
  const { ctx, dpr, w, h } = result;
  chart = new uCharts({
    context: ctx,
    type: props.type,
    categories: props.chartData.categories || [],
    series: props.chartData.series,
    width: w,
    height: h,
    background: "#FFFFFF",
    pixelRatio: dpr,
    animation: true,
    dataLabel: true,
    duration: 500,
    extra:
      props.type === "ring" ? { ring: { ringWidth: 40, labelWidth: 15 } } : {},
    ...props.opts,
  });
}

function updateChart() {
  if (!chart) {
    initChart();
    return;
  }
  chart.updateData({
    categories: props.chartData.categories || [],
    series: props.chartData.series || [],
  });
}

function onTap(e: any) {
  if (chart) {
    chart.touch(e);
    chart.showToolTip(e);
  }
}

function doResize() {
  chart = null;
  initChart();
}

function doReload() {
  chart = null;
  initChart();
}

watch(() => props.chartData, updateChart, { deep: true });

onMounted(() => {
  initChart();
});

onBeforeUnmount(() => {
  chart = null;
  // #ifdef H5
  h5Canvas = null;
  // #endif
});

defineExpose({ resize: doResize, reload: doReload });
</script>

<style lang="scss" scoped>
.echart {
  width: 100%;
  overflow: hidden;

  .charts {
    width: 100%;
    height: 100%;
  }
}
</style>
