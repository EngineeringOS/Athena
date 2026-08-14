# M006 Graphite Shell Reproduction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use
> `superpowers:subagent-driven-development` (recommended) or
> `superpowers:executing-plans` to implement this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Status:** `APPROVED` for implementation on 2026-08-14. Automated, visual,
desktop-runtime, and user-acceptance gates remain open until fresh evidence
proves them.

**Goal:** Replace Athena's invented static workbench with one Rust-owned,
recursive Graphite shell reproduced in Svelte/Web and GPUI/Desktop, populated
only by inert QElectroTech electrical surfaces and an empty folio page.

**Architecture:** `athena-application` owns a serializable recursive shell tree,
shell messages, normalization, and semantic effects independently of document
state. Svelte adapts the licensed Graphite window/panel primitives and GPUI
adapts Zed dock composition patterns; both render the same Rust tree and emit
the same messages without duplicating layout authority.

**Tech Stack:** latest compatible stable Rust; serde and proptest; GPUI and
`gpui-component` for native; `wasm-bindgen`; Svelte 5, Vite 8,
`@sveltejs/vite-plugin-svelte`, `@lucide/svelte`; Playwright.

Run Cargo and `wasm-pack` commands from `rust/`; run npm/Vite/Playwright commands
from the repository root. Stage only files explicitly listed by each task so
the pre-existing M005 work and legacy removals remain independent.

---

### Task 1: Freeze the Source, License, and Dependency Ledger

**Files:**
- Modify: `.gitignore`
- Create: `docs/superpowers/verification/2026-08-14-m006-source-ledger.md`
- Modify: `package.json`
- Modify: `package-lock.json`

**Traceability:** CAP-M006-001/003/004/005; Source Reproduction Gate;
`reference/Graphite/LICENSE.txt`; supplemental OpenCADStudio Rust-library and
native/WASM architecture evidence with all CAD behavior excluded.

- [x] **Step 1: Record exact source ownership before adaptation**

  Create a ledger row for every planned Athena shell file with columns
  `Athena file`, `authority`, `source range`, `status`, `removed dependencies`,
  and `license treatment`. Cite Graphite `MainWindow.svelte:22-35`,
  `PanelSubdivision.svelte:201-292`, `Panel.svelte:387-658`,
  `TitleBar.svelte:35-168`, `StatusBar.svelte:27-91`, and
  `Editor.svelte:89-128,253-270`; cite Zed `workspace.rs:1372-1430,8991-9460`;
  cite the QET RST files named by the spec. Record OpenCADStudio
  `docs/native-vs-web.md:3-12,64-69` as supplemental library/architecture
  comparison evidence only, explicitly excluding its commands, CAD features,
  UI, file formats, 2D/3D product behavior, ribbon, and generic-CAD assumptions.
  Mark its `iced` widgets, subscriptions, pane-grid, renderer, and event loop as
  toolkit-specific exclusions; Zed/GPUI remains the native implementation path.

- [x] **Step 2: Verify current compatible package releases**

  Run:

  ```powershell
  npm view svelte version
  npm view vite version
  npm view @sveltejs/vite-plugin-svelte version
  npm view @lucide/svelte version
  ```

  Expected at execution: `5.56.9`, `8.2.1`, `7.3.0`, and `1.31.0`, or newer
  mutually compatible stable releases documented in the ledger.

- [x] **Step 3: Add only the web presentation dependencies**

  Add scripts `dev`, `build`, `preview`, and `test`; add Svelte, Vite, the
  Svelte Vite plugin, and `@lucide/svelte` as dev dependencies. Do not use the
  deprecated `lucide-svelte` package. Run `npm install` and record the resolved
  versions and licenses.

- [x] **Step 4: Verify the dependency boundary**

  Run `npm ls --depth=0` and
  `cargo metadata --format-version 1 --no-deps`. Expected: exact web
  presentation versions resolve; application/domain crates do not depend on
  Svelte, GPUI, browser, filesystem, or cloud APIs. The first Vite build remains
  Task 5 because that task creates `vite.config.js` and the Svelte entry point.

