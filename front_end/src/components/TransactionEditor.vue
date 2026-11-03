<template>
  <view class="transaction-editor-wrapper">
    <wd-popup
      :model-value="visible"
      position="bottom"
      :z-index="500"
      custom-class="editor-popup"
      custom-style="background: transparent;"
      @update:model-value="$emit('close')"
      @close="$emit('close')"
    >
      <view class="editor">
        <view class="sheet-grabber" />

        <view class="editor-header">
          <text class="editor-title">{{
            editId != null ? "编辑账单" : "记一笔"
          }}</text>
          <view class="editor-close" @click="$emit('close')">
            <wd-icon name="close" size="26rpx" color="var(--text-tertiary)" />
          </view>
        </view>

        <view class="type-tabs">
          <view
            v-for="tab in typeTabs"
            :key="tab.value"
            class="type-tab"
            :class="{ active: currentType === tab.value }"
            @click="onTypeChange(tab.value)"
          >
            <text>{{ tab.label }}</text>
          </view>
        </view>

        <view class="category-area">
          <CategorySlider
            v-model="selectedCategory"
            :categories="currentCategories"
          />
        </view>

        <view class="amount-area">
          <text class="amount-number">{{ rawInput || "0" }}</text>
        </view>

        <view class="meta-row">
          <view class="meta-btn" @click="showDatePicker = true">
            <wd-icon name="calendar" size="28rpx" color="var(--color-primary)" />
            <text class="meta-text">{{ transDate }}</text>
          </view>
          <wd-input
            v-model="remark"
            custom-class="remark-input"
            placeholder="添加备注..."
            clearable
          />
        </view>

        <NumberKeyboard @keyPress="onKeyPress" @keyLongPress="onKeyLongPress" />
      </view>
    </wd-popup>

    <wd-datetime-picker
      v-model="transTimestamp"
      v-model:visible="showDatePicker"
      type="date"
      title="选择日期"
      :z-index="600"
    />

    <wd-action-sheet
      v-model="showSaveSheet"
      :actions="saveActions"
      cancel-text="取消"
      :z-index="1200"
      custom-class="save-action-sheet"
      @select="onSelectSaveAction"
      @cancel="showSaveSheet = false"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import NumberKeyboard from "@/components/NumberKeyboard.vue";
import CategorySlider from "@/components/CategorySlider.vue";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { timestampToDateStr, yuanToCents, MAX_AMOUNT_YUAN } from "@/utils/format";
import { updateTransaction as apiUpdateTransaction } from "@/api/common";

const { add, update } = useTransactions();
const { categories } = useCategories();

const props = defineProps<{
  visible: boolean;
  bookId: string;
  editData?: {
    id: string;
    type: number;
    amount: number;
    category_id?: string;
    account_id?: string;
    to_account_id?: string;
    remark?: string;
    trans_time?: string;
  } | null;
}>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const typeTabs = [
  { label: "支出", value: 0 },
  { label: "收入", value: 1 },
];

const currentType = ref(0);
// 最后一步运算数暂存
const inputBuffer = ref("");
// 历史运算数数组
const expressionParts = ref<string[]>([]);
const selectedCategory = ref("");
const remark = ref("");
const transTimestamp = ref(Date.now());
const transDate = computed(() => timestampToDateStr(transTimestamp.value));
const showDatePicker = ref(false);

const currentCategories = computed(() =>
  (categories.value[currentType.value] || []).filter((c) => c.visible !== 0),
);

const rawInput = computed(() => {
  if (!inputBuffer.value && expressionParts.value.length === 0) return "";
  if (expressionParts.value.length === 0) return inputBuffer.value;
  return (
    expressionParts.value.join("") +
    (inputBuffer.value ? " " + inputBuffer.value : "")
  );
});

function evalExpression(): number {
  if (expressionParts.value.length === 0) {
    return parseFloat(inputBuffer.value) || 0;
  }
  const parts = [...expressionParts.value];
  if (inputBuffer.value) {
    parts.push(inputBuffer.value);
  }
  let result = parseFloat(parts[0]) || 0;
  for (let i = 1; i < parts.length; i += 2) {
    const op = parts[i];
    const num = parseFloat(parts[i + 1]) || 0;
    if (op === "+") result += num;
    else if (op === "-") result -= num;
  }
  return Math.round(result * 100) / 100;
}

