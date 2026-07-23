<template>
  <wd-popup
    :model-value="visible"
    position="bottom"
    @update:model-value="$emit('close')"
    @close="$emit('close')"
  >
    <view class="editor">
      <view class="editor-header">
        <text class="editor-title">{{
          editId != null ? "编辑账单" : "记一笔"
        }}</text>
        <view class="editor-close" @click="$emit('close')">
          <text>✕</text>
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
          <text class="meta-icon">📅</text>
          <text class="meta-text">{{ transDate }}</text>
        </view>
        <wd-input
          v-model="remark"
          custom-class="remark-input"
          placeholder="添加备注..."
          clearable
        />
      </view>

      <NumberKeyboard @keyPress="onKeyPress" />
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
import { timestampToDateStr } from "@/utils/format";

const { add, update } = useTransactions();
const { categories } = useCategories();

const props = defineProps<{
  visible: boolean;
  bookId: number;
  editData?: {
    id: number;
    type: number;
    amount: number;
    category_id?: number;
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
const selectedCategory = ref(1);
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

const onTypeChange = (type: number) => {
  currentType.value = type;
  const cats = categories.value[type];
  selectedCategory.value = cats.length > 0 ? cats[0].id : 0;
};

const resetInput = () => {
  inputBuffer.value = "";
  expressionParts.value = [];
  remark.value = "";
};

const editId = ref<number | null>(null);

const handleSave = (keepOpen: boolean) => {
  const amount = evalExpression();
  if (amount <= 0) {
    uni.showToast({ title: "请输入金额", icon: "none" });
    return;
  }

  const amountCents = Math.round(amount * 100);
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, "0");
  const mm = String(now.getMinutes()).padStart(2, "0");
  const ss = String(now.getSeconds()).padStart(2, "0");
  const transTime = transDate.value + `T${hh}:${mm}:${ss}`;

  if (editId.value != null) {
    update(editId.value, {
      type: currentType.value,
      amount: amountCents,
      category_id: selectedCategory.value,
      remark: remark.value || undefined,
      trans_time: transTime,
    });
  } else {
    add({
      book_id: props.bookId,
      type: currentType.value,
      amount: amountCents,
      account_id: 1,
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
          0;
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
        selectedCategory.value = categories.value[0]?.[0]?.id || 1;
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
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  overflow: hidden;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 20rpx;

  .editor-title {
    font-size: 34rpx;
    font-weight: 700;
    color: var(--text-primary);
  }

  .editor-close {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: var(--bg-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: var(--shadow-sm);
    transition: transform 0.15s var(--spring-default);

    &:active {
      transform: scale(0.9);
    }

    text {
      font-size: 28rpx;
      color: var(--text-secondary);
      line-height: 1;
    }
  }
}

.type-tabs {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 16rpx;
}

.type-tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: var(--radius-sm);
  font-size: 26rpx;
  font-weight: 500;
  color: var(--text-primary);
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s var(--spring-default);

  &.active {
    background: var(--color-primary);
    color: var(--text-inverse);
    font-weight: 600;
    box-shadow: var(--shadow-primary-sm);
  }
}

.category-area {
  padding: 4rpx 0 8rpx;
}

.amount-area {
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 24rpx 40rpx 8rpx;
  min-height: 140rpx;
  gap: 4rpx;

  .amount-sign {
    font-size: 56rpx;
    font-weight: 300;
    color: var(--text-muted);
    margin-right: -4rpx;
  }

  .amount-number {
    font-size: 64rpx;
    font-weight: 300;
    text-align: right;
    color: var(--text-primary);
    letter-spacing: 2rpx;
    line-height: 1.1;
    word-break: break-all;
  }
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding-bottom: 12rpx;

  .meta-btn {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 24rpx;
    background: var(--bg-secondary);
    box-shadow: var(--shadow-sm);
    font-size: 24rpx;
    font-weight: 500;
    color: var(--text-secondary);
    flex-shrink: 0;
    transition: all 0.15s var(--spring-default);

    &:active {
      transform: scale(0.94);
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
      --wd-input-border: transparent;
      --wd-input-color: var(--text-secondary);
      --wd-input-placeholder: var(--text-placeholder);
      --wd-input-radius: var(--radius-sm);
      box-shadow: var(--shadow-sm);
      border-radius: var(--radius-sm);
    }
  }
}
</style>