- [x] **Step 5: Commit the ledger and dependency boundary**

  ```powershell
  git add package.json package-lock.json docs/superpowers/verification/2026-08-14-m006-source-ledger.md
  git commit -m "build: establish m006 shell sources"
  ```

### Task 2: Replace the Fixed Workspace with the Recursive Shell Contract

**Files:**
- Create: `rust/crates/application/src/shell_model.rs`
- Modify: `rust/crates/application/src/lib.rs`
- Create: `rust/crates/application/tests/shell_model_contracts.rs`
- Modify: `rust/crates/application/Cargo.toml`
- Modify: `rust/Cargo.lock`

**Traceability:** CAP-M006-002/004; Graphite recursive subdivision and 80:20
document-priority behavior.

- [x] **Step 1: Write failing default-tree and invariant tests**

  Assert a default `WorkspaceShell` has stable groups/tabs, a horizontal root
  split with shares `20/64/16`, two equal vertical side splits, exactly one
  document group, shares summing to the parent's total, and active tabs present
  in their groups. Add proptests for positive normalized shares and stable tab
  identity after serialization.

- [x] **Step 2: Run the tests and confirm RED**

  Run `cargo test -p athena-application --test shell_model_contracts`.
  Expected: compile failure because `WorkspaceShell`, `ShellNode`, and related
  contracts do not exist.

- [x] **Step 3: Implement the documented public shell vocabulary**

  Define and document:

  ```rust
  pub struct WorkspaceShell {
      pub title_bar: TitleBarState,
      pub root: ShellNode,
      pub status_bar: StatusBarState,
      pub floating_layers: FloatingLayersState,
      pub focus: ShellFocus,
  }
  pub struct TitleBarState { pub document_label: String, pub dirty: bool }
  pub struct StatusBarState { pub hint: String, pub document_info: Vec<String> }
  pub struct FloatingLayersState { pub overlay: Option<GroupId>, pub dock_preview: Option<DockTarget> }
  pub struct SplitId(Uuid);
  pub struct GroupId(Uuid);
  pub struct TabId(Uuid);
  pub enum ShellNode { Split(SplitNode), PanelGroup(PanelGroup) }
  pub struct SplitNode { pub id: SplitId, pub axis: SplitAxis, pub children: Vec<SplitChild> }
  pub struct SplitChild { pub node: Box<ShellNode>, pub share: u32 }
  pub struct PanelGroup { pub id: GroupId, pub tabs: Vec<PanelTab>, pub active_tab: TabId }
  pub struct PanelTab { pub id: TabId, pub role: PanelRole, pub label: String, pub closeable: bool }
  pub enum PanelRole { Project, Folios, Elements, TitleBlocks, FolioDocument, SelectionProperties, FolioProperties, Diagnostics, History }
  pub enum SplitAxis { Horizontal, Vertical }
  pub enum ShellFocus { Workspace, Document }
  pub struct DockTarget { pub group_id: GroupId, pub placement: DockPlacement }
  pub enum DockPlacement { Left, Right, Top, Bottom, Center }
  ```

  Use typed UUID-backed IDs, serde tagging, module-level `//!` documentation,
  public-contract docs, `MIN_PANEL_PX = 100`, and a deterministic default tree.
  Add typed `DockTarget` edge/center values and keep title/status/floating state
  in this contract so platform adapters cannot invent parallel chrome state.
  Leave the existing `layout.rs` property-widget plate implementation unchanged
  in this task; Task 3 moves it intact while deleting only its obsolete fixed
  three-column workspace state.

- [x] **Step 4: Run focused tests and refactor while green**

  Run `cargo test -p athena-application --test shell_model_contracts` and
  `cargo fmt --all -- --check`. Expected: all focused tests pass and formatting
  is clean.

- [x] **Step 5: Commit the recursive model**

  ```powershell
  git add rust/crates/application/Cargo.toml rust/crates/application/src/shell_model.rs rust/crates/application/src/lib.rs rust/crates/application/tests/shell_model_contracts.rs
  git commit -m "feat: model recursive workspace shell"
  ```

### Task 3: Implement Deterministic Shell Transitions and Effects

