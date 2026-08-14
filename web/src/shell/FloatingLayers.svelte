<!--
  Adapted from Graphite MainWindow.svelte:30-35 and Panel.svelte:471-479 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  let { state } = $props();
</script>

<div
  class="floating-layers"
  data-shell-region="floating"
  data-floating-layers
  data-overlay-group={state.overlay ?? undefined}
  aria-hidden="true"
></div>
