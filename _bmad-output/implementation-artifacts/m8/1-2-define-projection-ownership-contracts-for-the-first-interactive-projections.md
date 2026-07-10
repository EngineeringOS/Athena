---
baseline_commit: d8e1c6163b7edd8895e8b8fe182519f0fbf54b53
---

# Story 1.2: Define Projection Ownership Contracts For The First Interactive Projections

Status: done

## Story

As a platform engineer,  
I want each interactive projection to declare what it may display, edit, emit, and own,  
so that editability is governed explicitly instead of emerging accidentally from UI capability.

## FR Traceability

- FR-2: classify meaningful changes explicitly
- FR-4: refresh graphical state deterministically after an accepted change
- FR-7: define what each projection may display, edit, emit, and own
- FR-8: keep mutation semantics independent from the current renderer stack
- NFR-1: meaningful changes route through one Athena-owned mutation path
- NFR-2: canonical engineering meaning remains upstream of any renderer or editor client
- NFR-4: command intents, mutation outcomes, and rejection paths remain inspectable
- NFR-6: graph stack must not own command meaning or durable mutation semantics

## Acceptance Criteria

1. Given the current supported projections such as `cabinet` and `wiring`, when Athena marks them as interactive or non-interactive in M8, then each projection publishes an ownership contract for display, editability, command emission, transient state, and persisted projection metadata, and unsupported mutation attempts are rejected or snapped back on refresh.
2. Given projection ownership is reviewed against kernel boundaries, when dependency direction is checked, then the contracts stay renderer-neutral and domain-extensible, and they do not move engineering truth into layout or renderer-local state.

## Tasks / Subtasks

- [x] Publish one renderer-neutral projection ownership contract model at the projection boundary. (AC: 1, 2)
  - [x] Add a typed ownership contract model under [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt) or a closely related projection-owned file for:
    - interaction mode / interactive vs non-interactive state
    - display scope
    - semantic command emission declarations
    - projection command emission declarations
    - transient interaction ownership
    - persisted projection metadata ownership
  - [x] Keep the contract renderer-neutral and independent from runtime/frontend-only types.
  - [x] Add clean KDoc for every new public/core Kotlin type introduced by this story.
- [x] Attach ownership contracts to the first supported projection definitions. (AC: 1, 2)
  - [x] Extend the current typed view-definition boundary so each supported projection can publish its ownership contract without introducing a second registry.
  - [x] Mark the existing `cabinet` and `wiring` views explicitly as interactive or non-interactive through the electrical runtime plugin's typed view definitions.
  - [x] Keep the contribution path domain-extensible through existing plugin contracts rather than hardcoding contracts in the frontend.
- [x] Surface projection ownership contracts through runtime and IDE transport without widening into live graph mutation yet. (AC: 1, 2)
  - [x] Extend runtime projection session view payloads in [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) so supported views carry ownership contracts.
  - [x] Extend the Athena LSP projection payload in [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) and the downstream TypeScript bridge/adapter payload types so IDE clients can inspect the same contract.
  - [x] Preserve the current `switch-active-view` governed command behavior; do not implement graph gesture mutation handling, projection mutation execution, or renderer callbacks in Story `1.2`.
- [x] Make unsupported mutation ownership explicit at the current proof boundary. (AC: 1)
  - [x] Ensure non-interactive or undeclared projection edit paths remain explicitly unsupported instead of being inferred from current UI affordances.
  - [x] Preserve the current rejection/snapback posture: undeclared graphical mutation must still remain unowned and disposable until Epic `2.x` implements real edit paths.
- [x] Verify the ownership-contract layer with focused tests and regression-safe checks. (AC: 1, 2)
  - [x] Add focused contract tests under `kernel/projection-model`, electrical plugin tests, runtime projection session tests, and LSP projection request tests as needed.
  - [x] Verify the existing projection/session and command-runtime tests still pass under Java 25 after the ownership-contract changes.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

### Review Findings

