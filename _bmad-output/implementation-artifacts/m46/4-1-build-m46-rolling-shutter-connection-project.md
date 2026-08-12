---
story: 4.1
epic: 4
title: Build M46 Rolling-Shutter Connection Project
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 4.1: Build M46 Rolling-Shutter Connection Project

Status: done

## Story

As an engineer,
I want one complete realistic connection example,
so that semantic and visual behavior can be judged together.

## Acceptance Criteria

1. Given independent `examples/m46/rolling-shutter`, when repository compiles, then direct local packages,
   Lock V3, governed source, explicit bindings, same-basename Sheet, and style publish READY without reading any
   prior milestone example; source proves all six M46 Connection Kinds, one first-class multi-endpoint Net,
   potentials/signals, typed physical requirements, and placed plus unplaced connectivity.
2. Given compiled page, when Canonical Scene renders, then supply, breaker, contactors, overload, motor, PLC,
   sensors, terminals, coils, auxiliary contacts, and lamps use admitted package-backed SVG geometry and semantic
   Port anchors; visible connections trace through Connection IR and include one joined junction, one unconnected
   crossing, one shared trunk, one interruption, and one explicit compact annotation.
3. Given compiler and LSP golden tests, when the project is materialized and opened as repository root, then
   repository contract, Lock V3, package admission, engineering compilation, Connection IR, projection, Route Plan,
   Scene, and Connection Read Model are deterministic and READY; no missing package, source, anchor, route, or
   trace diagnostic remains.
4. Given visual contract inspection, when scene/page facts are checked, then page follows the canonical engineering
   document grammar: one-pixel square frame, narrow flush top/left rulers, clean white interior, hidden construction
   grid, thin orthogonal linework, tiny connection points/junctions, compact regular labels, disciplined spacing,
   no bottom table, no default Port-name/debug/source/internal-id flood, and no clipped or twisted narrow layout.

## Tasks / Subtasks

- [x] Build independent direct-package M46 repository (AC: 1, 3)
  - [x] Create `examples/m46/rolling-shutter/` with own manifest, compiler-owned Lock V3, source root, same-basename companions, and direct local package modules.
  - [ ] Copy admitted professional SVG assets from `reference/elements` and `reference/elements_contrib` into M46 package modules with separate geometry/anchor and Engineering Port facts.
  - [x] Materialize Lock V3 through repository resolver authority; no fallback or retired-shape compatibility.
  - [x] Keep proof-only helpers in tests/examples/artifacts; no milestone-named production classes.
- [x] Author one complete semantic connection system (AC: 1-3)
  - [x] Replace retired `power`/`control` relationship routes with direct typed `connect <kind>` and first-class `net` syntax.
  - [x] Prove all six connection kinds, typed specifications, endpoint roles, precedence, and placed/unplaced facts.
  - [x] Author multi-endpoint `Control24V` Net with explicit signal and physical requirements.
  - [x] Preserve human-first syntax; source contains no topology, route, Scene, viewport, renderer, digest, or other IR fields.
- [ ] Bind package-backed IEC occurrences and logical Sheet placement (AC: 1, 2, 4)
  - [ ] Bind required occurrences to admitted local package Elements/Variants with semantic-Port-to-SVG-anchor mappings.
  - [ ] Author logical Sheet placement/style; compiler plans topology and compact annotation facts.
  - [x] Keep route/annotation intent minimal and free of screenshot pixel geometry.
  - [ ] Verify every visible route traces to accepted Connection/Net IR and every occurrence resolves admitted geometry.
- [ ] Replace toy geometry with professional static SVG package assets (AC: 2, 4)
  - [ ] Add an asset-admission test for inert SVG text and internal reusable geometry; reject external links, scripts, foreign markup, and event attributes.
  - [ ] Copy selected professional source SVGs into the local M46 packages, normalizing only static document metadata required by `svg-safe-1`; preserve asset provenance and never depend on `reference/` at runtime.
  - [ ] Represent breaker, contactor coil, NO/NC contact, transformer, terminal, lamp, pushbutton, and three-phase motor as package symbols/elements with exact semantic Port anchors.
  - [ ] Re-author the rolling-shutter source, bindings, and Sheet as a complete left-power/center-control-supply/right-ladder page matching the canonical engineering-document grammar.
- [ ] Add compiler and LSP golden acceptance tests (AC: 1-4)
  - [x] Compiler test covers independent package resolution, six kinds, Net identity, specifications, topology, placement, and deterministic identities/digests.
  - [x] LSP test opens M46 repository root and asserts READY publication, Connection Read Model, zero blocking diagnostics, and no prior-example reads.
  - [ ] Assertions cover junction/crossing, shared segment, interruption, annotation, hidden debug labels, frame/rulers, blank interior, and no table/grid.
  - [ ] Focused and full compiler/LSP regressions, encoding audit, source-set hygiene audit, and `git diff --check` pass sequentially.

## Dev Notes

### Authority Chain

```text
M46 .athena Engineering Connection/Net truth
  -> canonical Connection IR
  -> ConnectionProjection
  -> deterministic ConnectionRoutePlan
  -> SceneConnection
  -> SVG/Konva paint
```

- Local packages own reusable Symbol/Element/Part/Variant facts and SVG geometry/anchors. Project source owns which
  engineering objects exist and how Ports connect. Sheet owns durable logical placement/route intent. Style owns paint.
