# M005 Application Spine and Folio Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use
> `superpowers:subagent-driven-development` or
> `superpowers:executing-plans` to execute this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Status:** `APPROVED` for implementation on 2026-08-14. Runtime, visual, and
user-acceptance gates remain open until fresh M005 evidence proves them.

**Goal:** Deliver project creation, ordered folios, inherited title-block
variables, and save/reopen through a Graphite-derived typed application spine
shared by GPUI desktop and WASM browser adapters.

**Architecture:** Add `athena-application` above the existing platform-neutral
domain/format/editor crates. `AthenaEditor` accepts `AthenaMessage`, a
deterministic dispatcher delegates to portfolio/document/layout handlers, and
returns `AthenaFrontendMessage`. GPUI and WASM adapt events/effects only.

**Tech Stack:** latest stable Rust at execution time; serde/serde_json/uuid;
GPUI and `gpui-component` for desktop; `wasm-bindgen` and minimal HTML/JS for
web; Cargo tests, wasm checks, and Playwright workflow/screenshot tests.

Unless a step says otherwise, run Cargo and `wasm-pack` commands from `rust/`
and npm/Playwright commands from the repository root.

---

### Task 1: Recheck Toolchain and Freeze the Dependency Boundary

**Files:**
- Modify: `rust/Cargo.toml`
- Modify: `rust/Cargo.lock`
- Create: `rust/crates/application/Cargo.toml`
- Create: `rust/crates/application/src/lib.rs`
- Create: `docs/superpowers/verification/2026-08-14-m005-dependency-audit.md`

**Traceability:** CAP-M005-001; GRA-APP-001, GRA-DISPATCH-001,
GRA-MSG-001, GRA-FRONTEND-001; `AGENTS.md` target-architecture and latest-stable
dependency guardrails.

- [x] Run `rustup update stable` and record `rustc --version --verbose`.
- [x] Run `cargo search gpui --limit 1`,
  `cargo search gpui-component --limit 1`, and `cargo info` for every direct
  workspace dependency; record latest stable releases and compatibility.
- [x] Update only to compatible stable releases and keep workspace dependency
  declarations centralized in `rust/Cargo.toml`.
- [x] Add an empty `crates/application` workspace member named
  `athena-application` with dependencies only on `athena-domain`,
  `athena-editor`, `athena-format`, serde, serde_json, uuid, and thiserror.
- [x] Add a dependency-direction test or metadata audit proving domain, format,
  editor, and application do not depend on desktop, web-core, GPUI,
  `wasm-bindgen`, filesystem APIs, or browser APIs.
- [x] Run `cargo metadata --format-version 1 --no-deps` from `rust/` and record
  the application/platform dependency edges.
- [x] Commit only this task with
  `git commit -m "build: add application boundary"`.

### Task 2: Model Ordered Folios and Title-Block Variables

**Files:**
- Modify: `rust/crates/domain/src/ids.rs`
- Modify: `rust/crates/domain/src/project.rs`
- Move: `rust/crates/domain/src/sheet.rs` to `rust/crates/domain/src/folio.rs`
- Modify: `rust/crates/domain/src/folio.rs`
- Create: `rust/crates/domain/src/title_block.rs`
- Create: `rust/crates/domain/src/variables.rs`
- Modify: `rust/crates/domain/src/lib.rs`
- Create: `rust/crates/domain/tests/folio_foundation.rs`

**Traceability:** CAP-M005-002/003; QET-PROJ-001, QET-FOLIO-001/002,
QET-TITLE-001/002, QET-VAR-001, QET-PLATE-001, GRA-DOC-001.

- [ ] Write failing tests for project construction with one initial folio,
  inherited defaults, stable folio IDs, add/activate/move behavior, explicit
  order, user-configured Page Num, and validation of duplicate/missing order
  entries.
- [ ] Define these public domain contracts, with module and API documentation:

