import init, { WebEditor, built_in_symbol_names } from "./pkg/athena_web_core.js";

const canvas = document.querySelector("#schematic-canvas");
const context = canvas.getContext("2d");
const status = document.querySelector("#status");
const footer = document.querySelector("#footer-status");
let editor;
let currentScene;
let canvasGestureActive = false;
let ignoreNextCanvasClick = false;
let syncingInspector = false;
let activeTool = "select";
const SNAPSHOT_KEY = "athena.electrical.snapshot.v1";
const inspector = {
  reference: document.querySelector("#symbol-reference"),
  description: document.querySelector("#symbol-description"),
  wireLabel: document.querySelector("#wire-label"),
  sheetName: document.querySelector("#sheet-name"),
  gridVisible: document.querySelector("#grid-visible"),
  gridSpacing: document.querySelector("#grid-spacing"),
};

function fitCanvas() {
  const rect = canvas.getBoundingClientRect();
  const scale = window.devicePixelRatio || 1;
  canvas.width = Math.max(1, Math.round(rect.width * scale));
  canvas.height = Math.max(1, Math.round(rect.height * scale));
  context.setTransform(scale, 0, 0, scale, 0, 0);
}

function drawScene(scene) {
  fitCanvas();
  const width = canvas.clientWidth;
  const height = canvas.clientHeight;
  context.clearRect(0, 0, width, height);
  context.fillStyle = "#f8fafc";
  context.fillRect(0, 0, width, height);
  const origin = { x: 24, y: 24 };
  const viewport = scene.viewport;
  const toCanvas = (point) => ({ x: origin.x + viewport.origin.x + point.x * viewport.zoom, y: origin.y + viewport.origin.y + point.y * viewport.zoom });
  for (const layer of scene.layers) {
    for (const primitive of layer.primitives) {
      if (primitive.Page) {
        const { min, max } = primitive.Page.bounds;
        const topLeft = toCanvas(min); const bottomRight = toCanvas(max);
        context.fillStyle = "#ffffff";
        context.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
      } else if (primitive.Grid) {
        const { bounds, spacing } = primitive.Grid;
        context.strokeStyle = "#e2e8f0"; context.lineWidth = 1;
        for (let x = bounds.min.x; x <= bounds.max.x; x += spacing) line(toCanvas({ x, y: bounds.min.y }), toCanvas({ x, y: bounds.max.y }));
        for (let y = bounds.min.y; y <= bounds.max.y; y += spacing) line(toCanvas({ x: bounds.min.x, y }), toCanvas({ x: bounds.max.x, y }));
      } else if (primitive.Polyline) {
        context.strokeStyle = "#334155"; context.lineWidth = 1.5; polyline(primitive.Polyline.points.map(toCanvas));
      } else if (primitive.SymbolBody) {
        const { min, max } = primitive.SymbolBody.bounds;
        const topLeft = toCanvas(min); const bottomRight = toCanvas(max);
        context.fillStyle = "#94a3b8";
        context.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
      } else if (primitive.ConnectionHandle) {
        const handle = primitive.ConnectionHandle; const point = toCanvas(handle.position);
        context.fillStyle = "#0ea5e9"; context.beginPath(); context.arc(point.x, point.y, handle.radius * viewport.zoom, 0, Math.PI * 2); context.fill();
      } else if (primitive.Circle) {
        const circle = primitive.Circle; const point = toCanvas(circle.center);
        context.fillStyle = "#0f172a"; context.beginPath(); context.arc(point.x, point.y, circle.radius * viewport.zoom, 0, Math.PI * 2); context.fill();
      } else if (primitive.Overlay) {
        drawOverlay(primitive.Overlay.overlay, toCanvas, viewport);
      }
    }
  }
  document.querySelector("#canvas-interactive-status").textContent = `${scene.hit_regions.length} interactive regions`;
  const state = JSON.parse(editor.inspector_state_json());
  document.querySelector("#selection-status").textContent = state.symbol_reference || state.wire_label ? "1 selected" : "0 selected";
  document.querySelector("#canvas-grid-state").textContent = state.grid_visible ? "On" : "Off";
}

function line(start, end) { context.beginPath(); context.moveTo(start.x, start.y); context.lineTo(end.x, end.y); context.stroke(); }
function polyline(points) { if (points.length < 2) return; context.beginPath(); context.moveTo(points[0].x, points[0].y); for (const point of points.slice(1)) context.lineTo(point.x, point.y); context.stroke(); }

