# `:kernel:engineering-model`

[English](README.md) | 简体中文

`:kernel:engineering-model` 模块定义 Athena 的规范工程模型。`Engineering IR` 是 lowering 之后的语义真源，独立于 parser 结构、插件实现细节与 renderer 专属布局。

这里的 `IR` 借用了 LLVM 的 “Intermediate Representation（中间表示）” 术语，用来表达 Athena 在编译边界之后的规范语义模型。

## 职责

- 在 `EngineeringModel.kt` 中发布规范文档模型。
- 在 `DerivedEngineeringContextModel.kt` 中发布 M9 derived-context contract。
- 在 `EngineeringCapabilityFactModel.kt` 中发布 M9 capability-fact contract。
- 在 `EngineeringConstraintEvaluationModel.kt` 中发布 M9 constraint-evaluation contract。
- 在 `EngineeringImpactConsequenceModel.kt` 中发布 M9 impact-consequence contract。
- 在 `EngineeringKnowledgeStateModel.kt` 中发布 M9 neutral engineering-knowledge snapshot contract。
- 通过 `StableSemanticIdentity` 定义稳定语义标识。
- 通过 `SourceProvenance` 保留作者来源信息。
- 为后续校验、知识推导与渲染提供类型化的工程属性与引用。
- 在规范 `Engineering IR` 之上保留可检查、可追踪的 derived engineering inputs、derived values、capability facts、constraint-evaluation results、impact consequences 与 engineering-knowledge state。

## 主要类型

- `EngineeringDocument`
- `EngineeringSystem`
- `EngineeringComponent`
- `EngineeringPort`
- `EngineeringConnection`
- `EngineeringReference`
- `EngineeringProperty`
- `EngineeringPropertyValue`
- `DerivedEngineeringContext`
- `DerivedEngineeringSubjectContext`
- `DerivedEngineeringInput`
- `DerivedEngineeringInputTrace`
- `DerivedEngineeringValue`
- `DerivedEngineeringValueTrace`
- `EngineeringCapabilityFacts`
- `EngineeringCapabilitySubjectFacts`
- `EngineeringCapabilityFact`
- `EngineeringCapabilityFactTrace`
- `EngineeringConstraintEvaluations`
- `EngineeringConstraintSubjectEvaluations`
- `EngineeringConstraintEvaluation`
- `EngineeringConstraintEvaluationTrace`
- `EngineeringImpactConsequences`
- `EngineeringImpactConsequence`
- `EngineeringImpactReasonKind`
- `EngineeringKnowledgeState`

## 依赖

该模块没有项目模块依赖。

## 边界

该模块不解析 DSL 文本，不执行 derived-context 公式，不做 capability-fact 提升，不做 rule-slice evaluation，不做 before/after impact computation，不做语义规则校验，不做插件发现，不加载受治理知识包，也不生成 SVG。它是其他模块操作的稳定模型边界。

## 验证

```bash
./gradlew :kernel:engineering-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:engineering-model:test
```
