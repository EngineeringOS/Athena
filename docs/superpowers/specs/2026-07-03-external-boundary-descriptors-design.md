# External Boundary Descriptors Design

**Date:** 2026-07-03
**Story:** 2.6 - Define External Boundary Contract Descriptors

## Goal

Add a compiler-owned, machine-readable boundary descriptor surface for external standards, runtime bridges, and enterprise contexts without allowing any external boundary to become a semantic authority or an execution surface in M0.

## Scope

This design adds:

- a new `compiler.boundary` package
- local `.properties` boundary descriptor fixtures
- deterministic load and validation behavior
- inspectable compiler-facing boundary validation results
- boundary documentation

This design does not add:

- importer or exporter execution
- live runtime connectors
- plugin discovery reuse
- governed knowledge activation reuse
- pass reordering or a fifth public compiler pass

## Core Decisions

### 1. Separate boundary contracts from plugins and governed knowledge

Boundary descriptors are perimeter contracts, not executable plugins and not reviewed knowledge artifacts. They need their own vocabulary and validation path so the compiler can express external-system relationships without collapsing them into the wrong extension boundary.

### 2. Use a local `.properties` manifest for M0

The descriptor format stays JVM-first, zero-extra-dependency, deterministic, and aligned with the repository's current M0 proof style. A boundary descriptor is loaded from a local directory root containing a single manifest file.

### 3. Keep `Engineering IR` as the sole canonical authority

Each descriptor declares its authority posture, but validation only accepts descriptors whose upstream semantic authority remains `ENGINEERING_IR`. Any descriptor that tries to elevate an external standard, runtime schema, or enterprise system to canonical authority is rejected.

### 4. Expose descriptor validation as metadata, not as a compiler pass

Boundary validation results will be attached to `CompilerCompilationSuccess`, but the compiler pipeline remains:

- `PARSE`
- `LOWER`
- `VALIDATE`
- `DOWNSTREAM_DERIVATION`

This keeps boundary validation inspectable without changing pass ownership.

## Proposed Package Shape

Primary package:

- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/boundary/`

Planned types:

- descriptor source model
- descriptor manifest model
- category, direction, authority, and exchanged-form enums
- compatibility assumption model
- validation diagnostic/report model
- loader and validator

## Fixture Shape

Fixture root:

- `compiler/src/test/resources/boundary-descriptors/`

Planned fixtures:

- `automationml-reference` - valid standards reference boundary
- `opc-ua-runtime` - valid passive runtime boundary
- `external-authority` - invalid descriptor that attempts external canonical authority
- `operational-execution` - invalid descriptor that implies live importer/exporter or connector behavior

## Compiler Integration

`AthenaCompiler` will accept a boundary descriptor source, resolve it before compilation, and attach the resulting report to compile results. This resolution remains compiler-owned metadata assembly and does not affect semantic diagnostics, render behavior, or pass ordering in Story 2.6.

## Testing Strategy

The implementation will follow TDD:

1. Add failing boundary descriptor validation tests.
2. Add a failing compiler integration test proving boundary metadata does not change pipeline behavior.
3. Implement the minimal boundary surface to satisfy those tests.
4. Run the required sequential Java 25 verification commands.

## Non-Goals

Story 2.6 intentionally does not implement:

- `AutomationML` import or export logic
- `OPC UA` protocol handling
- ERP, MES, or cloud connectivity
- descriptor-driven classpath activation
- descriptor-driven semantic rewriting
