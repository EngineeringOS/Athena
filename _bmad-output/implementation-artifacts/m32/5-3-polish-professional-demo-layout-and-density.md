---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 5.3
epic: 5
title: Polish Professional Demo Layout And Density
---

# Story 5.3: Polish Professional Demo Layout And Density

## Status

Review

## Story

As a customer reviewer,
I want the M32 sample to look like a serious industrial engineering document,
so that Athena no longer reads as a toy renderer.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/5-2-add-structured-package-product-smoke.md`
- Product smoke docs: `docs/usages/engineering-package-platform.md`
- Design direction: `draft/layouts/003-presentation-language.md`

## Acceptance Criteria

1. Given the M32 sample opens in the IDE, when the primary sheet renders, then component
   backgrounds and hitboxes are transparent, no duplicated visible text appears, viewBox tightly
   fits resolved content plus governed margins, and control panels do not hide sheet navigation.
2. Given package-backed elements render, when visual review compares them with the professional
   target direction from `draft/layouts/003-presentation-language.md`, then primitive IEC-like and
   complex product-like elements show descriptor-driven anchors, labels, and compact composition
   rather than generic rectangles.
3. Given the story implementation is complete, when UI, CSS, layout, screenshots, docs, package
   sample assets, proof payloads, and sprint artifacts are deeply reviewed, then stale toy-layout
   artifacts are removed or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for an M32 demo density proof covering transparent normal chrome, no duplicate
  labels, governed tight viewBox, and sheet-navigation-safe layout. (AC: 1)
- [x] Add RED tests proving package-backed sample elements use descriptor anchors, labels, compact
  composition, and non-generic resource identities. (AC: 2)
- [x] Implement demo layout/density proof payloads and sample layout fixture at the
  package-runtime/example boundary. (AC: 1,2)
- [x] Document the professional-density proof and any screenshot/IDE visual review handoff. (AC: 3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- This story should strengthen the M32 sample proof without making Theia or Graphic Resources own
  package authority. The proof may be structured first; screenshot proof remains secondary.
- Use descriptor-backed render payloads and sample layout facts. Do not reintroduce visible normal
  wrappers, generic rectangles as success, duplicate labels, hard-coded oversized viewBox, or
  resource-internal semantic inference.
- Layout facts should be sample/demo data under `examples/m32/sample-project`, not `.athena` visual
  syntax and not kernel semantic truth.
- The control-panel/sheet-navigation condition can be represented as structured adapter-safe
  viewport/navigation proof if full IDE screenshot automation is not part of M32.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing density proof tests before implementation.
- Focused command should target the package runtime module.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed before implementation because `M32DemoLayoutDensityProofRunner` was unresolved.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after adding density proof payloads and the sample layout fixture.
- REFACTOR VERIFY: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after documentation updates.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` exited 0 with `BUILD SUCCESSFUL`.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` exited 0 with `Encoding audit passed.`
- PURGE: `git status --short` showed M32 artifacts, `settings.gradle.kts`, docs, `examples/m32/`, `kernel/package-model/`, and `kernel/package-runtime/`; `.tools` was not present.

### Completion Notes List

- Added `M32DemoLayoutDensityProofRunner` and payloads proving transparent normal chrome, no
  duplicate labels, descriptor-driven anchors/labels, no generic rectangle fallback, tight derived
  viewBox, compact composition, and sheet-navigation visibility.
- Added `examples/m32/sample-project/presentation/layout-density-proof.json` as sample presentation
  data, not `.athena` visual syntax or semantic kernel truth.
- Documented the professional-density proof and screenshot/IDE handoff boundary in
  `docs/usages/engineering-package-platform.md`.
- AC evidence:
  - AC1: `M32DemoLayoutDensityProofTest` verifies transparent backgrounds/hitboxes, no duplicate
    labels, governed tight viewBox, sheet navigation visibility, and no hard-coded 1680x1188
    viewport.
  - AC2: `M32DemoLayoutDensityProofTest` verifies descriptor-driven anchors/labels, compact
    composition, and no generic fallback resource identity.
  - AC3: docs and purge gate completed; no toy-layout shortcut, visible normal wrapper claim, or
    hard-coded oversized canvas constant was retained in the sample proof.
  - AC4: encoding audit and purge/status gate completed.

### File List

- `_bmad-output/implementation-artifacts/m32/5-3-polish-professional-demo-layout-and-density.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `examples/m32/sample-project/presentation/layout-density-proof.json`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProof.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M32DemoLayoutDensityProofTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 5 professional demo layout and density polish.
- 2026-07-22: Added structured professional-density proof and marked story ready for review after focused and full verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent layout proof code, sample layout assets, docs, tests, screenshots,
  sprint status, and cleanup ledger.
- Remove stale toy-layout assumptions, generic visual shortcuts, visible normal wrapper claims,
  duplicate text fixtures, and hard-coded canvas/viewBox constants.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
