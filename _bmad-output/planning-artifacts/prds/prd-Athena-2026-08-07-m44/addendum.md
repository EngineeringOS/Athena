# M44 PRD Addendum

This addendum preserves technical decisions and rejected alternatives that should inform architecture without bloating the product requirements.

## Technical Authority

M44 reuses Athena's existing `ENGINEERING` / `REPRESENTATION` package model, repository dependency resolver, `athena.yaml`, `athena.lock`, package snapshots, and digest admission. It does not introduce a Maven/npm package manager. A library item is addressed by package identity, item identity, version, and admitted digest. Runtime materialization is cache/snapshot behavior; project source remains a library consumer.

Engineering metadata and representation resources may be published as coordinated package dependencies.
A `PartDefinition` may bind to one or more `RepresentationBinding` targets. A Function or Entity binds
to representation choice through `RepresentationBinding`; a Sheet publishes concrete
`RepresentationInstance` occurrences. `SymbolMacroDefinition` and `ElementMacroDefinition` are
representation reuse categories with stable references; they must not create engineering relationships,
engineering validation facts, or engineering solution Patterns.

## EPLAN Mapping

```text
EPLAN Device       -> Athena Engineering Entity
EPLAN Function     -> Athena Function / capability role
EPLAN Part         -> Athena Part implementation
EPLAN Symbol       -> Athena Symbol library representation
EPLAN Connection   -> Athena Relationship
EPLAN Page         -> Athena Sheet/Projection context
EPLAN Macro        -> future library Pattern/Macro item
```

This mapping keeps Symbol geometry from owning Device identity, Part selection, Function semantics, or Relationship truth. Route points remain derived representation facts for one Sheet.

## Library Port Metadata

Library metadata describes a port contract, not a complete project relationship. Minimum fields are:

```text
connectionPointKey
direction: IN | OUT | BIDIRECTIONAL
domain: ELECTRICAL | MECHANICAL | SIGNAL | COMMUNICATION | library-defined
flowKind: POWER | CURRENT | VOLTAGE | DIGITAL | ANALOG | MATERIAL | library-defined
geometryAnchor
orientation
```

Engineering source remains authoritative for Port identity, direction, domain, flow kind, and all
semantic properties. Library metadata owns keyed geometry, orientation, and compatibility envelope
only. The compiler resolves semantic port keys against the locked library item, checks compatibility,
and emits scene coordinates. Mismatch blocks publication with a plain diagnostic; library metadata
cannot mutate Engineering Ports. Project source does not store SVG coordinates.

Each Symbol item has exactly one canonical SVG resource and one UTF-8 `symbol.yaml` metadata
descriptor using schema `athena-symbol-v1`. The loader parses YAML into canonical JSON (sorted object
keys, explicit units, normalized numbers) for descriptor digesting. No alternate descriptor syntax or
compatibility parser is allowed.
Required fields are schema version, item identity, SVG digest, coordinate frame/units, center/hotspot,
anchors, label zones, and connection-point keys. Vendor extensions must be namespaced. Package trace
includes package identity, item identity, version, admitted digest, provenance, and license.

M44 semantic Relationships carry kind, endpoint identities, domain, flow kind, and direction. Route
geometry is only a Sheet projection. Terminal/Cable/Wire physical realization and report/BOM/
manufacturing projections are later milestones.

## Renderer Boundary

The one canonical flow is:

```text
Athena source
  -> Engineering/Projection/Spatial facts
  -> AthenaDiagramScene
  -> KonvaCanvas2DBackend
  -> Theia canvas
```

SVG and PNG export consume the same scene and locked asset bytes. Konva caches SVG-derived images, culls outside-viewport occurrences, and keeps interaction overlays separate from route/label/symbol paint. A future WebGL/WebGPU backend may replace Konva behind the backend contract; M44 does not ship both interactive engines.

## Edit and History Boundary

Typed operations include `MoveOccurrence`, `AlignOccurrences`, `DistributeOccurrences`, `SnapOccurrenceToGrid`, `ReconnectPort`, `ChangeSymbol`, `BindPart`, and `SetStyle`. Operations carry target identities and Source Revision preconditions. The server computes minimal source patches, validates, applies atomically, recompiles, and publishes a new scene. Undo/Redo records operations/source patches, never per-pointer-move scene state.

Operations are classified by authority:

