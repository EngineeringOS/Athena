---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.2: Expose Package State Through `ide/lsp` As The Sole IDE Semantic Path

Status: done

## Story

As a language-tooling engineer,
I want package-state queries and updates to flow through `ide/lsp`,
so that the IDE consumes repository/package semantics without inventing a second semantic model.

## FR Traceability

- FR-7: upgrade the active runtime-backed repository session into a package graph session
- FR-9: surface package state in the existing Athena IDE path
- FR-10: keep language-surface hardening downstream of the same semantic authority
- FR-12: preserve later graphical projection without widening M5
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: protocol payloads remain deterministic and reproducible from the same repository state
- NFR-3: manifest, lock, graph, and diagnostics remain inspectable across the protocol boundary

## Acceptance Criteria

1. Given a `RepositoryGraphSession` exists in runtime, when the IDE requests package-aware repository state, then `ide/lsp` exposes that state through standard or Athena-namespaced protocol methods, and Theia frontend and backend do not define a parallel repository/package authority.
2. Given package state is exposed to the IDE, when the protocol boundary is reviewed, then the payloads are derived from JVM-owned repository-model, compiler, and runtime contracts, and package semantics remain inspectable across the protocol boundary.

## Tasks / Subtasks

- [x] Add a typed package-state request/response surface in `ide/lsp`. (AC: 1, 2)
  - [x] Introduce one or more typed request/payload classes for repository graph session state exposed through Athena-namespaced LSP requests.
  - [x] Keep payloads derived from `RepositoryGraphSession`, `RepositoryGraphReport`, and repository-model contracts instead of inventing Node-owned package models.
  - [x] Include inspectable repository/package essentials such as manifest identity, lock state, resolved packages, and package diagnostics.
- [x] Expose runtime-owned repository graph session state through the Athena language server. (AC: 1, 2)
  - [x] Add read-only request handlers in `AthenaLanguageServer` that answer package-state queries from the current runtime-owned session.
  - [x] Preserve the existing standard LSP document features and `athena/semanticInspection`; do not regress current editor flows.
  - [x] Keep package-state authority downstream of `RepositoryGraphSession`, not `AthenaRepositorySessionManager` or frontend caches.
- [x] Reuse the existing backend/frontend LSP request tunnel instead of creating a second package-state transport. (AC: 1)
  - [x] Use the existing `/athena/lsp/request` backend path and `AthenaRepositorySessionManager.sendRequest(...)` seam for new package-state requests.
  - [x] Add only the minimal typed frontend bridge method(s) needed to request package-state through LSP without surfacing UI feedback yet.
  - [x] Do not add new HTTP repository-state endpoints or frontend-owned package authority.
- [x] Cover authoritative package-state queries with focused LSP tests. (AC: 1, 2)
  - [x] Prove package-state requests return runtime-owned manifest, lock, resolved graph, and diagnostics data through Athena LSP.
  - [x] Prove the payload remains inspectable and deterministic for the same governed repository state.
  - [x] Prove existing semantic inspection or authoring support behavior remains intact after adding package-state requests.
- [x] Run sequential Java 25 verification for LSP and affected regressions. (AC: 1, 2)
  - [x] Run focused `ide:lsp` tests first.
  - [x] Run the wider sequential Gradle regression command.

## Dev Notes

### Story Intent

- Story `3.1` established the runtime-owned `RepositoryGraphSession`.
- Story `3.2` now makes `ide/lsp` the explicit IDE query boundary for repository/package session state, so later UI work consumes semantic package meaning through LSP instead of backend or frontend mirrors.

### Architecture Guardrails

- Align to AD-17: `ide/lsp` is the transport boundary for the runtime-owned `RepositoryGraphSession`.
- Align to AD-18: stay additive and package-operability-scoped.
- Preserve Epic 2 compiler/runtime authority: package payloads must come from `RepositoryGraphSession` publication output, not from Node-side reconstruction.
- Avoid turning Story `3.2` into Story `3.3`; visible workbench presentation belongs later.

### Technical Requirements

- Reuse the existing runtime and LSP seams:
  - `RepositoryGraphSession`
  - `AthenaLspSessionHostReady.session`
  - `AthenaLanguageServer`
  - `AthenaRepositorySessionManager.sendRequest(method, params)`
  - `AthenaLspEditorBridgeService.sendLanguageRequest(method, params, model?)`
