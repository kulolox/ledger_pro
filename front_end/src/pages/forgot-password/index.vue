<template>
  <view class="auth-page">
    <view class="auth-card">
      <view class="auth-header">
        <text class="auth-title">找回密码</text>
        <text class="auth-desc">通过邮箱验证码重置密码</text>
      </view>

      <view class="auth-form">
        <view class="form-item">
          <text class="form-label">邮箱</text>
          <wd-input
            v-model="formData.email"
            class="form-input"
            placeholder="请输入注册邮箱"
            type="mail"
            clearable
          />
        </view>
        <view class="form-item">
          <text class="form-label">验证码</text>
          <view class="code-row">
            <wd-input
              v-model="formData.code"
              class="form-input code-input"
              type="inter"
              placeholder="请输入验证码"
              clearable
            />
            <wd-button
              class="send-code-btn"
              type="success"
              :disabled="countdown > 0"
              :loading="sending"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : "发送验证码" }}
            </wd-button>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">新密码</text>
          <wd-input
            v-model="formData.new_password"
            class="form-input"
            show-password
            placeholder="请设置新密码（至少6位）"
            clearable
          />
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
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from "vue";
import { sendResetCode, resetPassword } from "@/api/common";

const formData = reactive({
  email: "",
  code: "",
  new_password: "",
});
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});

const handleSendCode = async () => {
  if (!formData.email) {
    uni.showToast({ title: "请输入邮箱", icon: "none" });
    return;
  }
  sending.value = true;
  try {
    await sendResetCode({ email: formData.email });
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
    uni.showToast({ title: "发送失败，请稍后重试", icon: "error" });
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
    }, 800);
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
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  padding: 48rpx;
}

.auth-card {
  width: 100%;
  max-width: 600rpx;
}

.auth-header {
  text-align: center;
  margin-bottom: 56rpx;

  .auth-title {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: 1rpx;
  }

  .auth-desc {
    display: block;
    font-size: 28rpx;
    color: var(--text-muted);
    margin-top: 14rpx;
  }
}

.auth-form {
  .form-item {
    margin-bottom: 28rpx;

    .form-label {
      display: block;
      font-size: 26rpx;
      color: var(--text-label);
      font-weight: 500;
      margin-bottom: 14rpx;
    }

    .code-row {
      display: flex;
      gap: 16rpx;

      .code-input {
        flex: 1;
      }

      .send-code-btn {
        flex-shrink: 0;
        height: var(--input-height);
        font-size: 24rpx;
        font-weight: 500;
        border-radius: var(--radius-md);
      }
    }
  }

  :deep(.form-input) {
    --wd-input-bg: var(--bg-secondary);
    --wd-input-border: var(--border-primary);
    --wd-input-color: var(--text-primary);
    --wd-input-placeholder: var(--text-placeholder);
    --wd-input-radius: var(--radius-md);
    --wd-input-height: var(--input-height);

    .wd-input__inner {
      color: var(--text-primary);
      font-size: 30rpx;
      caret-color: var(--color-primary);

      &::placeholder {
        color: var(--text-placeholder);
      }
    }

    .wd-input__clear,
    .wd-input__password {
      color: var(--color-primary);
    }
  }
}

.submit-btn {
  margin-top: 28rpx;
  height: var(--input-height);
  border-radius: var(--radius-md);
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: var(--shadow-primary-md);
}

.auth-footer {
  text-align: center;
  margin-top: 36rpx;

  .link {
    font-size: 26rpx;
    color: var(--color-primary);
    font-weight: 500;
  }
}
</style>
