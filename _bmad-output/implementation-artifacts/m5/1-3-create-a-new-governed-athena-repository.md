---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 1.3: Create A New Governed Athena Repository

Status: done

## Story

As an engineer,
I want Athena to create a repository using the governed M5 contract,
so that new work starts from the correct repository shape instead of the old M4 proof bootstrap.

## FR Traceability

- FR-1: stable repository-root manifest contract
- FR-2: deterministic root lock contract
- FR-3: primary package identity and governed source layout rules
- FR-8: repository create flow targets the governed M5 contract
- NFR-1: repository/package meaning remains downstream of compiler/runtime authority
- NFR-2: created repository structure must be deterministic
- NFR-3: created manifest and lock state must stay inspectable
- NFR-4: adapt the current M4 shell rather than rewriting it

## Acceptance Criteria

1. Given the user starts a new Athena repository, when the create flow completes, then Athena creates a repository-root `athena.yaml`, a repository-root `athena.lock`, and the governed primary authored-source root under `src/`, and the created structure matches the M5 repository contract rather than the temporary M4 shape.
2. Given the created repository is reviewed immediately after creation, when the initial files are inspected, then `athena.yaml` contains authored repository/package intent, and `athena.lock` is initialized in a contract-valid state without pretending full dependency resolution has already happened.

## Tasks / Subtasks

- [x] Upgrade the existing repository bootstrapper from the M4 proof seed to the governed M5 repository seed. (AC: 1, 2)
  - [x] Update the Theia backend bootstrap flow under `ide/theia-backend/src/node/` rather than inventing a second create path.
  - [x] Keep the response compatible with the current frontend/open flow by still returning repository root, initial source path, repository name, and project slug.
  - [x] Add clean code comments only where the seed format or compatibility choice is not obvious.
- [x] Create the canonical M5 root files and authored source layout. (AC: 1, 2)
  - [x] Create repository-root `athena.yaml`.
  - [x] Create repository-root `athena.lock`.
  - [x] Create governed `src/` and one initial authored `.athena` source file under it.
  - [x] Keep the bootstrap deterministic from the same parent path and repository name.
- [x] Make the generated manifest express authored package intent without widening scope. (AC: 1, 2)
  - [x] Initialize the primary package identity in a format accepted by Story `1.2` validation.
  - [x] Keep the source-root intent explicit as `src`.
  - [x] Keep dependencies empty for now; dependency declarations belong to later M5 package-graph stories.
  - [x] Avoid package-local manifests, multi-package authoring, registry coordinates, Git transport, or publish metadata.
- [x] Make the generated lock file explicit as bootstrap derived state rather than fake final resolution output. (AC: 1, 2)
  - [x] Initialize `athena.lock` in a readable, contract-valid bootstrap state.
  - [x] Make it obvious that the file is derived-state-oriented and not handwritten dependency intent.
  - [x] Do not pretend a full dependency graph has already been resolved in Story `1.3`.
- [x] Preserve current M4 open-path compatibility while updating product-facing copy and docs. (AC: 1)
  - [x] Ensure the created repository still contains exactly one initial `.athena` source so the current resolver/open path continues to work before Story `1.4`.
  - [x] Update any frontend/home or module documentation that would otherwise still claim new repositories create only `src/<project>.athena`.
  - [x] Do not upgrade repository open/session semantics here; Story `1.4` still owns contract-aware open state.
- [x] Cover the new create behavior with focused tests and regression-safe verification. (AC: 1, 2)
  - [x] Add backend-focused tests for valid repository bootstrap shape, duplicate-target rejection, invalid name rejection, and root manifest/lock creation.
  - [x] Add at least one JVM-side validation proof that a freshly bootstrapped repository passes the Story `1.2` repository-contract validator.
  - [x] Run Java 25 Gradle verification sequentially, plus the relevant Node/Theia backend verification sequentially.

## Dev Notes

### Story Intent

