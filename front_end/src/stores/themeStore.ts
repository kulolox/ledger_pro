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

export function applyThemeToDom(mode: ThemeMode = themeMode.value) {
  // #ifdef APP-PLUS
  try {
    // App 端状态栏颜色联动：深色模式文字设为白色(light)，浅色模式文字设为黑色(dark)
    // @ts-ignore
    if (typeof plus !== "undefined" && plus.navigator) {
      // @ts-ignore
      plus.navigator.setStatusBarStyle(mode === "dark" ? "light" : "dark");
    }
  } catch {
    // Ignore native error
  }
  // #endif

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
    isDark: () => themeMode.value === "dark",
  };
}
