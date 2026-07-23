---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 2.2
epic: 2
title: Implement Package Resolver And Resolution Facts
---

# Story 2.2: Implement Package Resolver And Resolution Facts

## Status

Review

## Story

As an Athena compiler/runtime consumer,
I want package resolution facts,
so that downstream binding can prove exactly which packages and descriptors were used.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/2-1-add-governed-local-package-registry-roots.md`

## Acceptance Criteria

1. Given valid Engineering and Representation packages, when resolver runs, then it returns package
   id, package kind, version, descriptor path, dependency list, validation status, diagnostics, and
   selected registry root.
2. Given missing, ambiguous, incompatible, or invalid packages, when resolver runs, then it returns
   structured diagnostics and no generic renderer fallback is reported as success.
3. Given identical source, registry content, config, and policy, when resolver runs repeatedly, then
   resolution facts are deterministic.
4. Given Story 2.1 established governed registry roots, when resolver facts are added, then resolver
   behavior consumes explicit roots and does not scan arbitrary workspace folders.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED tests for valid Engineering and Representation package resolution facts. (AC: 1,4)
- [x] Add RED tests for missing, ambiguous, incompatible, and invalid package diagnostics with no
  renderer fallback success. (AC: 2)
- [x] Add RED determinism test proving repeated resolution emits stable fact order and diagnostics.
  (AC: 3)
- [x] Implement package resolver request, requirement, candidate descriptor, dependency, resolved
  package fact, and diagnostic models in `:kernel:package-runtime`. (AC: 1)
- [x] Implement resolver behavior over governed registry candidates and explicit roots. (AC: 1,2,4)
- [x] Document resolver facts and the boundary between registry discovery, resolver facts, and cache
  identity. (AC: 1..4)
- [x] Run focused package-runtime tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- Story 2.1 added `:kernel:package-runtime` with governed roots, explicit workspace-folder
  ignoring, duplicate package id policy, selected package facts, and registry diagnostics.
- Story 2.2 should not parse JSON/YAML descriptors yet unless existing code already provides a
  cohesive parser. Use typed in-memory candidates/facts first so downstream compiler stories can
  consume deterministic resolution evidence.
- Resolver facts must name authority precisely:
  - engineering package
  - representation package
  - descriptor path
  - selected registry root
  - validation state
  - dependency list
  - diagnostics
- Do not implement cache identity; that belongs to Story 2.3.
- Do not implement Binding Resolver, Presentation Profile, renderer fallback, Theia UI, or sample
  project assets in this story.
- Suggested diagnostic codes:
  - `package.resolution.missing`
  - `package.resolution.ambiguous`
  - `package.resolution.incompatible`
  - `package.resolution.invalid`
  - `package.resolution.renderer-fallback-forbidden`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Keep resolver behavior in `kernel/package-runtime/src/main/kotlin/.../packageruntime`.
- Keep package descriptor contracts in `kernel/package-model`.
- Prefer cohesive files such as `PackageResolutionModels.kt`,
  `LocalPackageResolver.kt`, and `LocalPackageResolverTest.kt`.

## Testing Requirements

- Follow TDD: write failing resolver tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`.
- Full regression command after story completion: `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- AC evidence must include exact test names/commands and the polish/purge result.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed in
  `:kernel:package-runtime:compileTestKotlin` with unresolved `PackageResolutionRequest`,
  `PackageRequirement`, `PackageResolutionPackageKind`, `PackageDependency`,
  `PackageDescriptorCandidate`, and `LocalPackageResolver`.
- GREEN: First `:kernel:package-runtime:test` compile passed but failed one assertion because
  diagnostics were sorted by package id instead of requirement order; after preserving deterministic
  requirement order, `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  passed.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after resolver
  documentation and implementation.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added package resolution request, requirement, dependency, candidate, resolved fact, validation
  status, result, and diagnostic contracts in `:kernel:package-runtime`.
- Added `LocalPackageResolver` over explicit governed roots and typed descriptor candidates.
- Resolver emits deterministic facts for valid Engineering and Representation packages, including
  package id, kind, version, descriptor path, dependencies, validation status, diagnostics, and
  selected root.
- Resolver fails closed for missing, ambiguous, incompatible, and invalid packages; no renderer
  fallback is reported as success.
- Preserved diagnostic order by requirement order for traceability while keeping resolved package
  facts deterministically sorted.
- Documented package resolution facts and the cache-identity boundary.
- AC-to-evidence: AC1 covered by
  `resolver emits facts for valid engineering and representation packages`; AC2 covered by
  `resolver fails closed for missing ambiguous incompatible and invalid packages`; AC3 covered by
  `resolver emits deterministic facts for identical inputs`; AC4 covered by explicit governed roots
  in resolver tests and no workspace scan path; AC5 covered by full `check`, encoding audit,
  `git status --short`, and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/2-2-implement-package-resolver-and-resolution-facts.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/LocalPackageResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageResolutionModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/LocalPackageResolverTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 2 after governed registry roots.
- 2026-07-22: Implemented local package resolver facts and fail-closed diagnostics.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package runtime, package model, compiler knowledge package, tests,
  documentation, fixtures, and sprint artifacts.
- Remove dead/stale resolver experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
