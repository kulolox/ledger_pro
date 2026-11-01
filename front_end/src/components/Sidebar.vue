<template>
  <wd-popup
    :model-value="visible"
    position="left"
    :z-index="500"
    @update:model-value="$emit('update:visible', $event)"
    @close="$emit('close')"
  >
    <view class="sidebar">
      <view class="sidebar-scroll">
        <view class="sidebar-user" @click="$emit('userClick')">
          <view class="user-avatar">
            <text class="avatar-text">{{ avatarText }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ userInfo?.nickname || "未登录" }}</text>
            <text class="user-email">{{ userInfo?.email || "" }}</text>
          </view>
        </view>

        <view class="sidebar-section">
          <view class="section-header">
            <text class="section-title">我的账本</text>
            <view
              v-if="books.length < 4"
              class="section-add-btn"
              @click="showAddInput = true"
            >
              <wd-icon name="plus" size="28rpx" color="var(--color-primary)" />
            </view>
          </view>

          <wd-popup
            :model-value="showAddInput"
            position="center"
            custom-class="add-book-dialog"
            custom-style="background: transparent;"
            :close-on-click-modal="true"
            @update:model-value="onAddCancel"
          >
            <view class="add-book-dialog-content">
              <text class="dialog-title">新增账本</text>
              <input
                class="dialog-input"
                v-model="newBookName"
                placeholder="输入账本名称"
                :focus="showAddInput"
                @confirm="onAddBook"
              />
              <view class="dialog-actions">
                <view class="dialog-btn cancel" @click="onAddCancel">
                  <text>取消</text>
                </view>
                <view class="dialog-btn confirm" @click="onAddBook">
                  <text>确定</text>
                </view>
              </view>
            </view>
          </wd-popup>

          <view class="book-list" v-if="books.length > 0">
            <view
              v-for="book in books"
              :key="book.id"
              class="book-item"
              :class="{ active: book.id === currentBookId }"
              @click="$emit('switchBook', book.id)"
            >
              <view
                class="book-icon"
                :style="{
                  background: book.coverIcon
                    ? 'transparent'
                    : getBookColor(book.id),
                }"
              >
                <text>{{ book.coverIcon || getBookLetter(book.name) }}</text>
              </view>
              <text class="book-name">{{ book.name }}</text>
              <wd-icon
                v-if="book.id === currentBookId"
                name="check"
                size="30rpx"
                color="var(--color-primary)"
              />
            </view>
          </view>
          <view v-else class="book-empty">
            <text class="book-empty-text">暂无账本</text>
          </view>
        </view>

        <view class="sidebar-actions">
          <view class="action-item" @click="onManageCategories">
            <wd-icon name="tags" size="34rpx" color="var(--text-secondary)" />
            <text class="action-label">分类管理</text>
          </view>
          <view class="action-item" @click="onSearch">
            <wd-icon
              name="search-line"
              size="34rpx"
              color="var(--text-secondary)"
            />
            <text class="action-label">账单查询</text>
          </view>
          <view class="action-item" @click="onImportExport">
            <wd-icon name="import" size="34rpx" color="var(--text-secondary)" />
            <text class="action-label">导入导出</text>
          </view>
          <view class="action-item" @click="onSync">
            <wd-icon name="sync" size="34rpx" color="var(--text-secondary)" />
            <text class="action-label">同步数据</text>
          </view>
        </view>

        <view class="sidebar-divider" />

        <view class="theme-section">
          <view class="section-header theme-header">
            <text class="section-title">界面外观</text>
          </view>
          <view class="theme-segmented">
            <view
              class="segmented-item"
              :class="{ active: themeMode === 'light' }"
              @click="setTheme('light')"
            >
              <text class="segmented-icon">☀️</text>
              <text class="segmented-label">浅色</text>
            </view>
            <view
              class="segmented-item"
              :class="{ active: themeMode === 'dark' }"
              @click="setTheme('dark')"
            >
              <text class="segmented-icon">🌙</text>
              <text class="segmented-label">深色</text>
            </view>
          </view>
        </view>
      </view>

      <view class="logout-section">
        <view class="logout-btn logout-item" @click="onLogout">
          <wd-icon name="poweroff" size="30rpx" color="var(--color-expense)" />
          <text class="logout-label">退出登录</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { createAccountBook, logoutApi, type UserInfo } from "@/api/common";
