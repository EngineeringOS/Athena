---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.8: Enforce Non-Sovereign Plugin Boundaries

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform owner,
I want runtime contracts to enforce that plugins remain non-sovereign,
so that extensions can add behavior without taking ownership of canonical semantics, project lifecycle, or runtime orchestration.

## Acceptance Criteria

1. Given runtime-hosted plugins can contribute domain semantics, commands, or views, when a plugin is evaluated for activation or runtime use, then Athena validates that the plugin attaches only through approved runtime-owned contracts, and the plugin cannot bypass lifecycle, command, validation, or canonical semantic boundaries.
2. Given a plugin attempts to redefine `Engineering IR`, own `Workspace` or `Project` lifecycle, or mutate semantic state outside the `Command Runtime`, when the runtime evaluates that plugin behavior, then Athena rejects or blocks the incompatible behavior through runtime contract enforcement, and the failure remains inspectable enough for platform owners to explain why the plugin was not allowed.
3. Given multiple plugins are active in the runtime, when their contributions are used together, then the runtime can still explain which invariants remain core-owned and non-overridable, and plugin participation does not relocate semantic authority into plugin-private models.
4. Given non-sovereign plugin enforcement is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and overreaching plugin behavior is detectably rejected or constrained, and the implementation proves that runtime growth in M1 does not compromise canonical ownership.

## Tasks / Subtasks

- [x] Add an explicit non-sovereign plugin enforcement model to the existing approval path. (AC: 1, 2, 3)
  - [x] Extend the current plugin validation/discovery pipeline with runtime-visible sovereignty diagnostics instead of creating a second approval system.
  - [x] Keep rejection reasons inspectable through runtime-owned reporting surfaces.
  - [x] Preserve deterministic approved inventory ordering and the single hosted discovery report.
- [x] Tighten runtime-hosted plugin contracts around canonical ownership boundaries. (AC: 1, 2, 3)
  - [x] Make the runtime explain which invariants remain core-owned and non-overridable.
  - [x] Ensure plugin contributions stay attached only through runtime-owned domain, command, and view seams.
  - [x] Keep semantic mutation routed only through `AthenaCommandRuntimeService` and preserve `Engineering IR` as the only canonical semantic authority.
- [x] Prove overreach rejection with negative fixtures and runtime/compiler tests. (AC: 1, 2, 3, 4)
  - [x] Add fixture plugins or manifests that represent overreaching behavior the current codebase can detect.
  - [x] Add compiler and runtime tests covering rejection, inspectable diagnostics, and compliant-plugin success cases.
  - [x] Preserve the existing electrical runtime plugin as a positive control.
- [x] Document and verify the M1 non-sovereign boundary. (AC: 4)
  - [x] Update the relevant compiler/runtime boundary notes to describe the stronger `FR-19` enforcement slice.
  - [x] Run sequential Java `25` verification for the affected modules and the full regression path.

## Dev Notes

### Story Intent

- Story `2.8` is the stronger `FR-19` enforcement slice that follows the runtime hosting work from Story `2.7`.
- The implementation must prove that runtime growth does not move semantic authority out of the core-owned path.
- This story is about enforcement and inspectability, not plugin breadth, UX expansion, or marketplace behavior.

### Architecture Guardrails

- `Athena Runtime` remains the sole owner of `Workspace`, `Project` activation, execution context, and service orchestration.
- `Engineering IR` remains the only canonical semantic authority; plugin-private semantic models cannot become durable truth.
- All semantic mutation must continue to enter through the `Command Runtime`.
- Runtime-hosted plugins may extend domain semantics, commands, and views only through runtime-owned typed contracts.
- The implementation must reject or constrain overreaching behavior without creating a parallel plugin architecture, private lifecycle owner, or alternate approval path.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep core Kotlin contracts and policy-bearing classes documented with KDoc.
- Reuse `AthenaPluginValidator`, `AthenaPluginDiscovery`, `AthenaPluginDiscoveryReport`, and `AthenaPluginRuntimeServices` as the primary enforcement path.
- Preserve the shared hosted inventory rule from Story `2.7`: runtime and compiler must continue to use one approved plugin inventory in the default path.
- Do not introduce importer/exporter/AI-skill execution, plugin install flows, hot reload, plugin-owned lifecycle, or plugin-private semantic state management in this story.

### Files Likely In Scope

- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginDiscovery.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `docs/compiler/m1-plugin-runtime-hosting-boundary.md`

### Current Code Notes