- [x] [Review][Decision] Re-scope Story `1.2` so the review surface excludes unrelated mutation-result API refactors - Removed the carried Story `1.1` runtime mutation-wrapper files from this story's ownership record so Story `1.2` stays contract-first at the projection boundary.
- [x] [Review][Decision] Replace graph-gesture strings in the public ownership contract with Athena-owned neutral vocabulary - Replaced renderer-biased values with Athena-owned contract nouns such as `adjust-layout-placement`, `adjust-layout-grouping`, `navigate-view`, `inspect-selection`, `preview-related-elements`, `layout-placement`, and `layout-group-membership`.
- [x] [Review][Patch] Default the new `ownershipContract` field on public runtime/LSP view payloads so the extension stays additive. [kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt:23]
- [x] [Review][Patch] Add a focused GLSP adapter test for a partial or missing `ownershipContract` inside populated `supportedViews` entries. [integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs:107]
- [x] [Review][Patch] Assert the full ownership-contract payload in the LSP transport test instead of checking only `interactivity` and `projectionCommandIds`. [ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt:47]

## Dev Notes

### Story Intent

- This is the projection-ownership contract story for M8, not the graph-editing story.
- The point is to freeze explicit projection ownership before graph gestures begin emitting real semantic or projection mutation commands.
- The safest implementation is to extend the existing typed `ViewDefinition` / projection-session path rather than creating a second projection-policy registry beside it.
- Story `1.3` owns normalization of source-originated changes into the shared mutation-result path.
- Epic `2.x` owns graph gesture translation and the first live semantic and projection mutation paths.

### Architecture Guardrails

- Align to AD-35 by distinguishing semantic mutation, projection mutation, and transient interaction explicitly. Story `1.2` should publish ownership declarations that make those boundaries inspectable at the projection level. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-35]
- Align to AD-36 by keeping renderer gesture language out of durable projection ownership contracts. Contract nouns must stay Athena-owned and renderer-neutral. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-36]
- Align to AD-37 by preserving the current runtime-owned rejection and refresh posture. Story `1.2` may publish ownership semantics, but it must not widen into live graph mutation execution yet. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-37]
- Align to AD-40 by making every interactive projection publish an explicit ownership contract for display, editability, command emission, transient-only state, and persisted projection metadata. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-40]
- Align to AD-42 by keeping scope narrow and contract-first. Do not widen into the first real semantic or projection mutation execution path in this story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md#AD-42]
- Preserve inherited M7 rules:
  - `kernel/projection-model` remains the renderer-neutral projection boundary
  - layout/geometry remain view-scoped metadata, not engineering truth
  - `ide/lsp` remains the sole IDE semantic/projection entry point
  - graph adapters remain downstream and translation-only

### Technical Requirements

- The current typed projection boundary already exists in [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt).
- The current supported views already flow from typed plugin-owned `ViewDefinition`s through:
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
  - [`integrations/graph-glsp/src/athena-glsp-diagram-model.ts`](../../../integrations/graph-glsp/src/athena-glsp-diagram-model.ts)
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
- Story `1.2` should extend that typed path instead of inventing a second projection policy surface.
- Keep the Kotlin package root under `com.engineeringood.athena.projection`, `com.engineeringood.athena.runtime`, and `com.engineeringood.athena.ide.lsp` unless a deliberate, documented split is justified.
- Add KDoc for all public/core Kotlin classes touched by this story because the user explicitly requires clean KDoc on core Kotlin surfaces.

### Architecture Compliance

- The story is only successful if future M8 work can point to one clear projection ownership language:
  - one ownership contract model
  - one typed contribution path from plugins/view definitions
  - one runtime/LSP transport shape for IDE clients
- Prevent these failure modes:
  - projection ownership being inferred from current workbench buttons or SVG affordances
  - graph-specific ownership enums under `ide/theia-frontend` or `integrations/graph-glsp`
  - a second projection policy registry outside the existing typed `ViewDefinition` path
  - putting engineering truth into layout, geometry, or renderer-local state
  - Story `1.2` widening into live graph mutation execution or broad editor UX work

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the current M8 architecture:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`
- Reuse the existing Kotlin/JUnit and TypeScript test conventions already present in runtime, projection-model, and Theia/frontend modules.
- Do not add third-party dependencies just to express ownership contracts or payload typing.

### File Structure Requirements

- Expected update files:
  - [`kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt)
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)
  - [`integrations/graph-glsp/src/athena-glsp-diagram-model.ts`](../../../integrations/graph-glsp/src/athena-glsp-diagram-model.ts)
  - [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts) if transport mapping must stay type-complete
  - [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)
