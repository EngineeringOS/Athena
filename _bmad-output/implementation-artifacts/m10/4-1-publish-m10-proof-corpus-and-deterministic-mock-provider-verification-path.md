---
baseline_commit: 61fa8d7
---

# Story 4.1: Publish M10 Proof Corpus And Deterministic Mock-Provider Verification Path

Status: done

## Outcome

Athena now ships a committed `examples/m10` proof corpus and a deterministic proof-provider verification path.

## Proof

- `examples/m10/reasoning-proof` contains baseline and current repositories
- the default M10 LSP path uses the deterministic proof provider
- one committed LSP test verifies diagnostic explanation, impact summary, and next-check flows on the published corpus

## Key Files

- `examples/m10/README.md`
- `examples/m10/reasoning-proof/`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM10ProofCorpusTest.kt`

## Verification

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest"`

## Change Log

- 2026-07-12: Completed Story 4.1 with published proof fixtures and deterministic verification.
