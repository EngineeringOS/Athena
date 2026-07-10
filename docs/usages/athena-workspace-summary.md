# Athena Workspace Summary

## Purpose

This document summarizes the current Athena workspace as it exists today:

- what the workspace is
- what M0 proved
- what M1 proved
- what M2 proved
- what M3 proved
- what M4 proved
- what M5 proved
- what M6 proved
- what M7 proved
- how to use the current runnable and verifiable surfaces
- how the current implementation aligns with the EngineeringOS manifesto

This is the current workspace summary, not a historical story note.

## One-Line Summary

Athena is the JVM-first EngineeringOS implementation workspace that now proves eight milestone layers:

1. M0: `DSL -> AST -> Engineering IR -> validation -> SVG`
2. M1: `runtime -> graph -> commands -> history/diff -> plugin-hosted extension`
3. M2: `Engineering IR -> Layout IR -> Geometry IR -> multi-view runtime projection -> desktop operator proof`
4. M3: `stable plugin API -> hosted plugin approval -> explicit pass pipeline -> external proof domains`
5. M4: `Theia desktop shell -> single repository session -> Athena LSP authority -> diagnostics/navigation/inspection -> additive professional workbench`
6. M5: `governed repository contract -> deterministic package graph -> canonical lock -> RepositoryGraphSession -> package-aware IDE operation`
7. M6: `semantic baseline -> semantic diff -> review/commit/history -> runtime/LSP/Theia semantic SCM panel`
8. M7: `projection model -> runtime-owned ProjectionSession -> graph adapter -> graph-first workbench -> first renderer proof`

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
- `integrations/`: vendor substrate adapters and graph-framework translators kept downstream of Athena semantic authority
- `ide/`: primary M4 IDE product path with the first branded Athena Theia desktop shell
- `extensions/`: domain-specific extensions that attach through approved contracts
- `ui/`: shared UI and interaction infrastructure
- `apps/`: runnable entrypoints
- `examples/`: published milestone proof corpora
- `docs/compiler/`: implementation boundary notes
- `docs/roadmap/`: active milestone sequencing and backlog placement notes
- `docs/usages/`: operator and developer usage guides
- `manifesto/`: product, architecture, and technology doctrine
- `_bmad-output/`: planning, story, review, and retrospective records

### Active Gradle Modules

| Group | Module | Responsibility |
| --- | --- | --- |
| `kernel` | `:kernel:language` | DSL syntax layer and parser |
| `kernel` | `:kernel:repository-model` | VCS-neutral repository/package contract boundary for manifest, lock, package identity, dependency declarations, and graph reports |
| `kernel` | `:kernel:semantic-scm` | VCS-neutral semantic SCM contract boundary for baseline, diff, consequence, review, commit intent, and package-aware history nouns |
| `kernel` | `:kernel:engineering-model` | canonical semantic model after lowering |
| `kernel` | `:kernel:layout-model` | explicit layout projection contracts |
| `kernel` | `:kernel:geometry-model` | explicit geometry projection contracts |
| `kernel` | `:kernel:projection-model` | renderer-neutral graphical projection documents derived from geometry and consumed by runtime/LSP/graph seams |
| `kernel` | `:kernel:validation` | generic semantic validation |
| `kernel` | `:kernel:plugins:plugin-api` | stable plugin SPI for hosted extension contracts |
| `kernel` | `:kernel:plugins:plugin-host` | plugin sources, approval, inventory, and hosted lifecycle inspection |
| `kernel` | `:kernel:compiler` | lowering, pass orchestration, validation orchestration, layout derivation, geometry derivation, rendering coordination |
| `kernel` | `:kernel:runtime` | workspace lifecycle, execution context, graph, command runtime, history, diff, plugin hosting, projection sessions |
| `kernel` | `:kernel:svg-renderer` | deterministic SVG backend fed from `Geometry IR` |
| `integrations` | `node: graph-glsp` | translation-only graph adapter that consumes Athena-owned projection-session payloads and rebuilds disposable GLSP-shaped graph data |
| `integrations` | `:integrations:scm-git` | first vendor adapter for semantic baseline loading behind the M6 semantic SCM seam |
| `extensions` | `:extensions:domain-electrical` | first real Electrical domain extension and first supported view definitions |
| `extensions` | `:extensions:domain-dummy` | synthetic proof domain proving the SPI is not secretly electrical-shaped |
| `ui` | `:ui:compose-workbench` | shared Compose workbench and interaction layer |
| `apps` | `:apps:cli` | terminal entrypoint |
| `apps` | `:apps:desktop-viewer` | desktop Compose application entrypoint |

