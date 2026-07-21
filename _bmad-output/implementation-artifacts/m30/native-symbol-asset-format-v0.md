# M30 Native Symbol Asset Format v0

## Decision

M30 Story 2.1 uses UTF-8 Java `.properties` files as the first native representation asset format.

JSON/YAML remains the preferred long-term direction for authorable symbol libraries, but
`:kernel:representation-model` currently has no JSON/YAML parser dependency. Story 2.1 avoids adding
a new dependency without a wider build decision and keeps the first loader JVM-only, deterministic,
and browser-independent.

## Runtime Asset Boundary

Allowed runtime asset:

```text
*.properties
```

Forbidden runtime asset:

```text
*.elmt
QET XML
SVG fragments as semantic authority
```

QET `.elmt` may only be used later by an offline converter targeting Athena Representation
Definition IR candidates. It must not be loaded by Athena product runtime.

## Required Fields

```properties
library.id=athena.native.iec-v0

symbol.0.id=iec.motor.compact
symbol.0.version=1.0.0
symbol.0.lifecycle=ACTIVE
symbol.0.kind=MOTOR_LOAD
symbol.0.bounds.width=32
symbol.0.bounds.height=32
```

## Primitive Fields

Rectangle:

```properties
symbol.0.primitive.0.type=rectangle
symbol.0.primitive.0.id=body
symbol.0.primitive.0.x=0
symbol.0.primitive.0.y=0
symbol.0.primitive.0.width=32
symbol.0.primitive.0.height=32
```

Line:

```properties
symbol.0.primitive.0.type=line
symbol.0.primitive.0.id=line-1
symbol.0.primitive.0.x1=0
symbol.0.primitive.0.y1=0
symbol.0.primitive.0.x2=32
symbol.0.primitive.0.y2=0
```

Circle:

```properties
symbol.0.primitive.0.type=circle
symbol.0.primitive.0.id=mark
symbol.0.primitive.0.cx=16
symbol.0.primitive.0.cy=16
symbol.0.primitive.0.r=10
```

## Terminal, Label, Variant, And Style Fields

```properties
symbol.0.terminal.0.id=terminal-1
symbol.0.terminal.0.role=POWER_INPUT
symbol.0.terminal.0.x=0
symbol.0.terminal.0.y=16
symbol.0.terminal.0.side=LEFT
symbol.0.terminal.0.marker=CIRCLE
symbol.0.terminal.0.number=1

symbol.0.label-slot.0.id=device-tag
symbol.0.label-slot.0.role=DEVICE_TAG

symbol.0.label-anchor.0.id=device-tag
symbol.0.label-anchor.0.role=DEVICE_TAG
symbol.0.label-anchor.0.x=0
symbol.0.label-anchor.0.y=-8

symbol.0.variant.0.id=compact

symbol.0.style-token.0.name=stroke
symbol.0.style-token.0.value=iec.black
```

## Migration Note

When Athena adopts a structured JSON/YAML parser for representation assets, this format should be
treated as a v0 bootstrap format. The contract to preserve is `NativeRepresentationLibrary` and
`RepresentationDefinition`, not the `.properties` syntax itself.
