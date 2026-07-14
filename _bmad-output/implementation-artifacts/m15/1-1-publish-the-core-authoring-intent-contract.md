---
baseline_commit: c04b3eb
---

# Story 1.1: Publish The Core Authoring Intent Contract

Status: done

## Story

As a platform engineer,  
I want Athena to define typed guided-authoring intents,  
so that palette, inspector, and graph gestures emit platform-owned requests rather than frontend-local mutation logic.

## FR Traceability

- FR-1: Athena can publish authoring intent as a first-class platform contract
- FR-2: Athena can preserve M8 as the only mutation authority for guided authoring
- FR-12: Athena can preview guided mutations before acceptance
- NFR-1: guided authoring introduces no second write path outside M8
- NFR-2: workbench surfaces remain consumers of platform-owned authoring contracts
- NFR-4: canonical semantic identity remains stronger than graph element ids, widget ids, or presentation occurrence ids
- NFR-5: preview and approval remain inspectable and deterministic

## Acceptance Criteria

1. Given M15 introduces guided authoring, when the contract is reviewed, then Athena defines typed authoring intents for at least component creation, property update, connection creation, and reveal.
2. Given workbench actions are compared across panel and graph surfaces, when authoring responsibilities are reviewed, then the shared contract belongs to platform modules rather than to Theia widget code or GLSP-local behavior.

## Tasks / Subtasks

- [x] Define the first platform-owned authoring intent vocabulary in dedicated kernel modules. (AC: 1, 2)
  - [x] Introduce `kernel/authoring-model` as the home for typed guided-authoring requests and preview-facing value contracts.
  - [x] Introduce narrow first intent types such as `CreateComponentIntent`, `UpdateComponentPropertiesIntent`, `ConnectPortsIntent`, and `RevealSubjectIntent`.
  - [x] Keep all public or core Kotlin types documented with clean KDoc.
- [x] Freeze the ownership boundary in code and docs. (AC: 1, 2)
  - [x] Make the contract comments explicit that authoring intent sits above M8 and is not a second mutation authority.
  - [x] Make the contract comments explicit that authoring intent is platform capability, not extension-local UI logic.
  - [x] Make the contract comments explicit that frontend widgets and graph tools are consumers of authoring intent, not owners of canonical semantic change.
- [x] Keep Story `1.1` narrow and foundational. (AC: 1, 2)
  - [x] Do not widen Story `1.1` into full runtime orchestration, Theia widgets, GLSP tools, or LSP request handlers yet.
  - [x] Do not widen Story `1.1` into full preview execution, commit flow, or inspection UI.
  - [x] Do not widen Story `1.1` into domain-specific component catalogs or Siemens-specific behavior.
- [x] Add focused tests and module documentation for the new contract layer. (AC: 1, 2)
  - [x] Add focused tests under the owning module to prove:
    - platform-owned authoring intent naming
    - separation between authoring intent and mutation execution
    - suitability for later palette, inspector, graph, form, template, AI, and API surfaces
  - [x] Add module README files in English and Chinese if a new kernel module is introduced.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the naming-and-boundary freeze for M15 guided semantic authoring.
- The success condition is not "Athena already has a working component panel."
- The success condition is "Athena now has one clean authoring contract that later panel, inspector, connect-flow, and AI or template surfaces can all target without breaking M8."
- Story `1.2` should define preview and acceptance contracts above the same authoring layer.
- Story `1.3` should publish shared runtime and transport seams that consume the contract.

### Architecture Guardrails

- Align to AD-84: M15 introduces authoring intent above M8, not a frontend mutation shortcut. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md#AD-84---M15-Introduces-Authoring-Intent-Above-M8-Not-A-Frontend-Mutation-Shortcut]
- Align to AD-85: authoring intent is platform capability, not extension-local UI logic. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md#AD-85---Authoring-Intent-Is-Platform-Capability-Not-Extension-Local-UI-Logic]
- Align to AD-87: one authoring intent may expand into multiple governed mutations. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md#AD-87---One-Authoring-Intent-May-Expand-Into-Multiple-Governed-Mutations]
- Preserve inherited AD-34, AD-39, AD-80, and AD-82: one mutation authority remains binding, cross-surface anchoring stays canonical-identity-first, M14 resolved component knowledge remains read-only, and DSL remains canonical serialization rather than the default human interface. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Current state already contains:
  - canonical `Engineering IR`
  - one mutation authority through M8
  - downstream presentation and workbench layers through M12 and M13
  - governed component knowledge through M14
