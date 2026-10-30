<template>
  <wd-popup
    :model-value="visible"
    position="bottom"
    :z-index="500"
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
          <wd-icon name="close" size="24rpx" color="var(--text-secondary)" />
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
        <text class="amount-currency">¥</text>
        <text class="amount-number">{{ rawInput || "0.00" }}</text>
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

    <wd-datetime-picker
      v-model="transTimestamp"
      v-model:visible="showDatePicker"
      type="date"
    />
  </wd-popup>
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import NumberKeyboard from "@/components/NumberKeyboard.vue";
import CategorySlider from "@/components/CategorySlider.vue";
import { useTransactions } from "@/stores/transactionStore";
import { useCategories } from "@/stores/categoryStore";
import { timestampToDateStr, yuanToCents } from "@/utils/format";

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

const currentCategories = computed(
  () => (categories.value[currentType.value] || []).filter((c) => c.visible !== 0),
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
  inputBuffer.value += key;
};

// 长按保存键：弹出 ActionSheet
const onKeyLongPress = async (key: string) => {
  if (key !== "save") return;
  const amount = evalExpression();
  if (amount <= 0) {
    uni.showToast({ title: "请输入金额", icon: "none" });
    return;
  }
  uni.showActionSheet({
    itemList: ["保存并关闭", "保存并再记一笔"],
    success: (res) => {
      if (res.tapIndex === 0) handleSave(false);
      else if (res.tapIndex === 1) handleSave(true);
    },
  });
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

const handleSave = (keepOpen: boolean) => {
  const amount = evalExpression();
  if (amount <= 0) {
    uni.showToast({ title: "请输入金额", icon: "none" });
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
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
  border-top: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  overflow: hidden;
}

.sheet-grabber {
  width: 72rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: var(--border-primary);
  margin: 18rpx auto 0;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 32rpx 20rpx;

  .editor-title {
    font-size: 32rpx;
    font-weight: 700;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
  }

  .editor-close {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: var(--bg-secondary);
    border: 1rpx solid var(--border-subtle);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: var(--shadow-sm);
    transition: transform 0.15s var(--spring-default);

    &:active {
      transform: scale(0.9);
    }
  }
}

.type-tabs {
  display: flex;
  gap: 8rpx;
  margin: 0 32rpx 16rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 6rpx;
}

.type-tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: var(--radius-sm);
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-secondary);
  background: transparent;
  transition: all 0.2s var(--spring-default);

  &.active {
    background: var(--color-primary);
    color: var(--text-inverse);
    font-weight: 600;
    box-shadow: var(--shadow-sm);
  }
}

.category-area {
  padding: 4rpx 0 8rpx;
}

.amount-area {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  padding: 16rpx 40rpx 12rpx;
  min-height: 120rpx;
  gap: 8rpx;

  .amount-currency {
    font-size: 40rpx;
    font-weight: 600;
    color: var(--color-primary);
  }

  .amount-number {
    font-size: 64rpx;
    font-weight: 700;
    text-align: right;
    color: var(--text-primary);
    letter-spacing: -0.5rpx;
    line-height: 1.1;
    word-break: break-all;
    font-variant-numeric: tabular-nums;
  }
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 32rpx 16rpx;

  .meta-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 20rpx 24rpx;
    background: var(--bg-secondary);
    border: 1rpx solid var(--border-subtle);
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-sm);
    font-size: 24rpx;
    font-weight: 500;
    color: var(--text-secondary);
    flex-shrink: 0;
    transition: all 0.15s var(--spring-default);

    &:active {
      transform: scale(0.96);
    }

    &.active {
      color: var(--color-primary);
    }

    .meta-icon {
      font-size: 24rpx;
    }

    .meta-text {
      font-size: 24rpx;
    }
  }

  :deep(.remark-input) {
    &.wd-input {
      flex: 1;
      --wd-input-bg: var(--bg-secondary);
      --wd-input-border: var(--border-subtle);
      --wd-input-color: var(--text-primary);
      --wd-input-placeholder: var(--text-placeholder);
      --wd-input-radius: var(--radius-md);
      box-shadow: var(--shadow-sm);
      border-radius: var(--radius-md);
    }
  }
}
</style>
