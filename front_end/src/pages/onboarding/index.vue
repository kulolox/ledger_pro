<template>
  <view class="onboarding-page">
    <view class="onboarding-content">
      <view class="logo-area">
        <AppLogo :size="160" />
      </view>
      <text class="welcome-title">{{ isEdit ? '修改昵称' : '欢迎使用 LedgerPro' }}</text>
      <text class="welcome-desc">{{ isEdit ? '' : '轻量记账，数据完全本地保存' }}</text>

      <view class="input-area">
        <wd-input
          v-model="nickname"
          custom-class="nickname-input"
          placeholder="请输入你的昵称"
          :maxlength="12"
          clearable
        />
      </view>

      <view class="start-btn" @click="onStart">
        <text class="start-btn-text">{{ isEdit ? '保存' : '开始使用' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import AppLogo from "@/components/AppLogo.vue";
import { useUser } from "@/stores/userStore";

const { statusBarHeight } = uni.getSystemInfoSync();

const nickname = ref("");
const { nickname: currentNickname, createUser, updateNickname, loadUser } = useUser();
const isEdit = ref(false);

onLoad((query: any) => {
  if (query?.edit === "1") {
    isEdit.value = true;
  }
});

onMounted(async () => {
  if (isEdit.value) {
    await loadUser();
    nickname.value = currentNickname.value;
  }
});

async function onStart() {
  const name = nickname.value.trim();
  if (!name) {
    uni.showToast({ title: "请输入昵称", icon: "none" });
    return;
  }
  try {
    if (isEdit.value) {
      await updateNickname(name);
    } else {
      await createUser(name);
    }
    uni.reLaunch({ url: "/pages/index/index" });
  } catch {
    uni.showToast({ title: "保存失败，请重试", icon: "error" });
  }
}
</script>

<style lang="scss" scoped>
.onboarding-page {
  min-height: 100vh;
  padding-top: v-bind(statusBarHeight + "px");
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.onboarding-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 64rpx;
  width: 100%;
  box-sizing: border-box;
}

.logo-area {
  margin-bottom: 48rpx;
}

.welcome-title {
  font-size: 44rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16rpx;
}

.welcome-desc {
  font-size: 28rpx;
  color: var(--text-label);
  margin-bottom: 64rpx;
}

.input-area {
  width: 100%;
  margin-bottom: 48rpx;
}

:deep(.nickname-input) {
  height: 96rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 0 32rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  text-align: center;
}

.start-btn {
  width: 100%;
  height: 96rpx;
  background: var(--color-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-save);
  transition: opacity 0.15s;

  &:active {
    opacity: 0.85;
  }
}

.start-btn-text {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--text-inverse);
}
</style>
