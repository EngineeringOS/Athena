---
baseline_commit: 61fa8d7
---

# Story 3.2: Surface Reasoning Actions In Semantic Inspection And Review Panels

Status: done

## Outcome

Athena semantic panels now expose direct reasoning actions where governed evidence already lives.

## Proof

- `Semantic Inspection` can request diagnostic explanations
- `Semantic SCM` can request impact summaries and next-check proposals
- the returned proposals stay anchored to selected semantic ids, review context, and stored evidence

## Key Files

- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`
- `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
- `ide/theia-frontend/src/browser/style/index.css`

## Verification

- `yarn --cwd ide workspace @engineeringood/athena-theia-frontend build`
- `yarn --cwd ide workspace @engineeringood/athena-theia-product build`

## Change Log

- 2026-07-12: Completed Story 3.2 with additive reasoning actions in the existing semantic panels.
