<script>
  import { onMount } from "svelte";

  import { startAthena } from "./lib/athena.js";
  import { createShellStore } from "./lib/shell-store.svelte.js";

  const shellStore = createShellStore();

  onMount(async () => {
    try {
      await startAthena(shellStore.reduceEffects);
      shellStore.markReady();
    } catch (error) {
      shellStore.markFailed();
      console.error("Athena WASM startup failed", error);
    }
  });
</script>

<main
  data-athena-shell
  data-wasm-status={shellStore.wasmStatus}
  aria-label="Athena electrical schematic editor"
>
  {#if shellStore.shell}
    <section data-shell-root aria-label="Electrical workspace"></section>
  {/if}
</main>

<style>
  :global(*) {
    box-sizing: border-box;
  }

  :global(html, body, #app) {
    width: 100%;
    height: 100%;
    margin: 0;
  }

  :global(body) {
    overflow: hidden;
    background: #111;
    color: #eee;
    font-family: "Source Sans Pro", "Segoe UI", sans-serif;
    font-size: 14px;
    letter-spacing: 0;
  }

  main {
    width: 100%;
    height: 100%;
    background: #111;
  }
</style>
