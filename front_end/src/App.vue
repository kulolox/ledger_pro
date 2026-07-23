<script setup lang="ts">
import { onLaunch } from "@dcloudio/uni-app";
import { initDB, select } from "@/utils/db";

onLaunch(async () => {
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
  padding-top: var(--status-bar-height);
  background: var(--bg-primary);
}

::-webkit-scrollbar {
  display: none;
}
</style>
