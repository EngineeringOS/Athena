# `:kernel:semantic-scm`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:semantic-scm` module defines Athena's canonical M6 semantic SCM contract surface. It publishes the VCS-neutral baseline, change, consequence, review, commit-intent, and history nouns that later runtime, compiler, IDE, and vendor-adapter stories can consume without polluting `:kernel:repository-model` with source-control semantics.

## Responsibilities

- Publish typed semantic baseline descriptors and baseline snapshot contracts.
- Publish the vendor-neutral baseline locator, adapter, request, result, and resolver seam.
- Publish deterministic semantic diff categorization over repository/package and engineering meaning.
- Publish snapshot-scoped semantic diagnostics so incomplete or partially resolvable comparison inputs remain inspectable.
- Publish stable semantic change, consequence, review, commit-intent, and history contract shapes.
- Generate deterministic semantic review summaries from one `SemanticDiff`.
- Publish additive semantic review enrichment records that remain separate from core review entries.
- Generate deterministic semantic commit intent from reviewed semantic change.
- Publish package-aware semantic history requests, baseline-sequence comparisons, package-version meaning, dependency movement, validation movement, contract-break risk, and release relevance in transport-light typed form.
- Generate deterministic publish-oriented semantic history summaries from baseline-sequence comparisons.
- Keep review entries typed enough to distinguish repository-contract change, dependency movement, engineering change, validation impact, and degraded comparison input.
- Keep commit-intent entries typed enough to distinguish repository-contract work, dependency movement, engineering change, derived consequences, validation consequences, and degraded comparison input.
- Keep semantic history package-centered and baseline-sequence-oriented without widening M6 into registry or release-transport workflows.
- Keep every review entry traceable back to authored changes, derived consequences, or diagnostics through stable fact references.
- Keep every commit-intent entry traceable back to reviewed semantic facts, authored changes, derived consequences, or diagnostics through stable fact references.
- Publish compiler-derived repository-contract and validation consequence classification above canonical repository and validation state.
- Keep authored change versus derived consequence explicit in the public contract vocabulary.
- Keep the public contract surface VCS-neutral and inspectable.

## Main Types

- `SemanticBaselineDescriptor`
- `SemanticBaselineSnapshot`
- `SemanticBaselineLocator`
- `SemanticBaselineResolutionRequest`
- `SemanticBaselineResolutionResult`
- `SemanticBaselineAdapter`
- `SemanticBaselineResolver`
- `SemanticDiffCalculator`
- `SemanticChangeCategory`
- `SemanticChangeRecord`
- `SemanticDerivedConsequence`
- `SemanticDiff`
- `SemanticReviewEntryKind`
- `SemanticReviewEntry`
- `SemanticReviewEnrichmentKind`
- `SemanticReviewEnrichment`
- `SemanticReviewSummary`
- `SemanticReviewSummaryGenerator`
- `SemanticCommitEntryKind`
- `SemanticCommitEntry`
- `SemanticCommitIntent`
- `SemanticCommitIntentGenerator`
- `SemanticHistoryRequest`
- `SemanticHistoryComparison`
- `SemanticPackageVersionMeaning`
- `SemanticDependencyMovement`
- `SemanticValidationMovement`
- `SemanticContractBreakRisk`
- `SemanticReleaseRelevance`
- `SemanticHistorySummaryGenerator`
- `SemanticHistoryEntry`
- `SemanticHistorySummary`
- `SemanticScmAdapter`

## Dependencies

- `:kernel:repository-model`
- `:kernel:engineering-model`
- `:kernel:validation`

## Boundaries

This module publishes the baseline-loading seam plus deterministic semantic diff, consequence, review-summary, review-enrichment, commit-intent, and package-aware history contracts and generators, but it still does not implement vendor substrate access, commit execution, hosted-plugin enrichment orchestration, LSP or Theia SCM transport, registry workflows, or remote publish transport. It stays JVM-first, VCS-neutral, and centered on semantic contracts plus small orchestration helpers.

## Verification

```bash
./gradlew :kernel:semantic-scm:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test
```
