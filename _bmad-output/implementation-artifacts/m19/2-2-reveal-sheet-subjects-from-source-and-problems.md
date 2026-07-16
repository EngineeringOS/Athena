---
baseline_commit: d9716be133ac72af2bf5d505d20fde4d1d5a6c7a
---

# Story 2.2: Reveal sheet subjects from source and Problems

Status: review

## Story

As an engineer,
I want source and Problems selections to reveal the corresponding sheet object,
so that I can navigate from diagnostics back to the sheet without ambiguity.

## Acceptance Criteria

1. Given a source span tied to a canonical subject, when reveal is requested, the active schematic sheet highlights or selects the matching rendered subject.
2. Given a Problem/diagnostic tied to a canonical subject, when reveal is requested, the active schematic sheet highlights or selects the matching rendered subject.
3. Reveal preserves the same canonical subject id and, when projection facts provide it, occurrence, endpoint, anchor, source uri, and source range.
4. Reveal can switch from another supported view to the schematic sheet when the current view does not contain the target subject and the sheet projection does.
5. Reveal fails closed when no governed projection path contains the canonical subject; it must not infer meaning from diagnostic text, DOM labels, CSS classes, or rendered pixel state.
6. Executable frontend tests cover source-to-sheet reveal, Problem/diagnostic-to-sheet reveal, view switching, and no-inference failure behavior.

## Tasks / Subtasks

- [x] Lock source span reveal into canonical sheet subjects (AC: 1, 3, 5, 6)
  - [x] Add or update frontend tests proving a source-range selection resolves to a canonical subject and reveal target.
  - [x] Route source reveal through the existing canonical selection/reveal path instead of a second frontend-owned channel.
  - [x] Preserve source uri/range and projection occurrence facts when they exist.
- [x] Lock Problems/diagnostic reveal into canonical sheet subjects (AC: 2, 3, 5, 6)
  - [x] Add or update tests proving diagnostic payloads with governed subject ids reveal sheet subjects.
  - [x] Keep diagnostic reveal dependent on canonical ids or source spans already published by LSP/projection state.
  - [x] Assert diagnostic text is not parsed for semantic meaning.
- [x] Prove view switching and failure behavior (AC: 4, 5, 6)
  - [x] Add or update tests for `AthenaGraphAdapterService.revealSemanticId` across supported view diagrams.
  - [x] Assert reveal switches to `schematic-sheet` when it contains the subject and the active view does not.
  - [x] Assert reveal returns no target when no sheet/graph/alias path contains the canonical subject.
- [x] Keep M19 bounded (AC: 5)
  - [x] Do not add cabinet preview behavior.
  - [x] Do not choose or hard-code the final protocol/layout engine stack.
  - [x] Do not move semantic authority into frontend-local state.

## Dev Notes

### Current State

- Story 2.1 added `resolveRenderedSelectionTarget` in `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`.
- `resolveSemanticSelectionFromSourceRange` already maps source uri/range to canonical subject identity from governed inspection payloads.
- `AthenaGraphAdapterService.revealSemanticId(semanticId, diagram?)` already checks whether the active diagram contains a semantic id, walks supported views, and can switch active view.
- `AthenaGraphWorkbenchWidget.handleSemanticSelectionChanged(...)` already calls `graphAdapterService.revealSemanticId(...)` when the current diagram does not contain the selected semantic id.
- `AthenaLspEditorBridgeService.syncPublishedDiagnostics(uri)` currently publishes diagnostics into Theia Problems through `ProblemManager.setMarkers(...)`.
- The M19 proof fixture at `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs` contains canonical component, port, connection, and cross-reference subjects for deterministic tests.

### Architectural Guardrails

- Canonical subject identity remains the only reveal currency.
- Source, Problems, inspector, and rendered sheet reveal must round-trip through governed subject ids and projection-published occurrence/alias facts.
- The frontend may carry reveal intent and call adapter services, but it may not resolve imports, symbols, component meaning, or diagnostic text locally.
- Theia remains the only frontend target for this story. Ignore desktop viewer and Kotlin Compose surfaces.
- Cabinet preview remains deferred from M19.

### Project Structure Notes

- Likely update targets:
  - `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
  - `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
  - `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
  - `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`
  - `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`
  - a focused adapter-service or reveal test under `ide/theia-frontend/scripts/`
- Prefer focused tests and small pure helpers. If existing behavior satisfies an AC, add tests rather than reshaping the UI.

### Testing Requirements

- Use the existing Theia frontend test stack: `yarn build` and `node --test scripts/*.test.mjs` for focused tests, then `yarn test` for regression.
- Keep tests deterministic and fixture-driven.
- Reuse `examples/m19/schematic-sheet-proof/ready-sheet.diagram.mjs` when useful.
- After text/doc changes, run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### References

- [Source: `_bmad-output/implementation-artifacts/m19/epics.md` - Epic 2, Story 2.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md` - FR-5]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md` - Theia IDE coherence and boundary notes]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md` - AD-1, AD-4, AD-5, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m19/2-1-round-trip-selection-through-canonical-identity.md` - canonical selection baseline]
- [Source: `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`]
- [Source: `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`]
- [Source: `ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`]
- [Source: `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from M19 Epic 2, PRD FR-5, architecture AD-4, and current Theia reveal/Problems code.
- Red phase: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-semantic-selection-model.test.mjs scripts/athena-graph-adapter-service.test.mjs }` failed because reveal target helpers did not exist; the direct adapter import also pulled Monaco CSS into Node.
- Focused verification: `yarn build; if ($LASTEXITCODE -eq 0) { node --test scripts/athena-semantic-selection-model.test.mjs scripts/athena-m19-sheet-proof.test.mjs }` passed 15 tests.
- Regression verification: `yarn test` in `ide/theia-frontend` passed 57 tests.
- Encoding verification: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Added governed source-range and diagnostic reveal target helpers that materialize canonical subject, source, occurrence, endpoint, anchor, and connection facts only when the projection contains the subject.
- Added diagnostic reveal behavior that accepts canonical ids from diagnostic data or governed source ranges, and explicitly avoids parsing diagnostic message text.
- Tightened graph adapter reveal behavior so unsupported reveal requests restore the original view and return `undefined` instead of pretending the original diagram is a hit.
- Added deterministic frontend tests for source reveal, Problems reveal, no-inference failure, and supported-view reveal iteration.

### File List

- `_bmad-output/implementation-artifacts/m19/2-2-reveal-sheet-subjects-from-source-and-problems.md`
- `_bmad-output/implementation-artifacts/m19/sprint-status.yaml`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`
- `ide/theia-frontend/scripts/athena-semantic-selection-model.test.mjs`

### Change Log

- 2026-07-16: Created story context for source and Problems reveal into canonical schematic sheet subjects.
- 2026-07-16: Implemented canonical source/Problems reveal targets and fail-closed governed view reveal behavior.
