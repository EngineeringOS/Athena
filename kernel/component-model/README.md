# `:kernel:component-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:component-model` module defines Athena's first vendor-neutral component knowledge contract for M14.

This module keeps the ownership ladder explicit:

`Engineering IR -> component knowledge -> vendor implementations / semantic ports / physical traits -> downstream M9 and M13 consumers`

## Responsibilities

- Publish stable concept identities through `EngineeringConceptId`.
- Publish vendor-neutral concept definitions through `EngineeringConceptDefinition`.
- Publish read-only resolved component knowledge through `ResolvedComponentDefinition`.
- Preserve the boundary that canonical authored truth remains in `Engineering IR`.
- Preserve the boundary that component knowledge resolution is read-only and not a new mutation path.

## Main Types

- `EngineeringConceptId`
- `EngineeringConceptDefinition`
- `ResolvedComponentDefinition`

## Dependencies

This module depends on `:kernel:engineering-model` for canonical semantic subject identity through `StableSemanticIdentity`.

## Boundaries

This module does not define vendor implementation mappings, semantic port contracts, physical-trait contracts, knowledge-pack loading, compiler orchestration, runtime transport, projection logic, presentation logic, or renderer behavior. It is the narrow concept contract layer those later modules can target.

## Verification

```bash
./gradlew :kernel:component-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:component-model:test
```
