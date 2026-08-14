import init, { WasmAthenaEditor } from "./pkg/athena_web_core.js";

// The browser owns DOM and file interactions; every authoring change crosses
// the tagged Rust message/effect boundary below.

const dom = {
  workbench: document.querySelector(".workbench"),
  projectName: document.querySelector("#project-name"),
  windowProjectName: document.querySelector("#window-project-name"),
  dirtyMarker: document.querySelector("#dirty-marker"),
  tabDirty: document.querySelector("#tab-dirty"),
  outline: document.querySelector("#folio-outline"),
  folioCount: document.querySelector("#folio-count"),
  projectVariables: document.querySelector("[data-widget='project.variables']"),
  projectVariableRows: document.querySelector("#project-variable-rows"),
  projectDefaults: document.querySelector("#project-defaults"),
  folioPlate: document.querySelector("#folio-plate"),
  resolved: document.querySelector("#resolved-title-block"),
  activeFolioName: document.querySelector("#active-folio-name"),
  documentTabName: document.querySelector("#document-tab-name"),
  propertiesFolioName: document.querySelector("#properties-folio-name"),
  status: document.querySelector("#status"),
  activeContext: document.querySelector("#active-context"),
  saveState: document.querySelector("#save-state"),
  openFile: document.querySelector("#open-file"),
};

const cache = {
  projectId: null,
  projectName: "",
  activeFolioId: null,
  outline: [],
  workspace: null,
  plates: new Map(),
  resolved: new Map(),
  dirty: false,
  lastSavedBytes: null,
  openMode: "file",
};

let editor;

function tagged(type, data) {
  return data === undefined ? { type } : { type, data };
}

function send(family, type, data) {
  const message = tagged(family, tagged(type, data));
  routeEffects(JSON.parse(editor.dispatch(JSON.stringify(message))));
}

function targetKey(target) {
  return target === "Project" ? "project" : `folio:${target.Folio}`;
}

function activeTarget() {
  return { Folio: cache.activeFolioId };
}

function routeEffects(effects) {
  for (const effect of effects) {
    const data = effect.data;
    switch (effect.type) {
      case "ProjectOpened":
        cache.projectId = data.project_id;
        cache.projectName = data.name;
        setStatus(`Opened ${data.name}`);
        break;
      case "ProjectClosed":
        cache.projectId = null;
        cache.projectName = "";
        cache.activeFolioId = null;
        cache.outline = [];
        cache.plates.clear();
        cache.resolved.clear();
        cache.dirty = false;
        setStatus("Project closed");
        break;
      case "ActiveFolioChanged":
        cache.activeFolioId = data.folio_id;
        break;
      case "DirtyStateChanged":
        cache.dirty = data.dirty;
        break;
      case "OutlineChanged":
        cache.projectName = data.project_name;
        cache.outline = data.folios;
        break;
      case "WorkspaceLayoutUpdated":
        cache.workspace = data;
        break;
      case "PanelLayoutUpdated":
        cache.plates.set(targetKey(data.target), data.widgets);
        break;
      case "WidgetValuesUpdated": {
        const widgets = cache.plates.get(targetKey(data.target)) ?? [];
        for (const [id, value] of data.values) {
          const widget = widgets.find((candidate) => candidate.id === id);
          if (widget) widget.value = value;
        }
        break;
      }
      case "ResolvedTitleBlockUpdated":
        cache.resolved.set(data.folio_id, data.display);
        break;
      case "SaveRequested":
        completeBrowserSave(data);
        break;
      case "OpenRequested":
        completeBrowserOpen();
        break;
      case "Diagnostic":
        setStatus(data.message);
        break;
      case "Error":
        setStatus(`Error: ${data.message}`);
        break;
    }
  }
  render();
}

function completeBrowserSave(request) {
  const bytes = Uint8Array.from(request.bytes);
  cache.lastSavedBytes = bytes;
  const url = URL.createObjectURL(new Blob([bytes], { type: "application/json" }));
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = `${cache.projectName || "athena-project"}.athena.json`;
  anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  const effects = editor.deliver_save_result(
    BigInt(request.request_id),
    request.project_id,
    BigInt(request.revision),
    JSON.stringify(tagged("Success")),
  );
  routeEffects(JSON.parse(effects));
  setStatus("Saved");
}