- Story `1.1` froze the canonical repository/package nouns.
- Story `1.2` proved compiler-owned root-contract validation.
- Story `1.3` now changes the actual repository-create flow so the product stops emitting the old proof-only seed.
- Story `1.4` still owns governed repository open into contract-aware seed state, so `1.3` must preserve current open compatibility rather than replacing the resolver/session path.

### Architecture Guardrails

- Align to AD-11 by creating exactly one repository-root `athena.yaml` and one repository-root `athena.lock`.
- Align to AD-12 by creating one primary package per repository and a governed `src/` authored source root.
- Align to AD-13 by treating repository-model nouns as canonical and not inventing new package contract shapes in frontend/backend code.
- Align to AD-16 by making `athena.lock` clearly derived-state-oriented even in bootstrap form.
- Preserve inherited AD-3 and AD-5: create flow orchestration may start in Theia backend, but canonical meaning still belongs to the JVM validation path and later runtime/session ownership.
- Preserve AD-18 by keeping product changes additive and package-operability-scoped.

### Technical Requirements

- The current update seam is `ide/theia-backend/src/node/athena-repository-bootstrapper.ts`.
- The frontend caller in `ide/theia-frontend/src/browser/athena-repository-creation-service.ts` currently expects:
  - `repositoryRootPath`
  - `sourcePath`
  - `repositoryName`
  - `projectName`
- Keep that response shape unless a change is strictly necessary; Story `1.3` should not force unrelated frontend/session churn.
- The primary package identity written into `athena.yaml` must satisfy the Story `1.2` validator. A deterministic default such as `com.engineeringood.<slug>` is appropriate.
- Keep the initial authored system file compatible with the current M4 resolver by creating one file only under `src/`.
- The lock file may remain bootstrap-simple, but it must read like derived state, not like dependency intent.
- Do not add new third-party Node or JVM dependencies just to generate the repository seed.

### Architecture Compliance

- Prevent these failure modes:
  - backend keeps writing only `src/<slug>.athena` and no root contract files
  - `athena.yaml` uses a package identity that immediately fails Story `1.2` validation
  - `athena.lock` looks like a fake fully resolved graph
  - repository creation silently widens into package resolution or session upgrade work
  - frontend/home/product docs keep describing the old M4 repository seed after the code changes

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node.js `22+`
  - Yarn `1.22.22`
- Prefer built-in Node capabilities for backend tests if additional coverage is added there.

### File Structure Requirements

