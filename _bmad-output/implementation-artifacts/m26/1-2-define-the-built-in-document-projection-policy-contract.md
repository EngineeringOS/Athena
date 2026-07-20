---
baseline_commit: f407e9fc48ec28b40b909a326af28993d3edb3c2
---

# Story 1.2: Define The Built-In Document Projection Policy Contract

Status: done

## Story

As an Athena platform engineer,
I want a compiler/runtime-owned `athena-document-projection-v0` policy contract,
so that document-view organization is governed upstream and can be versioned deterministically.

## Acceptance Criteria

1. The document projection model exposes a policy contract for policy id, policy version or
   deterministic policy hash, supported sheet-view roles, supported artifact kinds, and occurrence
   identity recipe metadata.
2. The built-in policy assigns the initial schematic sheet-view roles for `Power Distribution`,
   `Control And PLC Logic`, and `Field Wiring And Terminal Transition`.
3. The policy contract documents that M26 supports schematic sheet views only while reserving
   terminal report as a future artifact kind.
4. The story introduces no authored `.athena` syntax and does not modify ANTLR4, Tree-sitter,
   compiler syntax, LSP syntax, samples, or syntax documentation.
5. Tests verify the same policy input produces the same policy identity and view-role ordering.

## Tasks / Subtasks

- [x] Define the policy contract in the document projection model (AC: 1, 3)
  - [x] Add a `DocumentProjectionPolicy` contract with id, version, deterministic identity/hash,
        supported artifact kinds, supported sheet-view roles, and occurrence identity recipe fields.
  - [x] Keep the contract in `kernel/document-projection-model` using the existing
        `com.engineeringood.athena.document` package.
  - [x] Keep terminology document-projection-first; avoid folio/page-authority naming.
- [x] Define the built-in `athena-document-projection-v0` policy instance (AC: 1, 2, 3)
  - [x] Expose the policy id as `athena-document-projection-v0`.
  - [x] Expose the initial view roles in deterministic order: power distribution, control and PLC
        logic, field wiring and terminal transition.
  - [x] Expose schematic sheet view as supported and terminal report as reserved/deferred.
- [x] Add policy determinism and boundary tests (AC: 1, 3, 4, 5)
  - [x] Verify two independently created built-in policies produce the same deterministic identity.
  - [x] Verify view-role ordering and display titles remain stable.
  - [x] Verify supported versus reserved artifact kinds are explicit.
  - [x] Verify no new `.athena` grammar, Tree-sitter, compiler syntax, LSP syntax, sample source, or
        syntax documentation files are changed by this story.
- [x] Preserve Story 1.1 contract hardening (AC: 1, 5)
  - [x] Reuse delimiter-safe stable identity behavior rather than concatenating raw ids directly.
  - [x] Do not weaken `DocumentOccurrence` identity recipe validation or canonical index validation.

## Dev Notes

- This is still a model-contract story, not the document projection engine. Story 1.3 creates the
  workspace-level entry point; Story 1.4 materializes sheet views and occurrence membership.
- M26 architecture AD-2: Document Projection Policy owns view organization, view roles, occurrence
  membership, continuation facts, cross-reference facts, and navigation topology.
- M26 architecture AD-7: occurrence identity is deterministic and policy-versioned. The policy
  identity/hash must be stable for the same inputs.
- M26 architecture AD-12: no new source syntax by default. Do not touch `.g4`, Tree-sitter grammar,
  syntax fixtures, syntax docs, or `.athena` sample files in this story.
- M26 architecture AD-3: Document Projection IR owns topology, not geometry. Do not add raw `x`,
  `y`, `width`, or `height` fields.
- M26 frontend scope: Theia is the only future frontend proof surface, but Story 1.2 should not touch
  Theia, `apps:desktop-viewer`, `ui:compose-workbench`, or deprecated KMP frontend scope.
- Story 1.1 created `kernel/document-projection-model` and established:
  - delimiter-safe stable keys,
  - occurrence id recipe validation,
  - occurrence/detail role compatibility checks,
  - canonical occurrence index validation,
  - no raw geometry authority fields in the model contracts.
- The plugin-host expectation update in Story 1.1 was a pre-existing regression alignment needed for
  full `test`; do not expand plugin scope here.
- Verification must run sequentially on Windows.

### Project Structure Notes

- Expected update files:
  - `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
  - `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`
- Do not add a new module unless the existing model file becomes too large or mixed-responsibility.
  The current file remains an appropriate cohesive model contract cluster.
- Do not update `settings.gradle.kts` in this story unless a new module is actually required.

### References

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m26/prd.md`
  - FR-3: Document Projection IR and built-in policy
  - FR-4: deterministic sheet-view materialization policy
  - MVP Scope: `athena-document-projection-v0`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m26/ARCHITECTURE-SPINE.md`
  - AD-2: Document Projection Policy Owns View Organization
  - AD-3: Document Projection IR Owns Topology, Not Geometry
  - AD-7: Occurrence Identity Is Deterministic And Policy-Versioned
  - AD-12: No New Source Syntax By Default
- Epics: `_bmad-output/implementation-artifacts/m26/epics.md`
  - Epic 1, Story 1.2
- Previous Story: `_bmad-output/implementation-artifacts/m26/1-1-define-document-projection-model-contracts.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` failed in red phase because `BuiltInDocumentProjectionPolicies` and related policy contract types did not exist.
- `.\gradlew.bat --no-daemon --console=plain :kernel:document-projection-model:test` passed after implementing the policy model contract.
- `.\gradlew.bat --no-daemon --console=plain test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added `DocumentProjectionPolicy`, deterministic policy identity, artifact availability,
  supported sheet-view role definitions, artifact support declarations, and occurrence identity
  recipe metadata.
- Added `BuiltInDocumentProjectionPolicies.athenaDocumentProjectionV0()` with the three accepted M26
  schematic sheet-view roles and terminal report reserved as a future artifact kind.
- Split policy contracts into `DocumentProjectionPolicyModel.kt` to keep the base occurrence model
  readable and within the project file-organization heuristic.
- Confirmed Story 1.2 did not add or document new `.athena` syntax.

### File List

- `_bmad-output/implementation-artifacts/m26/1-2-define-the-built-in-document-projection-policy-contract.md`
- `_bmad-output/implementation-artifacts/m26/sprint-status.yaml`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionModel.kt`
- `kernel/document-projection-model/src/main/kotlin/com/engineeringood/athena/document/DocumentProjectionPolicyModel.kt`
- `kernel/document-projection-model/src/test/kotlin/com/engineeringood/athena/document/DocumentProjectionModelContractTest.kt`

## Change Log

- 2026-07-20: Created Story 1.2 from M26 Epic 1 with Story 1.1 review learnings.
- 2026-07-20: Implemented built-in document projection policy contract and verification.
