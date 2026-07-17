---
baseline_commit: 0b43cbe
---

# Story 1.1: Create the openable M22 sample project

Status: done

## Story

As a reviewer,
I want an M22 sample project with real `.athena` files,
so that I can inspect M22 layout optimization through the normal Athena Theia workflow.

## Acceptance Criteria

1. Given the M22 milestone workspace, when I inspect `examples/m22/sample-project`, then it contains real `.athena` source files for baseline, optimized layout, and round-trip scenarios.
2. Given the M22 sample project, when I inspect the source corpus, then it includes power source, protection, controller, HMI, terminal block, and load subjects.
3. Given the Athena IDE workspace, when I use the documented M22 launch path, then the M22 sample project opens in the Athena Theia IDE without requiring users to inspect `.mjs` files.
4. Given M22 baseline proof needs, when the first sample source is opened, then it starts from accepted M20/M21 graph workbench behavior.
5. Given M22 boundaries, when the sample project and docs are reviewed, then they do not introduce public repository/import ecosystem behavior, cabinet authoring, physical routing, AI layout, full EPLAN parity, or final layout-stack selection.

## Tasks / Subtasks

- [x] Create the openable M22 sample project (AC: 1, 2, 4, 5)
  - [x] Add `examples/m22/README.md` describing the M22 proof corpus and IDE sample path.
  - [x] Add `examples/m22/sample-project/README.md` listing real `.athena` scenarios and how to open them.
  - [x] Add `examples/m22/sample-project/athena.yaml` and `athena.lock` using the same local governed repository shape as M21.
  - [x] Add `.athena` source files under `examples/m22/sample-project/src/` for at least:
    - [x] baseline accepted M21 canvas behavior
    - [x] governed layout optimization acceptance
    - [x] component layout round-trip scenario
    - [x] deferred boundary scope
- [x] Wire a truthful M22 IDE launch path (AC: 3, 4)
  - [x] Add `start:m22` scripts to `ide/package.json` and `ide/theia-product/package.json`.
  - [x] Add `start:smoke:m22` scripts to `ide/package.json` and `ide/theia-product/package.json`.
  - [x] Reuse the existing Theia workspace opening pattern from the M21 opener and smoke proof.
  - [x] Ensure the command targets `examples/m22/sample-project` and does not rely on stale workspace history.
- [x] Document the M22 usage path (AC: 3, 4, 5)
  - [x] Add `docs/usages/m22-proof-usage.md`.
  - [x] State clearly that customers inspect real `.athena` files in the IDE; `.mjs` fixtures are supporting tests only.
  - [x] Carry forward accepted M20/M21 canvas invariants.
  - [x] State M22 non-goals and deferred domains.
- [x] Add focused sample-project validation (AC: 1, 2, 3, 5)
  - [x] Add a lightweight static check verifying the M22 sample project shape, launch scripts, usage docs, and syntax guardrails.
  - [x] Add an M22 compiler regression proving sample source files parse and produce no semantic diagnostics.
  - [x] Add an M22 Electron smoke path that proves the sample project is the actual loaded workspace.
  - [x] Keep detailed visual acceptance checklist implementation for Story 1.2.
- [x] Keep status and documentation clean (AC: 5)
  - [x] Update this story's Dev Agent Record when implementation finishes.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Do not update unrelated milestone artifacts unless a stale reference would mislead M22 users.

## Dev Notes

### Current State

- M21 already has an openable sample project at `examples/m21/sample-project/`.
- M21 established the truthful launch and smoke pattern:
  - root `ide/package.json` delegates `start:m21` and `start:smoke:m21`
  - `ide/theia-product/package.json` launches `athena-electron-open-workspace-main.js ../../examples/m21/sample-project`
  - `verify-athena-m21-sample-project.js` proves the actual opened workspace and graph workbench DOM state
- M22 must follow that pattern with `examples/m22/sample-project`, not a blank shell or `.mjs` fixture.
- This story creates the visible M22 sample and launch surface only. It does not implement the full Layout Constraint Model, optimization engine, ELK adapter, or source round-trip behavior.

### Architectural Guardrails

- Follow M22 AD-6 and AD-8 for Theia projection consumption and scope boundaries.
- Preserve accepted graph workbench behavior:
  - stage grid is the coordinate surface
  - sheet and component bodies do not hide the grid
  - `Cabinet Main` details are available from the top information popover only
  - top and bottom controls remain transparent canvas overlays
  - outline navigation keeps the same `.athena` editor tab
