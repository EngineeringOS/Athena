# M7 Addendum

This addendum captures useful M7 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Position

The active sequence is now:

- **M5** - repository and package graph
- **M6** - semantic SCM
- **M7** - graphical projection and visual workbench

This split is already recorded in `docs/roadmap/athena-milestone-roadmap.md`.

## 2. Why M7 Must Build On M2 And M6

M2 already proved:

- explicit `Layout IR`
- explicit `Geometry IR`
- runtime-owned projection sessions
- synchronized multi-view projection over the same canonical engineering meaning

M6 already proved:

- semantic review
- semantic commit intent
- package-aware semantic history
- release relevance and contract-break risk on the same JVM path

The key M7 implication is:

- canonical semantics remain upstream
- layout and geometry remain downstream model layers
- graphical views consume those layers
- semantic SCM and package history stay available beside graphical projection, not inside it

## 2.1 Renderer Framing From The EPLAN Cross-Compare Note

The discussion under `draft/open/2026-07-09-Eplan-cross-compare-discuss.md` adds an important correction to how M7 should frame renderer work:

- Athena semantic authority should be treated as an engineering object graph, not a flattened device-symbol model.
- One engineering object may project into electrical, functional, physical, manufacturing, documentation, or other views without duplicating identity.
- IEC-style notations, QElectroTech-style elements, and similar symbol systems belong to view definition or renderer asset layers, not the semantic core.
- Renderer packs are closer to downstream asset bundles than to domain truth.
- Relationship-rich graph structure matters more than a drawing-owned component tree.

M7 should therefore prove the first serious renderer target while preserving this separation:

`Engineering Object Graph -> projection/view definition -> layout/geometry -> graphical surface`

The milestone should not accidentally hard-code "symbol library == semantic model" just because the first renderer target is visual.

## 3. Relationship To The Current Theia Shell

M4 already proved the shell, M5 proved package-aware repository operation, and M6 proved semantic SCM product consumption.

That means M7 should prefer:

- additive graphical surfaces inside the existing shell
- reuse of current repository, inspection, and semantic SCM context
- explicit coexistence with text and existing workbench panels
- a graph-first split workbench posture where the active graphical view stays primary and the authored `.athena` source remains visible as secondary context

It should avoid:

- replacing the workbench shell
- bypassing runtime/LSP boundaries for convenience
- creating a second frontend-owned semantic/projection model
- settling for a dashboard-style graphical panel that spends more space on descriptive text than on the engineering graph itself

## 3.1 Workbench Quality Bar For The First Renderer Proof

The current hosted graph panel proves product integration, but it is not yet the quality bar for the first serious renderer proof.

The next renderer-focused story should explicitly move Athena toward a professional engineering editing posture:

- use an infinite-canvas style diagram surface with pan and zoom instead of a bounded dashboard card
- keep the graph canvas primary and dense
- keep the authored `.athena` document visible beside it through a docked split layout
- let supporting semantic details live in compact side panels, toolbars, or properties surfaces rather than broad explanatory blocks
- look closer to an ECAD-style working surface, including the density expected from EPLAN-like editing workflows, without turning M7 into final UX/skin work or a literal visual clone

The architectural point is not cosmetic. A graph-first split workspace is part of proving that Athena can behave like a serious engineering workbench rather than a renderer demo.
This also aligns with the GLSP benchmark direction: a real diagram editor surface, not a report-style SVG preview.

## 3.2 Reference Product Framing For The Athena Workbench

The recent discussion clarified that Athena should use reference products in two different ways instead of collapsing them into one vague "professional" target.

**Architecture and workflow references**

- Code RealTime is a useful reference for a shared code-plus-diagram workflow rather than a one-way preview.
- HDevelopEVO is a useful reference for a structured engineering IDE posture: project-aware, tool-rich, and serious about large professional work.
- STM32CubeMX is a useful reference for task-focused engineering views, inspectors, and generated-artifact flows tied back to governed upstream meaning.

**Business and visual-workbench reference**

- QElectroTech is the closest current business and workspace reference for Athena's electrical-diagram posture.
- EPLAN remains the higher-end benchmark for density, seriousness, and engineering-workbench focus.

The key synthesis is:

- Athena should feel closer to QElectroTech and EPLAN at the workbench level.
- Athena should stay semantic-first and dual-surface at the architecture level.
- Athena should not narrow itself to "electrical drafting software" only, because the longer product direction still covers wider industrial views such as ECAD, CAD, SCADA, and related engineering projections.

That means M7 should borrow the professional posture, infinite-canvas behavior, and dense split-workbench feel from the electrical-tool references, while still preserving Athena's broader semantic-platform direction.

## 4. Projection Boundary Concerns The Architecture Must Resolve

### 4.1 Protocol Shape

- whether Athena needs a projection server boundary, direct runtime bridge, or a staged intermediate seam
- how the frontend consumes projection state without becoming semantic authority
- how transport remains inspectable and deterministic

### 4.2 Projection Model Ownership

- which types belong to kernel/runtime authority
- which types belong to the graphical adapter layer
- what the product shell is allowed to own locally
- how view definitions and renderer asset references are modeled without leaking renderer vocabulary back into the semantic core

### 4.3 Selection And Inspection Synchronization

