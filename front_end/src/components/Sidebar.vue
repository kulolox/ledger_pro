<template>
  <wd-popup
    :model-value="visible"
    position="left"
    @update:model-value="$emit('update:visible', $event)"
    @close="$emit('close')"
  >
    <view class="sidebar">
      <view class="sidebar-user" @click="$emit('userClick')">
        <view class="user-avatar">
          <text class="avatar-text">{{ avatarText }}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userInfo?.nickname || '未登录' }}</text>
          <text class="user-email">{{ userInfo?.email || '' }}</text>
        </view>
      </view>

      <view class="sidebar-divider" />

      <view class="sidebar-section">
        <view class="section-header">
          <text class="section-title">我的账本</text>
          <view
            v-if="books.length < 4 && !showAddInput"
            class="section-add-btn"
            @click="showAddInput = true"
          >
            <wd-icon name="plus" size="28rpx" color="var(--color-primary)" />
          </view>
        </view>

        <view v-if="showAddInput" class="add-book-inline">
          <input
            class="add-book-input"
            v-model="newBookName"
            placeholder="输入账本名称"
            :focus="showAddInput"
            @confirm="onAddBook"
          />
          <text class="add-book-cancel" @click="onAddCancel">取消</text>
        </view>

        <view class="book-list" v-if="books.length > 0">
          <view
            v-for="book in books"
            :key="book.id"
            class="book-item"
            :class="{ active: book.id === currentBookId }"
            @click="$emit('switchBook', book.id)"
          >
            <view class="book-icon" :style="{ background: book.coverIcon ? 'transparent' : getBookColor(book.id) }">
              <text>{{ book.coverIcon || getBookLetter(book.name) }}</text>
            </view>
            <text class="book-name">{{ book.name }}</text>
            <wd-icon v-if="book.id === currentBookId" name="check" size="28rpx" color="var(--color-primary)" />
          </view>
        </view>
        <view v-else class="book-empty">
          <text class="book-empty-text">暂无账本</text>
        </view>
      </view>

      <view class="sidebar-divider" />

      <view class="sidebar-actions">
        <view class="action-item" @click="$emit('manageCategories')">
          <wd-icon name="tags" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">分类管理</text>
        </view>
        <view class="action-item" @click="$emit('importExport')">
          <wd-icon name="import" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">导入导出</text>
        </view>
        <view class="action-item" @click="$emit('sync')">
          <wd-icon name="sync" size="28rpx" color="var(--text-secondary)" />
          <text class="action-label">同步数据</text>
        </view>
        <view class="action-item logout-item" @click="$emit('logout')">
          <wd-icon name="poweroff" size="28rpx" color="var(--color-expense)" />
          <text class="action-label">退出登录</text>
        </view>
      </view>
    </view>
  </wd-popup>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import type { UserInfo } from "@/api/common";

interface AccountBook {
  id: number;
  name: string;
  coverIcon?: string;
}

const props = defineProps<{
  visible: boolean;
  userInfo: UserInfo | null;
  books: AccountBook[];
  currentBookId: number;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  close: [];
  userClick: [];
  switchBook: [bookId: number];
  sync: [];
  logout: [];
  addBook: [name: string];
  manageCategories: [];
  importExport: [];
}>();

const avatarText = computed(() => {
  if (!props.userInfo?.nickname) return '?';
  return props.userInfo.nickname.charAt(0).toUpperCase();
});

const showAddInput = ref(false);
const newBookName = ref("");

function onAddBook() {
  if (props.books.length >= 4) {
    uni.showToast({ title: "最多4个账本", icon: "none" });
    newBookName.value = "";
    showAddInput.value = false;
    return;
  }
  const name = newBookName.value.trim();
  if (name) {
    emit('addBook', name);
  }
  newBookName.value = "";
  showAddInput.value = false;
}

function onAddCancel() {
  newBookName.value = "";
  showAddInput.value = false;
}

const bookColors = ['#007aff', '#34c759', '#ff9500', '#ff3b30', '#af52de'];

function getBookColor(id: number): string {
  return bookColors[id % bookColors.length];
}

function getBookLetter(name: string): string {
  return name.charAt(0).toUpperCase();
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
  transition: background 0.15s;

  &:active {
    background: var(--bg-overlay);
  }
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

  .user-email {
    font-size: 24rpx;
    color: var(--text-muted);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.sidebar-divider {
  height: 1rpx;
  background: var(--border-subtle);
  margin: 0 36rpx;
}

.sidebar-section {
  padding: 28rpx 0 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 36rpx 16rpx;
}

.section-title {
  font-size: 22rpx;
  color: var(--text-label);
  font-weight: 500;
  letter-spacing: 1rpx;
}

.section-add-btn {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  background: var(--bg-active);
}

.book-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 36rpx;
  transition: background 0.15s;

  &:active {
    background: var(--bg-overlay);
  }

  &.active {
    background: var(--bg-active);

    .book-name {
      color: var(--color-primary);
      font-weight: 600;
    }
  }
}

.book-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  text {
    font-size: 30rpx;
    font-weight: 600;
    color: var(--text-inverse);
  }
}

.book-name {
  font-size: 30rpx;
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-empty {
  padding: 40rpx 36rpx;
  text-align: center;

  .book-empty-text {
    font-size: 26rpx;
    color: var(--text-muted);
  }
}

.add-book-inline {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 0 36rpx 16rpx;
}

.add-book-input {
  flex: 1;
  height: 64rpx;
  border-radius: 12rpx;
  background: var(--bg-primary);
  padding: 0 20rpx;
  font-size: 28rpx;
  color: var(--text-primary);
  box-sizing: border-box;
}

.add-book-cancel {
  font-size: 26rpx;
  color: var(--text-muted);
  flex-shrink: 0;
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

.logout-item {
  .action-label {
    color: var(--color-expense);
  }
}
</style>
