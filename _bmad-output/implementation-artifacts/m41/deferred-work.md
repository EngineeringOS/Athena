# M41 Deferred Work

## Deferred from: code review of 1-2-derive-bounds-and-alignment (2026-08-03)

- Story 2.1: preserve actual route port identity in Presentation endpoints; reject missing or
  unresolved endpoint identities without first/last fallback or silent route drops; remove
  zero-length route segments.
- Story 3.1: preserve Sheet boundaries through Spatial-to-Presentation assembly, resolve repeated
  occurrence labels per Sheet, keep the fixed Sheet composition inside the Presentation canvas,
  and prevent duplicate authored Presentation publication.
- Story 3.2: surface authored Spatial/Presentation failures in compilation diagnostics, validate
  route occurrence/anchor consistency, guard Presentation canvas extent overflow, reject non-finite
  route points, scope duplicate/nested Construct validation per Sheet, and validate duplicate typed
  Region, Construct, and alignment identities in `SpatialReality`.
- Story 4.1: correct crossing and body-intersection measurements so normal bends, shared endpoints,
  anchor contacts, and segment-through-body cases are measured truthfully.
## Deferred from: code review of 2-2-build-orthogonal-route-facts-and-lanes (2026-08-03)

- Move complete independent-document Route/Lane invariants into `SpatialReality.validate` during
  Story 3.2 final geometry validation. Story 2.2 keeps the complete compiler-stage
  `SpatialRouteValidator`; Presentation membership propagation is fixed in Story 2.2.

## Deferred from: code review of 2-3-trace-routes-and-hold-the-no-optimization-boundary (2026-08-03)

- Story 3.2: reject duplicate Lane identities and all other invalid independent `SpatialDocument`
  Route/Lane cardinalities in `SpatialReality.validate` before Spatial-to-Presentation lookup.

## Deferred from: code review of 3-1-publish-explicit-geometry-facts (2026-08-04)

- Story 3.2: accumulate and surface authored Spatial/Presentation failure diagnostics instead of
  discarding them at Presentation assembly.
- Story 3.2: validate required facts independently per Sheet rather than through flattened
  document collections.
- Story 3.2: validate reciprocal Lane membership and reject phantom Route memberships.
- Story 3.2: reject duplicate Spatial identities and validate Grid Reference ownership and targets.
- Story 3.2: validate arbitrary Sheet extents against fixed composition and canvas bounds.
- Story 3.2: reject diagonal and zero-length independent-document Route segments.
- Story 4.1: reject non-finite quality measurements while correcting final quality formulas.

## Deferred from: code review of 4-2-measure-density-and-occupancy-against-the-baseline (2026-08-04)

- Story 5.2: assert projected Presentation Occurrence identities and count alongside connector
  trace as part of visible product E2E evidence.
