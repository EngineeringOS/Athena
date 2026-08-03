---
story_key: 4-1-compile-drawing-profiles-and-line-classes
epic: m37-e4
requirements: [FR-17, FR-18]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 4.1: Compile Drawing Profiles And Line Classes

Status: review

## Story

As a professional drawing author,
I want typed drawing profiles and connection presentation classes,
so that line appearance and sheet grammar are governed by source instead of renderer defaults.

## Acceptance Criteria

1. Athena supports typed Drawing Standard Profile and Connection Presentation Class declarations for the M37 professional drawing path.
2. A compiled profile resolves frame, coordinate grid, title-block regions, text scale, stroke classes, junction rules, crossing rules, and reference-designation label policy.
3. Each connection presentation class resolves stroke weight, stroke style, color token, endpoint behavior, label policy, and crossing behavior.
4. Every visible engineering connection or `RouteFact` resolves exactly one presentation class from Connection Intent and selected profile.
5. Profile identity, class identity, source span, selected policy, and line-style evidence survive through compiled facts and presentation-facing payloads.
6. Missing, duplicate, incompatible, or unclassified presentation declarations emit typed diagnostics before rendering.
7. Parser, tree-sitter, compiler, renderer-adapter or protocol tests if touched, source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED drawing grammar tests (AC: 1, 2, 3, 4, 5, 6)
  - [x] Add parser/compiler test for Drawing Standard Profile and Connection Presentation Class declarations.
  - [x] Add resolution test mapping Connection Intent or route class to exactly one line class.
  - [x] Add invalid declaration tests for missing, duplicate, incompatible, and unclassified presentation.
  - [x] Add presentation-facing payload test proving line-style evidence carries only Athena facts.
- [x] Implement typed drawing profile and line class model (AC: 1, 2, 3)
  - [x] Use responsibility-named types only; no `Proof`, `Demo`, `Sample`, milestone names, `V0`/`V1`, XML, SVG metadata, renderer authority, or compatibility shims.
  - [x] Keep profile/class source-owned and compiler-validated.
  - [x] Keep renderer output paint-only: renderer receives compiled style tokens, never infers engineering meaning.
- [x] Implement class resolution and diagnostics (AC: 4, 5, 6)
  - [x] Resolve visible route facts to exactly one Connection Presentation Class.
  - [x] Carry profile id, class id, selected policy, source provenance, route id, connection id, and compiler snapshot.
  - [x] Emit typed diagnostics for missing/defaultless mappings, duplicate classes, invalid stroke/style/color, and ambiguous or unclassified routes.
- [x] Verify and record gates (AC: 7)
  - [x] Run focused drawing profile/line class tests.
  - [x] Run parser/tree-sitter tests if grammar touched.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Drawing grammar is typed Athena compiler input. It is not renderer CSS, SVG metadata, or planner output.
- Connection Intent remains semantic source. Presentation Profile selects how visible connection classes are communicated.
- Projection Policy selects the profile; profile/class declarations never mutate semantic truth.
- Renderer-adapter payload may carry style tokens and line classes, but must not infer class from geometry, color, SVG, DOM, or route shape.
- No compatibility shims. Athena is pre-public.

### Current Code Intelligence

- `RouteQualityEvidence.kt` now exposes route evidence and normalized Athena-only payloads.
- `RouteQualityPolicy.kt` owns hard rejects and score criteria. Do not fold line class vocabulary into route quality scoring.
- `AthenaProfessionalDrawingCompiler.kt` currently emits professional drawing evidence and route facts. If this story touches compiler presentation output, update compiler tests sequentially.
- Existing Theia rendering still has CSS/stroke defaults; those may remain as paint tokens only, not engineering authority.
- Existing `.athena` grammar already supports `profile`/`binding` forms from M34-M37. Extend in-place if required; no new parallel DSL.

### Expected Diagnostics

- `drawing.profile.missing`
- `drawing.profile.duplicate`
- `drawing.profile.stroke-class.invalid`
- `drawing.profile.line-class.duplicate`
- `drawing.profile.line-class.ambiguous`
- `drawing.profile.line-class.unclassified`
- `drawing.profile.protocol-authority.invalid`

### TDD And Verification

- RED first: focused drawing profile/line class test fails before implementation.
- Required sequential commands:
  - focused test chosen by touched module, expected start: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.DrawingProfileCompilerTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` if compiler touched
  - tree-sitter/parser tests if grammar touched
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 4.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-17, FR-18]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/addendum.md` - Non-Negotiable Source Principle, Route Quality Ownership]
- [Source: `_bmad-output/implementation-artifacts/m37/3-4-emit-explainable-route-quality-evidence.md` - Route quality evidence payload]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test --tests com.engineeringood.athena.routing.DrawingProfileCompilerTest` failed before implementation with unresolved drawing profile compiler symbols.
- GREEN: focused `DrawingProfileCompilerTest` passed after adding typed Drawing Standard Profile and Connection Presentation Class compiler.
- Regression: `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- Grammar note: `.athena` grammar, tree-sitter, and compiler modules were not touched in this slice, so parser/tree-sitter/compiler regression was not required.
- Hygiene: `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- Encoding: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Whitespace: `git diff --check` passed with existing line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added typed Drawing Standard Profile model, stroke classes, Connection Presentation Classes, and presentation payload.
- Line class resolution maps `RouteFact` Connection Intent to exactly one class and carries profile/class/source/policy evidence.
- Diagnostics cover duplicate profiles, invalid stroke classes, duplicate or ambiguous line classes, missing profile fields, and unclassified routes.
- Renderer-facing payload carries Athena style evidence only and no renderer inference or markup authority.
- No compiler module touched; `:kernel:compiler:test` not required for this story.

### File List

- _bmad-output/implementation-artifacts/m37/4-1-compile-drawing-profiles-and-line-classes.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/DrawingProfileCompiler.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/DrawingProfileCompilerTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 4.1 from finalized M37 PRD, addendum, epics, and Story 3.4 route evidence learnings.
- 2026-07-30: Implemented typed drawing profile and line class compiler, diagnostics, normalized line style payload, and routing tests.
