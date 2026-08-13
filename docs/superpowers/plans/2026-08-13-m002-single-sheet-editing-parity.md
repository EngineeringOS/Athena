# Single-Sheet Editing Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add shared single-sheet selection, marquee, wire editing, and minimal property editing behavior that works the same in the desktop and web shells.

**Architecture:** Keep persistent schematic state, commands, undo/redo, and scene projection in the shared Rust crates under `rust/crates/`. Add a shared editor-session layer in `athena-editor` for transient interaction state, use `athena-render` for wire edit overlays and hit regions, then make `athena-desktop` and `athena-web-core` thin adapters over that shared behavior.

**Tech Stack:** Rust 2024 workspace in `rust/`; `serde`, `uuid`, `thiserror`, `wasm-bindgen`; GPUI and `gpui-component` for desktop; HTML/Canvas 2D plus JavaScript host in `web/`; Rust unit/integration tests; existing Playwright smoke tests in `web/tests`.

---

## Scope and file map

This plan covers only the `m002` single-sheet editing slice described in
`docs/superpowers/specs/2026-08-13-m002-single-sheet-editing-parity-design.md`.
It does not cover multi-sheet orchestration, reports, printing, cloud sync,
or library authoring UX.

```text
rust/crates/domain/src/electrical.rs
  wire route structure and minimal field-bearing schematic entities

rust/crates/editor/src/lib.rs
  public editor-session exports
rust/crates/editor/src/command.rs
  persistent document mutations for wire editing and settings updates
rust/crates/editor/src/validation.rs
  command preflight checks and wire-route invariants
rust/crates/editor/src/history.rs
  unchanged history boundary, but exercised by new command tests
rust/crates/editor/src/selection.rs
  transient selection sets and marquee selection helpers
rust/crates/editor/src/interaction.rs
  pointer/key interaction state machine
rust/crates/editor/src/session.rs
  shell-facing controller wrapping EditorState, History, and EditorPresentation

rust/crates/render/src/scene.rs
  scene projection, overlay primitives, and hit region generation
rust/crates/render/src/hit_test.rs
  hit priority for endpoint handles, wire vertices, terminals, segments, symbols

rust/crates/desktop/src/app.rs
  native adapter over shared editor session
rust/crates/desktop/src/input.rs
  native event-to-session input conversion
rust/crates/desktop/src/panels.rs
  GPUI shell, inspector controls, and shortcuts
rust/crates/desktop/src/canvas.rs
  overlay-aware scene painting

rust/crates/web-core/src/lib.rs
  wasm-bindgen adapter over shared editor session
web/index.html
  minimal inspector controls and toolbar
web/bootstrap.js
  pointer drag, keyboard shortcuts, and overlay drawing
web/tests/authoring-mvp.test.js
  browser smoke coverage for marquee and wire editing
```

The implementation should reuse the current `EditorState`, `History`, and
`project_sheet()` flow. Do not create a second command system in the shells.

### Task 1: Add a shared editor session and transient interaction state

**Files:**
- Create: `rust/crates/editor/src/selection.rs`
- Create: `rust/crates/editor/src/interaction.rs`
- Create: `rust/crates/editor/src/session.rs`
- Modify: `rust/crates/editor/src/lib.rs`
- Create: `rust/crates/editor/tests/interaction_contracts.rs`
- Modify: `rust/crates/editor/tests/authoring_mvp.rs`

- [x] **Step 1: Write the failing interaction tests**

Add `rust/crates/editor/tests/interaction_contracts.rs` with tests for:

