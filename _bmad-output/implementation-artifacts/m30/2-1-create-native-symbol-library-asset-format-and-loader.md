---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 2.1
epic: 2
title: Create Native Symbol Library Asset Format And Loader
---

# Story 2.1: Create Native Symbol Library Asset Format And Loader

## Status

Done

## Story

As a library maintainer,
I want native Athena symbol assets and a loader,
so that symbols are data-driven and not hard-coded in renderer code.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given a native symbol asset file exists, when the loader runs, then it validates symbol id, version, lifecycle, bounds, primitives, anchors, label slots, variants, and style tokens.
2. Given tests load the symbol library, when no browser runtime is present, then loading and validation still pass.
3. Given runtime assets are inspected, when QET XML is searched, then QET .elmt is not the product runtime asset format.

## Tasks/Subtasks

- [x] Choose and document a v0 data asset format, preferably JSON/YAML plus Kotlin tests. (AC: 1)
- [x] Implement loader/validator at the representation-library boundary. (AC: 1,2)
- [x] Add tests proving QET files are not loaded as runtime assets. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Open question is resolved: use JSON/YAML assets plus Kotlin tests unless codebase constraints prove otherwise.
- The loader validates view-layer assets only; it does not mutate semantic source.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Inspected `:kernel:representation-model` module and confirmed it has no JSON/YAML parser dependency.
- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.NativeRepresentationLibraryLoaderTest"` failing on missing native loader and definition variants support.
- 2026-07-21: Initial GREEN attempt failed on style-token parsing because indexed entries looked for `.id`; fixed parser to allow alternate index key fields.
- 2026-07-21: GREEN confirmed with focused `NativeRepresentationLibraryLoaderTest`.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Verification passed with `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- 2026-07-21: Review confirmed the loader coverage now checks version, primitive id, terminal number, and label anchor fields required by AC-1.
- 2026-07-21: Review cleanup removed temporary test-file residue by deleting created assets in the loader test.
- 2026-07-21: Review verification passed with focused loader test, full module test, and encoding audit.

### Completion Notes

- Added the native `.properties` symbol library v0 format and documented why JSON/YAML is deferred until a parser dependency is chosen.
- Implemented JVM-only native representation library loading and validation without browser runtime, QET runtime, or semantic source mutation.
- Added explicit `.elmt` rejection coverage so QET assets cannot become Athena runtime symbol assets.
- Added loader assertions for version, primitive id, terminal number, and label anchor coverage required by the acceptance criteria.
- Removed temp-file residue from the loader test by deleting created assets after load.
- Completed final polish/purge review; no stale artifacts were removed or retained beyond existing cleanup-ledger item `M30-CL-001`.

## File List

- `_bmad-output/implementation-artifacts/m30/2-1-create-native-symbol-library-asset-format-and-loader.md`
- `_bmad-output/implementation-artifacts/m30/native-symbol-asset-format-v0.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/NativeRepresentationLibraryLoader.kt`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/RepresentationContracts.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/NativeRepresentationLibraryLoaderTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added native symbol asset format, loader, variants contract, and QET runtime rejection test.
- 2026-07-21: Closed review with loader AC coverage and temp-file cleanup.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
