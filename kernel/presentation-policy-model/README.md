# `:kernel:presentation-policy-model`

This module defines typed policies that select and compose representations for a projection context.

## Responsibilities

- Model presentation policy profiles and diagnostics.
- Compose component representations from admitted semantic and profile facts.
- Publish industrial control profile policy without coupling to a renderer.

## Boundaries

This module does not own component identity, package loading, layout coordinates, graphic primitives, or rendering.

## Verification

```powershell
.\gradlew.bat :kernel:presentation-policy-model:test
```