```rust
fn world(x: i64, y: i64) -> athena_geometry::WorldPoint {
    athena_geometry::WorldPoint::new(x as f64, y as f64)
}

fn fixture_session_with_two_symbols() -> EditorSession {
    let mut session = fixture_session_with_catalog_symbols_at([(100, 80), (200, 80)]);
    session.refresh_scene_for_test().unwrap();
    session
}

fn fixture_session_with_offset_wire_and_symbol() -> EditorSession {
    let mut session = fixture_session_with_catalog_symbols_at([(100, 80), (220, 120)]);
    session.create_wire_for_test((120, 80), (200, 120)).unwrap();
    session.refresh_scene_for_test().unwrap();
    session
}

#[test]
fn empty_click_clears_selection() {
    let mut session = fixture_session_with_two_symbols();
    session.select_symbol_for_test(0);
    session.pointer_down(world(300, 300), PointerModifiers::default()).unwrap();
    session.pointer_up(world(300, 300), PointerModifiers::default()).unwrap();
    assert!(session.presentation().selected.is_empty());
}

#[test]
fn shift_click_toggles_symbol_selection() {
    let mut session = fixture_session_with_two_symbols();
    let first = session.symbol_ids_for_test()[0];
    let second = session.symbol_ids_for_test()[1];
    session.pointer_down_on_item(first, PointerModifiers::default()).unwrap();
    session.pointer_up_on_item(first, PointerModifiers::default()).unwrap();
    session.pointer_down_on_item(second, PointerModifiers { shift: true, ..Default::default() }).unwrap();
    session.pointer_up_on_item(second, PointerModifiers { shift: true, ..Default::default() }).unwrap();
    assert_eq!(session.presentation().selected.len(), 2);
}

#[test]
fn marquee_direction_changes_selection_rule() {
    let mut session = fixture_session_with_offset_wire_and_symbol();
    session.pointer_down(world(0, 0), PointerModifiers::default()).unwrap();
    session.pointer_move(world(80, 40), PointerModifiers::default()).unwrap();
    assert_eq!(session.debug_marquee_mode(), DebugMarqueeMode::Enclosed);
    session.pointer_up(world(80, 40), PointerModifiers::default()).unwrap();

    session.pointer_down(world(80, 40), PointerModifiers::default()).unwrap();
    session.pointer_move(world(0, 0), PointerModifiers::default()).unwrap();
    assert_eq!(session.debug_marquee_mode(), DebugMarqueeMode::Touched);
}
```

- [x] **Step 2: Run the targeted tests to verify they fail**

Run:

```bash
cargo test -p athena-editor interaction_contracts -- --nocapture
```

Expected: compile errors for missing `session`, `pointer_*`, and marquee APIs.

- [x] **Step 3: Implement the shared session surface**

Add the new editor-session types:

```rust
pub struct EditorSession {
    state: EditorState,
    history: History,
    active_sheet_id: SheetId,
    presentation: EditorPresentation,
    selection: SelectionState,
    interaction: InteractionState,
}

pub enum InteractionState {
    Idle,
    MarqueeSelecting(MarqueeState),
    DraggingSelection(DragSelectionState),
    EditingWireVertex(WireVertexDragState),
    ReconnectingWireEndpoint(WireEndpointReconnectState),
}

pub struct PointerModifiers {
    pub shift: bool,
    pub command: bool,
}
```

Implement `pointer_down`, `pointer_move`, `pointer_up`, `delete_selection`,
`rotate_selection_90`, `mirror_selection`, `undo`, `redo`, `presentation`,
and `scene`. Keep `EditorSession` as the single shared shell-facing controller.

- [x] **Step 4: Re-run the targeted tests**

Run:

```bash
cargo test -p athena-editor interaction_contracts authoring_mvp -- --nocapture
```

Expected: PASS for the new interaction tests and the existing MVP acceptance
test still passing.

- [x] **Step 5: Commit**

```bash
git add rust/crates/editor
git commit -m "feat: add shared editor session state"
```

### Task 2: Add persistent wire-editing commands and route normalization

**Files:**
- Modify: `rust/crates/editor/src/command.rs`
- Modify: `rust/crates/editor/src/validation.rs`
- Modify: `rust/crates/domain/src/electrical.rs`
- Modify: `rust/crates/editor/tests/command_contracts.rs`
- Modify: `rust/crates/editor/tests/authoring_mvp.rs`

- [ ] **Step 1: Write the failing wire-edit command tests**

Extend `rust/crates/editor/tests/command_contracts.rs` with:

