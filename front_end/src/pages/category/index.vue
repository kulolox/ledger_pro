<template>
  <view class="category-page">
    <NavBar rightWidth="auto">
      <template #left>
        <view class="nav-back" @click="goBack">
          <wd-icon name="left" size="24px" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">分类管理</text>
      </template>
      <template #right>
        <view class="nav-sort" :class="{ active: sortMode }" @click="sortMode = !sortMode">
          <text>{{ sortMode ? '完成' : '排序' }}</text>
        </view>
      </template>
    </NavBar>

    <view class="tab-bar">
      <view
        class="tab-item"
        :class="{ active: currentTab === 0 }"
        @click="currentTab = 0"
      >
        <text>支出</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 1 }"
        @click="currentTab = 1"
      >
        <text>收入</text>
      </view>
    </view>

    <view class="category-list" id="category-sort-list">
      <view v-if="categoryList.length === 0" class="empty-state">
        <text class="empty-text">暂无分类</text>
      </view>

      <view
        v-for="cat in categoryList"
        :key="cat.id"
        class="category-item"
        :class="{ dimmed: !sortMode && cat.visible === 0 }"
        :data-id="cat.id"
      >
        <view class="cat-left">
          <view v-if="sortMode" class="sort-handle">
            <wd-icon name="menu" size="32rpx" color="var(--text-muted)" />
          </view>
          <view class="cat-icon" :style="{ background: cat.color }">
            <text>{{ cat.icon }}</text>
          </view>
          <view class="cat-info">
            <text class="cat-name">{{ cat.name }}</text>
            <text v-if="!sortMode && cat.visible === 0" class="cat-hidden-tag">已隐藏</text>
          </view>
        </view>
        <view class="cat-right">
          <template v-if="!sortMode">
            <view class="cat-action visibility" @click="onToggleVisible(cat)">
              <wd-icon :name="cat.visible === 0 ? 'eye-invisible' : 'eye'" size="28rpx" color="var(--text-muted)" />
            </view>
            <view v-if="isSystem(cat)" class="cat-lock">
              <wd-icon name="lock" size="24rpx" color="var(--text-muted)" />
            </view>
            <template v-else>
              <view class="cat-action edit" @click="onEdit(cat)">
                <wd-icon name="edit" size="28rpx" color="var(--text-secondary)" />
              </view>
              <view class="cat-action delete" @click="onDelete(cat)">
                <wd-icon name="delete" size="28rpx" color="var(--color-expense)" />
              </view>
            </template>
          </template>
        </view>
      </view>
    </view>

    <view v-if="!sortMode" class="add-bar">
      <view class="add-btn" @click="onAdd">
        <wd-icon name="plus" size="32rpx" color="var(--color-primary)" />
        <text class="add-label">添加分类</text>
      </view>
    </view>

    <wd-popup
      :model-value="editorVisible"
      position="bottom"
      @update:model-value="onEditorClose"
    >
      <view class="editor">
        <view class="editor-header">
          <text class="editor-cancel" @click="onEditorClose">取消</text>
          <text class="editor-title">{{
            isEdit ? "编辑分类" : "添加分类"
          }}</text>
          <text class="editor-confirm" @click="onSave">保存</text>
        </view>

        <view class="editor-form">
          <view class="form-item">
            <text class="form-label">名称</text>
            <input
              class="form-input"
              v-model="form.name"
              placeholder="分类名称"
              maxlength="10"
            />
          </view>

          <view class="form-item">
            <text class="form-label">图标</text>
            <input
              class="form-input emoji-input"
              v-model="form.icon"
              placeholder="选择一个 emoji"
              maxlength="2"
            />
          </view>

          <view class="form-item">
            <text class="form-label">颜色</text>
            <view class="color-row">
              <input
                class="form-input color-input"
                v-model="form.color"
                placeholder="#ff6b35"
                maxlength="7"
              />
              <view
                class="color-preview"
                :style="{ background: form.color || '#ccc' }"
              />
            </view>
          </view>

          <view class="color-presets">
            <text class="preset-label">预设颜色</text>
            <view class="preset-grid">
              <view
                v-for="c in presetColors"
                :key="c"
                class="preset-item"
                :class="{ selected: form.color === c }"
                :style="{ background: c }"
                @click="form.color = c"
              />
            </view>
          </view>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import { useCategories } from "@/stores/categoryStore";
