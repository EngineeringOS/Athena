# `:domain-electrical-runtime`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:domain-electrical-runtime` module is the first real Athena domain plugin. It packages the M0 Electrical/Runtime lowering and validation rules behind the core-owned plugin contracts, proving that domain meaning can live outside the compiler core.

## Responsibilities

- Implement `ElectricalRuntimeDomainPlugin` as an `AthenaDomainPlugin`.
- Declare a core-owned plugin manifest with identity, type, compatibility, and extension points.
- Contribute lowering for `device`, `port`, and `connect` declarations.
- Contribute Electrical/Runtime-specific semantic diagnostics such as device type, port direction, and signal compatibility checks.
- Publish the JVM `ServiceLoader` registration under `META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin`.

## Main Types

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## Dependencies

- `:compiler`
- `:language`
- `:semantics-core`
- `:ir`

## Boundaries

This module does not own the canonical IR schema, compiler pass ordering, generic semantic validation, or plugin discovery mechanics. Those remain core-owned in `:compiler`, `:ir`, and `:semantics-core`.

## Verification

```bash
./gradlew :domain-electrical-runtime:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :domain-electrical-runtime:test
```
