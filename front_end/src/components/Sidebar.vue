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
        <view class="sidebar-user" @click="onUserClick">
          <view class="user-avatar">
            <text class="avatar-text">{{ avatarText }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ userInfo?.nickname || "未登录" }}</text>
            <text class="user-email">{{ userInfo?.email || "" }}</text>
          </view>
          <wd-icon name="right" size="28rpx" color="var(--text-dim)" />
        </view>

        <view class="sidebar-section">
          <view class="section-header">
            <text class="section-title">我的账本</text>
            <view class="section-header-actions">
              <view
                v-if="books.length > 0"
                class="section-edit-btn"
                :class="{ active: isEditingBooks }"
                @click="isEditingBooks = !isEditingBooks"
              >
                <wd-icon
                  :name="isEditingBooks ? 'check' : 'edit'"
                  size="28rpx"
                  :color="
                    isEditingBooks
                      ? 'var(--color-primary)'
                      : 'var(--text-secondary)'
                  "
                />
              </view>
              <view
                v-if="!isEditingBooks && books.length < 4"
                class="section-add-btn"
                @click="onOpenAddBook"
              >
                <wd-icon name="plus" size="28rpx" color="var(--color-primary)" />
              </view>
            </view>
          </view>

          <wd-popup
            :model-value="showBookDialog"
            position="center"
            custom-class="add-book-dialog"
            custom-style="background: transparent;"
            :close-on-click-modal="true"
            @update:model-value="onDialogCancel"
          >
            <view class="add-book-dialog-content">
              <text class="dialog-title">{{
                editingBook ? "重命名账本" : "新增账本"
              }}</text>
              <input
                class="dialog-input"
                v-model="bookDialogName"
                :placeholder="editingBook ? '输入新的账本名称' : '输入账本名称'"
                maxlength="20"
                :focus="showBookDialog"
                @confirm="onConfirmBookDialog"
              />
              <view class="dialog-actions">
                <view class="dialog-btn cancel" @click="onDialogCancel">
                  <text>取消</text>
                </view>
                <view class="dialog-btn confirm" @click="onConfirmBookDialog">
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
              :class="{
                active: book.id === currentBookId && !isEditingBooks,
                editing: isEditingBooks,
              }"
              @click="onBookItemClick(book)"
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
              <view v-if="isEditingBooks" class="book-edit-action">
                <wd-icon
                  name="edit"
                  size="28rpx"
                  color="var(--color-primary)"
                />
              </view>
              <wd-icon
                v-else-if="book.id === currentBookId"
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

        <view class="sidebar-divider" />

        <view class="menu-section">
          <view class="section-header menu-header">
            <text class="section-title">功能菜单</text>
          </view>
          <view class="sidebar-actions">
            <view class="action-item" @click="onManageCategories">
              <view class="action-icon-wrap">
                <wd-icon
                  name="tags"
                  size="32rpx"
                  color="var(--color-primary)"
                />
              </view>
              <text class="action-label">分类管理</text>
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
            <view class="action-item" @click="onSearch">
              <view class="action-icon-wrap">
                <wd-icon
                  name="search-line"
                  size="32rpx"
                  color="var(--color-primary)"
                />
              </view>
              <text class="action-label">账单查询</text>
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
            <view class="action-item" @click="onImportExport">
              <view class="action-icon-wrap">
                <wd-icon
                  name="import"
                  size="32rpx"
                  color="var(--color-primary)"
                />
              </view>
              <text class="action-label">导入导出</text>
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
            <view class="action-item" @click="onSync">
              <view class="action-icon-wrap">
                <wd-icon
                  name="sync"
                  size="32rpx"
                  color="var(--color-primary)"
                />
              </view>
              <text class="action-label">同步数据</text>
              <wd-icon name="right" size="26rpx" color="var(--text-dim)" />
            </view>
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
import { ref, computed, watch } from "vue";
import {
  createAccountBook,
  updateAccountBook,
  logoutApi,
  type UserInfo,
} from "@/api/common";
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

