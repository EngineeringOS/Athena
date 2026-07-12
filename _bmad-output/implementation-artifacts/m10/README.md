# M10 Implementation Artifacts

This folder preserves the completed M10 implementation artifacts under the milestone-standard `m10/` path.

M10 proved that Athena can deliver governed AI-assisted engineering reasoning without moving engineering truth into prompts, frontend state, or provider SDKs.

## Included Stories

1. `1-1-publish-governed-ai-reasoning-contracts.md`
2. `1-2-derive-deterministic-reasoning-contexts-from-governed-semantic-outputs.md`
3. `1-3-host-provider-neutral-reasoning-sessions-and-typed-outcome-states.md`
4. `1-4-expose-reasoning-requests-through-athena-lsp-as-sole-ide-boundary.md`
5. `2-1-generate-grounded-diagnostic-explanation-proposals.md`
6. `2-2-generate-engineering-impact-summary-proposals.md`
7. `2-3-generate-review-ready-next-check-proposals.md`
8. `2-4-preserve-cited-fact-audit-trails-across-reasoning-proofs.md`
9. `3-1-integrate-theia-ai-foundation-into-athena-product-shell.md`
10. `3-2-surface-reasoning-actions-in-semantic-inspection-and-review-panels.md`
11. `3-3-preserve-explicit-proposal-decision-state-in-existing-athena-workbench-seams.md`
12. `4-1-publish-m10-proof-corpus-and-deterministic-mock-provider-verification-path.md`
13. `4-2-document-provider-configuration-safety-boundaries-and-operator-usage.md`
14. `epic-1-retro-2026-07-12.md`
15. `epic-2-retro-2026-07-12.md`
16. `epic-3-retro-2026-07-12.md`
17. `epic-4-retro-2026-07-12.md`
18. `m10-retrospective-2026-07-12.md`
19. `milestone-summary-2026-07-12.md`
20. `sprint-status.yaml`

## Usage And Recap

- Usage guide: `docs/usages/m10-proof-usage.md`
- Milestone summary: `milestone-summary-2026-07-12.md`
- Milestone retrospective: `m10-retrospective-2026-07-12.md`

## Verification Snapshot

M10 closeout was verified with sequential Java 25 and Yarn runs:

- `:kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaAiDeterministicProofProviderTest --tests com.engineeringood.athena.runtime.AthenaAiReasoningSessionRuntimeServiceTest`
- `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaAiReasoningRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaM10ProofCorpusTest`
- `:ide:lsp:build`
- `yarn --cwd ide workspace @engineeringood/athena-theia-frontend build`
- `yarn --cwd ide workspace @engineeringood/athena-theia-product build`

## Final Notes

- The live desktop verification path depends on the installed host under `ide/lsp/build/install/athena-lsp-host`, so LSP API changes require `:ide:lsp:build` before manual IDE checks.
- The M10 proof corpus also depends on canonical `athena.lock` rendering in both `examples/m10/reasoning-proof/baseline` and `current`.
