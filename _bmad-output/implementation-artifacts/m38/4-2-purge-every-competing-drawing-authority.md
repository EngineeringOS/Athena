---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.2: Purge Every Competing Drawing Authority

Status: done

## Story

As a platform maintainer,
I want one current drawing path and no stale alternatives,
so that future work cannot silently restore detached connections.

## Acceptance Criteria

1. Every retained active visible producer publishes strict `PresentationConnector` facts or is removed from active publication.
2. No production route-to-terminal fallback, legacy anatomy, compatibility shell, body-center endpoint, renderer repair, hardcoded sample policy, or duplicate connector lowerer remains in active code.
3. No production `src/main` type is named `Proof`, `Demo`, `Sample`, by milestone, or with `V0` or `V1`.
4. No alias, adapter, deprecation, migration default, or dead module exists solely to preserve pre-M38 behavior.
5. Repository audits enforce these prohibitions.
6. Affected tests, source-set hygiene audit, encoding audit, forbidden authority scan, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Remove active competing presentation authority (AC: 1, 2, 4)
  - [x] Delete public route-to-terminal attachment helper from `PresentationDocument`.
  - [x] Delete stale `usesCenterFallback`/accepted attachment contract tests or replace with strict connector tests.
  - [x] Keep `PresentationDocument.connectors` as the only visible connection collection.
- [x] Remove stale governed edit target for route facts (AC: 1, 2, 4)
  - [x] Delete `RouteFact` as a governed graphic mutation target from LSP protocol.
  - [x] Update affected tests to use current source-intent/connector edit boundary.
- [x] Add M38 authority audit guard (AC: 2, 3, 5)
  - [x] Add focused test or script coverage that scans production `src/main` for forbidden authority names and stale patterns.
  - [x] Allow current internal compiler/routing names only when they are not visible publication authority.
  - [x] Fail on production `Proof`, `Demo`, `Sample`, milestone type names, `V0`/`V1`, body-center fallback, renderer repair, public route snapshot publication, and duplicate connector lowering.
- [x] Verify active producers (AC: 1, 2, 5)
  - [x] Confirm professional drawing path uses `PresentationConnectorCompiler`.
  - [x] Confirm cabinet projection path publishes strict `PresentationConnector` values.
  - [x] Confirm Theia/LSP visible payload consumes `PresentationDocument.connectors`.
- [x] Verify sequentially (AC: 6)
  - [x] Run affected Gradle tests one command at a time.
  - [x] Run source-set hygiene audit.
  - [x] Run encoding audit.
  - [x] Run forbidden authority scan.
  - [x] Run `git diff --check`.

## Dev Notes

M38 says the visible connection authority is strict `PresentationConnector`, not route attachment facts
or frontend recovery. This story is cleanup and prevention. Do not add compatibility shims.

Hard rules:

- Stay in M38.
- No Java2D. Theia is the render layer.
- Pre-public cleanup: delete wrong paths directly.
- Do not alter M36/M37 examples for this story.
- Do not create a new trust/authority namespace or parallel model.
- `RouteFact` may remain internal compiler/routing input while M40 is deferred. It must not be public visible drawing authority.

Known current targets:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
  has `PresentationRouteAttachmentFact` and `attachRoutesToPresentationTerminals`; these pre-M38 helpers compete with strict connectors.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedGraphicEditIntentProtocol.kt`
  still exposes `RouteFact` as direct governed graphic mutation target.
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRouteAttachmentContractTest.kt`
  protects the stale attachment helper and should be removed or replaced.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
  is the current strict connector lowerer.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
  and `AthenaCabinetProjectionCompiler.kt` are active visible producers and must keep publishing connectors.

Previous story learning:

- Story 4.1 proved the dedicated M38 example compiles and resolves package-local representation material.
- The sample initially failed because future source concepts were used. Fixes went into source/material directly, not kernel compatibility.
- Material resolution must be tested directly when example geometry matters; generic presentations can contain connectors without graphic occurrences.

Testing requirements:

```powershell
.\gradlew.bat --no-daemon --console=plain -q :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain -q :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain -q :kernel:compiler:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-01: Story created from M38 sprint order after Story 4.1 reached review.
- 2026-08-01: Dev started from sprint order.
- 2026-08-01: Removed stale route-to-terminal presentation helper and its test.
- 2026-08-01: Removed `RouteFact` direct graphic mutation target from LSP governed edit protocol.
- 2026-08-01: Added M38 drawing authority audit and used it to catch and rename production `Proof` trace type to `Evidence` trace type.
- 2026-08-01: Verification passed sequentially: focused authority audit, presentation-model tests, LSP tests, full compiler tests, source-set hygiene audit, encoding audit, forbidden authority scan, and `git diff --check`.

### Completion Notes

- Deleted public `PresentationRouteAttachmentFact`, `attachRoutesToPresentationTerminals`, and `usesCenterFallback` path so Presentation connectors remain the visible connection authority.
- Removed stale direct `RouteFact` graphic mutation target from the LSP governed edit intent model and updated tests.
- Renamed professional drawing trace evidence input from production `Proof` naming to `Evidence` naming.
- Added production source authority audit for forbidden drawing-authority names and stale fallback patterns.

### File List

- `_bmad-output/implementation-artifacts/m38/4-2-purge-every-competing-drawing-authority.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRouteAttachmentContractTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGovernedGraphicEditIntentProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36GovernedGraphicEditIntentTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M37ProfessionalDrawingTraceEvidenceTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M38DrawingAuthorityAuditTest.kt`

### Change Log

- 2026-08-01: Created Story 4.2 context and marked ready for dev.
- 2026-08-01: Started Story 4.2 implementation.
- 2026-08-01: Purged competing drawing authority and marked story ready for review.