- `AthenaPluginValidator` already validates manifest completeness, compatibility, and extension-point/type alignment, but it does not yet model stronger runtime sovereignty enforcement.
- `AthenaPluginDiscovery` is already the single deterministic approval path and must remain the source of rejected-candidate diagnostics and approved inventory construction.
- `AthenaPluginRuntimeServices` already hosts domain, command, and view contributions through runtime-owned seams; `2.8` should strengthen inspectable boundary enforcement around those seams rather than widening them.
- `AthenaServiceRegistry` already feeds the hosted discovery report into compiler construction; that single-discovery path must not regress.
- `ElectricalRuntimeDomainPlugin` is the compliant positive-control plugin and should continue to activate successfully.

### Previous Story Intelligence

- Story `2.7` already proved one runtime-owned hosted plugin inventory, explicit runtime command/view contribution seams, and compiler reuse of the hosted discovery report.
- Story `2.7` deliberately stopped short of the full `FR-19` sovereignty enforcement slice; the architecture note states that Story `2.8` is the stronger boundary step.
- Stories `2.2` through `2.6` already established the command runtime, history, diff inspection, and incremental recompute path, so `2.8` must reuse those boundaries instead of re-specifying them.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Follow red-green-refactor. Add failing tests first for overreaching plugin rejection and inspectable diagnostics.
- Cover both approval-time behavior and runtime-facing inspection of the enforced invariants.
- Keep at least one compliant runtime plugin activation test to prove the new enforcement does not reject valid hosted plugins.
- Minimum verification commands for story completion:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### Project Structure Notes

- The story should extend the existing module split instead of adding a new plugin SDK or a parallel runtime package tree.
- Keep enforcement policy close to existing compiler/plugin/runtime seams so the code remains obvious to read and easy to audit.
- UX work is explicitly deferred in this phase; any inspector or CLI output changes must remain support surfaces for inspectability, not a new plugin-management product surface.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 2: Change And Extend Project Semantics Through One Runtime Path`
  - `Story 2.8: Enforce Non-Sovereign Plugin Boundaries`
  - `Story 2.7: Host Runtime Plugins For Domain Semantics, Commands, And Views`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-19: Keep Plugins Non-Sovereign`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-2`, `AD-3`, `AD-4`, `AD-9`
- `manifesto/docs/architecture/05-plugin.md`
  - `Extension Classes`
  - `Governance Boundary`
- `docs/compiler/m1-plugin-runtime-hosting-boundary.md`
  - `Purpose`
  - `Runtime-Owned Hosting Model`
  - `Non-Goals`

## Story Completion Status

- Status: review
- Story context created from Epic 2, the latest architecture spine, the M1 plugin boundary note, and Story `2.7` implementation learnings.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-06: created the `2.8` BMAD story artifact from the current Epic 2 backlog entry and aligned it to the existing runtime-hosted plugin boundary.
- Added manifest-owned non-sovereign policy metadata with explicit runtime command/view extension points and forbidden ownership-claim diagnostics.
- Tightened `AthenaHostedPluginRuntimeServices` so the hosted discovery report rejects plugins that declare or implement runtime command/view seams inconsistently.
- Exposed runtime-visible core-owned invariants and approved extension-point attachments for hosted plugin inspection.
- The first red phase intentionally failed because the new manifest/runtime enforcement API did not exist yet; implementation followed from those failures.
- Sequential Java 25 verification passed for targeted plugin-boundary tests, `:compiler:test`, `:runtime:test`, `:cli:test`, `:apps:compose-viewer:test`, full `test`, and full `build`.

### Completion Notes List

- Added explicit `RUNTIME_COMMANDS` and `RUNTIME_VIEWS` manifest attachments plus forbidden ownership claims so plugin authority boundaries are inspectable in core metadata.
- Kept compiler validation responsible for rejecting sovereign ownership claims while runtime hosting rejects mismatched runtime contributor declarations.
- Preserved one hosted discovery report shared by runtime and compiler, now filtered through runtime contract enforcement before inventory publication.
- Extended hosted plugin inspection with approved extension-point attachments and runtime-visible core-owned invariants.
- Updated the electrical runtime plugin to declare its runtime command/view contracts explicitly and kept it as the positive control across compiler, runtime, CLI, and Compose tests.

### File List

- `_bmad-output/implementation-artifacts/m1/2-8-enforce-non-sovereign-plugin-boundaries.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginManifestModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaPluginValidator.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginContractTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginDiscoveryTest.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaPluginTestFixtures.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `domain-electrical-runtime/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`
- `docs/compiler/m1-plugin-runtime-hosting-boundary.md`

### Change Log

- Created the BMAD story artifact for `2.8`, implemented non-sovereign plugin boundary enforcement, documented the stronger `FR-19` slice, and moved the story to `review`.
