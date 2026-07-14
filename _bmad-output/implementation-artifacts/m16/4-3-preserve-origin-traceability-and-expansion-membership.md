---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 4.3: Preserve Origin Traceability And Expansion Membership

Status: done

## Story

As an architecture owner,
I want Athena to preserve origin traceability and expansion membership for accepted reuse,
so that later diff, replace, review, and AI reasoning work can stay grounded.

## FR Traceability

- FR-9: Athena records accepted Semantic Macro identity, instantiation identity, and parameter values
- FR-10: Athena can identify which semantic subjects belong to an accepted expansion
- NFR-4: Accepted reuse stays deterministic and inspectable after approval

## Acceptance Criteria

1. Given one Semantic Macro expansion has been accepted, when origin data is inspected, then Athena records Semantic Macro identity, instantiation identity, and parameter values for that expansion.
2. Given expanded semantic subjects are inspected, when membership is queried, then Athena can identify which semantic subjects belong to the accepted expansion.

## Tasks / Subtasks

- [x] Add a runtime-owned accepted-expansion origin inspection result. (AC: 1, 2)
  - [x] Resolve accepted expansion facts from applied command history instead of introducing a second traceability cache.
  - [x] Support both subject-based and instantiation-based lookup.
- [x] Publish traceability details through LSP transport. (AC: 1, 2)
  - [x] Extend origin inspection payloads with command id, bundle id, accepted expansion, and matched membership.
  - [x] Add LSP tests for accepted subject and instantiation lookup.
- [x] Keep applied-history ownership explicit. (AC: 2)
  - [x] Verify undo removes applied origin availability for the affected expansion.

## Implementation Notes

- Added `AthenaSemanticMacroOriginInspectionReady` so runtime can return one applied accepted expansion together with its command id, bundle id, accepted expansion facts, and matched membership edge.
- `inspectOrigin()` now derives traceability strictly from applied `AthenaApplySemanticMacroBundleCommand` history records, so undo/redo remains the authority for whether an expansion is still active.
- LSP origin payloads now surface accepted expansion package/version, normalized parameter values, memberships, and matched membership role for subject-specific queries.

## Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests *origin*`
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests *origin*`

## References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeService.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticMacroRuntimeServiceTest.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticMacroProtocol.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaReuseRequestTest.kt]

## Story Completion Status

- Status: done
- Completion note: Accepted Semantic Macro origin and membership facts are now inspectable from applied command history and remain aligned with undo/redo behavior.
