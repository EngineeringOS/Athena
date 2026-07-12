---
baseline_commit: 61fa8d7
---

# Story 2.1: Generate Grounded Diagnostic Explanation Proposals

Status: done

## Outcome

Athena now generates deterministic diagnostic explanation proposals from governed evidence through the M10 proof provider and exposes them through runtime, LSP, and the semantic-inspection panel.

## Proof

- diagnostic explanation proposals cite diagnostic, derived-context, capability-fact, and constraint evidence
- proposals remain advisory and keep explicit decision state
- semantic inspection can request and revisit persisted explanation proposals

## Key Files

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiDeterministicProofProvider.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAiReasoningProtocol.kt`
- `ide/theia-frontend/src/browser/athena-semantic-inspection-widget.tsx`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiDeterministicProofProviderTest.kt`

## Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiDeterministicProofProviderTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest"`

## Change Log

- 2026-07-12: Completed Story 2.1 with grounded diagnostic explanation proofs across runtime, LSP, and semantic inspection.
