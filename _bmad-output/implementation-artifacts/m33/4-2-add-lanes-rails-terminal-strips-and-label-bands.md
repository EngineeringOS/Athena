---
story_id: 4.2
story_key: 4-2-add-lanes-rails-terminal-strips-and-label-bands
epic: 4
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 4.2: Add Lanes, Rails, Terminal Strips, And Label Bands

## Status

Done

## Story

As a controls engineer, I want disciplined schematic structure so the drawing is dense and
scannable.

## Acceptance Criteria

- Composition emits supply rails, control lanes, terminal-strip grouping, compact label bands, and
  route channels.
- Diagnostics detect collisions, excessive whitespace, label overflow, and out-of-sheet content.
- Output preserves package-backed anchor identities and label-slot identities without frontend or
  renderer inference.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED tests for deterministic rail, lane, terminal-strip, label-band, and route-channel
  facts from explicit structure intents inside a Story 4.1 drawing area. (AC: 1)
- [x] Add RED tests proving occurrence facts preserve representation identity, anchor id/point, and
  label-slot id/bounds through plan, proof, and transport. (AC: 3)
- [x] Add RED fail-closed tests for duplicate/missing ids or members, malformed/non-finite bounds,
  overlapping subjects and labels, policy-excessive subject gaps, label-band overflow, missing
  required anchors/slots, and facts outside the drawing area. (AC: 2,3)
- [x] Add cohesive `DrawingSheetStructureModels.kt` and `DrawingSheetStructureCompiler.kt` to the
  existing `:kernel:drawing-composition` module; consume `DrawingSheetCompositionPlan` and
  representation-owned ids/geometry without adding dependencies. (AC: 1..3)
- [x] Emit renderer-neutral structure facts and deterministic proof with explicit drawing,
  representation, structure-intent, and presentation-policy authorities. (AC: 1..3)
- [x] Derive terminal-strip bounds only from validated member bounds plus explicit policy padding;
  preserve caller-declared memberships and never classify subjects from names/types. (AC: 1,3)
- [x] Add transport-safe equality/order tests; invalid input returns diagnostics and no partial
  plan, proof, or payload. (AC: 1..3)
- [x] Audit M30 membership-only composition, layout engine/Presentation IR placement, and current
  Workbench graph spacing; remove no live path and ledger exact migration ownership. (AC: 4)
- [x] Run focused module, affected representation/projection modules, full repository, frontend,
  encoding, and diff verification strictly sequentially for Gradle. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and deep polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 AD-3, AD-4, AD-8, and AD-10; satisfy FR-22..FR-26 without creating CAD truth.
- Extend Story 4.1's `:kernel:drawing-composition` module. Do not extend the old M30
  `SchematicCompositionIntentCompiler`, whose seven broad all-member facts have active M31 callers
  and no professional geometry authority.
- Input v0 is explicit: one valid `DrawingSheetCompositionPlan`, ordered subject occurrences,
  rail intents, lane intents, terminal-strip intents, label-band intents, route-channel intents,
  and one structure policy. No semantic-name/type, source-text, file-count, DOM, viewport, or SVG
  inference is allowed.
- A subject occurrence carries canonical subject id, representation identity, validated global
  bounds, declared lane membership, anchor identities/global points/required flags, and label
  identities/slot identities/global bounds/required flags. These are projection/representation
  facts, not new engineering truth.
- Structural intents carry stable ids, explicit bounds or endpoints, ordered memberships, and
  explicit axis where applicable. The compiler validates and normalizes them; it does not invent
  missing memberships or guessed paper geometry.
- Terminal-strip bounds are the exact union of member bounds expanded by finite non-negative policy
  padding. Other structure bounds remain explicit intent facts in v0.
- Collision v0 means positive-area overlap among subjects in the same lane or labels in the same
  band. Touching boundaries are not collisions. Excessive whitespace means a sorted neighboring
  subject gap on the lane axis exceeds the explicit finite non-negative policy maximum.
- Label overflow means a required/resolved label bound is not fully contained by its declared label
  band. Out-of-sheet means a subject, lane, rail endpoint, terminal-strip result, label band, label,
  or route channel is not fully contained by Story 4.1 `drawingAreaBounds`.
- Fail closed: any diagnostic returns no structure plan, proof, or transport payload. Diagnostics
  contain deterministic code, authority, subject, and message.
- This story emits structured facts only. Stories 6.2/7.2 integrate the live Workbench; do not claim
  that rails/lanes/strips/bands are visible in Electron yet.
- No external dependency or web research is required. Use Kotlin/JDK and existing model types only.

## Architecture Compliance

- Composition facts may own grouping, membership, channels, and projected bounds, but never
  `.athena` mutation, package resolution, semantic identity creation, route geometry, or renderer
  paint.
- Package-backed identity remains upstream: reuse `DrawingSymbolIdentity`,
  `DrawingSymbolAnchorId`, `DrawingSymbolSlotId`, `GraphicPoint`, and `GraphicBounds` rather than
  defining stringly duplicate representation contracts.
- Preserve dependency direction: projection + representation + Story 4.1 sheet facts -> drawing
  structure facts. No LSP, Theia, SVG, compiler, package-runtime, or layout-engine dependency.
- Policy owns density thresholds and strip padding. It does not select IEC symbols or mutate source.
- Every collection and diagnostic is emitted in stable id/order sequence for deterministic proof.

## File Structure Requirements