import type { Category } from "@/constants/categories";
import Sortable from "sortablejs";

const {
  categories,
  loadCategories,
  addCategory,
  editCategory,
  removeCategory,
  reorder,
  toggleVisible,
} = useCategories();

const currentTab = ref(0);
const sortMode = ref(false);

const categoryList = computed(() => {
  return categories.value[currentTab.value] || [];
});

function isSystem(cat: Category): boolean {
  return cat.is_system === 1;
}

const presetColors = [
  "#ff6b35", "#5b9bd5", "#ff6b8a", "#a29bfe", "#fdcb6e",
  "#00b894", "#e17055", "#00cec9", "#fd79a8", "#636e72",
  "#ff3b30", "#34c759", "#007aff", "#ff9500", "#af52de",
  "#8e44ad", "#2ecc71", "#e74c3c", "#1abc9c", "#f39c12",
];

let sortableInstance: Sortable | null = null;

const editorVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<number>(0);
const form = ref({ name: "", icon: "", color: "" });

watch(sortMode, async (val) => {
  if (val) {
    await nextTick();
    const el = document.querySelector("#category-sort-list");
    if (el) {
      sortableInstance = Sortable.create(el as HTMLElement, {
        handle: ".sort-handle",
        animation: 150,
        ghostClass: "sort-ghost",
        onEnd: (evt) => {
          if (evt.oldIndex !== undefined && evt.newIndex !== undefined && evt.oldIndex !== evt.newIndex) {
            const list = [...categoryList.value];
            const [moved] = list.splice(evt.oldIndex, 1);
            list.splice(evt.newIndex, 0, moved);
            saveOrder(list);
          }
        },
      });
    }
  } else {
    if (sortableInstance) {
      sortableInstance.destroy();
      sortableInstance = null;
    }
  }
});

onUnmounted(() => {
  if (sortableInstance) {
    sortableInstance.destroy();
    sortableInstance = null;
  }
});

function goBack() {
  uni.navigateBack();
}

function onAdd() {
  isEdit.value = false;
  editingId.value = 0;
  form.value = { name: "", icon: "", color: "" };
  editorVisible.value = true;
}

function onEdit(cat: Category) {
  isEdit.value = true;
  editingId.value = cat.id;
  form.value = {
    name: cat.name,
    icon: cat.icon,
    color: cat.color,
  };
  editorVisible.value = true;
}

function onEditorClose() {
  editorVisible.value = false;
}

async function onSave() {
  if (!form.value.name.trim()) {
    uni.showToast({ title: "请输入分类名称", icon: "none" });
    return;
  }
  if (!form.value.icon.trim()) {
    uni.showToast({ title: "请选择图标", icon: "none" });
    return;
  }
  if (!form.value.color.trim()) {
    uni.showToast({ title: "请选择颜色", icon: "none" });
    return;
  }

  try {
    if (isEdit.value) {
      await editCategory(editingId.value, {
        name: form.value.name.trim(),
        icon: form.value.icon.trim(),
        color: form.value.color.trim(),
      });
      uni.showToast({ title: "修改成功", icon: "success" });
    } else {
      await addCategory({
        name: form.value.name.trim(),
        type: currentTab.value,
        icon: form.value.icon.trim(),
        color: form.value.color.trim(),
      });
      uni.showToast({ title: "添加成功", icon: "success" });
    }
    editorVisible.value = false;
  } catch {
    uni.showToast({ title: "操作失败", icon: "error" });
  }
}

function onDelete(cat: Category) {
  uni.showModal({
    title: "确认删除",
    content: `确定要删除"${cat.name}"分类吗？`,
    confirmText: "删除",
    confirmColor: "#ff3b30",
    success: async (res) => {
      if (res.confirm) {
        try {
          await removeCategory(cat.id);
          uni.showToast({ title: "已删除", icon: "success" });
        } catch {
          uni.showToast({ title: "删除失败", icon: "error" });
        }
      }
    },
  });
}