The `ide/` group is now present physically as the primary M4 product path and already contains a runnable Node/Yarn Theia workspace. It remains separate from the Gradle module graph because cross-language boundaries are still protocol boundaries.

M6 is now fully present in the workspace shape: `:kernel:semantic-scm` owns the semantic SCM boundary above `:kernel:repository-model`, `:integrations:scm-git` seeds the first downstream baseline-loading substrate, runtime owns semantic diff/review/commit/history projection state, `ide/lsp` exposes additive request surfaces, and the current Theia `Semantic SCM` panel projects review, commit preparation, package evolution, release relevance, contract-break risk, and validation movement without creating a second semantic authority in the frontend.

M7 is now fully present in the current workspace shape: `integrations/graph-glsp` translates runtime-owned projection sessions into disposable graph data, the Theia frontend hosts the first real `Graphical View` panel inside the existing workbench, the current workbench synchronizes graphical selection back into source reveal, semantic inspection, and semantic SCM context through canonical semantic ids, and the first inspect-first interaction slice routes active-view switching through governed runtime commands while discarding stale transient selection on projection refresh.

The current M7 workspace shape is intentionally still boundary-first: `:kernel:projection-model` now freezes the compiler-derived graphical document boundary above geometry, `integrations/graph-glsp` is the first dedicated home for GLSP-class graph translation, `ide/theia-frontend` consumes that adapter through the existing Athena LSP bridge, and `ide/lsp` plus `kernel/runtime` remain the only semantic and projection authorities for the IDE path.

## What M7 Achieved

M7 is complete as the first graphical projection and visual workbench proof.

Per BMAD tracking, Epic 1, Epic 2, and Epic 3 are now `done` in [`_bmad-output/implementation-artifacts/m7/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m7/sprint-status.yaml).

### Epic 1 Result: Freeze The Projection Boundary And Runtime Authority

Epic 1 proved that graphical projection can remain downstream of canonical engineering meaning while still becoming a real runtime-owned product surface.

Delivered:

- dedicated `:kernel:projection-model` boundary above layout and geometry
- deterministic projection derivation from canonical engineering, layout, and geometry inputs
- runtime-owned `ProjectionSession` lifecycle with deterministic invalidation and refresh
- typed projection queries and governed command routing through `ide/lsp`
- explicit rule that frontend and graph adapters remain disposable consumers, not semantic authorities

### Epic 2 Result: Deliver The First Graphical Workbench Surface

Epic 2 proved that Athena can host a serious graph-first panel inside the existing Theia shell without creating a second semantic center in the frontend.

Delivered:

- translation-only `integrations/graph-glsp` boundary for graph-framework vocabulary
- graph-first `Graphical View` panel inside the Athena workbench
- synchronized selection across source, graphical view, semantic inspection, and semantic SCM
- inspect-first interaction rules with transient frontend state discarded on projection refresh
- denser IDE-style panel layout so the main canvas remains the dominant work surface

### Epic 3 Result: Prove The First Renderer And Lock The Technology Path

Epic 3 proved the first relationship-forward renderer and closed the architectural question around the M7 technology path.

Delivered:

- first renderer proof over canonical object and relationship identities
- extension-owned electrical `cabinet` and `wiring` projection mappings
- published graphical proof corpus under [`examples/m7/`](../../examples/m7/README.md)
- explicit graphical technology decision record for the current Athena constraints
- documented carry-forward boundary for later bidirectional code/graph work instead of hiding it inside M7

### M7 Proven Chain

```text
Athena DSL
        ->
Engineering IR
        ->
Layout / Geometry
        ->
Projection model
        ->
runtime-owned ProjectionSession
        ->
Athena LSP projection protocol
        ->
translation-only graph adapter
        ->
graph-first Athena workbench
```

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
- dedicated `:kernel:projection-model` module above geometry for renderer-neutral graphical documents
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

## What M4 Achieved

M4 is complete as the first serious IDE product proof.

Per BMAD tracking, Epic 1, Epic 2, and Epic 3 are now `done` in [`_bmad-output/implementation-artifacts/m4/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m4/sprint-status.yaml).

