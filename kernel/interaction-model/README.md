# `:kernel:interaction-model`

This module defines semantic interaction intents and capability discovery independently of any UI toolkit.

## Responsibilities

- Resolve interaction subjects to canonical semantic identities.
- Discover allowed actions through the semantic capability registry.
- Model interaction envelopes, lifecycle states, reveals, and authoring capabilities.
- Keep user actions reviewable and downstream of Athena authority.

## Boundaries

This module does not mutate source, compile projects, render views, or own frontend widget state.

## Verification

```powershell
.\gradlew.bat :kernel:interaction-model:test
```
