<template>
  <view class="auth-page" :class="['theme-' + themeMode]" :data-theme="themeMode">
    <view class="auth-topbar">
      <view class="nav-back-link" @click="goLogin">
        <wd-icon name="left" size="34rpx" color="var(--color-primary)" />
        <text class="nav-back-text">返回</text>
      </view>
      <view class="theme-pill" @click="toggleTheme">
        <text class="theme-pill-icon">{{ themeMode === "dark" ? "☀️" : "🌙" }}</text>
        <text class="theme-pill-text">{{ themeMode === "dark" ? "浅色" : "深色" }}</text>
      </view>
    </view>

    <view class="auth-card">
      <view class="auth-header">
        <AppLogo :size="144" />
        <text class="auth-title">找回密码</text>
        <text class="auth-desc">通过注册邮箱验证码重置密码</text>
      </view>

      <view class="auth-form">
        <view class="form-item">
          <text class="form-label">邮箱</text>
          <view class="input-surface">
            <wd-input
              v-model="formData.email"
              class="form-input"
              placeholder="请输入注册邮箱"
              clearable
            />
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="code-row">
            <view class="input-surface code-surface">
              <wd-input
                v-model="formData.code"
                class="form-input code-input"
                placeholder="请输入验证码"
                clearable
              />
            </view>
            <view
              class="send-code-btn"
              :class="{ disabled: countdown > 0 || sending }"
              @click="handleSendCode"
            >
              <text class="send-code-text">
                {{ countdown > 0 ? `${countdown}s 后重新发送` : "发送验证码" }}
              </text>
            </view>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">新密码</text>
          <view class="input-surface">
            <wd-input
              v-model="formData.new_password"
              class="form-input"
              show-password
              placeholder="请设置新密码 (至少6位)"
              clearable
            />
          </view>
        </view>

        <wd-button
          class="submit-btn"
          type="primary"
          block
          :loading="loading"
          @click="handleReset"
        >
          重置密码
        </wd-button>
      </view>

      <view class="auth-footer">
        <text class="link" @click="goLogin">返回登录</text>
      </view>
    </view>

    <!-- 人机图形验证码弹窗 -->
    <CaptchaModal
      ref="captchaModalRef"
      v-model="showCaptcha"
      :loading="sending"
      @confirm="handleCaptchaConfirm"
    />
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import AppLogo from "@/components/AppLogo.vue";
import CaptchaModal from "@/components/CaptchaModal.vue";
import { sendResetCode, resetPassword } from "@/api/common";
import { useTheme } from "@/stores/themeStore";

const { themeMode, toggleTheme, syncThemeFromStorage } = useTheme();

onShow(() => {
  syncThemeFromStorage();
});

const formData = reactive({
  email: "",
  code: "",
  new_password: "",
});
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
const showCaptcha = ref(false);
const captchaModalRef = ref<InstanceType<typeof CaptchaModal> | null>(null);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});

const handleSendCode = () => {
  if (countdown.value > 0 || sending.value) return;
  if (!formData.email) {
    uni.showToast({ title: "请输入邮箱", icon: "none" });
    return;
  }
  const emailRegex = /^[\w.+-]+@[\w-]+\.[\w.]+$/;
  if (!emailRegex.test(formData.email.trim())) {
    uni.showToast({ title: "邮箱格式不正确", icon: "none" });
    return;
  }
  showCaptcha.value = true;
};

const handleCaptchaConfirm = async (payload: { captchaKey: string; captchaCode: string }) => {
  if (sending.value) return;
  sending.value = true;
  try {
    await sendResetCode({
      email: formData.email.trim(),
      captchaKey: payload.captchaKey,
      captchaCode: payload.captchaCode,
    });
    showCaptcha.value = false;
    uni.showToast({ title: "验证码已发送", icon: "success" });
    countdown.value = 60;
    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        if (countdownTimer) {
          clearInterval(countdownTimer);
          countdownTimer = null;
        }
      }
    }, 1000);
  } catch (_e) {
    captchaModalRef.value?.refresh();
  } finally {
    sending.value = false;
  }
};

const handleReset = async () => {
  if (!formData.email) {
    uni.showToast({ title: "请输入邮箱", icon: "none" });
    return;
  }
  if (!formData.code) {
    uni.showToast({ title: "请输入验证码", icon: "none" });
    return;
  }
  if (formData.new_password.length < 6) {
    uni.showToast({ title: "新密码至少6位", icon: "none" });
    return;
  }

  loading.value = true;
  try {
    await resetPassword({
      email: formData.email,
      code: formData.code,
      new_password: formData.new_password,
    });
    uni.showToast({ title: "密码重置成功", icon: "success" });
    setTimeout(() => {
      uni.navigateBack();
    }, 500);
  } catch (_e) {
    uni.showToast({ title: "重置失败，请检查验证码是否正确", icon: "error" });
  } finally {
    loading.value = false;
  }
};

