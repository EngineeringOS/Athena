# M003 Professional Workbench Implementation Plan

> **For agentic workers:** Execute this plan task by task and keep every checkbox aligned with repository evidence.

**Goal:** Deliver a Graphite-inspired professional electrical-schematic shell on desktop and web over the existing shared Rust editor session.

**Architecture:** Keep the Rust domain/editor/render contracts unchanged. Mirror Graphite's separation between document state, tools/messages, viewport/canvas, overlays, panels, and platform wrappers at the shell boundary. Improve only shell presentation, platform event adapters, and read-only presentation state. The web shell uses semantic HTML/CSS and small JavaScript adapters; the desktop shell uses GPUI layout and `gpui-component` controls with matching roles and command identities.

**Tech Stack:** Rust, GPUI, `gpui-component`, WASM, HTML, CSS, JavaScript, Playwright.

---

### Task 1: Establish Graphite-inspired browser shell structure and visual tokens

**Files:**
- Modify: `web/index.html`
- Modify: `web/styles.css`

- [x] Replace the single topbar with app/menu bar, contextual tool shelf, dockable-panel headers, canvas viewport frame, inspector sections, and status/message bar while preserving existing IDs used by the WASM adapter.
- [x] Add stable `data-region` attributes for `app-bar`, `tool-bar`, `library-rail`, `canvas-stage`, `inspector-rail`, and `status-bar`.
- [x] Add Graphite-inspired visual tokens: neutral surfaces, one accent color, clear separators, compact tool shelf spacing, selected/active/disabled states, and dock/panel collapse classes.
- [x] Keep the canvas full-size inside a framed stage and move zoom/coordinate readouts outside the bitmap surface.

### Task 2: Add browser shell presentation behavior

**Files:**
- Modify: `web/bootstrap.js`

- [x] Add library search filtering without duplicating catalog state; filter only rendered buttons.
- [x] Add rail collapse buttons that toggle CSS classes and update `aria-expanded` without calling Rust mutations.
- [x] Add active tool presentation for wire and placement commands, and clear the active state when Escape cancels the tool.
- [x] Keep save/reload/export/history and inspector event routing on the existing `WebEditor` methods.

### Task 3: Add browser layout regression coverage

**Files:**
- Modify: `web/tests/authoring-mvp.test.js`

- [x] Assert the five regions are visible and the canvas has a non-zero stable size.
- [x] Assert library search reduces visible symbol buttons and restores them when cleared.
- [x] Assert rail collapse changes presentation only and leaves the editor status intact.
- [x] Assert Wire and placement controls expose active state and Escape clears it.
- [x] Run the full browser authoring suite with a local static server.

### Task 4: Align the native GPUI shell hierarchy with Graphite roles

**Files:**
- Modify: `rust/crates/desktop/src/panels.rs`

- [x] Restructure the native view into app/menu bar, contextual tool shelf, dockable library/properties panels, framed document viewport, and status/message bar.
- [x] Preserve all existing editor command callbacks and inspector bindings while giving controls stable IDs and active styling.
- [ ] Add native library search state and presentation-only rail collapse state without modifying `DesktopEditor` or the shared core.
- [x] Keep pointer coordinates derived from the actual canvas layout contract and document any remaining renderer inset.

### Task 5: Verify, document, and publish M003

**Files:**
- Create: `docs/superpowers/verification/2026-08-13-m003-professional-workbench.md`
- Modify: `docs/superpowers/plans/2026-08-13-m003-professional-workbench.md`

- [x] Run `cargo fmt --all -- --check` from `rust/`.
- [x] Run `cargo test -p athena-desktop --test desktop_authoring`.
- [x] Run `cargo test -p athena-web-core --test web_editor_contracts`.
- [x] Run `cargo clippy --workspace --all-targets -- -D warnings`.
- [x] Run `cargo build -p athena-web-core --target wasm32-unknown-unknown --release` and `wasm-pack build crates/web-core --target web --out-dir ../../../web/pkg --release`.
- [x] Run `node --check web/bootstrap.js` and `npx playwright test web/tests/authoring-mvp.test.js` from the repository root with a stable server process.
- [x] Launch `rust/target/debug/athena-desktop.exe` for a native process smoke and record the result.
- [x] Mark only proven plan items complete, commit the M003 slice, and push `next-001`.
