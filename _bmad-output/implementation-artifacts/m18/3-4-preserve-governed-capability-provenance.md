---
baseline_commit: 2cd66bf53232babf6e881ee61bd391d22e5f095c
---

# Story 3.4: Preserve Governed Capability Provenance

Status: done

## Story

As a platform architect,
I want imported namespaces to preserve governed capability provenance,
so that imports represent engineering capability and not just code inclusion.

## Acceptance Criteria

1. A compiler-owned capability provenance pass consumes a `ProjectSemanticGraphSnapshot` plus governed package capability markers and returns the same snapshot shape with namespace capability provenance recorded.
2. Namespace capability markers are keyed by governed `PackageKey`, are applied only to namespaces owned by that package, and are represented through `ProjectSemanticNamespace.admittedCapabilities`.
3. Imported namespaces preserve those capability markers after import resolution and reference linking, proving imported meaning carries engineering capability provenance rather than syntax-only inclusion.
4. The compiler can explain capability availability through deterministic Athena-owned diagnostics with stable `semantic.capability.*` codes and namespace/source provenance where available.
5. Capabilities and diagnostics are immutable, validated, distinct, and deterministically ordered through `ProjectSemanticGraphSnapshot.canonical`.
6. `AthenaCompiler` exposes the shared capability-provenance path without adding LSP payloads, lowering behavior, frontend logic, canvas behavior, Kotlin Compose desktop-viewer logic, remote registry behavior, or new dependencies.

## Tasks / Subtasks

- [x] Add capability-provenance tests first (AC: 1-5)
  - [x] Prove package capability markers are recorded on namespaces owned by that package.
  - [x] Prove imported namespace capability markers survive import resolution and reference linking.
  - [x] Prove capability explanation diagnostics use stable `semantic.capability.namespace.available` codes.
  - [x] Prove output is deterministic from reversed raw input order and idempotent across repeated projection.
  - [x] Prove blank capability markers are rejected by canonical snapshot validation.
- [x] Implement compiler-owned capability provenance projection (AC: 1-6)
  - [x] Add a cohesive capability provenance projector under `kernel/compiler/.../semantic`.
  - [x] Apply markers by `PackageKey` to owned namespaces only.
  - [x] Emit deterministic info diagnostics for capability availability with namespace source provenance.
  - [x] Rebuild through `ProjectSemanticGraphSnapshot.canonical`.
  - [x] Expose the path through `AthenaCompiler`.
- [x] Run scoped verification sequentially
  - [x] Run focused capability-provenance tests, then `:kernel:compiler:test`.
  - [x] Run `:ide:lsp:test` to protect downstream compiler/LSP consumers.
  - [x] Run the encoding audit after text edits.

## Dev Notes

- Keep implementation in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic`. This is compiler semantic logic only.
- Do not touch Theia frontend, EPLAN canvas behavior, or Kotlin Compose desktop-viewer code.
- Reuse `ProjectSemanticNamespace.admittedCapabilities`, `ProjectSemanticDiagnostic`, `ProjectSemanticGraphSnapshot.canonical`, and existing package keys.
- Capability marker examples for this story can use stable strings such as `component-knowledge:available`; do not introduce a broad capability ontology.
- Story 3.5 owns lowering behavior. Epic 4 owns LSP projection of diagnostics/navigation.

### Previous Story Intelligence

- Story 3.3 extended `ProjectSemanticReferenceLinker` so resolved imported namespaces contribute declaration candidates across governed package boundaries.
- Story 3.2 established same-namespace reference binding and `semantic.reference.*` diagnostics.
- Story 3.1 established namespace declaration indexes and deterministic declaration diagnostics.
- The M18 architecture already includes `ProjectSemanticNamespace.admittedCapabilities`; Story 3.4 should use it rather than inventing a parallel payload.

### References

- [Source: `epics.md` - Epic 3, Story 3.4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-6]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-6, AD-9, AD-12, AD-15]
- [Source: `3-3-link-references-across-governed-packages.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphModels.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticGraphSnapshot.kt`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticCapabilityProvenanceProjectorTest` failed before implementation with missing projector/compiler API.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.semantic.ProjectSemanticCapabilityProvenanceProjectorTest` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Ultimate context engine analysis completed - governed capability provenance scope prepared.
- Added compiler-owned `ProjectSemanticCapabilityProvenanceProjector`.
- Recorded governed package capability markers on owned semantic namespaces through `admittedCapabilities`.
- Emitted deterministic `semantic.capability.namespace.available` info diagnostics as the compiler explanation surface.
- Verified imported namespace capability markers survive import resolution, declaration indexing, and reference linking.
- Exposed capability preservation through `AthenaCompiler.preserveProjectSemanticCapabilities`.

### File List

- `_bmad-output/implementation-artifacts/m18/3-4-preserve-governed-capability-provenance.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticCapabilityProvenanceProjector.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticCapabilityProvenanceProjectorTest.kt`

## Change Log

- 2026-07-15: Created Story 3.4 from Epic 3, PRD FR-6, architecture AD-6/9/12/15, and Story 3.3 implementation intelligence.
- 2026-07-15: Added governed package capability projection, compiler diagnostics, compiler API, and verification evidence.
