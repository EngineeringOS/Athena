---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.2: Execute Presentation Operations

Status: done

## Story

As an engineering author,
I want Move, Align, Distribute, Snap, and Set Style to round-trip through validated source,
so that presentation edits survive recompilation without touching Engineering Reality.

## Acceptance Criteria

1. Given one READY Canonical Scene, a current full Source Revision, a selected occurrence or occurrence
   group, and valid Source Trace context, when Move, Align, Distribute, Snap, or Set Style is submitted,
   then the existing Edit Operation service derives Presentation authority and the exact same-basename
   Sheet or Style Companion writable path, validates current scene/targets/trace/revision, and executes the
   operation through the Story 3.1 transaction engine; no second endpoint, transaction path, source writer,
   journal, or renderer authority exists.
2. Given Move or Snap targets one visible occurrence, when the server computes its placement, then it writes
   one natural Sheet statement using the existing `"Occurrence" at A1 micro(x,y) [lock]` syntax, validates
   address and micro coordinates against the published grid, preserves stable occurrence identity, and
   applies lock semantics exactly: Move `PRESERVE` retains current lock state, `LOCK` sets it, `UNLOCK`
   removes it, and Snap preserves current lock state.
3. Given two or more selected occurrences, when Align is accepted, then the server uses accepted scene
   occurrence bounds and placement anchors to align the requested left/center/right or top/center/bottom
   reference, converts every resulting anchor to the nearest valid Sheet micro-grid coordinate, preserves
   each occurrence's orthogonal coordinate and lock state, writes all changed placement statements in one
   sorted patch, and recompilation reproduces the aligned result without frontend coordinate authority.
4. Given three or more selected occurrences, when Distribute is accepted, then the server deterministically
   orders occurrences by accepted scene center and occurrence identity, preserves first and last centers,
   places interior centers at equal horizontal or vertical intervals, quantizes anchors to valid Sheet
   micro-grid coordinates, preserves orthogonal coordinates and lock state, and publishes one atomic patch;
   ties, rounding, and input list order produce the same result on repeated execution.
5. Given a pointer drag begins on an occurrence, when the pointer moves, then Konva shows one disposable
   whole-occurrence preview at grid-snapped coordinates without moving accepted scene data, routes, source,
   or Operation Journal; on release it submits exactly one typed Move operation and restores accepted paint
   until READY publication arrives. Cancel, Escape, repository change, rejected operation, or unavailable
   publication discards preview with zero source mutation. Ctrl/Cmd selection maintains a stable multi-
   occurrence set for Align/Distribute while plain click keeps single selection.
6. Given any accepted Presentation operation, when transaction evidence is inspected, then only
   `*.sheet.athena` changes for Move/Align/Distribute/Snap or only same-basename
   `*.sheet.style.athena` changes for Set Style; one journal entry records exact forward/inverse patch,
   previous/resulting revision, stable targets/trace, and publication correlation id, while Entity,
   Function, Part, Symbol binding, Port, Relationship, capability, flow, semantic source, lock/package
   inputs, and source identities remain unchanged.
7. Given stale revision, stale scene, wrong target list, missing/ambiguous Sheet Companion, missing
   occurrence, mismatched primary trace, invalid/out-of-grid anchor, impossible group size, no-op result,
   staged parse/compile failure, or file IO failure, when processing ends, then no source or accepted scene
   changes and no journal entry is appended; diagnostic names exact subject, problem, and correction.
   Engineering compatibility validation remains server-owned and is not duplicated in TypeScript.
8. Given an accepted Move, Align, Distribute, Snap, and existing Set Style operation on the active M44
   example, when the project is refreshed and reopened, then authored placements/styles reproduce exactly,
   scene and occurrence trace remain selectable, thin-line/port-marker Golden Rule rendering remains intact,
   and operation transcripts plus rebuilt desktop/narrow screenshots are stored only under M44 artifacts.

## Tasks / Subtasks

- [x] Add failing Sheet placement editor tests (AC: 2, 3, 4, 7)
  - [x] Add one cohesive `SheetCompanionEditor` beside existing Sheet parser/style editor; do not assemble
        source in LSP service or frontend.
  - [x] Prove deterministic create/update of placement statements, natural syntax, exact lock transitions,
        preserved unrelated statements/order, parse-after-write, no-op detection, and out-of-grid rejection.
  - [x] Keep the parser grammar unchanged unless a test proves current approved syntax cannot express an
        operation; do not add geometry, route, or paint mechanics to Athena source.
