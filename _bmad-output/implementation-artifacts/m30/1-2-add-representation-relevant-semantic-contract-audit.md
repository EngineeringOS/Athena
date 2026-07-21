---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 1.2
epic: 1
title: Add Representation-Relevant Semantic Contract Audit
---

# Story 1.2: Add Representation-Relevant Semantic Contract Audit

## Status

Done

## Story

As a Athena architect,
I want a recorded audit of semantic facts available to representation binding,
so that implementation does not paper over missing domain facts in renderer code.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given the audit is published, when it is read, then it lists identity, type, role, ports, direction, signal/medium, terminal number, relationship capability, occurrence context, and provenance as binding facts.
2. Given kernel boundaries are reviewed, when visual concepts are searched, then primitives, hotspots, style tokens, QET link types, SVG paths, and frontend hitbox rules are not kernel concepts.
3. Given a binding-critical fact is missing, when the audit closes, then the gap is recorded as a domain-contract gap with owner and target story or milestone.

## Tasks/Subtasks

- [x] Inspect existing semantic/projection model facts relevant to representation binding. (AC: 1)
- [x] Document allowed kernel facts and forbidden view-layer concepts. (AC: 1,2)
- [x] Record gaps in the M30 cleanup ledger or story notes with owner/target. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- This is a boundary/audit story; do not add renderer fallbacks to compensate for missing facts.
- Use existing M24-M29 facts where possible before proposing new domain contracts.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Used CodeGraph to inspect engineering semantic model, connection model, project semantic declarations, document projection facts, source provenance, and electrical domain validation paths.
- 2026-07-21: Searched semantic/projection boundaries for visual concepts (`QET`, `.elmt`, `svg`, `hitbox`, `hotspot`, `style token`, `stroke`, `viewBox`, `path`) and found the pre-M30 electrical presentation-pack bridge.
- 2026-07-21: Recorded retained/deferred legacy bridge as cleanup-ledger entry `M30-CL-001`.
- 2026-07-21: Verification passed with `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.
- 2026-07-21: Review tightened the audit to list terminal number explicitly as a representation binding fact with current partial source boundaries.
- 2026-07-21: Review verification passed with AC keyword check and `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

### Completion Notes

- Published the M30 representation semantic contract audit with allowed binding facts, forbidden semantic-kernel concepts, and domain-contract gaps.
- Review clarified terminal number as an explicit binding need while preserving the domain-contract gap for uniform semantic-port terminal numbering.
- Recorded terminal number, normalized representation role, reusable relationship capability snapshot, and location/terminal-strip membership as explicit gaps with owners and targets.
- Did not add renderer fallbacks, source syntax, QET runtime references, or visual primitives to semantic source.
- Completed polish/purge review and recorded the only retained legacy bridge in the cleanup ledger.

## File List

- `_bmad-output/implementation-artifacts/m30/1-2-add-representation-relevant-semantic-contract-audit.md`
- `_bmad-output/implementation-artifacts/m30/cleanup-ledger.md`
- `_bmad-output/implementation-artifacts/m30/representation-semantic-contract-audit.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Published representation-relevant semantic contract audit and cleanup-ledger bridge entry.
- 2026-07-21: Closed review wording gap for terminal-number binding facts.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
