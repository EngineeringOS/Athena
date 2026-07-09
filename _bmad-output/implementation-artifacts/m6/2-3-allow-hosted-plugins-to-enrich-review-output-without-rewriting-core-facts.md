---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 2.3: Allow Hosted Plugins To Enrich Review Output Without Rewriting Core Facts

Status: review

## Story

As a platform engineer,
I want approved hosted plugins to enrich semantic review output,
so that domain-specific extensions can add useful interpretation without becoming alternative semantic authorities.

## FR Traceability

- FR-8: produce semantic review summaries over current repository change
- FR-9: surface semantic review output through existing Athena runtime and product seams
- FR-4: preserve stable semantic change categories as the core review authority
- NFR-1: semantic SCM remains downstream of compiler/runtime semantic authority
- NFR-2: plugin enrichments remain VCS-neutral and semantic-first
- NFR-3: the same semantic change set and hosted plugin set yields the same enriched review output
- NFR-4: plugin enrichment remains inspectable and additive

## Acceptance Criteria

1. Given core semantic change and review records exist, when hosted plugin enrichment is invoked, then approved plugins can contribute deterministic labels, hints, or domain-specific review summaries through existing plugin contracts, and they cannot suppress, replace, or rewrite core semantic SCM facts.
2. Given no plugin is installed or an enrichment step fails, when Athena continues review generation, then the core semantic review still succeeds with stable generic output, and plugin enrichment remains optional and additive.

## Tasks / Subtasks

- [x] Add a governed review-enrichment plugin contract in `:kernel:plugins:plugin-api`. (AC: 1, 2)
  - [x] Introduce one explicit extension point for semantic review enrichment.
  - [x] Keep all public/core Kotlin enrichment types under `com.engineeringood.athena.plugin` with clean KDoc.
  - [x] Keep the contract additive so plugins can publish labels, hints, or short summaries without mutating core review entries.
- [x] Extend hosted plugin inventory and runtime services for review enrichment. (AC: 1, 2)
  - [x] Surface approved review-enrichment contributors through hosted plugin inspection and runtime accessors.
  - [x] Reject plugins that declare or implement review enrichment incorrectly using the same hosted runtime boundary rules.
  - [x] Keep deterministic approved-plugin ordering for enrichment execution.
- [x] Publish additive plugin enrichments in semantic review output. (AC: 1, 2)
  - [x] Extend the semantic review contract with an inspectable enrichment section that remains separate from core review entries.
  - [x] Add a runtime-owned review-enrichment path that applies hosted plugins after core review generation.
  - [x] Preserve plugin isolation so plugin failures degrade into additive diagnostics or warnings rather than breaking core review.
- [x] Verify determinism and failure tolerance with focused tests. (AC: 1, 2)
  - [x] Add plugin-api or plugin-host tests for the new extension point and approval/runtime contract checks.
  - [x] Add runtime and semantic-review tests proving enrichments are additive, deterministic, and optional.
  - [x] Keep Gradle verification sequential on Windows with Java 25.
- [x] Update live M6 docs for review-enrichment ownership. (AC: 1, 2)
  - [x] Update plugin, runtime, and semantic-SCM docs to explain that hosted review enrichment is additive and non-authoritative.
  - [x] Update root and workspace summary docs so M6 reflects plugin-enriched review output correctly.

## Dev Notes

### Story Intent

- Story 2.3 adds the first governed plugin seam above the typed review-summary substrate completed in Stories 2.1 and 2.2.
- The goal is not to let plugins redefine review meaning. The goal is to let approved plugins add domain-aware interpretation that remains visibly downstream of the canonical semantic facts.
- Story 2.3 should freeze the additive enrichment model before Story 2.4 exposes semantic review and commit output through runtime/LSP/IDE seams.

### Architecture Guardrails

- Align to AD-19 by keeping semantic review authority inside `:kernel:semantic-scm` rather than moving it into plugins or frontend code.
- Align to AD-21 by reusing the existing JVM semantic diff and review path as the only source of core review facts.
- Align to AD-23 by keeping hosted plugin enrichment JVM-owned and product-downstream rather than Theia-owned.
- Align to AD-24 by preserving authored-versus-derived distinctions in the core review output even when plugins add interpretation.
- Align to AD-25 by making plugin enrichment additive, deterministic, and unable to suppress or rewrite core semantic SCM facts.

### Technical Requirements

- Prefer one explicit plugin-side enrichment contract rather than overloading view, render, or validation contributions.
- Keep plugin enrichment outputs inspectable and separate from `SemanticReviewEntry` so core review facts remain obviously authoritative.
- Runtime should apply enrichments only after core review generation completes successfully.
- No plugin enrichment path may mutate, filter, reorder, or replace the core `SemanticReviewSummary.entries`.
- Public/core Kotlin classes added in this story need clean KDoc.

### Architecture Compliance

- Prevent these failure modes:
  - plugin enrichment rewriting or suppressing core review entries
  - plugin failure breaking semantic review generation
  - runtime discovering review enrichers through ad hoc type scans outside the hosted plugin boundary
  - non-deterministic enrichment ordering across repeated runs
  - Git, Theia, or UI-specific nouns leaking into plugin review-enrichment contracts

