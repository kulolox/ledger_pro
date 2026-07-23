// #ifdef H5
import type { SqlJsStatic, Database } from "sql.js";
// #endif

const DB_NAME = "ledger_pro";
const STORAGE_KEY = "ledger_pro_db";

// --- SQL escaping ---

function escape(val: unknown): string {
  if (val === null || val === undefined) return "NULL";
  if (typeof val === "number") return String(val);
  return `'${String(val).replace(/'/g, "''")}'`;
}

// #ifdef H5
// --- H5 (sql.js) backend ---

let SQL: SqlJsStatic | null = null;
let h5db: Database | null = null;

async function ensureH5DB(): Promise<Database> {
  if (h5db) return h5db;
  if (!SQL) {
    const { default: initSqlJs } = await import("sql.js");
    const wasmResp = await fetch("/static/wasm/sql-wasm.wasm");
    const wasmBuf = await wasmResp.arrayBuffer();
    SQL = await initSqlJs({ wasmBinary: new Uint8Array(wasmBuf) });
  }
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    h5db = new SQL.Database(new Uint8Array(JSON.parse(saved)));
  } else {
    h5db = new SQL.Database();
  }
  return h5db;
}

function saveToStorage(): void {
  if (!h5db) return;
  const arr = h5db.export();
  localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.from(arr)));
}
// #endif

// #ifdef APP-PLUS
// --- App (plus.sqlite) backend ---

function plusSqlite() {
  return plus.sqlite;
}
// #endif

// --- Public API ---

export async function openDB(): Promise<void> {
  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plusSqlite().openDatabase({
      name: DB_NAME,
      path: `_doc/${DB_NAME}.db`,
      success: () => resolve(),
      fail: (e: any) => reject(e),
    });
  });
  // #endif
  // #ifdef H5
  await ensureH5DB();
  // #endif
}

export async function execute(sql: string): Promise<void> {
  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plusSqlite().executeSql({
      name: DB_NAME,
      sql: sql as any,
      success: () => resolve(),
      fail: (e: any) => reject(e),
    });
  });
  // #endif
  // #ifdef H5
  h5db!.run(sql);
  saveToStorage();
  // #endif
}

export async function select<T = Record<string, unknown>>(sql: string): Promise<T[]> {
  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plusSqlite().selectSql({
      name: DB_NAME,
      sql: sql as any,
      success: (e: any) => resolve((e || []) as T[]),
      fail: (e: any) => reject(e),
    });
  });
  // #endif
  // #ifdef H5
  const results = h5db!.exec(sql);
  if (!results.length) return [];
  const { columns, values } = results[0];
  return values.map((row) => {
    const obj: Record<string, unknown> = {};
    columns.forEach((col, i) => {
      obj[col] = row[i];
    });
    return obj as T;
  });
  // #endif
}

export async function insert(
  table: string,
  data: Record<string, unknown>,
): Promise<number> {
  const keys = Object.keys(data);
  const vals = keys.map((k) => escape(data[k]));
  const sql = `INSERT INTO ${table} (${keys.join(", ")}) VALUES (${vals.join(", ")})`;
  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plusSqlite().executeSql({
      name: DB_NAME,
      sql: sql as any,
      success: () => resolve(0),
      fail: (e: any) => reject(e),
    });
  });
  // #endif
  // #ifdef H5
  h5db!.run(sql);
  saveToStorage();
  return 0;
  // #endif
}

export async function updateById(
  table: string,
  id: number,
  data: Record<string, unknown>,
): Promise<void> {
  const sets = Object.keys(data)
    .map((k) => `${k} = ${escape(data[k])}`)
    .join(", ");
  const sql = `UPDATE ${table} SET ${sets} WHERE id = ${id}`;
  return execute(sql);
}

export async function closeDB(): Promise<void> {
  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    plusSqlite().closeDatabase({
      name: DB_NAME,
      success: () => resolve(),
      fail: (e: any) => reject(e),
    });
  });
  // #endif
  // #ifdef H5
  h5db?.close();
  h5db = null;
  SQL = null;
  // #endif
}

const TABLES_SQL = `
CREATE TABLE IF NOT EXISTS user_profile (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nickname TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  type INTEGER NOT NULL,
  icon TEXT NOT NULL DEFAULT '',
  color TEXT NOT NULL DEFAULT '',
  is_system INTEGER NOT NULL DEFAULT 0,
  parent_id INTEGER DEFAULT 0,
  sort_order INTEGER NOT NULL DEFAULT 0,
  visible INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS transactions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type INTEGER NOT NULL,
  amount INTEGER NOT NULL,
  category_id INTEGER NOT NULL DEFAULT 0,
  account_id INTEGER NOT NULL DEFAULT 1,
  to_account_id INTEGER DEFAULT 0,
  remark TEXT DEFAULT '',
  trans_time TEXT NOT NULL,
  created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX IF NOT EXISTS idx_tx_trans_time ON transactions(trans_time);
CREATE INDEX IF NOT EXISTS idx_tx_category ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_tx_type ON transactions(type);
`;

export async function initDB(): Promise<void> {
  await openDB();
  // #ifdef APP-PLUS
  await execute(TABLES_SQL);
  // #endif
  // #ifdef H5
  h5db!.exec(TABLES_SQL);
  saveToStorage();
  // #endif
}
