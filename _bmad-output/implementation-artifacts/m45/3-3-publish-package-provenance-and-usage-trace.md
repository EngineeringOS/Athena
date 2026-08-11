---
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
---

# Story 3.3: Publish Package Provenance and Usage Trace

Status: done

## Story

As an engineer,
I want every visible package-backed occurrence traceable to its admitted source item,
so that library usage is reviewable without machine-local path leakage.

## Acceptance Criteria

1. Immutable package provenance ends at `AdmittedPackageItem`; its canonical read projection contains stable package/item identity, logical source locator, source digest, and license.
2. Compiler-owned usage trace links each admitted `FunctionRepresentationBinding` to one or more visible scene occurrence ids without changing Engineering Reality or renderer authority.
3. A deterministic composed read view joins binding, admitted PackageItem, provenance, and occurrence identity; equivalent input ordering produces equal canonical bytes/digest.
4. Canonical bytes and exported read payloads contain no absolute Windows/POSIX paths; only package-relative logical locators are exposed.
5. Missing bindings, non-ready items, duplicate occurrence links, and unknown occurrence ids fail closed with plain subject/problem/correction diagnostics.

## Tasks / Subtasks

- [x] Task 1 (AC: 1, 4)
  - [x] Add immutable package provenance view and portable canonical serialization.
  - [x] Ensure binding model does not own package provenance.
- [x] Task 2 (AC: 2, 3, 5)
  - [x] Add deterministic usage-trace compiler/read model joining bindings, READY items, and scene occurrence ids.
  - [x] Add fail-closed diagnostics for missing/duplicate/unknown links.
- [x] Task 3 (AC: 3, 4)
  - [x] Add package-model/compiler tests for canonical stability and path exclusion.
  - [x] Run focused Gradle tests, encoding audit, and source-set hygiene audit sequentially.

## Dev Notes

- Reuse `PackageItemIdentity`, `PackageItemProvenance`, `AdmittedPackageItem`, `FunctionRepresentationBinding`, and existing canonicalization patterns.
- Package provenance is package-owned and terminates at the admitted item. Usage trace is a compiler-owned derived read model beginning at Function binding and ending at scene occurrence.
- Do not add `.elmt`, HTML, XML, reference-tree, filename-matching, registry, or compatibility paths. Future precompiled package index remains a derived compiler boundary only.
- Do not make Canonical Scene or renderer nodes authoritative. Occurrence ids are read-only links.
- No absolute paths in canonical bytes or exports. Normalize `\\` to `/` and reject drive/UNC/rooted locators.
- Production source contains product architecture only; tests and evidence remain in test/artifact directories.

### References

- [Source: _bmad-output/planning-artifacts/m45/epics.md#Story 3.3]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-10]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md#AD-14]
- [Source: AGENTS.md#Pre-1.0 Architecture Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Initial focused package model/runtime tests passed before moving usage compilation to the compiler module.
- Compiler usage trace tests passed after ownership correction; no external format or machine-local path is used.

### Completion Notes List

- Added `PackageProvenanceView`, `FunctionRepresentationUsageTrace`, and deterministic `PackageUsageTraceView`.
- Added compiler-owned `PackageUsageTraceCompiler` with READY-only admission and fail-closed occurrence diagnostics.
- Added tests for canonical ordering/digest stability, missing items, duplicate links, unknown occurrences, and absolute locators.
- Future precompiled package index remains a derived compiler boundary; M45 runtime remains native package-source based.

### File List

- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/PackageProvenanceTrace.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/FunctionRepresentationBindingContractsTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PackageUsageTraceCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PackageUsageTraceCompilerTest.kt`
- `_bmad-output/implementation-artifacts/m45/3-3-publish-package-provenance-and-usage-trace.md`

### Change Log

- 2026-08-09: Created BMad story context; implementation started.
- 2026-08-09: Implemented immutable provenance and compiler-owned usage trace; focused tests pass.