const goLogin = () => {
  uni.navigateBack();
};
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bg-primary);
  padding: calc(var(--status-bar-height, 24px) + 16rpx) 48rpx 48rpx;
  box-sizing: border-box;
  transition: background-color 0.2s var(--spring-default);
}

.auth-topbar {
  width: 100%;
  max-width: 680rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76rpx;
  margin-bottom: 12rpx;
}

.nav-back-link {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  margin-left: -10rpx;
  padding: 10rpx;
  cursor: pointer;
  transition: opacity 0.15s;

  &:active {
    opacity: 0.55;
  }

  .nav-back-text {
    font-size: 32rpx;
    color: var(--color-primary);
    font-weight: 400;
  }
}

.theme-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 22rpx;
  border-radius: var(--radius-full);
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.95);
  }

  .theme-pill-icon {
    font-size: 26rpx;
    line-height: 1;
  }

  .theme-pill-text {
    font-size: 24rpx;
    font-weight: 500;
    color: var(--text-secondary);
  }
}

.auth-card {
  width: 100%;
  max-width: 680rpx;
  margin: auto 0;
  box-sizing: border-box;
}

.auth-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;

  :deep(.app-logo) {
    margin-bottom: 28rpx;
  }

  .auth-title {
    display: block;
    font-size: 52rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
  }

  .auth-desc {
    display: block;
    font-size: 28rpx;
    color: var(--text-tertiary);
    margin-top: 12rpx;
  }
}

.auth-form {
  .form-item {
    margin-bottom: 32rpx;

    .form-label {
      display: block;
      font-size: 26rpx;
      color: var(--text-label);
      font-weight: 500;
      margin-bottom: 12rpx;
      padding-left: 8rpx;
    }

    .code-row {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .code-surface {
        flex: 1;
        min-width: 0;
      }

      .send-code-btn {
        flex-shrink: 0;
        height: 100rpx;
        box-sizing: border-box;
        padding: 0 32rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: var(--radius-lg);
        background: var(--bg-active);
        border: 1rpx solid rgba(0, 122, 255, 0.22);
        cursor: pointer;
        user-select: none;
        transition: transform 0.15s var(--spring-default), opacity 0.15s;

        &:active {
          transform: scale(0.97);
        }

        &.disabled {
          opacity: 0.5;
          pointer-events: none;
        }

        .send-code-text {
          font-size: 28rpx;
          font-weight: 600;
          color: var(--color-primary);
          white-space: nowrap;
        }
      }
    }
  }

  .input-surface {
    height: 100rpx;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    background: var(--bg-secondary);
    border: 1rpx solid var(--border-primary);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    padding: 0 8rpx;
    transition:
      border-color 0.2s var(--spring-default),
      box-shadow 0.2s var(--spring-default),
      background-color 0.2s var(--spring-default);

    &:focus-within {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 6rpx var(--ring-focus);
    }
  }

  :deep(.form-input) {
    flex: 1;
    width: 100%;
    height: 96rpx !important;
    padding: 0 20rpx !important;
    box-sizing: border-box !important;
    display: flex !important;
    align-items: center !important;
    --wd-input-bg: transparent;
    --wd-input-border: transparent;
    --wd-input-color: var(--text-primary);
    --wd-input-placeholder: var(--text-placeholder);
    --wd-input-radius: var(--radius-lg);
    --wd-input-height: 96rpx;
    background: transparent !important;

    &::after {
      display: none !important;
    }

    .wd-input__body {
      flex: 1;
      height: 100%;
      display: flex;
      align-items: center;
    }

    .wd-input__inner {
      color: var(--text-primary);
      font-size: 30rpx;
      height: 96rpx !important;
      padding: 0 !important;
      caret-color: var(--color-primary);

      &::placeholder {
        color: var(--text-placeholder);
      }
    }

    .wd-input__clear,
    .wd-input__password,
    .wd-input__icon {
      color: var(--text-tertiary);
    }
  }
}

:deep(.submit-btn) {
  margin-top: 52rpx;
  height: 104rpx !important;
  border-radius: var(--radius-lg) !important;
  font-size: 32rpx !important;
  font-weight: 600 !important;
  letter-spacing: 1rpx;
  background: var(--color-primary) !important;
  border: none !important;
  box-shadow: var(--shadow-primary-md);
  transition: transform 0.15s var(--spring-default), opacity 0.15s;

  &:active {
    transform: scale(0.98);
    opacity: 0.92;
  }
}

.auth-footer {
  text-align: center;
  margin-top: 44rpx;

  .link {
    display: inline-block;
    font-size: 28rpx;
    color: var(--color-primary);
    font-weight: 500;
    padding: 12rpx 24rpx;
    cursor: pointer;
    transition: opacity 0.15s;

    &:active {
      opacity: 0.6;
    }
  }
}
</style>
