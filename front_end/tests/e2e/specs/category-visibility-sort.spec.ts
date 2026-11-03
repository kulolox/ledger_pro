import { test, expect } from "../fixtures/test.fixture";

test.describe("Category Visibility and Reorder Interaction Flow", () => {
  test("分类拖拽手柄常驻、右侧按钮重排（删除-编辑-眼睛）与隐藏折叠恢复完整流程", async ({
    authenticatedPage,
  }) => {
    const page = authenticatedPage;

    await page.goto("/#/pages/category/index");
    await page.waitForSelector(".category-item", { timeout: 15000 });

    // 1. 验证右上角无排序按钮（已优化掉）
    const sortBtn = page.locator(".nav-sort");
    await expect(sortBtn).toHaveCount(0);

    // 2. 验证可见分类行首左侧常驻拖拽手柄
    const firstCat = page.locator(".category-list .category-item").first();
    const sortHandle = firstCat.locator(".sort-handle");
    await expect(sortHandle).toBeVisible();

    // 3. 验证自定义分类右侧按钮排布：删除 -> 编辑 -> 眼睛
    // 找一个非系统分类（有 delete 按钮的行）或者系统分类
    const catWithDelete = page.locator(".category-list .category-item", {
      has: page.locator(".cat-action.delete"),
    }).first();

    const countWithDelete = await catWithDelete.count();
    if (countWithDelete > 0) {
      const actions = catWithDelete.locator(".cat-right > view");
      // 确认按顺序包含 delete, edit, visibility
      await expect(actions.nth(0)).toHaveClass(/delete/);
      await expect(actions.nth(1)).toHaveClass(/edit/);
      await expect(actions.nth(2)).toHaveClass(/visibility/);
    }

    // 4. 当前默认无已隐藏分类卡片
    await expect(page.locator(".hidden-section")).toHaveCount(0);

    // 5. 点击首个分类的眼睛图标进行隐藏
    const firstCatName = await firstCat.locator(".cat-name").innerText();
    const eyeBtn = firstCat.locator(".cat-action.visibility");
    await eyeBtn.click();

    // 6. 出现已隐藏分类卡片，且徽标为 1
    await page.waitForSelector(".hidden-section", { timeout: 5000 });
    const hiddenTitle = page.locator(".hidden-section .hidden-title");
    await expect(hiddenTitle).toHaveText("已隐藏分类");
    const hiddenBadge = page.locator(".hidden-section .hidden-badge");
    await expect(hiddenBadge).toHaveText("1");

    // 7. 验证已隐藏分类列表中包含刚刚隐藏的分类名称，且隐藏项无拖拽手柄
    const hiddenItem = page.locator(".hidden-section .category-item").first();
    await expect(hiddenItem.locator(".cat-name")).toHaveText(firstCatName);
    await expect(hiddenItem.locator(".cat-hidden-tag")).toHaveText("已隐藏");
    await expect(hiddenItem.locator(".sort-handle")).toHaveCount(0);

    // 8. 验证已隐藏分类手风琴可折叠/展开
    const hiddenHeader = page.locator(".hidden-section-header");
    await hiddenHeader.click();
    await page.waitForTimeout(300);
    await expect(page.locator(".hidden-list")).toHaveCount(0);
    // 再次点击展开
    await hiddenHeader.click();
    await page.waitForTimeout(300);
    await expect(page.locator(".hidden-list")).toHaveCount(1);

    // 9. 点击已隐藏分类的眼睛恢复显示
    const restoreEyeBtn = hiddenItem.locator(".cat-action.visibility");
    await restoreEyeBtn.click();
    await page.waitForTimeout(1000);

    // 10. 已隐藏分类卡片消失，可见列表中恢复且位于末尾
    await expect(page.locator(".hidden-section")).toHaveCount(0);
    const lastCat = page.locator(".category-list .category-item").last();
    await expect(lastCat.locator(".cat-name")).toHaveText(firstCatName);
    await expect(lastCat.locator(".sort-handle")).toBeVisible();
  });
});
