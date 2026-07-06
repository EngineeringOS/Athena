# Athena M1 Command History Boundary

## Purpose

Story `2.3` extends the runtime-owned command path with a real command journal.

The goal is not a review UI yet. The goal is to prove that command execution leaves behind stable runtime-owned records that can drive undo, redo, replay, and serialization without relocating ownership into viewer callbacks or caller-side rollback code.

## Ownership Boundary

- `Athena Runtime`
  - remains the owner of command history lifecycle for the active project
- `AthenaExecutionContext`
  - owns the active canonical project snapshot plus the active command-history state
- `AthenaCommandRuntimeService`
  - records successful commands into runtime-owned history
  - performs undo, redo, and replay through the same runtime-owned semantic mutation rules
  - serializes recorded command history into deterministic runtime-owned payloads
- `Engineering IR`
  - remains the only canonical semantic authority before, during, and after history operations

History is operational metadata over canonical semantics. It does not become a second semantic model.

## History Rule

Each successful command record carries:

- stable runtime-owned command identity
- command kind and payload
- changed semantic identities
- canonical document before the command
- canonical document after the command

That is enough to support:

- inspection
- undo
- redo
- replay
- deterministic serialization

without requiring caller-side reconstruction of private UI state.

## Replay Rule

Replay rebuilds state from the stored baseline document plus the recorded command sequence.

This matters because replay is not supposed to be:

- viewer-local cache restoration
- ad hoc object resurrection
- caller-side reconstruction of semantic intent

Replay stays inside runtime-owned command semantics.

## Serialization Rule

The first serialization slice emits deterministic runtime-owned JSON over:

- command identity
- command kind
- command payload
- history status
- changed semantic identities
- lightweight before/after semantic summaries

It does not inspect viewer-local state, plugin-private state, or external persistence infrastructure.

## Surface Proof

Story `2.3` adds CLI-facing history operations over the same runtime-owned session:

- `history`
- `serialize-history`
- `undo`
- `redo`
- `replay`

These remain adapters to the same runtime-owned command and history boundary.

## Non-Goals

Story `2.3` does not yet introduce:

- semantic diff presentation
- GUI history panels
- persisted project storage
- branching or merge semantics
- plugin-contributed history behavior

Those remain later Epic `2` work.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :apps:cli:test
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove runtime-owned command history, undo, redo, replay, and serialization remain deterministic and canonical-state aligned on Java `25`.