- source -> graphical selection
- graphical selection -> semantic inspection
- graphical selection -> semantic SCM/history context where relevant
- later bidirectional flows only if they remain governed

### 4.4 Read-Only Versus Interaction Policy

- what is strictly read-only in M7
- whether one narrow move/select/focus interaction should be proven
- how future editable actions route back through commands instead of private canvas mutation

### 4.4.1 Bidirectional Code And Graph Direction After M7

The intended product direction is not a permanently read-only graph beside authoritative code.

Athena should eventually support governed bidirectional engineering work:

- authored code can refresh the graph deterministically
- approved graph actions can route back through governed commands and update the semantic/code authority path
- review should work from either side without creating two separate truths

Important boundary:

- this is not permission to widen M7 into unrestricted graphical authoring
- this is a carry-forward requirement for the next suitable milestone after the first renderer/workbench proof is stable
- any graph mutation must still compile back through Athena semantic authority rather than persisting as local canvas truth

### 4.5 Technology Evaluation

The `draft/m4/002-glsp.md` direction remains useful background, but M7 should decide from present constraints:

- JVM-first architecture
- Theia product shell already in place
- explicit M2 projection layers already in place
- later possible web/WASM direction still not the current milestone center

The architecture phase should evaluate whether GLSP-class technology is still the right fit or whether Athena now needs a narrower or more direct projection seam first.

### 4.6 First Renderer Target Choice

The cross-compare note implies that the first renderer target is a strategic architecture choice, not merely a UI preference.

M7 should evaluate which first proof best demonstrates the model:

- a relationship-forward graph view that makes engineering object structure explicit
- an IEC-style engineering view that proves renderer assets stay downstream of semantics
- a hybrid inspection view that shows both structure and renderer-mapped projection

The deciding criterion should be: which option proves canonical engineering identity plus multi-projection discipline most clearly with the least semantic distortion.
It should also prove that Athena can host that renderer in a dense professional workspace layout rather than a text-heavy showcase surface.

## 5. Likely Epic Spine

### Epic 1

Freeze the graphical projection foundation:

- projection boundary
- projection model ownership
- deterministic graphical projection feed
- read-only versus editable rules
- renderer/view-definition separation over canonical engineering objects

### Epic 2

Build the first serious graphical workbench surface:

- graphical panel or view delivery
- graphical navigation and inspection
- synchronization with source and semantic inspection
- first renderer target proof aligned with the engineering object graph

### Epic 3

Prepare the later interaction path safely:

- narrow governed interaction proof if warranted
- technology validation outcome
- explicit carry-forward rules for later editing milestones

## 6. Supporting Backlog That Must Not Replace M7 Core

These are valid items during M7 execution, but they should stay subordinate to graphical projection:

- semantic tokens
- hover / rename / formatting
- broad visual polish or skin-system work
- wider frontend regression harnesses unrelated to graphical projection
- semantic SCM feature expansion unrelated to graphical consumption
- full bidirectional code/graph editing beyond the narrow governed interaction slice

## 7. Carry-Forward Inputs From M6

M6 left these useful constraints for M7:

- keep `athena/semanticHistoryState` as the sole downstream input for future graphical package-history review
- add automated frontend interaction coverage for semantic SCM review, commit, and history panels

The architectural implication is:

- M7 graphical history or review visualization must consume existing M6 contracts
- it must not define a new history vocabulary just because the surface becomes visual
- M7 graphical surfaces should reuse semantic identity already proven by M6 rather than inventing renderer-specific change identities

## 8. Guardrail For Scope Control

When a proposed M7 feature appears, ask:

1. Does it introduce real graphical projection or visual workbench capability?
2. Does it preserve semantic authority while doing so?
3. Is it actually semantic SCM growth?
4. Is it actually generic IDE polish?

If the answer is:

- `1` and `2` -> M7 core
- `3` -> move back to M6-style backlog or later SCM milestone
- `4` -> supporting backlog only

## 9. Carry-Forward Product Direction

When M7 closes successfully, the next product-level step should no longer be framed as "add another graph panel."

It should be framed as:

- governed bidirectional code and graph edit/review
- preserving one semantic authority path across text and diagram interactions
- raising the domain workbench toward QElectroTech/EPLAN-class daily usability without narrowing Athena to electrical-only scope

That later milestone should prove that Athena is not just a semantic platform with a viewer, but a real engineering workbench where code and graph are two governed projections over the same model.

## 10. Closure Sync

M7 closed with the following concrete outcomes:

- `kernel/projection-model` became the dedicated renderer-neutral projection boundary
- runtime-owned `ProjectionSession` plus `ide/lsp` typed projection requests became the active projection authority path
- `integrations/graph-glsp` became the first translation-only adapter implementation under the generic `integrations/graph-*` rule
- the delivered workbench posture is graph-first and inspect-first rather than dashboard-first or unrestricted editing
- `cabinet` and `wiring` became the first extension-owned proof surfaces for downstream renderer mappings

This means the original M7 planning questions were resolved without changing the milestone center:

- the architectural boundary stayed generic
- the implementation selected one concrete adapter
- the interaction slice stayed narrow and governed
- bidirectional code/graph mutation remains the next milestone concern rather than hidden M7 scope
