---
title: M43 Presentation Contract Pack
type: normative-architecture-companion
status: final
updated: 2026-08-06
governing-spine: ARCHITECTURE-SPINE.md
---

# M43 Presentation Contract Pack

This companion freezes cross-language contracts needed before compiler, LSP, Theia, Konva, SVG,
asset, and proof work can proceed independently. Where this document and implementation differ, the
implementation is wrong until this document is deliberately amended through the architecture memlog.

## 1. Normative Artifacts

| Artifact | Authority |
| --- | --- |
| `kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json` | `AthenaDiagramScene` JSON shape |
| `kernel/presentation-model/src/main/resources/schema/athena-scene-publication.schema.json` | READY/STALE/UNAVAILABLE publication shape |
| `kernel/interaction-model/src/main/resources/schema/athena-diagram-command.schema.json` | Query, command, result, and rejection shape |
| `kernel/presentation-model/src/main/resources/profile/svg-safe-1.json` | Admitted SVG elements, attributes, limits, and canonicalization |
| `contracts/presentation/v1/manifest.json` | Shared conformance vectors and expected digests |
| `ide/theia-frontend/src/browser/diagram/generated/` | Generated TypeScript types and validators; never hand-edited |

Every schema is JSON Schema 2020-12, uses `additionalProperties: false`, requires every semantically
mandatory field, omits absent optional fields, and forbids `null`. Kotlin serialization is validated
against committed schemas. TypeScript types and validators generate from those schemas. No dependent
story may create a prose-equivalent DTO.

## 2. Scene V1

`AthenaDiagramScene` requires these top-level fields in this order for documentation; canonical JSON
object-key ordering follows inherited M42 AD-32 rather than declaration order:

```text
schemaVersion = 1
sceneId
inputRevision
sceneDigest
page
snapGrid
styles[]
assets[]
occurrences[]
routes[]
decorations[]
traces[]
```

### 2.1 Page And Grid

- `page` contains page bounds, drawing bounds, background style, and coordinate-border bounds.
- `snapGrid` contains sheet ID, row count, column count, subdivisions `N`, drawing origin,
  formula version `athena-grid-1`, and placement-anchor policy.
- All public scene geometry uses signed integer `SceneUnit`. Width, height, stroke width, font size,
  and hit radius are positive integers where present.

### 2.2 Closed Scene Facts

| Fact | Required identity and content | Ownership topology |
| --- | --- | --- |
| Occurrence | element ID, occurrence ID, subject ID, bounds, transform, placement-anchor point/ID, z-index, style ID, optional asset ID, Ports, Labels, trace ID | One root per Projection occurrence. Geometry/style changes do not change its ID. |
| Port | element ID, Port ID, owner occurrence ID, point, visible shape, hit shape, direction metadata, style ID, trace ID | Child of exactly one occurrence; command target is Port ID, never paint ID. |
| Label | element ID, role, text, anchor, bounds, rotation, style ID, trace ID | Child of occurrence or route; role is a stable semantic slot, not list index. |
| Route | element ID, Relationship ID, optional Flow ID, endpoint Port IDs, ordered points, z-index, style ID, trace ID | Top-level relationship-owned fact; no occurrence owns it. |
| Decoration | element ID, kind, geometry/text, z-index, style ID, trace ID | Closed kinds: `PAGE_BACKGROUND`, `FRAME_SEGMENT`, `COORDINATE_LABEL`. |
| Style | style ID and complete renderer-ready paint values | Referenced by ID; no CSS lookup is required for scene geometry. |
| Asset | asset ID, digest, media kind, profile, intrinsic bounds, bundle entry ID, trace ID | Content-addressed manifest; bytes live in publication asset bundle. |
| Trace | trace ID, ordered origins, primary origin | Referenced by every visible/hittable fact. |

No generic extension element exists in schema v1. A new element kind requires schema version review.

