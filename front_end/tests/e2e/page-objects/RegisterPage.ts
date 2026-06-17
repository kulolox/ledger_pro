import type { Page, Locator } from "@playwright/test";

export class RegisterPage {
  readonly page: Page;
  readonly nicknameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly confirmPasswordInput: Locator;
  readonly submitBtn: Locator;
  readonly backLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nicknameInput = page.locator(".form-input input").first();
    this.emailInput = page.locator(".form-input input").nth(1);
    this.passwordInput = page.locator(".form-input input").nth(2);
    this.confirmPasswordInput = page.locator(".form-input input").nth(3);
    this.submitBtn = page.locator(".submit-btn");
    this.backLink = page.locator(".auth-footer .link");
  }

  async goto() {
    await this.page.goto("/#/pages/register/index");
    await this.page.waitForSelector(".form-input", { timeout: 10000 });
  }

  async register(nickname: string, email: string, password: string) {
    await this.nicknameInput.fill(nickname);
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.confirmPasswordInput.fill(password);
    await this.submitBtn.click();
  }

  async clickBackToLogin() {
    await this.backLink.click();
    await this.page.waitForSelector(".login-btn", { timeout: 10000 });
  }
}
