# M22 Addendum

## Original Target Interpretation

The refined M22 target after M21 should be:

```text
Governed Layout Optimization And Layout Round-Trip Foundation
```

M21 proved the contracts for layout intelligence. M22 should make those contracts visibly useful,
constraint-driven, and round-trippable:

```text
M21:
  layout intent and facts exist

M22:
  layout constraints guide optimization
  layout facts visibly improve the sheet
  user adjustments become governed `.athena` layout intent
```

This keeps M22 aligned with the EngineeringOS direction:

```text
semantic meaning
  > projection
  > presentation
  > layout intent
  > layout constraints
  > layout facts
  > renderer
```

and avoids becoming:

```text
canvas position
  > hidden UI state
  > source follows the drawing
```

## ELK Posture

ELK is useful in M22 as an optional experimental adapter spike.

Use ELK for:

- graph arrangement assistance
- hierarchy layout
- orthogonal edge routing assistance
- spacing and simple collision avoidance

Do not use ELK for:

- semantic authority
- engineering role decisions
- representation-family decisions
- persistence format
- final stack decision
- direct renderer truth
- advanced electrical routing intelligence
- standards-specific label generation

Correct posture:

```text
Athena layout intent
  -> Athena layout constraints
  -> Athena layout rules
  -> ELK adapter input
  -> ELK output
  -> normalized Athena layout facts
```

Wrong posture:

```text
ELK output
  -> renderer truth
  -> source follows pixels
```

## Candidate `.athena` Layout Hint Shapes

The exact syntax should be decided during architecture/story work. The syntax should prefer
declarative constraints over authored pixel coordinates.

Bad direction:

```athena
layout {
  KM1.x = 300
  KM1.y = 500
}
```

Better direction:

```athena
layout {
  place KM1 {
    near M1
    align vertical with QF1
    group motor-control
  }
}
```

Candidate directions:

### Layout Block

```athena
view schematic MainSheet {
  layout {
    place PLC1 zone control anchor grid(8, 4)
    place PSU1 zone power before PLC1
    align terminal-blocks vertical
  }
}
```

### Subject-Local Hint

```athena
device PLC1 {
  type Switch
  layout schematic {
    zone control
    anchor grid(8, 4)
  }
}
```

### Separate Projection Hint

```athena
layout schematic MainSheet {
  PLC1 {
    zone control
    anchor grid(8, 4)
  }

  connection PLC1.out -> M1.in {
    route orthogonal lane control
  }
}
```

Preferred direction for M22:

- start with a small layout block or projection hint
- persist component placement, alignment, and grouping hints first
- defer route/label persistence unless the implementation is mechanically simple
- avoid raw pixel names in source
- use grid coordinates or engineering zones when possible
- prefer constraints such as near, below, aligned-with, grouped-with, and preferred-zone over absolute
  coordinates

## Constraint Model Notes

M22 should introduce a Layout Constraint Model between layout intent and solved layout facts:

```text
Engineering IR
  -> Projection Model
  -> Presentation IR
  -> Layout Intent Model
  -> Layout Constraint Model
  -> Layout Optimization Layer
  -> Layout Facts
  -> Presentation Snapshot
  -> Theia Renderer
```

Useful initial constraints:

- `near(subject)`
- `below(subject)`
- `aligned-with(subject, axis)`
- `grouped-with(subjects)`
- `preferred-zone(zone)`
- `preserve-order(subjects)`

Do not make M22's first constraint model a full constraint solver language. It should be small,
inspectable, and enough to prove round-trip intent.

## Screenshot Reference Usage

`draft/screenshort` should influence acceptance, but it should not turn M22 into full EPLAN parity.

Use the screenshots to define:

- sheet density expectations
- professional spacing and alignment cues
- basic orthogonal edge-routing expectations
- basic label overlap-avoidance expectations
- visible engineering zones

Do not require:

- full toolbar parity
- complete symbol library parity
- all EPLAN panels
- manufacturing package output
- full authoring workflow depth
- advanced routing intelligence
- standards-specific label intelligence

## M22 Risk Notes

- ELK may improve graph neatness without engineering readability. Athena constraints and rules must
  remain primary.
- Layout round-trip may become too broad if route and label persistence are included too early.
- `.athena` layout syntax can create future refactor cost if it overfits M22's first renderer.
- A visible improvement milestone can fail if acceptance relies only on tests. The sample project
  must remain the proof surface.

## Recommended Epic Shape

1. Visible M22 sample and acceptance baseline.
2. Layout constraint model and governed layout solver boundary.
3. Optional experimental ELK adapter spike.
4. Professional schematic layout optimization proof.
5. Component adjustment intent and `.athena` round-trip.
6. IDE coherence, regression, and scope guardrails.
