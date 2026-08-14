import { expect, test } from "@playwright/test";

test.use({ viewport: { width: 1440, height: 900 } });

test.beforeEach(async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("[data-athena-shell]")).toHaveAttribute("data-wasm-status", "ready");
});

test("tab pointer and keyboard activation are reduced from Rust effects", async ({ page }) => {
  const project = page.getByRole("tab", { name: "Project" });
  const folios = page.getByRole("tab", { name: "Folios" });

  await expect(project).toHaveAttribute("aria-selected", "true");
  await folios.click();
  await expect(folios).toHaveAttribute("aria-selected", "true");
  await expect(project).toHaveAttribute("aria-selected", "false");

  await project.focus();
  await project.press("ArrowRight");
  await expect(folios).toHaveAttribute("aria-selected", "true");
  await expect(folios).toBeFocused();
});

test("gutter drag commits, Escape aborts, and double click resets adjacent shares", async ({ page }) => {
  const root = page.locator("[data-split-depth='0']");
  const gutter = root.locator(":scope > [data-gutter-axis='Horizontal']").first();
  const widths = () => root.evaluate((element) =>
    [...element.children]
      .filter((child) => child.matches(".split-child"))
      .map((child) => child.getBoundingClientRect().width),
  );

  const before = await widths();
  const box = await gutter.boundingBox();
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + 82, box.y + box.height / 2);
  await page.mouse.up();
  const committed = await widths();
  expect(committed[0]).toBeGreaterThan(before[0] + 50);

  const movedBox = await gutter.boundingBox();
  await page.mouse.move(movedBox.x + movedBox.width / 2, movedBox.y + movedBox.height / 2);
  await page.mouse.down();
  await page.mouse.move(movedBox.x + 62, movedBox.y + movedBox.height / 2);
  await page.keyboard.press("Escape");
  const aborted = await widths();
  expect(aborted[0]).toBeCloseTo(committed[0], 0);

  await gutter.dblclick();
  const reset = await widths();
  expect(reset[0] / (reset[0] + reset[1])).toBeCloseTo(17 / 84, 2);
});
