# Architecture Review - Divergence Attack

## Verdict
Pass with one watch item.

## Attack Cases

### Two implementers choose different descriptor syntaxes
Closed by AD-2: only `symbol.yaml` with schema `athena-symbol-v1`; no fallback parser.

### Frontend accepts invalid reconnect while server rejects it
Closed by AD-3, AD-7, and AD-9: frontend previews only; server validates and publishes.

### Style edit mutates engineering source or geometry
Closed by AD-8 and AD-3: style writes same-basename style companion; SVG geometry and Port semantics stay separate.

### SVG export and Theia render diverge
Closed by AD-1 and AD-10: both consume `AthenaDiagramScene`; SVG canonical, PNG raster proof only.

## Watch Item

AD-11 says Must slice before Should slice. Sprint/story status must enforce this order; architecture alone cannot prevent backlog disorder.