**Files:**
- Create: `rust/crates/application/src/shell.rs`
- Create: `rust/crates/application/src/plate.rs`
- Delete: `rust/crates/application/src/layout.rs`
- Create: `rust/crates/application/tests/shell_transitions.rs`
- Move: `rust/crates/application/tests/layout_contracts.rs` to `rust/crates/application/tests/plate_contracts.rs`
- Modify: `rust/crates/application/src/message.rs`
- Modify: `rust/crates/application/src/frontend_message.rs`
- Modify: `rust/crates/application/src/dispatcher.rs`
- Modify: `rust/crates/application/src/lib.rs`

**Traceability:** CAP-M006-002/005; full Interaction Contract.

- [x] **Step 1: Write failing transition tests**

  Cover tab activation/reorder/move, edge split, adjacent resize conservation,
  100px minimum clamping, drag abort restoration, 80:20 document reset,
  50:50 non-document reset, close/reopen with original position, split pruning,
  document focus restoration, and narrow overlay open/close. Each test compares
  the complete resulting tree and ordered semantic effects.

- [x] **Step 2: Run the tests and confirm RED**

  Run `cargo test -p athena-application --test shell_transitions`.
  Expected: compile failure because `ShellMessage` and `ShellEffect` are absent.

- [x] **Step 3: Add the shell-only protocol**

  Add `AthenaMessage::Shell(ShellMessage)` and
  `AthenaFrontendMessage::Shell(ShellEffect)`. Define messages for
  `Request`, `ActivateTab`, `ReorderTab`, `MoveTab`, `SplitGroup`,
  `BeginResize`, `ResizeAdjacent`, `CommitResize`, `AbortResize`,
  `ResetAdjacent`, `ClosePanel`, `ReopenPanel`, `SetDocumentFocus`,
  `OpenOverlay`, `CloseOverlay`, and `SetDockPreview`. Define the effect contract
  explicitly:

  ```rust
  pub enum ShellEffect {
      Replaced(WorkspaceShell),
      ValuesChanged {
          active_tabs: Vec<(GroupId, TabId)>,
          shares: Vec<(SplitId, Vec<u32>)>,
          focus: ShellFocus,
          overlay: Option<GroupId>,
      },
      DockPreview(Option<DockTarget>),
  }
  ```

  No shell message accesses or mutates electrical document state.

  Move the existing widget/plate rendering and callback routing from
  `layout.rs` to `plate.rs` without behavior changes. Retain
  `RequestProjectPlate`, `RequestFolioPlate`, and `CommitWidget` as plate
  messages, but remove `WorkspaceLayout`, `PanelState`, `PanelId`,
  `RequestWorkspace`, `SetPanelOpen`, and `WorkspaceLayoutUpdated`. Rename the
  existing test file and preserve every M005 plate assertion.

- [x] **Step 4: Implement normalization and transaction state**

  Make `ShellHandler` the only writer of the tree. Preserve combined adjacent
  shares, use viewport logical pixels only to calculate minimum shares, store
  resize snapshots for abort, prune empty splits, and retain restorable panel
  locations. Reject invalid IDs with a typed diagnostic and no mutation.

- [x] **Step 5: Run the focused and application suites**

  Run `cargo test -p athena-application --test shell_transitions`,
  `cargo test -p athena-application --test plate_contracts`, and
  `cargo test -p athena-application`. Expected: all tests pass.

- [x] **Step 6: Commit the shell state machine**

  ```powershell
  git add rust/crates/application/src/shell.rs rust/crates/application/src/plate.rs rust/crates/application/src/layout.rs rust/crates/application/src/message.rs rust/crates/application/src/frontend_message.rs rust/crates/application/src/dispatcher.rs rust/crates/application/src/lib.rs rust/crates/application/tests/layout_contracts.rs rust/crates/application/tests/plate_contracts.rs rust/crates/application/tests/shell_transitions.rs
  git commit -m "feat: add deterministic shell transitions"
  ```

### Task 4: Prove Native and WASM Shell Protocol Equivalence

