# `:kernel:document-projection-model`

This module defines renderer-neutral document projection contracts derived from Athena engineering truth.

## Responsibilities

- Model document projection snapshots, subjects, relations, and source provenance.
- Define projection policy inputs without selecting a renderer or mutating semantic state.
- Keep document navigation and inspection payloads deterministic and traceable.

## Boundaries

This module does not parse Athena source, own engineering truth, calculate drawing geometry, render graphics, or implement workbench UI.

## Verification

```powershell
.\gradlew.bat :kernel:document-projection-model:test
```