async function onToggleVisible(cat: Category) {
  const newVisible = cat.visible === 0 ? 1 : 0;
  try {
    await toggleVisible(cat.id, newVisible);
  } catch {
    uni.showToast({ title: "操作失败", icon: "error" });
  }
}

async function saveOrder(list: Category[]) {
  const ids = list.map((c) => c.id);
  try {
    await reorder(ids);
  } catch {
    uni.showToast({ title: "排序失败", icon: "error" });
  }
}

onShow(() => {
  loadCategories();
});
</script>

<style lang="scss" scoped>
.category-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;

  &:active {
    transform: scale(0.92);
  }
}

.nav-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
}

.nav-sort {
  padding: 14rpx 24rpx;
  border-radius: var(--radius-full);
  font-size: 26rpx;
  color: var(--color-primary);
  font-weight: 500;
  background: var(--bg-primary);
  flex-shrink: 0;

  &:active {
    transform: scale(0.92);
  }

  &.active {
    background: var(--color-primary);
    color: var(--text-inverse);
  }
}

.tab-bar {
  display: flex;
  margin: 0 32rpx 24rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 6rpx;
  box-shadow: var(--shadow-sm);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18rpx 0;
  border-radius: var(--radius-sm);
  transition: all 0.2s var(--spring-default);

  text {
    font-size: 28rpx;
    color: var(--text-muted);
    font-weight: 500;
  }

  &.active {
    background: var(--color-primary);

    text {
      color: var(--text-inverse);
      font-weight: 600;
    }
  }
}

.category-list {
  flex: 1;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 0 32rpx 24rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: var(--text-muted);
  }
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: 12rpx;
  box-shadow: var(--shadow-sm);
  transition: opacity 0.2s;

  &.dimmed {
    opacity: 0.45;
  }
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  overflow: hidden;
}

.sort-handle {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cat-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  text {
    font-size: 32rpx;
    color: var(--text-inverse);
    font-weight: 600;
  }
}

.cat-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  overflow: hidden;

  .cat-name {
    font-size: 30rpx;
    font-weight: 500;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .cat-hidden-tag {
    font-size: 20rpx;
    color: var(--text-muted);
  }
}

.cat-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

.cat-lock {
}

.cat-action {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  transition: transform 0.15s;

  &:active {
    transform: scale(0.88);
  }

  &.delete {
    background: rgba(255, 59, 48, 0.1);
  }

  &.visibility {
    background: transparent;
  }
}

.sort-ghost {
  opacity: 0.4;
  background: var(--bg-active) !important;
}

.add-bar {
  padding: 20rpx 32rpx calc(var(--safe-area-bottom, 0px) + 28rpx);
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 26rpx 0;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: background 0.15s;

  &:active {
    background: var(--bg-overlay);
  }

  .add-label {
    font-size: 30rpx;
    color: var(--color-primary);
    font-weight: 500;
  }
}

.editor {
  background: var(--bg-secondary);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  padding: 0 32rpx calc(var(--safe-area-bottom, 0px) + 40rpx);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 0 24rpx;
}

.editor-cancel {
  font-size: 30rpx;
  color: var(--text-muted);
}

.editor-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.editor-confirm {
  font-size: 30rpx;
  color: var(--color-primary);
  font-weight: 600;
}

.editor-form {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.form-label {
  font-size: 26rpx;
  color: var(--text-label);
  font-weight: 500;
}

.form-input {
  height: 88rpx;
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  padding: 0 24rpx;
  font-size: 30rpx;
  color: var(--text-primary);
  box-sizing: border-box;
}

.color-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.color-input {
  flex: 1;
}

.color-preview {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  flex-shrink: 0;
  border: 2rpx solid var(--border-primary);
}

.color-presets {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.preset-label {
  font-size: 24rpx;
  color: var(--text-muted);
}

.preset-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.preset-item {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  transition: transform 0.15s var(--spring-bounce);
  border: 4rpx solid transparent;

  &.selected {
    border-color: var(--text-primary);
    transform: scale(1.15);
  }

  &:active {
    transform: scale(0.88);
  }
}
</style>
