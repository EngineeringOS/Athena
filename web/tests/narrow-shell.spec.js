import { expect, test } from "@playwright/test";

test.use({ viewport: { width: 390, height: 844 }, hasTouch: true });

test("narrow shell keeps the folio visible and presents Rust-selected panel overlays", async ({ page }) => {
  const errors = [];
  page.on("console", (message) => {
    if (message.type() === "error") errors.push(message.text());
  });
  page.on("pageerror", (error) => errors.push(error.message));

  await page.goto("/");
  await expect(page.locator("[data-athena-shell]")).toHaveAttribute("data-wasm-status", "ready");
  await expect(page.locator("[data-empty-folio]")).toBeVisible();

  const project = page.getByRole("button", { name: "Project panels" });
  const library = page.getByRole("button", { name: "Library panels" });
  const properties = page.getByRole("button", { name: "Properties panels" });
  await expect(project).toBeVisible();
  await expect(library).toBeVisible();
  await expect(properties).toBeVisible();

  await project.click();
  const overlay = page.locator("[data-panel-overlay]");
  await expect(overlay).toBeVisible();
  await expect(overlay.getByRole("tab", { name: "Project" })).toBeVisible();
  await expect(overlay.getByRole("tab", { name: "Folios" })).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(overlay).toHaveCount(0);
  await expect(project).toBeFocused();

  await library.tap();
  await expect(overlay.getByRole("tab", { name: "Elements" })).toBeVisible();
  await page.locator("[data-overlay-backdrop]").click({ position: { x: 2, y: 2 } });
  await expect(overlay).toHaveCount(0);
  await expect(library).toBeFocused();

  await properties.focus();
  await properties.press("Enter");
  await expect(overlay.getByRole("tab", { name: "Selection Properties" })).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(properties).toBeFocused();

  const geometry = await page.evaluate(() => {
    const rect = (selector) => document.querySelector(selector).getBoundingClientRect();
    const controls = rect("[data-narrow-panel-controls]");
    const options = rect(".tool-options");
    const status = rect("[data-status-bar]");
    const surface = document.querySelector("[data-document-surface]");
    return {
      controlsBottom: controls.bottom,
      optionsTop: options.top,
      optionsBottom: options.bottom,
      statusTop: status.top,
      pageScrollsCoherently: surface.scrollWidth >= surface.clientWidth,
    };
  });
  expect(geometry.controlsBottom).toBeLessThanOrEqual(geometry.optionsTop);
  expect(geometry.optionsBottom).toBeLessThan(geometry.statusTop);
  expect(geometry.pageScrollsCoherently).toBe(true);
  expect(errors).toEqual([]);
});
