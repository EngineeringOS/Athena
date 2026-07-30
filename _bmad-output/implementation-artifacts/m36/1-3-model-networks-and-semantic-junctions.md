---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E1.S3: Model Networks And Semantic Junctions

Status: done

## Story

As an engineer or AI agent,
I want to declare connection networks and semantic junctions explicitly,
So that topology is never inferred from drawing crossings.

**Requirements:** FR-11.

## Acceptance Criteria

1. An authored connection network has stable identity, member ports or connections, compatibility evidence, and provenance.
2. A semantic junction exists only through an authored network operation or a compiled junction fact.
3. A visual crossing alone never creates engineering connectivity.
4. Invalid network membership and incompatible junctions fail before lowering.

## Dev Notes

- Athena source is SSOT. Do not make SVG, renderer state, XML, layout, or routing authoritative.
- `connect group` already exists in the authored language surface as `ConnectionGroupDeclaration`; this story should add semantic network meaning without inventing new syntax.
- `ProjectSemanticDeclarationIndexer` and `ProjectSemanticReferenceLinker` currently flatten grouped connections as authored structure. Extend the compiler semantic path instead of changing parsing behavior.
- `connection-model` owns Connection and Connection Network facts. `routing-model` already owns downstream route junction and crossing facts; do not modify routing topology semantics in this story.
- Keep the vocabulary generic: Connectable Entity, Interface, Port, Connection, Network, Junction.
- Do not add SVG binding, layout, routing, physical placement, or ECS work in this story.
- Invalid membership, ambiguous membership, and incompatible junction claims must fail with source-spanned compiler/LSP diagnostics before lowering.
- Run RED first, then sequential Gradle verification, encoding audit, and `git diff --check`.

## Tasks / Subtasks

- [x] Add failing tests for authored network compilation and junction semantics (AC: 1-4)
  - [x] Cover a valid grouped network, an explicit junction fact, a crossing-only negative case, and invalid membership / incompatibility failures.
  - [x] Prove the tests exercise compiler-owned semantic lowering rather than parser or renderer behavior.
- [x] Extend `connection-model` with compiler-owned Network and Junction facts derived from canonical Engineering IR (AC: 1-2)
  - [x] Preserve stable identity, member references, compatibility evidence, and provenance.
  - [x] Keep the model transient and non-authoritative for layout, routing, or graphics.
- [x] Integrate network and junction diagnostics through the compiler semantic path and LSP publication (AC: 3-4)
  - [x] Report source-spanned diagnostics for invalid membership, incompatible junctions, and crossing-only topology claims.
  - [x] Reuse the existing compiler diagnostics pipeline; do not add a separate semantic scanner.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential targeted and regression tests, encoding audit, and diff check.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Agent Record

### Debug Log References

- 2026-07-28: Focused `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM36ConnectionNetworkCompilationTest` passed after aligning passive/bidirectional direction handling and removing an over-strict network minimum-size gate.
- 2026-07-28: `:extensions:domain-electrical:test` passed after widening runtime direction validation to accept passive/bidirectional ports.
- 2026-07-28: `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest` passed after updating the network-junction fixture to the mixed-orientation incompatible case.
- 2026-07-28: Full `:kernel:compiler:test` passed after the lowerer and contract checks were aligned with valid single-member and fan-out network groups.

### Completion Notes

- Implemented compiler-owned connection network lowering from authored `connect group` declarations into transient network and junction facts.
- Aligned network validation with shared-port topology so valid fan-out groups compile while mixed-orientation junctions fail with source-spanned diagnostics.
- Updated the electrical runtime and IDE diagnostics path to accept the new passive/bidirectional connectivity semantics.
- Verified with sequential Gradle tests: `:kernel:connection-model:test`, `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM36ConnectionNetworkCompilationTest`, `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest`, `:kernel:compiler:test`, and `:extensions:domain-electrical:test`.

### File List

- _bmad-output/implementation-artifacts/m36/1-3-model-networks-and-semantic-junctions.md
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeValidation.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringIrLowerer.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM36ConnectionNetworkCompilationTest.kt
- kernel/connection-model/src/main/kotlin/com/engineeringood/athena/connection/ConnectableEntityContracts.kt
- kernel/connection-model/src/test/kotlin/com/engineeringood/athena/connection/ConnectableEntityContractCompilerTest.kt
- _bmad-output/implementation-artifacts/m36/sprint-status.yaml

### Change Log

- 2026-07-28: Created Story M36-E1.S3 for semantic connection networks and junction facts.
- 2026-07-28: Implemented M36 network lowering, junction compatibility checks, and diagnostics publication; story ready for review.

## References

- `_bmad-output/implementation-artifacts/m36/epics.md` - M36-E1.S3.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-11.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md` - AD-1, AD-4, AD-7.
