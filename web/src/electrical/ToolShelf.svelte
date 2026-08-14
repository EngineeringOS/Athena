<script>
  import { Cable, CircuitBoard, Hand, MousePointer2, Type } from "@lucide/svelte";

  const tools = [
    ["Select", MousePointer2],
    ["Move view", Hand],
    ["Place conductor", Cable],
    ["Place element", CircuitBoard],
    ["Add annotation", Type],
  ];
</script>

<nav class="tool-shelf" aria-label="Electrical tools">
  {#each tools as [label, Icon]}
    <button
      class="electrical-tool shell-control"
      type="button"
      aria-label={label}
      title={label}
      data-electrical-tool
      disabled
    >
      <Icon size={16} strokeWidth={1.5} />
    </button>
  {/each}
</nav>

<style>
  .tool-shelf {
    display: flex;
    flex-direction: column;
    align-items: stretch;
    border-right: 1px solid var(--color-4-dimgray);
    background: var(--color-1-nearblack);
  }

  .electrical-tool {
    display: grid;
    width: 34px;
    height: 34px;
    padding: 0;
    border: 0;
    border-radius: 2px;
    place-items: center;
    color: var(--color-8-uppergray);
    background: transparent;
    opacity: 0.65;
  }
</style>
