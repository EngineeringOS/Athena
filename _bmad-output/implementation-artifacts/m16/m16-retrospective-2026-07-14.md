# M16 Retrospective

Date: 2026-07-14
Closeout Updated: 2026-07-14

## What Worked

- Keeping M5 as the repository/package authority and M8 as the only mutation authority prevented the milestone from drifting into UI-owned reuse state.
- The checked-in proof repository made the milestone materially stronger because catalog, preview, acceptance, and origin inspection all ran against real governed package inputs.
- The accepted-expansion model held up well once origin and membership facts were carried through the command history instead of being reconstructed from frontend state.
- The end-to-end proof path across runtime, LSP, Theia, and Electron smoke verification gave better closeout confidence than unit coverage alone.

## What Needed Tightening

- The proof repository lock initially passed semantic validation but was not in canonical compiler-owned form. The closeout fix was to materialize the lock through compiler-owned repository resolution instead of hand-authoring it.
- Origin traceability only became durable after the inspection path was tied to applied accepted-expansion bundles recorded in command history. Anything weaker would have left traceability dependent on ephemeral preview state.
- Verification on this Windows repo needed strict sequential Gradle execution. Earlier timeouts looked like test failures until the stale daemon state was cleaned and rerun under the repo rule.

## Architecture Notes Recorded

- Keep the current compiler/parser path for now so M16 can close on working governed reuse behavior.
- Record ANTLR4 as the planned final choice for the authored-language parser when Athena grows more syntax such as imports and richer control forms.
- Record Tree-sitter as the planned IDE-facing parsing layer for editor features, not as the canonical compiler/parser replacement.

## Carry Forward

- Future reuse milestones should keep every entry surface converging through the same runtime-owned reuse contract and M8-backed mutation path.
- Every milestone that claims governed behavior should ship at least one checked-in repository-backed proof corpus plus one UI/E2E verification path.
- Origin inspection should stay canonical-state-based so preview dismissal, UI refresh, or future surface changes cannot erase accepted reuse traceability.