```rust
pub struct FolioId(Uuid);

pub enum TitleBlockPlacement {
    Bottom,
    Right,
}

pub enum VariableReference {
    Project(String),
    Folio(String),
}

pub enum TemplateSegment {
    Literal(String),
    Variable(VariableReference),
}

pub struct TemplateText(pub Vec<TemplateSegment>);

pub struct TitleBlockValues {
    pub template_id: String,
    pub placement: TitleBlockPlacement,
    pub title: TemplateText,
    pub author: TemplateText,
    pub date_text: TemplateText,
    pub file_label: TemplateText,
    pub folio_label: TemplateText,
    pub plant: TemplateText,
    pub location: TemplateText,
    pub revision: TemplateText,
    pub page_number: TemplateText,
}

pub struct SchematicSettings {
    pub page_width: i64,
    pub page_height: i64,
    pub grid_spacing: i64,
    pub grid_visible: bool,
    pub snap_enabled: bool,
}

pub struct SchematicContent {
    pub settings: SchematicSettings,
    pub symbol_instances: BTreeMap<SymbolInstanceId, SymbolInstance>,
    pub wires: BTreeMap<WireId, Wire>,
    pub junctions: BTreeMap<JunctionId, Junction>,
    pub annotations: BTreeMap<AnnotationId, Annotation>,
}

pub struct Folio {
    pub id: FolioId,
    pub label: String,
    pub title_block: TitleBlockValues,
    pub variables: BTreeMap<String, String>,
    pub schematic: SchematicContent,
}
```

- [ ] Replace `SheetId`/`Sheet` with `FolioId`/`Folio` as the sole project-page
  identity. Extract the current symbol/wire/junction/annotation maps and sheet
  payload into `SchematicContent` owned by `Folio`; rename `SheetSettings` to
  `SchematicSettings` and preserve its page/grid fields without changing their
  M005 behavior. Then use
  `folio_order: Vec<FolioId>` and `folios: BTreeMap<FolioId, Folio>` as the sole
  page map/order authority.
- [ ] Add project folio defaults containing title-block values and default folio
  variables, plus a separate project-scoped variable map. New folios clone the
  folio defaults at creation time; later default edits do not silently rewrite
  existing folios. `Folio.variables` is the sole folio-variable storage; title
  blocks only store typed references to it.
- [ ] Validate variable keys as trimmed, non-empty, unique within scope, and
  free of control characters. Values remain ordinary Unicode text.
- [ ] Implement typed `TemplateText` resolution for project and folio custom
  references. Standard title-block fields do not recursively reference one
  another. The panel inserts references from a scoped picker; M005 defines no
  ad hoc textual formula grammar. Missing references return typed diagnostics
  and remain visible in resolved output.
- [ ] Run `cargo test -p athena-domain --test folio_foundation` and verify all
  domain cases pass.
- [ ] Run all existing domain tests and update fixtures only where the new
  folio ownership intentionally replaces sheet ownership.
- [ ] Commit with `git commit -m "feat: model projects and ordered folios"`.

### Task 3: Define the Versioned Persistence Contract

**Files:**
- Modify: `rust/crates/format/src/envelope.rs`
- Modify: `rust/crates/format/src/migrations.rs`
- Modify: `rust/crates/format/src/snapshot.rs`
- Create: `rust/crates/format/tests/folio_roundtrip.rs`
- Create: `rust/crates/format/tests/fixtures/m005-project.json`

**Traceability:** CAP-M005-004; QET-PERSIST-001, QET-PROJ-001,
QET-FOLIO-001/002, QET-TITLE-002, QET-VAR-001, GRA-PORTFOLIO-001,
GRA-DOC-001.

- [ ] Write failing round-trip tests for project identity, folio order,
  inherited and overridden title-block fields, project/folio variables, and
  derived display equality.
- [ ] Define schema version `3` as a serde envelope containing the semantic
  project document and locally restorable active folio identity. Serialize Page
  Num as title-block data; never infer folio order from it.
- [ ] Make encoding deterministic for equal semantic state and decoding call
  domain validation before returning a project.
- [ ] Reject unsupported future versions, malformed UUIDs, empty project/folio
  labels, duplicate/missing folio order entries, and invalid active folio IDs
  with typed `FormatError` variants.
- [ ] Replace the prototype v1 -> v2 migration with typed rejection of schema 1
  and 2 as `UnsupportedPrototypeSchema { actual, minimum_supported: 3 }`. Do not
  silently reinterpret old sheet data; migrations begin from schema 3 forward.
- [ ] Run `cargo test -p athena-format` and confirm the canonical fixture
  encodes byte-for-byte identically after decode/re-encode.
- [ ] Commit with `git commit -m "feat: persist folio project documents"`.

