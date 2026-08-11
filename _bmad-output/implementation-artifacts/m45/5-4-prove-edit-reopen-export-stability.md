---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 5.4: Prove Edit, Reopen, Export Stability

Status: review

## Story

As an engineer,
I want durable edit, reopen, and export evidence,
so that package-backed authored page use is proven stable.

## Acceptance Criteria

1. Valid presentation, representation, and engineering operations against active `examples/m45/rolling-shutter` use full Source Revision CAS, commit only accepted source/package/Sheet/binding/lock bytes, recompile before publication, journal the accepted transaction, and preserve unaffected occurrence, Function, Entity, Port, Relationship, binding, Part-binding, package, and trace identities.
2. Invalid stale, invalid-package, incompatible Part, invalid Variant, invalid Placeholder, and invalid relationship/port operations fail closed: no source/Sheet/binding/lock mutation, no journal entry, no replacement asset, and previous accepted READY scene remains visible.
3. Restart/reopen from the M45 repository root reproduces the accepted canonical scene, package bindings, occurrence identities, source/package lineage, and 17x16 ruler/frame contract without fallback assets or external-format/reference paths.
4. Repeated renderer-neutral export of the accepted M45 scene produces deterministic SVG and PNG evidence. SVG/PNG exports contain admitted bundle geometry, thin technical routes, compact labels, tiny markers, one-pixel square frame, clean white drawing interior, and no default grid or bottom title/table block.
5. Product proof captures desktop and narrow screenshots under `_bmad-output/implementation-artifacts/m45/screenshots/`, validates workspace activation and LSP root before assertions, and records deterministic operation/reopen/export evidence under M45 artifacts.
6. Existing M45 frontend/Kotlin/LSP/product coverage and encoding/source-set hygiene pass sequentially. No `.elmt`, HTML, XML, `reference/elements`, or `reference/elements_contrib` parser, converter, compiler, runtime, or fixture path is added.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 2)
  - [x] Inspect current `EditOperation`, `SourceRevision`, transaction engine, operation journal, representation/engineering handlers, placement/style operations, and active M45 test seams.
  - [x] Add focused RED tests for one accepted operation and each required rejected class. Assert exact before/after source, Sheet, binding, lock, scene, journal, and identity behavior.
  - [x] Extend only typed server operation paths required to make all checks pass. Do not add frontend source writes, duplicate transaction state, compatibility fallback, or force-apply logic.

- [x] Task 2 (AC: 1, 3)
  - [x] Add reopen/restart integration proof for `examples/m45/rolling-shutter`: materialize lock from compiler authority, restart a fresh LSP/runtime session, reopen repository root then source, and assert canonical scene/occurrence/binding/provenance stability.
  - [x] Include FunctionRepresentationBinding, FunctionPartBinding where present, package item lineage, and renderer asset ids in the stable identity comparison.
  - [x] Keep persisted placement as `SemanticPlacementIntent -> LogicalLayoutCoordinate -> compiler-derived PhysicalGeometryCoordinate`; Snap remains independent.

- [x] Task 3 (AC: 3, 4)
  - [x] Locate and reuse current canonical Scene export seam. Add deterministic M45 SVG/PNG export proof only through Canonical Scene and admitted AssetBundle data.
  - [x] Assert repeated exports are byte/digest deterministic where contract permits; otherwise assert canonical export digest and decoded visual facts deterministically.
  - [x] Reject missing/invalid/non-ready package geometry at the compiler/LSP boundary. Export must never create fallback boxes, raw external assets, or renderer-owned geometry.

- [x] Task 4 (AC: 4, 5)
  - [x] Extend product proof only as needed to capture M45 desktop/narrow screenshots and export evidence after workspace-root activation.
  - [x] Verify Explorer/workspace root, repository state `READY`, and LSP root equal active M45 example before scene/visual assertions.
  - [x] Preserve Engineering Document Visual Golden Rule: full center editor panel, narrow flush rulers, aligned corner, one-pixel frame, blank white interior, thin fixed-screen-space linework, invisible port hit radius, no default grid/title block.

- [x] Task 5 (AC: 1-6)
  - [x] Rebuild affected kernel, LSP, frontend, and Theia product outputs before final proof. On Windows run Gradle verification strictly sequentially.
  - [x] Save operation transcript, reopen evidence, lock/lineage snapshot, exports, screenshots, and final proof JSON only under `_bmad-output/implementation-artifacts/m45/`.
  - [x] Run focused and affected regression suites, frontend tests, product E2E, `tools/encoding-audit.ps1`, and `tools/source-set-hygiene-audit.ps1`.

### Review Findings

