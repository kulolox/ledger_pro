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

  test("navigate to category management from sidebar", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "分类管理" }).click();

    await authenticatedPage.waitForSelector(".category-page", { timeout: 10000 });
    await expect(authenticatedPage.locator(".category-page")).toBeVisible();
    await expect(authenticatedPage.locator(".category-item").first()).toBeVisible();
  });

  test("navigate to import-export from sidebar", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "导入导出" }).click();

    await authenticatedPage.waitForSelector(".page", { timeout: 10000 });
    await expect(authenticatedPage.locator(".export-btn")).toBeVisible();
  });

  test("navigate to statistics via stats button", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickStats();

    await expect(authenticatedPage.locator(".stats-page")).toBeVisible();
  });

  test("back from category page returns to home", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "分类管理" }).click();
    await authenticatedPage.waitForSelector(".category-page", { timeout: 10000 });

    await authenticatedPage.locator(".nav-back").click();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("back from import-export page returns to home", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "导入导出" }).click();
    await authenticatedPage.waitForSelector(".page", { timeout: 10000 });

    await authenticatedPage.locator(".back-btn").click();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("back from statistics page returns to home", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickStats();
    await authenticatedPage.waitForSelector(".stats-page", { timeout: 10000 });

    await authenticatedPage.locator(".nav-left").click();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("sync button clickable in sidebar", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "同步数据" }).click();

    await authenticatedPage.waitForTimeout(500);
  });

  test("logout item visible in sidebar", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await homePage.clickMenu();
    const sidebar = authenticatedPage.locator(".sidebar");
    await expect(sidebar.locator(".logout-item")).toBeVisible();
  });
});