### Task 4: Add Document Commands and Transactional History

**Files:**
- Modify: `rust/crates/editor/src/command.rs`
- Modify: `rust/crates/editor/src/session.rs`
- Modify: `rust/crates/editor/src/history.rs`
- Create: `rust/crates/editor/tests/folio_commands.rs`

**Traceability:** CAP-M005-002/003; QET-HISTORY-001, QET-FOLIO-001,
QET-TITLE-002, QET-VAR-001, GRA-DOC-001, GRA-HISTORY-001.

- [ ] Write failing command tests for add folio, activate folio, move folio,
  rename project/folio, update defaults, update title-block field, and set or
  remove project/folio variables.
- [ ] Add typed command variants carrying stable IDs and old/new values needed
  for deterministic undo/redo. Do not use generic string paths for domain
  mutation.
- [ ] Ensure one committed property edit is one history transaction. Invalid
  edits must leave state, undo stack, redo stack, and dirty state unchanged.
- [ ] Add sequence tests proving add/move/edit -> undo all -> redo all restores
  equal canonical snapshots.
- [ ] Add `DocumentRevision(u64)` state identities allocated monotonically for
  each newly committed semantic state; undo/redo restores an existing revision
  identity rather than assigning equality by wall-clock time.
  Save-checkpoint tests must prove byte generation alone does not mutate history;
  matching success discards transactions at or before the saved revision and
  clears its redo timeline; edits committed while save is pending remain in undo
  history and keep the document dirty. Cancellation, failure, and stale or
  mismatched success preserve history and dirty state.
- [ ] Run `cargo test -p athena-editor --test folio_commands` followed by
  `cargo test -p athena-editor`.
- [ ] Commit with `git commit -m "feat: add folio editing commands"`.

### Task 5: Build `AthenaEditor`, Dispatcher, and Handler Ownership

**Files:**
- Modify: `rust/crates/application/Cargo.toml`
- Modify: `rust/crates/application/src/lib.rs`
- Create: `rust/crates/application/src/application.rs`
- Create: `rust/crates/application/src/dispatcher.rs`
- Create: `rust/crates/application/src/message.rs`
- Create: `rust/crates/application/src/frontend_message.rs`
- Create: `rust/crates/application/src/portfolio.rs`
- Create: `rust/crates/application/src/document.rs`
- Create: `rust/crates/application/src/layout.rs`
- Create: `rust/crates/application/tests/dispatch_contracts.rs`

**Traceability:** CAP-M005-001/002/004; GRA-APP-001,
GRA-DISPATCH-001, GRA-MSG-001, GRA-FRONTEND-001, GRA-PORTFOLIO-001,
GRA-DOC-001, GRA-HISTORY-001.

- [ ] Write failing tests that submit typed messages to `AthenaEditor` and
  assert ordered frontend effects and canonical state.
- [ ] Define the narrow facade and root protocol:

```rust
pub struct AthenaEditor {
    dispatcher: AthenaDispatcher,
}

impl AthenaEditor {
    pub fn handle_message(
        &mut self,
        message: impl Into<AthenaMessage>,
    ) -> Vec<AthenaFrontendMessage>;
}

pub enum AthenaMessage {
    Portfolio(PortfolioMessage),
    Document(DocumentMessage),
    Layout(LayoutMessage),
}
```

- [ ] Implement a deterministic queue where handlers append child messages and
  frontend effects; prevent reentrant direct calls into sibling handlers.
- [ ] Make `PortfolioHandler` own project lifetime/open/save coordination,
  `DocumentHandler` own the active project editing session, and
  `LayoutHandler` own workspace/plate presentation state.
- [ ] Emit effects for open/close, active folio, dirty state, outline/order,
  targeted panel layout, save/open requests, diagnostics, and errors.
- [ ] Route platform save results back through typed messages carrying
  `SaveRequestId`, project identity, and `DocumentRevision`. Permit at most one
  active save per project. Matching success establishes only the echoed revision
  as the clean checkpoint. Matching success, cancellation, or failure releases
  the active request; cancellation/failure preserve document state. A stale or
  mismatched result emits a typed diagnostic without clearing the actual active
  request.
- [ ] Add an architecture test proving no public application API returns a
  mutable domain or editor-session reference.
