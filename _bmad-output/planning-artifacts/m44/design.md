# M44 Design: Asset-First Editable Engineering Canvas

**Milestone:** M44  
**Status:** Design approved in brainstorming; ready for user spec review  
**Scope:** Rendering quality, editable canvas operations, and source round-trip  
**Date:** 2026-08-07

## 1. Outcome

M44 makes Athena's engineering canvas trustworthy for daily editing:

- Real IEC and vendor element geometry replaces placeholder rectangles.
- Ports, anchors, labels, and routes remain traceable to engineering meaning.
- Canvas edits become validated source patches, not hidden scene mutations.
- The same canonical scene drives Theia, SVG, and PNG output.
- Styles are externally configurable without changing engineering semantics.

M44 is a rendering and edit-authority milestone. It does not expand into a complete semantic workspace, AI copilot, macro system, QET importer, PDF/report system, or WebGPU renderer.

## 2. Authority Chain

```text
Athena source
  -> Engineering model
  -> Projection and Sheet companions
  -> AthenaDiagramScene
  -> Renderer backend
  -> Theia canvas / SVG / PNG
```

The canvas never becomes engineering authority. A submitted edit follows:

```text
Canvas gesture
  -> AthenaEditOperation
  -> server-side operation handler
  -> validated source patch
  -> compile and validate
  -> atomic new scene publication
```

The frontend may show a transient drag preview. It must not persist a preview or infer engineering meaning.

## 3. EPLAN-Aligned Object Separation

M44 keeps the distinctions established by the EPLAN study:

```text
Device        engineering entity
Function      engineering role/capability
Part          replaceable procurement/physical implementation
Symbol        graphical representation
Connection    engineering relationship
Route         representation of a relationship on one Sheet
Sheet         projection container
```

An engineering entity may bind to a `partRef` and a `symbolRef`. The symbol does not define device identity, part choice, relationship meaning, or rules.

## 4. Library and Asset Contract

IEC and vendor elements are libraries, not copied SVG files. M44 extends Athena's existing `ENGINEERING` and `REPRESENTATION` package model, repository dependency resolution, `athena.lock`, package snapshots, and digest admission. It does not introduce a separate Maven/npm-compatible package manager.

A project declares a library dependency and resolves a locked package version/digest. Source and scene references identify a package item, not an arbitrary filesystem path:

```text
packageId + itemId + lockedVersion + packageDigest
```

The runtime may materialize locked bytes in its cache/snapshot, but the project remains a consumer of the library. Reusing one symbol in ten projects does not duplicate its SVG into each project. Missing, incompatible, stale, or digest-mismatched dependencies block compilation with a package diagnostic.

Library packages may publish these item kinds:

- `SymbolDefinition`: one graphical symbol and its geometry metadata.
- `SymbolMacroDefinition`: a reusable symbol composition with exposed connection points and parameters.
- `PartDefinition`: procurement/physical implementation metadata and representation bindings.
- `ElementMacroDefinition`: a reusable element set that expands into normal engineering/projection facts.

Engineering metadata may live in an `ENGINEERING` package while symbol resources live in a `REPRESENTATION` package; package dependencies connect the two. A library may publish a coordinated pair so users consume one product identity while the lock records both package artifacts.

The governed representation item owns graphical geometry and stable geometry metadata:

- SVG bytes and digest.
- View box and scale profile.
- Center/hotspot and placement anchors.
- Stable connection-point keys and orientation.
- Label zones and default text anchors.
- Optional hit regions.
- Standard/vendor provenance, version, and license.

Each exposed port metadata record carries at least:

- stable connection-point key;
- `direction` (`IN`, `OUT`, or `BIDIRECTIONAL`);
- engineering domain (`ELECTRICAL`, `MECHANICAL`, `SIGNAL`, `COMMUNICATION`, or library-defined domain);
- signal/flow kind (`POWER`, `CURRENT`, `VOLTAGE`, `DIGITAL`, `ANALOG`, `MATERIAL`, or library-defined kind);
- geometry anchor and orientation.

These are library contracts, not complete project relationships. Semantic ports refer to connection-point keys; semantic facts do not contain SVG coordinates. The compiler resolves the key against the locked library metadata, checks compatibility, and produces scene coordinates.

An asset without geometry metadata may be decorative only. A connectable symbol requires both SVG and matching metadata. Digest or pairing mismatch prevents scene publication. The canonical scene asset reference points to the resolved library item and digest, so SVG export and Theia rendering consume identical locked bytes.

M44 does not import `reference/elements` or `reference/elements_contrib` at runtime. Those directories are research references used to select convenient example assets. A milestone example may manually package a small IEC/vendor set as a governed library, retaining provenance/license records. QElectroTech `.html` files are XML definitions and are not a runtime dependency; if used while preparing an example, they are normalized into Athena library metadata and then discarded from the active product path.

## 5. Canonical Scene

`AthenaDiagramScene` remains the only renderer-neutral presentation model. An occurrence contains:

- `semanticId` and `occurrenceId`.
- locked library item reference for `symbolRef` and optional `partRef` trace.
- Transform and Sheet placement anchor.
- Resolved ports and connection-point references.
- Resolved label bounds/zones.
- Style reference.
- `traceId` back to source and projection facts.

The scene also contains routes, styles, snap-grid facts, page bounds, decorations, asset references, and diagnostics. The scene compiler publishes no partial scene after an upstream semantic, projection, spatial, asset, or contract error.

## 6. Style Authority

Style is independent of engineering meaning and SVG geometry. The cascade is:

