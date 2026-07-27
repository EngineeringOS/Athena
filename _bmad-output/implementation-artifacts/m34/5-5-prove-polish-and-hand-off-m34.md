---
baseline_commit: 0311ad67c08779db3d1def271d0ac948a4683128
---

# Story 5.5: Prove Polish And Hand Off M34

Status: review

## Story

As Athena's product owner,
I want final M34 evidence and cleanup captured against the approved professional control drawing,
so that M34 closes truthfully without stale Cabinet/XML fallback paths or unverifiable visual claims.

## Acceptance Criteria

1. **Given** Story 5.4's professional `Control Drawing` path,
   **when** final product verification runs,
   **then** Electron opens `examples/m34/professional-control-drawing`, exposes exactly one visible
   product surface named `Control Drawing`, uses backing view `schematic`, and writes fresh desktop
   and narrow screenshots.
2. **Given** the final proof payload,
   **when** evidence is reviewed,
   **then** it proves 22 graphic occurrences, 34 routed semantic connections, 62 terminal bindings,
   36 visible drawing-layer items, 17 column zones, 8 row zones, zero legacy representation facts,
   zero raw XML/SVG/QET runtime authority, zero fallback graphics, and no route body intersections.
3. **Given** M34 is pre-release,
   **when** final polish/purge runs,
   **then** obsolete M33 XML/SVG catalog active paths, old M33 product smoke contracts, generated
   `.athena/snapshots`, and duplicate product-surface assumptions are removed or explicitly recorded
   as historical artifacts only.
4. **Given** all final gates are green,
   **when** the story moves to review,
   **then** the handoff note records exact commands, proof counts, screenshot paths, remaining risks,
   and the next milestone boundary without claiming IEC/EPLAN/QET equivalence.

**Implements:** FR-50, FR-37, FR-41, FR-43, NFR-13.

## Tasks / Subtasks

- [x] Run final product E2E proof (AC: 1..2)
  - [x] Rebuild the LSP distribution consumed by Electron.
  - [x] Build the Theia product after the final source/doc state.
  - [x] Run the M34 Electron product verifier against `examples/m34/professional-control-drawing`.
  - [x] Verify the generated screenshots exist and the proof payload reports the required counts.
- [x] Purge stale generated and legacy active paths (AC: 3)
  - [x] Verify no active non-BMAD code references `M33CabinetPackageSet`,
        `M33IecDrawingSvgCatalogService`, `athena/drawingSvgCatalog`, or M33 XML package manifests.
  - [x] Verify `examples/m34/*/.athena/snapshots` is absent after final cleanup.
  - [x] Keep QET references as offline visual/domain evidence only; do not add runtime QET loading.
- [x] Publish the final M34 evidence handoff (AC: 4)
  - [x] Add a concise final proof note with exact command evidence, counts, screenshot paths, and
        remaining risks.
  - [x] State the milestone boundary: M34 proves one professional Athena-governed control drawing;
        it does not claim full IEC compliance, EPLAN equivalence, or broad multi-view completion.
- [x] Run mandatory final verification and BMAD evidence review (AC: 1..4)
  - [x] Run focused frontend/product proof commands and broad Gradle verification sequentially.
  - [x] Run UTF-8 encoding audit and `git diff --check`.
  - [x] Update AC-to-evidence, three-layer review, File List, Change Log, and sprint status truthfully.

## Dev Notes

### Scope

- This story is closure/proof only. Do not add a new renderer feature unless verification exposes a
  real defect.
- Final M34 product surface is `Control Drawing`, display name `Control Drawing`, backing view id
  `schematic`.
- Active example is `examples/m34/professional-control-drawing/src/01-control-drawing.athena`.
- Do not reintroduce `examples/m34/sample-project` as the product proof target.

### Required Evidence

- Product verifier sentinel: `ATHENA_M34_CONTROL_DRAWING_PRODUCT_PROOF=`.
- Required screenshots:
  `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-desktop.png`
  and `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-narrow.png`.
- Required proof counts: 22 graphic occurrences, 34 routes, 62 terminal bindings, 36 drawing-layer
  items, 17 columns, 8 rows, 0 legacy representation facts, 0 non-orthogonal segments, 0 route body
  intersections.

### Previous Story Intelligence

- Story 5.4 moved the normal compiler publication for the professional repository to the typed
  `Control Drawing` presentation and removed the obsolete M33 XML/SVG LSP catalog.
- Story 5.4 broad verification passed with `.\gradlew.bat --no-daemon --console=plain test`.
- Generated `.athena/snapshots` are reproducible caches and must not become source authority.
- The worktree includes accumulated M33/M34 changes. Do not reset or revert unrelated work.

### Testing And Build Rules

- Never run Gradle verification commands concurrently on Windows.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after docs/text edits.
- Treat `git diff --check` line-ending warnings as warnings only when exit code is 0.

### References