## 3. Identity, Revision, Ordering, And Digest

### 3.1 Typed IDs

- `SceneId` is `scene:sha256:<64-lowercase-hex>` over canonical tuple `(projectId, sheetId)`.
- `SceneElementId` is `<kind>:sha256:<64-lowercase-hex>` over canonical tuple
  `(sceneId, kind, stableOwnerIds, semanticRole)`. Geometry, style, text, z-index, revision, and list
  index never participate.
- `StyleId` is `style:sha256:<64-lowercase-hex>` over the complete resolved style record.
- `AssetId` is `asset:sha256:<64-lowercase-hex>` over `(mediaKind, profileId, canonicalBytes)`.
- `TraceId` is `trace:sha256:<64-lowercase-hex>` over the canonical ordered origin records.
- Upstream Sheet, occurrence, subject, Relationship, Flow, and Port IDs remain their typed opaque
  values. Consumers compare but never parse any ID.

IDs survive input reordering, process restart, viewport changes, paint changes, and geometry changes.
They change only when their stable semantic owner or role changes. Repeated occurrences have distinct
Projection occurrence IDs and therefore distinct scene IDs.

### 3.2 Input Revision

`inputRevision` is `input:sha256:<64-lowercase-hex>` over canonical ordered records containing:

1. every open/closed project and Sheet Companion source relative path, exact UTF-8 byte digest, and
   editor document version when open;
2. locked package snapshot and admitted asset/font digests;
3. compiler protocol/schema versions;
4. selected Projection, Spatial, and Presentation profile IDs.

Formatting-only source changes therefore change `inputRevision`. Commands compare-and-set this value
plus explicit edited-document version/digest. No silent rebase is allowed.

### 3.3 Scene Digest And Order

`sceneDigest` is `sha256:<64-lowercase-hex>` over inherited M42 AD-32 canonical UTF-8 JSON for the
complete scene with only `sceneDigest` omitted. Referenced asset/font digests participate through
scene manifests; asset bytes, diagnostics, viewport state, and timestamps do not.

Arrays use these exact orders:

- styles by `styleId`;
- assets by `assetId`;
- occurrences by `(zIndex, elementId)`;
- child Ports by `portId`, then `elementId`;
- child Labels by `(role, elementId)`;
- routes by `(zIndex, relationshipId, elementId)`;
- decorations by `(zIndex, kindRank, elementId)`, where kind rank follows schema enum order;
- traces by `traceId`;
- route points retain compiler-authored path order.

Equal z-index uses kind rank `PAGE_BACKGROUND`, `FRAME_SEGMENT`, `ROUTE`, `OCCURRENCE`, `PORT`,
`LABEL`, `COORDINATE_LABEL`, then element ID.

## 4. Reality Ownership Matrix

| Fact | Projection | Spatial | Presentation/Scene | Adapter |
| --- | --- | --- | --- | --- |
| Sheet/occurrence/subject membership and reading order | Own | Reference | Copy IDs | Consume |
| Page and drawing bounds, grid origin/extents | Intent only | Own exact geometry | Copy/validate | Transform to CSS pixels |
| Occurrence bounds, transform, placement anchor | No coordinates | Own | Copy/validate | Paint/hit only |
| Port anchor and Route points | Endpoint meaning only | Own | Copy/validate | Paint/hit only |
| Label anchor, bounds, rotation, collision result | Label intent | Own geometry | Copy text/geometry | Paint only |
| Frame and coordinate-border geometry | Sheet intent | Own exact geometry | Convert to decoration facts | Paint only |
| Asset/style selection and resolved paint | Representation intent | No paint | Own resolution and z-order | Consume |
| Culling, cache, pan, zoom, hover, selection, preview | None | None | None | Own transient state |

Presentation compilation may join and validate upstream facts. It may not move an occurrence, reroute
a Relationship, remeasure a label into a new position, or repair an invalid Spatial document.