```text
Asset defaults
  -> Sheet/project style defaults
  -> role/route style
  -> occurrence override
  -> transient selection overlay
```

Persisted project styling belongs in the accompanying `*.sheet.style.athena` document. IDE-only overrides remain session state until the user explicitly solidifies them. Supported style concerns include stroke/fill colors, width, dash, cap/join, label typography, label placement, route markers, and port display. Style changes must not alter entity identity, capability, port direction, or relationship semantics.

## 7. Edit Operations and Source Round-Trip

All canvas editing enters through typed operations. Initial operation families:

```text
MoveOccurrence
AlignOccurrences
DistributeOccurrences
SnapOccurrenceToGrid
ReconnectPort
ChangeSymbol
BindPart
SetStyle
```

Each operation carries target identities, source revision/precondition, and trace context. The handler validates authority and computes the smallest source patch:

- placement, alignment, distribution, and snapping update the Sheet companion;
- style updates update the style companion;
- symbol representation changes update the representation binding to a locked library item;
- part replacement updates the part binding to a locked engineering-library item and triggers engineering validation;
- relationship/port changes update semantic source and must pass semantic checks.

The server applies a patch only after validation succeeds. A stale revision, invalid port direction, missing target, or failed compilation produces a human-readable diagnostic and no file mutation. Undo/Redo records operations/source patches, not Konva node positions or pointer-move events. One drag or align command produces one history interaction.

## 8. Theia Boundary

Theia remains the application shell. The frontend owns view concerns only:

- pointer, keyboard, selection, viewport, and transient interaction;
- rendering backend lifecycle;
- toolbar and inspector presentation;
- semantic selection synchronization.

The frontend does not resolve engineering relationships, calculate legal ports, assemble source text, choose a part, or run validation. Canvas selection carries `semanticId`, `occurrenceId`, and `traceId` so future Semantic Navigator and Inspector views can share selection without making M44 a full navigator milestone.

The current Athena LSP/kernel contracts remain the authority. GLSP and Graphite are reference material for operation handlers, alignment, snapping, history, caching, and invalidation; they are not runtime dependencies for M44.

## 9. Renderer Backend

M44 introduces a renderer-backend boundary but ships one interactive backend:

```text
AthenaDiagramScene -> RendererBackend -> KonvaCanvas2DBackend
```

The backend uses cached SVG-derived images for symbol geometry, separate route/label layers, a spatial index for visible occurrences, and interaction overlays for hit targets, ports, selection, and drag handles. Pan/zoom and transient transforms must not recompile the semantic model.

SVG and PNG export consume the same scene and asset bytes. Export is an oracle for parity, not a second interactive renderer. If a future WebGL/WebGPU backend becomes necessary, it replaces the backend implementation without changing source, operation, scene, or trace contracts.

## 10. Failure Handling

M44 must reject publication when any of these is true:

- semantic or relationship validation has an error;
- a Sheet companion is absent or spatially invalid;
- a declared library dependency cannot be resolved from the repository lock/cache;
- a symbol asset or metadata pair is missing/mismatched;
- a route endpoint cannot resolve to a valid port;
- a source revision precondition is stale;
- a source patch does not compile.

Diagnostics name the exact subject, problem, and correction in plain engineering language. No partial scene or half-applied source edit is visible.

## 11. Verification and Evidence

Acceptance proof must cover the full round trip:

```text
source -> scene -> render -> operation -> source patch -> recompile -> scene
```

Required evidence:

- asset contract tests for SVG/metadata pairing, ports, anchors, labels, digest, and provenance;
- package tests for library dependency resolution, lock/digest admission, item lookup, and representation/engineering package pairing;
- scene contract tests for identity, trace, route endpoints, and no-partial-publication;
- renderer tests for real SVG geometry, label readability, ports, style cascade, viewport culling, and SVG/PNG parity;
- operation tests for move, align, distribute, snap, reconnect, symbol change, part binding, style, stale revision, and atomic failure;
- history tests proving operation-level Undo/Redo;
- active M44 example project and Theia screenshots under `_bmad-output/implementation-artifacts/m44/`;
- source-set hygiene and encoding audits;
- sequential Gradle verification and rebuilt Theia frontend before final E2E.

M44 performance proof uses realistic SVG symbols and routes plus a separate scale fixture. It does not promise 100,000 complex SVG elements at 60 FPS. It must prove that normal engineering documents remain responsive and that viewport/caching boundaries prevent full-scene redraw on pan, zoom, or local edits.

## 12. Delivery Epics

```text
Epic 1  Governed asset and canonical scene contract
Epic 2  High-quality SVG renderer and style cascade
Epic 3  Operation handlers and source round-trip editing
Epic 4  Theia integration, regression proof, and milestone closure
```

Every story will be created and implemented through the BMad story skills, in order, under `_bmad-output/implementation-artifacts/m44/`. No legacy compatibility path or retired renderer behavior is preserved unless it remains required by these M44 contracts.

## 13. Explicit Non-Goals

- No QElectroTech runtime dependency or importer.
- No wholesale copy of reference asset catalogs.
- No semantic macro compatibility layer.
- No second scene model.
- No frontend business-logic replica of kernel rules.
- No premature WebGPU migration.
- No full M14-style semantic navigator implementation.

## 14. Design Decision

M44 is approved as an asset-first, source-round-trippable, renderer-backend-bounded engineering canvas. The design is intentionally narrower than a complete EngineeringOS workspace while preserving the contracts required for later semantic navigation, inspector integration, and alternate render backends.
