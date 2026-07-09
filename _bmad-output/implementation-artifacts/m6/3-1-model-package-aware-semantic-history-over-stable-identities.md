---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.1: Model Package-Aware Semantic History Over Stable Identities

Status: review

## Story

As a package owner,
I want Athena semantic history to be anchored to stable package identity and version meaning,
so that I can inspect package evolution without depending on raw vendor history vocabulary.

## FR Traceability

- FR-10: relate semantic change and history to package identity and version meaning
- FR-11: keep publish-oriented semantic history narrow and semantic-first in M6
- FR-12: preserve a later graphical projection path without widening M6 into it
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: semantic SCM nouns remain vendor-neutral
- NFR-6: M6 prepares later publish and graphical work without widening into either milestone

## Acceptance Criteria

1. Given M5 package identities and M6 semantic change records exist, when Athena introduces semantic history contracts, then history entries relate change to stable package identity, version meaning, dependency movement, and release relevance, and the history model remains semantic-first and transport-light.
2. Given broader release or distribution features are considered, when the semantic history model is reviewed, then registry transport, remote package distribution, and broad release automation remain out of scope for M6, and the milestone stays focused on semantic history meaning.

## Tasks / Subtasks

- [x] Expand `kernel/semantic-scm` history contracts beyond the current placeholder model. (AC: 1, 2)
  - [x] Keep public semantic history nouns VCS-neutral and package-identity anchored.
  - [x] Model version meaning, dependency movement, and release relevance explicitly instead of hiding them in free-form text.
  - [x] Keep all new core Kotlin classes under `com.engineeringood.athena.scm` with clean KDoc.
- [x] Align the adapter boundary and related contracts to the richer history model. (AC: 1)
  - [x] Keep history transport-light and baseline-oriented rather than vendor-log-shaped.
  - [x] Avoid introducing registry, remote publish, or workflow automation nouns into public kernel contracts.
- [x] Add focused contract tests that prove determinism and scope boundaries. (AC: 1, 2)
  - [x] Verify package identity, version meaning, dependency movement, and release relevance remain inspectable in typed form.
  - [x] Verify out-of-scope release transport concerns do not appear in the public contract surface.
- [x] Update live M6 semantic SCM docs for the richer history model. (AC: 2)
  - [x] Refresh the `kernel/semantic-scm` module README.
  - [x] Note that semantic history remains package-centered and transport-light in M6.

## Dev Notes

### Story Intent

- Story 3.1 is the contract-freezing step for Epic 3.
- This story should deepen `kernel/semantic-scm` history meaning, not add product surfaces or registry behavior.
- Generation across baseline sequences belongs to Story 3.2, and runtime/LSP/IDE exposure belongs to Story 3.3.

### Architecture Guardrails

- Align to AD-19 by keeping semantic history in the dedicated VCS-neutral semantic SCM core above `repository-model`.
- Align to AD-24 by preserving authored-versus-derived distinctions inside history facts and release relevance.
- Align to AD-26 by anchoring semantic history to package identity, version meaning, dependency movement, and release relevance while deferring transport.

### Technical Requirements

- Keep package identity anchored to the existing M5 `PackageIdentifier`.
- Prefer typed history contracts over stringly typed changelog-style summaries.
- Do not introduce Git, registry, or Theia nouns into public history contracts.
- Public/core Kotlin classes added or expanded in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - semantic history collapsing into vendor-native commit/log vocabulary
  - package identity and version meaning being implied only through free-form messages
  - registry transport or remote publication concerns leaking into the history contract layer
  - later release or graphical work being forced to reinterpret weak history data

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 3.1.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/...`
  - `kernel/semantic-scm/README.md`

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:semantic-scm:test"`
- Keep Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 1.1 introduced the first placeholder `SemanticHistoryEntry` and `SemanticHistorySummary` contracts.
- Stories 1.2 through 2.4 proved baseline, diff, review, commit, runtime, LSP, and Theia seams without yet freezing rich history meaning.
- M5 already froze stable package identity under `PackageIdentifier`, repository manifest, lock, and package graph contracts.

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/1-1-publish-canonical-semantic-scm-contracts-in-kernel-semantic-scm.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-4-expose-review-and-commit-semantics-through-runtime-lsp-and-existing-ide-seams.md]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]
- [Source: kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt]

## Story Completion Status

- Status: review
- Completion note: `:kernel:semantic-scm` now models package-aware semantic history with typed package version meaning, dependency movement, release relevance, and a baseline-sequence history request, while keeping the public contract transport-light and vendor-neutral.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epic, PRD, and architecture review for Epic 3 semantic history constraints
- CodeGraph plus source inspection over `SemanticScmContracts`, `SemanticScmAdapter`, `PackageIdentifier`, and the current runtime/review surfaces
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test :kernel:semantic-scm:test"`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Expanded the semantic history placeholder into typed package-history contracts with `SemanticHistoryRequest`, `SemanticPackageVersionMeaning`, `SemanticDependencyMovement`, and `SemanticReleaseRelevance`.
- Tightened the history-facing adapter boundary so future history generation works over a baseline sequence request instead of a thin package-only entry point.
- Added focused contract coverage proving package identity, version meaning, dependency movement, and release relevance remain inspectable in typed form.
- Refreshed the semantic-SCM English and Chinese module READMEs so M6 history is explicitly package-centered and transport-light.

### File List

- _bmad-output/implementation-artifacts/m6/3-1-model-package-aware-semantic-history-over-stable-identities.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt
- kernel/semantic-scm/src/test/kotlin/com/engineeringood/athena/scm/SemanticScmContractsTest.kt

### Change Log

- 2026-07-09: Replaced the thin semantic history placeholder with package-aware history request, version-meaning, dependency-movement, and release-relevance contracts, then refreshed module docs and focused contract tests.
