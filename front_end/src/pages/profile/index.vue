<template>
  <view class="profile-page" :class="['theme-' + themeMode]" :data-theme="themeMode">
    <NavBar>
      <template #left>
        <view class="nav-back" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">个人中心</text>
      </template>
    </NavBar>

    <view class="profile-content">
      <!-- 顶部头像与概览卡片 -->
      <view class="profile-hero">
        <view class="hero-avatar">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
        <view class="hero-info">
          <text class="hero-name">{{ userInfo?.nickname || "未设置昵称" }}</text>
          <text class="hero-email">{{ userInfo?.email || "-" }}</text>
        </view>
      </view>

      <!-- 基本信息分组 -->
      <view class="section-block">
        <text class="section-label">基本资料</text>
        <view class="cell-group">
          <view class="cell-item clickable" @click="onOpenNicknameDialog">
            <view class="cell-left">
              <text class="cell-title">昵称</text>
            </view>
            <view class="cell-right">
              <text class="cell-value">{{
                userInfo?.nickname || "点击设置"
              }}</text>
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
          </view>

          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-title">注册账号</text>
            </view>
            <view class="cell-right">
              <text class="cell-value readonly">{{
                userInfo?.email || "-"
              }}</text>
            </view>
          </view>

          <view class="cell-item">
            <view class="cell-left">
              <text class="cell-title">注册时间</text>
            </view>
            <view class="cell-right">
              <text class="cell-value readonly">{{ formattedCreatedAt }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 账号安全分组 -->
      <view class="section-block">
        <text class="section-label">账号安全</text>
        <view class="cell-group">
          <view class="cell-item clickable" @click="goChangePassword">
            <view class="cell-left">
              <text class="cell-title">修改密码</text>
            </view>
            <view class="cell-right">
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
          </view>
        </view>
      </view>

      <!-- 危险操作分组：注销账号 -->
      <view class="section-block danger-block">
        <text class="section-label">危险操作</text>
        <view class="cell-group danger-group">
          <view class="cell-item clickable" @click="onOpenDeleteDialog">
            <view class="cell-left">
              <view class="danger-text-col">
                <text class="cell-title danger-title">注销账号</text>
                <text class="danger-desc">永久删除账号及名下所有账本、账单数据</text>
              </view>
            </view>
            <view class="cell-right">
              <wd-icon name="right" size="26rpx" color="var(--color-expense)" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 修改昵称弹窗 -->
    <wd-popup
      :model-value="showNicknameDialog"
      position="center"
      custom-style="background: transparent;"
      :close-on-click-modal="true"
      @update:model-value="onCloseNicknameDialog"
    >
      <view class="dialog-card">
        <text class="dialog-title">修改昵称</text>
        <input
          class="dialog-input"
          v-model="nicknameInput"
          placeholder="请输入新的昵称"
          maxlength="20"
          :focus="showNicknameDialog"
          @confirm="onConfirmNickname"
        />
        <view class="dialog-actions">
          <view class="dialog-btn cancel" @click="onCloseNicknameDialog">
            <text>取消</text>
          </view>
          <view
            class="dialog-btn confirm"
            :class="{ disabled: savingNickname }"
            @click="onConfirmNickname"
          >
            <text>{{ savingNickname ? "保存中..." : "保存" }}</text>
          </view>
        </view>
      </view>
    </wd-popup>

    <!-- 注销账号严格确认弹窗 -->
    <wd-popup
      :model-value="showDeleteDialog"
      position="center"
      custom-style="background: transparent;"
      :close-on-click-modal="true"
      @update:model-value="onCloseDeleteDialog"
    >
      <view class="dialog-card danger-dialog">
        <text class="dialog-title danger">高危操作：永久注销账号</text>
        <view class="danger-warning-box">
          <text class="warning-lead"
            >注销账号后，以下数据将被立即永久销毁且无法恢复：</text
          >
          <view class="warning-list">
            <text class="warning-item">• 您的个人资料与注册邮箱登录凭证</text>
            <text class="warning-item">• 名下所有账本及全部历史收支、转账账单</text>
            <text class="warning-item">• 所有自定义收支分类与资产账户数据</text>
          </view>
        </view>
        <text class="confirm-prompt"
          >请在下方输入“<text class="highlight">确认注销</text>”以继续：</text
        >
        <input
          class="dialog-input danger-input"
          v-model="deleteConfirmText"
          maxlength="10"
          :focus="showDeleteDialog"
        />
        <view class="dialog-actions">
          <view class="dialog-btn cancel" @click="onCloseDeleteDialog">
            <text>再想想</text>
          </view>
          <view
            class="dialog-btn danger-confirm"
            :class="{
              disabled: deleteConfirmText !== '确认注销' || deletingAccount,
            }"
            @click="onConfirmDeleteAccount"
          >
            <text>{{ deletingAccount ? "正在注销..." : "永久注销" }}</text>
          </view>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import {
  getUserInfo as fetchUserInfoApi,
  updateUserInfo,
  deleteAccount,
  type UserInfo,
} from "@/api/common";
import {
  getUserInfo as getStoredUserInfo,
  setUserInfo as setStoredUserInfo,
  isLoggedIn,
  logout,
} from "@/utils/storage";
import { stopSync } from "@/utils/sync";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";

const userInfo = ref<UserInfo | null>(getStoredUserInfo());
const { themeMode } = useTheme();

const avatarText = computed(() => {
  if (!userInfo.value?.nickname) return "?";
  return userInfo.value.nickname.charAt(0).toUpperCase();
});

const formattedCreatedAt = computed(() => {
  const raw = userInfo.value?.createdAt;
  if (!raw) return "-";
  // 统一展示为 YYYY.MM.dd
  return raw.slice(0, 10).replace(/-/g, ".");
});

