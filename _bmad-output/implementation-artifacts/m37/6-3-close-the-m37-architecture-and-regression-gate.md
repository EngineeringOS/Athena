---
story_key: 6-3-close-the-m37-architecture-and-regression-gate
epic: m37-e6
requirements: [FR-35, FR-36, NFR-1, NFR-2, NFR-3, NFR-4, NFR-5, NFR-6, NFR-7, NFR-8]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 6.3: Close The M37 Architecture And Regression Gate

Status: review

## Story

As a milestone owner,
I want a complete clean verification pass and implementation handoff,
so that M37 can be developed and reviewed without hidden legacy or unverified claims.

## Acceptance Criteria

1. Full sequential verification passes: root Gradle tests, routing/runtime focused tests where relevant, tree-sitter corpus and Node tests, Theia frontend tests, LSP `installDist`, IDE build, source-set hygiene, encoding audit, and `git diff --check`.
2. Electron M37 E2E passes after LSP/kernel and Theia frontend rebuilds and captures fresh screenshots under `_bmad-output/implementation-artifacts/m37/screenshots`.
3. Architecture audit records one Athena source authority, transient planner IR, computed proof, paint-only renderer, package-local SVG geometry, no XML runtime authority, no endpoint-derived intent fallback in active drawing path, no stale Cabinet override, and no proof-success constants.
4. Every M37 FR/NFR has fresh evidence from tests, diagnostics, structured proof, documentation, or E2E smoke output.
5. M37 retrospective, usage handoff, screenshot index, and final sprint status record only verified outcomes and do not claim done while a required gate remains incomplete.
6. No completion claim is made while any required command, story artifact, or epic gate remains incomplete.

## Tasks / Subtasks

- [x] Add final gate documentation skeleton (AC: 3, 4, 5)
  - [x] Create M37 usage/handoff artifact under `_bmad-output/implementation-artifacts/m37`.
  - [x] Create or update M37 screenshot index with actual screenshot paths and proof summary.
  - [x] Create M37 retrospective or final gate summary with architecture findings and residual risks.
- [x] Run full sequential verification (AC: 1, 2)
  - [x] Run `.\gradlew.bat --no-daemon --console=plain test`.
  - [x] Run `yarn --cwd ide/tree-sitter-athena test`.
  - [x] Run `yarn --cwd ide/theia-frontend test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`.
  - [x] Run `yarn --cwd ide build`.
  - [x] Run `yarn --cwd ide start:smoke:m37`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
  - [x] Run `git diff --check`.
- [x] Run final architecture audit (AC: 3, 4, 6)
  - [x] Audit production `src/main` for forbidden proof/demo/sample/milestone/version names.
  - [x] Audit production authority terms for XML runtime authority, compatibility shim, fallback route authority, renderer repair, hardcoded proof success, and endpoint-derived intent fallback.
  - [x] Confirm M37 screenshots and structured proof come from `examples/m37/professional-control-drawing`.
  - [x] Confirm final status records distinguish `review` from `done` according to BMad workflow.
- [x] Update BMad records (AC: 5, 6)
  - [x] Update story Debug Log, Completion Notes, File List, Change Log, and Status after evidence exists.
  - [x] Update sprint status for story 6.3.
  - [x] Keep all edits in M37 artifacts; do not jump to other milestones.

## Dev Notes

### Authority Boundary

- M37 final gate is evidence, not implementation invention. Fix real failures, but do not add compatibility wrappers or proof constants to make gates pass.
- Athena source remains SSOT. SVG is geometry only. XML/AML/ECLASS are not runtime authority.
- Renderer is paint-only. Planner IR is transient. Projection Policy selects derived views and cannot create engineering truth.

### Current M37 Evidence