function drawOverlay(overlay, toCanvas, viewport) {
  if (overlay.SelectionBounds) {
    const { min, max } = overlay.SelectionBounds.bounds;
    const start = toCanvas(min); const end = toCanvas(max);
    context.strokeStyle = "#2563eb"; context.lineWidth = 1.5;
    context.strokeRect(start.x, start.y, end.x - start.x, end.y - start.y);
  } else if (overlay.WirePathHighlight) {
    context.strokeStyle = "#0ea5e9"; context.lineWidth = 3;
    polyline(overlay.WirePathHighlight.points.map(toCanvas));
  } else if (overlay.WireVertexHandle || overlay.WireEndpointHandle || overlay.TransformHandle) {
    const handle = overlay.WireVertexHandle || overlay.WireEndpointHandle || overlay.TransformHandle;
    const point = toCanvas(handle.position); const radius = handle.radius * viewport.zoom;
    context.fillStyle = "#0ea5e9"; context.fillRect(point.x - radius, point.y - radius, radius * 2, radius * 2);
  } else if (overlay.MarqueeRect) {
    const start = toCanvas(overlay.MarqueeRect.start); const end = toCanvas(overlay.MarqueeRect.end);
    context.fillStyle = "rgba(147, 197, 253, 0.33)";
    context.fillRect(start.x, start.y, end.x - start.x, end.y - start.y);
    context.strokeStyle = "#2563eb"; context.lineWidth = 1;
    context.strokeRect(start.x, start.y, end.x - start.x, end.y - start.y);
  } else if (overlay.GuideLine) {
    context.strokeStyle = "#f97316"; context.lineWidth = 1;
    line(toCanvas(overlay.GuideLine.start), toCanvas(overlay.GuideLine.end));
  }
}

function refresh() {
  currentScene = JSON.parse(editor.render_active_sheet());
  drawScene(currentScene);
  syncInspector();
}

function setActiveTool(tool) {
  activeTool = tool;
  document.querySelectorAll("[data-tool]").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.tool === tool);
  });
}

function toggleRail(name) {
  const className = `${name}-collapsed`;
  const collapsed = document.querySelector(".workbench").classList.toggle(className);
  const button = document.querySelector(`[data-toggle-rail='${name}']`);
  button.setAttribute("aria-expanded", String(!collapsed));
  button.title = `${collapsed ? "Expand" : "Collapse"} ${name} rail`;
  refresh();
}

function canvasPointToWorld(event) {
  const rect = canvas.getBoundingClientRect();
  const viewport = currentScene.viewport;
  const local = { x: event.clientX - rect.left - 24, y: event.clientY - rect.top - 24 };
  return { x: Math.round((local.x - viewport.origin.x) / viewport.zoom), y: Math.round((local.y - viewport.origin.y) / viewport.zoom) };
}

function eventModifiers(event) {
  return { shift: event.shiftKey, command: event.ctrlKey || event.metaKey };
}

function syncInspector() {
  if (!editor) return;
  const state = JSON.parse(editor.inspector_state_json());
  syncingInspector = true;
  inspector.reference.value = state.symbol_reference ?? "";
  inspector.description.value = state.symbol_description ?? "";
  inspector.wireLabel.value = state.wire_label ?? "";
  inspector.sheetName.value = state.sheet_name;
  inspector.gridVisible.checked = state.grid_visible;
  inspector.gridSpacing.value = String(state.grid_spacing);
  syncingInspector = false;
}

function saveLocal() {
  localStorage.setItem(SNAPSHOT_KEY, JSON.stringify(Array.from(editor.encode_snapshot())));
  status.textContent = "Saved locally";
}

function reloadLocal() {
  const encoded = localStorage.getItem(SNAPSHOT_KEY);
  if (!encoded) { status.textContent = "No local project saved"; return; }
  editor.load_snapshot(new Uint8Array(JSON.parse(encoded)));
  status.textContent = "Reloaded local project";
  refresh();
}

