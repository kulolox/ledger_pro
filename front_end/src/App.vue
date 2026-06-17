<script setup lang="ts">
import { onLaunch } from "@dcloudio/uni-app";
import { isLoggedIn } from "@/utils/storage";

onLaunch(() => {
  console.log("App Launch");
  const pages = getCurrentPages();
  if (pages.length === 0) {
    const hash = window.location.hash;
    if (!hash || hash === "#/" || hash === "#") {
      if (isLoggedIn()) {
        uni.reLaunch({ url: "/pages/index/index" });
      } else {
        uni.reLaunch({ url: "/pages/login/index" });
      }
    }
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
