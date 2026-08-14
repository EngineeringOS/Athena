<!--
  Adapted from Graphite MainWindow.svelte:22-35 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import { FolderTree, Library, SlidersHorizontal } from "@lucide/svelte";

  import FloatingLayers from "./FloatingLayers.svelte";
  import PanelSubdivision from "./PanelSubdivision.svelte";
  import StatusBar from "./StatusBar.svelte";
  import TitleBar from "./TitleBar.svelte";
  import "./shell.css";

  let { shellStore, dispatch } = $props();
  let overlayTrigger;

  let projectGroup = $derived(findGroupForRole(shellStore.shell.root, "Project"));
  let libraryGroup = $derived(findGroupForRole(shellStore.shell.root, "Elements"));
  let propertiesGroup = $derived(findGroupForRole(shellStore.shell.root, "SelectionProperties"));

  function openOverlay(event, group) {
    if (!group) return;
    overlayTrigger = event.currentTarget;
    dispatch("OpenOverlay", { group_id: group.id });
  }

  function closeOverlay() {
    dispatch("CloseOverlay");
    requestAnimationFrame(() => overlayTrigger?.focus());
  }

  function findGroupForRole(node, role) {
    if (node.type === "PanelGroup") {
      return node.data.tabs.some((tab) => tab.role === role) ? node.data : null;
    }
    for (const child of node.data.children) {
      const group = findGroupForRole(child.node, role);
      if (group) return group;
    }
    return null;
  }
</script>

<div class="main-window" data-main-window>
  <TitleBar state={shellStore.shell.title_bar} />
  <section class="workspace" data-shell-region="workspace" data-workspace>
    <nav class="narrow-panel-controls" data-narrow-panel-controls aria-label="Workspace panels">
      <button
        class="shell-control"
        type="button"
        aria-label="Project panels"
        aria-expanded={shellStore.shell.floating_layers.overlay === projectGroup?.id}
        disabled={!projectGroup}
        onclick={(event) => openOverlay(event, projectGroup)}
      >
        <FolderTree size={15} strokeWidth={1.5} />
        <span>Project</span>
      </button>
      <button
        class="shell-control"
        type="button"
        aria-label="Library panels"
        aria-expanded={shellStore.shell.floating_layers.overlay === libraryGroup?.id}
        disabled={!libraryGroup}
        onclick={(event) => openOverlay(event, libraryGroup)}
      >
        <Library size={15} strokeWidth={1.5} />
        <span>Library</span>
      </button>
      <button
        class="shell-control"
        type="button"
        aria-label="Properties panels"
        aria-expanded={shellStore.shell.floating_layers.overlay === propertiesGroup?.id}
        disabled={!propertiesGroup}
        onclick={(event) => openOverlay(event, propertiesGroup)}
      >
        <SlidersHorizontal size={15} strokeWidth={1.5} />
        <span>Properties</span>
      </button>
    </nav>
    <PanelSubdivision
      node={shellStore.shell.root}
      depth={0}
      {dispatch}
      dockPreview={shellStore.shell.floating_layers.dock_preview}
    />
  </section>
  <StatusBar state={shellStore.shell.status_bar} />
  <FloatingLayers
    state={shellStore.shell.floating_layers}
    root={shellStore.shell.root}
    {dispatch}
    onClose={closeOverlay}
  />
</div>