// 虚拟按键事件
const onKeyPress = (key: string) => {
  // 保存
  if (key === "save") {
    handleSave(false);
    return;
  }
  // 清空
  if (key === "clear") {
    inputBuffer.value = "";
    expressionParts.value = [];
    return;
  }
  // 删除一项
  if (key === "backspace") {
    if (inputBuffer.value.length > 0) {
      inputBuffer.value = inputBuffer.value.slice(0, -1);
    } else if (expressionParts.value.length > 0) {
      inputBuffer.value = expressionParts.value.pop()!;
    }
    return;
  }
  // 加减
  if (["+", "-"].includes(key)) {
    if (inputBuffer.value) {
      expressionParts.value.push(inputBuffer.value);
      expressionParts.value.push(key);
      inputBuffer.value = "";
    } else if (expressionParts.value.length > 0) {
      expressionParts.value[expressionParts.value.length - 1] = key;
    }
    return;
  }
  // 小数点
  if (key === ".") {
    if (inputBuffer.value.includes(".")) return;
    if (!inputBuffer.value) {
      inputBuffer.value = "0.";
    } else {
      inputBuffer.value += ".";
    }
    return;
  }
  // 小数位最多限制 2 位
  if (inputBuffer.value.includes(".")) {
    const dec = inputBuffer.value.split(".")[1];
    if (dec && dec.length >= 2) return;
  } else {
    // 整数位限制不超过 8 位（最大支持 2000 万元）
    if (inputBuffer.value.length >= 8) return;
  }
  inputBuffer.value += key;
};

const showSaveSheet = ref(false);

const saveActions = computed(() => [
  {
    name: "保存并关闭",
    color: "var(--text-primary)",
  },
  {
    name: "保存并再记一笔",
    color: "var(--color-primary)",
  },
]);

// 长按保存键：弹出 ActionSheet
const onKeyLongPress = (key: string) => {
  if (key !== "save") return;
  const amount = evalExpression();
  if (amount <= 0) {
    uni.showToast({ title: "请输入金额", icon: "none" });
    return;
  }
  if (amount > MAX_AMOUNT_YUAN) {
    uni.showToast({
      title: "单笔金额不能超过 2000 万元",
      icon: "none",
    });
    return;
  }
  showSaveSheet.value = true;
};

const onSelectSaveAction = ({ index }: { index: number }) => {
  showSaveSheet.value = false;
  if (index === 0) {
    handleSave(false);
  } else if (index === 1) {
    handleSave(true);
  }
};

const onTypeChange = (type: number) => {
  currentType.value = type;
  const cats = categories.value[type];
  selectedCategory.value = cats.length > 0 ? cats[0].id : "";
};

const resetInput = () => {
  inputBuffer.value = "";
  expressionParts.value = [];
  remark.value = "";
};

const editId = ref<string | null>(null);

const handleSave = async (keepOpen: boolean) => {
  const amount = evalExpression();
  if (amount <= 0) {
    uni.showToast({ title: "请输入金额", icon: "none" });
    return;
  }
  if (amount > MAX_AMOUNT_YUAN) {
    uni.showToast({
      title: "单笔金额不能超过 2000 万元",
      icon: "none",
    });
    return;
  }

  const amountCents = yuanToCents(amount);
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, "0");
  const mm = String(now.getMinutes()).padStart(2, "0");
  const ss = String(now.getSeconds()).padStart(2, "0");
  const transTime = transDate.value + `T${hh}:${mm}:${ss}`;

  const accountId = "1";

  if (editId.value != null) {
    update(editId.value, {
      type: currentType.value,
      amount: amountCents,
      category_id: selectedCategory.value,
      account_id: accountId,
      remark: remark.value || undefined,
      trans_time: transTime,
    });
    if (!String(editId.value).startsWith("-")) {
      try {
        await apiUpdateTransaction(String(editId.value), {
          book_id: props.bookId,
          type: currentType.value,
          amount: amountCents,
          account_id: accountId,
          category_id: selectedCategory.value,
          remark: remark.value || undefined,
          trans_time: transTime,
        });
      } catch (e) {
        console.warn("apiUpdateTransaction failed:", e);
      }
    }
  } else {
    add({
      book_id: props.bookId,
      type: currentType.value,
      amount: amountCents,
      account_id: accountId,
      category_id: selectedCategory.value,
      remark: remark.value || undefined,
      trans_time: transTime,
    });
  }

  uni.vibrateShort();

  if (keepOpen) {
    editId.value = null;
    resetInput();
    uni.showToast({ title: "已保存，继续记", icon: "success" });
  } else {
    uni.showToast({ title: "记账成功", icon: "success" });
    emit("saved");
    emit("close");
  }
};

watch(
  () => props.visible,
  (val) => {
    if (val) {
      resetInput();
      showDatePicker.value = false;

      if (props.editData) {
        editId.value = props.editData.id;
        currentType.value = props.editData.type;
        selectedCategory.value =
          props.editData.category_id ||
          categories.value[props.editData.type]?.[0]?.id ||
          "";
        if (props.editData.amount > 0) {
          inputBuffer.value = String(props.editData.amount / 100);
        }
        remark.value = props.editData.remark || "";
        if (props.editData.trans_time) {
          transTimestamp.value = new Date(props.editData.trans_time).getTime();
        }
      } else {
        editId.value = null;
        currentType.value = 0;
        selectedCategory.value = categories.value[0]?.[0]?.id || "";
        transTimestamp.value = Date.now();
      }
    }
  },
);
</script>

