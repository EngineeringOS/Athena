---
baseline_commit: 61fa8d7
---

# Story 2.3: Generate Review-Ready Next-Check Proposals

Status: done

## Outcome

Athena now emits narrow review-ready next-check proposals from governed evidence and keeps them advisory instead of executable.

## Proof

- next-check proposals are generated only when safe governed cues exist
- next-check output remains explicit review guidance with no mutation authority
- unresolved or unavailable state remains available through the same stored proposal seam

## Key Files

- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiDeterministicProofProvider.kt`
- `ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM10ProofCorpusTest.kt`

## Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiDeterministicProofProviderTest"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest"`

## Change Log

- 2026-07-12: Completed Story 2.3 with deterministic review-ready next-check guidance.
