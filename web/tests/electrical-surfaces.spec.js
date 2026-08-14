import { expect, test } from "@playwright/test";

test.use({ viewport: { width: 1440, height: 900 } });

test("QElectroTech panel taxonomy surrounds a truly empty electrical folio", async ({ page }) => {
  const errors = [];
  page.on("console", (message) => {
    if (message.type() === "error") errors.push(message.text());
  });
  page.on("pageerror", (error) => errors.push(error.message));

  await page.goto("/");
  await expect(page.locator("[data-athena-shell]")).toHaveAttribute("data-wasm-status", "ready");

  const groups = await page.locator("[data-panel-group]").evaluateAll((items) =>
    items.map((group) =>
      [...group.querySelectorAll(":scope > [data-panel-tab-bar] [role='tab']")].map(
        (tab) => tab.getAttribute("aria-label"),
      ),
    ),
  );
  expect(groups).toEqual([
    ["Project", "Folios"],
    ["Elements", "Title Blocks"],
    ["Folio 1"],
    ["Selection Properties", "Folio Properties"],
    ["Diagnostics", "History"],
  ]);

  const tools = page.locator("[data-electrical-tool]");
  await expect(tools).toHaveCount(10);
  expect(await tools.evaluateAll((items) => items.map((item) => item.getAttribute("aria-label")))).toEqual([
    "Select",
    "Move view",
    "Place conductor",
    "Place element",
    "Add annotation",
    "Display grid",
    "Zoom content",
    "Fit in view",
    "Reset zoom",
    "Automatic conductor creation",
  ]);
  for (const tool of await tools.all()) await expect(tool).toBeDisabled();

  const pageSurface = page.locator("[data-empty-folio]");
  await expect(pageSurface).toBeVisible();
  await expect(pageSurface.locator(":scope > .folio-grid")).toHaveCount(1);
  await expect(pageSurface.locator(":scope > [data-title-block-frame]")).toHaveCount(1);
  await expect(page.locator(".wire, .symbol, .selection, input, textarea, select, [contenteditable='true']")).toHaveCount(0);
  await expect(page.locator("body")).not.toContainText(/Main Distribution|PLANT-A|Q1|K1|M1/);
  await expect(page.locator("[data-panel-role='Diagnostics']")).toContainText("No diagnostics");
  await expect(page.locator("[data-status-bar]")).toContainText("Folio 1");
  await expect(page.locator("[data-status-bar]")).toContainText("100%");
  await expect(page.locator("[data-status-bar]")).toContainText("Grid 10 mm");
  expect(errors).toEqual([]);
});
