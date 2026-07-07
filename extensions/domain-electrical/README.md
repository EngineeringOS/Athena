# `:extensions:domain-electrical`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:extensions:domain-electrical` module is the first real Athena domain plugin. It packages the M0 Electrical/Runtime lowering and validation rules behind the core-owned plugin contracts, proving that domain meaning can live outside the compiler core.

## Responsibilities

- Implement `ElectricalRuntimeDomainPlugin` as an `AthenaDomainPlugin`.
- Declare a core-owned plugin manifest with identity, type, compatibility, and extension points.
- Contribute lowering for `device`, `port`, and `connect` declarations.
- Contribute the first supported `cabinet` and `wiring` view definitions through typed core-owned contracts.
- Contribute Electrical/Runtime-specific semantic diagnostics such as device type, port direction, and signal compatibility checks.
- Publish the JVM `ServiceLoader` registration under `META-INF/services/com.engineeringood.athena.compiler.plugin.AthenaPlugin`.

## Main Types

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## Dependencies

- `:kernel:compiler`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`

## Boundaries

This module does not own the canonical IR schema, compiler pass ordering, generic semantic validation, or plugin discovery mechanics. Those remain core-owned in `:kernel:compiler`, `:kernel:engineering-model`, and `:kernel:validation`.

## Verification

```bash
./gradlew :extensions:domain-electrical:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-electrical:test
```