function completeBrowserOpen() {
  if (cache.openMode === "recent") {
    cache.openMode = "file";
    if (!cache.lastSavedBytes) {
      setStatus("No recent project is available");
      return;
    }
    routeEffects(JSON.parse(editor.deliver_open_bytes(cache.lastSavedBytes)));
    setStatus("Reopened");
    return;
  }
  dom.openFile.click();
}

function widget(target, id) {
  return (cache.plates.get(targetKey(target)) ?? []).find((candidate) => candidate.id === id);
}

function widgetText(item) {
  if (!item) return "";
  if (item.value.type === "Text" || item.value.type === "Choice") return item.value.data;
  if (item.value.type === "Template") {
    return item.value.data.flatMap((segment) => ("Literal" in segment ? [segment.Literal] : [])).join("");
  }
  return "";
}

function templateValue(item, text) {
  const previous = item.value.data ?? [];
  const segments = [];
  let replacedLiteral = false;
  for (const segment of previous) {
    if ("Literal" in segment) {
      if (!replacedLiteral && text) segments.push({ Literal: text });
      replacedLiteral = true;
    } else {
      segments.push(segment);
    }
  }
  if (!replacedLiteral && text) segments.unshift({ Literal: text });
  return tagged("Template", segments);
}

function commitWidget(target, item, value) {
  send("Layout", "CommitWidget", { target, widget_id: item.id, value });
}

function commitVariable(target, item, raw) {
  const separator = raw.indexOf("=");
  const key = (separator >= 0 ? raw.slice(0, separator) : raw).trim();
  const entered = separator >= 0 ? raw.slice(separator + 1).trim() : "";
  commitWidget(target, item, tagged("VariableEdit", { key, value: entered || null }));
}

function variableKeys(target, id) {
  const item = widget(target, id);
  return item?.value.type === "Variables" ? Object.keys(item.value.data) : [];
}

function render() {
  renderWorkspace();
  renderOutline();
  renderProjectPlate();
  renderFolioPlate();
  renderTitleBlock();
  const activeLabel = cache.outline.find(([id]) => id === cache.activeFolioId)?.[1] ?? "No folio";
  dom.windowProjectName.textContent = cache.projectName || "Athena";
  dom.activeFolioName.textContent = activeLabel;
  dom.documentTabName.textContent = activeLabel;
  dom.propertiesFolioName.textContent = activeLabel;
  dom.activeContext.textContent = cache.projectId ? `${cache.outline.length} folios` : "No project";
  dom.saveState.textContent = cache.dirty ? "Modified" : "Clean";
  dom.dirtyMarker.textContent = cache.dirty ? "●" : "";
  dom.tabDirty.textContent = cache.dirty ? "●" : "";
}

function renderWorkspace() {
  const layout = cache.workspace;
  dom.workbench.classList.toggle("outline-collapsed", layout ? !layout.left_panel.open : false);
  dom.workbench.classList.toggle("properties-collapsed", layout ? !layout.right_panel.open : false);
  if (layout) {
    dom.workbench.style.setProperty("--outline-width", `${layout.left_panel.size}px`);
    dom.workbench.style.setProperty("--properties-width", `${layout.right_panel.size}px`);
  }
}

function renderOutline() {
  dom.outline.replaceChildren();
  cache.outline.forEach(([folioId, label], index) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `folio-row${folioId === cache.activeFolioId ? " is-active" : ""}`;
    button.innerHTML = `<span class="folio-index">${String(index + 1).padStart(2, "0")}</span><span>${escapeHtml(label)}</span>`;
    button.addEventListener("click", () => send("Document", "ActivateFolio", { folio_id: folioId }));
    dom.outline.append(button);
  });
  dom.folioCount.textContent = `${cache.outline.length} folio${cache.outline.length === 1 ? "" : "s"}`;
}