## 5. A1 And Micro Coordinate Contract

### 5.1 Addressing

- Top-left of drawing bounds is logical `(0,0)` before adding page drawing-origin offset.
- x grows right; y grows down.
- Rows are `A..Z, AA..AZ, BA...` using Excel-style one-based labels; internally `A = 0`.
- Columns are decimal one-based values; internally column `1 = 0`.
- For `grid: C * R cell: N`, macro origin is `((column-1)*N, rowIndex*N)`.
- `micro(x,y)` accepts `1..N`. Logical offset is `(x-1,y-1)`.
- Bottom/right macro boundary belongs to the next cell; `micro(N,N)` is the last internal point.
- Missing `micro` uses `(N/2+1,N/2+1)`. `N` is divisible by 4, so this is integral.
- Placement aligns the occurrence's compiler-owned `placementAnchor` to the resulting point; it does
  not imply top-left bounds placement.

Absolute scene point is `drawingBounds.origin + macroOrigin + microOffset`.

### 5.2 Required Cell-4 Vectors

| Address | Micro | Logical anchor before drawing-origin offset |
| --- | --- | --- |
| A1 | omitted, therefore `(3,3)` | `(2,2)` |
| A1 | `(1,1)` | `(0,0)` |
| A1 | `(4,4)` | `(3,3)` |
| A2 | omitted | `(6,2)` |
| B1 | omitted | `(2,6)` |
| A1 | `(4,1)` | `(3,0)` |
| A2 | `(1,1)` | `(4,0)` |

### 5.3 Snap

Adapter maps pointer CSS coordinates through inverse viewport transform into logical scene
coordinates and calls `athena-grid-1` snapping. It never applies device pixel ratio. Nearest
in-bounds anchor wins; equal squared distance sorts by row, column, micro y, micro x. Outside drawing
bounds has no candidate. A locked occurrence cannot start drag. An occupied anchor may preview, but
compiler overlap validation accepts or rejects it. Preview and accepted `SheetAnchor` must be equal;
Kotlin and TypeScript share the vectors under `contracts/presentation/v1/grid/`.

## 6. Diagram Authoring Protocol

Custom LSP methods are exact and versioned by their payload schemas:

- `athena/diagramScene` returns `AthenaScenePublication`;
- `athena/diagramConnectOptions` returns revision-bound connection options;
- `athena/applyDiagramCommand` returns typed accepted/rejected command result.

`athena/projectionSession` is not an alias and is removed from presentation use.

### 6.1 Command Envelope

Every command requires:

```text
schemaVersion = 1
commandId                 # UUID generated once by client; idempotency key
sceneId
expectedInputRevision
sourcePreconditions[]     # relative source ref, open document version, UTF-8 byte digest
kind
body
```

`MoveOccurrence` body requires Sheet ID, occurrence ID, exact `SheetAnchor`, and `lockAction` enum
`PRESERVE | LOCK | UNLOCK`.

`ConnectPorts` body requires source/target Port IDs and one compiler-issued `connectionOptionId`.
Before submission, Theia queries `ConnectOptions` with revision and endpoint IDs. Runtime returns
zero or more options, each binding Relationship definition ID, named participant roles, direction,
and one explicit writable source target. Ambiguous options require user selection. No option means
the command is unavailable. Frontend never infers Relationship kind, role, direction, or file.

### 6.2 Result

Result is exactly one of:

- `ACCEPTED`: command ID, previous input revision, versioned single-file `WorkspaceEdit`, and expected
  publication correlation ID;
- `REJECTED`: command ID, current input revision, reason enum `STALE`, `UNAVAILABLE`, `INVALID`,
  `CONFLICT`, `READ_ONLY`, `IO_FAILURE`, or `COMPILATION_FAILURE`, plus ordered diagnostics.

Duplicate `commandId` against identical preconditions returns the original result. Reuse with
different content is `CONFLICT`.