```rust
fn fixture_state_with_orthogonal_wire() -> (EditorState, SheetId, WireId) {
    let mut project = project_with_two_resistors();
    let sheet_id = project.sheet_order()[0];
    let wire = orthogonal_wire_for_fixture(&project, sheet_id, [(120, 80), (160, 80), (160, 120), (200, 120)]);
    let wire_id = wire.id;
    project.sheet_mut(sheet_id).unwrap().add_wire(wire).unwrap();
    (EditorState::new(project), sheet_id, wire_id)
}

fn fixture_state_with_reconnectable_wire() -> (EditorState, SheetId, WireId, TerminalId) {
    let mut project = project_with_three_resistors();
    let sheet_id = project.sheet_order()[0];
    let replacement_terminal_id = fixture_terminal_id(&project, sheet_id, 2, "1");
    let wire = orthogonal_wire_for_fixture(&project, sheet_id, [(120, 80), (180, 80), (180, 100), (200, 100)]);
    let wire_id = wire.id;
    project.sheet_mut(sheet_id).unwrap().add_wire(wire).unwrap();
    (EditorState::new(project), sheet_id, wire_id, replacement_terminal_id)
}

#[test]
fn insert_move_and_delete_wire_vertex_round_trip_through_history() {
    let (mut state, sheet_id, wire_id) = fixture_state_with_orthogonal_wire();
    let mut history = History::new(16);

    history.apply(&mut state, EditorCommand::InsertWireVertex {
        sheet_id,
        wire_id,
        segment_index: 0,
        position: Point::new(140, 80),
    }).unwrap();
    history.apply(&mut state, EditorCommand::MoveWireVertex {
        sheet_id,
        wire_id,
        vertex_index: 1,
        position: Point::new(140, 120),
    }).unwrap();
    history.apply(&mut state, EditorCommand::DeleteWireVertex {
        sheet_id,
        wire_id,
        vertex_index: 1,
    }).unwrap();

    assert!(history.undo(&mut state).unwrap());
    assert!(history.undo(&mut state).unwrap());
    assert!(history.undo(&mut state).unwrap());
}

#[test]
fn reconnect_wire_endpoint_retargets_terminal_and_updates_route_endpoint() {
    let (mut state, sheet_id, wire_id, replacement_terminal_id) =
        fixture_state_with_reconnectable_wire();
    let mut history = History::new(8);

    history.apply(&mut state, EditorCommand::ReconnectWireEndpoint {
        sheet_id,
        wire_id,
        endpoint: WireSide::End,
        terminal_id: replacement_terminal_id,
    }).unwrap();

    let wire = state.project().sheet(sheet_id).unwrap().wire(wire_id).unwrap();
    assert_eq!(wire.end, WireEndpoint::Terminal(replacement_terminal_id));
    assert_eq!(wire.route.last().copied(), Some(Point::new(240, 100)));
}
```

- [ ] **Step 2: Run the targeted tests to verify they fail**

Run:

```bash
cargo test -p athena-editor command_contracts -- --nocapture
```

Expected: compile errors for missing `InsertWireVertex`, `MoveWireVertex`,
`DeleteWireVertex`, `ReconnectWireEndpoint`, and `WireSide`.

- [ ] **Step 3: Implement the new wire-edit commands**

Add the command surface and normalization helpers:

```rust
pub enum WireSide {
    Start,
    End,
}

pub enum EditorCommand {
    // existing variants...
    ReconnectWireEndpoint {
        sheet_id: SheetId,
        wire_id: WireId,
        endpoint: WireSide,
        terminal_id: TerminalId,
    },
    InsertWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        segment_index: usize,
        position: Point,
    },
    MoveWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        vertex_index: usize,
        position: Point,
    },
    DeleteWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        vertex_index: usize,
    },
}

fn normalize_wire_route(route: &mut Vec<Point>) {
    // keep first and last endpoint points
    // remove duplicate consecutive points
    // remove redundant colinear interior points
    // preserve orthogonal segments only
}
```

Validation must reject:
- segment insert beyond the last segment
- moving endpoint vertices through `MoveWireVertex`
- deleting so many vertices that the route loses its endpoint pair
- reconnecting to a terminal not on the active sheet

- [ ] **Step 4: Re-run the editor command tests**

Run:

```bash
cargo test -p athena-editor command_contracts authoring_mvp -- --nocapture
```

Expected: PASS, including undo/redo for all new wire-edit commands.

- [ ] **Step 5: Commit**