import { getRefreshToken, logout } from "@/utils/storage";
import { fullSync, stopSync } from "@/utils/sync";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { useTheme } from "@/stores/themeStore";

interface AccountBook {
  id: string;
  name: string;
  coverIcon?: string;
}

const { themeMode, setTheme } = useTheme();

const props = defineProps<{
  visible: boolean;
  userInfo: UserInfo | null;
  books: AccountBook[];
  currentBookId: string;
}>();

const emit = defineEmits<{
  "update:visible": [value: boolean];
  "update:books": [books: AccountBook[]];
  close: [];
  userClick: [];
  switchBook: [bookId: string];
}>();

const avatarText = computed(() => {
  if (!props.userInfo?.nickname) return "?";
  return props.userInfo.nickname.charAt(0).toUpperCase();
});

const showAddInput = ref(false);
const newBookName = ref("");

async function onAddBook() {
  if (props.books.length >= 4) {
    uni.showToast({ title: "最多4个账本", icon: "none" });
    newBookName.value = "";
    showAddInput.value = false;
    return;
  }
  const name = newBookName.value.trim();
  if (name) {
    try {
      const book = await createAccountBook({ name });
      emit("update:books", [...props.books, book]);
      uni.showToast({ title: "创建成功", icon: "success" });
    } catch {
      uni.showToast({ title: "创建失败", icon: "error" });
    }
  }
  newBookName.value = "";
  showAddInput.value = false;
}

function onAddCancel() {
  newBookName.value = "";
  showAddInput.value = false;
}

const { load: loadTransactions, clear: clearTransactions } = useTransactions();
const { clear: clearCategories } = useCategories();

function onManageCategories() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/category/index" });
}

function onSearch() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/search/index" });
}

function onImportExport() {
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/import-export/index" });
}

async function onSync() {
  emit("update:visible", false);
  uni.showLoading({ title: "同步中..." });
  try {
    const result = await fullSync(props.currentBookId);
    uni.hideLoading();
    uni.showToast({
      title: `已同步：推送 ${result.pushed} 条，拉取 ${result.pulled} 条`,
      icon: "none",
      duration: 2000,
    });
    if (result.pulled > 0 || result.pushed > 0) {
      loadTransactions();
    }
  } catch {
    uni.hideLoading();
    uni.showToast({ title: "同步失败", icon: "error" });
  }
}

function onLogout() {
  emit("update:visible", false);
  const refreshToken = getRefreshToken();
  if (refreshToken) {
    logoutApi({ refresh_token: refreshToken }).catch(() => {});
  }
  stopSync();
  clearTransactions();
  clearCategories();
  logout();
  uni.reLaunch({ url: "/pages/login/index" });
}

const bookColors = ["#af52de", "#007aff", "#34c759", "#ff9500", "#ff3b30"];

function getBookColor(id: string): string {
  let hash = 0;
  for (const ch of id) hash += ch.charCodeAt(0);
  return bookColors[hash % bookColors.length];
}

function getBookLetter(name: string): string {
  return name.charAt(0).toUpperCase();
}
</script>

<style lang="scss" scoped>
.sidebar {
  width: 580rpx;
  height: 100vh;
  background: var(--bg-secondary);
  border-right: 1rpx solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
  transition: background-color 0.2s var(--spring-default);
}

