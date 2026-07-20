# M20 Addendum

## Reference Inputs

These references shaped the M20 direction but do not become the milestone itself:

- M19 retrospective and usage summary
- `docs/usages/m19-proof-usage.md`
- `reference/structurizr/structurizr-autolayout/`
- `draft/screenshort/`
- `draft/elements-lib/0001-qelectrotech-elements.md`
- `draft/layouts/001-disucss.md`

## Product Interpretation

M20 is an engineering presentation fidelity milestone. It is not a semantic milestone and not a
stack-selection milestone.

The useful product thesis is:

> Athena should keep the same canonical sheet meaning from M19, but present it with a sheet layout
> model and drawing rules that customers can immediately accept as professional and usable.

That means the milestone is about:

- professional visual hierarchy
- readable density and spacing
- viewport choreography that feels steady
- source/reveal/Problems coherence surviving layout refinement
- small executable visual acceptance proofs
- a governed sheet layout contract between Presentation IR and rendering

It is not about:

- changing semantic authority
- adding cabinet preview
- adding repository/import ecosystem work
- deciding a new protocol/layout stack

## Technical Notes

### Presentation focus

M20 should mostly operate in the presentation layer. Layout facts and canonical identities still come
from upstream projection, but the sheet surface can improve:

- title block scale and placement
- label spacing and collision avoidance
- conductor legibility
- viewport fit and zoom defaults
- selection visibility
- density at common window sizes

### Sheet layout contract

The milestone should treat sheet layout as a governed model, not as renderer-local state.

Useful concepts:

- Sheet Layout Model
- Engineering Drawing Rules
- Representation Family
- Sheet composition and publication framing

That gives later milestones room for layout intelligence without turning M20 into a layout-engine
decision.

### Layout acceptance criteria

The sheet should be judged on whether it:

- reads as a professional engineering artifact at first glance
- keeps labels and routes understandable at dense scale
- avoids awkward overlaps and visual clutter
- keeps selected subjects visible
- keeps source and Problems navigation trustworthy

### Boundary notes

Keep these items explicit in PRD, epics, stories, and tests:

- cabinet preview is deferred
- repository/import ecosystem work is deferred
- full IEC breadth is deferred
- frontend-owned semantic resolution is forbidden
- final layout-stack selection is deferred
- layout intelligence and ELK-style optimization stay for later

## Planning Guidance

The M20 epic/story breakdown should favor:

- sheet composition modeling
- viewport and selection choreography
- deterministic visual acceptance proofs
- boundary stories that keep M20 from reopening M19 work

That keeps M20 focused on making Athena feel acceptable to customers without dragging it back into
semantic or platform churn.
