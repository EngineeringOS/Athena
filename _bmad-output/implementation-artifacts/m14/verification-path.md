# M14 Verification Path

This document publishes the deterministic proof path for M14.

## Scope

The path must prove:

- successful concept resolution
- successful vendor mapping
- unresolved-definition failure
- conflicting-definition failure
- deterministic repeated output on the same repository or catalog state

## Proof Matrix

| Proof area | Test / command | What it proves |
| --- | --- | --- |
| Repository-backed successful resolution | `AthenaM14ProofCorpusTest` | The governed repository at `examples/m14/siemens-proof-corpus` resolves the narrow electrical proof families through real repository files. |
| Successful vendor mapping | `AthenaM14ProofCorpusTest` and `AthenaCompilerComponentKnowledgeIntegrationTest` | Vendor-neutral concepts resolve to Siemens-first proof implementations and expose inspectable vendor part numbers. |
| Unresolved-definition failure | `AthenaComponentKnowledgeRuntimeServiceTest` and `AthenaComponentKnowledgeResolverTest` | Unknown component references surface explicit typed unresolved diagnostics instead of silent fallback. |
| Conflicting-definition failure | `AthenaComponentKnowledgeResolverTest` | Incompatible duplicate concept or implementation definitions surface explicit conflicts rather than load-order precedence. |
| Deterministic repeated output | `AthenaM14ProofCorpusTest`, `AthenaComponentKnowledgeRuntimeServiceTest` | Repeated inspection on the same state returns equal resolved snapshots and equal diagnostics. |

## Canonical Commands

Run sequentially on Windows with Java 25:

```powershell
java25; .\gradlew.bat :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest --console=plain --no-daemon
```

This compiler command proves:

- successful concept resolution
- successful vendor mapping
- unresolved-definition failure
- conflicting-definition failure

```powershell
java25; .\gradlew.bat :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --console=plain --no-daemon
```

This runtime command proves:

- repository-backed successful proof resolution
- deterministic repeated component knowledge snapshots
- unresolved-definition failure through runtime inspection

## Repository-Backed Corpus

The primary real repository proof lives at:

`examples/m14/siemens-proof-corpus`

It intentionally stays narrow.

Covered families:

- `electrical.plc.cpu`
- `electrical.contactor.power`
- `electrical.relay.overload`
- `electrical.motor.ac`
- `electrical.power-supply.dc24`

Covered Siemens-first proof mappings:

- `proof.cpu.313c`
- `proof.contactor.3pole`
- `proof.relay.overload`
- `proof.motor.ac`
- `proof.power-supply.24vdc`

## Failure Interpretation

- `component.definition.unresolved` means Athena could not resolve the authored reference to any active concept or implementation.
- conflict diagnostics from `AthenaComponentKnowledgeResolverTest` mean duplicate active definitions disagree and Athena correctly refused to select one by chance.

## Position

This verification path does not create a second semantic authority.

- repository files remain the governed source input
- M8 remains the only mutation authority
- M14 remains a read-only resolution layer above canonical `Engineering IR`
