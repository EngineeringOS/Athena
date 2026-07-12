# `:kernel:layout-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:layout-model` module defines Athena's first explicit layout-intent contracts for M2. These types sit downstream of canonical `Engineering IR` and describe how one supported view wants semantic truth arranged without becoming semantic truth themselves.

## Responsibilities

- Define `ViewDefinition` for supported projection contexts.
- Define governed projection-family contracts that classify downstream presentation families without creating a second semantic authority.
- Define the first `Layout IR` document, group, node, relative-placement, and relationship contracts.
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

## Dependencies

- `:kernel:engineering-model`

## Boundaries

This module does not derive layout from semantic state, does not own runtime projection sessions, does not emit geometry, and does not mutate engineering semantics. It is the durable model layer that later compiler and runtime stories consume when deriving grouping, ordering, relative placement, emphasis, and governed projection-family classification from canonical semantics.

## Verification

```bash
./gradlew :kernel:layout-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:layout-model:test
```
