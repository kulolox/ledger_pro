<template>
  <view class="page">
    <NavBar>
      <template #left>
        <view class="back-btn" @click="goBack">
          <wd-icon name="left" size="36rpx" color="var(--text-primary)" />
        </view>
      </template>
      <template #center>
        <text class="nav-title">导入导出</text>
      </template>
    </NavBar>

    <view class="segmented-wrap">
      <wd-tabs v-model="activeTab" custom-class="tab-custom">
        <wd-tab title="导出数据" name="export" />
        <wd-tab title="导入数据" name="import" />
      </wd-tabs>
    </view>

    <view v-if="activeTab === 'export'" class="tab-content">
      <view class="form-card">
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
          <text class="section-label">时间范围</text>
          <view class="mode-row">
            <view
              class="mode-chip"
              :class="{ active: exportMode === 'month' }"
              @click="exportMode = 'month'"
            >
              <text>按月</text>
            </view>
            <view
              class="mode-chip"
              :class="{ active: exportMode === 'year' }"
              @click="exportMode = 'year'"
            >
              <text>按年</text>
            </view>
          </view>
        </view>

        <view v-if="exportMode === 'month'" class="section">
          <text class="section-label">导出月份</text>
          <view class="month-picker">
            <view class="month-arrow" @click="prevExportMonth">
              <wd-icon name="left" size="30rpx" color="var(--text-primary)" />
            </view>
            <text class="month-text">{{ exportMonthLabel }}</text>
            <view class="month-arrow" @click="nextExportMonth">
              <wd-icon name="right" size="30rpx" color="var(--text-primary)" />
            </view>
          </view>
        </view>

        <view v-if="exportMode === 'year'" class="section">
          <text class="section-label">导出年份</text>
          <view class="year-picker">
            <view class="year-arrow" @click="exportYear--">
              <wd-icon name="left" size="30rpx" color="var(--text-primary)" />
            </view>
            <text class="year-text">{{ exportYear }}</text>
            <view class="year-arrow" @click="exportYear++">
              <wd-icon name="right" size="30rpx" color="var(--text-primary)" />
            </view>
          </view>
        </view>

        <view class="section">
          <text class="section-label">导出账本</text>
          <picker :range="bookNames" :value="bookIndex" @change="onBookChange">
            <view class="picker-row">
              <text>{{ books[bookIndex]?.name || "日常账本" }}</text>
              <wd-icon name="right" size="28rpx" color="var(--text-tertiary)" />
            </view>
          </picker>
        </view>

        <view class="export-btn" @click="onExport">
          <text>{{ exporting ? "导出中..." : "导出" }}</text>
        </view>
      </view>

      <view class="helper-notice">
        <text class="helper-text">
          导出的文件将保存在本地“文件”目录，或可直接通过系统面板分享至微信等应用。
        </text>
      </view>
    </view>

    <view v-if="activeTab === 'import'" class="tab-content">
      <view class="form-card">
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
              <text>{{ books[bookIndex]?.name || "日常账本" }}</text>
              <wd-icon name="right" size="28rpx" color="var(--text-tertiary)" />
            </view>
          </picker>
        </view>

        <view class="section">
          <view class="file-label-row">
            <text class="section-label file-label">选择文件</text>
            <text class="template-link" @click="onDownloadTemplate">
              下载 {{ importFormat.toUpperCase() }} 模板
            </text>
          </view>
          <view class="file-area" @click="onChooseFile">
            <view v-if="!selectedFile" class="file-empty">
              <view class="file-icon-circle">
                <wd-icon
                  name="folder"
                  size="52rpx"
                  color="var(--text-tertiary)"
                />
              </view>
              <text class="file-hint">点击选择文件</text>
              <text class="file-types">支持 CSV / Excel / JSON</text>
            </view>
            <view v-else class="file-selected">
              <text class="file-name">{{ selectedFile.name }}</text>
              <text class="file-size">{{
                formatFileSize(selectedFile.size)
              }}</text>
              <text class="file-clear" @click.stop="selectedFile = null"
                >✕</text
              >
            </view>
          </view>
        </view>

        <view v-if="previewData.length > 0" class="section">
          <text class="section-label"
            >数据预览 ({{ previewData.length }} 条)</text
          >
          <scroll-view scroll-y class="preview-list">
            <view
              v-for="(item, idx) in previewData"
              :key="idx"
              class="preview-row"
            >
              <text
                class="preview-type"
                :class="item.type === 1 ? 'income' : 'expense'"
              >
                {{ item.type === 1 ? "收入" : "支出" }}
              </text>
              <text class="preview-amount"
                >¥{{ (item.amount / 100).toFixed(2) }}</text
              >
              <text class="preview-remark">{{ item.remark || "-" }}</text>
            </view>
          </scroll-view>
        </view>

        <view
          class="import-btn"
          :class="{ disabled: !selectedFile || importing }"
          @click="onImport"
        >
          <text>{{ importing ? "导入中..." : "导入" }}</text>
        </view>
      </view>

      <view v-if="importResult" class="result-card">
        <text class="result-title">导入结果</text>
        <text class="result-text">
          成功导入 {{ importResult.imported }} / {{ importResult.total }} 条
        </text>
        <view v-if="importResult.errors.length > 0" class="result-errors">
          <text
            v-for="(err, idx) in importResult.errors"
            :key="idx"
            class="error-line"
            >{{ err }}</text
          >
        </view>
      </view>

      <view class="helper-notice">
        <text class="helper-text">
          导入前请确保数据格式与模板一致，已存在相同日期的账单将自动校验入库。
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import { isLoggedIn, getCurrentBookId } from "@/utils/storage";
import { pullFromServer } from "@/utils/sync";
import {
  getAccountBooks,
  exportTransactions,
  importTransactions,
  downloadTemplate,
  type AccountBook,
} from "@/api/common";
import { yuanToCents } from "@/utils/format";

