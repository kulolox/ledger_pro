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
          'kb-key-clear': key.type === 'clear',
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
  { label: "⌫", value: "backspace", type: "fn" },
  { label: "4", value: "4", type: "num" },
  { label: "5", value: "5", type: "num" },
  { label: "6", value: "6", type: "num" },
  { label: "+", value: "+", type: "op" },
  { label: "1", value: "1", type: "num" },
  { label: "2", value: "2", type: "num" },
  { label: "3", value: "3", type: "num" },
  { label: "-", value: "-", type: "op" },
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
  background: var(--bg-tertiary);
  padding: 16rpx 16rpx calc(var(--safe-area-bottom, 0px) + 20rpx);
  border-top: 1rpx solid var(--border-subtle);
  transition: background-color 0.2s var(--spring-default);
}

.kb-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
  grid-template-rows: repeat(4, 104rpx);
}

.kb-key {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-key);
  border-radius: var(--radius-md);
  user-select: none;
  box-shadow: var(--shadow-key);
  border: 1rpx solid var(--border-subtle);
  transition: transform 0.12s var(--spring-default), background-color 0.12s var(--spring-default);
  cursor: pointer;

  .kb-key-text {
    font-size: 42rpx;
    color: var(--text-primary);
    font-weight: 500;
    line-height: 1;
    font-variant-numeric: tabular-nums;
  }

  &:active {
    background: var(--bg-key-hover);
    transform: scale(0.95);
  }
}

.kb-key-fn,
.kb-key-clear {
  background: var(--bg-operator);

  &:active {
    background: var(--bg-operator-hover);
  }

  .kb-key-text {
    color: var(--text-secondary);
    font-size: 30rpx;
    font-weight: 500;
  }
}

.kb-key-op {
  background: var(--bg-operator);

  &:active {
    background: var(--bg-operator-hover);
  }

  .kb-key-text {
    color: var(--color-primary);
    font-size: 44rpx;
    font-weight: 500;
  }
}

.kb-key-save {
  background: var(--color-primary);
  border: none;
  box-shadow: var(--shadow-sm);

  &:active {
    background: var(--color-primary-hover);
    transform: scale(0.95);
  }

  .kb-key-text {
    color: var(--text-inverse);
    font-size: 32rpx;
    font-weight: 600;
    letter-spacing: 1rpx;
  }
}
</style>
