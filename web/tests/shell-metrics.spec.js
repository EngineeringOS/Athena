import { expect, test } from "@playwright/test";

test.use({ viewport: { width: 1440, height: 900 } });

test("Graphite shell hierarchy and measured geometry are reproduced", async ({ page }) => {
  const errors = [];
  page.on("console", (message) => {
    if (message.type() === "error") errors.push(message.text());
  });
  page.on("pageerror", (error) => errors.push(error.message));

  await page.goto("/");
  await expect(page.locator("[data-athena-shell]")).toHaveAttribute("data-wasm-status", "ready");

  const regions = page.locator("[data-main-window] > [data-shell-region]");
  await expect(regions).toHaveCount(4);
  expect(await regions.evaluateAll((items) => items.map((item) => item.dataset.shellRegion))).toEqual([
    "title",
    "workspace",
    "status",
    "floating",
  ]);

  await expect(page.locator("[data-title-bar]")).toHaveCSS("height", "28px");
  await expect(page.locator("[data-status-bar]")).toHaveCSS("height", "24px");
  await expect(page.locator("[data-panel-tab-bar]").first()).toHaveCSS("height", "28px");
  await expect(page.locator("[data-panel-group]").first()).toHaveCSS("border-radius", "6px");
  await expect(page.locator(".shell-control").first()).toHaveCSS("border-radius", "2px");
  await expect(page.locator("[data-gutter-axis='Horizontal']").first()).toHaveCSS("width", "4px");
  await expect(page.locator("[data-gutter-axis='Vertical']").first()).toHaveCSS("height", "4px");
  await expect(page.locator("body")).toHaveCSS("font-size", "14px");
  expect(await page.locator("body").evaluate((element) => getComputedStyle(element).fontFamily)).toContain("Source Sans Pro");

  const tokens = await page.locator("[data-main-window]").evaluate((element) => {
    const style = getComputedStyle(element);
    return ["--color-1-nearblack", "--color-2-mildblack", "--color-3-darkgray", "--color-e-nearwhite"].map((name) => style.getPropertyValue(name).trim());
  });
  expect(tokens).toEqual(["#111", "#222", "#333", "#eee"]);

  const rootWidths = await page.locator("[data-split-depth='0']").evaluate((root) =>
    [...root.children]
      .filter((child) => child.matches(".split-child"))
      .map((child) => child.getBoundingClientRect().width),
  );
  const total = rootWidths.reduce((sum, width) => sum + width, 0);
  expect(rootWidths).toHaveLength(3);
  expect(rootWidths[0] / total).toBeCloseTo(0.2, 2);
  expect(rootWidths[1] / total).toBeCloseTo(0.64, 2);
  expect(rootWidths[2] / total).toBeCloseTo(0.16, 2);
  expect(errors).toEqual([]);
});
