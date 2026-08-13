# M003 Professional Workbench Verification

Date: 2026-08-13
Branch: `next-001`

## Implemented

- Graphite-inspired application/menu bar, contextual tool shelf, document
  viewport frame, dockable library/properties roles, and status region.
- Browser library filtering, presentation-only rail collapse, active Wire/
  Select state, and Escape cancellation.
- Native GPUI shell hierarchy aligned to the same roles, with a native
  `cancel_active_tool` adapter that does not mutate the document.
- AGENTS and M003 design documents now state that Graphite/Zed are the shell
  architecture baseline and QElectroTech is the electrical behavior inventory.

## Fresh Verification

- `cargo fmt --all -- --check`: passed.
- `cargo test -p athena-desktop --test desktop_authoring`: 8 passed.
- `cargo test -p athena-web-core --test web_editor_contracts`: 6 passed.
- `cargo clippy --workspace --all-targets -- -D warnings`: passed.
- `cargo build -p athena-web-core --target wasm32-unknown-unknown --release`: passed.
- `wasm-pack build crates/web-core --target web --out-dir ../../../web/pkg --release`: passed.
- `cargo build -p athena-desktop`: passed.
- `node --check web/bootstrap.js`: passed.
- `npx playwright test web/tests/authoring-mvp.test.js`: 3 passed. Playwright
  now owns the static server through `playwright.config.js`, making this
  browser proof reproducible without an externally managed server process.
