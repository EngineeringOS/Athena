---
status: done
story_id: 2.3
epic: 2
title: Validate Relationship Compatibility And Persistence Eligibility
---

# Story 2.3: Validate Relationship Compatibility And Persistence Eligibility

## Story

As an Athena user, I want invalid relationships blocked before persistence, so that the source graph
cannot be corrupted by the canvas.

## Acceptance Criteria

- Compatible output-to-input electrical subjects may proceed to preview.
- Output-to-output, input-to-input, signal mismatch, duplicate connection, ambiguous owner, and
  dirty/invalid source states are rejected.
- Rejections return governed diagnostics.
- Rejections leave `.athena` source unchanged.

## Tasks/Subtasks

- [x] Add failing compatibility validator tests.
- [x] Implement minimal electrical compatibility rules.
- [x] Implement persistence eligibility checks for deterministic source ownership.
- [x] Return diagnostics suitable for preview/inspector.
- [x] Run focused runtime/compiler tests sequentially.

## Dev Notes

- Architecture: M28 AD-1, AD-5, AD-7 are binding.
- Do not expand into standards-complete IEC validation.

## Dev Agent Record

### Debug Log

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test --tests "com.engineeringood.athena.authoring.SemanticRelationshipCompatibilityValidatorTest"` failed because `SemanticRelationshipValidationRequest` and the validator contract were unresolved.
- GREEN: the same focused validator test passed after adding the validation request/result/diagnostic model and electrical validator.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test` passed.

### Completion Notes

- Added a governed M28 validation contract for semantic relationship authoring eligibility.
- Implemented the electrical v0 compatibility rules: output-to-input direction, matching signal, duplicate connection rejection, deterministic owner requirement, and dirty/invalid source blocking.
- Validation returns structured diagnostics and echoes source text unchanged; it has no mutation path.

## File List

- kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/SemanticRelationshipValidationModels.kt
- kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/SemanticRelationshipCompatibilityValidatorTest.kt

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented electrical semantic relationship compatibility and persistence eligibility validation.
