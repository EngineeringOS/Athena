---
baseline_commit: 1339722f55143519dbd7ee0ec0a06049159302f1
---

# Story 1.4: Expose Hosted Plugin Lifecycle And Inventory Inspection

Status: done

## Story

As a platform engineer,
I want hosted plugin lifecycle and approved inventory inspection exposed through governed services,
so that runtime and tests can see exactly which plugins and contributions are active.

## Acceptance Criteria

1. Given hosted plugin discovery and approval are explicit, when lifecycle handling is surfaced for M3, then Athena exposes governed hosted plugin states such as load, initialize, inspect, and shutdown, and those lifecycle surfaces do not hand ownership of compiler or runtime orchestration to plugins.
2. Given approved plugins may contribute multiple capabilities, when the hosted inventory is inspected, then Athena can report approved plugin identity, attached extension points, and declared contribution categories in deterministic order, and compiler and runtime services can consume that inventory without private plugin scanning logic.
3. Given inspectability is a non-functional requirement of M3, when hosted plugin services are reviewed, then the active inventory is visible enough to debug contribution participation and rejection causes, and the inspection path preserves the boundary between stable SPI and internal implementation details.
4. Given lifecycle and inventory inspection are implemented, when the standard Java `25` build and runtime/plugin tests are executed, then the workspace builds successfully and hosted plugin inventory can be queried by approved services and verification code, and the first proof remains JVM-first and single-process.

## Tasks / Subtasks

- [x] Add host-owned lifecycle and inventory inspection models on top of the approved inventory. (AC: 1, 2, 3, 4)
  - [x] Introduce explicit lifecycle-state models for `loaded`, `initialized`, and `shutdown`.
  - [x] Introduce deterministic hosted inventory descriptors that expose approved identity, extension points, and declared contribution categories.
  - [x] Keep the lifecycle and inventory logic in the hosted boundary rather than leaking it into compiler-only code.
- [x] Rewire runtime hosted plugin services onto the lifecycle-aware hosted registry. (AC: 1, 2, 3, 4)
  - [x] Add runtime-visible lifecycle and hosted-inventory accessors without ceding orchestration ownership to plugins.
  - [x] Preserve the existing runtime contract filtering for commands, views, and view definitions.
  - [x] Ensure shutdown removes active runtime participation while preserving inspection evidence.
- [x] Prove inspectability and lifecycle transitions with focused tests and Java `25` verification. (AC: 1, 2, 3, 4)
  - [x] Add hosted registry tests that prove `loaded -> initialized -> shutdown`.
  - [x] Add runtime tests that prove initialized inventory visibility and shutdown behavior.
  - [x] Run sequential Windows verification and record the results here.

## Dev Notes

### Story Intent

- Story `1.4` builds on Story `1.3`. Source and approval are already split; this story makes the hosted result explicitly inspectable and lifecycle-aware.
- Lifecycle here remains host-owned. Plugins do not become lifecycle orchestrators.
- Story `1.5` still owns the explicit compiler pass pipeline.

### Implementation Direction

- `:kernel:plugins:plugin-host` is now the home for the host-owned registry and inventory models.
- `:kernel:runtime` consumes that registry and adds runtime-private contract enforcement plus runtime-specific execution surfaces.
- The runtime service keeps inspection evidence visible even after shutdown, but active command, view, and domain-semantics participation stops once shutdown is reached.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-14-expose-hosted-plugin-lifecycle-and-inventory-inspection]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-4---hosted-plugin-discovery-is-a-two-layer-concern-source-then-approval]
- [Source: _bmad-output/implementation-artifacts/m3/1-3-split-hosted-plugin-discovery-into-source-and-approval-layers.md]
- [Source: kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistry.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph explore "AthenaServiceRegistry AthenaHostedPluginRuntimeServices AthenaPluginRuntimeServices discoveryReport hostedPlugins"`
- `Get-Content kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `Get-Content kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `AthenaHostedPluginRegistry` with explicit host-owned lifecycle states and deterministic inventory descriptors.
- Added contribution-category inspection so hosted inventory now exposes approved plugin identity, attached extension points, and declared contribution categories.
- Extended runtime plugin services with lifecycle and inventory accessors while preserving runtime-owned contract filtering.
- Preserved inspection evidence across shutdown while removing active runtime command, view, and domain-semantics participation after shutdown.
- Verified the lifecycle and inventory surfaces through focused plugin-host and runtime tests plus a full Java `25` build.

### File List

- `_bmad-output/implementation-artifacts/m3/1-4-expose-hosted-plugin-lifecycle-and-inventory-inspection.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/plugins/plugin-host/build.gradle.kts`
- `kernel/plugins/plugin-host/README.md`
- `kernel/plugins/plugin-host/README.zh-CN.md`
- `kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistry.kt`
- `kernel/plugins/plugin-host/src/test/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistryTest.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`

### Change Log

- 2026-07-07: Added hosted lifecycle and inventory registry models, exposed runtime lifecycle inspection, and verified the Java `25` build.


