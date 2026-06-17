import type { Page, Locator } from "@playwright/test";

export class ForgotPasswordPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly codeInput: Locator;
  readonly newPasswordInput: Locator;
  readonly sendCodeBtn: Locator;
  readonly submitBtn: Locator;
  readonly backLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator(".form-input input").first();
    this.codeInput = page.locator(".code-input input");
    this.newPasswordInput = page.locator(".form-input input").last();
    this.sendCodeBtn = page.locator(".send-code-btn");
    this.submitBtn = page.locator(".submit-btn");
    this.backLink = page.locator(".auth-footer .link");
  }

  async goto() {
    await this.page.goto("/#/pages/forgot-password/index");
    await this.page.waitForSelector(".form-input", { timeout: 10000 });
  }

  async sendCode(email: string) {
    await this.emailInput.fill(email);
    await this.sendCodeBtn.click();
  }

  async resetPassword(email: string, code: string, newPassword: string) {
    await this.emailInput.fill(email);
    await this.codeInput.fill(code);
    await this.newPasswordInput.fill(newPassword);
    await this.submitBtn.click();
  }

  async clickBackToLogin() {
    await this.backLink.click();
    await this.page.waitForSelector(".login-btn", { timeout: 10000 });
  }
}
