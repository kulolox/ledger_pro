<template>
  <view
    class="update-page"
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
        <text class="nav-title">检查更新</text>
      </template>
    </NavBar>

    <view class="update-content">
      <!-- 顶部应用信息 Logo -->
      <view class="app-hero">
        <view class="logo-container">
          <AppLogo :size="152" animated />
        </view>
        <text class="app-name">LedgerPro</text>
        <text class="current-version">当前版本 v{{ currentVersion }}</text>
      </view>

      <!-- 状态一：检查中 -->
      <view v-if="status === 'checking'" class="state-card checking-card">
        <wd-loading size="44rpx" color="var(--color-primary)" />
        <text class="checking-text">正在检查新版本...</text>
      </view>

      <!-- 状态二：已是最新版 -->
      <view v-else-if="status === 'latest'" class="state-card latest-card">
        <view class="latest-badge-icon">
          <wd-icon name="check" size="40rpx" color="#ffffff" />
        </view>
        <text class="latest-title">当前已是最新版本</text>
        <text class="latest-desc">暂无可用更新，请稍后再试</text>
        <view class="action-btn-outline" @click="checkForUpdate">
          <text class="action-btn-text">重新检查</text>
        </view>
      </view>

      <!-- 状态三：发现新版本或下载中 / 完成 -->
      <view
        v-else-if="
          status === 'has_update' ||
          status === 'downloading' ||
          status === 'downloaded' ||
          status === 'error'
        "
        class="update-card"
      >
        <view class="card-header">
          <view class="version-tag-row">
            <text class="new-version-title"
              >发现新版本 v{{ updateInfo?.latestVersion }}</text
            >
            <text v-if="updateInfo?.packageSize" class="package-size-badge">{{
              updateInfo.packageSize
            }}</text>
          </view>
          <text v-if="updateInfo?.forceUpdate" class="force-tag">重要更新</text>
        </view>

        <!-- 更新日志卡片 -->
        <view class="changelog-box">
          <text class="changelog-label">更新内容：</text>
          <text class="changelog-content">{{
            formattedChangelog || "日常问题修复与性能优化"
          }}</text>
        </view>

        <!-- 下载进度区域 -->
        <view v-if="status === 'downloading'" class="progress-section">
          <view class="progress-info-row">
            <text class="progress-state-text">正在下载软件包...</text>
            <text class="progress-percent-text">{{ downloadPercent }}%</text>
          </view>
          <view class="progress-bar-track">
            <view
              class="progress-bar-fill"
              :style="{ width: `${downloadPercent}%` }"
            />
          </view>
          <view class="progress-meta-row">
            <text class="progress-bytes">{{ downloadedSizeText }}</text>
          </view>
        </view>

        <!-- 错误提示 -->
        <view v-if="status === 'error'" class="error-notice">
          <wd-icon name="warn-bold" size="30rpx" color="#ff4d4f" />
          <text class="error-text">网络连接异常，软件包下载失败</text>
        </view>

        <!-- 底部主操作按钮 -->
        <view class="card-actions">
          <view
            v-if="status === 'has_update'"
            class="action-btn primary"
            @click="startDownload"
          >
            <wd-icon name="down" size="32rpx" color="#ffffff" />
            <text class="btn-text">立即下载更新</text>
          </view>

          <view
            v-else-if="status === 'downloading'"
            class="action-btn primary disabled"
          >
            <text class="btn-text">下载中 {{ downloadPercent }}%</text>
          </view>

          <view
            v-else-if="status === 'downloaded'"
            class="action-btn success"
            @click="installPackage"
          >
            <wd-icon name="check" size="32rpx" color="#ffffff" />
            <text class="btn-text">下载完成，立即安装</text>
          </view>

          <view
            v-else-if="status === 'error'"
            class="action-btn retry"
            @click="startDownload"
          >
            <text class="btn-text">重新下载</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import NavBar from "@/components/NavBar.vue";
