# M28 Cleanup Ledger

Date: 2026-07-21

## Removed Or Updated Stale Artifacts

| Artifact | Action | Reason |
|---|---|---|
| M28 usage doc | Updated | Added nested anatomy, semantic relationship authoring, product-path smoke, canonical semantic persistence, and verification commands. |
| Backend relationship source-edit path | Updated | Added validation before serializing accepted electrical relationships so invalid accepts cannot mutate `.athena`. |
| Product smoke scripts | Added | M28 now has explicit `start:m28` and `start:smoke:m28` product entry points. |
| Structured assertion record | Added | Coverage is mapped by seam so future failures do not require visual guessing. |

## Intentionally Retained

| Artifact | Owner | Reason | Target Milestone |
|---|---|---|---|
| Top-level `port Device.port { ... }` syntax | Language/compiler | Legacy compatibility while new M28 source uses nested ports. | Migration/removal decision after M29 or dedicated cleanup milestone. |
| `ConnectPortsIntent` compatibility contract | Authoring/LSP | Existing pre-M28 call sites still use it; tests prove it lifts into `SemanticRelationshipIntent`. | Retire when Semantic Interaction IR owns relationship commands. |
| Guided connection frontend model tests | Frontend | They cover legacy graph command behavior and prevent unplanned breakage while M28 introduces the generic path. | M29 interaction model refactor. |
| M27 visual smoke proof script | Theia product | Still guards the sheet/frame/viewBox regressions that caused visible density failures. | Keep until equivalent shared visual contract replaces milestone-specific smoke. |

## No-Delete Decisions

- Did not delete unrelated dirty files from older milestones because the worktree contains user and
  prior-session changes outside M28 scope.
- Did not remove generated Tree-sitter artifacts because M28 changed grammar and the generated
  parser/node types are part of the checked-in editor syntax surface.
- Did not remove `connect-ports` tests because the compatibility policy is still active.

## Cleanup Rule For M29

M29 should start by deciding whether interaction compatibility tests are:

```text
legacy retained
  or
migrated behind Interaction IR
  or
removed with source-compatible replacements
```

Do not leave both old graph-command interaction and new interaction IR paths unowned.
