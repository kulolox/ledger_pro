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

  test("edit custom category opens editor with pre-filled form", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    // create a category to edit
    await catPage.clickAdd();
    await catPage.fillCategoryForm("待编辑", "🎯", "#00b894");
    await catPage.saveCategory();
    await authenticatedPage.waitForTimeout(2000);

    // find and edit the custom category we just created
    const items = catPage.categoryItems;
    const count = await items.count();
    let editIdx = -1;
    for (let i = count - 1; i >= 0; i--) {
      const hasEdit = await catPage.hasEditButton(i);
      if (hasEdit) {
        editIdx = i;
        break;
      }
    }
    expect(editIdx).toBeGreaterThanOrEqual(0);

    await catPage.editCategory(editIdx);
    await authenticatedPage.waitForTimeout(300);

    await expect(catPage.editorTitle).toHaveText("编辑分类");
    expect(await catPage.getFormValue("name")).toBe("待编辑");
    expect(await catPage.getFormValue("icon")).toBe("🎯");
    expect(await catPage.getFormValue("color")).toBe("#00b894");
  });

  test("edit and save updates category name", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    // create a category
    await catPage.clickAdd();
    await catPage.fillCategoryForm("修改前", "📌", "#5b9bd5");
    await catPage.saveCategory();
    await authenticatedPage.waitForTimeout(2000);

    // find and edit it
    const items = catPage.categoryItems;
    const count = await items.count();
    let editIdx = -1;
    for (let i = count - 1; i >= 0; i--) {
      if (await catPage.hasEditButton(i)) {
        editIdx = i;
        break;
      }
    }

    await catPage.editCategory(editIdx);
    await authenticatedPage.waitForTimeout(300);

    // uni-app <uni-input> v-model won't sync with native input events.
    // Use native value setter + dispatch to trigger Vue reactivity.
    await catPage.editorNameInput.locator("input").evaluate((el) => {
      const setter = Object.getOwnPropertyDescriptor(
        HTMLInputElement.prototype, "value"
      ).set;
      setter.call(el, "修改后");
      el.dispatchEvent(new Event("input", { bubbles: true }));
      el.dispatchEvent(new Event("change", { bubbles: true }));
    });
    await catPage.saveCategory();
    await authenticatedPage.waitForTimeout(2000);

    const updatedName = await catPage.getCategoryName(editIdx);
    expect(updatedName).toBe("修改后");
  });

  test("toggle category visibility hides and shows", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    // create custom category so we own it
    await catPage.clickAdd();
    await catPage.fillCategoryForm("可隐藏", "🔒", "#636e72");
    await catPage.saveCategory();
    await authenticatedPage.waitForTimeout(2000);

    // find it
    const items = catPage.categoryItems;
    const count = await items.count();
    let targetIdx = -1;
    for (let i = count - 1; i >= 0; i--) {
      const name = await catPage.getCategoryName(i);
      if (name === "可隐藏") {
        targetIdx = i;
        break;
      }
    }
    expect(targetIdx).toBeGreaterThanOrEqual(0);

    // toggle hide
    expect(await catPage.isCategoryDimmed(targetIdx)).toBe(false);
    await catPage.toggleVisibility(targetIdx);
    await authenticatedPage.waitForTimeout(2000);
    expect(await catPage.isCategoryDimmed(targetIdx)).toBe(true);
  });

  test("system category shows lock icon not edit/delete", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    const items = catPage.categoryItems;
    const count = await items.count();
    let systemIdx = -1;
    for (let i = 0; i < count; i++) {
      if (await catPage.hasLockIcon(i)) {
        systemIdx = i;
        break;
      }
    }
    expect(systemIdx).toBeGreaterThanOrEqual(0);

    expect(await catPage.hasLockIcon(systemIdx)).toBe(true);
    expect(await catPage.hasEditButton(systemIdx)).toBe(false);
    expect(await catPage.hasDeleteButton(systemIdx)).toBe(false);
  });

  test("color preset selection updates color input", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.clickAdd();

    const beforeColor = await catPage.getFormValue("color");
    expect(beforeColor).toBe("");

    await catPage.clickColorPreset(0);
    await authenticatedPage.waitForTimeout(300);

    const afterColor = await catPage.getFormValue("color");
    expect(afterColor).not.toBe("");
  });

  test("save with empty name shows validation", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.goto();

    await catPage.clickAdd();
    await catPage.saveCategory();

    await authenticatedPage.waitForTimeout(500);
    // editor should still be open (validation prevented save)
    await expect(catPage.editor).toBeVisible();
  });

  test("back button returns to home", async ({ authenticatedPage }) => {
    const catPage = new CategoryPage(authenticatedPage);
    await catPage.gotoFromHome();

    await catPage.clickBack();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });
});
