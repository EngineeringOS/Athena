# `:kernel:layout-engine`

English

The `:kernel:layout-engine` module defines the M21 strategy boundary that turns layout intent
snapshots into Athena-owned schematic placement and region facts.

## Responsibilities

- Define a small strategy interface for schematic layout.
- Provide the first deterministic rule-based schematic strategy.
- Define subordinate helper proposal normalization without selecting any concrete helper stack.
- Preserve canonical subject, occurrence, intent, snapshot, and source-span identity in placement
  facts.
- Expose coherent schematic regions for power, control, terminal, load, and annotation zones when
  those zones are present in layout intent.
- Keep the renderer, Theia, CSS, DOM, canvas interaction state, and external helper choices outside
  the strategy contract.

## Boundaries

Helper proposals are subordinate. They must normalize into Athena-owned placement facts, and they may
not replace snapshot, subject, occurrence, role, zone, or source-span identity from layout intent.
Region facts are derived from normalized placement facts and remain explainable through the same
layout intent ids, occurrence ids, roles, and zones. Region ids are scoped by snapshot id; consumers
that persist or compare region facts must use the snapshot id and region id together.

This module does not derive layout intent, route conductors, place labels, choose an external helper
stack, author cabinets, optimize physical paths, or persist user drag state as truth.

## Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:layout-engine:test
```
