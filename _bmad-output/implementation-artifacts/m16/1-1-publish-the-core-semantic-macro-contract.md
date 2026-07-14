---
baseline_commit: 0f4912953312065542f67eec12f3c1fc4133f61c
---

# Story 1.1: Publish The Core Semantic Macro Contract

Status: done

## Story

As a platform engineer,
I want Athena to define a Semantic Macro as a first-class reusable assembly contract,
so that governed engineering reuse has a stable identity above package metadata and below runtime expansion.

## FR Traceability

- FR-1: Athena can define Semantic Macro as a governed reusable assembly contract
- FR-2: Athena can publish reuse contracts as dedicated platform models
- FR-11: Athena can reuse existing M5 governance instead of inventing a new package system
- NFR-1: M16 introduces no second mutation path outside M8
- NFR-2: M16 introduces no second package resolver or second lockfile outside M5
- NFR-3: Semantic Macro truth remains semantic-first and may not collapse into graphics truth
- NFR-6: Workbench surfaces remain consumers of platform-owned reuse services

## Acceptance Criteria

1. Given M16 introduces semantic reuse, when the contract is reviewed, then Athena defines Semantic Macro identity, instantiation identity, parameter schema, preview, accepted-expansion, and origin contracts in dedicated platform models.
2. Given Semantic Macro ownership is inspected, when architecture owners compare it with package and UI layers, then the contract remains distinct from M5 package contracts and from frontend-local widget models.

## Tasks / Subtasks

- [x] Introduce the first dedicated reuse-contract module. (AC: 1, 2)
  - [x] Add `:kernel:reuse-model` to `settings.gradle.kts`.
  - [x] Create `kernel/reuse-model/build.gradle.kts` using the same minimal Kotlin JVM module pattern used by `:kernel:authoring-model`, `:kernel:component-model`, and `:kernel:repository-model`.
  - [x] Add a narrow module marker type such as `ReuseModelModuleMarker` so bootstrap and documentation flows can identify the module cleanly.
- [x] Publish the first Semantic Macro identity and contract vocabulary. (AC: 1, 2)
  - [x] Define stable ids for Semantic Macro and instantiation identity using the repo's existing `@JvmInline` value-class style.
  - [x] Define explicit parameter-schema contracts that stay surface-agnostic and transport-friendly.
  - [x] Define accepted-expansion and origin-traceability contracts at the semantic-contract level only.
  - [x] Keep template payload contracts out of Story `1.1`; those belong to Story `1.2`.
- [x] Freeze the ownership boundary in code and docs. (AC: 1, 2)
  - [x] Make the contract comments explicit that Semantic Macro identity is distinct from package identity.
  - [x] Make the contract comments explicit that Semantic Macro truth is semantic-first and not graphics-first.
  - [x] Make the contract comments explicit that reuse-model contracts do not create a second mutation path and do not own runtime orchestration, catalog behavior, or renderer state.
- [x] Keep Story `1.1` narrow and foundational. (AC: 1, 2)
  - [x] Do not introduce `:kernel:template-model` yet.
  - [x] Do not implement runtime-owned catalog loading, parameter validation execution, preview execution, or M8 handoff yet.
  - [x] Do not implement Theia widgets, LSP request handlers, or domain-specific electrical macro packs yet.
  - [x] Do not widen the story into marketplace, federation, update, replace, or rebind workflows.
- [x] Add focused tests and module documentation. (AC: 1, 2)
  - [x] Add contract tests that prove Semantic Macro ids, instantiation ids, parameter-schema contracts, and origin contracts stay distinct from package ids and frontend-local state.
  - [x] Add English and Chinese module READMEs following the existing `kernel/authoring-model` documentation pattern.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run Gradle tasks concurrently.

## Dev Notes

### Story Intent

- Story `1.1` is the naming-and-boundary freeze for M16 semantic reuse.
- The success condition is not "Athena already has a working macro catalog."
- The success condition is "Athena now has one clean semantic reuse contract that later template, catalog, preview, acceptance, and inspection flows can target without breaking M5 or M8."
- Story `1.2` should define reusable component-template and connection-template payload contracts above this layer.
- Story `1.3` should publish shared runtime and transport seams that consume this contract.

### Architecture Guardrails

- Align to AD-94: M16 introduces a semantic reuse layer above package governance and above guided authoring. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md#AD-94---M16-Introduces-A-Semantic-Reuse-Layer-Above-Package-Governance-And-Above-Guided-Authoring]
- Align to AD-95: Semantic Macro definitions resolve through the existing package graph. Story `1.1` must keep package and macro identity separate. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md#AD-95---Semantic-Macro-Definitions-Resolve-Through-The-Existing-Package-Graph]
- Align to AD-96: Semantic Macro contracts describe engineering assemblies, not graphics. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md#AD-96---Semantic-Macro-Contracts-Describe-Engineering-Assemblies-Not-Graphics]
- Align to AD-100: origin traceability is a first-class canonical derivative of accepted expansion. Story `1.1` only defines the contract for that fact set; it does not implement acceptance yet. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md#AD-100---Origin-Traceability-Is-A-First-Class-Canonical-Derivative-Of-Accepted-Expansion]
- Preserve inherited AD-13, AD-16, and AD-34: repository/package contracts remain in `kernel/repository-model`, `athena.lock` remains derived reproducibility state, and M8 remains the only mutation authority. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Current Code State To Preserve

