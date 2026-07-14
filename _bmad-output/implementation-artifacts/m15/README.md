# M15 Implementation Artifacts

This folder preserves the completed M15 implementation artifacts under the milestone-standard `m15/` path.

M15 is the guided semantic authoring foundation milestone. It proves that Athena can become a real authoring product surface without requiring mainstream engineers to directly write canonical DSL.

## Included Stories

1. `1-1-publish-the-core-authoring-intent-contract.md`
2. `1-2-define-preview-and-acceptance-contracts-for-guided-authoring.md`
3. `1-3-publish-shared-authoring-runtime-and-transport-seams.md`
4. `2-1-publish-the-first-governed-component-panel.md`
5. `2-2-insert-components-through-guided-placement-intents.md`
6. `2-3-keep-placement-synchronized-across-source-and-graph.md`
7. `3-1-publish-a-canonical-inspector-snapshot.md`
8. `3-2-update-editable-properties-through-governed-intents.md`
9. `3-3-keep-implementation-port-and-trait-details-coherent.md`
10. `4-1-publish-allowed-connection-targets-from-semantic-ports.md`
11. `4-2-create-connections-through-governed-connect-intents.md`
12. `4-3-reflect-connection-state-across-graph-and-inspector.md`
13. `5-1-preserve-one-identity-everywhere.md`
14. `5-2-reuse-review-first-mutation-preview-for-guided-authoring.md`
15. `5-3-publish-the-first-guided-authoring-proof-corpus-and-verification-path.md`
16. `milestone-summary-2026-07-13.md`
17. `m15-retrospective-2026-07-13.md`
18. `sprint-status.yaml`

## Planned Scope

- Epic 1: authoring intent and runtime foundation
- Epic 2: guided component placement
- Epic 3: inspector editing
- Epic 4: port-aware connection authoring
- Epic 5: unified reveal, review, and proof

## Current Status

- Milestone state: closed
- Milestone tracking: `sprint-status.yaml`
- Planning inputs:
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md`
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md`
  - `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md`
  - `_bmad-output/planning-artifacts/epics-M15-2026-07-13.md`

## Completed Highlights

- Epic 1: shared authoring intents, preview contracts, and runtime transport seams above M8.
- Epic 2: governed component panel, source-backed component insertion, and cross-surface placement synchronization.
- Epic 3: canonical inspector snapshots, governed property editing, and component-knowledge-backed detail coherence.
- Epic 4: semantic-port target filtering, preview-first connection creation, and graph or inspector connection-state coherence.
- Epic 5: one identity everywhere, review-first guided commit, and a repository-backed guided authoring proof corpus.

## Usage And Recap

- Usage guide: `docs/usages/m15-proof-usage.md`
- Milestone summary: `milestone-summary-2026-07-13.md`
- Milestone retrospective: `m15-retrospective-2026-07-13.md`

## Milestone Intent

- Keep M8 as the only mutation authority.
- Keep M14 as the source of authorable component knowledge.
- Prove that mainstream engineers can create governed engineering intent without directly editing raw DSL.
- Keep source, graph, inspector, diagnostics, and semantic SCM synchronized through canonical rebuild.
- Keep the first proof narrow, Siemens-first, electrical only, and repository-backed.

## Product Position

- Athena is `semantic-first`, not `DSL-first`.
- Guided authoring is the mainstream product direction.
- Component panel, inspector, graph connect flow, forms, templates, AI, API, and DSL are all entry surfaces.
- Those surfaces must converge through one guided authoring and mutation path before canonical state changes.
- Direct DSL authoring remains an expert surface, not the required default workflow.

## Verification Snapshot

Sequential Windows verification with Java 25 passed:

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- `yarn build` in `ide/theia-frontend`
- `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
