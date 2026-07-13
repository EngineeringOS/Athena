# `:kernel:part-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:part-model` module defines Athena's vendor implementation mapping contract for M14.

This module keeps the ownership ladder explicit:

`Engineering IR -> component knowledge -> vendor part implementations -> downstream M9 and M13 consumers`

## Responsibilities

- Publish stable vendor identity through `VendorId`.
- Publish vendor-facing catalog identity through `VendorPartNumber`.
- Publish Athena-owned implementation identity through `PartImplementationId`.
- Publish vendor implementation mappings through `PartImplementationDefinition`.
- Publish read-only resolved implementation selections through `ResolvedPartImplementation`.
- Preserve the boundary that `EngineeringConceptId` remains the vendor-neutral semantic target.

## Main Types

- `VendorId`
- `VendorPartNumber`
- `PartImplementationId`
- `PartImplementationDefinition`
- `ResolvedPartImplementation`

## Dependencies

This module depends on:

- `:kernel:component-model` for `EngineeringConceptId`
- `:kernel:engineering-model` for canonical semantic subject identity through `StableSemanticIdentity`

## Boundaries

This module does not define the engineering concept model itself, semantic port contracts, physical-trait contracts, knowledge-pack loading, compiler orchestration, runtime transport, projection logic, presentation logic, or renderer behavior. It only models vendor implementations as downstream realizations of vendor-neutral concepts.

## Verification

```bash
./gradlew :kernel:part-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:part-model:test
```
