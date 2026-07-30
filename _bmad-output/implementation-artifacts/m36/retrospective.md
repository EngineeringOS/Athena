# M36 Retrospective

Date: 2026-07-30
Status: complete

## Outcome

M36 established typed engineering connectivity and lowered it through transient connection and
layout IR into deterministic Cabinet placement, route facts, Graphic Primitive IR, and a
paint-only renderer. The dedicated `examples/m36/connectivity-cabinet` project compiles and renders
21 governed occurrences, including the SVG-backed ABB PFEA112 element, and 31 terminal-anchored
orthogonal routes.

All six M36 epics and all 18 stories are complete.

## Architecture Decisions Preserved

- Athena source remains the single semantic and metadata authority.
- Component identity, port meaning, element anchor, and SVG geometry reference remain separate.
- SVG is package-local geometry input; it does not define engineering meaning.
- Layout Graph, placement proposals, and route facts are derived compiler IR, never source truth.
- The renderer paints validated Graphic Primitive IR and does not infer engineering facts.
- Pre-public legacy contracts are deleted or replaced directly; no compatibility shims remain.

## Cleanup Result

- Removed milestone/demo/proof/sample production classes from `src/main`.
- Renamed real production behavior by responsibility, including
  `AthenaCabinetProjectionCompiler`, `AthenaRouteEngine`, `PhysicalConstraintEvaluator`, and
  `AthenaIndustrialControlProfile`.
- Deleted the stale desktop viewer, mock AI proof provider, legacy binding adapter, and obsolete
  XML/fallback production paths.
- Added a source-set hygiene audit wired into root Gradle verification and a permanent review
  checklist.
- Preserved useful historical helpers only under `src/test`.

## Verification Evidence

- Root Gradle test: 152 tasks passed.
- Runtime test: 127 tests passed.
- Tree-sitter: 17/17 corpus tests and 61/61 Node tests passed.
- Theia frontend: 219/219 tests passed.
- IDE build and LSP `installDist` passed.
- Source-set hygiene audit, UTF-8 encoding audit, and `git diff --check` passed.
- Electron E2E passed at 1920x1080, 1280x900, and 720x900.
- E2E evidence: 31 routes, zero center-anchor fallbacks, zero route/body intersections, complete
  occurrence trace evidence, one visible Cabinet surface, and no XML/HTML runtime authority.

## Failure Learned From

The first final Electron E2E run failed `cabinetRefreshAccepted` while Cabinet remained active.
The cause was a stale Electron frontend bundle: LSP had been rebuilt, but the Theia product had not.
After running the full `yarn --cwd ide build`, the unchanged E2E assertion passed. Future UI E2E
gates must rebuild both LSP and the IDE product before launch.

## Remaining Product Risk

The Cabinet is structurally correct and responsive, but the current screenshots still show dense
route and label composition at fit-to-screen zoom. This is not represented as visual perfection.
Future work should improve route lane allocation, label scale/readability, and visual hierarchy
without moving semantic or physical authority into the renderer.