function exportSnapshot() {
  const bytes = editor.encode_snapshot();
  const url = URL.createObjectURL(new Blob([bytes], { type: "application/json" }));
  const anchor = document.createElement("a");
  anchor.href = url; anchor.download = "athena-electrical-project.json"; anchor.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

async function start() {
  await init();
  editor = new WebEditor("Browser electrical project");
  const names = JSON.parse(built_in_symbol_names());
  const symbolList = document.querySelector("#symbol-list");
  for (const { name } of names) {
    const button = document.createElement("button");
    button.textContent = name;
    button.dataset.symbolName = name.toLowerCase();
    button.addEventListener("click", () => { editor.begin_placement(name); status.textContent = `Place ${name} on the canvas`; });
    symbolList.append(button);
  }
  document.querySelector("#symbol-search").addEventListener("input", (event) => {
    const query = event.target.value.trim().toLowerCase();
    let visible = 0;
    symbolList.querySelectorAll("button").forEach((button) => {
      const matches = !query || button.dataset.symbolName.includes(query);
      button.hidden = !matches;
      if (matches) visible += 1;
    });
    document.querySelector("#library-count").textContent = query ? `${visible} matches` : "Built-in catalog";
  });
  document.querySelectorAll("[data-toggle-rail]").forEach((button) => button.addEventListener("click", () => toggleRail(button.dataset.toggleRail)));
  canvas.addEventListener("pointerdown", (event) => {
    const point = canvasPointToWorld(event);
    const modifiers = eventModifiers(event);
    if (event.button !== 0) return;
    canvasGestureActive = editor.pointer_down(point.x, point.y, modifiers.shift, modifiers.command);
    if (canvasGestureActive) {
      canvas.setPointerCapture(event.pointerId);
      status.textContent = "Shared Rust editor gesture started";
      refresh();
    }
  });
  canvas.addEventListener("pointermove", (event) => {
    if (!canvasGestureActive) return;
    const point = canvasPointToWorld(event); const modifiers = eventModifiers(event);
    editor.pointer_move(point.x, point.y, modifiers.shift, modifiers.command);
    refresh();
  });
  canvas.addEventListener("pointerup", (event) => {
    if (!canvasGestureActive) return;
    const point = canvasPointToWorld(event); const modifiers = eventModifiers(event);
    editor.pointer_up(point.x, point.y, modifiers.shift, modifiers.command);
    canvasGestureActive = false;
    ignoreNextCanvasClick = true;
    canvas.releasePointerCapture(event.pointerId);
    status.textContent = "Shared Rust editor updated";
    refresh();
  });
  canvas.addEventListener("click", (event) => {
    if (canvasGestureActive || ignoreNextCanvasClick) { ignoreNextCanvasClick = false; return; }
    const point = canvasPointToWorld(event);
    editor.pointer_click(point.x, point.y);
    status.textContent = "Shared Rust editor updated";
    refresh();
  });
  document.querySelector("[data-command=undo]").addEventListener("click", () => { editor.undo(); refresh(); });
  document.querySelector("[data-command=redo]").addEventListener("click", () => { editor.redo(); refresh(); });
  document.querySelector("[data-command=export]").addEventListener("click", exportSnapshot);
  document.querySelector("[data-command=save]").addEventListener("click", saveLocal);
  document.querySelector("[data-command=reload]").addEventListener("click", reloadLocal);
  document.querySelector("[data-command=select]").addEventListener("click", () => { editor.cancel_active_tool(); setActiveTool("select"); status.textContent = "Select symbols, wires, or drag a marquee"; refresh(); });
  document.querySelector("[data-command=wire]").addEventListener("click", () => { editor.begin_wire(); setActiveTool("wire"); status.textContent = "Click two terminals to connect them"; });
  document.querySelector("[data-command=rotate]").addEventListener("click", () => { editor.rotate_selection_90(); refresh(); });
  document.querySelector("[data-command=mirror]").addEventListener("click", () => { editor.mirror_selection(); refresh(); });
  document.querySelector("[data-command=delete]").addEventListener("click", () => { editor.delete_selection(); refresh(); });
  inspector.reference.addEventListener("change", () => {
    if (syncingInspector) return;
    editor.update_selected_symbol_reference(inspector.reference.value); refresh();
  });
  inspector.description.addEventListener("change", () => {
    if (syncingInspector) return;
    editor.update_selected_symbol_description(inspector.description.value); refresh();
  });
  inspector.wireLabel.addEventListener("change", () => {
    if (syncingInspector) return;
    editor.update_selected_wire_label(inspector.wireLabel.value); refresh();
  });
  inspector.sheetName.addEventListener("change", () => {
    if (syncingInspector) return;
    editor.update_sheet_name(inspector.sheetName.value); refresh();
  });
  function updateGrid() {
    if (syncingInspector) return;
    const spacing = Number.parseInt(inspector.gridSpacing.value, 10);
    if (Number.isInteger(spacing) && spacing > 0) {
      editor.update_sheet_grid(inspector.gridVisible.checked, spacing); refresh();
    }
  }
  inspector.gridVisible.addEventListener("change", updateGrid);
  inspector.gridSpacing.addEventListener("change", updateGrid);
  window.addEventListener("keydown", (event) => {
    const secondary = event.ctrlKey || event.metaKey;
    if (secondary && event.key.toLowerCase() === "z") {
      event.preventDefault();
      if (event.shiftKey) editor.redo(); else editor.undo();
      refresh();
    } else if (event.key === "Delete" || event.key === "Backspace") {
      if (event.target instanceof HTMLInputElement) return;
      event.preventDefault(); editor.delete_selection(); refresh();
    } else if (event.key.toLowerCase() === "r") {
      editor.rotate_selection_90(); refresh();
    } else if (event.key.toLowerCase() === "m") {
      editor.mirror_selection(); refresh();
    } else if (event.key === "Escape") {
      canvasGestureActive = false; editor.cancel_active_tool(); setActiveTool("select"); status.textContent = "Gesture cancelled"; refresh();
    }
  });
  window.addEventListener("resize", refresh);
  refresh();
  status.textContent = "Ready";
}

start().catch((error) => { status.textContent = `Unable to load editor: ${error}`; console.error(error); });
