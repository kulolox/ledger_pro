<template>
  <view
    class="category-page"
    :class="['theme-' + themeMode]"
    :data-theme="themeMode"
  >
    <NavBar>
      <template #left>
        <view class="nav-back" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">分类管理</text>
      </template>
    </NavBar>

    <view class="segmented-bar">
      <wd-tabs v-model="currentTab" custom-class="tab-custom">
        <wd-tab title="支出" />
        <wd-tab title="收入" />
      </wd-tabs>
    </view>

    <view class="category-list-wrap">
      <!-- 可见分类主卡片 -->
      <view v-if="visibleCategoryList.length === 0" class="empty-state">
        <text class="empty-text">暂无可见分类</text>
      </view>

      <view v-else class="category-list" id="category-sort-list">
        <view
          v-for="(cat, index) in visibleCategoryList"
          :key="cat.id"
          class="category-item"
          :class="{
            'is-dragging': draggingIndex === index,
          }"
          :style="
            draggingIndex === index
              ? { transform: `translateY(${dragOffsetY}px)`, zIndex: 99 }
              : {}
          "
          :data-id="cat.id"
        >
          <view class="cat-left">
            <view
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
              </view>
              <text class="cat-desc">{{ getCategoryDesc(cat) }}</text>
            </view>
          </view>
          <view class="cat-right">
            <template v-if="!isSystem(cat)">
              <view class="cat-action delete" @click="onDelete(cat)">
                <wd-icon
                  name="delete"
                  size="30rpx"
                  color="var(--color-expense)"
                />
              </view>
              <view class="cat-action edit" @click="onEdit(cat)">
                <wd-icon
                  name="edit"
                  size="30rpx"
                  color="var(--text-secondary)"
                />
              </view>
            </template>
            <view v-else class="cat-lock">
              <wd-icon name="lock-on" size="28rpx" color="var(--text-dim)" />
            </view>
            <view class="cat-action visibility" @click="onToggleVisible(cat)">
              <wd-icon
                name="eye"
                size="34rpx"
                color="var(--color-primary)"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 已隐藏分类手风琴卡片 -->
      <view
        v-if="hiddenCategoryList.length > 0"
        class="hidden-section"
      >
        <view class="hidden-section-header" @click="toggleHiddenExpand">
          <view class="hidden-title-group">
            <text class="hidden-title">已隐藏分类</text>
            <text class="hidden-badge">{{ hiddenCategoryList.length }}</text>
          </view>
          <wd-icon
            name="down"
            size="30rpx"
            color="var(--text-tertiary)"
            class="hidden-expand-icon"
            :class="{ expanded: hiddenExpanded }"
          />
        </view>

        <view v-if="hiddenExpanded" class="hidden-list">
          <view
            v-for="cat in hiddenCategoryList"
            :key="cat.id"
            class="category-item dimmed"
            :data-id="cat.id"
          >
            <view class="cat-left">
              <view class="cat-icon" :style="{ background: cat.color }">
                <text>{{ cat.icon }}</text>
              </view>
              <view class="cat-info">
                <view class="cat-title-row">
                  <text class="cat-name">{{ cat.name }}</text>
                  <text class="cat-hidden-tag">已隐藏</text>
                </view>
                <text class="cat-desc">{{ getCategoryDesc(cat) }}</text>
              </view>
            </view>
            <view class="cat-right">
              <template v-if="!isSystem(cat)">
                <view class="cat-action delete" @click="onDelete(cat)">
                  <wd-icon
                    name="delete"
                    size="30rpx"
                    color="var(--color-expense)"
                  />
                </view>
                <view class="cat-action edit" @click="onEdit(cat)">
                  <wd-icon
                    name="edit"
                    size="30rpx"
                    color="var(--text-secondary)"
                  />
                </view>
              </template>
              <view v-else class="cat-lock">
                <wd-icon name="lock-on" size="28rpx" color="var(--text-dim)" />
              </view>
              <view class="cat-action visibility" @click="onToggleVisible(cat)">
                <wd-icon
                  name="eye-invisible"
                  size="34rpx"
                  color="var(--text-tertiary)"
                />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="add-bar">
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
                placeholder="#FF4D4F"
                maxlength="7"
              />
              <view
                class="color-preview"
                :style="{ background: form.color || '#ccc' }"
                @click="openColorPicker"
              />
            </view>
          </view>

          <view class="color-presets">
            <view class="preset-grid">
              <view
                v-for="c in presetColors"
                :key="c"
                class="preset-item"
                :class="{
                  selected: form.color.toUpperCase() === c.toUpperCase(),
                }"
                :style="{ background: c }"
                @click="form.color = c"
              />
              <!-- 自定义调色盘触发项 -->
              <view
                class="preset-item custom-palette-item"
                :class="{ selected: isCustomColor }"
                @click="openColorPicker"
              >
                <view
                  class="custom-palette-inner"
                  :style="isCustomColor ? { background: form.color } : {}"
                >
                  <wd-icon
                    name="fill"
                    size="28rpx"
                    :color="isCustomColor ? '#ffffff' : 'var(--text-secondary)'"
                  />
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </wd-popup>

    <!-- 分类删除与账单归档处理弹窗 -->
    <wd-popup
      :model-value="deleteModalVisible"
      position="bottom"
      custom-class="delete-modal-popup"
      custom-style="background: transparent;"
      @update:model-value="deleteModalVisible = false"
    >
      <view class="delete-modal">
        <view class="modal-header">
          <text class="modal-title">删除分类</text>
        </view>

        <view class="modal-body">
          <!-- 当前被删除分类提示卡片 -->
          <view class="source-cat-card">
            <view
              class="source-cat-badge"
              :style="{ background: pendingDeleteCat?.color || '#ccc' }"
            >
              <text class="source-cat-icon">{{ pendingDeleteCat?.icon }}</text>
            </view>
            <view class="source-cat-info">
              <text class="source-cat-name">{{ pendingDeleteCat?.name }}</text>
              <text class="source-cat-count">
                该分类下有关联账单共
                <text class="highlight-count">{{ pendingDeleteTxCount }}</text>
                笔
              </text>
            </view>
          </view>

          <!-- 处理方式切换选项 -->
          <view class="action-tabs">
            <view
              class="action-tab-item"
              :class="{ active: deleteAction === 'migrate' }"
              @click="deleteAction = 'migrate'"
            >
              <wd-icon name="swap" size="30rpx" />
              <text class="action-tab-text">迁移归档到新分类</text>
            </view>
            <view
              class="action-tab-item danger"
              :class="{ active: deleteAction === 'delete_all' }"
              @click="deleteAction = 'delete_all'"
            >
              <wd-icon name="delete" size="30rpx" />
              <text class="action-tab-text">连同账单一起删除</text>
            </view>
          </view>

          <!-- 模式一：迁移归档列表 -->
          <view v-if="deleteAction === 'migrate'" class="migrate-section">
            <view class="section-title">请选择目标分类：</view>
            <scroll-view scroll-y class="target-cat-scroll">
              <view class="target-cat-grid">
                <view
                  v-for="target in targetCategoryOptions"
                  :key="target.id"
                  class="target-cat-item"
                  :class="{ selected: String(selectedTargetId) === String(target.id) }"
                  @click="selectedTargetId = String(target.id)"
                >
                  <view
                    class="target-cat-icon-wrap"
                    :style="{ background: target.color }"
                  >
                    <text class="target-cat-icon">{{ target.icon }}</text>
                  </view>
                  <text class="target-cat-name">{{ target.name }}</text>
                  <view
                    v-if="String(selectedTargetId) === String(target.id)"
                    class="check-badge"
                  >
                    <wd-icon name="check" size="20rpx" color="#ffffff" />
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>

          <!-- 模式二：连同账单删除警告 -->
          <view v-else class="delete-all-section">
            <view class="warning-box">
              <wd-icon name="warn-bold" size="48rpx" color="#ff4d4f" />
              <view class="warning-content">
                <text class="warning-title">不可撤销的危险操作</text>
                <text class="warning-desc">
                  该分类下的全部
                  {{
                    pendingDeleteTxCount
                  }}
                  笔历史账单将被同时删除，无法自动恢复！
                </text>
              </view>
            </view>
          </view>

          <!-- 底部操作按钮 -->
          <view class="modal-footer">
            <view
              v-if="deleteAction === 'migrate'"
              class="submit-btn"
              :class="{ disabled: !selectedTargetId || deleteLoading }"
              @click="confirmDeleteWithAction"
            >
              <text class="submit-btn-text">
                {{ selectedTargetId ? "确认归档并删除" : "请先选择目标分类" }}
              </text>
            </view>
            <view
              v-else
              class="submit-btn danger-btn"
              :class="{ disabled: deleteLoading }"
              @click="confirmDeleteWithAction"
            >
              <text class="submit-btn-text">
                连同 {{ pendingDeleteTxCount }} 笔账单彻底删除
              </text>
            </view>
          </view>
        </view>
      </view>
    </wd-popup>

    <!-- 自定义调色盘组件 -->
    <ColorPicker v-model="form.color" v-model:visible="colorPickerVisible" />

    <wd-dialog selector="category-delete" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import ColorPicker from "@/components/ColorPicker.vue";