- [ ] Run `cargo test -p athena-application`.
- [ ] Commit with `git commit -m "feat: add typed application dispatcher"`.

### Task 6: Implement Backend-Owned Workspace and Electrical Plates

**Files:**
- Modify: `rust/crates/application/src/layout.rs`
- Create: `rust/crates/application/src/widget.rs`
- Create: `rust/crates/application/tests/layout_contracts.rs`

**Traceability:** CAP-M005-003/005; QET-TITLE-001/002, QET-VAR-001,
QET-PLATE-001, GRA-LAYOUT-001, GRA-PANEL-001, GRA-PROPS-001,
GRA-UX-001.

- [ ] Write failing tests for a workspace with document viewport, project
  outline, and contextual properties panels; active panel identity and split
  sizes must be Rust-owned.
- [ ] Define stable `LayoutTarget`, `PanelId`, `WidgetId`, `WidgetValue`, and
  `WidgetCallback` contracts. IDs must remain stable across value-only refreshes.
- [ ] Build QET-evidenced project and folio/title-block plates only: project
  name/variables/defaults; folio label/template/placement/standard fields/custom
  variables and Page Num. Automatic Page Num patterns remain outside M005.
- [ ] Emit structural replacement only when widget structure changes and
  value diffs otherwise. Reject callbacks for unknown target/widget IDs.
- [ ] Test panel close/reopen, active-folio change, invalid field diagnostics,
  and callback commit routing through `AthenaMessage`.
- [ ] Run `cargo test -p athena-application --test layout_contracts`.
- [ ] Commit with `git commit -m "feat: add electrical panel layouts"`.

### Task 7: Replace the WASM Direct-Method Facade

**Files:**
- Modify: `rust/crates/web-core/Cargo.toml`
- Rewrite: `rust/crates/web-core/src/lib.rs`
- Rewrite: `rust/crates/web-core/tests/web_editor_contracts.rs`
- Create: `rust/crates/web-core/tests/wasm_protocol.rs`

**Traceability:** CAP-M005-001/004; GRA-APP-001, GRA-MSG-001,
GRA-FRONTEND-001, GRA-WASM-001.

- [ ] Remove direct exported mutation methods such as `update_sheet_name`,
  `update_selected_*`, and arbitrary command JSON application.
- [ ] Make the WASM handle own one persistent `AthenaEditor` and expose only
  creation, typed message dispatch, frontend-effect callback registration,
  open-bytes delivery, and platform-result delivery.
- [ ] Serialize messages/effects with tagged serde enums generated from Rust
  types; JavaScript must not construct unvalidated domain snapshots.
- [ ] Add native Rust protocol tests plus `wasm-bindgen-test` coverage for
  create/add/move/edit/save/open message sequences, including an edit committed
  while a save request is pending, retry after matching cancellation/failure,
  and a stale result that must not release the current active request.
- [ ] Run
  `cargo check -p athena-web-core --target wasm32-unknown-unknown` and
  `wasm-pack test --headless --chrome crates/web-core`.
- [ ] Commit with `git commit -m "feat: route web through typed messages"`.

### Task 8: Rebuild the GPUI Adapter Around Frontend Effects

**Files:**
- Modify: `rust/crates/desktop/Cargo.toml`
- Rewrite: `rust/crates/desktop/src/app.rs`
- Rewrite: `rust/crates/desktop/src/panels.rs`
- Modify: `rust/crates/desktop/src/storage.rs`
- Modify: `rust/crates/desktop/src/lib.rs`
- Rewrite: `rust/crates/desktop/tests/desktop_authoring.rs`

**Traceability:** CAP-M005-001/004/005; QET-PLATE-001,
GRA-FRONTEND-001, GRA-LAYOUT-001, GRA-PANEL-001, GRA-PROPS-001,
GRA-PLATFORM-001, GRA-UX-001.

- [ ] Make the desktop state own `AthenaEditor` plus adapter-only GPUI view
  state. All user actions dispatch messages and then reduce returned effects.
- [ ] Render the Graphite-evidenced shell hierarchy: compact application/title
  controls, document viewport, project outline panel, contextual electrical
  properties panel, and status feedback. Use `gpui-component` controls and
  icons where available.
- [ ] Render fields from Rust layout/widget data; widget commits dispatch stable
  callback messages. Do not hard-code a second title-block form model.
