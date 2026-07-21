# M30 Representation-Relevant Semantic Contract Audit

## Purpose

This audit records which upstream semantic and projection facts M30 representation binding may
consume, which visual concepts must stay out of the semantic kernel, and which gaps must be closed
by domain contracts rather than renderer fallbacks.

## Binding Facts Available Today

| Binding Need | Available Fact | Source Boundary |
| --- | --- | --- |
| Canonical identity | `StableSemanticIdentity` on system, component, port, connection, and references | `kernel/engineering-model` |
| Component type / role seed | `EngineeringComponent.kind` and authored `type` property | `kernel/engineering-model`, electrical runtime validation |
| Device model label seed | Authored `model` property | `kernel/engineering-model` |
| Ports | `EngineeringPort` plus device-owned nested ports indexed as port declarations | `kernel/engineering-model`, compiler semantic graph |
| Port owner | `EngineeringPort.ownerReference` | `kernel/engineering-model` |
| Direction | Authored `direction` property and resolved `SemanticPortDirection` | `kernel/engineering-model`, `kernel/connection-model` |
| Signal / medium | Authored `signal` property and resolved `SemanticSignalFamilyId` | `kernel/engineering-model`, `kernel/connection-model` |
| Semantic port role | `SemanticPortRoleId` in resolved semantic port definitions | `kernel/connection-model` |
| Terminal number | Available on representation terminal notation for native symbols; only partially available as authored/domain labels for semantic ports today | `kernel/representation-model`, electrical domain contracts |
| Relationship capability | `SemanticRelationshipIntent` and compatibility validation from M28/M29 authoring path | `kernel/authoring-model` |
| Relationship endpoints | `EngineeringConnection.from` and `EngineeringConnection.to` | `kernel/engineering-model` |
| Occurrence context | `DocumentOccurrence`, `SheetView`, logical zones, continuation facts, and cross-reference facts | `kernel/document-projection-model` |
| Routing attachment context | Route facts and terminal anchors from M24/M27 projection pipeline | `kernel/routing-model`, graph projection |
| Provenance | `SourceProvenance`, `DocumentProjectionProvenance`, and representation provenance | engineering, document projection, representation model |

## Facts Allowed Into Representation Binding

Representation binding may consume:

- Semantic identity, authored names, component kind, authored domain properties, and source provenance.
- Port identity, owner identity, direction, signal family, semantic role, optional protocol, and terminal number or terminal-facing labels when a domain contract exposes them.
- Relationship identity, endpoints, validated compatibility result, relationship role, and route/continuation projection facts.
- Document projection occurrence, sheet view, logical zone, cross-reference, continuation, and source/projection provenance.
- Representation policy, definition, lifecycle, label slot, terminal binding, reference binding, and diagnostic contracts from `kernel/representation-model`.

## Forbidden Kernel Concepts

The semantic kernel must not own or infer:

- Symbol primitives such as lines, arcs, rectangles, polygons, SVG paths, or QET XML shapes.
- Visual hotspots, hitboxes, hover borders, drag borders, dotted selection outlines, or frontend wrapper rules.
- Style tokens, stroke width, colors, text placement, font choices, or renderer chrome.
- QET link types, runtime `.elmt` paths, imported SVG fragments, or QET source references in `.athena`.
- Renderer fallback geometry, hard-coded SVG view boxes, off-screen duplicate elements, or canvas-centering rules.

## Legacy Bridge Observed

`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimePresentationPacks.kt`
still contains pre-M30 presentation-pack visual vocabulary such as `viewBoxWidth`, `viewBoxHeight`,
`PresentationSvgPath.pathData`, and stroke tokens. This file is not the semantic kernel, but it is
also not the final M30 representation architecture.

This bridge is recorded as cleanup-ledger entry `M30-CL-001` and must be replaced or narrowed by
the native representation definition, binding, and renderer stories instead of expanded.

## Domain Contract Gaps

| Gap | Why It Matters | Owner | Target |
| --- | --- | --- | --- |
| Terminal number is not a first-class semantic port fact for every authored port. | Professional IEC symbols need terminal numbers as binding facts, not label guesses. | Domain model / electrical slice | M30 Story 3.2 binding proof, defer broader domain normalization to M31 if needed |
| Component representation role is inferred from `type` / properties rather than a normalized semantic role catalog. | Native symbol binding needs stable roles such as supply reference, protective device, coil, contact, actuator, load, and folio reference. | Representation binding + domain semantics | M30 Story 3.1/3.2 |
| Relationship subtype/capability is available through authoring validation but not yet published as a single reusable binding snapshot. | Binding should choose connection, flow, containment, communication, or reference representations without frontend inference. | Authoring model / representation binding | M30 Story 3.1, with wider relationship capability model deferred to M31 |
| Location, device reference, and terminal-strip membership are projection/domain facts but not yet uniform semantic facts. | Cross references and cabinet/schematic projections need them without renderer-side string parsing. | Document projection + domain model | M30 Story 3.2 for demo subset, M31 for full domain contract |

## Closure Rule

If a future story needs a missing binding fact, it must add or extend a semantic/domain/projection
contract or record a deferred gap here. It must not patch the renderer to infer engineering meaning
from SVG geometry, CSS, filenames, QET data, or visible labels.
