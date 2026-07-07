# Athena Workspace Summary

## Purpose

This document summarizes the current Athena workspace as it exists today:

- what the workspace is
- what M0 proved
- what M1 proved
- what M2 proved
- what M3 proved
- how to use the current runnable and verifiable surfaces
- how the current implementation aligns with the EngineeringOS manifesto

This is the current workspace summary, not a historical story note.

## One-Line Summary

Athena is the JVM-first EngineeringOS implementation workspace that now proves four milestone layers:

1. M0: `DSL -> AST -> Engineering IR -> validation -> SVG`
2. M1: `runtime -> graph -> commands -> history/diff -> plugin-hosted extension`
3. M2: `Engineering IR -> Layout IR -> Geometry IR -> multi-view runtime projection -> desktop operator proof`
4. M3: `stable plugin API -> hosted plugin approval -> explicit pass pipeline -> external proof domains`

The central architectural claim remains unchanged:

- the DSL is the authored source of truth
- `Engineering IR` is the canonical semantic model
- `Layout IR` and `Geometry IR` are downstream consequences
- plugin-hosted domains participate through governed contracts, not kernel special cases
- runtime owns orchestration and active projection sessions
- UI and backends consume governed downstream state

## Current Workspace Shape

### Top-Level Repo Role

- `kernel/`: core semantic, projection, runtime, and rendering substrate
- `extensions/`: domain-specific extensions that attach through approved contracts
- `ui/`: shared UI and interaction infrastructure
- `apps/`: runnable entrypoints
- `examples/`: published milestone proof corpora
- `docs/compiler/`: implementation boundary notes
- `docs/usages/`: operator and developer usage guides
- `manifesto/`: product, architecture, and technology doctrine
- `_bmad-output/`: planning, story, review, and retrospective records

### Active Gradle Modules

| Group | Module | Responsibility |
| --- | --- | --- |
| `kernel` | `:kernel:language` | DSL syntax layer and parser |
| `kernel` | `:kernel:engineering-model` | canonical semantic model after lowering |
| `kernel` | `:kernel:layout-model` | explicit layout projection contracts |
| `kernel` | `:kernel:geometry-model` | explicit geometry projection contracts |
| `kernel` | `:kernel:validation` | generic semantic validation |
| `kernel` | `:kernel:plugins:plugin-api` | stable plugin SPI for hosted extension contracts |
| `kernel` | `:kernel:plugins:plugin-host` | plugin sources, approval, inventory, and hosted lifecycle inspection |
| `kernel` | `:kernel:compiler` | lowering, pass orchestration, validation orchestration, layout derivation, geometry derivation, rendering coordination |
| `kernel` | `:kernel:runtime` | workspace lifecycle, execution context, graph, command runtime, history, diff, plugin hosting, projection sessions |
| `kernel` | `:kernel:svg-renderer` | deterministic SVG backend fed from `Geometry IR` |
| `extensions` | `:extensions:domain-electrical` | first real Electrical domain extension and first supported view definitions |
| `extensions` | `:extensions:domain-dummy` | synthetic proof domain proving the SPI is not secretly electrical-shaped |
| `ui` | `:ui:compose-workbench` | shared Compose workbench and interaction layer |
| `apps` | `:apps:cli` | terminal entrypoint |
| `apps` | `:apps:desktop-viewer` | desktop Compose application entrypoint |

## What M0 Achieved

M0 is complete as the first compiler proof.

Athena proved that the EngineeringOS semantic core can exist independently from any drawing tool by implementing:

- a minimal Electrical/Runtime DSL
- parsing into a syntax-owned AST
- deterministic lowering into canonical `Engineering IR`
- generic semantic validation over canonical state
- extension-provided domain semantics
- deterministic SVG derivation from semantic truth
- real plugin discovery and compatibility checks
- governed knowledge package loading and resolution
- external boundary descriptor validation
- published conformance examples under [`examples/m0/`](../../examples/m0)

## What M1 Achieved

M1 is complete as the first runtime-centered workspace proof above M0.

Delivered:

- `Athena Runtime` owns workspace lifecycle and active project activation
- compiler access routes through runtime-owned execution context
- runtime exposes an `Engineering Graph` over canonical semantic state
- semantic mutation flows through explicit commands only
- undo, redo, replay, history, and semantic diff are runtime-owned
- runtime-hosted plugins extend semantics, commands, and views without becoming sovereign
- desktop Compose viewer proves inspection over runtime-owned state

## What M2 Achieved

M2 is complete as the first explicit projection and multi-view proof.

