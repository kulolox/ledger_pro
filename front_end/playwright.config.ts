import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests/e2e/specs",
  timeout: 60000,
  expect: { timeout: 10000 },
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  use: {
    channel: "chrome",
    baseURL: "http://localhost:5173",
    headless: true,
    viewport: { width: 390, height: 844 },
    ignoreHTTPSErrors: true,
    screenshot: "only-on-failure",
    trace: "on-first-retry",
  },
  webServer: [
    {
      command: "npx uni",
      port: 5173,
      cwd: "./",
      reuseExistingServer: !process.env.CI,
      timeout: 30000,
    },
  ],
});