```bash
git add rust/crates/domain/src/electrical.rs rust/crates/editor/src/command.rs rust/crates/editor/src/validation.rs rust/crates/editor/tests
git commit -m "feat: add orthogonal wire editing commands"
```

### Task 3: Extend scene projection, overlays, and hit priority for selection and wire editing

**Files:**
- Modify: `rust/crates/render/src/lib.rs`
- Modify: `rust/crates/render/src/scene.rs`
- Modify: `rust/crates/render/src/hit_test.rs`
- Modify: `rust/crates/render/tests/scene_contracts.rs`

- [ ] **Step 1: Write the failing render and hit-test tests**

Extend `rust/crates/render/tests/scene_contracts.rs` with:

```rust
fn selected_wire_scene_fixture() -> athena_render::Scene {
    let (project, sheet_id) = project_with_symbol_and_wire();
    let wire_id = *project.sheet(sheet_id).unwrap().wires.keys().next().unwrap();
    let presentation = EditorPresentation::with_selected_wire(wire_id);
    project_sheet(&project, sheet_id, &presentation).unwrap()
}

#[test]
fn selected_wire_projects_highlight_and_vertex_overlays() {
    let (project, sheet_id) = project_with_symbol_and_wire();
    let wire_id = *project.sheet(sheet_id).unwrap().wires.keys().next().unwrap();
    let presentation = EditorPresentation::with_selected_wire(wire_id);
    let scene = project_sheet(&project, sheet_id, &presentation).unwrap();

    assert!(scene.layers.iter().flat_map(|layer| &layer.primitives).any(|primitive| {
        matches!(primitive, DrawPrimitive::Overlay { overlay: Overlay::WireVertexHandle { wire_id: id, .. } } if *id == wire_id)
    }));
}

#[test]
fn endpoint_handles_win_over_vertices_terminals_and_segments() {
    let scene = selected_wire_scene_fixture();
    assert!(matches!(
        hit_test(&scene, WorldPoint::new(120.0, 80.0), 1.0),
        Some(HitRegion::WireEndpointHandle { .. })
    ));
}

#[test]
fn marquee_overlay_projects_translucent_rectangle() {
    let (project, sheet_id) = project_with_symbol_and_wire();
    let presentation = EditorPresentation::with_marquee(WorldPoint::new(0.0, 0.0), WorldPoint::new(80.0, 40.0));
    let scene = project_sheet(&project, sheet_id, &presentation).unwrap();
    assert!(scene.layers.iter().flat_map(|layer| &layer.primitives).any(|primitive| {
        matches!(primitive, DrawPrimitive::Overlay { overlay: Overlay::MarqueeRect { .. } })
    }));
}
```

- [ ] **Step 2: Run the targeted render tests to verify they fail**

Run:

```bash
cargo test -p athena-render scene_contracts -- --nocapture
```

Expected: compile errors for missing `WireEndpointHandle`, `WireVertexHandle`,
`MarqueeRect`, and expanded `EditorPresentation` helpers.

- [ ] **Step 3: Implement the overlay and hit-region expansion**

Add the new shared render types:

```rust
pub enum Overlay {
    SelectionBounds { item: PresentationItemId, bounds: Rect },
    TransformHandle { item: PresentationItemId, position: WorldPoint, radius: f64 },
    WireVertexHandle { wire_id: WireId, vertex_index: usize, position: WorldPoint, radius: f64 },
    HoverTerminal { terminal_id: TerminalId, position: WorldPoint, radius: f64 },
    MarqueeRect { start: WorldPoint, end: WorldPoint, enclosed: bool },
    GuideLine { start: WorldPoint, end: WorldPoint },
    ValidationMarker { position: WorldPoint, message: String },
}

pub enum HitRegion {
    WireEndpointHandle { wire_id: WireId, endpoint: WireSide, position: WorldPoint, radius: f64 },
    WireVertex { wire_id: WireId, vertex_index: usize, position: WorldPoint },
    Terminal { terminal_id: TerminalId, symbol_id: SymbolInstanceId, position: WorldPoint, radius: f64 },
    WireSegment { wire_id: WireId, segment_index: usize, start: WorldPoint, end: WorldPoint },
    SymbolBody { symbol_id: SymbolInstanceId, bounds: Rect },
    Annotation { annotation_id: AnnotationId, bounds: Rect },
    Junction { junction_id: JunctionId, position: WorldPoint, radius: f64 },
}
```

