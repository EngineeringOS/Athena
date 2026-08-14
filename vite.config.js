import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import { svelte } from "@sveltejs/vite-plugin-svelte";

export default defineConfig({
  root: fileURLToPath(new URL("./web", import.meta.url)),
  plugins: [svelte()],
  build: {
    outDir: "dist",
    emptyOutDir: true,
  },
});
