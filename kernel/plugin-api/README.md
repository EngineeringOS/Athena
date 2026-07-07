# `:kernel:plugin-api`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:plugin-api` module is Athena's stable extension-facing contract surface. It owns the public plugin SPI that extension modules compile against, without making them depend on compiler implementation packages.

## Responsibilities

- Publish the stable `AthenaPlugin` contract and typed plugin interfaces.
- Publish plugin manifest metadata, extension-point vocabulary, and compatibility models.
- Publish plugin-facing lowering and validation context models that remain independent from compiler-private implementation packages.
- Keep the public SPI small, documented, and kernel-owned.

## Boundaries

This module does not own plugin discovery, plugin approval, compiler orchestration, runtime lifecycle, or renderer orchestration. Those remain in host and orchestration modules.

## Verification

```bash
./gradlew :kernel:plugin-api:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:plugin-api:test
```
