<!--
  Adapted from Graphite MainWindow.svelte:30-35 and Panel.svelte:471-479 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import { X } from "@lucide/svelte";

  import PanelGroup from "./PanelGroup.svelte";

  let { state, root, dispatch, onClose } = $props();
  let overlayGroup = $derived(findGroup(root, state.overlay));

  $effect(() => {
    if (overlayGroup) requestAnimationFrame(() => document.querySelector("[data-overlay-close]")?.focus());
  });

  function findGroup(node, id) {
    if (!id) return null;
    if (node.type === "PanelGroup") return node.data.id === id ? node.data : null;
    for (const child of node.data.children) {
      const group = findGroup(child.node, id);
      if (group) return group;
    }
    return null;
  }

</script>

<svelte:window onkeydown={(event) => {
  if (overlayGroup && event.key === "Escape") {
    event.preventDefault();
    onClose();
  }
}} />

<div
  class:has-overlay={Boolean(overlayGroup)}
  class="floating-layers"
  data-shell-region="floating"
  data-floating-layers
  data-overlay-group={state.overlay ?? undefined}
  aria-hidden={!overlayGroup}
>
  {#if overlayGroup}
    <div class="overlay-backdrop">
      <button
        class="overlay-scrim"
        type="button"
        aria-label="Close panel overlay"
        data-overlay-backdrop
        onclick={onClose}
      ></button>
      <section class="panel-overlay" data-panel-overlay aria-label="Workspace panel overlay">
        <button
          class="overlay-close icon-control shell-control"
          type="button"
          aria-label="Close panel overlay"
          title="Close panel overlay"
          data-overlay-close
          onclick={onClose}
        >
          <X size={16} strokeWidth={1.5} />
        </button>
        <PanelGroup group={overlayGroup} {dispatch} dockPreview={state.dock_preview} />
      </section>
    </div>
  {/if}
</div>
