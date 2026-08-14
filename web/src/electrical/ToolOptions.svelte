<script>
  import { Focus, Grid3x3, Maximize2, RotateCcw, WandSparkles } from "@lucide/svelte";

  const tools = [
    ["Display grid", Grid3x3],
    ["Zoom content", Focus],
    ["Fit in view", Maximize2],
    ["Reset zoom", RotateCcw],
    ["Automatic conductor creation", WandSparkles],
  ];
</script>

<nav class="tool-options" aria-label="Folio display and conductor options">
  {#each tools as [label, Icon]}
    <button
      class="electrical-tool shell-control"
      type="button"
      aria-label={label}
      title={label}
      data-electrical-tool
      disabled
    >
      <Icon size={15} strokeWidth={1.5} />
    </button>
  {/each}
</nav>

<style>
  .tool-options {
    display: flex;
    align-items: center;
    min-width: 0;
    padding: 0 4px;
    border-bottom: 1px solid var(--color-4-dimgray);
    background: var(--color-1-nearblack);
  }

  .electrical-tool {
    display: grid;
    width: 28px;
    height: 26px;
    padding: 0;
    border: 0;
    border-radius: 2px;
    place-items: center;
    color: var(--color-8-uppergray);
    background: transparent;
    opacity: 0.65;
  }
</style>