- `:kernel:authoring-model` already defines the M15 guided-authoring contract through:
  - `AuthoringIntentModels.kt`
  - `AuthoringPreviewModels.kt`
  - focused contract tests
  - bilingual module documentation
- `AthenaAuthoringSessionRuntimeService.kt` in `:kernel:runtime` already consumes the existing authoring contract and expands it into runtime-owned preview state.
- `AthenaAuthoringProtocol.kt` in `:ide:lsp` already transports the existing authoring contract across the LSP boundary.
- `AthenaAuthoringSourceEditProtocol.kt` already turns accepted M15 create-component preview state into source-backed edits.
- There are currently no indexed `SemanticMacro` symbols and no `kernel/template-model` module in the repository. Story `1.1` is responsible for starting that missing contract layer cleanly.

### Technical Requirements

- Follow the existing contract-module pattern used by `:kernel:authoring-model`:
  - `@JvmInline` ids for stable contract identities
  - small sealed interfaces and data classes
  - KDoc on all public or core Kotlin contract types
  - narrow, platform-owned terminology
- Keep Semantic Macro contract names aligned to the architecture spine and addendum. Good candidates include:
  - `SemanticMacroId`
  - `SemanticMacroInstantiationId`
  - `SemanticMacroParameterId` or `SemanticMacroParameterName`
  - `SemanticMacroContract`
  - `SemanticMacroParameterDefinition`
  - `SemanticMacroAcceptedExpansion`
  - `ExpansionOrigin`
  - `ExpansionMembership`
- Avoid names that imply the wrong ownership model:
  - `MacroPackageCore`
  - `SvgMacroTruth`
  - `BlockPasteTemplate`
  - `GraphicMacroDefinition`
- Story `1.1` should define semantic contract vocabulary only. It should not hardcode catalog grouping, preview UI wording, or renderer-specific fields.

### Architecture Compliance

- The story is only successful if later M16 work can point to one clean ladder:
  - reuse catalog or other entry surface
  - reuse-model contract
  - template-model payloads
  - runtime reuse services
  - M8 mutation authority
  - canonical `Engineering IR`
- Prevent these failure modes:
  - package ids quietly becoming semantic macro ids
  - renderer or widget fields appearing inside core semantic reuse contracts
  - Story `1.1` absorbing `template-model` payload responsibilities too early
  - Story `1.1` leaking runtime execution details into core contracts

### Library / Framework Requirements

- Use the repo-approved stack already frozen by the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not add third-party libraries to model Semantic Macro contracts.
- Reuse current Kotlin and test style already present in:
  - `kernel/authoring-model`
  - `kernel/component-model`
  - `kernel/repository-model`

### File Structure Requirements

- Expected update files likely include:
  - `settings.gradle.kts`
  - `kernel/reuse-model/build.gradle.kts`
  - `kernel/reuse-model/README.md`
  - `kernel/reuse-model/README.zh-CN.md`
  - `kernel/reuse-model/src/main/kotlin/com/engineeringood/athena/reuse/...`
  - `kernel/reuse-model/src/test/kotlin/com/engineeringood/athena/reuse/...`
- Follow the same documentation pattern currently used in `kernel/authoring-model/README.md` and `README.zh-CN.md`.
- Follow the same narrow module-marker pattern currently used by `AuthoringModelModuleMarker`.

### Testing Requirements

- Minimum verification should target the new module directly first:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:reuse-model:test"`
- Recommended regression after the module tests pass:
  - `cmd /c "call java25 && .\\gradlew.bat --no-daemon --console=plain :kernel:reuse-model:test :kernel:authoring-model:test :kernel:runtime:test"`
- Keep Gradle verification sequential on Windows. Do not overlap Gradle invocations.
- Run `powershell -ExecutionPolicy Bypass -File .\\tools\\encoding-audit.ps1` after updating bilingual docs.

### Explicit Non-Goals

- No `:kernel:template-model` payload implementation yet.
- No runtime-owned catalog loading yet.
- No parameter-validation execution yet.
- No preview-execution or accepted-expansion orchestration yet.
- No Theia workbench implementation yet.
- No LSP transport handlers for Semantic Macro yet.
- No electrical macro pack content yet.

### Previous Milestone Intelligence

- M15 already established the contract-module pattern in `:kernel:authoring-model`.
- The most robust path for M16 Story `1.1` is to mirror that contract style instead of inventing a completely different naming or packaging pattern.
- M15 also proved that runtime and LSP already consume contract-only modules cleanly; M16 should preserve that split.

### References

- [Source: _bmad-output/planning-artifacts/epics-M16-2026-07-14.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-14-m16/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-14-m16/ARCHITECTURE-SPINE.md]
- [Source: kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt]
- [Source: kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt]
- [Source: kernel/authoring-model/README.md]

## Story Completion Status

- Status: done
- Completion note: `:kernel:reuse-model` now publishes the first Semantic Macro contract, preview, and accepted-expansion traceability models with focused tests and bilingual module docs.