function renderProjectPlate() {
  const target = "Project";
  const name = widget(target, "project.name");
  if (name && document.activeElement !== dom.projectName) dom.projectName.value = widgetText(name);
  const variables = widget(target, "project.variables");
  dom.projectVariableRows.replaceChildren();
  if (variables?.value.type === "Variables") {
    for (const [key, value] of Object.entries(variables.value.data)) {
      const row = document.createElement("div");
      row.innerHTML = `<span>${escapeHtml(key)}</span><strong>${escapeHtml(value)}</strong>`;
      dom.projectVariableRows.append(row);
    }
  }
  dom.projectDefaults.replaceChildren();
  const heading = document.createElement("div");
  heading.className = "section-title";
  heading.innerHTML = "<span>Folio defaults</span>";
  dom.projectDefaults.append(heading);
  for (const item of cache.plates.get("project") ?? []) {
    if (["project.name", "project.variables"].includes(item.id)) continue;
    dom.projectDefaults.append(renderWidget(target, item, false));
  }
}

function renderFolioPlate() {
  dom.folioPlate.replaceChildren();
  const target = activeTarget();
  for (const item of cache.plates.get(targetKey(target)) ?? []) {
    dom.folioPlate.append(renderWidget(target, item, true));
  }
}

function renderWidget(target, item, includeReferences) {
  const group = document.createElement("div");
  group.className = "property-row";
  const label = document.createElement("label");
  label.textContent = item.label;
  let control;
  if (item.kind.type === "Select") {
    control = document.createElement("select");
    for (const option of item.kind.data.options) {
      control.append(new Option(option, option, false, option === item.value.data));
    }
    control.addEventListener("change", () => commitWidget(target, item, tagged("Choice", control.value)));
  } else {
    control = document.createElement("input");
    control.type = "text";
    control.autocomplete = "off";
    control.value = item.value.type === "VariableEdit" || item.value.type === "Variables" ? "" : widgetText(item);
    control.placeholder = item.kind.type === "VariableTable" ? "name=value" : "";
    const commit = () => {
      if (item.kind.type === "VariableTable") commitVariable(target, item, control.value);
      else if (item.value.type === "Template") commitWidget(target, item, templateValue(item, control.value));
      else commitWidget(target, item, tagged("Text", control.value));
    };
    control.addEventListener("change", commit);
    control.addEventListener("keydown", (event) => {
      if (event.key === "Enter") {
        event.preventDefault();
        commit();
        if (item.kind.type === "VariableTable") control.value = "";
      }
    });
  }
  control.dataset.widget = item.id;
  label.append(control);
  group.append(label);

  if (item.kind.type === "VariableTable" && item.value.type === "Variables") {
    const rows = document.createElement("div");
    rows.className = "variable-rows property-variables";
    for (const [key, value] of Object.entries(item.value.data)) {
      const row = document.createElement("div");
      row.innerHTML = `<span>${escapeHtml(key)}</span><strong>${escapeHtml(value)}</strong>`;
      rows.append(row);
    }
    group.prepend(rows);
  }

  if (includeReferences && item.value.type === "Template") {
    const pickers = document.createElement("div");
    pickers.className = "reference-pickers";
    const references = [
      ...variableKeys("Project", "project.variables").map((key) => ["Project", key]),
      ...variableKeys(target, "folio.variables").map((key) => ["Folio", key]),
    ];
    for (const [scope, key] of references) {
      const button = document.createElement("button");
      button.type = "button";
      button.textContent = `${scope === "Project" ? "P" : "F"} · ${key}`;
      button.setAttribute("aria-label", `Insert ${key}`);
      button.addEventListener("click", () => {
        const segments = [...item.value.data, { Variable: { [scope]: key } }];
        commitWidget(target, item, tagged("Template", segments));
      });
      pickers.append(button);
    }
    if (pickers.childElementCount) group.append(pickers);
  }
  return group;
}

