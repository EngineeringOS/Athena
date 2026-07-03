# M0 Plugin Contract Boundary

## Purpose

Story `2.1` publishes Athena's first core-owned typed plugin contract surface for M0.

The goal is to prove that plugins are real, typed, and governed without collapsing Story `2.1` into classpath discovery or activation mechanics.

## What The Core Owns

The core now owns:

- plugin type vocabulary for `domain`, `rule`, and `renderer`
- extension-point vocabulary for the M0 compiler substrate
- plugin manifest shape
- core-version compatibility range shape
- validation rules and inspectable diagnostics for plugin declarations

These contracts live under `compiler` because the compiler owns extension boundaries and pass governance.

## What Plugins May Contribute

The M0 typed contracts are intentionally narrow.

- `AthenaDomainPlugin` may declare domain capability metadata
- `AthenaRulePlugin` may declare rule capability metadata
- `AthenaRendererPlugin` may declare renderer target metadata

These contracts do not let a plugin:

- replace `Engineering IR`
- redefine semantic authority
- alter compiler pass ordering
- bypass core-owned manifest validation

## Story 2.1 Proof Shape

Story `2.1` proves the boundary with one minimal real sample plugin:

- `domain-electrical-runtime` implements `ElectricalRuntimeDomainPlugin`
- the sample plugin declares a core-owned manifest
- tests instantiate it directly and validate it through the core validator

This proves the contract is executable before discovery exists.

## Non-Goals

Story `2.1` does not include:

- classpath discovery
- manifest resource loading
- `ServiceLoader`
- activation inventory building
- plugin ordering
- plugin-to-plugin dependency resolution
- renderer or rule plugin activation

Those remain in Story `2.2` and later Epic 2 slices.