import { useDialog } from "@wot-ui/ui";
import { useCategories } from "@/stores/categoryStore";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";
import type { Category } from "@/constants/categories";
import { getCategoryTransactionCount } from "@/api/common";

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
const hiddenExpanded = ref(true);

function toggleHiddenExpand() {
  hiddenExpanded.value = !hiddenExpanded.value;
}

const allCategoryList = computed(() => {
  return categories.value[currentTab.value] || [];
});

const visibleCategoryList = computed(() => {
  return allCategoryList.value.filter((cat) => cat.visible !== 0);
});

const hiddenCategoryList = computed(() => {
  return allCategoryList.value.filter((cat) => cat.visible === 0);
});

watch(currentTab, () => {
  draggingIndex.value = -1;
  dragOffsetY.value = 0;
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
  return (
    categoryDescMap[cat.name] || (isSystem(cat) ? "系统预设分类" : "自定义分类")
  );
}

function isSystem(cat: Category): boolean {
  return cat.is_system === 1;
}

// 7 个高频高区分度预设色彩
const presetColors = [
  "#FF4D4F", // 活力红 (餐饮、消费重头)
  "#FF7A45", // 暖阳橙 (生活、购物日常)
  "#FAAD14", // 金秋黄 (休闲、娱乐消遣)
  "#52C41A", // 清新绿 (居住、健康日用)
  "#1677FF", // 商务蓝 (交通、通讯数码)
  "#722ED1", // 优雅紫 (医疗、教育人情)
  "#8C8C8C", // 沉稳灰 (其他、杂项备用)
];

const colorPickerVisible = ref(false);

const isCustomColor = computed(() => {
  if (!form.value.color) return false;
  const currentUpper = form.value.color.toUpperCase();
  return !presetColors.some((c) => c.toUpperCase() === currentUpper);
});

function openColorPicker() {
  colorPickerVisible.value = true;
}

const editorVisible = ref(false);
const isEdit = ref(false);
const editingId = ref<string>("");
const form = ref({ name: "", icon: "", color: "" });
const dialog = useDialog("category-delete");

// 删除关联弹窗状态
const deleteModalVisible = ref(false);
const pendingDeleteCat = ref<Category | null>(null);
const pendingDeleteTxCount = ref(0);
const deleteAction = ref<"migrate" | "delete_all">("migrate");
const selectedTargetId = ref<string>("");
const deleteLoading = ref(false);

const targetCategoryOptions = computed(() => {
  if (!pendingDeleteCat.value) return [];
  const currentType = pendingDeleteCat.value.type;
  const list = categories.value[currentType] || [];
  return list.filter((c) => c.id !== pendingDeleteCat.value?.id);
});

// 拖拽手势状态
const draggingIndex = ref<number>(-1);
const dragOffsetY = ref<number>(0);
let startTouchY = 0;
let initialOrderIds: string[] = [];
const ITEM_HEIGHT = 64; // px

function onTouchStart(e: any, index: number) {
  if (visibleCategoryList.value.length < 2) return;
  const touch = e.touches?.[0] || e.changedTouches?.[0];
  if (!touch) return;
  draggingIndex.value = index;
  startTouchY = touch.clientY;
  dragOffsetY.value = 0;
  initialOrderIds = (categories.value[currentTab.value] || []).map((c) => c.id);
  // #ifdef APP-PLUS
  try {
    uni.vibrateShort({ success: () => {} });
  } catch {}
  // #endif
}

function onTouchMove(e: any, index: number) {
  if (draggingIndex.value < 0) return;
  const touch = e.touches?.[0] || e.changedTouches?.[0];
  if (!touch) return;
  const currentY = touch.clientY;
  const delta = currentY - startTouchY;
  dragOffsetY.value = delta;

  const list = [...visibleCategoryList.value];
  const curIdx = draggingIndex.value;

  // 向下滑动跨过一项
  if (delta > 32 && curIdx < list.length - 1) {
    const [moved] = list.splice(curIdx, 1);
    list.splice(curIdx + 1, 0, moved);
    categories.value[currentTab.value] = [...list, ...hiddenCategoryList.value];
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
    categories.value[currentTab.value] = [...list, ...hiddenCategoryList.value];
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
  if (draggingIndex.value < 0) return;
  draggingIndex.value = -1;
  dragOffsetY.value = 0;

  const currentList = categories.value[currentTab.value] || [];
  const currentOrderIds = currentList.map((c) => c.id);
  const isChanged =
    currentOrderIds.length !== initialOrderIds.length ||
    currentOrderIds.some((id, i) => id !== initialOrderIds[i]);

  if (isChanged) {
    saveOrder(currentList);
  }
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
  form.value = { name: "", icon: "", color: presetColors[0] };
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
  try {
    uni.showLoading({ title: "检查中..." });
    const res = await getCategoryTransactionCount(cat.id);
    uni.hideLoading();

    const count = Number(res?.count ?? 0);
    if (count === 0) {
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
    } else {
      pendingDeleteCat.value = cat;
      pendingDeleteTxCount.value = count;
      deleteAction.value = "migrate";
      selectedTargetId.value = "";
      deleteModalVisible.value = true;
    }
  } catch (err: any) {
    uni.hideLoading();
    uni.showToast({ title: err?.message || "检查分类失败", icon: "none" });
  }
}

async function confirmDeleteWithAction() {
  if (!pendingDeleteCat.value) return;
  const cat = pendingDeleteCat.value;

  if (deleteAction.value === "migrate") {
    if (!selectedTargetId.value) {
      uni.showToast({ title: "请选择目标分类", icon: "none" });
      return;
    }
    try {
      deleteLoading.value = true;
      await removeCategory(cat.id, {
        targetCategoryId: selectedTargetId.value,
      });
      deleteModalVisible.value = false;
      uni.showToast({ title: "归档并删除成功", icon: "success" });
    } catch (err: any) {
      uni.showToast({ title: err?.message || "操作失败", icon: "none" });
    } finally {
      deleteLoading.value = false;
    }
  } else if (deleteAction.value === "delete_all") {
    const result = await dialog.confirm({
      title: "危险警示",
      msg: `确定要连同该分类下的 ${pendingDeleteTxCount.value} 笔账单一起彻底删除吗？删除后数据将无法恢复！`,
      zIndex: 1000,
    });
    if (result.action === "confirm") {
      try {
        deleteLoading.value = true;
        await removeCategory(cat.id, {
          deleteTransactions: true,
        });
        deleteModalVisible.value = false;
        uni.showToast({ title: "分类及账单已删除", icon: "success" });
      } catch (err: any) {
        uni.showToast({ title: err?.message || "删除失败", icon: "none" });
      } finally {
        deleteLoading.value = false;
      }
    }
  }
}

async function onToggleVisible(cat: Category) {
  const isHiding = cat.visible !== 0;

  if (isHiding) {
    if (visibleCategoryList.value.length <= 1) {
      uni.showToast({ title: "至少保留一个可见分类", icon: "none" });
      return;
    }

    const otherVisible = visibleCategoryList.value.filter((c) => c.id !== cat.id);
    const updatedCat = { ...cat, visible: 0 };
    const newFullList = [...otherVisible, ...hiddenCategoryList.value, updatedCat];
    categories.value[currentTab.value] = newFullList;

    try {
      await toggleVisible(cat.id, 0);
      await saveOrder(newFullList);
      uni.showToast({ title: "已隐藏", icon: "none" });
    } catch {
      uni.showToast({ title: "操作失败", icon: "error" });
    }
  } else {
    const otherHidden = hiddenCategoryList.value.filter((c) => c.id !== cat.id);
    const updatedCat = { ...cat, visible: 1 };
    const newFullList = [...visibleCategoryList.value, updatedCat, ...otherHidden];
    categories.value[currentTab.value] = newFullList;

    try {
      await toggleVisible(cat.id, 1);
      await saveOrder(newFullList);
      uni.showToast({ title: "已恢复显示", icon: "none" });
    } catch {
      uni.showToast({ title: "操作失败", icon: "error" });
    }
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

.hidden-section {
  margin-top: 24rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;

  .hidden-section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 32rpx;
    cursor: pointer;
    background: var(--bg-secondary);
    transition: background-color 0.15s;

    &:active {
      background: var(--bg-hover);
    }

    .hidden-title-group {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .hidden-title {
        font-size: 28rpx;
        font-weight: 600;
        color: var(--text-secondary);
      }

      .hidden-badge {
        font-size: 22rpx;
        padding: 2rpx 14rpx;
        border-radius: var(--radius-full);
        background: var(--bg-input-filled);
        color: var(--text-tertiary);
        font-weight: 500;
      }
    }

    .hidden-expand-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.25s var(--spring-default);

      &.expanded {
        transform: rotate(180deg);
      }
    }
  }

  .hidden-list {
    border-top: 1rpx solid var(--border-subtle);
  }
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
  touch-action: none;
  transition: opacity 0.15s;

  &:active {
    cursor: grabbing;
    opacity: 0.7;
  }
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
  background: linear-gradient(to top, var(--bg-primary) 75%, transparent);
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
  cursor: pointer;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.92);
  }
}

