---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 7.1
epic: 7
title: Document Deferred Offline QET Converter Design
---

# Story 7.1: Document Deferred Offline QET Converter Design

## Status

Done

## Story

As a architect,
I want the QET converter boundary recorded correctly,
so that future import work targets Athena Representation IR without polluting runtime or Athena source.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given converter design is read, when data flow is inspected, then it states QET .elmt -> QET AST -> Athena Representation IR candidate.
2. Given design scope is reviewed, when required converter concerns are listed, then primitive normalization, style mapping, terminal orientation, dynamic text, diagnostics, licensing/provenance, and deterministic output are covered.
3. Given forbidden paths are searched, when runtime and Athena source rules are reviewed, then QET runtime dependency and Athena source QET references are explicitly forbidden.

## Tasks/Subtasks

- [x] Write deferred converter design note. (AC: 1,2)
- [x] Document unsupported-feature diagnostics and licensing/provenance handling. (AC: 2)
- [x] Add explicit runtime/source prohibitions. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Do not implement product runtime importer in this story.
- A tiny offline parser spike is allowed only if later approved to validate Representation IR coverage.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Confirmed RED with `node --test ide\theia-frontend\scripts\athena-m30-qet-converter-design.test.mjs`; failed because `_bmad-output/implementation-artifacts/m30/qet-offline-converter-design.md` did not exist.
- 2026-07-21: Ran `node --test ide\theia-frontend\scripts\athena-m30-qet-converter-design.test.mjs`; 2 tests passed.
- 2026-07-21: Ran expanded guard set including M30 sample, smoke wiring, rendering, transparent chrome, SVG bounds, and QET converter design tests; 10 tests passed.
- 2026-07-21: Ran `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`; encoding audit passed.
- 2026-07-21: Final closeout re-ran the current M30 guard set including the QET converter design checks; the offline-only boundary remained intact.

### Completion Notes

- Added the deferred offline QET converter design note with the required `QET .elmt -> QET Element AST -> Athena Representation Definition IR candidate` flow.
- Documented primitive normalization, style mapping, terminal orientation, dynamic text, unsupported-feature diagnostics, licensing/provenance, and deterministic output.
- Added explicit prohibitions against QET runtime loading and `.athena` source references to QET paths or visual primitives.
- Polish/purge review found no temporary parser spike, runtime importer, QET dependency, or retained artifact requiring a new cleanup-ledger entry.

## File List

- `_bmad-output/implementation-artifacts/m30/7-1-document-deferred-offline-qet-converter-design.md`
- `_bmad-output/implementation-artifacts/m30/qet-offline-converter-design.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `ide/theia-frontend/scripts/athena-m30-qet-converter-design.test.mjs`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added deferred offline QET converter design note and guard test.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
