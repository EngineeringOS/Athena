<!--
  Adapted from Graphite StatusBar.svelte:27-91 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  let { state } = $props();
</script>

<footer class="status-bar" data-shell-region="status" data-status-bar>
  <div class="status-hints"><span>{state.hint}</span></div>
  <span class="status-separator" aria-hidden="true"></span>
  <div class="status-info">
    {#each state.document_info as item}
      <span>{item}</span>
    {/each}
  </div>
</footer>
