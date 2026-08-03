# `:kernel:drawing-composition`

This module composes validated placement, routing, and sheet facts into renderer-neutral engineering drawing structures.

## Responsibilities

- Compile Cabinet composition and visual transforms from physical facts.
- Compile route geometry from governed route intents and placement facts.
- Build drawing sheet structure, composition, and cross-reference facts.
- Preserve source and semantic traceability through composition.

## Boundaries

This module does not own engineering semantics, package selection, physical constraints, source mutation, or backend painting.

## Verification

```powershell
.\gradlew.bat :kernel:drawing-composition:test
```
