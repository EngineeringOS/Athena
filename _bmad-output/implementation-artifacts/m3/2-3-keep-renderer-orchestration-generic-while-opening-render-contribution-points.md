---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.3: Keep Renderer Orchestration Generic While Opening Render Contribution Points

Status: done

## Story

As a platform engineer,
I want renderer orchestration to stay generic while domains contribute presentation behavior through approved contracts,
so that rendering remains downstream without kernel modules importing domain plugin classes.

## Acceptance Criteria

1. Given kernel renderer modules already consume canonical semantics and downstream projection contracts, when M3 render contribution support is introduced, then Athena keeps renderer and backend orchestration kernel-owned and generic, and domains contribute presentation behavior only through typed render contribution contracts.
2. Given domain rendering is now externalized, when renderer boundaries are reviewed, then kernel renderer modules do not import proof-domain implementation classes directly, and domains do not become backend hosts or semantic authorities.
3. Given projection layers proven in M2 remain downstream consequences, when render contribution points are implemented, then domain presentation behavior remains downstream of canonical semantics plus layout and geometry inputs, and domain plugins do not replace layout or geometry ownership with plugin-private visual truth.
4. Given renderer contribution points are implemented, when the standard Java `25` build and rendering regression checks are executed, then the workspace builds successfully and kernel render orchestration remains generic while approved plugins can contribute domain presentation behavior, and Epic 2 preserves the renderer-side manifesto boundary.

## Tasks / Subtasks

- [x] Expose a generic compiler-facing render contribution result. (AC: 1, 2, 3, 4)
  - [x] Add compiler-visible models that attribute active render contributions by plugin id and declared contribution id.
  - [x] Keep backend preparation and backend emission kernel-owned while making active render contributions inspectable.
  - [x] Preserve existing rendering outputs and pass ordering for current M0 to M2 examples.
- [x] Consume typed render contribution contracts in the backend pipeline without importing proof-domain classes. (AC: 1, 2, 3, 4)
  - [x] Collect approved render contributions from hosted plugins in deterministic approved-plugin order.
  - [x] Select applicable contributions generically from the downstream rendering context such as `viewId` and renderer target.
  - [x] Keep `SvgRenderModelDeriver` and `SvgRenderer` domain-agnostic.
- [x] Surface render contribution inspection through runtime-owned and compiler-owned seams. (AC: 1, 2, 4)
  - [x] Expose runtime-facing render contribution inspection alongside existing hosted plugin inventory and view-definition inspection.
  - [x] Make backend emission or compilation output report which approved render contributions were active for the emitted renderer target.
  - [x] Ensure the contribution path remains inspectable enough for debugging and future proof domains.
- [x] Prove the generic render boundary with regression coverage and sequential Java `25` verification. (AC: 1, 2, 3, 4)
  - [x] Add tests that show electrical render contributions are visible through generic contracts without kernel imports of proof-domain implementation classes.
  - [x] Add tests that show render contribution selection stays downstream of geometry/view choice and does not alter semantic authority.
  - [x] Verify plugin-api, plugin-host, compiler, runtime, electrical-extension, and full-build regressions sequentially on Java `25`.

## Dev Notes

### Story Intent

- Story `2.3` should not invent a plugin-private renderer or a domain-authored visual truth source.
- The minimal real proof is that typed render contribution contracts are consumed and surfaced by the generic backend pipeline rather than existing as unused metadata.
- View definitions, layout, geometry, render-model derivation, and SVG emission remain downstream kernel-owned stages.

### Implementation Direction

- Prefer contribution attribution over domain-specific rendering logic:
  - keep SVG generation generic
  - let plugins declare render intent through `AthenaRenderContribution`
  - let compiler/runtime report which contributions are active for a `viewId` + renderer target pair
- Preserve deterministic ordering:
  - approved plugin order first
  - plugin-declared render contribution order second
- The likely kernel seams are:
  - compiler rendering result models
  - backend emission summaries
  - runtime plugin inspection services
  - hosted inventory tests

### Previous Story Intelligence

- Story `2.2` already established the pattern of keeping backward-compatible flattened results while adding inspectable attribution metadata.
- Story `1.4` and `1.6` already proved hosted inventory inspection and deterministic stage participation; this story should reuse those patterns rather than creating a parallel inspection model.
- Do not move proof-domain rendering rules into `:kernel:svg-renderer` or `:kernel:compiler`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-23-keep-renderer-orchestration-generic-while-opening-render-contribution-points]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md#ad-7---kernel-renderer-contracts-stay-generic-domains-contribute-only-domain-presentation]
- [Source: kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaContributionModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `codegraph explore "Story 2.3 render contribution renderer orchestration AthenaRenderContribution AthenaRenderContributor AthenaRendererPlugin SvgRenderer SvgRenderModelDeriver runtime views view definitions ElectricalRuntimeDomainPlugin render contributions"`
- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `Get-Content _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md`
- `Get-Content kernel/plugins/plugin-api/src/main/kotlin/com/engineeringood/athena/plugin/AthenaContributionModel.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `Get-Content kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `Get-Content kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaHostedPluginRegistry.kt`
- `Get-Content kernel/plugins/plugin-host/src/main/kotlin/com/engineeringood/athena/plugin/host/AthenaPluginApproval.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `Get-Content kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `Get-Content kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-api:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:plugins:plugin-host:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `CompilerRenderContributionAttribution` and extended `CompilerRenderingSuccess` so the compiler now reports the active render contribution set for the emitted `viewId` and renderer target without changing backend ownership.
- Taught `AthenaCompiler` to collect approved render contribution declarations in deterministic plugin order and to select applicable contributions generically from `viewId` plus renderer target during backend emission.
- Exposed runtime-facing render contribution inspection through `AthenaPluginRuntimeServices.renderContributions()` while keeping hosted inventory and renderer orchestration generic.
- Kept `SvgRenderModelDeriver` and `SvgRenderer` domain-agnostic; the electrical proof plugin's existing typed render contribution metadata is now consumed rather than duplicated or moved into kernel renderer code.
- Added compiler and runtime regression coverage proving supported render contribution declarations remain generic and that emitted rendering activates only the matching cabinet contribution for the default SVG render path.
- Verified the story with sequential Java `25` runs for compiler, runtime, plugin-api, plugin-host, electrical extension, and a full workspace build.

### File List

- `_bmad-output/implementation-artifacts/m3/2-3-keep-renderer-orchestration-generic-while-opening-render-contribution-points.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`

### Change Log

- 2026-07-07: Created Story 2.3 and implemented generic compiler/runtime render contribution attribution without moving proof-domain rendering logic into kernel renderer code.
