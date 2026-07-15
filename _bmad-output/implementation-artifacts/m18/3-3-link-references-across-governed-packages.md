---
baseline_commit: 33b6ea2c36506e80e1f53783257a886880d72b9c
---

# Story 3.3: Link References Across Governed Packages

Status: done

## Story

As a package author,
I want authored references to bind across governed package boundaries,
so that reusable package meaning can be consumed deterministically.

## Acceptance Criteria

1. The compiler-owned reference linker resolves supported authored references through namespaces selected by resolved imports from governed package dependencies.
2. Cross-package bindings resolve only through admitted packages, source units, declarations, and `ProjectSemanticImportResolutionStatus.RESOLVED`; direct dependency availability alone is not a fallback symbol authority.
3. Binding provenance preserves source unit/reference span and target declaration id/source span through existing `ProjectSemanticBinding` and `ProjectSemanticDeclaration` records.
4. Unresolved or ambiguous cross-package references produce deterministic Athena-owned `semantic.reference.*` diagnostics rather than filesystem, classpath, frontend, canvas, or caller-order lookup.
5. Binding records and diagnostics remain canonical, immutable, validated, and deterministically ordered independent of caller collection order.
6. `AthenaCompiler` uses the same shared reference-linking path without adding LSP payloads, lowering behavior, frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, or new dependencies.

## Tasks / Subtasks

- [x] Add cross-package reference-linking tests first (AC: 1-5)
  - [x] Prove a root source unit import can expose a governed dependency namespace and bind a connection endpoint to a dependency declaration.
  - [x] Prove dependency package availability without a resolved import does not make dependency declarations linkable.
  - [x] Prove unresolved cross-package references emit stable `semantic.reference.unresolved` diagnostics.
  - [x] Prove ambiguous imported declaration candidates emit stable `semantic.reference.ambiguous` diagnostics.
  - [x] Prove output remains deterministic from reversed raw input order.
- [x] Extend compiler-owned reference linking to resolved imported namespaces (AC: 1-6)
  - [x] Reuse `ProjectSemanticReferenceLinker`; do not create a second linker.
  - [x] Include declarations from the source unit namespace plus namespaces selected by resolved imports.
  - [x] Ignore unresolved or ambiguous import resolutions as candidate authorities.
  - [x] Rebuild bindings and diagnostics through `ProjectSemanticGraphSnapshot.canonical`.
  - [x] Preserve `AthenaCompiler.linkProjectSemanticReferences` as the single public compiler path.
- [x] Run scoped verification sequentially
  - [x] Run focused reference-linking tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler/LSP consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`. This is compiler semantic logic only.
- Do not touch Theia frontend, EPLAN canvas behavior, or Kotlin Compose desktop-viewer code.
- Reuse `ProjectSemanticImportResolver`, `ProjectSemanticReferenceLinker`, `ProjectSemanticImportResolutionStatus.RESOLVED`, namespace declaration indexes, `ProjectSemanticBinding`, and `ProjectSemanticGraphSnapshot.canonical`.
- Candidate declarations for one source unit should come from:
  1. the source unit's own namespace, and
  2. namespaces selected by that source unit's resolved imports.
- Do not treat all direct dependency declarations as visible. The authored source must have a resolved import before a dependency namespace contributes link candidates.
- Story 3.4 owns capability provenance. Story 3.5 owns lowering. Epic 4 owns LSP navigation projection.

### Previous Story Intelligence

- Story 3.2 added `ProjectSemanticReferenceLinker`, linking `ConnectionDeclaration` endpoint `QualifiedName` references to indexed port declarations in the same namespace.
- Story 3.2 established `semantic.reference.unresolved` and `semantic.reference.ambiguous` diagnostics and exposed `AthenaCompiler.linkProjectSemanticReferences`.
- Story 3.1 added declaration indexing and deterministic declaration ambiguity diagnostics.
- Story 2.4 added resolved import records with selected namespace ids and governed package availability explanations.

### References

- [Source: `epics.md` - Epic 3, Story 3.3]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-5]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-3, AD-5, AD-6, AD-7, AD-9, AD-12, AD-13]
- [Source: `3-2-link-references-across-source-units.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticImportResolver.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinker.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest` failed before implementation on cross-package import-selected candidate tests.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticReferenceLinkerTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - cross-package reference-linking scope prepared.
- Extended `ProjectSemanticReferenceLinker` to include declarations from namespaces selected by resolved imports.
- Preserved governed-package boundary discipline: direct dependency availability alone does not expose declarations without authored resolved imports.
- Added cross-package binding, no-import unresolved, ambiguous imported reference, and deterministic-ordering tests.
- Kept `AthenaCompiler.linkProjectSemanticReferences` as the single public compiler path.

### File List

- `_bmad-output/implementation-artifacts/m18/3-3-link-references-across-governed-packages.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinker.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticReferenceLinkerTest.kt`

## Change Log

- 2026-07-15: Created Story 3.3 from Epic 3, PRD FR-5, architecture AD-3/5/6/7/9/12/13, and Story 3.2 implementation intelligence.
- 2026-07-15: Added cross-governed-package reference linking through resolved import-selected namespaces and verification evidence.
