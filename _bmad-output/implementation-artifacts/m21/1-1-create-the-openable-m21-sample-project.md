---
baseline_commit: 3433765
---

# Story 1.1: Create the openable M21 sample project

Status: review

## Story

As a reviewer,
I want an M21 sample project with real `.athena` files,
so that I can inspect M21 layout work through the normal Athena Theia workflow.

## Acceptance Criteria

1. Given the M21 milestone workspace, when I inspect `examples/m21/sample-project`, then it contains real `.athena` source files covering the M21 acceptance scenarios.
2. Given the Athena IDE workspace, when I use the documented M21 launch path, then the M21 sample project opens in the Athena Theia IDE without requiring users to inspect `.mjs` files.
3. Given the M21 sample project, when the first sheet is opened, then it starts from the accepted M20 sheet behavior as the visible baseline.
4. Given the M21 sample project, when repository/session diagnostics run, then the sample package is syntactically valid and uses the package syntax already accepted by the IDE/LSP.
5. Given M21 boundaries, when the sample project and docs are reviewed, then they do not introduce repository/import ecosystem behavior, cabinet authoring, physical routing, desktop-viewer scope, AI layout, or final layout-stack selection.

## Tasks / Subtasks

- [x] Create the openable M21 sample project (AC: 1, 3, 4)
  - [x] Add `examples/m21/README.md` describing the M21 proof corpus and IDE sample path.
  - [x] Add `examples/m21/sample-project/README.md` that lists the real `.athena` scenarios and how to open them.
  - [x] Add `examples/m21/sample-project/athena.yaml` and `athena.lock` using the same local governed repository shape as M20.
  - [x] Add `.athena` source files under `examples/m21/sample-project/src/` for at least:
    - [x] baseline accepted M20 canvas behavior
    - [x] power/control/terminal/load layout-intelligence acceptance
    - [x] schematic routing and label readability
    - [x] deferred boundary scope
- [x] Wire a truthful M21 IDE launch path (AC: 2, 4)
  - [x] Add `start:m21` scripts to `ide/package.json` and `ide/theia-product/package.json`, or provide an equivalent documented command that actually opens `examples/m21/sample-project`.
  - [x] If a script is used, make it pass the M21 sample-project path into the Electron/Theia startup flow instead of only launching a blank shell.
  - [x] Reuse the existing Theia workspace opening pattern from `athena-electron-reuse-e2e-main.js` where it is appropriate.
  - [x] Ensure the command does not rely on stale workspace history from a previous manual IDE run.
- [x] Document the usage path (AC: 2, 3, 5)
  - [x] Add `docs/usages/m21-proof-usage.md`.
  - [x] State clearly that customers inspect real `.athena` files in the IDE; `.mjs` fixtures, if added later, are supporting tests only.
  - [x] Carry forward accepted M20 canvas invariants: stage grid as coordinate surface, transparent overlays, popover-only `Cabinet Main`, and same-tab outline navigation.
  - [x] State M21 non-goals: repository/import ecosystem, full IEC breadth, cabinet authoring, physical routing, desktop-viewer scope, AI layout, and final layout-stack selection.
- [x] Add focused sample-project validation (AC: 1, 2, 4, 5)
  - [x] Add or update a lightweight scripted check that verifies the M21 sample project exists, contains the required files, and has no stale `.mjs`-only proof wording.
  - [x] Add or update an IDE launch/smoke check for the M21 sample-project path if this can be done without duplicating the later Story 1.2 visual proof.
  - [x] Keep visual graph workbench assertions for Story 1.2; this story only needs to prove the project is openable and valid.
- [x] Keep status and documentation clean (AC: 5)
  - [x] Update this story's Dev Agent Record when implementation finishes.
  - [x] Run the encoding audit after touching docs or text assets.
  - [x] Do not update unrelated M20 artifacts unless a stale reference would mislead M21 users.

## Dev Notes

### Current State

- M20 already has an openable sample project at `examples/m20/sample-project/`.
- The M20 sample shape includes:
  - `athena.yaml`
  - `athena.lock`
  - `README.md`
  - `src/01-schematic-sheet.athena`
  - `src/02-dense-sheet.athena`
  - `src/03-acceptance-sheet.athena`
  - `src/04-boundary-scope.athena`
- M20 usage docs say `yarn start:m20` opens the sample project, but `ide/package.json` and `ide/theia-product/package.json` currently map `start:m20` to the generic Electron start command. Do not repeat that ambiguity for M21.
- There is already a proven pattern for opening a target repository from Electron test code in `ide/theia-product/scripts/athena-electron-reuse-e2e-main.js`, using Theia `WorkspaceService.open(URI.fromFilePath(...), { preserveWindow: true })`.

### Architectural Guardrails

- Follow M21 AD-1, AD-9, AD-10, and AD-11.
- The M21 sample project is a visible IDE proof baseline. It must not be only a model fixture or script-only test corpus.
- Theia may open and present the project, but it must not become semantic or layout authority.
- Story 1.1 does not implement layout intent, layout engine, schematic route facts, or label-avoidance behavior. It creates the visible sample and launch surface that later stories must use.
- Preserve the accepted M20 canvas contract:
  - stage grid is the coordinate surface
  - sheet and component bodies do not hide the grid
  - `Cabinet Main` details are available from the top information popover only
  - top and bottom controls remain transparent canvas overlays
  - outline navigation keeps the same `.athena` editor tab

### Project Structure Notes

