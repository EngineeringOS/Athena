# M11 Implementation Artifacts

This folder preserves the completed M11 implementation artifacts under the milestone-standard `m11/` path.

M11 proved the first serious electrical ECAD workbench depth while keeping canonical engineering semantics upstream of sheets, notation, repeated references, runtime transport, and renderer behavior.

## Included Stories

1. `1-1-publish-electrical-projection-family-contracts.md`
2. `1-2-introduce-the-first-governed-electrical-sheet-model.md`
3. `1-3-introduce-governed-electrical-symbol-and-notation-packs.md`
4. `1-4-deliver-multi-view-electrical-outputs-through-runtime-and-lsp.md`
5. `1-5-preserve-reveal-and-inspection-coherence-across-delivered-electrical-views.md`
6. `2-1-support-repeated-references-and-cross-reference-resolution.md`
7. `2-2-publish-a-larger-electrical-proof-repository-and-verification-path.md`
8. `2-3-improve-dense-electrical-workbench-navigation-and-reveal.md`
9. `2-4-preserve-mutation-review-and-knowledge-coherence-under-ecad-depth.md`
10. `2-5-keep-the-renderer-downstream-under-serious-electrical-depth.md`
11. `epic-1-retro-2026-07-12.md`
12. `epic-2-retro-2026-07-12.md`
13. `m11-retrospective-2026-07-12.md`
14. `milestone-summary-2026-07-12.md`
15. `sprint-status.yaml`

## Usage And Recap

- Usage guide: `docs/usages/m11-proof-usage.md`
- Milestone summary: `milestone-summary-2026-07-12.md`
- Milestone retrospective: `m11-retrospective-2026-07-12.md`

## Verification Snapshot

M11 closeout was verified with sequential Java 25 and Node runs:

- `:kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM11DepthTest`
- `:kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionDepthTest`
- `:ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest`
- `yarn test` in `integrations/graph-glsp`
- `yarn test` in `ide/theia-frontend`
