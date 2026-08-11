# M44 Review Correction - Complete Engineering Authoring Transaction Loop

## Verdict

Accepted. M44 remains aligned with Athena target and EPLAN lessons only if it is treated as the first
complete engineering authoring transaction milestone, not a canvas-only milestone.

## Alignment With Athena Principles

- Athena source remains Engineering Reality authority.
- Canvas is a projection/editor surface, not hidden engineering source.
- User actions become classified Edit Operations.
- Server validates, mutates source atomically, recompiles, and republishes Canonical Scene.
- Renderer state is disposable adapter state.
- SVG and library metadata provide geometry/compatibility, not engineering truth.

## Alignment With EPLAN Lessons

- Device/Entity, Function, Part, Symbol, Relationship, Sheet, Route stay separate.
- Function-level representation is required; one Entity can have multiple Function/Symbol occurrences.
- Part replacement preserves engineering identity.
- Connection truth is Relationship/domain/flow/direction, not route paint.
- Library/master data is governed package data, not project-local drawing copies.
- Macro is representation reuse only in M44; engineering solution reuse belongs to future Pattern authority.

## Required Corrections Applied

- Added operation authority classes:
  - `PresentationEditOperation`: Move, Align, Distribute, Snap, Set Style.
  - `RepresentationEditOperation`: Change Symbol.
  - `EngineeringEditOperation`: Reconnect, Bind Part.
- Added `RepresentationBinding` and `RepresentationInstance`.
- Added Operation Journal as accepted source-transaction history.
- Strengthened Undo/Redo: journal-backed source transactions, never mouse events.
- Strengthened renderer boundary: Konva node tree is disposable.
- Changed M44 closure from Must/Should feature split to one golden authoring loop.
- Updated epics/stories so operation classification and journal foundation happen before edit breadth.

## Golden Loop Required For Closure

PLC, Contactor, Motor with real symbols; Move; Align; Change Symbol; valid Reconnect; invalid Reconnect
rejection; Bind/replace Part; Undo; Redo; restart/reopen; same accepted identities and result.
