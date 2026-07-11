# `:kernel:engineering-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:engineering-model` module defines Athena's canonical engineering model. `Engineering IR` is the semantic source of truth after lowering, independent from parser structures, plugin implementation details, and renderer-specific layouts.

IR: borrow from LLVM intermediate representation ("LLVM IR").

## Responsibilities

- Publish the canonical document model in `EngineeringModel.kt`.
- Publish M9 derived-context contracts in `DerivedEngineeringContextModel.kt`.
- Publish M9 capability-fact contracts in `EngineeringCapabilityFactModel.kt`.
- Publish M9 constraint-evaluation contracts in `EngineeringConstraintEvaluationModel.kt`.
- Publish M9 impact-consequence contracts in `EngineeringImpactConsequenceModel.kt`.
- Publish M9 neutral engineering-knowledge snapshot contracts in `EngineeringKnowledgeStateModel.kt`.
- Define stable semantic identities through `StableSemanticIdentity`.
- Preserve authored provenance through `SourceProvenance`.
- Provide typed engineering properties and references for downstream validation and rendering.
- Preserve typed, inspectable derived engineering inputs, derived values, capability facts, constraint-evaluation results, and impact consequences above canonical `Engineering IR`.

## Main Types

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

## Dependencies

This module has no project-module dependencies.

## Boundaries

This module does not parse DSL text, execute derived-context formulas, promote capability facts, evaluate rule slices, compute before/after impact, validate semantic rules, discover plugins, load governed knowledge, or generate SVG. It is the stable model other modules operate on.

## Verification

```bash
./gradlew :kernel:engineering-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:engineering-model:test
```
