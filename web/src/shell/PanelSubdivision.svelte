<!--
  Adapted from Graphite PanelSubdivision.svelte:201-292 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import Gutter from "./Gutter.svelte";
  import PanelGroup from "./PanelGroup.svelte";
  import PanelSubdivision from "./PanelSubdivision.svelte";

  let { node, depth, dispatch, dockPreview } = $props();
  let split = $derived(node.type === "Split" ? node.data : null);
  let horizontal = $derived(split?.axis === "Horizontal");

  function containsDocument(candidate) {
    if (candidate.type === "PanelGroup") {
      return candidate.data.tabs.some((tab) => tab.role === "FolioDocument");
    }
    return candidate.data.children.some((child) => containsDocument(child.node));
  }
</script>

{#if node.type === "PanelGroup"}
  <PanelGroup group={node.data} {dispatch} {dockPreview} />
{:else}
  <div
    class:horizontal
    class:vertical={!horizontal}
    class="panel-subdivision"
    data-split-id={split.id}
    data-split-depth={depth}
  >
    {#each split.children as child, index}
      {#if index > 0}
        <Gutter
          splitId={split.id}
          beforeIndex={index - 1}
          axis={split.axis}
          {dispatch}
        />
      {/if}
      <div
        class="split-child"
        style:flex-grow={child.share}
        data-share={child.share}
        data-contains-document={containsDocument(child.node)}
      >
        <PanelSubdivision
          node={child.node}
          depth={depth + 1}
          {dispatch}
          {dockPreview}
        />
      </div>
    {/each}
  </div>
{/if}
