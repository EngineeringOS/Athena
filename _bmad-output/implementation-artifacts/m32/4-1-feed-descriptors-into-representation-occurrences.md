---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 4.1
epic: 4
title: Feed Descriptors Into Representation Occurrences
---

# Story 4.1: Feed Descriptors Into Representation Occurrences

## Status

Review

## Story

As an Athena representation compiler,
I want resolved descriptors to create M30-compatible Representation Occurrences,
so that package-backed symbols use the existing presentation pipeline.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Previous retrospective: `_bmad-output/implementation-artifacts/m32/epic-3-retro-2026-07-22.md`

## Acceptance Criteria

1. Given valid package resolution and binding facts, when Representation Occurrence creation runs,
   then occurrences reference descriptor/resource handles, variant, labels, anchors, bounds, and
   style profile while preserving semantic subject identity.
2. Given existing M30 native definitions, when package-backed definitions are enabled, then
   existing Presentation IR and composition contracts continue to work or fail with structured
   migration diagnostics.
3. Given descriptor validation or binding evidence reports missing descriptor, resource, anchor, or
   label facts, when occurrence creation runs, then it fails closed and does not create a generic
   fallback occurrence.
4. Given the story implementation is complete, when representation model, compiler, fixtures, and
   docs are reviewed, then stale native-only assumptions are removed or ledgered and AC evidence is
   recorded.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for creating a descriptor-backed Representation Occurrence from Binding
  Evidence and Representation Descriptor facts. (AC: 1)
- [x] Add RED tests proving semantic subject identity is preserved and descriptor/resource ids do
  not become engineering truth. (AC: 1,2)
- [x] Add RED tests for fail-closed diagnostics when binding evidence has missing descriptor,
  resource, anchor, or label facts. (AC: 3)
- [x] Implement package-backed occurrence models/mapper at the representation/package runtime
  boundary without changing renderer ownership. (AC: 1..3)
- [x] Document descriptor-backed occurrence creation and migration boundary from native M30
  definitions. (AC: 2,4)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4,5)

## Dev Notes

- Start from CodeGraph review of existing `kernel/representation-model`,
  `kernel/presentation-model`, and M30 representation policy/occurrence contracts.
- Consume Story 3.3 `BindingResolver` and Story 3.4 `BindingEvidencePayload` facts. Do not derive
  package/profile choice from descriptor id, resource id, labels, DOM, CSS, or file names.
- Package-backed occurrences should be bridge facts for existing presentation integration, not a
  renderer-owned geometry database.
- Existing M30 native definitions should remain compatible unless a structured migration
  diagnostic is emitted.
- Preferred implementation locations depend on existing code shape after CodeGraph inspection:
  `kernel/representation-model` for shared occurrence contracts, or `kernel/package-runtime` for
  runtime mapping from binding evidence to occurrence facts.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Keep semantic truth upstream: `.athena` and engineering model remain canonical.
- Keep renderer authority downstream: renderers consume resolved occurrence facts only.
- Avoid modifying Theia until a story explicitly owns renderer integration.

## Testing Requirements

- Follow TDD: write failing occurrence tests before production code.
- Focused command should target the module touched by implementation, most likely
  `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` or
  `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  failed in `:kernel:package-runtime:compileTestKotlin` with unresolved
  `PackageBackedRepresentationOccurrenceFactory` and request/result fields.
- GREEN: focused `:kernel:package-runtime:test` passed after adding the package-backed occurrence
  bridge into the existing M30 `RepresentationBindingCompiler`.
- REFACTOR VERIFY: focused `:kernel:package-runtime:test` passed after API enum/order correction
  and docs.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed sequentially.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  passed after text edits.
- PURGE: `git status --short` showed M32 artifacts and no staged/tracked `.tools` addition.

### Completion Notes List

- Added package-backed Representation Occurrence request/result contracts and factory in
  `:kernel:package-runtime`.
- Added dependency from `:kernel:package-runtime` to `:kernel:representation-model` so package
  evidence can bridge into the existing M30 `RepresentationOccurrence` pipeline.
- Factory converts Binding Evidence plus validated Representation Descriptor facts into an M30
  `RepresentationBindingRequest`, preserving canonical semantic subject identity.
- Fail-closed preflight emits representation diagnostics for missing symbol/descriptor evidence,
  missing anchors, and missing label slots instead of accepting fallback occurrences.
- Documented descriptor-backed occurrence creation and native M30 compatibility boundary.
- AC evidence:
  - AC1: `PackageBackedRepresentationOccurrenceFactoryTest.factory creates M30 compatible
    occurrence from binding evidence and descriptor`.
  - AC2: `PackageBackedRepresentationOccurrenceFactoryTest.factory preserves semantic identity and
    keeps descriptor identifiers as representation identity only`.
  - AC3: `PackageBackedRepresentationOccurrenceFactoryTest.factory fails closed when binding
    evidence or descriptor facts are incomplete`.
  - AC4: docs updated; CodeGraph review found no touched native-only stale artifact requiring a
    cleanup-ledger entry.
  - AC5: focused runtime test, full `check`, encoding audit, and workspace purge review recorded.

### File List

- `_bmad-output/implementation-artifacts/m32/4-1-feed-descriptors-into-representation-occurrences.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/build.gradle.kts`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactory.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceModels.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/PackageBackedRepresentationOccurrenceFactoryTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 4 after Binding Resolver and Binding Evidence foundation.
- 2026-07-22: Implemented descriptor-backed occurrence bridge into M30 Representation Occurrence
  contracts.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package runtime, representation model, presentation model, docs,
  tests, fixtures, and sprint artifacts.
- Remove dead/stale native-only assumptions or misleading renderer-authority claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
