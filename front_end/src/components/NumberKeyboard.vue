<template>
  <view class="keyboard" @touchmove.prevent>
    <view class="kb-grid">
      <view
        v-for="key in keys"
        :key="key.value"
        class="kb-key"
        :class="{
          'kb-key-fn': key.type === 'fn',
          'kb-key-op': key.type === 'op',
          'kb-key-save': key.type === 'save',
          'kb-key-zero': key.value === '0',
        }"
        @click="onKeyClick(key)"
        @longpress="key.type === 'save' && emit('keyLongPress', key.value)"
      >
        <text class="kb-key-text">{{ key.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  keyPress: [key: string];
  keyLongPress: [key: string];
}>();

const keys = [
  { label: "7", value: "7", type: "num" },
  { label: "8", value: "8", type: "num" },
  { label: "9", value: "9", type: "num" },
  { label: "x", value: "backspace", type: "fn" },
  { label: "4", value: "4", type: "num" },
  { label: "5", value: "5", type: "num" },
  { label: "6", value: "6", type: "num" },
  { label: "+", value: "+", type: "op" },
  { label: "1", value: "1", type: "num" },
  { label: "2", value: "2", type: "num" },
  { label: "3", value: "3", type: "num" },
  { label: "−", value: "-", type: "op" },
  { label: "清空", value: "clear", type: "clear" },
  { label: ".", value: ".", type: "num" },
  { label: "0", value: "0", type: "num" },
  { label: "保存", value: "save", type: "save" },
];

const onKeyClick = (key: (typeof keys)[number]) => {
  emit("keyPress", key.value);
};
</script>

<style lang="scss" scoped>
.keyboard {
  padding: 16rpx 16rpx 0;
}

.kb-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  grid-template-rows: repeat(4, 96rpx);
}

.kb-key {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-key);
  border-radius: var(--radius-md);
  user-select: none;
  box-shadow:
    0 2rpx 0 rgba(0, 0, 0, 0.06),
    0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  transition:
    transform 0.12s var(--spring-default),
    box-shadow 0.12s var(--spring-default);
  .kb-key-text {
    font-size: 38rpx;
    color: var(--text-primary);
    font-weight: 500;
    line-height: 1;
  }
  &:active {
    box-shadow:
      0 1rpx 0 rgba(0, 0, 0, 0.04),
      0 2rpx 6rpx rgba(0, 0, 0, 0.04);
    transform: scale(0.95);
  }
}

.kb-key-fn {
  background: var(--bg-operator);
  box-shadow:
    0 2rpx 0 rgba(0, 0, 0, 0.04),
    0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  &:active {
    box-shadow:
      0 1rpx 0 rgba(0, 0, 0, 0.02),
      0 2rpx 6rpx rgba(0, 0, 0, 0.02);
  }

  .kb-key-text {
    color: var(--text-secondary);
    font-size: 28rpx;
  }
}

.kb-key-op {
  background: var(--bg-operator);
  box-shadow:
    0 2rpx 0 rgba(0, 0, 0, 0.04),
    0 4rpx 12rpx rgba(0, 0, 0, 0.04);

  &:active {
    box-shadow:
      0 1rpx 0 rgba(0, 0, 0, 0.02),
      0 2rpx 6rpx rgba(0, 0, 0, 0.02);
  }

  .kb-key-text {
    color: var(--color-primary);
    font-size: 40rpx;
    font-weight: 500;
  }
}

.kb-key-save {
  background: linear-gradient(
    135deg,
    var(--color-save),
    var(--color-save-hover)
  );
  box-shadow:
    0 4rpx 0 var(--color-save-hover),
    0 6rpx 16rpx rgba(0, 0, 0, 0.15);

  &:active {
    box-shadow:
      0 2rpx 0 var(--color-save-hover),
      0 3rpx 8rpx rgba(0, 0, 0, 0.12);
    transform: scale(0.95);
  }

  .kb-key-text {
    color: var(--text-inverse);
    font-size: 28rpx;
    font-weight: 700;
    letter-spacing: 2rpx;
  }
}
</style>
