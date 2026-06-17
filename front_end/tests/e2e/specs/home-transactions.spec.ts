import { test, expect } from "../fixtures/test.fixture";
import { HomePage } from "../page-objects/HomePage";

test.describe("Home - Transactions", () => {
  test("home page renders after login", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    await expect(homePage.monthLabel).toBeVisible();
    await expect(homePage.fab).toBeVisible();
    await expect(homePage.menuBtn).toBeVisible();
    await expect(homePage.statsBtn).toBeVisible();
  });

  test("month summary shows zero for new user", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);

    const expenseText = await homePage.expenseSummary.textContent();
    const incomeText = await homePage.incomeSummary.textContent();

    expect(expenseText).toBe("-0.00");
    expect(incomeText).toBe("+0.00");
  });

  test("empty state shown when no transactions", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await expect(homePage.emptyState).toBeVisible();
  });

  test("add transaction button opens editor", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickAdd();

    await expect(authenticatedPage.locator(".editor-wrapper")).toBeVisible();
  });

  test("month navigation changes month label", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    const initialLabel = await homePage.getMonthLabel();

    await homePage.prevMonth();
    await authenticatedPage.waitForTimeout(300);
    const afterPrevLabel = await homePage.getMonthLabel();
    expect(afterPrevLabel).not.toBe(initialLabel);

    await homePage.nextMonth();
    await authenticatedPage.waitForTimeout(300);
    const afterNextLabel = await homePage.getMonthLabel();
    expect(afterNextLabel).toBe(initialLabel);
  });

  test("menu button visible and clickable", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    await expect(authenticatedPage.locator(".sidebar")).toBeVisible();
  });

  test("stats button navigates to statistics page", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickStats();

    await expect(authenticatedPage.locator(".stats-page")).toBeVisible();
  });
});
