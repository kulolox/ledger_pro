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

  it("handles large amounts with thousands separator", () => {
    const result = formatCents(123456789);
    expect(result).toContain("1,234,567.89");
  });
});

describe("formatNumber", () => {
  it("formats number with 2 decimals", () => {
    expect(formatNumber(12.5)).toBe("12.50");
  });

  it("formats zero", () => {
    expect(formatNumber(0)).toBe("0.00");
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
  it("formats 2026-06-17 to 6月17日", () => {
    expect(formatDayHeader("2026-06-17")).toBe("6月17日");
  });

  it("strips leading zeros from month and day", () => {
    expect(formatDayHeader("2026-01-05")).toBe("1月5日");
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
