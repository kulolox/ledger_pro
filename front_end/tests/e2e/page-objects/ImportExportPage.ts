import type { Page, Locator } from "@playwright/test";

export class ImportExportPage {
  readonly page: Page;
  readonly backBtn: Locator;
  readonly exportTab: Locator;
  readonly importTab: Locator;
  readonly exportBtn: Locator;
  readonly importBtn: Locator;
  readonly formatChips: Locator;
  readonly fileArea: Locator;
  readonly resultCard: Locator;
  readonly previewRows: Locator;
  readonly modeChips: Locator;
  readonly monthPicker: Locator;
  readonly monthText: Locator;
  readonly monthPrev: Locator;
  readonly monthNext: Locator;
  readonly yearPicker: Locator;
  readonly yearText: Locator;
  readonly yearPrev: Locator;
  readonly yearNext: Locator;
  readonly templateLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.backBtn = page.locator(".back-btn");
    this.exportTab = page.locator(".tab").first();
    this.importTab = page.locator(".tab").nth(1);
    this.exportBtn = page.locator(".export-btn");
    this.importBtn = page.locator(".import-btn");
    this.formatChips = page.locator(".format-chip");
    this.fileArea = page.locator(".file-area");
    this.resultCard = page.locator(".result-card");
    this.previewRows = page.locator(".preview-row");
    this.modeChips = page.locator(".mode-chip");
    this.monthPicker = page.locator(".month-picker");
    this.monthText = page.locator(".month-text");
    this.monthPrev = page.locator(".month-arrow").first();
    this.monthNext = page.locator(".month-arrow").nth(1);
    this.yearPicker = page.locator(".year-picker");
    this.yearText = page.locator(".year-text");
    this.yearPrev = page.locator(".year-arrow").first();
    this.yearNext = page.locator(".year-arrow").nth(1);
    this.templateLink = page.locator(".template-link");
  }

  async goto() {
    await this.page.goto("/#/pages/import-export/index");
    await this.page.waitForSelector(".page", { timeout: 10000 });
  }

  async clickBack() {
    await this.backBtn.click();
  }

  async switchToExport() {
    await this.exportTab.click();
  }

  async switchToImport() {
    await this.importTab.click();
  }

  async selectFormat(format: string) {
    const chips = this.formatChips;
    const count = await chips.count();
    for (let i = 0; i < count; i++) {
      const text = await chips.nth(i).textContent();
      if (text?.trim().toLowerCase() === format.toLowerCase()) {
        await chips.nth(i).click();
        return;
      }
    }
  }

  async clickExport() {
    await this.exportBtn.click();
  }

  async clickImport() {
    await this.importBtn.click();
  }

  async clickFileArea() {
    await this.fileArea.click();
  }

  async switchToMode(mode: "month" | "year") {
    const chips = this.modeChips;
    const count = await chips.count();
    for (let i = 0; i < count; i++) {
      const text = await chips.nth(i).textContent();
      if (text?.trim().includes(mode === "month" ? "按月" : "按年")) {
        await chips.nth(i).click();
        return;
      }
    }
  }

  async clickPrevMonth() {
    await this.monthPrev.click();
  }

  async clickNextMonth() {
    await this.monthNext.click();
  }

  async clickPrevYear() {
    await this.yearPrev.click();
  }

  async clickNextYear() {
    await this.yearNext.click();
  }

  async clickTemplate() {
    await this.templateLink.click();
  }
}
