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

      <!-- 状态三：发现新版本（仅向用户提供外部安全下载源：蓝奏云 + GitHub） -->
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

        <!-- 外部下载渠道推荐（不提供服务器下载，避免服务器受大流量攻击） -->
        <view class="download-channels-section">
          <!-- 渠道 1：蓝奏云极速下载（推荐） -->
          <view class="channel-card lanzou-card">
            <view class="channel-card-top">
              <view class="channel-title-row">
                <text class="channel-name">蓝奏云极速下载</text>
                <text class="channel-badge-recommend">推荐 · 免加速</text>
              </view>
              <text class="channel-desc"
                >国内 CDN 节点分发，手机浏览器极速秒下</text
              >
            </view>

            <!-- 提取码展示条 -->
            <view v-if="effectiveLanzouPwd" class="pwd-banner">
              <view class="pwd-info">
                <text class="pwd-label">提取码：</text>
                <text class="pwd-val">{{ effectiveLanzouPwd }}</text>
              </view>
              <view
                class="pwd-copy-btn"
                @click.stop="copyText(effectiveLanzouPwd, '提取码已复制')"
              >
                <wd-icon
                  name="copy"
                  size="24rpx"
                  color="var(--color-primary)"
                />
                <text class="pwd-copy-text">复制提取码</text>
              </view>
            </view>

            <view class="channel-action-row">
              <view class="channel-btn-primary" @click="openLanzouDownload">
                <wd-icon name="share" size="28rpx" color="#ffffff" />
                <text class="btn-text">外部浏览器下载</text>
              </view>
              <view
                class="channel-btn-sub"
                @click="copyText(effectiveLanzouUrl, '蓝奏云链接已复制')"
              >
                <wd-icon
                  name="copy"
                  size="28rpx"
                  color="var(--text-secondary)"
                />
                <text class="btn-text">复制链接</text>
              </view>
            </view>
          </view>

          <!-- 渠道 2：GitHub Release 原始下载 -->
          <view class="channel-card github-card">
            <view class="channel-card-top">
              <view class="channel-title-row">
                <text class="channel-name">GitHub 原始发布</text>
                <text class="channel-badge-sub">官方 Release</text>
              </view>
              <text class="channel-desc"
                >开源仓库官方原始 APK，适合海外或加速网络环境</text
              >
            </view>

            <view class="channel-action-row">
              <view class="channel-btn-outline" @click="openGithubDownload">
                <wd-icon
                  name="share"
                  size="28rpx"
                  color="var(--color-primary)"
                />
                <text class="btn-text">外部浏览器下载</text>
              </view>
              <view
                class="channel-btn-sub"
                @click="copyText(effectiveGithubUrl, 'GitHub 链接已复制')"
              >
                <wd-icon
                  name="copy"
                  size="28rpx"
                  color="var(--text-secondary)"
                />
                <text class="btn-text">复制链接</text>
              </view>
            </view>
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

const formattedChangelog = computed(() => {
  if (!updateInfo.value?.changelog) return "";
  return updateInfo.value.changelog.replace(/\\n/g, "\n");
});

// 计算蓝奏云下载链接（若后端下发则用后端，若无则兜底）
const effectiveLanzouUrl = computed(() => {
  if (updateInfo.value?.lanzouUrl) {
    return updateInfo.value.lanzouUrl;
  }
  return "https://wwaog.lanzouq.com/ieg8b4ep449g";
});

// 计算蓝奏云提取码（优先字段，其次从 changelog 解析，最后兜底 eond）
const effectiveLanzouPwd = computed(() => {
  if (updateInfo.value?.lanzouPwd) {
    return updateInfo.value.lanzouPwd;
  }
  const log = updateInfo.value?.changelog || "";
  const match = log.match(/【蓝奏云提取码[:：\s]*([a-zA-Z0-9]+)】/);
  if (match && match[1]) {
    return match[1];
  }
  return "eond";
});

// 计算 GitHub 原始下载链接
const effectiveGithubUrl = computed(() => {
  if (updateInfo.value?.githubUrl) {
    return updateInfo.value.githubUrl;
  }
  const ver =
    updateInfo.value?.latestVersion || currentVersion.value || "1.0.6";
  const tag = ver.startsWith("v") ? ver : `v${ver}`;
  return `https://github.com/flycat0114/ledger_pro/releases/download/${tag}/LedgerPro-${tag}-prod-release-20261104.apk`;
});

function goBack() {
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
    status.value = "latest";
  }
}

// 复制通用文本到剪贴板
function copyText(text: string, successTip = "已复制到剪贴板") {
  if (!text) return;
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: successTip, icon: "none" });
    },
  });
}

// 在手机外部浏览器中打开 URL
function openBrowserUrl(url: string, errorTip = "调起浏览器失败，链接已复制") {
  if (!url) {
    uni.showToast({ title: "链接地址不存在", icon: "none" });
    return;
  }

  // #ifdef APP-PLUS
  try {
    plus.runtime.openURL(url, (err: any) => {
      console.warn("Failed to open external browser:", err);
      copyText(url, errorTip);
    });
    return;
  } catch (e) {
    console.error("openBrowserUrl error:", e);
  }
  // #endif

  // #ifndef APP-PLUS
  if (typeof window !== "undefined") {
    window.open(url, "_blank");
  } else {
    copyText(url);
  }
  // #endif
}

