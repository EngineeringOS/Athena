# M8 Addendum

This addendum captures useful M8 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Position

The active sequence is now:

- **M5** - repository and package graph
- **M6** - semantic SCM
- **M7** - graphical projection and visual workbench
- **M8** - unified semantic mutation model

This means M8 is not "more graphics."
It is the first milestone where text and graph must act through one governed edit path.

## 2. Why M8 Must Build On M6 And M7

M6 already proved:

- semantic diff
- semantic review
- semantic commit intent
- package-aware semantic history

M7 already proved:

- renderer-neutral projection boundary
- runtime-owned `ProjectionSession`
- graph-first workbench delivery
- translation-only graph adaptation
- inspect-first interaction posture

The key M8 implication is:

- review authority is already upstream
- projection authority is already upstream
- the next missing proof is mutation authority across both text and graph

## 3. Mutation Taxonomy That Architecture Must Respect

### 3.1 Semantic Mutation

Semantic mutation changes engineering meaning.

Examples:

- add component
- remove connection
- change motor power
- modify PLC mapping

Expected path:

`command -> validation -> canonical state update -> review facts -> projection refresh`

### 3.2 Projection Mutation

Projection mutation changes persisted representation without changing engineering truth.

Examples:

- move node position
- regroup cabinet placement
- change routed presentation metadata

Expected path:

`command -> governed projection/layout metadata update -> projection refresh`

### 3.3 Transient Interaction

Transient interaction is never persisted as truth.

Examples:

- pan
- zoom
- hover
- temporary highlight
- disposable selection affordance

Expected path:

- frontend-local only
- discarded or recomputed on refresh

## 4. One Mutation Authority Rule

The architecture must freeze one non-negotiable rule:

- text editing does not bypass mutation governance
- graph interaction does not bypass mutation governance
- no renderer may serialize local state as canonical truth

That does **not** mean source editing should become unnatural.
It means that, at the product boundary, accepted change must still converge into Athena-owned command, validation, and review meaning.

## 5. Graph Gesture To Command Intent

M8 should not describe graph editing in renderer-native terms such as "drag and save."

Instead, supported gestures must translate into Athena-owned command intent.

Example:

```text
Drag KM1 in cabinet view
        ->
MoveProjectionNode(id="KM1", projection="cabinet", position=(x, y))
```

or:

```text
Select M1 and change power
        ->
UpdateMotorPowerCommand(id="M1", power="7.5kw")
```

The important boundary is:

- renderer emits intent
- runtime decides
- validation judges
- refresh recomputes result

## 6. Projection Ownership Contract

Each projection should declare four things explicitly:

1. what it can display
2. what it can edit
3. what command intents it may emit
4. what state it owns locally

This prevents later milestones from inferring editability from accidental UI behavior.

Example:

`Cabinet`

- may display:
  - devices
  - relationships
  - grouped placement
- may edit:
  - position
  - grouping
- may emit:
  - move projection node
  - regroup projection nodes
- local-only:
  - zoom
  - pan
  - temporary highlight

## 7. Unified Review Model

M8 should preserve the M6 semantic SCM rule:

- review vocabulary stays semantic
- graph-originated change does not invent a renderer-specific review language

The same accepted mutation should be explainable through:

- source context
- graph context
- semantic SCM context

This is the real meaning of "bidirectional editing" for Athena.
It is not only about changing things from two surfaces.
It is about keeping review and reveal coherent after the change.

## 8. Likely Narrow Proof Targets

M8 should stay narrow.

The first implementation should likely prove:

- one semantic mutation path
- one projection mutation path
- one rejection/validation feedback path
- one reveal/review path across source and graph

Good candidate examples:

- semantic mutation:
  - update motor power
  - connect supported ports
- projection mutation:
  - move a cabinet node
  - change grouping inside a cabinet projection

## 9. Risks That Must Not Be Hidden

- direct manipulation can tempt the team into renderer-owned state
- projection mutation can drift into semantic mutation if boundaries are vague
- review facts can fork if graph-originated changes get their own vocabulary
- source editing and graph editing can diverge if they do not converge through one command model
- domain richness can prematurely pull M8 into notation depth and symbol-library work

## 10. Scope Guardrail

When a proposed M8 feature appears, ask:

1. Does it prove one mutation authority across text and graph?
2. Does it preserve semantic and review authority?
3. Is it really a notation/symbol-pack concern instead?
4. Is it really broad graphical authoring instead?

If the answer is:

- `1` and `2` -> M8 core
- `3` -> later notation milestone
- `4` -> later authoring milestone

## 11. Carry-Forward Product Direction

If M8 succeeds, Athena should no longer be described as:

- a semantic platform with a graph viewer

It should instead be described as:

- a governed engineering workbench where source and graph are two editing surfaces over one mutation and review model

What remains after M8 should then be:

- richer domain workflows
- deeper notation systems
- broader authoring breadth
- stronger review and approval workflows

But those later milestones should inherit one mutation authority instead of inventing their own.
