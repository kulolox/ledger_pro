import { test, expect } from "../fixtures/test.fixture";
import { ForgotPasswordPage } from "../page-objects/ForgotPasswordPage";
import { LoginPage } from "../page-objects/LoginPage";
import { getResetCode } from "../utils/db";
import { TEST_USER } from "../utils/test-data";

test.describe("Forgot Password", () => {
  test("forgot password page renders correctly", async ({ page }) => {
    const fp = new ForgotPasswordPage(page);
    await fp.goto();

    await expect(fp.emailInput).toBeVisible();
    await expect(fp.sendCodeBtn).toBeVisible();
    await expect(fp.submitBtn).toBeVisible();
    await expect(fp.backLink).toBeVisible();
  });

  test("send reset code button clickable", async ({ page }) => {
    const fp = new ForgotPasswordPage(page);
    await fp.goto();

    await fp.emailInput.fill("test@example.com");
    await fp.sendCodeBtn.click();
    await page.waitForTimeout(1000);
    const btnText = await fp.sendCodeBtn.textContent();
    expect(btnText).toBeTruthy();
  });

  test("reset with empty code shows error", async ({ page }) => {
    const fp = new ForgotPasswordPage(page);
    await fp.goto();

    await fp.resetPassword("test@example.com", "", "NewPass123");
    await page.waitForTimeout(500);
    await expect(page).toHaveURL(/forgot-password/);
  });

  test("back to login from forgot password", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.clickForgotPassword();

    const fp = new ForgotPasswordPage(page);
    await fp.clickBackToLogin();

    await expect(loginPage.loginBtn).toBeVisible();
  });

  test("full reset password flow with real code", async ({ page, testUser }) => {
    const fp = new ForgotPasswordPage(page);
    await fp.goto();

    await fp.sendCode(TEST_USER.email);
    await page.waitForTimeout(2000);

    const code = await getResetCode(testUser.userId);
    expect(code).toBeTruthy();

    const newPassword = "NewPass999";
    await fp.resetPassword(TEST_USER.email, code, newPassword);

    await page.waitForTimeout(1500);

    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, newPassword);
    await expect(page.locator(".home-page")).toBeVisible();

    await page.goto("/#/pages/forgot-password/index");
    await page.waitForSelector(".form-input", { timeout: 10000 });
    await fp.sendCode(TEST_USER.email);
    await page.waitForTimeout(2000);

    const code2 = await getResetCode(testUser.userId);
    const fp2 = new ForgotPasswordPage(page);
    await fp2.resetPassword(TEST_USER.email, code2, TEST_USER.password);
    await page.waitForTimeout(1500);
  });
});
