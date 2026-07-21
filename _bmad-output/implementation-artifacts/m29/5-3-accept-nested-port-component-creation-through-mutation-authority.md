# Story 5.3: Accept Nested-Port Component Creation Through Mutation Authority

## Status

Done

## Objective

Accept semantic entity creation through governed mutation and generate nested-port component source.

## Required Context

- Existing `CreateComponentIntent`.
- M28 nested anatomy parser/lowering/source serializer behavior.

## Scope

- Map accepted insertion command to `CreateComponentIntent`.
- Persist deterministic nested-port component anatomy.
- Recompile/reproject and show inserted entity in source, semantic inspection, projection facts, and
  graph view.

## Acceptance Criteria

- Given a valid insert preview is accepted, when mutation authority applies it, then source receives
  nested-port component anatomy.
- Given recompile/reproject completes, when outputs are inspected, then the inserted component exists
  as semantic entity and rendered projection occurrence.
- Given the command is accepted, then Theia does not bypass runtime/source-edit authority.

## Verification

- Structured proof payload for `entity-creation-accept`.
- Parser/compiler/projection regression tests for generated nested ports.

## Dev Agent Record

### Completion Notes

- Extended the accepted create-component LSP proof to assert recompiled projection output contains
  the inserted component occurrence.
- Verified accepted insertion still writes nested-port source anatomy, recompiles semantic
  inspection, and exposes inserted ports as projection labels.
- Confirmed Theia remains outside the mutation path: source edits are returned by backend
  runtime/source-edit authority and then applied through the tracked source buffer.
- Final polish/purge sweep found no story-local stale code or generated artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`

### File List

- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`

### Change Log

- 2026-07-21: Added projection occurrence proof for accepted nested-port component creation.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
