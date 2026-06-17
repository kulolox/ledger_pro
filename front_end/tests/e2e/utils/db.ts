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
    const hash = await bcrypt.hash(TEST_USER.password, 10);

    const [userResult] = await conn.execute(
      "INSERT INTO `user` (nickname, email, password_hash, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())",
      [TEST_USER.nickname, TEST_USER.email, hash],
    );
    const userId = (userResult as any).insertId;

    const bookId = Date.now();
    await conn.execute(
      "INSERT INTO `account_book` (id, name, owner_id, type, created_at, updated_at) VALUES (?, ?, ?, 0, NOW(), NOW())",
      [bookId, TEST_BOOK_NAME, userId],
    );

    const [accountResult] = await conn.execute(
      "INSERT INTO `account` (book_id, name, type, icon, created_at, updated_at) VALUES (?, '现金', 0, '💵', NOW(), NOW())",
      [bookId],
    );
    const accountId = (accountResult as any).insertId;

    const [categories] = await conn.execute("SELECT id FROM `category`");
    for (const row of categories as any[]) {
      await conn.execute(
        `INSERT INTO user_category (user_id, name, type, icon, color, parent_id, is_system, source_id, visible, sort_order, created_at, updated_at)
         SELECT ?, name, type, icon, color, parent_id, 1, id, 1, sort_order, NOW(), NOW()
         FROM category WHERE id = ?`,
        [userId, row.id],
      );
    }

    return { userId, bookId, accountId };
  } finally {
    await conn.end();
  }
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

export async function getResetCode(userId: number): Promise<string> {
  const conn = await mysql.createConnection(DB_CONFIG);
  try {
    const [rows] = await conn.execute("SELECT reset_code FROM `user` WHERE id = ?", [userId]);
    const row = (rows as any[])[0];
    return row?.reset_code || "";
  } finally {
    await conn.end();
  }
}
