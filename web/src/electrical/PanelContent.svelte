<script>
  import EmptyFolio from "./EmptyFolio.svelte";
  import ToolOptions from "./ToolOptions.svelte";
  import ToolShelf from "./ToolShelf.svelte";

  let { role } = $props();

  const emptyStates = {
    Project: ["Project", "No project structure"],
    Folios: ["Folios", "No additional folios"],
    Elements: ["Elements", "No electrical elements"],
    TitleBlocks: ["Title Blocks", "No title block templates"],
    SelectionProperties: ["Selection Properties", "Nothing selected"],
    FolioProperties: ["Folio Properties", "No folio properties"],
    Diagnostics: ["Diagnostics", "No diagnostics"],
    History: ["History", "No history entries"],
  };
</script>

{#if role === "FolioDocument"}
  <div class="folio-editor" data-folio-editor>
    <ToolShelf />
    <ToolOptions />
    <EmptyFolio />
  </div>
{:else if emptyStates[role]}
  <section class="panel-empty-state" aria-label={emptyStates[role][0]}>
    <header>{emptyStates[role][0]}</header>
    <p>{emptyStates[role][1]}</p>
  </section>
{/if}

<style>
  .panel-empty-state {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
    padding: 10px 12px;
    color: var(--color-a-softgray);
  }

  .panel-empty-state header {
    padding-bottom: 7px;
    border-bottom: 1px solid var(--color-4-dimgray);
    color: var(--color-d-mutedwhite);
    font-weight: 600;
  }

  .panel-empty-state p {
    margin: 12px 0 0;
    color: var(--color-8-uppergray);
  }

  .folio-editor {
    display: grid;
    flex: 1;
    grid-template: 30px minmax(0, 1fr) / 34px minmax(0, 1fr);
    min-width: 0;
    min-height: 0;
    background: var(--color-2-mildblack);
  }

  .folio-editor :global(.tool-shelf) {
    grid-row: 1 / 3;
    grid-column: 1;
  }

  .folio-editor :global(.tool-options) {
    grid-row: 1;
    grid-column: 2;
  }

  .folio-editor :global(.document-surface) {
    grid-row: 2;
    grid-column: 2;
  }
</style>
