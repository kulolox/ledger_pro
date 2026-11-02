<script setup lang="ts">
import { watch } from "vue";
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { isLoggedIn } from "@/utils/storage";
import { useTheme, applyThemeToDom, syncThemeFromStorage } from "@/stores/themeStore";

const { themeMode } = useTheme();

watch(
  themeMode,
  (mode) => {
    applyThemeToDom(mode);
  },
  { immediate: true },
);

onLaunch(() => {
  applyThemeToDom(themeMode.value);
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

onShow(() => {
  syncThemeFromStorage();
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
  min-height: 100vh;
  transition:
    background-color 0.2s cubic-bezier(0.25, 1, 0.5, 1),
    color 0.2s cubic-bezier(0.25, 1, 0.5, 1);
}

::-webkit-scrollbar {
  display: none;
}
</style>
