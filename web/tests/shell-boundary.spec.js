import { expect, test } from "@playwright/test";

test("Svelte hosts one Rust-owned shell without legacy schematic state", async ({ page }) => {
  const errors = [];
  page.on("console", (message) => {
    if (message.type() === "error") errors.push(message.text());
  });
  page.on("pageerror", (error) => errors.push(error.message));

  await page.goto("/");

  const shell = page.locator("[data-athena-shell]");
  await expect(shell).toHaveCount(1);
  await expect(shell).toHaveAttribute("data-wasm-status", "ready");
  await expect(page.locator(".schematic-preview, .wire, .symbol")).toHaveCount(0);

  const wasmImported = await page.evaluate(() =>
    performance
      .getEntriesByType("resource")
      .some((entry) => new URL(entry.name).pathname.endsWith("/pkg/athena_web_core.js")),
  );
  expect(wasmImported).toBe(true);

  const debugKeys = await page.evaluate(() => {
    const visit = (value, keys = []) => {
      if (!value || typeof value !== "object") return keys;
      for (const [key, child] of Object.entries(value)) {
        keys.push(key.toLowerCase());
        visit(child, keys);
      }
      return keys;
    };
    return visit(window.__athenaShellDebug ?? {});
  });
  for (const forbidden of ["project", "folio", "symbol", "conductor"]) {
    expect(debugKeys.some((key) => key.includes(forbidden))).toBe(false);
  }
  expect(errors).toEqual([]);
});
