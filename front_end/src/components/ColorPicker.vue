<template>
  <wd-popup
    :model-value="visible"
    position="bottom"
    :z-index="650"
    custom-class="color-picker-popup"
    custom-style="background: transparent;"
    @update:model-value="onPopupChange"
  >
    <view class="color-picker" :class="['theme-' + themeMode]">
      <!-- 头部操作栏 -->
      <view class="picker-header">
        <text class="picker-cancel" @click="handleCancel">取消</text>
        <text class="picker-title">自定义调色盘</text>
        <text class="picker-confirm" @click="handleConfirm">完成</text>
      </view>

      <!-- 颜色对比与输入区 -->
      <view class="preview-card">
        <view class="color-compare">
          <view class="compare-block">
            <view class="color-circle" :style="{ background: currentColor }" />
            <text class="compare-label">当前选色</text>
          </view>
          <view class="compare-divider" />
          <view class="compare-block" @click="resetToInitial">
            <view
              class="color-circle initial"
              :style="{ background: initialColor }"
            />
            <text class="compare-label">原始颜色</text>
          </view>
        </view>

        <view class="hex-input-box">
          <text class="hex-prefix">#</text>
          <input
            class="hex-field"
            :value="hexWithoutHash"
            placeholder="RRGGBB"
            maxlength="6"
            @input="onHexInput"
          />
        </view>
      </view>

      <!-- 滑块调节区 -->
      <view class="sliders-container">
        <!-- 色相滑块 -->
        <view class="slider-group">
          <view class="slider-label-row">
            <text class="slider-label">色相 (Hue)</text>
            <text class="slider-val">{{ hsl.h }}°</text>
          </view>
          <view
            class="slider-track hue-track"
            @touchstart="onSliderTouchStart('hue', $event)"
            @touchmove.stop.prevent="onSliderTouchMove('hue', $event)"
            @touchend="onSliderTouchEnd"
          >
            <view
              class="slider-thumb"
              :style="{
                left: `${(hsl.h / 360) * 100}%`,
                background: currentHuePureColor,
              }"
            />
          </view>
        </view>

        <!-- 饱和度滑块 -->
        <view class="slider-group">
          <view class="slider-label-row">
            <text class="slider-label">饱和度 (Saturation)</text>
            <text class="slider-val">{{ hsl.s }}%</text>
          </view>
          <view
            class="slider-track sat-track"
            :style="{ background: satTrackBackground }"
            @touchstart="onSliderTouchStart('sat', $event)"
            @touchmove.stop.prevent="onSliderTouchMove('sat', $event)"
            @touchend="onSliderTouchEnd"
          >
            <view
              class="slider-thumb"
              :style="{
                left: `${hsl.s}%`,
                background: currentColor,
              }"
            />
          </view>
        </view>

        <!-- 明度滑块 -->
        <view class="slider-group">
          <view class="slider-label-row">
            <text class="slider-label">明度 (Lightness)</text>
            <text class="slider-val">{{ hsl.l }}%</text>
          </view>
          <view
            class="slider-track light-track"
            :style="{ background: lightTrackBackground }"
            @touchstart="onSliderTouchStart('light', $event)"
            @touchmove.stop.prevent="onSliderTouchMove('light', $event)"
            @touchend="onSliderTouchEnd"
          >
            <view
              class="slider-thumb"
              :style="{
                left: `${hsl.l}%`,
                background: currentColor,
              }"
            />
          </view>
        </view>
      </view>

      <!-- 快速推荐色谱板 -->
      <view class="palette-section">
        <view class="swatch-grid">
          <view
            v-for="color in designerSwatches"
            :key="color"
            class="swatch-item"
            :class="{
              active: currentColor.toUpperCase() === color.toUpperCase(),
            }"
            :style="{ background: color }"
            @click="selectSwatch(color)"
          />
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { ref, computed, watch, getCurrentInstance } from "vue";
import { useTheme } from "@/stores/themeStore";
import { hexToHsl, hslToHex, isValidHex, type HSL } from "@/utils/color";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    visible: boolean;
  }>(),
  {
    modelValue: "#1677FF",
    visible: false,
  },
);

