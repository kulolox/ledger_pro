<template>
  <view class="page">
    <view class="nav-bar">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="nav-title">导入导出</text>
      <view class="nav-placeholder" />
    </view>

    <view class="tabs">
      <view class="tab" :class="{ active: activeTab === 'export' }" @click="activeTab = 'export'">
        <text>导出数据</text>
      </view>
      <view class="tab" :class="{ active: activeTab === 'import' }" @click="activeTab = 'import'">
        <text>导入数据</text>
      </view>
    </view>

    <view v-if="activeTab === 'export'" class="tab-content">
      <view class="section">
        <text class="section-label">导出格式</text>
        <view class="format-row">
          <view
            v-for="fmt in formats"
            :key="fmt.value"
            class="format-chip"
            :class="{ active: exportFormat === fmt.value }"
            @click="exportFormat = fmt.value"
          >
            <text>{{ fmt.label }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-label">导出月份</text>
        <view class="month-picker">
          <view class="month-arrow" @click="prevExportMonth"><text>‹</text></view>
          <text class="month-text">{{ exportMonthLabel }}</text>
          <view class="month-arrow" @click="nextExportMonth"><text>›</text></view>
        </view>
      </view>

      <view class="section">
        <text class="section-label">导出账本</text>
        <picker :range="bookNames" :value="bookIndex" @change="onBookChange">
          <view class="picker-row">
            <text>{{ books[bookIndex]?.name || '选择账本' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="export-btn" @click="onExport">
        <text>{{ exporting ? '导出中...' : '导出' }}</text>
      </view>
    </view>

    <view v-if="activeTab === 'import'" class="tab-content">
      <view class="section">
        <text class="section-label">导入格式</text>
        <view class="format-row">
          <view
            v-for="fmt in formats"
            :key="fmt.value"
            class="format-chip"
            :class="{ active: importFormat === fmt.value }"
            @click="importFormat = fmt.value"
          >
            <text>{{ fmt.label }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-label">导入账本</text>
        <picker :range="bookNames" :value="bookIndex" @change="onBookChange">
          <view class="picker-row">
            <text>{{ books[bookIndex]?.name || '选择账本' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="section">
        <text class="section-label">选择文件</text>
        <view class="file-area" @click="onChooseFile">
          <view v-if="!selectedFile" class="file-empty">
            <text class="file-icon">📁</text>
            <text class="file-hint">点击选择文件</text>
            <text class="file-types">支持 CSV / Excel / JSON</text>
          </view>
          <view v-else class="file-selected">
            <text class="file-name">{{ selectedFile.name }}</text>
            <text class="file-size">{{ formatFileSize(selectedFile.size) }}</text>
            <text class="file-clear" @click.stop="selectedFile = null">✕</text>
          </view>
        </view>
      </view>

      <view v-if="previewData.length > 0" class="section">
        <text class="section-label">数据预览 ({{ previewData.length }} 条)</text>
        <scroll-view scroll-y class="preview-list">
          <view v-for="(item, idx) in previewData" :key="idx" class="preview-row">
            <text class="preview-type" :class="item.type === 1 ? 'income' : 'expense'">
              {{ item.type === 1 ? '收入' : '支出' }}
            </text>
            <text class="preview-amount">¥{{ (item.amount / 100).toFixed(2) }}</text>
            <text class="preview-remark">{{ item.remark || '-' }}</text>
          </view>
        </scroll-view>
      </view>

      <view class="import-btn" :class="{ disabled: !selectedFile || importing }" @click="onImport">
        <text>{{ importing ? '导入中...' : '导入' }}</text>
      </view>

      <view v-if="importResult" class="result-card">
        <text class="result-title">导入结果</text>
        <text class="result-text">成功导入 {{ importResult.imported }} / {{ importResult.total }} 条</text>
        <view v-if="importResult.errors.length > 0" class="result-errors">
          <text v-for="(err, idx) in importResult.errors" :key="idx" class="error-line">{{ err }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { getAccountBooks, exportTransactions, importTransactions, type AccountBook } from "@/api/common";

const activeTab = ref<"export" | "import">("export");

const formats = [
  { label: "CSV", value: "csv" },
  { label: "Excel", value: "excel" },
  { label: "JSON", value: "json" },
];

const exportFormat = ref("csv");
const importFormat = ref("csv");

const exportYear = ref(new Date().getFullYear());
const exportMonth = ref(new Date().getMonth() + 1);

const exportMonthLabel = computed(() => `${exportYear.value}年 ${exportMonth.value}月`);

const books = ref<AccountBook[]>([]);
const currentBookId = ref(getCurrentBookId());

const bookIndex = computed(() => {
  const idx = books.value.findIndex((b) => b.id === currentBookId.value);
  return idx >= 0 ? idx : 0;
});

const bookNames = computed(() => books.value.map((b) => b.name));

const exporting = ref(false);
const importing = ref(false);
const selectedFile = ref<{ name: string; size: number; path: string } | null>(null);
const previewData = ref<any[]>([]);
const importResult = ref<{ imported: number; errors: string[]; total: number } | null>(null);

function onBookChange(e: { detail: { value: number } }) {
  currentBookId.value = books.value[e.detail.value]?.id || currentBookId.value;
}

function prevExportMonth() {
  if (exportMonth.value === 1) {
    exportMonth.value = 12;
    exportYear.value--;
  } else {
    exportMonth.value--;
  }
}

function nextExportMonth() {
  if (exportMonth.value === 12) {
    exportMonth.value = 1;
    exportYear.value++;
  } else {
    exportMonth.value++;
  }
}

async function onExport() {
  if (exporting.value) return;
  exporting.value = true;
  try {
    const month = `${exportYear.value}-${String(exportMonth.value).padStart(2, "0")}`;
    await exportTransactions({
      book_id: currentBookId.value,
      month,
      format: exportFormat.value,
    });
    uni.showToast({ title: "导出成功", icon: "success" });
  } catch {
    uni.showToast({ title: "导出失败", icon: "error" });
  } finally {
    exporting.value = false;
  }
}

function onChooseFile() {
  uni.chooseFile({
    count: 1,
    extension: importFormat.value === "excel" ? [".xlsx", ".xls"] : importFormat.value === "csv" ? [".csv"] : [".json"],
    success: (res: any) => {
      const file = res.tempFiles[0];
      selectedFile.value = {
        name: file.name,
        size: file.size,
        path: file.path,
      };
      previewFile(file.path);
    },
    fail: (err: any) => {
      if (err.errMsg !== "chooseFile:fail cancel") {
        uni.showToast({ title: "选择文件失败", icon: "error" });
      }
    },
  });
}

function parsePreviewType(val: string): number {
  if (!val) return 0;
  const trimmed = val.trim();
  if (trimmed === "收入") return 1;
  if (trimmed === "支出") return 0;
  const n = parseInt(trimmed);
  return isNaN(n) ? 0 : n;
}

function parsePreviewAmount(val: string): number {
  if (!val) return 0;
  const trimmed = val.trim();
  if (trimmed.includes(".")) {
    return Math.round(parseFloat(trimmed) * 100);
  }
  const n = parseInt(trimmed);
  if (isNaN(n)) return 0;
  return n > 100000 ? n : Math.round(n * 100);
}

function previewFile(filePath: string) {
  const fs = uni.getFileSystemManager();
  try {
    const data = fs.readFileSync(filePath, "utf-8") as string;
    if (importFormat.value === "json") {
      const parsed = JSON.parse(data);
      previewData.value = Array.isArray(parsed)
        ? parsed.slice(0, 10).map((item: any) => ({
            type: parsePreviewType(String(item.type || "")),
            amount: parsePreviewAmount(String(item.amount || "")),
            remark: item.remark || "",
          }))
        : [];
    } else if (importFormat.value === "csv") {
      const lines = data.split("\n").filter((l: string) => l.trim());
      if (lines.length <= 1) {
        previewData.value = [];
        return;
      }
      const headers = lines[0].split(",").map((h: string) => h.trim());
      const idxType = headers.indexOf("type");
      const idxAmount = headers.indexOf("amount");
      const idxCategory = headers.indexOf("category");
      const idxRemark = headers.indexOf("remark");
      previewData.value = lines.slice(1, 11).map((line: string) => {
        const cols = line.split(",").map((c: string) => c.trim());
        const typeVal = parsePreviewType(cols[idxType] || "");
        const amountVal = parsePreviewAmount(cols[idxAmount] || "");
        const categoryVal = idxCategory >= 0 ? cols[idxCategory] : "";
        return {
          type: typeVal,
          amount: amountVal,
          remark: (categoryVal || cols[idxRemark] || ""),
        };
      });
    } else {
      previewData.value = [];
    }
  } catch {
    previewData.value = [];
  }
}

async function onImport() {
  if (!selectedFile.value || importing.value) return;
  importing.value = true;
  importResult.value = null;
  try {
    const result = await importTransactions(currentBookId.value, selectedFile.value.path, importFormat.value);
    importResult.value = result;
    selectedFile.value = null;
    previewData.value = [];
    uni.showToast({ title: `成功导入 ${result.imported} 条`, icon: "success" });
  } catch (e: any) {
    uni.showToast({ title: e.message || "导入失败", icon: "error" });
  } finally {
    importing.value = false;
  }
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

function goBack() {
  uni.navigateBack();
}

onShow(async () => {
  if (!isLoggedIn()) {
    uni.reLaunch({ url: "/pages/login/index" });
    return;
  }
  try {
    books.value = await getAccountBooks();
  } catch {
    books.value = [{ id: 1, name: "默认账本", coverIcon: "", type: 0 }];
  }
  if (!currentBookId.value && books.value.length > 0) {
    currentBookId.value = books.value[0].id;
  }
});
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: calc(var(--safe-area-bottom, 0px) + 40rpx);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: calc(var(--status-bar-height, 44px) + 16rpx) 32rpx 20rpx;
}

.back-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;

  &:active {
    transform: scale(0.92);
  }

  .back-icon {
    font-size: 48rpx;
    color: var(--text-secondary);
    font-weight: 300;
    line-height: 1;
  }
}

.nav-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.nav-placeholder {
  width: 64rpx;
}

.tabs {
  display: flex;
  margin: 0 32rpx 32rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 6rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: var(--radius-md);
  font-size: 28rpx;
  color: var(--text-muted);
  transition: all 0.2s;

  &.active {
    background: var(--bg-primary);
    color: var(--color-primary);
    font-weight: 600;
    box-shadow: var(--shadow-sm);
  }
}

.tab-content {
  padding: 0 32rpx;
}

.section {
  margin-bottom: 36rpx;
}

.section-label {
  font-size: 24rpx;
  color: var(--text-label);
  font-weight: 500;
  margin-bottom: 16rpx;
  display: block;
  letter-spacing: 1rpx;
}

.format-row {
  display: flex;
  gap: 16rpx;
}

.format-chip {
  padding: 16rpx 36rpx;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  font-size: 28rpx;
  color: var(--text-secondary);
  transition: all 0.15s;

  &.active {
    background: var(--color-primary);
    color: var(--text-inverse);
    font-weight: 600;
  }
}

.month-picker {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 24rpx;
}

.month-arrow {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);

  &:active {
    transform: scale(0.92);
  }

  text {
    font-size: 40rpx;
    color: var(--text-secondary);
    font-weight: 300;
    line-height: 1;
  }
}

.month-text {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 200rpx;
  text-align: center;
}

.picker-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 28rpx 24rpx;
  font-size: 28rpx;
  color: var(--text-primary);

  .picker-arrow {
    font-size: 36rpx;
    color: var(--text-muted);
    font-weight: 300;
  }
}

.export-btn {
  margin-top: 20rpx;
  padding: 28rpx 0;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  text-align: center;
  box-shadow: var(--shadow-md);

  &:active {
    opacity: 0.85;
  }

  text {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-inverse);
  }
}

.file-area {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 48rpx 24rpx;
  border: 2rpx dashed var(--border-primary);
  text-align: center;
  transition: border-color 0.15s;

  &:active {
    border-color: var(--color-primary);
  }
}

.file-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  .file-icon {
    font-size: 56rpx;
  }

  .file-hint {
    font-size: 28rpx;
    color: var(--text-primary);
    font-weight: 500;
  }

  .file-types {
    font-size: 22rpx;
    color: var(--text-muted);
  }
}

.file-selected {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 8rpx 0;

  .file-name {
    font-size: 28rpx;
    color: var(--text-primary);
    font-weight: 500;
    flex: 1;
    text-align: left;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    font-size: 24rpx;
    color: var(--text-muted);
    flex-shrink: 0;
  }

  .file-clear {
    font-size: 28rpx;
    color: var(--text-muted);
    padding: 8rpx;
    flex-shrink: 0;

    &:active {
      color: var(--color-expense);
    }
  }
}

.preview-list {
  max-height: 400rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
}

.preview-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid var(--border-subtle);

  &:last-child {
    border-bottom: none;
  }
}

.preview-type {
  font-size: 24rpx;
  font-weight: 600;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;

  &.expense {
    background: rgba(var(--color-expense-rgb, 255 59 48), 0.1);
    color: var(--color-expense);
  }

  &.income {
    background: rgba(var(--color-income-rgb, 52 199 89), 0.1);
    color: var(--color-income);
  }
}

.preview-amount {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.preview-remark {
  font-size: 24rpx;
  color: var(--text-muted);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.import-btn {
  margin-top: 20rpx;
  padding: 28rpx 0;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  text-align: center;
  box-shadow: var(--shadow-md);

  &:active {
    opacity: 0.85;
  }

  &.disabled {
    opacity: 0.4;
    pointer-events: none;
  }

  text {
    font-size: 32rpx;
    font-weight: 600;
    color: var(--text-inverse);
  }
}

.result-card {
  margin-top: 32rpx;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 28rpx 24rpx;
}

.result-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
  display: block;
  margin-bottom: 12rpx;
}

.result-text {
  font-size: 26rpx;
  color: var(--color-primary);
  font-weight: 500;
}

.result-errors {
  margin-top: 16rpx;
}

.error-line {
  font-size: 22rpx;
  color: var(--color-expense);
  display: block;
  margin-bottom: 4rpx;
}
</style>
