import { test, expect } from "../fixtures/test.fixture";
import { HomePage } from "../page-objects/HomePage";

test.describe("Theme - Apple Style & Dual Theme Toggle", () => {
  test("default theme is light or system and can toggle between light and dark", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    // 1. Verify Home Page is loaded
    await expect(homePage.monthLabel).toBeVisible();

    // 2. Open Sidebar
    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await expect(sidebar).toBeVisible();

    // 3. Find theme switcher segmented control
    const themeSwitcher = sidebar.locator(".theme-segmented");
    await expect(themeSwitcher).toBeVisible();

    const lightOption = themeSwitcher.locator(".segmented-item").first();
    const darkOption = themeSwitcher.locator(".segmented-item").nth(1);

    // 4. Click dark option
    await darkOption.click();
    await authenticatedPage.waitForTimeout(300);

    // Verify dark theme applied on document element
    const themeOnHtml = await authenticatedPage.evaluate(() => {
      return document.documentElement.getAttribute("data-theme");
    });
    expect(themeOnHtml).toBe("dark");

    // Verify localStorage persistence
    const storedTheme = await authenticatedPage.evaluate(() => {
      return localStorage.getItem("theme_mode");
    });
    expect(storedTheme).toBe("dark");

    // Verify background color updated to dark (#000000 or similar dark surface)
    const bgColor = await authenticatedPage.evaluate(() => {
      return getComputedStyle(document.body).backgroundColor;
    });
    // In dark mode, background should be dark (rgb(0, 0, 0))
    expect(bgColor).toMatch(/rgb\(0,\s*0,\s*0\)/);

    // 5. Click light option
    await lightOption.click();
    await authenticatedPage.waitForTimeout(300);

    const themeAfterLight = await authenticatedPage.evaluate(() => {
      return document.documentElement.getAttribute("data-theme");
    });
    expect(themeAfterLight).toBe("light");

    const storedThemeLight = await authenticatedPage.evaluate(() => {
      return localStorage.getItem("theme_mode");
    });
    expect(storedThemeLight).toBe("light");

    // Close sidebar by clicking outside the drawer
    await authenticatedPage.mouse.click(360, 200);
    await authenticatedPage.waitForTimeout(500);
  });
});
