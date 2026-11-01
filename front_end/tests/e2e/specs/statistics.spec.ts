import { test, expect } from "../fixtures/test.fixture";
import { HomePage } from "../page-objects/HomePage";

async function pressKey(page: any, key: string) {
  await page.locator(".kb-key").filter({ hasText: key }).click();
}

async function inputAmount(page: any, amount: string) {
  for (const ch of amount) {
    await pressKey(page, ch);
  }
}

test.describe("Statistics - Apple Style & Charts & Empty State", () => {
  test("shows apple donut empty placeholder when no data exists", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickStats();

    await expect(authenticatedPage.locator(".stats-page")).toBeVisible();
    await expect(authenticatedPage.locator(".month-capsule")).toBeVisible();

    // Verify empty state donut ring for expense
    const emptySection = authenticatedPage.locator(".empty-chart-section");
    await expect(emptySection).toBeVisible();
    await expect(emptySection.locator(".empty-donut-ring")).toBeVisible();
    await expect(emptySection.locator(".empty-donut-value")).toHaveText("0.00");
    await expect(emptySection.locator(".empty-donut-label")).toHaveText("暂无支出");
    await expect(emptySection.locator(".empty-action-btn")).toBeVisible();

    // Switch to income tab
    const incomeTab = authenticatedPage.locator(".wd-tabs__nav-item").filter({ hasText: "收入" });
    await incomeTab.click();
    await authenticatedPage.waitForTimeout(300);

    // Verify empty state for income
    await expect(emptySection.locator(".empty-donut-label")).toHaveText("暂无收入");

    // Switch back to expense tab
    const expenseTab = authenticatedPage.locator(".wd-tabs__nav-item").filter({ hasText: "支出" });
    await expenseTab.click();
    await authenticatedPage.waitForTimeout(300);

    // Click "记一笔" button in empty state navigates to home page
    await emptySection.locator(".empty-action-btn").click();
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("displays donut chart and category list after recording a transaction", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    // Create an expense
    await homePage.clickAdd();
    const editor = authenticatedPage.locator(".editor");
    await expect(editor).toBeVisible();

    await inputAmount(authenticatedPage, "68.50");
    const remarkInput = editor.locator(".remark-input input");
    await remarkInput.fill("苹果风拿铁");
    await pressKey(authenticatedPage, "保存");

    // Wait for editor to close
    await expect(editor).toBeHidden({ timeout: 5000 });

    // Navigate to statistics
    await homePage.clickStats();
    await expect(authenticatedPage.locator(".stats-page")).toBeVisible();

    // Donut chart section should be visible
    const chartSection = authenticatedPage.locator(".chart-section");
    await expect(chartSection).toBeVisible();

    // Category list should display top insights header and category items
    const categoryList = authenticatedPage.locator(".category-list");
    await expect(categoryList).toBeVisible();
    await expect(categoryList.locator(".insights-header")).toBeVisible();
    const firstItem = categoryList.locator(".category-item").first();
    await expect(firstItem).toBeVisible();
    await expect(firstItem.locator(".cat-amount")).toContainText("68.50");
    await expect(firstItem.locator(".cat-percent")).toContainText("100.0%");

    // Empty state should NOT be visible
    await expect(authenticatedPage.locator(".empty-chart-section")).toBeHidden();
  });

  test("month capsule allows selecting year and month via popup", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickStats();

    const capsule = authenticatedPage.locator(".month-capsule");
    await expect(capsule).toBeVisible();

    const monthDisplay = capsule.locator(".capsule-label");
    const initialText = await monthDisplay.textContent();

    // Click capsule to open Year-Month popup and select a different month
    await capsule.click();
    await authenticatedPage.waitForTimeout(300);
    const stepPrevYear = authenticatedPage.locator(".year-step-btn").first();
    await stepPrevYear.click();
    await authenticatedPage.locator(".month-cell").first().click();
    await authenticatedPage.waitForTimeout(300);

    const prevText = await monthDisplay.textContent();
    expect(prevText).not.toBe(initialText);

    // Click capsule and use "回到本月" button to return
    await capsule.click();
    await authenticatedPage.waitForTimeout(300);
    await authenticatedPage.locator(".today-btn").click();
    await authenticatedPage.waitForTimeout(300);

    const backText = await monthDisplay.textContent();
    expect(backText).toBe(initialText);
  });
});