Per BMAD tracking, both M2 epics are now closed as `done` in [`_bmad-output/implementation-artifacts/m2/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m2/sprint-status.yaml).

### Epic 1 Result: Explicit Projection Layers

Epic 1 proved the manifesto split between semantics, layout intent, and geometry.

Delivered:

- dedicated `:kernel:layout-model` and `:kernel:geometry-model` modules
- first supported `cabinet` and `wiring` `ViewDefinition` pair from the electrical extension
- deterministic `Layout IR` derivation from canonical semantics
- deterministic `Geometry IR` derivation from layout intent
- first geometry-backed backend proof corpus under [`examples/m2/`](../../examples/m2)
- stable semantic identity preserved across engineering, layout, and geometry layers

### Epic 2 Result: Runtime-Owned Multi-View Operation

Epic 2 proved that explicit projection layers can stay inside the existing runtime-owned change path.

Delivered:

- runtime-owned projection sessions and active view switching
- desktop consumption of runtime-owned projection snapshots instead of UI-private derivation
- projection refresh after the supported `connect ports` mutation path
- semantic diff and history inspection that include projection consequences
- final desktop operator proof over `cabinet` and `wiring`
- scripted operator-proof smoke verification through the desktop entrypoint

### M2 Proven Chain

```text
Athena DSL
        ->
Engineering IR
        ->
Layout IR
        ->
Geometry IR
        ->
runtime-owned projection session
        ->
SVG backend / desktop workbench
```

## What M3 Achieved

M3 is complete for Epic 1 and Epic 2 as the first hosted-extensibility proof.

Per BMAD tracking, Epic 1 and Epic 2 are now `done` in [`_bmad-output/implementation-artifacts/m3/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m3/sprint-status.yaml). Epic 3 remains the next milestone execution area because the automated zero/one/multi-plugin matrix is still intentionally pending.

### Epic 1 Result: Hosted Extensibility Platform

Epic 1 proved that Athena now has a real hosted extension platform rather than compiler-internal plugin hooks.

Delivered:

- dedicated `:kernel:plugins:plugin-api` stable SPI boundary
- dedicated `:kernel:plugins:plugin-host` hosted source, approval, and inventory boundary
- explicit plugin manifest, extension-point, schema, validation, pass, and render contribution contracts
- inspectable hosted lifecycle and approved-plugin inventory shared by compiler and runtime
- explicit compiler pass pipeline:
  - `parse`
  - `lower`
  - `semantic enrichment`
  - `validate`
  - `backend preparation`
  - `backend emission`
- deterministic approved-plugin ordering through the hosted path

### Epic 2 Result: External Proof Domains

Epic 2 proved that real domain behavior can live outside kernel modules through the hosted SPI.

Delivered:

- generic kernel validation remains separate from plugin-owned domain validation
- `:extensions:domain-electrical` refactored into the stable M3 hosted proof shape
- `:extensions:domain-dummy` added as a second synthetic proof domain
- runtime-hosted render metadata, runtime views, and hosted command-path proof remain inspectable
- published `examples/m3/` proof corpus for:
  - `electrical-only`
  - `dummy-only`
  - `both`
- post-review fix for the hosted-domain mismatch case so the compiler now fails loudly when a hosted plugin set claims none of the authored declarations

### M3 Proven Chain

```text
Athena DSL
        ->
stable plugin API
        ->
hosted plugin source + approval
        ->
explicit compiler pass pipeline
        ->
canonical Engineering IR
        ->
plugin-owned domain validation / render intent / runtime participation
        ->
published hosted proof corpus
```

## What Athena Is Now

Athena is now:

- a semantic compiler proof
- a runtime-managed semantic workspace proof
- an explicit multi-view projection proof
- a geometry-backed backend proof
- a hosted compiler-platform proof with governed external domains
- a runtime-owned desktop operator proof

It is not yet:

- a full Studio product shell
- a freeform layout or geometry editor
- a production-grade web Studio
- a cloud or collaborative platform
- a full standalone knowledge compiler

## Current Runnable And Verifiable Usage

Java 25 is mandatory.

On this Windows workstation, run Gradle sequentially and use `java25` first.

```powershell
java25; .\gradlew.bat --no-daemon --console=plain build
```

### CLI Surface

Verified example:

```powershell
java25; .\gradlew.bat :apps:cli:run --args "parse examples/m0/demo-cabinet.athena"
```

The CLI remains useful for semantic compilation and command-path inspection, but M2's main proof surfaces are now the geometry corpus and the desktop operator proof.

### M2 Geometry Proof

Use [`examples/m2/demo-cabinet.athena`](../../examples/m2/demo-cabinet.athena) as the shared semantic seed for the first synchronized `cabinet` and `wiring` projections.

The expected backend artifacts are:

- [`examples/m2/demo-cabinet.cabinet.svg`](../../examples/m2/demo-cabinet.cabinet.svg)
- [`examples/m2/demo-cabinet.wiring.svg`](../../examples/m2/demo-cabinet.wiring.svg)
- [`examples/m2/demo-cabinet.expectation.txt`](../../examples/m2/demo-cabinet.expectation.txt)

The proof is verified through the compiler test suite:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
```

### M2 Desktop Operator Proof

Use [`examples/m2/operator-proof.athena`](../../examples/m2/operator-proof.athena) as the desktop seed. It starts without authored connections so the runtime can prove the command-backed creation of `connection:PLC1.out->M1.in`.

Desktop verification commands:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:operatorProofSmoke
```

