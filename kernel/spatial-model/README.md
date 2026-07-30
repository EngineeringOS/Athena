# `:kernel:spatial-model`

This module defines source-independent semantic spatial intent.

## Responsibilities

- Model spatial relations and placement intent using canonical semantic identities.
- Preserve provenance for downstream layout and projection compilers.
- Keep intent distinct from solved coordinates and rendered geometry.

## Boundaries

This module does not solve constraints, place occurrences, route connections, render graphics, or mutate Athena source.

## Verification

```powershell
.\gradlew.bat :kernel:spatial-model:test
```
