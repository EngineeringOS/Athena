import init, { WebEditor, built_in_symbol_names } from "./pkg/athena_web_core.js";

const canvas = document.querySelector("#schematic-canvas");
const context = canvas.getContext("2d");
const status = document.querySelector("#status");
const footer = document.querySelector("#footer-status");
let editor;
let currentScene;
const SNAPSHOT_KEY = "athena.electrical.snapshot.v1";

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
      }
    }
  }
  footer.textContent = `${scene.hit_regions.length} interactive regions | Local-first browser editor`;
}

function line(start, end) { context.beginPath(); context.moveTo(start.x, start.y); context.lineTo(end.x, end.y); context.stroke(); }
function polyline(points) { if (points.length < 2) return; context.beginPath(); context.moveTo(points[0].x, points[0].y); for (const point of points.slice(1)) context.lineTo(point.x, point.y); context.stroke(); }

function refresh() {
  currentScene = JSON.parse(editor.render_active_sheet());
  drawScene(currentScene);
}

function canvasPointToWorld(event) {
  const rect = canvas.getBoundingClientRect();
  const viewport = currentScene.viewport;
  const local = { x: event.clientX - rect.left - 24, y: event.clientY - rect.top - 24 };
  return { x: Math.round((local.x - viewport.origin.x) / viewport.zoom), y: Math.round((local.y - viewport.origin.y) / viewport.zoom) };
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
    button.addEventListener("click", () => { editor.begin_placement(name); status.textContent = `Place ${name} on the canvas`; });
    symbolList.append(button);
  }
  canvas.addEventListener("click", (event) => {
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
  document.querySelector("[data-command=wire]").addEventListener("click", () => { editor.begin_wire(); status.textContent = "Click two terminals to connect them"; });
  window.addEventListener("resize", refresh);
  refresh();
  status.textContent = "Ready";
}

start().catch((error) => { status.textContent = `Unable to load editor: ${error}`; console.error(error); });
