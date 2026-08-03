# M38 Boundary And Handoff

## M38 Owns

```text
RepresentationDefinition
  -> GraphicOccurrence
  -> strict PresentationConnector
  -> Theia paint
```

- one geometry/Anchor authority;
- one body/Anchor placement transform;
- exact connection endpoint attachment;
- one visible connector collection;
- complete source trace;
- hard deletion of fallback and duplicate authorities.

## M38 Does Not Own

- new engineer-facing layout or routing language;
- global placement quality;
- route lane/obstacle optimization;
- label optimization;
- second renderer or font engine;
- universal transport/schema framework;
- remote package resources;
- Engineering Component System.

## M39 Handoff

M39 may replace placement production with an internal Projection Composition Engine. It must still
produce the current Graphic Occurrence input and preserve exact placed Anchors.

## M40 Handoff

M40 may replace route-candidate generation with professional lane, obstacle, bundle, and optimization
logic. It must still pass through M38 strict connector attachment and validation.

## Rejected Designs

- CSS/HTML as Athena language;
- route bends and paint mechanics in normal source;
- Java2D or another compiler-side text renderer;
- SVG-owned Port/signal semantics;
- renderer endpoint/crossing repair;
- compatibility adapters for stale architecture.
