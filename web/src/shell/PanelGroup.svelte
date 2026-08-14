<!--
  Adapted from Graphite Panel.svelte:387-658 under Apache-2.0.
  Copyright Graphite contributors. Modified for Athena on 2026-08-15.
-->
<script>
  import { X } from "@lucide/svelte";
  import PanelContent from "../electrical/PanelContent.svelte";

  let { group, dispatch, dockPreview } = $props();
  let activeTab = $derived(group.tabs.find((tab) => tab.id === group.active_tab));
  let dockingPlacement = null;

  function activate(tab, focus = false) {
    dispatch("ActivateTab", { group_id: group.id, tab_id: tab.id });
    if (focus) requestAnimationFrame(() => document.querySelector(`[data-tab-id="${tab.id}"]`)?.focus());
  }

  function tabKeydown(event, index) {
    let target = index;
    if (event.key === "ArrowRight") target = (index + 1) % group.tabs.length;
    else if (event.key === "ArrowLeft") target = (index - 1 + group.tabs.length) % group.tabs.length;
    else if (event.key === "Home") target = 0;
    else if (event.key === "End") target = group.tabs.length - 1;
    else return;
    event.preventDefault();
    activate(group.tabs[target], true);
  }

  function closeTab(event, tab) {
    event.stopPropagation();
    dispatch("ClosePanel", { tab_id: tab.id });
  }

  function dragStart(event, tab) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData(
      "application/x-athena-tab",
      JSON.stringify({ tabId: tab.id, groupId: group.id }),
    );
  }

  function tabDrop(event, index) {
    event.preventDefault();
    const source = draggedTab(event);
    if (!source) return;
    if (source.groupId === group.id) {
      dispatch("ReorderTab", { group_id: group.id, tab_id: source.tabId, to: index });
    } else {
      dispatch("MoveTab", { tab_id: source.tabId, target_group_id: group.id, to: index });
    }
    clearDockPreview();
  }

  function bodyDragOver(event) {
    event.preventDefault();
    const bounds = event.currentTarget.getBoundingClientRect();
    const x = (event.clientX - bounds.left) / bounds.width;
    const y = (event.clientY - bounds.top) / bounds.height;
    if (x < 0.25) dockingPlacement = "Left";
    else if (x > 0.75) dockingPlacement = "Right";
    else if (y < 0.25) dockingPlacement = "Top";
    else if (y > 0.75) dockingPlacement = "Bottom";
    else dockingPlacement = "Center";
    dispatch("SetDockPreview", { target: { group_id: group.id, placement: dockingPlacement } });
  }

  function bodyDrop(event) {
    event.preventDefault();
    const source = draggedTab(event);
    if (!source || !dockingPlacement) return;
    if (dockingPlacement === "Center") {
      if (source.groupId !== group.id) {
        dispatch("MoveTab", {
          tab_id: source.tabId,
          target_group_id: group.id,
          to: group.tabs.length,
        });
      }
    } else {
      dispatch("SplitGroup", {
        tab_id: source.tabId,
        target_group_id: group.id,
        placement: dockingPlacement,
        new_group_id: crypto.randomUUID(),
        new_split_id: crypto.randomUUID(),
      });
    }
    clearDockPreview();
  }

  function draggedTab(event) {
    try {
      return JSON.parse(event.dataTransfer.getData("application/x-athena-tab"));
    } catch {
      return null;
    }
  }

  function clearDockPreview() {
    dockingPlacement = null;
    dispatch("SetDockPreview", { target: null });
  }
</script>

<section class="panel-group" data-panel-group={group.id}>
  <div class="tab-bar" role="tablist" data-panel-tab-bar={group.id}>
    <div class="tab-group">
      {#each group.tabs as tab, index (tab.id)}
        <div
          class:active={tab.id === group.active_tab}
          class="panel-tab"
          data-tab-id={tab.id}
          role="tab"
          aria-label={tab.label}
          aria-selected={tab.id === group.active_tab}
          tabindex={tab.id === group.active_tab ? 0 : -1}
          draggable="true"
          onclick={() => activate(tab)}
          onkeydown={(event) => tabKeydown(event, index)}
          ondragstart={(event) => dragStart(event, tab)}
          ondragover={(event) => event.preventDefault()}
          ondrop={(event) => tabDrop(event, index)}
        >
          <span class="tab-name">{tab.label}</span>
          {#if tab.closeable}
            <button
              class="tab-close icon-control shell-control"
              type="button"
              aria-label={`Close ${tab.label}`}
              title={`Close ${tab.label}`}
              onclick={(event) => closeTab(event, tab)}
            >
              <X size={13} strokeWidth={1.5} />
            </button>
          {/if}
        </div>
      {/each}
    </div>
  </div>
  <div
    class="panel-body"
    data-panel-body={group.id}
    data-panel-role={activeTab?.role}
    role="tabpanel"
    aria-label={activeTab?.label}
    tabindex="0"
    ondragover={bodyDragOver}
    ondragleave={(event) => {
      if (!event.currentTarget.contains(event.relatedTarget)) clearDockPreview();
    }}
    ondrop={bodyDrop}
  >
    <PanelContent role={activeTab?.role} />
  </div>
  {#if dockPreview?.group_id === group.id}
    <div class={`docking-ghost ${dockPreview.placement.toLowerCase()}`} aria-hidden="true"></div>
  {/if}
</section>
