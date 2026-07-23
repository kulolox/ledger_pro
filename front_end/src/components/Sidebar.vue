<template>
  <wd-popup
    :model-value="visible"
    position="left"
    @update:model-value="$emit('update:visible', $event)"
    @close="$emit('close')"
  >
    <view class="sidebar">
      <view class="sidebar-user">
        <view class="user-avatar">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ nickname || "LedgerPro" }}</text>
        </view>
      </view>

      <view class="sidebar-divider" />

      <view class="sidebar-actions">
        <view class="action-item" @click="onSettings">
          <wd-icon name="settings" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">修改昵称</text>
        </view>
        <view class="action-item" @click="onManageCategories">
          <wd-icon name="tags" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">分类管理</text>
        </view>
        <view class="action-item" @click="onSearch">
          <wd-icon
            name="search-line"
            size="28rpx"
            color="var(--text-secondary)"
          />
          <text class="action-label">账单查询</text>
        </view>
        <view class="action-item" @click="onMigration">
          <wd-icon name="swap" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">数据迁移</text>
        </view>

        <view class="action-item clear-item" @click="onClearData">
          <wd-icon name="delete" size="28rpx" color="var(--color-expense)" />
          <text class="action-label">清除数据</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useUser } from "@/stores/userStore";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { execute } from "@/utils/db";

const props = defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  "update:visible": [value: boolean];
  close: [];
}>();

const { nickname } = useUser();
const { clear: clearTransactions } = useTransactions();
const { clear: clearCategories } = useCategories();

const avatarText = computed(() => {
  if (!nickname.value) return "?";
  return nickname.value.charAt(0).toUpperCase();
});

function onManageCategories() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/category/index" });
}

function onSearch() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/search/index" });
}

function onMigration() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/migration/index" });
}

function onSettings() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/onboarding/index?edit=1" });
}

async function onClearData() {
  emit("update:visible", false);
  const res = await new Promise<{ confirm: boolean }>((resolve) => {
    uni.showModal({
      title: "清除数据",
      content: "将删除所有交易记录、分类和设置，此操作不可恢复。",
      confirmText: "确定清除",
      cancelText: "取消",
      success: (r) => resolve(r as any),
    });
  });
  if (!res.confirm) return;

  try {
    await execute("DELETE FROM categories");
    await clearTransactions();
    await execute("DELETE FROM user_profile");
    clearCategories();
    uni.reLaunch({ url: "/pages/onboarding/index" });
  } catch {
    uni.showToast({ title: "清除失败", icon: "error" });
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: 560rpx;
  height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  padding-top: calc(var(--status-bar-height, 44px) + 20rpx);
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx 36rpx 28rpx;
}

.user-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-text {
    font-size: 40rpx;
    font-weight: 700;
    color: var(--text-inverse);
  }
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  overflow: hidden;

  .user-name {
    font-size: 34rpx;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.sidebar-divider {
  height: 1rpx;
  background: var(--border-subtle);
  margin: 0 36rpx;
}

.sidebar-actions {
  padding: 20rpx 0 calc(var(--safe-area-bottom, 0px) + 20rpx);
}

.action-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 36rpx;
  transition: background 0.15s;

  &:active {
    background: var(--bg-overlay);
  }

  .action-label {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 500;
  }
}

.clear-item {
  .action-label {
    color: var(--color-expense);
  }
}
</style>