**Files:**
- Create: `rust/crates/application/src/shell_fixture.rs`
- Create: `rust/crates/application/tests/shell_fixture.rs`
- Modify: `rust/crates/application/src/lib.rs`
- Modify: `rust/crates/web-core/tests/cross_platform_equivalence.rs`
- Modify: `rust/crates/desktop/tests/desktop_authoring.rs`
- Modify: `rust/crates/desktop/src/app.rs`

**Traceability:** CAP-M006-005; Automated Contract Gate.

- [x] **Step 1: Write the failing cross-adapter fixture assertions**

  Replay one deterministic sequence containing request, activation, reorder,
  move, split, resize, abort, close/reopen, focus, and overlay transitions
  through `AthenaEditor`, `WebEditorCore`, and `DesktopEditor`. Assert identical
  serialized effects and final `WorkspaceShell` hashes.

- [x] **Step 2: Run and confirm RED**

  Run `cargo test -p athena-web-core --test cross_platform_equivalence` and
  `cargo test -p athena-desktop --test desktop_authoring`.
  Expected: failures because adapters do not cache shell effects.

- [x] **Step 3: Add the canonical fixture and adapter caches**

  Export `canonical_m006_shell_messages()`. Add `shell: WorkspaceShell` to
  `DesktopViewState`, reduce every shell effect in order, and expose a read-only
  accessor. Keep WebEditorCore as a protocol pass-through with no DOM state.

- [x] **Step 4: Run equivalence tests**

  Run all three focused test binaries. Expected: identical effect traces and
  final hashes across application, native adapter, and WASM adapter.

- [x] **Step 5: Commit the equivalence proof**

  ```powershell
  git add rust/crates/application/src/shell_fixture.rs rust/crates/application/src/lib.rs rust/crates/application/tests/shell_fixture.rs rust/crates/web-core/tests/cross_platform_equivalence.rs rust/crates/desktop/src/app.rs rust/crates/desktop/tests/desktop_authoring.rs
  git commit -m "test: prove shell protocol equivalence"
  ```

### Task 5: Establish the Svelte/Vite WASM Presentation Boundary

**Files:**
- Create: `vite.config.js`
- Replace: `web/index.html`
- Create: `web/src/main.js`
- Create: `web/src/App.svelte`
- Create: `web/src/lib/athena.js`
- Create: `web/src/lib/shell-store.svelte.js`
- Delete: `web/bootstrap.js`
- Replace: `playwright.config.js`
- Create: `web/tests/shell-boundary.spec.js`

**Traceability:** CAP-M006-001/005; browser is presentation-only.

- [x] **Step 1: Write a failing Playwright boundary test**

  Assert Vite serves one `[data-athena-shell]`, the page imports the generated
  WASM adapter, no legacy `.schematic-preview`, `.wire`, or `.symbol` nodes
  exist, and no JavaScript object contains project/folio/symbol/conductor state.

- [x] **Step 2: Run and confirm RED**

  Run `npm test -- web/tests/shell-boundary.spec.js`.
  Expected: failure because the static shell and fake schematic still exist.

- [x] **Step 3: Build the minimal adapter scaffold**

  Run
  `wasm-pack build crates/web-core --target web --out-dir ../../web/pkg` before
  Vite. Configure Vite root `web/` and output `web/dist/`. `athena.js` owns one
  `WasmAthenaEditor`, serializes typed `Shell` messages, and publishes Rust
  effects. The reactive store reduces only serialized `ShellEffect` values;
  Svelte owns DOM focus, pointer capture, and viewport dimensions only.

- [x] **Step 4: Remove the legacy static shell**

  Replace the old HTML bootstrap and delete `web/bootstrap.js`; remove all fake
  symbols, wires, selections, and editable M005 controls from the visible M006
  shell. Do not delete or weaken the Rust M005 behavior tests.

- [x] **Step 5: Build and run the boundary test**

  Run `wasm-pack build crates/web-core --target web --out-dir ../../web/pkg`,
  `npm run build`, and `npm test -- web/tests/shell-boundary.spec.js`.
  Expected: the WASM package and Vite build succeed and the boundary test passes
  with zero page or console errors.

