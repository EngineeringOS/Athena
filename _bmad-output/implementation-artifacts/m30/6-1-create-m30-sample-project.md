---
status: done
baseline_commit: 55843076c6c87e14fe9b69a7298b24679319bda0
story_id: 6.1
epic: 6
title: Create M30 Sample Project
---

# Story 6.1: Create M30 Sample Project

## Status

Done

## Story

As a customer-demo owner,
I want an openable M30 sample project,
so that Athena has a concrete visual proof target.

## Required Context

- PRD: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/prd.md
- Addendum: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m30/addendum.md
- Architecture: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
- Contract: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/REPRESENTATION-CONTRACT.md
- Sprint: _bmad-output/implementation-artifacts/m30/sprint-status.yaml

## Acceptance Criteria

1. Given examples/m30/sample-project exists, when compiled, then it uses admitted Athena syntax and has no QET file references or symbol geometry in source.
2. Given sample semantic content is inspected, when demo requirements are checked, then it includes devices, ports, relationships, and references needed for the demo symbol set.
3. Given Theia opens the sample, when projection loads, then semantic validation does not block downstream projection.

## Tasks/Subtasks

- [x] Create semantic sample source with nested ports and admitted syntax only. (AC: 1,2)
- [x] Include enough relationships/reference facts for native symbol and binding proof. (AC: 2)
- [x] Add compile/open proof for the sample. (AC: 3)
- [x] Complete mandatory polish/purge gate and update M30 cleanup ledger if anything is removed or retained.

## Dev Notes

- The sample is semantic source, not a sheet file and not a symbol asset file.
- Target visual narrative: rolling-shutter/control-circuit style sheet with supply, controls, terminals, actuator/motor/load, references.
- Do not add QET runtime dependency, QET path references in Athena source, or visual primitives to semantic source syntax.
- Do not put representation geometry into the semantic kernel; kernel may expose only representation-relevant engineering facts.
- Use TDD where production code changes are required. On Windows, run Gradle verification sequentially.
- After touching docs/text assets, run powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1.

## Dev Agent Record

### Debug Log

- 2026-07-21: Added RED compiler proof `AthenaM30SampleProjectCompilerTest`; confirmed failure because `examples/m30/sample-project` was missing.
- 2026-07-21: Added RED frontend inventory proof `athena-m30-sample-project.test.mjs`; confirmed failure because the M30 sample source was missing.
- 2026-07-21: Created `examples/m30/sample-project` with one semantic source file using compact nested ports, admitted `connect`, and `layout schematic-sheet` intent only.
- 2026-07-21: Verification passed: `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaM30SampleProjectCompilerTest`; `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`; `node --test ide\theia-frontend\scripts\athena-m30-sample-project.test.mjs`.
- 2026-07-21: Final closeout re-verified the sample with focused compiler proof and current M30 sample-project inventory tests.

### Completion Notes

- Added a rolling-shutter control proof sample with supply, protection, control switch, relay, terminal transition, motor/load, pilot lamp, nested ports, connections, and layout intent.
- The sample source contains no QET file reference, renderer geometry, SVG, or symbol asset syntax.
- Compiler proof confirms the sample compiles, links as one semantic source file, exposes documentation sheets, and produces cross-reference facts.
- Mandatory polish/purge completed; no stale sample artifacts or temporary files were retained.

## File List

- `examples/m30/sample-project/athena.yaml`
- `examples/m30/sample-project/athena.lock`
- `examples/m30/sample-project/README.md`
- `examples/m30/sample-project/src/01-rolling-shutter-control-source.athena`
- `ide/theia-frontend/scripts/athena-m30-sample-project.test.mjs`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM30SampleProjectCompilerTest.kt`
- `_bmad-output/implementation-artifacts/m30/6-1-create-m30-sample-project.md`
- `_bmad-output/implementation-artifacts/m30/sprint-status.yaml`

## Change Log

- 2026-07-21: Story created for M30.
- 2026-07-21: Added M30 rolling-shutter customer-demo sample project and compile/open proofs.

## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M30 cleanup ledger.
- Run the story verification after cleanup so the final state, not a pre-cleanup state, is what passed.
