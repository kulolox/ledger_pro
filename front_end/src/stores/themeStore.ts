import { ref } from "vue";

export type ThemeMode = "light" | "dark";

const THEME_STORAGE_KEY = "theme_mode";

export function getInitialTheme(): ThemeMode {
  try {
    const saved = uni.getStorageSync(THEME_STORAGE_KEY);
    if (saved === "light" || saved === "dark") {
      return saved;
    }
  } catch {
    // Ignore storage errors
  }
  return "light";
}

const themeMode = ref<ThemeMode>(getInitialTheme());

const PAGE_SELECTORS =
  "uni-page-body, page, .home-page, .login-page, .auth-page, .stats-page, .category-page, .search-page, .profile-page, .import-export-page, .page";

export function syncStatusBarStyle(mode: ThemeMode = themeMode.value) {
  const isDark = mode === "dark";
  const statusStyle = isDark ? "light" : "dark"; // 5+App: light为白字(深色背景)，dark为黑字(浅色背景)
  const frontColor = isDark ? "#ffffff" : "#000000";
  const navBgColor = isDark ? "#1c1c1e" : "#ffffff";

  const execute = () => {
    // #ifdef APP-PLUS
    try {
      // @ts-ignore
      if (typeof plus !== "undefined") {
        // 1. 顶部状态栏文字颜色
        if (plus.navigator) {
          // @ts-ignore
          plus.navigator.setStatusBarStyle(statusStyle);
        }

        // 2. 底部虚拟导航栏颜色联动 (Android)
        // @ts-ignore
        if (plus.os && plus.os.name === "Android" && plus.android) {
          // @ts-ignore
          const Color = plus.android.importClass("android.graphics.Color") as any;
          // @ts-ignore
          const windowAndroid = plus.android.runtimeMainActivity().getWindow();
          const bottomBgColor = isDark ? "#000000" : "#f2f2f7";
          windowAndroid.setNavigationBarColor(Color.parseColor(bottomBgColor));

          // 联动底部导航栏按键图标颜色 (浅色背景黑图标，深色背景白图标)
          const decorView = windowAndroid.getDecorView();
          let flags = decorView.getSystemUiVisibility();
          // SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = 16 (Android 8.0+)
          if (!isDark) {
            flags |= 16;
          } else {
            flags &= ~16;
          }
          decorView.setSystemUiVisibility(flags);
        }
      }
    } catch {
      // Ignore native error
    }
    // #endif

    try {
      if (typeof uni !== "undefined" && typeof uni.setNavigationBarColor === "function") {
        uni.setNavigationBarColor({
          frontColor,
          backgroundColor: navBgColor,
        });
      }
    } catch {
      // Ignore
    }
  };

  execute();
  // 二次与三次确认，彻底防止原生页面转场动画结束后的系统默认反向覆盖
  setTimeout(execute, 80);
  setTimeout(execute, 260);
}

export function applyThemeToDom(mode: ThemeMode = themeMode.value) {
  syncStatusBarStyle(mode);

  if (typeof document !== "undefined") {
    document.documentElement.setAttribute("data-theme", mode);
    document.documentElement.classList.remove("theme-light", "theme-dark");
    document.documentElement.classList.add(`theme-${mode}`);
    document.documentElement.style.colorScheme = mode;

    if (document.body) {
      document.body.classList.remove("theme-light", "theme-dark");
      document.body.classList.add(`theme-${mode}`);
      document.body.setAttribute("data-theme", mode);
    }

    const appEl = document.querySelector("uni-app");
    if (appEl) {
      appEl.setAttribute("data-theme", mode);
      appEl.classList.remove("theme-light", "theme-dark");
      appEl.classList.add(`theme-${mode}`);
    }

    const pages = document.querySelectorAll(PAGE_SELECTORS);
    pages.forEach((p) => {
      if (p.getAttribute("data-theme") !== mode) {
        p.setAttribute("data-theme", mode);
      }
      if (!p.classList.contains(`theme-${mode}`)) {
        p.classList.remove("theme-light", "theme-dark");
        p.classList.add(`theme-${mode}`);
      }
    });
  }
}

// Initial application
applyThemeToDom(themeMode.value);

// Automatically sync theme when new pages or popups are mounted in DOM
if (typeof document !== "undefined" && typeof MutationObserver !== "undefined") {
  const observer = new MutationObserver(() => {
    applyThemeToDom(themeMode.value);
  });
  if (document.body) {
    observer.observe(document.body, { childList: true, subtree: true });
  }
}

// Cross-webview / Cross-page event listener
try {
  uni.$on("theme-change", (newMode: ThemeMode) => {
    if (newMode && (newMode === "light" || newMode === "dark") && themeMode.value !== newMode) {
      themeMode.value = newMode;
      applyThemeToDom(newMode);
    }
  });
} catch {
  // Ignore in environments without uni.$on
}

export function setTheme(mode: ThemeMode) {
  themeMode.value = mode;
  try {
    uni.setStorageSync(THEME_STORAGE_KEY, mode);
  } catch {
    // Ignore storage errors
  }
  applyThemeToDom(mode);
  try {
    uni.$emit("theme-change", mode);
  } catch {
    // Ignore
  }
}

export function toggleTheme(): ThemeMode {
  const next = themeMode.value === "light" ? "dark" : "light";
  setTheme(next);
  return next;
}

export function syncThemeFromStorage(): ThemeMode {
  const mode = getInitialTheme();
  if (themeMode.value !== mode) {
    themeMode.value = mode;
  }
  applyThemeToDom(mode);
  return mode;
}

export function useTheme() {
  return {
    themeMode,
    setTheme,
    toggleTheme,
    syncThemeFromStorage,
    syncStatusBarStyle,
    isDark: () => themeMode.value === "dark",
  };
}
