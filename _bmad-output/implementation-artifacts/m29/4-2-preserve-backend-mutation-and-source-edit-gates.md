# Story 4.2: Preserve Backend Mutation And Source-Edit Gates

## Status

Done

## Objective

Keep accepted relationship mutation behind existing backend authoring/runtime/source-edit gates.

## Required Context

- Architecture AD-5 and AD-11.
- M28 source-backed mutation behavior.

## Scope

- Send accepted relationship commands through runtime to `SemanticRelationshipIntent`.
- Preserve backend validation and source-edit authority.
- Block invalid accepts at backend mutation/source-edit gate.

## Acceptance Criteria

- Given a valid relationship command is accepted, when runtime handles it, then Theia does not edit
  `.athena` directly.
- Given invalid accept is attempted, when backend validation runs, then source remains unchanged.
- Given legacy compatibility exists, when relationship mutation is accepted, then compatibility
  adapts into `SemanticRelationshipIntent` and not the reverse.

## Verification

- Runtime/LSP/source-edit tests.
- M28 relationship mutation regression test remains valid through Interaction IR.

## Dev Agent Record

### Completion Notes

- Added an LSP regression proving an accepted invalid `semantic-relationship` request does not
  receive a backend source edit.
- Verified the invalid accept path leaves compiled semantic connection state unchanged, preserving
  `.athena` source persistence behind the backend source-edit gate.
- Confirmed the valid semantic relationship and legacy connect-port regression coverage remains in
  the same focused LSP test class.
- Final polish/purge sweep found no story-local stale code, docs, generated artifacts, or
  compatibility paths to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`

### File List

- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`

### Change Log

- 2026-07-21: Added backend source-edit gate regression for invalid semantic relationship accepts.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
