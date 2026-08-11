---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 5.3: Publish Complete Rolling-Shutter Page

Status: review

## Story

As an engineer,
I want a complete IEC-style rolling-shutter page,
so that Athena proves real package-backed engineering authoring.

## Acceptance Criteria

1. Active `examples/m45/rolling-shutter` compiles one complete rolling-shutter page containing supply, breaker, contactors, overload, motor, PLC, sensors, terminals, coils, auxiliary contacts, and indicator lamps.
2. Page publishes at least ten `PACKAGE_READY` package-backed occurrences with admitted native Symbol/Element geometry, visible semantic ports, relationship routes, compact labels, stable occurrence identity, and inspectable source/package trace.
3. Page uses the owned spatial chain only:
   `SemanticPlacementIntent -> LogicalLayoutCoordinate -> compiler-derived PhysicalGeometryCoordinate`.
   Sheet source persists no raw canvas X/Y, and Snap settings do not reinterpret persisted logical placement.
4. Published document follows the canonical visual grammar:
   - A-H vertical ruler and 1-17 horizontal ruler;
   - narrow rulers flush to top and left edges, including an aligned corner;
   - one-pixel square page frame;
   - clean white interior with no default visible grid, micro-grid, persistent port rings, or bottom title/table block;
   - thin technical IEC linework, tiny connection markers, compact labels, and generous whitespace;
   - visually approaches `draft/screenshort/equipement_d'un_volet_roulant.png`.
5. Invalid or non-ready package items cannot appear as fallback boxes, points, previous-version substitutions, or raw external assets. Existing accepted scene remains the failure boundary.
6. Evidence and focused tests prove the active page contract. Full edit/reopen/export stability and pinned screenshot comparison remain Story 5.4 responsibility.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 2, 5)
  - [x] Inspect current M45 Package Index, `athena-lock-v3`, `FunctionRepresentationBinding`, `FunctionPartBinding`, package admission, and scene compilation seams.
  - [x] Extend only current native M45 compiler/model paths required for READY package-backed occurrences to contribute admitted geometry, Element/Symbol representation references, ports, labels, and provenance/usage trace to Canonical Scene.
  - [x] Do not introduce a second package graph, resolver, scene model, package descriptor, fallback asset path, or renderer-owned authority.

- [x] Task 2 (AC: 1, 2, 3)
  - [x] Complete `examples/m45/rolling-shutter` with direct local package definitions already admitted through its three native package roots: `com.athena.iec`, `com.vendor.siemens`, and `com.community.elements`.
  - [x] Author complete Engineering Reality and source-owned bindings for required page subjects. Preserve the authority chain:
    `Entity -> Function -> FunctionRepresentationBinding -> Element -> Symbol`.
  - [x] Add source-owned `FunctionPartBinding` only where physical/procurement implementation facts are required; never place Part identity in Element, Symbol, Variant, Macro, SVG, or scene occurrence authority.
  - [x] Author Sheet logical coordinates and presentation intent only. Use semantic placement where location has engineering meaning. Never persist raw canvas pixels.
  - [x] Materialize current `athena-lock-v3` from compiler authority. Do not hand-edit a lock or accept lock drift.

- [x] Task 3 (AC: 2, 4, 5)
  - [x] Extend Canonical Scene and Theia/Konva adapter only where current renderer-neutral scene contract needs package-backed asset data. Keep frontend a typed read/intent client; never assemble source or scene authority in TypeScript.
  - [x] Ensure browser paint uses admitted package-local SVG geometry only. `ScenePort.hitRadius` remains invisible interaction geometry; routes, markers, labels, and frame retain constant screen-space visual weight under zoom.
  - [x] Preserve the repository Engineering Document Visual Golden Rule exactly. Construction overlay remains opt-in and absent by default.

- [x] Task 4 (AC: 1-5)
  - [x] Add focused Kotlin tests around active M45 repository compilation and scene publication: required subjects, at least ten package-backed occurrences, admitted geometry/representation refs, ports, routes, labels, ruler/frame geometry, blank drawing surface, stable traces, and fail-closed non-ready package rejection.
  - [x] Add or update LSP integration coverage for active M45 repository publication. Confirm typed server publication only; no frontend direct source mutation.
  - [x] Update frontend contract tests only when scene payload changes. Keep assertions implementation-neutral and renderer-disposable.

