<template>
  <view class="change-pwd-page">
    <NavBar>
      <template #left>
        <view class="nav-back" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">修改密码</text>
      </template>
    </NavBar>

    <view class="page-body">
      <!-- 顶部验证方式切换（Apple HIG 分段控制器） -->
      <view class="mode-segmented">
        <view
          class="segmented-item"
          :class="{ active: mode === 'password' }"
          @click="mode = 'password'"
        >
          <text>原密码修改</text>
        </view>
        <view
          class="segmented-item"
          :class="{ active: mode === 'email' }"
          @click="mode = 'email'"
        >
          <text>邮箱验证码找回</text>
        </view>
      </view>

      <!-- 方式一：原密码修改 -->
      <view v-if="mode === 'password'" class="section-block">
        <text class="section-label">通过当前密码验证</text>
        <view class="cell-group">
          <view class="form-row">
            <text class="row-label">当前密码</text>
            <wd-input
              v-model="pwdForm.oldPassword"
              class="row-input"
              show-password
              placeholder="请输入当前登录密码"
              clearable
            />
          </view>

          <view class="form-row">
            <text class="row-label">新密码</text>
            <wd-input
              v-model="pwdForm.newPassword"
              class="row-input"
              show-password
              placeholder="请输入 6-32 位新密码"
              clearable
            />
          </view>

          <view class="form-row">
            <text class="row-label">确认密码</text>
            <wd-input
              v-model="pwdForm.confirmPassword"
              class="row-input"
              show-password
              placeholder="请再次输入新密码"
              clearable
            />
          </view>
        </view>
        <text class="section-tip"
          >密码长度需为 6-32 位字符，新密码不能与当前密码相同。</text
        >
      </view>

      <!-- 方式二：邮箱验证码找回/重置（二级设置卡片风格） -->
      <view v-else class="section-block">
        <text class="section-label">通过注册邮箱验证</text>
        <view class="cell-group">
          <view class="form-row">
            <text class="row-label">注册邮箱</text>
            <text class="row-static-value">{{
              emailForm.email || "未绑定邮箱"
            }}</text>
          </view>

          <view class="form-row">
            <text class="row-label">验证码</text>
            <wd-input
              v-model="emailForm.code"
              class="row-input"
              placeholder="请输入6位验证码"
              maxlength="6"
              clearable
            />
            <view
              class="code-btn"
              :class="{ disabled: countdown > 0 || sendingCode }"
              @click="onSendCode"
            >
              <text>{{
                countdown > 0 ? `${countdown}s 后重发` : "获取验证码"
              }}</text>
            </view>
          </view>

          <view class="form-row">
            <text class="row-label">新密码</text>
            <wd-input
              v-model="emailForm.newPassword"
              class="row-input"
              show-password
              placeholder="请输入 6-32 位新密码"
              clearable
            />
          </view>

          <view class="form-row">
            <text class="row-label">确认密码</text>
            <wd-input
              v-model="emailForm.confirmPassword"
              class="row-input"
              show-password
              placeholder="请再次输入新密码"
              clearable
            />
          </view>
        </view>
        <text class="section-tip"
          >验证码将发送至您的注册邮箱，5 分钟内有效。</text
        >
      </view>

      <wd-button
        class="submit-btn"
        type="primary"
        block
        :loading="submitting"
        @click="onSubmit"
      >
        {{ mode === "password" ? "保存修改" : "验证并重置密码" }}
      </wd-button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import {
  changePassword,
  sendResetCode,
  resetPassword,
  type UserInfo,
} from "@/api/common";
import { getUserInfo } from "@/utils/storage";

const mode = ref<"password" | "email">("password");

const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const emailForm = reactive({
  email: "",
  code: "",
  newPassword: "",
  confirmPassword: "",
});

const submitting = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

onShow(() => {
  const info = getUserInfo<UserInfo>();
  if (info?.email) {
    emailForm.email = info.email;
  }
});

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});

function goBack() {
  uni.navigateBack();
}

async function onSendCode() {
  if (countdown.value > 0 || sendingCode.value) return;
  if (!emailForm.email) {
    uni.showToast({ title: "未获取到注册邮箱", icon: "none" });
    return;
  }
  sendingCode.value = true;
  try {
    await sendResetCode({ email: emailForm.email });
    uni.showToast({ title: "验证码已发送", icon: "success" });
    countdown.value = 60;
    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
      }
    }, 1000);
  } catch {
    uni.showToast({ title: "发送失败，请稍后重试", icon: "error" });
  } finally {
    sendingCode.value = false;
  }
}

