# Athena Browser Shell

This directory is deliberately a thin browser host. Build the Rust/WASM module
first, then serve this directory over HTTP:

```powershell
wasm-pack build ../rust/crates/web-core --target web --out-dir ../../web/pkg
python -m http.server 8080 --directory .
```

`bootstrap.js` owns DOM events, Canvas 2D pixels, and the browser download
mechanism. The `WebEditor` handle in WASM owns schematic state, commands,
history, serialization, and scene generation.
