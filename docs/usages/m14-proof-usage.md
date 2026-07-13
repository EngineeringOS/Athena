# Athena M14 Proof Usage

Updated: 2026-07-14

## Purpose

This guide shows how to exercise the finished M14 proof surfaces:

- vendor-neutral engineering concept contracts
- vendor implementation mappings
- typed semantic-port and minimal physical-trait contracts
- governed component reference resolution
- compiler-owned component knowledge context for later M9-facing use
- projection and presentation downstream evidence
- runtime and `ide/lsp` publication of resolved component knowledge
- repository-backed proof corpus and deterministic failure path

It assumes the workspace is already checked out locally and `java25` is available on this workstation.

## Companion Records

- [`_bmad-output/implementation-artifacts/m14/README.md`](../../_bmad-output/implementation-artifacts/m14/README.md)
- [`_bmad-output/implementation-artifacts/m14/milestone-summary-2026-07-14.md`](../../_bmad-output/implementation-artifacts/m14/milestone-summary-2026-07-14.md)
- [`_bmad-output/implementation-artifacts/m14/m14-retrospective-2026-07-14.md`](../../_bmad-output/implementation-artifacts/m14/m14-retrospective-2026-07-14.md)
- [`_bmad-output/implementation-artifacts/m14/verification-path.md`](../../_bmad-output/implementation-artifacts/m14/verification-path.md)

## Operating Rule

Run Gradle verification sequentially on this Windows workstation.

Use:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain <task>
```

Do not overlap Gradle verification commands in parallel shells.

## What M14 Proves

M14 proves that Athena can keep `Engineering IR` as canonical truth while resolving governed component knowledge above it and feeding that resolved knowledge into later downstream seams.

The central M14 claim is:

- `Engineering IR` remains canonical authored truth
- component concepts, vendor implementations, semantic ports, and physical traits are explicit kernel contracts
- governed component resolution stays compiler-owned and deterministic
- runtime and LSP consume Athena-owned resolved knowledge rather than re-resolving in clients
- projection and presentation can consume resolved component evidence without becoming semantic authority
- M8 remains the only mutation authority across graph, forms, templates, AI, API, and DSL surfaces

## Published Fixtures

### Main Repository-Backed Proof Corpus

- [`examples/m14/siemens-proof-corpus`](../../examples/m14/siemens-proof-corpus)

This narrow proof corpus exercises:

- `electrical.plc.cpu`
- `electrical.contactor.power`
- `electrical.relay.overload`
- `electrical.motor.ac`
- `electrical.power-supply.dc24`

with Siemens-first proof mappings:

- `proof.cpu.313c`
- `proof.contactor.3pole`
- `proof.relay.overload`
- `proof.motor.ac`
- `proof.power-supply.24vdc`

## Proof Surface 1: Core Contracts

### Main Modules

- [`kernel/component-model/`](../../kernel/component-model/README.md)
- [`kernel/part-model/`](../../kernel/part-model/README.md)
- [`kernel/connection-model/`](../../kernel/connection-model/README.md)
- [`kernel/physical-model/`](../../kernel/physical-model/README.md)

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:component-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:part-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test
```

What this proves:

- Athena has typed kernel contracts for component knowledge instead of hiding them in runtime code
- compatibility judgment still stays out of the semantic-port foundation
- physical traits stay minimal and do not become geometry truth

## Proof Surface 2: Compiler-Owned Resolution

### Main Modules

- [`kernel/compiler/`](../../kernel/compiler/README.md)
- [`extensions/domain-electrical/`](../../extensions/domain-electrical/README.md)

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgeResolutionModelContractTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaGovernedKnowledgePackageSourceBuilderTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest
```

What this proves:

- the electrical extension publishes the first narrow Siemens-first proof slice
- governed package selection controls which component knowledge becomes active
- unresolved and conflicting definitions fail explicitly
- resolved component knowledge is attached to compiler-owned knowledge context

## Proof Surface 3: Downstream Projection And Presentation Evidence

### Main Modules

- [`kernel/projection-model/`](../../kernel/projection-model/README.md)
- [`kernel/presentation-model/`](../../kernel/presentation-model/README.md)
- [`kernel/compiler/`](../../kernel/compiler/README.md)

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaProjectionPresentationComponentKnowledgeIntegrationTest --tests com.engineeringood.athena.compiler.PresentationModelDeriverTest
```

What this proves:

- projection documents can carry resolved concept and minimal physical evidence
- presentation documents can consume the same evidence downstream
- M14 still does not replace `Projection Model` or `Presentation IR`

## Proof Surface 4: Runtime And LSP Delivery

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`ide/lsp/`](../../ide/lsp/README.md)

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaComponentKnowledgeRequestTest
```

What this proves:

- runtime sessions publish resolved component knowledge as Athena-owned state
- dirty source preview does not become a second mutation authority
- repository-backed proof corpus resolves deterministically through runtime
- LSP transports resolved component knowledge without moving authority to the client

## Interactive Use

1. Open the governed repository at `examples/m14/siemens-proof-corpus`.
2. Open `src/siemens-proof-corpus.athena`.
3. Request the component knowledge session through runtime or LSP-driven surfaces.
4. Confirm the resolved components include `PS1`, `PLC1`, `KM1`, `FR1`, and `M1`.
5. Confirm the resolved vendor part numbers include the Siemens-first proof mappings.
6. Confirm only `PLC1` currently publishes the proof semantic-port and physical-trait slice.

What this proves:

- the repository-backed proof is not limited to inline test strings
- resolved component knowledge remains inspectable and reproducible on the same repository state

## Full Verified Path

The following commands were confirmed during the M14 closeout pass:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:component-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:part-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:connection-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:physical-model:test
java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaKnowledgeResolutionModelContractTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaGovernedKnowledgePackageSourceBuilderTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaComponentKnowledgeResolverTest --tests com.engineeringood.athena.compiler.AthenaCompilerComponentKnowledgeIntegrationTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaProjectionPresentationComponentKnowledgeIntegrationTest --tests com.engineeringood.athena.compiler.PresentationModelDeriverTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaPluginRuntimeServicesTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaSourceMutationRuntimeServiceTest --tests com.engineeringood.athena.runtime.AthenaComponentKnowledgeRuntimeServiceTest
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaM14ProofCorpusTest
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaComponentKnowledgeRequestTest
```

## Current Boundaries

M14 does prove:

- explicit component, implementation, semantic-port, and physical-trait contracts
- governed deterministic component knowledge resolution
- explicit unresolved and conflicting-definition failure paths
- runtime and LSP delivery of resolved component knowledge
- repository-backed proof of the narrow electrical Siemens-first slice
- downstream projection and presentation evidence without semantic-authority drift

M14 does not yet prove:

- broad vendor catalog coverage
- final knowledge-pack ecosystem scale
- rich rule authoring or company standards packs
- mainstream graphical authoring over component libraries
- final electrical renderer correctness for large production drawings
