<template>
  <view class="echart-container" :style="{ height: height + 'px' }">
    <!-- SVG 矢量环形扇区与引出折线 -->
    <svg
      v-if="pieData.length > 0"
      class="echart-svg"
      :viewBox="`0 0 ${viewBoxWidth} ${height}`"
      xmlns="http://www.w3.org/2000/svg"
    >
      <!-- 扇区路径 -->
      <g class="chart-slices">
        <path
          v-for="(slice, idx) in slices"
          :key="'slice_' + idx"
          :d="slice.path"
          :fill="slice.color"
          :stroke="isDark ? '#1c1c1e' : '#ffffff'"
          stroke-width="2"
          class="slice-path"
        />
      </g>

      <!-- 折线引导线 -->
      <g class="chart-lines">
        <polyline
          v-for="(line, idx) in labelLines"
          :key="'line_' + idx"
          :points="line.points"
          fill="none"
          :stroke="subTextColor"
          stroke-width="1.2"
          stroke-opacity="0.85"
        />
      </g>
    </svg>

    <!-- 外部分类名称与百分比文字标签（绝对定位叠加在折线末端，跨端 100% 兼容） -->
    <view
      v-for="(label, idx) in labels"
      :key="'label_' + idx"
      class="chart-label-badge"
      :class="{ 'align-right': label.anchor === 'end' }"
      :style="{
        left: label.anchor === 'start' ? (label.x / viewBoxWidth) * 100 + '%' : 'auto',
        right: label.anchor === 'end' ? ((viewBoxWidth - label.x) / viewBoxWidth) * 100 + '%' : 'auto',
        top: label.y + 'px',
      }"
    >
      <text class="label-text" :style="{ color: textColor }">{{ label.text }}</text>
    </view>

    <!-- 居中总览文本（总金额 + 总支出/总收入，绝对居中于圆环内凹处） -->
    <view class="chart-center-overlay" v-if="centerTitle">
      <text
        class="center-amount"
        :style="{
          color: centerColor,
          fontSize: (centerFontSize * 2) + 'rpx',
        }"
      >
        {{ centerTitle }}
      </text>
      <text class="center-subtext" :style="{ color: subTextColor }">
        {{ centerSubTitle }}
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useTheme } from "@/stores/themeStore";

const props = withDefaults(
  defineProps<{
    options: Record<string, any>;
    height?: number;
  }>(),
  {
    height: 270,
  },
);

const { themeMode } = useTheme();

const isDark = computed(() => themeMode.value === "dark");
const textColor = computed(() => (isDark.value ? "#f2f2f7" : "#1c1c1e"));
const subTextColor = computed(() => "#8e8e93");

const viewBoxWidth = 360;
const chartH = computed(() => props.height || 270);
const cx = viewBoxWidth / 2;
const cy = computed(() => chartH.value / 2);
const outerRadius = 72;
const innerRadius = 50;
const padAngle = 2.5; // 度

const pieData = computed(() => {
  const series = props.options?.series?.[0];
  if (!series || !Array.isArray(series.data)) return [];
  return series.data;
});

const totalAmount = computed(() => {
  return pieData.value.reduce(
    (acc: number, item: any) => acc + (Number(item.value) || 0),
    0,
  );
});

const centerTitle = computed(() => props.options?.title?.text || "");
const centerSubTitle = computed(() => props.options?.title?.subtext || "");
const centerColor = computed(() => {
  return (
    props.options?.title?.textStyle?.color ||
    (isDark.value ? "#ffffff" : "#1c1c1e")
  );
});

const centerFontSize = computed(() => {
  const len = centerTitle.value.length;
  if (len > 12) return 13;
  if (len > 9) return 15;
  return 17;
});

function polarToCartesian(
  centerX: number,
  centerY: number,
  r: number,
  angleInDegrees: number,
) {
  const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180.0;
  return {
    x: centerX + r * Math.cos(angleInRadians),
    y: centerY + r * Math.sin(angleInRadians),
  };
}

interface SliceInfo {
  path: string;
  color: string;
}

interface LineInfo {
  points: string;
}

interface LabelInfo {
  x: number;
  y: number;
  anchor: "start" | "end";
  text: string;
}

