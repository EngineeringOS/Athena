# Rust Electrical Schematic Authoring MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first end-to-end electrical schematic authoring slice with a shared Rust/WASM core, a GPUI desktop shell, and a thin browser shell.

**Architecture:** Create a new Cargo workspace under `rust/`. Keep domain state, geometry, commands, serialization, scene projection, and local persistence platform-neutral. Expose the editor through a GPUI native binary and a small `wasm-bindgen` API consumed by `web/` HTML/JavaScript.

**Tech Stack:** Latest stable Rust toolchain and latest compatible stable releases at implementation time; Cargo workspace, `serde`/`serde_json`, `uuid`, `thiserror`, `wasm-bindgen`, GPUI from the current compatible release/source in `reference/zed`, `gpui-component` from the current compatible release/source in `reference/gpui-component`, browser Canvas 2D for the MVP scene host, Rust unit/integration tests, and `wasm-bindgen-test` where available. Do not inherit stale dependency versions from the reference checkouts without verification.

---

## Scope and file map

This plan covers only Foundation + Authoring MVP. Later plans will add electrical parity, cloud services, and realtime collaboration.

```text
rust/Cargo.toml
rust/crates/domain/        canonical project/sheet/symbol/wire types
rust/crates/geometry/      coordinates, transforms, snapping, routing
rust/crates/format/        JSON envelope, migrations, atomic snapshot bytes
rust/crates/editor/        commands, validation, history, tools, selection
rust/crates/render/        platform-neutral scene and hit-test projection
rust/crates/library/       in-memory symbol definitions and search
rust/crates/desktop/       GPUI application shell
rust/crates/web-core/      wasm-bindgen bridge
web/index.html             thin browser host
web/bootstrap.js           WASM loading and Canvas 2D event bridge
```

Each crate must remain usable without the UI shells unless its purpose is a
shell adapter. The first implementation should use simple deterministic data
structures; do not add cloud or plugin abstractions before their contracts are
needed.

### Task 1: Create the Rust workspace and crate skeleton

**Files:**
- Create: `rust/Cargo.toml`
- Create: `rust/crates/domain/Cargo.toml`, `rust/crates/domain/src/lib.rs`
- Create: `rust/crates/geometry/Cargo.toml`, `rust/crates/geometry/src/lib.rs`
- Create: `rust/crates/format/Cargo.toml`, `rust/crates/format/src/lib.rs`
- Create: `rust/crates/editor/Cargo.toml`, `rust/crates/editor/src/lib.rs`
- Create: `rust/crates/render/Cargo.toml`, `rust/crates/render/src/lib.rs`
- Create: `rust/crates/library/Cargo.toml`, `rust/crates/library/src/lib.rs`
- Create: `rust/crates/desktop/Cargo.toml`, `rust/crates/desktop/src/main.rs`
- Create: `rust/crates/web-core/Cargo.toml`, `rust/crates/web-core/src/lib.rs`

- [x] **Step 1: Write the workspace manifest**

Define all crates as members and centralize versions for `serde`, `serde_json`,
`uuid`, `thiserror`, `wasm-bindgen`, and GPUI. Keep desktop dependencies
optional from core crates.

- [x] **Step 2: Add compile smoke tests**

Add one `#[test] fn workspace_crates_compile_contract()` in each core crate that
constructs its public marker type. Run `cargo test --workspace` from `rust/`.
Expected: all tests pass with no UI or filesystem dependency in core crates.

- [x] **Step 3: Commit**

```text
git add rust
git commit -m "feat: scaffold rust schematic workspace"
```

### Task 2: Define the canonical domain model

**Files:**
- Modify: `rust/crates/domain/src/lib.rs`
- Create: `rust/crates/domain/src/ids.rs`
- Create: `rust/crates/domain/src/project.rs`
- Create: `rust/crates/domain/src/sheet.rs`
- Create: `rust/crates/domain/src/symbol.rs`
- Create: `rust/crates/domain/src/electrical.rs`
- Create: `rust/crates/domain/tests/domain_invariants.rs`

- [x] **Step 1: Write failing domain invariant tests**

Cover: a new project has one sheet; every entity ID is unique; a symbol
instance references an existing definition; terminal ownership is stable; a
wire endpoint is either a terminal or an explicit junction; and deleting a
sheet cannot leave entities reachable from the project index.

- [x] **Step 2: Implement typed IDs and state**