.color-presets {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.preset-label-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.preset-label {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-secondary);
}

.preset-sub {
  font-size: 20rpx;
  color: var(--text-dim);
}

.preset-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
  align-items: center;
}

.preset-item {
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  transition: transform 0.15s var(--spring-bounce);
  border: 4rpx solid transparent;
  cursor: pointer;
  box-sizing: border-box;

  &.selected {
    border-color: var(--text-primary);
    transform: scale(1.15);
  }

  &:active {
    transform: scale(0.88);
  }
}

.custom-palette-item {
  background: conic-gradient(
    from 0deg,
    #ff4d4f,
    #faad14,
    #52c41a,
    #13c2c2,
    #1677ff,
    #722ed1,
    #ff4d4f
  );
  padding: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  .custom-palette-inner {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    background: var(--bg-card);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s ease;
  }
}

/* 分类删除与归档弹窗样式 */
.delete-modal {
  background: var(--bg-card);
  border-radius: 36rpx 36rpx 0 0;
  padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}

.modal-cancel {
  font-size: 28rpx;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: 8rpx 12rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.modal-placeholder {
  width: 60rpx;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.source-cat-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: var(--bg-primary);
  padding: 20rpx 24rpx;
  border-radius: var(--radius-lg);
  border: 1rpx solid var(--border-light);
}

.source-cat-badge {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.source-cat-icon {
  font-size: 36rpx;
}

.source-cat-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  flex: 1;
}

.source-cat-name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.source-cat-count {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.highlight-count {
  color: var(--color-expense);
  font-weight: 700;
  padding: 0 4rpx;
}

.action-tabs {
  display: flex;
  background: var(--bg-seg-track);
  border-radius: var(--radius-md);
  padding: 6rpx;
  gap: 8rpx;
}

.action-tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 16rpx 12rpx;
  border-radius: calc(var(--radius-md) - 4rpx);
  font-size: 26rpx;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;

  &.active {
    background: var(--bg-card);
    color: var(--color-primary);
    font-weight: 600;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  }

  &.danger.active {
    color: var(--color-expense);
  }
}

.migrate-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-secondary);
}

