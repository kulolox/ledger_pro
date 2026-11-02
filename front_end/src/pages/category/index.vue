<template>
  <view class="category-page" :class="['theme-' + themeMode]" :data-theme="themeMode">
    <NavBar rightWidth="108rpx">
      <template #left>
        <view class="nav-back" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">分类管理</text>
      </template>
      <template #right>
        <view
          class="nav-sort"
          :class="{ active: sortMode }"
          @click="toggleSortMode"
        >
          <text>{{ sortMode ? "完成" : "排序" }}</text>
        </view>
      </template>
    </NavBar>

    <view class="segmented-bar">
      <wd-tabs v-model="currentTab" custom-class="tab-custom">
        <wd-tab title="支出" />
        <wd-tab title="收入" />
      </wd-tabs>
    </view>

    <view class="category-list-wrap">
      <view v-if="categoryList.length === 0" class="empty-state">
        <text class="empty-text">暂无分类</text>
      </view>

      <view v-else class="category-list" id="category-sort-list">
        <view
          v-for="(cat, index) in categoryList"
          :key="cat.id"
          class="category-item"
          :class="{
            dimmed: !sortMode && cat.visible === 0,
            'is-dragging': sortMode && draggingIndex === index,
          }"
          :style="sortMode && draggingIndex === index ? { transform: `translateY(${dragOffsetY}px)`, zIndex: 99 } : {}"
          :data-id="cat.id"
        >
          <view class="cat-left">
            <view
              v-if="sortMode"
              class="sort-handle"
              @touchstart="onTouchStart($event, index)"
              @touchmove.stop.prevent="onTouchMove($event, index)"
              @touchend="onTouchEnd"
              @touchcancel="onTouchEnd"
            >
              <wd-icon name="menu" size="32rpx" color="var(--text-tertiary)" />
            </view>
            <view class="cat-icon" :style="{ background: cat.color }">
              <text>{{ cat.icon }}</text>
            </view>
            <view class="cat-info">
              <view class="cat-title-row">
                <text class="cat-name">{{ cat.name }}</text>
                <text v-if="!sortMode && cat.visible === 0" class="cat-hidden-tag"
                  >已隐藏</text
                >
              </view>
              <text class="cat-desc">{{ getCategoryDesc(cat) }}</text>
            </view>
          </view>
          <view class="cat-right">
            <template v-if="!sortMode">
              <view class="cat-action visibility" @click="onToggleVisible(cat)">
                <wd-icon
                  :name="cat.visible === 0 ? 'eye-invisible' : 'eye'"
                  size="34rpx"
                  :color="
                    cat.visible === 0
                      ? 'var(--text-tertiary)'
                      : 'var(--color-primary)'
                  "
                />
              </view>
              <view v-if="isSystem(cat)" class="cat-lock">
                <wd-icon name="menu" size="28rpx" color="var(--text-dim)" />
              </view>
              <template v-else>
                <view class="cat-action edit" @click="onEdit(cat)">
                  <wd-icon
                    name="edit"
                    size="30rpx"
                    color="var(--text-secondary)"
                  />
                </view>
                <view class="cat-action delete" @click="onDelete(cat)">
                  <wd-icon
                    name="delete"
                    size="30rpx"
                    color="var(--color-expense)"
                  />
                </view>
              </template>
            </template>
            <view v-else class="sort-actions">
              <view
                class="sort-btn move-up"
                :class="{ disabled: index === 0 }"
                @click.stop="moveItem(index, -1)"
              >
                <wd-icon
                  name="arrow-up"
                  size="26rpx"
                  :color="index === 0 ? 'var(--text-dim)' : 'var(--text-secondary)'"
                />
              </view>
              <view
                class="sort-btn move-down"
                :class="{ disabled: index === categoryList.length - 1 }"
                @click.stop="moveItem(index, 1)"
              >
                <wd-icon
                  name="arrow-down"
                  size="26rpx"
                  :color="index === categoryList.length - 1 ? 'var(--text-dim)' : 'var(--text-secondary)'"
                />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!sortMode" class="add-bar">
      <view class="add-btn" @click="onAdd">
        <wd-icon name="plus" size="32rpx" color="#ffffff" />
        <text class="add-label">添加分类</text>
      </view>
    </view>

    <wd-popup
      :model-value="editorVisible"
      position="bottom"
      custom-class="editor-popup"
      custom-style="background: transparent;"
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

    <wd-dialog selector="category-delete" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import { useDialog } from "@wot-ui/ui";
import { useCategories } from "@/stores/categoryStore";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";
import type { Category } from "@/constants/categories";

const {
  categories,
  loadCategories,
  addCategory,
  editCategory,
  removeCategory,
  reorder,
  toggleVisible,
} = useCategories();

const { themeMode } = useTheme();

const currentTab = ref(0);
const sortMode = ref(false);

const categoryList = computed(() => {
  return categories.value[currentTab.value] || [];
});

