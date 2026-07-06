# Athena Workspace Summary

## Purpose

This document summarizes the current Athena workspace as it exists today:

- what the workspace is
- what M0 proved
- what M1 proved
- how to use the current runnable surfaces
- how the current implementation aligns with the EngineeringOS manifesto

This is the current workspace summary, not a historical story note.

## One-Line Summary

Athena is the JVM-first EngineeringOS implementation workspace that now proves both:

1. M0: `DSL -> AST -> Engineering IR -> validation -> SVG`
2. M1: `runtime -> graph -> commands -> history/diff -> Compose viewer -> plugin-hosted extension`

The central architectural claim remains unchanged:

- the DSL is the authored source of truth
- `Engineering IR` is the canonical semantic model
- runtime and UI are downstream orchestration surfaces
- plugins extend behavior without becoming semantic authorities

## Current Workspace Shape

### Top-Level Repo Role

- `kernel/`: core semantic and runtime substrate
- `extensions/`: domain-specific extensions that attach through approved contracts
- `ui/`: shared UI and interaction infrastructure
- `apps/`: runnable entrypoints
- `examples/`: published example inputs and expected artifacts
- `docs/compiler/`: implementation boundary notes
- `docs/usages/`: operator and developer usage guides
- `manifesto/`: product, architecture, and technology doctrine
- `_bmad-output/`: planning, story, and retrospective records

### Active Gradle Modules

| Group | Module | Responsibility |
| --- | --- | --- |
| `kernel` | `:kernel:language` | DSL syntax layer and parser |
| `kernel` | `:kernel:engineering-model` | canonical semantic model after lowering |
| `kernel` | `:kernel:validation` | generic semantic validation |
| `kernel` | `:kernel:compiler` | lowering, pass orchestration, plugin contracts, governed knowledge, boundary descriptors |
| `kernel` | `:kernel:runtime` | workspace lifecycle, execution context, graph, command runtime, history, diff, plugin hosting, optional AI proposal flow |
| `kernel` | `:kernel:svg-renderer` | deterministic SVG projection from semantic state |
| `extensions` | `:extensions:domain-electrical` | first real Electrical domain extension |
| `ui` | `:ui:compose-workbench` | shared Compose workbench and interaction layer |
| `apps` | `:apps:cli` | terminal entrypoint |
| `apps` | `:apps:desktop-viewer` | desktop Compose application entrypoint |

## What M0 Achieved

M0 is complete as the first compiler proof.

### M0 Architectural Result

Athena proved that the EngineeringOS semantic core can exist independently from any downstream drawing tool by implementing:

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

### M0 Proven Pipeline

```text
Athena DSL
        ->
AST
        ->
Engineering IR
        ->
VALIDATE
        ->
SVG / expectation artifacts
```

### M0 Manifesto Alignment

M0 directly proves these manifesto positions:

- `manifesto/docs/architecture/01-compiler.md`
  - Athena has an explicit compiler with ordered, inspectable passes.
- `manifesto/docs/architecture/03-ir.md`
  - `Engineering IR` is the canonical execution boundary.
- `manifesto/docs/architecture/05-plugin.md`
  - extensions exist through typed contracts instead of hard-coding every domain into the core.

## What M1 Achieved

M1 is complete as the first runtime-and-studio-side proof above M0.

