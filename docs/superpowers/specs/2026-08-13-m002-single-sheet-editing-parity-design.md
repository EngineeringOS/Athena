# Athena M002 Single-Sheet Editing Parity Design

## Status

Proposed on 2026-08-13 after direct source review of QElectroTech and
Graphite. This document is the design baseline for the next implementation
plan and intentionally records evidence for each major design choice.

## Goal

M002 expands the Rust authoring foundation into a single-sheet editing slice
that behaves like an electrical schematic editor rather than a generic canvas.
The target is interaction parity for the core sheet workflow:

- select symbols and wires
- drag selected content
- marquee select
- edit orthogonal wires through visible handles
- update a minimal property set
- undo and redo every persistent edit
- keep desktop and web behavior aligned through one Rust core

This milestone stays inside one sheet. Multi-sheet workflows, reports,
cross-reference systems, and library authoring remain outside M002.

## Evidence Baseline

This design is anchored in direct source observations, not memory:

- QElectroTech scene/tool dispatch:
  [diagrameventinterface.h](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/diagramevent/diagrameventinterface.h:33)
- QElectroTech view-level free selection and edit entry:
  [diagramview.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/diagramview.cpp:448),
  [diagramview.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/diagramview.cpp:1159)
- QElectroTech conductor selection, handlers, snapping, and profile save:
  [conductor.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:697),
  [conductor.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:738),
  [conductor.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:799),
  [conductor.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:822),
  [conductor.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:850)
- QElectroTech conductor segment movement constraints:
  [conductorsegment.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/conductorsegment.cpp:41)
- QElectroTech conductor property coverage and undo path:
  [conductorpropertieswidget.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/ui/conductorpropertieswidget.cpp:72),
  [diagramview.cpp](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/qelectrotech-source-mirror/sources/diagramview.cpp:121)
- Graphite select-tool message flow and directional selection:
  [select_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/select_tool.rs:105),
  [select_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/select_tool.rs:545),
  [select_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/select_tool.rs:1058),
  [select_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/select_tool.rs:1523)
- Graphite transform separation:
  [transform_layer_message_handler.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/transform_layer/transform_layer_message_handler.rs:72)
- Graphite overlay provider model:
  [overlays_message_handler.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/portfolio/document/overlays/overlays_message_handler.rs:8)
- Graphite path selection and overlay behavior:
  [path_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/path_tool.rs:517),
  [path_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/path_tool.rs:626),
  [path_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/path_tool.rs:1909),
  [path_tool.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/tool/tool_messages/path_tool.rs:2320),
  [utility_functions.rs](/abs/path/D:/Aaron/workspace/projects/2026/eos/Athena/reference/Graphite/editor/src/messages/portfolio/document/overlays/utility_functions.rs:33)

## Reference Findings

### QElectroTech Behavior We Must Preserve

QElectroTech splits raw scene behavior from modal tool behavior. The diagram
routes mouse and key events into a DiagramEventInterface, and the interface
consumes the event by marking it accepted. That is the functional model we
need for symbol placement, wire placement, and future tool modes, even though
the Rust implementation will not mirror Qt classes directly.

QElectroTech also keeps some editing behavior on the item and view layer:

- conductor double click opens property editing
- selecting a conductor raises its z-order and creates visible blue handlers
- handlers sit at the middle of internal segments, not at terminal extension
  segments
- dragging a handler snaps to grid before mutating the conductor path
- releasing a handler saves the changed conductor profile into undo history
- free polygon selection is view-owned and rendered as a translucent overlay

The conductor geometry itself is not a freeform polyline. It is an ordered
chain of orthogonal segments with movement rules that preserve coherence and
minimum lengths around terminal-adjacent static segments. The evidence is in
ConductorSegment::canMove* and moveX/moveY, which bound edits instead of
allowing arbitrary collapse.

QElectroTech properties for conductors are broad, but the property widget
shows that the editable model is still structured data, not ad hoc labels:
formula, text, function, cable, bus, tension/protocol, wire color, wire
section, text settings, line settings, and visibility toggles. M002 will take
only the minimum subset already agreed with the user, but it will keep the
same principle: typed properties on the document model, changed through undo.

### Graphite Architecture We Should Borrow

Graphite demonstrates three patterns that map well to Athena:

1. tool behavior is driven by explicit message enums and FSM state
2. transformation logic is separated from document storage and from overlay
   rendering
3. overlays are rendered by dedicated providers instead of being folded into
   the core scene model

The Select tool and Path tool both compute directional marquee behavior from
drag direction: rightward means enclosed, leftward means touched. That aligns
with the user-approved M002 selection rule and gives a concrete interaction
precedent.

Graphite also separates transient editor state from persistent document state.
Selection history can step backward and forward at the document-message level,
while overlays are redrawn separately. For Athena, this supports a clean split
between persistent schematic commands and transient hover, snap preview,
marquee box, and visible wire vertices.

Path overlays in Graphite are especially relevant. Selected segments are
derived from selected anchors and handles, then rendered as highlighted
geometry plus manipulator anchors/handles. Athena does not need Bezier logic,
but it should adopt the same principle for wire editing: wire selection drives
overlay state, and overlay state visualizes endpoints, vertices, hover targets,
and highlighted segments without polluting the saved model.

## M002 Athena Design

### Interaction Model

Athena M002 uses an interaction-first editor core with explicit transient
states:

- Idle
- MarqueeSelecting
- DraggingSelection
- EditingWireVertex
- ReconnectingWireEndpoint
- EditingProperties
- ToolPlacementTransient