Use newtypes around `Uuid`: `ProjectId`, `SheetId`, `SymbolDefinitionId`,
`SymbolInstanceId`, `TerminalId`, `WireId`, `JunctionId`, `AnnotationId`.
Implement `Project`, `Sheet`, `SymbolDefinition`, `SymbolInstance`, `Terminal`,
`Wire`, `Junction`, `FieldValue`, and `ProjectSettings` with `Serialize`,
`Deserialize`, `Clone`, `Debug`, and equality derives. Store entity collections
in deterministic `BTreeMap`s and preserve an explicit sheet order vector.

- [x] **Step 3: Add constructors and invariant validation**

Implement `Project::new(name)`, `Project::add_sheet`,
`Project::validate() -> Result<(), DomainError>`, and sheet/entity lookup APIs.
Validation must return structured errors identifying the offending ID.

- [x] **Step 4: Run tests and commit**

```text
cargo test -p athena-domain
git add rust/crates/domain
git commit -m "feat: define electrical schematic domain model"
```

### Task 3: Implement geometry, snapping, and orthogonal routing

**Files:**
- Modify: `rust/crates/geometry/src/lib.rs`
- Create: `rust/crates/geometry/src/point.rs`
- Create: `rust/crates/geometry/src/transform.rs`
- Create: `rust/crates/geometry/src/snap.rs`
- Create: `rust/crates/geometry/src/routing.rs`
- Create: `rust/crates/geometry/tests/geometry_contracts.rs`

- [x] **Step 1: Write failing geometry tests**

Test integer-grid snapping, terminal-priority snapping within a tolerance,
rotation/mirroring round trips, deterministic orthogonal paths, and rejection
of a route containing a zero-length segment.

- [x] **Step 2: Implement geometry primitives**

Use a fixed-point-friendly `WorldPoint { x: f64, y: f64 }`, `Rect`, `Transform`
with quarter-turn rotation and mirror flags, `SnapSettings`, and
`ConnectionAnchor`. Keep all methods pure and deterministic.

- [x] **Step 3: Implement routing and hit geometry**

Implement `orthogonal_route(start, end, obstacles, settings)` with a stable
horizontal-then-vertical default and a deterministic one-bend fallback. Return
`RouteError::NoValidPath` when blocked. Implement distance-to-segment and point-
within-tolerance helpers for later selection.

- [x] **Step 4: Verify and commit**

```text
cargo test -p athena-geometry
git add rust/crates/geometry
git commit -m "feat: add schematic geometry and routing"
```

### Task 4: Add versioned JSON format and migrations

**Files:**
- Modify: `rust/crates/format/src/lib.rs`
- Create: `rust/crates/format/src/envelope.rs`
- Create: `rust/crates/format/src/migrations.rs`
- Create: `rust/crates/format/src/snapshot.rs`
- Create: `rust/crates/format/tests/format_contracts.rs`

- [x] **Step 1: Write failing format tests**

Assert that a project round-trips through JSON, serialized output is stable for
identical input, unknown future fields are ignored, unsupported schema versions
produce a typed error, and migration from schema version `1` to the current
version preserves all entity IDs.

- [x] **Step 2: Implement the envelope**

Define `DocumentEnvelope { format: String, schema_version: u32, project: Project }`
with format value `athena-electrical-project`. Expose `encode_json`,
`decode_json`, and `migrate`. Use `serde(deny_unknown_fields)` only on the
outer envelope; permit forward-compatible fields inside domain records.

- [x] **Step 3: Implement snapshot bytes**

Expose `SnapshotStore` as a pure byte codec with `encode_snapshot` and
`decode_snapshot`; leave actual filesystem/IndexedDB writes to adapters.

- [x] **Step 4: Verify and commit**

```text
cargo test -p athena-format
git add rust/crates/format
git commit -m "feat: add versioned project format"
```

### Task 5: Build deterministic commands and undo/redo

**Files:**
- Modify: `rust/crates/editor/src/lib.rs`
- Create: `rust/crates/editor/src/command.rs`
- Create: `rust/crates/editor/src/history.rs`
- Create: `rust/crates/editor/src/validation.rs`
- Create: `rust/crates/editor/tests/command_contracts.rs`

- [x] **Step 1: Write failing command tests**

Cover placing/moving/deleting a symbol, creating/splitting/deleting a wire,
setting a field, rejecting a wire with an unknown terminal, one-command
multi-selection moves, undo restoring the exact previous snapshot, redo
reapplying the exact command, and serializing/deserializing a command envelope.

- [x] **Step 2: Define command types**

