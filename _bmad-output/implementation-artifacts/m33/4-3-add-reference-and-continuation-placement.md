---
story_id: 4.3
story_key: 4-3-add-reference-and-continuation-placement
epic: 4
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 4.3: Add Reference And Continuation Placement

## Status

Done

## Story

As a reviewer, I want reference markers and continuation placement so the sheet follows
professional drawing patterns.

## Acceptance Criteria

- Composition emits projection-owned reference target facts and composition-owned continuation
  placement facts.
- The M33 composition proof includes at least one folio/continuation marker bound to the governed
  native marker identity, anchor, label slot, and reference slots.
- References use typed projection facts and package anatomy ids, not hidden DOM, fixture-name
  parsing, source mutation, or renderer inference.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED tests mapping a typed `ProjectionCrossReference` link into deterministic source/target
  reference facts and current-sheet continuation placement facts. (AC: 1)
- [x] Add RED proof/transport tests for the M33 native `iec.folio-continuation-reference` identity,
  `continuation` anchor, `cross-reference` label slot, source/target sheet reference slots, zone,
  compact notation, semantic subject, and projected bounds. (AC: 2,3)
- [x] Add RED fail-closed tests for missing/duplicate links or placements, mismatched sheet and
  occurrence ids, unknown subject/anchor/label/reference slot/zone, malformed bounds, collisions,
  out-of-sheet placement, and cyclic self-link evidence. (AC: 1..3)
- [x] Add cohesive `DrawingSheetReferenceModels.kt` and `DrawingSheetReferenceCompiler.kt` inside
  `:kernel:drawing-composition`; consume Story 4.1/4.2 plans plus engineering-, projection-, and
  representation-model types only. (AC: 1..3)
- [x] Emit deterministic renderer-neutral target/placement facts, proof, diagnostics, and
  transport with projection, representation, composition-bounds, and structure-intent authorities
  kept distinct. (AC: 1..3)
- [x] Add source-authority regression checks excluding `.athena`, DOM, CSS/SVG, `_reference`
  naming, source mutation, file count, and viewport inference. (AC: 3)
- [x] Audit legacy M26/M31 frontend reference navigation and M30 reference fixtures; remove no live
  path and ledger exact Epic 6/7 integration ownership. (AC: 4)
- [x] Run focused module, affected projection/representation modules, full repository, frontend,
  encoding, and diff verification with Gradle commands strictly sequential. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-3, AD-4, AD-8, and AD-10; satisfy FR-22/FR-24 without turning references into
  renderer or source truth.
- Input v0 is explicit: one valid Story 4.1 sheet plan, one valid Story 4.2 structure plan, typed
  `ProjectionCrossReference` values, and typed placement intents. Do not derive references from
  occurrence names, labels, file names, DOM ids, or edge geometry.
- Projection owns semantic subject, source/target sheet ids, source/target occurrence ids, compact
  notation, and link identity. Composition owns only where a governed marker occurrence is placed
  on the current sheet and which named zone contains it.
- A placement intent carries placement id, projection cross-reference id/link occurrence id,
  current-sheet subject id, native marker representation identity, global bounds, declared global
  anchor point/id, label slot id, available reference slot ids, selected source/target reference
  slots, named zone id, and placement role (`SOURCE` or `TARGET`).
- Validate exact Story 2.3 anatomy vocabulary through typed ids, but do not depend on
  `package-runtime`: `iec.folio-continuation-reference`, anchor `continuation`, label slot
  `cross-reference`, and reference slots `source-sheet`, `source-zone`, `target-sheet`,
  `target-zone`.
- A source placement must match the projection link source sheet/occurrence; a target placement must
  match target sheet/occurrence. Source and target sheet+occurrence cannot be identical.
- Placement bounds and anchor point must remain inside Story 4.1 drawing area and marker bounds.
  Bounds must not overlap Story 4.2 subject or resolved label bounds by positive area. The declared
  named zone must exist in Story 4.1.
- Fail closed: diagnostics return no plan/proof/payload and carry deterministic code, authority,
  subject, and message.
- Story 4.3 proves composition facts. Live Workbench marker consumption remains Story 6.2/7.2; do
  not claim Electron visibility here.
- No external dependency or web research is required.

## Architecture Compliance

- Reuse `ProjectionCrossReference`, `ProjectionCrossReferenceLink`, `DrawingSymbolIdentity`,
  `DrawingSymbolAnchorId`, `DrawingSymbolSlotId`, `GraphicBounds`, and `GraphicPoint`.
- Dependency remains engineering + projection + representation + drawing-composition facts -> reference
  composition facts. No compiler, package-runtime, LSP, Theia, SVG, DOM, or source language
  dependency.
- Never synthesize semantic ids or mutate projection links. Preserve them byte-for-byte in facts and
  transport.
- Keep projection-reference authority separate from composition placement/bounds authority and
  package-anatomy identity authority.

## File Structure Requirements

- Keep current `:kernel:drawing-composition` dependencies unchanged.
- Add `DrawingSheetReferenceModels.kt`, `DrawingSheetReferenceCompiler.kt`, and matching focused
  tests.
