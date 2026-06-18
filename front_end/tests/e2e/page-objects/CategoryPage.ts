import type { Page, Locator } from "@playwright/test";

export class CategoryPage {
  readonly page: Page;
  readonly backBtn: Locator;
  readonly expenseTab: Locator;
  readonly incomeTab: Locator;
  readonly sortBtn: Locator;
  readonly categoryItems: Locator;
  readonly addBtn: Locator;
  readonly editor: Locator;
  readonly editorNameInput: Locator;
  readonly editorIconInput: Locator;
  readonly editorColorInput: Locator;
  readonly editorSaveBtn: Locator;
  readonly editorCancelBtn: Locator;
  readonly editorTitle: Locator;
  readonly colorPresets: Locator;

  constructor(page: Page) {
    this.page = page;
    this.backBtn = page.locator(".nav-back");
    this.expenseTab = page.locator(".tab-item").first();
    this.incomeTab = page.locator(".tab-item").nth(1);
    this.sortBtn = page.locator(".nav-sort");
    this.categoryItems = page.locator(".category-item");
    this.addBtn = page.locator(".add-btn");
    this.editor = page.locator(".editor");
    this.editorNameInput = page.locator(".editor-form .form-input").first();
    this.editorIconInput = page.locator(".emoji-input");
    this.editorColorInput = page.locator(".color-input");
    this.editorSaveBtn = page.locator(".editor-confirm");
    this.editorCancelBtn = page.locator(".editor-cancel");
    this.editorTitle = page.locator(".editor-title");
    this.colorPresets = page.locator(".preset-item");
  }

  async goto() {
    await this.page.goto("/#/pages/category/index");
    await this.page.waitForSelector(".category-page", { timeout: 10000 });
  }

  async gotoFromHome() {
    await this.page.goto("/#/pages/index/index");
    await this.page.waitForSelector(".home-page", { timeout: 10000 });
    await this.page.locator(".menu-btn").click();
    const sidebar = this.page.locator(".sidebar");
    await sidebar.locator(".action-item").filter({ hasText: "分类管理" }).click();
    await this.page.waitForSelector(".category-page", { timeout: 10000 });
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

  async toggleSort() {
    await this.sortBtn.click();
  }

  async clickAdd() {
    await this.addBtn.click();
    await this.page.waitForSelector(".editor", { timeout: 5000 });
  }

  async fillCategoryForm(name: string, icon: string, color: string) {
    await this.editorNameInput.locator("input").fill(name);
    await this.editorIconInput.locator("input").fill(icon);
    await this.editorColorInput.locator("input").fill(color);
  }

  async saveCategory() {
    await this.editorSaveBtn.click();
  }

  async cancelCategory() {
    await this.editorCancelBtn.click();
  }

  async editCategory(index: number) {
    await this.categoryItems.nth(index).locator(".cat-action.edit").click();
    await this.page.waitForSelector(".editor", { timeout: 5000 });
  }

  async deleteCategory(index: number) {
    await this.categoryItems.nth(index).locator(".cat-action.delete").click();
  }

  async toggleVisibility(index: number) {
    await this.categoryItems.nth(index).locator(".cat-action.visibility").click();
  }

  async getCategoryCount(): Promise<number> {
    return await this.categoryItems.count();
  }

  async getEditorTitle(): Promise<string> {
    return (await this.editorTitle.textContent()) || "";
  }

  async getFormValue(field: "name" | "icon" | "color"): Promise<string> {
    const inputs = [this.editorNameInput, this.editorIconInput, this.editorColorInput];
    const idx = field === "name" ? 0 : field === "icon" ? 1 : 2;
    return (await inputs[idx].locator("input").inputValue()) || "";
  }

  async clickColorPreset(index: number) {
    await this.colorPresets.nth(index).click();
  }

  async getCategoryName(index: number): Promise<string> {
    return (await this.categoryItems.nth(index).locator(".cat-name").textContent()) || "";
  }

  async isCategoryDimmed(index: number): Promise<boolean> {
    const cls = (await this.categoryItems.nth(index).getAttribute("class")) || "";
    return cls.includes("dimmed");
  }

  async hasLockIcon(index: number): Promise<boolean> {
    return (await this.categoryItems.nth(index).locator(".cat-lock").count()) > 0;
  }

  async hasEditButton(index: number): Promise<boolean> {
    return (await this.categoryItems.nth(index).locator(".cat-action.edit").count()) > 0;
  }

  async hasDeleteButton(index: number): Promise<boolean> {
    return (await this.categoryItems.nth(index).locator(".cat-action.delete").count()) > 0;
  }
}