- Story 5.4 final smoke passed with workspace `examples/m37/professional-control-drawing`, active surface `Control Drawing`, backing view `schematic`, 11 routes, 10 graphic occurrences, 28 presentation terminals, and 53 ms compile-to-presentation refresh.
- Story 5.4 screenshots:
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1920x1080.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1280x900.png`
  - `_bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-narrow.png`
- Story 6.1 hardened SVG `data-athena-ref` validation to geometry-only `anchor:<id>`.
- Story 6.2 moved professional drawing hard rules into named compiler-owned route hard-rule evidence and aligned stale route profile tests.

### Verification Rules

- Gradle commands must be sequential on Windows. Do not run Gradle in parallel.
- Rebuild LSP/kernel and Theia frontend before Electron E2E to avoid stale-bundle lies.
- If full root tests expose unrelated stale expectations, align them to current M37 architecture directly; Athena is pre-public.
- Do not mark this story review until every required command above has passed or a blocker is explicitly recorded.

### Required Commands

- `.\gradlew.bat --no-daemon --console=plain test`
- `yarn --cwd ide/tree-sitter-athena test`
- `yarn --cwd ide/theia-frontend test`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
- `yarn --cwd ide build`
- `yarn --cwd ide start:smoke:m37`
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 6.3]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-35, FR-36, NFR-1 through NFR-8]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - M37 principles and direct refactor targets]
- [Source: `_bmad-output/implementation-artifacts/m37/5-4-prove-m37-end-to-end-with-screenshots.md` - E2E proof evidence]
- [Source: `_bmad-output/implementation-artifacts/m37/6-1-harden-package-local-geometry-references.md` - SVG bridge hardening]
- [Source: `_bmad-output/implementation-artifacts/m37/6-2-purge-stale-and-fabricated-authority-paths.md` - stale authority cleanup]
- [Source: `AGENTS.md` - source-set hygiene, pre-1.0, E2E proof, encoding, and Gradle sequencing rules]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-31: Created M37 usage handoff, screenshot index, and retrospective gate artifacts.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after aligning stale M35/M36 LSP smoke expectations to current projection readiness and removing obsolete sample-specific M36 graphic-move expectation.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM34ProfessionalCabinetCompositionTest` first exposed stale M34 display-label assertion, then passed after aligning expected `Control Drawing`.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain test` passed.
- 2026-07-31: `yarn --cwd ide/tree-sitter-athena test` passed.
- 2026-07-31: `yarn --cwd ide/theia-frontend test` first exposed stale port-direction highlight expectation, then passed after aligning expected `in|out|bidirectional|passive`.
- 2026-07-31: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist` passed.
- 2026-07-31: `yarn --cwd ide build` passed.
- 2026-07-31: `yarn --cwd ide start:smoke:m37` passed with `Control Drawing`, backing `schematic`, 11 routes, 10 graphic occurrences, 28 terminals, zero route/body intersections, and screenshots under `_bmad-output/implementation-artifacts/m37/screenshots`.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- 2026-07-31: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- 2026-07-31: `git diff --check` passed with line-ending warnings only.
- 2026-07-31: Production `src/main` audit found no forbidden proof/demo/sample/milestone/version names and no active XML/runtime authority, compatibility shim, renderer repair, proof-success, or endpoint-derived intent fallback terms.

### Completion Notes List

- M37 final gate documentation is in place and records only verified evidence.
- Full sequential verification passed after direct cleanup of stale regression expectations. No compatibility shim or proof constant was added.
- Fresh M37 Electron smoke used only `examples/m37/professional-control-drawing` and regenerated all three required screenshots.
- Architecture audit preserved Athena source SSOT, transient planner IR, computed proof, paint-only renderer, package-local SVG geometry, no XML runtime authority, no endpoint-derived intent fallback, no stale Cabinet override, and no proof-success constants.
- Story status is `review`, not `done`, matching BMad dev workflow.

### File List

- _bmad-output/implementation-artifacts/m37/6-3-close-the-m37-architecture-and-regression-gate.md
- _bmad-output/implementation-artifacts/m37/retrospective.md
- _bmad-output/implementation-artifacts/m37/screenshot-index.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- _bmad-output/implementation-artifacts/m37/usage-handoff.md
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1280x900.png
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-desktop-1920x1080.png
- _bmad-output/implementation-artifacts/m37/screenshots/m37-professional-control-drawing-narrow.png
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM35DedicatedCabinetProjectionSmokeTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36DedicatedCabinetProjectionSmokeTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM36GovernedGraphicEditIntentTest.kt
- ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt

## Change Log

- 2026-07-31: Created implementation-ready Story 6.3 from finalized M37 PRD, epics, addendum, current sprint evidence, and Stories 5.4/6.1/6.2 lessons.
- 2026-07-31: Closed M37 final gate with full sequential regression, stale expectation cleanup, fresh Electron smoke screenshots, architecture audits, and final handoff documentation.