Interactive desktop launch:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:run
```

### Focused Usage Guide

For the M2 proof surfaces only, read [`docs/usages/m2-proof-usage.md`](m2-proof-usage.md).

### M3 Hosted Extensibility Proof

Use the published M3 corpus:

- [`examples/m3/electrical-proof.athena`](../../examples/m3/electrical-proof.athena)
- [`examples/m3/dummy-proof.athena`](../../examples/m3/dummy-proof.athena)
- [`examples/m3/dual-domain-proof.athena`](../../examples/m3/dual-domain-proof.athena)

The expectation sidecars document the intended hosted plugin set and minimum expected outcome for each proof:

- electrical-only render-backed proof
- dummy-only hosted-semantic proof without default global view definitions
- dual-domain hosted coexistence proof

Primary verification commands:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
java25; .\gradlew.bat --no-daemon --console=plain build
```

Focused guide:

- [`docs/usages/m3-proof-usage.md`](m3-proof-usage.md)

## Alignment With The Manifesto

| Manifesto Theme | Current Athena Status | Workspace Evidence |
| --- | --- | --- |
| Semantics before drawings | implemented | DSL lowers into canonical `Engineering IR`; all downstream surfaces consume derived state |
| Own the semantic layer | implemented | `:kernel:engineering-model`, `:kernel:compiler`, `:kernel:validation` |
| Compiler as operational heart | implemented | pass-based lowering, validation, layout derivation, geometry derivation, rendering coordination |
| Graph as operational shape | implemented | `:kernel:runtime` exposes engineering graph projection over canonical state |
| Plugin-first growth | implemented with governance | extension contracts plus non-sovereign runtime hosting |
| Hosted extensibility platform | implemented | stable plugin SPI, hosted approval boundary, explicit pass-pipeline participation |
| Layout distinct from semantics | implemented | `:kernel:layout-model` and layout derivation are explicit |
| Geometry is downstream | implemented | `:kernel:geometry-model` and geometry-backed SVG/backend proof |
| Studio is downstream shell | partially implemented | desktop workbench consumes runtime-owned projection sessions, but full Studio shell is not complete |
| AI augments, not replaces | partially implemented | optional AI proposals still route only as accepted command-shaped changes |
| Open semantic infrastructure | partially implemented | strong local JVM-first proof exists; broader ecosystem targets remain future work |

## Explicitly Deferred Relative To The Manifesto

Still deferred:

- automated zero-plugin / electrical-only / dummy-only / both-together matrix close-out under M3 Epic 3
- arbitrary manual layout authoring workflows
- arbitrary geometry editing as the source of engineering truth
- browser-first or WASM Studio delivery
- cloud runtime and collaboration layers
- broader downstream target adapters such as `QElectroTech`, `EPLAN`, `FreeCAD`, or `OpenUSD`
- a full governed knowledge compiler as a separate first-class subsystem

## Practical Reading Order

If you want the current implementation in the right order, read:

1. [`README.md`](../../README.md)
2. [`docs/usages/m2-proof-usage.md`](m2-proof-usage.md)
3. [`examples/m2/README.md`](../../examples/m2/README.md)
4. [`kernel/layout-model/README.md`](../../kernel/layout-model/README.md)
5. [`kernel/geometry-model/README.md`](../../kernel/geometry-model/README.md)
6. [`docs/usages/m3-proof-usage.md`](m3-proof-usage.md)
7. [`apps/desktop-viewer/README.md`](../../apps/desktop-viewer/README.md)
8. [`manifesto/docs/architecture/03-ir.md`](../../manifesto/docs/architecture/03-ir.md)
9. [`manifesto/docs/architecture/05-plugin.md`](../../manifesto/docs/architecture/05-plugin.md)
10. [`manifesto/docs/architecture/07-studio.md`](../../manifesto/docs/architecture/07-studio.md)
11. [`manifesto/docs/architecture/09-layout-and-geometry.md`](../../manifesto/docs/architecture/09-layout-and-geometry.md)

## Bottom Line

Athena has now completed the first four implementation phases that matter most to EngineeringOS:

- M0 proved that semantic engineering can be compiled from authored DSL into canonical `Engineering IR` and deterministic downstream artifacts.
- M1 proved that the same semantic core can be hosted by runtime, inspected as graph, changed through commands, and extended by plugins without giving up canonical ownership.
- M2 proved that the same canonical semantic source can derive explicit `Layout IR`, explicit `Geometry IR`, synchronized multi-view projections, and a runtime-owned desktop operator workflow.
- M3 proved that Athena can freeze a stable hosted extension boundary, admit governed external domains through an approval layer, and keep real domain meaning outside kernel code while preserving canonical semantic authority.

That means Athena now embodies the manifesto's central thesis more completely than before:

Engineering meaning is primary, and layout, geometry, hosted domain behavior, runtime behavior, UI, and backend output are controlled downstream consequences.
