# Authoring MVP Verification

## Toolchain

- Rust `rustc 1.97.1`, `cargo 1.97.1`
- `wasm-pack 0.15.0`
- Target `wasm32-unknown-unknown`
- Node `v24.15.0`

## Commands

From `rust/`:

```powershell
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
cargo test -p athena-web-core --test web_editor_contracts
cargo build -p athena-web-core --target wasm32-unknown-unknown --release
wasm-pack build crates/web-core --target web --out-dir ../../web/pkg --release
```

The focused editor, render, library, desktop, and web-core checks pass. The
release WASM build and `wasm-pack` packaging pass. `web/bootstrap.js` passes
`node --check`.

## Browser host

The shell is served with:

```powershell
python -m http.server 8080 --directory web
```

`http://127.0.0.1:8080/` returns HTTP 200 and the generated package is under
`web/pkg/`. The Playwright authoring flow is recorded in
`web/tests/authoring-mvp.test.js`; no Playwright or Puppeteer runner is
installed in this environment, so its execution and pixel assertions remain a
manual follow-up.

The browser host keeps local snapshots in `localStorage`; Rust/WASM validates
and decodes the bytes before replacing the editor state. `web/pkg/` is build
output and is deliberately ignored, so a fresh checkout must run `wasm-pack`.

## Known warnings

- `proc-macro-error2 v2.0.1` reports a pre-existing future-incompatibility notice.
- Windows linker emits informational `linker_messages` output for GPUI/WASM
  integration tests.
