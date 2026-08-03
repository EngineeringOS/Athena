---
story_key: 5-2-compile-the-professional-drawing-surface
epic: m37-e5
requirements: [FR-31, FR-32]
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.2: Compile The Professional Drawing Surface

Status: review

## Story

As an evaluator,
I want the M37 source compiled into one complete professional drawing sheet,
so that semantic connectivity and drawing grammar can be judged together.

## Acceptance Criteria

1. The dedicated M37 source compiles into a presentation sheet with frame, coordinate grid, title block, supply rails, terminal groups, protective earth, device contacts, coils, indicators, labels, and orthogonal wiring.
2. Every visible route has validated endpoints, authored Connection Intent influence, lane or wire-column evidence, route-quality evidence, and exactly one Connection Presentation Class.
3. Accepted output has zero loose endpoints, fallback anchors, route/body intersections, ambiguous crossings, label/body overlaps, label/title-block overlaps, and unclassified routes.
4. Renderer payload consumes compiled Graphic Primitive IR only and performs no engineering, endpoint, route, label, or line-class inference.
5. Structural composition and renderer-boundary tests plus source-set hygiene, encoding audit, and `git diff --check` pass sequentially.

## Tasks / Subtasks

- [x] Add RED M37 professional drawing surface tests (AC: 1, 2, 3, 4)
  - [x] Compile `examples/m37/professional-control-drawing` through `AthenaProfessionalDrawingCompiler`.
  - [x] Assert sheet structure, required occurrence families, orthogonal routes, and title/grid/frame composition.
  - [x] Assert every route has endpoint, intent, lane, quality, and line-class evidence.
  - [x] Assert zero-defect gates are computed and fail when evidence is missing or degraded.
  - [x] Assert renderer-boundary payload contains Athena facts only.
- [x] Integrate professional drawing grammar gates into the compiler (AC: 2, 3, 4)
  - [x] Resolve route line classes through `DrawingProfileCompiler.standardProfessional()` or selected profile evidence.
  - [x] Validate endpoint attachment using compiled route facts.
  - [x] Validate route labels using `RouteLabelPlacementCompiler`.
  - [x] Convert profile, endpoint, label, lane, and route-quality failures into drawing diagnostics before presentation success.
- [x] Replace hardcoded success fields with computed evidence (AC: 3, 4)
  - [x] Compute component/label clearance from label diagnostics and bounds.
  - [x] Compute fallback absence from route quality, endpoints, and anchor ids.
  - [x] Compute renderer purity from presentation authorities and payload.
  - [x] Do not add compatibility shims, renderer repair, fake proof booleans, or milestone-named production classes.
- [x] Verify and record gates (AC: 5)
  - [x] Run focused M37 drawing surface test.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update Dev Agent Record, File List, Change Log, story status, and sprint status only after evidence exists.

## Dev Notes

### Authority And Scope

- Athena source remains SSOT. Drawing compiler may derive facts; renderer may only paint compiled Graphic Primitive IR.
- This story is not screenshot E2E. Screenshot proof belongs to Story 5.4.
- Do not copy QET/XML/HTML element formats. The M37 example is already dedicated and package-local.
- No compatibility paths for M34/M35/M36 behavior. If current compiler has hardcoded proof fields, replace with computed checks directly.

### Current Code Intelligence

- `AthenaProfessionalDrawingCompiler` already resolves representation material, sheet composition, semantic placements, occurrences, and route facts.
- Current evidence still contains hardcoded success fields for label clearance and fallback absence; those must become computed.
- `DrawingProfileCompiler` resolves line presentation classes from route intent influence and emits Athena-only payloads.
- `EndpointAttachmentValidator` validates exact endpoint attachment and rejects fallback/detached/body-interior endpoints.
- `RouteLabelPlacementCompiler` computes label bounds/collisions and blocks unresolved or colliding route labels.
- Story 5.1 added `DedicatedProfessionalDrawingSampleTest` and the source project under `examples/m37/professional-control-drawing`.

### Expected Diagnostics

- `drawing.profile.line-class.unclassified`
- `drawing.endpoint.*`
- `drawing.label.*`
- `drawing.route.quality.degraded`
- `drawing.route.fallback`
- `drawing.renderer-authority.invalid`

### TDD And Verification

- RED first: focused M37 surface test must fail on current hardcoded or missing gate behavior before implementation.
- Required sequential commands:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M37ProfessionalDrawingSurfaceTest`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check`

### References

- [Source: `_bmad-output/implementation-artifacts/m37/epics.md` - Story 5.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-30-m37/prd.md` - FR-31, FR-32]
- [Source: `_bmad-output/implementation-artifacts/m37/5-1-author-the-dedicated-m37-example.md` - dedicated sample]
- [Source: `_bmad-output/implementation-artifacts/m37/4-1-compile-drawing-profiles-and-line-classes.md` - line classes]
- [Source: `_bmad-output/implementation-artifacts/m37/4-2-enforce-exact-endpoint-attachment.md` - endpoint gates]
- [Source: `_bmad-output/implementation-artifacts/m37/4-4-compile-collision-aware-route-labels.md` - label gates]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- 2026-07-31: Focused M37 surface test first failed on real computed gates: `drawing.route.body-intersection` for `motor_down`, then route label collisions for `motor_up` and `motor_down`.
- 2026-07-31: Fixed upstream route planning to try clean top/bottom/left/right escape lanes around component bodies instead of falling back to intersecting geometry.
- 2026-07-31: Fixed route label compiler to deterministically relocate label-label overlaps when structure collisions are absent; component/frame/title collisions still diagnose fail-closed.
- 2026-07-31: Aligned stale M34 tests to current pre-1.0 architecture: M37 professional gates reject stale M34 drawing material instead of preserving compatibility.
- 2026-07-31: Verification passed sequentially: focused M37 surface test, routing label and avoidance tests, full `:kernel:compiler:test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- M37 professional drawing surface now compiles from the dedicated M37 example into one governed presentation sheet with frame/title/grid/structure, typed occurrences, orthogonal routes, exact endpoint evidence, route intent/lane/quality evidence, and line-class evidence.
- Zero-defect gates are computed from compiler evidence: loose endpoints, fallback anchors, body intersections, ambiguous crossings, label collisions, unclassified routes, raw markup authority, and renderer engineering inference are all blocked before presentation success.
- Route engine and label compiler fixes stay upstream of rendering. Renderer remains paint-only over compiled Graphic Primitive IR and route facts.

### File List

- _bmad-output/implementation-artifacts/m37/5-2-compile-the-professional-drawing-surface.md
- _bmad-output/implementation-artifacts/m37/sprint-status.yaml
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/drawing-profile.athena
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/m37-surface-bindings.athena
- examples/m37/professional-control-drawing/packages/representation/com/engineeringood/m37/professional/m37-surface-elements.athena
- examples/m37/professional-control-drawing/src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingModels.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalCabinetCompositionTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34ProfessionalControlDrawingCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedProfessionalDrawingSampleTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M37ProfessionalDrawingSurfaceTest.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/DrawingProfileCompiler.kt
- kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompiler.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineLaneAndAvoidanceTest.kt
- kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteLabelPlacementCompilerTest.kt

## Change Log

- 2026-07-30: Created implementation-ready Story 5.2 from finalized M37 PRD, epics, and Stories 4.1/4.2/4.4/5.1 learnings.
- 2026-07-31: Implemented computed professional drawing surface gates, route body avoidance, collision-aware route label relocation, dedicated M37 surface assertions, stale M34 test alignment, and full verification evidence.
