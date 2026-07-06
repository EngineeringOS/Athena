# Athena M1 Plugin Runtime Hosting Boundary

## Purpose

Story `2.7` turns the proven M0 plugin mechanism into a runtime-hosted M1 capability without creating a second plugin architecture.

The boundary stays narrow:

- runtime owns plugin discovery, approval reporting, and contribution access
- compiler domain semantics still use the existing approved plugin inventory
- plugins may now contribute runtime commands and runtime views
- `Engineering IR` remains the only canonical semantic authority

Story `2.8` strengthens that boundary without changing the architecture:

- runtime command and runtime view hosting must be declared explicitly in the plugin manifest through `RUNTIME_COMMANDS` and `RUNTIME_VIEWS`
- manifest ownership claims such as `ENGINEERING_IR`, `WORKSPACE_LIFECYCLE`, `PROJECT_LIFECYCLE`, `RUNTIME_ORCHESTRATION`, and `DIRECT_SEMANTIC_MUTATION` are rejected as non-sovereign violations
- runtime hosting rejects plugins that implement command/view contributor contracts without the matching manifest declaration, or declare those runtime contracts without implementing them
- `AthenaPluginRuntimeServices` exposes the core-owned invariants that remain non-overridable even when plugins are active

## Runtime-Owned Hosting Model

- `AthenaPluginRuntimeServices`
  - owns the hosted plugin discovery report
  - exposes the shared approved inventory
  - exposes hosted plugin inspection metadata
  - executes contributed plugin commands through the standard command runtime
  - derives plugin view contributions from the active execution context
- `AthenaHostedPluginRuntimeServices`
  - is the default JVM-first runtime host implementation
  - reuses the existing compiler plugin discovery and approval path
  - does not invent a second loader or manifest format
  - filters the discovered plugin set through runtime contract enforcement before publishing the hosted discovery report
- `AthenaServiceRegistry`
  - resolves plugin runtime services by default
  - builds the default compiler with the hosted plugin discovery report

## Shared Inventory Rule

- Runtime and compiler must not discover plugins independently in the default M1 path.
- The runtime host performs discovery once and keeps the inspectable `AthenaPluginDiscoveryReport`.
- `AthenaCompiler` may accept that hosted report directly so lowering and validation use the same approved plugin inventory visible from runtime inspection.

## First Hosted Contribution Slice

The first proof stays on `ElectricalRuntimeDomainPlugin`.

It now contributes:

- domain lowering and validation through `AthenaDomainPlugin`
- one runtime command contribution: `electrical-runtime.connect-first-compatible`
- one runtime view contribution that enriches existing inspector and diagnostics surfaces

The command contribution still routes through `AthenaCommandRuntimeService`.
The view contribution only adds derived runtime-owned shell data.

## Non-Sovereign Enforcement Overlay

- approved hosted plugins keep one inspectable `AthenaPluginDiscoveryReport` shared by runtime and compiler
- rejected plugins remain visible through `rejectedCandidates` with stable runtime contract diagnostics
- hosted plugin metadata now exposes the approved extension-point attachments per plugin
- runtime inspection exposes these core-owned invariants:
  - `Athena Runtime` owns workspace and project lifecycle orchestration
  - `Engineering IR` remains the only canonical semantic authority
  - all semantic mutation must flow through the `Command Runtime`
  - plugin contributions remain extensions over runtime-owned contracts rather than top-level owners

## Frontend Surfaces

- CLI
  - `plugins`
  - `plugin-command <source-file> <contribution-id>`
- Compose workbench
  - merges plugin view inspector groups into existing inspector state
  - merges plugin diagnostics into existing diagnostics state
  - does not add a new plugin-specific UX subsystem in Story `2.7`

## Non-Goals

- no marketplace, hot reload, or dynamic install flow
- no importer, exporter, or AI-skill execution
- no plugin-owned workspace lifecycle
- no plugin-private semantic authority
- no plugin-owned bypass around the command runtime, validation path, or runtime orchestration

## Verification Path

From the repo root:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaPluginContractTest"
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaPluginDiscoveryTest"
java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests "com.engineeringood.athena.runtime.AthenaPluginRuntimeServicesTest"
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "com.engineeringood.athena.compiler.AthenaCompilerTest.compile can reuse a runtime hosted plugin discovery report without rediscovering plugins"
java25; .\gradlew.bat --no-daemon --console=plain :apps:cli:test --tests "com.engineeringood.athena.cli.PluginRuntimeCliTest"
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test --tests "com.engineeringood.athena.apps.composeviewer.AthenaComposeViewerWorkbenchSessionTest.gui session dispatches connect ports through runtime and refreshes shell state"
```

These checks prove the minimum `FR-18` slice:

- one runtime-owned hosted plugin inventory
- compiler reuse of that hosted inventory
- one contributed plugin command over the existing command runtime
- one contributed plugin view over the existing Compose shell seams
- explicit rejection of sovereign ownership claims and mismatched runtime-hosted plugin contracts
