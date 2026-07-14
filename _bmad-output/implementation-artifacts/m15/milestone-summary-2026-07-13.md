# M15 Milestone Summary

## Outcome

M15 proved Athena's first guided semantic authoring foundation.

Engineers can now author narrow electrical intent without directly writing canonical DSL by using:

- the governed component panel
- the canonical inspector
- the graph-based guided connect flow
- the shared preview and acceptance contract

All accepted actions still converge through M8 mutation authority and rebuild canonical source-backed state.

## What M15 Added

- Typed authoring intents, preview payloads, and decision contracts above M8.
- A governed component panel sourced from active M14 component knowledge.
- Source-backed component insertion through accepted guided placement.
- A canonical inspector snapshot with governed property editing.
- Port-aware allowed-target filtering for graph connection authoring.
- Preview-first connection creation with source-backed `connect` edits.
- Cross-surface coherence for component, port, and connection identity.
- A repository-backed proof corpus and deterministic verification path.
- Native Theia Outline remains the supported `.athena` AST surface on the right sidebar; the temporary Explorer-bottom rehome was rolled back after proving the stock widget lost its inner layout height when reparented.

## Product Meaning

M15 is the first milestone that proves Athena can be used as a product surface instead of only as a semantic kernel demonstration.

The milestone keeps the central product position intact:

- DSL remains canonical serialization.
- Guided workbench authoring becomes the mainstream user entry surface.
- Semantic identity remains stronger than graph ids, widget ids, or presentation ids.

## Verification Snapshot

Sequential Windows verification with Java 25 passed:

- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"`
- `yarn build` in `ide/theia-frontend`
- `node --test scripts/athena-component-panel-model.test.mjs scripts/athena-authoring-protocol.test.mjs scripts/athena-inspector-model.test.mjs scripts/athena-guided-connection-model.test.mjs`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
