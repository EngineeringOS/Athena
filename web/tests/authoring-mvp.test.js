import { test, expect } from "@playwright/test";

test("browser completes the typed project and folio authoring workflow", async ({ page }) => {
  const errors = [];
  page.on("console", (message) => { if (message.type() === "error") errors.push(message.text()); });
  page.on("pageerror", (error) => errors.push(error.message));

  await page.goto("/");
  await expect(page.locator("#project-name")).toHaveValue("Main Distribution");
  await expect(page.locator("#folio-outline")).toContainText("Folio 1");

  await page.locator(".outline-panel [data-command='add-folio']").click();
  await page.locator(".outline-panel [data-command='add-folio']").click();
  await expect(page.locator("#folio-outline button")).toHaveCount(3);
  for (const label of ["Folio 1", "Folio 2", "Folio 3"]) {
    await expect(page.locator("#folio-outline")).toContainText(label);
  }

  await page.locator("#folio-outline button").nth(1).click();
  await page.locator("[data-widget='folio.label']").fill("Control");
  await page.locator("[data-widget='folio.label']").press("Enter");
  await page.locator("#folio-outline button").nth(2).click();
  await page.locator("[data-widget='folio.label']").fill("I/O");
  await page.locator("[data-widget='folio.label']").press("Enter");
  await page.getByRole("button", { name: "Move folio up" }).click();
  await expect(page.locator("#folio-outline")).toContainText("I/O");
  const orderBeforeSave = await page.locator("#folio-outline .folio-row span:last-child").allTextContents();
  expect(orderBeforeSave).toEqual(["Folio 1", "I/O", "Control"]);

  await page.locator("[data-widget='project.variables']").fill("plant=PLANT-A");
  await page.locator("[data-widget='project.variables']").press("Enter");
  await page.locator("[data-widget='project.variables']").fill("designer=A. Engineer");
  await page.locator("[data-widget='project.variables']").press("Enter");

  await page.locator("[data-widget='folio.title']").fill("Main control");
  await page.locator("[data-widget='folio.title']").press("Enter");
  await page.locator(".property-row:has([data-widget='folio.author'])").getByRole("button", { name: "Insert designer" }).click();
  await page.locator("[data-widget='folio.location']").fill("MCC-01");
  await page.locator("[data-widget='folio.location']").press("Enter");
  await page.locator("[data-widget='folio.revision']").fill("A");
  await page.locator("[data-widget='folio.revision']").press("Enter");
  await page.locator("[data-widget='folio.page_number']").fill("2");
  await page.locator("[data-widget='folio.page_number']").press("Enter");
  await page.locator("[data-widget='folio.variables']").fill("area=MCC-01");
  await page.locator("[data-widget='folio.variables']").press("Enter");
  await expect(page.locator("#resolved-title-block")).toContainText("A. Engineer");
  await expect(page.locator("#resolved-title-block")).toContainText("MCC-01");
  await expect(page.locator("#resolved-title-block")).toContainText("A");
  await expect(page.locator("#resolved-title-block")).toContainText("2");

  await page.getByRole("button", { name: "Save" }).click();
  await expect(page.locator("#status")).toContainText("Saved");
  await page.getByRole("button", { name: "Close" }).click();
  await page.getByRole("button", { name: "Reopen" }).click();
  await expect(page.locator("#project-name")).toHaveValue("Main Distribution");
  await expect(page.locator("#status")).toContainText("Reopened");
  await expect.poll(() => page.locator("#folio-outline .folio-row span:last-child").allTextContents()).toEqual(orderBeforeSave);
  await expect(page.locator("#resolved-title-block")).toContainText("A. Engineer");
  await expect(page.locator("#resolved-title-block")).toContainText("MCC-01");
  expect(errors).toEqual([]);
});

test("browser reports invalid variables through Rust diagnostics", async ({ page }) => {
  await page.goto("/");
  await page.locator("[data-widget='project.variables']").fill("=invalid");
  await page.locator("[data-widget='project.variables']").press("Enter");
  await expect(page.locator("#status")).toContainText("variable key");
  await expect(page.locator("#project-variable-rows")).not.toContainText("invalid");
});

test("browser renders the Graphite shell and Rust-owned electrical plate", async ({ page }) => {
  await page.goto("/");
  for (const region of ["app-bar", "tool-bar", "outline-rail", "canvas-stage", "properties-rail", "status-bar"]) {
    await expect(page.locator(`[data-region='${region}']`)).toBeVisible();
  }
  await expect(page.locator("#folio-plate")).toBeVisible();
  await expect(page.locator("#resolved-title-block")).toBeVisible();
  await page.getByRole("button", { name: "Collapse properties" }).click();
  await expect(page.locator(".workbench")).toHaveClass(/properties-collapsed/);
});
