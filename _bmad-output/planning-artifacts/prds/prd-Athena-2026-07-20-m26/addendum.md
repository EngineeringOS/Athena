# M26 Addendum

## Decision Notes

- The selected M26 lane is Semantic Document Projection Foundation.
- Earlier page-bundle terminology was removed because it pulls Athena toward a page-first mental
  model.
- Library breadth and publishing were considered adjacent directions but are not the M26 focus.
- M26 should reuse M24 routing and M25 presentation policy instead of rebuilding those layers.
- The first document projection policy is compiler/runtime-owned
  `athena-document-projection-v0`.
- Cross-reference and continuation labels must follow the M25 route-label lesson: compact on canvas,
  detailed on hover, selection, or inspector.
- The `.athena` project remains the single source of truth. M26 must not create another document or
  page authority beside semantic source and compiler/projection facts.
- The accepted sample must prove that source-file organization does not define sheet-view
  organization.

## Architecture Notes For Downstream Design

The likely architecture spine should introduce Document Projection IR as a governed projection layer,
not as an EPLAN-style page model:

```text
Semantic model
    -> projection snapshot
    -> Document Projection Policy
    -> Document Projection IR
    -> document occurrence index
    -> sheet-view facts
    -> cross-reference and continuation facts
    -> Presentation Policy/Profile
    -> Presentation IR
    -> Theia renderer
```

Candidate contract areas:

- document projection identity
- document projection policy version
- policy identity or deterministic policy hash
- sheet-view identity and metadata
- sheet-view occurrence membership
- view role and view ordering policy
- logical view zones and display metadata
- document occurrence index
- continuation facts
- cross-reference facts
- explicit `DocumentOccurrence`, `DocumentLocation`, `ContinuationFact`, and `CrossReferenceFact`
  contracts
- document locations, not raw coordinates
- IDE sheet-view navigation payload

Document Projection IR owns:

- document projection identity
- document projection policy version
- view identity
- view ordering
- view roles
- logical location
- occurrence membership
- occurrence index
- continuation facts
- cross-reference facts
- references
- navigation topology
- no raw `x`, `y`, `width`, or `height`

Presentation Policy/Profile and Presentation IR own:

- visual placement
- symbols
- labels
- terminals
- markers
- rendering primitives
- paint-ready coordinates

M26 refines the M19 Sheet IR boundary:

- document projection owns sheet-view identity, topology, occurrence membership, references, and
  navigation
- Presentation IR or future publication presentation contracts own page frame, page size,
  title-block rendering, coordinates, and paint-ready sheet chrome
- `athena-document-projection-v0` is the first small built-in document projection policy

The architecture should explicitly prevent:

- renderer-owned view identity
- canvas-local page breaks
- treating `.athena` source files as document pages
- sheet views owning geometry, placement coordinates, or page packing authority
- a separate document file, page database, renderer cache, or UI state becoming source of truth
- frontend-derived cross references
- canvas scans resolving document meaning
- new `document`, `sheet`, `page`, `view`, `zone`, or reference syntax unless ANTLR4, Tree-sitter,
  parser, compiler, LSP, fixtures, tests, and docs are updated in the same story
- verbose raw semantic labels as default visible page text
- desktop-viewer or deprecated KMP frontend changes

## Deferred Topics

- final PDF or print export
- revision table workflow
- document release package
- full terminal strip report
- wire list report
- company-specific document standard packs
- large-project auto-pagination
- cross-project documentation references
