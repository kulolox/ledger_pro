import { select, insert, execute } from "@/utils/db";

interface ExportData {
  version: number;
  exportedAt: string;
  user_profile: Record<string, unknown>[];
  categories: Record<string, unknown>[];
  transactions: Record<string, unknown>[];
}

async function queryAll(): Promise<ExportData> {
  const userProfile = await select("SELECT * FROM user_profile");
  const categories = await select("SELECT * FROM categories");
  const transactions = await select("SELECT * FROM transactions");

  return {
    version: 1,
    exportedAt: new Date().toISOString(),
    user_profile: userProfile,
    categories,
    transactions,
  };
}

async function doImport(jsonStr: string): Promise<void> {
  const data: ExportData = JSON.parse(jsonStr);

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

// #ifdef H5
export async function exportData(): Promise<void> {
  const data = await queryAll();
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
  await doImport(text);
}

function readFileAsText(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error("文件读取失败"));
    reader.readAsText(file);
  });
}
// #endif

// #ifdef APP-PLUS
export async function exportData(): Promise<void> {
  const data = await queryAll();
  const json = JSON.stringify(data, null, 2);
  const filename = `ledger-pro-backup-${new Date().toISOString().slice(0, 10)}.json`;

  await new Promise<void>((resolve, reject) => {
    plus.io.requestFileSystem(plus.io.PUBLIC_DOWNLOADS, (fs) => {
      fs.root.getFile(filename, { create: true }, (fileEntry) => {
        fileEntry.createWriter((writer) => {
          writer.onwrite = () => {
            plus.share.sendWithSystem(
              { type: "application/json", files: [fileEntry.fullPath] },
              () => resolve(),
              () => resolve(),
            );
          };
          writer.onerror = reject;
          writer.write(json);
        }, reject);
      }, reject);
    }, reject);
  });
}

export async function importDataFile(filePath: string): Promise<void> {
  const text = await new Promise<string>((resolve, reject) => {
    plus.io.resolveLocalFileSystemURL(filePath, (entry) => {
      (entry as any).file((file: any) => {
        const reader = new plus.io.FileReader();
        reader.onloadend = (e: any) => resolve(e.target.result as string);
        reader.onerror = reject;
        reader.readAsText(file, "utf-8");
      }, reject);
    }, reject);
  });
  await doImport(text);
}

export function listBackupFiles(): Promise<Array<{ name: string; fullPath: string }>> {
  return new Promise((resolve, reject) => {
    plus.io.requestFileSystem(plus.io.PUBLIC_DOWNLOADS, (fs) => {
      const reader = fs.root.createReader();
      reader.readEntries((entries) => {
        const jsonFiles = entries
          .filter((e: any) => e.name && e.name.endsWith(".json"))
          .map((e: any) => ({ name: e.name, fullPath: e.fullPath }))
          .sort((a: any, b: any) => b.name.localeCompare(a.name));
        resolve(jsonFiles);
      }, reject);
    }, reject);
  });
}
// #endif