Implement `EditorCommand` variants `PlaceSymbol`, `MoveItems`, `RotateItems`,
`MirrorItems`, `DeleteItems`, `CreateWire`, `SplitWire`, `DeleteWire`,
`SetFieldValue`, and `ApplySheetSettings`. Define `CommandEnvelope` with
operation ID, project/sheet IDs, base revision, author/session IDs, command
version, and payload.

- [x] **Step 3: Implement atomic application**

Implement `EditorState::apply(command) -> Result<AppliedCommand, ApplyError>`.
Validate all IDs and endpoints before mutating. `AppliedCommand` stores the
inverse command and a before/after revision. A failed command leaves the project
byte-for-byte unchanged.

- [x] **Step 4: Implement history**

Implement bounded `History` with `undo`, `redo`, and redo invalidation after a
new edit. History stores commands/inverses, not UI state. Add snapshot equality
helpers for exact test assertions.

- [x] **Step 5: Verify and commit**

```text
cargo test -p athena-editor
git add rust/crates/editor
git commit -m "feat: add deterministic editor commands"
```

### Task 6: Add symbol library and search

**Files:**
- Modify: `rust/crates/library/src/lib.rs`
- Create: `rust/crates/library/src/catalog.rs`
- Create: `rust/crates/library/src/search.rs`
- Create: `rust/crates/library/tests/library_contracts.rs`

- [x] **Step 1: Write failing library tests**

Test registering a definition with terminals and primitives, duplicate-ID
rejection, case-insensitive name/tag search, deterministic result ordering, and
placement preview data containing all terminal anchors.

- [x] **Step 2: Implement catalog and search**

Implement `SymbolCatalog`, `SymbolDefinitionRecord`, `Primitive`, and
`SearchQuery`. Keep the MVP catalog in memory and load a small built-in set of
electrical symbols (resistor, switch, lamp, motor, connector, power terminal)
from Rust constants.

- [x] **Step 3: Verify and commit**

```text
cargo test -p athena-library
git add rust/crates/library
git commit -m "feat: add electrical symbol catalog"
```

### Task 7: Project editor scene and hit-testing projection

**Files:**
- Modify: `rust/crates/render/src/lib.rs`
- Create: `rust/crates/render/src/scene.rs`
- Create: `rust/crates/render/src/hit_test.rs`
- Create: `rust/crates/render/tests/scene_contracts.rs`

- [x] **Step 1: Write failing scene tests**

Assert deterministic layer ordering, symbol terminals producing connection
handles, wires producing segment hit regions, selection overlays never entering
the saved project, and identical project snapshots producing identical scenes.

- [x] **Step 2: Implement scene types and projection**

Define `Scene`, `SceneLayer`, `DrawPrimitive`, `HitRegion`, `Viewport`, and
`Overlay`. Implement `project_sheet(project, sheet_id, editor_presentation)`;
it must emit page/grid, wires/junctions, symbols, fields/annotations, then
selection/guides/validation overlays.

- [x] **Step 3: Implement hit testing**

Implement `hit_test(scene, viewport_point, tolerance)` returning deterministic
topmost hits with terminal priority over symbol body and wire vertex priority
over wire segment.

- [x] **Step 4: Verify and commit**

```text
cargo test -p athena-render
git add rust/crates/render
git commit -m "feat: project schematic scenes"
```

### Task 8: Add local persistence adapters

**Files:**
- Create: `rust/crates/editor/src/persistence.rs`
- Create: `rust/crates/editor/tests/persistence_contracts.rs`
- Create: `rust/crates/desktop/src/storage.rs`
- Modify: `rust/crates/web-core/src/lib.rs`

- [x] **Step 1: Write failing persistence tests**

Test that a snapshot writes to a temporary path and reopens, a failed replacement
keeps the previous bytes, and an outbox retains unsynced command envelopes in
order.

- [x] **Step 2: Implement platform-neutral persistence contracts**

Define `SnapshotSink`, `SnapshotSource`, and `OutboxStore` traits returning
structured `PersistenceError`s. Implement an in-memory adapter for core tests.

- [x] **Step 3: Implement desktop file adapter**

Use an explicit temporary sibling file followed by rename for atomic replacement.
Do not put path or filesystem types in the core domain crates.

- [x] **Step 4: Implement browser bridge stubs**

Expose WASM methods accepting/returning `Uint8Array` snapshot bytes. Browser
JavaScript owns IndexedDB/localStorage integration; Rust owns encoding and
validation.

- [x] **Step 5: Verify and commit**

```text
cargo test -p athena-editor -p athena-web-core
git add rust/crates/editor rust/crates/desktop rust/crates/web-core
git commit -m "feat: add local snapshot and outbox contracts"
```

### Task 9: Build the GPUI desktop shell

