# Athena Browser Shell

The browser is a thin platform adapter over the Rust application protocol.
Project, folio, title-block, variable, validation, history, panel, and dirty
state are owned by `athena-application` inside WASM. JavaScript keeps only DOM
references, effect-derived render caches, file bytes, and browser focus state.

Build and run from the repository root:

```powershell
Push-Location rust
wasm-pack build crates/web-core --target web --out-dir ../../../web/pkg
Pop-Location
python -m http.server 8080 --directory web
```

The shell sends tagged `AthenaMessage` JSON and reduces only tagged
`AthenaFrontendMessage` effects. Browser save/open adapters return bytes and
typed outcomes to the same persistent `WasmAthenaEditor` instance.
