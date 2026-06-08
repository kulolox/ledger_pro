<template>
  <view :id="uid" class="echart" :style="{ height: height + 'px' }" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from "vue";
import * as echarts from "echarts";

const props = withDefaults(defineProps<{
  options: Record<string, any>;
  height?: number;
}>(), {
  height: 300,
});

const uid = ref("ec" + Math.random().toString(36).slice(2, 10));
let chart: echarts.ECharts | null = null;

function init() {
  const el = document.getElementById(uid.value);
  if (!el) return;
  if (chart) chart.dispose();
  chart = echarts.init(el);
  if (props.options?.series) {
    chart.setOption(props.options);
  }
}

onMounted(() => nextTick(init));

watch(() => props.options, (opts) => {
  if (chart && !chart.isDisposed() && opts?.series) {
    chart.setOption(opts, true);
  } else if (opts?.series) {
    nextTick(init);
  }
}, { deep: true });

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style lang="scss" scoped>
.echart {
  width: 100%;
}
</style>