- [x] Task 5 (AC: 4, 6)
  - [x] Rebuild affected kernel, LSP, and frontend outputs before product evidence. Run Gradle tasks strictly sequentially on Windows.
  - [x] Capture desktop and narrow-editor product screenshots under `_bmad-output/implementation-artifacts/m45/screenshots/`; record relevant command/test output under M45 artifacts.
  - [x] Run `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1`.

## Dev Notes

### Architecture and Authority

- M45 is native package authoring only. `.elmt`, HTML, XML, and all `reference/` content are authoring reference material, never parser, converter, compiler, runtime, package, or test-fixture inputs.
- Athena source owns Entity, Function, Engineering Port, Relationship, `FunctionPartBinding`, and `FunctionRepresentationBinding`. Packages own reusable Symbol, Element, Part, Macro, Variant, and Placeholder facts. SVG owns package-local geometry only.
- Only `PACKAGE_READY` package/items may enter runtime snapshot, lock, binding, Source Revision, or Canonical Scene. `PACKAGE_INCOMPLETE` exists only through `AdmissionReport`; `PACKAGE_INVALID` has no preview/publication path.
- Do not bypass current direct-child package catalog or `ResolvedPackageGraph`. Packages remain immediate `packages/<packageId>/package.yaml` children; no `registry`, `representation`, or `engineering` intermediate roots.
- Lock authority is canonical `athena-lock-v3` only. Delete/refactor stale V2, pseudo-item, fallback scan, compatibility adapter, dual-write, or previous-version path encountered in the active M45 surface. Do not retain them.
- `FunctionPartBinding` identity is `(functionId, implementationRole)`, with exactly one active admitted Part per role. It is projection-independent and must not alter occurrence identity.
- A Symbol/Element port anchor is a package-side compatibility/geometry mapping. Engineering Port identity, semantic direction, domain, accepted flow, and relationships remain Athena source facts.
- Macro is representation reuse, not Pattern. Variant preserves normalized Element interface fingerprint. Placeholder substitutes only declared non-interface representation fields. None may create Functions, Relationships, rules, capabilities, or Part selections.
- All mutations remain typed Source Revision CAS transactions. Stage declared source/Sheet/binding/lock files, admit and compile before durable commit, then journal/snapshot/scene publication. Validation failure commits nothing.

### Three-Tier Spatial Rule

- Semantic location: `SemanticPlacementIntent` owns engineering region/role meaning.
- Logical location: `LogicalLayoutCoordinate` in Sheet companion owns stable Sheet/Region/Cell/SubGrid placement.
- Physical geometry: compiler derives x/y/rotation/bounds for Canonical Scene and paint.
- Snap is independent interaction policy. It cannot rewrite coordinate meaning.

### Current Code and Example Intelligence

- Existing M45 package discovery/index/lock path:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/LocalPackageManifestParser.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/NativePackageItemIndexCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- Existing representation/source transaction seam:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationAndEngineeringOperationHandler.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/RepresentationBindingCompanion.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/FunctionPartBindingCompanion.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceRevisionService.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- Existing renderer-neutral lowering seam:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- Active M45 source and local packages:
  - `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.athena`
  - `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.athena`
  - `examples/m45/rolling-shutter/packages/`
- Current scene tests still carry M44 fixture assumptions. Replace active-surface expectations with M45 behavior where touched; do not preserve M44 test compatibility.

### Previous Story Intelligence

- Story 5.2 established typed `InsertElementOccurrence`, full Source Revision, admitted Element identity, Sheet id, logical placement, source/Sheet/binding transaction staging, recompile-before-publication, journal-after-durable-commit, and no-partial-write rejection behavior.
- Reuse `EditOperation`, `SourceRevision`, `RepresentationBindingCompanion`, `SheetCompanionEditor`, and current transaction/journal services. Do not create a frontend source writer.
- Story 5.2 package admission uses current `athena-lock-v3` Package Index. SVG assets must satisfy `svg-safe-1`; namespace, `use`, and `text` content in earlier curated resources were rejected.

### Testing and Verification

