<template>
  <view class="migration-page">
    <NavBar>
      <template #left>
        <view class="back-btn" @click="goBack">
          <wd-icon
            name="arrow-left"
            size="24px"
            color="var(--text-secondary)"
          />
        </view>
      </template>
      <template #center>
        <text class="nav-title">数据迁移</text>
      </template>
      <template #right>
        <view class="nav-placeholder" />
      </template>
    </NavBar>

    <view class="content">
      <view class="card">
        <view class="card-icon-wrapper">
          <wd-icon name="download" size="48rpx" color="var(--color-primary)" />
        </view>
        <text class="card-title">导出数据</text>
        <text class="card-desc"
          >将所有数据导出为备份文件，可用于迁移到新手机</text
        >
        <view class="card-btn" @click="onExport">
          <text>{{ exporting ? "导出中..." : "导出数据" }}</text>
        </view>
      </view>

      <view class="divider-row">
        <view class="divider-line" />
      </view>

      <view class="card">
        <view class="card-icon-wrapper">
          <wd-icon name="upload" size="48rpx" color="var(--color-primary)" />
        </view>
        <text class="card-title">导入数据</text>
        <text class="card-desc">从备份文件恢复数据，将覆盖当前所有数据</text>
        <view
          class="card-btn"
          :class="{ disabled: importing }"
          @click="onImport"
        >
          <text>{{ importing ? "导入中..." : "选择文件并导入" }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import NavBar from "@/components/NavBar.vue";
// #ifdef H5
import { exportData, importData } from "@/utils/migration";
// #endif
// #ifdef APP-PLUS
import { exportData, importDataFile, listBackupFiles } from "@/utils/migration";
// #endif
import { useUser } from "@/stores/userStore";
import { useCategories } from "@/stores/categoryStore";
import { useTransactions } from "@/stores/transactionStore";

const exporting = ref(false);
const importing = ref(false);

function goBack() {
  uni.navigateBack();
}

async function onExport() {
  if (exporting.value) return;
  exporting.value = true;
  try {
    await exportData();
    uni.showToast({ title: "导出成功", icon: "success" });
  } catch {
    uni.showToast({ title: "导出失败", icon: "error" });
  } finally {
    exporting.value = false;
  }
}

function onImport() {
  if (importing.value) return;

  uni.showModal({
    title: "确认导入",
    content: "导入将覆盖当前所有数据，此操作不可恢复。确定继续？",
    confirmText: "确定导入",
    cancelText: "取消",
    success: (res) => {
      if (res.confirm) {
        triggerFileInput();
      }
    },
  });
}

// #ifdef H5
function triggerFileInput() {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = ".json,application/json";
  input.onchange = async (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (!file) return;
    importing.value = true;
    try {
      await importData(file);
      await refreshStores();
      uni.showToast({ title: "导入成功", icon: "success" });
    } catch (e: any) {
      uni.showToast({ title: e.message || "导入失败", icon: "error" });
    } finally {
      importing.value = false;
    }
  };
  input.click();
}
// #endif

// #ifdef APP-PLUS
async function triggerFileInput() {
  importing.value = true;
  try {
    const files = await listBackupFiles();
    if (files.length === 0) {
      uni.showToast({
        title: "下载目录中没有备份文件，请先将 .json 文件放到下载目录",
        icon: "none",
        duration: 3000,
      });
      return;
    }
    const { tapIndex } = await new Promise<{ tapIndex: number }>((resolve) => {
      uni.showActionSheet({
        title: "选择备份文件",
        itemList: files.map((f) => f.name),
        success: (res) => resolve({ tapIndex: res.tapIndex }),
        fail: () => resolve({ tapIndex: -1 }),
      });
    });
    if (tapIndex < 0) return;
    await importDataFile(files[tapIndex].fullPath);
    await refreshStores();
    uni.showToast({ title: "导入成功", icon: "success" });
  } catch (e: any) {
    uni.showToast({ title: e.message || "导入失败", icon: "error" });
  } finally {
    importing.value = false;
  }
}
// #endif

async function refreshStores() {
  const { loadUser } = useUser();
  const { loadCategories } = useCategories();
  const { load } = useTransactions();
  const now = new Date();
  const m = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  await loadUser();
  await loadCategories();
  await load(m);
}
</script>

<style lang="scss" scoped>
.migration-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding-bottom: calc(var(--safe-area-bottom, 0px) + 40rpx);
}

.back-btn {
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

.nav-placeholder {
  width: 64rpx;
  height: 64rpx;
  flex-shrink: 0;
}

.content {
  padding: 40rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.card {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 48rpx 36rpx 36rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.card-icon-wrapper {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.card-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12rpx;
}

.card-desc {
  font-size: 26rpx;
  color: var(--text-muted);
  line-height: 1.6;
  margin-bottom: 32rpx;
  padding: 0 16rpx;
}

.card-btn {
  width: 100%;
  padding: 24rpx 0;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-inverse);

  &:active {
    opacity: 0.85;
  }

  &.disabled {
    opacity: 0.5;
  }
}

.divider-row {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 100rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: var(--border-subtle);
}
</style>
