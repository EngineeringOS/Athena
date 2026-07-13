# `:kernel:physical-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:physical-model` module defines Athena's minimal physical-trait contract for M14.

This module keeps the ownership ladder explicit:

`Engineering IR -> physical trait knowledge -> downstream layout / projection / presentation consumers`

## Responsibilities

- Publish reusable physical size through `PhysicalSize`.
- Publish mounting-type identifiers through `PhysicalMountingTypeId`.
- Publish installation-marker identifiers through `PhysicalInstallationMarkerId`.
- Publish minimal physical-trait definitions through `PhysicalTraitDefinition`.
- Publish read-only resolved physical-trait knowledge through `ResolvedPhysicalTraitDefinition`.
- Preserve the boundary that canonical authored truth remains in `Engineering IR`.

## Main Types

- `PhysicalSize`
- `PhysicalMountingTypeId`
- `PhysicalInstallationMarkerId`
- `PhysicalTraitDefinition`
- `ResolvedPhysicalTraitDefinition`

## Dependencies

This module depends on `:kernel:engineering-model` for canonical semantic identities through `StableSemanticIdentity`.

## Boundaries

This module does not define layout placement, geometry bounds, canvas coordinates, scene calculation, routing, presentation logic, or renderer behavior. It only defines minimal reusable physical knowledge: width, height, depth, mounting type, and installation markers.

## Verification

```bash
./gradlew :kernel:physical-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:physical-model:test
```