Per BMAD tracking, Epic 1 and Epic 2 are both closed as `done` in [`_bmad-output/implementation-artifacts/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/sprint-status.yaml).

### Epic 1 Result: Runtime-Managed Inspection

Epic 1 proved that M0 could be lifted into a runtime-owned system boundary without moving semantic authority into the UI.

Delivered:

- `Athena Runtime` owns workspace lifecycle and active project activation
- compiler access now routes through runtime-owned execution context
- Compose modules were split cleanly with version-catalog management
- desktop semantic viewer can display the active project through runtime-owned services
- selection, pan, and zoom exist as UI infrastructure, not semantic truth

### Epic 2 Result: Runtime-Managed Semantic Change

Epic 2 proved that semantic inspection and change can stay inside one runtime-centered path.

Delivered:

- runtime-owned `Engineering Graph` projection over canonical semantic state
- command runtime as the only semantic mutation path
- undo, redo, replay, and serialized command history
- one GUI-backed port-connection mutation proof
- affected-scope recomputation plus validation/render refresh
- semantic diff and history consequence inspection
- runtime-hosted plugins for domain semantics, commands, and views
- enforced non-sovereign plugin boundaries
- optional AI proposals routed only as accepted command-shaped changes

### M1 Proven Pipeline

```text
DSL / GUI / accepted AI proposal
        ->
Athena Runtime
        ->
Command Runtime or compiler entry
        ->
Engineering IR
        ->
Graph / validation / render / diff / history
        ->
CLI or Compose viewer
```

## What Athena Is Now

Athena is now more than an M0 compiler proof and still less than the full EngineeringOS product vision.

It is currently:

- a semantic compiler proof
- a runtime-managed semantic workspace proof
- a desktop semantic viewer proof
- a command-backed semantic mutation proof
- a graph, history, diff, and plugin-hosting proof

It is not yet:

- a full Studio product shell
- a multi-view layout system
- a geometry-first editor
- a cloud or collaborative platform
- a full knowledge compiler
- a production-grade web Studio

## Current Runnable Usage

### Build And Test

Java 25 is mandatory.

On this Windows workstation, run Gradle sequentially and use `java25` first.

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat build
```

### CLI Surface

Verified command:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :apps:cli:run --args="--help"
```

Current CLI surface:

- `--help`
- `parse <source-file>`
- `connect <source-file> <source-port-path> <target-port-path>`
- `ai-propose-connect <source-file> <source-port-path> <target-port-path> <summary>`
- `ai-proposals [source-file]`
- `ai-accept <source-file> <proposal-id>`
- `ai-reject <source-file> <proposal-id>`
- `plugins`
- `plugin-command <source-file> <contribution-id>`
- `history [source-file]`
- `serialize-history [source-file]`
- `diff [source-file]`
- `history-consequences <command-id> | <source-file> <command-id>`
- `undo [source-file]`
- `redo [source-file]`
- `replay [source-file]`

Typical parse usage:

```powershell
java25; .\gradlew.bat :apps:cli:run --args "parse examples/m0/demo-cabinet.athena"
```

### Desktop Viewer

Desktop smoke verification was rechecked with:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
```

The current smoke path renders the demo cabinet successfully.

To launch the desktop viewer:

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:run
```

### Example Corpus

The current example corpus is still centered on M0 fixtures under [`examples/m0/`](../../examples/m0).

That is intentional. M1 builds runtime, viewer, graph, command, and plugin behavior on top of the same semantic seed rather than replacing the semantic source with UI-local state.

## Alignment With The Manifesto

| Manifesto Theme | Current Athena Status | Workspace Evidence |
| --- | --- | --- |
| Semantics before drawings | implemented | DSL lowers into canonical `Engineering IR`; viewer is downstream |
| Own the semantic layer | implemented | `:kernel:engineering-model`, `:kernel:compiler`, `:kernel:validation` |
| Compiler as operational heart | implemented | pass-based compiler plus validation, rendering, knowledge, and boundary handling |
| Graph as operational shape | implemented in runtime form | `:kernel:runtime` exposes engineering graph projection over canonical state |
| Plugin-first growth | implemented with governance | runtime-hosted plugins plus non-sovereign enforcement |
| Studio is downstream shell | partially implemented | desktop Compose viewer exists, but full Studio shell is not complete |
| Layout distinct from semantics | intentionally deferred | no `Layout IR` or `Geometry IR` yet; semantic truth remains separate from UI state |
| Geometry is downstream | intentionally deferred | current proof stops at SVG derivation and viewer projection, not geometry authority |
| AI augments, not replaces | partially implemented | AI proposals are optional and accepted only through command-shaped runtime flow |
| Open semantic infrastructure | partially implemented | local JVM-first proof exists; broader ecosystem and external targets remain future work |

## Explicitly Deferred Relative To The Manifesto

Athena has not yet implemented the full downstream stack described by the manifesto.

Still deferred:

- `Layout IR`
- `Geometry IR`
- multi-view synchronized layout generation
- browser-first or WASM Studio delivery
- cloud runtime and collaboration layers
- broader downstream target adapters such as `QElectroTech`, `EPLAN`, `FreeCAD`, or `OpenUSD`
- governed knowledge compiler as a separate first-class subsystem

This is consistent with the manifesto and current architecture notes. The current workspace intentionally proves the semantic layer first.

## Practical Reading Order

If you want the current implementation in the right order, read:

1. [`README.md`](../../README.md)
2. [`docs/compiler/m0-pass-pipeline.md`](../compiler/m0-pass-pipeline.md)
3. [`docs/compiler/m1-runtime-host-boundary.md`](../compiler/m1-runtime-host-boundary.md)
4. [`docs/compiler/m1-engineering-graph-boundary.md`](../compiler/m1-engineering-graph-boundary.md)
5. [`docs/compiler/m1-command-runtime-boundary.md`](../compiler/m1-command-runtime-boundary.md)
6. [`docs/compiler/m1-plugin-runtime-hosting-boundary.md`](../compiler/m1-plugin-runtime-hosting-boundary.md)
7. [`manifesto/docs/architecture/01-compiler.md`](../../manifesto/docs/architecture/01-compiler.md)
8. [`manifesto/docs/architecture/03-ir.md`](../../manifesto/docs/architecture/03-ir.md)
9. [`manifesto/docs/architecture/04-graph.md`](../../manifesto/docs/architecture/04-graph.md)
10. [`manifesto/docs/architecture/05-plugin.md`](../../manifesto/docs/architecture/05-plugin.md)
11. [`manifesto/docs/architecture/07-studio.md`](../../manifesto/docs/architecture/07-studio.md)
12. [`manifesto/docs/architecture/09-layout-and-geometry.md`](../../manifesto/docs/architecture/09-layout-and-geometry.md)

## Bottom Line

Athena has completed the first two implementation phases that matter most to EngineeringOS:

- M0 proved that semantic engineering can be compiled from authored DSL into canonical `Engineering IR` and deterministic downstream artifacts.
- M1 proved that the same semantic core can be hosted by runtime, inspected as graph, changed through commands, projected into a desktop viewer, and extended by plugins without giving up canonical ownership.

That means Athena already embodies the manifesto's central thesis:

Engineering meaning is primary, and everything else is a controlled downstream consequence.
