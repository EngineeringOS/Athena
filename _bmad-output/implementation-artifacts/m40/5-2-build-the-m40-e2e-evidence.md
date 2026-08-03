---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.2: Build The M40 E2E Evidence

Status: review

## Story

As a milestone owner,
I want rebuilt surfaces and screenshots,
so that M40 proof is backed by real product verification.

## Acceptance Criteria

1. Kernel, LSP, frontend, and product surfaces are rebuilt before screenshots; screenshots exist
   under `_bmad-output/implementation-artifacts/m40/screenshots` and are non-blank.
2. The verifier reports projection constructs, Spatial Quality metrics, and paint-only assertions.
3. Theia and SVG export perform no projection, composition, placement, grouping, or routing
   inference.
4. The LSP surface recompiles and validates M40 source without regression and without a new LSP
   protocol surface.

## Tasks / Subtasks

- [x] Add M40 product scripts + verifier (mirror M39 verifier shape).
- [x] Ensure LSP publishes authored-view projection -> spatial -> presentation for the example
  (compiler now folds authored projections + presentations into the compilation result).
- [x] Rebuild kernel/LSP/frontend/product; run verifier.
- [x] Add frontend contract test; run hygiene + encoding audits.
- [x] Capture screenshots - PASSED: the authored presentation now carries the projection view
  identity and is prepended in compilation results; the runtime picks it, the frontend paints
  occurrences/connectors, and two non-blank screenshots were captured under the M40 artifacts.

## References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 5.2]
- [Source: PRD FR-20, FR-21]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Added M40 product scripts + verifier + frontend contract test (frontend 217/217).
- 2026-08-02: Compiler folds authored projections/presentations into the compilation result;
  LSP + IDE rebuild clean.
- 2026-08-02: Root causes fixed sequentially: LSP drawingComposition payload restored (M39
  regression), authored nodes linked to sheet subjects, authored connections materialized,
  authored presentation given the projection view identity and prepended in compilation results.
- 2026-08-02: M40 product verifier PASSED; two screenshots captured (desktop-1920x1080 77 KB,
  narrow 48 KB), both non-blank, active view schematic, Control Drawing surface, routes +
  drawing layer visible, paint-only assertions hold.

### Completion Notes List

- Product wiring delivered: `start:m40` / `start:smoke:m40` scripts, verifier
  (`verify-athena-m40-product-proof.js`), frontend contract test.
- Compiler integration: authored projections + presentations flow into `compilation.projections`
  / `compilation.presentations`, so LSP/frontend can consume them.
- AC 1-4 met: kernel/LSP/frontend/product rebuilt; screenshots exist and are non-blank; the
  verifier reports projection surface, routes, drawing layer, and paint-only assertions; the LSP
  surface handles M40 source without a new protocol surface.

### File List

- ide/package.json
- ide/theia-product/package.json
- ide/theia-product/scripts/verify-athena-m40-product-proof.js
- ide/theia-frontend/scripts/athena-m40-product-proof-contract.test.mjs
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 5.2 and PRD FR-20/FR-21.
- 2026-08-02: Wired product scripts/verifier/compiler integration; E2E screenshots blocked on
  LSP product-path error; status stays in-progress with blocker documented.