.target-cat-scroll {
  max-height: 380rpx;
}

.target-cat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  padding: 6rpx 4rpx;
}

.target-cat-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18rpx 10rpx;
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  border: 2rpx solid transparent;
  cursor: pointer;
  transition: all 0.15s var(--spring-default);

  &:active {
    transform: scale(0.94);
  }

  &.selected {
    border-color: var(--color-primary);
    background: var(--bg-active);
  }
}

.target-cat-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}

.target-cat-icon {
  font-size: 32rpx;
}

.target-cat-name {
  font-size: 24rpx;
  color: var(--text-primary);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.check-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-all-section {
  padding: 16rpx 0;
}

.warning-box {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  background: rgba(255, 77, 79, 0.08);
  border: 1rpx solid rgba(255, 77, 79, 0.2);
  border-radius: var(--radius-md);
  padding: 24rpx;
}

.warning-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
}

.warning-title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--color-expense);
}

.warning-desc {
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.5;
}

.modal-footer {
  margin-top: 12rpx;
}

.submit-btn {
  height: 84rpx;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  &.disabled {
    background: var(--bg-seg-track);
    cursor: not-allowed;
    opacity: 0.6;

    .submit-btn-text {
      color: var(--text-dim);
    }
  }

  &.danger-btn {
    background: var(--color-expense);
  }
}

.submit-btn-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #ffffff;
}
</style>
