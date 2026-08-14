<!--
  Adapted from Graphite MainWindow.svelte:22-35 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import FloatingLayers from "./FloatingLayers.svelte";
  import PanelSubdivision from "./PanelSubdivision.svelte";
  import StatusBar from "./StatusBar.svelte";
  import TitleBar from "./TitleBar.svelte";
  import "./shell.css";

  let { shellStore, dispatch } = $props();
</script>

<div class="main-window" data-main-window>
  <TitleBar state={shellStore.shell.title_bar} />
  <section class="workspace" data-shell-region="workspace" data-workspace>
    <PanelSubdivision
      node={shellStore.shell.root}
      depth={0}
      {dispatch}
      dockPreview={shellStore.shell.floating_layers.dock_preview}
    />
  </section>
  <StatusBar state={shellStore.shell.status_bar} />
  <FloatingLayers state={shellStore.shell.floating_layers} />
</div>
