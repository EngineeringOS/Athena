# Story 5.2: Preview Semantic Entity Creation Source Impact

## Status

Done

## Objective

Preview source impact and affected semantic identities for semantic entity creation.

## Required Context

- `INTERACTION-CONTRACT.md` Preview Ownership.
- M28 nested-port source anatomy.

## Scope

- Build entity creation preview through backend runtime/source-edit logic.
- Include proposed source edit target, component identity, nested ports, affected subjects, and
  diagnostics.
- Ensure rejection leaves source and projection unchanged.

## Acceptance Criteria

- Given insert semantic entity action is requested, when preview is built, then proposed source
  impact and affected subjects are returned.
- Given preview is rejected, when source/projection state is inspected, then no mutation occurred.
- Given source impact is generated, then it is not string-concatenated in Theia.

## Verification

- Structured proof payload for `entity-creation-preview`.
- Runtime/source-edit tests.

## Dev Agent Record

### Completion Notes

- Added backend preview source impact for create-component authoring previews using the same LSP
  source-edit helper that later serves accepted mutations.
- Preview payloads now include proposed edit target, suggested component identity, nested port
  source anatomy, and affected parent/proposed component identities.
- Added rejection coverage proving preview rejection returns no source edit and leaves source-backed
  semantic inspection unchanged.
- Final polish/purge sweep found no Theia entity-creation source snippet construction and no
  story-local stale/generated artifacts to remove.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests "com.engineeringood.athena.ide.lsp.AthenaAuthoringRequestTest"`
- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests "com.engineeringood.athena.runtime.AthenaAuthoringSessionRuntimeServiceTest"`
- `rg "device PLC|port PLC|componentRef|vendorPartNumber|sourceImpact" ide/theia-frontend/src ide/theia-frontend/scripts`

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt`

### Change Log

- 2026-07-21: Added backend-owned entity creation preview source impact and rejection proof.
## Mandatory Final Polish/Purge Gate

Before this story may be marked done:

- Review all files touched by the story for dead code, stale docs, obsolete tests, temporary proof artifacts, misleading design notes, unused compatibility paths, and accidental generated artifacts.
- Remove everything that is not required for the accepted architecture and tests.
- If an artifact must remain temporarily, record owner, reason, and target milestone in the story notes or M29 cleanup ledger.
- Run the story's verification after cleanup so the final state, not a pre-cleanup state, is what passed.