const categoryDescMap: Record<string, string> = {
  餐饮: "早餐、午餐、聚餐、外卖",
  交通: "公交、地铁、打车、加油",
  购物: "日用品、服饰、数码产品",
  娱乐: "电影、游戏、休闲消遣",
  居家: "房租、水电煤气、物业",
  通讯: "话费、宽带、云服务",
  医疗: "药品、门诊、健康体检",
  教育: "书籍、课程、学费培训",
  人情: "礼金、请客送礼、节日红包",
  其他: "备用款项、其他杂支",
  工资: "月度薪资、绩效奖金",
  兼职: "副业收入、稿费劳务",
  理财: "基金股票、利息分红",
  红包: "节日红包、转账收款",
  退款: "退货退款、报销款项",
};

function getCategoryDesc(cat: Category): string {
  return categoryDescMap[cat.name] || (isSystem(cat) ? "系统预设分类" : "自定义分类");
}

function isSystem(cat: Category): boolean {
  return cat.is_system === 1;
}

const presetColors = [
  "#ff6b35",
  "#5b9bd5",
  "#ff6b8a",
  "#a29bfe",
  "#fdcb6e",
  "#00b894",
  "#e17055",
  "#00cec9",
  "#fd79a8",
  "#636e72",
  "#ff3b30",
  "#34c759",
  "#007aff",
  "#ff9500",
  "#af52de",
  "#8e44ad",
  "#2ecc71",
  "#e74c3c",
  "#1abc9c",
  "#f39c12",
];

const editorVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<string>("");
const form = ref({ name: "", icon: "", color: "" });
const dialog = useDialog("category-delete");

// 拖拽手势状态
const draggingIndex = ref<number>(-1);
const dragOffsetY = ref<number>(0);
let startTouchY = 0;
const ITEM_HEIGHT = 64; // px

function toggleSortMode() {
  sortMode.value = !sortMode.value;
  if (!sortMode.value) {
    draggingIndex.value = -1;
    dragOffsetY.value = 0;
  }
}

function onTouchStart(e: any, index: number) {
  if (!sortMode.value) return;
  const touch = e.touches?.[0] || e.changedTouches?.[0];
  if (!touch) return;
  draggingIndex.value = index;
  startTouchY = touch.clientY;
  dragOffsetY.value = 0;
  // #ifdef APP-PLUS
  try {
    uni.vibrateShort({ success: () => {} });
  } catch {}
  // #endif
}

function onTouchMove(e: any, index: number) {
  if (!sortMode.value || draggingIndex.value < 0) return;
  const touch = e.touches?.[0] || e.changedTouches?.[0];
  if (!touch) return;
  const currentY = touch.clientY;
  const delta = currentY - startTouchY;
  dragOffsetY.value = delta;

  const list = [...categoryList.value];
  const curIdx = draggingIndex.value;

  // 向下滑动跨过一项
  if (delta > 32 && curIdx < list.length - 1) {
    const [moved] = list.splice(curIdx, 1);
    list.splice(curIdx + 1, 0, moved);
    categories.value[currentTab.value] = list;
    draggingIndex.value = curIdx + 1;
    startTouchY += ITEM_HEIGHT;
    dragOffsetY.value = currentY - startTouchY;
    // #ifdef APP-PLUS
    try {
      uni.vibrateShort({ success: () => {} });
    } catch {}
    // #endif
  }
  // 向上滑动跨过一项
  else if (delta < -32 && curIdx > 0) {
    const [moved] = list.splice(curIdx, 1);
    list.splice(curIdx - 1, 0, moved);
    categories.value[currentTab.value] = list;
    draggingIndex.value = curIdx - 1;
    startTouchY -= ITEM_HEIGHT;
    dragOffsetY.value = currentY - startTouchY;
    // #ifdef APP-PLUS
    try {
      uni.vibrateShort({ success: () => {} });
    } catch {}
    // #endif
  }
}

function onTouchEnd() {
  if (!sortMode.value || draggingIndex.value < 0) return;
  draggingIndex.value = -1;
  dragOffsetY.value = 0;
  saveOrder(categoryList.value);
}

function moveItem(index: number, step: -1 | 1) {
  const target = index + step;
  const list = [...categoryList.value];
  if (target < 0 || target >= list.length) return;
  const [moved] = list.splice(index, 1);
  list.splice(target, 0, moved);
  categories.value[currentTab.value] = list;
  // #ifdef APP-PLUS
  try {
    uni.vibrateShort({ success: () => {} });
  } catch {}
  // #endif
  saveOrder(list);
}

onUnmounted(() => {
  draggingIndex.value = -1;
  dragOffsetY.value = 0;
});

function goBack() {
  uni.navigateBack();
}

function onAdd() {
  isEdit.value = false;
  editingId.value = "";
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

async function onDelete(cat: Category) {
  const result = await dialog.confirm({
    title: "确认删除",
    msg: `确定要删除"${cat.name}"分类吗？`,
  });
  if (result.action === "confirm") {
    try {
      await removeCategory(cat.id);
      uni.showToast({ title: "已删除", icon: "success" });
    } catch {
      uni.showToast({ title: "删除失败", icon: "error" });
    }
  }
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
  syncThemeFromStorage();
  loadCategories();
});
</script>

