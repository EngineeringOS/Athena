---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 1.1: Define Document Projection Model Contracts

Status: done

## Story

As an Athena platform engineer,
I want explicit Document Projection IR contracts,
so that sheet views, document occurrences, locations, and occurrence indexing have a governed model
before renderer integration.

## Acceptance Criteria

1. The M26 document projection model package defines contracts for document projection identity,
   sheet view identity, sheet view role, logical zone, document occurrence, document location, and
   occurrence index entry.
2. The contracts contain canonical subject identity and projection provenance fields where needed.
3. The contracts contain no raw `x`, `y`, `width`, or `height` geometry fields.
4. Contract names use document projection, sheet view, document occurrence, and document location
   terminology instead of document-authority terminology.
5. Model-level tests verify deterministic identity serialization for representative component,
   terminal, route, and label occurrences.

## Tasks / Subtasks

- [x] Create the document projection model module and marker (AC: 1, 4)
  - [x] Add `:kernel:document-projection-model` to Gradle settings without re-enabling deprecated frontend modules.
  - [x] Add module build configuration consistent with existing small kernel model modules.
  - [x] Add a marker class for bootstrap/documentation flows.
- [x] Define core document projection identity and view contracts (AC: 1, 2, 3, 4)
  - [x] Add value classes for document projection identity, policy identity/version, sheet view identity, logical zone id, and document occurrence id.
  - [x] Add role enums for sheet view role, artifact kind, occurrence role, and occurrence detail role.
  - [x] Add document source range/provenance contracts.
- [x] Define document occurrence, location, and occurrence index contracts (AC: 1, 2, 3, 4)
  - [x] Add `DocumentLocation`, `DocumentOccurrence`, `DocumentOccurrenceIndexEntry`, and `DocumentOccurrenceIndex`.
  - [x] Ensure canonical subject identity is carried by each occurrence/index entry.
  - [x] Ensure the model uses logical zones/locations only, never raw geometry.
- [x] Add deterministic contract tests (AC: 3, 5)
  - [x] Verify component, terminal, route, and label occurrence identity serialization.
  - [x] Verify deterministic index ordering and lookup by canonical subject.
  - [x] Verify contracts expose no raw geometry field names.

## Dev Notes

- Scope is model contracts only. Do not implement `ContinuationFact` or `CrossReferenceFact` here;
  Stories 2.1 and 2.2 introduce those when the facts are first derived.
- Follow existing small model module style:
  - `kernel/layout-model`
  - `kernel/routing-model`
  - `kernel/representation-model`
  - `kernel/projection-model`
- Use package naming consistent with kernel modules; keep the file cluster cohesive instead of
  splitting every tiny DTO into its own file.
- `StableSemanticIdentity` lives in `kernel:engineering-model` under
  `com.engineeringood.athena.ir`.
- M26 architectural boundary:
  - Document Projection IR owns identity, sheet-view topology, logical location, occurrence
    membership, and occurrence indexing.
  - Presentation IR owns paint-ready coordinates and rendering primitives.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend scope.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected new module: `kernel/document-projection-model`.
- Expected primary source package: `com.engineeringood.athena.document`.
- Expected tests: `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document`.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m26/epics.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase before contracts existed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after implementation.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed during review hardening because expected strings were hand-counted against the new delimiter-safe identity encoding.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after replacing brittle string expectations and adding integrity guards.
- `.\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test` passed after updating stale electrical plugin expectations.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added the new `kernel:document-projection-model` module with core M26 document projection identity,
  sheet view, logical zone, occurrence, location, and occurrence index contracts.
- Kept Story 1.1 scope limited to core occurrence/index contracts; continuation and cross-reference
  facts remain for Epic 2.
- Added contract tests for deterministic component, terminal, route, and label occurrence identity,
  canonical occurrence ordering, subject lookup, and no raw geometry authority fields.
- Hardened review findings by making identity serialization delimiter-safe, validating occurrence
  ids against their identity recipe, constraining occurrence/detail role pairs, rejecting duplicate
  or mixed-projection occurrence indexes, and extending no-geometry reflection coverage to all new
  contract types.
- Updated stale plugin-host test expectations to match the current electrical plugin contract
  exposing `PRESENTATION_PACKS`, `schematic`, and `documentation`; this was a pre-existing
  regression-suite expectation drift required for full `test` to pass.

### Senior Developer Review (AI)

Outcome: Changes addressed.

Findings triaged and resolved:

- Identity strings could be delimiter-ambiguous. Resolved with length-prefixed stable key segments.
- `DocumentOccurrence` could accept an id inconsistent with its fields. Resolved with identity
  recipe validation.
- Occurrence indexes could be constructed non-canonically or with duplicate/mixed projection
  entries. Resolved with a private constructor, canonical factory validation, and deterministic
  tuple sorting.
- Occurrence/detail role combinations were not guarded. Resolved with role compatibility checks.
- No-raw-geometry reflection coverage omitted some newly added contracts. Resolved by covering all
  new model contracts.
- Deprecated frontend comments moved in `settings.gradle.kts`. Resolved by restoring the prior
  comment placement while keeping only the new module include.

Deferred review note:

- Validating a `DocumentLocation` zone against a full `SheetView` requires sheet-view membership
  context and is deferred to Story 1.4, where sheet views and occurrence membership are first
  materialized together.

### File List

- `_bmad-output/implementation-artifacts/m26/1-1-define-document-projection-model-contracts.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `settings.gradle.kts`
- `kernel/document-projection-model/build.gradle.kts`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModelMarker.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt`

## Change Log

- 2026-07-20: Implemented Story 1.1 document projection model contracts and verification.
- 2026-07-20: Addressed code review findings and marked Story 1.1 done after full verification.
