---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 5.1: Freeze The Professional Drawing Product Contract

Status: review

## Story

As an Athena product owner,
I want one executable visual contract and one focused workbench surface,
so that implementation cannot substitute another Cabinet strip or architecture proof.

## Acceptance Criteria

1. **Given** the approved screenshot and QET reference project, **when** target-contract tests inspect
   them, **then** the contract records 17 columns, 8 rows, required sheet regions, required
   device/function families, terminal-label families, title metadata, and visual tolerance without
   importing QET runtime.
2. **Given** the M34 workbench, **when** the dedicated sample opens, **then** one professional
   `Control Drawing` surface backed by Athena's existing `schematic` projection is primary and no
   Cabinet/Documentation/Schematic view selector is shown.
3. **Given** the dedicated sample semantic source, **when** authority tests run, **then** enclosure,
   rails, route channels, frame, zones, and title labels are not authored as devices, and no active
   M34 product derivation infers engineering roles from identifier prefixes.
4. **Given** all previous acceptance criteria are green, **when** mandatory final polish/purge runs,
   **then** source, tests, target evidence, docs, generated outputs, and workspace state are deeply
   reviewed; stale or duplicate Story 5.1 artifacts are removed; and RED/GREEN, AC-to-evidence, and
   three-layer review are recorded.

**Implements:** FR-44, FR-49..FR-50, NFR-13.

## Tasks / Subtasks

- [x] Add Story 5.1 RED contracts before production edits (AC: 1..3)
  - [x] Add `athena-m34-professional-drawing-target-contract.test.mjs` to assert the approved target
        path, 17x8 grid, required circuit/symbol/terminal/title families, dedicated sample path, and
        QET offline-only boundary.
  - [x] Update the workbench model test to require one `Control Drawing` product surface backed by
        `schematic`, with compatibility views hidden.
  - [x] Add a source-authority test that rejects fake drawing-structure devices and active
        identifier-prefix role inference in the corrective product path.
  - [x] Run the focused Node tests and record their pre-implementation failures.
- [x] Establish the dedicated professional-drawing sample contract (AC: 1, 3)
  - [x] Create `examples/m34/professional-control-drawing/athena.yaml` with one admitted source root
        and package-local representation root.
  - [x] Create the initial compilable semantic source containing only real engineering devices and
        ports; Story 5.3 will expand it to the full circuit.
  - [x] Create the package directory skeleton with ordinary Java-style package hierarchy and no
        copied `.qet`, `.elmt`, XML manifest, or runtime dependency.
  - [x] Rewrite `m34-professional-renderer-target.md` as the normative Epic 5 contract rather than a
        next-milestone scope gate.
- [x] Focus the workbench on the accepted product surface (AC: 2)
  - [x] Change `resolveAthenaGraphPrimaryProductSurface` to select the existing `schematic` view and
        expose the product name `Control Drawing`.
  - [x] Replace Cabinet-specific activation naming/logic without altering the backend view registry
        or adding a fifth projection view.
  - [x] Keep the single product-surface button informational when already active and do not expose
        compatibility selectors.
  - [x] Update frontend contract tests and generated frontend bundle only through the existing build.
- [x] Remove product-path semantic guessing (AC: 3)
  - [x] Replace `ProjectSemanticSchematicLayoutFactDeriver.roleFor` name-prefix classification with
        typed semantic property classification and a stable diagnostic/default role.
  - [x] Add focused Kotlin tests proving names such as `QF1`, `KM1`, and `XT1` do not determine role.
  - [x] Run the focused compiler test sequentially and record RED/GREEN evidence.
- [x] Perform mandatory final polish/purge and evidence review (AC: 4)
  - [x] Review changed source, tests, docs, sample files, built frontend artifacts, and workspace
        state; remove stale Story 5.1 outputs without touching unrelated user changes.
  - [x] Run focused Node tests, focused sequential Gradle tests, and the UTF-8 encoding audit.
  - [x] Record AC-to-evidence, three-layer review, completion notes, and exact File List before moving
        the story to `review`.

## Dev Notes

### Scope Boundary

Story 5.1 freezes and exposes the corrected product contract. It does not add Engineering Function
syntax, explicit placement, IEC package content, routing, or final visual polish; Stories 5.2-5.5 own
those changes. It may create only a minimal compilable dedicated sample skeleton.

### Required Architecture

- Reuse the existing `schematic` projection id. `Control Drawing` is the customer product surface,
  not a new fifth backend view.
- QET is offline evidence only. No product source or test may load `.qet`/`.elmt` to produce runtime
  output.
- Target tests may inspect the approved repository assets, but product compilation must remain
  independent of them.
- Drawing structure is composition truth, never fake Engineering Components.
- Semantic role classification must derive from typed authored/compiled facts, never naming
  conventions.
- Preserve the current dirty worktree. Do not reset, revert, or overwrite unrelated M33/M34/user
  changes.

### Existing Code To Modify

- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts` currently selects `cabinet` in
  `resolveAthenaGraphPrimaryProductSurface`.
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` calls
  `requiresAthenaCabinetProductActivation` and switches to `cabinet`.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
  currently maps `PLC/HMI/XT/QF/M` prefixes to layout roles.
- `examples/m34/sample-project/src/01-native-cabinet-proof.athena` contains fake enclosure, rail,
  route-channel, and title-label devices. Do not mutate it as the corrective sample; isolate the new
  dedicated project and remove the old path only at final Epic 5 purge.

### Testing Requirements

- Node contract tests use the repository's existing `node --test` style.
- Kotlin tests use `kotlin.test` and focused Gradle test filters.
- Gradle commands must run sequentially on Windows.
- No screenshot claim is allowed in Story 5.1; final real Electron proof belongs to Story 5.5.
- Every Story 5.1 task ends with a direct AC-to-evidence check.

### References

- [Sprint Change Proposal](../../planning-artifacts/sprint-change-proposal-2026-07-26-m34-professional-control-drawing.md)
- [M34 PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md#corrective-product-acceptance---professional-control-drawing)
- [M34 Architecture Spine](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md#corrective-architecture-decisions---professional-control-drawing)
- [M34 Epics](epics.md#epic-5-close-the-professional-control-drawing-gap)
- [Professional Renderer Target](m34-professional-renderer-target.md)
- [Approved Target](../../../draft/screenshort/equipement_d'un_volet_roulant.png)
- [QET Reference Project](../../../reference/qelectrotech-source-mirror/examples/schema_indus.qet)
- [Story 4.3](4-3-prove-and-hand-off-the-m34-cabinet-product.md)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: focused Node contracts failed before production edits because the dedicated sample and
  normative target contract did not exist, the workbench still selected Cabinet, and source
  authority still allowed identifier-prefix classification.
- RED: `ProjectSemanticSchematicLayoutFactDeriverTest` classified `QF1`, `M1`, and `XT1` from their
  names instead of authored `type` facts.
- GREEN: `node --test scripts/athena-m34-professional-drawing-target-contract.test.mjs
  scripts/athena-graph-workbench-model.test.mjs
  scripts/athena-m34-professional-drawing-source-authority.test.mjs` passed 38/38.
- GREEN regression: `node --test scripts/*.test.mjs` passed 237/237.
- GREEN compiler: `./gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests
  "com.engineeringood.athena.compiler.semantic.ProjectSemanticSchematicLayoutFactDeriverTest"
  --tests "com.engineeringood.athena.compiler.AthenaM34ProfessionalDrawingSampleContractTest"`
  completed successfully.
- GREEN build: `yarn build` from `ide` completed Theia browser, node, Electron, graph adapter, and LSP
  runtime builds with zero build errors.
- GREEN workspace: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- AC 1: executable contract verifies the approved 1050x720 raster, 17x8 sheet grid, 2% aspect
  tolerance, required engineering families, title regions, and offline-only QET evidence.
- AC 2: one `Control Drawing` product surface deterministically maps to the existing `schematic`
  projection; raw Cabinet, Documentation, Schematic, and Wiring peer selectors remain hidden.
- AC 3: dedicated project contains only real engineering devices; repository compilation succeeds;
  authored `type` facts own layout-role classification and unknown types default to `ANNOTATION`.
- AC 4: reviewed source authority, compiler/transport boundary, and Theia adapter/workspace output;
  no duplicate Story 5.1 sample, QET/ELMT/XML runtime asset, fifth projection, or stale Cabinet
  activation helper remains in the active path.
- Three-layer review: semantic source owns devices/ports/connections; compiler owns role and
  projection facts; frontend only exposes the selected governed product projection.
- Story 5.1 makes no Electron screenshot or final visual-match claim. Story 5.5 owns that proof.

### File List

- `_bmad-output/implementation-artifacts/m34/5-1-freeze-the-professional-drawing-product-contract.md`
- `_bmad-output/implementation-artifacts/m34/m34-professional-renderer-target.md`
- `_bmad-output/implementation-artifacts/m34/sprint-status.yaml`
- `examples/m34/professional-control-drawing/athena.yaml`
- `examples/m34/professional-control-drawing/src/01-control-drawing.athena`
- `examples/m34/professional-control-drawing/packages/representation/com/engineeringood/m34/control/README.md`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `ide/theia-frontend/scripts/athena-m30-svg-bounds-regression.test.mjs`
- `ide/theia-frontend/scripts/athena-m33-workbench-primary-surface.test.mjs`
- `ide/theia-frontend/scripts/athena-m34-professional-drawing-source-authority.test.mjs`
- `ide/theia-frontend/scripts/athena-m34-professional-drawing-target-contract.test.mjs`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-product/src-gen/frontend/index.js`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalDrawingSampleContractTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticSchematicLayoutFactDeriverTest.kt`

### Change Log

- 2026-07-26: Created through the BMAD create-story workflow after approved M34 course correction.
- 2026-07-26: Froze the executable target, dedicated sample, one Control Drawing surface, and typed
  semantic role authority; completed build, regression, encoding, and final purge review.
