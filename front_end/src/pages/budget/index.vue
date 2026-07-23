<template>
  <view class="budget-page">
    <NavBar>
      <template #left>
        <view class="nav-left" @click="goBack">
          <wd-icon name="left" size="24px" color="var(--text-secondary)" />
        </view>
      </template>
      <template #center>
        <view class="nav-center">
          <view class="nav-arrow" @click="prevMonth">
            <wd-icon name="left" size="24px" color="var(--text-secondary)" />
          </view>
          <text class="month-label">{{ monthLabel }}</text>
          <view class="nav-arrow" @click="nextMonth">
            <wd-icon name="right" size="24px" color="var(--text-secondary)" />
          </view>
        </view>
      </template>
      <template #right>
        <view class="nav-right" @click="onAdd">
          <wd-icon name="plus" size="28px" color="var(--color-primary)" />
        </view>
      </template>
    </NavBar>

    <scroll-view scroll-y class="budget-body">
      <view class="total-ring">
        <view class="ring-chart">
          <text class="ring-percent">{{ totalPercent }}%</text>
        </view>
        <text class="ring-label">总预算 ¥{{ formatCents(totalBudget) }}</text>
        <text class="ring-spent">已用 ¥{{ formatCents(totalSpent) }}</text>
      </view>

      <view v-if="budgets.length === 0" class="empty-state">
        <wd-icon name="dashboard" size="72rpx" color="var(--text-muted)" />
        <text class="empty-title">暂无预算</text>
        <text class="empty-hint">点击右上角 + 添加预算</text>
      </view>

      <view
        v-for="item in budgets"
        :key="item.id"
        class="budget-item"
        @click="onEdit(item)"
      >
        <view class="budget-header">
          <text class="budget-cat">{{ item.category_name || "总预算" }}</text>
          <text class="budget-amount">¥{{ formatCents(item.amount) }}</text>
        </view>
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{ width: item.percent + '%' }"
            :class="progressClass(item.percent)"
          />
        </view>
        <view class="budget-footer">
          <text class="budget-spent">已用 ¥{{ formatCents(item.spent) }}</text>
          <text v-if="item.percent >= 80" class="budget-warn">⚠️ 即将超支</text>
        </view>
      </view>
    </scroll-view>

    <wd-popup v-model="showForm" position="bottom" round>
      <view class="form-content">
        <text class="form-title">{{
          editingId ? "编辑预算" : "新增预算"
        }}</text>

        <view class="form-field">
          <text class="field-label">分类</text>
          <picker
            :value="selectedCatIndex"
            :range="categoryOptions"
            range-key="label"
            @change="onCategoryChange"
            class="field-picker"
          >
            <text class="field-value">{{
              categoryOptions[selectedCatIndex]?.label || "总预算"
            }}</text>
          </picker>
        </view>

        <view class="form-field">
          <text class="field-label">月预算金额</text>
          <input
            v-model="form.amount"
            type="digit"
            class="field-input"
            placeholder="输入预算金额"
          />
        </view>

        <view class="form-actions">
          <wd-button plain block @click="showForm = false">取消</wd-button>
          <wd-button type="primary" block :loading="saving" @click="onSave">{{
            editingId ? "保存" : "创建"
          }}</wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import NavBar from "@/components/NavBar.vue";
import {
  listBudgets,
  createBudget,
  updateBudget,
  deleteBudget,
  getCategories,
  type Category,
} from "@/api/common";
import { getCurrentBookId } from "@/utils/storage";
import { formatCents } from "@/utils/format";

const budgets = ref<any[]>([]);
const showForm = ref(false);
const editingId = ref<number | null>(null);
const saving = ref(false);
const selectedCatIndex = ref(0);
const form = ref({ amount: "" });

const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const monthLabel = computed(
  () => `${currentYear.value}年 ${currentMonth.value}月`,
);
const bookId = computed(() => getCurrentBookId());
const categories = ref<Category[]>([]);

const categoryOptions = computed(() => {
  const opts = [{ label: "总预算（不限分类）", value: null as any }];
  categories.value.forEach((c: any) => {
    if (c.type === 0) opts.push({ label: c.name, value: c.id });
  });
  return opts;
});

const totalBudget = computed(() =>
  budgets.value.reduce((s: number, b: any) => s + b.amount, 0),
);
const totalSpent = computed(() =>
  budgets.value.reduce((s: number, b: any) => s + b.spent, 0),
);
const totalPercent = computed(() =>
  totalBudget.value > 0
    ? Math.min(100, Math.round((totalSpent.value * 100) / totalBudget.value))
    : 0,
);

function progressClass(p: number): string {
  if (p >= 90) return "danger";
  if (p >= 80) return "warn";
  return "normal";
}

function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else currentMonth.value--;
  loadBudgets();
}
function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else currentMonth.value++;
  loadBudgets();
}

async function loadBudgets() {
  const month = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
  try {
    budgets.value = (await listBudgets({ book_id: bookId.value, month })).map(
      (b: any) => {
        const cat = categories.value.find((c: any) => c.id === b.category_id);
        return { ...b, category_name: cat?.name || "" };
      },
    );
  } catch {
    budgets.value = [];
  }
}

