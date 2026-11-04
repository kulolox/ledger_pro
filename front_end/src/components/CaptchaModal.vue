<template>
  <wd-popup
    :model-value="modelValue"
    position="center"
    custom-style="background: transparent;"
    :close-on-click-modal="true"
    @update:model-value="onClose"
  >
    <view class="captcha-card">
      <text class="captcha-title">安全验证</text>
      <text class="captcha-desc">请输入图形验证码以继续</text>

      <view class="captcha-body">
        <view class="captcha-image-wrapper" @click="fetchCaptcha">
          <image
            v-if="captchaBase64"
            class="captcha-image"
            :src="captchaBase64"
            mode="aspectFit"
          />
          <view v-else class="captcha-loading">
            <text class="captcha-loading-text">加载中...</text>
          </view>
          <view class="refresh-hint">
            <text class="refresh-text">换一张</text>
          </view>
        </view>

        <view class="captcha-input-box">
          <input
            class="captcha-input"
            v-model="inputCode"
            placeholder="请输入4位验证码"
            maxlength="4"
            :focus="modelValue"
            @confirm="onConfirm"
          />
        </view>
      </view>

      <view class="dialog-actions">
        <view class="dialog-btn cancel" @click="onClose">
          <text>取消</text>
        </view>
        <view
          class="dialog-btn confirm"
          :class="{ disabled: !inputCode || inputCode.length < 4 || loading || fetching }"
          @click="onConfirm"
        >
          <text>{{ loading ? "验证中..." : "确认" }}</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { getCaptcha } from "@/api/common";

const props = defineProps<{
  modelValue: boolean;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "confirm", payload: { captchaKey: string; captchaCode: string }): void;
}>();

const captchaKey = ref("");
const captchaBase64 = ref("");
const inputCode = ref("");
const fetching = ref(false);

const fetchCaptcha = async () => {
  if (fetching.value) return;
  fetching.value = true;
  try {
    const res = await getCaptcha();
    if (res && res.captchaKey) {
      captchaKey.value = res.captchaKey;
      captchaBase64.value = res.captchaBase64;
      inputCode.value = "";
    }
  } catch (e) {
    uni.showToast({ title: "验证码加载失败，请重试", icon: "none" });
  } finally {
    fetching.value = false;
  }
};

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      inputCode.value = "";
      fetchCaptcha();
    }
  },
  { immediate: true }
);

const onClose = () => {
  emit("update:modelValue", false);
};

const onConfirm = () => {
  if (!inputCode.value || inputCode.value.trim().length < 4 || props.loading) {
    return;
  }
  emit("confirm", {
    captchaKey: captchaKey.value,
    captchaCode: inputCode.value.trim(),
  });
};

defineExpose({
  refresh: fetchCaptcha,
});
</script>

<style lang="scss" scoped>
.captcha-card {
  width: 600rpx;
  padding: 44rpx 40rpx 36rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  box-sizing: border-box;
}

.captcha-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 12rpx;
}

.captcha-desc {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 32rpx;
}

.captcha-body {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.captcha-image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  background: var(--bg-input-filled);
  border-radius: var(--radius-md);
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.captcha-image {
  width: 260rpx;
  height: 96rpx;
}

.captcha-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-loading-text {
  font-size: 26rpx;
  color: var(--text-tertiary);
}

.refresh-hint {
  position: absolute;
  right: 24rpx;
  font-size: 24rpx;
  color: var(--color-primary);
}

.refresh-text {
  font-size: 24rpx;
  color: var(--color-primary);
}

.captcha-input-box {
  width: 100%;
}

.captcha-input {
  width: 100%;
  height: 84rpx;
  border-radius: var(--radius-md);
  background: var(--bg-input-filled);
  padding: 0 24rpx;
  font-size: 30rpx;
  letter-spacing: 4rpx;
  text-transform: uppercase;
  color: var(--text-primary);
  box-sizing: border-box;
  text-align: center;
}

.dialog-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 36rpx;
}

.dialog-btn {
  flex: 1;
  height: 84rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 29rpx;
  font-weight: 600;
  cursor: pointer;
  transition:
    opacity 0.15s,
    transform 0.15s;

  &:active {
    transform: scale(0.98);
  }

  &.cancel {
    background: var(--bg-input-filled);
    color: var(--text-secondary);
  }

  &.confirm {
    background: var(--color-primary);
    color: #ffffff;
  }

  &.disabled {
    opacity: 0.45;
    pointer-events: none;
  }
}
</style>