- Package geometry cannot author connection kind, Net membership, potential/signal, physical requirements, or source
  Endpoint roles. Graphic proximity/crossing cannot create connectivity.
- M46 project must be independently copyable/openable. Tests may inspect its own files only and must never compile or
  copy `examples/m45` on the fly. Assets copied during implementation become normal M46-owned example inputs.
- `.elmt`, HTML, XML, and reference directories remain absent from runtime, compiler, test fixture, and example contracts.

### Required Semantic And Visual Coverage

- Six kinds: `CONDUCTOR`, `WIRE`, `CABLE_CORE`, `JUMPER`, `BUSBAR`, `SIGNAL`.
- Page content: supply, breaker, contactor main/coil/auxiliary, overload, motor, PLC, two sensors, terminal(s), lamps.
- Topology: first-class multi-endpoint Net, junction, unconnected crossing, shared trunk, interruption, explicit compact
  annotation, and placed/unplaced connectivity.
- Golden grammar comes from `draft/screenshort/equipement_d'un_volet_roulant.png` and `AGENTS.md`. Exact circuit may
  differ, but professional class must match: one-pixel frame, narrow rulers, white interior, thin black orthogonal routes,
  tiny markers, compact labels, generous whitespace, no default grid/table/debug labels.

### Existing Contracts To Reuse

- Reuse current repository manifest/Lock V3 pipeline and M45 direct-package module shape, but create new M46 files and ids.
- Reuse current `EngineeringConnection`/`EngineeringNet` language, `ConnectionIrCompiler`,
  `ConnectionSpecificationResolver`, `ConnectionProjection`, `ConnectionRoutePlanner`, topology/annotation planners,
  `AthenaDiagramSceneCompiler`, package catalog/admission, diagram publication, and Connection Read Model.
- Story 3.4 separated `ScenePort.anchorId` from `ScenePort.semanticPortId`, added `SceneConnection.projectionId`, and
  closed Connect/Reconnect/route transactions. M46 example must use semantic Port ids for truth and anchor ids for paint.
- Do not revive M45 generic `power`/`control` relationship routes, `ProjectionConnection`, `SpatialRoute`, `SceneRoute`,
  generic reconnect, milestone fixture fallbacks, or alternate renderer path.

### Test Strategy

- RED: real M46 repository path missing; compiler/LSP golden test fails.
- GREEN: create smallest complete independent repository and current syntax/package bindings until repository/IR/Scene
  publication becomes READY and all semantic/visual assertions pass.
- REFACTOR: remove duplicated/stale fixture assumptions, keep test support cohesive, and keep production source untouched
  unless a real current-contract defect blocks the example.

Run Gradle strictly sequentially:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 4 / Story 4.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-16, NFR-1/2/5/6/7]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/addendum.md`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-1 through AD-19]
- [Source: `_bmad-output/planning-artifacts/m46/implementation-plan.md`, Task 12]
- [Source: `_bmad-output/implementation-artifacts/m46/3-4-connect-reconnect-and-adjust-routes-through-transactions.md`]
- [Source: `_bmad-output/implementation-artifacts/m45/M45-CLOSURE.md`]
- [Source: `AGENTS.md`, Pre-1.0, Human-First Language, Theia Workspace Proof, E2E Proof, and Visual Golden Rules]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Story context created from complete M46 sprint, Epic 4, PRD, addendum, architecture, implementation readiness/plan,
  Story 3.4 intelligence, M45 closure, active example structure, repository rules, and current git context.

### Completion Notes List

- M46 repository independently compiles and publishes package-backed Connection IR, Route Plan, Scene, and Connection Read Model.
- Compact Sheet annotation placement now uses logical Sheet units; fixed pixel-sized bounds no longer make authored Sheet companions unavailable.
- Style edits preserve explicit `annotation:` intents.
- Connection transaction regression fixture now uses independent M46 source and current direct connection syntax; no M45 example dependency remains.
- Verification passed: compiler full test, LSP full test, language companion tests, encoding audit, source-set hygiene audit, and `git diff --check`.
- Fixed Style Companion editor routing: `.sheet.style.athena` stays an Athena LSP document but no longer receives the incompatible engineering Monarch/Tree-sitter grammar; valid style source therefore has dedicated style diagnostics only.
- Binding Companions now publish dedicated parse diagnostics instead of entering M42 engineering compilation; Style and Binding Companions have separate Monaco syntax grammars while Tree-sitter remains main engineering-source only.

### File List

- `_bmad-output/implementation-artifacts/m46/4-1-build-m46-rolling-shutter-connection-project.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlanner.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionAnnotationPlannerTest.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetStyleCompanionEditor.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/ConnectionOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M46RollingShutterConnectionProjectTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServerTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-language-definition.ts`
- `ide/theia-frontend/scripts/athena-language-highlighting-definition.test.mjs`

### Change Log

- 2026-08-11: Created Story 4.1 through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-11: Implemented M46 connection project, compact logical annotation sizing, style annotation preservation, and current-contract transaction coverage; status `review`.
- 2026-08-11: Visual acceptance failed: the page still used toy geometry. Returned to `in-progress` for professional static SVG package assets and rebuilt product evidence.
- 2026-08-11: Routed Sheet Style Companions away from main engineering Tree-sitter/Monarch while retaining dedicated JVM LSP diagnostics; rebuilt frontend and LSP distribution.
- 2026-08-11: Added companion-aware Monaco grammars and stopped Binding Companions from invoking the M42 engineering compiler, eliminating the false representation-source contract diagnostic.
