---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 4.1
epic: 4
title: Define Composition Intent Model
---

# Story 4.1: Define Composition Intent Model

## Status

Done

## Story

As a compiler engineer,
I want schematic composition intent facts,
so that professional sheet structure is planned without becoming CAD geometry truth.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given composition intent model is inspected, when demo requirements are checked, then rails, columns, terminal groups, route lanes, reference zones, and compact label placement are represented.
2. Given composition intent runs, when inputs are inspected, then it consumes representation bounds/anchors and M27 spatial facts.
3. Given output is inspected, when facts are emitted, then lane membership, column membership, alignment group, label band, route channel, terminal group, and reference zone facts exist before final presentation geometry.
4. Given persistence is reviewed, when source truth is checked, then composition intent does not persist CAD geometry as semantic truth.

## Tasks/Subtasks

- [x] Add failing composition intent contract tests. (AC: 1,3)
- [x] Implement composition intent model and mapping from representation/spatial inputs. (AC: 1,2,3)
- [x] Add anti-CAD persistence test or audit. (AC: 4)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- Use composition intent wording in code/docs where possible; avoid implying final CAD geometry ownership.
- Final coordinates remain downstream layout/presentation facts.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: RED confirmed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test --tests "com.engineeringood.athena.representation.SchematicCompositionIntentCompilerTest"` failing on missing composition intent contracts.
- 2026-07-21: GREEN confirmed with focused `SchematicCompositionIntentCompilerTest` after adding composition input, fact, plan, and compiler contracts.
- 2026-07-21: Full module verification passed with `.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test`.
- 2026-07-21: Review verification passed with focused `SchematicCompositionIntentCompilerTest` and full module test.

### Completion Notes

- Added schematic composition intent facts for rail, column, terminal group, route lane, reference zone, label band, and alignment group.
- Composition planning consumes representation occurrence bounds, terminal-anchor counts, and spatial intent references while emitting pre-geometry membership facts.
- Added anti-CAD transport assertion so composition payloads do not persist coordinates, viewBox, SVG, or CAD truth.
- Completed final polish/purge review; no stale artifacts were removed or retained for this story.
- Review confirmed composition intent remains pre-geometry planning, not CAD truth.

## File List

- `_bmad-output/implementation-artifacts/m30/4-1-define-composition-intent-model.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`
- `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/SchematicCompositionIntent.kt`
- `kernel/representation-model/src/test/kotlin/com/engineeringood/athena/representation/SchematicCompositionIntentCompilerTest.kt`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added schematic composition intent model and anti-CAD tests.
- 2026-07-21: Closed review after composition intent verification.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
