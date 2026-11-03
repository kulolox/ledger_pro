import { test, expect } from "../fixtures/test.fixture";

test.describe("Profile System Version and App Update Flow", () => {
  test("个人中心页账号安全分组整合及软件版本卡片展示", async ({
    authenticatedPage,
  }) => {
    const page = authenticatedPage;

    await page.goto("/#/pages/profile/index");
    await page.waitForSelector(".profile-hero", { timeout: 15000 });

    // 1. 验证基本资料分组存在
    await expect(page.locator("text=基本资料")).toBeVisible();

    // 2. 验证账号安全分组整合了「修改密码」和「注销账号」
    const securityBlock = page.locator(".section-block", {
      hasText: "账号安全",
    });
    await expect(securityBlock).toBeVisible();
    await expect(securityBlock.locator("text=修改密码")).toBeVisible();
    await expect(securityBlock.locator("text=注销账号")).toBeVisible();

    // 3. 验证危险操作分组不再作为独立 card 存在
    await expect(page.locator(".danger-block")).toHaveCount(0);

    // 4. 验证关于系统分组及版本号展示
    const systemBlock = page.locator(".section-block", { hasText: "关于系统" });
    await expect(systemBlock).toBeVisible();
    await expect(systemBlock.locator("text=软件版本")).toBeVisible();
    await expect(systemBlock.locator("text=v1.0.0")).toBeVisible();
  });

  test("检查更新二级页面检查更新与下载进度交互流程", async ({
    authenticatedPage,
  }) => {
    const page = authenticatedPage;

    await page.goto("/#/pages/update/index");
    await page.waitForSelector(".app-hero", { timeout: 15000 });

    // 1. 验证应用 Hero 区域展示
    await expect(page.locator(".app-name")).toHaveText("LedgerPro");
    await expect(page.locator(".current-version")).toContainText("v1.0.0");

    // 2. 等待自动请求后端版本检查完成
    await page.waitForSelector(".update-card, .latest-card", {
      timeout: 10000,
    });

    const isUpdateAvailable =
      (await page.locator(".update-card").count()) > 0;

    if (isUpdateAvailable) {
      // 验证新版本信息与更新日志卡片
      await expect(page.locator(".new-version-title")).toContainText("发现新版本");
      await expect(page.locator(".changelog-box")).toBeVisible();

      // 点击立即下载
      const downloadBtn = page.locator(".action-btn.primary");
      await downloadBtn.click();

      // 验证进度条展现
      await page.waitForSelector(".progress-section", { timeout: 3000 });
      await expect(page.locator(".progress-bar-track")).toBeVisible();

      // 等待下载完成
      await page.waitForSelector(".action-btn.success", { timeout: 10000 });
      await expect(page.locator(".action-btn.success")).toContainText("下载完成");
    } else {
      await expect(page.locator(".latest-title")).toHaveText(
        "当前已是最新版本",
      );
    }
  });
});
