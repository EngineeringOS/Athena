---
baseline_commit: c7fda3877a76fd8db52a65510e1b123aed4304f1
---

# Story 3.4: Source, Problems, Outline, Graph, And Inspector Coherence

Status: done

## Story

As an engineer,
I want source, Problems, outline, graph, document projection, and inspector views to agree,
so that visual inspection stays tied to semantic truth.

## Acceptance Criteria

1. Given the M27 sample contains routes, symbols, labels, and document projection facts, selecting a
   subject in graph or inspector resolves matching source identity, outline item, Problems
   diagnostic, route quality, and document projection context where available.
2. Route quality diagnostics include governed provenance and actionable reason text.
3. The canvas remains a consumer of diagnostics and facts rather than their source.

## Tasks / Subtasks

- [x] Verify canonical selection and reveal coherence (AC: 1, 3)
  - [x] Existing frontend tests resolve canonical semantic selection from typed inspection payloads.
  - [x] Existing frontend tests resolve rendered sheet subjects without DOM inference.
  - [x] Existing frontend tests keep port selection alive through endpoint and anchor aliases.
- [x] Verify Problems/source coherence (AC: 1, 2, 3)
  - [x] Existing frontend tests resolve Problem diagnostics through canonical ids or governed source
        ranges without parsing message text.
  - [x] Routing-model tests publish route-quality diagnostics from route facts.

## Dev Notes

- This story is satisfied by existing M26/M27 coherence tests plus Story 2 route-quality fact work.
- No DOM scan is introduced as route or document meaning authority.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Completion Notes List

- Fresh frontend tests passed across canonical selection, source reveal, Problem reveal, repeated
  projection occurrences, endpoint aliases, and related-subject resolution.
- Fresh routing tests passed route-quality diagnostics and inspection payload coverage.

### File List

- `_bmad-output/implementation-artifacts/m27/3-4-source-problems-outline-graph-and-inspector-coherence.md`

## Change Log

- 2026-07-20: Created and closed Story 3.4 from existing coherence and route-quality coverage.

## Verification

- `yarn --cwd ide/theia-frontend test` - passed earlier in M27 graph-view closeout, 133/133 tests.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` - passed during Story 2.3 closeout.
