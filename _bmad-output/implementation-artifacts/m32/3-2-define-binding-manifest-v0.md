---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 3.2
epic: 3
title: Define Binding Manifest V0
---

# Story 3.2: Define Binding Manifest V0

## Status

Review

## Story

As an Athena package integrator,
I want a manifest that links engineering packages, presentation profiles, and representation
package choices,
so that package internals stay separate while compatible profiles remain discoverable.

## Acceptance Criteria

1. Given a Binding Manifest, when it is validated, then it carries engineering package id/version
   range, concept identity, default representation package, alternative representation packages,
   compatible Presentation Profile tags, policy tags, and provenance.
2. Given a manifest embeds representation geometry, Graphic Resource internals, semantic compiler
   rules, or source mutation behavior, when validation runs, then it fails with an
   authority-boundary diagnostic.
3. Given the story implementation is complete, when manifest contracts, examples, and docs are
   reviewed, then stale coupling assumptions are removed or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for a valid Binding Manifest v0. (AC: 1)
- [x] Add RED validation tests for geometry, Graphic Resource internals, compiler behavior, and
  source mutation authority leaks. (AC: 2)
- [x] Implement Binding Manifest v0 model and validator in package platform contracts. (AC: 1,2)
- [x] Document Binding Manifest as compatibility bridge, not geometry or semantic authority. (AC:
  1,2)
- [x] Run focused package-model tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1,2)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Binding Manifest bridges Engineering Package, Presentation Profile, and Representation Package
  compatibility. It does not choose a descriptor at runtime; Story 3.3 owns Binding Resolver
  selection.
- Do not embed representation primitives, Graphic Resource internals, compiler rules, or `.athena`
  source mutation behavior.
- Preferred implementation location:
  `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform`.
- Suggested files: `BindingManifestModels.kt`, `BindingManifestValidation.kt`,
  `BindingManifestContractTest.kt`.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`.
- Full regression command: `.\gradlew.bat --no-daemon --console=plain check`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  failed with unresolved Binding Manifest symbols before implementation.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-model:test`
  passed after adding `BindingManifestModels.kt` and `BindingManifestValidation.kt`.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed sequentially.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  passed.
- PURGE: `git status --short` showed M32 artifacts and no staged/tracked `.tools` addition;
  local `.tools` remains untouched and excluded.

### Completion Notes List

- Added Binding Manifest v0 contracts for manifest id, engineering package id/version range,
  concept id, default/alternative representation packages, compatible Presentation Profile tags,
  policy tags, and provenance.
- Added authority-boundary validation that rejects representation geometry, Graphic Resource
  internals, compiler behavior, and source mutation leaks.
- Documented Binding Manifest as a compatibility bridge only; Story 3.3 remains responsible for
  resolver selection behavior.
- AC evidence:
  - AC1: `BindingManifestContractTest.binding manifest links engineering package concept profiles
    and representation packages`.
  - AC2: `BindingManifestContractTest.binding manifest validation rejects geometry resource
    compiler and mutation authority leaks`.
  - AC3: package platform docs updated and purge review recorded.
  - AC4: focused package-model test, full `check`, encoding audit, and workspace purge review
    recorded above.

### File List

- `_bmad-output/implementation-artifacts/m32/3-2-define-binding-manifest-v0.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/BindingManifestModels.kt`
- `kernel/package-model/src/main/kotlin/com/engineeringood/athena/packageplatform/BindingManifestValidation.kt`
- `kernel/package-model/src/test/kotlin/com/engineeringood/athena/packageplatform/BindingManifestContractTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 3 after Presentation Profile v0.
- 2026-07-22: Implemented Binding Manifest v0 contracts, validation, documentation, and
  verification evidence.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, package runtime, docs, tests, fixtures, and sprint
  artifacts.
- Remove dead/stale manifest experiments or misleading authority claims.
- Ledger retained stale items in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`.
- Record AC-to-evidence mapping before moving the story beyond `review`.