- Expected focused test updates:
  - [`kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`](../../../kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt)
  - [`extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt`](../../../extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt)
  - [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt)
  - [`ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt)
- Files whose current behavior and ownership must be preserved:
  - [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)
    - current pan/zoom/selection/overlay behavior remains transient UI behavior and must not gain durable mutation semantics in this story
  - [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-model.ts)
    - current view-model shaping remains presentation-only
  - [`integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`](../../../integrations/graph-glsp/src/athena-glsp-projection-adapter.ts)
    - adapter remains translation-only

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test :extensions:domain-electrical:test :kernel:runtime:test :ide:lsp:test"`
- Required proof checks:
  - typed view definitions now publish explicit ownership contracts
  - `cabinet` and `wiring` are marked explicitly as interactive or non-interactive
  - runtime projection session and LSP payloads carry the same ownership contract shape
  - no frontend, Theia, or GLSP-specific terms leak into the public ownership contract
  - current projection-session and view-switch behavior still pass unchanged
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`ProjectionModel.kt`](../../../kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt) currently models only derived projection documents, nodes, connections, and labels. It has no explicit ownership-contract layer yet.
- [`LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt) currently carries `ViewDefinition` metadata such as `layoutIntent`, `groupingRules`, `viewEmphasis`, and `description`, but no explicit ownership contract.
- [`AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently exposes supported views and one active projection snapshot, but no explicit per-view ownership contract.
- [`AthenaProjectionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt) currently exposes:
  - supported views
  - one governed command allowlist entry: `switch-active-view`
  - ready/unavailable projection state
  It does not yet expose ownership contracts.
- [`athena-graph-workbench-widget.tsx`](../../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx) currently implements pan/zoom, fit, selection, and active-view switching. Those interactions are still transient or already governed and must stay that way in Story `1.2`.

### Previous Story Intelligence

- Story `1.1` already proved:
  - runtime-owned mutation categories and outcomes
  - additive contract extension discipline matters
  - AI/plugin wrappers must not become parallel public result channels
- Practical carry-forward for Story `1.2`:
  - extend existing public shapes additively
  - prefer defaulted fields/computed properties over required constructor breaks
  - keep contract publication narrow and renderer-neutral

### Git Intelligence Summary

- Recent milestone baseline:
  - `d8e1c61 feat: complete m7 graphical workbench proof`
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
- Practical implication:
  - build on the existing M7 projection-session delivery path
  - keep ownership nouns in kernel/projection-model and typed transport seams
  - do not let the graph stack become the first place that defines projection editability

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+`
  - Eclipse Theia `1.73.1`

### Project Structure Notes

- `m8/` is the active milestone folder and Story `1.2` should become the second implementation artifact under it.
- The story should freeze ownership nouns now:
  - projection ownership contract
  - interactive / non-interactive projection
  - display scope
  - semantic command emission
  - projection command emission
  - transient interaction
  - persisted projection metadata
- The story should not yet implement the first live graph-originated semantic or projection mutation path; those belong to Stories `2.2` and `2.3`.

### References

- [Source: _bmad-output/planning-artifacts/epics-M8-2026-07-10.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-10-m8/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-10-m8/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m8/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m8/1-1-publish-unified-mutation-contracts-and-categories-in-runtime-owned-command-surfaces.md]
- [Source: kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModel.kt]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt]
- [Source: integrations/graph-glsp/src/athena-glsp-diagram-model.ts]
- [Source: integrations/graph-glsp/src/athena-glsp-projection-adapter.ts]
- [Source: ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts]
- [Source: ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx]

## Story Completion Status

- Status: done
- Completion note: Projection ownership contracts now publish through typed view definitions, runtime sessions, LSP transport, and downstream TypeScript adapters without widening into live graph mutation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M8 PRD, addendum, architecture spine, and epic breakdown review
- Story 1.1 completion record review
- current projection-model and layout-model review
- current electrical runtime view-definition review
- current runtime projection session review
- current LSP projection transport review
- current graph adapter and Theia bridge payload review
- recent commit history review
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:projection-model:test :extensions:domain-electrical:test :kernel:runtime:test :ide:lsp:test"`
- `yarn --cwd integrations/graph-glsp test`
- `yarn --cwd ide/theia-frontend test`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`

### Completion Notes List

- Published `ProjectionOwnershipContract` and `ProjectionInteractivity` beside `ViewDefinition` in `kernel/layout-model` so the ownership contract stays on the existing typed view-definition boundary without reversing current module dependencies.
- Marked `cabinet` as interactive and `wiring` as inspect-only in the electrical runtime plugin, with explicit display scope, command-emission, transient interaction, and persisted projection metadata ownership using Athena-neutral contract vocabulary.
- Extended runtime projection sessions, Athena LSP projection payloads, and downstream Theia/GLSP adapter types so supported views now expose the same ownership contract shape end to end.
- Preserved the current proof boundary: `switch-active-view` remains the only governed projection command, while undeclared graphical mutation stays unsupported and disposable.
- Defaulted `ownershipContract` on runtime and LSP view payloads, then added defensive ownership-contract normalization and focused stale/partial transport coverage in the GLSP adapter path.
- Verified the story with focused Java 25 Gradle tests, both affected TypeScript package tests, and a full sequential `gradlew test` regression run.

### File List

- _bmad-output/implementation-artifacts/m8/1-2-define-projection-ownership-contracts-for-the-first-interactive-projections.md
- _bmad-output/implementation-artifacts/m8/sprint-status.yaml
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt
- ide/lsp/build.gradle.kts
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.d.ts
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.d.ts.map
- ide/theia-frontend/lib/browser/athena-graph-workbench-model.js.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.d.ts.map
- ide/theia-frontend/lib/browser/athena-lsp-editor-bridge-service.js.map
- ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
- ide/theia-frontend/tsconfig.tsbuildinfo
- integrations/graph-glsp/lib/athena-glsp-diagram-model.d.ts
- integrations/graph-glsp/lib/athena-glsp-diagram-model.d.ts.map
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.d.ts.map
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.js
- integrations/graph-glsp/lib/athena-glsp-projection-adapter.js.map
- integrations/graph-glsp/src/athena-glsp-diagram-model.ts
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt
- kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt
- kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt

### Change Log

- 2026-07-10: Created M8 Story 1.2 with comprehensive context for projection ownership contracts, runtime/LSP transport shaping, and contract-first guardrails.
- 2026-07-10: Implemented typed projection ownership contracts, threaded them through runtime and IDE transport, and verified the result with focused Java 25, TypeScript, and full Gradle regression runs.
- 2026-07-11: Tightened Story 1.2 review scope to the projection-contract seam, replaced renderer-biased ownership values with Athena-neutral contract nouns, and strengthened additive transport defaults plus focused ownership-contract tests.

## Suggested Review Order

**Contract Vocabulary**

- Freeze neutral ownership nouns.
  [`ElectricalRuntimeDomainPlugin.kt:215`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt#L215)

- Keep runtime payload additive.
  [`AthenaRuntimeProjectionSession.kt:23`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt#L23)

- Keep LSP payload additive.
  [`AthenaProjectionProtocol.kt:48`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt#L48)

**Proof Coverage**

- Verify runtime-owned contract details.
  [`AthenaRuntimeProjectionSessionTest.kt:34`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt#L34)

- Verify full LSP transport shape.
  [`AthenaProjectionRequestTest.kt:20`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt#L20)

- Guard stale JS payloads.
  [`athena-glsp-adapter.test.mjs:202`](../../../integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs#L202)

- Keep workbench fixtures aligned.
  [`athena-graph-workbench-model.test.mjs:44`](../../../ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs#L44)
