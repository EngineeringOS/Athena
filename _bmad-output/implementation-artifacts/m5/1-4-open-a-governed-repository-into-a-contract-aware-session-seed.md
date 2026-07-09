---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.4: Open A Governed Repository Into A Contract-Aware Session Seed

Status: done

## Story

As an engineer,
I want Athena to open a governed repository into validated contract-aware seed state,
so that repository work starts from governed package meaning before the full repository graph session is established.

## FR Traceability

- FR-1: stable repository-root manifest contract
- FR-3: primary package identity and governed source layout rules
- FR-5: package-aware diagnostics through the semantic boundary
- FR-6: canonical semantic authority remains in JVM services
- FR-8: repository open flow targets the governed M5 contract
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-3: contract validation output remains inspectable
- NFR-4: extend the current M4 IDE shell without rewriting it

## Acceptance Criteria

1. Given an existing governed Athena repository, when the repository is opened through the current Athena product path, then Athena loads the repository-root contract, validates it, and establishes initial contract-aware open state from that contract, and repository open no longer depends on one preferred authored file as the primary repository meaning, and the full `RepositoryGraphSession` upgrade remains a later story concern.
2. Given the opened repository has contract problems, when the open flow runs, then Athena surfaces contract-aware diagnostics early through the same semantic boundary used by the IDE path, and invalid contract state is visible without moving semantic authority into the frontend.

## Tasks / Subtasks

- [x] Replace the old M4 repository-open resolver with a governed contract-aware seed loader. (AC: 1, 2)
  - [x] Load repository meaning from the compiler-owned repository contract validator rather than from one discovered `.athena` file.
  - [x] Keep the open result narrow: it may still choose one deterministic authored source as an initial editor seed for the temporary runtime path, but that file must no longer define repository meaning.
  - [x] Preserve contract authority in JVM code under `ide/lsp` and `kernel/compiler`; do not move validation logic into Theia frontend or backend.
- [x] Establish contract-aware session seed state for the current LSP initialize flow. (AC: 1)
  - [x] Update the LSP session host result and transport snapshot so the open payload reflects governed repository state rather than the old single-source-only assumption.
  - [x] Keep the current runtime activation path compatible without widening into the later `RepositoryGraphSession` story.
  - [x] Derive the user-facing project seed deterministically from governed repository data.
- [x] Surface invalid contract state through the current IDE semantic path. (AC: 2)
  - [x] Return early, compiler-owned contract diagnostics when `athena.yaml`, `athena.lock`, or governed layout validation fails.
  - [x] Make those diagnostics visible to the Theia session activation path without inventing a frontend-owned validator.
  - [x] Keep the error payload focused on repository-contract problems; dependency resolution and package graph work remain later stories.
- [x] Update focused tests for valid, multi-source, and invalid repository-open scenarios. (AC: 1, 2)
  - [x] Prove that a valid governed repository with multiple `.athena` files can still open because repository meaning comes from the contract, not from a single discovered file.
  - [x] Prove that invalid governed repositories fail open with contract-aware diagnostics.
  - [x] Run Java 25 Gradle verification sequentially, and run any relevant Theia verification sequentially if the transport contract changes outside JVM code.

## Dev Notes

### Story Intent

- Story `1.2` proved compiler-owned repository contract validation.
- Story `1.3` changed repository creation to emit the governed M5 repository shape.
- Story `1.4` now changes repository open so the IDE path starts from governed contract meaning instead of the old M4 "find exactly one source" proof rule.
- Story `3.1` still owns the later `RepositoryGraphSession` upgrade, so `1.4` must stop at seed state and compatibility shims.

### Architecture Guardrails

- Align to AD-11 and AD-12 by treating repository-root `athena.yaml`, `athena.lock`, and the governed `src/` root as the open-path authority.
- Align to AD-13 by consuming `:kernel:repository-model` contracts instead of defining parallel repository/package nouns under `ide/lsp`.
- Align to AD-17 by keeping this story at contract-aware open seed state rather than implementing the full package graph runtime session.
- Preserve inherited AD-3 and AD-5: Theia may trigger repository open, but semantic authority and diagnostics remain downstream in JVM services.
- Preserve AD-18 by keeping IDE changes additive and package-operability-scoped.

### Technical Requirements

- The compiler seam is `AthenaCompiler.validateRepositoryContract(repositoryRoot)`.
- The current LSP-owned session seam is `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`.
- The current initialize transport seam is `AthenaLanguageServer.initialize()` and `AthenaLspSessionSnapshot.toTransportPayload()`.
- The Theia backend session consumer is `ide/theia-backend/src/node/athena-repository-session-manager.ts`.
- The old `AthenaRepositoryResolver` currently rejects repositories with multiple `.athena` files; that behavior must no longer define repository-open meaning.
- The current runtime still requires one `projectName` and one `sourcePath`, so this story may derive a deterministic initial seed file for compatibility as long as that file is treated as a temporary activation seed, not repository authority.
- Do not introduce dependency resolution, lock materialization, repository graph session state, or frontend semantic ownership here.

