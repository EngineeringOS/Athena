---
status: done
story_id: 3.3
epic: 3
title: Accept Relationship Through M8 Mutation Authority
---

# Story 3.3: Accept Relationship Through M8 Mutation Authority

## Story

As an Athena user, I want accepted relationships to persist through governed mutation, so that
`.athena` remains today's source of truth.

## Acceptance Criteria

- Theia sends `SemanticRelationshipIntent` to mutation authority and does not write source text.
- Mutation authority serializes valid electrical relationships as deterministic `connect A.p -> B.q`.
- Failed acceptance leaves source unchanged and returns diagnostics.
- Any component anatomy generated or reshaped by M28 prefers nested port syntax.

## Tasks/Subtasks

- [x] Add failing mutation acceptance tests.
- [x] Wire Theia/LSP/runtime command to M8 mutation authority.
- [x] Implement deterministic source serialization for accepted electrical relationship.
- [x] Block ambiguous persistence targets.
- [x] Run focused mutation/LSP/frontend tests sequentially.

## Dev Notes

- Architecture: M28 AD-5, AD-6, AD-7 are binding.

## Dev Agent Record

### Debug Log

- RED: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest.accepted semantic-relationship preview returns governed electrical connection source edit"` first failed because `SemanticRelationshipIntent` made runtime preview handling non-exhaustive, then failed because the LSP decision dispatcher returned no source edit for generic relationships.
- RED: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest.accepted create-component preview returns source edit and graph-source state can rebuild through tracked text"` failed because LSP semantic inspection could not resolve source ranges for generated nested ports.
- GREEN: the focused semantic-relationship acceptance test passed after adding generic protocol conversion and source-edit routing.
- GREEN: the focused create-component test passed after nested port source-range lookup was added.
- GREEN: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-authoring-protocol.test.mjs } else { exit $LASTEXITCODE }` passed.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"` passed.

### Completion Notes

- Added generic `semantic-relationship` authoring transport fields while preserving legacy `connect-ports` compatibility.
- Runtime preview handling is exhaustive for `SemanticRelationshipIntent`.
- Accepted electrical semantic relationships serialize through the backend source-edit authority as deterministic `connect A.p -> B.q`; Theia only submits the intent/decision.
- Generated component anatomy now prefers nested `port name { ... }` syntax, and component updates preserve nested ports.
- LSP semantic inspection now maps nested port declarations back to source ranges.

## File List

- ide/theia-frontend/src/browser/athena-authoring-protocol.ts
- ide/theia-frontend/scripts/athena-authoring-protocol.test.mjs
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaConnectPortsSourceEditProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaUpdateComponentSourceEditProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt

## Change Log

- 2026-07-21: Story created for M28.
- 2026-07-21: Implemented generic relationship acceptance, backend source serialization, and nested generated anatomy preservation.
