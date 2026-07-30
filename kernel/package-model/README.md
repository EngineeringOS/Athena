# `:kernel:package-model`

This module defines typed engineering and representation package contracts.

## Responsibilities

- Model engineering package identity, exports, and validation.
- Model representation package descriptors and binding rules.
- Model presentation profiles and package admission limits.
- Keep package metadata typed and independent from transport or filesystem loading.

## Boundaries

This module does not scan directories, resolve resources, parse Athena source, select runtime representations, or own project engineering facts.

## Verification

```powershell
.\gradlew.bat :kernel:package-model:test
```