- [x] Implement deterministic server placement planning (AC: 2, 3, 4, 6, 7)
  - [x] Add failing pure tests for Move/Snap anchor validation, six alignment axes, two distribution axes,
        input-order independence, tie breaking, rounding, page/grid bounds, group cardinality, and lock
        preservation.
  - [x] Resolve occurrences only by stable `occurrenceId` from the accepted Canonical Scene; never by label,
        Entity name guess, Konva node, or source-text search.
  - [x] Compute align/distribute from accepted bounds plus placement anchors on server, then convert through
        one shared scene-point/Sheet-anchor codec matching frontend grid vectors.
- [x] Route all Presentation operations through Story 3.1 transactions (AC: 1, 2, 3, 4, 6, 7)
  - [x] Add a focused `PlacementOperationHandler` that produces one Sheet patch and delegates staging,
        publication, rollback, journal append, replay, and conflict handling to `SourceTransactionEngine`.
  - [x] Replace style-only authoring context with one general read-only Presentation edit context if needed;
        migrate Set Style and delete the narrower endpoint rather than keeping compatibility aliases.
  - [x] Validate target list, primary Source Trace, scene identity, full Source Revision, exact writable path,
        and accepted-result identity invariants before publication.
  - [x] Keep Set Style green on its existing handler and accepted-only journal path.
- [x] Build disposable drag and multi-selection interaction (AC: 5, 7)
  - [x] Add failing frontend/adapter tests for plain selection, Ctrl/Cmd toggle selection, whole-occurrence
        drag preview, grid snapping, Escape/cancel/reject restore, and exactly one release operation.
  - [x] Wire adapter `onMove` to one typed Move operation using server-published edit context; frontend must
        not read/write Sheet source, calculate align/distribute results, or mutate accepted scene objects.
  - [x] Add compact icon controls/menus for six Align modes, horizontal/vertical Distribute, Snap, and lock
        intent using existing Theia/codicon conventions; keep rulers edge-aligned and canvas dominant at
        desktop and narrow sizes.
  - [x] During preview, transform the complete visible occurrence representation and selection overlay;
        keep routes accepted/unchanged until republish and never expose large port rings or helper grids.
- [x] Prove source round-trip and no semantic drift (AC: 1, 6, 7, 8)
  - [x] Add LSP tests for each operation, exact writable file, staged parse/compile, one journal append,
        accepted replay, conflicting id, rollback, failure no-op, and unchanged engineering source/lock.
  - [x] Add generated-contract and architecture tests proving the same closed endpoint/schema remain in use
        and no placement-specific file-write or source-text payload appears in TypeScript.
  - [x] Record before/after/reopen digests, target traces, placement statements, accepted journal entries,
        and rejection evidence for Move, Align, Distribute, Snap, and Set Style under M44 transcripts.
- [x] Rebuild and verify sequentially (AC: all)
  - [x] Run focused language, interaction-model, compiler, LSP, and frontend tests one command at a time.
  - [x] Rebuild complete Theia frontend/product before live E2E; open workspace once before polling scene.
  - [x] Capture desktop and narrow accepted/reopened screenshots and compare linework against
        `draft/screenshort/equipement_d'un_volet_roulant.png`.
  - [x] Run sequential full Gradle `test`, encoding audit, source-set hygiene audit, and `git diff --check`.

## Dev Notes

### Authority And Scope

- This story executes Presentation operations already classified by Story 3.1. Do not change authority
  mapping or create operation-specific protocols. `EditOperationEnvelope`, structured `SourceRevision`,
  `SourcePatchSet`, `SourceTransactionEngine`, and `SessionOperationJournal` remain the common path.
- Sheet placement is authored presentation intent. It may move one occurrence but cannot change semantic
  source, Representation Binding, Part, Port, Relationship, Function, Entity, capability, or flow facts.
- Align/Distribute are server calculations over one accepted scene. Frontend sends operation intent and
  stable occurrence ids only. Persisted output is ordinary placement statements, not an align/distribute
  command object or renderer geometry.
- Route geometry remains derived. A drag preview may leave accepted routes visually in place until the
  operation republishes; frontend must not author route bends or hide the accepted/rejected transition.
- Story 3.3 owns Change Symbol/Reconnect/Bind Part. Story 3.4 broadens invalid engineering-edit proof.
  Story 3.5 owns Undo/Redo execution and durable transcripts. Do not pull those implementations forward.

### Placement Semantics

- `scene.snapGrid.drawingOrigin`, `subdivisions`, `rows`, and `columns` define the only coordinate codec.
  Reuse the same row-label and anchor vectors already proven in Kotlin/frontend; avoid duplicate formulas.
- A placement statement identifies the occurrence by stable `occurrenceId`. Display labels remain paint.
- For alignment, calculate desired edge/center delta from `SceneOccurrence.bounds`; apply delta to
  `placementAnchor`, preserve the orthogonal component, then round to nearest micro-grid position.
