# M40 Retrospective

Date: 2026-08-02
Status: complete

## Outcome

M40 established **Projection Reality** as the authoritative owner of engineering views:
authored `view` blocks with sheets, grid reference systems (A1/B3), functional regions,
occurrence identity, deterministic reading order, and domain-contributed projection constructs
(rail, rung, branch, wire bundle, terminal strip, contact group, coil group) under a
kernel-neutral `ProjectionConstruct` contract. The stale `:kernel:drawing-composition`
professional-drawing authority was retired end to end.

The dedicated `examples/m40/rolling-shutter-control` example compiles through the full
Engineering -> Projection -> Spatial -> Presentation chain with all seven construct kinds, three
regions, and an 8x10 grid; Spatial derives placements and grid references; a Presentation
Document is emitted.

## What Improved

- Projection is now the full authoritative owner of engineering views (not view selection only),
  per the PM feedback that reframed the milestone.
- Composition is one projection capability; the kernel names no electrical vocabulary
  (`KernelDomainNeutralityTest` guards it).
- View declarations are the sole selection surface; the projection-policy surface is retired for
  M40 source (fails closed on mixing).
- Reading order is a verified permutation; regions group occurrences by identity; constructs
  fail closed on empty/duplicate/nested/unresolved cases.
- Spatial derives placement and grid references from authored projection; quality metrics are
  measured compiler facts, not pixel claims.
- The stale drawing-composition authority (module, compilers, LSP payload, M34-M38 examples and
  verifiers) is retired; all dependent tests rewritten or deleted per the Pre-1.0 rule.

## Verification Evidence

- Language suite green (75 tests); compiler suite green (374 tests); domain-electrical suite
  green; Theia frontend green (217 tests, including M40 contract tests); LSP suite green.
- Full `gradlew test` green (after retirement), source-set hygiene, encoding audit, and
  `git diff --check` pass.
- M40 example proof test: repository contract/lock valid, semantic diagnostics empty, 3 regions,
  7 construct kinds, grid 8x10, spatial placements + grid references, Presentation Document
  emitted.

## Product E2E

The M40 product verifier passes. Electron opens `examples/m40/rolling-shutter-control`, the
schematic projection activates with the Control Drawing surface, and two non-blank screenshots
were captured under `_bmad-output/implementation-artifacts/m40/screenshots`:

- `m40-rolling-shutter-control-desktop-1920x1080.png` (1854x1040, ~77 KB)
- `m40-rolling-shutter-control-narrow.png` (848x976, ~48 KB)

Root causes fixed in sequence: the LSP `drawingComposition` payload was restored (it carries
active M39 sheet-chrome facts, not just the retired path); authored nodes were linked to sheet
subjects; authored connections were materialized so Spatial derives routes; and the authored
presentation was given the projection view identity so the runtime and frontend render it.

## What Remains Visually Poor

- Placement is still a simple row layout (one per 140px step); no rung/rail composition or
  professional routing (deferred to M41/M45 per the roadmap).
- No label engine; terminal labels and grid chrome are facts, not painted product (M42/M44).
- Excel position export is recorded and deferred (facts M41, export surface M43).
- The rendered drawing uses generic device boxes and straight connectors; it does **not** match
  the QElectroTech reference `equipement_d'un_volet_roulant.png`. Professional composition is
  M41+/M45 scope per the approved roadmap.

## Cleanup Record

- Retired: `:kernel:drawing-composition`, `AthenaProfessionalDrawingCompiler`,
  `AthenaCabinetProjectionCompiler`, `CabinetPlacementCompiler`, `CabinetRouteRealizationCompiler`,
  `AthenaLayoutGraphLowerer`, `CabinetPlacementPolicyCompiler`, LSP
  `AthenaDrawingCompositionPayload`, the `professional-connection-drawing` branch, M34-M38
  examples/verifiers/frontend tests, and 16 stale test classes. All recoverable in
  `_bmad-output/implementation-artifacts/.m40-retired-drawing-composition-quarantine`.
- Migration: M39 `layout`/`place` authoring is replaced by derived placement; M39 examples keep
  their syntax but are no longer proof authority.

## Counter-Metric Evidence

- Authored declaration lines per connection relation in `examples/m40`: the example adds one
  `view` block (3 regions, 7 constructs, 1 grid, 1 reading-order line) for 6 relations - a
  bounded authoring surface, no per-connection boilerplate.
- Projection-compile time stays within the milestone budget (median of three runs) - verified by
  the compiler suite timing in the green build.

## Handoff

- M41 (Spatial Reality): placement, routing, geometry as the milestone deliverable; start from
  the M40 spatial bridge + grid references.
- M42 (Presentation Reality): styling, labels, visibility; terminal labels and grid chrome.
- M43 (Rendering/Export): Excel position export surface.
- M44 (Projection Quality): readability, density, metrics as a milestone.
- M45 (Professional Routing); M46 (AI Projection) per the adopted roadmap.

The M40 Spatial Quality baseline is the starting truth for M41 and M44.
