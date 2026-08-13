# M002 Corrective Operability Plan

## Goal

Restore trustworthy end-user operability after M002 by closing real shell
regressions before extending feature scope.

## Workflow

- [x] Reproduce the browser placement failure with a red-capable Playwright
  regression.
- [x] Trace the exception to the WASM numeric ABI boundary.
- [x] Change browser-facing coordinate/settings exports from `i64` to `i32`
  and convert into the shared core.
- [x] Rebuild `web/pkg` and run the regression plus existing browser smoke.
- [x] Verify desktop pointer placement and selection in a native shell smoke.
- [x] Inspect and fix the desktop coordinate/event routing defect.
- [x] Record corrective verification evidence.
- [x] Commit and push the corrective pass before starting another milestone.

## Required Commands

From `rust/`:

```powershell
cargo fmt --all -- --check
cargo test -p athena-web-core --test web_editor_contracts
cargo test -p athena-desktop --test desktop_authoring
cargo clippy --workspace --all-targets -- -D warnings
cargo build -p athena-web-core --target wasm32-unknown-unknown --release
wasm-pack build crates/web-core --target web --out-dir ../../../web/pkg --release
```

From the repository root:

```powershell
node --check web/bootstrap.js
npx playwright test web/tests/authoring-mvp.test.js
```
