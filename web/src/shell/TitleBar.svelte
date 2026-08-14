<!--
  Adapted from Graphite TitleBar.svelte:35-168 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import { Minus, Square, X } from "@lucide/svelte";

  let { state } = $props();
</script>

<header class="title-bar" data-shell-region="title" data-title-bar>
  <nav class="menu-bar" aria-label="Application menu">
    <button class="menu-command shell-control" type="button">File</button>
    <button class="menu-command shell-control" type="button">Edit</button>
    <button class="menu-command shell-control" type="button">View</button>
  </nav>
  <div class="window-frame">
    <span>{state.document_label}</span>
    {#if state.dirty}<span class="dirty-indicator" aria-label="Modified">*</span>{/if}
  </div>
  <div class="window-actions" aria-label="Window controls">
    <button class="icon-control shell-control" type="button" aria-label="Minimize" title="Minimize" disabled>
      <Minus size={14} strokeWidth={1.5} />
    </button>
    <button class="icon-control shell-control" type="button" aria-label="Maximize" title="Maximize" disabled>
      <Square size={12} strokeWidth={1.5} />
    </button>
    <button class="icon-control shell-control" type="button" aria-label="Close" title="Close" disabled>
      <X size={14} strokeWidth={1.5} />
    </button>
  </div>
</header>
