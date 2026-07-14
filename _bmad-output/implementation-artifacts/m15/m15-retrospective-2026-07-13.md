# M15 Retrospective

## What Worked

- Reusing M8 mutation authority prevented M15 from creating a second write path.
- Reusing M14 component knowledge prevented the component panel and inspector from inventing a second catalog model.
- Keeping accepted actions source-backed made proof verification inspectable and deterministic.
- The narrow Siemens-first slice kept guided authoring implementable without pretending to solve full ECAD authoring yet.

## What Needed Tightening

- Authoring templates initially emitted invalid authored `type` symbols for electrical validation. The fix was to keep `componentRef` as the real semantic identity while mapping authored `type` to currently valid baseline device types.
- Connect flow needed one explicit shared compatibility model so graph filtering and inspector connection state used the same meaning.
- Example and proof assets need to keep following the UTF-8 and README rules because they are part of the user-facing proof path.
- The attempt to dock stock Theia Outline under Explorer looked small but was actually a framework-layout problem. Document symbols and AST data stayed valid, but the reparented widget collapsed to zero inner height, so the supported path stays the native right sidebar until Athena owns that panel itself.

## Risks Remaining

- Guided authoring is still narrow and not yet a full palette, template, or macro workflow.
- Connection highlighting can still be visually refined without changing semantic ownership.
- More complex electrical cases and denser proof repositories still belong to later milestones.

## Carry-Forward

- Keep all future authoring surfaces above the same intent and preview contract.
- Keep renderer and presentation work downstream of canonical identity and component knowledge.
- Treat repository-backed proof scenarios as milestone-close evidence, not optional demos.
