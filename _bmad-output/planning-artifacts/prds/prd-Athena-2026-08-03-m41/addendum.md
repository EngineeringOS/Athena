# M41 PRD Addendum: Spatial Reality Recovery

Date: 2026-08-03
Status: Decisions confirmed

This addendum preserves technical mechanisms, alternatives, and recovery context that inform
architecture but do not belong in the PRD capability contract.

## Recovery Inputs

- Approved design:
  `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md`
- Failed delivery audit:
  `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md`
- Initial PRD review:
  `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/review-rubric.md`
- Approved course correction:
  `_bmad-output/implementation-artifacts/m41/sprint-change-proposal-2026-08-03.md`

## Confirmed Decisions

1. **Exit bar:** M41 delivers a coherent Spatial drawing and exact Spatial facts. It does not claim
   QElectroTech or EPLAN parity.
2. **Fixture:** `examples/m41/rolling-shutter` remains the active Golden Fixture. Its expected
   Projection-derived counts are 8 Occurrences, 3 Regions, 7 Constructs, and one Route per visible
   Connection.
3. **Placement:** geometry remains compiler-derived. Athena source and Projection gain no
   coordinates or layout mechanics.
4. **Composition:** the Golden Fixture uses three left-to-right Region columns. Sheet and spacing
   policy are fixed in PRD FR-1 through FR-3.
5. **Routing:** M41 owns exact port Anchors, deterministic basic orthogonal Routes, obstacle
   avoidance, Lanes, and fail-closed trace. M45 owns professional optimization.
6. **Quality:** M41 measures exact geometry facts per Sheet. Label metrics are removed because M42
   owns label geometry.
7. **Pipeline:** `ProjectionSpatialCompiler` becomes the single Spatial orchestration entry.
   Presentation receives only a complete validated Spatial document.
8. **Solvers:** reuse and refactor the current rule-based layout and routing geometry engines behind
   implementation-neutral Spatial adapters. Solver types do not become public Spatial or authoring
   contracts.
9. **Compatibility:** no compatibility path preserves the failed one-row compiler, route fallback,
   pseudo-envelopes, raw metric maps, or weak product proof.
10. **Story process:** recreate and implement Stories 1.1 through 5.3 in numeric order using
    `bmad-create-story` and `bmad-dev-story` for every story.

## Architecture Mechanism

The intended orchestration sequence is:

1. Validate and canonicalize the Projection Snapshot.
2. Convert Sheet, Region, Construct, Occurrence, Connection, and reading-order facts to layout
   intent and constraints.
3. Run the current deterministic layout engine through a Projection adapter.
4. Normalize output into typed per-Sheet Spatial facts.
5. Derive stable port Anchors from placed Occurrence rectangles and port order.
6. Run obstacle-aware orthogonal routing from exact Anchors.
7. Derive Region bounds, Construct envelopes, Grid References, Lanes, and quality snapshots.
8. Validate exact coverage, identity, ownership, geometry, trace, routing, and metrics.
9. Transform the complete document to Presentation without coordinate repair.

This mechanism is an architecture input, not Athena syntax.

## Options Considered

### Patch The Failed Stories

Rejected. The old stories weakened PRD consequences and certified wrong behavior. Editing them in
place would preserve false completion records and ambiguous acceptance history.

Selected alternative: invalidate and delete failed story files, then recreate them from replacement
PRD/epics through BMad.

### Preserve The Existing Spatial Bridges

Rejected. Multiple bridges and transformations create competing geometry authority and make
fallback behavior difficult to detect.

Selected alternative: migrate callers to one `ProjectionSpatialCompiler`, then delete stale bridge,
row-placement, reporter, and transformation paths.

### Author Coordinates Or Placement Hints

Rejected. This violates the human-first source rule and creates a second geometry authority.

Selected alternative: derive placement from Regions, Constructs, topology, authored order/reading
order, and stable identity.

### Defer All Routing To M45

Rejected. M41 cannot own coherent Spatial Reality without exact endpoints and correct basic routes.

Selected alternative: M41 provides complete obstacle-safe orthogonal Routes; M45 optimizes them.

### Pull Professional Routing Into M41

Rejected. Bend/crossing optimization, bundle/trunk routing, and multi-Sheet continuation would
expand scope and obscure the correctness gate.

### Keep Label Pressure As A Proxy

Rejected. Route count is not label geometry. Publishing it under a label name is false evidence.

Selected alternative: remove all M41 label metrics. M42 introduces label geometry and its metrics.

### Accept PNG Size As Product Proof

Rejected. File size cannot distinguish a coherent drawing from a collapsed or blank result.

Selected alternative: assert runtime facts, occupied geometry ranges, Drawing Area pixel
distribution, coordinate preservation, and fresh desktop/narrow screenshots.

## M40 Comparison Boundary

M40 evidence may appear only where fixture, unit, viewport, and calculation method are genuinely
comparable. Old label counts/collision targets are not comparable because M41 has no label geometry.
The M41 baseline must be generated from structured compiler output, not copied prose constants.

## Downstream Handoff

- M42 consumes validated Spatial geometry for styling, labels, visibility, terminal labels, and
  grid chrome.
- M43 consumes Spatial Grid References and Presentation facts for rendering/export surfaces.
- M44 consumes truthful M41 geometry measurements as input to readability optimization.
- M45 consumes exact basic Routes/Lanes as input to professional routing optimization.

No downstream milestone may reinterpret M41 geometry as permission to repair it in Presentation or
Theia.