### Library / Framework Requirements

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`
- No third-party dependency should be added for Story 2.3.

### File Structure Requirements

- Expected new or updated files:
  - `kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/...`
  - `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/...`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/...`
  - `kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/...`
  - corresponding tests under `kernel/plugins/plugin-api`, `kernel/plugins/plugin-host`, `kernel/runtime`, and `kernel/semantic-scm`
  - live doc updates in `README*.md`, `docs/usages/athena-workspace-summary.md`, and module READMEs

### Testing Requirements

- Minimum story verification:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test :kernel:plugins:plugin-host:test :kernel:semantic-scm:test :kernel:runtime:test"`
- Recommended focused regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test :extensions:domain-dummy:test :integrations:scm-git:test"`
- Completion gate:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Previous Story Intelligence

- Story 2.1 already established typed review entries and stable fact references over semantic diff and consequence output.
- Story 2.2 already added typed commit-intent output that depends on reviewed semantic facts rather than raw files.
- M3 already established hosted plugin discovery, approval, and lifecycle boundaries, so Story 2.3 should reuse that governance pattern instead of inventing a second plugin path.
- M5 and M6 architecture already require plugin enrichments to remain additive and non-authoritative.

### Git Intelligence Summary

- Current milestone baseline:
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
- Practical implication:
  - keep plugin contracts stable and inspectable
  - keep review authority in semantic SCM and runtime

### Latest Technical Information

- No extra web research is required for this story.
- The relevant stack is already frozen locally:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`

### Project Structure Notes

- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- Root package remains `com.engineeringood`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M6-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m6/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m6/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-1-generate-semantic-review-summaries-from-typed-change-records.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-2-prepare-deterministic-commit-intent-from-semantic-change.md]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginContracts.kt]
- [Source: kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistry.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt]
- [Source: kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt]

## Story Completion Status

- Status: review
- Completion note: Hosted semantic review enrichment is now governed through `:kernel:plugins:plugin-api` and `:kernel:plugins:plugin-host`, published additively through runtime-owned review services, proven with the electrical extension, documented in the live M6 docs, and fully verified under Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M6 epic, PRD, and architecture review for Story 2.3 additive plugin enrichment guardrails
- semantic-scm, runtime, and hosted-plugin boundary inspection for existing review-summary and plugin-service seams
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test :kernel:plugins:plugin-host:test :kernel:semantic-scm:test :kernel:runtime:test :extensions:domain-electrical:test :extensions:domain-dummy:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :integrations:scm-git:test :kernel:repository-model:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Added the `SEMANTIC_REVIEW_ENRICHMENT` extension point plus `AthenaSemanticReviewEnrichmentContributor` so hosted plugins can publish additive review labels, hints, and summaries through the stable SPI.
- Extended hosted plugin approval, inventory categorization, and runtime inspection/execution services so semantic review enrichers are approved, discovered, ordered, and failure-guarded like other hosted extension points.
- Extended `SemanticReviewSummary` with separate `SemanticReviewEnrichment` records and wired `AthenaSemanticReviewService` to append plugin enrichments after core review generation without mutating core review entries.
- Added electrical-plugin review enrichment proof plus runtime tests for deterministic contributor exposure, contract rejection, degraded plugin failure warnings, and runtime/service-registry review publication.
- Updated root, workspace, plugin, semantic-SCM, and runtime READMEs in English and Chinese to reflect additive hosted semantic review enrichment in the current M6 proof.

### File List

- _bmad-output/implementation-artifacts/m6/2-3-allow-hosted-plugins-to-enrich-review-output-without-rewriting-core-facts.md
- _bmad-output/implementation-artifacts/m6/sprint-status.yaml
- README.md
- README.zh-CN.md
- docs/usages/athena-workspace-summary.md
- extensions/domain-electrical/build.gradle.kts
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt
- kernel/plugins/plugin-api/README.md
- kernel/plugins/plugin-api/README.zh-CN.md
- kernel/plugins/plugin-api/build.gradle.kts
- kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaPluginManifestModel.kt
- kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaSemanticReviewEnrichmentModel.kt
- kernel/plugins/plugin-api/src/test/kotlin/com/engineeringood/athena/plugin/PluginApiContributionContractTest.kt
- kernel/plugins/plugin-host/README.md
- kernel/plugins/plugin-host/README.zh-CN.md
- kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistry.kt
- kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginApproval.kt
- kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistryTest.kt
- kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginContractTest.kt
- kernel/runtime/README.md
- kernel/runtime/README.zh-CN.md
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewService.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticReviewServiceTest.kt
- kernel/semantic-scm/README.md
- kernel/semantic-scm/README.zh-CN.md
- kernel/semantic-scm/src/main/kotlin/com/engineeringood/athena/scm/SemanticScmContracts.kt

### Change Log

- 2026-07-09: Added additive hosted semantic review enrichment across plugin SPI, host governance, semantic review contracts, runtime-owned review publication, electrical proof-plugin behavior, focused tests, and live M6 documentation.