- [x] **Step 6: Commit the web boundary**

  ```powershell
  git add package.json package-lock.json vite.config.js playwright.config.js web/index.html web/src web/tests/shell-boundary.spec.js web/bootstrap.js
  git commit -m "feat: establish svelte wasm shell boundary"
  ```

### Task 6: Adapt Graphite Window and Recursive Panel Primitives

**Files:**
- Create: `web/src/shell/NOTICE.md`
- Create: `web/src/shell/MainWindow.svelte`
- Create: `web/src/shell/TitleBar.svelte`
- Create: `web/src/shell/StatusBar.svelte`
- Create: `web/src/shell/PanelSubdivision.svelte`
- Create: `web/src/shell/PanelGroup.svelte`
- Create: `web/src/shell/Gutter.svelte`
- Create: `web/src/shell/FloatingLayers.svelte`
- Create: `web/src/shell/shell.css`
- Create: `web/tests/shell-metrics.spec.js`
- Create: `web/tests/shell-interactions.spec.js`

**Traceability:** CAP-M006-001/002/003; Graphite Apache-2.0 adaptation.

- [ ] **Step 1: Write failing metric and interaction tests**

  Assert 28px title/tab bars, 24px status, 4px gutters, 6px panels, 2px
  rectangular controls, Source Sans Pro 14px, neutral `#111`-`#eee` tokens,
  root `20/64/16` bounds, tab activation, keyboard traversal, pointer resize,
  Escape abort, and 500ms double-click reset.

- [ ] **Step 2: Run and confirm RED**

  Run `npm test -- web/tests/shell-metrics.spec.js web/tests/shell-interactions.spec.js`.
  Expected: missing component/metric failures.

- [ ] **Step 3: Adapt the Graphite hierarchy with attribution**

  Put the Apache-2.0 copyright, license reference, and `Modified for Athena`
  notice in every source-derived file and summarize adaptations in `NOTICE.md`.
  Retain only main window, title/status, recursive alternating splits, group
  tabs, gutters, docking ghost/floating-layer ordering, and Graphite tokens.
  Remove Graphite portfolio, document, graph, node, raster/vector, storage,
  dialog product logic, branding, and stores.

- [ ] **Step 4: Wire every interaction to Rust messages**

  Components receive serialized tree values and dispatch typed messages through
  `athena.js`. Pointer capture and focus remain DOM concerns. Use Lucide icons
  with tooltips and visible hover/focus/selected/disabled states; do not use
  text glyphs as icons.

- [ ] **Step 5: Run focused web tests and build**

  Run both focused Playwright files and `npm run build`. Expected: all metric
  and interaction assertions pass with no console/page errors.

- [ ] **Step 6: Commit the Graphite web shell**

  ```powershell
  git add web/src/shell web/tests/shell-metrics.spec.js web/tests/shell-interactions.spec.js docs/superpowers/verification/2026-08-14-m006-source-ledger.md
  git commit -m "feat: adapt graphite web shell"
  ```

### Task 7: Populate Inert Electrical Panels and the Empty Folio

**Files:**
- Create: `web/src/electrical/PanelContent.svelte`
- Create: `web/src/electrical/ToolShelf.svelte`
- Create: `web/src/electrical/ToolOptions.svelte`
- Create: `web/src/electrical/EmptyFolio.svelte`
- Create: `web/tests/electrical-surfaces.spec.js`

**Traceability:** CAP-M006-004; QET panel and toolbar taxonomy.

- [ ] **Step 1: Write failing electrical-surface tests**

  Assert all nine panel roles and exact default tab grouping; assert the page
  contains only border, grid, and title-block frame; assert all electrical tool
  commands are disabled with accessible labels; reject `.wire`, `.symbol`,
  `.selection`, editable inputs, fake project data, and fake diagnostics.

- [ ] **Step 2: Run and confirm RED**

  Run `npm test -- web/tests/electrical-surfaces.spec.js`.
  Expected: missing electrical surface failures.

- [ ] **Step 3: Add QET-evidenced inert content**

  Render compact empty-state structures for Project/Folios, Elements/Title
  Blocks, Selection/Folio Properties, Diagnostics/History. Add disabled Lucide
  toolbar controls for selection, conductor, element, annotation, grid, zoom,
  and navigation; render status hints and document/zoom/grid information.

