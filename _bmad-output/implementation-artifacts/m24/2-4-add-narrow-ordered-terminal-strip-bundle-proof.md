---
status: review
epic: 2
story: 2.4
title: Add narrow ordered terminal strip bundle proof
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 2.4: Add narrow ordered terminal-strip bundle proof

As Aaron, I want a small terminal-strip case to show ordered parallel route behavior, so that M24
moves toward the supplied EPLAN-like reference without trying to clone it.

## Acceptance Criteria

- A terminal-strip scenario inspired by `../../draft/screenshort/coffret_cordons_chauffants.png`
  exists in tests or the M24 sample.
- Semantically related routes can travel through ordered parallel lanes or bundles.
- Terminal attachment order is stable and readable.
- The proof remains narrow and does not claim full cabinet or EPLAN parity.
- Documentation names the reference image as directional only.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md`
- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`

## Notes

Honor architecture AD-7. This is a one-step product proof, not a dense sheet clone.

## Tasks/Subtasks

- [x] Add a narrow terminal-strip proof covering ordered parallel route lanes.
- [x] Add bundle identity/fact contracts without introducing renderer-owned wire state.
- [x] Verify focused story behavior and routing-model regression behavior.

## Dev Agent Record

### Debug Log

- Confirmed RED state with `:kernel:routing-model:test --tests "com.engineeringood.athena.routing.TerminalStripBundleProofTest"` before implementation; compilation failed because bundle contracts were not admitted yet.
- Implemented minimal `RouteBundleId`, `RouteBundleFact`, and optional `AthenaRouteRequest.bundleId`.
- Verified the focused terminal-strip proof and the full routing-model test suite.

### Completion Notes

- Added a stable ordered route bundle contract for semantically related schematic routes.
- Kept the proof narrow: ordered terminal-strip lanes and bundle ordering only, no cabinet routing, no EPLAN parity claim, no new syntax.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.TerminalStripBundleProofTest"`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`

## File List

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RoutingIdentities.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/TerminalStripBundleProofTest.kt`
- `_bmad-output/implementation-artifacts/m24/2-4-add-narrow-ordered-terminal-strip-bundle-proof.md`
- `_bmad-output/implementation-artifacts/m24/sprint-status.yaml`

## Change Log

- 2026-07-19: Completed Story 2.4 terminal-strip bundle proof and routing-model verification.

## Status

review
