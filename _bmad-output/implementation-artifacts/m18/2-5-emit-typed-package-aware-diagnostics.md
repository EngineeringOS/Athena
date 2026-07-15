---
baseline_commit: 0bf9e8c267c01d85d50c11c3d9425e56869e907c
---

# Story 2.5: Emit Typed Package-Aware Diagnostics

Status: done

## Story

As an IDE user,
I want import and graph failures reported as typed Athena diagnostics,
so that I can understand and fix package-aware authoring errors.

## Acceptance Criteria

1. Import-resolution failures from `ProjectSemanticImportResolutionStatus` are converted into Athena-owned `ProjectSemanticDiagnostic` records with stable codes, severity, message, source unit id, and authored import span provenance.
2. Missing package, missing source unit, invalid availability, ambiguous namespace/binding, raw-path/classpath/frontend-style resolution attempts, and graph-invalid or cycle cases are represented by stable semantic diagnostic codes rather than generic parse errors or frontend warnings.
3. Diagnostic generation is compiler-owned and derives only from governed repository publication, source-unit admission, canonical graph validation, and semantic import-resolution records; it performs no filesystem scan, JVM classpath lookup, frontend alias lookup, LSP-only resolution, canvas lookup, or remote service call.
4. Diagnostics are deterministic and canonicalized through `ProjectSemanticGraphSnapshot`; codes and ordering remain stable across compiler tests, CLI/test consumers, and later LSP projection surfaces.
5. Graph-invalid and cycle diagnostics are represented before IDE closeout, even when the graph cannot publish a valid snapshot.
6. `AthenaCompiler` exposes the shared diagnostic path without adding a new LSP payload, declaration linker, lowering behavior, frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, or new dependency.

## Tasks / Subtasks

- [x] Add typed package-aware diagnostic tests first (AC: 1-5)
  - [x] Prove each import-resolution failure status emits the expected stable diagnostic code and import target span.
  - [x] Prove repository/source admission diagnostics preserve stable codes for invalid graph, source package mismatch, duplicate source unit, and non-admitted source unit cases.
  - [x] Prove diagnostics are canonicalized deterministically from reversed raw input order.
  - [x] Prove graph-invalid or cycle-like repository failures return diagnostics even when no snapshot can be published.
- [x] Implement compiler-owned diagnostic projection from semantic graph state (AC: 1-6)
  - [x] Add a cohesive diagnostic projector under `kernel/compiler/.../semantic` that consumes a resolved `ProjectSemanticGraphSnapshot` and returns the same snapshot shape with derived diagnostics.
  - [x] Map import statuses to stable codes, messages, severities, source unit ids, source spans, and related locations where available.
  - [x] Rebuild through `ProjectSemanticGraphSnapshot.canonical` so diagnostics are immutable, validated, and ordered by existing snapshot rules.
  - [x] Expose the path through `AthenaCompiler` without touching LSP projection behavior beyond compatibility tests.
- [x] Run scoped verification sequentially
  - [x] Run focused semantic diagnostic tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler diagnostic consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`. The story is compiler semantic logic, not frontend, canvas, or Kotlin Compose desktop-viewer work.
- Reuse existing models in `ProjectSemanticDiagnosticModels.kt`: `ProjectSemanticDiagnosticCode`, `ProjectSemanticDiagnosticSeverity`, `ProjectSemanticDiagnostic`, and `ProjectSemanticRelatedLocation`.
- Existing builder diagnostics live in `GovernedProjectSemanticGraphBuilder`; keep their stable-code style and do not create a second diagnostic vocabulary.
- Existing snapshot validation and ordering for diagnostics live in `ProjectSemanticGraphSnapshot`. Rebuild through `ProjectSemanticGraphSnapshot.canonical` or `canonicalizeDiagnostics` instead of sorting ad hoc.
- Import-resolution state was added in Story 2.4. Treat `ProjectSemanticImportResolutionStatus.RESOLVED` as non-diagnostic; map unavailable package, unavailable namespace, and ambiguous namespace to typed diagnostics using the authored import target span.
- Stable code naming should stay under the existing `semantic.*` convention. Suggested minimum codes:
  - `semantic.import.package.unavailable`
  - `semantic.import.namespace.unavailable`
  - `semantic.import.namespace.ambiguous`
  - existing repository/source codes from `GovernedProjectSemanticGraphBuilder` remain valid and must not be renamed casually.
- Raw-path/classpath/frontend-style attempts should be represented through the same package/import diagnostics derived from governed package availability; do not introduce a fallback resolver just to detect them.
- LSP publication remains Epic 4. This story may add compiler-facing API shape and downstream tests, but must not implement Theia UI behavior or separate LSP diagnostic projection logic.
- Graph-invalid and cycle cases are expected to come from governed repository publication diagnostics or canonical snapshot rejection paths. Preserve diagnostics when `ProjectSemanticGraphBuildResult.snapshot` is null.

### Previous Story Intelligence

- Story 2.4 added `ProjectSemanticImportResolution`, `ProjectSemanticImportExplanation`, `ProjectSemanticImportResolver`, and `AthenaCompiler.resolveProjectSemanticImports`.
- Story 2.4 review fixes established that package ownership must be selected by longest matching package-name prefix before namespace matching, and unavailable-package explanations may retain known matching namespace candidate ids.
- Story 2.4 verified `:kernel:compiler:test`, `:ide:lsp:test`, and the encoding audit. Reuse the same sequential verification discipline.

### References

- [Source: `epics.md` - Epic 2, Story 2.5]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-4]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-7, AD-8, AD-9, AD-12, AD-14]
- [Source: `2-4-resolve-imports-against-the-semantic-graph.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDiagnosticModels.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/GovernedProjectSemanticGraphBuilder.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Local adversarial code review found duplicate diagnostics on repeated projection; added an idempotency regression test before fixing top-level diagnostic canonicalization.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticProjectorTest` failed before implementation with missing projector/API.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticProjectorTest` failed before review fix on duplicate diagnostics from repeated projection.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticDiagnosticProjectorTest` passed after implementation and review fix.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - compiler-owned package-aware diagnostic scope prepared.
- Added compiler-owned `ProjectSemanticDiagnosticProjector` for import-resolution diagnostics.
- Mapped unavailable package, unavailable namespace, and ambiguous namespace statuses to stable `semantic.import.*` diagnostic codes with import target span provenance.
- Exposed diagnostic projection through `AthenaCompiler.emitProjectSemanticDiagnostics`.
- Made top-level diagnostic canonicalization distinct so repeated projection is idempotent.
- Verified rejected graph builds preserve stable diagnostics when no semantic snapshot can be published.

### File List

- `_bmad-output/implementation-artifacts/m18/2-5-emit-typed-package-aware-diagnostics.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDiagnosticProjector.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDiagnosticProjectorTest.kt`

## Change Log

- 2026-07-15: Created Story 2.5 from Epic 2, PRD FR-4, architecture AD-7/8/9/12/14, and Story 2.4 implementation intelligence.
- 2026-07-15: Added compiler-owned semantic diagnostic projection, tests, compiler API, idempotent canonical diagnostics, and verification evidence.
