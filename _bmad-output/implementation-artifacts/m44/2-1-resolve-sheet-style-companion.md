---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 2.1: Resolve Sheet Style Companion

Status: done

## Story

As an engineering author,
I want same-basename Style Companions to control presentation only,
so that style can change without corrupting engineering truth.

## Acceptance Criteria

1. Given `rolling-shutter.sheet.athena` has no `rolling-shutter.sheet.style.athena`, when Athena compiles
   the scene, then default styles are used and all engineering identity, source trace, occurrence,
   relationship, and port facts remain unchanged.
2. Given exactly one same-basename `rolling-shutter.sheet.style.athena` exists beside the Sheet Companion,
   when Athena compiles the scene, then the style companion is parsed, included in Source Revision, and
   its declared style fields override defaults without changing Entity, Function, Part, Port,
   Relationship, Symbol, Occurrence, or Trace identity.
3. Given more than one style companion candidate exists by casing or basename ambiguity, when Athena
   resolves companions, then scene publication fails closed with a plain subject/problem/correction
   diagnostic and no partial scene.
4. Given a style companion declares line width, dash, cap/join, color, opacity, label typography, route
   marker, or port display fields, when style cascade resolves, then only declared fields override and
   repeated compilation produces identical resolved style ids and style values.
5. Given a style companion is edited, when `currentInputRevision()` is computed, then the style companion
   digest participates in Source Revision. Missing style companion contributes an explicit absent marker,
   so creating/removing the file changes revision deterministically.

## Tasks / Subtasks

- [x] Add failing language/runtime tests for Style Companion discovery (AC: 1, 2, 3, 5)
  - [x] Missing style companion resolves to defaults and explicit absent revision marker.
  - [x] Exactly one same-basename `*.sheet.style.athena` resolves beside the Sheet Companion.
  - [x] Ambiguous same-basename/case variants reject with subject/problem/correction diagnostic.
  - [x] Source Revision changes when style companion bytes change, appear, or disappear.
- [x] Add failing compiler/presentation tests for style cascade (AC: 1, 2, 4)
  - [x] Style companion overrides route/symbol/label/port style fields only where declared.
  - [x] Style changes never alter semantic ids, occurrence ids, trace ids, relationship ids, port ids, or
        asset ids.
  - [x] Repeated compile of same source/sheet/style inputs produces identical resolved styles.
- [x] Implement Style Companion language contract (AC: 2, 3, 4)
  - [x] Reuse `SheetCompanionLanguage` patterns; do not invent a second parser stack.
  - [x] Keep authoring syntax human-first and presentation-only.
  - [x] Reject unknown unnamespaced style fields; diagnostic must name exact style subject and correction.
- [x] Wire style companion into compiler/LSP publication (AC: all)
  - [x] Locate style companion by same basename as `*.sheet.athena`.
  - [x] Feed parsed style facts into `AthenaDiagramSceneCompiler`.
  - [x] Include style companion path/digest or explicit absent marker in `currentInputRevision()`.
  - [x] Publication unavailable on companion ambiguity or style parse error; no guessed fallback scene.
- [x] Preserve authority boundaries (AC: all)
  - [x] Style files may write presentation styling only, never Engineering Reality.
  - [x] Frontend consumes resolved styles only; it must not parse `*.sheet.style.athena`.
- [x] Run sequential validation (AC: all)
  - [x] Targeted language tests for style companion parse/discovery.
  - [x] Targeted compiler tests for resolved styles and identity preservation.
  - [x] Targeted LSP tests for Source Revision and unavailable diagnostics.
  - [x] Frontend contract/build/test if schema or generated types change.
  - [x] `.\gradlew.bat --no-daemon --console=plain test`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - [x] `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`

## Dev Notes

### PRD / Architecture Guardrails

- `FR-7`: style cascade is deterministic and cannot alter engineering meaning.
- `FR-8`: persisted style lives in same-basename `*.sheet.style.athena`; session preview/discard belongs
  to Story 2.2.
- `AD-6`: Source Revision must include Sheet companion digest and Style Companion digest or explicit absent
  marker.