- Expected update files:
  - `ide/theia-backend/src/node/athena-repository-bootstrapper.ts`
  - `ide/theia-backend/src/node/athena-backend-contribution.ts` only if the route contract needs small adjustments
  - `ide/theia-frontend/src/browser/athena-repository-creation-service.ts` only if frontend expectations need narrow copy/typing updates
  - `ide/theia-frontend/src/browser/athena-home-widget.tsx`
  - `ide/theia-backend/README.md` and/or `ide/theia-frontend/README.md` if create-flow documentation would otherwise be stale
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/...` for fresh-repository validation proof
- Expected new files:
  - backend test or verification files under `ide/theia-backend/`
- Files whose current behavior must be preserved:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
    - still resolves one authored source only until Story `1.4`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
    - still initializes from the current source-path-centered session snapshot
  - `ide/theia-frontend/src/browser/athena-repository-session-service.ts`
    - create flow should remain compatible with the current one-root activation path

### Testing Requirements

- Minimum verification commands for story completion:
  - relevant Node/Theia backend verification command(s), run sequentially
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - created repository contains `athena.yaml`, `athena.lock`, and `src/<slug>.athena`
  - manifest content passes Story `1.2` validation
  - lock file is clearly bootstrap derived state
  - current frontend create action and current source-centered open path stay compatible
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- The current Theia backend bootstrapper creates only:
  - `src/`
  - one `<slug>.athena` file
- The current frontend create flow opens the repository root immediately after bootstrap.
- The current home widget still describes the repository rule as M4 light bootstrap and must be corrected if code changes.
- The current LSP/session path still assumes exactly one active authored source and should stay operable after repository creation.

### Previous Story Intelligence

- Story `1.2` already proved a narrow compiler-owned validator. Use it as the contract truth check for any generated repository shape.
- The user’s standing constraints still apply:
  - package root stays `com.engineeringood`
  - physical structure and docs must match the architecture
  - Java `25` is mandatory
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent relevant commits remain the same milestone baseline used in Stories `1.1` and `1.2`.
- Practical implication:
  - keep the implementation incremental in the existing Theia backend/frontend seams instead of introducing a new product bootstrap subsystem

### Latest Technical Information

- No web research is required for Story `1.3`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `1.3` should leave Story `1.4` room to switch repository open from “find one source” to “open governed contract-aware seed state.”

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-13-create-a-new-governed-athena-repository]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/1-2-validate-repository-root-contract-and-primary-package-layout.md]
- [Source: ide/theia-backend/src/node/athena-repository-bootstrapper.ts]
- [Source: ide/theia-backend/src/node/athena-backend-contribution.ts]
- [Source: ide/theia-frontend/src/browser/athena-repository-creation-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-home-widget.tsx]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt]

## Story Completion Status

- Status: done
- Completion note: Replaced the old proof-only repository bootstrap with the governed M5 seed, added Node guardrail tests plus JVM validation proof, updated repository-facing product copy/docs, and passed sequential frontend/backend and Java 25 verification.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, architecture spine, and Story `1.2` review
- CodeGraph exploration of current create/open seams
- current Theia frontend repository-create service review
- current Theia backend repository bootstrapper and route review
- current LSP/session payload and language-server bootstrap review
- `yarn workspace @engineeringood/athena-theia-frontend build`
- `yarn workspace @engineeringood/athena-theia-backend test`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain build"`

### Completion Notes List

- Updated the Theia backend repository bootstrapper to create `athena.yaml`, `athena.lock`, and the initial `src/<slug>.athena` authored source.
- Kept the create-response contract compatible with the current frontend/open path by preserving repository root, source path, repository name, and project slug.
- Initialized the bootstrap manifest with `primaryPackage.name = com.engineeringood.<slug>`, `version = 0.1.0`, and `sourceRoot = src`.
- Initialized the bootstrap lock file as explicit derived-state-oriented seed content without pretending the package graph is already resolved.
- Added a backend Node test runner using the built-in `node:test` module for governed bootstrap shape, invalid-name rejection, and duplicate-target rejection.
- Added JVM-side validation proof that the governed bootstrap seed passes the Story `1.2` repository-contract validator.
- Updated the frontend home widget and IDE group docs so the product no longer describes new repositories as M4 light-only bootstraps.
- Kept current resolver/open compatibility by preserving exactly one initial authored `.athena` source under `src/`.

### File List

- _bmad-output/implementation-artifacts/m5/1-3-create-a-new-governed-athena-repository.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- ide/README.md
- ide/README.zh-CN.md
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt
- ide/theia-backend/package.json
- ide/theia-backend/scripts/athena-repository-bootstrapper.test.mjs
- ide/theia-backend/src/node/athena-repository-bootstrapper.ts
- ide/theia-backend/lib/node/athena-repository-bootstrapper.d.ts
- ide/theia-backend/lib/node/athena-repository-bootstrapper.d.ts.map
- ide/theia-backend/lib/node/athena-repository-bootstrapper.js
- ide/theia-backend/lib/node/athena-repository-bootstrapper.js.map
- ide/theia-frontend/src/browser/athena-home-widget.tsx
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts
- ide/theia-frontend/lib/browser/athena-home-widget.d.ts.map
- ide/theia-frontend/lib/browser/athena-home-widget.js
- ide/theia-frontend/lib/browser/athena-home-widget.js.map
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryContractLoaderTest.kt

## Change Log

- 2026-07-09: Created Story `1.3` context and moved it to ready-for-dev.
- 2026-07-09: Implemented the governed repository bootstrap, added Node and JVM validation coverage, updated product copy/docs, and completed sequential verification.
