# `:kernel:semantic-scm`

[English](README.md) | 简体中文

`:kernel:semantic-scm` 模块定义 Athena 在 M6 阶段的规范语义 SCM 契约边界。它发布保持 VCS 中立的 baseline、change、consequence、review、commit intent 与 history 核心语义名词，让 runtime、compiler、IDE 与供应商适配层共享同一套 Athena 语义契约，而不会把源码管理语义污染到 `:kernel:repository-model`。

## 职责

- 发布类型化的语义 baseline 描述与 baseline snapshot 契约。
- 发布 vendor-neutral 的 baseline locator、adapter、request、result 与 resolver seam。
- 发布面向 repository/package 与 engineering meaning 的 deterministic semantic diff categorization。
- 发布 snapshot 级别的 semantic diagnostics，让不完整或部分可解析的 comparison input 仍然可检查。
- 发布稳定的 semantic change、derived consequence、review、commit intent 与 history 契约形状。
- 在 baseline state 上发布可选 engineering-knowledge snapshot，让 review flow 保持在 renderer 与 frontend reconstruction 之前。
- 从 `SemanticDiff` 生成 deterministic semantic review summary。
- 发布与 core review entry 分离的 additive semantic review enrichment record。
- 从已评审的 semantic change 生成 deterministic semantic commit intent。
- 以 transport-light 的类型化模型发布 package-aware semantic history request、baseline-sequence comparison、package version meaning、dependency movement、validation movement、contract-break risk 与 release relevance。
- 从 baseline-sequence comparison 生成 deterministic publish-oriented semantic history summary。
- 保持 semantic history 以 package identity 与 baseline sequence 为中心，而不是扩展成 registry 或 release transport 工作流。
- 保持 review entry 足够类型化，能够区分 repository-contract change、dependency movement、direct engineering change、downstream engineering impact、validation impact 与 degraded comparison input。
- 保持 commit-intent entry 足够类型化，能够区分 repository-contract work、dependency movement、direct engineering change、downstream engineering impact、derived consequence、validation consequence 与 degraded comparison input。
- 保持公共契约表面 VCS 中立且可检查。

## 主要类型

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

## 依赖

- `:kernel:repository-model`
- `:kernel:engineering-model`
- `:kernel:validation`

## 边界

该模块发布 baseline-loading seam，以及 deterministic semantic diff、consequence、带 engineering impact 的 review-summary、review-enrichment、commit-intent 与 package-aware history 契约和 generator，但它仍然不实现 vendor substrate access、commit execution、hosted-plugin enrichment orchestration、LSP / Theia SCM transport、registry workflow 或 remote publish transport。它保持 JVM-first、VCS-neutral，并聚焦在语义契约与少量编排辅助之上。

## 验证

```bash
./gradlew :kernel:semantic-scm:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test
```