### 6.3 Transaction State Machine

```text
receive
  -> validate schema and idempotency
  -> compare input/source preconditions
  -> resolve semantic target and structured source mutation in Kotlin
  -> compile proposed in-memory source snapshot
  -> compare preconditions again
  -> return one versioned single-file WorkspaceEdit
  -> Theia applies it as one undo unit
  -> normal repository compilation publishes correlated READY scene
```

`DiagramAuthoringService` in runtime owns semantic resolution and overlay compilation. LSP is transport
only. Frontend never parses Athena source or performs string surgery. M43 commands edit one file; a
future multi-file command requires a new atomicity decision. Stale/conflict never rebases. Rejection
changes neither source nor accepted scene. Undo/redo is the existing editor transaction followed by
normal recompile.

## 7. Publication, Diagnostics, And Trace

### 7.1 Publication State

`AthenaScenePublication` is closed:

- `READY`: attempted and accepted revisions match; scene and asset bundle present; commands enabled.
- `STALE`: current text/package input failed; last accepted scene may remain visible with both
  attempted and accepted revisions plus diagnostics; all diagram mutation commands disabled.
- `UNAVAILABLE`: no accepted scene, including missing/ambiguous companion or adapter/schema failure;
  diagnostics required and commands disabled.

No state mixes scene elements or assets across accepted revisions.

### 7.2 Diagnostics

Compile, transport, adapter, asset, and command failures use one public envelope: severity, exact
subject, problem, correction, primary portable source reference when available, related references,
and secondary stable diagnostic code. Human message never consists only of a code or framework name.

### 7.3 Trace

Trace origins extend inherited M42 AD-31 with source snapshot digest and role enum:

`SEMANTIC_DECLARATION`, `PORT_DECLARATION`, `RELATIONSHIP_DECLARATION`, `SHEET_DECLARATION`,
`SHEET_PLACEMENT`, `ASSET_DEFINITION`, `SPATIAL_DERIVATION`.

Ranges are zero-based UTF-16 LSP positions. Origins sort by role rank, source path, start, end, then
subject ID. Primary navigation is occurrence/label to semantic declaration, Port to Port declaration,
Route to Relationship declaration, and frame/coordinate label to Sheet declaration. Asset definition
and placement are related origins. Derived paint retains owning Sheet or semantic origin rather than
inventing an absolute file URI.

## 8. Paint, Assets, And Fonts

### 8.1 Resolved Paint

Scene styles contain explicit 8-digit RGBA colors, integer stroke width and dash array, line cap/join,
fill rule, opacity, font asset ID, font size/weight, text alignment/baseline, and visible/hit geometry.
No CSS class, theme lookup, system font fallback, or adapter default affects compiled geometry.
Label anchor, bounds, wrapping, and rotation are compiler facts; adapter may measure only for
diagnostics and cannot reposition.

### 8.2 Asset Pipeline

Compiler parses assets with DTD and external entities disabled, applies `svg-safe-1`, canonicalizes
admitted bytes, and computes digest. SVG v1 admits only `svg`, `g`, `path`, `rect`, `circle`, `ellipse`,
`line`, `polyline`, `polygon`, `defs`, and `clipPath`, with geometry/transform/viewBox, inline
fill/stroke, local clip fragments, `title`, and `desc` attributes defined by profile. It rejects
scripts, event attributes, CSS/style, animation, filters, `foreignObject`, `text`, `use`, `image`,
non-local URL values, DTD, entities, and external references.

Limits:

- SVG source: 2 MiB each;
- PNG: maximum 4096 x 4096 and 64 MiB decoded each;
- WOFF2: 4 MiB each;
- publication asset bundle: 32 MiB encoded total.

Runtime publishes deduplicated canonical bytes in a revision-scoped asset bundle. Adapters verify
digest and never reopen source/package paths or fetch network URLs. Cache entries and Blob URLs are
keyed by accepted revision plus asset ID and revoked on scene replacement or widget disposal.

