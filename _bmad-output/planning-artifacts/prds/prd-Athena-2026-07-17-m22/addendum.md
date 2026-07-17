# M22 Addendum

## Original Target Interpretation

The original M22 target after M21 should be:

```text
Governed Auto Layout And Layout Round-Trip Foundation
```

M21 proved the contracts for layout intelligence. M22 should make those contracts visibly useful and
round-trippable:

```text
M21:
  layout intent and facts exist

M22:
  layout facts visibly improve the sheet
  user adjustments become governed `.athena` layout intent
```

This keeps M22 aligned with the EngineeringOS direction:

```text
semantic meaning
  > projection
  > presentation
  > layout intent
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

ELK is useful in M22 as an adapter spike.

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

Correct posture:

```text
Athena layout intent
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

The exact syntax should be decided during architecture/story work. Candidate directions:

### Layout Block

```athena
view schematic MainSheet {
  layout {
    place PLC1 zone control anchor grid(8, 4)
    place PSU1 zone power before PLC1
    align terminal-blocks vertical
    route PLC1.out to M1.in orthogonal lane control
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
- persist placement hints first
- keep route/label persistence optional unless the implementation remains clean
- avoid raw pixel names in source
- use grid coordinates or engineering zones when possible

## Screenshot Reference Usage

`draft/screenshort` should influence acceptance, but it should not turn M22 into full EPLAN parity.

Use the screenshots to define:

- sheet density expectations
- professional spacing and alignment cues
- orthogonal routing expectations
- label placement expectations
- visible engineering zones

Do not require:

- full toolbar parity
- complete symbol library parity
- all EPLAN panels
- manufacturing package output
- full authoring workflow depth

## M22 Risk Notes

- ELK may improve graph neatness without engineering readability. Athena rules must remain primary.
- Layout round-trip may become too broad if route and label persistence are included too early.
- `.athena` layout syntax can create future refactor cost if it overfits M22's first renderer.
- A visible improvement milestone can fail if acceptance relies only on tests. The sample project
  must remain the proof surface.

## Recommended Epic Shape

1. Visible M22 sample and acceptance baseline.
2. Governed layout solver and ELK adapter spike.
3. Professional schematic auto-layout proof.
4. Layout adjustment intent and `.athena` round-trip.
5. IDE coherence, regression, and scope guardrails.
