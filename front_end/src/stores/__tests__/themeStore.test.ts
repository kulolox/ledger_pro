import { describe, it, expect, beforeEach, vi } from "vitest";
import { useTheme, setTheme, toggleTheme } from "../themeStore";

describe("themeStore", () => {
  let storage: Record<string, string> = {};

  beforeEach(() => {
    storage = {};
    vi.mocked(uni.getStorageSync).mockImplementation((k: string) => storage[k] || "");
    vi.mocked(uni.setStorageSync).mockImplementation((k: string, v: string) => {
      storage[k] = v;
    });
    setTheme("light");
  });

  it("initializes with light theme by default", () => {
    const { themeMode } = useTheme();
    expect(themeMode.value).toBe("light");
  });

  it("sets theme to dark and persists in storage", () => {
    const { themeMode } = useTheme();
    setTheme("dark");
    expect(themeMode.value).toBe("dark");
    expect(storage["theme_mode"]).toBe("dark");
  });

  it("toggles theme between light and dark", () => {
    const { themeMode } = useTheme();
    expect(themeMode.value).toBe("light");

    const t1 = toggleTheme();
    expect(t1).toBe("dark");
    expect(themeMode.value).toBe("dark");

    const t2 = toggleTheme();
    expect(t2).toBe("light");
    expect(themeMode.value).toBe("light");
  });
});