const activeTab = ref<"export" | "import">("export");

const formats = [
  { label: "CSV", value: "csv" },
  { label: "Excel", value: "excel" },
  { label: "JSON", value: "json" },
];

const exportFormat = ref("csv");
const importFormat = ref("csv");

const exportMode = ref<"month" | "year">("month");
const exportYear = ref(new Date().getFullYear());
const exportMonth = ref(new Date().getMonth() + 1);

const exportMonthLabel = computed(
  () => `${exportYear.value}年 ${exportMonth.value}月`,
);

const books = ref<AccountBook[]>([]);
const currentBookId = ref(getCurrentBookId());

const bookIndex = computed(() => {
  const idx = books.value.findIndex((b) => b.id === currentBookId.value);
  return idx >= 0 ? idx : 0;
});

const bookNames = computed(() => books.value.map((b) => b.name));

const exporting = ref(false);
const importing = ref(false);
const selectedFile = ref<{ name: string; size: number; path: string } | null>(
  null,
);
const previewData = ref<any[]>([]);
const importResult = ref<{
  imported: number;
  errors: string[];
  total: number;
} | null>(null);

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
    const params: Record<string, any> = {
      book_id: currentBookId.value,
      format: exportFormat.value,
    };
    if (exportMode.value === "year") {
      params.year = String(exportYear.value);
    } else {
      params.month = `${exportYear.value}-${String(exportMonth.value).padStart(2, "0")}`;
    }
    await exportTransactions(
      params as Parameters<typeof exportTransactions>[0],
    );
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
    extension:
      importFormat.value === "excel"
        ? [".xlsx", ".xls"]
        : importFormat.value === "csv"
          ? [".csv"]
          : [".json"],
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
  return yuanToCents(val);
}

