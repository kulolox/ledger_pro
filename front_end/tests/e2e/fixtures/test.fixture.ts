import { test as base } from "@playwright/test";
import { createTestUser, destroyTestUser, type TestUserRow } from "../utils/db";
import { TEST_USER } from "../utils/test-data";

export type TestFixtures = {
  testUser: TestUserRow;
  authenticatedPage: Awaited<ReturnType<typeof loginAs>>;
};

export type WorkerFixtures = {
  workerUser: TestUserRow;
};

async function loginAs(page: any, email: string, password: string) {
  await page.goto("/#/pages/login/index");
  await page.waitForSelector(".form-input", { timeout: 10000 });
  const inputs = page.locator(".form-input input");
  await inputs.first().fill(email);
  await inputs.nth(1).fill(password);
  await page.locator(".login-btn").click();
  await page.waitForSelector(".home-page", { timeout: 15000 });
  return page;
}

let cachedStorageState: any = null;

export const test = base.extend<TestFixtures, WorkerFixtures>({
  workerUser: [
    async ({}, use) => {
      const user = await createTestUser();
      await use(user);
      await destroyTestUser(user.userId, user.bookId);
    },
    { scope: "worker" },
  ],

  testUser: async ({ workerUser }, use) => {
    await use(workerUser);
  },

  authenticatedPage: async ({ browser, testUser }, use) => {
    const context = cachedStorageState
      ? await browser.newContext({ storageState: cachedStorageState })
      : await browser.newContext();
    const page = await context.newPage();

    if (!cachedStorageState) {
      await loginAs(page, TEST_USER.email, TEST_USER.password);
      cachedStorageState = await context.storageState();
    } else {
      await page.goto("/#/pages/index/index");
      await page.waitForSelector(".home-page", { timeout: 10000 });
    }

    await use(page);
    await context.close();
  },
});

export { expect } from "@playwright/test";
