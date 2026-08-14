/** Creates the browser projection of Rust-owned shell presentation state. */
export function createShellStore() {
  let shell = $state(null);
  let wasmStatus = $state("loading");

  function reduceEffects(effects) {
    for (const effect of effects) {
      if (effect.type !== "Shell") continue;
      reduceShellEffect(effect.data);
    }
  }

  function reduceShellEffect(effect) {
    if (effect.type === "Replaced") {
      shell = effect.data;
      return;
    }
    if (!shell) return;
    if (effect.type === "ValuesChanged") {
      patchValues(shell.root, effect.data.active_tabs, effect.data.shares);
      shell.focus = effect.data.focus;
      shell.floating_layers.overlay = effect.data.overlay;
      return;
    }
    if (effect.type === "DockPreview") {
      shell.floating_layers.dock_preview = effect.data;
    }
  }

  return {
    get shell() {
      return shell;
    },
    get wasmStatus() {
      return wasmStatus;
    },
    reduceEffects,
    markReady() {
      wasmStatus = "ready";
    },
    markFailed() {
      wasmStatus = "failed";
    },
  };
}

// Value-only effects patch stable identities and never create a browser-owned
// topology or semantic document model.
function patchValues(node, activeTabs, shares) {
  if (node.type === "PanelGroup") {
    const active = activeTabs.find(([groupId]) => groupId === node.data.id);
    if (active) node.data.active_tab = active[1];
    return;
  }
  const splitShares = shares.find(([splitId]) => splitId === node.data.id)?.[1];
  if (splitShares) {
    node.data.children.forEach((child, index) => {
      child.share = splitShares[index];
    });
  }
  node.data.children.forEach((child) => patchValues(child.node, activeTabs, shares));
}
