---
status: done
story_id: 3.4
epic: 3
title: Recompile, Reproject, And Preserve Coherence
---

# Story 3.4: Recompile, Reproject, And Preserve Coherence

## Story

As an Athena user, I want accepted relationships to reappear as normal projection facts, so that
preview state cannot masquerade as committed engineering state.

## Acceptance Criteria

- Accepted mutation triggers compiler validation and projection refresh.
- New electrical relationship appears as committed route facts with terminal anchors and quality.
- Source, Problems, Semantic SCM, inspector, sheet selector, and graph remain coherent.
- Preview state is cleared after refresh.

## Tasks/Subtasks

- [x] Add failing projection-refresh tests for accepted relationship.
- [x] Wire mutation success to recompile/reproject path.
- [x] Assert committed route facts replace preview state.
- [x] Assert inspector/graph/source coherence.
- [x] Run focused runtime/frontend tests sequentially.

## Dev Notes

- Architecture: M28 AD-6 and AD-7 are binding.

## Dev Agent Record

### Debug Log

- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest.accepted semantic-relationship preview returns governed electrical connection source edit"` passed after adding projection/session assertions to the accepted relationship flow.

### Completion Notes

- Accepted semantic relationship source edits now recompile through tracked document changes and are visible in LSP projection session as committed connection facts.
- Projection coherence assertions cover committed connection payload, electrical endpoints for both ports, and electrical routing corridor publication.
- Authoring session state is asserted with zero pending previews and accepted preview status after mutation.

## File List

- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Added accepted relationship recompile/reprojection coherence assertions.
