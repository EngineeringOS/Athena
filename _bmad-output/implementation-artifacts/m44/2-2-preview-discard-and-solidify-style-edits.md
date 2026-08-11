---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.2: Preview, Discard, And Solidify Style Edits

Status: done

## Story

As a design reviewer,
I want to preview route and symbol style edits before writing them,
so that presentation can be adjusted deliberately.

## Acceptance Criteria

1. Given an accepted Canonical Scene and a selected style target, when the user previews any supported
   line width, dash, color, label typography, route marker, or port display change, then only a frontend
   session overlay changes; no project file, Source Revision, accepted scene, source trace, placement, or
   engineering fact changes.
2. Given one or more session style previews exist, when the user discards them, changes repository,
   receives a different accepted scene revision, reopens the document, or receives an unavailable
   publication, then the overlays are removed and the canvas redraws the exact accepted publication
   without writing files.
3. Given a valid preview and current full Source Revision, when the user solidifies it, then the frontend
   submits one typed `SetStyle` `PresentationEditOperation` carrying target identity and Source Trace;
   the server creates or minimally patches only the exact same-basename `*.sheet.style.athena`, validates
   staged style parse and scene compilation, and atomically publishes one accepted result with a new
   Source Revision and publication correlation id.
4. Given a stale Source Revision, ambiguous Style Companion, invalid field/value, missing style target,
   source-trace mismatch, or staged compilation failure, when solidify is attempted, then no file changes,
   no partial scene publishes, and one plain subject/problem/correction diagnostic identifies the failure.
5. Given solidify succeeds and the project is recompiled or reopened, then resolved style ids and values
   are deterministic, the accepted style survives, and `semanticId`, `occurrenceId`, `traceId`, asset ids,
   relationship ids, port ids, placement, and engineering facts remain unchanged.
6. Given the M44 style controls are shown, when the reviewer edits style, then controls support width,
   dash, color, label typography, route marker, and port display using the existing deterministic role
   cascade plus explicit occurrence targeting; no CSS selector language, label-derived identity, source
   text assembly, or engineering validation exists in the frontend.

## Tasks / Subtasks

- [x] Extend failing style-language and presentation-contract tests (AC: 1, 5, 6)
  - [x] Add minimal `route-marker: none | end-arrow` and
        `port-display: hidden | marker | marker-and-label` fields to Style Companion syntax and resolved
        scene styles.
  - [x] Add deterministic canonical style serialization/patch support; split serializer/patch behavior
        from `SheetCompanionLanguage.kt` if needed to keep responsibilities readable.
  - [x] Prove role and explicit occurrence targeting never infer identity from display labels.
- [x] Add failing interaction-contract and LSP transaction tests for `SetStyle` (AC: 3, 4, 5)
  - [x] Introduce `SetStyle` as the first narrow `PresentationEditOperation`, carrying target identity,
        Source Trace, full Source Revision preconditions, declared style fields, and one writable file.
  - [x] Prove only same-basename `*.sheet.style.athena` is writable; frontend never supplies source text.
  - [x] Prove absent companion creation and existing companion minimal patch both pass staged parse/compile.
  - [x] Prove stale, ambiguous, invalid, missing-target, trace-mismatch, and compile-failure paths write
        nothing and return subject/problem/correction diagnostics.
- [x] Implement server-owned style solidification (AC: 3, 4, 5)
  - [x] Reuse the existing diagram command and authoring service boundaries; do not add a direct file-write
        endpoint or a style-only compatibility path.
  - [x] Compare-and-set the complete Source Revision, including absent/present Style Companion state.
  - [x] Stage the exact Style Companion patch, parse and compile staged inputs, then publish atomically.
  - [x] Return publication correlation id and resulting revision; keep transaction shape ready for Story
        3.1 journal completion without creating a second history authority.
- [x] Add frontend session preview/discard behavior (AC: 1, 2, 6)
  - [x] Add a pure disposable preview-session model keyed by stable style target identity.
  - [x] Apply overlay values at paint time without mutating `AthenaDiagramScene` or Konva nodes as
        authority.
  - [x] Clear overlays on discard, repository switch, accepted revision change, reopen, and unavailable
        publication.
  - [x] Make route cap/join/marker and port display consume resolved or preview style instead of hardcoded
        paint values.