### Architecture Compliance

- Prevent these failure modes:
  - repository open still fails only because more than one authored source exists under governed `src/`
  - contract validation is duplicated in Node/Theia code
  - initialize success or failure hides the actual repository-contract diagnostics
  - the story widens into full package graph runtime state or dependency resolution

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node.js `22+`
  - Yarn `1.22.22`

### File Structure Requirements

- Expected update files:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt` or a narrowly replaced/open-seed equivalent under the same module
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
  - `ide/theia-backend/src/node/athena-repository-session-manager.ts` only if the transport payload needs small additive session-state updates
- Expected test files:
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServerTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt`
- Files whose current behavior must be preserved:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoader.kt`
    - remains the validation authority introduced by Story `1.2`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`
    - still activates one temporary project seed for the current runtime path

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- The current Theia session manager already launches the JVM LSP host and reads initialize payloads from `capabilities.experimental`.
- The current LSP initialize path still activates runtime with one `projectName` and one `sourcePath`.
- The current repository-create flow already emits governed `athena.yaml`, `athena.lock`, and `src/`.

### Previous Story Intelligence

- Story `1.2` provides the compiler-owned repository contract loader and diagnostics model this story should consume.
- Story `1.3` guarantees a fresh governed repository already satisfies the contract, so open-path tests should include both fresh valid seeds and deliberately broken repositories.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical structure and docs must match the architecture
  - Gradle verification must stay sequential on Windows

### Latest Technical Information

- No web research is required for Story `1.4`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `1.4` should leave Story `3.1` room to introduce `RepositoryGraphSession` cleanly.

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-14-open-a-governed-repository-into-a-contract-aware-session-seed]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/1-2-validate-repository-root-contract-and-primary-package-layout.md]
- [Source: _bmad-output/implementation-artifacts/m5/1-3-create-a-new-governed-athena-repository.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt]
- [Source: ide/theia-backend/src/node/athena-repository-session-manager.ts]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, architecture spine, and Stories `1.2` and `1.3`
- CodeGraph exploration of the current M5 repository-open seams
- current compiler repository-contract validation seam review
- current LSP initialize/session-host/repository-resolver review
- current Theia repository-session-manager transport review
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `yarn workspace @engineeringood/athena-theia-backend build`
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`

### Completion Notes List

- Reworked the LSP repository-open adapter so it validates the governed repository contract first, then derives one deterministic authored-source seed only for the temporary runtime activation path.
- Expanded the LSP session payload to include manifest, lock, source-root, and primary-package metadata so the open state is contract-aware instead of single-file-centric.
- Preserved invalid-open authority in the JVM path by returning compiler-owned repository-contract diagnostics through the existing initialize/session failure path.
- Updated the home surface and session types to reflect governed repository open state without moving validation into Theia code.
- Added governed test fixtures plus multi-source and invalid-contract coverage for the repository resolver and language-server initialize flow.
- Passed sequential Java 25 and Theia verification, including the full repository `build`.

### File List

- _bmad-output/implementation-artifacts/m5/1-4-open-a-governed-repository-into-a-contract-aware-session-seed.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryDescriptor.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSupportTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServerTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspTestFixtures.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepeatedEditingStabilityTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticInspectionTest.kt
- ide/theia-backend/src/node/athena-repository-session-manager.ts
- ide/theia-backend/lib/node/athena-repository-session-manager.d.ts
- ide/theia-backend/lib/node/athena-repository-session-manager.d.ts.map
- ide/theia-backend/lib/node/athena-repository-session-manager.js
- ide/theia-backend/lib/node/athena-repository-session-manager.js.map
- ide/theia-frontend/src/browser/athena-home-widget.tsx
- ide/theia-frontend/src/browser/athena-repository-session-service.ts
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-home-widget.js
- ide/theia-frontend/lib/browser/athena-home-widget.js.map
- ide/theia-frontend/lib/browser/athena-repository-session-service.d.ts
- ide/theia-frontend/lib/browser/athena-repository-session-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-repository-session-service.js
- ide/theia-frontend/lib/browser/athena-repository-session-service.js.map

## Change Log

- 2026-07-09: Created Story `1.4` context and moved it to ready-for-dev.
- 2026-07-09: Implemented contract-aware repository open, widened the session payload with governed contract metadata, updated Theia session surfaces, added governed LSP fixtures/coverage, and passed sequential Java 25 plus Theia verification.
