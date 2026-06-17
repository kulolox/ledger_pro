import type { Page, Locator } from "@playwright/test";

export class HomePage {
  readonly page: Page;
  readonly menuBtn: Locator;
  readonly fab: Locator;
  readonly statsBtn: Locator;
  readonly monthLabel: Locator;
  readonly prevMonthBtn: Locator;
  readonly nextMonthBtn: Locator;
  readonly expenseSummary: Locator;
  readonly incomeSummary: Locator;
  readonly balanceSummary: Locator;
  readonly dayGroups: Locator;
  readonly emptyState: Locator;

  constructor(page: Page) {
    this.page = page;
    this.menuBtn = page.locator(".menu-btn");
    this.fab = page.locator(".fab");
    this.statsBtn = page.locator(".stats-btn");
    this.monthLabel = page.locator(".month-label");
    this.prevMonthBtn = page.locator(".nav-arrow").first();
    this.nextMonthBtn = page.locator(".nav-arrow").nth(1);
    this.expenseSummary = page.locator(".expense-card .summary-value");
    this.incomeSummary = page.locator(".income-card .summary-value");
    this.balanceSummary = page.locator(".balance-card .summary-value");
    this.dayGroups = page.locator(".day-group");
    this.emptyState = page.locator(".empty-state");
  }

  async goto() {
    await this.page.goto("/#/pages/index/index");
    await this.page.waitForSelector(".home-page", { timeout: 10000 });
  }

  async clickAdd() {
    await this.fab.click();
  }

  async clickMenu() {
    await this.menuBtn.click();
  }

  async clickStats() {
    await this.statsBtn.click();
    await this.page.waitForURL("**/pages/statistics/index", { timeout: 10000 });
  }

  async clickTransaction(index: number) {
    await this.page.locator(".tx-item").nth(index).click();
  }

  async longPressTransaction(index: number) {
    const tx = this.page.locator(".tx-item").nth(index);
    await tx.hover();
    await this.page.mouse.down();
    await this.page.waitForTimeout(600);
    await this.page.mouse.up();
  }

  async getDayGroupCount(): Promise<number> {
    return await this.dayGroups.count();
  }

  async getMonthLabel(): Promise<string> {
    return (await this.monthLabel.textContent()) || "";
  }

  async prevMonth() {
    await this.prevMonthBtn.click();
  }

  async nextMonth() {
    await this.nextMonthBtn.click();
  }
}