import AppLogo from "@/components/AppLogo.vue";
import { checkAppUpdate, type AppVersionInfo } from "@/api/common";
import { getAppRuntimeInfo, APP_VERSION } from "@/constants/app";
import { useTheme, syncThemeFromStorage } from "@/stores/themeStore";
import { getApiBaseUrl } from "@/utils/request";

const { themeMode } = useTheme();

type PageStatus =
  | "checking"
  | "latest"
  | "has_update"
  | "downloading"
  | "downloaded"
  | "error";

const currentVersion = ref(APP_VERSION);
const currentVersionCode = ref(100);
const status = ref<PageStatus>("checking");
const updateInfo = ref<AppVersionInfo | null>(null);

const downloadPercent = ref(0);
const downloadedSizeText = ref("0 MB");
let savedFilePath = "";
let downloadTask: UniApp.DownloadTask | null = null;
let simulationTimer: ReturnType<typeof setInterval> | null = null;

const formattedChangelog = computed(() => {
  if (!updateInfo.value?.changelog) return "";
  return updateInfo.value.changelog.replace(/\\n/g, "\n");
});

function goBack() {
  if (simulationTimer) clearInterval(simulationTimer);
  if (downloadTask) {
    try {
      downloadTask.abort();
    } catch {}
  }
  uni.navigateBack();
}

async function checkForUpdate() {
  status.value = "checking";
  try {
    const runtime = await getAppRuntimeInfo();
    currentVersion.value = runtime.version;
    currentVersionCode.value = runtime.versionCode;

    const res = await checkAppUpdate(runtime.version, runtime.versionCode);
    updateInfo.value = res;
    if (res?.hasUpdate) {
      status.value = "has_update";
    } else {
      status.value = "latest";
    }
  } catch {
    // 若请求后端异常或离线，当成当前已是最新
    status.value = "latest";
  }
}

function startDownload() {
  status.value = "downloading";
  downloadPercent.value = 0;
  downloadedSizeText.value = "0 MB";

  let downloadUrl = updateInfo.value?.downloadUrl;
  if (!downloadUrl) {
    status.value = "error";
    return;
  }

  // 若为相对路径，自动拼接当前 API 基础路径
  if (downloadUrl.startsWith("/")) {
    const baseUrl = getApiBaseUrl().replace(/\/+$/, "");
    downloadUrl = baseUrl + downloadUrl;
  }

  // #ifdef APP-PLUS
  try {
    downloadTask = uni.downloadFile({
      url: downloadUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          savedFilePath = res.tempFilePath;
          status.value = "downloaded";
          downloadPercent.value = 100;
          installPackage();
        } else {
          console.error("Download failed with statusCode:", res.statusCode);
          status.value = "error";
        }
      },
      fail: (err) => {
        console.error("Download failed:", err);
        status.value = "error";
      },
    });

    downloadTask.onProgressUpdate((res) => {
      downloadPercent.value = res.progress;
      const writtenMB = (res.totalBytesWritten / (1024 * 1024)).toFixed(1);
      const totalMB = (res.totalBytesExpectedToWrite / (1024 * 1024)).toFixed(1);
      downloadedSizeText.value = `${writtenMB} MB / ${totalMB} MB`;
    });
  } catch (e) {
    console.error("startDownload error:", e);
    status.value = "error";
  }
  // #endif

  // #ifndef APP-PLUS
  simulateDownload();
  // #endif
}

function simulateDownload() {
  if (simulationTimer) clearInterval(simulationTimer);
  let current = 0;
  const targetMB = parseFloat(updateInfo.value?.packageSize || "18.5") || 18.5;

  simulationTimer = setInterval(() => {
    current += 5;
    if (current >= 100) {
      current = 100;
      if (simulationTimer) clearInterval(simulationTimer);
      downloadPercent.value = 100;
      downloadedSizeText.value = `${targetMB.toFixed(1)} MB / ${targetMB.toFixed(1)} MB`;
      status.value = "downloaded";
      savedFilePath = "ledgerpro-update.apk";
    } else {
      downloadPercent.value = current;
      const writtenMB = ((current / 100) * targetMB).toFixed(1);
      downloadedSizeText.value = `${writtenMB} MB / ${targetMB.toFixed(1)} MB`;
    }
  }, 100);
}

