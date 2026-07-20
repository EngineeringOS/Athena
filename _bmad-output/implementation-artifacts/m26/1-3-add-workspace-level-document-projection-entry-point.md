---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 1.3: Add Workspace-Level Document Projection Entry Point

Status: done

## Story

As an Athena runtime integrator,
I want document projection to start from the workspace semantic graph or linked project snapshot,
so that M26 does not pretend an active file is the whole engineering project.

## Acceptance Criteria

1. A document projection entry point consumes a project/workspace semantic snapshot or linked/lowered
   project units rather than only the active editor file.
2. The entry point returns a document projection snapshot containing projection identity, policy
   identity, sheet views, occurrence index, reference-fact containers, and diagnostics/proof
   metadata.
3. Continuation and cross-reference containers may be empty until Epic 2 derivation stories populate
   them.
4. Source-file names are not accepted as sheet-view ids or sheet-view roles.
5. The entry point can still project a single-file project for regression compatibility.
6. Tests cover both single-file and multi-file sample snapshots.

## Tasks / Subtasks

- [x] Define the document projection snapshot and entry point contracts (AC: 1, 2, 3)
  - [x] Add model contracts for workspace/project semantic inputs needed by M26 without making
        source files into sheet views.
  - [x] Add a snapshot contract carrying projection identity, policy identity, sheet views,
        occurrence index, empty continuation/cross-reference containers, and proof metadata.
  - [x] Keep the model in `kernel/document-projection-model`; do not create an engine module unless
        needed by existing code structure.
- [x] Implement a minimal built-in projection entry point (AC: 1, 2, 3, 4, 5)
  - [x] Use `BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()`.
  - [x] Derive document projection identity from policy id/version and workspace semantic graph id.
  - [x] Materialize the accepted sheet-view roles from the policy, not from source filenames.
  - [x] Support a single source unit and multiple source units with the same API.
- [x] Add single-file and multi-file tests (AC: 1, 4, 5, 6)
  - [x] Verify single-file projection returns a valid snapshot and sheet views.
  - [x] Verify multi-file projection returns the same policy-owned sheet-view ids/titles regardless
        of source filename order.
  - [x] Verify source filenames are preserved as provenance/proof metadata only and never become
        sheet-view ids or roles.
  - [x] Verify reference-fact containers are present but empty for Story 1.3.

## Dev Notes

- M26 architecture AD-5 requires a workspace-level projection entry point. Avoid active-file-only
  assumptions.
- M26 architecture AD-6 states source files are not sheet views.
- Story 1.3 should not yet derive occurrence membership, continuations, or cross references. Story
  1.4 handles sheet view membership, and Epic 2 handles continuation/cross-reference facts.
- Story 1.2 provides the built-in policy contract and the deterministic role ordering.
- Keep `.athena` as the single source of truth. Document Projection IR is derived output only and is
  not separately authored or persisted.
- Do not introduce new `.athena` syntax. Do not touch ANTLR4, Tree-sitter, compiler syntax, LSP
  syntax, samples, or syntax docs in this story.
- Do not touch `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend scope.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected update files:
  - `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
  - `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt`
  - `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- A new cohesive file such as `DocumentProjectionSnapshotModel.kt` is acceptable if it keeps model
  contracts readable.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-3: workspace-level document projection entry point
  - FR-4: source-file boundaries must not be sheet-view boundaries
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-5: Workspace-Level Projection Entry Point Is Required
  - AD-6: Source Files Are Not Sheet Views
  - AD-7: Occurrence Identity Is Deterministic And Policy-Versioned
- Epics: `_bmad-output/implementation-artifacts/m26/epics.md`
  - Epic 1, Story 1.3
- Previous Story: `_bmad-output/implementation-artifacts/m26/1-2-define-the-built-in-document-projection-policy-contract.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because workspace snapshot and document projection entry point contracts did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after implementing the snapshot and entry point contracts.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `DocumentProjectionWorkspaceSemanticSnapshot` and source unit summary contracts to represent
  workspace/project-level input without treating source files as sheet views.
- Added `DocumentProjectionSnapshot`, empty reference-fact containers, and proof metadata contracts.
- Added `DocumentProjectionEntryPoint.projectWorkspace()` using the built-in policy to produce
  policy-owned sheet views from semantic graph identity.
- Added tests for single-file compatibility, multi-file source order stability, source filename
  non-authority, empty reference containers, and no raw geometry fields on new contracts.

### File List

- `_bmad-output/implementation-artifacts/m26/1-3-add-workspace-level-document-projection-entry-point.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionSnapshotModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 1.3 from M26 Epic 1.
- 2026-07-20: Implemented workspace-level document projection entry point contracts.
- 2026-07-20: Marked Story 1.3 done after full verification.