### 8.3 Symbol Asset Boundary

An IEC/part symbol is a representation asset, not an engineering definition. Athena source defines
the stable Port ID, direction, flow, and engineering properties. A symbol SVG may own its visual
geometry, center, and named visual-anchor coordinates/properties. It may reference an existing Port
only so the compiler can validate and map an anchor; SVG metadata cannot create, rename, remove, or
change any Port or engineering property.

`PresentationAssetCompiler` is the sole package/profile resolver. It consumes the locked package
snapshot, validates mappings against existing Engineering Port IDs, and emits `SceneAsset` plus
compiler-owned visual Port geometry. Package/profile/asset digests are part of `inputRevision`.
Adapters consume the resulting scene and bundle only; they never select assets or map anchors.

M43 `svg-safe-1` is generic: `id`, `title`, `desc`, and anchor-like metadata are inert, and Story 2-2
must not parse them as symbol or Port declarations. A later versioned `symbol-asset-v1` profile must
define its closed visual-metadata whitelist and exact center/anchor schema, then fail closed for a
missing, surplus, or mismatched anchor. It must not add shape syntax or visual implementation details
to `.athena` engineering source.

## 9. Rendering And Proof

- Live adapter uses host CSS pixels for fit/pan/zoom. Konva alone owns backing-store DPR. DPR changes
  refresh Canvas/cache but never alter scene-to-CSS transform or hit coordinates.
- Deterministic SVG uses fixed namespace, element/attribute ordering, integer formatting, escaping,
  embedded admitted asset bytes, and no timestamps/runtime metadata. Exact bytes are golden.
- PNG proof is Electron/Chromium E2E capture, not a third adapter. Harness waits for `READY`, asset
  decode, `document.fonts.ready`, and two animation frames. Evidence records scene digest,
  Electron/Chromium versions, viewport, CSS DPR, font/asset digests, capture timing, and pixel
  tolerance.

## 10. Scale Gate

Normative synthetic fixture expands to exactly 100,000 paint elements, with 5 percent intersecting a
`1600 x 1000` CSS-pixel viewport at DPR 1. Environment records CPU, GPU, RAM, OS, Electron/Chromium,
Node, and adapter versions. Gesture transcript uses 60 warm-up frames then 300 measured pan, zoom,
selection, and drag frames.

All thresholds must pass on recorded M43 reference hardware:

| Metric | Gate |
| --- | --- |
| Scene validation through first stable paint | <= 5 seconds |
| Incremental JS heap after settle | <= 512 MiB |
| Pan/zoom/drag input-to-paint p95 | <= 50 ms |
| Selection hit-to-visible-state p95 | <= 100 ms |
| Hit identity and source-trace errors | 0 |
| Unhandled errors, blank frames, lost accepted revision | 0 |

Failure blocks M43 closure. First response is profiling, culling/batching/cache correction, and node
mapping reduction behind the existing adapter. A Pixi adapter is eligible only when measured evidence
shows the same scene/command contract cannot meet this gate with Konva.

## 11. Shared Conformance Corpus

```text
contracts/presentation/v1/
  manifest.json
  scene/
    rolling-shutter.json
    invalid-scenes.json
    digest-vectors.json
  grid/
    cell-4-vectors.json
    cell-8-vectors.json
  commands/
    move-transcript.json
    connect-transcript.json
    stale-and-conflict-transcript.json
  trace/
    expected-origins.json
  assets/
    admitted.svg
    rejected-vectors.json
    bundle-manifest.json
  render/
    rolling-shutter.svg
    png-proof-manifest.json
  benchmark/
    scene-100k-manifest.json
    interaction-transcript.json
```

Compiler, schema validator, LSP protocol, generated TypeScript, live adapter, SVG adapter, command
service, and E2E consume these same artifacts. Equivalent private fixtures do not satisfy closure.