- Keep M22 sample source syntax inside currently accepted compiler/LSP grammar.
- Do not introduce public package registry, full IEC/QElectroTech breadth, cabinet authoring, physical routing, AI layout, full EPLAN parity, or final solver-stack selection.

### Project Structure Notes

Likely new files:

- `examples/m22/README.md`
- `examples/m22/sample-project/README.md`
- `examples/m22/sample-project/athena.yaml`
- `examples/m22/sample-project/athena.lock`
- `examples/m22/sample-project/src/01-baseline-sheet.athena`
- `examples/m22/sample-project/src/02-layout-optimization-acceptance.athena`
- `examples/m22/sample-project/src/03-component-round-trip.athena`
- `examples/m22/sample-project/src/04-boundary-scope.athena`
- `docs/usages/m22-proof-usage.md`
- `ide/theia-product/scripts/verify-athena-m22-sample-project.js`
- `ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM22SampleProjectCompilerTest.kt`

Likely update targets:

- `ide/package.json`
- `ide/theia-product/package.json`

### Sample Content Guidance

The source files should use package syntax already accepted by the IDE/LSP:

- package `com.engineeringood.m22.sample`
- `system`, `device`, `port`, and `connect` declarations
- known device types such as `Switch`, `Motor`, and `Lamp`
- no `import` syntax unless already proven by current IDE/LSP for these samples

The M22 sample should include names that make future layout optimization and round-trip intent
obvious:

- power source / supply
- protection / breaker
- controller / PLC
- HMI
- terminal block
- primary load / motor
- component alignment/grouping hint scenario names

### Testing Requirements

- Run Node/Yarn checks sequentially.
- Do not run Gradle verification concurrently with any other Gradle task.
- Minimum checks:
  - `node --test ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM22SampleProjectCompilerTest`
  - `yarn workspace @engineeringood/athena-theia-product start:smoke:m22`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- If package scripts or Electron product scripts change, verify the script path is wired from both root `ide/package.json` and `ide/theia-product/package.json`.

### References

- [Source: `_bmad-output/implementation-artifacts/m22/epics.md` - Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/prd.md` - FR-1, FR-2, FR-11, FR-12]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md` - AD-6, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m21/1-1-create-the-openable-m21-sample-project.md`]
- [Source: `examples/m21/sample-project/README.md`]
- [Source: `docs/usages/m21-proof-usage.md`]
- [Source: `ide/theia-product/scripts/verify-athena-m21-sample-project.js`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs` failed first because the M22 sample project and scripts were missing, then passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM22SampleProjectCompilerTest` passed after the M22 source corpus was added.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m22` passed and reported the M22 sample-project workspace path plus graph-workbench DOM proof.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added the M22 openable sample project with four real `.athena` scenarios: baseline, layout optimization acceptance, component round-trip, and deferred boundary scope.
- Added truthful `start:m22` and `start:smoke:m22` launch paths that target `examples/m22/sample-project`.
- Added M22 usage documentation, static sample validation, compiler validation, and Electron smoke validation.
- Preserved M20/M21 graph workbench invariants and kept M22 boundaries explicit.

### File List

- `_bmad-output/implementation-artifacts/m22/1-1-create-the-openable-m22-sample-project.md`
- `_bmad-output/implementation-artifacts/m22/sprint-status.yaml`
- `docs/usages/m22-proof-usage.md`
- `examples/m22/README.md`
- `examples/m22/sample-project/README.md`
- `examples/m22/sample-project/athena.yaml`
- `examples/m22/sample-project/athena.lock`
- `examples/m22/sample-project/src/01-baseline-sheet.athena`
- `examples/m22/sample-project/src/02-layout-optimization-acceptance.athena`
- `examples/m22/sample-project/src/03-component-round-trip.athena`
- `examples/m22/sample-project/src/04-boundary-scope.athena`
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/verify-athena-m22-sample-project.js`
- `ide/theia-frontend/scripts/athena-m22-sample-project.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM22SampleProjectCompilerTest.kt`
## Change Log

- 2026-07-18: Created M22 Story 1.1 with openable sample-project and truthful launch-path requirements.
- 2026-07-18: Implemented M22 sample project, launch scripts, usage docs, static validation, compiler validation, and Electron smoke proof.
