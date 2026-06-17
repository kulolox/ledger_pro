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
}
