<script setup lang="ts">
import { onLaunch } from "@dcloudio/uni-app";
import { initDB, select } from "@/utils/db";

onLaunch(async () => {
  const { safeAreaInsets } = uni.getSystemInfoSync();
  try {
    const style = document.documentElement.style;
    style.setProperty("--status-bar-height", `${uni.getSystemInfoSync().statusBarHeight}px`);
    if (safeAreaInsets?.bottom) {
      style.setProperty("--safe-area-bottom", `${safeAreaInsets.bottom}px`);
    }
  } catch {}

  console.log("App Launch");

  try {
    await initDB();
  } catch {
    uni.reLaunch({ url: "/pages/index/index" });
    return;
  }

  try {
    const rows = await select("SELECT id FROM user_profile LIMIT 1");
    if (rows.length > 0) {
      uni.reLaunch({ url: "/pages/index/index" });
    } else {
      uni.reLaunch({ url: "/pages/onboarding/index" });
    }
  } catch {
    uni.reLaunch({ url: "/pages/index/index" });
  }
});
</script>

<style lang="scss">
@import url("@/styles/common.scss");

page {
  color: var(--text-primary);
  font-family: var(--font-family-base);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--bg-primary);
}

::-webkit-scrollbar {
  display: none;
}
</style>