const isEditingBooks = ref(false);
const showBookDialog = ref(false);
const bookDialogName = ref("");
const editingBook = ref<AccountBook | null>(null);

watch(
  () => props.visible,
  (val) => {
    if (!val) {
      isEditingBooks.value = false;
    }
  },
);

function onOpenAddBook() {
  editingBook.value = null;
  bookDialogName.value = "";
  showBookDialog.value = true;
}

function onOpenEditBook(book: AccountBook) {
  editingBook.value = book;
  bookDialogName.value = book.name;
  showBookDialog.value = true;
}

function onBookItemClick(book: AccountBook) {
  if (isEditingBooks.value) {
    onOpenEditBook(book);
    return;
  }
  emit("switchBook", book.id);
}

async function onConfirmBookDialog() {
  const name = bookDialogName.value.trim();
  if (!name) {
    uni.showToast({ title: "请输入账本名称", icon: "none" });
    return;
  }

  if (editingBook.value) {
    const targetBook = editingBook.value;
    if (name === targetBook.name) {
      onDialogCancel();
      return;
    }
    try {
      const updated = await updateAccountBook(targetBook.id, { name });
      emit(
        "update:books",
        props.books.map((b) =>
          b.id === targetBook.id ? { ...b, name: updated.name || name } : b,
        ),
      );
      uni.showToast({ title: "修改成功", icon: "success" });
      isEditingBooks.value = false;
    } catch {
      uni.showToast({ title: "修改失败", icon: "error" });
    }
    onDialogCancel();
    return;
  }

  if (props.books.length >= 4) {
    uni.showToast({ title: "最多4个账本", icon: "none" });
    onDialogCancel();
    return;
  }
  try {
    const book = await createAccountBook({ name, type: 0 });
    emit("update:books", [...props.books, book]);
    uni.showToast({ title: "创建成功", icon: "success" });
  } catch {
    uni.showToast({ title: "创建失败", icon: "error" });
  }
  onDialogCancel();
}

function onDialogCancel() {
  bookDialogName.value = "";
  editingBook.value = null;
  showBookDialog.value = false;
}

const { load: loadTransactions, clear: clearTransactions } = useTransactions();
const { clear: clearCategories } = useCategories();

function onUserClick() {
  emit("userClick");
  emit("update:visible", false);
  uni.navigateTo({ url: "/pages/profile/index" });
}

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
  background: var(--border-primary);
  margin: 32rpx 0;
}

.sidebar-section {
  margin-bottom: 8rpx;
}

.menu-section {
  margin-bottom: 8rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8rpx 16rpx;

  &.menu-header,
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

.section-header-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.section-edit-btn {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
  background: var(--bg-input-filled);
  cursor: pointer;
  transition: all 0.15s var(--spring-default);

  &.active {
    background: var(--color-primary-subtle);
  }

  &:active {
    transform: scale(0.92);
  }
}

.section-add-btn {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14rpx;
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
  background: var(--bg-card-subtle);
  border: 1rpx solid var(--border-subtle);
  cursor: pointer;
  transition: all 0.18s var(--spring-default);

  &:active {
    transform: scale(0.99);
    background: var(--bg-hover);
  }

  &.active {
    background: var(--bg-active);
    border-color: rgba(0, 122, 255, 0.26);

    .book-name {
      color: var(--text-primary);
      font-weight: 700;
    }
  }

  &.editing {
    border-color: var(--border-primary);
  }
}

.book-edit-action {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  background: var(--color-primary-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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
  background: var(--bg-card-subtle);
  border: 1rpx solid var(--border-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.action-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 24rpx 24rpx;
  cursor: pointer;
  transition: background-color 0.15s;

  &:active {
    background: var(--bg-hover);
  }

  &:not(:last-child)::after {
    content: "";
    position: absolute;
    left: 102rpx;
    right: 24rpx;
    bottom: 0;
    height: 1rpx;
    background: var(--border-primary);
  }

  .action-icon-wrap {
    width: 56rpx;
    height: 56rpx;
    border-radius: 16rpx;
    background: var(--color-primary-subtle);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .action-label {
    flex: 1;
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
