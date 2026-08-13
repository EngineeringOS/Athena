# M002 Corrective Operability Verification

Date: 2026-08-13
Branch: `next-001`

## Evidence

- `cargo fmt --all -- --check` passed from `rust/`.
- `cargo test -p athena-desktop --test desktop_authoring` passed: 8 tests.
- `cargo test -p athena-web-core --test web_editor_contracts` passed: 6 tests.
- `cargo clippy --workspace --all-targets -- -D warnings` passed.
- `cargo build -p athena-web-core --target wasm32-unknown-unknown --release` passed.
- `wasm-pack build crates/web-core --target web --out-dir ../../../web/pkg --release` passed.
- `node --check web/bootstrap.js` passed.
- `npx playwright test web/tests/authoring-mvp.test.js` passed: 2 tests.
- `athena-desktop.exe` launched as a native process and remained alive for a five-second smoke window.

## Corrective Change

The native shell previously converted window-relative pointer coordinates using
offsets that included layout padding which was not part of the rendered canvas.
The canvas host now has no extra padding, and the adapter accounts only for the
fixed toolbar/library chrome, the host border, and the renderer's documented
24px scene inset. This keeps placement, selection, marquee, and drag gestures
aligned with the visible schematic.

## Scope Note

The native process-liveness smoke confirms the GPUI shell starts successfully;
the deterministic desktop authoring tests provide the pointer command-path
coverage. Full OS-level click automation is not available in this environment.