- For distribution, sort by axis center then `occurrenceId`, preserve first/last centers, and use equal
  center intervals. Quantize deterministically; if quantization collapses distinct required positions or
  leaves the grid, reject with subject/problem/correction rather than inventing offsets.
- Preserve existing Sheet statement order. When inserting a missing authored placement, place it in stable
  occurrence-id order after title/grid declarations. Patch only changed placement lines and retain exactly
  one terminal newline.
- Move `PRESERVE`, Align, Distribute, and Snap retain each current `lock` token. Move `LOCK` and `UNLOCK`
  explicitly set/remove it. Do not reinterpret lock as engineering immutability.

### Current Code And Required Changes

- `EditOperationService.kt` currently derives correct Sheet writable files but routes only `SetStyle`;
  replace its generic unsupported branch for four placement bodies with one cohesive placement handler.
- `SourceTransactionEngine.kt` already owns staged validation, atomic publish/rollback, accepted scene
  checks, correlation id, journal append, replay, and conflict semantics. Do not duplicate this flow.
- `SheetCompanionLanguage.kt` parses the approved placement form but has no mutation editor.
  `SheetStyleCompanionEditor.kt` is the local deterministic editor pattern to follow.
- `AthenaPresentationWidget` currently submits only Set Style. `KonvaDiagramAdapter` exposes `onMove`, but
  widget does not wire it and current hit-target drag does not preview complete occurrence paint.
- Current selection is singular. Extend view state to a stable occurrence-id set while keeping one active
  selection for trace synchronization and operation Source Trace context.
- Existing `athena/diagramStyleContext` is too narrow for placement operations. Prefer one general
  Presentation edit context and remove the old name in the same story; no compatibility endpoint.
- Keep renderer correction from Story 3.1: route `strokeScaleEnabled: false`, tiny screen-space port markers,
  invisible generous hit geometry, and Golden Rule reference in `AGENTS.md`.

### Expected Product Files

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionEditor.kt` (new)
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionEditorTest.kt` (new)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandler.kt` (new)
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandlerTest.kt` (new)
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-product/scripts/verify-athena-m44-presentation-operations.js` (proof only)

Group cohesive Kotlin value/support types per repository organization rules. Do not create one file per
tiny DTO and do not grow `EditOperationService` into a mixed planning/editor/transaction dump.

### Testing And Regression Guardrails

- Use red-green-refactor per subtask. Tests must first fail because operation support is absent.
- Language tests own deterministic Sheet mutation and syntax preservation. Pure planner tests own geometry
  calculations. LSP tests own authority/revision/trace/path/staging/publication/journal behavior. Frontend
  tests own transient interaction only.
- Live proof must operate on a temporary copy of `examples/m44/rolling-shutter`, never mutate the active
  example, and must verify exactly one Sheet or Style Companion changes per operation.
- Preserve existing Story 2.2 preview/discard/solidify, Story 2.3 ruler geometry, Story 3.1 operation replay,
  M43 scale profile, source-set hygiene, and all generated contract checks.
- Keep pinned Kotlin 2.4.0, LSP4J 0.23.1, TypeScript 5.9.2, Node >=22, Yarn 1.22.22, Theia 1.73.1,
  Konva 10.3.0, and React 18.3.1. No new dependency or second renderer.
- Gradle commands run strictly sequentially.

### Previous Story Intelligence

- Story 3.1 removed Diagram Commands and compatibility surfaces. Do not restore `DiagramCommand`,
  `athena/applyDiagramCommand`, `ConnectPorts`, singular `WorkspaceEdit`, or fake accepted responses.
- Structured Source Revision includes scene/source-root/source/Sheet/Style/lock/package/compiler/schema/
  profile facts. Every successful placement changes Sheet digest and resulting revision.
- Journal append occurs only after successful source and READY scene publication. Exact accepted replay
  returns the original result; rejected operations never enter journal.
- Product proof must prepare workspace/source once, then poll; repeatedly activating the widget prevents
  stable canvas digest evidence.
- Rendering stroke width is screen-space and `ScenePort.hitRadius` is interaction-only. Preserve both tests
  and the permanent visual Golden Rule.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md` (`UJ-2`, `FR-9`, `FR-10`)
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/addendum.md` (`Edit and History Boundary`)
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md` (`AD-6`, `AD-7`, `AD-8`, `AD-11`, `AD-12`)
- Design: `_bmad-output/planning-artifacts/m44/design.md` (`Edit Operations and Source Round-Trip`)
- Epics: `_bmad-output/planning-artifacts/m44/epics.md` (`Epic 3`, `Story 3.2`)
- Previous story: `_bmad-output/implementation-artifacts/m44/3-1-classify-edit-operations-and-journal-transactions.md`
- Visual baseline: `draft/screenshort/equipement_d'un_volet_roulant.png`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted through BMad create-story from M44 PRD/addendum, architecture, design, epics, full sprint
  status, Story 3.1 implementation and visual correction intelligence, current code paths, git history, and
  repository Golden Rules. CodeGraph MCP/CLI timed out; current source was read directly after both attempts.
