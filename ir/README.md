# `:ir`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:ir` module defines Athena's canonical engineering model. `Engineering IR` is the semantic source of truth after lowering, independent from parser structures, plugin implementation details, and renderer-specific layouts.

## Responsibilities

- Publish the canonical document model in `EngineeringIrModel.kt`.
- Define stable semantic identities through `StableSemanticIdentity`.
- Preserve authored provenance through `SourceProvenance`.
- Provide typed engineering properties and references for downstream validation and rendering.

## Main Types

- `EngineeringIrDocument`
- `EngineeringSystem`
- `EngineeringComponent`
- `EngineeringPort`
- `EngineeringConnection`
- `EngineeringReference`
- `EngineeringProperty`
- `EngineeringPropertyValue`

## Dependencies

This module has no project-module dependencies.

## Boundaries

This module does not parse DSL text, validate semantic rules, discover plugins, load governed knowledge, or generate SVG. It is the stable model other modules operate on.

## Verification

```bash
./gradlew :ir:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :ir:test
```
