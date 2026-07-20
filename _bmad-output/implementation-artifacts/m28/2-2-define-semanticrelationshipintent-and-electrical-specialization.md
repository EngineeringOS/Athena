---
status: done
story_id: 2.2
epic: 2
title: Define SemanticRelationshipIntent And Electrical Specialization
---

# Story 2.2: Define SemanticRelationshipIntent And Electrical Specialization

## Story

As an Athena platform engineer, I want a generic relationship intent with an electrical
specialization, so that M28 does not hard-code ECAD vocabulary as the platform architecture.

## Acceptance Criteria

- Relationship authoring uses `SemanticRelationshipIntent` as the generic contract.
- M28 electrical authoring uses `ElectricalConnectionRelationship` specialization.
- The model includes relationship type, source subject, target subject, projection context,
  persistence target, and provenance.
- Future relationship types can be represented without renaming the root contract.

## Tasks/Subtasks

- [x] Locate existing M8 mutation intent vocabulary.
- [x] Add failing tests for generic relationship intent construction.
- [x] Implement model and mapping for electrical specialization.
- [x] Keep naming generic at architecture seams.
- [x] Run focused runtime/mutation tests sequentially.

## Dev Notes

- Architecture: M28 AD-1 and AD-5 are binding.
- Avoid introducing `ConnectPortsIntent` as the root type.

## Dev Agent Record

### Debug Log

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test --tests "com.engineeringood.athena.authoring.AuthoringIntentContractTest.semantic relationship intent is generic while electrical connection is a specialization"` failed with unresolved `SemanticRelationshipIntent`, `ElectricalConnectionRelationship`, `SemanticRelationshipProjectionContext`, and `SemanticRelationshipPersistenceTarget`.
- GREEN: the same focused generic relationship test passed after adding the model.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test --tests "com.engineeringood.athena.authoring.AuthoringIntentContractTest.legacy connect ports intent lifts into electrical semantic relationship intent"` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test` passed.

### Completion Notes

- Added platform-owned `SemanticRelationshipIntent` with generic relationship type, subject identities, projection context, persistence target, and provenance.
- Added `ElectricalConnectionRelationship` as the M28 electrical specialization without making ECAD connection vocabulary the root contract.
- Kept existing `ConnectPortsIntent` as a legacy compatibility shape and added an explicit lift into `SemanticRelationshipIntent`.

## File List

- kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt
- kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringIntentContractTest.kt

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented generic semantic relationship intent and electrical compatibility specialization.