- [x] [Review][Patch] Exclude a currently opened companion from Project Semantic Graph input. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt:254]
- [x] [Review][Patch] Carry authored Variant and typed Placeholder values through FunctionRepresentationBinding admission and fail-closed transactions. [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationBindingCompanion.kt:31]
- [x] [Review][Patch] Regenerate and validate export/evidence artifacts instead of hardcoding PASS over stale files. [ide/theia-product/scripts/verify-athena-m45-export.js:31]
- [x] [Review][Patch] Verify actual workspace root, LSP repository root, READY state, and exact golden counts before product screenshots. [ide/theia-product/scripts/athena-m45-proof-main.js:80]
- [x] [Review][Patch] Assert rejected stale operations preserve every governed file byte as well as scene and journal. [ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt:215]
- [x] [Review][Patch] Assert compact labels, clean white interior, and absence of bottom title/table content in deterministic export proof. [ide/theia-product/scripts/verify-athena-m45-export.js:43]

## Dev Notes

### M45 Authority Guardrails

- Athena source owns Entity, Function, Engineering Port, Relationship, FunctionPartBinding, and FunctionRepresentationBinding.
- Direct local packages own native Symbol, Element, Part, Macro, Variant, and Placeholder reusable facts. SVG owns package-local geometry only.
- `PACKAGE_READY` is the only package/item state allowed into lock, runtime snapshot, binding, Source Revision, Canonical Scene, export, or Theia paint.
- Native M45 supports no `.elmt`, HTML, XML, or `reference/` runtime/import/fixture path. Do not add one.
- Macro remains representation reuse. Variant preserves Element interface fingerprint. Placeholder substitutes declared non-interface representation fields. No package item may create relationships, functions, parts, capabilities, or rules.
- Canonical Scene remains renderer-neutral. Konva and Theia are disposable read/intent clients.

### Transaction Rules

- Reuse `EditOperation`, `SourceRevisionService`, `SourceTransactionEngine`, `SessionOperationJournal`, `RepresentationAndEngineeringOperationHandler`, `PlacementOperationHandler`, `SetStyleOperationHandler`, `RepresentationBindingCompanion`, and `FunctionPartBindingCompanion`.
- Every mutation uses Source Revision CAS and declared writable files. Stage source/Sheet/binding/lock inputs, admit and compile, then durable commit; journal/snapshot/scene publish only after accepted commit.
- Rejection must prove no partial write. Never repair a failed operation by fallback scene mutation, frontend text assembly, force apply, or previous-version substitution.
- Reopen/restart proof opens the repository root first. A `NO FOLDER OPENED` state is an immediate E2E failure; do not diagnose canvas until workspace/root/LSP signals are correct.

### 5.3 Intelligence

- Story 5.3 published 13 Function occurrences and 10 routes in active M45 rolling-shutter page. Every visible occurrence has an admitted package `representationRef` and `assetId`.
- Element lock entries inherit exactly one package-local admitted SVG resource from their composed Symbol. LSP fails closed if binding, locked Element, or resource is missing.
- Product evidence already exists:
  - `_bmad-output/implementation-artifacts/m45/m45-product-proof.json`
  - `screenshots/m45-rolling-shutter-desktop-1920x1080.png`
  - `screenshots/m45-rolling-shutter-narrow-720x900.png`
- `AthenaNavigationIndex` now indexes Function nested ports for semantic inspection. Preserve this source-trace behavior.
- Existing M45 proof confirms repository-root workspace activation, `READY` publication, 17 columns, 16 rows, 13 occurrences, 10 routes, and one source editor. Do not regress those assertions.

### Export And Visual Rules

- Reuse an existing Canonical Scene export boundary. Do not export by screenshot scraping, DOM SVG assembly, raw package filesystem scans, or direct Konva internals.
- Admitted AssetBundle bytes are the only asset source. Export includes package geometry through published asset ids and bundle entries.
- `ScenePort.hitRadius` is invisible interaction geometry. Use asset-defined tiny connection markers only.
- Zoom must not increase visible route/marker/label weight. Keep compact technical linework and square one-pixel frame.
- Drawing interior stays white and clean. Rulers are edge chrome only; no default cell/micro-grid or bottom table.