const showNicknameDialog = ref(false);
const nicknameInput = ref("");
const savingNickname = ref(false);

const showDeleteDialog = ref(false);
const deleteConfirmText = ref("");
const deletingAccount = ref(false);

const { clear: clearTransactions } = useTransactions();
const { clear: clearCategories } = useCategories();

onShow(async () => {
  syncThemeFromStorage();
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  userInfo.value = getStoredUserInfo();
  try {
    const fresh = await fetchUserInfoApi();
    userInfo.value = fresh;
    setStoredUserInfo(fresh);
  } catch {
    // 离线或请求失败时沿用本地缓存
  }
});

function goBack() {
  uni.navigateBack();
}

function onOpenNicknameDialog() {
  nicknameInput.value = userInfo.value?.nickname || "";
  showNicknameDialog.value = true;
}

function onCloseNicknameDialog() {
  showNicknameDialog.value = false;
  nicknameInput.value = "";
}

async function onConfirmNickname() {
  if (savingNickname.value) return;
  const trimmed = nicknameInput.value.trim();
  if (!trimmed) {
    uni.showToast({ title: "昵称不能为空", icon: "none" });
    return;
  }
  if (trimmed === userInfo.value?.nickname) {
    onCloseNicknameDialog();
    return;
  }

  savingNickname.value = true;
  try {
    const updated = await updateUserInfo({ nickname: trimmed });
    userInfo.value = {
      ...(userInfo.value || ({} as UserInfo)),
      ...updated,
      nickname: updated?.nickname || trimmed,
    };
    setStoredUserInfo(userInfo.value);
    uni.showToast({ title: "修改成功", icon: "success" });
    onCloseNicknameDialog();
  } catch {
    uni.showToast({ title: "修改失败", icon: "error" });
  } finally {
    savingNickname.value = false;
  }
}

function goChangePassword() {
  uni.navigateTo({ url: "/pages/change-password/index" });
}

function onOpenDeleteDialog() {
  deleteConfirmText.value = "";
  showDeleteDialog.value = true;
}

function onCloseDeleteDialog() {
  showDeleteDialog.value = false;
  deleteConfirmText.value = "";
}

async function onConfirmDeleteAccount() {
  if (deleteConfirmText.value !== "确认注销" || deletingAccount.value) return;
  deletingAccount.value = true;
  try {
    await deleteAccount();
    stopSync();
    clearTransactions();
    clearCategories();
    logout();
    onCloseDeleteDialog();
    uni.showToast({ title: "账号已永久注销", icon: "success" });
    setTimeout(() => {
      uni.reLaunch({ url: "/pages/login/index" });
    }, 600);
  } catch {
    uni.showToast({ title: "注销失败，请稍后重试", icon: "error" });
  } finally {
    deletingAccount.value = false;
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
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

.profile-content {
  padding: 28rpx 32rpx 64rpx;
  display: flex;
  flex-direction: column;
  gap: 36rpx;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 36rpx 32rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  border: 1rpx solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

.hero-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-text {
    font-size: 46rpx;
    font-weight: 700;
    color: #ffffff;
  }
}

.hero-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.hero-name {
  font-size: 38rpx;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-email {
  font-size: 26rpx;
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.cell-group {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  border: 1rpx solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  overflow: hidden;

  &.danger-group {
    border-color: rgba(255, 59, 48, 0.22);
  }
}

.cell-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 28rpx;
  gap: 20rpx;

  &.clickable {
    cursor: pointer;
    transition: background-color 0.15s;

    &:active {
      background: var(--bg-hover);
    }
  }

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

.cell-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.cell-title {
  font-size: 30rpx;
  font-weight: 500;
  color: var(--text-primary);

  &.danger-title {
    color: var(--color-expense);
    font-weight: 600;
  }
}

.danger-text-col {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.danger-desc {
  font-size: 23rpx;
  color: var(--text-tertiary);
}

.cell-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.cell-value {
  font-size: 28rpx;
  color: var(--text-primary);
  font-weight: 500;

  &.readonly {
    color: var(--text-secondary);
    font-family: var(--font-mono);
  }
}

.cell-hint {
  font-size: 25rpx;
  color: var(--text-tertiary);
}

.dialog-card {
  width: 580rpx;
  padding: 44rpx 40rpx 36rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  box-sizing: border-box;
}

.dialog-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 32rpx;

  &.danger {
    color: var(--color-expense);
    margin-bottom: 24rpx;
  }
}

.danger-warning-box {
  background: var(--color-expense-bg);
  border: 1rpx solid rgba(255, 59, 48, 0.22);
  border-radius: var(--radius-lg);
  padding: 24rpx;
  margin-bottom: 28rpx;
}

.warning-lead {
  display: block;
  font-size: 25rpx;
  font-weight: 600;
  color: var(--color-expense);
  margin-bottom: 12rpx;
  line-height: 1.45;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.warning-item {
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.45;
}

.confirm-prompt {
  display: block;
  font-size: 25rpx;
  color: var(--text-secondary);
  margin-bottom: 14rpx;

  .highlight {
    font-weight: 700;
    color: var(--color-expense);
  }
}

.dialog-input {
  width: 100%;
  height: 84rpx;
  border-radius: var(--radius-md);
  background: var(--bg-input-filled);
  padding: 0 24rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  box-sizing: border-box;

  &.danger-input {
    border: 1rpx solid rgba(255, 59, 48, 0.28);
  }
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
  transition: opacity 0.15s, transform 0.15s;

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

  &.danger-confirm {
    background: var(--color-expense);
    color: #ffffff;
  }

  &.disabled {
    opacity: 0.45;
    pointer-events: none;
  }
}
</style>