Likely new files:

- `examples/m21/README.md`
- `examples/m21/sample-project/README.md`
- `examples/m21/sample-project/athena.yaml`
- `examples/m21/sample-project/athena.lock`
- `examples/m21/sample-project/src/01-baseline-sheet.athena`
- `examples/m21/sample-project/src/02-layout-intelligence-acceptance.athena`
- `examples/m21/sample-project/src/03-routing-and-label-readability.athena`
- `examples/m21/sample-project/src/04-boundary-scope.athena`
- `docs/usages/m21-proof-usage.md`

Likely update targets:

- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-main.js` or a new M21-specific Electron entry/helper if needed
- `ide/theia-product/scripts/verify-athena-start.js` or a new M21 sample smoke script if the launch path needs automated proof
- `ide/theia-frontend/scripts/*` only if adding a lightweight source/static sample-project guard

Keep code edits scoped. This story should not introduce `kernel/layout-model`, `kernel/layout-engine`, or `kernel/routing-model`; those belong to Epic 2 and Epic 3.

### Sample Content Guidance

The `.athena` files should use syntax already accepted by the IDE/LSP. Prefer the M20 sample project style unless existing compiler tests prove a richer syntax is safe.

The M21 acceptance sample should include names that make future layout intent obvious, such as:

- power source / supply
- protection / breaker
- controller / PLC
- HMI
- terminal block
- primary load / motor

Do not claim actual M21 layout intelligence in Story 1.1. The source files can describe scenarios that later stories will project and improve.

### Testing Requirements

- Run Node/Yarn checks sequentially; do not run Gradle verification concurrently with any other Gradle task.
- Minimum expected checks after implementation:
  - sample-project existence/static check added by this story
  - M21 launch or smoke check if introduced by this story
  - `yarn workspace @engineeringood/athena-theia-product build` if Theia product scripts or Electron entry files change
  - `yarn start:smoke` or the new `start:m21` smoke path if one is added
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- If only docs/sample files are added and no product code changes, still run the sample-project static check and encoding audit.

### Verification Expectations

Before marking this story complete, prove:

1. The M21 sample project exists on disk with real `.athena` files.
2. The documented launch path targets `examples/m21/sample-project`, not a blank or stale workspace.
3. The sample package syntax is accepted by the same parser/LSP path used by M20.
4. Usage docs do not tell users to inspect `.mjs` files to understand the feature.
5. Boundary language remains explicit.

### References

- [Source: `_bmad-output/implementation-artifacts/m21/epics.md` - Epic 1, Story 1.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m21/prd.md` - FR-1, FR-2, FR-10, FR-11]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m21/ARCHITECTURE-SPINE.md` - AD-1, AD-9, AD-10, AD-11]
- [Source: `_bmad-output/implementation-artifacts/m20/m20-ui-acceptance-deep-retrospective-2026-07-17.md` - M20 visible proof lesson]
- [Source: `examples/m20/sample-project/README.md`]
- [Source: `docs/usages/m20-proof-usage.md`]
- [Source: `ide/theia-product/scripts/athena-electron-reuse-e2e-main.js` - target repository opening pattern]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `node --test ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs` passed after updating the static guard to the final Theia CLI workspace-fragment approach.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM21SampleProjectCompilerTest` passed.
- `yarn workspace @engineeringood/athena-theia-product build` passed.
- `yarn workspace @engineeringood/athena-theia-product start:smoke:m21` initially failed because the opener assumed a frontend `require` bridge; final run passed with `workspace=D:\Aaron\workspace\projects\2026\eos\Athena\examples\m21\sample-project` and Java 25 resolved.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added the M21 openable sample project with four real `.athena` scenarios: M20 baseline, layout-intelligence acceptance, schematic routing/label readability, and deferred boundary scope.
- Added a documented `yarn start:m21` Theia workflow that opens `examples/m21/sample-project` directly instead of a blank shell or `.mjs` fixture.
- Added an Electron smoke test that proves the M21 sample project is the actual loaded workspace and does not rely on stale workspace history.
- Kept sample syntax inside currently accepted local Athena grammar and used compiler-safe device types while preserving the future layout roles in names and metadata.
- Documented the M21 usage path, M20 canvas invariants, and explicit non-goals.

### File List

- `_bmad-output/implementation-artifacts/m21/1-1-create-the-openable-m21-sample-project.md`
- `_bmad-output/implementation-artifacts/m21/sprint-status.yaml`
- `docs/usages/m21-proof-usage.md`
- `examples/m21/README.md`
- `examples/m21/sample-project/README.md`
- `examples/m21/sample-project/athena.yaml`
- `examples/m21/sample-project/athena.lock`
- `examples/m21/sample-project/src/01-baseline-sheet.athena`
- `examples/m21/sample-project/src/02-layout-intelligence-acceptance.athena`
- `examples/m21/sample-project/src/03-routing-and-label-readability.athena`
- `examples/m21/sample-project/src/04-boundary-scope.athena`
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-electron-open-workspace-main.js`
- `ide/theia-product/scripts/verify-athena-m21-sample-project.js`
- `ide/theia-frontend/scripts/athena-m21-sample-project.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM21SampleProjectCompilerTest.kt`

## Change Log

- 2026-07-17: Created M21 Story 1.1 with visible IDE sample-project and truthful launch-path requirements.
- 2026-07-17: Implemented the openable M21 sample project, truthful Theia launch/smoke path, usage documentation, and validation coverage.