**Files:**
- Modify: `rust/crates/desktop/Cargo.toml`
- Modify: `rust/crates/desktop/src/main.rs`
- Create: `rust/crates/desktop/src/app.rs`
- Create: `rust/crates/desktop/src/canvas.rs`
- Create: `rust/crates/desktop/src/panels.rs`
- Create: `rust/crates/desktop/src/input.rs`

- [ ] **Step 1: Add a native launch smoke test**

Add an ignored integration test or manual command that launches the app with a
new project and exits cleanly after the window initializes. Keep CI-safe core
tests separate from display-dependent tests.

- [x] **Step 2: Implement the shell layout**

Create project tabs/menu, left symbol library panel, central canvas, right
inspector, and status bar. Use GPUI primitives and `gpui-component` controls for
buttons, inputs, lists, and panels. The shell owns only presentation state.

- [x] **Step 3: Wire canvas input to commands**

Map pointer/keyboard events to select, place-symbol, wire, pan, zoom, and
undo/redo tools. Render the shared scene projection. Commit exactly one command
on symbol drop and wire completion.

- [x] **Step 4: Verify manually and commit**

```text
cargo check -p athena-desktop
cargo test --workspace
cargo run -p athena-desktop
git add rust/crates/desktop
git commit -m "feat: add gpui desktop authoring shell"
```

### Task 10: Build the thin WASM/browser shell

**Files:**
- Modify: `rust/crates/web-core/src/lib.rs`
- Create: `web/index.html`
- Create: `web/bootstrap.js`
- Create: `web/styles.css`
- Create: `web/README.md`

- [x] **Step 1: Add WASM API tests**

Test in Rust that `create_project`, `apply_command`, `render_active_sheet`,
`encode_snapshot`, and `load_snapshot` expose stable JSON/byte contracts.

- [x] **Step 2: Implement the WASM bridge**

Expose a small `WebEditor` handle with methods for project creation, command
application, scene projection, snapshot encoding/loading, and pointer events.
Return serialized scene data; do not expose mutable domain internals.

- [x] **Step 3: Implement the HTML host**

Load the generated WASM module, create a canvas and minimal toolbar, forward
pointer/keyboard events, draw the returned scene primitives with Canvas 2D, and
use browser download APIs for snapshot export. Keep all tool and document state
inside the WASM handle.

- [x] **Step 4: Verify and commit**

```text
wasm-pack test --headless --chrome -p athena-web-core
wasm-pack build rust/crates/web-core --target web
python -m http.server 8080 --directory web
git add rust/crates/web-core web
git commit -m "feat: add thin wasm browser shell"
```

### Task 11: Authoring MVP end-to-end verification

**Files:**
- Create: `rust/tests/authoring_mvp.rs`
- Create: `web/tests/authoring-mvp.test.js`
- Create: `docs/superpowers/verification/2026-08-13-authoring-mvp.md`

- [x] **Step 1: Write the shared scenario test**

Exercise: create project, add sheet, register/place a resistor and power
terminal, connect terminals with a wire, move the resistor, undo/redo, encode,
decode, and compare the final snapshot and scene projection.

- [ ] **Step 2: Add browser smoke verification**

Use Playwright or the existing browser runner to load `web/index.html`, place a
symbol, draw a wire, trigger undo/redo, and assert the canvas is nonblank and a
downloadable snapshot is produced.

- [x] **Step 3: Run the full verification matrix**

```text
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
wasm-pack test --headless --chrome -p athena-web-core
```

Record exact commands, platform/tool versions, and any display-dependent tests
that were skipped in `docs/superpowers/verification/2026-08-13-authoring-mvp.md`.

- [x] **Step 4: Commit verification artifacts**

```text
git add rust/tests web/tests docs/superpowers/verification
git commit -m "test: verify authoring mvp across shells"
```

## Self-review

- Spec coverage: domain, geometry, commands, versioned JSON, local-first
  persistence, scene projection, GPUI, thin WASM shell, and MVP acceptance are
  covered by Tasks 1-11. Cloud and realtime are intentionally deferred to later
  plans as required by the approved decomposition.
- Placeholder scan: no `TBD`, `TODO`, or unspecified implementation steps are
  used in the task checklist.
- Type consistency: `Project`, `EditorCommand`, `CommandEnvelope`, `Scene`,
  `SnapshotSink`, and `WebEditor` are introduced before their consumers.
- Existing worktree changes: implementation must not revert or clean unrelated
  legacy files. Only the new `rust/`, `web/`, and plan-scoped documentation are
  in scope.
