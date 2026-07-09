---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.3: Surface Package Evolution And Release Relevance Through Existing Athena Product Boundaries

Status: review

## Story

As an engineer or release owner,
I want to inspect semantic history through the current Athena product boundaries,
so that package evolution and release relevance are operable in the desktop product and ready for later visual consumption.

## FR Traceability

- FR-9: surface semantic review output through existing Athena runtime, LSP, and IDE seams
- FR-10: relate semantic change and history to package identity and version meaning
- FR-11: keep publish-oriented semantic history narrow and semantic-first in M6
- FR-12: preserve a later graphical projection path without widening M6 into it
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-5: M6 extends the current Athena shell and `ide/lsp` path instead of forcing a product-shell rewrite
- NFR-6: M6 prepares later publish and graphical work without widening into either milestone

## Acceptance Criteria

1. Given semantic history summaries exist in JVM-owned services, when Athena exposes them through runtime, LSP, and the current IDE path, then users can inspect package evolution and release relevance through existing additive product seams, and no graphical projection or shell rewrite is required.
2. Given later M7 graphical work is planned, when M6 history outputs are reviewed, then those outputs are suitable as downstream inputs for later visual review, and M6 does not widen into GLSP, canvas, or graphical work.

## Tasks / Subtasks

- [x] Add one runtime-owned semantic history projection service for package-aware baseline sequences. (AC: 1)
  - [x] Reuse existing baseline resolution, semantic diff, and kernel history summarization instead of rebuilding history in runtime.
  - [x] Keep all new core Kotlin classes under `com.engineeringood.athena.runtime` with clean KDoc.
- [x] Expose semantic history through `ide/lsp` as an additive Athena request surface. (AC: 1)
  - [x] Add typed request and payload contracts for package history, package lineage, dependency movement, validation movement, and release relevance.
  - [x] Keep LSP transport vendor-neutral and downstream of runtime semantic authority.
- [x] Extend the current Theia semantic SCM panel to inspect package evolution without a shell rewrite. (AC: 1, 2)
  - [x] Keep semantic review, commit, and package history inside the existing panel rather than introducing a new M6 shell.
  - [x] Add lightweight package and baseline-sequence controls suitable for later visual consumption.
- [x] Add focused runtime, LSP, and frontend verification. (AC: 1, 2)
  - [x] Verify runtime and LSP package-history projections through Java 25 tests.
  - [x] Verify the Theia frontend workspace still builds after the semantic SCM panel extension.
- [x] Refresh live module docs for runtime and IDE semantic-history exposure. (AC: 2)
  - [x] Update English and Chinese module READMEs.
  - [x] Replace corrupted Chinese IDE README copies with clean UTF-8 text while updating the story output.

## Dev Notes

### Story Intent

- Story 3.3 completes Epic 3 by projecting the kernel history work from Story 3.2 through the existing runtime, LSP, and IDE seams.
- The story remains additive to the current semantic SCM panel and does not introduce graphical work or a second frontend semantic model.

### Architecture Guardrails

- Align to AD-21 by keeping package-history meaning downstream of canonical semantic diff facts.
- Align to AD-24 by preserving authored-versus-derived distinctions inside projected history entries.
- Align to AD-26 by keeping package evolution semantic-first, release-oriented, and transport-light.

### Technical Requirements

- Reuse `AthenaSemanticBaselineService`, `AthenaSemanticDiffService`, and `SemanticHistorySummaryGenerator`.
- Keep history projection types runtime-owned; LSP and Theia stay downstream projections.
- Do not widen into GLSP, canvas, or dedicated graphical history tooling.
- Public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - frontend reconstruction of package history outside the JVM semantic path
  - one-off history payloads that diverge from the kernel history contract
  - a separate M6 widget or shell flow that forks the semantic SCM workbench path
  - visual or graphical milestone creep entering M6 through history surfacing

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- Existing Theia/Yarn workspace only; no new third-party dependency should be added for Story 3.3

### File Structure Requirements

- Expected new or updated files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/...`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/...`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/...`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/...`
  - `ide/theia-frontend/src/browser/...`
  - `kernel/runtime/README.md`
  - `ide/lsp/README.md`
  - `ide/README.md`

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
  - `yarn --cwd ide build`
- After touching docs/text:
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- Keep Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 2.4 already proved the current runtime -> LSP -> Theia semantic SCM path for review and commit projections.
- Story 3.1 froze package-aware history contracts and Story 3.2 added the kernel history summarizer.
- M5 already established the repository session, `.athena` editor path, and existing Theia shell seams that this story extends.

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/3-2-produce-publish-oriented-history-summaries-from-baseline-sequences.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticHistorySummaryGenerator.kt]

## Story Completion Status

- Status: review
- Completion note: runtime, LSP, and the existing Theia semantic SCM panel now surface package-aware semantic history, release relevance, and contract-break risk through the same additive product seam, without widening M6 into graphical work.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epic and Epic 3 acceptance review for runtime/LSP/IDE package-history exposure
- CodeGraph plus source inspection over runtime semantic SCM services, LSP protocol/server, and the current Theia semantic SCM widget
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- `yarn --cwd ide build`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `AthenaSemanticHistoryStateService` so runtime now publishes one typed package-history state over baseline sequences.
- Expanded the Athena LSP protocol with `athena/semanticHistoryState` and typed payloads for package lineage, dependency movement, validation movement, release relevance, and contract-break risk.
- Extended the existing Theia semantic SCM widget with package-history controls and a package-evolution section instead of creating a second M6 shell.
- Added focused runtime and LSP tests for ready and unresolved-baseline history flows.
- Replaced corrupted Chinese IDE README copies while updating the runtime and IDE docs to describe package-history exposure.

### File List

- _bmad-output/implementation-artifacts/m6/3-3-surface-package-evolution-and-release-relevance-through-existing-athena-product-boundaries.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- ide/README.md
- ide/README.zh-CN.md
- ide/lsp/README.md
- ide/lsp/README.zh-CN.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticHistoryStateRequestTest.kt
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx
- ide/theia-frontend/src/browser/style/index.css
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateServiceTest.kt

### Change Log

- 2026-07-09: Added runtime/LSP/IDE package-history projection, extended the existing semantic SCM widget with release-relevance inspection, refreshed English and Chinese module docs, and verified the Java 25 plus Theia workspace gates.
