<template>
  <view class="login-page">
    <view class="auth-topbar">
      <view class="topbar-spacer" />
      <view class="theme-pill" @click="toggleTheme">
        <text class="theme-pill-icon">{{ themeMode === "dark" ? "🌙" : "☀️" }}</text>
        <text class="theme-pill-text">{{ themeMode === "dark" ? "深色" : "浅色" }}</text>
      </view>
    </view>

    <view class="login-card">
      <view class="login-header">
        <AppLogo :size="152" />
        <text class="app-name">LedgerPro</text>
        <text class="app-slogan">极简记账 · 一秒开始</text>
      </view>

      <view class="login-form">
        <view class="form-item">
          <text class="form-label">邮箱账号</text>
          <view class="input-surface">
            <wd-input
              v-model="formData.email"
              class="form-input"
              placeholder="name@example.com"
              clearable
            />
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">登录密码</text>
          <view class="input-surface">
            <wd-input
              v-model="formData.password"
              class="form-input"
              show-password
              placeholder="请输入密码"
              clearable
            />
          </view>
        </view>

        <wd-button
          class="login-btn"
          type="primary"
          block
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </wd-button>
      </view>

      <view class="login-links">
        <text class="link link-primary" @click="goRegister">注册账号</text>
        <view class="link-dot" />
        <text class="link link-secondary" @click="goForgotPassword">忘记密码？</text>
      </view>
    </view>

    <view class="security-footer">
      <view class="security-badge">
        <wd-icon name="lock-on" size="24rpx" color="var(--color-income)" />
        <text class="security-text">端到端加密数据安全存储</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import AppLogo from "@/components/AppLogo.vue";
import { login } from "@/api/common";
import { setToken, setRefreshToken, setUserInfo } from "@/utils/storage";
import { useTheme } from "@/stores/themeStore";

const { themeMode, toggleTheme } = useTheme();

const formData = reactive({
  email: "",
  password: "",
});

const loading = ref(false);

const handleLogin = async () => {
  if (!formData.email) {
    uni.showToast({ title: "请输入邮箱", icon: "none" });
    return;
  }
  if (!formData.password) {
    uni.showToast({ title: "请输入密码", icon: "none" });
    return;
  }

  loading.value = true;
  try {
    const { token, refreshToken, userInfo } = await login({
      email: formData.email,
      password: formData.password,
    });
    setToken(token);
    setRefreshToken(refreshToken);
    setUserInfo(userInfo);
    uni.showToast({ title: "登录成功", icon: "success" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/index/index" });
    }, 500);
  } catch (e: any) {
    console.error("登录失败:", e);
    uni.showToast({
      title: e?.message || e?.errMsg || "登录失败，请检查网络",
      icon: "none",
      duration: 3000,
    });
  } finally {
    loading.value = false;
  }
};

const goRegister = () => {
  uni.navigateTo({ url: "/pages/register/index" });
};

const goForgotPassword = () => {
  uni.navigateTo({ url: "/pages/forgot-password/index" });
};
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-primary);
  padding: calc(var(--status-bar-height, 24px) + 16rpx) 48rpx 36rpx;
  box-sizing: border-box;
  transition: background-color 0.2s var(--spring-default);
}

.auth-topbar {
  width: 100%;
  max-width: 680rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72rpx;
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
  transition: transform 0.15s var(--spring-default), background-color 0.2s;

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

.login-card {
  width: 100%;
  max-width: 680rpx;
  margin: auto 0;
  padding: 20rpx 8rpx;
  box-sizing: border-box;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 72rpx;

  :deep(.app-logo) {
    margin: 0 auto 32rpx;
  }

  .app-name {
    display: block;
    font-size: 52rpx;
    font-weight: 800;
    color: var(--text-primary);
    letter-spacing: -1rpx;
    line-height: 1.15;
  }

  .app-slogan {
    display: block;
    font-size: 28rpx;
    color: var(--text-tertiary);
    margin-top: 12rpx;
    letter-spacing: 1rpx;
    font-weight: 400;
  }
}

.login-form {
  .form-item {
    margin-bottom: 36rpx;

    .form-label {
      display: block;
      font-size: 26rpx;
      color: var(--text-label);
      font-weight: 500;
      margin-bottom: 14rpx;
      padding-left: 8rpx;
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

:deep(.login-btn) {
  margin-top: 48rpx;
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

.login-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
  margin-top: 48rpx;
  padding: 8rpx 0;

  .link-dot {
    width: 6rpx;
    height: 6rpx;
    border-radius: 50%;
    background: var(--text-dim);
  }

  .link {
    font-size: 28rpx;
    padding: 8rpx 12rpx;
    cursor: pointer;
    transition: opacity 0.15s;

    &:active {
      opacity: 0.6;
    }

    &.link-primary {
      color: var(--color-primary);
      font-weight: 500;
    }

    &.link-secondary {
      color: var(--text-tertiary);
      font-weight: 400;
    }
  }
}

.security-footer {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 32rpx;

  .security-badge {
    display: inline-flex;
    align-items: center;
    gap: 10rpx;
    opacity: 0.85;
  }

  .security-text {
    font-size: 22rpx;
    color: var(--text-tertiary);
    letter-spacing: 0.5rpx;
  }
}
</style>
