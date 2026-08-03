# M41 Deferred Work

## Deferred from: code review of 1-1-derive-deterministic-explainable-placement (2026-08-03)

- Story 1.3: correct grid axis generation so vertical rows are `A/B/C` and horizontal columns are
  `1/2/3`; current generation reverses these dimensions.
- Story 1.3: define grid behavior beyond 26 lettered rows without emitting punctuation.
- Story 1.3: reject blank grid identity and non-positive row/column dimensions.

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