- [ ] Implement adapter-owned open/save dialogs and atomic file replacement,
  then report success/failure back as typed messages.
- [ ] Add a GPUI integration harness that runs the required M005 workflow
  without calling domain/editor methods directly.
- [ ] Run `cargo test -p athena-desktop` and `cargo run -p athena-desktop` for
  interactive verification.
- [ ] Commit with `git commit -m "feat: adapt gpui shell to application effects"`.

### Task 9: Reduce the Browser to a Thin WASM Shell

**Files:**
- Rewrite: `web/bootstrap.js`
- Modify: `web/index.html`
- Rewrite: `web/styles.css`
- Rewrite: `web/tests/authoring-mvp.test.js`
- Modify: `web/README.md`

**Traceability:** CAP-M005-001/004/005; QET-PLATE-001,
GRA-FRONTEND-001, GRA-LAYOUT-001, GRA-PANEL-001, GRA-PROPS-001,
GRA-WASM-001, GRA-UX-001.

- [ ] Replace direct editor method calls with typed message dispatch and a
  single frontend-effect router.
- [ ] Keep browser-owned state limited to DOM references, focus, file handles,
  and rendering caches. Project, folio, dirty state, variables, field values,
  validation, and panel lifecycle remain Rust-owned.
- [ ] Render the same shell hierarchy and project/folio plate groups as desktop,
  using the Rust-provided workspace and widget models.
- [ ] Implement browser open/save via file input/download or File System Access
  APIs, then return bytes/results through typed messages.
- [ ] Add Playwright tests for the exact required workflow, invalid variables,
  save/close/reopen, stable folio order, and no console/page errors.
- [ ] Run `npm test` and record the exact passing test count.
- [ ] Commit with `git commit -m "feat: adapt web shell to application effects"`.

### Task 10: Cross-Platform, Visual, and Adversarial Certification

**Files:**
- Create: `rust/crates/application/tests/cross_platform_fixture.rs`
- Create: `docs/superpowers/verification/2026-08-14-m005-application-spine-and-folio-foundation.md`
- Modify: `docs/superpowers/plans/2026-08-14-m005-application-spine-and-folio-foundation.md`

**Traceability:** CAP-M005-001/002/003/004/005; all 22 IDs in the M005
Evidence Scope. Verification must report each gate separately.

- [ ] Generate one canonical semantic message fixture and assert identical
  state hashes and semantic effect traces through native and WASM adapters.
- [ ] Run fresh:

```powershell
Push-Location rust
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
cargo check -p athena-domain -p athena-format -p athena-editor -p athena-application -p athena-web-core --target wasm32-unknown-unknown
Pop-Location
npm test
```

- [ ] Start both shells and capture `test-results/m005/desktop-1440x900.png`,
  `web-1440x900.png`, and `web-390x844.png`. Compare hierarchy and density to
  the user-provided Graphite reference
  `https://static.graphite.art/content/index/gui-demo-painted-dreams__4.avif`,
  and compare electrical group content to
  `reference/qelectrotech-doc/source/_external/_images/en/qet_panel/qet_panel_projects.png`,
  `reference/qelectrotech-doc/source/_external/_images/en/qet_folios/qet_folio_tabs.png`,
  and
  `reference/qelectrotech-doc/source/_external/_images/en/qet_folios/qet_folio_prop_title_block_main.png`.
  Check clipping, overlap, focus, hover, and text fit. Record that full
  professional visual fidelity is outside M005 unless separately accepted.
- [ ] Run adversarial checks for direct platform mutation, duplicated form
  state, unstable widget IDs, invalid folio order, unresolved callback IDs,
  malformed variables, future schema versions, empty-command paths, stale or
  mismatched save results, post-request edits, and desktop/web semantic
  divergence.
- [ ] Record `CONFIRMED`, `DEDUCED`, `UNKNOWN`, `FAILED`, and `SKIPPED`
  separately. Leave failed or skipped required gates unchecked.
- [ ] Request an independent code/spec review and resolve all important issues.
- [ ] Present the running workflow to the user. Only the user may mark the User
  Gate accepted.
- [ ] After explicit acceptance, commit verification-only changes with
  `git commit -m "docs: verify m005 folio foundation"` and push `next-001`.
