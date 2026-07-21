# Story 6.1: Create Openable M29 Sample Project

## Status

Done

## Objective

Create `examples/m29/sample-project` for M29 product proof.

## Required Context

- M27/M28 sample patterns.
- M29 PRD FR36-FR37.

## Scope

- Add an openable sample project with nested-port source.
- Include subjects for reveal/navigation, relationship mutation, and semantic entity creation proof.
- Avoid validation errors that trigger `STOP_DOWNSTREAM`.

## Acceptance Criteria

- Given `examples/m29/sample-project` is opened, when compile/projection runs, then projection is
  available.
- Given sample source is inspected, then it includes suitable subjects for all M29 proof flows.
- Given graph view opens, then M27 visual density and sheet behavior do not regress.

## Verification

- Sample compile/projection smoke.
- No ambiguous authored references.

## Dev Agent Record

### Completion Notes

- Added openable `examples/m29/sample-project` with repository config, lockfile, README, and three
  nested-port source files.
- Included source subjects for reveal/navigation, valid and invalid relationship flows, and semantic
  entity creation context.
- Added a compiler smoke test proving all M29 sample sources compile without diagnostics, expose
  projections, and link project references without ambiguity.
- Final polish/purge sweep found no generated lock drift or stale sample artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaM29SampleProjectCompilerTest"`

### File List

- `examples/m29/sample-project/athena.yaml`
- `examples/m29/sample-project/athena.lock`
- `examples/m29/sample-project/README.md`
- `examples/m29/sample-project/src/01-interaction-authoring-source.athena`
- `examples/m29/sample-project/src/02-interaction-candidates.athena`
- `examples/m29/sample-project/src/03-entity-creation-context.athena`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM29SampleProjectCompilerTest.kt`

### Change Log

- 2026-07-21: Added openable M29 sample project and compiler/projection smoke.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