- Story `1.1` must freeze authoring-intent naming and ownership before runtime, transport, and workbench surfaces expand.
- The contract should be shaped so later work can target it:
  - Theia component panel
  - Theia inspector
  - GLSP-originated connect flow
  - future forms, templates, AI, and API surfaces
- Explicitly avoid root-level names that freeze one UI implementation, such as:
  - `PaletteInsertRequest`
  - `GraphEdgeCreateAction`
  if they would hardcode one surface into the platform contract.

### Architecture Compliance

- The story is only successful if later M15 work can point to one clean ladder:
  - guided authoring surface
  - authoring intent
  - authoring runtime
  - M8 mutation authority
  - canonical `Engineering IR`
- Prevent these failure modes:
  - frontend widgets inventing their own mutation payloads
  - graph tools creating semantic state directly
  - intent naming collapsing around one workbench implementation
  - widening Story `1.1` into runtime, transport, or UI work too early

### Library / Framework Requirements

- Use the repo-approved stack already frozen by planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse current Kotlin and JUnit style already present in kernel modules.
- Do not add third-party libraries just to model the new contract.

### File Structure Requirements

- Expected update files likely include:
  - `kernel/authoring-model/build.gradle.kts`
  - `kernel/authoring-model/src/main/kotlin/...`
  - `kernel/authoring-model/src/test/kotlin/...`
  - `kernel/authoring-model/README.md`
  - `kernel/authoring-model/README.zh-CN.md`
  - `settings.gradle.kts`
- Explicit non-goals:
  - no `kernel/authoring-runtime` orchestration yet
  - no `ide/lsp` transport handlers yet
  - no Theia widget implementation yet
  - no GLSP connect tool implementation yet

### Testing Requirements

- Minimum verification commands should target the new module directly first.
- Required proof checks:
  - platform-owned authoring intent naming
  - stable separation between intent contracts and mutation execution
  - no concurrent Gradle build or test execution on Windows

### Current Code State To Preserve

- `Engineering IR` remains canonical authored engineering truth.
- M8 remains the only write authority.
- M14 remains the source of authorable component knowledge.
- M12 and M13 remain downstream workbench and presentation layers.
- Guided authoring remains a product layer above canonical truth, not a second truth model.

### Previous Milestone Intelligence

- M8 already proved one mutation authority above source and graph; Story `1.1` must not weaken that line by introducing UI-local write semantics.
- M14 already proved component knowledge; Story `1.1` should consume that future substrate rather than redefining it.
- M15 now changes product entry semantics, so Story `1.1` must be deliberate, platform-first, and UI-agnostic.

### Git Intelligence Summary

- Current baseline commit:
  - `c04b3eb feat(m14): add component knowledge foundation`
- Practical implication:
  - M15 begins after the completed M14 closeout state
  - first work should freeze authoring-intent naming and ownership before runtime and workbench authoring flows deepen

### References

- [Source: _bmad-output/planning-artifacts/epics-M15-2026-07-13.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-13-m15/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-13-m15/ARCHITECTURE-SPINE.md]
- [Source: docs/roadmap/athena-milestone-roadmap.md]

## Story Completion Status

- Status: done
- Completion note: implemented `:kernel:authoring-model`, added typed authoring intent contracts plus focused tests and bilingual module documentation, and verified with `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test"` followed by `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:authoring-model:test :kernel:component-model:test :kernel:part-model:test"`.

## File List

- `settings.gradle.kts`
- `kernel/authoring-model/build.gradle.kts`
- `kernel/authoring-model/README.md`
- `kernel/authoring-model/README.zh-CN.md`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt`
- `kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringModelModuleMarker.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringIntentContractTest.kt`
- `kernel/authoring-model/src/test/kotlin/com/engineeringood/athena/authoring/AuthoringModelModuleMarkerTest.kt`

## Change Log

- 2026-07-13: implemented the M15 `authoring-model` foundation with platform-owned guided authoring intents, focused contract tests, and bilingual module documentation.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M15 Story `1.1` authoring-model contract and test-first implementation
- Sequential Java 25 Gradle verification for `:kernel:authoring-model:test`
- Sequential Java 25 regression verification for `:kernel:authoring-model:test :kernel:component-model:test :kernel:part-model:test`

### Completion Notes

- Added the new `:kernel:authoring-model` module and wired it into `settings.gradle.kts`.
- Published typed platform-owned authoring intents for component creation, property update, port connection, and reveal.
- Kept the story narrow: no authoring runtime, LSP, Theia widget, or GLSP implementation was introduced.
- Added English and Chinese module READMEs and verified repository encoding rules after the Chinese documentation update.