- Existing request pattern already proven:
  - `athena/semanticInspection`
- Story `3.2` should add:
  - one or more read-only Athena-namespaced LSP package-state request handlers
  - typed request/payload contracts shared inside JVM LSP code
  - minimal typed frontend bridge accessors if useful for future UI consumption
- Keep this story out of scope for:
  - new backend HTTP repository-state endpoints
  - frontend widgets or workbench surfaces that render package graph information
  - command/update workflows beyond the existing LSP notification/request tunnel
  - semantic SCM or graphical projection concerns

### Architecture Compliance

- Prevent these failure modes:
  - package-state payloads reconstructed in TypeScript instead of projected from JVM runtime authority
  - `AthenaRepositorySessionManager` becoming a second semantic package model
  - package-state requests bypassing `ide/lsp` through new backend HTTP contracts
  - Story `3.2` silently expanding into `3.3` UI feedback work
  - existing `athena/semanticInspection`, diagnostics publishing, completion, definition, or references regressing

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- Likely new files:
  - possibly one new LSP payload/request model file under `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - one or more focused LSP tests under `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Files whose current behavior must be preserved:
  - `ide/theia-backend/src/node/athena-repository-session-manager.ts`
    - keep using the existing generic request tunnel unless a tiny typed convenience is truly needed
  - `ide/theia-frontend/src/browser/athena-repository-session-service.ts`
    - should not become a second package authority
  - `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
    - current semantic inspection path should continue to work unchanged

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - package-state requests return runtime-owned repository graph session data
  - payload fields are inspectable and deterministic
  - existing Athena LSP request/diagnostic/authoring tests remain green
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `AthenaLanguageServer` already exposes `athena/semanticInspection` through a typed Athena request.
- `AthenaRepositorySessionManager` already provides a generic LSP request tunnel to the running JVM host.
- `AthenaLspEditorBridgeService` already routes a typed semantic inspection request through `/athena/lsp/request`.
- Story `3.1` already moved repository/package authority into `RepositoryGraphSession`; `3.2` must query that state rather than rebuild it.

### Previous Story Intelligence

- Story `3.1` intentionally kept the transport summary narrow while introducing `RepositoryGraphSession`.
- Story `2.4` already proved the canonical repository publication seam that now feeds the session.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical workspace structure must match architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent M5 work progressed in a narrow sequence: contract -> input -> graph -> lock -> published report -> runtime session.
- Practical implication:
  - Story `3.2` should be the narrow protocol step that exposes existing authority, not a UI expansion.

### Latest Technical Information

- No web research is required for Story `3.2`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `3.2` should leave clean room for:
  - Story `3.3` IDE package feedback surfaces
  - Story `3.4` narrow editor hardening
  - later M6 semantic SCM flows

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-32-expose-package-state-through-idelsp-as-the-sole-ide-semantic-path]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/3-1-upgrade-the-active-runtime-session-into-a-repository-graph-session.md]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt]
- [Source: ide/theia-backend/src/node/athena-repository-session-manager.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, and architecture review for the sole IDE semantic path
- Story `3.1` completion notes and repository graph session review
- CodeGraph review of language server, repository session service, and semantic inspection request flow
- frontend/backed request tunnel review

### Completion Notes List

- Added `athena/repositoryGraphSession` as a typed Athena LSP request backed directly by the runtime-owned `RepositoryGraphSession`.
- Added typed repository/package protocol payloads covering manifest dependency intent, resolved packages, lock state, and package diagnostics without introducing a Node-owned semantic model.
- Added focused LSP tests for canonical repository session payloads and malformed lock diagnostics over the same request boundary.
- Added a minimal typed frontend bridge method on top of the existing `/athena/lsp/request` transport and regenerated the frontend build outputs.
- Verified sequentially with `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`, `yarn workspace @engineeringood/athena-theia-frontend build`, and `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

### File List

- _bmad-output/implementation-artifacts/m5/3-2-expose-package-state-through-ide-lsp-as-the-sole-ide-semantic-path.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionRequestTest.kt
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map

## Change Log

- 2026-07-09: Created Story `3.2` context and moved it to ready-for-dev.
- 2026-07-09: Implemented typed repository graph session requests through Athena LSP, added the minimal frontend bridge method, and moved the story to review after sequential Java 25 and frontend build verification.
