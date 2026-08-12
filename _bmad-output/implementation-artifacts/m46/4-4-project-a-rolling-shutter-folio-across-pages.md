---
story: 4.4
epic: 4
title: Project A Rolling-Shutter Folio Across Pages
status: done
baseline_commit: 45295edcfa7ee88d7e5630958f58231549bc98f2
created: 2026-08-11
---

# Story 4.4: Project A Rolling-Shutter Folio Across Pages

Status: done

## Story

As an engineer,
I want one engineering Folio with distinct power and CPU/control pages,
so that each page remains readable while all connectivity retains one semantic identity.

## Acceptance Criteria

1. Given one M46 project with multiple resolved direct packages, when the compiler loads its Folio, then one
   same-root Folio Companion orders independent Power and CPU/Control Page Companions; every Page owns format,
   frame, snap, logical placements, route constraints, and style companion. The retired single
   `rolling-shutter.sheet.athena` discovery path is absent without a fallback.
2. Given a Page projects occurrences whose Function Representation Bindings resolve from different admitted
   IEC, vendor, and community packages, when the Folio compiles, then assets resolve solely through the project
   manifest and Lock V3. Folio/Page declarations neither import packages nor copy Symbol/Element/Part facts.
3. Given a Connection or Net has occurrences on Power and CPU/Control pages, when both projections compile, then
   their Connection Projections reference the same accepted Connection IR identity. Cross-page continuation is
   explicit interruption/reference data; graphic continuity never creates connection truth and M46 adds no
   Smart Connect authoring UX.
4. Given Theia opens the M46 Folio, when an engineer switches page, then each page is independently selectable,
   renders only its page Scene, and follows the IEC visual golden rule: square one-pixel frame, flush narrow
   rulers, white interior, no grid/table/debug labels, thin black orthogonal linework, compact labels, and
   consistent package-asset scale. Canvas selection remains in canvas semantic selection and does not open source.
5. Given malformed Folio/Page names, missing Page Companion, duplicate page ids/order, unresolved occurrence, or
   a Page Companion that tries to own a package, when compile/LSP runs, then publication fails closed with an
   exact human correction; previous accepted Folio publication remains STALE where applicable.

## Tasks / Subtasks

- [x] Replace one-sheet companion authority with Folio and Page contracts (AC: 1, 5)
  - [x] RED: add language tests for valid ordered Folio/Pages plus every invalid discovery/name/duplicate case.
  - [x] Add typed Folio/Page intent and deterministic same-root discovery. Use `*.folio.athena` for the Folio
    and `*.<page>.sheet.athena` plus `*.<page>.sheet.style.athena` for Pages.
  - [x] Delete `SheetCompanionLocator` and `*.sheet.athena` same-basename fallback. No deprecated parser,
    location type, diagnostic code, or test remains.
 - [x] Compile independent pages from one engineering source (AC: 1, 2, 3, 5)
  - [x] RED: compiler tests prove two ordered Projection Sheets, page-local placement/route constraints, one
    shared Connection IR identity, and fail-closed missing/invalid pages.
  - [x] Update compiler load/mapping/spatial orchestration to apply each Page Companion only to its named Sheet.
  - [x] Keep packages at manifest/Lock/FunctionRepresentationBinding authority. Reject package/import syntax in
    Folio/Page companions rather than silently treating it as a page concern.
  - [x] Make Scene traces identify the exact originating Page Companion, not the retired generic Sheet path.
 - [x] Expose Folio page navigation in Theia (AC: 4)
  - [x] RED: frontend contract test verifies a compact labelled page selector reads published page order and
    selects the corresponding Scene without opening source.
  - [x] Extend the companion opener and Presentation widget so Folio/Page files open the engineering document
    designer and page switching uses the existing active Projection Sheet mechanism.
  - [x] Keep page UI sparse: one selector, no debug controls, no package/import controls, no AST/source labels.
 - [x] Materialize the professional M46 Folio and evidence (AC: 1-4)
  - [x] Replace the retired single M46 Sheet with Power and CPU/Control Page Companions plus one Folio Companion.
  - [x] Place the power page as an intentional IEC power/control sheet and the CPU/control page after
    `machine_no_000_-_op,_cpu.png`: three top buses, two consistently scaled device blocks, compact drops,
    disciplined white space, and page-local routes.
  - [x] Rebuild kernel, LSP, frontend, and IDE sequentially; open the M46 workspace root; capture desktop and
    narrow screenshots under `_bmad-output/implementation-artifacts/m46/screenshots/`.
  - [x] Run focused/full language/compiler/LSP/frontend tests, encoding audit, source-set hygiene audit, and
    `git diff --check` sequentially. Record exact evidence before marking any task complete.

## Dev Notes

### Authority

