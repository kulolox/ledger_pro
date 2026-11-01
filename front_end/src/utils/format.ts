export function formatCents(cents: number): string {
  return formatNumber(cents / 100);
}

export function formatNumber(val: number): string {
  if (isNaN(val)) return "0.00";
  const sign = val < 0 ? "-" : "";
  const abs = Math.abs(val);

  if (abs >= 100_000_000) {
    return `${sign}${(abs / 100_000_000).toLocaleString("zh-CN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}亿`;
  }

  if (abs >= 10_000) {
    return `${sign}${(abs / 10_000).toLocaleString("zh-CN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}万`;
  }

  return val.toLocaleString("zh-CN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

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
  const month = parseInt(parts[1]);
  const day = parseInt(parts[2]);
  return `${month}月${day}日`;
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