This is the direct Rust analogue of the QElectroTech event-interface split and
Graphite tool FSM approach. The shell forwards pointer and keyboard events into
core messages. The core owns the active interaction state and returns:

- persistent commands to apply
- transient overlay updates
- cursor and hint updates for the shell

### Persistent vs Transient State

Persistent single-sheet document state in M002:

- sheet metadata: name, grid enabled, grid spacing
- symbol instances with stable IDs, placement transform, and user fields
- wires with stable IDs, terminal endpoints, and orthogonal route vertices
- wire labels and minimal editable wire metadata

Transient editor state in M002:

- current selection
- hover target
- marquee rectangle and drag direction
- snap candidate and snap reason
- visible wire edit handles
- active drag baseline and preview displacement
- property inspector draft values before commit

Selection, hover, marquee, and overlays are not serialized into the schematic
file. That follows the Graphite separation between document messages and
overlay/tool state, and it avoids contaminating saved data with UI residue.

### Command Model

Every persistent edit in M002 must flow through typed commands and history.
The minimum command surface is:

- SelectObjects
- ClearSelection
- MoveSelection
- RotateSelection90
- MirrorSelection
- DeleteSelection
- UpdateSymbolProperties
- UpdateWireProperties
- UpdateSheetProperties
- ReconnectWireEndpoint
- InsertWireVertex
- MoveWireVertex
- DeleteWireVertex

MoveSelection, ReconnectWireEndpoint, InsertWireVertex, MoveWireVertex,
and DeleteWireVertex are the critical parity commands because they cover the
wire editing behavior proven in QElectroTech. Property updates must also be
undoable, matching the observed QPropertyUndoCommand pattern in QElectroTech.

### Selection Rules

These rules are already user-approved and are reinforced by direct reference
evidence:

- empty click clears selection
- Shift extends or toggles selection
- left-to-right marquee means contain / enclosed
- right-to-left marquee means intersect / touched
- selected symbols render with a blue outline and handles
- selected wires render as full highlighted paths with visible vertices

The directional marquee rule comes directly from Graphite Select and Path tool
logic. The visible conductor handles and full-wire selection behavior come from
QElectroTech conductor selection and handler generation.

### Hit Priority

M002 will use this hit priority:

1. wire endpoint handle
2. wire vertex
3. terminal snap target
4. wire segment
5. symbol body
6. annotation text
7. empty sheet

This is a design consequence of the reference code, not a memory guess:
QElectroTech gives conductor handlers first-class event interception through
sceneEventFilter, and Graphite path editing similarly distinguishes selected
anchors, handles, and segments as separate interactive overlay targets.

### Wire Geometry Model

M002 wires are stored as:

- source terminal reference
- target terminal reference
- ordered interior route vertices

The rendered path is always orthogonal. Terminal extension stubs are derived,
not authored as explicit editable user vertices. That follows the QElectroTech
conductor model where handlers exist only for the middle segments after the
first and last extension segments are removed from the handler list.

Movement rules:

- all edited points snap to the grid
- terminal snap wins over plain grid snap
- moving a wire vertex preserves orthogonal topology
- reconnecting an endpoint rebinds the endpoint to a terminal and recomputes
  adjacent route structure
- deleting a vertex normalizes the route and removes redundant bends
- editing cannot collapse static terminal-adjacent geometry into invalid shape

This is the closest clean-room translation of QElectroTech constrained segment
movement without copying its class graph.

### Property Editing Slice

M002 property scope is intentionally minimal:

- symbol: reference, label/description
- wire: label plus endpoint and route summary
- sheet: name, grid enabled, grid spacing

This scope was user-approved already. The reference evidence affects how it is
implemented, not whether it exists:

- QElectroTech shows conductor properties as a typed form, not inline string
  hacks
- QElectroTech pushes property changes through undo
- Graphite separates overlay and selection state from property-bearing document
  state

Therefore Athena will expose a small typed inspector now, with the document
schema ready to grow into fuller electrical metadata later.

### Desktop and Web Alignment

Desktop and web remain one behavior slice:

- one Rust editor core
- one Rust document model
- one Rust command and history system
- one Rust hit-test and overlay derivation path
- thin GPUI desktop shell
- thin HTML/WASM web shell

The shells may differ in platform plumbing, but not in selection, drag, snap,
wire editing, or undo semantics. That is consistent with the M001 baseline and
with Graphite native/web overlay split, where the provider model is shared
while the rendering backend differs.

## Acceptance Criteria

M002 is complete when all of the following are true:

- symbols can be selected, moved, rotated, mirrored, and deleted on a sheet
- wires can be selected as full objects
- selected wires show visible editable vertices and endpoint affordances
- a user can drag an existing wire vertex and the route remains orthogonal
- a user can insert a vertex on a wire segment
- a user can delete a vertex and the route normalizes cleanly
- a user can reconnect a wire endpoint to another terminal
- marquee selection follows enclosed/intersect directional rules
- all persistent edits listed above participate in undo and redo
- the same interaction behavior works in desktop and web shells

## Implementation Boundary for the Next Plan

The next implementation plan should stay tightly scoped:

- extend core hit testing to return symbol, terminal, wire segment, vertex, and
  endpoint-handle hits
- add transient interaction state and overlay derivation for selection and wire
  editing
- add the minimal command set for selection movement, wire editing, and
  property edits
- bind the desktop and web shells to the new core interaction messages
- verify behavior through focused core tests first, then shell smoke checks

The next plan should not include multi-sheet orchestration, symbol-library
authoring UX, reports, printing, or cloud sync.