```text
Project manifest + Lock V3 -> admitted packages -> Function Representation Binding
Engineering source -> Connection IR -> Folio -> Page Projection -> Route Plan -> Scene page -> Theia/SVG paint
```

- A package provides reusable Symbol/Element/Part geometry and package-local anchor references only. It does
  not own a Folio, Page, Connection, Net, page order, or project engineering relationship.
- Folio groups and orders pages. Page owns presentation intent only. A Page selects already-authored projection
  occurrences; it never repeats engineering source or imports a package.
- Use no raw pixel coordinates, Konva fields, SVG paths, renderer state, topology ids, or Connection IR fields
  in human-authored Folio/Page syntax.
- Do not introduce XML, HTML, `.elmt`, external runtime assets, compatibility aliases, duplicate runtime
  renderers, or package-specific compiler branches.

### Current Code And Reuse

- `AuthoredProjectionViewCompiler` already builds ordered multiple `ProjectionSheet`s and connection
  projections. Reuse it; do not make a Folio-specific second projection model.
- `AthenaExecutionContext` already holds an active projection sheet id. Reuse the governed selection mechanism;
  do not store an alternate frontend-only active page.
- Replace `SheetCompanionLanguage.kt` single-companion discovery/parser and
  `AthenaCompilerCompilationSupport` single companion load/mapping. Preserve the small line-oriented parser
  style and existing plain-language diagnostics.
- Extend `SheetCompanionProjectionPlacementMapper` rather than copying placement/route mapping.
- `AthenaSheetCompanionOpener` currently handles `.sheet.athena`; update it to Folio/Page paths. Preserve
  normal engineering-source opening and Workspace-first proof contract.
- Presentation assets are package-backed. Normalize bad assets before renderer fitting; never compensate for
  bad SVG geometry with page-specific renderer scale hacks.

### Visual Contract

- `draft/screenshort/machine_no_000_-_op,_cpu.png` is the CPU/control page composition reference.
- Power page follows `draft/screenshort/equipement_d'un_volet_roulant.png` and AGENTS.md.
- Default page interior has no grid, title table, persistent port rings, thick routes, arrows, blue source text,
  AST links, internal ids, or unselected port labels.
- Scene/stage zoom must preserve screen-space line/marker/label weight.

### Required Verification

Run Gradle commands strictly one at a time:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:language:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
Set-Location ide\theia-frontend
yarn test
yarn build
Set-Location ..\..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
git diff --check
```

## References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-9, FR-10, FR-16
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-8 through AD-20
- `_bmad-output/planning-artifacts/m46/epics.md`, Epic 4 / Story 4.4
- `_bmad-output/planning-artifacts/m46/implementation-plan.md`, Task 12.5
- `_bmad-output/implementation-artifacts/m46/4-1-build-m46-rolling-shutter-connection-project.md`
- `AGENTS.md`, Human-First, Pre-1.0, E2E, Workspace, and Visual Golden rules

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- Story created after rejecting the incorrect single-companion multi-page design. Existing code proves a
  single `SheetCompanionLocator`/parser and a compiler load path that applies one companion, while the
  projection/runtime layers already support multiple ordered Sheets and an active sheet id.
- RED: `SheetCompanionLanguageTest` failed because Folio parser/discovery contracts did not exist.
- GREEN: added typed Folio/Page contracts, ordered `folio { page ... }` parsing, same-root Folio discovery,
  and deterministic independent Page Companion paths. Focused language test passed.

### Completion Notes List

- Folio/Page contracts, page-scoped scene publication, independent URI-keyed Theia widgets, and compact page navigation complete.
- Page request identity now flows through `athena/diagramScene` and `athena/presentationEditContext`; no frontend global active-page authority.
- M46 LSP proof confirms Power and CPU/Control pages publish READY with shared connection IR/topology evidence.
- Verification: `:kernel:language:test`, `:kernel:compiler:test`, `:ide:lsp:test`, frontend `yarn test` (88/88), encoding audit, source-set hygiene audit, `git diff --check`.
- Clean-start log: `.athena/logs/clean-start.log` reports workspace-root activation, `ATHENA_DESKTOP_READY`, LSP repository root for `examples/m46/rolling-shutter`, and zero diagnostics. Existing restored Theia layout emitted one stale preview-widget warning; no source/LSP failure.

### File List

- `_bmad-output/implementation-artifacts/m46/4-4-project-a-rolling-shutter-folio-across-pages.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/M46RollingShutterConnectionProjectTest.kt`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/athena-sheet-companion-opener.ts`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`

### Change Log

- 2026-08-11: Created through BMad story workflow from the approved Folio correction; status `ready-for-dev`.
- 2026-08-11: Started RED/GREEN Folio language contract; status `in-progress`.
- 2026-08-12: Completed Folio/Page compiler and Theia independent page editor flow; status `review`.
