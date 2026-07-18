# `:kernel:layout-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:layout-model` module defines Athena's explicit layout contracts. These types sit downstream of canonical `Engineering IR` and describe how one supported view wants semantic truth arranged without becoming semantic truth themselves.

M21 adds first-class schematic layout intent snapshots. A layout intent snapshot captures explainable
engineering placement meaning before any strategy solves coordinates: role, preferred zone,
priority, alignment, relationship constraints, canonical subject identity, occurrence identity,
snapshot identity, and source span.

M23 adds a separate authored layout-intent priority vocabulary for source-owned hints:
`HARD`, `SOFT`, and `PREFERENCE`. The admitted M23 layout syntax currently defaults every
authored statement to `PREFERENCE`; `HARD` and `SOFT` exist as model capacity for later conflict
handling. This authored priority type deliberately does not replace or reinterpret the existing
solver-facing `LayoutPriority` values.

## Responsibilities

- Define `ViewDefinition` for supported projection contexts.
- Define governed projection-family contracts that classify downstream presentation families without creating a second semantic authority.
- Define the first `Layout IR` document, group, node, relative-placement, and relationship contracts.
- Define M21 layout intent snapshots before solved layout facts.
- Keep canonical semantic identity as a first-class field across layout artifacts.
- Provide a durable kernel-owned home for layout intent instead of hiding it in UI or renderer code.

## Main Types

- `ViewDefinition`
- `ProjectionFamilyContract`
- `ElectricalProjectionDescriptor`
- `ElectricalProjectionFamily`
- `LayoutDocument`
- `LayoutGroup`
- `LayoutNode`
- `LayoutNodeId`
- `LayoutRelationship`
- `LayoutRelativePlacement`
- `LayoutSnapshotId`
- `LayoutIntentId`
- `LayoutOccurrenceId`
- `LayoutSourceSpan`
- `LayoutIntentItem`
- `LayoutIntentSnapshot`
- `SchematicLayoutRole`
- `SchematicLayoutZone`
- `LayoutAlignment`
- `LayoutPriority`
- `AuthoredLayoutIntentPriority`
- `AuthoredLayoutIntent`
- `AuthoredLayoutIntentStatement`
- `LayoutIntentRelationshipConstraint`

## Dependencies

- `:kernel:engineering-model`

## Boundaries

This module does not derive layout from semantic state, does not own runtime projection sessions, does not emit geometry, does not route conductors, does not avoid labels, does not choose an adapter, and does not mutate engineering semantics. It is the durable model layer that later compiler and runtime stories consume when deriving grouping, ordering, relative placement, emphasis, governed projection-family classification, and M21 schematic layout intent from canonical semantics.

## Verification

```bash
./gradlew :kernel:layout-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:layout-model:test
```
