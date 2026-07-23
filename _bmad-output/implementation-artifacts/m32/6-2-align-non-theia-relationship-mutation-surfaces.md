---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 6.2
epic: 6
title: Align Non-Theia Relationship Mutation Surfaces
---

# Story 6.2: Align Non-Theia Relationship Mutation Surfaces

## Status

Review

## Story

As an Athena multi-surface maintainer,
I want CLI, desktop, and Compose relationship mutation surfaces aligned or retired,
so that `SemanticRelationshipIntent` remains the single relationship authoring contract.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/6-1-consolidate-authoring-preview-compatibility.md`

## Acceptance Criteria

1. Given non-Theia surfaces using low-level `AthenaConnectPortsCommand`, when migration audit runs,
   then each surface uses `SemanticRelationshipIntent` or is explicitly retired with tests and
   documentation.
2. Given relationship mutation commands are searched, when fixed-string and CodeGraph caller checks
   run, then no unowned mutable relationship path bypasses M28/M31 authority.
3. Given the story implementation is complete, when CLI, desktop, Compose, runtime, tests, docs,
   cleanup ledger, and sprint artifacts are reviewed, then stale command paths are removed or
   ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Run CodeGraph and fixed-string audit for `AthenaConnectPortsCommand`,
  `SemanticRelationshipIntent`, CLI, desktop, and Compose mutation surfaces. (AC: 1,2)
- [x] Add RED tests proving non-Theia relationship mutation compatibility is aligned to
  `SemanticRelationshipIntent` or explicitly retired. (AC: 1,2)
- [x] Implement alignment/retirement proof at the owning runtime/CLI boundary. (AC: 1,2)
- [x] Update docs and cleanup ledger/action status for the retained or retired paths. (AC: 3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Use CodeGraph before grep/read on relationship command surfaces.
- Do not migrate by adding a second relationship mutation contract. `SemanticRelationshipIntent`
  is the target contract.
- If a surface is not present in this repository, record it as retired/absent with scan evidence
  rather than inventing placeholder code.
- Preserve M32 sample smoke/density proof.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing compatibility/audit tests before implementation.
- Focused command should target the runtime or CLI module touched by implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

TBD by dev-story agent.

### Debug Log References

- CodeGraph exploration: `AthenaConnectPortsCommand SemanticRelationshipIntent
  AthenaCommandRuntimeService CLI desktop Compose relationship mutation surfaces compatibility
  contract`; result showed `AthenaConnectPortsCommand` blast radius in runtime and
  `SemanticRelationshipIntent` owned product/Theia authoring path.
- Fixed-string audit:
  `rg -n "AthenaConnectPortsCommand|connect-ports|connect ports|SemanticRelationshipIntent" apps ide kernel extensions --glob '!**/build/**'`
  and repository audit excluding `reference/**` and `_bmad-output/**`.
- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaCommandRuntimeTest`
  failed at `AthenaCommandRuntimeTest.kt` with unresolved `compatibilityContract`.
- GREEN: same focused runtime test passed after adding
  `legacy-connect-ports-runtime-command-v1`.
- Adjacent verification:
  `.\gradlew.bat --no-daemon --console=plain :apps:cli:test` passed.
- Desktop module check:
  `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test` failed because
  `settings.gradle.kts` comments out `:apps:desktop-viewer`; source remains audited and
  compatibility-ledgered.
- Adjacent verification:
  `.\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test` passed.
- Full regression:
  `.\gradlew.bat --no-daemon --console=plain check` passed.
- Encoding:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added an explicit runtime relationship mutation compatibility contract for
  `AthenaConnectPortsCommand`.
- Contract ids the retained command as `legacy-connect-ports-runtime-command-v1`, names
  `SemanticRelationshipIntent` as the product authoring contract, and declares no mutable
  source-authoring authority.
- Closed cleanup ledger item `M32-CL-002` and marked the Story 6.2 sprint action done.
- AC-to-evidence:
  - AC1: runtime contract test proves retained non-Theia command is versioned compatibility
    against `SemanticRelationshipIntent`; docs/ledger name CLI, desktop Compose, and
    domain-electrical runtime.
  - AC2: CodeGraph plus fixed-string audit found no Theia product `connect-ports` authoring
    path; remaining command hits are runtime compatibility surfaces, tests, or docs.
  - AC3: docs, cleanup ledger, sprint action, and story evidence updated.
  - AC4: polish/purge ran via full `check`, encoding audit, and source/status review.

### File List

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `docs/usages/engineering-package-platform.md`
- `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m32/6-2-align-non-theia-relationship-mutation-surfaces.md`

## Change Log

- 2026-07-22: Story created for M32 Epic 6 non-Theia relationship mutation surface alignment.
- 2026-07-22: Added runtime compatibility contract, verification, cleanup ledger closeout, and
  AC-to-evidence mapping; moved story to review.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent runtime, CLI, desktop/Compose availability, docs, tests, cleanup
  ledger, and sprint artifacts.
- Remove stale low-level relationship command paths or ledger retained/absent compatibility with
  owner and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