async function previewFile(filePath: string) {
  try {
    let data = "";
    if (typeof uni.getFileSystemManager === "function") {
      const fs = uni.getFileSystemManager();
      data = fs.readFileSync(filePath, "utf-8") as string;
    } else if (typeof fetch === "function") {
      const resp = await fetch(filePath);
      data = await resp.text();
    }
    if (!data) {
      previewData.value = [];
      return;
    }
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
          remark: categoryVal || cols[idxRemark] || "",
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
    const result = await importTransactions(
      currentBookId.value,
      selectedFile.value.path,
      importFormat.value,
    );
    importResult.value = result;
    selectedFile.value = null;
    previewData.value = [];
    await pullFromServer(currentBookId.value);
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

async function onDownloadTemplate() {
  try {
    await downloadTemplate(importFormat.value);
    uni.showToast({ title: "模板下载成功", icon: "success" });
  } catch {
    uni.showToast({ title: "模板下载失败", icon: "error" });
  }
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
    books.value = [];
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
  padding-bottom: calc(var(--safe-area-bottom, 0px) + 48rpx);
  transition: background-color 0.2s var(--spring-default);
}

.back-btn {
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

.segmented-wrap {
  padding: 16rpx 32rpx 20rpx;
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

.tab-content {
  padding: 0 32rpx;
}

.form-card {
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 36rpx 32rpx;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 36rpx;
  transition: background-color 0.2s var(--spring-default);
}

.section {
  display: flex;
  flex-direction: column;
}

.section-label {
  font-size: 24rpx;
  color: var(--text-tertiary);
  font-weight: 500;
  margin-bottom: 20rpx;
  display: block;

  &.file-label {
    margin-bottom: 0;
  }
}

.format-row,
.mode-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.format-chip,
.mode-chip {
  padding: 16rpx 48rpx;
  border-radius: var(--radius-full);
  background: var(--bg-input-filled);
  font-size: 28rpx;
  font-weight: 500;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.16s var(--spring-default);

  &:active {
    transform: scale(0.96);
  }

  &.active {
    background: var(--color-primary);
    color: #ffffff;
    font-weight: 600;
    box-shadow: var(--shadow-sm);
  }
}

.month-picker,
.year-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-input-filled);
  border-radius: var(--radius-md);
  padding: 20rpx 28rpx;
}

.month-arrow,
.year-arrow {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.15s var(--spring-default);

  &:active {
    transform: scale(0.9);
  }
}

.month-text,
.year-text {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  letter-spacing: -0.5rpx;
  font-variant-numeric: tabular-nums;
}

.picker-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-input-filled);
  border-radius: var(--radius-md);
  padding: 26rpx 28rpx;
  font-size: 30rpx;
  color: var(--text-primary);
  cursor: pointer;
}

.export-btn,
.import-btn {
  margin-top: 8rpx;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  box-shadow: var(--shadow-primary-md);
  cursor: pointer;
  transition:
    transform 0.15s var(--spring-default),
    opacity 0.15s;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  &.disabled {
    opacity: 0.4;
    pointer-events: none;
  }

  text {
    font-size: 32rpx;
    font-weight: 600;
    color: #ffffff;
  }
}

.file-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.template-link {
  font-size: 26rpx;
  font-weight: 500;
  color: var(--color-primary);
  cursor: pointer;
  transition: opacity 0.15s;

  &:active {
    opacity: 0.65;
  }
}

.file-area {
  background: var(--bg-card-subtle);
  border-radius: var(--radius-lg);
  padding: 56rpx 32rpx;
  border: 3rpx dashed var(--border-primary);
  text-align: center;
  cursor: pointer;
  transition:
    border-color 0.2s,
    background-color 0.2s;

  &:active {
    border-color: var(--color-primary);
    background: var(--bg-active);
  }
}

.file-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;

  .file-icon-circle {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: var(--bg-input-filled);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8rpx;
  }

  .file-hint {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 600;
  }

  .file-types {
    font-size: 24rpx;
    color: var(--text-tertiary);
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
    color: var(--text-tertiary);
    flex-shrink: 0;
  }

  .file-clear {
    font-size: 28rpx;
    color: var(--text-tertiary);
    padding: 8rpx;
    flex-shrink: 0;

    &:active {
      color: var(--color-expense);
    }
  }
}

.preview-list {
  max-height: 400rpx;
  background: var(--bg-input-filled);
  border-radius: var(--radius-md);
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
    background: var(--color-expense-bg);
    color: var(--color-expense);
  }

  &.income {
    background: var(--color-income-bg);
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
  color: var(--text-tertiary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-card {
  margin-top: 28rpx;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 28rpx 32rpx;
  box-shadow: var(--shadow-sm);
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

.helper-notice {
  padding: 28rpx 24rpx 16rpx;
  text-align: center;

  .helper-text {
    font-size: 24rpx;
    color: var(--text-tertiary);
    line-height: 1.6;
  }
}
</style>
