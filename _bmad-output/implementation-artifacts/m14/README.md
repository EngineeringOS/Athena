# M14 Implementation Artifacts

This folder preserves the completed M14 implementation artifacts under the milestone-standard `m14/` path.

M14 is the component-knowledge foundation milestone. It proves that Athena can resolve authored component references into vendor-neutral engineering concepts, semantic ports, minimal physical traits, and vendor implementations without moving canonical meaning out of `Engineering IR` or opening a second mutation path.

## Included Stories

1. `1-1-define-the-core-engineering-concept-contract.md`
2. `1-2-define-the-vendor-implementation-mapping-contract.md`
3. `1-3-publish-the-first-electrical-concept-and-siemens-implementation-slice.md`
4. `2-1-define-typed-semantic-port-contracts.md`
5. `2-2-keep-compatibility-judgement-out-of-the-port-contract.md`
6. `2-3-define-minimal-physical-trait-contracts.md`
7. `2-4-publish-the-first-electrical-semantic-port-and-physical-trait-slice.md`
8. `3-1-define-the-knowledge-pack-registry-and-active-pack-set-contract.md`
9. `3-2-resolve-component-references-through-existing-package-governance.md`
10. `3-3-surface-unresolved-and-conflicting-definitions-explicitly.md`
11. `4-1-publish-resolved-component-knowledge-through-runtime-and-transport-seams.md`
12. `4-2-integrate-resolved-component-knowledge-into-m9-inputs.md`
13. `4-3-integrate-resolved-component-knowledge-into-projection-and-presentation-consumers.md`
14. `4-4-preserve-m8-as-the-only-mutation-authority.md`
15. `5-1-publish-the-first-m14-electrical-proof-corpus.md`
16. `5-2-record-the-product-position-for-multi-surface-authoring.md`
17. `5-3-publish-the-deterministic-verification-and-failure-path.md`
18. `multi-surface-authoring-position.md`
19. `verification-path.md`
20. `milestone-summary-2026-07-14.md`
21. `m14-retrospective-2026-07-14.md`
22. `sprint-status.yaml`

## Planned Scope

- Epic 1: component and part model foundation
- Epic 2: semantic port and physical trait foundation
- Epic 3: deterministic knowledge-pack loading and resolution
- Epic 4: downstream integration without new write authority
- Epic 5: proof corpus, product position, and verification path

## Current Status

- Milestone state: closed
- Milestone tracking: `sprint-status.yaml`
- Planning inputs:
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/prd.md`
  - `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m14/addendum.md`
  - `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m14/ARCHITECTURE-SPINE.md`
  - `_bmad-output/planning-artifacts/epics-M14-2026-07-13.md`

## Completed Highlights

- Epic 1: vendor-neutral engineering concept and vendor implementation contracts
- Epic 2: typed semantic-port and minimal physical-trait contracts
- Epic 3: deterministic governed knowledge-pack activation and component resolution
- Epic 4: compiler-owned M9-facing integration plus projection/presentation downstream evidence
- Epic 5: real repository-backed proof corpus, product-position record, and deterministic verification path

## Usage And Recap

- Usage guide: `docs/usages/m14-proof-usage.md`
- Milestone summary: `milestone-summary-2026-07-14.md`
- Milestone retrospective: `m14-retrospective-2026-07-14.md`

## Milestone Intent

- Freeze the vendor-neutral engineering concept contract before deeper M14 work expands.
- Keep `Engineering IR` as the canonical authored truth.
- Keep M8 as the only mutation authority.
- Feed resolved component knowledge downstream into later M9 and M13 consumers.
- Reuse existing package governance instead of inventing a second dependency or lock model.

## Usage

- Use `sprint-status.yaml` as the single milestone progress tracker.
- Add each new M14 story file into this folder using the existing `1-1-...md` naming pattern.
- Keep milestone closeout artifacts here too:
  - milestone summary
  - retrospective
  - verification recap

## Product Position

- Athena is `semantic-first`, not `DSL-first`.
- Graph, forms, templates, AI, API, and DSL are all producer surfaces.
- M8 semantic mutation remains the only write authority across those surfaces.
- M14 adds read-only component knowledge resolution and downstream evidence only.
- Direct DSL authoring is an expert surface, not the required mainstream workflow.
- See `multi-surface-authoring-position.md` for the explicit M14 position note.

## Verification Snapshot

Story `1.1` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:component-model:test --console=plain --no-daemon`

Story `1.2` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:part-model:test --console=plain --no-daemon`

Story `1.3` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :extensions:domain-electrical:test --console=plain --no-daemon`

Story `2.1` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:connection-model:test --console=plain --no-daemon`

Story `2.2` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:connection-model:test --console=plain --no-daemon`

Story `2.3` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:physical-model:test --console=plain --no-daemon`

Story `2.4` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :extensions:domain-electrical:test --console=plain --no-daemon`

Story `3.1` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgeResolutionModelContractTest --console=plain --no-daemon`

Story `3.2` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaGovernedKnowledgePackageSourceBuilderTest --console=plain --no-daemon`

Story `3.3` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --console=plain --no-daemon`

Story `4.1` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaPluginRuntimeServicesTest --console=plain --no-daemon`
- `java25; .\gradlew.bat :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaComponentKnowledgeRequestTest --console=plain --no-daemon`

Story `4.2` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest --console=plain --no-daemon`
- `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --console=plain --no-daemon`

Story `4.3` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest --tests com.engineeringood.athena.compiler.AthenaProjectionPresentationComponentKnowledgeIntegrationTest --tests com.engineeringood.athena.compiler.PresentationModelDeriverTest --console=plain --no-daemon`

Story `4.4` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --console=plain --no-daemon`

Story `5.1` was verified sequentially on Windows with Java 25:

- `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest --console=plain --no-daemon`

Story `5.2` recorded product-position boundaries in documentation and passed the repository encoding audit.

Story `5.3` published and re-verified the deterministic proof path with:

- `java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest --console=plain --no-daemon`
- `java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --console=plain --no-daemon`
