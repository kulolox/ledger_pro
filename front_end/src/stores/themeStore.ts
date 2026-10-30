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

function applyThemeToDom(mode: ThemeMode) {
  if (typeof document !== "undefined") {
    document.documentElement.setAttribute("data-theme", mode);
    document.documentElement.style.colorScheme = mode;
    document.body.classList.remove("theme-light", "theme-dark");
    document.body.classList.add(`theme-${mode}`);
    document.body.setAttribute("data-theme", mode);

    const appEl = document.querySelector("uni-app");
    if (appEl) {
      appEl.setAttribute("data-theme", mode);
      appEl.classList.remove("theme-light", "theme-dark");
      appEl.classList.add(`theme-${mode}`);
    }

    const pages = document.querySelectorAll("uni-page-body, page, .home-page");
    pages.forEach((p) => {
      p.setAttribute("data-theme", mode);
      p.classList.remove("theme-light", "theme-dark");
      p.classList.add(`theme-${mode}`);
    });
  }
}

// Initial application
applyThemeToDom(themeMode.value);

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
