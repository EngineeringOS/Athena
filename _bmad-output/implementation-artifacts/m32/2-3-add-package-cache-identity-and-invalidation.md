---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 2.3
epic: 2
title: Add Package Cache Identity And Invalidation
---

# Story 2.3: Add Package Cache Identity And Invalidation

## Status

Review

## Story

As an Athena runtime maintainer,
I want package cache identity tied to descriptor, resource, registry, and policy content,
so that package changes cannot leave stale presentation artifacts.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Previous story: `_bmad-output/implementation-artifacts/m32/2-2-implement-package-resolver-and-resolution-facts.md`

## Acceptance Criteria

1. Given a resolved package, when cache identity is computed, then it includes package id, version,
   descriptor content identity, resource identity, registry root, binding policy identity, and active
   profile.
2. Given descriptor, resource, policy, or profile input changes, when projection reruns, then cache
   identity changes and stale package artifacts are not reused.
3. Given identical package resolution and policy inputs, when cache identity is computed repeatedly,
   then the identity is deterministic.
4. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED tests for cache identity contents from resolved package, descriptor digest, resource
  digest, registry root, binding policy identity, and active profile. (AC: 1)
- [x] Add RED tests proving descriptor/resource/policy/profile changes alter cache identity. (AC: 2)
- [x] Add RED tests proving identical inputs produce deterministic cache identity. (AC: 3)
- [x] Implement package cache identity input, identity value, and calculator in
  `:kernel:package-runtime`. (AC: 1..3)
- [x] Document cache identity and invalidation boundary. (AC: 1..3)
- [x] Run focused package-runtime tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4)

## Dev Notes

- Story 2.3 builds on `ResolvedPackageFact` from Story 2.2.
- Cache identity is not a package resolver and not renderer state. It is a deterministic content and
  policy key that downstream projection/runtime can compare.
- Do not implement a persistent cache store, file watcher, renderer cache, Binding Resolver, or
  profile UI in this story.
- Use deterministic string assembly and hashing from standard JDK APIs; do not add dependencies.
- Suggested diagnostic/evidence language: cache identity changes when descriptor content identity,
  resource identity, registry root, binding policy identity, or active profile changes.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Keep implementation under `kernel/package-runtime/src/main/kotlin/.../packageruntime`.
- Prefer cohesive files such as `PackageCacheIdentity.kt` and `PackageCacheIdentityTest.kt`.

## Testing Requirements

- Follow TDD: write failing cache identity tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`.
- Full regression command after story completion: `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` failed in
  `:kernel:package-runtime:compileTestKotlin` with unresolved `PackageCacheIdentityCalculator`,
  `PackageCacheIdentityInput`, and `PackageResourceIdentity`.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed after
  adding deterministic cache identity models and calculator.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed after cache identity
  documentation and implementation.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added deterministic package cache identity inputs, resource identity facts, identity value, and
  SHA-256 calculator in `:kernel:package-runtime`.
- Cache identity includes package id, kind, version, descriptor path, descriptor content identity,
  resource identities, selected registry root, binding policy identity, and active profile.
- Tests prove descriptor, resource, policy, and profile changes alter the digest, while identical
  inputs produce stable identity.
- Documented cache identity as a content/policy key, not a persistent cache store or renderer cache.
- AC-to-evidence: AC1 covered by
  `cache identity includes package descriptor resource registry policy and profile inputs`; AC2
  covered by `cache identity changes when descriptor resource policy or profile changes`; AC3
  covered by `cache identity is deterministic for identical inputs`; AC4 covered by full `check`,
  encoding audit, `git status --short`, and no new cleanup-ledger entry.

### File List

- `_bmad-output/implementation-artifacts/m32/2-3-add-package-cache-identity-and-invalidation.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageCacheIdentity.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageCacheIdentityTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 2 after package resolver facts.
- 2026-07-22: Implemented package cache identity and invalidation tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package runtime, package model, compiler knowledge package, tests,
  documentation, fixtures, and sprint artifacts.
- Remove dead/stale cache experiments, stale docs, duplicate fixtures, or misleading authority
  claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