```text
PresentationEditOperation
  MoveOccurrence
  AlignOccurrences
  DistributeOccurrences
  SnapOccurrenceToGrid
  SetStyle
  writes: *.sheet.athena / *.sheet.style.athena

RepresentationEditOperation
  ChangeSymbol
  writes: representation binding
  preserves: EngineeringEntity, Function, Relationship, Port identity

EngineeringEditOperation
  ReconnectPort
  BindPart
  writes: engineering source
  requires: Relationship validation, Capability validation, Flow validation
```

No operation category may share a shortcut path. Reconnect and Bind Part must enter the engineering
validation path. Change Symbol must not alter engineering identity. Presentation edits must not touch
Engineering Reality.

Placement edits target `*.sheet.athena`; persisted style edits target same-basename
`*.sheet.style.athena`; representation and Part binding edits target declared source/package binding
files; semantic Relationship edits target engineering source. Each operation declares its writable
file set. Coordinated Symbol/Part edits are one multi-file transaction.

Source Revision is compare-and-set over existing scene `inputRevision`, engineering-source digest,
Sheet digest, Style Companion digest or absent marker, `athena.lock` digest, admitted package/item
digests, compiler/schema version, and source-root identity. Server stages patches, validates staged
compile, then atomically publishes all files or rolls back all files. One accepted operation produces
one publication correlation id and new Source Revision. Undo/Redo stores operation-level source
patches; failed Redo remains available and emits stale/conflict diagnostic without mutation.

The Operation Journal is the authoritative history of accepted source transactions. Undo/Redo uses the
journal, not canvas events. Future AI/collaboration may read the journal, so entries must record operation
type, authority class, target identity, Source Revision, writable file set, accepted patch, publication
correlation id, and resulting Source Revision.

Style Companion discovery is deterministic: same-basename `*.sheet.style.athena` is optional; missing
means defaults only; multiple matches are an error. Style digest participates in scene identity and
Source Trace.

## Reference Asset Use

`reference/elements` and `reference/elements_contrib` are research-only QElectroTech mirrors. Some files with `.html` suffix contain XML `<definition>` data paired with an SVG of the same basename. M44 may use a small manually selected pair while preparing its example library, normalize metadata into an Athena package item, and retain provenance/license. No QElectroTech runtime parser or reference-directory dependency is part of M44.

M44 proof fixture is electrical-first with one cross-domain relationship and one Entity with multiple
Function/Symbol occurrences. Function/capability facts are consumed from M42; M44 does not select
Functions from a Symbol library. Structured `=Function +Location -Device` addressing is deferred;
typed identities remain mandatory.

M44 closure is one golden authoring loop: resolve library assets, compile Canonical Scene, move, align,
change symbol, reconnect valid port, reject invalid connection, bind/replace part, undo, redo, restart,
and reproduce same accepted identities and result.

## Deferred Decisions

- Remote library registry, publication, signatures, and discovery belong to a later package-platform milestone.
- Concrete normalized metadata encoding is fixed to UTF-8 `symbol.yaml` with `athena-symbol-v1`;
  canonical JSON digesting, units, and extension rules above are normative.
- Benchmark profile is Windows 11, 1920x1080, DPR 1, 60 Hz, pinned Electron/Chromium, 300 real
  occurrences; p95 pan/zoom frame time <=20 ms, p95 drag preview <=100 ms, heap growth <=20 MB over
  60 seconds after 3 seconds warm-up. Transcript records hardware, fonts, browser, and fixture digest.

## Rejected Alternatives

- **Direct Konva mutation:** rejected because scene state cannot become source authority.
- **Immediate Graphite/wgpu adoption:** rejected because M44 rendering quality and round-trip editing do not require a new graphics engine.
- **Second interactive SVG engine:** rejected because it duplicates scene and interaction authority.
- **Project-local SVG copying:** rejected because IEC/vendor elements are reusable Library Package items.
- **QElectroTech runtime parsing:** rejected because reference files are examples, not Athena product authority.
- **PNG byte-digest claims:** rejected because raster output depends on environment; M44 records pinned
  Electron/Chromium evidence and pixel tolerance while requiring exact canonical SVG bytes.
- **Macro as engineering intelligence:** rejected because representation macro reuse is not engineering
  solution reuse. Future Patterns own automatic breaker/contactor/overload style engineering solutions.