### Epic 1 Result: Real Athena IDE Product Shell

Epic 1 proved that Athena can exist as a real Theia-based product shell rather than only as a CLI or Compose proof.

Delivered:

- physical `ide/` product group as the primary IDE path
- branded Athena desktop shell under `ide/theia-product`
- explicit frontend/backend ownership split under `ide/theia-frontend` and `ide/theia-backend`
- one-repository open flow with runtime-backed activation under `ide/lsp`
- light repository creation flow with the temporary M4 bootstrap shape
- deterministic Windows desktop smoke path with Java 25 resolved inside the Electron launcher

### Epic 2 Result: Serious Language Tooling Through Athena LSP

Epic 2 proved that authored-source tooling in the IDE can stay downstream of the JVM semantic stack.

Delivered:

- Athena LSP as the sole semantic entry point for the IDE path
- diagnostics in editor and Problems
- completion, document symbols, go-to-definition, and references
- repeated-edit continuity over tracked in-memory Athena document state
- backend transport fix so stale lower-version diagnostics no longer win over the latest tracked state

### Epic 3 Result: Professional Multi-Panel Workbench

Epic 3 proved that the IDE shell can host a professional engineering workbench without replacing Theia or creating a second semantic model in the frontend.

Delivered:

- repository navigation docked in the left panel
- Problems and Output docked in the bottom panel
- Athena-owned workbench commands and `View -> Athena` entry points
- read-only semantic inspection panel beside source editing
- additive Athena workbench extension registry for future panel growth

### M4 Proven Chain

```text
Athena desktop shell
        ->
single repository session
        ->
Athena LSP
        ->
tracked JVM document state
        ->
diagnostics / navigation / semantic inspection
        ->
Athena-owned professional workbench
```

## What M5 Achieved

M5 is complete as the first governed repository/package graph proof.