Update hit priority to:
- endpoint handle
- wire vertex
- terminal
- wire segment
- symbol body
- annotation
- junction

Project selected wires with highlighted full-path overlays plus visible interior
vertices and endpoint handles. Project hover/snap terminal feedback separately
from the saved scene.

- [ ] **Step 4: Re-run the render tests**

Run:

```bash
cargo test -p athena-render scene_contracts -- --nocapture
```

Expected: PASS for the new overlay and hit-order coverage.

- [ ] **Step 5: Commit**

```bash
git add rust/crates/render
git commit -m "feat: project wire editing overlays"
```

### Task 4: Refactor the desktop shell onto the shared session and add inspector controls

**Files:**
- Modify: `rust/crates/desktop/src/app.rs`
- Modify: `rust/crates/desktop/src/input.rs`
- Modify: `rust/crates/desktop/src/canvas.rs`
- Modify: `rust/crates/desktop/src/panels.rs`
- Modify: `rust/crates/desktop/tests/desktop_authoring.rs`

- [ ] **Step 1: Write the failing desktop integration tests**

Extend `rust/crates/desktop/tests/desktop_authoring.rs` with:

```rust
fn desktop_fixture_with_two_symbols_and_one_wire() -> DesktopEditor {
    let mut editor = DesktopEditor::new("Desktop interaction fixture");
    let resistor = editor.catalog_symbols().into_iter().find(|symbol| symbol.name == "Resistor").unwrap();
    editor.begin_placement(resistor.definition_id);
    editor.canvas_click(Point::new(100, 80)).unwrap();
    editor.begin_placement(resistor.definition_id);
    editor.canvas_click(Point::new(220, 120)).unwrap();
    editor.begin_wiring();
    editor.canvas_click(Point::new(120, 80)).unwrap();
    editor.canvas_click(Point::new(200, 120)).unwrap();
    editor
}

fn desktop_fixture_with_selected_wire() -> DesktopEditor {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();
    editor.select_wire_for_test(0).unwrap();
    editor
}

fn desktop_fixture_with_selected_symbol() -> DesktopEditor {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();
    editor.select_symbol_for_test(0).unwrap();
    editor
}

#[test]
fn marquee_selects_symbols_directionally_in_desktop_shell() {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();
    editor.pointer_drag_for_test(Point::new(80, 60), Point::new(260, 120), DesktopModifiers::default()).unwrap();
    assert_eq!(editor.selected_count(), 2);
}

#[test]
fn desktop_shell_moves_a_wire_vertex_and_updates_the_scene() {
    let mut editor = desktop_fixture_with_selected_wire();
    editor.drag_wire_vertex_for_test(1, Point::new(140, 80), Point::new(140, 120)).unwrap();
    let scene = editor.scene().unwrap();
    assert!(format!("{scene:?}").contains("140"));
}

#[test]
fn inspector_updates_selected_symbol_reference() {
    let mut editor = desktop_fixture_with_selected_symbol();
    editor.set_selected_symbol_reference_for_test("K1").unwrap();
    assert_eq!(editor.selected_symbol_reference_for_test().as_deref(), Some("K1"));
}
```

- [ ] **Step 2: Run the targeted desktop tests to verify they fail**

Run:

```bash
cargo test -p athena-desktop desktop_authoring -- --nocapture
```

Expected: compile errors for missing pointer-drag and inspector APIs.

- [ ] **Step 3: Replace desktop-owned editing logic with the shared session**

Refactor `DesktopEditor` to wrap `EditorSession`:

```rust
pub struct DesktopEditor {
    catalog: SymbolCatalog,
    session: EditorSession,
    active_tool: ActiveTool,
}

impl DesktopEditor {
    pub fn pointer_down(&mut self, point: Point, modifiers: DesktopModifiers) -> Result<(), String> {
        self.session.pointer_down(world_point(point), modifiers.into()).map_err(|e| e.to_string())
    }

    pub fn pointer_move(&mut self, point: Point, modifiers: DesktopModifiers) -> Result<(), String> {
        self.session.pointer_move(world_point(point), modifiers.into()).map_err(|e| e.to_string())
    }

    pub fn pointer_up(&mut self, point: Point, modifiers: DesktopModifiers) -> Result<(), String> {
        self.session.pointer_up(world_point(point), modifiers.into()).map_err(|e| e.to_string())
    }
}
```

