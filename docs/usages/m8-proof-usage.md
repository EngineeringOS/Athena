# Athena M8 Proof Usage

## Purpose

This guide shows how to exercise the finished M8 proof surfaces:

- one runtime-owned mutation authority above source and graph
- one real graph semantic mutation path through Athena command intent
- one real graph projection mutation path through governed projection metadata
- one shared semantic review and reveal path across source, graph, and semantic SCM

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node and Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md`](../../_bmad-output/implementation-artifacts/m8/milestone-summary-2026-07-11.md)
- [`_bmad-output/implementation-artifacts/m8/3-3-publish-the-m8-proof-corpus-and-verification-path.md`](../../_bmad-output/implementation-artifacts/m8/3-3-publish-the-m8-proof-corpus-and-verification-path.md)
- [`examples/m8/README.md`](../../examples/m8/README.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd integrations/graph-glsp <task>
yarn --cwd ide <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## What M8 Proves

M8 proves that Athena no longer has separate editing semantics for source and graph.

The central M8 claim is:

- source-originated and graph-originated changes converge on one runtime-owned mutation model
- accepted graph semantic edits still execute through Athena command meaning instead of renderer-local save logic
- accepted graph projection edits stay projection-only and refresh from runtime-owned placement metadata
- accepted mutation review remains downstream of `kernel/semantic-scm`
- cross-surface reveal remains anchored on canonical semantic identity instead of graph-local ids

## Published Fixture

### Main Fixture

- [`examples/m4/open-repository-proof/`](../../examples/m4/open-repository-proof/)
- [`examples/m4/open-repository-proof/src/factory-line.athena`](../../examples/m4/open-repository-proof/src/factory-line.athena)

M8 intentionally reuses the same governed repository fixture published earlier.

The proof is now mutation-centric because the same repository can be used to prove:

- source mutation evaluation
- graph semantic mutation
- graph projection mutation
- semantic review convergence
- source, graph, and SCM reveal convergence

## Proof Surface 1: Source Mutation Evaluation

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`ide/lsp/`](../../ide/lsp/README.md)
- [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest"
```

### Interactive Use

1. Start the Athena desktop shell.
2. Open `examples/m4/open-repository-proof`.
3. Open `src/factory-line.athena`.
4. Change `device M1` to another valid device identity, or add one valid connection through source.
5. Observe that source evaluation reports an Athena-owned mutation category, outcome, validation feedback, and semantic consequences instead of a separate editor-local result model.

What this proves:

- dirty source evaluation is normalized into the same mutation-result vocabulary used by graph interaction
- accepted source changes can publish semantic diff and review facts without mutating runtime state through an editor-only shortcut
- rejected or invalid source changes surface governed feedback rather than inventing a second source-specific semantics path

## Proof Surface 2: Graph Semantic Mutation

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`ide/lsp/`](../../ide/lsp/README.md)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"
```

### Interactive Use

1. Open the same repository fixture and `factory-line.athena`.
2. Open `Graphical View`.
3. Stay in the `cabinet` view.
4. Select the supported port-label targets and execute the semantic relationship interaction.
5. Confirm the accepted result refreshes from canonical runtime state rather than leaving the graph in a local unsaved mode.

What this proves:

- the graph workbench emits Athena command intent rather than renderer-owned save behavior
- the original semantic graph mutation proof reused canonical command execution; M31 migrated the product path to governed `SemanticRelationshipIntent`
- accepted semantic graph changes feed the same review and history vocabulary as source-originated changes

## Proof Surface 3: Graph Projection Mutation

### Main Modules

- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest"
```

### Interactive Use

1. Open `Graphical View` in the `cabinet` projection.
2. Drag one supported component placement target.
3. Confirm the graph refreshes to the accepted runtime-owned placement, not to a transient frontend-only position.
4. Try the same interaction in an unsupported or inspect-only context and confirm the local state snaps back after refresh.

What this proves:

- projection mutation stays distinct from semantic mutation
- accepted placement is stored as runtime-owned projection metadata
- canonical engineering meaning remains unchanged before and after the placement mutation

## Proof Surface 4: Unified Review And Reveal

### Main Modules

- [`kernel/semantic-scm/`](../../kernel/semantic-scm/README.md)
- [`ide/theia-frontend/src/browser/athena-semantic-selection-service.ts`](../../ide/theia-frontend/src/browser/athena-semantic-selection-service.ts)
- [`ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx`](../../ide/theia-frontend/src/browser/athena-semantic-scm-widget.tsx)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
```

### Interactive Use

1. Produce an accepted mutation from source or graph.
2. Inspect the semantic SCM panel and semantic inspection output.
3. Click the changed subject from semantic SCM.
4. Confirm Athena reveals the same subject in source and switches the graph to the correct governed view when needed.
5. Select the subject from source or graph and confirm the other surfaces follow the same canonical semantic id.

What this proves:

- accepted source and graph mutations feed one semantic review model
- reveal is anchored on canonical semantic identity
- graph-local ids remain downstream aliases only

## Full Verification Path

Run the milestone proof sequentially:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test --tests com.engineeringood.athena.runtime.AthenaGraphCommandIntentServiceTest --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest --tests com.engineeringood.athena.ide.lsp.AthenaSourceMutationRequestTest --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide verify:m8
```

## Current Boundaries

M8 does prove:

- one mutation authority across source and graph
- one real graph semantic mutation path
- one real graph projection mutation path
- one shared semantic review model for accepted source and graph mutations
- one canonical reveal path across source, graph, and semantic SCM

M8 does not yet prove:

- full canonical write-through source editing; the current source path remains preview-first evaluation
- unrestricted graphical authoring
- full notation-pack or symbol-library depth
- final domain authoring breadth
- final UX skin system
- full GLSP language-server-style runtime adoption as the live editing core