async function onSubmit() {
  if (submitting.value) return;

  if (mode.value === "password") {
    if (!pwdForm.oldPassword) {
      uni.showToast({ title: "请输入当前密码", icon: "none" });
      return;
    }
    if (pwdForm.newPassword.length < 6 || pwdForm.newPassword.length > 32) {
      uni.showToast({ title: "新密码长度需在6-32位之间", icon: "none" });
      return;
    }
    if (pwdForm.newPassword === pwdForm.oldPassword) {
      uni.showToast({ title: "新密码不能与当前密码相同", icon: "none" });
      return;
    }
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      uni.showToast({ title: "两次输入的新密码不一致", icon: "none" });
      return;
    }

    submitting.value = true;
    try {
      await changePassword({
        old_password: pwdForm.oldPassword,
        new_password: pwdForm.newPassword,
      });
      uni.showToast({ title: "密码修改成功", icon: "success" });
      setTimeout(() => {
        uni.navigateBack();
      }, 600);
    } catch {
      uni.showToast({ title: "修改失败，请检查当前密码是否正确", icon: "error" });
    } finally {
      submitting.value = false;
    }
    return;
  }

  // 邮箱验证码模式
  if (!emailForm.email) {
    uni.showToast({ title: "未获取到注册邮箱", icon: "none" });
    return;
  }
  if (!emailForm.code.trim()) {
    uni.showToast({ title: "请输入邮箱验证码", icon: "none" });
    return;
  }
  if (emailForm.newPassword.length < 6 || emailForm.newPassword.length > 32) {
    uni.showToast({ title: "新密码长度需在6-32位之间", icon: "none" });
    return;
  }
  if (emailForm.newPassword !== emailForm.confirmPassword) {
    uni.showToast({ title: "两次输入的新密码不一致", icon: "none" });
    return;
  }

  submitting.value = true;
  try {
    await resetPassword({
      email: emailForm.email,
      code: emailForm.code.trim(),
      new_password: emailForm.newPassword,
    });
    uni.showToast({ title: "密码重置成功", icon: "success" });
    setTimeout(() => {
      uni.navigateBack();
    }, 600);
  } catch {
    uni.showToast({ title: "重置失败，请检查验证码是否正确", icon: "error" });
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="scss" scoped>
.change-pwd-page {
  min-height: 100vh;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;

  &:active {
    background: var(--bg-hover);
  }
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.page-body {
  padding: 28rpx 32rpx 64rpx;
  display: flex;
  flex-direction: column;
  gap: 36rpx;
}

.mode-segmented {
  display: flex;
  background: var(--bg-seg-track);
  padding: 6rpx;
  border-radius: 20rpx;
  box-sizing: border-box;
}

.segmented-item {
  flex: 1;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 27rpx;
  font-weight: 500;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.18s var(--spring-default);

  &.active {
    background: var(--bg-seg-thumb);
    color: var(--text-primary);
    font-weight: 600;
    box-shadow: var(--shadow-seg);
  }

  &:active {
    transform: scale(0.98);
  }
}

.section-block {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.section-label {
  font-size: 24rpx;
  font-weight: 500;
  color: var(--text-tertiary);
  padding: 0 12rpx;
  letter-spacing: 0.5rpx;
}

.section-tip {
  font-size: 24rpx;
  color: var(--text-tertiary);
  padding: 2rpx 12rpx 0;
  line-height: 1.5;
}

.cell-group {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  border: 1rpx solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.form-row {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 28rpx;
  min-height: 104rpx;
  gap: 20rpx;

  &:not(:last-child)::after {
    content: "";
    position: absolute;
    left: 28rpx;
    right: 28rpx;
    bottom: 0;
    height: 1rpx;
    background: var(--border-primary);
  }
}

.row-label {
  width: 144rpx;
  flex-shrink: 0;
  font-size: 30rpx;
  font-weight: 500;
  color: var(--text-primary);
}

.row-static-value {
  flex: 1;
  font-size: 29rpx;
  color: var(--text-secondary);
  font-family: var(--font-mono);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-btn {
  flex-shrink: 0;
  height: 60rpx;
  padding: 0 24rpx;
  border-radius: var(--radius-full);
  background: var(--color-primary-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.15s var(--spring-default), opacity 0.15s;

  text {
    font-size: 25rpx;
    font-weight: 600;
    color: var(--color-primary);
    white-space: nowrap;
  }

  &:active {
    transform: scale(0.95);
  }

  &.disabled {
    opacity: 0.45;
    pointer-events: none;
  }
}

:deep(.row-input) {
  flex: 1;
  min-width: 0;
  width: 100%;
  height: 96rpx !important;
  padding: 0 !important;
  box-sizing: border-box !important;
  display: flex !important;
  align-items: center !important;
  --wd-input-bg: transparent;
  --wd-input-border: transparent;
  --wd-input-color: var(--text-primary);
  --wd-input-placeholder: var(--text-placeholder);
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
    font-size: 29rpx;
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

:deep(.submit-btn) {
  height: 96rpx !important;
  border-radius: var(--radius-lg) !important;
  font-size: 31rpx !important;
  font-weight: 600 !important;
  background: var(--color-primary) !important;
  border: none !important;
  box-shadow: var(--shadow-primary-md);
}
</style>