- Expand the existing M33 composition authority frontend test to scan these files.
- Keep files cohesive and split if distinct roles exceed the 200-300 line heuristic.

## Testing

- At least one cross-sheet source/target link with a source placement on the current sheet and exact
  structured proof.
- Deterministic ordering under shuffled references/placements.
- One fail-closed test for every category in Tasks/Subtasks; invalid output has null plan/proof/
  transport and nonblank authority/subject/message.
- Boundary-touching marker/subject bounds are allowed; positive overlap is rejected.
- Repeat compilation and transport equality.
- Run `:kernel:drawing-composition:test`, `:kernel:projection-model:test`,
  `:kernel:representation-model:test`, full `gradlew test`, frontend `yarn test`, and encoding audit.

## Evidence Plan

- AC1: exact projection-link target facts and source/target placement-role tests.
- AC2: exact M33 marker anatomy vocabulary, zone, bounds, notation, and proof/transport assertions.
- AC3: typed identity tests and frontend source-authority scan.
- AC4: cleanup ledger, RED/GREEN/debug record, regressions, encoding/diff checks, and adversarial
  review.

## Polish And Purge

Audit `_reference` fixture-name assumptions and legacy frontend navigation separately from typed
reference facts. Remove only dead Story 4.3 code. Ledger active compatibility paths with owner,
reason, target story, and verification; never claim migrated Workbench behavior before E2E proof.

## Previous Story Intelligence

- Story 4.2 emits deterministic subject/anchor/label facts and validates drawing-area containment,
  collision, and membership without frontend inference. Reuse those facts for marker collision and
  subject/anchor identity checks.
- Story 4.2 adversarial review found duplicate-member and caller-order instability. Story 4.3 must
  reject duplicate links/placements and normalize all id-defined collections.
- Story 4.1 provides projection named zones and exact drawing-area bounds with separate metadata and
  bounds authority.
- Story 2.3 provides the governed folio marker anatomy listed above; do not create a second symbol or
  package dependency.

## Git Intelligence

- Baseline remains M32 commit `0311ad6`; M33 remains one uncommitted milestone change.
- Use Kotlin/JDK and `kotlin.test`; no new dependency is expected.
- Never add `.tools`, generated Theia bundles, build outputs, or reference/QET sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: reference compiler/model contracts were absent; the focused test also exposed that
  `projection-model` does not transitively publish its public `StableSemanticIdentity` type.
- GREEN: declared the direct engineering-model dependency and added typed reference target,
  placement, proof, diagnostics, and transport contracts.
- RED/GREEN: adversarial review exposed non-unique multi-link target facts and incomplete same-sheet
  SOURCE/TARGET enforcement. Stable target ids and dual-side completeness checks now cover both.
- FOCUSED: `:kernel:drawing-composition:test` passed all reference, structure, and sheet tests.
- AFFECTED: `:kernel:projection-model:test` and `:kernel:representation-model:test` passed
  sequentially.
- FULL REGRESSION: `gradlew test` passed with 151 actionable tasks.
- FRONTEND: `yarn test` passed all 204 tests, including expanded no-DOM/source-reference authority
  scanning.

### Completion Notes List

- Added typed mapping from `ProjectionCrossReference` links to unique reference target facts and
  current-sheet source/target continuation placements.
- Bound proof to the governed native marker identity, continuation anchor, cross-reference label
  slot, source/target sheet and zone slots, compact notation, semantic subject, named zone, and
  projected bounds without package-runtime or renderer inference.
- Added fail-closed diagnostics for malformed/duplicate/cyclic links, incomplete or mismatched
  placements, missing subjects/anatomy/zones, invalid/out-of-sheet bounds, and positive-area
  collisions. Boundary touching remains valid.
- AC-to-evidence: AC1 is covered by exact typed link/target/placement and same-sheet role tests; AC2
  by exact native marker vocabulary and proof/transport assertions; AC3 by typed identity mapping
  and source-authority scan; AC4 by cleanup ledger, sequential regressions, encoding/diff checks,
  and adversarial review.
- Polish/purge: no dead Story 4.3 code remains. Existing M26/M31 reference navigation and M30
  fixture compatibility are active and remain ledgered for Stories 6.2/7.2; no live Electron
  marker claim is made.
- Final acceptance review: PASS for AC1-AC4.

### File List

- `_bmad-output/implementation-artifacts/m33/4-3-add-reference-and-continuation-placement.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m33-sheet-composition-authority.test.mjs`
- `kernel/drawing-composition/build.gradle.kts`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetReferenceCompiler.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetReferenceModels.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetReferenceCompilerTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context with typed projection links, governed marker anatomy,
  exact placement validation, authority separation, and live-integration guardrails.
- 2026-07-23: Implemented typed reference targets, native marker placement proof, deterministic
  transport, and fail-closed reference diagnostics.
- 2026-07-23: Fixed adversarial multi-link identity and same-sheet completeness findings, passed
  full regression, and closed the story.
