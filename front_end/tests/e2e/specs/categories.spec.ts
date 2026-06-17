import { test, expect } from "../fixtures/test.fixture";
import { CategoryPage } from "../page-objects/CategoryPage";

test.describe("Categories", () => {
  test("category page renders with categories", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await expect(catPage.expenseTab).toBeVisible();
    await expect(catPage.incomeTab).toBeVisible();
    await expect(catPage.sortBtn).toBeVisible();
    await expect(catPage.addBtn).toBeVisible();

    const count = await catPage.getCategoryCount();
    expect(count).toBeGreaterThan(0);
  });

  test("switch between expense and income tabs", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    const expenseCount = await catPage.getCategoryCount();
    expect(expenseCount).toBeGreaterThan(0);

    await catPage.switchToIncome();
    await authenticatedPage.waitForTimeout(300);

    const incomeCount = await catPage.getCategoryCount();
    expect(incomeCount).toBeGreaterThan(0);
  });

  test("toggle sort mode", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.toggleSort();
    await authenticatedPage.waitForTimeout(300);
    await expect(authenticatedPage.locator(".sort-handle").first()).toBeVisible();

    await catPage.toggleSort();
    await authenticatedPage.waitForTimeout(300);
    await expect(catPage.addBtn).toBeVisible();
  });

  test("add category opens editor", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.clickAdd();
    await expect(catPage.editor).toBeVisible();
    await expect(catPage.editorSaveBtn).toBeVisible();
    await expect(catPage.editorCancelBtn).toBeVisible();
  });

  test("cancel add category closes editor", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.clickAdd();
    await catPage.cancelCategory();
    await authenticatedPage.waitForTimeout(500);
    await expect(catPage.editor).not.toBeVisible();
  });

  test("add and delete custom category", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    const beforeCount = await catPage.getCategoryCount();

    // add new category
    await catPage.clickAdd();
    await catPage.fillCategoryForm("测试分类", "📱", "#ff6b35");
    await catPage.saveCategory();
    await authenticatedPage.waitForTimeout(2000);

    const afterCount = await catPage.getCategoryCount();
    expect(afterCount).toBe(beforeCount + 1);

    // delete the custom category (last item should be it, or find by name)
    const items = catPage.categoryItems;
    const count = await items.count();
    // custom categories have delete button; system ones have lock icon
    // click delete on last non-system item
    for (let i = count - 1; i >= 0; i--) {
      const deleteBtn = items.nth(i).locator(".cat-action.delete");
      if ((await deleteBtn.count()) > 0) {
        await deleteBtn.click();
        await authenticatedPage.waitForTimeout(500);

        // confirm in modal
        const confirmBtn = authenticatedPage.locator(".wd-message-box__actions .wd-message-box__action-btn").last();
        if ((await confirmBtn.count()) > 0) {
          await confirmBtn.click();
        }
        await authenticatedPage.waitForTimeout(2000);
        break;
      }
    }
  });

  test("back button returns to home", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.clickBack();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });
});
