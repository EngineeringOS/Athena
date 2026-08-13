import { test, expect } from "@playwright/test";

test("shared browser shell places, selects, edits, and undoes schematic content", async ({ page }) => {
  await page.goto("http://127.0.0.1:8080/");
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 140, y: 120 } });
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 260, y: 160 } });
  await page.getByRole("button", { name: "Wire" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 160, y: 120 } });
  await page.locator("#schematic-canvas").click({ position: { x: 280, y: 160 } });
  await page.locator("#schematic-canvas").dragTo(page.locator("#schematic-canvas"), {
    sourcePosition: { x: 90, y: 70 },
    targetPosition: { x: 310, y: 190 },
  });
  await page.locator("#schematic-canvas").dragTo(page.locator("#schematic-canvas"), {
    sourcePosition: { x: 280, y: 120 },
    targetPosition: { x: 280, y: 140 },
  });
  await expect(page.locator("#footer-status")).toContainText("interactive regions");
  await page.locator("#symbol-reference").fill("K1");
  await page.locator("#symbol-reference").press("Enter");
  await page.getByRole("button", { name: "Undo" }).click();
  await expect(page.locator("canvas")).toBeVisible();
});

test("browser shell boots the library and placement changes the scene", async ({ page }) => {
  const errors = [];
  page.on("console", message => { if (message.type() === "error") errors.push(message.text()); });
  page.on("pageerror", error => errors.push(error.message));
  await page.goto("http://127.0.0.1:8080/");
  await expect(page.getByRole("button", { name: "Resistor" })).toBeVisible();
  await expect(page.locator("#status")).toContainText("Ready");
  const before = await page.locator("#footer-status").textContent();
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 140, y: 120 } });
  await expect(page.locator("#footer-status")).not.toHaveText(before);
  expect(errors).toEqual([]);
});
