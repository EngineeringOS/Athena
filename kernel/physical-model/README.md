# `:kernel:physical-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:physical-model` module defines Athena's reusable physical traits and M35 physical
installation contracts.

This module keeps the ownership ladder explicit:

`Engineering IR -> physical trait knowledge -> validated installation contract -> downstream physical projection consumers`

## Responsibilities

- Publish reusable physical size through `PhysicalSize`.
- Publish mounting-type identifiers through `PhysicalMountingTypeId`.
- Publish installation-marker identifiers through `PhysicalInstallationMarkerId`.
- Publish minimal physical-trait definitions through `PhysicalTraitDefinition`.
- Publish read-only resolved physical-trait knowledge through `ResolvedPhysicalTraitDefinition`.
- Resolve `PhysicalInstallationContract` from governed project facts and existing resolved traits.
- Validate positive dimensions, mounting type, allowed orientations, clearance, and compatible container kinds.
- Preserve field-level provenance and canonical digest material for deterministic downstream checks.
- Compile typed `PhysicalInstallationIR` topology from installation intent and validated contracts.
- Evaluate physical fit, containment, collision, clearance, and mounting compatibility.
- Preserve the boundary that canonical authored truth remains in `Engineering IR`.

## Main Types

- `PhysicalSize`
- `PhysicalMountingTypeId`
- `PhysicalInstallationMarkerId`
- `PhysicalTraitDefinition`
- `ResolvedPhysicalTraitDefinition`
- `PhysicalInstallationContract`
- `PhysicalInstallationContractResolver`
- `PhysicalInstallationIR`
- `PhysicalInstallationTopologyCompiler`
- `PhysicalConstraintEvaluator`

## Dependencies

This module depends on `:kernel:engineering-model` for canonical semantic identities through `StableSemanticIdentity`.

## Boundaries

This module does not define layout placement, final drawing coordinates, representation selection, anchor coordinates, route segments, canvas coordinates, scene calculation, presentation logic, renderer behavior, package manifests, SVG metadata, product catalogs, manufacturer/article identity, solver/optimization behavior, AI layout, or an Engineering Component System. Existing `PhysicalSize` is unchecked trait input only; validated M35 values are constructed by the contract resolver after diagnostics pass.

## Verification

```bash
./gradlew :kernel:physical-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:physical-model:test
```