- [ ] **Step 4: Render the empty folio without demonstration content**

  Use a stable page aspect ratio, restrained page border, deterministic grid,
  and an unfilled title-block frame. The canvas remains the dominant unframed
  center surface and does not become a decorative card.

- [ ] **Step 5: Run focused and zero-error tests**

  Run the electrical surface test and the complete Playwright suite. Expected:
  all pass with zero page errors, console errors, overlaps, or clipped labels.

- [ ] **Step 6: Commit inert electrical content**

  ```powershell
  git add web/src/electrical web/tests/electrical-surfaces.spec.js
  git commit -m "feat: add inert electrical shell surfaces"
  ```

### Task 8: Implement Narrow-Viewport Overlay Panels

**Files:**
- Modify: `web/src/shell/MainWindow.svelte`
- Modify: `web/src/shell/FloatingLayers.svelte`
- Modify: `web/src/shell/shell.css`
- Create: `web/tests/narrow-shell.spec.js`

**Traceability:** CAP-M006-006; narrow viewport contract.

- [ ] **Step 1: Write failing 390x844 overlay tests**

  Assert the document remains visible; explicit Project, Library, and
  Properties buttons open Rust-selected overlay groups; Escape/backdrop closes;
  trigger focus returns; keyboard and touch activation work; status/tool rows
  do not overlap; the folio fits or scrolls coherently.

- [ ] **Step 2: Run and confirm RED**

  Run `npm test -- web/tests/narrow-shell.spec.js --project=chromium`.
  Expected: overlay controls are absent.

- [ ] **Step 3: Implement overlay presentation over the same tree**

  Add the breakpoint shell controls and render the selected existing
  `PanelGroup` in `FloatingLayers`; do not construct a second mobile layout
  tree. Dispatch `OpenOverlay`/`CloseOverlay` to Rust and keep focus-return and
  pointer/touch capture in Svelte.

- [ ] **Step 4: Run narrow and desktop regressions**

  Run narrow, metric, and interaction Playwright tests. Expected: all pass at
  390x844 and 1440x900 with no unreachable groups or geometry regressions.

- [ ] **Step 5: Commit narrow viewport support**

  ```powershell
  git add web/src/shell/MainWindow.svelte web/src/shell/FloatingLayers.svelte web/src/shell/shell.css web/tests/narrow-shell.spec.js
  git commit -m "feat: add narrow shell overlays"
  ```

### Task 9: Decompose and Reproduce the Native GPUI Shell

**Files:**
- Replace: `rust/crates/desktop/src/panels.rs`
- Create: `rust/crates/desktop/src/shell/mod.rs`
- Create: `rust/crates/desktop/src/shell/tokens.rs`
- Create: `rust/crates/desktop/src/shell/title_bar.rs`
- Create: `rust/crates/desktop/src/shell/status_bar.rs`
- Create: `rust/crates/desktop/src/shell/subdivision.rs`
- Create: `rust/crates/desktop/src/shell/panel_group.rs`
- Create: `rust/crates/desktop/src/shell/electrical.rs`
- Modify: `rust/crates/desktop/src/lib.rs`
- Create: `rust/crates/desktop/tests/gpui_shell.rs`

**Traceability:** CAP-M006-001/002/003/004/005; Zed native workspace evidence.

- [ ] **Step 1: Write failing GPUI rendered-control tests**

  Assert the render tree exposes title/workspace/status ordering, recursive
  groups, default tabs, Graphite dimensions/tokens, disabled electrical tools,
  empty folio, tab activation, resize/abort/reset, focus mode, close/reopen,
  and no hard-coded schematic preview.

- [ ] **Step 2: Run and confirm RED**

  Run `cargo test -p athena-desktop --test gpui_shell -- --test-threads=1`.
  Expected: compile failure because focused shell modules do not exist.

- [ ] **Step 3: Define native tokens and focused render modules**

  Centralize the exact Graphite measurements/ramp in `tokens.rs`. Follow Zed's
  GPUI workspace composition for title, left/right/bottom-capable docks, center,
  resize handles, overlays, focus, and status. Each module consumes
  `DesktopViewState.shell()` and emits `ShellMessage`; no GPUI-owned alternate
  layout model is permitted.

