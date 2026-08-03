# M37 Usage Handoff

## Scope

M37 delivers Engineering Connectivity and Professional Routing Grammar for one visible product surface: the dedicated professional Control Drawing compiled from `examples/m37/professional-control-drawing`.

Athena source remains the single source of truth. Projection, routing, SVG geometry, planner facts, and renderer payloads are derived.

## How To Run

```powershell
.\gradlew.bat --no-daemon --console=plain :ide:lsp:installDist
yarn --cwd ide build
yarn --cwd ide start:smoke:m37
```

The smoke opens:

```text
examples/m37/professional-control-drawing
```

Expected source:

```text
src/com/engineeringood/m37/professionalcontroldrawing/01-professional-control-drawing.athena
```

Expected product surface:

```text
Control Drawing
```

## Architecture Rules Preserved

- Athena source owns Engineering Connectivity Contracts, Interfaces, Ports, Connections, Connection Intent, Projection Policy, and External Evidence.
- SVG owns only package-local geometry and `data-athena-ref="anchor:<id>"` geometry hints.
- XML is not runtime authority.
- Planner graph is transient IR.
- Renderer paints Graphic Primitive IR only.
- Proof fields are computed from compiled facts and diagnostics.
- Production `src/main` contains no proof/demo/sample/milestone/version-named classes.

## Current Proof Summary

- Active backing view: `schematic`
- Active product surface: `Control Drawing`
- Route count: `11`
- Graphic occurrences: `10`
- Presentation terminals: `28`
- Last recorded compile-to-presentation refresh: `66 ms`
- Last recorded zero-defect gates: passed

Final verification commands are recorded in Story 6.3.
