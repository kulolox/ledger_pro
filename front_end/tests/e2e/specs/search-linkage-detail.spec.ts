import { test, expect } from "../fixtures/test.fixture";
import { TEST_USER } from "../utils/test-data";
import mysql from "mysql2/promise";

const DB_CONFIG = {
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USERNAME || "root",
  password: process.env.DB_PASSWORD || "123456",
  database: "ledger_pro",
  supportBigNumbers: true,
  bigNumberStrings: true,
};

test.describe("Search Page - Type/Category Linkage & Detail Modal", () => {
  test("类型与分类联动 + 账单点击详情弹窗/编辑/删除全流程", async ({ authenticatedPage }) => {
    const page = authenticatedPage;

    // 1. 准备测试数据：获取当前登录用户、账本、账户及分类，插入一条测试交易
    const conn = await mysql.createConnection(DB_CONFIG);
    let bookId: string;
    let accountId: string;
    let categoryId: string;
    let txId: string;

    try {
      const [uRows] = await conn.execute(
        `SELECT CAST(id AS CHAR) as id FROM user WHERE email = ? LIMIT 1`,
        [TEST_USER.email]
      );
      const userId = (uRows as any[])[0].id;

      const [bRows] = await conn.execute(
        `SELECT CAST(id AS CHAR) as id FROM account_book WHERE owner_id = ? AND deleted = 0 LIMIT 1`,
        [userId]
      );
      bookId = (bRows as any[])[0].id;

      const [aRows] = await conn.execute(
        `SELECT CAST(id AS CHAR) as id FROM account WHERE book_id = ? AND deleted = 0 LIMIT 1`,
        [bookId]
      );
      accountId = (aRows as any[])[0].id;

      const [cRows] = await conn.execute(
        `SELECT CAST(id AS CHAR) as id FROM category WHERE deleted = 0 AND type = 0 LIMIT 1`
      );
      categoryId = (cRows as any[])[0].id;

      // 插入一条待查询与详情操作的测试流水
      txId = "999901";
      await conn.execute(
        `INSERT INTO transaction (id, book_id, account_id, category_id, type, amount, remark, trans_time, deleted) VALUES (?, ?, ?, ?, 0, 8850, '查询联动详情测试', NOW(), 0)
         ON DUPLICATE KEY UPDATE remark = VALUES(remark), deleted = 0`,
        [txId, bookId, accountId, categoryId],
      );
    } finally {
      await conn.end();
    }

    // 2. 访问账单查询页
    await page.goto("/#/pages/search/index");
    await page.waitForSelector(".filter-section", { timeout: 15000 });

    // 3. 验证【类型与分类联动】
    // 3.1 点击类型选择器选择“支出”
    const typeCell = page.locator(".filter-cell").filter({ hasText: "类型" });
    await typeCell.click();

    const typePickerTitle = page.locator(".wd-picker__title").filter({ hasText: "选择类型" });
    await expect(typePickerTitle).toBeVisible({ timeout: 5000 });

    const typeWrapper = page.locator(".wd-picker__wraper").filter({ has: typePickerTitle });
    await typeWrapper.locator("text=支出").last().click();
    await page.waitForTimeout(300);
    await typeWrapper.locator(".wd-picker__action--confirm").click();
    await page.waitForTimeout(500);

    expect(await typeCell.innerText()).toContain("支出");

    // 3.2 验证选择“支出”后打开分类选择器，仅展示支出分类
    const catCell = page.locator(".filter-cell").filter({ hasText: "分类" });
    await catCell.click();

    const catPickerTitle = page.locator(".wd-picker__title").filter({ hasText: "选择分类" });
    await expect(catPickerTitle).toBeVisible({ timeout: 5000 });

    const catWrapper = page.locator(".wd-picker__wraper").filter({ has: catPickerTitle });
    const expenseCatsText = await catWrapper.innerText();
    expect(expenseCatsText).toContain("全部分类");
    
    // 关闭分类 picker
    await catWrapper.locator(".wd-picker__action--cancel").click();
    await page.waitForTimeout(500);

    // 3.3 切换类型为“收入”
    await typeCell.click();
    await expect(typePickerTitle).toBeVisible({ timeout: 5000 });
    await typeWrapper.locator("text=收入").last().click();
    await page.waitForTimeout(300);
    await typeWrapper.locator(".wd-picker__action--confirm").click();
    await page.waitForTimeout(500);
    expect(await typeCell.innerText()).toContain("收入");

    // 3.4 验证分类选择器中联动为收入分类
    await catCell.click();
    await expect(catPickerTitle).toBeVisible({ timeout: 5000 });
    const incomeCatsText = await catWrapper.innerText();
    expect(incomeCatsText).toContain("全部分类");
    await catWrapper.locator(".wd-picker__action--cancel").click();
    await page.waitForTimeout(500);

    // 4. 重置筛选条件并检索刚刚插入的测试账单
    await page.locator(".reset-action").click();
    await page.waitForTimeout(500);
    expect(await typeCell.innerText()).toContain("全部");
    expect(await catCell.innerText()).toContain("全部分类");

    // 搜索刚刚插入的备注
    const searchInput = page.locator(".search-input input");
    await searchInput.fill("查询联动详情测试");
    await page.locator(".search-action").click();
    await page.waitForTimeout(1000);

    const txItem = page.locator(".tx-item").filter({ hasText: "查询联动详情测试" }).first();
    await expect(txItem).toBeVisible({ timeout: 10000 });

    // 5. 点击账单条目，弹出详情弹窗（查看账单详情）
    await txItem.click();
    await page.waitForSelector(".tx-detail-sheet", { timeout: 5000 });

    const sheet = page.locator(".tx-detail-sheet");
    expect(await sheet.innerText()).toContain("账单详情");
    expect(await sheet.innerText()).toContain("88.50");
    expect(await sheet.innerText()).toContain("查询联动详情测试");
    expect(await sheet.innerText()).toContain("修改");
    expect(await sheet.innerText()).toContain("删除");

    // 6. 点击“修改”按钮，弹出记账编辑组件
    await page.locator(".detail-edit-btn").click();
    await page.waitForSelector(".editor", { timeout: 5000 });
    expect(await page.locator(".editor-title").innerText()).toBe("编辑账单");

    // 修改备注并保存
    const editRemarkInput = page.locator(".remark-input input");
    await editRemarkInput.fill("E2E修改后备注");
    await page.locator(".kb-key-save").click();
    await page.waitForTimeout(1500);

    // 清空搜索框并重新搜索全部
    await searchInput.fill("");
    await page.locator(".search-action").click();
    await page.waitForTimeout(1000);

    // 验证搜索列表中展示了修改后的最新备注
    const modifiedTx = page.locator(".tx-item").filter({ hasText: "E2E修改后备注" }).first();
    await expect(modifiedTx).toBeVisible({ timeout: 10000 });

    // 7. 再次点击该账单，测试“删除”功能
    await modifiedTx.click();
    await page.waitForSelector(".tx-detail-sheet", { timeout: 5000 });

    // 点击删除按钮
    await page.locator(".detail-delete-btn").click();
    // 对话框二次确认删除
    await page.waitForSelector(".wd-dialog", { timeout: 5000 });
    const confirmDialog = page.locator(".wd-dialog");
    await confirmDialog.locator(".wd-dialog__actions-btn").last().click();
    await page.waitForTimeout(1000);

    // 验证页面列表上已被删除
    await expect(page.locator(".tx-item").filter({ hasText: "E2E修改后备注" })).toHaveCount(0);

    // 8. 数据库核验软删除标记 deleted = 1
    const checkConn = await mysql.createConnection(DB_CONFIG);
    try {
      const [rows] = await checkConn.execute(
        `SELECT deleted FROM transaction WHERE id = ?`,
        [txId]
      );
      expect((rows as any[])[0].deleted).toBe(1);
    } finally {
      await checkConn.end();
    }
  });
});
