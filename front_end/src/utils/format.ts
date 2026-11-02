export function formatCents(cents: number): string {
  return formatNumber(cents / 100);
}

function addCommas(numStr: string): string {
  const parts = numStr.split(".");
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  return parts.join(".");
}

export function formatNumber(val: number): string {
  if (isNaN(val)) return "0.00";
  const sign = val < 0 ? "-" : "";
  const abs = Math.abs(val);

  if (abs >= 100_000_000) {
    return `${sign}${addCommas((abs / 100_000_000).toFixed(2))}亿`;
  }

  if (abs >= 10_000) {
    return `${sign}${addCommas((abs / 10_000).toFixed(2))}万`;
  }

  return `${sign}${addCommas(abs.toFixed(2))}`;
}

// 单笔交易最大支持金额（2000 万元 = 20 亿分，适配 MySQL 32位 INT 存储）
export const MAX_AMOUNT_CENTS = 2000000000;
export const MAX_AMOUNT_YUAN = 20000000;

export function yuanToCents(yuan: number | string): number {
  const num = typeof yuan === "string" ? parseFloat(yuan) : yuan;
  if (isNaN(num)) return 0;
  return Math.round(num * 100);
}

export function centsToYuan(cents: number): number {
  if (isNaN(cents)) return 0;
  return Math.round(cents) / 100;
}

export function getTodayStr(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export function formatDayHeader(dateStr: string): string {
  const parts = dateStr.split("-");
  const month = String(parseInt(parts[1])).padStart(2, "0");
  const day = String(parseInt(parts[2])).padStart(2, "0");
  return `${month}.${day}`;
}

export function formatWeekday(dateStr: string): string {
  const weekdays = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
  const parts = dateStr.split("-");
  const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
  return weekdays[d.getDay()];
}

export function timestampToDateStr(ts: number): string {
  const d = new Date(ts);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}
