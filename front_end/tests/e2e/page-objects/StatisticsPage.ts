import type { Page, Locator } from "@playwright/test";

export class StatisticsPage {
  readonly page: Page;
  readonly backBtn: Locator;
  readonly monthLabel: Locator;
  readonly expenseSummary: Locator;
  readonly incomeSummary: Locator;
  readonly balanceSummary: Locator;
  readonly expenseTab: Locator;
  readonly incomeTab: Locator;
  readonly categoryList: Locator;
  readonly categoryItems: Locator;
  readonly emptyState: Locator;

  constructor(page: Page) {
    this.page = page;
    this.backBtn = page.locator(".nav-left");
    this.monthLabel = page.locator(".month-label");
    this.expenseSummary = page.locator(".expense-card .summary-value");
    this.incomeSummary = page.locator(".income-card .summary-value");
    this.balanceSummary = page.locator(".balance-card .summary-value");
    this.expenseTab = page.locator(".tab-item").first();
    this.incomeTab = page.locator(".tab-item").nth(1);
    this.categoryList = page.locator(".category-list");
    this.categoryItems = page.locator(".category-item");
    this.emptyState = page.locator(".empty-state");
  }

  async goto() {
    await this.page.goto("/#/pages/statistics/index");
    await this.page.waitForSelector(".stats-page", { timeout: 10000 });
  }

  async gotoFromHome() {
    await this.page.goto("/#/pages/index/index");
    await this.page.waitForSelector(".home-page", { timeout: 10000 });
    await this.page.locator(".stats-btn").click();
    await this.page.waitForURL("**/pages/statistics/index", { timeout: 10000 });
    await this.page.waitForSelector(".stats-page", { timeout: 10000 });
  }

  async clickBack() {
    await this.backBtn.click();
  }

  async switchToExpense() {
    await this.expenseTab.click();
  }

  async switchToIncome() {
    await this.incomeTab.click();
  }

  async getCategoryCount(): Promise<number> {
    return await this.categoryItems.count();
  }
}
