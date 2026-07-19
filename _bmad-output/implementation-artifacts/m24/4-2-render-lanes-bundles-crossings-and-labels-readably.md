---
status: review
epic: 4
story: 4.2
title: Render lanes bundles crossings and labels readably
baseline_commit: e4b243ab9441d585e79c2d8effceb1a6692a3be2
---

# Story 4.2: Render lanes, bundles, crossings, and labels readably

As an electrical engineer, I want route details to remain readable, so that the sheet looks ordered
under moderate connection density.

## Acceptance Criteria

- The M24 terminal-strip acceptance case renders ordered parallel lanes or bundles.
- Crossings are deliberate and visually distinguishable where present.
- Route labels or signal markers do not cover component bodies in the accepted sample.
- The graph grid remains visible and useful as the coordinate surface.
- The implementation does not attempt full EPLAN/reference-sheet density.

## References

- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/addendum.md`
- `../../planning-artifacts/prds/prd-Athena-2026-07-18-m24/review-rubric.md`

## Notes

The screenshot reference is directional only. Keep the proof narrow and inspectable.

## Tasks/Subtasks

- [x] Keep ordered terminal-strip lanes and bundles stable in the M24 routing proof.
- [x] Publish route labels from governed route facts without placing them over component bodies in the accepted proof.
- [x] Add Theia workbench route labels and crossing markers derived from governed presentation route geometry.
- [x] Preserve the accepted grid/canvas behavior and avoid full EPLAN/reference-sheet density scope.

## Dev Agent Record

### Debug Log

- Added a RED terminal-strip proof asserting lane order, bundle order, route labels, and no label placement over component bodies.
- Added a RED Theia graph workbench smoke asserting route crossing markers and route-label positions for governed presentation connectors.
- Implemented V0 route labels through `RuleBasedSchematicLabelStrategy`, using route segments and terminal transition identity.
- Added workbench model fields for `routeLabels` and `crossingMarkerPoints`; markers are derived from route geometry only.
- Added SVG rendering for route labels and crossing markers with quiet IDE styling.

### Completion Notes

- Story 4.2 stays inside governed schematic routing fidelity.
- No route syntax, ELK adapter, physical/cabinet routing, or desktop-viewer work was introduced.
- Crossing markers are presentation-only visual markers from already-governed route facts; they do not create connection meaning.

### Verification

- RED confirmed: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.TerminalStripBundleProofTest.terminal strip routes keep stable ordered lanes inside one semantic bundle"` failed before route labels existed.
- RED confirmed: `yarn --cwd ide/theia-frontend test` failed before crossing markers existed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests "com.engineeringood.athena.routing.TerminalStripBundleProofTest.terminal strip routes keep stable ordered lanes inside one semantic bundle"`
- `yarn --cwd ide/theia-frontend test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.PresentationModelDeriverTest"`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest.projection session payload renders terminal anchor route facts instead of legacy graph edges"`

## File List

- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineV0.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/TerminalStripBundleProofTest.kt`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

## Change Log

- 2026-07-19: Added governed route labels, frontend crossing markers, and terminal-strip readability coverage for M24 route rendering.

## Status

review
