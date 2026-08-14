<script>
  import { onMount } from "svelte";

  import { dispatchShell, startAthena } from "./lib/athena.js";
  import { createShellStore } from "./lib/shell-store.svelte.js";
  import MainWindow from "./shell/MainWindow.svelte";

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
    <MainWindow {shellStore} dispatch={dispatchShell} />
  {/if}
</main>
