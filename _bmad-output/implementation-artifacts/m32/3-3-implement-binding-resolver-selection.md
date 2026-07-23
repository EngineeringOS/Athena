---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 3.3
epic: 3
title: Implement Binding Resolver Selection
---

# Story 3.3: Implement Binding Resolver Selection

## Status

Review

## Story

As an Athena projection compiler,
I want Binding Resolver to select descriptors from semantic context, package facts, and
Presentation Profile,
so that representation choice is governed, explainable, and swappable.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Previous story: `_bmad-output/implementation-artifacts/m32/3-2-define-binding-manifest-v0.md`
- Epic 2 retrospective:
  `_bmad-output/implementation-artifacts/m32/epic-2-retro-2026-07-22.md`

## Acceptance Criteria

1. Given a semantic subject, projection context, engineering package, Binding Manifest,
   Presentation Profile, active profile, and valid representation packages, when Binding Resolver
   runs, then it selects representation package, descriptor, variant, anchor mapping, label
   binding, and style profile.
2. Given the active Presentation Profile changes, when Binding Resolver reruns, then the same
   semantic subject can resolve to a different appearance without changing `.athena` source.
3. Given Binding Resolver cannot resolve descriptor, anchor, label, package, manifest, or profile,
   when binding runs, then diagnostics name the failed authority and no fallback box is accepted as
   success.
4. Given the resolver produces binding facts, when contracts and docs are reviewed, then it
   preserves semantic identity and does not create truth from package names, profiles, Graphic
   Resource ids, labels, or coordinates.
5. Mandatory Polish/Purge Gate complete with AC-to-evidence mapping.

## Tasks/Subtasks

- [x] Add RED tests for successful Binding Resolver selection of representation package,
  descriptor, variant, anchor mapping, label binding, and style profile. (AC: 1,4)
- [x] Add RED tests proving active Presentation Profile switch changes resolved appearance without
  changing semantic subject/source identity. (AC: 2,4)
- [x] Add RED diagnostic tests for unresolved descriptor, anchor, label slot, representation
  package, manifest, and Presentation Profile authority failures. (AC: 3)
- [x] Implement Binding Resolver v0 models and deterministic resolver behavior in package runtime
  boundary. (AC: 1..4)
- [x] Document Binding Resolver as selection/mapping authority and keep Binding Manifest as
  compatibility bridge only. (AC: 1..4)
- [x] Run focused package-runtime tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 5)

## Dev Notes

- Binding Resolver consumes existing package-platform contracts:
  `EngineeringPackageDescriptor`, `PresentationProfileDescriptor`, `BindingManifest`,
  `RepresentationPackageDescriptor`, and `RepresentationDescriptor`.
- Binding Manifest stays declarative compatibility data. Story 3.3 owns selection behavior.
- Preferred implementation location:
  `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageplatform`.
- Keep `kernel/package-model` contract-only unless a missing transport-safe model must be shared.
- Binding Resolver must preserve `semanticSubjectId` as upstream identity. It must not infer
  engineering truth from package ids, profile ids, descriptor ids, resource ids, labels, or bounds.
- Determinism matters: if multiple compatible candidates exist, ordering must be explicit and
  test-covered.
- Suggested model names:
  - `BindingSubject`
  - `BindingResolutionRequest`
  - `BindingResolution`
  - `BindingResolver`
  - `BindingResolverDiagnostic`
  - `BindingAuthority`
- Suggested diagnostic authorities:
  - `engineering-package`
  - `presentation-profile`
  - `binding-manifest`
  - `representation-package`
  - `descriptor`
  - `anchor`
  - `label-slot`
  - `binding-policy`
- Existing package-runtime patterns from Stories 2.1-2.3 should guide diagnostics and deterministic
  ordering.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Preferred runtime implementation:
  `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageplatform`.
- Preferred tests:
  `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageplatform/BindingResolverSelectionTest.kt`.
- Documentation update:
  `docs/usages/engineering-package-platform.md`.

## Testing Requirements

- Follow TDD: write failing Binding Resolver tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  failed in `:kernel:package-runtime:compileTestKotlin` with unresolved `BindingResolver`,
  `BindingResolutionRequest`, `BindingSubject`, `BindingAuthority`, and result fields.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  passed after adding Binding Resolver runtime models and selection behavior.
- REFACTOR VERIFY: focused `:kernel:package-runtime:test` passed again after import/doc cleanup.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed sequentially.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  passed after text edits.
- PURGE: `git status --short` showed M32 artifacts and no staged/tracked `.tools` addition.

### Completion Notes List

- Added Binding Resolver v0 runtime contract for semantic subject, resolution request, authority
  diagnostics, selected binding facts, and fallback rejection state.
- Implemented deterministic selection from Binding Manifest package order and active Presentation
  Profile compatibility, preserving upstream semantic subject identity.
- Added fail-closed diagnostics for Presentation Profile incompatibility, missing Representation
  Package, missing descriptor, missing anchor, and missing label slot.
- Documented Binding Resolver as selection/mapping authority and Binding Manifest as compatibility
  data only.
- AC evidence:
  - AC1: `BindingResolverSelectionTest.binding resolver selects package descriptor variant
    anchors labels and style`.
  - AC2: `BindingResolverSelectionTest.binding resolver changes appearance when active
    presentation profile changes without source identity change`.
  - AC3: `BindingResolverSelectionTest.binding resolver fails closed with authority diagnostics
    instead of fallback boxes`.
  - AC4: resolver result carries semantic subject id from request and docs state that package,
    profile, descriptor, resource, label, and coordinate data do not create engineering truth.
  - AC5: focused runtime test, full `check`, encoding audit, and workspace purge review recorded.

### File List

- `_bmad-output/implementation-artifacts/m32/3-3-implement-binding-resolver-selection.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolverModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingResolverSelectionTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 3 after Binding Manifest v0.
- 2026-07-22: Implemented Binding Resolver v0 selection, diagnostics, docs, and tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package model, package runtime, docs, tests, fixtures, and sprint
  artifacts.
- Remove dead/stale resolver experiments, hard-coded representation choices, or misleading
  authority claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
