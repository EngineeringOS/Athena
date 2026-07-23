---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 2.1
epic: 2
title: Add Governed Local Package Registry Roots
---

# Story 2.1: Add Governed Local Package Registry Roots

## Status

Review

## Story

As an Athena project maintainer,
I want explicit local package registry roots,
so that package resolution is reproducible and does not scan arbitrary workspace folders.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Epic 1 retrospective: `_bmad-output/implementation-artifacts/m32/epic-1-retro-2026-07-22.md`

## Acceptance Criteria

1. Given Athena-owned and project-local package roots, when registry discovery runs, then roots are
   evaluated in deterministic priority order and arbitrary workspace folders are ignored.
2. Given duplicate package ids are found across roots, when resolution runs, then precedence is
   deterministic or ambiguity is diagnosed according to the registry policy.
3. Given existing compiler knowledge package loading normalizes roots, when M32 package registry
   roots are introduced, then the implementation reuses compatible deterministic path patterns
   without extending compiler-only knowledge package authority.
4. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED tests for governed package registry roots with Athena-owned and project-local roots in
  deterministic priority order. (AC: 1,3)
- [x] Add RED tests proving arbitrary workspace folders are ignored unless explicitly configured as
  registry roots. (AC: 1)
- [x] Add RED tests for duplicate package id behavior: deterministic precedence or structured
  ambiguity diagnostic based on registry policy. (AC: 2)
- [x] Implement local package registry root model/runtime boundary outside `kernel:package-model`
  contract files. (AC: 1,3)
- [x] Implement deterministic root normalization, priority ordering, explicit-root filtering, and
  duplicate package id policy diagnostics. (AC: 1,2)
- [x] Document the governed local registry root policy and brownfield relationship to
  `AthenaKnowledgeResolver`. (AC: 3)
- [x] Run focused package-runtime/package-model tests sequentially, then full regression
  sequentially; do not run Gradle concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4)

## Dev Notes

- Epic 1 established package contracts in `kernel:package-model`. Epic 2 should add resolver/runtime
  mechanics without putting runtime behavior into those contract files.
- Existing brownfield pattern:
  `kernel/compiler/.../knowledge/AthenaKnowledgeResolver` normalizes roots with
  `toAbsolutePath().normalize()`, distinct stable path keys, and deterministic sorting. Reuse that
  deterministic style, but do not make M32 package registry a compiler-only knowledge package
  feature.
- Registry roots must be explicit. Do not scan arbitrary workspace folders, parent directories,
  `.tools`, reference mirrors, or internet registries.
- Suggested boundary:
  - new module `:kernel:package-runtime` in this repo's existing module layout, or an existing
    runtime module only if fresh inspection shows a better cohesive package runtime home;
  - keep package descriptor contracts in `kernel/package-model`.
- Keep Story 2.1 limited to root discovery and duplicate id policy. Do not implement full package
  descriptor parsing, resolution facts, cache identity, Binding Resolver, Presentation Profile, or
  renderer integration.
- Suggested diagnostic codes:
  - `package.registry.root.invalid`
  - `package.registry.root.duplicate`
  - `package.registry.package-id.ambiguous`
  - `package.registry.package-id.precedence-applied`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- If a new module is added, update `settings.gradle.kts` and use existing Kotlin JVM module build
  conventions.
- Keep runtime files cohesive, e.g. `PackageRegistryModels.kt`,
  `LocalPackageRegistry.kt`, and `LocalPackageRegistryTest.kt`.
- Do not touch Theia, LSP, renderer, grammar, or M32 samples in this story.

## Testing Requirements

- Follow TDD: write failing registry-root tests before production code.
- Focused command should target the owning module, for example
  `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`.
- Full regression command after story completion: `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- AC evidence must include exact test names/commands and the polish/purge result.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed in
  `:kernel:package-runtime:compileTestKotlin` with unresolved `LocalPackageRegistry`,
  `LocalPackageRegistryRoot`, `PackageRegistryRootKind`, and related runtime symbols.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after
  adding `:kernel:package-runtime` and governed local registry root behavior.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after documentation and
  module integration.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `:kernel:package-runtime` as the M32 runtime boundary for package registry behavior.
- Added governed local registry root models for project-local and Athena-owned roots, deterministic
  path normalization, explicit workspace-folder ignoring, duplicate package id policies, selected
  package facts, and structured diagnostics.
- Added tests proving deterministic project-local precedence order, arbitrary workspace folder
  ignoring, ambiguity diagnostics, and explicit highest-priority precedence behavior.
- Documented governed local registry roots and the brownfield boundary with
  `AthenaKnowledgeResolver`.
- AC-to-evidence: AC1 covered by
  `registry evaluates explicit project and athena roots in deterministic priority order` and
  `registry ignores arbitrary workspace folders not declared as governed roots`; AC2 covered by
  `registry diagnoses or resolves duplicate package ids according to policy`; AC3 covered by
  `:kernel:package-runtime` placement and docs; AC4 covered by full `check`, encoding audit,
  `git status --short`, and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/2-1-add-governed-local-package-registry-roots.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/build.gradle.kts`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/LocalPackageRegistry.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageRegistryModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/LocalPackageRegistryTest.kt`
- `settings.gradle.kts`

## Change Log

- 2026-07-22: Story created for M32 Epic 2 after package descriptor contracts reached review.
- 2026-07-22: Implemented governed local package registry roots and duplicate id policy tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package runtime, package model, compiler knowledge package, tests,
  documentation, fixtures, and sprint artifacts.
- Remove dead/stale registry experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
