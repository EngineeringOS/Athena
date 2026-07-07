# Athena M3 Proof Usage

## Purpose

This guide shows how to exercise the finished M3 proof surfaces:

- the stable hosted plugin platform
- the external-domain proof corpus
- the current verification experiments that close Epic 1 and Epic 2

It assumes the workspace is already checked out locally and Java 25 is available through `java25`.

## Operating Rule

Run Gradle sequentially on this Windows workstation.

Use:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain <task>
```

## What M3 Proves

M3 proves that Athena is no longer only a semantic compiler and runtime proof. It is now also a hosted extensibility proof.

The central M3 claim is:

- stable plugin contracts live in a dedicated kernel API boundary
- hosted plugins enter through source plus approval, not private compiler hooks
- the compiler is an explicit pass pipeline
- real domain behavior can live in extensions instead of flowing back into kernel modules
- the published corpus under `examples/m3/` is reusable proof evidence

## Proof Surface 1: Hosted Platform Boundary

### Main Modules

- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- `:kernel:compiler`
- `:kernel:runtime`

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
```

What this proves:

- hosted plugin discovery stays separate from plugin approval
- approved inventory is deterministic and inspectable
- runtime and compiler share the same approved hosted state
- the compiler pass pipeline is visible and test-backed

## Proof Surface 2: External Proof Domains

### Hosted Domains

- [`extensions/domain-electrical/`](../../extensions/domain-electrical/README.md)
- [`extensions/domain-dummy/`](../../extensions/domain-dummy/README.md)

The electrical domain is the first real proof domain.

The dummy domain is the synthetic second proof domain used to prove that the SPI is not secretly electrical-specific.

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test
java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-dummy:test
```

What this proves:

- plugin-owned validation can stay outside kernel validation
- plugin-owned render intent can stay outside kernel renderer ownership
- a second hosted domain can coexist without kernel dummy-specific logic

## Proof Surface 3: Published M3 Corpus

### Corpus Files

- [`examples/m3/electrical-proof.athena`](../../examples/m3/electrical-proof.athena)
- [`examples/m3/electrical-proof.expectation.txt`](../../examples/m3/electrical-proof.expectation.txt)
- [`examples/m3/dummy-proof.athena`](../../examples/m3/dummy-proof.athena)
- [`examples/m3/dummy-proof.expectation.txt`](../../examples/m3/dummy-proof.expectation.txt)
- [`examples/m3/dual-domain-proof.athena`](../../examples/m3/dual-domain-proof.athena)
- [`examples/m3/dual-domain-proof.expectation.txt`](../../examples/m3/dual-domain-proof.expectation.txt)

### Meaning Of Each Proof

- `electrical-proof`: hosted electrical-only path with published `cabinet` and `wiring` views
- `dummy-proof`: hosted dummy-only path that remains semantically valid even without default global view definitions
- `dual-domain-proof`: hosted coexistence path for electrical plus dummy

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M3ExternalDomainProofExamplesTest
```

What this proves:

- the published corpus is executable proof, not dead documentation
- explicit hosted plugin sets can be exercised deterministically
- the same authored DSL remains the only semantic truth

## Experiment Recap

These are the key M3 experiments and what they demonstrated.

### Experiment 1: Freeze The Stable Hosted SPI

Result:

- succeeded

Evidence:

- `:kernel:plugins:plugin-api`
- `:kernel:plugins:plugin-host`
- hosted plugin tests, runtime tests, and compiler tests

Lesson:

- plugin-facing contracts must live in a dedicated kernel boundary, not inside compiler internals

### Experiment 2: Refactor A Real Domain Into The Hosted Shape

Result:

- succeeded

Evidence:

- `:extensions:domain-electrical`
- electrical tests
- compiler and runtime hosted-path regressions

Lesson:

- the first real proof domain can stay outside the kernel while still contributing validation, render intent, commands, and runtime views

### Experiment 3: Add A Synthetic Second Domain

Result:

- succeeded

Evidence:

- `:extensions:domain-dummy`
- dummy tests
- dual-domain hosted inventory assertions

Lesson:

- the M3 SPI is general enough to host a non-electrical proof domain without special-case kernel handling

### Experiment 4: Publish A Corpus That Reuses The Hosted Contracts

Result:

- succeeded

Evidence:

- `examples/m3/`
- `M3ExternalDomainProofExamplesTest`

Lesson:

- examples should stay architecture-contract inputs and seed future automation

### Experiment 5: Post-Review Mismatch Correction

Result:

- succeeded

Evidence:

- compiler now emits `domain.semantics.unavailable` when authored declarations exist but the active hosted LOWER set claims none of them
- dummy README marker syntax was corrected to `domain "dummy-runtime"`

Lesson:

- hosted-plugin presence is not the same as hosted-domain coverage

## Close-Out Verification

Normal M3 close-out commands for the current workspace state:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
java25; .\gradlew.bat --no-daemon --console=plain build
```

## Boundaries

M3 does prove:

- stable hosted SPI
- hosted source plus approval boundary
- explicit compiler pass pipeline
- real external proof domain participation
- synthetic second-domain participation
- reusable proof corpus for hosted states

M3 does not yet prove:

- automated zero-plugin / electrical-only / dummy-only / both matrix close-out
- dynamic local-directory plugin loading
- remote URL plugin acquisition
- hot load or hot unload
- plugin-defined grammar extensions
- broader domain-pack marketplace workflows
