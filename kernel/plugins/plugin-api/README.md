# `:kernel:plugins:plugin-api`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:plugins:plugin-api` module is Athena's stable extension-facing contract surface. It owns the public plugin SPI that extension modules compile against, without making them depend on compiler implementation packages.

## Responsibilities

- Publish the stable `AthenaPlugin` contract and typed plugin interfaces.
- Publish plugin manifest metadata, extension-point vocabulary, and compatibility models.
- Publish generic domain schema contracts for entities, properties, ports, and connections.
- Publish inspectable validation, compiler-stage, and renderer-facing contribution descriptors.
- Publish additive semantic review-enrichment contracts that let hosted plugins add labels, hints, or summaries without rewriting core semantic SCM facts.
- Publish plugin-facing lowering, semantic-enrichment, and validation context models that remain independent from compiler-private implementation packages.
- Publish the stable stage vocabulary that the compiler host uses to govern declared domain participation.
- Keep the public SPI small, documented, and kernel-owned.

## Boundaries

This module does not own plugin discovery, plugin approval, compiler orchestration, runtime lifecycle, or renderer orchestration. Those remain in host and orchestration modules. The SPI declares contribution intent and stable stage vocabulary; host, runtime, and compiler modules decide how that intent is approved, executed, and inspected.

## Verification

```bash
./gradlew :kernel:plugins:plugin-api:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugins:plugin-api:test
```

