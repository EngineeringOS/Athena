---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 1.1: Establish Projection View And Sheet Authority

Status: in-progress

## Story

As a compiler maintainer,
I want Projection Reality to own view and sheet identity, membership, and source trace,
so that every view-specific document has one authoritative owner.

## Acceptance Criteria

1. Every projection document carries exactly one view identity and one compiler authority
   (`ProjectionReality.authority = "projection compiler"`).
2. Every sheet belongs to exactly one view (sheet identity is view-scoped and matches the
   document view id) and carries a stable sheet identity.
3. A view with no sheets fails with a plain diagnostic.
4. An empty sheet (no subjects) fails with a plain diagnostic.
5. Duplicate sheet identities fail with a named diagnostic.
6. Sheets preserve source trace: sheet subjects carry engineering semantic identity and the
   sheet id recipe is stable and repeatable for the same view and order.
7. No generic graph framework, universal `Fact` base class, compatibility shim, empty wrapper
   model, milestone-named class, `V0`/`V1`, vague `Evidence`, or `ProfessionalDrawing` naming is
   introduced.

## Tasks / Subtasks

- [ ] Establish failing projection model tests first (AC: 1-7)
  - [ ] Add a test proving duplicate sheet identities produce a named diagnostic.
  - [ ] Add a test proving an empty sheet (no subjects) produces a plain diagnostic.
  - [ ] Add a test proving a sheet whose id is not scoped to the document view produces a
        named diagnostic.
  - [ ] Add a test proving the same view and sheet declaration compile to the same stable
        sheet identity and reading order on every run.
  - [ ] Add a naming guard test covering new M40 projection-model types.
- [ ] Enforce view and sheet ownership invariants (AC: 1-3)
  - [ ] Keep `ProjectionDocument.view` as the single view identity per document.
  - [ ] Keep the existing missing-view and missing-sheet diagnostics.
  - [ ] Add duplicate-sheet-identity validation.
  - [ ] Add empty-sheet validation (sheet with no subjects fails).
  - [ ] Add sheet view-membership validation (sheet id must be scoped to the document view).
- [ ] Preserve stable identity and source trace semantics (AC: 2, 6)
  - [ ] Keep `ProjectionSheetId` view-scoped ("view/sheet/order" recipe).
  - [ ] Keep sheet subjects carrying canonical engineering `StableSemanticIdentity`.
  - [ ] Do not add coordinates, anchors, lanes, routes, stroke, labels, or paint order.
- [ ] Clean stale naming discovered during implementation (AC: 7)
  - [ ] Rename/delete incompatible stale production code directly; no aliases.
  - [ ] Update tests/docs only where the current M40 model requires it.
- [ ] Verify and update tracking (AC: 1-7)
  - [ ] Run `:kernel:projection-model:test` sequentially.
  - [ ] Run `:kernel:compiler:test` and `:kernel:runtime:test` sequentially.
  - [ ] Run full `gradlew test` if shared model APIs are affected.
  - [ ] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [ ] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story starts Epic 1. It hardens the existing M39 projection model's view/sheet ownership and
adds the missing sheet diagnostics. It does not implement regions (Story 1.2), reading
order/selection (Story 1.3), constructs (Epic 2), transformation changes (Epic 3), or any
renderer/runtime surface change beyond what the model tests require.

### Current Code Intelligence

Current roots (M39):

- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
  - `ProjectionDocument(view: ViewDefinition, nodes, connections, resolvedSubjects, sheets,
    notationPack, crossReferences)` - one `view` per document.
- `ProjectionSheets.kt`
  - `ProjectionSheet(sheetId, displayName, order, previousSheetId, nextSheetId, subjects,
    policyEvidence, publication, composition)`.
  - `ProjectionSheetPublication.fromProjectionState` derives the view id from the sheet id:
    `sheetId.value.substringBefore("/sheet/")` - the existing view-scoping recipe.
- `ProjectionReality.kt`
  - `authority = "projection compiler"`, `ownedFacts` = view, sheet, occurrence, projection
    group, reading order.
  - `validate` currently checks: blank view id, empty sheets, blank sheet ids, blank occurrence
    source ids, unique sheet orders.
- `ProjectionElements.kt` - `ProjectionNode`, `ProjectionConnection` anchored to
  `StableSemanticIdentity`.
- `ProjectionIdentifiers.kt` - `ProjectionNodeId`, `ProjectionConnectionId`, `ProjectionSheetId`
  (view-scoped by convention).

Existing tests:

- `ProjectionRealityTest.kt` - authority/identity declarations + missing-fact diagnostics.
- `ProjectionModelContractTest.kt` - sheet publication/identity contracts.

The M39 `EngineeringToProjectionTransformation` already emits view-scoped sheet ids
(`"${view.id}/sheet/01-main"`) and non-empty subjects, so the new validations must not break it.

Use CodeGraph before editing. Prefer extending the existing validation in
`ProjectionReality.validate` over adding a parallel validator.

### Architecture Requirements

- Projection Reality is the authoritative owner of engineering views (AD-9, FR-1).
- A sheet belongs to exactly one view (FR-2); membership is structural: the sheet id is
  view-scoped and must match the document view id.
- Projection must not own coordinates, anchors, lanes, routes, stroke, labels, or paint order
  (AD-3, FR-15).
- Diagnostics must be plain and product-named.
- New names must be short and human-readable; no milestone names or `V0`/`V1`.

### Suggested Product Names

Allowed:

- `ProjectionViewAuthority` (test name only, if used)
- Diagnostics: `duplicate sheet identity`, `empty sheet`, `missing sheet view membership`

Avoid:

- `M40ProjectionSheet...`, `ProjectionEvidence...`, `FactBase`, `RealityGraphNode`,
  `V0` / `V1`, `Compatibility...`

### Testing Requirements

Use TDD. Write failing tests before production code.

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
```

Run broader checks if shared model APIs affect runtime/LSP:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/prd.md` - FR-1, FR-2]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-02-m40/ARCHITECTURE-SPINE.md` - AD-9, AD-12, AD-3]
- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - M40-E1 Story 1.1]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after M40 PRD finalized, architecture spine reviewed clean, and readiness report
  written (2026-08-02).

### Completion Notes

(pending development)

### File List

(pending development)

## Change Log

- 2026-08-02: Created story from final M40 PRD, architecture spine, and M40 epic breakdown;
  marked ready-for-dev.