- [Story 5.4](5-4-compose-and-route-the-professional-control-drawing.md)
- [Approved sprint change](../../planning-artifacts/sprint-change-proposal-2026-07-26-m34-professional-control-drawing.md)
- [M34 corrective PRD](../../planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md#corrective-product-acceptance---professional-control-drawing)
- [M34 corrective architecture](../../planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md#corrective-architecture-decisions---professional-control-drawing)
- [Approved target image](../../../draft/screenshort/equipement_d'un_volet_roulant.png)

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
- GREEN: `.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist`
- GREEN: `yarn build` in `ide/theia-frontend`
- GREEN: `yarn build` in `ide/theia-product`
- GREEN: `yarn build` in `ide`
- GREEN: `node .\ide\theia-frontend\scripts\athena-m34-professional-control-drawing-paint-contract.test.mjs`
- GREEN: `node .\ide\theia-frontend\scripts\athena-m34-product-smoke-contract.test.mjs`
- GREEN/E2E: `node .\ide\theia-product\scripts\verify-athena-m34-sample-project.js`
- GREEN: `.\gradlew.bat --no-daemon --console=plain test`
- GREEN: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
- GREEN: `git diff --check`
- PURGE: `git clean -fd -- examples/m34/professional-control-drawing/.athena/snapshots`
- PURGE: `git clean -fd -- examples/m33 ... M33Iec* ... athena-m33-*.test.mjs`
- CLEAN: `rg "M33Iec|M33Cabinet|athena-m33-|examples/m33|start:m33|..." -g '!_bmad-output/**' -g '!ide/theia-frontend/scripts/athena-m34-product-smoke-contract.test.mjs'`
- CLEAN: `Get-ChildItem -Path .\examples\m34 -Directory -Recurse -Force -Filter .athena ... snapshots`

### AC-To-Evidence Review

- AC-1: Product verifier opened `examples/m34/professional-control-drawing`, exposed exactly one visible
  product surface named `Control Drawing`, used backing view `schematic`, and wrote fresh desktop/narrow
  screenshots under `_bmad-output/implementation-artifacts/m34/screenshots/`.
- AC-2: Proof payload reported 22 graphic occurrences, 34 semantic routes, 62 terminal bindings,
  36 drawing-layer items, 17 columns, 8 rows, 0 legacy representation facts, 0 non-orthogonal segments,
  and 0 route body intersections.
- AC-3: Active stale M33 XML/SVG catalog paths, M33 package fixtures/scripts/examples, and generated
  `.athena/snapshots` were purged or left only as BMAD historical records. QET remains offline reference
  evidence only; no runtime QET/XML/SVG metadata authority was added.
- AC-4: Final handoff proof is recorded in `m34-final-control-drawing-e2e-proof.md` with exact counts,
  screenshot paths, authority boundaries, remaining risks, and the explicit non-claim boundary.

### Three-Layer Review

- Architecture: The active path is Athena source/material definitions -> typed compiler/professional
  drawing result -> typed Presentation/LSP payload -> paint-only Theia. XML/QET/M33 catalogs are not
  runtime authority.
- Product: M34 proves one focused `Control Drawing` surface, not three half-finished views and not
  QET/EPLAN equivalence. Cabinet/documentation/schematic selector competition remains outside this closeout.
- Verification: Focused compiler/LSP/frontend/product E2E checks, broad Gradle test, encoding audit,
  diff hygiene, legacy-reference search, and snapshot absence checks were run after purge.

### Completion Notes List

- Closed M34 against the dedicated professional control drawing example:
  `examples/m34/professional-control-drawing/src/01-control-drawing.athena`.
- Recorded final proof in `m34-final-control-drawing-e2e-proof.md` with sentinel
  `ATHENA_M34_CONTROL_DRAWING_PRODUCT_PROOF=`.
- Purged generated `.athena/snapshots`, `examples/m33`, untracked M33 IEC runtime library/tests,
  stale `athena-m33-*` frontend scripts, and dead `start:m33` workspace/product scripts from the active
  workspace.
- Remaining boundary: the result is the first Athena-governed professional control drawing proof. It is
  not a full IEC compliance engine, EPLAN/QET clone, or broad multi-view product.

### File List

- `_bmad-output/implementation-artifacts/m34/5-5-prove-polish-and-hand-off-m34.md`
- `_bmad-output/implementation-artifacts/m34/m34-final-control-drawing-e2e-proof.md`
- `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-desktop.png`
- `_bmad-output/implementation-artifacts/m34/screenshots/m34-control-drawing-product-smoke-narrow.png`
- `examples/m33/` (deleted)
- `examples/m34/professional-control-drawing/.athena/snapshots/` (deleted)
- `ide/package.json`
- `ide/theia-product/package.json`
- `ide/theia-frontend/scripts/athena-m33-cleanup-ledger-contract.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-live-cabinet-composition.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-retrospective-handoff-contract.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-sheet-composition-authority.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-visual-review-checklist.test.mjs` (deleted)
- `ide/theia-frontend/scripts/athena-m33-workbench-primary-surface.test.mjs` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibrary.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackage.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCatalog.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCompilationService.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolSupport.kt` (deleted)
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/M33IecTerminalReferenceSymbolLibrary.kt` (deleted)
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecCoreSymbolLibraryTest.kt` (deleted)
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecRepresentationPackageTest.kt` (deleted)
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecSymbolCompilationServiceTest.kt` (deleted)
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/M33IecTerminalReferenceSymbolLibraryTest.kt` (deleted)

### Change Log

- 2026-07-26: Created through BMAD create-story flow from corrective Epic 5 closure scope.
- 2026-07-26: Captured final M34 product proof, purged stale generated/M33 active artifacts, and moved
  story to review.
