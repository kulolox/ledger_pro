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

test.describe("Category Archive & Delete Flow", () => {
  test("场景一：有账单分类选择【归档到新分类】并删除", async ({ authenticatedPage }) => {
    const page = authenticatedPage;

    await page.goto("/#/pages/category/index");
    await page.waitForSelector(".category-item", { timeout: 15000 });

    // 1. 获取当前登录用户的真实 userId 和 bookId
    const conn = await mysql.createConnection(DB_CONFIG);
    let userId: string;
    let bookId: string;
    let accountId: string;
    try {
      const [uRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM user WHERE email = ? LIMIT 1`, [TEST_USER.email]);
      userId = (uRows as any[])[0].id;

      const [bRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM account_book WHERE owner_id = ? AND deleted = 0 LIMIT 1`, [userId]);
      bookId = (bRows as any[])[0].id;

      const [aRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM account WHERE book_id = ? AND deleted = 0 LIMIT 1`, [bookId]);
      accountId = (aRows as any[])[0].id;
    } finally {
      await conn.end();
    }

    // 2. 添加一个测试自定义分类
    await page.locator(".add-btn").click();
    await page.waitForSelector(".editor-form", { timeout: 5000 });

    const nameInput = page.locator(".form-item input").first();
    await nameInput.fill("待归档测试");

    const iconInput = page.locator(".form-item .emoji-input input");
    await iconInput.fill("🎮");

    await page.locator(".editor-confirm").click();
    await page.waitForSelector("text=待归档测试", { timeout: 10000 });

    // 3. 连接数据库查询新分类 ID 并插入 2 条账单
    const insertConn = await mysql.createConnection(DB_CONFIG);
    let catId: string;
    try {
      const [rows] = await insertConn.execute(
        `SELECT CAST(id AS CHAR) as id FROM user_category WHERE user_id = ? AND name = '待归档测试' AND deleted = 0 LIMIT 1`,
        [userId]
      );
      catId = (rows as any[])[0].id;

      await insertConn.execute(
        `INSERT INTO transaction (id, book_id, type, amount, account_id, category_id, trans_time, remark, deleted) VALUES 
         (888801, ?, 0, 2000, ?, ?, NOW(), '测试归档流水1', 0),
         (888802, ?, 0, 3500, ?, ?, NOW(), '测试归档流水2', 0)
         ON DUPLICATE KEY UPDATE category_id = VALUES(category_id), deleted = 0`,
        [bookId, accountId, catId, bookId, accountId, catId]
      );
    } finally {
      await insertConn.end();
    }

    // 4. 点击删除按钮
    const catRow = page.locator(".category-item", { hasText: "待归档测试" });
    const deleteBtn = catRow.locator(".cat-action.delete");
    await deleteBtn.click();

    // 5. 等待专属归档弹窗弹出
    await page.waitForSelector(".delete-modal", { timeout: 10000 });
    const modalText = await page.locator(".delete-modal").innerText();
    expect(modalText).toContain("有关联账单共");
    expect(modalText).toContain("2");
    expect(modalText).toContain("笔");

    // 6. 选中第一个可选目标分类
    const targetItem = page.locator(".target-cat-item").first();
    await targetItem.click();
    await page.waitForTimeout(300);

    // 7. 提交归档并删除
    const submitBtn = page.locator(".delete-modal .submit-btn");
    await submitBtn.click();
    await page.waitForTimeout(2500);

    // 8. 验证数据库中分类被软删，且账单已被迁移
    const checkConn = await mysql.createConnection(DB_CONFIG);
    try {
      const [cRows] = await checkConn.execute(`SELECT deleted FROM user_category WHERE id = ?`, [catId]);
      expect((cRows as any[])[0].deleted).toBe(1);

      const [txRows] = await checkConn.execute(
        `SELECT CAST(category_id AS CHAR) as category_id, deleted FROM transaction WHERE id IN (888801, 888802)`
      );
      const rows = txRows as any[];
      expect(rows.length).toBe(2);
      expect(rows[0].category_id).not.toBe(catId);
      expect(rows[0].deleted).toBe(0);
      expect(rows[1].deleted).toBe(0);
    } finally {
      await checkConn.end();
    }
  });

  test("场景二：有账单分类选择【连同账单一起删除】", async ({ authenticatedPage }) => {
    const page = authenticatedPage;

    await page.goto("/#/pages/category/index");
    await page.waitForSelector(".category-item", { timeout: 15000 });

    const conn = await mysql.createConnection(DB_CONFIG);
    let userId: string;
    let bookId: string;
    let accountId: string;
    try {
      const [uRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM user WHERE email = ? LIMIT 1`, [TEST_USER.email]);
      userId = (uRows as any[])[0].id;

      const [bRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM account_book WHERE owner_id = ? AND deleted = 0 LIMIT 1`, [userId]);
      bookId = (bRows as any[])[0].id;

      const [aRows] = await conn.execute(`SELECT CAST(id AS CHAR) as id FROM account WHERE book_id = ? AND deleted = 0 LIMIT 1`, [bookId]);
      accountId = (aRows as any[])[0].id;
    } finally {
      await conn.end();
    }

    // 1. 添加测试分类
    await page.locator(".add-btn").click();
    await page.waitForSelector(".editor-form", { timeout: 5000 });

    const nameInput = page.locator(".form-item input").first();
    await nameInput.fill("连同删除分类");

    const iconInput = page.locator(".form-item .emoji-input input");
    await iconInput.fill("🗑");

    await page.locator(".editor-confirm").click();
    await page.waitForSelector("text=连同删除分类", { timeout: 10000 });

    // 2. 插入 3 条关联账单
    const insertConn = await mysql.createConnection(DB_CONFIG);
    let catId: string;
    try {
      const [rows] = await insertConn.execute(
        `SELECT CAST(id AS CHAR) as id FROM user_category WHERE user_id = ? AND name = '连同删除分类' AND deleted = 0 LIMIT 1`,
        [userId]
      );
      catId = (rows as any[])[0].id;

      await insertConn.execute(
        `INSERT INTO transaction (id, book_id, type, amount, account_id, category_id, trans_time, remark, deleted) VALUES 
         (777701, ?, 0, 1000, ?, ?, NOW(), '连删流水1', 0),
         (777702, ?, 0, 2000, ?, ?, NOW(), '连删流水2', 0),
         (777703, ?, 0, 3000, ?, ?, NOW(), '连删流水3', 0)
         ON DUPLICATE KEY UPDATE category_id = VALUES(category_id), deleted = 0`,
        [bookId, accountId, catId, bookId, accountId, catId, bookId, accountId, catId]
      );
    } finally {
      await insertConn.end();
    }

    // 3. 点击删除按钮
    const catRow = page.locator(".category-item", { hasText: "连同删除分类" });
    const deleteBtn = catRow.locator(".cat-action.delete");
    await deleteBtn.click();

    // 4. 等待专属归档弹窗弹出
    await page.waitForSelector(".delete-modal", { timeout: 10000 });
    const modalText = await page.locator(".delete-modal").innerText();
    expect(modalText).toContain("有关联账单共");
    expect(modalText).toContain("3");
    expect(modalText).toContain("笔");

    // 5. 切换到【连同账单一起删除】Tab
    const deleteAllTab = page.locator(".action-tab-item.danger");
    await deleteAllTab.click();
    await page.waitForTimeout(500);

    // 6. 点击红色危险按钮
    const dangerSubmitBtn = page.locator(".delete-modal .submit-btn.danger-btn");
    await dangerSubmitBtn.click();

    // 7. 处理二次防呆确认弹窗
    await page.waitForSelector(".wd-dialog", { timeout: 5000 });
    const confirmDialog = page.locator(".wd-dialog");
    expect(await confirmDialog.innerText()).toContain("危险警示");
    // 点击确定按钮
    await confirmDialog.locator(".wd-dialog__actions-btn").last().click();
    await page.waitForTimeout(2500);

    // 8. 数据库核验：分类与 3 条账单均被成功软删除（deleted = 1）
    const checkConn = await mysql.createConnection(DB_CONFIG);
    try {
      const [cRows] = await checkConn.execute(`SELECT deleted FROM user_category WHERE id = ?`, [catId]);
      expect((cRows as any[])[0].deleted).toBe(1);

      const [txRows] = await checkConn.execute(
        `SELECT deleted FROM transaction WHERE id IN (777701, 777702, 777703)`
      );
      const rows = txRows as any[];
      expect(rows.length).toBe(3);
      expect(rows[0].deleted).toBe(1);
      expect(rows[1].deleted).toBe(1);
      expect(rows[2].deleted).toBe(1);
    } finally {
      await checkConn.end();
    }
  });
});