- [ ] **Step 4: Render QET inert content with gpui-component controls**

  Use gpui-component/Lucide-equivalent icons and tooltips. Keep electrical
  commands disabled, render only border/grid/title-block frame in the folio,
  and remove all native demonstration symbols/wires and embedded layout colors.

- [ ] **Step 5: Run native tests and build the binary**

  Run `cargo test -p athena-desktop -- --test-threads=1` and
  `cargo build -p athena-desktop`. Expected: tests pass and the desktop binary
  builds without warnings.

- [ ] **Step 6: Commit the native shell**

  ```powershell
  git add rust/crates/desktop/src/lib.rs rust/crates/desktop/src/panels.rs rust/crates/desktop/src/shell rust/crates/desktop/tests/gpui_shell.rs
  git commit -m "feat: reproduce graphite shell in gpui"
  ```

### Task 10: Capture Visual Evidence and Close Automated Gates

**Files:**
- Create: `docs/superpowers/verification/2026-08-14-m006-graphite-shell-reproduction.md`
- Create: `docs/superpowers/verification/assets/m006/graphite-1440x900.png`
- Create: `docs/superpowers/verification/assets/m006/athena-web-1440x900.png`
- Create: `docs/superpowers/verification/assets/m006/athena-web-390x844.png`
- Create: `docs/superpowers/verification/assets/m006/athena-desktop-1440x900.png`
- Modify: `docs/superpowers/plans/2026-08-14-m006-graphite-shell-reproduction.md`

**Traceability:** all M006 capabilities; Source, Automated, Visual, and User
Gates.

- [ ] **Step 1: Run fresh repository verification**

  Run:

  ```powershell
  cargo fmt --all -- --check
  cargo clippy --workspace --all-targets -- -D warnings
  cargo test --workspace -- --test-threads=1
  cargo check --workspace --target wasm32-unknown-unknown
  wasm-pack test --headless --chrome crates/web-core
  npm run build
  npm test
  ```

  Record command, timestamp, tool versions, exit code, and test counts. Record
  any intentionally skipped command as `SKIPPED` with the exact blocking cause;
  never convert a skip into a pass.

- [ ] **Step 2: Capture the three Athena target screenshots**

  Start Vite on an available loopback port and launch the GPUI binary. Capture
  Athena web at 1440x900 and 390x844 and desktop at 1440x900. Confirm each image
  is nonblank and record pixel bounds for title/status/tab/gutter/panel regions,
  root shares, text clipping, overlaps, and disabled states.

- [ ] **Step 3: Capture and compare the Graphite reference**

  Run the local Graphite checkout when its documented prerequisites succeed and
  capture 1440x900. If it cannot run, mark the image `SKIPPED` with the command
  and full cause, then compare Athena against the cited source measurements and
  the user-provided AVIF without claiming the visual gate passed. Produce
  side-by-side and overlay observations in the verification report.

- [ ] **Step 4: Audit the source ledger and plan line by line**

  Verify every Athena shell file has an evidence row and license treatment;
  verify every capability and interaction has a passing test or explicit open
  gate; verify no fake schematic content or duplicate layout authority remains.
  Update checkboxes only where repository state and recorded evidence prove the
  step.

- [ ] **Step 5: Present running targets for the user gate**

  Keep the web and desktop processes running when stable, report the exact web
  URL and desktop launch command, and request explicit visual acceptance. Until
  that response, record `User Gate: OPEN` and do not call the shell reproduced,
  professional, mirrored, or complete.

- [ ] **Step 6: Commit and push verified M006 evidence**

  After all automated gates pass and the report accurately identifies the open
  user gate:

  ```powershell
  git add docs/superpowers/plans/2026-08-14-m006-graphite-shell-reproduction.md docs/superpowers/verification/2026-08-14-m006-graphite-shell-reproduction.md docs/superpowers/verification/assets/m006
  git commit -m "docs: verify m006 graphite shell"
  git push origin next-001
  ```