- `AD-8`: placement writes `*.sheet.athena`; persisted style writes same-basename
  `*.sheet.style.athena`; multiple style companions are an error.
- Style Companion is presentation authority only. Do not add engineering fields, Symbol selection, Part
  binding, port direction, flow, capability, relationship, or validation facts here.

### Current Code Reality

- `SheetCompanionLanguage.kt` owns sheet parse and `SheetCompanionLocator`. It currently locates required
  same-basename `*.sheet.athena` for a source file.
- `AthenaDiagramProtocol.currentInputRevision()` hashes Athena source, Sheet Companion, repository lock,
  and presentation contract resources. It does not yet hash Style Companion.
- `AthenaDiagramSceneCompiler` currently creates one default `ResolvedStyle` and assigns it to all
  occurrences, ports, labels, routes, and decorations.
- `KonvaDiagramAdapter` already consumes resolved `styleId` values. Do not move style parsing to the
  frontend.

### Suggested Style Syntax

Keep M44 syntax small and explicit:

```athena
style "default" {
  stroke: #20252bff
  fill: #ffffffff
  width: 1
  dash: []
  cap: butt
  join: miter
  opacity: 255
  font-size: 1
  font-weight: 400
}
```

If parser cost is high, start with route/label/port/default style blocks only. Do not introduce CSS,
selectors, inheritance, expressions, units, or engineering terms in this story.

### Previous Story Intelligence

- Story 1.2 established explicit `semanticId`, Function-first trace, and M44-owned rolling-shutter
  fixture paths.
- Story 1.3 made the Theia adapter consume resolved styles and admitted assets from scene/publication
  only. Keep that boundary.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/planning-artifacts/m44/epics.md`
- Previous story: `_bmad-output/implementation-artifacts/m44/1-3-render-clean-real-symbol-sheet.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex.

### Debug Log References

- Story contexted from M44 Epic 2 Story 2.1 after Epic 1 stories reached review.
- Existing Sheet Companion locator, LSP input revision, scene compiler default style, and frontend style
  consumption inspected before story creation.
- RED: LSP invalid Style Companion published `STALE`; root cause was fatal Style Companion diagnostics
  missing from `AthenaDiagramPublicationService.requiresUnavailable()`.
- RED/GREEN: language tests exposed missing `cap`/`join`; compiler tests exposed missing style-cascade
  input. Added fields and role cascade, then verified identity preservation and deterministic output.
- Full regression first found intentional public Style Companion types missing from language facade
  allow-list; allow-list updated and complete regression rerun green.

### Completion Notes List

- Added deterministic optional Style Companion discovery, parsing, casing-ambiguity rejection, and
  explicit absent-marker revision behavior.
- Added `default`, `symbol`, `route`, `label`, and `port` presentation role cascade with declared-field
  overlays, content-derived style ids, `cap`/`join`, color, width, dash, opacity, and typography.
- LSP now passes parsed style facts into scene compilation and fails closed without retaining stale scene
  for invalid or ambiguous Style Companions.
- Verified style changes preserve semantic, occurrence, trace, relationship, port, and asset identities.
- Verification: targeted language/compiler/LSP tests, frontend `yarn test` (30 tests), full Gradle test,
  encoding audit, and source-set hygiene audit all pass.

### File List

- `kernel/language/src/main/kotlin/com/engineeringood/athena/language/SheetCompanionLanguage.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/SheetCompanionLanguageTest.kt`
- `kernel/language/src/test/kotlin/com/engineeringood/athena/language/LanguageFacadeBoundaryTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaDiagramSceneCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramPublicationService.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagramSceneRequestTest.kt`
- `_bmad-output/implementation-artifacts/m44/2-1-resolve-sheet-style-companion.md`
- `_bmad-output/implementation-artifacts/m44/sprint-status.yaml`

### Change Log

- 2026-08-07: Story created via BMad create-story flow for M44 Story 2.1.
- 2026-08-07: Implemented and verified Style Companion discovery, revision participation, deterministic
  role cascade, identity preservation, and fail-closed publication; moved story to review.