- [x] Add Theia style controls and solidify flow (AC: 1, 2, 3, 4, 6)
  - [x] Use appropriate color, numeric, option, and command controls for supported fields.
  - [x] Submit typed `SetStyle` through the existing LSP bridge; never parse or assemble Athena source in
        TypeScript.
  - [x] Keep controls compact and canvas-focused; do not add semantic navigator, CSS editor, or new
        renderer.
- [x] Prove deterministic round trip and authority preservation (AC: all)
  - [x] Add language, compiler, interaction-model, LSP, generated-contract, frontend preview, and Konva
        behavior tests.
  - [x] Record before/preview/discard/solidify/reopen file digests, revisions, stable identities, accepted
        style values, and rejection evidence under M44 implementation artifacts.
  - [x] Rebuild affected frontend/LSP/runtime surfaces and capture M44 Theia screenshot evidence when the
        product proof harness supports this story.
- [x] Run sequential validation (AC: all)
  - [x] Targeted Gradle tests, one command at a time.
  - [x] Frontend contract generation/check and frontend tests/build.
  - [x] `.\gradlew.bat --no-daemon --console=plain test`.
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`.

## Dev Notes

### PRD / Architecture Guardrails

- `FR-7` and `FR-8`: style is presentation-only, deterministic, explicitly previewed/discarded/
  solidified, and persisted only in same-basename Style Companion.
- `AD-1`: accepted `AthenaDiagramScene` stays canonical. Session preview is a disposable paint overlay.
- `AD-6`: solidify compare-and-sets full Source Revision, including Style Companion absent/present state.
- `AD-7` and `AD-8`: `SetStyle` is a `PresentationEditOperation`; server validates and writes only the
  exact same-basename Style Companion.
- `AD-9`: Konva consumes accepted scene plus transient overlay. Konva node state is never authority.
- `AD-11`: accepted solidify must use a transaction shape that Story 3.1 can journal. Do not invent a
  temporary style history mechanism.
- Library and SVG metadata remain geometry authority only. Style cannot select Symbol, Part, Function,
  Port semantics, Relationship, Capability, or engineering validation facts.

### Current Code Reality

- Story 2.1 added optional exact same-basename Style Companion discovery, full revision participation,
  parser validation, fail-closed publication, and deterministic `default`, `symbol`, `route`, `label`,
  and `port` cascade.
- `DiagramCommandContracts.kt` currently supports only `MOVE_OCCURRENCE` and `CONNECT_PORTS`. Extend this
  shared contract with `SetStyle`; do not create a private frontend payload.
- `DiagramAuthoringService.kt` already owns server-side move/connect source mutation. Reuse its validation,
  precondition, staged patch, and result conventions while fixing any atomicity gap exposed by tests.
- `athena-lsp-editor-bridge-service.ts` already submits generated diagram commands. Extend generated
  contracts and that path.
- `konva-diagram-adapter.ts` builds paint styles from accepted resolved styles and still hardcodes some
  route cap/join behavior. Overlay must be applied through a pure style resolver at render time.
- `athena-presentation-widget.tsx` owns canvas interaction UI. Keep style controls there or in one cohesive
  presentation-only component; do not let it read/write repository files.

### Target Model

Use explicit stable targets. A role style targets one of `default`, `symbol`, `route`, `label`, or `port`.
An occurrence override targets stable `occurrenceId`. Never derive a target from visible label text.

Minimum new values:

```athena
style "route" {
  route-marker: end-arrow
}

