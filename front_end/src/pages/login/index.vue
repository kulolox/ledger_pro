<template>
  <view class="login-page">
    <view class="login-card">
      <view class="login-header">
        <view class="logo-wrapper">
          <wd-icon name="book" size="72rpx" color="var(--text-inverse)" />
        </view>
        <text class="app-name">LedgerPro</text>
        <text class="app-slogan">极简记账，一秒开始</text>
      </view>

      <view class="login-form">
        <view class="form-item">
          <text class="form-label">邮箱</text>
          <wd-input
            v-model="formData.email"
            class="form-input"
            placeholder="请输入邮箱"
            clearable
          />
        </view>
        <view class="form-item">
          <text class="form-label">密码</text>
          <wd-input
            v-model="formData.password"
            class="form-input"
            show-password
            placeholder="请输入密码"
            clearable
          />
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
        <text class="link" @click="goRegister">注册账号</text>
        <text class="link" @click="goForgotPassword">忘记密码？</text>
      </view>

    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { login } from "@/api/common";
import { setToken, setRefreshToken, setUserInfo } from "@/utils/storage";

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
  } catch (e) {
    console.error("登录失败:", e);
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
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  padding: 48rpx;
}

.login-card {
  width: 100%;
  max-width: 600rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 72rpx;

  .logo-wrapper {
    width: 120rpx;
    height: 120rpx;
    border-radius: 28rpx;
    background: var(--bg-secondary);
    box-shadow: var(--shadow-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 32rpx;
  }

  .app-name {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: 2rpx;
  }

  .app-slogan {
    display: block;
    font-size: 28rpx;
    color: var(--text-muted);
    margin-top: 14rpx;
  }
}

.login-form {
  .form-item {
    margin-bottom: 32rpx;

    .form-label {
      display: block;
      font-size: 26rpx;
      color: var(--text-label);
      font-weight: 500;
      margin-bottom: 14rpx;
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

.login-btn {
  margin-top: 28rpx;
  height: var(--input-height);
  border-radius: var(--radius-md);
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: var(--shadow-primary-md);

  :deep(.wd-button) {
    border-radius: var(--radius-md);
  }
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 36rpx;
  padding: 0 8rpx;

  .link {
    font-size: 26rpx;
    color: var(--color-primary);
    font-weight: 500;
  }
}

.dev-bypass {
  text-align: center;
  margin-top: 80rpx;
  padding: 24rpx;
  border: 2rpx dashed var(--border-primary);
  border-radius: var(--radius-md);

  text {
    font-size: 24rpx;
    color: var(--text-muted);
  }
}
</style>
