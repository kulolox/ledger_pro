import { select, insert, execute } from "@/utils/db";

interface ExportData {
  version: number;
  exportedAt: string;
  user_profile: Record<string, unknown>[];
  categories: Record<string, unknown>[];
  transactions: Record<string, unknown>[];
}

export async function exportData(): Promise<void> {
  const userProfile = await select("SELECT * FROM user_profile");
  const categories = await select("SELECT * FROM categories");
  const transactions = await select("SELECT * FROM transactions");

  const data: ExportData = {
    version: 1,
    exportedAt: new Date().toISOString(),
    user_profile: userProfile,
    categories,
    transactions,
  };

  const json = JSON.stringify(data, null, 2);
  const blob = new Blob([json], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `ledger-pro-backup-${new Date().toISOString().slice(0, 10)}.json`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 100);
}

export async function importData(file: File): Promise<void> {
  const text = await readFileAsText(file);
  const data: ExportData = JSON.parse(text);

  if (!data.version || !data.transactions || !data.categories) {
    throw new Error("无效的备份文件格式");
  }

  await execute("DELETE FROM transactions");
  await execute("DELETE FROM categories");
  await execute("DELETE FROM user_profile");

  for (const row of data.user_profile || []) {
    await insert("user_profile", row);
  }
  for (const row of data.categories || []) {
    await insert("categories", row);
  }
  for (const row of data.transactions || []) {
    await insert("transactions", row);
  }
}

function readFileAsText(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("文件读取失败"));
    reader.readAsText(file);
  });
}