- Keep the existing `kernel/drawing-composition/build.gradle.kts` dependencies unchanged.
- Add `DrawingSheetStructureModels.kt` for cohesive input/fact/proof/transport contracts.
- Add `DrawingSheetStructureCompiler.kt` for one validation/derivation flow.
- Add matching tests under `kernel/drawing-composition/src/test/kotlin/...`.
- Update the M33 frontend authority scan only if needed to guard dependency/source/DOM/SVG leaks.
- Split only if a file crosses the repository's 200-300 line mixed-responsibility threshold.

## Testing

- Exact deterministic facts for at least two lanes, two rails, one terminal strip, two label bands,
  and two route channels using non-zero/negative drawing origins.
- Exact terminal-strip union/padding math and boundary-touching non-collision behavior.
- Representation identity, anchor point/id, and label slot/id preservation tests.
- One fail-closed case for every diagnostic category named in Tasks/Subtasks; all invalid results
  assert null plan/proof/payload and nonblank authority/subject.
- Repeated compilation and transport output must be data-equal with shuffled caller input where ids
  define order.
- Run `:kernel:drawing-composition:test`, `:kernel:representation-model:test`,
  `:kernel:projection-model:test`, full `gradlew test`, frontend `yarn test`, and encoding audit.

## Evidence Plan

- AC1: exact structure fact, membership, union, ordering, and transport assertions.
- AC2: collision, whitespace, label overflow, malformed input, and out-of-sheet negative tests.
- AC3: package representation/anchor/slot identity preservation and no-inference source guard.
- AC4: cleanup ledger, RED/GREEN log, sequential regressions, encoding audit, and adversarial review.

## Polish And Purge

Audit and ledger every path that bypasses the new structure facts. Do not delete M30 composition,
layout, Presentation IR, or Workbench spacing code while active callers remain. Remove only dead
Story 4.2 code/tests/docs and record exact owner, reason, target, and verification for deferred debt.

## Previous Story Intelligence

- Story 4.1 established `DrawingSheetCompositionPlan`, exact content-derived drawing-area bounds,
  split metadata/bounds authority, deterministic transport, and fail-closed overflow handling.
- Story 4.1 adversarial review found that one authority field can falsely merge projection metadata
  with derived geometry. Story 4.2 facts must keep representation identity, policy, intent, and
  derived bounds authorities distinct.
- Story 4.1 deliberately did not integrate the Workbench. Its A3/A4/960x540 fallback remains active
  and ledgered; do not use those constants as structure input.
- Story 3.3 rejects explicit duplicate/off-screen/fallback facts and derives root SVG bounds. Story
  4.2 supplies upstream structure proof, not another SVG safety implementation.

## Git Intelligence

- Baseline remains M32 commit `0311ad6`; M33 remains one uncommitted milestone change.
- Reuse the Story 4.1 model/compiler/transport patterns and `kotlin.test`; no dependency is expected.
- Never add `.tools`, generated Theia bundles, build outputs, or reference/QET sources.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: structure compiler/model references were absent and the focused test source could not
  compile.
- GREEN: added explicit renderer-neutral rails, lanes, strips, label bands, route channels,
  package identity facts, proof, diagnostics, and transport.
- RED/GREEN: adversarial tests exposed duplicate-member acceptance and caller-order-dependent
  anchors; member uniqueness now fails closed and anchor output sorts by declared identity.
- FOCUSED: `:kernel:drawing-composition:test` passed all seven tests.
- AFFECTED: `:kernel:representation-model:test` and `:kernel:projection-model:test` passed
  sequentially.
- FULL REGRESSION: `gradlew test` passed with 151 actionable tasks.
- FRONTEND: `yarn test` passed all 204 tests, including the expanded M33 composition authority
  guard.

### Completion Notes List

- Added explicit structure-intent contracts and deterministic facts for rails, lanes,
  terminal-strip groups, label bands, and route channels without semantic-name or frontend
  inference.
- Preserved package representation identity, anchor ids/global points, and label-slot ids/bounds
  through plan, proof, and transport with split representation/primitive/intent/policy/bounds
  authorities.
- Added fail-closed diagnostics for duplicate/missing members, malformed/overflow geometry,
  required package data, subject/label collisions, excessive whitespace, label overflow, invalid
  membership, and out-of-sheet facts. Invalid results expose no partial plan/proof/payload.
- AC-to-evidence: AC1 is covered by exact two-lane/two-rail/strip/band/channel facts and union math;
  AC2 by deterministic negative cases for every required category; AC3 by exact identity,
  anchor/slot, authority, and transport assertions; AC4 by source guard, cleanup ledger,
  sequential regressions, encoding/diff checks, and adversarial review.
- Polish/purge: no dead Story 4.2 path remains. M30's active all-member composition facts,
  Presentation IR/layout placement, and Workbench graph spacing remain ledgered for live migration
  in Stories 6.2/7.2; this story does not claim Electron visibility.
- Final acceptance review: PASS for AC1-AC4.

### File List

- `_bmad-output/implementation-artifacts/m33/4-2-add-lanes-rails-terminal-strips-and-label-bands.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m33-sheet-composition-authority.test.mjs`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetStructureCompiler.kt`
- `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetStructureModels.kt`
- `kernel/drawing-composition/src/test/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetStructureCompilerTest.kt`

## Change Log

- 2026-07-23: Expanded BMAD story context with explicit structure intents, package identity
  preservation, deterministic density diagnostics, module boundaries, and live-path guardrails.
- 2026-07-23: Implemented professional structure facts, package identity preservation,
  deterministic transport, and fail-closed density/geometry diagnostics.
- 2026-07-23: Fixed adversarial duplicate-member and ordering findings, passed full regression, and
  closed the story.
