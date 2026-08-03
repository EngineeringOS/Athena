---
story_key: 5-1-author-the-dedicated-m37-example
epic: m37-e5
requirements: [FR-31]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.1: Author The Dedicated M37 Example

Status: review

## Story

As an evaluator,
I want a dedicated professional control-drawing example authored in Athena,
so that M37 is proven from realistic semantic source rather than reused fixtures or mocked payloads.

## Acceptance Criteria

1. `examples/m37/professional-control-drawing` exists with package names matching filesystem hierarchy.
2. The source declares grouped Interfaces, Ports, Connections, Connection Intent, external evidence, representation bindings, Projection Policies, and professional drawing profiles.
3. The source models rails, terminals, protective earth, contacts, coils, indicators, and connection topology suitable for the rolling-shutter reference composition.
4. The example contains no XML authority, copied QET element format, reused M36 active proof, mock presentation payload, or generic fallback component.
5. The valid project compiles with zero blocking or degraded semantic diagnostics.
6. Sample compilation plus source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED dedicated example test (AC: 1, 2, 3, 4, 5)
  - [x] Assert M37 project files and Java-style package hierarchy.
  - [x] Assert source contains required semantic declarations and no stale authority paths.
  - [x] Assert compiler can compile the source with zero blocking/degraded semantic diagnostics.
- [x] Author dedicated M37 example (AC: 1, 2, 3, 4)
  - [x] Add `athena.yaml` with primary package and package-local representation roots.
  - [x] Add package-hierarchical `.athena` source under `src/com/engineeringood/m37/professionalcontroldrawing`.
  - [x] Add package-local representation files under `packages/representation/com/engineeringood/m37/...`.
  - [x] Avoid sharing M36 example paths or package names.
- [x] Verify and record gates (AC: 6)
  - [x] Run focused dedicated example test.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Example source is Athena SSOT. No XML, QET `.elmt`, copied HTML element format, raw SVG metadata authority, mock payload, or renderer truth.
- Package resources may exist, but Athena `.athena` source owns metadata.
- Dedicated M37 example must not point at M36 example resources or package names.
- Lock materialization is compiler-owned; do not hand-edit lock digest.

### Current Code Intelligence

- M36 example demonstrates package-local SVG and cabinet installation syntax, but M37 must be dedicated and independently named.
- Connectivity syntax exists for grouped interfaces, `intent`, `evidence`, and `projection`.
- Repository lock can be materialized through `AthenaCompiler.materializeRepositoryLock(sampleRoot)`.

### Expected Diagnostics

- No semantic diagnostics with `severity == ERROR`.
- No source text containing `<definition`, `.elmt`, `qelectrotech`, `m36`, `fallback card`, `mock presentation`, or XML authority markers.

### TDD And Verification

- RED first: focused example test fails before project exists.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedProfessionalDrawingSampleTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 5.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-31]
- [Source: `_bmad-output/implementation-artifacts/m37/4-4-compile-collision-aware-route-labels.md` - professional drawing grammar prerequisites]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.DedicatedProfessionalDrawingSampleTest` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added a dedicated M37 professional control-drawing project under `examples/m37` with Java-style source hierarchy, package-local representation files, projection policy, connection intent, evidence, profiles, and route topology.
- Added compiler coverage that rejects stale authority markers and verifies zero blocking/degraded diagnostics for the dedicated sample.

### File List

- _bmad-output/implementation-artifacts/m37/5-1-author-the-dedicated-m37-example.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- examples/m37/professional-control-drawing/athena.yaml
- examples/m37/professional-control-drawing/athena.lock
- examples/m37/professional-control-drawing/src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/drawing-profile.athena
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/elements/control-elements.athena
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedProfessionalDrawingSampleTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 5.1 from finalized M37 PRD, epics, and Epic 4 drawing grammar learnings.
- 2026-07-30: Implemented dedicated M37 sample and compiler verification; moved story to review after required gates passed.
