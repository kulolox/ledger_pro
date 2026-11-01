import { ref } from "vue";

export type ThemeMode = "light" | "dark";

const THEME_STORAGE_KEY = "theme_mode";

function getInitialTheme(): ThemeMode {
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
  "uni-page-body, page, .home-page, .login-page, .auth-page, .stats-page, .category-page, .search-page, .page";

export function applyThemeToDom(mode: ThemeMode = themeMode.value) {
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

export function setTheme(mode: ThemeMode) {
  themeMode.value = mode;
  try {
    uni.setStorageSync(THEME_STORAGE_KEY, mode);
  } catch {
    // Ignore storage errors
  }
  applyThemeToDom(mode);
}

export function toggleTheme(): ThemeMode {
  const next = themeMode.value === "light" ? "dark" : "light";
  setTheme(next);
  return next;
}

export function useTheme() {
  return {
    themeMode,
    setTheme,
    toggleTheme,
    isDark: () => themeMode.value === "dark",
  };
}
