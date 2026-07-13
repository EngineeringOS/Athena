# `:kernel:connection-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:connection-model` module defines Athena's semantic port knowledge contract for M14.

This module keeps the ownership ladder explicit:

`Engineering IR -> semantic port knowledge -> downstream M9 / projection / presentation consumers`

## Responsibilities

- Publish stable semantic port identities through `SemanticPortTypeId`.
- Publish stable role, direction, signal-family, and protocol identifiers.
- Publish vendor-neutral semantic port definitions through `SemanticPortDefinition`.
- Publish read-only resolved semantic port knowledge through `ResolvedSemanticPortDefinition`.
- Preserve the boundary that canonical authored port truth remains in `Engineering IR`.

## Main Types

- `SemanticPortTypeId`
- `SemanticPortRoleId`
- `SemanticSignalFamilyId`
- `SemanticProtocolId`
- `SemanticPortDirection`
- `SemanticPortDefinition`
- `ResolvedSemanticPortDefinition`

## Dependencies

This module depends on `:kernel:engineering-model` for canonical semantic identities through `StableSemanticIdentity`.

## Boundaries

This module does not define compatibility or sufficiency judgement, routing geometry, graph coordinates, shape ids, widget state, physical traits, knowledge-pack loading, compiler orchestration, runtime transport, projection logic, presentation logic, or renderer behavior. Richer judgement belongs downstream in the M9 ladder:

`DerivedEngineeringContext -> EngineeringCapabilityFacts -> EngineeringConstraintEvaluations`

This module is the narrow semantic-port knowledge contract only.

## Verification

```bash
./gradlew :kernel:connection-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:connection-model:test
```
