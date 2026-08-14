import init, { WasmAthenaEditor } from "../../pkg/athena_web_core.js";

let editor;
let publishEffects;

function tagged(type, data) {
  return data === undefined ? { type } : { type, data };
}

/** Starts the persistent Rust editor and requests its canonical shell. */
export async function startAthena(publish) {
  await init();
  editor = new WasmAthenaEditor();
  publishEffects = publish;
  dispatchShell("Request");
}

/** Sends one shell-only message across the tagged WASM protocol boundary. */
export function dispatchShell(type, data) {
  if (!editor || !publishEffects) throw new Error("Athena editor is not ready");
  const message = tagged("Shell", tagged(type, data));
  publishEffects(JSON.parse(editor.dispatch(JSON.stringify(message))));
}
