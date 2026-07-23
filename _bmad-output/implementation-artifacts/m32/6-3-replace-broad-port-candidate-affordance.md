---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 6.3
epic: 6
title: Replace Broad Port Candidate Affordance
---

# Story 6.3: Replace Broad Port Candidate Affordance

## Status

Review

## Story

As an Athena graphical author,
I want relationship candidates derived from registry evidence,
so that Graphical View does not highlight targets by `port:` prefix or broad node kind.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/6-2-align-non-theia-relationship-mutation-surfaces.md`

## Acceptance Criteria

1. Given a selected source terminal, when Graphical View requests candidates, then candidate and
   rejected target evidence comes from semantic capability/compatibility payloads before preview.
2. Given frontend relationship UX is inspected, when scans and tests run, then no
   `semanticId.startsWith('port:')` or equivalent broad node-kind gate remains as the candidate
   authority.
3. Given the story implementation is complete, when interaction model, LSP payloads, frontend UX,
   tests, docs, cleanup ledger, and sprint artifacts are reviewed, then stale broad-affordance code
   is removed or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Run CodeGraph and fixed-string audit for Graphical View candidate discovery, `port:` gates,
  semantic relationship discovery evidence, and frontend target highlighting. (AC: 1,2)
- [x] Add RED tests proving candidate affordance uses compatibility evidence rather than
  `semanticId.startsWith('port:')` or broad node kind. (AC: 1,2)
- [x] Implement candidate-evidence selection at the owning frontend/protocol boundary. (AC: 1,2)
- [x] Update docs and cleanup ledger/action status for removed or retained broad-affordance paths.
  (AC: 3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Use CodeGraph before grep/read on source files that own Graphical View candidate behavior.
- Do not make Theia infer relationship compatibility from semantic id prefixes, CSS classes,
  node kind, visible label text, or DOM structure.
- Prefer existing M29/M31 interaction/authoring payload contracts over adding a new frontend-only
  command contract.
- If a compatibility payload already exists, wire to it and add proof that broad fallback is absent.
- Preserve M32 sample smoke/density proof.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Previous Story Intelligence

- Story 6.2 retained `AthenaConnectPortsCommand` only as
  `legacy-connect-ports-runtime-command-v1` runtime compatibility.
- Product/Theia relationship authoring must remain `SemanticRelationshipIntent` based.
- If a compatibility path is retained, it must be explicitly versioned and ledgered; otherwise
  remove stale broad logic.
- Desktop viewer is not an included Gradle module in this checkout; frontend proof should target
  included scripts/tests or source scans unless the module is restored.

## Testing Requirements

- Follow TDD: write failing candidate-affordance tests before implementation.
- Focused commands should target the Theia/frontend script test or LSP/runtime module touched by
  implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- CodeGraph exploration:
  `athena graph workbench candidate target semanticId startsWith port relationship candidate
  affordance supportsCreateSemanticRelationshipIntent canAcceptConnection target highlighting`.
- CodeGraph file read:
  `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx` around
  `isConnectablePortNode` and `isRelationshipCandidateNode`.
- RED:
  `node --test scripts\athena-m31-governed-relationship-preview.test.mjs` failed because
  `relationshipCandidateEvidence` was missing and the relationship candidate path still used the
  broad `port:` gate.
- GREEN:
  `node --test scripts\athena-m31-governed-relationship-preview.test.mjs` passed after replacing
  candidate authority with semantic inspection compatibility evidence.
- Frontend regression:
  `yarn test` in `ide/theia-frontend` passed with 191 tests.
- Fixed-string scans:
  `rg -n -F "isConnectablePortNode" ...`, `rg -n -F "relationshipCandidateEvidence" ...`,
  `rg -n -F "startsWith('port:')" ...`, and `rg -n -F 'startsWith("port:")' ...`.
- Scan result:
  relationship candidate methods no longer contain `startsWith('port:')`; the remaining
  single-quote hit is `resolveCreatedEntitySemanticId`, which is create-entity reveal preference,
  not relationship candidate authority.
- Full regression:
  `.\gradlew.bat --no-daemon --console=plain check` passed.
- Encoding:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.

### Completion Notes List

- Replaced Graphical View broad relationship candidate authority with
  `relationshipCandidateEvidence()` based on existing LSP semantic inspection port facts.
- Candidate evidence now validates source/target port presence, signal-family equality, and
  direction compatibility before preview.
- Rejected targets now have evidence reasons before preview instead of silently becoming broad
  highlighted `port:` nodes.
- Closed cleanup ledger item `M32-CL-003` and marked the Story 6.3 sprint action done.
- AC-to-evidence:
  - AC1: `relationshipCandidateEvidence()` supplies candidate/rejected evidence from semantic
    inspection compatibility facts before preview.
  - AC2: frontend test and fixed-string scans prove candidate methods no longer use
    `semanticId.startsWith('port:')`.
  - AC3: docs, cleanup ledger, sprint action, and story evidence updated.
  - AC4: polish/purge ran via frontend tests, full `check`, encoding audit, and source/status
    review.

### File List

- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`
- `ide/theia-frontend/scripts/athena-m31-governed-relationship-preview.test.mjs`
- `docs/usages/engineering-package-platform.md`
- `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m32/6-3-replace-broad-port-candidate-affordance.md`

## Change Log

- 2026-07-22: Story created for M32 Epic 6 broad port candidate affordance cleanup.
- 2026-07-22: Replaced broad `port:` candidate authority with semantic inspection compatibility
  evidence; moved story to review.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent interaction model, LSP payloads, frontend UX, tests, docs, cleanup
  ledger, and sprint artifacts.
- Remove broad `port:`/node-kind relationship candidate authority or ledger retained compatibility
  with owner and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
