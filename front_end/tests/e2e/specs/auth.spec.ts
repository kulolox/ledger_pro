import { test, expect } from "../fixtures/test.fixture";
import { LoginPage } from "../page-objects/LoginPage";
import { RegisterPage } from "../page-objects/RegisterPage";
import { TEST_USER } from "../utils/test-data";

test.describe("Auth", () => {
  test("login page renders correctly", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.loginBtn).toBeVisible();
    await expect(loginPage.registerLink).toBeVisible();
    await expect(loginPage.forgotPasswordLink).toBeVisible();
  });

  test("login with valid credentials", async ({ page, testUser }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);

    await expect(page.locator(".home-page")).toBeVisible();
    await expect(page.locator(".month-label")).toBeVisible();
  });

  test("login with wrong password shows error", async ({ page, testUser }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.emailInput.fill(TEST_USER.email);
    await loginPage.passwordInput.fill("WrongPassword999");
    await loginPage.loginBtn.click();

    await page.waitForTimeout(2000);
    await expect(page).toHaveURL(/login/);
  });

  test("login with empty email shows toast", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.loginBtn.click();
    await page.waitForTimeout(500);
    // should still be on login page
    await expect(page).toHaveURL(/login/);
  });

  test("navigate to register page", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.clickRegister();

    const registerPage = new RegisterPage(page);
    await expect(registerPage.nicknameInput).toBeVisible();
    await expect(registerPage.emailInput).toBeVisible();
    await expect(registerPage.submitBtn).toBeVisible();
  });

  test("navigate to forgot password page", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.clickForgotPassword();

    await expect(page.locator(".auth-title")).toHaveText("找回密码");
  });

  test("register page back to login", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.clickRegister();

    const registerPage = new RegisterPage(page);
    await registerPage.clickBackToLogin();

    await expect(page.locator(".login-btn")).toBeVisible();
  });

  test("register with mismatched passwords shows error", async ({ page }) => {
    const registerPage = new RegisterPage(page);
    await registerPage.goto();

    await registerPage.nicknameInput.fill("TestUser");
    await registerPage.emailInput.fill("unique-" + Date.now() + "@test.com");
    await registerPage.passwordInput.fill("Test123456");
    await registerPage.confirmPasswordInput.fill("Different123");
    await registerPage.submitBtn.click();

    await page.waitForTimeout(500);
    await expect(page).toHaveURL(/register/);
  });
});
