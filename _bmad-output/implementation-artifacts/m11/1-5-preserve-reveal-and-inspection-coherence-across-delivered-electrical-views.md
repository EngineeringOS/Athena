---
baseline_commit: c278a71
---

# Story 1.5: Preserve Reveal And Inspection Coherence Across Delivered Electrical Views

Status: done

## Completion Summary

- Preserved canonical semantic selection across view families by separating `semanticId` from projection-local ids and by normalizing selection through the shared workbench selection model.
- Kept reveal and inspection anchored to canonical semantic ids across source, graph, sheet, and later repeated-reference contexts.
- Avoided frontend-owned alias maps by resolving sheet and graph occurrences back to the same semantic identity first.

## Acceptance Outcome

1. One canonical subject can be revealed and inspected across source, graph, and delivered electrical views.
2. Alternate view entries and repeated occurrences do not create duplicate semantic subjects.

## Verification

- `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"`
- `yarn test` in `ide/theia-frontend`

## Key Files

- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
