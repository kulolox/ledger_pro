import { test, expect } from "../fixtures/test.fixture";
import { HomePage } from "../page-objects/HomePage";

test.describe("Logout", () => {
  test("logout redirects to login page", async ({ authenticatedPage }) => {
    const homePage = new HomePage(authenticatedPage);
    await homePage.clickMenu();

    const sidebar = authenticatedPage.locator(".sidebar");
    await sidebar.locator(".logout-item").click();
    await authenticatedPage.waitForTimeout(1000);

    await expect(authenticatedPage.locator(".login-btn")).toBeVisible();
  });
});