- 2026-08-07: Dev workflow resumed; story status aligned to in-progress while preserving existing
  `baseline_commit`.
- 2026-08-07: Reported runtime diagnostics were traced through the live product. The stale M43 Sheet
  Companion still used retired `grid`/cell syntax, and the IDE seed resolver admitted Sheet Companions as
  candidates. A focused resolver regression was added and first failed before the source-only filter.
- 2026-08-07: The presentation operation product proof first failed after READY publication because the
  Theia product bundle did not include the changed frontend package. Direct bundle inspection confirmed the
  missing automation seam. Rebuilding `@engineeringood/athena-theia-frontend` before the product bundle
  restored the seam and allowed the same live proof to reach all accepted/rejected operation evidence.
- 2026-08-07: Narrow live evidence exposed an operation diagnostic consuming the editor height. A failing
  CSS contract was added first, then the toolbar was kept single-row with a bounded one-line diagnostic.
  Rebuilt Electron evidence confirms rulers remain edge-aligned and the canvas remains dominant.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- 2026-08-07: Added `SheetCompanionEditor` with deterministic natural placement writes, lock transition
  handling, grid validation, insertion ordering, parse-after-write, and explicit no-op result. Focused and
  full `:kernel:language:test` passed.
- 2026-08-07: Added server-owned `PlacementOperationPlanner`; it resolves stable occurrence ids from the
  accepted Canonical Scene, validates Sheet/grid/bounds, and deterministically plans Move, Snap, Align, and
  Distribute anchors with no frontend geometry authority. Focused and full `:ide:lsp:test` passed.
- 2026-08-07: Migrated the retained M43 rolling-shutter Sheet Companion to canonical `frame` + `snap`
  syntax, made IDE session seeding exclude `*.sheet.athena` and `*.sheet.style.athena`, and switched the
  desktop smoke session to the active M44 example's real nested source path. Resolver regression, rebuilt
  LSP distribution, rebuilt Theia product, and desktop smoke all passed; a fresh M44 Electron workspace is
  running.
- 2026-08-07: Routed Move, Snap, Align, and Distribute through the common typed Presentation transaction
  path with source-only Sheet patches, accepted-only journal evidence, stable source traces, replay-safe
  revision checks, and deterministic server placement planning. Added transient Konva drag/multi-select
  intent handling and compact operation controls with no frontend source or scene mutation authority.
- 2026-08-07: Final product proof accepted Move, Snap, Align, Distribute, and Set Style on a temporary M44
  repository copy; rejected an out-of-area Move without mutation; reopened the repository at the same READY
  scene. Only the Sheet Companion changed for placement operations, while engineering source and lock
  digests stayed unchanged. Final checks passed: focused JVM suites, full Gradle `test`, frontend suite
  (45 tests), style proof, presentation proof, encoding audit, source-set hygiene audit, and
  `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m44/3-2-execute-presentation-operations.md`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionEditor.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionEditorTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationPlanner.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationPlannerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolverTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/EditOperationService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngine.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/PlacementOperationHandlerTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/SourceTransactionEngineTest.kt`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-m44-presentation-operations-main.js`
- `ide/theia-product/scripts/verify-athena-m44-presentation-operations.js`
- `_bmad-output/implementation-artifacts/m44/operation-transcripts/3-2-presentation-operations-product-proof.json`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-accepted.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-desktop-after-move.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-desktop-after-snap.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-desktop-after-align.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-desktop-after-distribute.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-rejected.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-presentation-narrow-after.png`
- `ide/theia-product/scripts/verify-athena-start.js`
- `examples/m43/rolling-shutter/src/com/engineeringood/m43/rollingshutter/rolling-shutter.sheet.athena`

### Change Log

- 2026-08-07: Story created via BMad create-story flow for validated Presentation operation round-trip.
- 2026-08-07: Prevented retired Sheet Companions from becoming the IDE engineering seed and rebuilt live
  M44 desktop proof target.
- 2026-08-07: Completed typed presentation operations, transient edit interaction, source round-trip,
  transaction evidence, and final desktop/narrow product proof.