### Candidate Files And Tests

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SessionOperationJournal.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandler.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/JournalUndoRedoTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt`
- `ide/theia-product/scripts/verify-athena-m45-product-proof.js`
- `ide/theia-product/scripts/athena-m45-proof-main.js`
- `kernel/svg-renderer/` and `kernel/presentation-model/` existing export seams
- `examples/m45/rolling-shutter/`

### Verification

Run Gradle commands one at a time. Final evidence requires, at minimum:

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests <focused-M45-tests>
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests <focused-M45-tests>
Push-Location ide\theia-frontend; yarn test; Pop-Location
Push-Location ide; yarn build; yarn verify:m45-proof; Pop-Location
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

Save all proof artifacts under active M45 implementation artifacts only. No M45/Demo/Proof/Sample/V0/V1 production classes.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Ultimate context engine analysis completed - comprehensive developer guide created.
- M45 visual regression reproduced: route planner selected first valid edge detour, creating large rectangular paths in the rendered page.
- Replaced first-valid route selection with deterministic shortest legal orthogonal path selection; moved control occurrences to non-crossing logical Sheet points.
- Rebuilt compiler-owned `athena.lock` after Sheet source change; rebuilt LSP/frontend/Product and captured fresh desktop/narrow proof.
- Reproduced 20 unresolved Function Port diagnostics plus one false Sheet Companion syntax diagnostic through the real LSP `didOpen` path.
- Added RED coverage for canonical `Entity.Function.Port` declaration indexing, then indexed Function-owned Ports without compatibility aliases.
- Excluded Sheet, Sheet Style, and representation binding companions from project semantic source discovery; active M45 source now opens with zero error/warning diagnostics.
- Full compiler, LSP, package runtime, interaction model, SVG renderer, frontend, product, export, encoding, and source-set verification passed sequentially.

### Completion Notes List

- Story context created from final M45 PRD/architecture/epics, active sprint order, Story 5.3 completion record, active M45 proof artifacts, current transaction/export seams, and current git context.
- Current stack is repository-pinned Kotlin 2.4.0, LSP4J 0.23.1, Theia 1.73.1, Konva 10.3.0, React 18.3.1, TypeScript 5.9.2, Node >=22, and `svg-safe-1`; no dependency or external research change is required.
- Fixed distorted M45 routes; package SVGs remain contain-fit and preserve intrinsic aspect ratio.
- Focused M45 compiler page test, full `:ide:lsp:test`, frontend tests, product build, M45 product proof, encoding audit, and source-set hygiene audit pass sequentially.
- Accepted Move, Change Symbol, Bind Part, and Reconnect operations preserve required identities and journal exactly one accepted source transaction each; stale and invalid operations leave all governed bytes, scene, and journal unchanged.
- Fresh LSP reopen reproduces source revision, binding/Part-binding lineage, package assets, occurrence/relationship identities, and 17x16 frame contract.
- Deterministic SVG/PNG proof uses Canonical Scene plus admitted AssetBundle only. Routes and frame use one-pixel non-scaling strokes; port hit geometry remains invisible.
- Desktop and narrow rebuilt-product screenshots show the active M45 workspace, full center Engineering Document, aligned rulers, clean white page, and zero error/warning diagnostics.
- Project semantic diagnostics now index Function-owned Ports by canonical authored path and never parse companion authorities as engineering source.
- Review fixed active-editor-dependent Project Semantic Graph pollution for every companion authority.
- Variant and typed Placeholder edits now enter the closed representation transaction contract and reject unadmitted package items without mutation.
- Product proof now reads actual Theia workspace roots, Repository Session lifecycle, and LSP Repository Graph root; exact 13-occurrence/10-route golden counts are required.
- Export proof deletes stale evidence, regenerates transaction/reopen/SVG evidence, parses every required fact, and validates white-page/compact-label/no-title-table visual facts.

### File List

- `_bmad-output/implementation-artifacts/m45/5-4-prove-edit-reopen-export-stability.md`
- `_bmad-output/implementation-artifacts/m45/sprint-status.yaml`
- `examples/m45/rolling-shutter/athena.lock`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.athena`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M45RollingShutterPageTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDeclarationIndexerTest.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/EditOperationContractTest.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/svg/AthenaSvgRenderer.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/svg/AthenaSvgRendererTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `kernel/language/src/main/antlr/com/engineeringood/athena/language/antlr/Athena.g4`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/antlr/AthenaAntlrParseAdapter.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/EditOperationContracts.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-edit-operation.schema.json`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationWireMapper.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationBindingCompanion.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandler.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M45ExportStabilityTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M45SvgExportEvidenceGenerator.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandlerTest.kt`
- `ide/theia-product/scripts/athena-m45-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m45-product-proof.js`
- `ide/theia-product/scripts/verify-athena-m45-export.js`
- `ide/theia-frontend/scripts/generate-diagram-contracts.mjs`
- `ide/theia-frontend/src/browser/athena-workbench-automation.ts`
- `ide/theia-frontend/src/browser/diagram/generated/`
- `ide/package.json`
- `_bmad-output/implementation-artifacts/m45/m45-product-proof.json`
- `_bmad-output/implementation-artifacts/m45/m45-story-5-4-proof.json`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-operation-transcript.txt`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-reopen-evidence.txt`
- `_bmad-output/implementation-artifacts/m45/evidence/m45-lock-lineage-snapshot.txt`
- `_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.svg`
- `_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.png`
- `_bmad-output/implementation-artifacts/m45/exports/m45-export-proof.json`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-narrow-720x900.png`

### Change Log

- 2026-08-09: Created comprehensive BMad developer story context; status ready-for-dev.
- 2026-08-09: Corrected M45 route path selection and refreshed product proof; story remains in-progress pending edit/reopen/export evidence.
- 2026-08-10: Completed transaction, reopen, deterministic export, visual, semantic-diagnostic, and full affected regression proof; status moved to review.
- 2026-08-10: Adversarial review fixed six authority/proof gaps; focused Kotlin/frontend tests, rebuilt Theia product, actual-root Electron proof, and self-regenerating export proof passed; status done.