// 前往蓝奏云下载：自动将提取码复制到剪贴板并调起外部浏览器
function openLanzouDownload() {
  const url = effectiveLanzouUrl.value;
  const pwd = effectiveLanzouPwd.value;

  if (pwd) {
    uni.setClipboardData({
      data: pwd,
      showToast: false,
      success: () => {
        uni.showToast({
          title: `提取码 [${pwd}] 已复制，正在打开浏览器...`,
          icon: "none",
          duration: 2500,
        });
        setTimeout(() => {
          openBrowserUrl(url);
        }, 300);
      },
      fail: () => {
        openBrowserUrl(url);
      },
    });
  } else {
    openBrowserUrl(url);
  }
}

// 前往 GitHub 原始下载
function openGithubDownload() {
  openBrowserUrl(effectiveGithubUrl.value);
}

// ==================== 备份保留方法 (Backup Only - 仅在仓库中归档，不公开暴露给用户) ====================
// @ts-ignore
function _backupStartDownload() {
  // 原应用内下载逻辑已备份保留在此，不对用户界面暴露
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
  margin-top: 24rpx;
  margin-bottom: 48rpx;

  .logo-container {
    margin-bottom: 20rpx;
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
  padding: 36rpx 32rpx;
  box-shadow: var(--shadow-sm);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

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
    padding: 22rpx;
    margin-bottom: 32rpx;

    .changelog-label {
      display: block;
      font-size: 26rpx;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 10rpx;
    }

    .changelog-content {
      font-size: 25rpx;
      color: var(--text-secondary);
      line-height: 1.6;
      white-space: pre-wrap;
    }
  }

  /* 下载渠道分组列表 (仅外部渠道) */
  .download-channels-section {
    display: flex;
    flex-direction: column;
    gap: 20rpx;

    .channel-card {
      background: var(--bg-primary);
      border: 1rpx solid var(--border-subtle);
      border-radius: var(--radius-md);
      padding: 24rpx;
      display: flex;
      flex-direction: column;
      gap: 18rpx;
      transition: all 0.15s ease-out;

      &.lanzou-card {
        border-color: rgba(22, 119, 255, 0.35);
        background: linear-gradient(
          180deg,
          var(--bg-primary) 0%,
          rgba(22, 119, 255, 0.04) 100%
        );
      }

      .channel-card-top {
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .channel-title-row {
          display: flex;
          align-items: center;
          gap: 12rpx;

          .channel-name {
            font-size: 28rpx;
            font-weight: 700;
            color: var(--text-primary);
          }

          .channel-badge-recommend {
            font-size: 20rpx;
            padding: 2rpx 10rpx;
            border-radius: 6rpx;
            background: rgba(22, 119, 255, 0.12);
            color: var(--color-primary);
            font-weight: 600;
          }

          .channel-badge-sub {
            font-size: 20rpx;
            padding: 2rpx 10rpx;
            border-radius: 6rpx;
            background: var(--bg-tertiary);
            color: var(--text-secondary);
            font-weight: 500;
          }
        }

        .channel-desc {
          font-size: 22rpx;
          color: var(--text-tertiary);
          line-height: 1.4;
        }
      }

      /* 提取码横幅 */
      .pwd-banner {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: var(--bg-tertiary);
        border: 1rpx dashed var(--border-subtle);
        border-radius: 8rpx;
        padding: 12rpx 18rpx;

        .pwd-info {
          display: flex;
          align-items: center;
          gap: 6rpx;

          .pwd-label {
            font-size: 24rpx;
            color: var(--text-secondary);
          }

          .pwd-val {
            font-size: 28rpx;
            font-weight: 700;
            color: var(--color-primary);
            font-family: monospace;
            letter-spacing: 2rpx;
          }
        }

        .pwd-copy-btn {
          display: flex;
          align-items: center;
          gap: 6rpx;
          padding: 6rpx 14rpx;
          border-radius: 6rpx;
          background: rgba(22, 119, 255, 0.08);
          cursor: pointer;

          &:active {
            opacity: 0.7;
          }

          .pwd-copy-text {
            font-size: 22rpx;
            color: var(--color-primary);
            font-weight: 600;
          }
        }
      }

      /* 操作按钮行 */
      .channel-action-row {
        display: flex;
        gap: 14rpx;

        .channel-btn-primary {
          flex: 2;
          height: 76rpx;
          border-radius: var(--radius-full);
          background: var(--color-primary);
          color: #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10rpx;
          cursor: pointer;
          box-shadow: 0 4rpx 14rpx rgba(22, 119, 255, 0.28);
          transition: transform 0.1s;

          &:active {
            transform: scale(0.98);
          }

          .btn-text {
            font-size: 26rpx;
            font-weight: 600;
          }
        }

        .channel-btn-outline {
          flex: 2;
          height: 76rpx;
          border-radius: var(--radius-full);
          border: 1rpx solid var(--color-primary);
          background: rgba(22, 119, 255, 0.06);
          color: var(--color-primary);
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 10rpx;
          cursor: pointer;
          transition: transform 0.1s;

          &:active {
            transform: scale(0.98);
          }

          .btn-text {
            font-size: 26rpx;
            font-weight: 600;
          }
        }

        .channel-btn-sub {
          flex: 1;
          height: 76rpx;
          border-radius: var(--radius-full);
          background: var(--bg-tertiary);
          border: 1rpx solid var(--border-subtle);
          color: var(--text-secondary);
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8rpx;
          cursor: pointer;
          transition: transform 0.1s;

          &:active {
            transform: scale(0.98);
          }

          .btn-text {
            font-size: 24rpx;
            font-weight: 500;
          }
        }
      }
    }
  }
}
</style>
