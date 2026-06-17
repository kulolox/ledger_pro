import type { Page, Locator } from "@playwright/test";

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginBtn: Locator;
  readonly registerLink: Locator;
  readonly forgotPasswordLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator(".form-input input").first();
    this.passwordInput = page.locator(".form-input input").nth(1);
    this.loginBtn = page.locator(".login-btn");
    this.registerLink = page.locator(".login-links .link").first();
    this.forgotPasswordLink = page.locator(".login-links .link").nth(1);
  }

  async goto() {
    await this.page.goto("/#/pages/login/index");
    await this.page.waitForSelector(".form-input", { timeout: 10000 });
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginBtn.click();
    await this.page.waitForSelector(".home-page", { timeout: 15000 });
  }

  async clickRegister() {
    await this.registerLink.click();
    await this.page.waitForURL("**/pages/register/index", { timeout: 10000 });
  }

  async clickForgotPassword() {
    await this.forgotPasswordLink.click();
    await this.page.waitForURL("**/pages/forgot-password/index", { timeout: 10000 });
  }
}
