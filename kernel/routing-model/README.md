# `:kernel:routing-model`

English

The `:kernel:routing-model` module defines the M21 schematic route and label fact boundary. It turns
governed schematic endpoint and label requests into deterministic sheet-level facts that downstream
sheet surfaces can paint and inspect.

## Responsibilities

- Define stable schematic endpoint, route, lane, segment, and route fact contracts.
- Preserve snapshot, subject, occurrence, and endpoint identity in route facts.
- Provide the first deterministic rule-based schematic route strategy.
- Define stable schematic label and cross-reference facts for device names, terminal names, route
  names, and cross-reference markers.
- Keep label placement deterministic and tied to governed subject, occurrence, endpoint, or route
  identity.
- Keep route facts inspectable without renderer-owned endpoint meaning.

## Boundaries

Route and label facts describe schematic sheet topology and readability only. They are layout facts
for downstream sheet surfaces, not a second semantic authority and not an external helper stack
decision.

## Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
```
