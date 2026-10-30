import { test, expect } from "../fixtures/test.fixture";
import { LoginPage } from "../page-objects/LoginPage";
import { RegisterPage } from "../page-objects/RegisterPage";
import { HomePage } from "../page-objects/HomePage";
import { TEST_USER } from "../utils/test-data";

test.describe("Auth - Critical Path", () => {
  test("login page renders correctly", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await expect(loginPage.emailInput).toBeVisible();
    await expect(loginPage.passwordInput).toBeVisible();
    await expect(loginPage.loginBtn).toBeVisible();
    await expect(loginPage.registerLink).toBeVisible();
  });

  test("navigate to register page and back to login", async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.clickRegister();

    const registerPage = new RegisterPage(page);
    await expect(registerPage.nicknameInput).toBeVisible();
    await expect(registerPage.emailInput).toBeVisible();

    await registerPage.clickBackToLogin();
    await expect(loginPage.loginBtn).toBeVisible();
  });

  test("login with valid credentials and logout successfully", async ({ page, testUser }) => {
    // 1. 登录正常链路
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.login(TEST_USER.email, TEST_USER.password);

    await expect(page.locator(".home-page")).toBeVisible();
    await expect(page.locator(".month-label")).toBeVisible();

    // 2. 登出闭环链路（整合原 zz-logout.spec.ts）
    const homePage = new HomePage(page);
    await homePage.clickMenu();

    const sidebar = page.locator(".sidebar");
    await expect(sidebar).toBeVisible();
    await sidebar.locator(".logout-item").click();

    await page.waitForURL("**/pages/login/index", { timeout: 10000 });
    await expect(page.locator(".login-page")).toBeVisible();
  });
});
