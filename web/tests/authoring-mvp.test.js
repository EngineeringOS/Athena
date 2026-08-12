import { test, expect } from "@playwright/test";

test("places a symbol, draws a wire, and undoes it", async ({ page }) => {
  await page.goto("http://127.0.0.1:8080/");
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 140, y: 120 } });
  await page.getByRole("button", { name: "Wire" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 110, y: 120 } });
  await page.locator("#schematic-canvas").click({ position: { x: 170, y: 120 } });
  await expect(page.locator("#footer-status")).toContainText("interactive regions");
  await page.getByRole("button", { name: "Undo" }).click();
  await expect(page.locator("canvas")).toBeVisible();
});