Per BMAD tracking, Epic 1, Epic 2, and Epic 3 are now `done` in [`_bmad-output/implementation-artifacts/m5/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m5/sprint-status.yaml).

### Epic 1 Result: Governed Repository Contract

Epic 1 proved that Athena repositories now have explicit canonical repository meaning instead of the temporary M4 open-path shortcut.

Delivered:

- dedicated `:kernel:repository-model` boundary for manifest, lock, package identity, dependency declarations, resolved graph, and diagnostics
- governed repository-root contract with canonical `athena.yaml` and canonical derived `athena.lock`
- primary-package layout validation under `src/`
- repository create flow upgraded to the governed M5 bootstrap shape
- repository open flow upgraded from single-file discovery to contract-aware repository activation

### Epic 2 Result: Deterministic Package Graph And Lock

Epic 2 proved that repository/package meaning now resolves through compiler-owned deterministic package semantics.

Delivered:

- deterministic resolution-input construction from manifest-owned dependency intent
- local-first dependency resolution into canonical `ResolvedPackageGraph`
- canonical `athena.lock` materialization and lock validation against compiler-owned authority
- compiler-owned repository graph publication result carrying manifest, lock, graph, and package diagnostics
- runtime-owned repository report service that consumes compiler publication output without redefining package meaning

### Epic 3 Result: Package-Aware IDE Operation

Epic 3 proved that the existing Athena IDE can operate on governed repository/package meaning without replacing M4 boundaries.

Delivered:

- runtime-owned `RepositoryGraphSession` as the active repository session model
- Athena LSP repository graph session payload for manifest, lock, graph, and diagnostics
- repository graph and package diagnostics surfaced in the Athena workbench
- basic `.athena` syntax highlighting and editor configuration kept downstream of Athena LSP authority
- published governed repository fixture under [`examples/m5/`](../../examples/m5/README.md)

### M5 Proven Chain

```text
athena.yaml
        ->
canonical repository contract
        ->
deterministic package graph
        ->
canonical athena.lock
        ->
runtime-owned RepositoryGraphSession
        ->
Athena LSP package-state payload
        ->
Athena IDE repository graph and package diagnostics
```

## What M6 Achieved

M6 is complete as the first semantic SCM and package-history proof.

Per BMAD tracking, Epic 1, Epic 2, and Epic 3 are now `done` in [`_bmad-output/implementation-artifacts/m6/sprint-status.yaml`](../../_bmad-output/implementation-artifacts/m6/sprint-status.yaml).

### Epic 1 Result: Understand Repository Change Semantically

Epic 1 proved that repository change can be modeled semantically above package and engineering meaning instead of through Git-first nouns.

Delivered:

- dedicated `:kernel:semantic-scm` boundary above `:kernel:repository-model`
- VCS-neutral contracts for baselines, diffs, consequences, reviews, commit intent, and package-aware history
- vendor-neutral baseline-loading seam with `:integrations:scm-git` as the first downstream substrate adapter
- deterministic semantic diff categories over repository, package, and engineering meaning
- compiler-derived validation and repository-contract consequences carried through the runtime-owned JVM comparison path

### Epic 2 Result: Prepare And Review Semantic Change

Epic 2 proved that typed semantic change can become operable review and commit output without moving semantic authority into the frontend.

Delivered:

- deterministic semantic review-summary generation from typed change and consequence facts
- deterministic semantic commit-intent generation that remains semantic-first and adapter-ready
- additive hosted-plugin review enrichment that cannot rewrite core semantic SCM facts
- runtime-owned semantic SCM projection state exposed through `ide/lsp` and the current Theia workbench

### Epic 3 Result: Inspect Package Evolution And Release Relevance

Epic 3 proved that package evolution and release relevance can be surfaced through the same semantic product path without widening into graphical work.

Delivered:

- package-aware semantic history contracts over stable package identity and version meaning
- deterministic publish-oriented history summarization across semantic baselines
- runtime-owned package-history state for release relevance, contract-break risk, dependency movement, and validation movement
- Athena LSP package-history request surface
- current Theia `Semantic SCM` panel extended with package evolution and release relevance inspection

### M6 Proven Chain

```text
repository baseline
        ->
semantic diff
        ->
typed consequence facts
        ->
review summary / commit intent / package history
        ->
runtime-owned semantic SCM and history state
        ->
Athena LSP request surfaces
        ->
Athena Theia Semantic SCM panel
```

## What Athena Is Now

Athena is now:

- a semantic compiler proof
- a runtime-managed semantic workspace proof
- an explicit multi-view projection proof
- a geometry-backed backend proof
- a hosted compiler-platform proof with governed external domains
- a runtime-owned desktop operator proof
- a desktop-first Theia IDE product proof with JVM-owned language tooling
- a governed repository/package graph proof with package-aware IDE operation
- a semantic SCM proof with review, commit, and package-history flows on the same JVM-owned semantic path
- a graphical projection and visual workbench proof with runtime-owned projection authority

It is not yet:

- a full Studio product
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

### M5 Governed Repository And Package Graph

Use the published governed repository fixture:

- [`examples/m5/repository-graph-proof/athena.yaml`](../../examples/m5/repository-graph-proof/athena.yaml)
- [`examples/m5/repository-graph-proof/athena.lock`](../../examples/m5/repository-graph-proof/athena.lock)
- [`examples/m5/repository-graph-proof/src/root.athena`](../../examples/m5/repository-graph-proof/src/root.athena)

Primary verification commands:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test :kernel:runtime:test :ide:lsp:test
Set-Location ide
yarn build
yarn start:smoke
```

Focused guide:

- [`docs/usages/m5-proof-usage.md`](m5-proof-usage.md)

### M6 Semantic SCM And Package History

Primary verification commands:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test :kernel:runtime:test :ide:lsp:test"
Set-Location ide
yarn build
yarn start:smoke
```

Current M6 interactive proof inside the Athena shell:

1. Open the repository fixture at `examples/m5/repository-graph-proof`.
2. Open `src/root.athena`.
3. Reveal the `Semantic SCM` panel.
4. Inspect:
   - baseline-driven semantic review
   - commit-preparation output
   - package evolution
   - release relevance
   - contract-break risk
   - validation movement

Focused guide:

- [`docs/usages/m6-proof-usage.md`](m6-proof-usage.md)

### M7 Graphical Projection And Visual Workbench

Use the published graphical proof fixture:

- [`examples/m4/open-repository-proof/`](../../examples/m4/open-repository-proof/)
- [`examples/m4/open-repository-proof/src/factory-line.athena`](../../examples/m4/open-repository-proof/src/factory-line.athena)

Primary verification commands:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test :kernel:compiler:test :kernel:runtime:test :extensions:domain-electrical:test :ide:lsp:test"
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
java25; yarn --cwd ide build
java25; yarn --cwd ide verify:m7
java25; yarn --cwd ide start:smoke
```

Current M7 interactive proof inside the Athena shell:

1. Open the repository fixture at `examples/m4/open-repository-proof`.
2. Open `src/factory-line.athena`.
3. Reveal `Graphical View`.
4. Switch between `Cabinet` and `Wiring`.
5. Pan, zoom, and fit the graph viewport.
6. Use graphical selection to inspect synchronized source, semantic inspection, and semantic SCM context.

Focused guides:

- [`docs/usages/m7-proof-usage.md`](m7-proof-usage.md)
- [`_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md)

### M4 Theia Desktop Shell

The first branded Athena Theia desktop shell now lives under `ide/`.

Build and launch it sequentially on Windows:

```powershell
Set-Location ide
yarn install
yarn build
yarn start:smoke
yarn start
```

Current M4 shell scope:

- branded Athena desktop shell
- Athena-owned home surface
- curated editor, navigator, output, terminal, and markers-facing workbench capability set
- frontend and backend ownership split under `ide/theia-frontend` and `ide/theia-backend`
- explicit `Open Engineering Repository` command in the product shell
- explicit `New Engineering Repository` command in the product shell
- JVM-backed single-session activation through `ide/lsp`
- real stdio Athena LSP initialization inside the JVM boundary
- first `.athena` authored-source open path through `textDocument/didOpen`
- first JVM-owned diagnostics path through `textDocument/publishDiagnostics`
- first JVM-owned completion, document symbols, go-to-definition, and references path through Athena LSP
- first repeated-edit hardening path so diagnostics and navigation stay on the latest in-memory Athena document state
- professional multi-panel workbench with Athena Home, Repository Navigator, Problems, Output, and Semantic Inspection
- additive Athena-owned workbench extension registry for future M5 and later surfaces
- read-only semantic inspection path sourced from Athena LSP tracked document state

Still deferred in the current M4 execution path:

- syntax highlighting and semantic tokens for `.athena`
- hover, rename, formatting, and richer multi-file navigation coverage
- richer Athena LSP diagnostics policy and tuning beyond the current open/edit proof

Current temporary repository rule:

- Athena opens one repository only when it resolves exactly one authored `.athena` source
- if `src/` exists, Athena searches it first
- multi-root workspaces remain out of scope for M4

Current temporary new-repository bootstrap rule:

- Athena creates only `src/<project>.athena`
- the initial source is one minimal `system <Name> { }` seed
- no final `athena.yaml`, `athena.lock`, or package-resolution contract is created in M4

Repository-session proof commands:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :ide:lsp:build
Set-Location ide
yarn build
yarn start:smoke
```

Legacy repository-root compatibility proof against the first repository fixture:

```powershell
cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root examples\m4\open-repository-proof"
```

Direct Athena LSP transport proof is now also validated: the built stdio host accepts `initialize` for `examples/m4/open-repository-proof` and then accepts `textDocument/didOpen` for `src/factory-line.athena`, returning repository and source metadata through the `InitializeResult.capabilities.experimental` payload.

Diagnostics transport proof is also validated: the same stdio host publishes `2` diagnostics for an invalid `didOpen` payload and then publishes `0` diagnostics after a valid `didChange` payload for the same source.

Authoring and navigation proof is also validated: the same stdio host now returns completion items for partial Athena source, hierarchical document symbols, definition targets, and same-document references through standard LSP requests.

Repeated-edit stability is also validated: after valid and invalid consecutive `didChange` events, the same stdio host updates diagnostics and definition results to the latest document version, and a stale replay of an older `didChange` version no longer rolls the tracked state backward.

Open-flow manual proof fixture:

- [`examples/m4/open-repository-proof/src/factory-line.athena`](../../examples/m4/open-repository-proof/src/factory-line.athena)

Direct create-and-activate proof from the built backend bootstrapper:

```powershell
$parent = Join-Path $env:TEMP ("athena-m4-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $parent | Out-Null
$bootstrap = @'
const { AthenaRepositoryBootstrapper } = require("./ide/theia-backend/lib/node/athena-repository-bootstrapper");
(async () => {
  const result = await new AthenaRepositoryBootstrapper().createRepository(process.argv[2], "Factory Line Alpha");
  console.log(JSON.stringify(result));
})().catch(error => {
  console.error(error.stack);
  process.exit(1);
});
'@ | node - $parent
$info = $bootstrap | ConvertFrom-Json
cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root `"$($info.repositoryRootPath)`""
Remove-Item -Recurse -Force $parent
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
- [`docs/usages/m4-proof-usage.md`](m4-proof-usage.md)
- [`docs/usages/m5-proof-usage.md`](m5-proof-usage.md)
- [`docs/usages/m7-proof-usage.md`](m7-proof-usage.md)

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
| Graphical projection stays downstream | implemented | `:kernel:projection-model`, runtime-owned projection sessions, and translation-only graph adapters |
| Studio is downstream shell | first serious proof implemented | Theia desktop shell, Athena LSP boundary, and additive workbench now exist without moving semantic truth into UI state |
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
- graphical semantic-history review and interactive semantic SCM E2E automation

## Practical Reading Order

If you want the current implementation in the right order, read:

1. [`README.md`](../../README.md)
2. [`docs/usages/m2-proof-usage.md`](m2-proof-usage.md)
3. [`examples/m2/README.md`](../../examples/m2/README.md)
4. [`kernel/layout-model/README.md`](../../kernel/layout-model/README.md)
5. [`kernel/geometry-model/README.md`](../../kernel/geometry-model/README.md)
6. [`kernel/projection-model/README.md`](../../kernel/projection-model/README.md)
7. [`docs/usages/m3-proof-usage.md`](m3-proof-usage.md)
8. [`apps/desktop-viewer/README.md`](../../apps/desktop-viewer/README.md)
9. [`docs/usages/m4-proof-usage.md`](m4-proof-usage.md)
10. [`docs/usages/m5-proof-usage.md`](m5-proof-usage.md)
11. [`docs/usages/m6-proof-usage.md`](m6-proof-usage.md)
12. [`docs/usages/m7-proof-usage.md`](m7-proof-usage.md)
13. [`_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md)
14. [`docs/roadmap/athena-milestone-roadmap.md`](../roadmap/athena-milestone-roadmap.md)
15. [`manifesto/docs/architecture/03-ir.md`](../../manifesto/docs/architecture/03-ir.md)
16. [`manifesto/docs/architecture/05-plugin.md`](../../manifesto/docs/architecture/05-plugin.md)
17. [`manifesto/docs/architecture/07-studio.md`](../../manifesto/docs/architecture/07-studio.md)
18. [`manifesto/docs/architecture/09-layout-and-geometry.md`](../../manifesto/docs/architecture/09-layout-and-geometry.md)

## Bottom Line

Athena has now completed the first eight implementation phases that matter most to EngineeringOS:

- M0 proved that semantic engineering can be compiled from authored DSL into canonical `Engineering IR` and deterministic downstream artifacts.
- M1 proved that the same semantic core can be hosted by runtime, inspected as graph, changed through commands, and extended by plugins without giving up canonical ownership.
- M2 proved that the same canonical semantic source can derive explicit `Layout IR`, explicit `Geometry IR`, synchronized multi-view projections, and a runtime-owned desktop operator workflow.
- M3 proved that Athena can freeze a stable hosted extension boundary, admit governed external domains through an approval layer, and keep real domain meaning outside kernel code while preserving canonical semantic authority.
- M4 proved that Athena can become a real Theia-based engineering product shell, keep authored-source tooling behind Athena LSP, and host professional downstream workbench panels without creating a frontend-owned semantic authority.
- M5 proved that Athena can freeze repository/package meaning through canonical manifest and lock contracts, resolve deterministic local-first package graphs, host one runtime-owned `RepositoryGraphSession`, and surface package-aware operation inside the Athena IDE without moving semantic ownership into the frontend.
- M6 proved that Athena can understand repository change semantically through VCS-neutral baselines, deterministic diff/review/commit/history flows, and a current professional IDE surface for review, commit preparation, package evolution, and release relevance.
- M7 proved that Athena can keep graphical projection downstream of semantic authority through a dedicated projection model, runtime-owned projection sessions, a translation-only graph adapter, and a graph-first professional workbench inside the existing Athena shell.

That means Athena now embodies the manifesto's central thesis more completely than before:

Engineering meaning is primary, and layout, geometry, graphical projection, hosted domain behavior, runtime behavior, UI, and backend output are controlled downstream consequences.
