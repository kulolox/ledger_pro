import { describe, it, expect, vi, afterEach } from "vitest";
import {
  formatCents,
  formatNumber,
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
    vi.setSystemTime(new Date(2026, 0, 1)); // Jan 1, 2026
    expect(getTodayStr()).toBe("2026-01-01");
  });
});

describe("formatDayHeader", () => {
  it("formats date string to MM月DD日", () => {
    expect(formatDayHeader("2026-06-17")).toBe("6月17日");
  });

  it("handles single digit month", () => {
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