const slices = computed<SliceInfo[]>(() => {
  const total = totalAmount.value;
  if (total <= 0) return [];
  const cY = cy.value;
  const data = pieData.value;
  const result: SliceInfo[] = [];

  if (data.length === 1) {
    const item = data[0];
    const color = item.itemStyle?.color || item.color || "#007aff";
    const p1 = polarToCartesian(cx, cY, outerRadius, 0);
    const p2 = polarToCartesian(cx, cY, outerRadius, 180);
    const p3 = polarToCartesian(cx, cY, outerRadius, 359.99);
    const pIn3 = polarToCartesian(cx, cY, innerRadius, 359.99);
    const pIn2 = polarToCartesian(cx, cY, innerRadius, 180);
    const pIn1 = polarToCartesian(cx, cY, innerRadius, 0);
    const d = `M ${p1.x} ${p1.y} A ${outerRadius} ${outerRadius} 0 0 1 ${p2.x} ${p2.y} A ${outerRadius} ${outerRadius} 0 0 1 ${p3.x} ${p3.y} L ${pIn3.x} ${pIn3.y} A ${innerRadius} ${innerRadius} 0 0 0 ${pIn2.x} ${pIn2.y} A ${innerRadius} ${innerRadius} 0 0 0 ${pIn1.x} ${pIn1.y} Z`;
    result.push({ path: d, color });
    return result;
  }

  let currentAngle = 0;
  data.forEach((item: any) => {
    const val = Number(item.value) || 0;
    const angleSpan = (val / total) * 360;
    const color = item.itemStyle?.color || item.color || "#007aff";

    const startDeg = currentAngle + padAngle / 2;
    const endDeg = currentAngle + angleSpan - padAngle / 2;
    currentAngle += angleSpan;

    if (endDeg > startDeg) {
      const largeArc = endDeg - startDeg <= 180 ? 0 : 1;
      const pStartOut = polarToCartesian(cx, cY, outerRadius, startDeg);
      const pEndOut = polarToCartesian(cx, cY, outerRadius, endDeg);
      const pStartIn = polarToCartesian(cx, cY, innerRadius, startDeg);
      const pEndIn = polarToCartesian(cx, cY, innerRadius, endDeg);

      const d = `M ${pStartOut.x.toFixed(1)} ${pStartOut.y.toFixed(1)} A ${outerRadius} ${outerRadius} 0 ${largeArc} 1 ${pEndOut.x.toFixed(1)} ${pEndOut.y.toFixed(1)} L ${pEndIn.x.toFixed(1)} ${pEndIn.y.toFixed(1)} A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${pStartIn.x.toFixed(1)} ${pStartIn.y.toFixed(1)} Z`;
      result.push({ path: d, color });
    }
  });

  return result;
});

const labelLines = computed<LineInfo[]>(() => {
  const total = totalAmount.value;
  if (total <= 0) return [];
  const cY = cy.value;
  const data = pieData.value;
  const result: LineInfo[] = [];

  if (data.length === 1) {
    const p0 = polarToCartesian(cx, cY, outerRadius + 2, 90);
    const p1 = polarToCartesian(cx, cY, outerRadius + 14, 90);
    const p2x = p1.x + 14;
    result.push({
      points: `${p0.x.toFixed(1)},${p0.y.toFixed(1)} ${p1.x.toFixed(1)},${p1.y.toFixed(1)} ${p2x.toFixed(1)},${p1.y.toFixed(1)}`,
    });
    return result;
  }

  let currentAngle = 0;
  data.forEach((item: any) => {
    const val = Number(item.value) || 0;
    const angleSpan = (val / total) * 360;
    const midAngle = currentAngle + angleSpan / 2;
    currentAngle += angleSpan;

    const pct = (val / total) * 100;
    if (pct < 3 && data.length > 5) return;

    const p0 = polarToCartesian(cx, cY, outerRadius + 2, midAngle);
    const p1 = polarToCartesian(cx, cY, outerRadius + 14, midAngle);
    const isRight = p1.x >= cx;
    const p2x = isRight ? p1.x + 14 : p1.x - 14;

    result.push({
      points: `${p0.x.toFixed(1)},${p0.y.toFixed(1)} ${p1.x.toFixed(1)},${p1.y.toFixed(1)} ${p2x.toFixed(1)},${p1.y.toFixed(1)}`,
    });
  });

  return result;
});

const labels = computed<LabelInfo[]>(() => {
  const total = totalAmount.value;
  if (total <= 0) return [];
  const cY = cy.value;
  const data = pieData.value;
  const result: LabelInfo[] = [];

  if (data.length === 1) {
    const p1 = polarToCartesian(cx, cY, outerRadius + 14, 90);
    result.push({
      x: p1.x + 18,
      y: p1.y,
      anchor: "start",
      text: `${data[0].name} 100%`,
    });
    return result;
  }

  let currentAngle = 0;
  data.forEach((item: any) => {
    const val = Number(item.value) || 0;
    const angleSpan = (val / total) * 360;
    const midAngle = currentAngle + angleSpan / 2;
    currentAngle += angleSpan;

    const pct = (val / total) * 100;
    if (pct < 3 && data.length > 5) return;

    const p1 = polarToCartesian(cx, cY, outerRadius + 14, midAngle);
    const isRight = p1.x >= cx;
    const p2x = isRight ? p1.x + 18 : p1.x - 18;

    result.push({
      x: p2x,
      y: p1.y,
      anchor: isRight ? "start" : "end",
      text: `${item.name} ${pct.toFixed(0)}%`,
    });
  });

  return result;
});
</script>

<style lang="scss" scoped>
.echart-container {
  width: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  overflow: visible;
}

.echart-svg {
  width: 100%;
  height: 100%;
  display: block;
  overflow: visible;
}

.slice-path {
  transition: opacity 0.2s ease;
  &:active {
    opacity: 0.85;
  }
}

.chart-center-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  gap: 6rpx;

  .center-amount {
    font-weight: 800;
    font-variant-numeric: tabular-nums;
    letter-spacing: -0.5rpx;
    max-width: 85%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.1;
  }

  .center-subtext {
    font-size: 24rpx;
    font-weight: 500;
    line-height: 1;
  }
}

.chart-label-badge {
  position: absolute;
  pointer-events: none;
  transform: translateY(-50%);
  white-space: nowrap;
  font-size: 22rpx;
  font-weight: 500;
  line-height: 1;

  .label-text {
    display: block;
    font-variant-numeric: tabular-nums;
  }

  &.align-right {
    text-align: right;
  }
}
</style>
