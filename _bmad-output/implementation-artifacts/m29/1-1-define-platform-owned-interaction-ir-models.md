# Story 1.1: Define Platform-Owned Interaction IR Models

## Status

Done

## Objective

Create the platform-owned Interaction IR model boundary outside Theia.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m29/prd.md`
- Spine: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m29/ARCHITECTURE-SPINE.md`
- Contract: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m29/INTERACTION-CONTRACT.md`

## Scope

- Add `kernel/interaction-model` unless current Gradle boundaries prove a lower-risk placement.
- Define models for subjects, occurrences, capabilities, action intents, commands, previews, reveal
  requests/results, diagnostics, lifecycle, and provenance.
- Keep model code free of Theia, DOM, SVG, GLSP, CSS, and widget id dependencies.

## Acceptance Criteria

- Given the interaction model module is compiled, when tests inspect the model, then it includes the
  required fields from `INTERACTION-CONTRACT.md`.
- Given an interaction subject has projection context, when represented in the model, then canonical
  identity and occurrence context are distinct.
- Given adapter metadata is present, when subject identity is checked, then adapter metadata is not
  accepted as identity.

## Verification

- Sequential Gradle verification for the new/changed Kotlin module.
- Encoding audit after text/doc changes.

## Dev Agent Record

### Completion Notes

- Added proposed M29 `:kernel:interaction-model` Gradle project.
- Defined platform-owned Interaction IR contracts for subjects, occurrences, capabilities, semantic
  action intents, commands, previews, reveal requests/results, diagnostics, lifecycle, and
  provenance.
- Kept contracts independent from Theia, DOM, SVG, GLSP, CSS, and widget ids.
- Verified red phase first with missing symbols, then green phase with module tests passing.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `settings.gradle.kts`
- `kernel/interaction-model/build.gradle.kts`
- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionModels.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionModelContractTest.kt`

### Change Log

- 2026-07-21: Implemented Story 1.1 Interaction IR model contract.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
