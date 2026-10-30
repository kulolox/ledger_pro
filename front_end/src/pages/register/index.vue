<template>
  <view class="auth-page">
    <view class="auth-card">
      <view class="auth-header">
        <AppLogo :size="120" />
        <text class="auth-title">注册账号</text>
        <text class="auth-desc">创建您的 LedgerPro 账号</text>
      </view>

      <view class="auth-form">
        <view class="form-item">
          <text class="form-label">昵称</text>
          <wd-input
            v-model="formData.nickname"
            class="form-input"
            placeholder="请输入昵称"
            clearable
          />
        </view>
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
            placeholder="请设置密码（至少6位）"
            clearable
          />
        </view>
        <view class="form-item">
          <text class="form-label">确认密码</text>
          <wd-input
            v-model="confirmPassword"
            class="form-input"
            show-password
            placeholder="请再次输入密码"
            clearable
          />
        </view>

        <wd-button
          class="submit-btn"
          type="primary"
          block
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </wd-button>
      </view>

      <view class="auth-footer">
        <text class="link" @click="goLogin">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import AppLogo from "@/components/AppLogo.vue";
import { register } from "@/api/common";
import { setToken, setRefreshToken, setUserInfo } from "@/utils/storage";

const formData = reactive({
  nickname: "",
  email: "",
  password: "",
});
const confirmPassword = ref("");
const loading = ref(false);

const handleRegister = async () => {
  if (!formData.nickname) {
    uni.showToast({ title: "请输入昵称", icon: "none" });
    return;
  }
  if (!formData.email) {
    uni.showToast({ title: "请输入邮箱", icon: "none" });
    return;
  }
  if (formData.password.length < 6) {
    uni.showToast({ title: "密码至少6位", icon: "none" });
    return;
  }
  if (formData.password !== confirmPassword.value) {
    uni.showToast({ title: "两次密码不一致", icon: "none" });
    return;
  }

  loading.value = true;
  try {
    const { token, refreshToken, userInfo } = await register({
      email: formData.email,
      password: formData.password,
      nickname: formData.nickname,
    });
    setToken(token);
    setRefreshToken(refreshToken);
    setUserInfo(userInfo);
    uni.showToast({ title: "注册成功", icon: "success" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/index/index" });
    }, 500);
  } catch (e) {
    console.error("注册失败:", e);
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
  padding: 48rpx 32rpx;
  box-sizing: border-box;
}

.auth-card {
  width: 100%;
  max-width: 680rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: 64rpx 48rpx;
  box-shadow: var(--shadow-md);
  box-sizing: border-box;
}

.auth-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 56rpx;

  .auth-title {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
  }

  .auth-desc {
    display: block;
    font-size: 26rpx;
    color: var(--text-muted);
    margin-top: 10rpx;
    letter-spacing: 0.5rpx;
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
      margin-bottom: 12rpx;
    }
  }

  :deep(.form-input) {
    --wd-input-bg: var(--bg-primary);
    --wd-input-border: var(--border-subtle);
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
  margin-top: 36rpx;
  height: var(--input-height);
  border-radius: var(--radius-md);
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 1rpx;
  box-shadow: var(--shadow-primary-md);
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.98);
  }

  --wot-button-radius-main: var(--radius-md);
}

.auth-footer {
  text-align: center;
  margin-top: 36rpx;

  .link {
    font-size: 26rpx;
    color: var(--color-primary);
    font-weight: 500;
    transition: opacity 0.15s;

    &:active {
      opacity: 0.7;
    }
  }
}
</style>
