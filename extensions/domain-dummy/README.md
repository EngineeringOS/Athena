# `:extensions:domain-dummy`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:extensions:domain-dummy` module is the synthetic hosted proof-domain plugin for M3. It exists only to prove that Athena's extension SPI can host a second, non-electrical domain without teaching the kernel any dummy-specific vocabulary.

## Responsibilities

- Implement `DummyRuntimeDomainPlugin` as an `AthenaDomainPlugin`.
- Publish a synthetic domain schema through the stable `:kernel:plugins:plugin-api` contracts.
- Publish inspectable validation, compiler-stage, runtime-view, and renderer-facing contribution metadata through the stable SPI.
- Scope dummy participation to explicitly dummy-authored declarations so it can coexist with `:extensions:domain-electrical`.
- Prove a second hosted domain without adding kernel-owned dummy nouns, compiler special cases, or default global view-definition expansion.
- Publish the JVM `ServiceLoader` registration under `META-INF/services/com.engineeringood.athena.plugin.AthenaPlugin`.

## Proof Vocabulary

- Component types: `Glyph`, `Pulse`, `Totem`
- Connection type: `DummyLink`
- Ownership marker: `domain "dummy-runtime"`

## Main Types

- `DummyRuntimeDomainPlugin`
- `DummyRuntimeDomainMarker`

## Dependencies

- `:kernel:plugins:plugin-api`
- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:runtime`

## Boundaries

This module does not own the canonical Engineering IR schema, compiler pass ordering, generic semantic validation, public plugin SPI ownership, or hosted discovery mechanics. It is intentionally synthetic and must remain a proof of extensibility rather than a second product domain.

## Verification

```bash
./gradlew :extensions:domain-dummy:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :extensions:domain-dummy:test
```
