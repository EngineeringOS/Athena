# `:kernel:layout-engine`

English

The `:kernel:layout-engine` module defines the M21 strategy boundary that turns layout intent
snapshots into Athena-owned schematic placement facts.

## Responsibilities

- Define a small strategy interface for schematic layout.
- Provide the first deterministic rule-based schematic strategy.
- Define subordinate helper proposal normalization without selecting any concrete helper stack.
- Preserve canonical subject, occurrence, intent, snapshot, and source-span identity in placement
  facts.
- Keep the renderer, Theia, CSS, DOM, canvas interaction state, and external helper choices outside
  the strategy contract.

## Boundaries

Helper proposals are subordinate. They must normalize into Athena-owned placement facts, and they may
not replace snapshot, subject, occurrence, role, zone, or source-span identity from layout intent.

This module does not derive layout intent, route conductors, place labels, choose an external helper
stack, author cabinets, optimize physical paths, or persist user drag state as truth.

## Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test
```