const emit = defineEmits<{
  (e: "update:modelValue", val: string): void;
  (e: "update:visible", val: boolean): void;
  (e: "confirm", val: string): void;
  (e: "cancel"): void;
}>();

const { themeMode } = useTheme();
const instance = getCurrentInstance();

// 内部色彩状态
const initialColor = ref<string>("#1677FF");
const currentColor = ref<string>("#1677FF");
const hsl = ref<HSL>({ h: 215, s: 90, l: 54 });

// 设计师精选推荐色盘 (覆盖莫兰迪、马卡龙、高辨识度质感色阶)
const designerSwatches = [
  "#FF4D4F",
  "#FF7A45",
  "#FAAD14",
  "#52C41A",
  "#13C2C2",
  "#1677FF",
  "#722ED1",
  "#EB2F96",
  "#36CFC9",
  "#FF85C0",
  "#B37FEB",
  "#85A5FF",
  "#5CDBD3",
  "#95DE64",
  "#FFC069",
  "#FF9C6E",
  "#8C8C8C",
  "#595959",
];

// 计算属性
const hexWithoutHash = computed(() => {
  return currentColor.value.replace("#", "").toUpperCase();
});

const currentHuePureColor = computed(() => {
  return hslToHex(hsl.value.h, 100, 50);
});

const satTrackBackground = computed(() => {
  // 从灰度到当前色相当前明度的最高饱和纯色
  const gray = hslToHex(hsl.value.h, 0, hsl.value.l);
  const vibrant = hslToHex(hsl.value.h, 100, hsl.value.l);
  return `linear-gradient(to right, ${gray}, ${vibrant})`;
});

const lightTrackBackground = computed(() => {
  // 从纯黑到当前色相再到纯白
  const pure = hslToHex(hsl.value.h, hsl.value.s, 50);
  return `linear-gradient(to right, #000000 0%, ${pure} 50%, #FFFFFF 100%)`;
});

// 同步外部颜色输入
watch(
  () => props.visible,
  (val) => {
    if (val) {
      const hex =
        props.modelValue && isValidHex(props.modelValue)
          ? props.modelValue
          : "#1677FF";
      initialColor.value = hex;
      currentColor.value = hex;
      hsl.value = hexToHsl(hex);
    }
  },
  { immediate: true },
);

function updateFromHsl() {
  currentColor.value = hslToHex(hsl.value.h, hsl.value.s, hsl.value.l);
}

function selectSwatch(hex: string) {
  currentColor.value = hex;
  hsl.value = hexToHsl(hex);
}

function resetToInitial() {
  currentColor.value = initialColor.value;
  hsl.value = hexToHsl(initialColor.value);
}

function onHexInput(e: any) {
  const val = e.detail?.value || "";
  const fullHex = `#${val.trim()}`;
  if (isValidHex(fullHex) && val.length === 6) {
    currentColor.value = fullHex.toUpperCase();
    hsl.value = hexToHsl(fullHex);
  }
}

// 触摸滑块手势计算
let activeSliderType: "hue" | "sat" | "light" | null = null;
let currentTrackRect: { left: number; width: number } | null = null;

function getTrackRect(
  type: "hue" | "sat" | "light",
): Promise<{ left: number; width: number } | null> {
  return new Promise((resolve) => {
    const selector =
      type === "hue"
        ? ".hue-track"
        : type === "sat"
          ? ".sat-track"
          : ".light-track";
    uni
      .createSelectorQuery()
      .in(instance)
      .select(selector)
      .boundingClientRect((data) => {
        const item = Array.isArray(data) ? data[0] : data;
        if (
          item &&
          typeof item.left === "number" &&
          typeof item.width === "number"
        ) {
          resolve({ left: item.left, width: item.width });
        } else {
          resolve(null);
        }
      })
      .exec();
  });
}

async function onSliderTouchStart(type: "hue" | "sat" | "light", e: any) {
  activeSliderType = type;
  currentTrackRect = await getTrackRect(type);
  handleTouch(e);
}

