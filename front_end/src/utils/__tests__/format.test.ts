import { describe, it, expect, vi, afterEach } from "vitest";
import {
  formatCents,
  formatNumber,
  yuanToCents,
  centsToYuan,
  getTodayStr,
  formatDayHeader,
  formatWeekday,
  timestampToDateStr,
} from "../format";

describe("formatCents", () => {
  it("converts cents to yuan with 2 decimals", () => {
    expect(formatCents(1250)).toBe("12.50");
  });

  it("handles zero", () => {
    expect(formatCents(0)).toBe("0.00");
  });

  it("handles negative amounts", () => {
    const result = formatCents(-500);
    expect(result).toContain("-");
    expect(result).toContain("5.00");
  });

  it("handles amounts under 10,000 yuan with thousands separator", () => {
    const result = formatCents(999999);
    expect(result).toContain("9,999.99");
  });

  it("formats large amounts (>= 10,000 yuan) with 万 unit", () => {
    expect(formatCents(1000000)).toBe("1.00万");
    expect(formatCents(123456789)).toBe("123.46万");
    expect(formatCents(889888700)).toBe("889.89万");
    expect(formatCents(-5000000)).toBe("-5.00万");
  });

  it("formats huge amounts (>= 100,000,000 yuan) with 亿 unit", () => {
    expect(formatCents(10000000000)).toBe("1.00亿");
    expect(formatCents(123456789000)).toBe("12.35亿");
  });
});

describe("formatNumber", () => {
  it("formats number with 2 decimals", () => {
    expect(formatNumber(12.5)).toBe("12.50");
  });

  it("formats zero", () => {
    expect(formatNumber(0)).toBe("0.00");
  });

  it("formats large numbers with 万 and 亿 units", () => {
    expect(formatNumber(8898887)).toBe("889.89万");
    expect(formatNumber(150000000)).toBe("1.50亿");
  });
});

describe("yuanToCents & centsToYuan", () => {
  it("converts yuan number to cents with precision", () => {
    expect(yuanToCents(29.99)).toBe(2999);
    expect(yuanToCents(0.1 + 0.2)).toBe(30);
    expect(yuanToCents(0)).toBe(0);
  });

  it("converts yuan string to cents", () => {
    expect(yuanToCents("10.50")).toBe(1050);
    expect(yuanToCents("invalid")).toBe(0);
  });

  it("converts cents to yuan float with precision", () => {
    expect(centsToYuan(2999)).toBe(29.99);
    expect(centsToYuan(1050)).toBe(10.5);
    expect(centsToYuan(0)).toBe(0);
  });
});

describe("getTodayStr", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("returns YYYY-MM-DD format", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 5, 17)); // June 17, 2026
    expect(getTodayStr()).toBe("2026-06-17");
  });

  it("pads single digit month and day", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 0, 5)); // Jan 5, 2026
    expect(getTodayStr()).toBe("2026-01-05");
  });
});

describe("formatDayHeader", () => {
  it("formats 2026-06-17 to 06.17", () => {
    expect(formatDayHeader("2026-06-17")).toBe("06.17");
  });

  it("pads single-digit month and day as MM.DD (e.g. 11.01)", () => {
    expect(formatDayHeader("2026-11-01")).toBe("11.01");
    expect(formatDayHeader("2026-01-05")).toBe("01.05");
  });
});

describe("formatWeekday", () => {
  it("returns correct weekday for 2026-06-17 (Wednesday)", () => {
    expect(formatWeekday("2026-06-17")).toBe("周三");
  });

  it("returns correct weekday for Sunday", () => {
    expect(formatWeekday("2026-06-21")).toBe("周日");
  });

  it("returns correct weekday for Monday", () => {
    expect(formatWeekday("2026-06-22")).toBe("周一");
  });
});

describe("timestampToDateStr", () => {
  it("converts timestamp to date string", () => {
    const ts = new Date(2026, 5, 17).getTime();
    expect(timestampToDateStr(ts)).toBe("2026-06-17");
  });

  it("pads month and day", () => {
    const ts = new Date(2026, 0, 5).getTime();
    expect(timestampToDateStr(ts)).toBe("2026-01-05");
  });
});