style "port" {
  port-display: marker-and-label
}
```

Keep syntax small. No CSS selectors, inheritance language, expressions, units system, arbitrary SVG
attributes, or engineering fields.

### Previous Story Intelligence

- Story 2.1 found a fail-closed gap: invalid/ambiguous Style Companion diagnostics must produce
  `UNAVAILABLE`, never stale-scene fallback. Preserve and extend those tests.
- Story 2.1 intentionally exposed Style Companion types through the language facade allow-list. New
  public style contracts must keep that boundary explicit.
- Full Story 2.1 regression passed 118 Gradle tasks, frontend 30 tests, encoding audit, and source-set
  hygiene audit. Do not weaken those gates.
- Workspace contains extensive intentional pre-M42 deletion/refactor changes. Preserve all unrelated
  user changes; no compatibility restoration.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
- Design: `_bmad-output/planning-artifacts/m44/design.md`
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m44/2-1-resolve-sheet-style-companion.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted through BMad create-story from full M44 PRD, addendum, architecture, design, epics,
  Sprint Status, Story 2.1 intelligence, CodeGraph/code analysis, and current git state.
- Red-green-refactor completed for Style Companion syntax/editor, resolved scene style, typed command,
  wire mapping, atomic LSP transaction, disposable frontend preview, Konva consumption, and Theia controls.
- Fixed full-LSP regression by regenerating the active M44 derived lock after Sheet/Style Companions were
  intentionally removed from governed package source hashes.
- Product proof initially sampled pre-layout blank canvases. Per-layer digest evidence identified the
  readiness race; proof now waits for stable non-white paint and a valid Electron display surface.
- Sequential verification passed: full LSP suite (27 tests), frontend contracts, frontend suite (35 tests),
  full IDE build/runtime preparation, live Electron product proof, Gradle `test` (118 tasks), encoding audit,
  and source-set hygiene audit.

### Completion Notes List

- Story created with explicit preview/discard/solidify authority, transaction, identity, and failure
  boundaries.
- Added `route-marker` and `port-display` to small deterministic Style Companion syntax and scene styles.
- Added deterministic server-owned Style Companion creation/patch with full Source Revision CAS, staged
  parse/compile, atomic write, plain diagnostics, resulting revision, and publication correlation id.
- Added typed `SET_STYLE` Presentation operation and explicit JSON-RPC wire mapper; frontend submits intent
  only and never reads or assembles Athena source.
- Added disposable role/occurrence preview overlays. Discard and publication/repository transitions clear
  overlays without mutating accepted scene or source.
- Live product transcript proves preview changes paint only, discard restores exact accepted canvas digest,
  solidify changes only same-basename Style Companion, and active M44 example remains unchanged.

### File List

- `_bmad-output/implementation-artifacts/m44/2-2-preview-discard-and-solidify-style-edits.md`
- `_bmad-output/implementation-artifacts/m44/operation-transcripts/2-2-style-authoring-product-proof.json`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-accepted.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-discarded.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-preview.png`
- `_bmad-output/implementation-artifacts/m44/screenshots/m44-style-solidified.png`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`
- `examples/m44/rolling-shutter/athena.lock`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramAuthoringService.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/DiagramCommandWireMapper.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/DiagramCommandWireMapperTest.kt`
- `ide/theia-frontend/package.json`
- `ide/theia-frontend/scripts/athena-diagram-contracts.test.mjs`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-product-layout.test.mjs`
- `ide/theia-frontend/scripts/athena-style-preview-session.test.mjs`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/diagram/generated/schema-hash.ts`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-diagram-command.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/schema/athena-diagram-scene.schema.json`
- `ide/theia-frontend/src/browser/diagram/generated/types.ts`
- `ide/theia-frontend/src/browser/diagram/generated/validators.ts`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/diagram/style-preview-session.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-product/package.json`
- `ide/theia-product/scripts/athena-m44-style-proof-main.js`
- `ide/theia-product/scripts/verify-athena-m44-style-proof.js`
- `ide/package.json`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializer.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/repository/AthenaRepositoryLockMaterializerTest.kt`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/DiagramCommandContracts.kt`
- `kernel/interaction-model/src/main/resources/schema/athena-diagram-command.schema.json`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/DiagramCommandContractTest.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetStyleCompanionEditor.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/AthenaDiagramSceneContract.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationContracts.kt`
- `kernel/presentation-model/src/main/resources/schema/athena-diagram-scene.schema.json`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationContractTest.kt`

### Change Log

- 2026-08-07: Story created via BMad create-story flow for M44 Story 2.2.
- 2026-08-07: Implemented and verified deterministic style preview, discard, and server-owned solidify loop;
  story ready for review.
