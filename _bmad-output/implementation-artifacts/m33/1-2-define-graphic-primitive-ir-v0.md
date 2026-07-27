---
story_id: 1.2
story_key: 1-2-define-graphic-primitive-ir-v0
epic: 1
status: done
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
created: '2026-07-23'
updated: '2026-07-23'
---

# Story 1.2: Define Graphic Primitive IR v0

## Status

Done

## Story

As a renderer developer, I want renderer-neutral Graphic Primitive IR so representation output does
not directly become SVG.

## Acceptance Criteria

- IR supports line, polyline, arc, circle, rectangle, text, marker, connection dot, reference
  arrow, group, transform, bounds, and style token.
- IR validation rejects engineering truth, package resolution behavior, source mutation behavior,
  DOM selectors, and SVG-specific authority.
- IR is serializable and deterministic without Theia/browser runtime.
- AC-to-evidence and polish/purge notes are recorded before review.

## Tasks/Subtasks

- [x] Add RED contract tests for every Graphic Primitive IR v0 primitive, transform, bounds, and
  typed style token. (AC: 1,3)
- [x] Add RED diagnostics tests for missing scene fields, invalid geometry, unresolved style
  tokens, and forbidden authority. (AC: 2,3)
- [x] Implement deterministic renderer-neutral Graphic Primitive IR models and transport maps in
  the existing representation model boundary. (AC: 1,3)
- [x] Implement fail-closed validation for geometry, bounds, style tokens, and authority leaks.
  (AC: 2,3)
- [x] Inspect adjacent direct renderer primitive types and remove or ledger retained compatibility
  paths. (AC: 4)
- [x] Run focused, module, and repository regression tests sequentially on Windows. (AC: 1..4)
- [x] Complete mandatory AC-to-evidence and polish/purge review. (AC: 4)

## Dev Notes

- Bind to M33 architecture AD-2.
- SVG is first adapter, not authority. Avoid names like `svgPath` in core IR unless it is adapter
  payload.
- Keep style tokens declarative; do not embed CSS selectors as meaning.

## Testing

- Unit tests for primitive variants, bounds, transforms, text placement, style tokens.
- Negative tests for forbidden semantic/DOM/SVG authority fields.

## Evidence Plan

- Tests prove IR stability and validation.
- AC-to-evidence maps each primitive family to test proof.

## Polish And Purge

Remove or ledger direct renderer primitive types that duplicate the new IR.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- CodeGraph located the existing backend-neutral `PresentationShapeCommand`, SVG-specific
  `PresentationSvgPath`, M30 `PresentationPrimitive`, and current `SvgRenderer` boundaries.
- RED: focused test failed during test compilation because Graphic Primitive IR v0 and its
  validator did not exist.
- GREEN: focused `GraphicPrimitiveIrContractTest` passed after the minimal IR and validator were
  added.
- REGRESSION: `:kernel:representation-model:test` passed.
- FULL REGRESSION: `gradlew test` passed with 147 actionable tasks (25 executed, 122 up-to-date).
- TEXT: `tools/encoding-audit.ps1` passed.
- REVIEW RED: acceptance audit failed because primitive transport omitted geometry/tree content and
  validation accepted ambiguous ids, non-finite values, and cyclic nesting.
- REVIEW GREEN: focused IR tests, `:kernel:representation-model:test`, full `gradlew test`, and
  encoding audit passed after the review fixes.

### Completion Notes List

- Added renderer-neutral line, polyline, arc, circle, rectangle, text, marker, connection dot,
  reference arrow, group, and transformed primitive contracts.
- Added typed bounds, points, transforms, line/text style policy, stable ids, and deterministic
  transport maps.
- Added deterministic diagnostics for missing scene fields, invalid bounds/geometry/style,
  unresolved style references, and forbidden engineering/package/source/DOM/CSS/SVG authority.
- AC-to-evidence: AC1 and AC3 are covered by
  `graphic primitive document supports the complete renderer neutral v0 vocabulary`; AC2 is covered
  by the three validator tests; AC4 is covered by full regression, encoding audit, authority scan,
  and cleanup-ledger entries.
- Polish/purge: retained `PresentationSvgPath` and M30 `PresentationPrimitive` only because current
  consumers still depend on them; both are ledgered for Story 3.1 migration.
- Review follow-up: added complete recursive primitive/style/transform transport, controlled paint
  tokens, global id uniqueness, finite numeric checks, bounds containment, and cycle/depth guards.
- Final acceptance review: PASS for AC1-AC4; the final auditor confirmed complete Graphic Primitive
  transport and fail-closed validation after review fixes.

### File List

- `_bmad-output/implementation-artifacts/m33/1-2-define-graphic-primitive-ir-v0.md`
- `_bmad-output/implementation-artifacts/m33/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m33/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveTransport.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveValidation.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveIrContractTest.kt`

## Change Log

- 2026-07-23: Implemented and verified renderer-neutral Graphic Primitive IR v0.
- 2026-07-23: Addressed Epic 1 code-review findings for lossless transport and fail-closed IR validation.
- 2026-07-23: Final acceptance review passed; story closed.
