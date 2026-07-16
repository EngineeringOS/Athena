# M19 Addendum

## Reference Inputs

These references shaped the M19 direction but do not become the milestone itself:

- `draft/screenshort/`
- `reference/structurizr/structurizr-autolayout/`
- `draft/elements-lib/0001-qelectrotech-elements.md`
- `docs/roadmap/athena-milestone-roadmap.md`
- M11, M14, M15, and M18 usage/retrospective records

## Product Interpretation

M19 is a first professional engineering sheet workflow milestone, not a renderer-only milestone.

The useful product thesis is:

> Athena should show canonical engineering meaning in a professional sheet workflow that feels
> credible to end users, while keeping semantic truth upstream of projection and rendering.

That means the milestone is about:

- one understandable engineering sheet workflow
- Theia IDE coherence
- deterministic sheet and layout facts
- a small proof corpus
- careful scope discipline

It is not about:

- cloning EPLAN
- building a full IEC element catalog
- launching a public package repository
- moving semantic authority into the frontend

## Technical Notes

### Layout pattern

`reference/structurizr/structurizr-autolayout/` is useful as a pattern:

1. export a semantic/view model
2. run a deterministic layout pass
3. read back positions and page bounds
4. apply those facts to the view

Athena should keep that flow Athena-owned. If a layout engine is introduced later, it should be a
projection helper, not an authority.

For M19, the first proof corpus should assume page-anchored, grid-assisted placement and mostly
orthogonal conductor routing so the same input state can reproduce the same sheet and publication
facts without ambiguity.

### Sheet IR

The sheet is not just presentation state.

It should carry publication semantics such as:

- page size
- frame and coordinate zones
- title block and revision metadata
- sheet identity
- view composition

That sheet IR sits between projection and rendering. The renderer paints it, but does not own it.

### Technology selector discussion

The current repository already has a GLSP-shaped adapter seam, so the M19 question is not whether
to invent a new rendering boundary. The open question is which protocol/layout stack should sit
behind the sheet IR for the first serious engineering-sheet work.

Likely candidates:

- GLSP as the diagram protocol
- Sprotty as the SVG client/rendering layer
- ELK as the deterministic layout engine

That looks like a coherent fit for Athena's separation of authority, but it should be treated as a
separate tech-selector decision, not silently absorbed into the PRD. Until that discussion is
resolved, M19 should keep the sheet IR contract stable and let the adapter layer absorb the choice.

### Element anatomy

`draft/elements-lib/0001-qelectrotech-elements.md` is useful for understanding the anatomy of
engineered symbols:

- primitives
- terminals
- hotspots
- dynamic text
- metadata

For M19, this should inform a tiny Athena-native symbol subset. It should not become a full library
import or a symbol-system project by itself.

### Visual cues to preserve

The screenshot folder points to the cues users will recognize as serious engineering UI:

- sheet frames
- grid and coordinates
- title blocks
- labels and tags
- cross references
- cabinet and topology families

These are cues, not a design system mandate. They should inform the first schematic proof and the
minimum credible sheet definition.

## Boundary Notes

Keep these items explicit in PRD, epics, stories, and tests:

- full EPLAN parity is deferred
- cabinet preview is deferred from M19
- full cabinet layout intelligence is deferred
- IEC library breadth is deferred
- public repository/import ecosystem work is deferred
- frontend-local semantic resolution is forbidden

## Planning Guidance

The M19 epic/story breakdown should favor:

- a schematic-first user workflow
- first-class sheet IR plus projection and layout contracts
- source/reveal/inspector coherence
- a small, executable proof corpus
- cabinet preview held for a later milestone

That keeps M19 useful to end users without forcing rollback or a large refactor surface.