function onSliderTouchMove(type: "hue" | "sat" | "light", e: any) {
  if (activeSliderType !== type) return;
  handleTouch(e);
}

function onSliderTouchEnd() {
  activeSliderType = null;
  currentTrackRect = null;
}

function handleTouch(e: any) {
  if (!activeSliderType || !currentTrackRect || currentTrackRect.width <= 0)
    return;
  const touch = e.touches?.[0] || e.changedTouches?.[0];
  if (!touch) return;

  const clientX = touch.clientX;
  const offset = clientX - currentTrackRect.left;
  const ratio = Math.max(0, Math.min(1, offset / currentTrackRect.width));

  if (activeSliderType === "hue") {
    hsl.value.h = Math.round(ratio * 360);
  } else if (activeSliderType === "sat") {
    hsl.value.s = Math.round(ratio * 100);
  } else if (activeSliderType === "light") {
    hsl.value.l = Math.round(ratio * 100);
  }
  updateFromHsl();
}

function onPopupChange(val: boolean) {
  emit("update:visible", val);
  if (!val) {
    emit("cancel");
  }
}

function handleCancel() {
  emit("update:visible", false);
  emit("cancel");
}

function handleConfirm() {
  emit("update:modelValue", currentColor.value);
  emit("confirm", currentColor.value);
  emit("update:visible", false);
}
</script>

<style lang="scss" scoped>
.color-picker {
  background: var(--bg-secondary);
  border-top: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  padding: 0 32rpx calc(var(--safe-area-bottom, 0px) + 36rpx);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.15);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0 24rpx;
}

.picker-cancel {
  font-size: 30rpx;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 8rpx 16rpx;
}

.picker-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.picker-confirm {
  font-size: 30rpx;
  color: var(--color-primary);
  font-weight: 600;
  cursor: pointer;
  padding: 8rpx 16rpx;
}

/* 颜色对比与 Hex 输入卡片 */
.preview-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24rpx;
  margin-bottom: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.color-compare {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.compare-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.color-circle {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);

  &.initial {
    opacity: 0.85;
    cursor: pointer;
    border: 3rpx dashed var(--border-primary);
  }
}

.compare-label {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.compare-divider {
  width: 1rpx;
  height: 56rpx;
  background: var(--border-subtle);
}

.hex-input-box {
  display: flex;
  align-items: center;
  background: var(--bg-input-filled);
  padding: 0 20rpx;
  height: 76rpx;
  border-radius: var(--radius-md);
  border: 1rpx solid var(--border-subtle);
}

.hex-prefix {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-tertiary);
  margin-right: 8rpx;
}

.hex-field {
  width: 160rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 2rpx;
}

/* 滑块容器 */
.sliders-container {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  margin-bottom: 32rpx;
}

.slider-group {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.slider-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.slider-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  font-weight: 500;
}

.slider-val {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.slider-track {
  position: relative;
  height: 36rpx;
  border-radius: 18rpx;
  cursor: pointer;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.hue-track {
  background: linear-gradient(
    to right,
    #ff0000 0%,
    #ffff00 17%,
    #00ff00 33%,
    #00ffff 50%,
    #0000ff 67%,
    #ff00ff 83%,
    #ff0000 100%
  );
}

.slider-thumb {
  position: absolute;
  top: 50%;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.35);
  pointer-events: none;
  transition: transform 0.1s ease;
}

/* 精选色盘 */
.palette-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.palette-title {
  font-size: 24rpx;
  color: var(--text-tertiary);
  font-weight: 500;
}

.swatch-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16rpx;
  justify-items: center;
}

.swatch-item {
  width: 68rpx;
  height: 68rpx;
  border-radius: 20rpx;
  cursor: pointer;
  transition: transform 0.15s var(--spring-bounce);
  border: 4rpx solid transparent;

  &.active {
    transform: scale(1.15);
    border-color: var(--text-primary);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.25);
  }

  &:active {
    transform: scale(0.92);
  }
}
</style>
