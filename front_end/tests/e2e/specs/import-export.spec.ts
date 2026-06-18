import { test, expect } from "../fixtures/test.fixture";
import { ImportExportPage } from "../page-objects/ImportExportPage";

test.describe("Import Export", () => {
  test("import export page renders", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.exportTab).toBeVisible();
    await expect(iePage.importTab).toBeVisible();
    await expect(iePage.exportBtn).toBeVisible();
  });

  test("switch between export and import tabs", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.exportBtn).toBeVisible();

    await iePage.switchToImport();
    await authenticatedPage.waitForTimeout(300);
    await expect(iePage.importBtn).toBeVisible();
    await expect(iePage.fileArea).toBeVisible();

    await iePage.switchToExport();
    await authenticatedPage.waitForTimeout(300);
    await expect(iePage.exportBtn).toBeVisible();
  });

  test("export format selection", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.selectFormat("excel");
    await authenticatedPage.waitForTimeout(200);
    // the Excel chip should have active class
    const excelChip = iePage.formatChips.filter({ hasText: "Excel" });
    await expect(excelChip).toHaveClass(/active/);

    await iePage.selectFormat("csv");
    const csvChip = iePage.formatChips.filter({ hasText: "CSV" });
    await expect(csvChip).toHaveClass(/active/);
  });

  test("export button triggers action", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.selectFormat("json");
    await iePage.clickExport();
    await authenticatedPage.waitForTimeout(2000);
  });

  test("import tab shows file picker area", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();

    await expect(iePage.fileArea).toBeVisible();
    const fileHint = await iePage.fileArea.textContent();
    expect(fileHint).toContain("点击选择文件");
  });

  test("import button disabled without file", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();

    await expect(iePage.importBtn).toHaveClass(/disabled/);
  });

  test("back button returns to home", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.clickBack();
    await authenticatedPage.waitForTimeout(1000);
    await expect(authenticatedPage.locator(".home-page")).toBeVisible();
  });

  test("export has year/month mode toggle", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await expect(iePage.modeChips).toHaveCount(2);
    await expect(iePage.monthPicker).toBeVisible();
    await expect(iePage.yearPicker).not.toBeVisible();
  });

  test("switch to year mode shows year picker", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();

    await iePage.switchToMode("year");
    await authenticatedPage.waitForTimeout(300);

    await expect(iePage.monthPicker).not.toBeVisible();
    await expect(iePage.yearPicker).toBeVisible();
  });

  test("year picker changes year on arrow click", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToMode("year");
    await authenticatedPage.waitForTimeout(300);

    const initialYear = await iePage.yearText.textContent();

    await iePage.clickPrevYear();
    await authenticatedPage.waitForTimeout(200);
    const prevYear = await iePage.yearText.textContent();
    expect(prevYear).not.toBe(initialYear);

    await iePage.clickNextYear();
    await authenticatedPage.waitForTimeout(200);
    const nextYear = await iePage.yearText.textContent();
    expect(nextYear).toBe(initialYear);
  });

  test("yearly export button triggers download", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToMode("year");
    await authenticatedPage.waitForTimeout(300);

    await iePage.selectFormat("json");
    await iePage.clickExport();
    await authenticatedPage.waitForTimeout(2000);
  });

  test("import format chips in import tab", async ({ authenticatedPage }) => {
    const iePage = new ImportExportPage(authenticatedPage);
    await iePage.goto();
    await iePage.switchToImport();
    await authenticatedPage.waitForTimeout(300);

    await expect(iePage.formatChips.first()).toBeVisible();
  });
});