function onCategoryChange(e: any) {
  selectedCatIndex.value = e.detail?.value ?? e.target?.value ?? 0;
}

function onAdd() {
  editingId.value = null;
  form.value = { amount: "" };
  selectedCatIndex.value = 0;
  showForm.value = true;
}

function onEdit(item: any) {
  editingId.value = item.id;
  form.value = { amount: String(item.amount / 100) };
  const idx = categoryOptions.value.findIndex(
    (o: any) => o.value === item.category_id,
  );
  selectedCatIndex.value = idx >= 0 ? idx : 0;
  showForm.value = true;
}

async function onSave() {
  const amountVal = parseFloat(form.value.amount);
  if (!amountVal || amountVal <= 0) {
    uni.showToast({ title: "请输入有效预算金额", icon: "none" });
    return;
  }
  const month = `${currentYear.value}-${String(currentMonth.value).padStart(2, "0")}`;
  saving.value = true;
  try {
    const data = {
      book_id: bookId.value,
      category_id: categoryOptions.value[selectedCatIndex.value]?.value || null,
      amount: Math.round(amountVal * 100),
      period_type: 0,
      start_date: month + "-01",
    };
    if (editingId.value) {
      await updateBudget(editingId.value, data);
    } else {
      await createBudget(data);
    }
    showForm.value = false;
    uni.showToast({
      title: editingId.value ? "修改成功" : "创建成功",
      icon: "success",
    });
    await loadBudgets();
  } catch {
    uni.showToast({ title: "操作失败", icon: "error" });
  } finally {
    saving.value = false;
  }
}

function goBack() {
  uni.navigateBack();
}

onMounted(async () => {
  try {
    categories.value = await getCategories();
  } catch {
    categories.value = [];
  }
  loadBudgets();
});
</script>

<style lang="scss" scoped>
.budget-page {
  min-height: 100vh;
  background: var(--bg-primary);
}
.nav-left,
.nav-right {
  width: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-center {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.nav-arrow {
  padding: 8rpx;
}
.month-label {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 120rpx;
  text-align: center;
}

.total-ring {
  text-align: center;
  padding: 40rpx 32rpx;
  background: var(--bg-secondary);
  margin: 20rpx 32rpx;
  border-radius: 16rpx;
  .ring-chart {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    border: 8rpx solid var(--color-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16rpx;
    .ring-percent {
      font-size: 40rpx;
      font-weight: 700;
      color: var(--color-primary);
    }
  }
  .ring-label {
    display: block;
    font-size: 28rpx;
    color: var(--text-primary);
    font-weight: 600;
  }
  .ring-spent {
    display: block;
    font-size: 24rpx;
    color: var(--text-muted);
    margin-top: 4rpx;
  }
}

.budget-body {
  box-sizing: border-box;
  padding: 0 32rpx;
}

.empty-state {
  text-align: center;
  padding: 120rpx 0;
  .empty-title {
    display: block;
    font-size: 28rpx;
    color: var(--text-muted);
    margin-top: 16rpx;
  }
  .empty-hint {
    display: block;
    font-size: 24rpx;
    color: var(--text-placeholder);
    margin-top: 8rpx;
  }
}

.budget-item {
  background: var(--bg-secondary);
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  .budget-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12rpx;
  }
  .budget-cat {
    font-size: 28rpx;
    font-weight: 500;
    color: var(--text-primary);
  }
  .budget-amount {
    font-size: 28rpx;
    font-weight: 600;
    color: var(--text-primary);
  }
  .progress-bar {
    height: 12rpx;
    background: var(--bg-primary);
    border-radius: 6rpx;
    overflow: hidden;
  }
  .progress-fill {
    height: 100%;
    border-radius: 6rpx;
    transition: width 0.3s;
    &.normal {
      background: var(--color-primary);
    }
    &.warn {
      background: #ff9800;
    }
    &.danger {
      background: var(--color-expense);
    }
  }
  .budget-footer {
    display: flex;
    justify-content: space-between;
    margin-top: 8rpx;
  }
  .budget-spent {
    font-size: 22rpx;
    color: var(--text-muted);
  }
  .budget-warn {
    font-size: 22rpx;
    color: var(--color-expense);
  }
}

.form-content {
  padding: 40rpx 32rpx;
  .form-title {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 32rpx;
    display: block;
  }
  .form-field {
    margin-bottom: 24rpx;
    .field-label {
      display: block;
      font-size: 24rpx;
      color: var(--text-label);
      margin-bottom: 8rpx;
    }
    .field-picker {
      display: block;
    }
    .field-value {
      font-size: 28rpx;
      color: var(--text-primary);
      padding: 16rpx 0;
      display: block;
    }
    .field-input {
      font-size: 28rpx;
      color: var(--text-primary);
      background: var(--bg-secondary);
      padding: 16rpx 20rpx;
      border-radius: 8rpx;
      width: 100%;
    }
  }
  .form-actions {
    display: flex;
    gap: 16rpx;
    margin-top: 32rpx;
    > * {
      flex: 1;
    }
  }
}
</style>