.sidebar-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: calc(var(--status-bar-height, 44px) + 28rpx) 36rpx 32rpx;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 26rpx;
  margin-bottom: 52rpx;
  padding: 8rpx 4rpx;
  border-radius: var(--radius-lg);
  transition: background 0.15s;

  &:active {
    background: var(--bg-overlay);
  }
}

.user-avatar {
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 8rpx 24rpx rgba(0, 122, 255, 0.28);
  border: 4rpx solid var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .avatar-text {
    font-size: 40rpx;
    font-weight: 700;
    color: #ffffff;
  }
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  overflow: hidden;
  flex: 1;

  .user-name {
    font-size: 36rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-email {
    font-size: 24rpx;
    color: var(--text-tertiary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.sidebar-divider {
  height: 1rpx;
  background: var(--border-subtle);
  margin: 40rpx 0;
}

.sidebar-section {
  margin-bottom: 44rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8rpx 20rpx;

  &.theme-header {
    padding-bottom: 16rpx;
  }
}

.section-title {
  font-size: 24rpx;
  color: var(--text-tertiary);
  font-weight: 500;
  letter-spacing: 0.5rpx;
}

.section-add-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  background: var(--bg-active);
  cursor: pointer;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.92);
  }
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.book-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 20rpx 24rpx;
  border-radius: var(--radius-lg);
  border: 1rpx solid transparent;
  cursor: pointer;
  transition: all 0.18s var(--spring-default);

  &:active {
    transform: scale(0.99);
    background: var(--bg-overlay);
  }

  &.active {
    background: var(--bg-active);
    border-color: rgba(0, 122, 255, 0.18);

    .book-name {
      color: var(--text-primary);
      font-weight: 700;
    }
  }
}

.book-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);

  text {
    font-size: 28rpx;
    font-weight: 700;
    color: #ffffff;
  }
}

.book-name {
  font-size: 30rpx;
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-empty {
  padding: 36rpx 0;
  text-align: center;

  .book-empty-text {
    font-size: 26rpx;
    color: var(--text-tertiary);
  }
}

.add-book-dialog-content {
  width: 560rpx;
  padding: 48rpx 40rpx 32rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
}

.dialog-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 36rpx;
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
  font-size: 30rpx;
  font-weight: 600;
  cursor: pointer;

  &.cancel {
    background: var(--bg-input-filled);
    color: var(--text-secondary);
  }

  &.confirm {
    background: var(--color-primary);
    color: #ffffff;
  }
}

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 26rpx;
  padding: 24rpx 20rpx;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.15s;

  &:active {
    background: var(--bg-hover);
  }

  .action-label {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 500;
    letter-spacing: -0.3rpx;
  }
}

.theme-section {
  padding: 0;
}

.theme-segmented {
  display: flex;
  background: var(--bg-seg-track);
  padding: 4rpx;
  border-radius: 18rpx;
  box-sizing: border-box;
}

.segmented-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 56rpx;
  border-radius: 14rpx;
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-tertiary);
  transition: all 0.18s var(--spring-default);
  cursor: pointer;

  &.active {
    background: var(--bg-seg-thumb);
    color: var(--text-primary);
    font-weight: 600;
    box-shadow: var(--shadow-seg);
  }

  &:active {
    transform: scale(0.97);
  }

  .segmented-icon {
    font-size: 28rpx;
  }
}

.logout-section {
  padding: 28rpx 36rpx calc(var(--safe-area-bottom, 0px) + 28rpx);
  border-top: 1rpx solid var(--border-subtle);
  background: var(--bg-secondary);
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  height: 92rpx;
  background: var(--color-expense-bg);
  border: 1rpx solid rgba(255, 59, 48, 0.22);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    opacity 0.15s;

  &:active {
    transform: scale(0.98);
    opacity: 0.85;
  }

  .logout-label {
    font-size: 28rpx;
    font-weight: 600;
    color: var(--color-expense);
    letter-spacing: 0.5rpx;
  }
}
</style>