<style lang="scss" scoped>
.category-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  overflow: hidden;
  transition: background-color 0.2s var(--spring-default);
}

.nav-back {
  width: 68rpx;
  height: 68rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.15s;

  &:active {
    background: var(--bg-hover);
    transform: scale(0.92);
  }
}

.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  letter-spacing: -0.5rpx;
}

.nav-sort {
  padding: 10rpx 26rpx;
  border-radius: var(--radius-full);
  font-size: 28rpx;
  color: var(--color-primary);
  font-weight: 500;
  flex-shrink: 0;
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    background-color 0.15s;

  &:active {
    transform: scale(0.94);
    background: var(--bg-active);
  }

  &.active {
    background: var(--color-primary);
    color: #ffffff;
    font-weight: 600;
  }
}

.segmented-bar {
  padding: 16rpx 32rpx 8rpx;
  background: var(--bg-primary);
}

:deep(.tab-custom.wd-tabs) {
  background: transparent !important;
  --wot-tabs-nav-bg: transparent;
  --wot-tabs-nav-color: var(--text-tertiary);
  --wot-tabs-nav-color-active: var(--text-primary);
  --wot-tabs-nav-font-weight-active: 600;
  --wot-tabs-nav-item-font-size: 26rpx;
  --wot-tabs-nav-line-bg: transparent;

  .wd-tabs__nav {
    box-sizing: border-box !important;
    width: 100% !important;
    background: var(--bg-seg-track) !important;
    border-radius: 18rpx !important;
    padding: 4rpx !important;
  }

  .wd-tabs__nav-item {
    box-sizing: border-box !important;
    flex: 1;
    height: 56rpx !important;
    line-height: 56rpx !important;
    padding: 0 16rpx !important;
    border-radius: 14rpx !important;
    transition: all 0.2s var(--spring-default);

    &.is-active {
      background: var(--bg-seg-thumb);
      color: var(--text-primary) !important;
      font-weight: 600;
      box-shadow: var(--shadow-seg);
    }
  }

  .wd-tabs__line {
    display: none !important;
  }
}

.category-list-wrap {
  flex: 1;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 24rpx 32rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: var(--text-tertiary);
  }
}

.category-list {
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: var(--bg-secondary);
  border-bottom: 1rpx solid var(--border-subtle);
  transition:
    opacity 0.2s,
    background-color 0.15s;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: var(--bg-overlay);
  }

  &.dimmed {
    opacity: 0.45;
  }

  &.is-dragging {
    background: var(--bg-card);
    box-shadow: 0 16rpx 36rpx rgba(0, 0, 0, 0.16);
    opacity: 0.96;
    border-radius: var(--radius-md);
    position: relative;
    transition: none !important;
  }
}

.sort-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;

  .sort-btn {
    width: 52rpx;
    height: 52rpx;
    border-radius: 50%;
    background: var(--bg-tertiary);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.15s ease, background 0.15s ease;

    &:active:not(.disabled) {
      transform: scale(0.88);
      background: var(--border-focus);
    }

    &.disabled {
      opacity: 0.3;
      pointer-events: none;
    }
  }
}

.cat-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
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
  cursor: grab;
}

.cat-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);

  text {
    font-size: 36rpx;
    color: #ffffff;
    line-height: 1;
  }
}

.cat-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  overflow: hidden;

  .cat-title-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .cat-name {
    font-size: 32rpx;
    font-weight: 500;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .cat-hidden-tag {
    font-size: 20rpx;
    padding: 2rpx 10rpx;
    border-radius: 8rpx;
    background: var(--bg-input-filled);
    color: var(--text-tertiary);
  }

  .cat-desc {
    font-size: 24rpx;
    color: var(--text-tertiary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.cat-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

.cat-lock {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cat-action {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-input-filled);
  cursor: pointer;
  transition: transform 0.15s;

  &:active {
    transform: scale(0.88);
  }

  &.delete {
    background: var(--color-expense-bg);
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
  background: linear-gradient(
    to top,
    var(--bg-primary) 75%,
    transparent
  );
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  height: 100rpx;
  background: var(--color-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-primary-md);
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    opacity 0.15s;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  .add-label {
    font-size: 30rpx;
    color: #ffffff;
    font-weight: 600;
    letter-spacing: 0.5rpx;
  }
}

.editor {
  background: var(--bg-secondary);
  border-top: 1rpx solid var(--border-subtle);
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
  color: var(--text-tertiary);
  cursor: pointer;
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
  cursor: pointer;
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
  background: var(--bg-input-filled);
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
  color: var(--text-tertiary);
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
  cursor: pointer;

  &.selected {
    border-color: var(--text-primary);
    transform: scale(1.15);
  }

  &:active {
    transform: scale(0.88);
  }
}
</style>