- Run Gradle commands sequentially only. Never overlap `build`, `test`, `clean`, or module verification commands.
- Start with focused package/compiler/LSP tests. Rebuild affected frontend bundle before product screenshots; stale Theia output is not valid evidence.
- Product proof must open the M45 repository root first. Before checking canvas, verify Explorer shows the repository folder, repository state is `READY`, and LSP root equals active example root.
- Evidence belongs only under `_bmad-output/implementation-artifacts/m45/`. Production `src/main` must contain no M45/Demo/Proof/Sample/V0/V1 classes.
- Before review, run:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
  powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
  ```

### Project Structure Notes

- Evolve existing `kernel/package-model`, `kernel/package-runtime`, `kernel/compiler`, `kernel/interaction-model`, `ide/lsp`, and `ide/theia-frontend` seams. No parallel package/catalog/rendering subsystem.
- Keep example-only page definitions in `examples/m45/rolling-shutter`; no M45 proof code in production source.
- Save screenshots, SVG/PNG exports, lock/lineage snapshots, and operation transcripts only under the M45 implementation-artifacts directory.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 5.3]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md#FR-9 Publish Full Rolling-Shutter Page]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md#Three-Layer Spatial Location]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-7 Geometry And Engineering Semantics Stay Separate]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-12 All Authoring Mutations Recompile Before Publish]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-15 Golden Closure Proves The Full Supply Chain]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-18 Part Binding Is Source-Owned And Projection-Independent]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-19 Pre-1.0 Replacement Is A Hard Cut]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-20 Spatial Location Has Three Authorities]
- [Source: _bmad-output/implementation-artifacts/m45/5-2-insert-and-bind-real-element-occurrences.md]
- [Source: AGENTS.md#Engineering Document Visual Golden Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Ultimate context engine analysis completed - comprehensive developer guide created.
- `:kernel:compiler:test --tests M45NativePackageCatalogTest --tests M45RollingShutterPageTest` passed.
- `:ide:lsp:test --tests AthenaDiagramSceneRequestTest` passed after nested Function ports were indexed for semantic inspection.
- `ide/theia-frontend yarn test`, rebuilt `ide yarn build`, and `ide yarn verify:m45-proof` passed.
- Product evidence: `_bmad-output/implementation-artifacts/m45/m45-product-proof.json`, desktop and narrow screenshots.
- `tools/encoding-audit.ps1` and `tools/source-set-hygiene-audit.ps1` passed.
- Rebuilt `:ide:lsp:installDist` after cleaning stale distribution output; M45 product proof then opened the requested workspace and reached `READY`.

### Completion Notes List

- Story context created from M45 sprint status, final PRD/addendum, final architecture spine and reviews, implementation-readiness report, Story 5.2 record, active M45 example, current compiler/LSP seams, and recent git context.
- No current external technical version research was required: M45 implementation must use repository-pinned Kotlin, LSP4J, TypeScript, Theia, Konva, React, and `svg-safe-1` contracts rather than introduce a new dependency.
- Active M45 rolling-shutter source now publishes 13 package-backed Function occurrences and 10 routed relationships from `PACKAGE_READY` native package items.
- Element lock entries inherit their composed Symbol's package-local admitted SVG resource. LSP resolution fails closed when source binding, locked Element, or resource is missing.
- Frontend paints only admitted asset bundle geometry. Product proof opens M45 repository root, verifies `READY`, full-height center canvas, source editor, 17x16 frame, 13 occurrences, 10 routes, and composite canvas pixels at desktop and narrow sizes.
- Nested Function ports now resolve source ranges in semantic inspection; no product-proof LSP exception remains for indicator-lamp ports.
- Konva now contains admitted SVG assets inside occurrence bounds without aspect-ratio distortion. M45 visual proof shows short device labels and A-H/1-17 ruler chrome.

### File List

- `_bmad-output/implementation-artifacts/m45/5-3-publish-complete-rolling-shutter-page.md`
- `_bmad-output/implementation-artifacts/m45/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m45/m45-product-proof.json`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m45/screenshots/m45-rolling-shutter-narrow-720x900.png`
- `examples/m45/rolling-shutter/athena.lock`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.athena`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.athena`
- `examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.binding.athena`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/NativePackageItemIndexCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationAssetPackageCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M45RollingShutterPageTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/M45NativePackageCatalogTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/theia-frontend/src/browser/athena-workbench-automation.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/editor-rulers.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-product/scripts/athena-m45-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m45-product-proof.js`
- `ide/theia-product/package.json`
- `ide/package.json`

### Change Log

- 2026-08-09: Created comprehensive BMad developer story context; status ready-for-dev.
- 2026-08-09: Published native package-backed M45 rolling-shutter page, added compiler/LSP/frontend evidence, captured rebuilt desktop and narrow product proof, and marked story review.
- 2026-08-09: Replaced retired M43 product proof entrypoint with M45-only workspace proof; corrected SVG aspect-ratio paint, idempotent workspace activation, compact visual labels, and A-H ruler presentation.
