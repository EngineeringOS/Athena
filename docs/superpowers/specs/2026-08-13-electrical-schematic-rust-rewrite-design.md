# Athena Electrical Schematic Rewrite Design

## Status

Approved through collaborative brainstorming on 2026-08-13. This document is
the design baseline for implementation planning; it is not an implementation
plan.

## Intent and Scope

Athena is a clean-room Rust rewrite of the electrical-schematic product
capabilities documented by QElectroTech. QElectroTech source and documentation
are behavior and workflow references only. The rewrite does not preserve Qt/C++
architecture, its UI, its classes, or its XML formats.

The product is electrical schematics only. Generic diagramming is out of scope.
The complete QElectroTech electrical feature inventory remains the parity target:
projects, sheets/folios, symbol collections and editor, symbol placement,
terminals and conductors, routing and snapping, cross-references, numbering,
title blocks, annotations, reports, search/replace, preferences,
localization, import/export, printing, and multi-document workflows.

The first usable milestone is the core authoring loop: create a project and
sheets, place symbols, draw and edit connections, select and transform objects,
pan/zoom/snap, undo/redo, save locally, and reopen in both desktop and browser
shells.

Cloud delivery is staged over one architecture: local-first offline editing;
authenticated cloud projects and manual sharing; then realtime collaboration.

## Architecture

Use a Rust workspace with a platform-neutral core compiled for native and
`wasm32-unknown-unknown`. The core must not depend on UI, filesystem, browser,
or cloud APIs.

Suggested boundaries:

```text
crates/domain/       project, sheet, symbol, terminal, wire, annotation
crates/geometry/     coordinates, transforms, snapping, routing
crates/editor/       tools, selection, commands, undo/redo, validation
crates/render/       platform-neutral scene and hit-test projections
crates/format/       versioned serde schema and migrations
crates/library/      symbol collections, indexing, and search
crates/sync/         operation envelopes, revisions, presence contracts
crates/desktop/      GPUI application and native services
crates/web-core/     wasm-bindgen entry points
web/                 thin HTML/JavaScript browser host
```

The desktop shell uses GPUI and may use the local `gpui-component` checkout.
The browser shell is a separate thin HTML/JavaScript host for loading WASM,
mapping browser input, hosting the rendering surface, and adapting browser
storage/download/print APIs. Schematic behavior and application state remain in
Rust/WASM; the web layer must not become a second application.

Graphite is an architectural reference for separating document/editor logic,
tools, viewport/canvas, rendering, and native/web wrappers. It is not a runtime
dependency.

## Domain Model

The canonical saved document is an electrical project:

```text
Project
  metadata, settings, numbering rules
  symbol libraries
  sheets
    sheet metadata and title block
    symbol instances
    wires and junctions
    text, labels, annotations
    graphic construction items
  cross-reference relationships
  report definitions
```

Definitions are reusable library assets. Instances have stable IDs, transforms,
field values, and a definition reference. Terminals are typed connection points
owned by instances. Wires are routed polylines with electrical properties.
Junctions are explicit graph nodes. Cross-references use stable IDs and are
derived from semantic relationships, not text matching. Title-block/page data
is separate from the electrical graph.

The saved project is separate from editor presentation state (active sheet,
selection, active tool, viewport, hover, guides, and overlays). Derived indexes
such as connectivity, nomenclature, references, and search results are rebuilt
from canonical state or updated transactionally; they are never authoritative.

## Editing and Rendering

All mutations use typed, deterministic, serializable commands, including
`PlaceSymbol`, `MoveItems`, `CreateWire`, `SplitWire`, `SetFieldValue`,
`DeleteItems`, and `ApplySheetSettings`. Commands validate against the current
project and apply atomically, returning structured errors for invalid state.

The shared command pipeline is:

```text
desktop/web input -> tool -> typed command -> validate -> apply atomically
  -> history/inverse -> derived indexes -> snapshot + render scene -> optional sync
```

The editor provides select, place-symbol, wire, junction, text, pan, and measure
tools; additive/range/marquee selection; direct handles; context-aware grid,
terminal, wire, alignment, and orthogonal snapping; inline inspector editing;
undoable destructive actions; and direct placement previews.

The renderer consumes a platform-neutral layered scene:

1. page, grid, and rulers
2. wires and junctions
3. symbol graphics
4. fields and annotations
5. selection, handles, guides, and validation overlays

Desktop and web expose the same tools, shortcuts, and command semantics, with
only platform conventions differing.

## Storage and Collaboration

Use a versioned serde-backed JSON document schema with explicit migrations.
JSON is the canonical interchange/debug representation; compressed snapshots or
other transport encodings may be added later without changing the domain model.

Local-first editing applies commands immediately, writes snapshots atomically,
keeps unsynced commands in an outbox, and reopens the last valid snapshot plus
pending history. A failed write never replaces the last known-good snapshot.

Cloud command envelopes include project/sheet IDs, operation ID, author/session
ID, base revision, typed payload, timestamp/ordering metadata, and command/schema
versions. The server orders accepted commands. Clients may apply local commands
optimistically; rejected or conflicting commands are rebased through the core
and surfaced as recoverable conflicts. Periodic snapshots bound replay cost.

## Delivery Phases

1. Foundation: Rust workspace, domain model, versioned schema, command engine,
   geometry, scene projection, and deterministic tests.
2. Authoring MVP: projects/sheets, symbol library and placement, wires,
   selection/transforms, pan/zoom/snap, undo/redo, local persistence, native
   GPUI shell, and thin WASM browser shell.
3. Electrical parity: numbering, terminals, cross-references, title blocks,
   element editor, collections, reports, search/replace, export/print,
   preferences, localization, and multi-document workflows.
4. Cloud: authentication, project persistence, permissions, sharing,
   revisions/history, and sync outbox.
5. Realtime: ordered command transport, presence, reconnect/offline merge, and
   conflict UX.

## Non-goals

- QElectroTech file/XML compatibility
- Qt or C++ reuse
- Generic diagramming as a product mode
- Extending or integrating the legacy Theia/Electron code under `ide/`
- Moving schematic state or behavior into browser JavaScript
- Realtime CRDT semantics before the command/snapshot model is stable

## Acceptance Criteria for the Authoring MVP

- A project with multiple sheets can be created, named, saved, reopened, and
  switched without losing state.
- A library symbol can be previewed, placed, moved, rotated, mirrored,
  duplicated, edited, and deleted.
- Wires can be created between terminals, routed orthogonally, edited by handles,
  split, joined, and deleted with explicit junction behavior.
- Grid/terminal snapping, pan, zoom, marquee selection, and transforms behave
  deterministically in native and WASM test harnesses.
- Every mutation is undoable and redoable through the same serialized command
  pipeline.
- Native GPUI and browser shells render the same scene projection and expose the
  same core interaction semantics.
- Local persistence survives a failed write without corrupting the previous
  snapshot.
