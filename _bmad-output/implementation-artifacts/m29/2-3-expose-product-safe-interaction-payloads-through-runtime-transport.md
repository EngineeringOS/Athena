# Story 2.3: Expose Product-Safe Interaction Payloads Through Runtime Transport

## Status

Done

## Objective

Expose Interaction payloads over the existing runtime/LSP seam without making transport the schema
authority.

## Required Context

- Architecture AD-7.
- `INTERACTION-CONTRACT.md` LSP Envelope.

## Scope

- Add LSP/runtime DTOs that mirror kernel interaction contracts.
- Use `InteractionEnvelope(schemaVersion="m29.interaction.v1")`.
- Reject unsupported schema versions.
- Ignore adapter metadata for canonical accept/mutation input.

## Acceptance Criteria

- Given interaction subjects/actions are available, when transported, then payloads are deterministic,
  JSON-safe, and versioned.
- Given unsupported schema version is received, when parsed, then
  `interaction.transport.unsupported-version` is returned.
- Given adapter metadata is round-tripped, when accept/mutation is requested, then adapter metadata
  is ignored for canonical identity and eligibility.

## Verification

- LSP protocol tests.
- No Theia-only DTO becomes normative schema.

## Dev Agent Record

### Completion Notes

- Added `InteractionEnvelope` with `m29.interaction.v1` schema version.
- Added payload kinds for subjects, actions, command, preview, reveal, diagnostic, and proof.
- Added unsupported schema version diagnostic.
- Final polish/purge sweep found no story-local stale source or doc artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:interaction-model:test`

### File List

- `kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionEnvelope.kt`
- `kernel/interaction-model/src/test/kotlin/com/engineeringood/athena/interaction/InteractionEnvelopeTest.kt`

### Change Log

- 2026-07-21: Implemented Story 2.3 versioned Interaction envelope.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