function openUnknownAppSettings(packageName: string, mainActivity: any) {
  // #ifdef APP-PLUS
  try {
    const Uri: any = (plus.android as any).importClass("android.net.Uri");
    const Settings: any = (plus.android as any).importClass(
      "android.provider.Settings",
    );
    const packageUri = Uri.parse("package:" + packageName);
    const intent: any = (plus.android as any).newObject(
      "android.content.Intent",
      Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
      packageUri,
    );
    mainActivity.startActivity(intent);
  } catch (err) {
    console.warn("Failed to open unknown app settings:", err);
    uni.showToast({
      title: "请在系统设置-应用权限中允许安装未知应用",
      icon: "none",
    });
  }
  // #endif
}

function checkUnknownAppInstallPermission(): boolean {
  // #ifdef APP-PLUS
  if (uni.getSystemInfoSync().platform !== "android") return true;
  try {
    const mainActivity: any = (plus.android as any).runtimeMainActivity();
    const packageManager: any = mainActivity.getPackageManager();
    const packageName: string = mainActivity.getPackageName();
    const Build: any = (plus.android as any).importClass("android.os.Build");

    if (Build.VERSION.SDK_INT >= 26) {
      const canInstall: boolean = packageManager.canRequestPackageInstalls();
      if (!canInstall) {
        uni.showModal({
          title: "权限申请",
          content: "安装更新需要开启“允许安装未知应用”权限，是否前往系统设置开启？",
          confirmText: "去开启",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              openUnknownAppSettings(packageName, mainActivity);
            }
          },
        });
        return false;
      }
    }
  } catch (e) {
    console.warn("checkUnknownAppInstallPermission error:", e);
  }
  // #endif
  return true;
}

function installPackage() {
  // #ifdef APP-PLUS
  if (!savedFilePath) {
    uni.showToast({ title: "安装包未找到", icon: "none" });
    return;
  }

  if (!checkUnknownAppInstallPermission()) {
    return;
  }

  const realPath = plus.io.convertLocalFileSystemURL(savedFilePath);

  plus.runtime.install(
    realPath,
    { force: false },
    () => {
      uni.showToast({ title: "正在调起系统安装...", icon: "none" });
    },
    (err: any) => {
      console.error("plus.runtime.install error:", err);
      uni.showModal({
        title: "调起安装失败",
        content: `安装器调起失败: ${err?.message || "系统权限受限"}。\n请确认手机设置中已允许本应用“安装未知应用”。`,
        confirmText: "去设置",
        cancelText: "我知道了",
        success: (modalRes) => {
          if (modalRes.confirm) {
            try {
              const mainActivity: any = (plus.android as any).runtimeMainActivity();
              const packageName: string = mainActivity.getPackageName();
              openUnknownAppSettings(packageName, mainActivity);
            } catch {
              uni.showToast({
                title: "请手动在手机系统设置中授予本应用安装权限",
                icon: "none",
              });
            }
          }
        },
      });
    },
  );
  // #endif

  // #ifndef APP-PLUS
  uni.showToast({ title: "软件包已就绪（演示环境）", icon: "success" });
  // #endif
}

onShow(async () => {
  syncThemeFromStorage();
  const runtime = await getAppRuntimeInfo();
  currentVersion.value = runtime.version;
  currentVersionCode.value = runtime.versionCode;
  checkForUpdate();
});
</script>

<style lang="scss" scoped>
.update-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
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