In `panels.rs`, replace the placeholder inspector copy with live controls for:
- selected symbol reference
- selected symbol description
- selected wire label
- sheet name
- grid enabled
- grid spacing

Keep the native UI minimal; the important part is that every control calls the
shared session, not desktop-only mutation code.

- [ ] **Step 4: Re-run the desktop tests**

Run:

```bash
cargo test -p athena-desktop desktop_authoring -- --nocapture
```

Expected: PASS for directional selection, wire vertex drag, and inspector edit
coverage.

- [ ] **Step 5: Commit**

```bash
git add rust/crates/desktop
git commit -m "feat: wire desktop shell to shared session"
```

### Task 5: Refactor the WASM/web shell onto the shared session and add browser smoke coverage

**Files:**
- Modify: `rust/crates/web-core/src/lib.rs`
- Modify: `rust/crates/web-core/tests/web_editor_contracts.rs`
- Modify: `web/index.html`
- Modify: `web/bootstrap.js`
- Modify: `web/tests/authoring-mvp.test.js`

- [ ] **Step 1: Write the failing web-core and browser tests**

Extend `rust/crates/web-core/tests/web_editor_contracts.rs` with:

```rust
fn fixture_place_two_symbols_and_wire(editor: &mut WebEditorCore) {
    editor.begin_placement_by_name("Resistor").unwrap();
    editor.pointer_click(100, 80).unwrap();
    editor.begin_placement_by_name("Resistor").unwrap();
    editor.pointer_click(220, 120).unwrap();
    editor.apply_command_json(
        &serde_json::to_string(&athena_editor::EditorCommand::CreateWire {
            sheet_id: editor.active_sheet_id(),
            wire: athena_domain::Wire::new(
                athena_domain::WireEndpoint::Terminal(editor.terminal_ids()[0]),
                athena_domain::WireEndpoint::Terminal(editor.terminal_ids()[2]),
                vec![Point::new(120, 80), Point::new(160, 80), Point::new(160, 120), Point::new(200, 120)],
            ),
        }).unwrap(),
    ).unwrap();
}

fn fixture_web_editor_with_selected_wire() -> WebEditorCore {
    let mut editor = WebEditorCore::new("Browser project");
    fixture_place_two_symbols_and_wire(&mut editor);
    editor.select_wire_for_test(0).unwrap();
    editor
}

#[test]
fn web_controller_exposes_selection_and_property_editing() {
    let mut editor = WebEditorCore::new("Browser project");
    fixture_place_two_symbols_and_wire(&mut editor);
    editor.pointer_down(80, 60, false, false).unwrap();
    editor.pointer_move(260, 120, false, false).unwrap();
    editor.pointer_up(260, 120, false, false).unwrap();
    assert!(editor.render_active_sheet().unwrap().contains("MarqueeRect") || editor.selection_count() >= 1);
}

#[test]
fn web_controller_moves_wire_vertex_and_serializes_scene() {
    let mut editor = fixture_web_editor_with_selected_wire();
    editor.drag_wire_vertex_for_test(1, 140, 80, 140, 120).unwrap();
    assert!(editor.render_active_sheet().unwrap().contains("WireVertexHandle"));
}
```

Extend `web/tests/authoring-mvp.test.js` with:

```javascript
test("directional marquee and wire vertex drag work in the browser shell", async ({ page }) => {
  await page.goto("http://127.0.0.1:8080/");
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 140, y: 120 } });
  await page.getByRole("button", { name: "Resistor" }).click();
  await page.locator("#schematic-canvas").click({ position: { x: 240, y: 120 } });
  await page.locator("#schematic-canvas").dragTo(page.locator("#schematic-canvas"), {
    sourcePosition: { x: 80, y: 80 },
    targetPosition: { x: 280, y: 160 },
  });
  await expect(page.locator("#footer-status")).toContainText("interactive regions");
});
```

