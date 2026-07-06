# Athena M1 AI Proposal Boundary

## Purpose

Story `2.9` adds the narrow optional AI proof for M1.

This slice does not make AI a privileged author of canonical state.
It proves the opposite architectural rule: AI may suggest command-shaped changes, but canonical `Engineering IR` changes only after an explicit acceptance routes that proposal through the same runtime-owned command path used by every other surface.

## Ownership Boundary

- `Engineering IR`
  - remains the only canonical semantic authority
- `AthenaAiProposalRuntimeService`
  - owns the pending AI proposal queue for the active project
  - keeps proposals outside command history until explicit acceptance
- `AthenaCommandRuntimeService`
  - remains the only semantic mutation path into canonical runtime state
  - records accepted AI proposals in the same command history used by standard runtime commands
- `AthenaCliSessionStore`
  - persists both accepted command history and pending AI proposals for one-shot CLI invocations
  - preserves accepted-command origin metadata so restored history remains inspectable

AI does not get a second semantic model, direct graph mutation path, or history bypass.

## Acceptance Rule

The first optional AI surface is intentionally small:

- queue one `CONNECT_PORTS` command candidate
- inspect pending proposals
- accept one proposal
- reject one proposal

Acceptance does not apply a special AI mutation path.
Instead, acceptance calls the normal command runtime with command origin `AI_ACCEPTED`.

That keeps these runtime behaviors aligned:

- semantic validation
- incremental recompute
- viewer projection updates
- command history
- diff inspection
- replay and undo or redo semantics
- deterministic history serialization

Rejected or validation-failed proposals leave canonical state unchanged and remain outside history.

## Inspectability Rule

Accepted AI proposals are inspectable through the same runtime surfaces as standard commands, plus one extra origin marker:

- command id
- command kind
- command origin
- changed semantic identities
- before and after canonical document snapshots

The current origin values are:

- `STANDARD`
- `AI_ACCEPTED`

This is enough for later frontends to distinguish accepted AI-originated commands without inventing a second history model.

## Non-Goals

Story `2.9` does not introduce:

- autonomous AI mutation
- background agent execution
- prompt storage or model integration
- AI-specific semantic objects in canonical state
- domain-specific AI reasoning beyond proposing one runtime command

M1 still treats AI as an optional frontend adapter, not a sovereign runtime actor.

## Verification Path

From the repo root with Java `25` active:

```powershell
java25
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests "com.engineeringood.athena.runtime.AthenaAiProposalRuntimeServiceTest"
.\gradlew.bat --no-daemon --console=plain :apps:cli:test --tests "com.engineeringood.athena.cli.AthenaAiProposalCliTest"
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :apps:cli:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove that optional AI proposals stay outside canonical state until acceptance and that accepted proposals reuse the existing runtime-owned command boundary end to end.
