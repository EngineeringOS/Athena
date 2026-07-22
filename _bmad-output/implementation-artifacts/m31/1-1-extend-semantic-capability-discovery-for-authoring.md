---
status: done
baseline_commit: cbe65c3fcf0fe4f05e5edfa2eb98edbe4f3e237e
story_id: 1.1
epic: 1
title: Extend Semantic Capability Discovery For Authoring
---

# Story 1.1: Extend Semantic Capability Discovery For Authoring

## Status

Done

## Story

As an engineering author,
I want available authoring actions derived from the existing semantic capability registry,
so that Graphical View never invents what can be authored.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md`
- Contract: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md`
- Epics: `_bmad-output/implementation-artifacts/m31/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`

## Acceptance Criteria

1. Given M29 `SemanticCapabilityRegistry`, when a domain provider contributes authoring capability,
   then typed evidence carries capability id, intent kind, subject/context, actor policy outcome,
   domain/template/projection/representation requirement evidence, and no parallel registry exists.
2. Given an unsatisfied domain, template, projection, or representation requirement, when
   authoring capability is discovered, then evidence is absent and
   `authoring.capability.unavailable` identifies the failed requirement.
3. Given an actor origin outside the capability policy, when discovery runs, then the capability is
   unavailable with structured diagnostics.
4. Given adapter metadata contains coordinates or widget ids, when typed authoring evidence is
   built, then adapter metadata cannot enter eligibility or semantic identity.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add failing tests for eligible authoring evidence, unsatisfied requirements, actor policy,
  and adapter-metadata exclusion. (AC: 1,2,3,4)
- [x] Add focused authoring capability models within `kernel/interaction-model`. (AC: 1,2,3)
- [x] Extend `SemanticCapability` and `SemanticCapabilityRegistry` with typed authoring discovery.
  (AC: 1,2,3,4)
- [x] Run focused and full interaction-model tests sequentially. (AC: 1,2,3,4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- Extend the existing registry; do not introduce an `AuthoringCapabilityRegistry`.
- Keep `kernel/interaction-model` independent from `kernel/authoring-model`, Theia, DOM, SVG, and
  adapter coordinate types.
- Model requirement satisfaction explicitly so unavailable template/representation capability is a
  diagnostic, not missing optional data guessed downstream.
- Preserve existing `SemanticCapability` call sites through source-compatible defaults during this
  story; later M31 cleanup may remove obsolete paths after migration.
- Use TDD. Run Gradle commands strictly sequentially on Windows.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with focused interaction-model test failing on missing authoring
  capability models and registry discovery.
- 2026-07-21: GREEN confirmed after adding typed requirement evidence and existing-registry
  discovery.
- 2026-07-21: Final full `:kernel:interaction-model:test` passed after purge refactor.
- 2026-07-21: Encoding audit and diff check passed.

### Completion Notes

- Added typed authoring intent kinds, requirement status, capability, evidence, and discovery result
  models inside the existing interaction boundary.
- Extended `SemanticCapability` with source-compatible authoring metadata and added registry-owned
  actor/requirement eligibility checks.
- Unavailable actor policy or domain/template/projection/representation requirement returns
  `authoring.capability.unavailable`; adapter metadata is excluded from evidence.
- Polish/purge found no parallel registry, stale production path, or temporary artifact. Removed
  duplicated test setup and reran final verification.
- AC evidence: AC1/AC4 `registry exposes typed authoring capability evidence`; AC2 unsatisfied
  requirement test; AC3 actor policy test; AC5 final module/encoding verification.

## File List

- `_bmad-output/implementation-artifacts/m31/1-1-extend-semantic-capability-discovery-for-authoring.md`
- `_bmad-output/implementation-artifacts/m31/sprint-status.yaml`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/AuthoringCapabilities.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionModels.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/SemanticCapabilityRegistry.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/AuthoringCapabilityDiscoveryTest.kt`

## Change Log

- 2026-07-21: Story created for M31.
- 2026-07-21: Implemented typed authoring capability discovery through the existing semantic registry.
- 2026-07-21: Completed story review and mandatory polish/purge verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent capability, interaction, test, documentation, and compatibility paths.
- Remove dead or stale artifacts; ledger any retained item with owner, reason, target milestone,
  and verification.
- Run final story verification after cleanup and record AC-to-evidence mapping.