- [ ] **Step 2: Run the targeted tests to verify they fail**

Run:

```bash
cargo test -p athena-web-core web_editor_contracts -- --nocapture
node --test web/tests/authoring-mvp.test.js
```

Expected: compile/runtime failures for missing drag-aware web APIs.

- [ ] **Step 3: Replace web-core shell logic with the shared session**

Refactor `WebEditorCore` to wrap `EditorSession` and expose explicit pointer
and property APIs:

```rust
pub fn pointer_down(&mut self, x: i64, y: i64, shift: bool, command: bool) -> Result<(), String>;
pub fn pointer_move(&mut self, x: i64, y: i64, shift: bool, command: bool) -> Result<(), String>;
pub fn pointer_up(&mut self, x: i64, y: i64, shift: bool, command: bool) -> Result<(), String>;
pub fn update_selected_symbol_reference(&mut self, value: String) -> Result<(), String>;
pub fn update_selected_wire_label(&mut self, value: String) -> Result<(), String>;
pub fn update_sheet_grid(&mut self, enabled: bool, spacing: i64) -> Result<(), String>;
```

Update `web/index.html` to add:
- undo / redo / wire buttons
- sheet name input
- grid enabled checkbox
- grid spacing input
- selected symbol reference input
- selected symbol description input
- selected wire label input

Update `web/bootstrap.js` to:
- drive pointer down/move/up instead of click-only interaction
- render wire vertex and marquee overlays from the shared scene JSON
- wire inspector inputs to the new WASM methods
- map keyboard shortcuts for Delete, Esc, `R`, `M`, `Ctrl/Cmd+Z`, and
  `Ctrl/Cmd+Shift+Z`

- [ ] **Step 4: Re-run web-core and browser tests**

Run:

```bash
cargo test -p athena-web-core web_editor_contracts -- --nocapture
node --check web/bootstrap.js
```

Then run the browser smoke test with the local static host used by the repo.
Expected: the Rust tests pass, `web/bootstrap.js` parses cleanly, and the
browser smoke test passes once the host is running.

- [ ] **Step 5: Commit**

```bash
git add rust/crates/web-core web/index.html web/bootstrap.js web/tests/authoring-mvp.test.js
git commit -m "feat: wire browser shell to shared session"
```

### Task 6: Full verification pass and milestone closeout

**Files:**
- Modify: `docs/superpowers/plans/2026-08-13-m002-single-sheet-editing-parity.md`
- Create: `docs/superpowers/verification/2026-08-13-m002-single-sheet-editing-parity.md`

- [ ] **Step 1: Run the Rust verification suite**

Run:

```bash
cargo fmt --all -- --check
cargo clippy --workspace --all-targets -- -D warnings
cargo test --workspace
```

Expected: all commands exit `0`.

- [ ] **Step 2: Run the WASM/browser build checks**

Run:

```bash
cargo build -p athena-web-core --target wasm32-unknown-unknown --release
wasm-pack build crates/web-core --target web --out-dir ../../web/pkg --release
node --check web/bootstrap.js
```

Expected: all commands exit `0`.

- [ ] **Step 3: Run the desktop and browser smoke checks**

Run:

```bash
cargo test -p athena-desktop desktop_authoring -- --nocapture
cargo test -p athena-web-core web_editor_contracts -- --nocapture
```

If a local browser host is running on the agreed port, also run the Playwright
smoke test for `web/tests/authoring-mvp.test.js`. Record the exact URL used in
the verification note.

- [ ] **Step 4: Write the verification note**

Create `docs/superpowers/verification/2026-08-13-m002-single-sheet-editing-parity.md`
with:
- command list
- pass/fail status
- any residual risk
- explicit note on whether desktop native launch and browser smoke were both run

- [ ] **Step 5: Mark completed plan items and commit**

Update only the completed checkboxes in this plan after the verification note
exists and the commands above have been run successfully.

```bash
git add docs/superpowers/plans/2026-08-13-m002-single-sheet-editing-parity.md docs/superpowers/verification/2026-08-13-m002-single-sheet-editing-parity.md
git commit -m "docs: close out m002 verification"
```