<style lang="scss" scoped>
.editor {
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  border-top: 1rpx solid var(--border-subtle);
  border-radius: 64rpx 64rpx 0 0;
  box-shadow: 0 -16rpx 60rpx rgba(0, 0, 0, 0.12);
  overflow: hidden;
  transition: background-color 0.2s var(--spring-default);
}

.sheet-grabber {
  width: 72rpx;
  height: 10rpx;
  border-radius: 999rpx;
  background: var(--color-handle);
  margin: 20rpx auto 8rpx;
  opacity: 0.85;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 36rpx 24rpx;

  .editor-title {
    font-size: 34rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
  }

  .editor-close {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: var(--bg-tertiary);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition:
      transform 0.15s var(--spring-default),
      background-color 0.15s;

    &:active {
      transform: scale(0.9);
    }
  }
}

.type-tabs {
  display: flex;
  margin: 0 32rpx 16rpx;
  background: var(--bg-seg-track);
  border-radius: 18rpx;
  padding: 4rpx;
  box-sizing: border-box;
}

.type-tab {
  flex: 1;
  text-align: center;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 16rpx;
  box-sizing: border-box;
  border-radius: 14rpx;
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-tertiary);
  background: transparent;
  cursor: pointer;
  transition: all 0.2s var(--spring-default);

  &.active {
    background: var(--bg-seg-thumb);
    color: var(--text-primary);
    font-weight: 600;
    box-shadow: var(--shadow-seg);
  }
}

.category-area {
  padding: 4rpx 0 4rpx;
}

.amount-area {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  margin: 0 32rpx;
  padding: 8rpx 8rpx 16rpx;
  min-height: 92rpx;
  gap: 10rpx;
  border-bottom: 1rpx solid var(--border-subtle);

  .amount-currency {
    font-size: 44rpx;
    font-weight: 700;
    color: var(--color-primary);
  }

  .amount-number {
    font-size: 72rpx;
    font-weight: 700;
    text-align: right;
    color: var(--text-primary);
    letter-spacing: -1rpx;
    line-height: 1.1;
    word-break: break-all;
    font-variant-numeric: tabular-nums;
  }
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 20rpx 32rpx 12rpx;

  .meta-btn {
    display: flex;
    align-items: center;
    gap: 10rpx;
    height: 76rpx;
    box-sizing: border-box;
    padding: 0 26rpx;
    background: var(--bg-input-filled);
    border-radius: var(--radius-md);
    font-size: 26rpx;
    font-weight: 500;
    color: var(--text-secondary);
    flex-shrink: 0;
    cursor: pointer;
    transition: all 0.15s var(--spring-default);

    &:active {
      transform: scale(0.96);
    }

    .meta-text {
      font-size: 26rpx;
      font-variant-numeric: tabular-nums;
    }
  }

  :deep(.remark-input) {
    &.wd-input {
      flex: 1;
      height: 76rpx !important;
      padding: 0 24rpx !important;
      box-sizing: border-box !important;
      display: flex !important;
      align-items: center !important;
      background: var(--bg-input-filled) !important;
      --wd-input-bg: transparent;
      --wd-input-border: transparent;
      --wd-input-color: var(--text-primary);
      --wd-input-placeholder: var(--text-placeholder);
      border-radius: var(--radius-md);

      &::after {
        display: none !important;
      }

      .wd-input__inner {
        color: var(--text-primary);
        font-size: 28rpx;
        height: 76rpx !important;
        padding: 0 !important;
      }
    }
  }
}

:deep(.save-action-sheet) {
  border-radius: 36rpx 36rpx 0 0;
  overflow: hidden;
  background: var(--bg-secondary);

  .wd-action-sheet__actions {
    background: var(--bg-secondary);
  }

  .wd-action-sheet__action {
    padding: 32rpx 24rpx;
    background: var(--bg-secondary);

    // 彻底移除按钮间生硬死白的分割线
    &::after {
      display: none !important;
    }

    &:active {
      background: var(--bg-active) !important;
    }
  }

  .wd-action-sheet__name {
    font-size: 32rpx;
    font-weight: 600;
  }

  // 移除生硬的分割间隔块
  .wd-action-sheet__gap {
    height: 0 !important;
    display: none !important;
  }

  .wd-action-sheet__cancel {
    font-size: 30rpx;
    font-weight: 500;
    color: var(--text-secondary);
    background: var(--bg-secondary);
    padding: 30rpx 0;
    border-top: 1rpx solid var(--border-subtle);

    &:active {
      background: var(--bg-active) !important;
    }
  }
}
</style>
