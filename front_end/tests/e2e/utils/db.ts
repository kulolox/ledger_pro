import mysql from "mysql2/promise";
import bcrypt from "bcryptjs";
import { TEST_USER, TEST_BOOK_NAME } from "./test-data";

const DB_CONFIG = {
  host: process.env.DB_HOST || "localhost",
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USERNAME || "root",
  password: process.env.DB_PASSWORD || "123456",
  database: "ledger_pro",
};

export interface TestUserRow {
  userId: number;
  bookId: number;
  accountId: number;
}

export async function createTestUser(): Promise<TestUserRow> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    // 幂等清理可能残留的同邮箱测试用户
    const [existing] = await conn.execute("SELECT id FROM `user` WHERE email = ?", [TEST_USER.email]);
    for (const u of existing as any[]) {
      const [books] = await conn.execute("SELECT id FROM `account_book` WHERE owner_id = ?", [u.id]);
      for (const b of books as any[]) {
        await conn.execute("DELETE FROM `account` WHERE book_id = ?", [b.id]);
        await conn.execute("DELETE FROM `transaction` WHERE book_id = ?", [b.id]);
      }
      await conn.execute("DELETE FROM `account_book` WHERE owner_id = ?", [u.id]);
      await conn.execute("DELETE FROM `user_category` WHERE user_id = ?", [u.id]);
      await conn.execute("DELETE FROM `user` WHERE id = ?", [u.id]);
    }

    const baseId = Date.now() * 1000 + Math.floor(Math.random() * 1000);
    const userId = baseId;
    const bookId = baseId + 1;
    const accountId = baseId + 2;

    const hash = await bcrypt.hash(TEST_USER.password, 10);

    await conn.execute(
      "INSERT INTO `user` (id, nickname, email, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())",
      [userId, TEST_USER.nickname, TEST_USER.email, hash],
    );

    await conn.execute(
      "INSERT INTO `account_book` (id, name, owner_id, type, created_at, updated_at) VALUES (?, ?, ?, 0, NOW(), NOW())",
      [bookId, TEST_BOOK_NAME, userId],
    );

    await conn.execute(
      "INSERT INTO `account` (id, book_id, name, type, icon, created_at, updated_at) VALUES (?, ?, '现金', 0, '💵', NOW(), NOW())",
      [accountId, bookId],
    );

    let catId = baseId + 10;
    const [categories] = await conn.execute("SELECT id FROM `category`");
    for (const row of categories as any[]) {
      catId++;
      await conn.execute(
        `INSERT INTO user_category (id, user_id, name, type, icon, color, parent_id, is_system, visible, sort_order, created_at, updated_at)
         SELECT ?, ?, name, type, icon, color, parent_id, 1, 1, sort_order, NOW(), NOW()
         FROM category WHERE id = ?`,
        [catId, userId, row.id],
      );
    }

    return { userId, bookId, accountId };
  } finally {
    await conn.end();
  }
}

export async function createTestTransactions(bookId: number, accountId: number, categoryIds: { expense: number; income: number; other: number }): Promise<void> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    let txId = Date.now() * 1000 + Math.floor(Math.random() * 1000);
    await conn.execute(
      `INSERT INTO transaction (id, book_id, type, amount, account_id, category_id, remark, trans_time, created_at, updated_at) VALUES
       (?, ?, 0, 2550, ?, ?, '午餐', '2025-01-15 12:30:00', NOW(), NOW()),
       (?, ?, 1, 500000, ?, ?, '工资', '2025-01-15 09:00:00', NOW(), NOW()),
       (?, ?, 0, 1500, ?, ?, '交通费', '2025-01-20 08:00:00', NOW(), NOW())`,
      [txId++, bookId, accountId, categoryIds.expense, txId++, bookId, accountId, categoryIds.income, txId++, bookId, accountId, categoryIds.expense],
    );
  } finally {
    await conn.end();
  }
}

export async function getCategoryIds(userId: number): Promise<{ expense: number; income: number; other: number }> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    const [expenseRows] = await conn.execute(
      "SELECT id FROM user_category WHERE user_id = ? AND type = 0 AND name != '其他' LIMIT 1", [userId]
    );
    const [incomeRows] = await conn.execute(
      "SELECT id FROM user_category WHERE user_id = ? AND type = 1 AND name != '其他' LIMIT 1", [userId]
    );
    const [otherRows] = await conn.execute(
      "SELECT id FROM user_category WHERE user_id = ? AND type = 0 AND name = '其他' LIMIT 1", [userId]
    );
    return {
      expense: (expenseRows as any[])[0]?.id,
      income: (incomeRows as any[])[0]?.id,
      other: (otherRows as any[])[0]?.id,
    };
  } finally {
    await conn.end();
  }
}

export async function getTransactionCount(bookId: number): Promise<number> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    const [rows] = await conn.execute(
      "SELECT COUNT(*) as cnt FROM `transaction` WHERE book_id = ? AND deleted = 0", [bookId]
    );
    return (rows as any[])[0]?.cnt || 0;
  } finally {
    await conn.end();
  }
}

export async function getLatestTransactionCategory(bookId: number): Promise<number | null> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    const [rows] = await conn.execute(
      "SELECT category_id FROM `transaction` WHERE book_id = ? AND deleted = 0 ORDER BY id DESC LIMIT 1", [bookId]
    );
    return (rows as any[])[0]?.category_id ?? null;
  } finally {
    await conn.end();
  }
}

export async function cleanupTestTransactions(bookId: number): Promise<void> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    await conn.execute("DELETE FROM `transaction` WHERE book_id = ?", [bookId]);
  } finally {
    await conn.end();
  }
}

export async function exportTransactionsApi(bookId: number, params: Record<string, string>, token: string): Promise<{ status: number; body: string; contentType: string }> {
  const query = new URLSearchParams({ book_id: String(bookId), ...params }).toString();
  const url = `http://localhost:8114/api/v1/transactions/export?${query}`;
  const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
  const contentType = res.headers.get("content-type") || "";
  const body = await res.text();
  return { status: res.status, body, contentType };
}

export async function downloadTemplateApi(format: string, token: string): Promise<{ status: number; body: string; contentType: string }> {
  const url = `http://localhost:8114/api/v1/transactions/template?format=${format}`;
  const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
  const contentType = res.headers.get("content-type") || "";
  const body = await res.text();
  return { status: res.status, body, contentType };
}

export async function importTransactionsApi(bookId: number, format: string, csvContent: string, token: string): Promise<{ status: number; body: any }> {
  const formData = new FormData();
  const blob = new Blob([csvContent], { type: "text/csv" });
  formData.append("file", blob, "test-import.csv");
  formData.append("book_id", String(bookId));
  formData.append("format", format);

  const url = `http://localhost:8114/api/v1/transactions/import`;
  const res = await fetch(url, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body: formData,
  });
  const body = await res.json();
  return { status: res.status, body };
}

export async function destroyTestUser(userId: number, bookId: number): Promise<void> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    await conn.execute("DELETE FROM `user_category` WHERE user_id = ?", [userId]);
    await conn.execute("DELETE FROM `account` WHERE book_id = ?", [bookId]);
    await conn.execute("DELETE FROM `transaction` WHERE book_id = ?", [bookId]);
    await conn.execute("DELETE FROM `account_book` WHERE id = ?", [bookId]);
    await conn.execute("DELETE FROM `user` WHERE id = ?", [userId]);
    await conn.execute("DELETE FROM `token_blacklist` WHERE expires_at < NOW()");
  } finally {
    await conn.end();
  }
}