function renderTitleBlock() {
  const display = cache.resolved.get(cache.activeFolioId);
  const value = (key) => display?.[key]?.text ?? "";
  dom.resolved.innerHTML = `
    <div class="title-main"><span>DRAWING</span><strong>${escapeHtml(value("title") || "Untitled folio")}</strong></div>
    <div><span>Author</span><strong>${escapeHtml(value("author"))}</strong></div>
    <div><span>Plant</span><strong>${escapeHtml(value("plant"))}</strong></div>
    <div><span>Location</span><strong>${escapeHtml(value("location"))}</strong></div>
    <div><span>Revision</span><strong>${escapeHtml(value("revision"))}</strong></div>
    <div class="page-number"><span>Page</span><strong>${escapeHtml(value("page_number"))}</strong></div>`;
}

function setStatus(message) {
  dom.status.textContent = message;
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, (character) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]);
}

function addFolio() {
  send("Document", "AddFolio", { folio_id: crypto.randomUUID(), label: `Folio ${cache.outline.length + 1}` });
}

function moveActive(offset) {
  const index = cache.outline.findIndex(([id]) => id === cache.activeFolioId);
  const to = Math.max(0, Math.min(cache.outline.length - 1, index + offset));
  if (index >= 0 && index !== to) send("Document", "MoveFolio", { folio_id: cache.activeFolioId, to });
}

function bindEvents() {
  document.querySelectorAll("[data-command='add-folio']").forEach((button) => button.addEventListener("click", addFolio));
  document.querySelector("[data-command='move-up']").addEventListener("click", () => moveActive(-1));
  document.querySelector("[data-command='move-down']").addEventListener("click", () => moveActive(1));
  document.querySelector("[data-command='undo']").addEventListener("click", () => send("Document", "Undo"));
  document.querySelector("[data-command='redo']").addEventListener("click", () => send("Document", "Redo"));
  document.querySelector("[data-command='save']").addEventListener("click", () => send("Portfolio", "RequestSave"));
  document.querySelector("[data-command='close']").addEventListener("click", () => send("Portfolio", "CloseProject"));
  document.querySelector("[data-command='open']").addEventListener("click", () => { cache.openMode = "file"; send("Portfolio", "RequestOpen"); });
  document.querySelector("[data-command='reopen']").addEventListener("click", () => { cache.openMode = "recent"; send("Portfolio", "RequestOpen"); });
  document.querySelectorAll("[data-toggle]").forEach((button) => button.addEventListener("click", () => {
    const panel = button.dataset.toggle;
    const state = panel === "ProjectOutline" ? cache.workspace?.left_panel : cache.workspace?.right_panel;
    send("Layout", "SetPanelOpen", { panel_id: panel, open: !state?.open });
  }));
  dom.projectName.addEventListener("change", () => {
    const item = widget("Project", "project.name");
    if (item) commitWidget("Project", item, tagged("Text", dom.projectName.value));
  });
  dom.projectName.addEventListener("keydown", (event) => { if (event.key === "Enter") dom.projectName.blur(); });
  dom.projectVariables.addEventListener("keydown", (event) => {
    if (event.key !== "Enter") return;
    event.preventDefault();
    const item = widget("Project", "project.variables");
    if (item) commitVariable("Project", item, dom.projectVariables.value);
    dom.projectVariables.value = "";
  });
  dom.openFile.addEventListener("change", async () => {
    const file = dom.openFile.files?.[0];
    if (!file) return;
    const bytes = new Uint8Array(await file.arrayBuffer());
    cache.lastSavedBytes = bytes;
    routeEffects(JSON.parse(editor.deliver_open_bytes(bytes)));
    dom.openFile.value = "";
  });
  window.addEventListener("keydown", (event) => {
    if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== "z") return;
    event.preventDefault();
    send("Document", event.shiftKey ? "Redo" : "Undo");
  });
}

async function start() {
  await init();
  editor = new WasmAthenaEditor();
  bindEvents();
  send("Portfolio", "CreateProject", { project_id: crypto.randomUUID(), name: "Main Distribution" });
  send("Layout", "RequestProjectPlate");
  setStatus("Ready");
}

start().catch((error) => {
  setStatus(`Unable to load editor: ${error}`);
  console.error(error);
});
