import { test, expect } from "../fixtures/test.fixture";
import { HomePage } from "../page-objects/HomePage";

test.describe("Sidebar", () => {
  test("sidebar opens and shows user info", async ({ authenticatedPage, testUser }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    const sidebar = authenticatedPage.locator(".sidebar");
    await expect(sidebar).toBeVisible();
    await expect(sidebar.locator(".user-name")).toBeVisible();
    await expect(sidebar.locator(".user-email")).toBeVisible();
  });

  test("sidebar shows book list", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    const sidebar = authenticatedPage.locator(".sidebar");
    await expect(sidebar.locator(".book-list")).toBeVisible();
    const bookItems = sidebar.locator(".book-item");
    expect(await bookItems.count()).toBeGreaterThanOrEqual(1);

    const activeBook = sidebar.locator(".book-item.active");
    expect(await activeBook.count()).toBe(1);
  });

  test("add book dialog opens and closes", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".section-add-btn").click();

    const dialog = authenticatedPage.locator(".add-book-dialog-content");
    await expect(dialog).toBeVisible();

    await dialog.locator(".dialog-btn.cancel").click();
    await authenticatedPage.waitForTimeout(300);
    await expect(dialog).not.toBeVisible();
  });

  test("sidebar has management links", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    const sidebar = authenticatedPage.locator(".sidebar");
    const sidebarText = await sidebar.textContent();
    expect(sidebarText).toContain("分类管理");
    expect(sidebarText).toContain("导入导出");
  });
});
