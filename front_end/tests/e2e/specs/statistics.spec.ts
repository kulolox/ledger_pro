import { test, expect } from "../fixtures/test.fixture";
import { StatisticsPage } from "../page-objects/StatisticsPage";

test.describe("Statistics", () => {
  test("statistics page renders", async ({ authenticatedPage }) => {
    const statsPage = new StatisticsPage(authenticatedPage);
    await statsPage.goto();

    await expect(statsPage.expenseSummary).toBeVisible();
    await expect(statsPage.incomeSummary).toBeVisible();
    await expect(statsPage.balanceSummary).toBeVisible();
    await expect(statsPage.expenseTab).toBeVisible();
    await expect(statsPage.incomeTab).toBeVisible();
  });

  test("empty state shown when no data", async ({ authenticatedPage }) => {
    const statsPage = new StatisticsPage(authenticatedPage);
    await statsPage.goto();

    const expenseText = await statsPage.expenseSummary.textContent();
    const incomeText = await statsPage.incomeSummary.textContent();

    expect(expenseText).toBe("-0.00");
    expect(incomeText).toBe("+0.00");
  });

  test("switch between expense and income tabs", async ({ authenticatedPage }) => {
    const statsPage = new StatisticsPage(authenticatedPage);
    await statsPage.goto();

    await expect(statsPage.expenseTab.locator("..")).toHaveClass(/active/);

    await statsPage.switchToIncome();
    await authenticatedPage.waitForTimeout(300);
    await expect(statsPage.incomeTab.locator("..")).toHaveClass(/active/);

    await statsPage.switchToExpense();
    await authenticatedPage.waitForTimeout(300);
    await expect(statsPage.expenseTab.locator("..")).toHaveClass(/active/);
  });

  test("back button returns to home", async ({ authenticatedPage }) => {
    const statsPage = new StatisticsPage(authenticatedPage);
    await statsPage.goto();

    await authenticatedPage.waitForTimeout(500);
    await statsPage.clickBack();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("month navigation works", async ({ authenticatedPage }) => {
    const statsPage = new StatisticsPage(authenticatedPage);
    await statsPage.goto();

    const initialLabel = await statsPage.monthLabel.textContent();

    const prevArrow = authenticatedPage.locator(".nav-arrow").first();
    await prevArrow.click();
    await authenticatedPage.waitForTimeout(500);
    expect(await statsPage.monthLabel.textContent()).not.toBe(initialLabel);
  });
});