.update-content {
  flex: 1;
  padding: 40rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 32rpx;
  margin-bottom: 56rpx;

  .logo-container {
    margin-bottom: 24rpx;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .app-name {
    font-size: 40rpx;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 8rpx;
    letter-spacing: -0.5rpx;
  }

  .current-version {
    font-size: 26rpx;
    color: var(--text-tertiary);
  }
}

.state-card {
  width: 100%;
  box-sizing: border-box;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 64rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-sm);

  &.checking-card {
    gap: 24rpx;

    .checking-text {
      font-size: 28rpx;
      color: var(--text-secondary);
    }
  }

  &.latest-card {
    .latest-badge-icon {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background: var(--color-income);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24rpx;
      box-shadow: 0 8rpx 20rpx rgba(46, 204, 113, 0.28);
    }

    .latest-title {
      font-size: 32rpx;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8rpx;
    }

    .latest-desc {
      font-size: 24rpx;
      color: var(--text-tertiary);
      margin-bottom: 40rpx;
    }

    .action-btn-outline {
      padding: 16rpx 48rpx;
      border-radius: var(--radius-full);
      border: 1rpx solid var(--border-subtle);
      background: var(--bg-tertiary);
      cursor: pointer;
      transition: all 0.15s;

      &:active {
        transform: scale(0.96);
        background: var(--bg-hover);
      }

      .action-btn-text {
        font-size: 26rpx;
        color: var(--text-secondary);
        font-weight: 500;
      }
    }
  }
}

.update-card {
  width: 100%;
  box-sizing: border-box;
  background: var(--bg-secondary);
  border: 1rpx solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 40rpx 36rpx;
  box-shadow: var(--shadow-sm);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 28rpx;

    .version-tag-row {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .new-version-title {
        font-size: 32rpx;
        font-weight: 700;
        color: var(--text-primary);
      }

      .package-size-badge {
        font-size: 22rpx;
        padding: 4rpx 14rpx;
        border-radius: var(--radius-full);
        background: var(--bg-input-filled);
        color: var(--text-tertiary);
        font-weight: 500;
      }
    }

    .force-tag {
      font-size: 20rpx;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      background: var(--color-expense-bg);
      color: var(--color-expense);
      font-weight: 600;
    }
  }

  .changelog-box {
    background: var(--bg-tertiary);
    border-radius: var(--radius-md);
    padding: 24rpx;
    margin-bottom: 36rpx;

    .changelog-label {
      display: block;
      font-size: 26rpx;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 12rpx;
    }

    .changelog-content {
      font-size: 26rpx;
      color: var(--text-secondary);
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  .progress-section {
    margin-bottom: 36rpx;

    .progress-info-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 14rpx;

      .progress-state-text {
        font-size: 24rpx;
        color: var(--text-secondary);
      }

      .progress-percent-text {
        font-size: 26rpx;
        font-weight: 600;
        color: var(--color-primary);
      }
    }

    .progress-bar-track {
      height: 14rpx;
      border-radius: var(--radius-full);
      background: var(--bg-input-filled);
      overflow: hidden;
      margin-bottom: 10rpx;

      .progress-bar-fill {
        height: 100%;
        border-radius: var(--radius-full);
        background: linear-gradient(90deg, #1677ff 0%, #36cfc9 100%);
        transition: width 0.15s ease-out;
      }
    }

    .progress-meta-row {
      display: flex;
      justify-content: flex-end;

      .progress-bytes {
        font-size: 22rpx;
        color: var(--text-tertiary);
      }
    }
  }

  .error-notice {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 24rpx;
    padding: 16rpx 20rpx;
    border-radius: var(--radius-md);
    background: var(--color-expense-bg);

    .error-text {
      font-size: 24rpx;
      color: var(--color-expense);
    }
  }

  .card-actions {
    width: 100%;

    .action-btn {
      height: 88rpx;
      border-radius: var(--radius-full);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      cursor: pointer;
      transition: all 0.15s;

      &:active:not(.disabled) {
        transform: scale(0.98);
      }

      &.primary {
        background: var(--color-primary);
        color: #ffffff;
        box-shadow: 0 8rpx 20rpx rgba(22, 119, 255, 0.28);
      }

      &.success {
        background: var(--color-income);
        color: #ffffff;
        box-shadow: 0 8rpx 20rpx rgba(46, 204, 113, 0.28);
      }

      &.retry {
        background: var(--bg-tertiary);
        color: var(--text-primary);
        border: 1rpx solid var(--border-subtle);
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .btn-text {
        font-size: 30rpx;
        font-weight: 600;
      }
    }
  }
}
</style>
