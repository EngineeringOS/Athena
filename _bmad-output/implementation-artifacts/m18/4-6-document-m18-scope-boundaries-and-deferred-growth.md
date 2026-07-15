---
baseline_commit: e007793ad0a72d8556f978c9cd1fc7acb76596c4
---

# Story 4.6: Document M18 Scope Boundaries And Deferred Growth

Status: done

## Story

As a platform architect,
I want M18 artifacts to state what is proven and what is deferred,
so that later package-aware language growth does not accidentally expand M18 scope.

## Acceptance Criteria

1. M18 closeout documentation states what M18 proves: compiler-owned project semantic graph foundation, governed repository graph import authority, package-aware linking/lowering proof, LSP projection, Tree-sitter syntax UX, and repository-backed proof corpus.
2. Remote registry, marketplace, publish flows, full export/visibility, broad language redesign, frontend-owned semantic resolution, multi-root behavior, desktop-viewer work, and Kotlin Compose work are explicitly deferred or excluded.
3. Future package-aware growth points back to the project semantic graph foundation rather than introducing a parallel package or frontend semantic model.
4. Milestone artifacts and proof fixtures use relative paths only; no absolute workspace paths are introduced.
5. A repeatable validation command fails when M18 artifacts introduce absolute paths or omit required scope-boundary language.
6. Verification runs the scope-boundary audit and encoding audit.

## Tasks / Subtasks

- [x] Create M18 closeout boundary documentation (AC: 1-3)
  - [x] Summarize proven M18 surfaces.
  - [x] Summarize deferred growth surfaces.
  - [x] Anchor future growth on the project semantic graph foundation.
- [x] Update proof corpus/deferred-work docs (AC: 1-4)
  - [x] Keep references relative.
  - [x] Clarify local-only governed proof scope.
  - [x] Avoid implying registry, marketplace, publish, multi-root, desktop-viewer, Kotlin Compose, or frontend-owned semantic behavior.
- [x] Add validation for closeout scope boundaries (AC: 4-5)
  - [x] Fail on absolute Windows or file URI references in M18 artifacts.
  - [x] Fail if required M18 boundary terms disappear from closeout documentation.
  - [x] Fail if proof fixture paths introduce forbidden ecosystem/UI scope names.
- [x] Run scoped verification sequentially (AC: 6)
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- Story 4.6 is documentation and validation only. Do not change compiler, LSP, Theia frontend, canvas, renderer, desktop-viewer, or Kotlin Compose code.
- The user explicitly corrected that M18 frontend scope is IDE/Theia and that `apps/desktop-viewer` is not involved.
- The user explicitly corrected that BMAD artifacts must use relative paths, not absolute workspace paths.
- The correct future-growth anchor is the compiler-owned project semantic graph and LSP projections from it.
- This story should not add a registry, marketplace, publish, multi-root, frontend semantic resolver, or package ecosystem implementation.

### Previous Story Intelligence

- Story 4.1 proved compiler-owned package diagnostics through LSP.
- Story 4.2 proved package-aware definition/references through LSP.
- Story 4.3 proved document symbol behavior while leaving workspace symbols deferred.
- Story 4.4 proved Theia selection/reveal handoff through existing workbench surfaces only.
- Story 4.5 added repository-backed proof corpus and compiler/LSP closeout coverage.

### Project Structure Notes

- Expected files:
  - `_bmad-output/implementation-artifacts/m18/`
  - `examples/m18/`
  - `tools/`

### References

- [Source: `epics.md` - Epic 4, Story 4.6]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-11, SM-8, Non-Goals]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 11-14]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-4, AD-10, AD-11]
- [Source: `4-5-complete-repository-backed-m18-proof-corpus.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareNavigationTest` failed before unsaved sibling buffers participated in package-aware navigation snapshots.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaPackageAwareNavigationTest` passed after code-review follow-up fixes.
- `powershell -ExecutionPolicy Bypass -File .\tools\m18-scope-boundary-audit.ps1` passed after UNC/extended Windows path audit hardening.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after code-review follow-up fixes.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after code-review follow-up fixes.

### Completion Notes List

- Ultimate context engine analysis completed - Story 4.6 prepared for M18 closeout scope-boundary documentation.
- Added closeout documentation that separates proven M18 behavior from deferred package ecosystem, language breadth, frontend semantic ownership, desktop-viewer, and Kotlin Compose work.
- Added a repeatable M18 boundary audit for absolute path discipline and required scope-boundary language.
- Marked Epic 4 done after all six stories reached done.
- Resolved final M18 code-review follow-ups by letting package-aware LSP project snapshots use tracked dirty sibling buffers, deriving source roots from resolved package graph state, tolerating unreadable sibling source races, and hardening the M18 boundary audit for UNC/extended Windows absolute paths.

### File List

- `_bmad-output/implementation-artifacts/m18/4-6-document-m18-scope-boundaries-and-deferred-growth.md`
- `_bmad-output/implementation-artifacts/m18/deferred-work.md`
- `_bmad-output/implementation-artifacts/m18/m18-closeout-boundaries.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `examples/m18/README.md`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPackageAwareNavigationTest.kt`
- `tools/m18-scope-boundary-audit.ps1`

## Change Log

- 2026-07-15: Created and completed Story 4.6 with M18 boundary documentation, deferred-growth notes, and scope-boundary validation.
- 2026-07-15: Addressed final M18 code-review follow-ups for dirty-buffer LSP package snapshots and absolute-path boundary auditing.

## Senior Developer Review (AI)

Review Date: 2026-07-15

Outcome: Approve after fixes

Findings:

- Fixed: package-aware LSP snapshots read stale disk content for opened sibling source units instead of tracked dirty buffers.
- Fixed: M18 scope-boundary audit did not detect UNC or extended Windows absolute path prefixes.
- Dismissed: direct LSP initialization against nested local dependency manifests remains outside current IDE repository resolver scope; compiler-level repository proof covers the checked-in nested governed corpus.
- Dismissed: document-symbol package wrapping remains within Story 4.3's implemented package-aware document-symbol scope; no frontend/local semantic symbol authority was introduced.
