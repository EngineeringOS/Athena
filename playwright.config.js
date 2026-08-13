import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./web/tests",
  timeout: 30_000,
  use: {
    baseURL: "http://127.0.0.1:8080",
    trace: "retain-on-failure",
  },
  webServer: {
    command: "python -m http.server 8080 --directory web",
    url: "http://127.0.0.1:8080/",
    reuseExistingServer: true,
    timeout: 15_000,
  },
});
