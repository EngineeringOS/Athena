# `:extensions:domain-electrical`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:extensions:domain-electrical` module is the reference real Athena proof-domain plugin for M3. It packages a deliberately narrow Electrical/Runtime proof vocabulary behind the core-owned plugin contracts, proving that hosted domain meaning can live outside the kernel while the kernel stays generic.

## Responsibilities

- Implement `ElectricalRuntimeDomainPlugin` as an `AthenaDomainPlugin`.
- Declare a core-owned plugin manifest with identity, type, compatibility, and extension points.
- Publish the M3 electrical proof-domain schema through the stable `:kernel:plugins:plugin-api` contracts.
- Publish inspectable validation, compiler-stage, and renderer-facing contribution metadata through the stable SPI.
- Participate in the governed `LOWER` and `VALIDATE` compiler stages declared through the stable SPI.
- Contribute lowering for `device`, `port`, and `connect` declarations.
- Contribute the first supported `cabinet` and `wiring` view definitions through typed core-owned contracts.
- Contribute Electrical/Runtime-specific semantic diagnostics such as proof device type, port direction, and signal compatibility checks.
- Publish the JVM `ServiceLoader` registration under `META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin`.

## Proof Vocabulary

- Component types: `Lamp`, `Motor`, `Switch`
- Connection type: `Wire`

## Main Types

- `ElectricalRuntimeDomainPlugin`
- `ElectricalRuntimeDomainMarker`

## Dependencies

- `:kernel:plugins:plugin-api`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:runtime`

## Boundaries

This module does not own the canonical Engineering IR schema, compiler pass ordering, generic semantic validation, public plugin SPI ownership, or plugin discovery mechanics. Those remain core-owned in `:kernel:engineering-model`, `:kernel:plugins:plugin-api`, `:kernel:compiler`, and `:kernel:validation`.

## Verification

```bash
./gradlew :extensions:domain-electrical:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-electrical:test
```
